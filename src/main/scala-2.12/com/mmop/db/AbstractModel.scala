package com.mmop.db

trait AbstractModel {
  val fields : Map[String, Any]

  def getFieldDecimal(name: String): BigDecimal = BigDecimal(getField[java.math.BigDecimal](name))
  def getField[T](name: String): T = getFieldOption(name).get
  def getFieldOption[T](name: String): Option[T] = fields.get(name).map(_.asInstanceOf[T])
}