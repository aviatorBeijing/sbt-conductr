package com.lightbend.conductr.sbt

import com.typesafe.sbt.SbtNativePackager
import SbtNativePackager.Universal

import sbt._
import sbt.Keys._

import scala.util.{ Failure, Success }

object PlayBundlePlugin extends AutoPlugin {

  import PlayBundleImport._
  import BundlePlugin.autoImport._

  val autoImport = PlayBundleImport

  private val classLoader = this.getClass.getClassLoader

  override def requires =
    withContextClassloader(classLoader) { loader =>
      def play23OrBelow = Reflection.getSingletonObject[Plugins.Basic](classLoader, "play.Play$")
      def play24OrAbove = Reflection.getSingletonObject[Plugins.Basic](classLoader, "play.sbt.Play$")
      play24OrAbove orElse play23OrBelow match {
        case Failure(_)    => NoOpPlugin
        case Success(play) => BundlePlugin && play
      }
    }

  override def trigger = allRequirements

  override def projectSettings =
    Seq(
      javaOptions in Universal ++= Seq(
        s"-J-Xms${PlayBundleKeyDefaults.heapMemory.round1k.underlying}",
        s"-J-Xmx${PlayBundleKeyDefaults.heapMemory.round1k.underlying}"
      ),
      BundleKeys.nrOfCpus := PlayBundleKeyDefaults.nrOfCpus,
      BundleKeys.memory := PlayBundleKeyDefaults.residentMemory,
      BundleKeys.diskSpace := PlayBundleKeyDefaults.diskSpace,
      BundleKeys.endpoints := BundlePlugin.getDefaultWebEndpoints(Bundle).value,
      conductrBundleLibVersion := Version.conductrBundleLib,
      libraryDependencies += Library.playConductrBundleLib(PlayVersion.current, scalaBinaryVersion.value, conductrBundleLibVersion.value)
    )
}

/**
 * Mirrors the LagomVersion class of `play.core.PlayVersion`
 * By declaring the public methods from Lagom it is possible to "safely"
 * call the class via reflection.
 */
private object PlayVersion {

  import scala.language.reflectiveCalls

  val classLoader = this.getClass.getClassLoader

  // The method signature equals the signature of `play.core.PlayVersion`
  type PlayVersion = {
    def current: String
  }

  val current: String =
    withContextClassloader(classLoader) { loader =>
      Reflection.getSingletonObject[PlayVersion](loader, "play.core.PlayVersion$") match {
        case Failure(t)           => sys.error(s"The PlayVersion class can not be resolved. Error: ${t.getMessage}")
        case Success(playVersion) => playVersion.current
      }
    }
}