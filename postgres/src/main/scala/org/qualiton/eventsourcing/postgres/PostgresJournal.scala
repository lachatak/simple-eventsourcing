package org.qualiton.eventsourcing.postgres

import java.sql.SQLException

import cats.Monad
import cats.data.EitherT
import cats.effect.Effect
import cats.instances.list._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import com.zaxxer.hikari.HikariDataSource
import doobie.free.connection.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import fs2.Stream
import org.qualiton.eventsourcing.Result
import org.qualiton.eventsourcing.journal.{EventWithOffset, JournalWithOptimisticLocking, OptimisticLockException}
import org.qualiton.eventsourcing.postgres.PostgresJournal.{insert, select}
import org.qualiton.eventsourcing.serialization.EventSerializer

class PostgresJournal[F[_] : Effect : Monad, Event](dataSource: HikariDataSource)(implicit serializer: EventSerializer[Event])
  extends JournalWithOptimisticLocking[F, Event] {

  private val transactor: Transactor[F] = HikariTransactor(dataSource)

  def read(aggregateId: String, offset: Long = 0): Result[F, List[EventWithOffset[Event]]] =
    EitherT[F, Throwable, List[EventWithOffset[Event]]] {
      select(aggregateId, offset)
        .stream
        .map { jp =>
          val deserialized = serializer.deserialize(jp.manifest, jp.data)
          EventWithOffset[Event](deserialized, jp.aggregateOffset)
        }
        .transact(transactor)
        .compile
        .toList
        .attempt
    }

  def write(aggregateId: String, lastSeenOffset: Long, events: Seq[Event]): Result[F, Unit] =
    EitherT[F, Throwable, Int](
      Stream
        .emits(events)
        .zipWithIndex
        .map { eventWithIndex =>
          val (event, index) = eventWithIndex
          val serialized = serializer.serialize(event)
          JournalPostgresPersistence(aggregateId, lastSeenOffset + index + 1, serialized.manifest, serialized.data)
        }
        .covary[F]
        .compile
        .toList
        .flatMap(l => insert(l).attempt.transact(transactor)))
      .bimap({
        case ex: SQLException if ex.getSQLState == "23505" => OptimisticLockException(s"Unexpected version $lastSeenOffset for aggregate '$aggregateId")
        case t: Throwable => t
      }, _ => ())

}

object PostgresJournal {

  def select(aggregateId: String, offset: Long): Query0[JournalPostgresPersistence] =
    sql"SELECT aggregate_id, aggregate_offset, manifest, data FROM journal WHERE aggregate_id = $aggregateId AND aggregate_offset > $offset".query[JournalPostgresPersistence]

  def insert(journalPostgresPersistenceList: List[JournalPostgresPersistence]): ConnectionIO[Int] = {
    val sql = "INSERT INTO journal(aggregate_id, aggregate_offset, manifest, data) VALUES (?, ?, ?, TO_JSON(?::json))"
    Update[JournalPostgresPersistence](sql).updateMany(journalPostgresPersistenceList)
  }
}
