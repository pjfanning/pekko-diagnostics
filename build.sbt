GlobalScope / parallelExecution := false
Global / concurrentRestrictions += Tags.limit(Tags.Test, 1)

val specificationVersion: String = sys.props("java.specification.version")
val isJdk17orHigher: Boolean =
  VersionNumber(specificationVersion).matchesSemVer(SemanticSelector(">=17"))

inThisBuild(
  Seq(
    organization := "com.github.pjfanning",
    organizationName := "pjfanning",
    homepage := Some(url("https://github.com/pjfanning/pekko-diagnostics")),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/pjfanning/pekko-diagnostics"),
        "https://github.com/pjfanning/pekko-diagnostics.git")),
    startYear := Some(2022),
    developers += Developer(
      "contributors",
      "Contributors",
      "https://github.com/pjfanning",
      url("https://github.com/pjfanning/pekko-diagnostics/graphs/contributors")),
    licenses := {
      val tagOrBranch =
        if (version.value.endsWith("SNAPSHOT")) "main"
        else "v" + version.value
      Seq(("BUSL-1.1", url(s"https://raw.githubusercontent.com/pjfanning/pekko-diagnostics/${tagOrBranch}/LICENSE")))
    },
    description := "Pekko diagnostics tools and utilities",
    // add snapshot repo when Pekko version overriden
    resolvers ++=
      (if (System.getProperty("override.pekko.version") != null)
         Seq("Apache Snapshots".at("https://repository.apache.org/content/repositories/snapshots/"))
       else Seq.empty)))

lazy val common: Seq[Setting[_]] =
  Seq(
    crossScalaVersions := Dependencies.CrossScalaVersions,
    scalaVersion := Dependencies.CrossScalaVersions.head,
    crossVersion := CrossVersion.binary,
    scalafmtOnCompile := true,
    //sonatypeProfileName := "com.lightbend",
    headerLicense := Some(HeaderLicense.Custom("""Copyright (C) 2023 Lightbend Inc. <https://www.lightbend.com>""")),
    // Setting javac options in common allows IntelliJ IDEA to import them automatically
    Compile / javacOptions ++= Seq("-encoding", "UTF-8", "-source", "1.8", "-target", "1.8"),
    scalacOptions ++= {
      var scalacOptionsBase = Seq("-encoding", "UTF-8", "-feature", "-unchecked", "-deprecation")
      if (scalaVersion.value == Dependencies.Scala212)
        scalacOptionsBase ++: Seq("-Xfuture", "-Xfatal-warnings", "-Xlint", "-Ywarn-dead-code")
      else if (scalaVersion.value == Dependencies.Scala213)
        scalacOptionsBase ++: Seq("-Xfatal-warnings", "-Xlint", "-Ywarn-dead-code", "-Wconf:cat=deprecation:info")
      else
        scalacOptionsBase
    },
    javacOptions ++= (
      if (isJdk8) Seq.empty
      else Seq("--release", "8")
    ),
    scalacOptions ++= (
      if (isJdk8 || scalaVersion.value == Dependencies.Scala212) Seq.empty
      else Seq("--release", "8")
    ),
    Test / logBuffered := false,
    Test / parallelExecution := false,
    // show full stack traces and test case durations
    Test / testOptions += Tests.Argument("-oDF"),
    // -v Log "test run started" / "test started" / "test run finished" events on log level "info" instead of "debug".
    // -a Show stack traces and exception class name for AssertionErrors.
    testOptions += Tests.Argument(TestFrameworks.JUnit, "-v", "-a"),
    Test / fork := true, // some non-heap memory is leaking
    Test / javaOptions ++= {
      import scala.collection.JavaConverters._
      val pekkoProperties = System.getProperties.stringPropertyNames.asScala.toList.collect {
        case key: String if key.startsWith("pekko.") => "-D" + key + "=" + System.getProperty(key)
      }
      val openModules =
        if (isJdk17orHigher) Seq("--add-opens=java.base/java.util.concurrent=ALL-UNNAMED")
        else Nil
      "-Xms1G" :: "-Xmx1G" :: "-XX:MaxDirectMemorySize=256M" :: pekkoProperties ++ openModules
    },
    projectInfoVersion := (if (isSnapshot.value) "snapshot" else version.value),
    Global / excludeLintKeys += projectInfoVersion)

lazy val root = (project in file("."))
  .settings(
    name := "pekko-diagnostics-root",
    publishTo := Some(Resolver.file("Unused transient repository", file("target/unusedrepo"))))
  .settings(common)
  .enablePlugins(ScalaUnidocPlugin)
  .disablePlugins(SitePlugin)
  .settings(dontPublish)
  .aggregate(`pekko-diagnostics`, docs)

lazy val `pekko-diagnostics` = (project in file("diagnostics"))
  .settings(common)
  .settings(name := "pekko-diagnostics")
  .settings(libraryDependencies ++= Dependencies.pekkoDiagnostics)
  .settings(AutomaticModuleName.settings("org.apache.pekko.diagnostics"))

lazy val docs = (project in file("docs"))
  .enablePlugins(AkkaParadoxPlugin, ParadoxSitePlugin, PreprocessPlugin)
  .settings(common)
  .settings(dontPublish)
  .settings(
    name := "Pekko Diagnostics",
    makeSite := makeSite.dependsOn(LocalRootProject / ScalaUnidoc / doc).value,
    Preprocess / siteSubdirName := s"api/pekko-diagnostics/${if (isSnapshot.value) "snapshot" else version.value}",
    Preprocess / sourceDirectory := (LocalRootProject / ScalaUnidoc / unidoc / target).value,
    previewPath := (Paradox / siteSubdirName).value,
    paradoxGroups := Map("Languages" -> Seq("Java", "Scala")),
    Paradox / siteSubdirName := s"docs/pekko-diagnostics/${if (isSnapshot.value) "snapshot" else version.value}",
    Compile / paradoxProperties ++= Map(
      "version" -> version.value,
      "project.url" -> "https://github.com/pjfanning/pekko-diagnostics",
      "canonical.base_url" -> "https://github.com/pjfanning/pekko-diagnostics",
      "pekko.version" -> Dependencies.PekkoVersion,
      "scala.version" -> scalaVersion.value,
      "scala.binaryVersion" -> scalaBinaryVersion.value,
      "extref.scaladoc.base_url" -> s"/${(Preprocess / siteSubdirName).value}/",
      "extref.javadoc.base_url" -> s"/japi/pekko-diagnostics/${if (isSnapshot.value) "snapshot" else version.value}",
      "scaladoc.pekko.persistence.gdpr.base_url" -> s"/api/pekko-diagnostics/${if (isSnapshot.value) "snapshot"
      else version.value}",
      "extref.pekko.base_url" -> s"https://pekko.apache.org/docs/pekko/${Dependencies.PekkoVersionInDocs}/%s",
      "scaladoc.pekko.base_url" -> s"https://pekko.apache.org/api/pekko/${Dependencies.PekkoVersionInDocs}",
      "extref.pekko-http.base_url" -> s"https://pekko.apache.org/docs/pekko-http/${Dependencies.PekkoHttpVersionInDocs}/%s",
      "scaladoc.pekko.http.base_url" -> s"https://pekko.apache.org/api/pekko-http/${Dependencies.PekkoHttpVersionInDocs}/",
      "snip.github_link" -> "false"),
    ApidocPlugin.autoImport.apidocRootPackage := "org.apache.pekko",
    apidocRootPackage := "org.apache.pekko")

lazy val dontPublish = Seq(publish / skip := true, Compile / publishArtifact := false)

lazy val isJdk8 =
  VersionNumber(sys.props("java.specification.version")).matchesSemVer(SemanticSelector(s"=1.8"))
