lazy val root =
  (project in file("."))
    .aggregate(core, postgres)
    .dependsOn(core, postgres)
    .settings(BaseSettings.default)

lazy val core =
  (project in file("core"))
    .withTestConfig
    .withDependencies

lazy val postgres =
  (project in file("postgres"))
    .withTestConfig
    .withDependencies

