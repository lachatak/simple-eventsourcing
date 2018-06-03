import sbt.Keys._
import sbt.{ Def, _ }

object BaseSettings {

  val default: Seq[Def.Setting[_]] =
    Seq(
      scalaVersion := "2.12.6",
      organization := "org.kaloz",
      organizationName := "Kaloz",
      name := "Simple EventSourcing",
      description := "Boost service for generating charges"
    ) ++ Aliases.aliases
}
