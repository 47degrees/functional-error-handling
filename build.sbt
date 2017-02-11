lazy val root = (project in file(".")).
  settings(
    name := "FunctionalErrorHandling",
    version := "1.0",
    scalaVersion := "2.12.1",
    libraryDependencies ++= Seq(
      "org.typelevel" %% "cats" % "0.9.0",
      "co.fs2" %% "fs2-core" % "0.9.2",
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "org.scalacheck" %% "scalacheck" % "1.13.4" % Test
    )
  )
