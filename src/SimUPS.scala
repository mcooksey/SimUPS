/**
  * Created by mcooksey on 10/29/2015.
  */

import scala.collection.mutable

import java.sql.{PreparedStatement, Connection, DriverManager}

class Hub(val city: String, val state: String, var id: String) {
  def this(city: String, state: String) = {
    this(city, state, (city + "-" + state).toLowerCase())
  }

  override def toString = {
    s"city: $city, state:$state"
  }

  def send(parcel: Parcel): Unit = {
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

  def receive(parcel: Parcel): Unit = {
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

class Parcel(val origin: Hub, val destination: Hub, val name: String, val route: mutable.Queue[Hop]) {

}

object SimUPS {
  var hubs = new mutable.HashMap[String, Hub]
  var hops = new mutable.HashMap[String, Hop]


  def main(args: Array[String]) = {
    loadHubList()

    var continue = true

    while (continue) {
      println("==========================")
      println("What would you like to do?")
      println("--------------------------")
      println("1. Add a hop")
      println("2. Delete a hop")
      println("3. Delete a hub")
      println("4. Show available hubs")
      println("5. Load hub list")
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
        case '4' => showAllHubs()
        case '5' => loadHubList()
        case '6' => routePackage()
        case '7' => continue = false
        case '8' => debug()
        case _ => println("Invalid Entry")
      }
      println()

    }
  }

  def debug() = {
    val driver = "org.postgresql.Driver"
    val url = "jdbc:postgresql:sim_ups"
    val username = "mcooksey"
    val password = "csa1csa1"

    var connection: Connection = null

    Class.forName(driver)
    connection = DriverManager.getConnection(url, username, password)
    val sql = "INSERT INTO routes (origin, origin_state, origin_air, destination, destination_state, destination_air, hours, minutes, miles) VALUES (?, ?, false, ?, ?, false, ?, ?, ?)"
    val statement: PreparedStatement = connection.prepareStatement(sql)
    statement.setString(1, "tupelo")
    statement.setString(2, "ms")
    statement.setString(3, "columbus")
    statement.setString(4, "ms")
    statement.setInt(5, 0)
    statement.setInt(6, 59)
    statement.setInt(7, 60)

    statement.executeUpdate()
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

    val driver = "org.postgresql.Driver"
    val url = "jdbc:postgresql:sim_ups"
    val username = "mcooksey"
    val password = "csa1csa1"

    var connection: Connection = null

    Class.forName(driver)
    connection = DriverManager.getConnection(url, username, password)
    val sql = "INSERT INTO routes (origin, origin_state, origin_air, destination, destination_state, destination_air, hours, minutes, miles) VALUES (?, ?, false, ?, ?, false, ?, ?, ?)"
    val statement: PreparedStatement = connection.prepareStatement(sql)
    statement.setString(1, originCity)
    statement.setString(2, originState)
    statement.setString(3, destinationCity)
    statement.setString(4, destinationState)
    statement.setInt(5, hours)
    statement.setInt(6, minutes)
    statement.setInt(7, miles)

    statement.executeUpdate()

    loadHubList()
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
      deleteHubFromDB(hub)
      loadHubList()
      println("Hub and all associated hops deleted.")
    }
    else {
      println("Hub does not exist.")
    }
  }

  def deleteHubFromDB(key:String) = {
    val driver = "org.postgresql.Driver"
    val url = "jdbc:postgresql:sim_ups"
    val username = "mcooksey"
    val password = "csa1csa1"

    var connection: Connection = null
    val hub = hubs(key)
    Class.forName(driver)
    connection = DriverManager.getConnection(url, username, password)
    val sql = "DELETE FROM routes WHERE (lower(origin) = ? AND lower(origin_state) = ?) OR (lower(destination) = ? AND lower(destination_state) = ?)"
    val statement: PreparedStatement = connection.prepareStatement(sql)
    statement.setString(1, hub.city)
    statement.setString(2, hub.state)
    statement.setString(3, hub.city)
    statement.setString(4, hub.state)

    statement.executeUpdate()
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
      deleteHopFromDB(origin + ">" + destination)
      loadHubList()
      println("Hop removed.")
    }
    else {
      println("Hop does not exist.")
    }
  }

  def deleteHopFromDB(key:String) = {
    val hop = hops(key)

    val driver = "org.postgresql.Driver"
    val url = "jdbc:postgresql:sim_ups"
    val username = "mcooksey"
    val password = "csa1csa1"

    var connection: Connection = null
    Class.forName(driver)
    connection = DriverManager.getConnection(url, username, password)
    val sql = "DELETE FROM routes WHERE ((lower(origin) = ? AND lower(origin_state) = ?) AND (lower(destination) = ? AND lower(destination_state) = ?))" +
      " OR ((lower(origin) = ? AND lower(origin_state) = ?) AND (lower(destination) = ? AND lower(destination_state) = ?))"
    val statement: PreparedStatement = connection.prepareStatement(sql)
    statement.setString(1, hop.hub1.city.toLowerCase)
    statement.setString(2, hop.hub1.state.toLowerCase)
    statement.setString(3, hop.hub2.city.toLowerCase)
    statement.setString(4, hop.hub2.state.toLowerCase)
    statement.setString(5, hop.hub2.city.toLowerCase)
    statement.setString(6, hop.hub2.state.toLowerCase)
    statement.setString(7, hop.hub1.city.toLowerCase)
    statement.setString(8, hop.hub1.state.toLowerCase)

    statement.executeUpdate()
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
        if (i + 1 < path.size) {
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


  def loadHubList() = {
    hubs.clear()
    hops.clear()

    val driver = "org.postgresql.Driver"
    val url = "jdbc:postgresql:sim_ups"
    val username = "mcooksey"
    val password = "csa1csa1"

    var connection: Connection = null

    Class.forName(driver)
    connection = DriverManager.getConnection(url, username, password)
    val statement = connection.createStatement()
    val resultSet = statement.executeQuery("Select * from routes")
    while (resultSet.next()) {
      val origin = resultSet.getString("origin")
      val origin_state = resultSet.getString("origin_state")
      val destination = resultSet.getString("destination")
      val destination_state = resultSet.getString("destination_state")
      val hours = resultSet.getInt("hours")
      val minutes = resultSet.getInt("minutes")
      val miles = resultSet.getInt("miles")
      val hub1 = new Hub(origin, origin_state)
      val hub2 = new Hub(destination, destination_state)

      putHop(hub1, hub2, hours, minutes, miles)
    }

    connection.close()
    println("Hubs loaded from database.")
  }

}
