package engine;

import java.util.Optional;
import java.util.Timer;

import game.Match;
import game.Player;
import game.State;
import game.StateLogic;
import gui.Coordinator;
import gui.EvaluationListener;
import gui.KalahGui;
import gui.StateListener;
import gui.StateTracker;

public class Autoplay implements StateListener, EvaluationListener{
	
	private boolean enabled = false;
	private Timer timer = new Timer();
	public static Autoplay instance;
	public static final int HIGHTLIGHT_TIME = 3000;
	
	public Autoplay() {
		instance = this;
		Coordinator.registerStateListener(this);
		Coordinator.registerEval(this);
	}
	
	public static void setEnabled(boolean enabled) {
		instance.enabled = enabled;
		if(enabled) instance.start();
	}
	
	public void start() {
		State s = StateTracker.getActiveState();
		setActive(StateTracker.getActiveMatch(), s);
	}
	
	private void updateState(State s, StateEvaluation se) {
		
		if(s.isTerminal()) return;
		if(StateLogic.getMoves(s).length == 0) return;
		if(!enabled) return;
		//get the highest evaluated move
		double best_ev = -Double.MAX_VALUE;
		int best_idx = -1;
		for(int move : se.getMoves()) {
			double eval = se.getEvaluation().get(move);
			if(eval > best_ev) {
				best_ev = eval;
				best_idx = move;
			}
		}
		State suc = StateLogic.getSuc(best_idx, s);
		//give a visual signal for the selected move
		KalahGui.canvas.highlightHouse(best_idx, s.getPlayerToMove(), HIGHTLIGHT_TIME);
		//wait for the animation to finish
		timer.cancel();
		timer = new Timer();
		timer.schedule(new java.util.TimerTask() {
			@Override
			public void run() {
				Coordinator.setActiveState(StateTracker.getActiveMatch(), suc);	
			}
		}, HIGHTLIGHT_TIME);
	}
	

	@Override
	public void addMatch(Match m) {}

	@Override
	public void setActive(Match m, State s) {
		Optional<StateEvaluation> eval = EvaluationCache.get(s, s.getPlayerToMove());
		if(!eval.isPresent()) {
			return;
		}
		updateState(s, eval.get());
	}

	@Override
	public void receiveFullEvaluation(State s, StateEvaluation se, Player player) {
		if(!s.equals(StateTracker.activeState)) return;
		updateState(s, se);
	}

	@Override
	public void receiveFlatEvaluation(State s, double eval) {}
}
