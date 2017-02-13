package io.hfactory.multi.data

import com.ubeeko.hfactory.annotations.DescriptionAnnotation
import com.ubeeko.hfactory.entities.annotations.HBase.ColumnFamilyAnnotation
import com.ubeeko.htalk.bytesconv._

import scala.annotation.meta.field

case class System(
  @(DescriptionAnnotation @field)("Identifier")
  id: String,

  @(DescriptionAnnotation @field)("Tags")
  @(ColumnFamilyAnnotation @field)("t")
  tags: Map[String, String]
) {
  def rowKey: Array[Byte] = bytesFrom(id)
}
