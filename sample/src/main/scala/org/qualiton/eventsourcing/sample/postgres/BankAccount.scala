package org.qualiton.eventsourcing.sample.postgres

import cats.Monad
import cats.data.EitherT
import io.circe.generic.auto._
import org.qualiton.eventsourcing.aggregate.{Aggregate, AggregateState, VersionedState}
import org.qualiton.eventsourcing.journal.JournalWithOptimisticLocking
import org.qualiton.eventsourcing.serialization.{EventSerializer, SerializedEvent}

sealed trait BankAccountState extends AggregateState[BankAccountEvent, BankAccountState]

case object Empty extends BankAccountState {
  val eventHandler = EventHandler {
    case BankAccountOpened(id, name, balance) => BankAccount(id, name, balance)
  }
}

case class BankAccount(id: Int, name: String, balance: Int) extends BankAccountState {
  val eventHandler = EventHandler {
    case MoneyWithdrawn(_, amount) => copy(balance = balance - amount)
  }
}

sealed trait BankAccountEvent

case class BankAccountOpened(id: Int, name: String, balance: Int) extends BankAccountEvent

case class MoneyWithdrawn(id: Int, amount: Int) extends BankAccountEvent

class BankAccountEventSerializer extends EventSerializer[BankAccountEvent] {

  import org.qualiton.eventsourcing.json.JsonEncoding._

  def serialize(event: BankAccountEvent): SerializedEvent[BankAccountEvent] = event match {
    case event: BankAccountOpened => SerializedEvent("BankAccountOpened.V1", encode(event))
    case event: MoneyWithdrawn => SerializedEvent("MoneyWithdrawn.V1", encode(event))
  }

  def deserialize(manifest: String, data: String): BankAccountEvent = manifest match {
    case "BankAccountOpened.V1" => decode[BankAccountOpened](data)
    case "MoneyWithdrawn.V1" => decode[MoneyWithdrawn](data)
  }
}

case class BankAccountAggregate[F[_] : Monad](id: Int, journal: JournalWithOptimisticLocking[F, BankAccountEvent])
  extends Aggregate[F, BankAccountEvent, BankAccountState](journal) {

  val aggregateId = s"bank-account-$id"
  val initialState = Empty

  def onEvent(state: BankAccountState, event: BankAccountEvent): BankAccountState = state.eventHandler(event)

  private def recoverBankAccount(): EitherT[F, Throwable, (BankAccount, VersionedState[BankAccountState])] =
    recover flatMap {
      case state@VersionedState(bankAccount: BankAccount, _) => EitherT.pure[F, Throwable]((bankAccount, state))
      case _ => EitherT.leftT[F, (BankAccount, VersionedState[BankAccountState])](new Throwable(s"Bank account with id '$id' not found"))
    }

  def open(name: String, balance: Int): EitherT[F, Throwable, BankAccount] =
    for {
      versionedState <- recover
      bankAccount <- versionedState.state match {
        case Empty => persist(versionedState, BankAccountOpened(id, name, balance)).map(_.asInstanceOf[BankAccount])
        case _ => EitherT.leftT[F, BankAccount](new Throwable(s"Bank account with id '$id' already exists"))
      }
    } yield bankAccount

  def withdraw(amount: Int): EitherT[F, Throwable, Int] = retry(1) {
    for {
      accountState <- recoverBankAccount
      (bankAccount, versionedState) = accountState
      updatedBankAccount <-
        if (bankAccount.balance >= amount) {
          persist(versionedState, MoneyWithdrawn(id, amount)).map(_.asInstanceOf[BankAccount])
        } else EitherT.leftT[F, BankAccount](new Throwable(s"Not enough funds in account with id '$id'"))
    } yield updatedBankAccount.balance
  }
}

