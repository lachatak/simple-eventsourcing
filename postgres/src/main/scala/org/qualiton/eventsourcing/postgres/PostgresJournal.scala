package org.qualiton.eventsourcing.postgres

import java.sql.SQLException

import cats.data.EitherT
import cats.effect.Async
import cats.instances.list._
import com.zaxxer.hikari.HikariDataSource
import doobie.free.connection.ConnectionIO
import doobie.hikari.HikariTransactor
import doobie.implicits._
import doobie.util.query.Query0
import doobie.util.transactor.Transactor
import doobie.util.update.Update
import org.qualiton.eventsourcing.journal.{EventWithOffset, JournalWithOptimisticLocking, OptimisticLockException}
import org.qualiton.eventsourcing.postgres.PostgresJournal.{readQuery, writeUpdate}
import org.qualiton.eventsourcing.serialization.EventSerializer

class PostgresJournal[F[_] : Async, Event](dataSource: HikariDataSource)(implicit serializer: EventSerializer[Event])
  extends JournalWithOptimisticLocking[F, Event] {

  private val transactor: Transactor[F] = HikariTransactor(dataSource)

  def read(aggregateId: String, offset: Long = 0): EitherT[F, Throwable, Seq[EventWithOffset[Event]]] =
    EitherT(readQuery(aggregateId, offset)
      .stream
      .map { jp =>
        val event = serializer.deserialize(jp.manifest, jp.data)
        EventWithOffset(event, jp.aggregateOffset)
      }
      .compile
      .toList
      .transact(transactor)
      .attemptSql)
      .bimap(_.asInstanceOf[Throwable], _.toSeq)

  def write(aggregateId: String, lastSeenOffset: Long, events: Seq[Event]): EitherT[F, Throwable, Unit] = {

    val persistenceList = events
      .zipWithIndex
      .collect { case (event, index) =>
        val serializedEvent = serializer.serialize(event)
        JournalPostgresPersistence(aggregateId, lastSeenOffset + index + 1, serializedEvent.manifest, serializedEvent.data)
      }

    EitherT(writeUpdate(persistenceList).transact(transactor).attemptSql)
      .leftMap {
        case ex: SQLException if ex.getSQLState == "23505" => OptimisticLockException(s"Unexpected version $lastSeenOffset for aggregate '$aggregateId")
        case t: Throwable => t
      }
      .map(_ => ())
  }
}

object PostgresJournal {

  def readQuery(aggregateId: String, offset: Long): Query0[JournalPostgresPersistence] =
    sql"SELECT aggregate_id, aggregate_offset, manifest, data FROM journal WHERE aggregate_id = $aggregateId AND aggregate_offset > $offset".query[JournalPostgresPersistence]

  def writeUpdate(journalPostgresPersistenceList: Seq[JournalPostgresPersistence]): ConnectionIO[Int] = {
    val sql = "INSERT INTO journal(aggregate_id, aggregate_offset, manifest, data) VALUES (?, ?, ?, TO_JSON(?::json))"
    Update[JournalPostgresPersistence](sql).updateMany(journalPostgresPersistenceList.toList)
  }
}
