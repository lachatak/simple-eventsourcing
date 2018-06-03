package org.qualiton.eventsourcing.sample

package object postgres {

  type PortMapping = (Int, Option[Int])
  type EnvironmentVariable = (String, String)

  def toDockerEnvironmentArgument(environmentVariable: EnvironmentVariable): String =
    environmentVariable match {
      case (name, value) => s"${name}=${value}"
    }

}
