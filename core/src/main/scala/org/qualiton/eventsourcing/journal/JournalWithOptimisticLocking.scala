package org.qualiton.eventsourcing.journal

import cats.data.EitherT

trait JournalWithOptimisticLocking[F[_], Event] {

  def read(aggregateId: String, offset: Long = 0): EitherT[F, Throwable, Seq[EventWithOffset[Event]]]

  def write(aggregateId: String, lastSeenOffset: Long, events: Seq[Event]): EitherT[F, Throwable, Unit]
}
