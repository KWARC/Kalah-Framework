package engine;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import game.Match;
import game.Player;
import game.State;
import gui.Coordinator;
import gui.EvaluationListener;
import gui.ActivityListener.Level;

public class EvaluationCache implements EvaluationListener{

	private static Map<Player, Map<State, StateEvaluation>> cache = new HashMap<>();
	private static Object lock = new Object();
	
	static {
		Coordinator.registerEval(new EvaluationCache());
	}
	
	public EvaluationCache() {
		cache.put(Player.ONE, new HashMap<>());
		cache.put(Player.TWO, new HashMap<>());
	}
	
	public static Optional<StateEvaluation> get(State s, Player p){
		synchronized (lock) {
			if(cache.get(p).containsKey(s)) {
				Coordinator.log("Got a cache hit!", Level.DEBUG);
				return Optional.of(cache.get(p).get(s));
			}
			Coordinator.log("Cache miss.", Level.DEBUG);
			return Optional.empty();
		}
	}
	
	/**Clears the cache for a player. Use it a new evaluator becomes active, so that the new evaluations get requested.
	 */
	public static void clear(Player player) {
		synchronized (lock) {
			cache.put(player, new HashMap<>());
		}
	}
	
	@Override
	public void receiveFullEvaluation(State s, StateEvaluation se, Player player) {
		Coordinator.log("Registering state evaluation for " + s + " and " + player, Level.DEBUG);
		synchronized (lock) {
			cache.get(player).put(s, se);
		}
	}

	@Override
	public void receiveFlatEvaluation(State s, double eval) {}

}
