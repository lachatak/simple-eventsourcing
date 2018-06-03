import sbt.Keys._
import sbt.{Def, Developer, _}

object BaseSettings {

  val default: Seq[Def.Setting[_]] =
    Seq(
      scalaVersion := "2.12.6",
      organization := "org.qualiton",
      organizationName := "Qualiton Ltd",
      organizationHomepage := Some(url("http://qualiton.org")),
      homepage := Some(url("https://github.com/qualiton/simple-eventsourcing")),
      licenses := Seq(("Apache-2.0", url("http://www.apache.org/licenses/LICENSE-2.0"))),
      developers := List(
        Developer("lachatak", "Krisztian Lachata", "qualitonltd@gmail.com", url("http://qualiton.org"))
      ),
      startYear := Some(2018)
    ) ++ Aliases.aliases
}
