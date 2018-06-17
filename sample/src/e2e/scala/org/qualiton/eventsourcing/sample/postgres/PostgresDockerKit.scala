package org.qualiton.eventsourcing.sample.postgres

import java.sql.DriverManager
import java.util.Properties

import com.whisk.docker.{DockerCommandExecutor, DockerContainer, DockerContainerState, DockerKit, DockerReadyChecker}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.Try

trait PostgresDockerKit extends DockerKit {

  type PortMapping = (Int, Option[Int])
  type EnvironmentVariable = (String, String)

  def postgresVersion: String = "9.6-alpine"

  lazy val postgresAdvertisedPort: Int = 5432

  final val defaultPostgresUser: String = "postgres"

  def postgresPassword: String = "postgres"

  final val postgresContainerPort: Int = 5432

  def postgresPortMappings: Seq[PortMapping] =
    Seq(postgresContainerPort -> Some(postgresAdvertisedPort))

  def postgresEnv: Seq[EnvironmentVariable] =
    Seq(s"POSTGRES_PASSWORD" -> postgresPassword)

  def toDockerEnvironmentArgument(environmentVariable: EnvironmentVariable): String =
    environmentVariable match {
      case (name, value) => s"${name}=${value}"
    }

  def postgresContainer: DockerContainer =
    DockerContainer(s"postgres:$postgresVersion", name = Some(s"postgres"))
      .withPorts(postgresPortMappings: _*)
      .withEnv(postgresEnv.map(toDockerEnvironmentArgument): _*)
      .withReadyChecker(new PostgresReadyChecker(defaultPostgresUser, postgresPassword, Some(postgresAdvertisedPort)).looped(15, 5.second))

  override def dockerContainers: List[DockerContainer] =
    postgresContainer :: super.dockerContainers
}

class PostgresReadyChecker(user: String, password: String, port: Option[Int] = None)
  extends DockerReadyChecker {

  override def apply(container: DockerContainerState)(implicit docker: DockerCommandExecutor,
                                                      ec: ExecutionContext): Future[Boolean] =
    container
      .getPorts()
      .map(ports =>
        Try {
          Class.forName("org.postgresql.Driver")
          val props = new Properties()
          props.setProperty("user", user)
          props.setProperty("loggerLevel", "OFF")
          props.setProperty("password", password)
          val url = s"jdbc:postgresql://${docker.host}:${port.getOrElse(ports.values.head)}/postgres"
          Option(DriverManager.getConnection(url, props)).map(_.close).isDefined
        }.getOrElse(false))
}

