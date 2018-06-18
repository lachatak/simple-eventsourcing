package org.qualiton.eventsourcing.sample.postgres

import cats.data.EitherT
import cats.effect.IO
import com.typesafe.scalalogging.LazyLogging
import com.whisk.docker.impl.spotify.DockerKitSpotify
import com.whisk.docker.scalatest.DockerTestKit
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import org.qualiton.eventsourcing.flyway.FlywayUpdater
import org.qualiton.eventsourcing.postgres.PostgresJournal
import org.scalactic.TypeCheckedTripleEquals
import org.scalatest._
import org.scalatest.concurrent.Eventually
import org.scalatest.time.{Millis, Minute, Span}

import scala.concurrent.duration.{FiniteDuration, _}
import scala.language.postfixOps

class EndToEndBankAccountSpec
  extends FeatureSpec
    with GivenWhenThen
    with Matchers
    with OptionValues
    with EitherValues
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

  val bankAccountTestApp = new BankAccountTestApp

  override def beforeAll(): Unit = {
    super.beforeAll()
    bankAccountTestApp.start()
  }

  override def afterAll(): Unit = {
    bankAccountTestApp.stop()
    super.afterAll()
  }

  feature("Bank Account Operations") {
    info("As a user of Bank Account Aggregate")
    info("I want do bank account operations")

    scenario("When I open a bank account with 1000 and withdraw two times 100 the resulting balance should be 800") {
      Given("a valid bank account with 1000")
      val openBankAccount: EitherT[IO, Throwable, BankAccountAggregate[IO]] = for {
        bankAccount <- EitherT.right[Throwable](IO(BankAccountAggregate[IO](1, bankAccountTestApp.journal)))
        _ <- bankAccount.open("Krs", 1000)
      } yield bankAccount

      When("withdraw two times 100")
      val transactions = for {
        bankAccount <- openBankAccount
        _ <- bankAccount.withdraw(100)
        balance <- bankAccount.withdraw(100)
      } yield balance

      Then("the resulting balance should be 800")
      transactions.value.unsafeRunSync().right.value shouldBe (800)
    }
  }

  class BankAccountTestApp {

    lazy val dataSource: HikariDataSource = {
      val config = new HikariConfig()
      config.setJdbcUrl(s"jdbc:postgresql://localhost:5432/postgres")
      config.setUsername("postgres")
      config.setPassword("postgres")
      new HikariDataSource(config)
    }

    implicit lazy val bankAccountEventSerializer = new BankAccountEventSerializer
    lazy val journal = new PostgresJournal[IO, BankAccountEvent](dataSource)

    def start(): Unit = FlywayUpdater[IO](dataSource).unsafeRunSync()

    def stop(): Unit = dataSource.close()
  }

}
