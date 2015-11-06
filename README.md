# SimUPS
Training Thursday Exercise

The assignment is to create a simulated package delivery system (aka “SimUPS”) in order to gain experience on routing data between points, which is exactly what we will be doing with Scala/Akka in Blade Runner and beyond… In this simulation, your company is NOT concerned with speed or priority, just “guaranteed” delivery…

At the very least, you should create Hub objects that will route Package objects among the hubs based on their origin and destination, with the following requirements:
1.	Find the shortest (i.e., cheapest for your delivery company) route between hubs
2.	Be able to recover (i.e., re-route) if any portion of a route is blocked or otherwise unavailable
3.	The entire route (i.e., all hops) a Package takes from its origin to its destination should be displayed on the screen

Sample Hubs:

Origin City	Origin State	Destination	Destination State	Hours	Minutes	Miles	Ground (0) or Air (1)
Tupelo	MS	Memphis	TN	1	41	109	0
Tupelo	MS	Jackson	MS	3	4	195	0
Tupelo	MS	Birmingham	AL	2	1	134	0
Tupelo	MS	Meridian	MS	2	16	144	0
Hattiesburg	MS	Jackson	MS	1	37	90	0
Hattiesburg	MS	Meridian	MS	1	23	87	0
Hattiesburg	MS	Biloxi	MS	1	17	74	0
Hattiesburg	MS	New Orleans	LA	1	47	112	0
Jackson	MS	Vicksburg	MS	0	47	43	0
Meridian	MS	Biloxi	MS	2	32	165	0
Meridian	MS	Jackson	MS	1	24	92	0
Meridian	MS	Birmingham	AL	2	13	147	0
Memphis	TN	Nashville	TN	4	6	252	0
Memphis	TN	Little Rock	AR	2	1	137	0
Memphis	TN	Jackson	MS	2	57	209	0
Nashville	TN	Birmingham	AL	2	46	192	0
Nashville	TN	Chattanooga	TN	2	3	132	0
Nashville	TN	Knoxville	TN	2	44	179	0
Chattanooga	TN	Knoxville	TN	1	40	112	0
Chattanooga	TN	Atlanta	GA	1	44	118	0
Birmingham	AL	Atlanta	GA	2	9	147	0
Birmingham	AL	Montgomery	AL	1	21	92	0
Montgomery	AL	Atlanta	GA	2	17	160	0
Montgomery	AL	Mobile	AL	2	19	168	0
Mobile	AL	Biloxi	MS	0	57	62	0
New Orleans	LA	Biloxi	MS	1	27	90	0
New Orleans	LA	Baton Rouge	LA	1	19	80	0
Monroe	LA	Vicksburg	MS	1	12	77	0
Monroe	LA	Shreveport	LA	1	31	100	0
Shreveport	LA	Texarkana	AR	1	10	72	0
Shreveport	LA	Lafayette	LA	3	4	213	0
Lafayette	LA	Baton Rouge	LA	1	4	59	0
Texarkana	AR	Little Rock	AR	2	6	143	0
Jackson	MS	Nashville	TN	0	0	0	1
Jackson	MS	Atlanta	GA	0	0	0	1
Little Rock	AR	Atlanta	GA	0	0	0	1
New Orleans	LA	Nashville	TN	0	0	0	1
New Orleans	LA	Atlanta	GA	0	0	0	1
Nashville	TN	Little Rock	AR	0	0	0	1
