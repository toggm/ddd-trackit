name := "trackit"

version := "1.0-SNAPSHOT"

resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

resolvers += "Tegonal releases" at "https://github.com/tegonal/tegonal-mvn/raw/master/releases/"

libraryDependencies ++= Seq(
  cache,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.2",
  "com.github.nscala-time" %% "nscala-time" % "0.6.0",
  "com.github.athieriot" %% "specs2-embedmongo" % "0.6.0",
  "com.typesafe.akka" %% "akka-testkit" % "2.2.0" % "test",
  "com.tegonal" %% "play-airbrake" % "0.3.2"
)     

scalacOptions += "-feature"

javaOptions in Test += "-Dconfig.file=conf/test.conf"

javaOptions in ScoverageSbtPlugin.scoverageTest += "-Dconfig.file=conf/test.conf"

parallelExecution in Test := false

buildInfoSettings

sourceGenerators in Compile <+= buildInfo

buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

buildInfoPackage := "trackit"

play.Project.playScalaSettings ++ seq(ScoverageSbtPlugin.instrumentSettings : _*) ++ org.scalastyle.sbt.ScalastylePlugin.Settings

org.scalastyle.sbt.PluginKeys.config := file("project/scalastyle-config.xml")
