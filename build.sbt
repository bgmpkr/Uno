val scala3Version = "3.3.1"

lazy val root = project
  .in(file("."))
  .enablePlugins(CoverallsPlugin)
  .enablePlugins(AssemblyPlugin)
  .settings(
    name := "Uno",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,

    libraryDependencies += "org.scalactic" %% "scalactic" % "3.2.14",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.14" % "test",
    libraryDependencies += "org.scalafx" %% "scalafx" % "20.0.0-R31",
    libraryDependencies += "net.codingwell" %% "scala-guice" % "7.0.0",
    libraryDependencies += "com.google.inject" % "guice" % "7.0.0",
    libraryDependencies += "org.scala-lang.modules" %% "scala-xml" % "2.3.0",
    libraryDependencies += "com.typesafe.play" %% "play-json" % "2.10.0",

    coverageEnabled := true,

    assembly / assemblyMergeStrategy := {
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }
  )
