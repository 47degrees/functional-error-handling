lazy val root = (project in file(".")).
  settings(
    name := "FunctionalErrorHandling",
    version := "1.0",
    scalaVersion := "2.12.1",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats" % "0.9.0",
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "org.scalacheck" %% "scalacheck" % "1.13.4" % Test
    ),
    addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.3")
  )
