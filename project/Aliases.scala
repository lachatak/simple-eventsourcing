import sbt.{ Def, _ }
import scala.sys.process.Process

object Aliases extends AutoPlugin {

  private val deleteDockerImages = TaskKey[Unit]("ddi", "Delete hanging docker images")

  override def trigger = allRequirements

  override lazy val projectSettings: Seq[Def.Setting[_]] = super.projectSettings ++ Seq(
    deleteDockerImages := deleteRunningContainers()
  )

  private def deleteRunningContainers(): Unit = {
    val ids = Process("docker", Seq("ps", "-aq")).!!.replaceAll("\\n", " ").split(" ").filterNot(_.length == 0).toList
    if (ids.nonEmpty) {
      println(s"Cleanup images $ids!")
      Process("docker", Seq("rm", "-f") ++ ids).!
    } else {
      println("No container to remove!")
    }
  }

  lazy val aliases: Seq[Def.Setting[State => State]] =
    addCommandAlias("all-tests", ";clean ;test ;it ;e2e")
}
