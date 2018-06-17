package org.qualiton.eventsourcing.postgres

import cats.data.EitherT
import cats.effect.Effect
import com.zaxxer.hikari.HikariDataSource
import org.qualiton.eventsourcing.aggregate.snapshotting.{Snapshot, SnapshotStore}

class PostgresSnapshotStore[F[_] : Effect, State](dataSource: HikariDataSource) extends SnapshotStore[F, State] {

  println(dataSource)

  override def load(aggregateId: String): EitherT[F, Throwable, Option[Snapshot[State]]] = ???

  override def save(aggregateId: String, snapshot: Snapshot[State]): EitherT[F, Throwable, Unit] = ???

}
