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
    
    // A simple fixed shoot rate strategy used by the dumb player
    private static double ShootRate = 0.8;
    
    // name of the team
    //
    public String name()
    {
        return "g9" + (versions > 1 ? " v" + version : "");
    }

    // Initialize the player
    //
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
        
        this.roundNumber = 0;
        this.nplayers = nplayers;
        this.friends = friends.clone();
        this.enemies = enemies.clone();

        priority = new double[nplayers];
        roundHistory = new int[100][nplayers];
        shootRecord = new int[nplayers][nplayers];
        constShoot = new boolean[nplayers][nplayers];
        
        for (int i = 0; i < nplayers; i++) {
            this.priority[i] = 0.0;
        }
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
    void update_matrix(int[] prevRound, boolean[] alive) {

        roundHistory[roundNumber] = prevRound;

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

    }

    int num_of_alive( boolean[] alive ) {
        int ret = 0;
        for (int i=0; i<nplayers; ++i)
            if (alive[i])
                ret++;
        return ret;
    }
    
    // Pick a target to shoot
    // Parameters:
    //  prevRound - an array of previous shoots, prevRound[i] is the player that player i shot
    //              -1 if player i did not shoot
    //  alive - an array of player's status, true if the player is still alive in this round
    // Return:
    //  int - the player id to shoot, return -1 if do not shoot anyone
    //
    public int shoot(int[] prevRound, boolean[] alive) {
        // Store the round history
        int target = -1;

        if (roundNumber == 0) {

            for (int i = 0; i < nplayers; ++i) {
                if (i != id && alive[i] && is_enemy(i)) {
                    target = i;
                    break;
                }
            }
            roundNumber++;
            //print_priority();
            return target;
        }

        update_matrix(prevRound, alive);

        if (num_of_alive(alive) == 2) {
            roundNumber++;
            //print_priority();
            return -1;
        }
        
        if (num_of_alive(alive) <= 3) {

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

            if (alive_f.size() == 2) {
                roundNumber++;
                //print_priority();
                return -1;
            }

            if (alive_f.size() == 1 && alive_n.size() == 1) {
                roundNumber++;
                //print_priority();
                return alive_n.get(0);
            }

            if (alive_f.size() == 1 && alive_e.size() == 1) {
                int f = alive_f.get(0);
                int e = alive_e.get(0);
                if (shootRecord[f][e] > 0) {
                    roundNumber++;
                    //print_priority();
                    return e;
                } else {
                    roundNumber++;
                    //print_priority();
                    return -1;
                }
            }

            if (alive_n.size() == 2) {
                int n1 = alive_n.get(0);
                int n2 = alive_n.get(1);
                roundNumber++;

                if (prevRound[n1] == n2) {
                    //print_priority();
                    return n2;
                }
                if (prevRound[n2] == n1) {
                    //print_priority();
                    return n1;
                }
                //print_priority();
                return -1;
            }

            if (alive_e.size() == 2 ) {
                int e1 = alive_e.get(0);
                int e2 = alive_e.get(1);

                roundNumber++;
                //print_priority();
                if (prevRound[e1] == e2) {
                    return e2;
                }
                if (prevRound[e2] == e1) {
                    return e1;
                }
                return -1;
            }

            if (alive_n.size() == 1 && alive_e.size() ==1) {
                int n = alive_n.get(0);
                int e = alive_e.get(0);

                roundNumber++;
                //print_priority();
                if (prevRound[n] == e) {
                    return e;
                }
                if (prevRound[e] == n) {
                    return e;
                }
                return -1;
            }
        }

        System.out.println("--------------");

        /* Strategy used by the dumb player:
         Decide whether to shoot or not with a fixed shoot rate
         If decided to shoot, randomly pick one alive that is not your friend */
        



        this.calculatePriority();

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
        
        roundNumber++;
        //print_priority();
        return target;
    }

    public void print_priority() {
        System.out.println("Priority for round " + roundNumber);

        for (int i=0; i<nplayers; i++) {
            System.out.print(priority[i] + " ");
        }

        System.out.println();
        System.out.println();
    }

    
    public void calculatePriority() {
        boolean[] hasPriorityAssigned = new boolean[nplayers];


        for (int i=0; i<nplayers; i++) {
            int shooter = i;
            int victim = roundHistory[roundNumber][i];

            // anyone shoot us
            if (victim == id) {
                if (is_enemy(shooter)) {
                    priority[shooter] += ENEMY_SHOOTING_YOU;
                } else if (is_friend(shooter)) {
                    priority[shooter] += FRIEND_SHOOTING_YOU;
                } else {
                    priority[shooter] += NEUTRAL_SHOOTING_YOU;
                }
                hasPriorityAssigned[shooter] = true;
            }




            // anyone shoot friend
            if (is_friend(victim)) {
                if (!is_friend(shooter)) {
                    if (is_enemy(shooter)) {
                        priority[shooter] += ENEMY_SHOOTING_FRIEND;
                    } else {
                        priority[shooter] += NEUTRAL_SHOOTING_FRIEND;
                    }
                    hasPriorityAssigned[shooter] = true;
                }
            }




            // friend shoot enemy
            if (is_friend(shooter) && is_enemy(victim)) {
                if (constShoot[shooter][victim])
                    priority[victim] += FRIEND_CONST_SHOOT_ENEMY;
                else
                    priority[victim] += FRIEND_SHOOT_ENEMY;
            }

            // neutral shoot enemy
            if (is_neutral(shooter) && is_enemy(victim)) {
                if (constShoot[shooter][victim])
                    priority[victim] += NEUTRAL_CONST_SHOOT_ENEMY;
                else {
                }
            }

            // enemy shoot enemy
            if (is_enemy(shooter) && is_enemy(victim)) {
                if (constShoot[shooter][victim])
                    priority[victim] += ENEMY_CONST_SHOOT_ENEMY;
            }




            // friend shoot neutral
            if (is_friend(shooter) && is_neutral(victim)) {
            }

            // neutral shoot neutral
            if (is_neutral(shooter) && is_neutral(victim)) {
            }

            // enemy shoot neutral
            if (is_enemy(shooter) && is_neutral(victim)) {
            }
        }
       
        // Shooting an enemy who doesn't fit the above cases
        for (int i = 0; i < enemies.length; i++) {
            if (!hasPriorityAssigned[enemies[i]]) {
                priority[enemies[i]] += ENEMY;
            }
        }

    }
    
    private Random gen;
    private int nplayers;
    private int roundNumber;
    private int[] friends;
    private int[] enemies;
    private int[][] roundHistory;
    private int[][] shootRecord;
    private boolean[][] constShoot;
    
    private double[] priority;
    
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
