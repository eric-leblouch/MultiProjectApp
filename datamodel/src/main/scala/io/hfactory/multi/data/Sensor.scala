package io.hfactory.multi.data

import com.typesafe.scalalogging.slf4j.Logging
import com.ubeeko.hfactory.context.HContext
import com.ubeeko.hfactory.entities.HEntityRegistry
import com.ubeeko.hfactory.entities.annotations.HBase.ColumnFamilyAnnotation
import com.ubeeko.hfactory.annotations.{DescriptionAnnotation, DevOnly}
import com.ubeeko.hfactory.entities.annotations.HEntityController
import com.ubeeko.htalk.bytesconv._
import com.ubeeko.htalk.criteria._
import org.apache.hadoop.hbase.filter._

import scala.annotation.meta.field
import scala.collection.JavaConversions._

case class Sensor(
  @(DescriptionAnnotation @field)("Sensor identifier")
  id: String,

  @(DescriptionAnnotation @field)("Device this sensor belongs to")
  deviceId: String,

  @(DescriptionAnnotation @field)("System this sensor belongs to")
  systemId: String,

  @(DescriptionAnnotation @field)("Human-friendly description")
  description: Option[String],

  @(DescriptionAnnotation @field)("UI display options")
  options: Option[String],

  @(DescriptionAnnotation @field)("Tags") @(ColumnFamilyAnnotation @field)("t")
  tags: Map[String, String]
) {
  def rowKey = Sensor.rowKey(id, deviceId, systemId)

  def addTags(newTags: Map[String, String]) =
    copy(tags = tags ++ newTags)
}

object Sensor extends Logging {
  // XXX: This is obviously crap, but until we fix ids everywhere...
  type UniqueSensorId = (String, String, String)

  def rowKey(id: String, deviceId: String, systemId: String): Array[Byte] =
    bytesFrom(systemId) ++ zeroByte ++ bytesFrom(deviceId) ++ zeroByte ++ bytesFrom(id)

  @HEntityController(
    httpMethod = "GET",
    description = "Returns the sensors for the specified device."
  )
  def forDevice(
    @DescriptionAnnotation("Id of device to get the sensors of") deviceId: String,
    @DescriptionAnnotation("Id of the system of the device") systemId: String
  )(implicit entityReg: HEntityRegistry, hContext: HContext): Iterable[Sensor] = {
    val sensorEntity = entityReg.getEntity[Sensor]
    val f = new PrefixFilter(bytesFrom(systemId) ++ zeroByte ++ bytesFrom(deviceId) ++ zeroByte)
    sensorEntity.io.scan(filter = Some(f))
  }

  @HEntityController(
    httpMethod = "POST",
    description = "Performs tag operations on a sensor."
  )
  def tags(
    @DescriptionAnnotation("Sensor id") id: String,
    @DescriptionAnnotation("Device id") deviceId: String,
    @DescriptionAnnotation("System id") systemId: String,
    @DescriptionAnnotation("Tags to add to the device") put: Option[Map[String, String]],
    @DescriptionAnnotation("Tags to remove from the device") delete: Option[Array[String]]
  )(implicit entityReg: HEntityRegistry, hContext: HContext): Map[String, String] = {
    val sensorEntity = entityReg.getEntity[Sensor]
    val table = sensorEntity.io.table.name
    val key = rowKey(id, deviceId, systemId)
    import com.ubeeko.htalk.criteria._
    for { toDelete <- delete if toDelete.nonEmpty } {
      val deleteStatement = (new Delete(Table(table)) /: toDelete) { case (d, column) =>
        d delete(key, "t", column)
      }
      deleteStatement execute
    }
    for { toPut <- put if toPut.nonEmpty } {
      val putStatement = (new Put(Table(table)) /: toPut) { case(p, (column, value)) =>
        p put(key, "t", column, value)
      }
      putStatement execute
    }
    sensorEntity.io.get(sensorEntity.io.asRowKey(key)).map(_.tags).getOrElse(Map.empty)
  }

  @HEntityController(
    httpMethod = "GET",
    description = "Returns all existing tags across all sensors."
  )
  def allTags()
             (implicit entityReg: HEntityRegistry, hContext: HContext): Iterable[(String, String)] = {
    val sensorEntity = entityReg.getEntity[Sensor]
    val results = (sensorEntity.io.table.name get rows family("t")) ~ {rs =>
      rs.getCells("t").toList.map { case (key, value) =>
        (bytesTo[String](key.value), bytesTo[String](value))
      }.toSet
    }
    (Set.empty[(String, String)] /: results) { case (set, values) =>
      set ++ values
    }
  }

  @HEntityController(
    httpMethod = "GET",
    description = "Returns the sensors tagged with the specified tags"
  )
  def forTags(@DescriptionAnnotation("Tags the sensors must match") tags: Map[String, String])
             (implicit entityReg: HEntityRegistry, hContext: HContext): Iterable[Sensor] = {
    val sensorEntity = entityReg.getEntity[Sensor]
    val filters = tags.toList.map { case (key, value) =>
      val f = new SingleColumnValueFilter(bytesFrom("t"), bytesFrom(key), CompareFilter.CompareOp.EQUAL, bytesFrom(value))
      f.setFilterIfMissing(true)
      f
    }
    val f = if (filters.nonEmpty)
      Some(new FilterList(filters))
    else
      None
    sensorEntity.io.scan(filter = f)
  }

  @HEntityController(
    httpMethod = "GET",
    description = "Returns the number of sensors"
  )
  @DevOnly
  def count()(implicit entityReg: HEntityRegistry, hContext: HContext): Long = {
    val entity = entityReg.getEntity[Sensor]
    val table = entity.io.table.name
    val counters = table get rows count

    counters.rows
  }
}
