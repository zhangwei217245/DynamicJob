package x.spirit.dynamicjob.mockingjay.spatial
import scala.collection._

/**
  * Created by zhangwei on 11/18/16.
  */
class QuadTreeIndex[A](xmin:Double,xmax:Double,
                                        ymin:Double,ymax:Double, MaxObjs:Int = 100)
                      (implicit ev$1: A => ShapeRecord[Double]) {


  private class Node(cx:Double,cy:Double,sx:Double,sy:Double,
                     val objects:mutable.Buffer[A],var children:Array[Node]) {
    def whichChild(x:Double, y:Double): Int ={
      (if(x>cx) 1 else 0)+(if(y>cy) 2 else 0)
    }
    def whichChild(obj:A):Int = {
      whichChild(obj(0), obj(1))
    }
    def makeChildren() {
      children = Array(
        new Node(cx-sx/4,cy-sy/4,sx/2,sy/2,mutable.Buffer(),null),
        new Node(cx+sx/4,cy-sy/4,sx/2,sy/2,mutable.Buffer(),null),
        new Node(cx-sx/4,cy+sy/4,sx/2,sy/2,mutable.Buffer(),null),
        new Node(cx+sx/4,cy+sy/4,sx/2,sy/2,mutable.Buffer(),null)
      )
    }
    def overlap(obj:A,radius:Double):Boolean = {
      obj(0)-radius<cx+sx/2 && obj(0)+radius>cx-sx/2 &&
        obj(1)-radius<cy+sy/2 && obj(1)+radius>cy-sy/2
    }
  }

  private val root = new Node((xmax+xmin)/2,(ymax+ymin)/2,xmax-xmin,ymax-ymin,
    mutable.Buffer[A](),null)

  def searchByCoordinates(x:Double, y:Double):mutable.Buffer[A] = {
    val ret = mutable.Buffer[A]()
    searchRecurByCoordinates(x,y,root,ret)
    ret
  }

  def searchRecurByCoordinates(x:Double, y:Double, n:Node, ret:mutable.Buffer[A]) : Unit = {
    if (n.children == null){
      ret ++= n.objects
    } else {
      searchRecurByCoordinates(x, y, n.children(n.whichChild(x,y)), ret)
    }
  }

  def add(obj:A) {
    addRecur(obj,root)
  }

  private def addRecur(obj:A,n:Node) {
    if(n.children==null) {
      if(n.objects.length<MaxObjs) n.objects += obj
      else {
        n.makeChildren()
        for(o <- n.objects) {
          addRecur(o,n.children(n.whichChild(o)))
        }
        n.objects.clear
        addRecur(obj,n.children(n.whichChild(obj)))
      }
    } else {
      addRecur(obj,n.children(n.whichChild(obj)))
    }
  }

  def searchNeighbors(obj:A,radius:Double):mutable.Buffer[A] = {
    val ret = mutable.Buffer[A]()
    searchRecur(obj,radius,root,ret)
    ret
  }

  private def searchRecur(obj:A,radius:Double,n:Node,ret:mutable.Buffer[A]) {
    if(n.children==null) {
      ret ++= n.objects.filter(o => distance(o,obj)<radius)
    } else {
      for(child <- n.children; if !child.overlap(obj,radius))
        searchRecur(obj,radius,child,ret)
    }
  }

  private def distance(a:A,b:A):Double = {
    val dx = a(0)-b(0)
    val dy = a(1)-b(1)
    math.sqrt(dx*dx+dy*dy)
  }
}