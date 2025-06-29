ThisBuild / version := "0.1.0-SNAPSHOT"

//ThisBuild / scalaVersion := "2.13.3"

lazy val root = (project in file("."))
  .settings(
    name := "bank-import"
  )

libraryDependencies ++= Seq(
  ws,
  "com.typesafe.play" %% "play-slick-evolutions" % "5.0.0",
  "com.typesafe.slick" %% "slick-codegen" % "3.3.3",
  "mysql" % "mysql-connector-java" % "8.0.23",
  "net.codingwell" %% "scala-guice" % "4.2.11",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  //"com.github.takezoe" %% "blocking-slick-33" % "0.0.13",
  "ch.qos.logback" % "logback-classic" % "1.2.3" % Runtime,
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4"
)

inThisBuild(
  List(
    scalaVersion := "2.13.3",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision
  )
)

/*scalacOptions ++= List(
  "-Ywarn-unused",
  "-Ywarn-dead-code",
  "-Xfatal-warnings",
  "-Xlint",
  "-unchecked",
  "-deprecation",
  "-feature"
)*/

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Wconf:cat=lint-multiarg-infix:silent"
)

Compile / console / scalacOptions ~= {
  _.filterNot(Set("-Xlint", "-Ywarn-unused"))
}

Compile / doc /scalacOptions ++= Seq(
  "-no-link-warnings" // Suppresses problems with Scaladoc @throws links
)
