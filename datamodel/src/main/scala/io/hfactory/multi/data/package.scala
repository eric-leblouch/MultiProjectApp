package io.hfactory.multi

import java.text.{DateFormat, SimpleDateFormat}
import java.util.Date

import com.ubeeko.htalk.bytesconv._

import com.ubeeko.json._
import com.ubeeko.jsonconv.JsonConv
import com.ubeeko.stringconv.StringConv

package object data {
  /** Single ZERO byte array. Intended to be used as a separator in rowkeys. */
  val zeroByte = Array(0.toByte)

  implicit object DateBytesConv extends BytesConv[Date] {
    protected def fromBytes(b: Array[Byte]): Date = new Date(bytesTo[Long](b))
    protected def toBytes(x: Date): Array[Byte] = bytesFrom(x.getTime)
  }

  def dateFormat: DateFormat = {
    // The 'X' at the end is to properly handle ISO timezones (Z, -02:00, +02, etc).
    val df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX")
    df.setLenient(false)
    df
  }

  implicit object DateStringConv extends StringConv[Date] {
    protected def fromString(s: String): Date = dateFormat.parse(s)
    override protected def toString(x: Date): String = dateFormat.format(x)
  }

  implicit object DateJsonConv extends JsonConv[Date] {
    protected def fromJson(j: JValue): Date = j match {
      case JString(s) => DateStringConv.apply(s)
      case _          => throw invalidValue(j)
    }
    protected def toJson(x: Date): JValue = JString(DateStringConv.apply(x))
  }
}
