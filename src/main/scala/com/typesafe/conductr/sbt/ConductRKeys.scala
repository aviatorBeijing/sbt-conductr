/*
 * Copyright © 2014-2015 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.conductr.sbt

import sbt._, Keys._
import akka.util.Timeout

trait ConductRKeys {
  val conduct = inputKey[Unit]("ConductR task.")
  val conductrDiscoveredDist = taskKey[File]("Any distribution produced by the current project")
  val conductrDiscoveredConfigDist = taskKey[File]("Any additional configuration distribution produced by the current project")
  val conductrControlServerUrl = settingKey[URL]("The URL of the ConductR. Defaults to the env variables 'CONDUCTR_IP:[CONDUCTR_PORT]', otherwise uses the default: 'http://127.0.0.1:9005'")
  val conductrLoggingQueryUrl = settingKey[URL]("The URL of the Logging Query API. Defaults to the env variables 'LOGGING_QUERY_IP:[LOGGING_QUERY_PORT]', otherwise uses the default: 'http://127.0.0.1:9210'")
  val conductrConnectTimeout = settingKey[Timeout]("The timeout for ConductR communications when connecting")
  val conductrLoadTimeout = settingKey[Timeout]("The timeout for ConductR communications when loading")
  val conductrRequestTimeout = settingKey[Timeout]("The timeout for ConductR communications when requesting")
}
object ConductRKeys extends ConductRKeys {}
