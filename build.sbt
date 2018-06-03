lazy val root =
  (project in file("."))
    .aggregate(sample, core, postgres)
    .dependsOn(sample, core, postgres)
    .settings(BaseSettings.default)

lazy val core =
  Project("simple-eventsourcing-core", file("core"))
    .withTestConfig
    .withDependencies

lazy val postgres =
  Project("simple-eventsourcing-postgres", file("postgres"))
    .dependsOn(core % "test->test;compile->compile")
    .withTestConfig
    .withDependencies

lazy val sample =
  Project("simple-eventsourcing-sample", file("sample"))
    .dependsOn(core % "test->test;compile->compile", postgres % "test->test;compile->compile")
    .withTestConfig
    .withDependencies
