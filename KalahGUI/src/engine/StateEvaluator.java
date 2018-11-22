package engine;

import game.Player;
import game.State;

public abstract class StateEvaluator {

	/**
	 * Called when the evaluator is closed.
	 */
	public abstract void close();

	public abstract String getName();

	/**
	 * Asynchronously requests a full evaluation for the passed position and player.
	 * 
	 * @param s
	 *            The state to evaluate.
	 * @param player
	 *            The player to evaluate.
	 */
	public abstract void requestEvaluation(State s, Player player);
	
	/**
	 * Requests the principal variation for the requested state and player.
	 * @param s
	 *            The state to evaluate.
	 * @param player
	 *            The player to evaluate.
	 */
	public abstract void requestPV(State s, Player player);
	
	/**Evaluates the passed state. (E.g. Flat evaluation, evaluation functions output to the passed state).
	 * 
	 * @param s the state to evaluate.
	 */
	public abstract void evaluateState(State s);
}
