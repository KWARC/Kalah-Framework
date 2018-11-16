package engine;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import game.State.GameInfo;

public class Divergent extends EvaluationColormap {

	/**
	 * The color that houses with a move high value for north tend to get.
	 */
	private Color colorGood = Color.GREEN;
	/**
	 * The color that houses with a unclear move value tend to get.
	 */
	private Color colorNeither = Color.WHITE;
	/**
	 * The color that houses with a move high value for south tend to get.
	 */
	private Color colorBad = Color.RED;
	/**
	 * The lower value bound. This value and all values lower than it will be mapped
	 * to colorNorth.
	 */
	private double lower;
	/**
	 * The upper value bound. This value and all values high than it will be mapped
	 * to colorSouth.
	 */
	private double upper;

	public Divergent(double lower, double upper, Color good, Color badd, Color neither) {
		this(lower, upper);
		colorGood = good;
		colorBad = badd;
		colorNeither = neither;
	}

	public Divergent(double lower, double upper) {
		this.lower = lower;
		this.upper = upper;
	}

	@Override
	public Map<Integer, Color> getColors(GameInfo gi, StateEvaluation se) {
		Map<Integer, Color> cmap = new HashMap<>();
		for(Integer move : se.getEvaluation().keySet()) {
			double evaluation = se.getEvaluation().get(move);
			//evaluation = player.isSouth() ? evaluation : -evaluation;
			cmap.put(move, getColor(evaluation));
		}
		//Fill up the remaining house indices with the neutral color.
		for(int i = 0; i < gi.getHouses(); i++) {
			if(!cmap.containsKey(i)) cmap.put(i, colorNeither);
		}
		return cmap;
	}

	@Override
	public String toString() {
		return Divergent.class.getName() + " Good: #" + Integer.toHexString(colorGood.getRGB()) + " IDK: #"
				+ Integer.toHexString(colorNeither.getRGB()) + " Bad: #" + Integer.toHexString(colorBad.getRGB());
	}

	@Override
	public String getName() {
		return this.toString();
	}

	private Color getColor(double evaluation) {
		evaluation = Math.min(upper, evaluation);
		evaluation = Math.max(lower, evaluation);
		if (evaluation == 0)
			return colorNeither;
		// determine color extremes for linear interpolation
		int rHigh = evaluation > 0 ? colorGood.getRed() : colorBad.getRed();
		int bHigh = evaluation > 0 ? colorGood.getBlue() : colorBad.getBlue();
		int gHigh = evaluation > 0 ? colorGood.getGreen() : colorBad.getGreen();
		int rLow = colorNeither.getRed();
		int bLow = colorNeither.getBlue();
		int gLow = colorNeither.getGreen();
		// determine interpolation position
		double weightHigh = evaluation > 0 ? evaluation / upper : evaluation / lower;
		// actually interpolate.
		double r = rHigh * weightHigh + rLow * (1 - weightHigh);
		double g = gHigh * weightHigh + gLow * (1 - weightHigh);
		double b = bHigh * weightHigh + bLow * (1 - weightHigh);
		return new Color((int) r, (int) g, (int) b);
	}
	
	public Color getColorGood() {
		return colorGood;
	}

	public Color getColorNeither() {
		return colorNeither;
	}

	public Color getColorBad() {
		return colorBad;
	}
}
