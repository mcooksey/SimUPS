/**
  * Created by mcooksey on 10/29/2015.
  */

import scala.io.Source
import scala.collection.mutable

class Hub(val city: String, val state: String, var id:String) {
  def this(city:String, state:String) = {
    this(city, state, (city + "-" + state).toLowerCase())
  }
  override def toString = {
    s"city: $city, state:$state"
  }
}

class Hop(val hub1: Hub, val hub2: Hub, val hours: Int, val minutes: Int, val miles: Int, var id:String) {
  def this(hub1: Hub, hub2: Hub, hours: Int, minutes: Int, miles: Int) = {
    this(hub1, hub2, hours, minutes, miles, hub1.id + ">" + hub2.id)
  }
  override def toString = {
    s"id: $id, hours: $hours, minutes: $minutes, miles: $miles"
  }
}

object SimUPS {
  var hubs = new mutable.HashMap[String, Hub]
  var hops = new mutable.HashMap[String, Hop]

  def main(args: Array[String]) = {
    initializeHubList("C:\\Users\\mcooksey\\Documents\\GitHub\\SimUPS\\hubs.csv")

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
        case '7' => debug()
        case _ => println("Invalid Entry")
      }
      println()

    }
  }

  def debug() = {
    hops.foreach{case(k,v) => println(k, v.toString)}
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

    putHop(hub1, hub2, hours, minutes, miles)
  }

  def putHop(hub1:Hub, hub2:Hub, hours:Int, minutes:Int, miles:Int) {
    val hop = new Hop(hub1, hub2, hours, minutes, miles)
    val hopBack = new Hop(hub2, hub1, hours, minutes, miles)

    if (!hubs.keySet.exists(_ == hub1.id)) {
      hubs.put(hub1.id, hub1)
    }
    hops.put(hop.id, hop)

    if (!hubs.keySet.exists(_== hub2.id)) {
      hubs.put(hub2.id, hub2)
    }
    hops.put(hopBack.id, hopBack)
  }

  def deleteHub() = {
    println("\n*******DELETE HUB*********")
    print("Enter hub to delete: ")
    val hub = scala.io.StdIn.readLine().toLowerCase()
    if (hubs.keySet.exists(_ == hub)) {
      hubs.remove(hub)
      hops.foreach{
        case(key, hop) => {
          if (hop.hub1.id.equals(hub) || hop.hub2.id.equals(hub)) {
            hops.remove(key)
          }
        }
      }
      println("Hub and all associated hops deleted.")
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
          initializeHubList("C:\\Users\\mcooksey\\Documents\\GitHub\\SimUPS\\hubs.csv")
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
    println(s"*********${hubs.size} TOTAL*********")
    hubs.foreach {case (key,hub) => println(hub.city + ", " + hub.state)}
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
          if (hubs.keySet.exists(_ == origin)) {
            validOrigin = true
          }
          else {
            println("Invalid origin entered.")
          }
        } while (!validOrigin)

        do {
          print("Enter a destination: ")
          destination = scala.io.StdIn.readLine().toLowerCase()
          if (hubs.keySet.exists(_ == destination)) {
            validDestination = true
          }
          else {
            println("Invalid destination entered.")
          }
        } while (!validDestination)

        val shortestPath = Dijkstra.getShortestPath(hops, origin, destination)

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


  def initializeHubList(inFile:String) = {
    val source = Source.fromFile(inFile)
    for (line <- source.getLines()) {
      val cols = line.split(",").map(_.trim)
      // do whatever you want with the columns here
      val hub1 = new Hub(cols(0), cols(1))
      val hub2 = new Hub(cols(2), cols(3))
      val hours = cols(4).toInt
      val minutes = cols(5).toInt
      val miles = cols(6).toInt

      putHop(hub1, hub2, hours, minutes, miles)
    }
  }

}
