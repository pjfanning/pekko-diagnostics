/**
 * Copyright (C) 2016 Lightbend Inc. <http://www.lightbend.com>
 */
import sbt._

object Dependencies {
  val Scala212 = "2.12.21"
  val Scala213 = "2.13.18"
  val Scala3 = "3.3.7"
  val CrossScalaVersions = Seq(Scala213, Scala212, Scala3)

  val PekkoVersion = "1.1.5"
  val PekkoVersionInDocs = PekkoVersion.take(3)
  val PekkoHttpVersionInDocs = "1.1.0"
  val ScalaTestVersion = "3.2.19"

  val commonsText = "org.apache.commons" % "commons-text" % "1.15.0" // ApacheV2

  object Compile {
    val pekkoActor = "org.apache.pekko" %% "pekko-actor" % PekkoVersion
  }

  object TestDeps {
    val pekkoRemoting = "org.apache.pekko" %% "pekko-remote" % PekkoVersion
    val pekkoClusterMetrics = "org.apache.pekko" %% "pekko-cluster-metrics" % PekkoVersion
    val pekkoStreamTestKit = "org.apache.pekko" %% "pekko-stream-testkit" % PekkoVersion
    val scalaTest = "org.scalatest" %% "scalatest" % ScalaTestVersion
    val pekkoPersistenceTestKit = "org.apache.pekko" %% "pekko-persistence-testkit" % PekkoVersion
    val pekkoTestKit = "org.apache.pekko" %% "pekko-testkit" % PekkoVersion
    val junit = "junit" % "junit" % "4.13.2" // Common Public License 1.0
    val all = Seq(
      pekkoRemoting % Test,
      pekkoClusterMetrics % Test,
      pekkoStreamTestKit % Test,
      pekkoPersistenceTestKit % Test,
      pekkoTestKit % Test,
      scalaTest % Test, // ApacheV2
      junit % Test // Common Public License 1.0
    )
  }

  import Compile._

  val pekkoDiagnostics = Seq(
    commonsText, // for levenshtein distance impl
    pekkoActor) ++ TestDeps.all
}
