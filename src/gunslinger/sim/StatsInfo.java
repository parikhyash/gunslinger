package gunslinger.sim;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

/*
 * 
 * author: Amit Ruparel
 */

class StatsInfo
{
	public final boolean console=false;
	
	int nplayers;
	int games;
	String[] playerNames;
	
	int[][] ranks;
	
	//Data structures for stats
	HashMap<Integer,Integer> streaks;
	int noOfRounds;
	ShotsStats[] playerShotStats;
	ShotsStats allShotStats;
//	int totalShotsFired;
	
	
	public StatsInfo(int nplayers, int games,String[] playerNames)
	{ 
		this.nplayers=nplayers;
		this.games=games;
		this.playerNames=playerNames;
		
		//-
		this.ranks=new int[nplayers][nplayers];
		streaks=new HashMap<Integer,Integer>();
		noOfRounds=0;
		playerShotStats=new ShotsStats[nplayers];
		for(int i=0;i<nplayers;i++)
		{
			playerShotStats[i]=new ShotsStats();
		}
		allShotStats=new ShotsStats("All Players");
//		totalShotsFired=0;
	}
	
	public void setPlayerNames(Player[] players)
	{
		for(Player p: players)
		{
			playerNames[p.index]=p.name();
			playerShotStats[p.index].setName(p.name());
			
		}
	}

	public void process(Gunslinger_Mod game, Player[] playersArr) {
		// TODO Auto-generated method stub
		
		int[] rank=game.rank().clone();
		ArrayList<int[]> gameInfo=(ArrayList<int[]>) (game.gameInfo).clone();
		ArrayList<boolean[]> aliveHistory=(ArrayList<boolean[]>)(game.aliveHistory).clone();
		int[] gameRank=game.rank().clone();
		int[][] relationships=game.relationships.clone();
		
		/*int[][] playerToHost=game.getPlayerToHost();
		int[][] hostToPlayer=game.getHostToPlayer();*/
		
		try
		{
			processRank(gameRank,playersArr);
			processHistory(gameInfo,relationships,aliveHistory,playersArr);
		}
		catch(Exception e)
		{
			System.out.println("Couldn't process this game");
			e.printStackTrace();
			System.out.println(e+"");
		}
		
	}
	
	public void write(String fname) {
		// TODO Auto-generated method stub
		if(console)
		{
			System.out.println(Arrays.toString(playerNames));
			System.out.println(fname);
			System.out.println(getRankStats());
			System.out.println(getStreaksStats());
			System.out.println(getAvgRoundLength());
			
			//Main Stats
			System.out.println(allShotStats.toString());
			for(int i=0;i<nplayers;i++)
				System.out.println(playerShotStats[i].toString());
		}
		else
		{
			try
			{
				PrintWriter writer = new PrintWriter(fname);
				writer.println(getRankStats());
				writer.println(getStreaksStats());
				writer.println(getAvgRoundLength());
				
				writer.println(allShotStats.toString());
				for(int i=0;i<nplayers;i++)
					writer.println(playerShotStats[i].toString());
				
				
				writer.close();
			}
			catch(Exception e)
			{
				
			}
		}
		
	}
	
	public void processRank(int[] gameRank,  Player[] playersArr)
	{
		System.out.println(Arrays.toString(gameRank));
		
		for(int i=0;i<gameRank.length;i++)
		{
			ranks[playersArr[i].index][gameRank[playersArr[i].index]]++;
		}
		
		for(int[] a: ranks)
			System.out.println(Arrays.toString(a));

	}
	
	public void processHistory(ArrayList<int[]> gameInfo, int[][] relationships, ArrayList<boolean[]> aliveHistory,  Player[] playersArr)
	{	
		int prevRound[]=null;
		int currRound[]=null;
		
		int streak[]=new int[nplayers];
		
		
		for(int round=0;round<gameInfo.size()-10;round++)
		{
			//-
			System.out.println("---"+(gameInfo.size()-9));
			
			currRound=gameInfo.get(round);
			
			for(int shooter=0;shooter<nplayers;shooter++)
			{
				System.out.println(String.format("Round %s, shooter %s shot %s / %d",round,playersArr[shooter].name(),currRound[shooter]==-1?-1:playersArr[currRound[shooter]].name(),playerShotStats[playersArr[shooter].index].noShotsFired));
				
				
				//Calculate streaks
				if(prevRound==null) //Round 1
				{
					Arrays.fill(streak,1);
					
					prevRound=new int[nplayers];
					Arrays.fill(prevRound,-1);
					//break;
				}
				//else
				{
					if(prevRound[shooter]==currRound[shooter] && currRound[shooter]!=-1)
					{
						streak[shooter]++;
					}
					else
					{
						if(!streaks.containsKey(streak[shooter]))
							streaks.put(streak[shooter], 0);
					
						streaks.put(streak[shooter], streaks.get(streak[shooter])+1);
						
						
						streak[shooter]=1;
					}
				}
				
				//Fill in the ShotsStats object
				processShot(shooter,currRound[shooter],relationships,prevRound,currRound,aliveHistory,round,playersArr);
			}
			
			prevRound=currRound;
		}	
		//No of rounds
		noOfRounds+=gameInfo.size();
		
	}
	
	public void processShot(int shooter, int target,int[][] relationships,int[] prevRound,int[] currRound, ArrayList<boolean[]> aliveHistory, int round, Player[] playersArr)
	{
		assert currRound[shooter]==target;
		//playerShotStats;
		//allShotStats;
		
		if(target!=-1)
		{
			//Preprocessing
			boolean atFriend=relationships[shooter][target]==1;
			boolean atEnemy=relationships[shooter][target]==-1;
			boolean atNeutral=!atEnemy && !atFriend;
			boolean atWhoDidNotShoot=prevRound[target]==-1;
			
			boolean atWhoShotYou=prevRound[target]==shooter;
			
			boolean atWhoShotFriend=prevRound[target]!=-1?relationships[shooter][prevRound[target]]==1:false;
			
			boolean atWhoWasShot=false;
			boolean atWhoWasShotByFriend=false;
			for(int i=0;i<currRound.length;i++)
			{
				atWhoWasShot=atWhoWasShot || (prevRound[i]==target);
				if(relationships[shooter][i]==1)
					atWhoWasShotByFriend=atWhoWasShotByFriend || prevRound[i]==target;
			}
			
			//Processing
			allShotStats.totalShotsFired++;
			playerShotStats[playersArr[shooter].index].totalShotsFired++;
			if(atEnemy)
			{
				allShotStats.atEnemy++;
				playerShotStats[playersArr[shooter].index].atEnemy++;
				
				if(atWhoShotYou)
				{
					allShotStats.atWhoShotYou++;
					allShotStats.atEnemyWhoShotYou++;
					playerShotStats[playersArr[shooter].index].atEnemyWhoShotYou++;
					

					System.out.println("blahh-"+allShotStats.atWhoShotYou);
					System.out.println("2blahh-"+playerShotStats[playersArr[shooter].index].atEnemyWhoShotYou);
				}
				if(atWhoWasShot)
				{
					allShotStats.atWhoWasShot++;
					allShotStats.atEnemyWhoWasShot++;
					playerShotStats[playersArr[shooter].index].atEnemyWhoWasShot++;
				}
				if(atWhoShotFriend)
				{
					allShotStats.atWhoShotFriend++;
					allShotStats.atEnemyWhoShotFriend++;
					playerShotStats[playersArr[shooter].index].atEnemyWhoShotFriend++;
				}
				if(atWhoWasShotByFriend)
				{
					allShotStats.atWhoWasShotByFriend++;
					allShotStats.atEnemyWhoWasShotByFriend++;
					playerShotStats[playersArr[shooter].index].atEnemyWhoWasShotByFriend++;
				}
				if(atWhoDidNotShoot)
				{
					allShotStats.atWhoDidNotShoot++;
					allShotStats.atEnemyWhoDidNotShoot++;
					playerShotStats[playersArr[shooter].index].atEnemyWhoDidNotShoot++;
				}
			}
			else if(atFriend)
			{
				
				allShotStats.atFriend++;
				playerShotStats[playersArr[shooter].index].atFriend++;
				
				if(atWhoShotYou)
				{
					allShotStats.atWhoShotYou++;
					allShotStats.atFriendWhoShotYou++;
					playerShotStats[playersArr[shooter].index].atFriendWhoShotYou++;
					
					System.out.println("blahh-"+allShotStats.atWhoShotYou);
					System.out.println("2blahh-"+playerShotStats[playersArr[shooter].index].atFriendWhoShotYou);
				}
				if(atWhoWasShot)
				{
					allShotStats.atWhoWasShot++;
					allShotStats.atFriendWhoWasShot++;
					playerShotStats[playersArr[shooter].index].atFriendWhoWasShot++;
				}
				if(atWhoShotFriend)
				{
					allShotStats.atWhoShotFriend++;
					allShotStats.atFriendWhoShotFriend++;
					playerShotStats[playersArr[shooter].index].atFriendWhoShotFriend++;
				}
				if(atWhoWasShotByFriend)
				{
					allShotStats.atWhoWasShotByFriend++;
					allShotStats.atFriendWhoWasShotByFriend++;
					playerShotStats[playersArr[shooter].index].atFriendWhoWasShotByFriend++;
				}
				if(atWhoDidNotShoot)
				{
					allShotStats.atWhoDidNotShoot++;
					allShotStats.atFriendWhoDidNotShoot++;
					playerShotStats[playersArr[shooter].index].atFriendWhoDidNotShoot++;
				}
			}
			else if(atNeutral)
			{
				allShotStats.atNeutral++;
				playerShotStats[playersArr[shooter].index].atNeutral++;
				
				
				if(atWhoShotYou)
				{
					allShotStats.atWhoShotYou++;
					allShotStats.atNeutralWhoShotYou++;
					playerShotStats[playersArr[shooter].index].atNeutralWhoShotYou++;
					
					System.out.println("blahh-"+allShotStats.atWhoShotYou);
					System.out.println("2blahh-"+playerShotStats[playersArr[shooter].index].atNeutralWhoShotYou);
				}
				if(atWhoWasShot)
				{
					allShotStats.atWhoWasShot++;
					allShotStats.atNeutralWhoWasShot++;
					playerShotStats[playersArr[shooter].index].atNeutralWhoWasShot++;
				}
				if(atWhoShotFriend)
				{
					allShotStats.atWhoShotFriend++;
					allShotStats.atNeutralWhoShotFriend++;
					playerShotStats[playersArr[shooter].index].atNeutralWhoShotFriend++;
				}
				if(atWhoWasShotByFriend)
				{
					allShotStats.atWhoWasShotByFriend++;
					allShotStats.atNeutralWhoWasShotByFriend++;
					playerShotStats[playersArr[shooter].index].atNeutralWhoWasShotByFriend++;
				}
				if(atWhoDidNotShoot)
				{
					allShotStats.atWhoDidNotShoot++;
					allShotStats.atNeutralWhoDidNotShoot++;
					playerShotStats[playersArr[shooter].index].atNeutralWhoDidNotShoot++;
				}
			}
			
			
		}
		else
		{
			if(aliveHistory.get(round)[shooter])
			{
				allShotStats.noShotsFired++;
				playerShotStats[playersArr[shooter].index].noShotsFired++;
				
				System.out.println(String.format("%s did not shoot - %d",playerNames[shooter],playerShotStats[playersArr[shooter].index].noShotsFired));
				
				boolean noShotsWhenNotShotBefore=false;
				boolean noShotsWhenEnemyWasShot=false;
				boolean noShotsWhenFriendWasShot=false;
				boolean noShotsWhenFriendWasShooting=false;
				
				for(int i=0;i<currRound.length;i++)//Iterate over all players
				{
					if(prevRound[i]==-1)
						continue;
					
					noShotsWhenNotShotBefore=noShotsWhenNotShotBefore || shooter!=prevRound[i];
					noShotsWhenEnemyWasShot=noShotsWhenEnemyWasShot || relationships[shooter][prevRound[i]]==-1;
					noShotsWhenFriendWasShot=noShotsWhenFriendWasShot || relationships[shooter][prevRound[i]]==1;
					noShotsWhenFriendWasShooting=noShotsWhenFriendWasShooting || (relationships[shooter][i]==1 && prevRound[i]!=-1);
				}
				
				
				if(noShotsWhenNotShotBefore)
				{
					allShotStats.noShotsWhenNotShotBefore++;
					playerShotStats[playersArr[shooter].index].noShotsWhenNotShotBefore++;
				}
				if(noShotsWhenEnemyWasShot)
				{
					allShotStats.noShotsWhenEnemyWasShot++;
					playerShotStats[playersArr[shooter].index].noShotsWhenEnemyWasShot++;
				}
				if(noShotsWhenFriendWasShot)
				{
					allShotStats.noShotsWhenFriendWasShot++;
					playerShotStats[playersArr[shooter].index].noShotsWhenFriendWasShot++;
				}
				if(noShotsWhenFriendWasShooting)
				{
					allShotStats.noShotsWhenFriendWasShooting++;
					playerShotStats[playersArr[shooter].index].noShotsWhenFriendWasShooting++;
				}
				
			
			}
			else
			{
				System.out.println(String.format("Not considering since %s was dead",playerNames[shooter]));
			}
			
		}
	}
	
	
	public String getRankStats()
	{
		String retString="";
		
		for(int i=0;i<ranks.length;i++)
		{
			retString+=("P "+(i+1)+":");
			
			int total=0;
			for(int j=0;j<ranks.length;j++)
			{
				double perc= 100.0*ranks[i][j]/games;
				retString+=((j+1)+" - "+perc+"%, ");
			}
			retString+="\n";
		}
		
		return retString;
	}
	
	public String getStreaksStats()
	{
		String retString="Streaks distribution:\n";
		
		//Streaks
		for(Entry<Integer,Integer> e:streaks.entrySet())
		{
			retString+=e.getKey()+" "+e.getValue()+", ";
		}
		
		return retString.substring(0,retString.length()-2);
	}
	
	
	public String getAvgRoundLength()
	{
		return "Average no of rounds = "+(noOfRounds-10.0)/games;
	}
	
}


class ShotsStats
{
	public String name;
	
	public int totalShotsFired;
		public int atWhoShotYou;
		public int atWhoWasShot;
		public int atWhoShotFriend;
		public int atWhoWasShotByFriend;
		public int atWhoDidNotShoot;
		

		public int atEnemy;
			public int atEnemyWhoShotYou;
			public int atEnemyWhoWasShot;
			public int atEnemyWhoShotFriend;
			public int atEnemyWhoWasShotByFriend;
			public int atEnemyWhoDidNotShoot;
		
		public int atNeutral;
			public int atNeutralWhoShotYou;
			public int atNeutralWhoWasShot;
			public int atNeutralWhoShotFriend;
			public int atNeutralWhoWasShotByFriend;
			public int atNeutralWhoDidNotShoot;
			
		public int atFriend;
			public int atFriendWhoShotYou;
			public int atFriendWhoWasShot;
			public int atFriendWhoShotFriend;
			public int atFriendWhoWasShotByFriend;
			public int atFriendWhoDidNotShoot;
			
		public int noShotsFired;
			public int noShotsWhenNotShotBefore;
			public int noShotsWhenEnemyWasShot;
			public int noShotsWhenFriendWasShot;
			public int noShotsWhenFriendWasShooting;
	
	public ShotsStats()
	{
		//this.name=name;
		totalShotsFired=atWhoShotYou=atWhoWasShot=atWhoShotFriend=atWhoWasShotByFriend=atWhoDidNotShoot=atEnemy= atEnemyWhoShotYou= atEnemyWhoWasShot= atEnemyWhoShotFriend= atEnemyWhoWasShotByFriend= atEnemyWhoDidNotShoot=atNeutral= atNeutralWhoShotYou= atNeutralWhoWasShot= atNeutralWhoShotFriend= atNeutralWhoWasShotByFriend= atNeutralWhoDidNotShoot=atFriend= atFriendWhoShotYou= atFriendWhoWasShot= atFriendWhoShotFriend= atFriendWhoWasShotByFriend= atFriendWhoDidNotShoot= noShotsWhenNotShotBefore= noShotsWhenEnemyWasShot= noShotsWhenFriendWasShot= noShotsWhenFriendWasShooting=0;
	}
	
	public ShotsStats(String name)
	{
		this.name=name;
		totalShotsFired=atWhoShotYou=atWhoWasShot=atWhoShotFriend=atWhoWasShotByFriend=atWhoDidNotShoot=atEnemy= atEnemyWhoShotYou= atEnemyWhoWasShot= atEnemyWhoShotFriend= atEnemyWhoWasShotByFriend= atEnemyWhoDidNotShoot=atNeutral= atNeutralWhoShotYou= atNeutralWhoWasShot= atNeutralWhoShotFriend= atNeutralWhoWasShotByFriend= atNeutralWhoDidNotShoot=atFriend= atFriendWhoShotYou= atFriendWhoWasShot= atFriendWhoShotFriend= atFriendWhoWasShotByFriend= atFriendWhoDidNotShoot= noShotsWhenNotShotBefore= noShotsWhenEnemyWasShot= noShotsWhenFriendWasShot= noShotsWhenFriendWasShooting=0;
	}
	
	public void setName(String n)
	{
		this.name=n;
	}
	
	public String toString()
	{
		
		String retString="";
		
		retString+=String.format("Stats for %s: %n", this.name);
		
		//General
		retString+=String.format("totalShotsFired= %d %n",totalShotsFired);
		if(totalShotsFired!=0)
		{
			retString+=String.format("atWhoShotYou= %d / %.2f %n", this.atWhoShotYou,100.0*atWhoShotYou/totalShotsFired);
			retString+=String.format("atWhoWasShot= %d / %.2f %n", this.atWhoWasShot,100.0*atWhoWasShot/totalShotsFired);
			retString+=String.format("atWhoShotFriend= %d / %.2f %n", this.atWhoShotFriend,100.0*atWhoShotFriend/totalShotsFired);
			retString+=String.format("atWhoWasShotByFriend= %d / %.2f %n", this.atWhoWasShotByFriend,100.0*atWhoWasShotByFriend/totalShotsFired);
			retString+=String.format("atWhoDidNotShoot= %d / %.2f %n", this.atWhoDidNotShoot,100.0*atWhoDidNotShoot/totalShotsFired);
			retString+="\n\n";
		}
		
		//Enemy

		retString+=String.format("atEnemy= %d %n",atEnemy);
		if(atEnemy!=0)
		{
			retString+=String.format("atEnemyWhoShotYou= %d / %.2f %n", this.atEnemyWhoShotYou,100.0*atEnemyWhoShotYou/atEnemy);
			retString+=String.format("atEnemyWhoWasShot= %d / %.2f %n", this.atEnemyWhoWasShot,100.0*atEnemyWhoWasShot/atEnemy);
			retString+=String.format("atEnemyWhoShotFriend= %d / %.2f %n", this.atEnemyWhoShotFriend,100.0*atEnemyWhoShotFriend/atEnemy);
			retString+=String.format("atEnemyWhoWasShotByFriend= %d / %.2f %n", this.atEnemyWhoWasShotByFriend,100.0*atEnemyWhoWasShotByFriend/atEnemy);
			retString+=String.format("atEnemyWhoDidNotShoot= %d / %.2f %n", this.atEnemyWhoDidNotShoot,100.0*atEnemyWhoDidNotShoot/atEnemy);
		}
		
		//Neutral

		retString+=String.format("atNeutral= %d %n",atNeutral);
		if(atNeutral!=0)
		{
			retString+=String.format("atNeutralWhoShotYou= %d / %.2f %n", this.atNeutralWhoShotYou,100.0*atNeutralWhoShotYou/atNeutral);
			retString+=String.format("atNeutralWhoWasShot= %d / %.2f %n", this.atNeutralWhoWasShot,100.0*atNeutralWhoWasShot/atNeutral);
			retString+=String.format("atNeutralWhoShotFriend= %d / %.2f %n", this.atNeutralWhoShotFriend,100.0*atNeutralWhoShotFriend/atNeutral);
			retString+=String.format("atNeutralWhoWasShotByFriend= %d / %.2f %n", this.atNeutralWhoWasShotByFriend,100.0*atNeutralWhoWasShotByFriend/atNeutral);
			retString+=String.format("atNeutralWhoDidNotShoot= %d / %.2f %n", this.atNeutralWhoDidNotShoot,100.0*atNeutralWhoDidNotShoot/atNeutral);
		}
		
		//Friend

		retString+=String.format("atFriend= %d %n",atFriend);
		if(atFriend!=0)
		{
			retString+=String.format("atFriendWhoShotYou= %d / %.2f %n", this.atFriendWhoShotYou,100.0*atFriendWhoShotYou/atFriend);
			retString+=String.format("atFriendWhoWasShot= %d / %.2f %n", this.atFriendWhoWasShot,100.0*atFriendWhoWasShot/atFriend);
			retString+=String.format("atFriendWhoShotFriend= %d / %.2f %n", this.atFriendWhoShotFriend,100.0*atFriendWhoShotFriend/atFriend);
			retString+=String.format("atFriendWhoWasShotByFriend= %d / %.2f %n", this.atFriendWhoWasShotByFriend,100.0*atFriendWhoWasShotByFriend/atFriend);
			retString+=String.format("atFriendWhoDidNotShoot= %d / %.2f %n", this.atFriendWhoDidNotShoot,100.0*atFriendWhoDidNotShoot/atFriend);
		}
		//Noshots
		retString+=String.format("noShotsFired= %d %n",noShotsFired);
		retString+=String.format("noShotsWhenNotShotBefore= %d %n",noShotsWhenNotShotBefore);
		retString+=String.format("noShotsWhenEnemyWasShot= %d %n",noShotsWhenEnemyWasShot);
		retString+=String.format("noShotsWhenFriendWasShot= %d %n",noShotsWhenFriendWasShot);
		retString+=String.format("noShotsWhenFriendWasShooting= %d %n",noShotsWhenFriendWasShooting);
		
		
		
		return retString;
	}
}