import IdeSettings.packagePrefix
import org.scalajs.sbtplugin.ScalaJSPlugin
import sbt._
import sbt.Keys._
import sbtunidoc.BaseUnidocPlugin.autoImport.*
import sbtunidoc.ScalaUnidocPlugin

val scala3 = "3.9.0"

ThisBuild / scalaVersion := scala3

ThisBuild / scalacOptions ++= Seq(
  "-explain",
  "-explain-types",
  "-explain-cyclic",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Wunused:all",
)

// Named after the library so as not to clash with the projects of a build that
// includes this one by reference.
lazy val `laminar-components` = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin, ScalaUnidocPlugin)
  .settings(
    name          := "laminar-components",
    packagePrefix := "com.alecdorrington.laminarcomponents",
    Dependencies.laminar,
    Dependencies.munit,
    ScalaUnidoc / unidoc / scalacOptions ++=
      Seq("-project", "Laminar Components"),
  )
