package gui;

import game.Match;
import game.State;

public class StateTracker implements StateListener{

	public volatile static Match activeMatch = null;
	public volatile static State activeState = null;
	
	static {
		Coordinator.registerStateListener(new StateTracker());
	}
	
	public static Match getActiveMatch() {
		return activeMatch;
	}

	public static State getActiveState() {
		return activeState;
	}

	@Override
	public void addMatch(Match m) {
		StateTracker.activeMatch = m;
	}

	@Override
	public void setActive(Match m, State s) {
		StateTracker.activeMatch = m;
		StateTracker.activeState = s;
	}

	
	
}
