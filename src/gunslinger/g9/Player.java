package gunslinger.g9;

import java.lang.*;
import java.util.*;
import java.util.Vector;

// An example player
// Extends gunslinger.sim.Player to start with your player
//
public class Player extends gunslinger.sim.Player
{
    // total versions of the same player
    private static int versions = 0;
    // my version no
    private int version = versions++;

    public String name()
    {
        return "g9" + (versions > 1 ? " v" + version : "");
    }

    // Initialize the player
    //
    public void init(int nplayers, int[] friends, int enemies[])
    {
        gen = new Random(System.currentTimeMillis());
        // long seed = 12345;
        // gen = new Random(seed);

        this.roundNumber = 0;
        this.nplayers = nplayers;
        this.friends = friends.clone();
        this.enemies = enemies.clone();

        priority = new double[nplayers];
        roundHistory = new int[100][nplayers];
        shootRecord = new int[nplayers][nplayers];
        constShoot = new boolean[nplayers][nplayers];
        retaliate = new boolean[nplayers];
        provoked = false;
        num_alive_players = nplayers;
        num_alive_enemies = enemies.length;
        num_alive_friends = friends.length;
        num_alive_neutrals = nplayers - enemies.length - friends.length;
    }

    boolean is_friend(int p) {
        for (int i=0; i<friends.length; i++) {
            if (friends[i] == p) {
                return true;
            }
        }
        return false;
    }

    boolean is_enemy(int p) {
        for (int i=0; i<enemies.length; i++) {
            if (enemies[i] == p) {
                return true;
            }
        }
        return false;
    }

    boolean is_neutral(int i) {
        return (!is_friend(i)) && (!is_enemy(i));
    }

    // update shootRecord, constShoot, 
    void update_stats(int[] prevRound, boolean[] alive) {

        roundHistory[roundNumber] = prevRound;

        // update constShoot and shootRecord matric
        if (roundNumber > 1) {
            for (int i=0; i < nplayers; i++) {
                for (int j=0; j < nplayers; j++) {
                    if (prevRound[i] == j && roundHistory[roundNumber-1][i] == j) {
                        constShoot[i][j] = true;
                    } else {
                        constShoot[i][j] = false;
                    }
                }
                if (prevRound[i] != -1)
                    shootRecord[i][prevRound[i]] ++;
            }
        }

        //provoked
        for (int i=0; i<nplayers; i++) {
            if (prevRound[i] == id && alive[i]) {
                provoked = true;
            }
        }

        //retaliated
        if (roundNumber > 2) {
            for (int i=0; i<nplayers; i++) {
                int shooter = i;
                int victim = prevRound[i];
                if (victim != -1 && roundHistory[roundNumber-1][victim] == shooter) 
                    retaliate[shooter] = true;
            }
        }

        //update configuration

        /*
        for (int i=0; i<friends; i++) {
            if (alive[ friends[i] ]) {
                num_alive_friends++;
            }
        }
        for (int i=0; i<enemies; i++) {
            if (alive[ enemies[i] ]) {
                num_alive_enemies++;
            }
        }
        for (int i=0; i<nplayers; i++) {
            if (alive[ i ]) {
                num_alive_players++;
            }
        }
        num_alive_neutrals = num_alive_players - num_alive_friends - num_alive_enemies;
        */

    }


    public int shoot(int[] prevRound, boolean[] alive) {
        // Store the round history
        int target = -1;

        if (roundNumber==0) {
            roundNumber++;
            return -1;
        }

        /*
        if (!provoked && num_of_alive(alive) > 3) {
            roundNumber++;
            return -1;
        }
        */

        update_stats(prevRound, alive);
        this.prevRound = prevRound;
        this.alive = alive;

        if (num_of_alive() <= 3) {
            roundNumber++;

            Vector<Integer> alive_e = new Vector<Integer>();
            Vector<Integer> alive_f = new Vector<Integer>();
            Vector<Integer> alive_n = new Vector<Integer>();

            for (int i=0; i<alive.length; i++) {
                if (alive[i] && i != id) {
                    if (is_enemy(i)) {
                        alive_e.add(new Integer(i));
                    } else {
                        if (is_friend(i)) 
                            alive_f.add(new Integer(i));
                        else
                            alive_n.add(new Integer(i));
                    }
                }
            }

            if (alive_f.size() == 2)
                return -1;

            if (alive_f.size() == 1 && alive_n.size() == 1)
                return alive_n.get(0);

            if (alive_f.size() == 1 && alive_e.size() == 1) {
                int f = alive_f.get(0);
                int e = alive_e.get(0);
                if (shootRecord[f][e] > 0)
                    return e;
                else
                    return -1;
            }

            if (alive_n.size() == 2) {
                int n1 = alive_n.get(0);
                int n2 = alive_n.get(1);

                if (prevRound[n1] == n2) return n2;
                if (prevRound[n2] == n1) return n1;
                return -1;
            }

            if (alive_e.size() == 2 ) {
                int e1 = alive_e.get(0);
                int e2 = alive_e.get(1);

                if (prevRound[e1] == e2) return e2;
                if (prevRound[e2] == e1) return e1;
                return -1;
            }

            if (alive_n.size() == 1 && alive_e.size() ==1) {
                int n = alive_n.get(0);
                int e = alive_e.get(0);

                if (prevRound[n] == e) return e;
                if (prevRound[e] == n) return e;
                return -1;
            }
        }

        //System.out.println("--------------");

        this.calculatePriority(prevRound, alive);

        // calculate who to shoot by max priority
        target = -1;
        double max=0.0;
        for (int i=0; i< priority.length; i++) {
            if (alive[i] && is_enemy(i))
                target = i;
            max = priority[i];
        }

        for (int i = 0; i < priority.length; i++) {
            if (priority[i] > max && alive[i]) {
                target = i;
                max=priority[i];
            }
        }

        if (max <= SHOOTING_THRESHOLD) target = -1;

        roundNumber++;
        return target;
    }

    public void print_priority() {
        //System.out.println("Priority for round " + roundNumber);

        for (int i=0; i<nplayers; i++) {
            //System.out.print(priority[i] + " ");
        }

        //System.out.println();
        //System.out.println();
    }


    public void calculatePriority(int prevRound[], boolean[] alive) {
        // System.out.println("calculating priority\n\n\n\n\n\n");
        for (int i = 0; i < nplayers; i++) {
            priority[i] = 0;
        }

        for (int i=0; i < nplayers; i++) {
            int shooter = i;
            int victim = prevRound[i];

            if (!validTarget(shooter) || !validTarget(victim)) {
                continue;
            }

            if (is_enemy(shooter)) {
                // System.out.println("shooting enemy");
                priority[shooter] += 10;
            }

            if (hasBeenShot(shooter) != -1 && constShoot[shooter][victim] && is_enemy(victim)) {
                // System.out.println("shooting constant enemy");
                priority[victim] += 10;
            }

            if (hasBeenShot(id) != -1) {
                int who_shot_me = hasBeenShot(id);
                if (hasBeenShot(who_shot_me) != -1) {
                    int who_shot_who_shot_me = hasBeenShot(who_shot_me);
                    if (validTarget(who_shot_who_shot_me) && hasBeenShot(who_shot_who_shot_me) == -1) {
                        // System.out.println("weird case");
                        if (is_enemy(who_shot_me)) {
                            priority[who_shot_me] += 9;
                        } else if (is_neutral(who_shot_me)) {
                            priority[who_shot_me] += 8;
                        }
                    }
                }
            }

        }

        // if any of our friends have been shot, then retaliate
        for (int j = 0; j < nplayers; j++) {
            if (alive[j] && is_friend(j) && hasBeenShot(j) != -1) {
                // System.out.println("helping friend");
                int who_shot_friend = hasBeenShot(j);
                if (alive[who_shot_friend]) {
                    if (is_enemy(who_shot_friend)) {
                        priority[who_shot_friend] += 6;
                    } else if (is_neutral(who_shot_friend)) {
                        priority[who_shot_friend] += 5;
                    }
                }
            }
        }

        if (num_alive_enemies < num_alive_friends) {
            for (int i = 0; i < nplayers; i++) {
                for (int j = 0; j < nplayers; j++) {
                    if (alive[i] && is_friend(i) && constShoot[i][j] && !is_friend(j)) {
                        priority[j] += 5;
                    }
                }
            }
        }

        /*
        System.out.println("printing priority");
        System.out.println(Arrays.toString(priority));
        */
    }

    public boolean validTarget(int i) {
        if (i != -1 && this.alive[i]) {
            return true;
        }
        return false;
    }

    public int hasBeenShot(int player) {
        for (int i = 0; i < nplayers; i++) {
            if (this.prevRound[i] == player) {
                return i;
            }
        }
        return -1;
    }

    public int num_of_alive() {
        int num = 0;
        for (int i = 0; i < nplayers; i++) {
            if (this.alive[i]) {
                num++;
            }
        } 
        return num;
    }

    private Random gen;
    private int nplayers;
    private int roundNumber;
    private boolean provoked = false;
    private int[] friends;
    private int[] enemies;
    private int[][] roundHistory;
    private int[][] shootRecord;
    private boolean[][] constShoot;
    private boolean[] retaliate;
    private boolean[] alive;
    private int[] prevRound;

    private double[] priority;

    private int num_alive_players;
    private int num_alive_enemies;
    private int num_alive_friends;
    private int num_alive_neutrals;
    private int config_type;

    private static final double SHOOTING_THRESHOLD = 0.0;
    private static final double ENEMY_SHOOTING_YOU = .5;
    private static final double NEUTRAL_SHOOTING_YOU = .4;
    private static final double FRIEND_SHOOTING_YOU = .35;
    private static final double ENEMY_SHOOTING_FRIEND = .3;
    private static final double NEUTRAL_SHOOTING_FRIEND = .25;
    private static final double ENEMY_CONSISTENTLY_SHOT = .2;

    private static final double ENEMY = .1;
    private static final double FRIEND_CONST_SHOOT_ENEMY = 0.45;
    private static final double FRIEND_SHOOT_ENEMY = 0.4;
    private static final double NEUTRAL_CONST_SHOOT_ENEMY = 0.4;
    private static final double ENEMY_CONST_SHOOT_ENEMY = 0.4;
}
