package org.qualiton.eventsourcing.postgres

import cats.effect.Effect
import com.zaxxer.hikari.HikariDataSource
import org.qualiton.eventsourcing.eventstream.OffsetStore

class PostgresOffsetStore[F[_] : Effect](dataSource: HikariDataSource) extends OffsetStore[F] {

  println(dataSource)

  override def load(offsetId: String): F[Long] = ???

  override def save(offsetId: String, value: Long): F[Unit] = ???
}
