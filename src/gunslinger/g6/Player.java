package gunslinger.g6;

import java.util.*;

// An example player
// Extends gunslinger.sim.Player to start with your player
//
public class Player extends gunslinger.sim.Player
{
	// keep track of rounds
	private int round = 0;

	// Conststency Matrix
	private double[][] consistency;

	// Current Neutral-turned enemies
	ArrayList<Integer> current_alive_turned_enemies = new ArrayList<Integer>();

	// total versions of the same player
	private static int versions = 0;
	// my version no
	private int version = versions++;

	// A simple fixed shoot rate strategy used by the dumb player
	private static double ShootRate = 0.8;

	// name of the team
	//
	public String name()
	{
		return "g6" + (versions > 1 ? " v" + version : "");
	}

	// Initialize the player
	public void init(int nplayers, int[] friends, int enemies[])
	{
		// Note:
		//  Seed your random generator carefully
		//  if you want to repeat the same random number sequence
		//  pick your favourate seed number as the seed
		//  Or you can simply use the clock time as your seed     
		//       
		gen = new Random(System.currentTimeMillis());
		// long seed = 12345;
		// gen = new Random(seed);

		this.nplayers = nplayers;

		for (int i = 0; i != friends.length; i++)
			this.friends.add(friends[i]);

		for (int i = 0; i != enemies.length; i++)
			this.enemies.add(enemies[i]);

		history = new int[1000][nplayers];
		for (int i=0 ; i<nplayers ; i++)
			history[0][i] = -1;

		consistency = new double[nplayers][nplayers];
		for (int i=0 ; i<nplayers ; i++)
		{
			for (int j=0 ; j<nplayers ; j++)
			{
				consistency[i][j] = 0.0;
			}
		}
	}

	// Update consistency matrix after every round
	public void updateConsistency(int[] prevRound, int[][] history, int currentRound)
	{
		for (int i=0 ; i<nplayers ; i++)
		{
			ArrayList<Integer> trackRoundNumber = new ArrayList<Integer>();
			trackRoundNumber.add(0);
			int mostRecentRound = -1;
			for (int k=0 ; k<nplayers ; k++)
			{
				for (int j=1 ; j<=currentRound ; j++)
				{
					if (history[j][i] == k)
					{
						trackRoundNumber.add(j);
						mostRecentRound = j;
					}
				}

				// Average of differences in shooting
				double sum = 0.0;
				for (int j=1 ; j<trackRoundNumber.size() ; j++)
				{
					sum += trackRoundNumber.get(j) - trackRoundNumber.get(j-1);
				}
				sum /= (trackRoundNumber.size() - 1);

				double tempConsistency = ((((double)currentRound - sum)/(double)currentRound) * ((double)mostRecentRound/(double)currentRound));
				consistency[i][k] = tempConsistency;
			}
		}
	}

	public void printConsistency()
	{
		/*System.out.println("---------------- Printing Consistencies ----------------");
		for (int i=0 ; i<nplayers ; i++)
		{
			for (int j=0 ; j<nplayers ; j++)
			{
				System.out.print(consistency[i][j] + " ");
			}
			System.out.println();
		}*/
	}

	// Calculate average consistency 
	public double averageConsistency(int playerID)
	{
		double average = 0.0;
		for (int i=0 ; i<nplayers ; i++)
		{
			average += consistency[playerID][i];
		}
		average /= nplayers;
		return average;
	}

	// Pick a target to shoot
	// Parameters:
	//  prevRound - an array of previous shoots, prevRound[i] is the player that player i shot
	//              -1 if player i did not shoot
	//  alive - an array of player's status, true if the player is still alive in this round
	//  Return:
	//  int - the player id to shoot, return -1 if do not shoot anyone
	//
	public int shoot(int[] prevRound, boolean[] alive)
	{	
		int target;

		round++;	
		//System.out.println("Players: " + nplayers);

		/* Strategy used by the dumb player:
		   Decide whether to shoot or not with a fixed shoot rate
		   If decided to shoot, randomly pick one alive that is not your friend */
		int alive_friends = 0;
		int alive_players = 0;
		int alive_enemies = 0;
		ArrayList<Integer> current_alive_friends = new ArrayList<Integer>();
		ArrayList<Integer> current_alive_enemies = new ArrayList<Integer>();
		HashMap<Integer, Integer> current_alive_enemy_score = new HashMap<Integer, Integer>();

		// Remove DEAD enemies from my "turned" enemy list
		for (int i = 0; i<nplayers; i++)
		{			
			if (!alive[i] && (current_alive_turned_enemies.contains(i)))
			{
				current_alive_turned_enemies.remove(new Integer(i));
			}
		}

		// Shoot or not in this round?
		// Populating current alive players
		// Populating current alive friends
		// Populating current alive enemies
		for (int i=0 ; i<nplayers ; i++)
		{
			if (alive[i])
			{
				alive_players++;
				if (friends.contains(i))
				{
					current_alive_friends.add(i);
					alive_friends++;					
				}

				else if (enemies.contains(i))
				{
					current_alive_enemies.add(i);
					alive_enemies++;
				}
			}
		}

		// Merge the two enemy lists
		current_alive_enemies.addAll(current_alive_turned_enemies);

		/*		if (current_alive_enemies.size() > 0)
				System.out.println("--------------- Alive ENEMIES--------------------");

				for (int i = 0; i < current_alive_enemies.size(); i++)
				{
				System.out.println("Alive enemies " + current_alive_enemies.get(i));
				}
		 */
		//			return -1;
		double initial_prob = 1.0 - ((1.0*alive_friends)/(alive_players - 1));
		//System.out.println("Initial Prob. of g6: " + initial_prob + "\n");

		if (round == 1)
		{
			boolean shoot = true;
			//boolean shoot = gen.nextDouble() < ShootRate;
			if (initial_prob > 0.5)
			{
				shoot = true;
				target = enemies.get(0);
				return target;
			}
			else
			{
				shoot = false;
				return -1;
			}
		}

		else
		{	
			// Add previous round to History
			for (int i = 0; i < nplayers ; i++)
			{
				history[round-1][i] = prevRound[i];
			}

			updateConsistency(prevRound, history, round);
//			printConsistency();

			// I AM SHOT
			// Make my shooter my prime target
			for (int i=0 ; i < nplayers ; i++)
			{
				// If someone shoots me
				if (prevRound[i] == id)
				{
					// If I am in the middle of a directed flow A->"B"->C
					// Trying to be consistent and hoping friends (if any) will help
					if (alive[i] && prevRound[id]!=-1 && alive[prevRound[id]] && prevRound[id]!=i && alive_friends > 1)
					{
//						//System.out.println("--------------HITTING X  - directed flow SHOT Moi-----------------");
						target = prevRound[id];
						return target;
					}
				}
			}

			for (int i=0 ; i < nplayers ; i++)
			{
				// If someone shoots me
				if (prevRound[i] == id)
				{
					// If shooter is an enemy and he is alive
					if (alive[i] && enemies.contains(i))
					{
//						//System.out.println("--------------HITTING enemy  - enemy SHOT Moi-----------------");
						target = i;
						return target;
					}
				}
			}

			for (int i=0 ; i < nplayers ; i++)
			{
				// If someone shoots me
				if (prevRound[i] == id)
				{	
					// If shooter is a neutral person and he is alive
					if (alive[i] && !friends.contains(i) && !enemies.contains(i))
					{
						target = i;
						// If not a turned enemy
						if(!current_alive_turned_enemies.contains(i))
						{
							// Add current hitter to my enemy list
							current_alive_turned_enemies.add(i);
						}
//						System.out.println("--------------HITTING turned  - turned SHOT Moi-----------------");
						return target;
					}
				}
			}

			for (int i=0 ; i < nplayers ; i++)
			{
				// If someone shoots me
				if (prevRound[i] == id)
				{	
					// If shooter is a friend and he is alive
					if (alive[i] && friends.contains(i))
					{
						// Check consistency of the friend hitting me
						// If consistency very high, shoot the friend
						if (consistency[i][id] > FRIEND_HITTING_ME)
						{
//							System.out.println("--------------HITTING friend  - friend SHOT Moi-----------------");
							target = i;
							return target;
						}
					}
				}
			}

			// I WAS NOT SHOT
			// Can decide what to do next
			// If 3 Players or less are left
			if (alive_players <= 3)
			{
				for (int i=0 ; i<nplayers ; i++)
				{
					for (int j=0 ; j<nplayers ; j++)
					{
						if (i!=id && j!=id && alive[i] && alive[j] && prevRound[i] == j && initial_prob < INITIAL_PROB)
						{
							if (!friends.contains(i))
							{
//								System.out.println("--------------3 players - 1-----------------");
								target = i;
								return target;
							}
							else if (!friends.contains(j))
							{
//								System.out.println("--------------3 players  - 2-----------------");
								target = j;
								return target;
							}

						}

						if (i!=id && j!=id && alive[i] && alive[j] && prevRound[j] == i && initial_prob < INITIAL_PROB)
						{
							if (!friends.contains(j))
							{
//								System.out.println("-------------- 3 players - 3-----------------");
								target = j;
								return target;
							}
							else if (!friends.contains(i))
							{
//								System.out.println("-------------- 3 players - 4-----------------");
								target = i;
								return target;
							}	
						}
					}
				}
			}

			// If more than 3 are left
			for (int i=0 ; i < nplayers ; i++)
			{
				for (int p = 0; p < nplayers ; p++)
				{
					// If enemy/neutral shoots a friend
					// HELPING THE FRIEND
					if (alive[i] && alive[p] && !friends.contains(i) && friends.contains(p) && prevRound[i] == p)
					{
//						System.out.println("--------------Helping friend - X SHOT FRiend-----------------");
//						if (averageConsistency(p) > FRIEND_HITTING_NONFRIEND && initial_prob < INITIAL_PROB)
//						{
							target = i;
							return target;
//						}
					}
				}
			}

			for (int i=0 ; i < nplayers ; i++)
			{
				for (int p = 0; p < nplayers ; p++)
				{
					// If someone shoots enemy
					// HELPING SOMEONE ELIMINATE OUR ENEMY
					if (alive[i] && alive[p] && enemies.contains(p) && prevRound[i] == p && initial_prob < INITIAL_PROB)
					{
						// Friend shoots enemy
						if (friends.contains(i))
						{
							for (int e = 0 ; e < nplayers ; e++)
							{
								if (prevRound[e] != i )//&& averageConsistency(i) > FRIEND_HITTING_ENEMY)
								{
//									System.out.println("--------------HELPING FRIEND - FRIEND SHOT ENEMY-----------------");
									target = p;
									return target;
								}
							}
						}
					}
				}
			}

			for (int i=0 ; i < nplayers ; i++)
			{
				for (int p = 0; p < nplayers ; p++)
				{
					// Neutral shoots enemy
					if (alive[i] && !friends.contains(i) && !enemies.contains(i) && enemies.contains(p))
					{
						for (int e = 0 ; e < nplayers ; e++)
						{
							// Help the neutral in case the neural has not been shot
							if (alive[p] && prevRound[e] != i && averageConsistency(i) > NEUTRAL_HITTING_ENEMY && initial_prob < INITIAL_PROB)
							{
//								System.out.println("--------------HELPING N - N SHOT ENEMY-----------------");
								target = p;
								return target;
							}
						}
					}
				}
			}

			// If friend shoots a friend
			/*if (friends.contains(i) && friends.contains(p) && prevRound[i] == p && alive[i] == true)
			  {
			  double consistency_shooter = 0.0;
			  double consistency_victim = 0.0;
			// Check for consistency of friend hitting another friend
			for (int f = 0; f < alive.length; f++){
			if (friends.contains(f) && consistency[i][f] > 0)
			{
			consistency_shooter += consistency[i][f];
			}
			if (friends.contains(f) && consistency[p][f] > 0)
			{
			consistency_victim += consistency[p][f];
			}
			}

			if (consistency_victim > consistency_shooter)
			{
			target = p;
			System.out.println(i + " Friend shot my friend " + p + " Support victim ");
			return target;
			}
			else if (consistency_victim < consistency_shooter)
			{
			target = i;
			System.out.println(i + " Friend shot my friend " + p + " Support shooter");
			return target;
			}
			System.out.println(i + " Friend didn't shoot my friend " + p);
			}*/
			for (int i=0 ; i < nplayers ; i++)
			{
				for (int p = 0; p < nplayers ; p++)
				{	
					// If enemy shoots an enemy
					if (alive[i] && alive[p] && enemies.contains(i) && enemies.contains(p) && prevRound[i] == p && initial_prob < INITIAL_PROB)
					{
//						System.out.println("--------------HITTING ENEMY - ENEMY SHOT ENEMY-----------------");
						// TO DO: Use consistency in later versions
						target = i;
						return target;
					}
				}	
			}

			// Pick first target and shoot
			//int target = current_alive_enemies.get(gen.nextInt(current_alive_enemies.size()));
			if (current_alive_enemies.size()>0 && alive_friends>0)
			{
//				System.out.println("--------------HITTING ENEMY FIRST IN LIST - LAST SCENARIO-----------------");
				target = current_alive_enemies.get(0);
				return target;
			}
		}

		return -1;
	}

	private Random gen;
	private int nplayers;
	private ArrayList<Integer> friends = new ArrayList<Integer>();
	private ArrayList<Integer> enemies = new ArrayList<Integer>();

	// track the history of all the rounds
	private int[][] history;

	private double INITIAL_PROB = 0.5;
	private double FRIEND_HITTING_ME = 0.6;
	private double FRIEND_HITTING_ENEMY = 0.1;
	private double NEUTRAL_HITTING_ENEMY = 0.3;
	private double FRIEND_HITTING_NONFRIEND = 0.1;
	private double ENEMY_HITTING_ME = 0.1;
}
