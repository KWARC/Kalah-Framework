package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DecimalFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import engine.HouseColoringAgent;
import engine.HouseColoringManager;
import engine.StateEvaluation;
import game.Match;
import game.Player;
import game.State;
import game.StateLogic;
import gui.ActivityListener.Level;

public class KalahCanvas extends JPanel implements StateListener, EvaluationListener{

	private static final long serialVersionUID = 1L;
	
	private final int W, H;
	private int houseRad;
	private int bankHeight;
	private int gap;
	private Font font;
	protected State state;
	private int houses = 0;
	private Bank p1b, p2b;
	private List<House> p1h, p2h;
	private WinnerMsg winnerMsg;
	private volatile boolean drawComponents;
	private PlayerNames names;
	private volatile Match match; //the currently active match
	
	public KalahCanvas(int width, int height) {
		setBorder(BorderFactory.createLineBorder(Color.black));
		W = width; H = height;
		Coordinator.registerStateListener(this);
		Coordinator.registerEval(this);
		names = new PlayerNames(width, height);
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				if(state == null || !drawComponents) return;
				if(state.isTerminal()) return;
				int x = e.getX(), y = e.getY();
				boolean p1tm = state.isP1ToMove();
				for(House h : p1h) h.setHighlighting(h.isInside(x, y) & p1tm);
				for(House h : p2h) h.setHighlighting(h.isInside(x, y) & !p1tm);
				redraw();
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				if(state == null) return;
				if(state.isTerminal()) return;
				int x = e.getX(), y = e.getY();
				House clicked = null;
				if(state.isP1ToMove()) {
					for(House h : p1h) {
						if(h.isInside(x, y)) clicked = h;
					}
				}
				else for(House hh : p2h) if(hh.isInside(x, y)) clicked = hh;
				if(clicked != null) {
					int move = state.isP1ToMove() ? clicked.getIndex() : state.getGameInfo().getHouses() - 1 - clicked.getIndex();
					if(clicked.seeds != 0) Coordinator.setActiveState(match, StateLogic.getSuc(move, state));
					redraw();
				}
			}
		});
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(W, H);
	}
	
	@Override
	public Dimension getMinimumSize() {
		return getPreferredSize();
	}

	
	public void setHouseRad(int houseRad) {
		this.houseRad = houseRad;
		this.gap = houseRad/2;
		newBoard();
		redraw();
	}

	public void redraw() {
		if(!drawComponents) {
			repaint();
			return;
		}
		repaint(p1b.getBB());
		repaint(p2b.getBB());
		for(House h : p1h) repaint(h.getBB());
		for(House h : p2h) repaint(h.getBB());
		repaint(winnerMsg.getBB());
		repaint(names.northBB());
		repaint(names.southBB());
	}

	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		if(!drawComponents) return;
		g.setFont(font);
		p1b.draw(g); p2b.draw(g);
		for(House h : p1h) {
			h.draw(g);
		}
		for(House h : p2h) {
			h.draw(g);
		}
		winnerMsg.draw(g, state);
		names.draw(g);
	}
	
	public void highlightHouse(int idx, Player p, int time) {
		List<House> houses;
		if(p.isPlayerOne()) {
			houses = p1h;
		} else {
			houses = p2h;
			idx = p2h.size() - 1 - idx;
		}
		houses.get(idx).blink(3, time);
	}
	
	private void newBoard() {
		while(getGraphics() == null);//waiting for graphics
		winnerMsg = new WinnerMsg(W, H, getGraphics());
		Point off;
		do {
			this.houseRad -= 1;
			this.gap = houseRad/2;
			int width = gap + (houses + 2) * (2 * houseRad + gap);
			int height = 2 * gap + bankHeight + 2 * houseRad;
			off = new Point((W - width) / 2, (H - height) / 2);
		}while(off.x < 0);
		int ytop = gap + houseRad + off.y;
		int x = gap + off.x;
		p2b = new Bank(new Point(x + houseRad, ytop), bankHeight, houseRad, state.getP2Score());
		p1b = new Bank(new Point((houses + 1) * (2 * houseRad + gap) + x + houseRad, ytop), bankHeight, houseRad, state.getP1Score());
		p2h = initHouses(ytop, x + 2 * houseRad + gap, state.getP2HousesRev()); 			
		p1h = initHouses(ytop + bankHeight, x + 2 * houseRad + gap, state.getP1Houses());
	}
	
	private List<House> initHouses(int starty, int startx, int[] seeds) {
		int x = startx;
		List<House> houses = new LinkedList<>();
		for(int i = 0; i < this.houses; i++) {
			houses.add(new House(this, new Point(x + houseRad, starty), houseRad, seeds[i], i));
			x += 2 * houseRad + gap;
		}
		return houses;
	}
	
	/**
	 * Draw a String centered in the middle of a Rectangle.
	 *
	 * @param g The Graphics instance.
	 * @param text The String to draw.
	 * @param rect The Rectangle to center the text in.
	 */
	protected static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
	    FontMetrics metrics = g.getFontMetrics(font);
	    // Determine the X coordinate for the text
	    int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
	    // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
	    int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
	    Font old = g.getFont();
	    // Set the font
	    g.setFont(font);
	    // Draw the String
	    g.drawString(text, x, y);
	    g.setFont(old);
	}

	@Override
	public void receiveFullEvaluation(State s, StateEvaluation se, Player player) {
		if(!s.equals(this.state)) {return;}
		Coordinator.log("Received evaluation, now coloring houses.", Level.DEBUG);
		HouseColoringAgent agent = HouseColoringManager.get(player);
		Map<Integer, Color> cmap = agent.getCmap(s.getGameInfo(), se);
		List<House> ph = player.isSouth() ? p1h : p2h;
		for(int i = 0; i < ph.size(); i++) {
			int idx = i;
			if(!player.isSouth()) idx = ph.size() - i - 1; //reverse for north. Houses are indexed in reverse ordering to facilitate rendering.
			ph.get(idx).setFillColor(cmap.get(i));
			Double eval = se.getEvaluation().get(i);
			if(eval != null) ph.get(idx).setValue(Optional.of(eval));
		}
		redraw();
	}
	
	@Override
	public void setActive(Match m, State s) {
		this.state = s;
		this.match = m;
		if(state == null) {
			drawComponents = false;
			return;
		}
		//reset house parameters
		this.houseRad = 50;
		this.bankHeight = 4 * houseRad;
		this. gap = houseRad / 2;
		if(s.getGameInfo().getHouses() != houses) {
			this.houses = s.getGameInfo().getHouses();
			if(houses > 15) { //Ugly but works in most cases
				font = new Font("", 1, 15);
			}else {
				font = new Font("", 1, 30);
			}
			newBoard();
		}
		p1b.setSeeds(s.getP1Score());
		p2b.setSeeds(s.getP2Score());
		int[] p2htmp = s.getP2HousesRev(), p1htmp = s.getP1Houses();
		//retrive house color map.
		HouseColoringAgent agent = HouseColoringManager.get(s.getPlayerToMove());
		Optional<StateEvaluation> eval = agent.tryGetEval(s);
		Map<Integer, Color> cmap;
		if(eval.isPresent()) {
			cmap = agent.getCmap(state.getGameInfo(), eval.get());
		}else {
			cmap = agent.getFallbackCmap(state.getGameInfo());
		}
		Map<Integer, Color> otherCmap = HouseColoringManager.get(s.getPlayerToMove().other()).getFallbackCmap(state.getGameInfo());
		for(int i = 0; i < p1h.size(); i++) {
			House p1house = p1h.get(i), p2house = p2h.get(i);
			p1house.setSeeds(p1htmp[i]);
			p2house.setSeeds(p2htmp[i]);
			//Set house fill color and overwrite last move's coloring
			if(s.isP1ToMove()) {
				p1house.setFillColor(cmap.get(i));
				p2house.setFillColor(otherCmap.get(i));
				// if we have an evalation present, set the evaluation values.
				if(eval.isPresent()) {
					Optional<Double> valOpt1;
					Map<Integer, Double> evalmap = eval.get().getEvaluation();
					if(evalmap.containsKey(i)) valOpt1 = Optional.of(evalmap.get(i));
					else valOpt1 = Optional.empty();
					p1house.setValue(valOpt1);
				} else {
					p1house.setValue(Optional.empty());
				}
				p2house.setValue(Optional.empty());
			} else {
				//reverse indexing for north, as each player's 0 index house is at the opposing bank from the evaluation's point of view.
				int p2idx = p2h.size() - i - 1;
				p2house.setFillColor(cmap.get(p2idx));
				p1house.setFillColor(otherCmap.get(i));
				//do it again here.. this should be a method.
				if(eval.isPresent()) {
					Optional<Double> valOpt2;
					Map<Integer, Double> evalmap = eval.get().getEvaluation();
					if(evalmap.containsKey(p2idx)) valOpt2 = Optional.of(evalmap.get(p2idx));
					else valOpt2 = Optional.empty();
					p2house.setValue(valOpt2);
				} else {
					p2house.setValue(Optional.empty());
				}
				p1house.setValue(Optional.empty());
			}
			//set thiccness
			p1house.setThick(s.isP1ToMove() && s.getResult() == State.Result.TBD);
			p2house.setThick(!s.isP1ToMove() && s.getResult() == State.Result.TBD);
		}
		if(state != null) drawComponents = true;
		redraw();
	}

	@Override
	public void addMatch(Match m) {}

	@Override
	public void receiveFlatEvaluation(State s, double eval) {}
}

class PlayerNames implements StateListener{
	
	private String north = "North", south = "South";
	private final Font font = new Font("", 1, 16);
	private Rectangle boxNorth, boxSouth;
	
	public PlayerNames(int width, int height) {
		Coordinator.registerStateListener(this);
		boxNorth = new Rectangle(0, 0, width, (int)(height/5f));
		boxSouth = new Rectangle(0, (int)(4*height/5f), width, (int)(height/5f));
	}
	
	public void draw(Graphics g) {
		g.setColor(Color.BLACK);
		g.setFont(font);
		g.setClip(boxNorth);
		KalahCanvas.drawCenteredString(g, north, boxNorth, font);
		g.setClip(boxSouth);
		KalahCanvas.drawCenteredString(g, south, boxSouth, font);
	}
	
	public Rectangle northBB() {
		return boxNorth;
	}
	
	public Rectangle southBB() {
		return boxSouth;
	}

	@Override
	public void addMatch(Match m) {}

	@Override
	public void setActive(Match m, State s) {
		north = m.getP2Name();
		south = m.getP1Name();
	}
}

class WinnerMsg{
	
	private final String P1WIN = "South Wins!";
	private final String P2WIN = "North Wins!";
	private final String NOWIN = "Draw!";
	private final Font font = new Font("", 1, 30);
	private final Rectangle bb;
	
	/**
	 * Draws a message centered in the canvas indicating the winner of the game in terminal board states.
	 * 
	 * @param width The width of the canvas
	 * @param height The height of the canvas
	 * @param g The graphics context
	 */
	public WinnerMsg(int width, int height, Graphics g) {
		Point mid = new Point(width/2, height/2);
		int wmax = Integer.MIN_VALUE;
		FontMetrics fm = g.getFontMetrics(font);
		for(String msg : new String[] {P1WIN, P2WIN, NOWIN}) {
			wmax = (int) Math.round(Math.max(wmax, fm.stringWidth(msg)));
		}
		bb = new Rectangle(mid.x - wmax/2, mid.y - fm.getAscent(), wmax, fm.getHeight());
	}
	
	public void draw(Graphics g, State s) {
		String msg = null;
		switch(s.getResult()) {
		case DRAW:
			msg = NOWIN;
			break;
		case P1WIN:
			msg = P1WIN;
			break;
		case P2WIN:
			msg = P2WIN;
			break;
		case TBD:
			return;
		}
		g.setClip(bb);
		g.setColor(Color.BLACK);
		g.setFont(font);
		KalahCanvas.drawCenteredString(g, msg, bb, font);
	}
	
	public Rectangle getBB() {
		return bb;
	}
}

class Bank{
	private Point top;
	private int height;
	private int rad;
	private int seeds;
	private boolean highlight;
	private Rectangle boundingBox, inside;
	
	public Bank(Point top, int height, int rad) {
		this.top = top;
		this.height = height;
		this.rad = rad;
		this.seeds = 0;
		inside = new Rectangle(top.x - rad - 1, top.y - 1, 2 * rad + 2, height + 2);
		boundingBox = new Rectangle(top.x - rad - 1, top.y - rad - 1, 2 * rad + 2, 2 * rad + height + 2);
	}
	
	public Bank(Point top, int height, int rad, int seeds) {
		this.top = top;
		this.height = height;
		this.rad = rad;
		this.seeds = seeds;
		inside = new Rectangle(top.x - rad - 1, top.y - 1, 2 * rad + 2, height + 2);
		boundingBox = new Rectangle(top.x - rad - 1, top.y - rad - 1, 2 * rad + 2, 2 * rad + height + 2);
	}
	
	public void setSeeds(int seeds) {
		this.seeds = seeds;
	}

	public void draw(Graphics g) {
		int left = top.x - rad;
		int right = top.x + rad;
		int width = rad * 2; //also height
		int topup = top.y - rad;
		Color drawCol = highlight ? Color.GRAY : Color.BLACK;
		g.setClip(left - 1, topup - 1, width + 2, width + 2);
		g.setColor(Color.WHITE);
		g.fillOval(left, topup, width, width);
		g.setColor(drawCol);
		g.drawOval(left, topup, width, width);
		
		g.setClip(left - 1, topup + height - 1, width + 2, width + 2);
		g.setColor(Color.WHITE);
		g.fillOval(left, topup + height, width, width);
		g.setColor(drawCol);
		g.drawOval(left, topup + height, width, width);
		g.setClip(left - 1, top.y - 1, width + 2, rad + height + 2);
		g.setColor(Color.WHITE);
		g.fillRect(left, top.y, width, height);
		g.setColor(drawCol);
		g.drawLine(left, top.y, left, top.y + height);
		g.drawLine(right, top.y, right, top.y + height);
		KalahCanvas.drawCenteredString(g, Integer.toString(seeds), inside, g.getFont());
	}

	public Rectangle getBB() {
		return boundingBox;
	}
	
	public boolean isInside(int x, int y) {
		if(Math.sqrt(Math.pow(x - top.x, 2) + Math.pow(y - top.y, 2)) < rad) return true;
		if(Math.sqrt(Math.pow(x - top.x, 2) + Math.pow(y - top.y + height, 2)) < rad) return true;
		return inside.contains(x, y);
	}
	
	public void setHighlighting(boolean on) {
		highlight = on;
	}
}


class House{
	
	private KalahCanvas canvas;
	private Point pos;
	private int rad;
	protected int seeds;
	protected boolean highlight;
	private Rectangle boundingBox, inside;
	private int idx;
	private boolean thick = false;
	private Color fillColor = Color.WHITE;
	private volatile Color highlight_color = Color.GRAY;
	private Optional<Double> value = Optional.empty();
	private BlockingQueue<Animation> anim_queue = new ArrayBlockingQueue<>(1);
	
	private boolean blinking = false;
	private Color blink_color;

	public House(KalahCanvas canvas, Point pos, int rad, int seeds, int idx) {
		this.canvas = canvas;
		this.pos = pos;
		this.rad = rad;
		this.seeds = seeds;
		this.idx = idx;
		boundingBox = new Rectangle(pos.x - rad - 3, pos.y - rad - 3, 2 * rad + 6, 2 * rad + 6);
		int smallRad = (int) (Math.cos(Math.toRadians(45.0)) * rad);
		inside = new Rectangle(pos.x - smallRad, pos.y - smallRad, 2 * smallRad, 2 * smallRad);
		new Thread(new AnimationThread(anim_queue)).start();
	}
	
	public void setValue(Optional<Double> value) {
		synchronized(this.value) {
			this.value = value;
		}
	}

	public void setSeeds(int seeds) {
		this.seeds = seeds;
	}
	
	public void setFillColor(Color fillColor) {
		this.fillColor = fillColor;
	}
	
	public void blink(double hz, int timespan) {
		anim_queue.clear();
		try {
			anim_queue.put(new BlinkAnimation(this, Color.decode("0xCCCCCC"), Color.decode("0x000000"), hz, timespan));
		} catch (InterruptedException e) {
			e.printStackTrace();
			Coordinator.log("Failed to queue blink animation for house: " + idx, Level.ERROR);
		}
	}

	public void draw(Graphics g) {
		g.setClip(boundingBox);
		g.setColor(fillColor);
		g.fillOval(pos.x - rad, pos.y - rad, rad * 2, rad * 2);
		
		Color main_col;
		if(highlight && seeds != 0) main_col = highlight_color;
		else main_col = Color.BLACK;
		g.setColor(main_col);
		
		Stroke str = ((Graphics2D)g).getStroke();
		if(thick && seeds != 0)((Graphics2D)g).setStroke(new BasicStroke(5));
		if(blinking) g.setColor(blink_color);
		
		g.drawOval(pos.x - rad, pos.y - rad, rad * 2, rad * 2);
		
		((Graphics2D)g).setStroke(str);
		if(blinking) g.setColor(main_col);
		
		KalahCanvas.drawCenteredString(g, Integer.toString(seeds), inside, g.getFont());
		synchronized(value) {
			if(value.isPresent()) {
				//get height of the font
				Font old = g.getFont();
				Font nw = new Font("", 0, old.getSize() / 2);
				Rectangle below = new Rectangle(boundingBox.x, boundingBox.y + boundingBox.height, boundingBox.width, g.getFontMetrics().getHeight());
				g.setClip(below);
				KalahCanvas.drawCenteredString(g, new DecimalFormat("#.###").format(value.get()), below, nw);
			}
		}
	}
	
	public Rectangle getBB() {
		return boundingBox;
	}
	
	public boolean isInside(int x, int y) {
		return Math.sqrt(Math.pow(x - pos.x, 2) + Math.pow(y - pos.y, 2)) < rad;
	}
	
	public void setHighlighting(boolean on) {
		highlight = on;
	}
	
	public void setThick(boolean on) {
		thick = on;
	}
	
	public int getIndex() {
		return idx;
	}
	
	private abstract class Animation{
		public abstract void execute();
	}
	
	private class BlinkAnimation extends Animation{

		private Color col, alt_col;
		private double hz;
		private int timespan;
		private House house;
		
		public BlinkAnimation(House house, Color col, Color alt_col, double hz, int timespan) {
			this.house = house;
			this.col = col;
			this.alt_col = alt_col;
			this.hz = hz;
			this.timespan = timespan;
		}
		
		@Override
		public void execute() {
			boolean use_alt = false;
			Color to_use;
			house.blinking = true;
			while(this.timespan > 0) {
				int interval = Math.min(this.timespan, (int) (1000 / hz));
				this.timespan -= interval;
				if(use_alt) to_use = alt_col;
				else to_use = col;
				house.blink_color = to_use;
				house.canvas.redraw();
				use_alt = !use_alt;
				try {
					Thread.sleep(interval);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			house.blinking = false;
			house.canvas.redraw();
		}
		
	}
	private class AnimationThread implements Runnable{
		
		private BlockingQueue<Animation> queue;
		
		public AnimationThread(BlockingQueue<Animation> queue) {
			this.queue = queue;
		}
		
		@Override
		public void run() {
			Animation a;
			while(true) {
				try {
					a = queue.take();
				} catch (InterruptedException e) {
					Coordinator.log("Could not retrive issued animation.", Level.WARN);
					e.printStackTrace();
					continue;
				}
				a.execute();
			}
		}
		
	}
	
}