lazy val root =
  (project in file("."))
    .aggregate(core, postgres)
    .dependsOn(core, postgres)
    .settings(BaseSettings.default)

lazy val core =
  Project("simple-eventsourcing-core", file("core"))
    .withTestConfig
    .withDependencies

lazy val postgres =
  Project("simple-eventsourcing-postgres", file("postgres"))
    .withTestConfig
    .withDependencies

