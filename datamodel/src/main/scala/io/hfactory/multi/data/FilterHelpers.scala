package io.hfactory.multi.data

import com.ubeeko.htalk.bytesconv._
import org.apache.hadoop.hbase.filter.{CompareFilter, SingleColumnValueFilter, Filter}

private [data] object FilterHelpers {
  def valueFilter[T](fieldName: String, value: T)(implicit conv: BytesConv[T]): Filter = {
    val filter = {
      val f = bytesFrom("d")
      val q = bytesFrom(fieldName)
      val v = bytesFrom(value)
      new SingleColumnValueFilter(f, q, CompareFilter.CompareOp.EQUAL, v)
    }
    filter.setFilterIfMissing(true)
    filter
  }

  def maybeFilter[T](fieldName: String, value: Option[T])(implicit conv: BytesConv[T]): Option[Filter] =
    value.map(valueFilter(fieldName, _))
}
