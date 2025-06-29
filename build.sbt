ThisBuild / version := "0.1.0-SNAPSHOT"

lazy val root = (project in file("."))
  .settings(
    name := "bank-import"
  )

lazy val codegen = (project in file("codegen"))
  .settings(
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick-codegen" % "3.3.3",
      "joda-time" % "joda-time" % "2.10.10",
      "org.joda" % "joda-convert" % "2.2.1",
      "com.github.tototoshi" %% "slick-joda-mapper" % "2.5.0",
      "mysql" % "mysql-connector-java" % "8.0.23",
      "ch.qos.logback" % "logback-classic" % "1.2.3" % Runtime
    )
  ).dependsOn(root)

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

scalacOptions ++= Seq(
  "-deprecation",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-unused",
  "-Wunused",
  "-Wconf:cat=lint-multiarg-infix:silent"
)

Compile / console / scalacOptions ~= {
  _.filterNot(Set("-Xlint", "-Ywarn-unused"))
}

Compile / doc / scalacOptions ++= Seq(
  "-no-link-warnings" // Suppresses problems with Scaladoc @throws links
)

