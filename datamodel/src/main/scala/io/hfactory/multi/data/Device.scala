package io.hfactory.multi.data

import com.ubeeko.hfactory.context.HContext
import com.ubeeko.hfactory.entities.HEntityRegistry
import com.ubeeko.hfactory.entities.annotations.HBase.ColumnFamilyAnnotation
import com.ubeeko.hfactory.annotations.DescriptionAnnotation
import com.ubeeko.hfactory.entities.annotations.HEntityController

import com.ubeeko.htalk.bytesconv._
import com.ubeeko.htalk.criteria._

import org.apache.hadoop.hbase.filter._

import FilterHelpers._

import scala.annotation.meta.field

case class Device(
  @(DescriptionAnnotation @field)("Device identifier")
  id: String,

  @(DescriptionAnnotation @field)("System this device belongs to")
  systemId: String,

  @(DescriptionAnnotation @field)("Tags")
  @(ColumnFamilyAnnotation @field)("t")
  tags: Map[String, String]
) {
  def rowKey: Array[Byte] = Device.rowKey(systemId, id)

  def addTags(newTags: Map[String, String]) =
    copy(tags = tags ++ newTags)
}

object Device {
  def rowKey(systemId: String, deviceId: String): Array[Byte] =
    bytesFrom(systemId) ++
    zeroByte ++
    bytesFrom(deviceId)

  @HEntityController(
    httpMethod = "GET",
    description = "Returns the device with the specified id."
  )
  @DescriptionAnnotation(
    "The list may contain more than one device if there are devices with the same id across different systems."
  )
  def withId(@DescriptionAnnotation("Device id")id: String)
            (implicit entityReg: HEntityRegistry, hContext: HContext): Iterable[Device] = {
    val deviceEntity = entityReg.getEntity[Device]
    deviceEntity.io.scan(filter = Some(valueFilter("id", id)))
  }

  @HEntityController(
    httpMethod = "GET",
    description = "Returns all devices for the specified system."
  )
  def forSystem(@DescriptionAnnotation("System to get the devices of") systemId: String)
               (implicit entityReg: HEntityRegistry, hContext: HContext): Iterable[Device] = {
    val deviceEntity = entityReg.getEntity[Device]
    val systemFilter = new PrefixFilter(bytesFrom(systemId) ++ zeroByte)
    deviceEntity.io.scan(filter = Some(systemFilter))
  }

  @HEntityController(
    httpMethod = "POST",
    description = "Performs tag operations on a device."
  )
  def tags(
    @DescriptionAnnotation("Device id") id: String,
    @DescriptionAnnotation("System id") systemId: String,
    @DescriptionAnnotation("Tags to add to the device") put: Option[Map[String, String]],
    @DescriptionAnnotation("Tags to remove from the device") delete: Option[Array[String]]
  )(implicit entityReg: HEntityRegistry, hContext: HContext): Map[String, String] = {
    val deviceEntity = entityReg.getEntity[Device]
    val table = deviceEntity.io.table.name
    val key = rowKey(systemId, id)
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
    deviceEntity.io.get(deviceEntity.io.asRowKey(key)).map(_.tags).getOrElse(Map.empty)
  }
}
