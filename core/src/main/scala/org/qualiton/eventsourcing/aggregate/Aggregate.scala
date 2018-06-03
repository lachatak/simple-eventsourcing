package org.qualiton.eventsourcing.aggregate

import cats.Monad
import cats.data.EitherT
import org.qualiton.eventsourcing.journal.{JournalWithOptimisticLocking, OptimisticLockException}

abstract class Aggregate[F[_] : Monad, Event, State](journal: JournalWithOptimisticLocking[F, Event]) {

  val aggregateId: String
  protected val initialState: State

  def onEvent(state: State, event: Event): State

  protected def retry[T](n: Int)(f: => EitherT[F, Throwable, T]): EitherT[F, Throwable, T] =
    f recoverWith {
      case _: OptimisticLockException if n > 0 => retry(n - 1)(f)
    }

  def recover(): EitherT[F, Throwable, VersionedState[State]] =
    for {
      events <- journal.read(aggregateId)
      versionedState = events.foldLeft(VersionedState(initialState)) { case (versionedState, eventWithOffset) =>
        VersionedState(onEvent(versionedState.state, eventWithOffset.event), eventWithOffset.offset)
      }
    } yield versionedState

  def persist(currentVersionedState: VersionedState[State], events: Event*): EitherT[F, Throwable, State] = {
    for {
      _ <- journal.write(aggregateId, currentVersionedState.version, events)
      newState = events.foldLeft(currentVersionedState.state)(onEvent)
    } yield newState
  }
}
