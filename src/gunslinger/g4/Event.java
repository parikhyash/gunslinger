package gunslinger.g4;

import gunslinger.g4.GameHistory.PlayerType;

public class Event implements Comparable<Event> {
	// The threat level that this event poses. Higher numbers denote greater
	// threat.
	private double mDangerLevel;
	private double mDangerMultiplier;
	private int mShooterId;
	private int mShotId;
	private int mTarget;
	private PlayerType mShooterType;
	private PlayerType mShotType;
	
	//Weights
//	private static final int FRIEND_SHOOTS_FRIEND;
//	private static final int FRIEND_SHOOTS_NEUTRAL;
//	private static final int FRIEND_SHOOTS_THREAT = 4;
//	private static final int FRIEND_SHOOTS_ENEMY = 2;
//	private static final int FRIEND_SHOOTS_ME;
	private static final int NEUTRAL_SHOOTS_FRIEND = 17;
	private static final int NEUTRAL_SHOOTS_NEUTRAL = 6;
//	private static final int NEUTRAL_SHOOTS_THREAT = 4;
//	private static final int NEUTRAL_SHOOTS_ENEMY = 2;
	private static final int THREAT_SHOOTS_ME = 16;
	private static final int THREAT_SHOOTS_FRIEND = 19;
	private static final int THREAT_SHOOTS_NEUTRAL = 14;
	private static final int THREAT_SHOOTS_THREAT = 14;
	private static final int THREAT_SHOOTS_ENEMY = 14;
	private static final int ENEMY_SHOOTS_FRIEND = 20;
	private static final int ENEMY_SHOOTS_NEUTRAL = 15;
	private static final int ENEMY_SHOOTS_THREAT = 15;
	private static final int ENEMY_SHOOTS_ENEMY = 15;
	private static final int ENEMY_SHOOTS_ME = 18;
	
	// Exponential backoff rate
	private static double MULTIPLIER = .55;
	
	
	public Event(GameHistory history, int shooter) {
		mShooterId = shooter;
		mShotId = history.playerShotAt(mShooterId);
		mShooterType = history.getPlayerType(mShooterId); 
		mShotType = history.getPlayerType(mShotId);
		
		System.out.println(mShooterType + " shot at " + mShotType);
	
		mTarget = -1;
		resetDangerLevel();
		mDangerMultiplier = 1;
	}
	
	public void onRoundPassed(GameHistory history) {
		if (!history.isAlive(mShotId) || !history.isAlive(mShooterId)) {
			mDangerLevel = 0;
			return;
		}
		boolean dangerLevelChanged = false;
		if (mShotType == PlayerType.NEUTRAL) {
			mShotType = history.getPlayerType(mShotId);
			dangerLevelChanged = true;
		}
		if (mShooterType == PlayerType.NEUTRAL) {
			mShooterType = history.getPlayerType(mShotId);
			dangerLevelChanged = true;
		} 
		if (dangerLevelChanged) {
			resetDangerLevel();
		}
		adjustBackoffMultiplier();
	}
	
	public int compareTo(Event other) {
		return (int) Math.signum(other.getDangerScore() - getDangerScore()); 
	}
	
	public double getDangerScore() {
		return mDangerLevel * mDangerMultiplier;
	}
	
	public int getTarget() {
		return mTarget;
	}
	
	private void adjustBackoffMultiplier() {
		mDangerMultiplier *= MULTIPLIER;
	}
	
	private void resetDangerLevel() {
		mDangerLevel = 0;
		switch (mShooterType) {
		case FRIEND:
			switch (mShotType) {
			case FRIEND:
			case NEUTRAL:
			case THREAT:
			case ENEMY:
			case SELF:
				break;
			}
			break;
		case THREAT:
			switch (mShotType) {
			case FRIEND:
				mDangerLevel = THREAT_SHOOTS_FRIEND;
				mTarget = mShooterId;
				break;
			case NEUTRAL:
				mDangerLevel = THREAT_SHOOTS_NEUTRAL;
				mTarget = mShooterId;
				break;
			case THREAT:
				mDangerLevel = THREAT_SHOOTS_THREAT;
				mTarget = mShooterId;
				break;
			case ENEMY:
				mDangerLevel = THREAT_SHOOTS_ENEMY;
				mTarget = mShooterId;
				break;
			case SELF:
				mDangerLevel = THREAT_SHOOTS_ME;
				mTarget = mShooterId;
				break;
			}
			break;
		case NEUTRAL:
			switch (mShotType) {
			case FRIEND:
				mDangerLevel = NEUTRAL_SHOOTS_FRIEND;
				mTarget = mShooterId;
				break;
			case NEUTRAL:
				mDangerLevel = NEUTRAL_SHOOTS_NEUTRAL;
				mTarget = mShooterId;
				break;
			case THREAT:
			case ENEMY:
				break;
			case SELF:
				mDangerLevel = THREAT_SHOOTS_ME;
				mTarget = mShooterId;
				break;
			}
			break;
		case ENEMY:
			switch (mShotType) {
			case FRIEND:
				mDangerLevel = ENEMY_SHOOTS_FRIEND;
				mTarget = mShooterId;
				break;
			case NEUTRAL:
				mDangerLevel = ENEMY_SHOOTS_NEUTRAL;
				mTarget = mShooterId;
				break;
			case THREAT:
				mDangerLevel = ENEMY_SHOOTS_THREAT;
				mTarget = mShooterId;
				break;
			case ENEMY:
				mDangerLevel = ENEMY_SHOOTS_ENEMY;
				mTarget = mShooterId;
				break;
			case SELF:
				mDangerLevel = ENEMY_SHOOTS_ME;
				mTarget = mShooterId;
				break;
			}
			break;
		default:
			break;
		}
	}
}
