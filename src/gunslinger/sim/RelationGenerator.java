package gunslinger.sim;

import java.util.ArrayList;
import java.util.Random;

public class RelationGenerator
{
    public static int[][] generate(int nplayers, int nfriends, int nenemies)
    {
        // impossible to generate
        if (nplayers * nfriends % 2 == 1)
            return null;

        // relationship matrix
        int[][] relationship = new int[nplayers][nplayers];
        genFriendship(relationship, nplayers, nfriends);
        genEnmityship(relationship, nplayers, nenemies);
        return relationship;
    }

    public static int[][] getFriendList(int[][] relationship, int nplayers, int nfriends) {
        int[][] friendship = new int[nplayers][nfriends];
        
        for (int i = 0; i < nplayers; i++) {
            int k = 0;
            for (int j = 0; j < nplayers; j++) {
                if (relationship[i][j] == 1)
                    friendship[i][k++] = j;
            }
        }
        return friendship;
    }

    public static int[][] getEnemyList(int[][] relationship, int nplayers, int nenemies) {
        int[][] enmityship = new int[nplayers][nenemies];
        
        for (int i = 0; i < nplayers; i++) {
            int k = 0;
            for (int j = 0; j < nplayers; j++) {
                if (relationship[i][j] == -1)
                    enmityship[i][k++] = j;
            }
        }
        return enmityship;
    }

    // generate enemies
    //
    private static boolean genEnmityship(int[][] relationship, int nplayers, int nenemies)
    {
        for (int p = 0; p != nplayers; ++p) {
            ArrayList<Integer> plist = new ArrayList<Integer>();
            for (int i = 0; i != nplayers; ++i)
                if (i != p && relationship[p][i] == 0)
                    plist.add(i);
            java.util.Collections.shuffle(plist);
            
            for (int e = 0; e != nenemies; ++e)
                relationship[p][plist.get(e)] = -1;
        }            
        return true;
    }


    // generate friends
    //
    private static boolean genFriendship(int[][] relationship, int nplayers, int nfriends)
    {
        // player - current edges mapping
        int[] playerToEdges = new int[nplayers];

        // initially all players have 0 edges
        for (int i = 0; i < nplayers; i++)
            playerToEdges[i] = 0;

        // edges - player ids
        ArrayList<ArrayList<Integer>> edgeToPlayers = 
            new ArrayList<ArrayList<Integer>>();

        // initialize
        for (int i = 0; i <= nfriends; i++)
            edgeToPlayers.add(new ArrayList<Integer>());

        // initially, all players have in the 0-edge gropu
        for (int i = 0; i < nplayers; i++)
            edgeToPlayers.get(0).add(i);

        
        // find friends for players in order
        for (int i = 0; i < nplayers; i++) {
            // if already has enough friends
            if (playerToEdges[i] == nfriends)
                continue;

            // remove current player from its edge group
            edgeToPlayers.get(playerToEdges[i]).remove(new Integer(i));
            
            // find until player i has nfriends
            while (playerToEdges[i] < nfriends) {
                int friend = -1;
                for (int e = 0; e < nfriends; e++) {
                    ArrayList<Integer> group = edgeToPlayers.get(e);
                    if (!group.isEmpty()) {
                        java.util.Collections.shuffle(group);
                        for (int k = 0; k < group.size(); k++) {
                            friend = group.get(k);
                            // check if edge already exists?
                            // can we be his friend?
                            if (relationship[i][friend] == 0)
                                break;
                        }
                    }
                    // if we have found a friend
                    if (friend != -1)
                        break;
                }
                
                // The generation has failed!!!
                if (friend == -1)
                    return false;
                
                // update matrix
                relationship[i][friend] = 1;
                relationship[friend][i] = 1;
                
                // update maps
                edgeToPlayers.get(playerToEdges[friend]).remove(new Integer(friend));
                edgeToPlayers.get(playerToEdges[friend]+1).add(friend);
                playerToEdges[friend]++;
                playerToEdges[i]++;
            }
            
            // add player i to the last group
            edgeToPlayers.get(nfriends).add(i);
        }

        // all players should be in the last edge group
        assert edgeToPlayers.get(nfriends).size() == nplayers;
    
        return true;
    }

    private static boolean checkRelationship(int[][] relationship, int players, int friends, int enemies)
    {
        if (relationship == null)
            return false;

        // wrong matrix dimension
        if (players != relationship.length)
            return false;

        for (int i = 0; i < relationship.length; i++) {
            int friend = 0, enemy = 0;
            
            // wrong matrix dimension
            if (relationship[i].length != players)
                return false;

            // cannot connect to itself
            if (relationship[i][i] != 0)
                return false;

            for (int j = 0; j < relationship[i].length; j++) {
                if (relationship[i][j] == 1)
                    friend++;
                if (relationship[i][j] == -1)
                    enemy++;
            }
            if (friend != friends || enemy != enemies)
                return false;
        }
        return true;
    }


    // generate friend relations
    //
    // private int[][] genFriendship(int nplayers, int nfriends, int[][] relatinship)
    // {
    //     int[][] friendship;
    //     int[] curfriends;
    //     while (true) {
    //         // reset relationship
    //         for (int i = 0; i != nplayers; ++i)
    //             for (int j = 0; j != nplayers; ++j)
    //                 relationship[i][j] = 0;

    //         friendship = new int[nplayers][nfriends];
    //         curfriends  = new int[nplayers];

    //         ArrayList<Integer> baselist = new ArrayList<Integer>();
    //         for (int i = 0; i != nplayers; ++i)
    //             baselist.add(i);

    //         // randomly generate friends
    //         for (int p = 0; p != nplayers - 1; ++p) {
    //             // new friends to generate
    //             int newfriends = nfriends - curfriends[p];

    //             // generate a random permutation of following players
    //             Integer[] permlist = baselist.subList(p+1,nplayers).toArray(new Integer[0]);
    //             java.util.Collections.shuffle(Arrays.asList(permlist), gen);
            
    //             // pick top-k of the permuation
    //             for (int i = 0; newfriends > 0 && i != permlist.length; ++i) {
    //                 int friend = permlist[i];
                
    //                 // skip player that has already had enough friends
    //                 if (curfriends[friend] == nfriends)
    //                     continue;
                
    //                 // set friendship
    //                 relationship[p][friend] = 1;
    //                 relationship[friend][p] = 1;
    //                 friendship[p][curfriends[p]] = friend;
    //                 friendship[friend][curfriends[friend]] = p;

    //                 // update friend count
    //                 curfriends[p]++;
    //                 curfriends[friend]++;

    //                 newfriends--;
    //             }
    //         }

    //         boolean success = true;
    //         for (int p = 0; p != nplayers; ++p) {
    //             // generation fails
    //             // try again
    //             if (curfriends[p] != nfriends) {
    //                 success = false;
    //                 break;
    //             }
    //         }

    //         if (success)
    //             break;
    //         else
    //             System.err.println("Generation friendship fails, retry!");
    //     }

    //     return friendship;
    // }

    // // generate enemies
    // //
    // private int[][] genEnmityship(int nplayers, int nenemies, int[][] relationship)
    // {
    //     int[][] enmityship = new int[nplayers][nenemies];
        
    //     for (int p = 0; p != nplayers; ++p) {
    //         ArrayList<Integer> plist = new ArrayList<Integer>();
    //         for (int i = 0; i != nplayers; ++i)
    //             if (i != p && relationship[p][i] == 0)
    //                 plist.add(i);
    //         java.util.Collections.shuffle(plist);
            
    //         for (int e = 0; e != nenemies; ++e) {
    //             relationship[p][plist.get(e)] = -1;
    //             enmityship[p][e] = plist.get(e);
    //         }
    //     }            
    //     return enmityship;
    // }



    public static void main(String[] args) {
        //        int players = Integer.parseInt(args[0]);
        //        int friends = Integer.parseInt(args[1]);
        //        int enemies = Integer.parseInt(args[2]);
        //        int repeats = Integer.parseInt(args[3]);
        int repeats = 1000;
        Random random = new Random();

        for (int players = 2; players < 20; players++) {
            for (int friends = 1; friends <= players - 1; friends++) {
                if (friends * players % 2 == 1)
                    continue;
                for (int i = 0; i < repeats; i++) {
                    int enemies = random.nextInt(players - friends);
                    int[][] relation = generate(players, friends, enemies);
                    if (!checkRelationship(relation, players, friends, enemies)) {
                        System.err.println("Generation fails");
                    }
                }

            }
        }
    }
}
