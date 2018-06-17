package org.qualiton.eventsourcing.journal

import org.qualiton.eventsourcing.Result

trait JournalWithOptimisticLocking[F[_], Event] {

  def read(aggregateId: String, offset: Long = 0): Result[F, List[EventWithOffset[Event]]]

  def write(aggregateId: String, lastSeenOffset: Long, events: Seq[Event]): Result[F, Unit]
}
