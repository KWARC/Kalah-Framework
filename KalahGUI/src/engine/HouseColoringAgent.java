package engine;

import java.awt.Color;
import java.util.Map;
import java.util.Optional;

import game.State;
import game.State.GameInfo;


public class HouseColoringAgent{

	private EvaluationColormap cmap;
	private EvaluationColormap uniformMap;
	private Color defaultCol;
	
	private HouseColoringAgent() {}
	
	public HouseColoringAgent(EvaluationColormap cmap, Color defaultColor) {
		this();
		this.cmap = cmap;
		this.uniformMap = new Uniform(defaultColor);
		this.defaultCol = defaultColor;
	}
	
	
	public Color getDefaultColor() {
		return defaultCol;
	}
	
	public HouseColoringAgent(EvaluationColormap cmap) {
		this(cmap, Color.WHITE);
	}
	
	/**
	 * Try to return the state evaluation for the passed state.
	 * If the evaluation is present, it will be returned within an optional.
	 * Otherwise the evaluation will be requested.
	 * 
	 * @param s the state to evaluate
	 * @return an optional possibly containg the evaluation of s
	 */
	public Optional<StateEvaluation> tryGetEval(State s){
		Optional<StateEvaluation> cacheHit = EvaluationCache.get(s, s.getPlayerToMove());
		if(cacheHit.isPresent()){
			return Optional.of(cacheHit.get());
		}
		EvaluationManager.requestFullEval(s, s.getPlayerToMove());
		return Optional.empty();
	}
	
	public Map<Integer, Color> getFallbackCmap(GameInfo gi){
		return uniformMap.getColors(gi, null);
	}


	public Map<Integer, Color> getCmap(GameInfo gi, StateEvaluation se) {
		return cmap.getColors(gi, se);
	}
}
