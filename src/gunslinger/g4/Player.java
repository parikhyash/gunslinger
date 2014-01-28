package gunslinger.g4;

import java.util.*;

// An example player
public class Player extends gunslinger.sim.Player
{
	// total versions of the same player
	private static int versions = 0;
	// my version no
	private int version = versions++;

	// A simple fixed shoot rate strategy used by the dumb player
	private static double ShootRate = 0.8;

	private GameHistory mHistory;
	private EventManager mManager;
	private LateGameManager mLateGame;

	// name of the team
	//
	public String name()
	{
		return "g4" + (versions > 1 ? " v" + version : "");
	}

	// Initialize the player
	//
	public void init(int nplayers, int[] friends, int enemies[])
	{
		mHistory = new GameHistory(id, nplayers, friends, enemies);
		mManager = new EventManager();
		mHistory.addRoundListener(mManager);
        mLateGame = new LateGameManager(mHistory);
		mHistory.addRoundListener(mLateGame);
        gen = new Random(System.currentTimeMillis());

	}

	// Pick a target to shoot
	// Parameters:
	//  prevRound - an array of previous shoots, prevRound[i] is the player that player i shot
	//              -1 if player i did not shoot
	//              In the first round, prevRound = null
	//  alive - an array of player's status, true if the player is still alive in this round
	// Return:
	//  int - the player id to shoot, return -1 if do not shoot anyone
	//
	public int shoot(int[] prevRound, boolean[] alive)
	{
		mHistory.addRound(prevRound, alive);
        if (mLateGame.isLateGame())
            return mLateGame.shoot();
		int target = mManager.getBestShot();
		return target;
	}


	private Random gen;
	private int nplayers;
	private ArrayList<Integer> friends = new ArrayList<Integer>();
	private ArrayList<Integer> enemies = new ArrayList<Integer>();
}
