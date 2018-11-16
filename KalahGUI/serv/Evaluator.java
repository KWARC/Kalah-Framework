import java.util.List;

public interface Evaluator {
	/**
	 * Returns a full state evaluation of the player-to-move for the passed state.
	 * 
	 * @param s The state to evaluate.
	 * @return The state evaluation for this state.
	 */
	public StateEvaluation evaluateFull(State s);
	
	/**
	 * Returns a flat evaluation of the state, which usually is just the evaluation function applied to the passed state.
	 * 
	 * @param s the state to evaluate
	 * @return the evaluation
	 */
	public double evaluateState(State s);
	
	/**
	 * Returns the principal variation for the passed state. 
	 * The principal variation is the preferred sequence of moves.
	 * 
	 * @param s The state to get the PV for
	 * @return the PV for the state.
	 */
	public List<Integer> getPrincipalVariation(State s);
}
