ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.12.15"

lazy val root = (project in file("."))
  .settings(
    name := "zio-ml-server",
    libraryDependencies ++= Seq(
      "io.d11" %% "zhttp" % "1.0.0.0-RC25",
      "io.d11" %% "zhttp-test" % "1.0.0.0-RC25" % Test,
      "dev.zio" %% "zio-json" % "0.2.0-M3",
      "dev.zio" %% "zio-test" % "1.0.13" % "test",
      "ml.combust.mleap" %% "mleap-runtime" % "0.17.0"
    ),
  )
