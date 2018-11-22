package engine;

import game.Player;
import game.State;

public class LazyEvaluator extends StateEvaluator{

	@Override
	public void close() {
		return;
	}

	@Override
	public String getName() {
		return LazyEvaluator.class.getName();
	}

	@Override
	public void requestEvaluation(State s, Player player) {
		return;
	}

	@Override
	public void requestPV(State s, Player player) {
		return;
	}

	@Override
	public void evaluateState(State s) {
		return;
	}

}
