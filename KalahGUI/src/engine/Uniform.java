package engine;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import game.State.GameInfo;

public class Uniform extends EvaluationColormap{

	private Color col;
	
	public Uniform() {
		col = Color.WHITE;
	}
	
	public Uniform(Color col) {
		this.col = col;
	}
	
	@Override
	public Map<Integer, Color> getColors(GameInfo s, StateEvaluation se) {
		return uniformMap(s, col);
	}

	private Map<Integer, Color> uniformMap(GameInfo gi, Color col) {
		Map<Integer, Color> cmap = new HashMap<>();
		for(int idx = 0; idx < gi.getHouses(); idx++) {
			cmap.put(idx, col);
		}
		return cmap;
	}
	
	@Override
	public String toString() {
		return Uniform.class.getName() + ": #" + Integer.toHexString(col.getRGB());
	}

	@Override
	public String getName() {
		return this.toString();
	}
}
