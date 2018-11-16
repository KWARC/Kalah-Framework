package engine;

import java.awt.Color;
import java.util.Map;

import game.State.GameInfo;

public abstract class EvaluationColormap {

	/** Returns the color map for the passed state evaluation.
	 * 
	 * Note: playerOne is equivalent to the southern player.
	 * 
	 * @param gi The game information.
	 * @param se The state evaluation.
	 * @return The color map for the state evaluation.
	 */
	public abstract Map<Integer, Color> getColors(GameInfo gi, StateEvaluation se);
	
	public abstract String getName();
	
	
}
