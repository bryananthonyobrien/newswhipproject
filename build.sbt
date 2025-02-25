import Dependencies._

ThisBuild / scalaVersion     := "2.13.12"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "newsWhipProject",
    Compile / run / mainClass := Some("URLManager"),
    assembly / mainClass := Some("URLManager"),
    assembly / assemblyJarName := "newswhipproject-assembly.jar",
    libraryDependencies += munit % Test,
    scalacOptions += "-deprecation"
  )

enablePlugins(AssemblyPlugin)
