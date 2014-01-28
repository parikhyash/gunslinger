//Deliverable2 9/15
package gunslinger.g7;

import java.util.*;

// An example player
// Extends gunslinger.sim.Player to start with your player
//
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
        return "g7" + (versions > 1 ? " v" + version : "");
    }
 
    // Initialize the player
    //
    public void init(int nplayers, int[] friends, int enemies[])
    {
        friendWeight=1.0;
        weightD=0.7;
        weightT=0.7;
        threatWeight=0.3;
        dyingWeight=0.2;
        enemyWeight=0.4;
        
        this.nplayers = nplayers;
        this.friends = friends.clone();
        this.enemies = enemies.clone();
        playerArr = new PlayerStats[nplayers];
        for(int i=0;i<nplayers;i++)
        	playerArr[i] = new PlayerStats();
        
        for(int j=0;j<friends.length;j++)
            playerArr[friends[j]].setFriend(true);
        
        for(int j=0;j<enemies.length;j++)
            playerArr[enemies[j]].setEnemy(true);
        
        for(int i=0;i<nplayers;i++)
        	if(!playerArr[i].getFriend() && !playerArr[i].getEnemy())
        	   playerArr[i].setNeutral(true);
        
        priorityShoot = new double[nplayers];
        round=1;
        
        //printRelationships();
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
        /* Strategy used by the dumb player:
           Decide whether to shoot or not with a fixed shoot rate
           If decided to shoot, randomly pick one alive that is not your friend */
                
        // Shoot or not in this round?
    	int target=-1;
        boolean shoot=true;
    	if(round==1)
    	{
    		double ratio= (double)(nplayers-enemies.length-friends.length-1)/enemies.length;
    		//System.out.println("RATIO: " + ratio);
    		if (ratio>0.4)
    			shoot=false;
    		if (shoot)
    			target=enemies[0];
    		round++;
    		return target;
    	}
    	else
    	{
    		for(int i=0;i<nplayers;i++)
    			if(!alive[i])
    			   playerArr[i].died();
    		
    		friendWeight = (double) enemies.length/enemiesAlive();
    		//System.out.println(friendWeight);
    		
    		//Check if friend was shot here.
    		checkShotFriend(prevRound);
    		
    		//Ranking Formula to determine which player to shoot
    		double tempVar;
    		for (int i=0;i<nplayers;i++) 
    		{
    			//Probability that player dies
    			tempVar=playerArr[i].getProbDie();
    				
    			if (ArrayContains(prevRound, i)) 
    			{
    				tempVar=tempVar*(1-weightD)+weightD;
    				//System.out.println(i + " was Shot");
    			}
    			else
    				tempVar=tempVar*(1-weightD);
    			playerArr[i].setProbDie(tempVar);

    			//Probability that player is a threat to us
    			tempVar=playerArr[i].getProbThreat();
    			if (prevRound[i]==id)
    				tempVar=tempVar*(1-weightT)+weightT;
    			else
    				tempVar=tempVar*(1-weightT);
    			playerArr[i].setProbThreat(tempVar);
    		
    			//Don't shoot friends or players that are already dead
    			if (playerArr[i].getFriend() || !playerArr[i].getAlive())
    				priorityShoot[i]=0;
    			else 
    			{
    				//Determine priority of enemies or neutrals based on values calculated above
    				tempVar=threatWeight*playerArr[i].getProbThreat()+dyingWeight*playerArr[i].getProbDie();
    				if (playerArr[i].getEnemy())
    					tempVar=tempVar+enemyWeight;
    				
    				//How often our friends were shot by another player
    				tempVar=tempVar+friendWeight*(playerArr[i].getNumShotFriends()/round);
    				priorityShoot[i]=tempVar;
    			}
    		}
    		
    		//Shoot the player with the highest priority
    		int to_shoot=getMaxIndex(priorityShoot);
    		if(to_shoot>0 && to_shoot!=id)
    			target=to_shoot;
    	
	    	/*
	    	System.out.print("[");
	    	for(int i=0; i < priorityShoot.length; i++) {
	    		System.out.print(i + ":" + priorityShoot[i] + " ");
	    	}
	    	System.out.println("]");
	    	*/
    	
	    	round++;	
	    	return target;				
    	}
    }
    
    //Get the maximum index within an array
    public int getMaxIndex(double a[])
    {
    	double max=0;
    	int maxindex = 0;
    	for(int i=0;i<a.length;i++)
    		if(a[i]>max)
    		{
    			max=a[i];
    			maxindex=i;
    		}
    	return maxindex;
    }
    
    //Check if a friend was shot in the previous round
    public void checkShotFriend(int prev[]) 
    {
    	//System.out.println("CheckShotFriends");
    	for(int i=0; i < prev.length; i++) 
    	{
    		for(int j=0; j < friends.length; j++) 
    		{
    			if(prev[i] == friends[j]) 
    			{
    				playerArr[i].shotFriend();
    				//System.out.println("Player" + i + " shot friend " + prev[i]);
    			}
    			
    		}
    		/*
    		if(Arrays.asList(friends).contains(prev[i])) {
    			playerArr[i].shotFriend();
    			System.out.println("Player" + i + " shot friend " + prev[i]);
    		}
    		*/
    	}
    }
    
    public void printRelationships() {
    	System.out.println("Enemies: [");
    	for(int i=0; i < enemies.length; i++) 
    		System.out.print(enemies[i] + " ");
    	System.out.print("]");
    	System.out.println();
    	
    	System.out.println("Friends: [");
    	for(int i=0; i < friends.length; i++)
    		System.out.print(friends[i] + " ");
    	System.out.print("]");
    	System.out.println();
    	
    }
    
    //Check if an array contains value i
    public boolean ArrayContains(int arr[], int i) 
    {
    	for(int j=0; j < arr.length; j++) 
    	{
    		if(arr[j] == i)
    			return true;
    	}
    	
    	return false;
    }
    
    //Count how many enemies are still alive
    public double enemiesAlive() 
    {
    	double x = 0;
    
    	for(int i=0; i < playerArr.length; i++) 
    	{
    		if(playerArr[i].getEnemy() && playerArr[i].getAlive()) 
    		{
    			x++;
    			//System.out.println("enemy alive");
    		}
    	}
    	
    	//Handles divide by 0 case
    	if(x == 0)
    		x = 0.6;
    	//System.out.println("enemiesAlive: " + x);
    	return x;
    }
    
    private int nplayers;
    private int[] friends;
    private int[] enemies;
    private double[] priorityShoot;
    private PlayerStats playerArr[];
    
    private double friendWeight;
    private double weightD;
    private double weightT;
    private double threatWeight;
    private double dyingWeight;
    private double enemyWeight;
    
    private int round;
}
