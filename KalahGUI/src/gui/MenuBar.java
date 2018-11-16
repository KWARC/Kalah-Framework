package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;

import org.apache.commons.lang3.tuple.Pair;

import engine.Autoplay;
import engine.EvaluationManager;
import engine.HouseColoringManager;
import engine.StateEvaluator;
import engine.forms.ExternalEvaluatorCreator;
import game.Match;
import game.State.GameInfo;
import gui.ActivityListener.Level;

@SuppressWarnings("serial")
public class MenuBar extends JMenuBar{
	
	public MenuBar(JFrame frame) {
		add(new FileMenu());
		add(new EvaluationMenu(frame));
	}
}

@SuppressWarnings("serial")
class EvaluationMenu extends JMenu implements ActionListener{

	private final String PVREQ = "Request principal variation";
	private final String AUTOPLAY = "Enable Autoplay";
	private boolean auto_enabled = false;
//	private final String ADDEVAL = "Add external evaluator";
	
//	private JFrame frame;
	

	
	public EvaluationMenu(JFrame frame) {
		super("Evaluation");
		getAccessibleContext().setAccessibleDescription("Control state evaluation functionality using this menu.");
		//JMenu submenu = new JMenu("Add Evaluator");
		//add(submenu);
		newSimpleItem(PVREQ, this);
//		newSimpleItem(ADDEVAL, this);
//		this.frame = frame;
		
		JRadioButtonMenuItem rb = new JRadioButtonMenuItem(AUTOPLAY);
		rb.addActionListener(this);
		rb.setSelected(auto_enabled);
		this.add(rb);
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		switch(arg0.getActionCommand()) {
		case PVREQ:
			EvaluationManager.requestPV(StateTracker.activeState);
			break;
		case AUTOPLAY:
			auto_enabled = !auto_enabled;
			Autoplay.setEnabled(auto_enabled);
			Coordinator.log("Autoplay enabled: " + auto_enabled, Level.INFO);
//		case ADDEVAL:
//			ExternalEvaluatorCreator.show(frame);
		}
	}
	
	
	private void newSimpleItem(String name, JMenu toAdd) {
		JMenuItem newItem = new JMenuItem(name);
		newItem.addActionListener(this);
		toAdd.add(newItem);
	}
}

@SuppressWarnings("serial")
class FileMenu extends JMenu implements ActionListener{
	
	public FileMenu() {
		super("File");
		getAccessibleContext().setAccessibleDescription("This menu gouverns file related interactions.");
		newSimpleItem("New"); newSimpleItem("Open");
		
	}
	
	private void newSimpleItem(String name) {
		JMenuItem newItem = new JMenuItem(name);
		newItem.addActionListener(this);
		add(newItem);
	}
	
	private String ask(String question, String title) {
		return (String) JOptionPane.showInputDialog(this, question, title, JOptionPane.PLAIN_MESSAGE);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		switch(arg0.getActionCommand()) {
		case "New":
			String p1 = ask("Enter player one's name.", "New Game");
			if(p1 == null) return; //aborted
			String p2 = ask("Enter player two's name.", "New game");
			if(p2 == null) return; //aborted
			try {
				int houses = Integer.parseInt(ask("Enter houses.", "New game"));
				int seeds = Integer.parseInt(ask("Enter seeds.", "New game"));
				Match newm = new Match(new GameInfo(houses, seeds), p1, p2);
				Coordinator.addMatch(newm);
				Coordinator.setActiveState(newm, newm.getStates().get(0));
			}catch (NumberFormatException e) {
				Coordinator.log("Trying to start a new game, but could not parse the entered string. See " + e.getMessage(), Level.ERROR);
			}
			break;
		case "Open":
			JFileChooser fc = new JFileChooser();
			fc.showOpenDialog(this);
			File f = fc.getSelectedFile();
			if(f == null) return; //Aborted 
			Coordinator.setActiveMatch(f);
		}
	}
}
