package gunslinger.g1;
import java.util.*;

public class Player extends gunslinger.sim.Player
{
    // total versions of the same player
    private static int versions = 0;
    private int playerNumber = versions++;

    public static final String VERSION = "0.1.2";

    // Attributes to use in the feature vector
    private final int NUM_FEATURES = 5;

    public int FRIEND       = 0;
    public int SHOT         = 1;
    public int FOE          = 2;
    public int FRIENDS_FOE  = 3;
    public int ENEMY        = 4;
    public int NONE         = 5;

    private Random gen;

    private int nplayers;
    private int[] friends;
    private int[] enemies;

    private boolean provoked;

    private AiPlayer[] players;
    
    private Relationship relationship;

    public String name()
    {
        return "g1(" + VERSION + ")" + (versions > 1 ? " v" + playerNumber : "");
    }

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
        this.enemies = enemies.clone();
        this.friends = friends.clone();

        initFeatureVectors(nplayers, enemies, friends);
        relationship = new Relationship(nplayers,friends,enemies, id);
    }

    private void initFeatureVectors(int n, int[] e, int[] f)
    {
        // Initialize all the players based on specific game values
        this.players = new AiPlayer[n];
        for(int i = 0; i < n; i++) {
            this.players[i] = new AiPlayer(n, e.length, f.length);
        }
        // Initialize player attributes based on initial friend and foe lists
        for(int i = 0; i < n; i++)
        {
            if(this.id == i)
            {
                this.players[i].me = true;
            }
            else if (contains(i, e))
            {
                this.players[i].attrs[ENEMY] = 1;
                this.players[this.id].enemies[i] = true;
            }
            else if (contains(i,f))
            {
                this.players[i].attrs[FRIEND] = 1;
                this.players[this.id].friends[i] = true;
                this.players[i].friends[this.id] = true;
            }
        }
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
        updateLists(alive);
        updateFeatureVectors(prevRound, alive);
        //relationship.update(prevRound,alive);

        if (!provoked){
            return -1;
        }

        int[] playersScores = new int[nplayers];
        for (int i = 0; i < nplayers; i++){
            playersScores[i] += players[i].badness_level();
        }

        // find biggest score, -1 if score is negative
        int playerToShoot = -1;
        float biggestScore = 0.0f;
        for (int i = 0; i < nplayers; i++) {
          if (playersScores[i] > biggestScore && validTarget(i)) {
              biggestScore = playersScores[i];
              playerToShoot = i;
          }
        }

        return playerToShoot;
    }

    private void updateFeatureVectors(int[] prevRound, boolean[] alive)
    {
        if (prevRound == null) return;

        for (int i = 0; i < friends.length; i++){
            if (contains(friends[i], prevRound)){
                provoked = true;
            }
        }
        if (contains(id, prevRound)) provoked = true;

        for (int i = 0; i < nplayers; i++){
            players[i].attrs[SHOT] = 0;
        }
        for (int i = 0; i < nplayers; i++){
            // player i last shot lastShot
            int lastShot = prevRound[i];
            if (lastShot >= 0) {
                // If someone shot our friend
                if (players[id].friends[lastShot])
                    // If we done currently identify him as being our friends enemy
                    if (!players[i].enemies[lastShot])
                        players[i].attrs[FRIENDS_FOE]++;
                        players[i].enemies[lastShot] = true;
                if (lastShot == id)
                    players[i].attrs[FOE]++;
                if(i != id)
                    players[i].enemies[lastShot] = true;
                    players[lastShot].attrs[SHOT]++;
            }
        }
    }

    // TODO: combine with updateFeatureVectors
    private void updateLists(boolean[] alive)
    {
        for (int i = 0; i < nplayers; i++){
            if (!alive[i]) {
                // if he just died this turn
                if (!this.players[i].dead) {
                    this.players[i].dead = true;
                    // we check to see who their enemies where and update accordingly
                    for (int j = 0; j < nplayers; j++){
                        // we check to see if they where a foe of our friends
                        for (int k = 0; k < friends.length; k++) {
                            if (this.players[j].enemies[friends[k]]) {
                                this.players[j].attrs[FRIENDS_FOE]--;
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean contains(int player, int[] lst)
    {
         for (int i : lst)
            if (i == player) return true;
        return false;
    }

    private boolean validTarget(int player)
    {
        return player != id && !this.players[player].dead;
    }

}
