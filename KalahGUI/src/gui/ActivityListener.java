package gui;

public interface ActivityListener{
	
	public enum Level{
		INFO(1), DEBUG(0), WARN(2), ERROR(3);
		
		int importance;
		
		private Level(int importance) {
			this.importance = importance;
		}
	}
	
	public void log(String s, Level level);
}
