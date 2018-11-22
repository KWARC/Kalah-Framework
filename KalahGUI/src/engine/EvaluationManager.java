package engine;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import engine.config.EvaluationConfiguration;
import engine.config.EvaluatorConfiguration;
import game.Player;
import game.State;
import gui.Coordinator;
import gui.ActivityListener.Level;

public class EvaluationManager{

	private static StateEvaluator activeNorth, activeSouth;
	private static Map<String, StateEvaluator> evaluators = new HashMap<>();
	
	static{
		LazyEvaluator lev = new LazyEvaluator();
		evaluators.put("Lazy Evaluator", lev);
		setActive(Player.ONE, lev);
		setActive(Player.TWO, lev);
	}
	
	/**
	 * Request a full evaluation for the currently active (displayed) state.
	 */
	public static void requestFullEval(State s, Player p) {
		Coordinator.log("Requesting evaluation for state: " + s + " and player " + p, Level.DEBUG);
		StateEvaluator eval = p.isSouth() ? activeSouth : activeNorth;
		eval.requestEvaluation(s, p);
	}
	
	
	/**
	 * Request a flat evaluation for the currently active (displayed) state.
	 */
	public static void requestFlatEval(State s) {
		StateEvaluator eval = s.getPlayerToMove().isSouth() ? activeSouth : activeNorth;
		eval.evaluateState(s);
	}
	
	public static void requestPV(State s) {
		StateEvaluator eval =  s.getPlayerToMove().isSouth() ? activeSouth : activeNorth;
		eval.requestPV(s, s.getPlayerToMove());
	}


	public static void setActive(Player p, StateEvaluator eval) {
		if(p.isSouth()) activeSouth = eval;
		else activeNorth = eval;
		EvaluationCache.clear(p);
	}
	
	public static void addEvaluator(String name, StateEvaluator evaluator) {
		evaluators.put(name, evaluator);
	}
	
	public static void fromConfig(EvaluationConfiguration conf) {
		conf.getEvaluators().forEach(e -> evaluators.put(e.getName(), new ExternalStateEvaluator(e.getPort(), e.getIp())));
		assert evaluators.containsKey(conf.getNorth());
		assert evaluators.containsKey(conf.getSouth());
		setActive(Player.TWO, evaluators.get(conf.getNorth()));
		setActive(Player.ONE, evaluators.get(conf.getSouth()));
	}
}
