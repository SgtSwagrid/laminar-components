import sbt.*
import sbt.Keys.*

object Dependencies:

  object V:

    val laminar = "18.0.0-M5"
    val munit   = "1.3.6"

  lazy val laminar = libraryDependencies ++= Seq(
    "com.raquo" %% "laminar"   % V.laminar,
    "com.raquo" %% "airstream" % V.laminar,
  )

  lazy val munit = libraryDependencies ++=
    Seq("org.scalameta" %% "munit" % V.munit % Test)
