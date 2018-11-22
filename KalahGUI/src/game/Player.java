package game;

public enum Player {
	ONE(true), TWO(false);
	
	private boolean south;
	
	public boolean isSouth() {
		return south;
	}
	
	public boolean isPlayerOne() {
		return south;
	}
	
	private Player(boolean south) {
		this.south = south;
	}
	
	@Override
	public String toString() {
		return "player: " + (south ? "South" : "North");
	}
	
	public Player other() {
		return this == ONE ? TWO : ONE;
	}
}
