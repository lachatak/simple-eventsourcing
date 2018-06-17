package org.qualiton.eventsourcing.postgres

import cats.effect.Effect
import com.zaxxer.hikari.HikariDataSource
import org.qualiton.eventsourcing.eventstream.{EventEnvelope, EventStream}

class PostgresEventStream[F[_] : Effect, Event](dataSource: HikariDataSource) extends EventStream[F, Event] {

  println(dataSource)

  override def subscribe(f: EventEnvelope[Event] => F[Unit], offset: Long): F[Unit] = ???
}
