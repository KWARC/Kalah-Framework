package game;

import java.util.Arrays;
import java.util.stream.IntStream;

import gui.Coordinator;
import gui.ActivityListener.Level;

public class StateLogic {

	public static int[] getMoves(State s) {
		if (s.getResult() != State.Result.TBD) {
			return new int[0];
		}
		int[] houses;
		if (s.isP1ToMove()) {
			houses = s.getP1Houses();
		} else {
			houses = s.getP2Houses();
		}
		int cnt = 0;
		for (int i = 0; i < s.getP1Houses().length; i++) {
			if (houses[i] != 0) {
				cnt++;
			}
		}
		int[] res = new int[cnt];
		int j = 0;
		for (int i = 0; i < s.getP1Houses().length; i++) {
			if (houses[i] != 0) {
				res[j] = i;
				j++;
			}
		}
		return res;
	}

	public static State getSuc(int move, State s) {
		if(!IntStream.of(getMoves(s)).anyMatch(x -> x == move)) {
			Coordinator.log("Requested the successor of " + s + " for move " + move + ". But the move is invalid.", Level.WARN);
		}
		int[] myhouses, othouses;
		int mybank, otbank;
		if (s.isP1ToMove()) {
			myhouses = s.getP1Houses().clone();
			othouses = s.getP2Houses().clone();
			mybank = s.getP1Score();
			otbank = s.getP2Score();
		} else {
			othouses = s.getP1Houses().clone();
			myhouses = s.getP2Houses().clone();
			otbank = s.getP1Score();
			mybank = s.getP2Score();
		}
		int toDistr = myhouses[move], idx = move + 1;
		myhouses[move] = 0;
		boolean addBank = true, again = false;
		while (true) {
			while (idx != s.getGameInfo().getHouses() && toDistr != 0) {
				toDistr -= 1;
				myhouses[idx] += 1;
				idx += 1;
				if (toDistr == 0 && myhouses[idx - 1] == 1 && addBank
						&& othouses[s.getGameInfo().getHouses() - (idx - 1) - 1] != 0) {
					int opp = s.getGameInfo().getHouses() - (idx - 1) - 1;
					mybank += othouses[opp] + 1;
					othouses[opp] = 0;
					myhouses[idx - 1] = 0;
					break;
				}
			}
			if (toDistr == 0) {
				break;
			}
			if (addBank) {
				mybank += 1;
				toDistr -= 1;
				if (toDistr == 0) {
					again = true;
					break;
				}
			}
			int[] tmp = othouses;
			othouses = myhouses;
			myhouses = tmp;
			addBank = !addBank;
			idx = 0;
		}
		if (!addBank) {
			int[] tmp = othouses;
			othouses = myhouses;
			myhouses = tmp;
		}
		int summy = 0, sumot = 0;
		for (int i = 0; i < myhouses.length; i++) {
			summy += myhouses[i];
			sumot += othouses[i];
		}
		if (summy == 0) {
			otbank += sumot;
			Arrays.fill(othouses, 0);
		}
		if (sumot == 0) {
			mybank += summy;
			Arrays.fill(myhouses, 0);
		}
		if (s.isP1ToMove()) {
			return new State(myhouses, othouses, mybank, otbank, again, s.getGameInfo());
		} else {
			return new State(othouses, myhouses, otbank, mybank, !again, s.getGameInfo());
		}
	}
}
