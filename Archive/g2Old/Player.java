package gunslinger.g2Old;

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
    PriorityQueue<PriorityTuple> priorityList;
    
	//bingyi-build the dynamic 2D table to record history
    List<List<Integer>> record = new ArrayList<List<Integer>>();
    //bingyi 

    //May not use these variables later
    //boolean shootable[];
    
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
         
         priorityList=new  PriorityQueue<PriorityTuple>(10, new PriorityTupleComparator());
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
        
        //printPrevRound(prevRound);
        int[] shotAt=new int[prevRound.length];
        
        for(int player=0;player<prevRound.length;player++)
        {
        	if(prevRound[player]!=-1)
        		shotAt[prevRound[player]]++;
        	gaugeSeverity(player,prevRound[player],alive);
        	
        	//bingyi
        	record.get(player).add(prevRound[player]);
		
        }
        
        if(priorityList.size()==0)
        	return -1;
        
        attentionSeekingPrint(priorityList.toString());
        
        int myTarget=getMyTarget(shotAt);
        priorityList.clear();
    	//bingyi
	printMatrix(record);
	//bingyi		
        return myTarget;
    }
    
    public void attentionSeekingPrint(String s)
    {
    	System.out.println("----------------\n"+s+"\n-----------------");
    }
    
    private int getMyTarget(int[] shotAt) {
		
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

	private void gaugeSeverity(int shooter,int target, boolean[] alive) {
		
		System.out.println(shooter+" "+target);
		
		if(target==-1 || shooter==this.id)
			return;
		
    	//If x -> y, p1
    	if(target==this.id && !isFriend(shooter))
    	{
    		priorityList.add(new PriorityTuple(shooter, 1));
    	}
    	
    	//If f -> e, p2
    	else if(isFriend(shooter)&&isEnemy(target)&&alive[target])
    	{
    		priorityList.add(new PriorityTuple(target, 2));
    	}
    	
    	//If e -> f, p2
    	else if(isFriend(target)&&isEnemy(shooter)&& alive[target])
    	{
    		if(alive[shooter])
    			priorityList.add(new PriorityTuple(shooter,2));
    	}
    	
    	//If n -> e, p4
    	else if(!isFriend(shooter)&&!isEnemy(shooter)&&isEnemy(target)&&alive[target])
    	{
    		priorityList.add(new PriorityTuple(target,7));
    	}
    	
    	//If n -> f, p5
    	else if(!isFriend(shooter)&&!isEnemy(shooter)&&isFriend(target)&&alive[target])
    	{
    		if(alive[shooter])
    			priorityList.add(new PriorityTuple(shooter,6));
    	}
    	
    	//If e -> n, 
    	else if(!isFriend(target)&&!isEnemy(target)&&isEnemy(shooter))
    	{
    		if(alive[shooter])
    			priorityList.add(new PriorityTuple(shooter, 4));
    	}
    	
    	//If f -> n, 
    	else if(isFriend(shooter)&&!isEnemy(target)&&!isFriend(target))
    	{
    		if(alive[target])
    			priorityList.add(new PriorityTuple(target, 5));
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
    
}
