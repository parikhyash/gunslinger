/*package gunslinger.sim;

import java.util.ArrayList;

public class StatsCollector {
	
	public static void main(String args[]) throws Exception
	{
		String[] argsToSend=new String[8];
		int[] nenemies={2};
		int[] nfriends={2};
		int games=1000;
		
		//VERY IMPORTANT. IF YOU CHANGE players.list CHANGE THIS TOO
		int n=9;
		

		argsToSend[0]="./src/players.list";
		argsToSend[3]="false"; //GUI
		argsToSend[4]="true"; //Recompile
		argsToSend[5]="false"; //Verbose
		argsToSend[6]="false"; //Trace
		
		argsToSend[7]="1"; //No of games
		
		
		for(int i=0;i<nenemies.length;i++)
		{
			
			argsToSend[1]=""+nenemies[i]; 
			argsToSend[2]=""+nfriends[i];
			
			StatsInfo sinfo=new StatsInfo(n, games);
			
			for(int game=0;game<games;game++)
			{
				Gunslinger_Mod gunslinger=new Gunslinger_Mod();
				gunslinger.main(argsToSend);
				
				ArrayList<int[]> gameInfo=gunslinger.getGameInfo();
				int[][] relationships=gunslinger.getRelationships();
				int[] rankForThisGame=gunslinger.getRank();
				
				sinfo.process(gameInfo,relationships,rankForThisGame);
				
			}
			
			sinfo.write(nenemies[i]+"e-f"+nfriends[i]);
			
		}
		
	}

}

class StatsInfo
{
	int[][] ranks;
	int games;
	
	public StatsInfo(int n, int games)
	{
		ranks=new int[n][n];
		this.games=games;
	}
	
	
	public void write(String string) {
		// TODO Auto-generated method stub
		
		printRankStats();
		
	}

	public void process(ArrayList<int[]> gameInfo, int[][] relationships,int[] rank) {
		// TODO Auto-generated method stub
		
		processRank(rank);
		
	}
	
	public void processRank(int[] rank)
	{
		for(int i=0;i<rank.length;i++)
		{
			ranks[i][rank[i]]++;
		}
	}
	
	public void printRankStats()
	{
		for(int i=0;i<ranks.length;i++)
		{
			System.out.print("P "+(i+1)+":");
			int total=0;
			for(int j=0;j<ranks.length;j++)
			{
				double perc= ranks[i][j]/games;
				System.out.print((j+1)+"-"+perc+"%, ");
			}
			System.out.println();
		}
	}
	
}
*/