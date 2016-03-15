import sbt.Keys._
import sbt._

object Build extends Build {

  val theScalaVersion = "2.11.7"
  val circeVersion = "0.3.0"

  val root = Project(id = "scala-cv-part2", base = file("."))
      .settings(
        name := """scala-cv-part2""",
        version := "1.0-SNAPSHOT",
        scalaVersion := theScalaVersion,

        libraryDependencies ++= Seq(
          "com.typesafe.akka" %% "akka-stream-experimental" % "2.0.3"
        ),

        // For Swing
        libraryDependencies := {
          CrossVersion.partialVersion(scalaVersion.value) match {
            // if scala 2.11+ is used, add dependency on scala-xml module
            case Some((2, scalaMajor)) if scalaMajor >= 11 =>
              libraryDependencies.value ++ Seq(
                "org.scala-lang.modules" %% "scala-swing" % "2.0.0-M2")
            case _ =>
              // or just libraryDependencies.value if you don't depend on scala-swing
              libraryDependencies.value :+ "org.scala-lang" % "scala-swing" % scalaVersion.value
          }
        },

        // Allows us to stop when using run without killing SBT
        fork in run := true
      )
}