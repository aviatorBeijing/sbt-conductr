addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.1")

lazy val root = Project("plugins", file(".")).dependsOn(plugin)
lazy val plugin = file("../../").getCanonicalFile.toURI
