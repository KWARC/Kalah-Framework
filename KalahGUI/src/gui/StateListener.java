package gui;

import game.Match;
import game.State;

public interface StateListener{
	
	public void addMatch(Match m);
	
	/**Sets the passed state in match m as the active (displayed) state.
	 * Note: this can be a "new" state, not originally within m.
	 * 
	 * @param m The match which the state contains
	 * @param s The state to set active
	 */
	public void setActive(Match m, State s);
	
//	/**
//	 * Adds a state to the listener without updating the current state.
//	 * Components needing information about a whole match should enroll.
//	 * 
//	 * @param toAdd: The state to add to the listener.
//	 */
//	public void addState(State toAdd);
//	
//	/**
//	 * Sets the name of the northern player to northName.
//	 * The northern player is the second player.
//	 * 
//	 * @param northName The name of the northern player.
//	 */
//	public void setNorth(String northName);
//	
//	/**
//	 * Sets the name of the southern player to southName.
//	 * The southern player is the starting player.
//	 * 
//	 * @param southName The name of the southern player.
//	 */
//	public void setSouth(String southName);
}