package org.qualiton.eventsourcing.flyway

import cats.effect.{Effect, Sync}
import javax.sql.DataSource
import org.flywaydb.core.Flyway

object FlywayUpdater {

  def apply[F[_] : Effect](dataSource: DataSource): F[Unit] =
    Sync[F].delay {
      val flyway: Flyway = new Flyway
      flyway.setDataSource(dataSource)
      flyway.migrate

      ()
    }
}
