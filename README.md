Code structure:

|-- yourprojectdir
|   |-- gunslinger
|   |   |-- sim
|   |   |-- dumb
|   |   |-- g1
|   |   |-- g2
|   |   |-- ...


1. Compile the simulator
In your project directory, run

javac gunslinger/sim/Gunslinger.java

2. Run the simulator
In your project directory, run

java gunslinger.sim.Gunslinger <playerlist> <num of enermies> <num of friends> <gui> <recompile> <verbose> <trace> <games>
java gunslinger.sim.Gunslinger players.list 2 2 true true true true 1

Arguments:
- playerlist: list of players
- num of enermies:
- num of friends: 
- gui: enable graphical interface
- recompile: recompile the players. (turn it on if you have made changes to your player)
- verbose: enable more printing information
- trace: enable step-by-step trace
- games: how many games to run in a tournament

You can also change the default parameters in Gunslinger.java.


3. Write your own player
1) Create a new directory g[1-10] under gunslinger, e.g. g1, g2, g3,..
2) Create a new file Player.java under g1, which implements a player class that extends the base class gunslinger.sim.Player. Copy dumb/Player.java to your directory and make changes to it is probably a good starting point
3) If you want to create more auxilary classes, go ahead and create those .java file under your directory. The simulator will compile and load these auxilary class automatically


4. Zip and upload your player
Create a zip file g[1-10].zip that includes the g[1-10] directory.
Upload the zip file into the according directory.

5. Report bugs of the simulator
You are welcome to report any bug in the simulator to me: Jiacheng Yang, jiachengy@cs.columbia.edu. I will try to fix it as soon as possible.
