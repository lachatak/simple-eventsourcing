package org.qualiton.eventsourcing.sample.postgres

import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import com.whisk.docker.impl.spotify.DockerKitSpotify
import com.whisk.docker.scalatest.DockerTestKit
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.qualiton.eventsourcing.postgres.PostgresJournal
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Minute, Span}
import org.scalatest.{FeatureSpec, GivenWhenThen, Matchers}

import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps

class BankAccountSpec
  extends FeatureSpec
    with GivenWhenThen
    with Matchers
    with Eventually
    with TypeCheckedTripleEquals
    with DockerTestKit
    with DockerKitSpotify
    with PostgresDockerKit
    with LazyLogging {

  override val StartContainersTimeout: FiniteDuration = 120 seconds

  override val StopContainersTimeout: FiniteDuration = 60 seconds

  override val dockerInitPatienceInterval =
    PatienceConfig(
      timeout = Span(1, Minute),
      interval = Span(100, Millis))

  override def beforeAll(): Unit = {
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    super.afterAll()
  }

  feature("Bank Account Operations") {
    info("As a user of Bank Account Aggregate")
    info("I want do bank account operations")

    scenario(s"When a ...") {
      Given(s"a valid ...")
      When("it is ...")
      Then("the the ..")
    }
  }

  trait Scope {

    val dataSource: HikariDataSource = {
      val config = new HikariConfig()
      config.setJdbcUrl("jdbc:postgresql://localhost:5432/postgres")
      config.setUsername("postgres")
      config.setPassword("postgres")
      new HikariDataSource(config)
    }

    implicit val bankAccountEventSerializer = new BankAccountEventSerializer

    val journal = new PostgresJournal[IO, BankAccountEvent](dataSource)

    1 to 1000 foreach { id =>
      val bankAccount = new BankAccountAggregate[IO](id, journal)
      for {
        _ <- bankAccount.open("Krs", 1000)
        _ <- bankAccount.withdraw(100)
        _ <- bankAccount.withdraw(100)
      } yield ()
    }
  }

}
