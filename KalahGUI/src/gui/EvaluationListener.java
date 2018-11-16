package gui;

import engine.StateEvaluation;
import game.Player;
import game.State;

public interface EvaluationListener {

	/** Retrieve the evaluation `se` for State `s` for the player indicated by `south`.
	 * 
	 *  Note: this function is called when the `StateEvaluationManager` finished the work on a received evaluation request.
	 *  If multiple unfinished requests exist, they will be worked of sequentially the order they were requested.
	 *  This means, that EvaluationListener should always check if the received evaluation is relevant.
	 * 
	 * Note: south is equivalent of player one / starting player.
	 * 
	 * @param s The state of the requested evaluation.
	 * @param se The evaluation of `s`.
	 * @param player Determines the player to which the evaluation is associated.
	 */
	public void receiveFullEvaluation(State s, StateEvaluation se, Player player);
	
	public void receiveFlatEvaluation(State s, double eval);
}
