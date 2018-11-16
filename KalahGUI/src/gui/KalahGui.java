package gui;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

public class KalahGui {

	public static KalahCanvas canvas;
	
	public static void createAndShowGUI() {
		JFrame f = new JFrame("Kalah GUI");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		canvas = new KalahCanvas(1000, 500);
		
		VariationExplorer varExp = new VariationExplorer(200, 500);
		
		JSplitPane p = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, canvas, varExp);
		f.add(p);
		
		f.setJMenuBar(new MenuBar(f));
		
		f.setTransferHandler(new MatchDropHandler());
		
		f.pack();
		f.setVisible(true);
		
		
	}
}

