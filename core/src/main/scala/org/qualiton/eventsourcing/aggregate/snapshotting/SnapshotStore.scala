package org.qualiton.eventsourcing.aggregate.snapshotting

import cats.data.EitherT

trait SnapshotStore[F[_], State] {

  def load(aggregateId: String): EitherT[F, Throwable, Option[Snapshot[State]]]

  def save(aggregateId: String, snapshot: Snapshot[State]): EitherT[F, Throwable, Unit]
}
