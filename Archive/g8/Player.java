package gunslinger.g8;

import java.util.*;

// Extends gunslinger.sim.Player to start with your player
public class Player extends gunslinger.sim.Player
{
    // total versions of the same player
    private static int versions = 0;
    // my version no
    private int version = versions++;
    
    // name of the team
    //
    public String name()
    {
        return "g8";
    }
 
    // Initialize the player
    //
    public void init(int nplayers, int[] friends, int enemies[])
    {
        this.nplayers = nplayers;
        this.friends = new Integer[friends.length];
        for (int i = 0; i < friends.length; i++) {
            this.friends[i] = friends[i];
        }
        this.enemies = new Integer[enemies.length];
        for (int i = 0; i < enemies.length; i++) {
            this.enemies[i] = enemies[i];
        }
    	this.history = new List[nplayers];
        this.timesTargeted = new Integer[nplayers];
    	for (int i = 0; i < nplayers; i++) {
    	    history[i] = new ArrayList<Integer>();
            timesTargeted[i] = 0;
    	}
	previousTarget = -1;
    }

    // Pick a target to shoot
    // Parameters:
    //  prevRound - an array of previous shoots, prevRound[i] is the player that player i shot
    //              -1 if player i did not shoot
    //  alive - an array of player's status, true if the player is still alive in this round
    // Return:
    //  int - the player id to shoot, return -1 if do not shoot anyone
    //
    public int shoot(int[] prevRound, boolean[] alive)
    {
    	if (prevRound != null) {
      	    for (int i = 0; i < nplayers; i++) {    		
        	history[i].add(0, prevRound[i]);
                if (prevRound[i] != -1) {
                    timesTargeted[prevRound[i]]++;
                }
    	    }
    	} else if (enemies.length * 5 < nplayers || friends.length * 2 > nplayers) {
	    return -1;
	}
	double maxRank = 0;
	int target = -1;
        for (int i = 0; i != nplayers; ++i) {
            if (i != id && alive[i] && !isFriend(i)) {
		double tempRank = rank(alive, prevRound, i);
                if (maxRank < tempRank) {
		    maxRank = tempRank;
		    target = i;
		}
	    }
	    /*
	    if (Thread.currentThread().isInterrupted()) {
		previousTarget = target;
		return target;
		} */
	}    	
	previousTarget = target;
        return target;
    }
    
    public List<Integer> shotAtLastRound(int[] prevRound, boolean[] alive) {
	List<Integer> shotAtLastRound = new ArrayList<Integer>();
	if (prevRound == null) {
	    return shotAtLastRound;
	}
	for (int i = 0; i < nplayers; i++) {
	    if (prevRound[i] != -1 && alive[i] && alive[prevRound[i]] && isFriend(i)
		      && !shotAtLastRound.contains(prevRound[i])) {
		    shotAtLastRound.add(prevRound[i]);
	    }
	}
	return shotAtLastRound;
    }

    // Assigns a ranking to a player. Higher ranking means a higher priority to shoot
    // Parameters:
    //  alive - an array of player's status, true if the player is still alive in this round
    //  target - the id of the person we are trying to assign a ranking to
    // Rerturn:
    // int - the ranking for the target
    public double rank(boolean[] alive, int[] prevRound, int target) {
    	double rank = isEnemy(target) ? 1 : 0;        
    	List<Integer> playerHistory = history[target];
	List<Integer> shotAtLastRound = shotAtLastRound(prevRound, alive);
	double roundWeight = 1;
    	for (int otherTarget : playerHistory) {
    	    if (id == otherTarget) {
    		  rank += 2 * roundWeight;
    	    } else if (isFriend(otherTarget) && alive[otherTarget]) {
		rank += roundWeight;
    	    } else if (isEnemy(otherTarget) && alive[otherTarget]) {
		rank -= roundWeight;
    	    }
	    roundWeight *= 0.9;
    	}
	if (shotAtLastRound.contains(target) &&
	    ((isNeutral(target) && enemies.length < friends.length + 1) || isEnemy(target))) {
	    rank += 1.5;
	}
	if (target == previousTarget && alive[previousTarget]) {
	    rank++;
	}
    	return rank;
    }

    public boolean isEnemy(int id) {
	return Arrays.asList(enemies).contains(id);
    }

    public boolean isFriend(int id) {
	return Arrays.asList(friends).contains(id);
    }

    public boolean isNeutral(int id) {
	return !isEnemy(id) && !isFriend(id);
    }

    // player we last shot at
    private int previousTarget;
    // number of players in the game
    private int nplayers;
    // list of ids that correspond to friends
    private Integer[] friends;
    // list of ids that correspond to enemies
    private Integer[] enemies;
    // array of Lists that represent the previous moves of the other players
    private List<Integer>[] history;
    // list that represents the number of times each player has been targeted
    private Integer[] timesTargeted;
}
