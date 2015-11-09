/**
  * Created by mcooksey on 10/29/2015.
  */

import scala.io.Source
import scala.collection.mutable


class Hub(val city: String, val state: String, var id: String) {
  def this(city: String, state: String) = {
    this(city, state, (city + "-" + state).toLowerCase())
  }

  override def toString = {
    s"city: $city, state:$state"
  }

  def send(parcel:Parcel):Unit = {
    val nextHop = SimUPS.hops(parcel.route.dequeue().id)
    val nextHub = nextHop.hub2
    print(s"${this.city} is sending ${parcel.name} to ${nextHub.city}")
    for (i <- 1 to 3) {
      print(".")
      Thread.sleep(1000)
    }
    println()
    nextHub.receive(parcel)
  }

  def receive(parcel:Parcel):Unit = {
    if (this.id.equals(parcel.destination.id)) {
      println(s"${parcel.name} has reached destination in ${this.city}!")
    }
    else {
      if (parcel.origin.id.equals(this.id)) {
        println(s"Received new shipment ${parcel.name} in ${this.city}.")
      }
      else {
        println(s"${parcel.name} has been received by ${this.city}.")
      }
      this.send(parcel)
    }
  }
}

class Hop(val hub1: Hub, val hub2: Hub, val hours: Int, val minutes: Int, val miles: Int, var id: String) {
  def this(hub1: Hub, hub2: Hub, hours: Int, minutes: Int, miles: Int) = {
    this(hub1, hub2, hours, minutes, miles, hub1.id + ">" + hub2.id)
  }

  override def toString = {
    s"id: $id, hours: $hours, minutes: $minutes, miles: $miles"
  }
}

class Parcel(val origin: Hub, val destination:Hub, val name:String, val route:mutable.Queue[Hop]) {

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
      println("2. Delete a hop")
      println("3. Delete a hub")
      println("4. Reload default hubs")
      println("5. Show available hubs")
      println("6. Route a package")
      println("7. Quit")
      println("--------------------------")
      println("==========================")
      print("\nEnter the number for desired operation: ")

      val option = scala.io.StdIn.readChar()

      option match {
        case '1' => addHop()
        case '2' => deleteHop()
        case '3' => deleteHub()
        case '4' => reloadHubs()
        case '5' => showAllHubs()
        case '6' => routePackage()
        case '7' => continue = false
        case '8' => debug()
        case _ => println("Invalid Entry")
      }
      println()

    }
  }

  def debug() = {
    val route = new mutable.Queue[String]
    route.enqueue("test")
    route.enqueue("Blah")
    while (!route.isEmpty) {
      val res = route.dequeue()
      println(res)
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

    putHop(hub1, hub2, hours, minutes, miles)
  }

  def putHop(hub1: Hub, hub2: Hub, hours: Int, minutes: Int, miles: Int) {
    val hop = new Hop(hub1, hub2, hours, minutes, miles)
    val hopBack = new Hop(hub2, hub1, hours, minutes, miles)

    if (!hubs.keySet.contains(hub1.id)) {
      hubs.put(hub1.id, hub1)
    }
    hops.put(hop.id, hop)

    if (!hubs.keySet.contains(hub2.id)) {
      hubs.put(hub2.id, hub2)
    }
    hops.put(hopBack.id, hopBack)
  }

  def deleteHub() = {
    println("\n*******DELETE HUB*********")
    print("Enter hub to delete: ")
    val hub = scala.io.StdIn.readLine().toLowerCase()
    if (hubs.keySet.contains(hub)) {
      hubs.remove(hub)
      hops.foreach {
        case (key, hop) => {
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

  def deleteHop() = {
    println("\n*******DELETE HOP*********")
    var validOrigin = false
    var origin = new String
    var validDestination = false
    var destination = new String
    do {
      print("Enter the origin of the hop: ")
      origin = scala.io.StdIn.readLine().toLowerCase()
      if (hubs.keySet.contains(origin)) {
        validOrigin = true
      }
      else {
        println("Invalid origin entered.")
      }
    } while (!validOrigin)

    do {
      print("Enter the destination of the hop: ")
      destination = scala.io.StdIn.readLine().toLowerCase()
      if (hubs.keySet.contains(destination)) {
        validDestination = true
      }
      else {
        println("Invalid destination entered.")
      }
    } while (!validDestination)
    if (hops.keySet.contains(origin + ">" + destination)) {
      hops.remove(origin + ">" + destination)
      if (hops.keySet.contains(destination + ">" + origin)) {
        hops.remove(destination + ">" + origin)
      }
      println("Hop removed.")
    }
    else {
      println("Hop does not exist.")
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
    hubs.foreach { case (key, hub) => println(hub.city + ", " + hub.state) }
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
      if (hubs.keySet.contains(origin)) {
        validOrigin = true
      }
      else {
        println("Invalid origin entered.")
      }
    } while (!validOrigin)

    do {
      print("Enter a destination: ")
      destination = scala.io.StdIn.readLine().toLowerCase()
      if (hubs.keySet.contains(destination)) {
        validDestination = true
      }
      else {
        println("Invalid destination entered.")
      }
    } while (!validDestination)

    print("Enter a description for the package:")
    val description = scala.io.StdIn.readLine()

    val shortestPath = Dijkstra.getShortestPath(hops, origin, destination)

    val path = shortestPath._2.to[List]

    val route = new mutable.Queue[Hop]()

    if (path.isEmpty) {
      println(s"No route found between $origin and $destination...")
    }
    else {
      println("\n**Routing package**")
      print("Path is: ")

      for (i <- path.indices) {
        if (i+1 < path.size) {
          print(path(i) + " > ")
          val hop = hops(path(i) + ">" + path(i + 1))
          route.enqueue(hop)
        }
        else {
          println(path(i) + s". Total miles: ${shortestPath._1}\n")
        }
      }
      val parcel = new Parcel(hubs(origin), hubs(destination), "\"" + description + "\"", route)
      hubs(origin).receive(parcel)

    }
  }


  def initializeHubList(inFile: String) = {
    val source = Source.fromFile(inFile)
    for (line <- source.getLines()) {
      val cols = line.split(",").map(_.trim)
      val hub1 = new Hub(cols(0), cols(1))
      val hub2 = new Hub(cols(2), cols(3))
      val hours = cols(4).toInt
      val minutes = cols(5).toInt
      val miles = cols(6).toInt

      putHop(hub1, hub2, hours, minutes, miles)
    }
  }

}
