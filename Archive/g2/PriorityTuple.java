package gunslinger.g2;

import java.util.Comparator;

public class PriorityTuple {
	
	int playerId;
	int priority;
	
	public PriorityTuple(int playerId,int priority)
	{
		this.playerId=playerId;
		this.priority=priority;
	}
	
	public int getPlayerId() { return playerId; }
	public int getPriority() { return priority; }
	
	public String toString()
	{
		return "("+playerId+","+priority+")";
	}
	
}

class PriorityTupleComparator implements Comparator<PriorityTuple> {
	//If return value is positive it means that the first object has higher priority
	@Override
	public int compare(PriorityTuple p1, PriorityTuple p2) {
		
		return (p1.getPriority()-p2.getPriority());
	}
}