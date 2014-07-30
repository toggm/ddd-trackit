// Comment to get more information during initialization
logLevel := Level.Warn

// The Typesafe repository 
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

resolvers += Classpaths.typesafeResolver

resolvers += Classpaths.sbtPluginReleases

resolvers += "Tegonal releases" at "https://github.com/tegonal/tegonal-mvn/raw/master/releases/"

// Use the Play sbt plugin for Play projects
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.2.2")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.3.2")

addSbtPlugin("com.sksamuel.scoverage" %% "sbt-scoverage" % "0.95.7")

addSbtPlugin("com.tegonal" % "play-messagescompiler" % "1.0.4")

addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.3.1")

lazy val root = project.in( file(".") ).dependsOn( reporterPlugin )
lazy val reporterPlugin = uri("git://github.com/mmarich/sbt-simple-junit-xml-reporter-plugin.git")
