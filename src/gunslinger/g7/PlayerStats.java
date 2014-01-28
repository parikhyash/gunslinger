//Deliverable3 9/17
package gunslinger.g7;

public class PlayerStats 
{
	private double probDie;
	private double probThreat;
	private boolean enemy;
	private boolean friend;
	private boolean neutral;
	private boolean alive;
	private boolean shotAliveFriend;
	private int numShotFriends;
	private int lastPlayerShotBy;
	private int shotPlayers[];
	
	public PlayerStats(int numPlayers)
	{
		probDie = 0;
		probThreat = 0;
		alive = true;
		numShotFriends = 0;
		shotPlayers = new int[numPlayers];
	}
	
	//Getter Methods
	public double getProbDie() 					{return probDie;}
	public double getProbThreat()				{return probThreat;}
	public boolean getEnemy()					{return enemy;}
	public boolean getFriend()					{return friend;}
	public boolean getNeutral()					{return neutral;}
	public boolean getAlive()					{return alive;}
	public int getLastPlayerShotBy()			{return lastPlayerShotBy;}
	public boolean shotUs(int id)
	{
		if (shotPlayers[id]>0)
			return true;
		else
			return false;
	}
	public boolean getShotAliveFriend(int f[], int p, boolean a[])
	{
		shotAliveFriend = false;
		for (int i=0; i<f.length; i++)
		{
			if (p==f[i] && a[f[i]])
				shotAliveFriend=true;
		}
		return shotAliveFriend;
	}
	public int getNumShotFriends(int f[])				
	{
		numShotFriends = 0;
		for(int i=0; i < shotPlayers.length; i++)
		{
			for(int j=0; j < f.length; j++) 
			{
				if(i == f[j])
					numShotFriends += shotPlayers[i];
			}
		}
			
		return numShotFriends;
	}
	
	//Setter Methods
	public void setProbDie(double d)			{probDie = d;}
	public void setProbThreat(double t)			{probThreat = t;}
	public void setFriend(boolean f)			{friend = f;}
	public void setEnemy(boolean e)				{enemy = e;}
	public void setNeutral(boolean n)			{neutral = n;}
	public void died()							{alive = false;}
	public void shotPlayer(int i)				
	{
		if(i != -1)
			shotPlayers[i]++;
	}
	
	public void setLastPlayerShotBy(int i) 		{lastPlayerShotBy = i;}
}