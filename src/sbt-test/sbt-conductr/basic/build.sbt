import ByteConversions._

lazy val root = project
  .in(file("."))
  .enablePlugins(JavaAppPackaging, ConductRPlugin)

name := "sbt-conductr-tester"
version := "1.0.0"
scalaVersion := "2.11.4"

// ConductR

BundleKeys.nrOfCpus := 1.0
BundleKeys.memory := 10.MiB
BundleKeys.diskSpace := 5.MB
BundleKeys.roles := Set("web-server")
BundleKeys.endpoints := Map.empty