package gunslinger.g5;

import java.util.*;


/**
 * Group 5's proprietary Player implementation.
 * @author Andrew Goldin
 * @author Priyanka Singh
 * @author Neha Aggarwal
 * 
 * TODO: Use rudimentary prediction to break ties. Consider who may help the target retaliate.
 * TODO: Decide whether or not to be even MORE of a pacifist.
 * TODO: Do we actually never want to shoot round 1?
 * TODO: Don't shoot until ourselves/a friend is shot?
 * TODO: Compartmentalizaion. Weight manager class and static constants for weight values.
 */
public class Player extends gunslinger.sim.Player {

	// total versions of the same player
	private static int versions = 0;
	// my version no
	private int version = versions++;

	// A simple fixed shoot rate strategy used by the dumb player
	private static double ShootRate = 0.8;

	private Random gen;
	private int nplayers;
	private ArrayList<Integer> friends;
	private ArrayList<Integer> enemies;
	private int[] weights;
	private int numNeutrals;


	private int[] prevRound;
	private boolean[] alive;
	private GameHistory history;


	/**
	 * Returns the player/team name.
	 */
	public String name() {
		return "g5" + (versions > 1 ? " v" + version : "");
	}


	/**
	 * Initializes the player.
	 * @param nplayers the number of players in the game
	 * @param friends a list of the player's friends
	 * @param enemies a list of the player's enemies
	 */
	public void init(int nplayers, int[] friends, int[] enemies) {

		// seed the generator
		gen = new Random(System.currentTimeMillis());
		// long seed = 12345;
		// gen = new Random(seed);

		this.nplayers = nplayers;

		// creating the weight array
		this.weights = new int[nplayers];

		// number of neutral players
		numNeutrals = nplayers - friends.length - enemies.length - 1;

		// initialize friends/enemies instance variables
		this.friends = new ArrayList<Integer>();
		this.enemies = new ArrayList<Integer>();

		// populate friend and enemy lists
		for (int i = 0; i != friends.length; i++) {
			this.friends.add(friends[i]);
		}
		for (int i = 0; i != enemies.length; i++) {
			this.enemies.add(enemies[i]);
		}

		//System.out.println("g5 FRIENDS: " + this.friends);
		//System.out.println("g5 ENEMIES: " + this.enemies);

		// initialize the game history
		history = new GameHistory();

	}


	/**
	 * The meat of the Player class, decides whether or not to shoot another player, via a
	 * priority targeting system.
	 * @param prevRound the previous round information
	 * @param alive the players who are still alive
	 * @return the ID of a player who has been targeted, -1 if player decides not to shoot
	 */
	public int shoot(int[] prevRound, boolean[] alive) {

		this.prevRound = prevRound;
		this.alive = alive;

		// Don't shoot first round
		// perhaps a smarter choice can be made based on number of friends/enemies as to who our friends might target
		if (prevRound == null) {
			return -1;
		}




		// Add the previous round information to the game history
		history.add(prevRound);

		// If there are only friends remaining, do not shoot
		//System.out.println("g5 ONLY FRIENDS LEFT: " + onlyFriendsRemaining());
		if (onlyFriendsRemaining()) {
			return -1;
		}

		// begin computing weights
		Arrays.fill(weights, 0);
		weightNonFriends();
		weightShotMe();
		weightShotByOthers();
		weightShotFriends();

		// select a random target from those with highest weights
		int[] targets = maxIndex(weights);
		
		// this constraint works well when there is only one enemy
		if (weights[targets[0]] == 5) {
			return -1;
		}
		
		int target = targets[gen.nextInt(targets.length)];

		// print all relevant round info
		//System.out.println("g5 ID: " + id);
		//System.out.println(history);
		//System.out.println("g5 ENEMIES ALIVE: " + numEnemiesAlive());
		//System.out.println("g5 FRIENDS ALIVE: " + numFriendsAlive());
		//System.out.println("g5 WEIGHTS: " + Arrays.toString(weights));
		//System.out.println("g5 TARGET: " + target);

		// if weights are all zero, don't shoot
		if (weights[target] == 0) {
			return -1;
		}
		return target;

	}


	/**
	 * Determines if the only players left alive are our friends.
	 * @return true if only friends remain, false otherwise
	 */
	private boolean onlyFriendsRemaining() {
		for (int i = 0; i < nplayers; i++) {
			if (alive[i] && !friends.contains(i) && i != id) {
				return false;
			}
		}
		return true;
	}


	/**
	 * Computes the number of enemies still alive.
	 * @return the number of enemies alive
	 */
	private int numEnemiesAlive() {
		int amount = 0;
		for (int i = 0; i < enemies.size(); i++) {
			if (alive[enemies.get(i)]) {
				amount++;
			}
		}
		return amount;
	}


	/**
	 * Computes the number of friends still alive.
	 * @return the number of friends alive
	 */
	private int numFriendsAlive() {
		int amount = 0;
		for (int i = 0; i < friends.size(); i++) {
			if (alive[friends.get(i)]) {
				amount++;
			}
		}
		return amount;
	}


	/**
	 * Adds weight to players who are not friends, with additional weight to enemies.
	 */
	private void weightNonFriends() {
		// weight for non-friends
		for (int i = 0; i < nplayers; i++) {
			if (alive[i]) {
				if (!friends.contains(i) && i != id) {
					//weights[i] += 5; // weight for just being a neutral
					// extra weight for enemies
					if (enemies.contains(i)) {
						weights[i] += 5;
					}
				}
			}
		}
	}


	/**
	 * Adds weight to those who have shot our player, with additional weight to enemies.
	 */
	private void weightShotMe() {
		// give weight to any enemies who ever shot us
		// give weight to neutrals who have shot us more than once in a row
		int[] shooters = history.everShot(id);
		if (shooters != null) {
			for (int i = 0; i < shooters.length; i++) {
				if (alive[shooters[i]]) {
					if (enemies.contains(shooters[i])) {
						weights[shooters[i]] += 5;
					}
					int numConsShots = history.consecutiveShots(shooters[i], id);
					if (numConsShots > 0) {
						if (enemies.contains(shooters[i])) {
							weights[shooters[i]] += 5 * numConsShots;
						}
						else if (!friends.contains(shooters[i])) {
							if (numConsShots > 1) {
								weights[shooters[i]] += 5 * numConsShots;
							}
							else {
								weights[shooters[i]] += 2 * numConsShots;
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Adds weight to those who have been shot by other players, with additional weight to enemies.
	 */
	private void weightShotByOthers() {
		for (int victim = 0; victim < nplayers; victim++) {
			if (alive[victim]) {
				int[] shooters = history.everShot(victim);
				if (shooters != null) {
					for (int shooter = 0; shooter < shooters.length; shooter++) {
						if (alive[shooters[shooter]]) {
							// enemy shooting enemy or neutral; target shooter
							if (enemies.contains(shooters[shooter]) && !friends.contains(victim)) {
								weights[shooters[shooter]] += 5;
							}
							// friend or neutral shooting enemy; target victim
							else if (!enemies.contains(shooter) && enemies.contains(victim)) {
								weights[victim] += 3;
							}
							
						}
					}
				}
			}
		}
	}


	/**
	 * Adds weight to players who have attacked our friends, with additional weight to enemies.
	 */
	private void weightShotFriends() {
		for (int i = 0; i < friends.size(); i++) {
			if (alive[friends.get(i)]) {
				int[] shooters = history.everShot(friends.get(i));
				if (shooters != null) {
					for (int shooter = 0; shooter < shooters.length; shooter++) {
						if (alive[shooters[shooter]]) {
							if (enemies.contains(shooters[shooter])) {
								weights[shooters[shooter]] += 3;
							}
							int numConsShots = history.consecutiveShots(shooters[shooter], friends.get(i));
							if (numConsShots > 0) {
								if (enemies.contains(shooters[shooter])) {
									weights[shooters[shooter]] += 3 * numConsShots;
								}
								else if (!friends.contains(shooter)) {
									if (numConsShots > 1) {
										weights[shooters[shooter]] += 3 * numConsShots;
									}
									else {
										weights[shooters[shooter]] += 1 * numConsShots;
									}
								}
							}
						}
					}
				}
			}
		}
	}


	/**
	 * Given an int array, returns an array of all indices which contain the max value.
	 * Used in the game for determining which players have the highest weights after evaluation.
	 * @param arr the array to be searched
	 * @return int array containing indices from arr which contain arr's max value
	 */
	private int[] maxIndex(int[] arr) {
		int max = arr[0];
		for (int i = 1; i < arr.length; i++) {
			if (arr[i] > max) {
				max = arr[i];
			}
		}
		ArrayList<Integer> temp = new ArrayList<Integer>();
		for (int i = 0; i < arr.length; i++) {
			if (arr[i] == max) {
				temp.add(i);
			}
		}
		int[] top = new int[temp.size()];
		for (int i = 0; i < top.length; i++) {
			top[i] = temp.get(i).intValue();
		}
		return top;
	}

}