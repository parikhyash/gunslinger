package gunslinger.g3;
import java.util.*;

public class Player extends gunslinger.sim.Player
{
    private int nplayers;
    private int[] friends;
    private int[] enemies;

    int[] expected_shots;

    //for history storing
    private int[][] history;
    private int roundNum = 0;

    private static int versions = 0;
    private int version = versions++;
    public String name() { return "g3" + (versions > 1 ? " v" + version : ""); }

    public void init(int nplayers, int[] friends, int enemies[])
    {
        this.nplayers = nplayers;
        this.friends = friends.clone();
        this.enemies = enemies.clone();
		history = new int[1000][nplayers]; 	//todo: add support for > 1000 rounds?

		for (int i = 0; i < history.length; i++)
		{ for (int j = 0; j < history[0].length; j++) { history[i][j] = -2; } }
    }

	//Returns true if the game is in equilibrium (same shots by each player in the past three rounds) and false otherwise.
	public boolean equilibrium(int[] prevRound, boolean[] alive)
	{
		if(roundNum < 3) return false; //Don't consider equilibrium conditions unless at least three rounds have passed
		for(int j= 0; j<alive.length; j++)
		{
			int enemyTarget = history[roundNum-1][j]; //Player j's target in the most recent round.
			for(int i= 1;i<3;i++)
			{
				//If a player shot differently in the past three rounds, the game is not in equilibrium so return false.
				if(history[roundNum-1-i][j]!=enemyTarget)
					return false;
			}
		}
		return true; //If all players have shot similarly in the past three rounds, return true.
	}

	public int endGame(int[] prevRound, boolean[] alive) //Strategy when three players are left in equilibrium
	{
		for(int i = 0; i < prevRound.length; i++)
		{
			for(int j = 0; j < enemies.length; j++)
			{
				if(prevRound[i] == enemies[j]) //If one of the remaining players shot an enemy in equilibrium
				{
					System.out.println("[PLAYER3]: Shooting player " + enemies[j] + " who was shot by player " + i + ".");
					return enemies[j]; //Shoot the enemy being shot at in equilibrium
				}
			}
		}
		return -1;
	}

	public double[] expected_shots(boolean[] alive) //Computes the expected number of shots each player will receive based on the history of shots
	{
		double[] expected_shots= new double[alive.length];
		for(int i = 0;i<alive.length;i++)
		{
			if(alive[i] == true) //If player i is alive
			{
				//Use a weighted moving average of shots over the past rounds to calculate the expected
				//number of shots for each player
				expected_shots[i]= .5 * history[roundNum-1][i] + .3 * history[roundNum-2][i] + .2 * history[roundNum-3][i];
			}
			else
			{
				expected_shots[i]= 0; //If a player is dead, he cannot be shot

			}
		}
		return expected_shots;
	}

    // Parameters:
    //  prevRound - an array of previous shoots, prevRound[i] is the player that player i shot, -1 if player i did not shoot
    //  alive - an array of player's status, true if the player is still alive in this round
    // Return: int - the player id to shoot, return -1 if do not shoot anyone

    public int shoot(int[] prevRound, boolean[] alive)
    {

    	if (prevRound == null) //First Round Strategy -> wait do nothing
    	{
    		System.err.println("[PLAYER3] First Round, I am id: " + id + " waiting...");
    	}
    	else
    	{
			roundNum++;
			int prevRoundNum = roundNum-1;
			//update history
			for (int i = 0; i < prevRound.length; i++)
			{
				history[prevRoundNum][i] = prevRound[i];
			}

			if(equilibrium(prevRound, alive)) //If the game is in equilibrium, implement the end game strategy
			{
				System.err.println("[PLAYER3] The game is in equilibrium. Implementing end game strategy.");
				return endGame(prevRound, alive);
			}
			//Priority 1: Shoot person you shot at before if they are not dead
			int lastPersonShotAt = prevRound[id];

			if( lastPersonShotAt != -1 && alive[lastPersonShotAt] )
			{
			printHistory();
				return lastPersonShotAt;
			}

			//Priority 2: Shoot the person who shot you last round
			for(int i = 0;i < prevRound.length; i++)
			{
				if( (prevRound[i] == id) && alive[i] )
				{
				printHistory();
					return i;
				}
			}
			//Priority 3: Shoot at enemies that shot at friends
			for(int i = 0;i < prevRound.length; i++)
			{
				for(int j = 0;j < friends.length; j++)
				{
					// Did the player shoot a friend?
					if ( (friends[j] == prevRound[i]) && alive[i])
					{
						// Is the player an enemy
						for(int k = 0;k < enemies.length; k++)
						{
							if (enemies[k] == i)
							{
								printHistory();
								return i;
							}
							//else keep a low profile by not killing neutral players
						}
					}
				}
			}
    	}
		printHistory();
    	return -1;
    }

	public void printHistory() //For testing purposes only. print history every time we return a shot
	{
		System.out.println("[PLAYER3] Printing history:");
		loop:
		for (int i = 0; i < history.length; i++)
		{
			for (int j = 0; j < history[0].length; j++)
			{
				if (history[i][j] == -2)
				{
					break loop;
				}
				System.out.print(history[i][j] + "\t");
			}
			System.out.print("\n");
		}
		System.out.println("[PLAYER3]Done printing history");
	}
}