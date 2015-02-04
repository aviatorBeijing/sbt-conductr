/*
 * Copyright © 2014 Typesafe, Inc. All rights reserved.
 */

package com.typesafe.conductr.sbt
package console

import com.typesafe.conductr.client.ConductRController

object Column {

  import AnsiConsole.Implicits._
  import Console.Implicits._

  /**
   * A fixed width column with no additional functionality.
   * Extend to set title, width and data to be displayed.
   */
  trait RegularColumn extends LeftJustified {
    /**
     * How much empty space to leave on the right-side of the column,
     */
    val marginRight = 1

    /**
     * Title of the column.
     */
    def title: String

    /**
     * Width of the column. Shorter values will be padded with spaces to this width.
     */
    def width: Int

    /**
     * Data of the column. Every data cell in the column can have multiple rows.
     * Therefore two-dimensional sequence.
     */
    def data: Seq[Seq[String]]

    /**
     * Column title padded to the required width.
     */
    def titleForPrint: String =
      justify(title)

    /**
     * Column data padded to the required width and flattened to one-dimensional sequence.
     * @param rowCounts specifies how many inner rows does every outer has
     */
    def dataForPrint(rowCounts: Seq[Int]): Seq[String] =
      data.zip(rowCounts).flatMap {
        case (rows, rowCount) =>
          rows.padTo(rowCount, "").map(ellipsize _ andThen justify _)
      }

    /**
     * Wrap string with spaces for needed width.
     */
    protected def justify(s: String): String

    protected def space(length: Int): String =
      Stream.continually(" ").take(length).mkString

    /**
     * Add '... ' if string is over the allowed width.
     */
    private def ellipsize(s: String): String = {
      val ellipsis = "... "
      if (s.length >= width)
        s.take(width - ellipsis.length) + ellipsis
      else
        s
    }
  }

  trait LeftJustified { self: RegularColumn =>
    override protected def justify(s: String): String = {
      s + space(width - s.visibleLength)
    }
  }

  trait RightJustified { self: RegularColumn =>
    override protected def justify(s: String): String = {
      space(width - s.visibleLength - marginRight) + s + space(marginRight)
    }
  }

  /**
   * Displays bundle id, bundle digest and config digest.
   */
  case class Id(bundles: Seq[ConductRController.BundleInfo]) extends RegularColumn {
    override val title = "ID/BUNDLE/CONF"
    override val width = 27

    val hashLength = 7

    val data: Seq[Seq[String]] =
      bundles.map { bundle =>
        val id = bundle.bundleId.take(hashLength)
        val bundleId = bundle.bundleDigest.take(hashLength)
        val configId = bundle.configDigest.fold("")(_.take(hashLength))
        List(s"$id/$bundleId/$configId")
      }
  }

  /**
   * Displays bundle name.
   */
  case class Name(bundles: Seq[ConductRController.BundleInfo]) extends RegularColumn {
    override val title = "NAME"
    override val width = 30

    val data: Seq[Seq[String]] =
      bundles.map { bundle => List(bundle.attributes.bundleName) }
  }

  /**
   * Displays address where bundle is running and/or deployed.
   * Address is inverted if bundle is running on the particular host.
   */
  case class Where(bundles: Seq[ConductRController.BundleInfo]) extends RegularColumn {
    override val title = "WHERE"
    override val width = 22

    val data: Seq[Seq[String]] =
      bundles.map { bundle =>
        bundle.bundleInstallations.map { node =>
          val nodeIpAndPort = node.uniqueAddress.address.toString.dropWhile(_ != '@').drop(1)
          if (bundle.bundleExecutions.withFilter(_.isStarted).map(_.host) contains node.uniqueAddress.address.host.get)
            nodeIpAndPort.invert
          else
            nodeIpAndPort
        }
      }
  }

  /**
   * Displays the number of hosts where bundle is replicated.
   */
  case class Replicated(bundles: Seq[ConductRController.BundleInfo]) extends RegularColumn with RightJustified {
    override val title = "#REP"
    override val width = 7

    val data: Seq[Seq[String]] =
      bundles.map { bundle =>
        List(bundle.bundleInstallations.size.toString)
      }
  }

  /**
   * Displays the number of hosts where bundle is starting.
   */
  case class Starting(bundles: Seq[ConductRController.BundleInfo]) extends RegularColumn with RightJustified {
    override val title = "#STR"
    override val width = 7

    val data: Seq[Seq[String]] =
      bundles.map { bundle =>
        List(bundle.bundleExecutions.count(!_.isStarted).toString)
      }
  }

  /**
   * Displays the number of hosts where bundle is running.
   */
  case class Running(bundles: Seq[ConductRController.BundleInfo]) extends RegularColumn with RightJustified {
    override val title = "#RUN"
    override val width = 7

    val data: Seq[Seq[String]] =
      bundles.map { bundle =>
        List(bundle.bundleExecutions.count(_.isStarted).toString)
      }
  }

  /**
   * Displays bundle CPU requirement.
   */
  case class Cpu(bundles: Seq[ConductRController.BundleInfo]) extends RegularColumn with RightJustified {
    override val title = "#CPU"
    override val width = 9

    val data: Seq[Seq[String]] =
      bundles.map { bundle =>
        List(bundle.attributes.nrOfCpus.toString)
      }
  }

  /**
   * Displays bundle memory requirement.
   */
  case class Memory(bundles: Seq[ConductRController.BundleInfo]) extends RegularColumn with RightJustified {
    override val title = "MEM"
    override val width = 8

    val data: Seq[Seq[String]] =
      bundles.map { bundle =>
        List(bundle.attributes.memory.toSize)
      }
  }

  /**
   * Displays bundle file size.
   */
  case class FileSize(bundles: Seq[ConductRController.BundleInfo]) extends RegularColumn with RightJustified {
    override val title = "FSIZE"
    override val width = 8

    val data: Seq[Seq[String]] =
      bundles.map { bundle =>
        List(bundle.attributes.diskSpace.toSize)
      }
  }

  /**
   * Displays bundle cluster roles requirement.
   */
  case class Roles(bundles: Seq[ConductRController.BundleInfo], width: Int = 20) extends RegularColumn {
    override val title = "ROLES"

    val data: Seq[Seq[String]] =
      bundles.map { bundle =>
        List(bundle.attributes.roles.mkString(","))
      }
  }
}
