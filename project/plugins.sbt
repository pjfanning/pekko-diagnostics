// next line is needed by CopyrightHeader.scala
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.21.0"

addSbtPlugin("com.github.sbt" % "sbt-header" % "5.11.0")
addSbtPlugin("com.github.sbt" % "sbt-unidoc" % "0.6.1")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.6.2")
addSbtPlugin("com.lightbend.paradox" % "sbt-paradox-dependencies" % "0.2.4")
addSbtPlugin("com.lightbend.akka" % "sbt-paradox-akka" % "0.57")
addSbtPlugin("com.github.sbt" % "sbt-site-paradox" % "1.8.0")
addSbtPlugin("com.lightbend.sbt" % "sbt-java-formatter" % "0.7.0")
addSbtPlugin("com.github.sbt" % "sbt-ci-release" % "1.12.1")
