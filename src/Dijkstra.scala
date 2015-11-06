import scala.collection.mutable
import scala.collection.mutable.ListBuffer

/**
  * Created by mcooksey on 11/6/2015.
  */
object Dijkstra {
  type Path[Key] = (Double, List[Key])

  def getShortestPath(hopList:mutable.HashMap[String,Hop], origin:String, destination:String)= {
    val result = new mutable.HashMap[String, List[(Double,String)]]
    hopList.foreach{
      case(key, hop) => {
        if (result.keySet.exists(_ == hop.hub1.id)) {
          var tempList = new ListBuffer[(Double, String)]()
          tempList = result(hop.hub1.id).to[ListBuffer]
          val tuple2 = new Tuple2(hop.miles.toDouble, hop.hub2.id)
          tempList += tuple2
          result.put (hop.hub1.id,tempList.to[List])
        }
        else {
          result.put (hop.hub1.id,List((hop.miles.toDouble, hop.hub2.id)))
        }
        if (result.keySet.exists(_== hop.hub2.id)) {
          var tempList = new ListBuffer[(Double,String)]()
          tempList = result(hop.hub2.id).to[ListBuffer]
          val tuple2 = new Tuple2(hop.miles.toDouble, hop.hub1.id)
          tempList += tuple2
          result.put (hop.hub2.id,tempList.to[List])
        }
        else {
          result.put(hop.hub2.id, List((hop.miles.toDouble, hop.hub1.id)))
        }
      }
    }
    Dijkstra[String](result, List((0.0, List(origin))), destination, Set())
  }

  def Dijkstra[Key](lookup: mutable.HashMap[Key, List[(Double, Key)]], fringe: List[Path[Key]], dest: Key, visited: Set[Key]): Path[Key] = fringe match {
    case (dist, path) :: fringe_rest => path match {case key :: path_rest =>
      if (key == dest) (dist, path.reverse)
      else {
        val paths = lookup(key).flatMap {case (d, key) => if (!visited.contains(key)) List((dist + d, key :: path)) else Nil}
        val sorted_fringe = (paths ++ fringe_rest).sortWith {case ((d1, _), (d2, _)) => d1 < d2}
        Dijkstra(lookup, sorted_fringe, dest, visited + key)
      }
    }
    case Nil => (0, List())
  }
}
