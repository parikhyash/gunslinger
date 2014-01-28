package gunslinger.g2;

import java.util.*;


public class Player extends gunslinger.sim.Player
{
    // total versions of the same player
    private static int versions = 0;
    // my version no
    private int version = versions++;
    
    //Local copies of necessary variables
    private int nplayers;
    private int[] friends;
    private int[] enemies;
    
    
    
    //Book-keeping variables
    private PriorityQueue<PriorityTuple> priorityList;
    private int[][] whoShotWhomCount;
    //bingyi-build the dynamic 2D table to record history
    private List<List<Integer>> record = new ArrayList<List<Integer>>();
    //bingyi
    
    //yash
    private int prediction[][];
    private double next_round[][];
    
    // name of the team
    public String name()
    {
        return "g2" + (versions > 1 ? " v" + version : "");
    }
 
    // Initialize the player
    //
    public void init(int nplayers, int[] friends, int enemies[])
    {
    	 this.nplayers = nplayers;
         this.friends = friends.clone();
         this.enemies = enemies.clone();
         
         for(int i=0;i<nplayers;i++)
         {
         	record.add(new ArrayList<Integer>());
         }
         
         whoShotWhomCount=new int[nplayers][nplayers];
         priorityList=new PriorityQueue<PriorityTuple>(10, new PriorityTupleComparator());
         
         //yash
         prediction=new int[this.nplayers][this.nplayers];
         next_round=new double[this.nplayers][this.nplayers];
         
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
    	
        if(prevRound==null)
        {
        	return -1; //Make love, not war
        }
        
        
        System.out.println(Arrays.toString(friends));
        
        //Keep track of who got shot, how many times earlier
        boolean[] shotAt=new boolean[prevRound.length];
        int[] shotBy=new int[prevRound.length];
        Arrays.fill(shotBy,-1);
        
        for(int player=0;player<prevRound.length;player++)
        {
        	if(prevRound[player]==-1)
        		continue;
        	shotBy[prevRound[player]]=player;
        	shotAt[prevRound[player]]=true;
        }
        
        //bingyi
        for(int player=0;player<prevRound.length;player++)
        	record.get(player).add(prevRound[player]);
        
        //yash
        //call prediction function here
        predictNextRound(next_round,record, alive);
        
        //summing up next_round stats
        double[] next_round_sum=new double[nplayers];
        
        //add the columns of prediction matrix to get player most likely to be shot at
        for(int i=0;i<nplayers;i++)
        {
        	for(int j=0;j<nplayers;j++)
        	{
        		next_round_sum[i]+=next_round[j][i];
        	}
        }
        
        //d
        System.out.println("Typical "+Arrays.toString(next_round_sum));
        
        for(int player=0;player<prevRound.length;player++)
        {
        	if(prevRound[player]!=-1)
        		whoShotWhomCount[player][prevRound[player]]++;
        	gaugeSeverity(player,prevRound[player],alive,shotAt,shotBy,prevRound,next_round_sum);
        }
        //you shoot; he's still alive
    	if(prevRound[this.id]!=-1 && alive[prevRound[this.id]])
    	{
    		priorityList.add(new PriorityTuple(prevRound[this.id],CON));
    	}
        
        
        //Printing the next_round_sum array returned
        for(int i=0;i<next_round.length;i++)
        {
        	for(int j=0;j<next_round.length;j++)
        	{
        		System.out.print(next_round[i][j]);
        	}
        	System.out.println();
        }
        
        
        /*
        int popular_target[]=new int[prevRound.length];
        int players[]=new int[prevRound.length];
        
        for(int i=0;i<players.length;i++)
        {
        	players[i]=i;
        }
        
        
        
        //sort player and popular_target arrays to get sorted list of targets
        for(int i=0;i<popular_target.length;i++)
        {
        	for(int j=0;j<popular_target.length-1;j++)
        	{
        		if(popular_target[j]<popular_target[j+1])
        		{
        			int temp=popular_target[j];
        			popular_target[j]=popular_target[j+1];
        			popular_target[j+1]=temp;
        			temp=players[j];
        			players[j]=players[j+1];
        			players[j+1]=temp;
        		}
        	}
        }
        //We only need the most likely target, but I'm sorting the list just to get an idea of what our targets are like
        
        //printing players and their likeliness to be shot
        for(int j=0;j<prediction.length;j++)
    	{
    		System.out.print("("+players[j]+","+popular_target[j]+") ");
    	}
    	System.out.println();
        
        */
        /*
        //shoot the most likely player if he is not a friend
        for(int i=0;i<popular_target.length;i++)
        {
        //	if((isEnemy(players[i]) || isNeutral(players[i])) && players[i]!=this.id && alive[players[i]])
        	if((isEnemy(players[i]) || Enemyus(players[i], record) || EnemyourAF(players[i], record, alive))&& players[i]!=this.id && alive[players[i]])	
		{
        		return players[i];
        	}
        }
        */
        
        //Choose whom to shoot
        
        //if no enemies or neutrals are to be shot at
        if(priorityList.size()==0)
        {
        	 priorityList.clear();
        	 return -1;
        }
        //yash
        
        
        //ArrayList<Integer> targets=new ArrayList<Integer>();
        int target;
        PriorityTuple firstTuple;
        double maxNextRoundSum;
        try
        {
        do
        {
        firstTuple=priorityList.remove();
        
        maxNextRoundSum=next_round_sum[firstTuple.playerId];
        target=firstTuple.playerId;
        //targets.add(firstTuple.playerId);
        }
        while(isFriend(target));
        }
        catch(NullPointerException e)
        {
        	return -1;
        }
        
        while(priorityList.size()!=0 && firstTuple.priority==priorityList.peek().priority)
        {
        	PriorityTuple anotherTuple=priorityList.remove();
        	//targets.add(tupleToAdd.playerId);
        	if(next_round_sum[anotherTuple.playerId]>maxNextRoundSum)
        	{
        		maxNextRoundSum=next_round_sum[anotherTuple.playerId];
        		target=anotherTuple.playerId;
        	}	
        }
        
        priorityList.clear();
        return target;
        
        
//        System.out.println("this is our hate most list\n");
//        printListofArray(hate_most); 
//        if(priorityList.size()==0)
//        	return -1;
//        
//        attentionSeekingPrint(priorityList.toString());
        
//        int myTarget=getMyTarget(shotAt);
//        priorityList.clear();
//    	 //bingyi
//        printMatrix(record);
//        //bingyi		
//        return myTarget;
    }
    
    //bingyi
	public void predictNextRound(double[][] next_round, List<List<Integer>> m, boolean[] alive)	
	{
		next_round=new double[nplayers][nplayers];
		int columns = m.get(0).size();
		int rows = m.size();
		
		for (int i=0; i<rows;i++) {
			
			//if someone shoot the other last round, most likely the other will shot back next_round
			if (m.get(i).get(columns-1) != -1) {
				if (alive[i] && alive[m.get(i).get(columns-1)]) {
					next_round[m.get(i).get(columns-1)][i]=RETAL_PROB;
					next_round[i][m.get(i).get(columns-1)]=CONS_PROB;
				}
				
			}
			
			for (int j=0; j<columns;j++) {
				
				//d
				System.out.println(alive.length+" "+i+" "+m.get(i).get(j)+" "+columns);
				
				if (m.get(i).get(j) != -1 && alive[i] && alive[m.get(i).get(j)]) {
					next_round[i][m.get(i).get(j)]+=SHOOT_AGAIN_PROB;
				}
			}
			
		}
		
		//Normalize
		/*
		for(int row=0;row<nplayers;row++)
		{
			double sum=0;
			for(int col=0;col<nplayers;col++)
			{
				System.out.println("--"+next_round[row][col]);
				sum+=next_round[row][col];
			}
			for(int col=0;col<nplayers;col++)
			{
				next_round[row][col]=1.0*next_round[row][col]/sum;
				
				//d
				System.out.println(String.format("next rd %d %d %.5f",row,col,next_round[row][col]));
			}
			
		}*/
	}
	
	public void printpredict(int[][] m){
	for (int i=0; i<m[0].length; i++) { 
		for (int j=0;j<m[0].length;j++) {
    			System.out.println( m[i][j]+" ");
  		} 
  		System.out.println();
  		}
	}
    //bingyi 
    public void attentionSeekingPrint(String s)
    {
    	System.out.println("----------------\n"+s+"\n-----------------");
    }
    
    private int getMyTarget(int[] shotAt) 
    {
		
    	/*PriorityTuple firstTuple=priorityList.remove();
    	if(shotAt[firstTuple.getPlayerId()]>0)
    		return firstTuple.getPlayerId();
    	
    	PriorityTuple nextTuple;
    	if(priorityList.size()!=0)
    		nextTuple=priorityList.remove();
    	
    	while(nextTuple.getPriority()==firstTuple.getPriority())
    	{
    		if(shotAt[firstTuple.getPlayerId()]<shotAt[nextTuple.getPlayerId()])
    			return nextTuple.getPlayerId();
    		
    		if(priorityList.size()!=0)
        		nextTuple=priorityList.remove();
    		else
    			break;
    	}
    	
    	return firstTuple.getPlayerId();*/
    	
    	return priorityList.remove().getPlayerId();
    	
	}

	private void gaugeSeverity(int shooter,int target, boolean[] alive,boolean[] shotAt, int[] shotBy, int[] prevRound,double[] next_round_sum) {
		
		//d
		System.out.println(shooter+" "+target);
		
		if(target==-1 || shooter==this.id)
			return;
		
		//anyone shooting you
		if(target==this.id && alive[shooter] && !isFriend(shooter))
			priorityList.add(new PriorityTuple(shooter,RET));

		//enemy shot by someone not shot
		if(isEnemy(target) && (!shotAt[shooter] || !alive[shotBy[shooter]]) && alive[shooter] && alive[target])
			priorityList.add(new PriorityTuple(target, SEC_PRI));
		

		// shot by enemy when silent
		if(isEnemy(shooter) && prevRound[target]==-1 && alive[shooter] && alive[target])
			priorityList.add(new PriorityTuple(shooter, SEC_PRI));
		
		//Think more about the following two
		//friend is shot by someone when he doesn shoot
		if(isFriend(target) && prevRound[target]==-1 && alive[target] && alive[shooter])
		{
			if(isEnemy(shooter))
				priorityList.add(new PriorityTuple(shooter, SEC_PRI));
			else
				priorityList.add(new PriorityTuple(shooter, SEC_PRI2));
		}
			
		//friend shoots someone and is not shot by anyone
		if(isFriend(shooter) && !shotAt[shooter] && alive[target])
		{
			if(isEnemy(target))
				priorityList.add(new PriorityTuple(target, SEC_PRI));
			else
				priorityList.add(new PriorityTuple(target, SEC_PRI2));
		}
		
		//Lower priority stuff
		//f -> e
		if(isFriend(shooter)&&isEnemy(target)&&alive[target])
    	{
    		priorityList.add(new PriorityTuple(target, F2E));
    	}
		
		//e -> f
		if(isFriend(target)&&isEnemy(shooter)&& alive[target] && alive[shooter])
    	{
			priorityList.add(new PriorityTuple(shooter,E2F));
    	}
		
		//Lowest priority.
		//n -> e
		if(isNeutral(shooter) && isEnemy(target) && alive[target] && alive[shooter] && next_round_sum[target]>=THRESHOLD)
    	{
    		priorityList.add(new PriorityTuple(target,NEUTRAL_CASES));
    	}
		
		//If n -> f
		if(isNeutral(shooter) && isFriend(target) && alive[target] && alive[shooter] && next_round_sum[target]>=THRESHOLD)
	    {
    		priorityList.add(new PriorityTuple(shooter,NEUTRAL_CASES));
	    }
		
		//e -> n
		if(isNeutral(target) && isEnemy(shooter) && alive[shooter] && alive[target] && next_round_sum[shooter]>=THRESHOLD)
    	{
    		priorityList.add(new PriorityTuple(shooter, NEUTRAL_CASES));
    	}
    	
    	//If f -> n, 
    	if(isFriend(shooter) && isNeutral(target) && alive[target] && alive[shooter] && next_round_sum[target]>=THRESHOLD)
    	{
    		priorityList.add(new PriorityTuple(target, NEUTRAL_CASES));
    	}	
    	
	}

	public boolean isFriend(int pid)
    {
    	for(int friend:friends)
    	{
    		if(friend==pid)
    			return true;
    	}
    	return false;
    }
    
    public boolean isEnemy(int pid)
    {
    	for(int enemy:enemies)
    	{
    		if(enemy==pid)
    			return true;
    	}
    	return false;
    }
    
    //yash
    public boolean isNeutral(int pid)
    {
    	return !isEnemy(pid) && !isFriend(pid);
    }
    //yash
   
    //bingyi
    public boolean Enemyus(int pid, List<List<Integer>> m)
    {
    	for (int i=0; i<m.get(pid).size();i++) {
		if (m.get(pid).get(i) == this.id) {
			return true;
			}
		}
    	return false;
    }
    public boolean EnemyourAF(int pid, List<List<Integer>> m, boolean[] alive)
    {
    	for (int i=0; i<m.get(pid).size();i++) {
		if (contains(this.friends, m.get(pid).get(i))&&alive[i]) {
			return true;
		} 
    	}
	return false;
    }
     public boolean contains(final int[] array, final int key) {
        for (final int i : array) {
            if (i == key) {
                return true;
            }
        }
        return false;
    }
    //bingyi 
    public void printPrevRound(int[] a)
    {
    	String s="";
    	for(int i=0;i<a.length;i++)
    	{
    		s+="["+i+","+a[i]+"]";
    	}
    	
    	attentionSeekingPrint(s);
    }
    
    public void printMatrix(List<List<Integer>> m)
    {
    //try{
        int rows = m.size();
        int columns = m.get(0).size();
        String str = "|\t";

    }
    
    private int RET=10;
    private int CON=20;
    private int SEC_PRI=30;
    private int SEC_PRI2=35;
    private int E2F=40;
    private int F2E=40;
    private int NEUTRAL_CASES=50;
    
    private double CONS_PROB=1;
    private double RETAL_PROB=1;
    private double SHOOT_AGAIN_PROB=0.5;
    private double THRESHOLD=2;
    
}
