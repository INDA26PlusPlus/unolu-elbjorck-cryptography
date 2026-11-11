val scala3Version = "3.10.0"

lazy val root = project
  .in(file("."))
  .settings(
    name := "server",
    version := "0.1.0-SNAPSHOT",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.3.6" % Test,
      "org.xerial" % "sqlite-jdbc" % "3.54.1.0",
      "io.crums" % "merkle-tree" % "1.1.0",
    ),
    //crossPaths       := false,

    //autoScalaLibrary := false
  )
