package gunslinger.g4;

import java.util.LinkedList;
import java.util.PriorityQueue;

public class EventManager implements RoundListener {
	private PriorityQueue<Event> mEvents; 
	
	public EventManager() {
		mEvents = new PriorityQueue<Event>();
	}
	
	@Override
	public void onNewRound(GameHistory history) {
		LinkedList<Event> toRemove = new LinkedList<Event>();
		for (Event event : mEvents) {
			event.onRoundPassed(history);
			if (event.getDangerScore() == 0)
				toRemove.add(event);
		}

		mEvents.removeAll(toRemove);
		
		for (int shooter = 0; shooter < history.getNPlayers(); shooter++) {
			int shotAt = history.playerShotAt(shooter);
			if (shotAt != -1 && history.isAlive(shooter) && history.isAlive(shotAt)) {
				mEvents.add(new Event(history, shooter));
			}
		}
	}

	public int getBestShot() {
		if (mEvents.size() > 0) {
			return mEvents.peek().getTarget();
		}
		else {
			return -1;
		}
	}
}
