name := "trackit"

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.2"

resolvers += "Sonatype Snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

resolvers ++= Seq(
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/"
)

resolvers += "Tegonal releases" at "https://github.com/tegonal/tegonal-mvn/raw/master/releases/"

libraryDependencies ++= Seq(
  cache,
  "org.reactivemongo" %% "play2-reactivemongo" % "0.10.5.0.akka23",
  "org.scalatestplus" %% "play" % "1.1.0" % "test",
  "com.github.nscala-time" %% "nscala-time" % "1.4.0"
)     

scalacOptions += "-feature"

javaOptions in Test += "-Dconfig.file=conf/test.conf"

//javaOptions in ScoverageSbtPlugin.scoverageTest += "-Dconfig.file=conf/test.conf"

parallelExecution in Test := false

//sourceGenerators in Compile <+= buildInfo

//buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion)

//buildInfoPackage := "trackit"

//play.Project.playScalaSettings ++ seq(ScoverageSbtPlugin.instrumentSettings : _*) ++ org.scalastyle.sbt.ScalastylePlugin.Settings

instrumentSettings

//org.scalastyle.sbt.PluginKeys.config := file("project/scalastyle-config.xml")