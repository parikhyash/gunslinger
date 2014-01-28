//Deliverable2 9/15
package gunslinger.g7;

public class PlayerStats 
{
	private double probDie;
	private double probThreat;
	private boolean enemy;
	private boolean friend;
	private boolean neutral;
	private boolean alive;
	private int numShotFriends;
	
	public PlayerStats() 
	{
		probDie = 0;
		probThreat = 0;
		alive = true;
		numShotFriends = 0;
	}
	
	//Getter Methods
	public double getProbDie() 			{return probDie;}
	public double getProbThreat()		{return probThreat;}
	public boolean getEnemy()			{return enemy;}
	public boolean getFriend()			{return friend;}
	public boolean getNeutral()			{return neutral;}
	public boolean getAlive()			{return alive;}
	public int getNumShotFriends()		{return numShotFriends;}
	
	//Setter Methods
	public void setProbDie(double d)	{probDie = d;}
	public void setProbThreat(double t)	{probThreat = t;}
	public void setFriend(boolean f)	{friend = f;}
	public void setEnemy(boolean e)		{enemy = e;}
	public void setNeutral(boolean n)	{neutral = n;}
	public void died()					{alive = false;}
	public void shotFriend()			{numShotFriends++;}
}