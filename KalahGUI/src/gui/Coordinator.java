package gui;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.swing.SwingUtilities;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import engine.Autoplay;
import engine.EvaluationManager;
import engine.HouseColoringManager;
import engine.StateEvaluation;
import engine.config.CmapConfiguration;
import engine.config.EvaluationConfiguration;
import game.Match;
import game.Player;
import game.State;
import game.State.GameInfo;
import gui.ActivityListener.Level;

public class Coordinator {
	private volatile static List<ActivityListener> alisteners = new LinkedList<>();	
	private volatile static List<StateListener> mlisteners = new LinkedList<>();
	private volatile static List<EvaluationListener> evlisteners = new LinkedList<>();
	private static Object lock = new Object();
	
	public static void log(String s, Level level) {
		System.out.println(level + " " + s);
		synchronized(lock) {
			for(ActivityListener al : alisteners) {
				al.log(s, level);
			}
		}
	}
	
	public static void sendPV(Match pv) {
		addMatch(pv);
	}
	
	public static void sendFlatEvaluation(State s, double d) {
		for(EvaluationListener ev : evlisteners) {
			ev.receiveFlatEvaluation(s, d);
		}
	}
	
	public static void sendFullEvaluation(State s, StateEvaluation se, Player player) {
		Coordinator.log("Distributing " + se + " for " + s + " and " + player, Level.DEBUG);
		for(EvaluationListener ev : evlisteners) {
			ev.receiveFullEvaluation(s, se, player);
		}
	}
	
	public static void setActiveMatch(File f) {
		Coordinator.log("Loading match from file: " + f.toString(), Level.DEBUG);
		try {
			Match m = Match.fromFile(f);
			Coordinator.setActiveMatch(m);
		} catch (IOException e) {
			Coordinator.log("The file " + f.toString() + " could not be interpreted as a match.", Level.ERROR);
		}
	}
	
	public static void setActiveMatch(Match match) {
		addMatch(match);
		try{
			setActiveState(match, match.getStates().get(0));
		}catch(Exception e){
			
			Coordinator.log("Error while displaying the first game state: " + e.toString(), Level.ERROR);
			Coordinator.log("Match states: " + match.getStates(), Level.ERROR);throw e;
		}
	}
	
	public static void setActiveState(Match m, State newS) {
		Coordinator.log("Updating displayed state to: " + newS.toString() + " in match " + m.getDescriptiveName(), Level.DEBUG);
		synchronized(lock) {
			for(StateListener listener : mlisteners) {
				listener.setActive(m, newS);
			}
		}
	}
	
	public static void addMatch(Match name) {
		Coordinator.log("Adding a new match.", Level.DEBUG);
		synchronized(lock) {
			for(StateListener listener : mlisteners) {
				listener.addMatch(name);
			}
		}
	}
	
	public static void initGui() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					KalahGui.createAndShowGUI();
					new StateTracker(); //establish connection to the state tracker. Otherwise, the static block would not be run.
					new Autoplay();
					EvaluationManager.fromConfig(EvaluationConfiguration.readConfig(new File("eval.yaml")));
					HouseColoringManager.fromConfiguration(CmapConfiguration.fromFile(new File("cmap.yaml")));
					setActiveMatch(new Match(new GameInfo(6, 6), "South", "North"));
				}catch(Exception e) {
					System.err.println("Error while setting up. Shutting down.");
					System.err.println(e.getLocalizedMessage());
				}
			}
		});
	}
	
	public static void main(String[] args) throws JsonParseException, JsonMappingException, IOException {
		initGui();
	}
	
	
	public static void registerEval(EvaluationListener sul) {
		System.out.println("Registered evaluation listener: " + sul);
		synchronized(lock) {
			evlisteners.add(sul);
		}
	}
	
	public static void registerStateListener(StateListener mul) {
		System.out.println("Registered state listener: " + mul);
		synchronized(lock) {
			mlisteners.add(mul);
		}
	}
	
	public static void registerAL(ActivityListener al) {
		System.out.println("Registered activity listener: " + al);
		synchronized(lock) {
			alisteners.add(al);
		}
	}
}
