package org.qualiton.eventsourcing.eventstream

trait OffsetStore[F[_]] {
  def load(offsetId: String): F[Long]

  def save(offsetId: String, value: Long): F[Unit]
}
