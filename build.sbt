sbtPlugin := true

name := "sbt-traceur"
organization in ThisBuild := "com.typesafe.sbt"
version in ThisBuild := "1.0.2"
description := "sbt-web plugin to compile ES6 to ES5 with traceur-compiler from Google."
licenses += ("MIT", url("https://github.com/LuigiPeace/sbt-traceur/blob/master/LICENSE"))

publishMavenStyle := false
bintrayRepository := "sbt-traceur"
bintrayOrganization in bintray := None

scalaVersion := "2.10.4"

resolvers += Resolver.typesafeRepo("releases")

addSbtPlugin("com.typesafe.sbt" % "sbt-js-engine" % "1.2.1")

libraryDependencies ++= Seq(
  "org.webjars" % "traceur" % "0.0.90"
)