package gunslinger.sim;

// The base class of a player
// Extends the base class to start your player
// See dumb/Player.java for an example
//
public abstract class Player
{
    // player's index in the playerlist file
    static int globalIndex = 0;    
    final int index;

    // player's id in each game
    // the id is assigned randomly when each game starts
    protected int id;

    // constructor for base class
    public Player()
    {
        index = globalIndex++;
    }

    // name of group
    //
    public abstract String name();
    
    // Initialize the player
    //
    public abstract void init(int nplayers, int[] friends, int enemies[]);

    // Pick a target to shoot
    // Parameters:
    //  prevRound - an array of previous shoots, prevRound[i] is the player that player i shot
    //              -1 if player i did not shoot
    //  alive - an array of player's status, true if the player is still alive in this round
    // Return:
    //  int - the player id to shoot, return -1 if do not shoot anyone
    //
    public abstract int shoot(int[] prevRound, boolean[] alive);
}
