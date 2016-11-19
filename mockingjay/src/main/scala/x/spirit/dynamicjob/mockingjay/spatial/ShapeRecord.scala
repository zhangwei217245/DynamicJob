package x.spirit.dynamicjob.mockingjay.spatial


import geotrellis.vector.{Geometry, Point}

/**
  * Created by zhangwei on 11/18/16.
  */
class ShapeRecord[T](geometry: Geometry, dataFields: Map[String, T]) {

  val centroid = geometry.centroid;

  def covers(x:Double, y:Double):Boolean = geometry.jtsGeom.covers(Point(x,y).jtsGeom)

  def getDataFields:Map[String, T] = dataFields

  def apply(i: Int): Double = {
    val cpoint = centroid.as[Point].getOrElse(Point(0.0, 0.0))
    if (i == 0) {
      cpoint.x
    } else {
      cpoint.y
    }
  }

  def radius:Double = {
    var xmax = -180.0
    var xmin = 180.0
    var ymax = -90.0
    var ymin = 90.0
    geometry.jtsGeom.getBoundary.getCoordinates.foreach({coord =>
      if (xmax < coord.x) {xmax = coord.x}
      if (ymax > coord.y) {ymax = coord.y}
      if (xmin > coord.x) {xmin = coord.x}
      if (ymin < coord.y) {ymin = coord.y}
    })
    val r_x = Math.abs(xmax - xmin)
    val r_y = Math.abs(ymax - ymin)
    if (r_x > r_y) {
      r_x
    } else {
      r_y
    }
  }
}
