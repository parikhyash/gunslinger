package gunslinger.g5;

import java.util.ArrayList;
import java.util.Arrays;

public class GameHistory {

	private ArrayList<int[]> history;
	
	public GameHistory() {
		history = new ArrayList<int[]>();
	}
	
	public void add(int[] roundInfo) {
		history.add(roundInfo);
	}
	
	public int[] everShot(int victimID) {
		ArrayList<Integer> templist = new ArrayList<Integer>();
		for (int i = 0; i < history.size(); i++) {
			int[] info = history.get(i);
			for (int j = 0; j < info.length; j++) {
				if (info[j] == victimID && !templist.contains(j)) {
					templist.add(j);
				}
			}
		}
		if (templist.isEmpty()) {
			return null;
		}
		int[] shooters = new int[templist.size()];
		for (int i = 0; i < shooters.length; i++) {
			shooters[i] = templist.get(i).intValue();
		}
		return shooters;
	}
	
	public int shotsFired(int round) {
		int amount = 0;
		int[] info = history.get(round);
		for (int i = 0; i < info.length; i++) {
			if (info[i] != -1) {
				amount++;
			}
		}
		return amount;
	}
	
	public int consecutiveShots(int shooterID, int victimID) {
		int numShots = 0;
		for (int i = history.size() - 1; i >= 0; i--) {
			if (history.get(i)[shooterID] == victimID) {
				numShots++;
			}
			else {
				break;
			}
		}
		return numShots;
	}
	
	public String toString() {
		String s = "g5 HISTORY:";
		for (int i = 0; i < history.size(); i++) {
			s += Arrays.toString(history.get(i));
		}
		return s;
	}
	
	
}
