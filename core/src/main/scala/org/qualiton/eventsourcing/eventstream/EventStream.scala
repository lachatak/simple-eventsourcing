package org.qualiton.eventsourcing.eventstream

trait EventStream[F[_], Event] {
  def subscribe(f: EventEnvelope[Event] => F[Unit], offset: Long = 0): F[Unit]
}
