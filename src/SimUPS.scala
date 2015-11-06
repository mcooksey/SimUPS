/**
  * Created by mcooksey on 10/29/2015.
  */

import java.io.InputStream

import scala.io.Source
import scala.collection.mutable.ListBuffer
import scala.collection.mutable

class Hub(val city: String, val state: String, var id:String) {
  def this(city:String, state:String) = {
    this(city, state, city + "-" + state)
  }
}

class Hop(val hub1: Hub, val hub2: Hub, val hours: Int, val minutes: Int, val miles: Int) {
}



object SimUPS {
  var hubListDijkstra = new mutable.HashMap[String, List[(Double,String)]]()
  var hubs = new mutable.ListBuffer[Hub]
  var hops = new mutable.ListBuffer[Hop]

  type Path[Key] = (Double, List[Key])

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

  def main(args: Array[String]) = {
    hubListDijkstra = initializeHubList("C:\\TEMP\\hubs.csv")

    var continue = true

    while (continue) {
      println("==========================")
      println("What would you like to do?")
      println("--------------------------")
      println("1. Add a hop")
      println("2. Delete a hub")
      println("3. Reload default hubs")
      println("4. Show available hubs")
      println("5. Route a package")
      println("6. Quit")
      println("--------------------------")
      println("==========================")
      print("\nEnter the number for desired operation: ")

      val option = scala.io.StdIn.readChar()

       option match {
        case '1' => addHop()
        case '2' => deleteHub()
        case '3' => reloadHubs()
        case '4' => showAllHubs()
        case '5' => routePackage()
        case '6' => continue = false
        case _ => println("Invalid Entry")
      }
      println()

    }
  }

  def addHop() = {
    println("Add a hop")
    print("Enter Origin City: ")
    val originCity = scala.io.StdIn.readLine().toLowerCase()
    print("Enter Origin State: ")
    val originState = scala.io.StdIn.readLine().toLowerCase()
    print("Enter Destination City: ")
    val destinationCity = scala.io.StdIn.readLine().toLowerCase()
    print("Enter Destination State: ")
    val destinationState = scala.io.StdIn.readLine().toLowerCase()
    print("Enter travel time hours: ")
    val hours = scala.io.StdIn.readInt()
    print("Enter travel time minutes: ")
    val minutes = scala.io.StdIn.readInt()
    print("Enter miles between hubs: ")
    val miles = scala.io.StdIn.readInt()

    val hub1 = new Hub(originCity, originState)
    val hub2 = new Hub(destinationCity, destinationState)
    val path = new Hop(hub1, hub2, hours, minutes, miles)
    val path2 = new Hop(hub2, hub1, hours, minutes, miles)

    if (hubListDijkstra.keySet.exists(_ == hub1.id)) {
      var tempList = new ListBuffer[(Double, String)]()
      tempList = hubListDijkstra(hub1.id).to[ListBuffer]
      val tuple2 = new Tuple2(path.miles.toDouble, hub2.id)
      tempList += tuple2
      hubListDijkstra.put (hub1.id,tempList.to[List])
    }

    else {
      hubListDijkstra.put (hub1.id,List((path.miles.toDouble, hub2.id)))
      hubs += hub1
    }

    hops += path

    if (hubListDijkstra.keySet.exists(_== hub2.id)) {
      var tempList = new ListBuffer[(Double,String)]()
      tempList = hubListDijkstra(hub2.id).to[ListBuffer]
      val tuple2 = new Tuple2(path.miles.toDouble, hub1.id)
      tempList += tuple2
      hubListDijkstra.put (hub2.id,tempList.to[List])
    }
    else {
      hubListDijkstra.put(hub2.id, List((path.miles.toDouble, hub1.id)))
      hubs += hub2
    }

    hops += path2
  }

  def deleteHub() = {
    println("\n*******DELETE HUB*********")
    print("Enter hub to delete: ")
    val hub = scala.io.StdIn.readLine().toLowerCase()
    if (hubListDijkstra.keySet.exists(_ == hub)) {
      hubListDijkstra.remove(hub)

      println("Hub deleted.")
    }
    else {
      println("Hub does not exist.")
    }
  }

  def reloadHubs() = {
    println("\n*******RELOAD HUBS********")

    var continue = true
    while (continue) {
      print("This will load all hubs that do not exist from the .csv file. Continue? (y/n): ")
      val option = scala.io.StdIn.readChar().toLower

      option match {
        case 'y' => {
          //1hubs.clear()
          hubListDijkstra = initializeHubList("C:\\TEMP\\hubs.csv")
          println("Hubs reloaded.")
          continue = false
        }
        case 'n' => continue = false
        case _ => println("Invalid Entry")
      }
    }
  }

  def showAllHubs() = {
    println("\n******Available Hubs******")
    println(s"*********${hubListDijkstra.size} TOTAL*********")
    hubListDijkstra.foreach {case (k,v) => println(k)}
  }

  def routePackage() = {
    println("\n*****ROUTE A PACKAGE******")
        var validOrigin = false
        var origin = new String
        var validDestination = false
        var destination = new String
        do {
          print("Enter an origin: ")
          origin = scala.io.StdIn.readLine().toLowerCase()
          if (hubListDijkstra.keySet.exists(_ == origin)) {
            validOrigin = true
          }
          else {
            println("Invalid origin entered.")
          }
        } while (!validOrigin)

        do {
          print("Enter a destination: ")
          destination = scala.io.StdIn.readLine().toLowerCase()
          if (hubListDijkstra.keySet.exists(_ == destination)) {
            validDestination = true
          }
          else {
            println("Invalid destination entered.")
          }
        } while (!validDestination)

        val shortestPath = Dijkstra[String](hubListDijkstra, List((0.0, List(origin))), destination, Set())

        val path = shortestPath._2.to[List]
        val totalMiles = shortestPath._1.toDouble

        if (path.isEmpty) {
          println(s"No route found between $origin and $destination...")
        }

        else {
          for (city <- path) {
            println(city)
          }
          println(s"Total miles: $totalMiles")
        }
  }


  def initializeHubList(inFile:String):mutable.HashMap[String, List[(Double, String)]] = {
    val source = Source.fromFile(inFile)
    for (line <- source.getLines()) {
      val cols = line.split(",").map(_.trim)
      // do whatever you want with the columns here
      val hub1 = new Hub(cols(0), cols(1))
      val hub2 = new Hub(cols(2), cols(3))
      val path = new Hop(hub1, hub2, cols(4).toInt, cols(5).toInt, cols(6).toInt)

      if (hubListDijkstra.keySet.exists(_ == hub1.id)) {
        var tempList = new ListBuffer[(Double, String)]()
        tempList = hubListDijkstra(hub1.id).to[ListBuffer]
        val tuple2 = new Tuple2(path.miles.toDouble, hub2.id)
        tempList += tuple2
        hubListDijkstra.put (hub1.id,tempList.to[List])
      }

      else {
        hubListDijkstra.put (hub1.id,List((path.miles.toDouble, hub2.id)))
      }

      if (hubListDijkstra.keySet.exists(_== hub2.id)) {
        var tempList = new ListBuffer[(Double,String)]()
        tempList = hubListDijkstra(hub2.id).to[ListBuffer]
        val tuple2 = new Tuple2(path.miles.toDouble, hub1.id)
        tempList += tuple2
        hubListDijkstra.put (hub2.id,tempList.to[List])
      }
      else {
        hubListDijkstra.put(hub2.id, List((path.miles.toDouble, hub1.id)))
      }

    }
    source.close()
    hubListDijkstra
  }

}
