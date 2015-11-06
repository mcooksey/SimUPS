# SimUPS

The assignment is to create a simulated package delivery system (aka “SimUPS”) in order to gain experience on routing data between points, which is exactly what we will be doing with Scala/Akka in Blade Runner and beyond… In this simulation, your company is NOT concerned with speed or priority, just “guaranteed” delivery…

At the very least, you should create Hub objects that will route Package objects among the hubs based on their origin and destination, with the following requirements:

1. Find the shortest (i.e., cheapest for your delivery company) route between hubs
2. Be able to recover (i.e., re-route) if any portion of a route is blocked or otherwise unavailable
3. The entire route (i.e., all hops) a Package takes from its origin to its destination should be displayed on the screen

Sample data is included in the hubs.csv file.
