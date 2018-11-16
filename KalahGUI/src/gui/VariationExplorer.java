package gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import game.Match;
import game.State;

public class VariationExplorer extends JPanel{
	
	private static final long serialVersionUID = 1L;
	private static int margin = 3;

	public VariationExplorer(int w, int h) {
		setup(w, h);
	}
	
	private void setup(int w, int h) {
		setBorder(BorderFactory.createLineBorder(Color.black));
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setPreferredSize(new Dimension(w, h));
		Dimension mySize = getPreferredSize();
		
		JLabel l = new JLabel("Variation Explorer", SwingConstants.CENTER);
		l.setPreferredSize(new Dimension(mySize.width - 2 * margin, mySize.height / 15));
		add(l);
		
		VariationTree t = new VariationTree();
		JScrollPane treeView = new JScrollPane(t);
		treeView.setPreferredSize(new Dimension(mySize.width - 2 * margin, mySize.height));
		add(treeView);
		
		VariationNavigator nav = new VariationNavigator(t);
		add(nav);
	}
	
	public void loadStates() {
		
	}

}
class VariationTree extends JTree implements StateListener{

	private static final long serialVersionUID = 1L;
	private DefaultMutableTreeNode root;
	private DefaultTreeModel model;
	private TreeSelectionModel selec;
	private Map<Match, Map<State, DefaultMutableTreeNode>> stateNodeMap = new HashMap<>();
	private Map<Match, DefaultMutableTreeNode> matchNodeMap = new HashMap<>();
	private volatile boolean externalRequest, internalRequest = false;
	
	public VariationTree() {
		setBorder(BorderFactory.createLineBorder(Color.black));
		setEditable(false);
//		setRootVisible(false);
		Coordinator.registerStateListener(this);
		
		model = (DefaultTreeModel) getModel();
		root = (DefaultMutableTreeNode) model.getRoot();
		root.setUserObject("Matches");//sets the displayed name of the root
		selec = getSelectionModel();
		root.removeAllChildren();
		model.reload();
		addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				if(root.getChildCount() == 0) return;
				if(!e.isAddedPath()) return;
				if(internalRequest) {
					internalRequest = false;
					return;
				}
				externalRequest = true;
				DefaultMutableTreeNode selected = (DefaultMutableTreeNode) e.getPath().getLastPathComponent();
				if(selected == model.getRoot()) return;
				Object selectedObj = selected.getUserObject();
				if(selectedObj instanceof State ) {
					State ns = (State) selectedObj;
					for(Match m : stateNodeMap.keySet()) {
						for(State s : stateNodeMap.get(m).keySet()) {
							if(selected.equals(stateNodeMap.get(m).get(s))) {
								Coordinator.setActiveState(m, ns);
							}
						}
					}
				}
			}
		});
		Icon personIcon = null;
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
        renderer.setLeafIcon(personIcon);
        renderer.setClosedIcon(personIcon);
        renderer.setOpenIcon(personIcon);
        setCellRenderer(renderer);
	}

	/**
	 * Returns the next state in the tree if there is one. Otherwise returns null.
	 */
	protected State getNext() {
		DefaultMutableTreeNode node = getSelected();
		if(node == root) return null;
		TreeNode parent = node.getParent();
		int idx = parent.getIndex(node);
		State suc = null;
		if(node.getChildCount() != 0) {
			//check for an explored variation. (the children added second)
			Object state = getStateForNode(node.getChildAt(0));
			if(state instanceof State) suc = (State) state;
		}
		if(parent.getChildCount() - 1 != idx) {
			//check for explored successors. (the children added first)
			//The overwriting of suc is intentional. This way, successors are preferred.
			Object state = getStateForNode(parent.getChildAt(idx + 1));
			if(state instanceof State) suc = (State) state;
		}
		return suc;
		
	}
	
	/**
	 * Returns the last state if there was one. If there is none, returns null.
	 */
	protected State getPrevious() { 
		DefaultMutableTreeNode node = getSelected();
		if(node == root) return null;
		TreeNode parent = node.getParent();
		int idx = parent.getIndex(node);
		if(idx != 0) {
			Object state = getStateForNode(parent.getChildAt(idx - 1));
			return state instanceof State ? (State) state : null;
		}else if(parent.equals(root)){
			return null;
		}else {
			Object state = getStateForNode(parent);
			return state instanceof State ? (State) state : null;
		}
	}
	
	private void setSelected(DefaultMutableTreeNode newCurrent){
		internalRequest = !externalRequest;
		externalRequest = false;
		selec.setSelectionPath(new TreePath(newCurrent.getPath()));
	}
	
	
	protected DefaultMutableTreeNode getSelected() {
		try {
			return (DefaultMutableTreeNode) selec.getSelectionPath().getLastPathComponent();
		} catch (Exception e) {
			return root;
		}
	}
	
	private Object getStateForNode(TreeNode node) {
		return ((DefaultMutableTreeNode)node).getUserObject();
	}
	
	private void addChild(Match m, State toAdd, DefaultMutableTreeNode parent){
		DefaultMutableTreeNode newn = new DefaultMutableTreeNode(toAdd);
		stateNodeMap.get(m).put(toAdd, newn);
		parent.add(newn);
		model.reload();
		setSelected(newn);
	}

	@Override
	public void addMatch(Match m) {
		matchNodeMap.put(m, new DefaultMutableTreeNode(m.getName()));
		root.add(matchNodeMap.get(m));
		stateNodeMap.put(m, new HashMap<>());
		for(State s : m.getStates()) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(s);
			stateNodeMap.get(m).put(s, node);
			matchNodeMap.get(m).add(node);
		}
		model.reload();
		setSelected(matchNodeMap.get(m));
	}

	@Override
	public void setActive(Match m, State s) {
		DefaultMutableTreeNode cur = getSelected();
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) cur.getParent();
		int curIdx = parent.getIndex(cur);
		if(stateNodeMap.get(m).containsKey(s)) { // if we already know the state, declare it as active.
			setSelected(stateNodeMap.get(m).get(s));
		}else if(curIdx == parent.getChildCount() - 1){ //if the selected node is the last child of the parent
			addChild(m, s, parent);// append the state to the parent
		} else { //if both previous conditions are not met, then we create a new variation
			for(int i = 0; i < cur.getChildCount(); i++) {//delete the old variation
				stateNodeMap.get(m).remove(getStateForNode(cur.getChildAt(i)));
			}
			cur.removeAllChildren();
			// add the the new state
			addChild(m, s, cur);
		}
	}
}

class VariationNavigator extends JToolBar implements ActionListener{

	private static final long serialVersionUID = 1L;
	private static final String NEXT = "next";
	private static final String PREV = "prev";
	private VariationTree tree;
	
	public VariationNavigator(VariationTree tree) {
		setLayout(new FlowLayout());
		add(makeImageButton("bwd", PREV, "Previous Move"));
		add(makeImageButton("fwd", NEXT, "Next Move"));
		setFloatable(false);
		setRollover(true);
		this.tree = tree;
	}
	
	private JButton makeImageButton(String imgName, String actionCommand, String toolTipText) {
		String imgLoc = "img/" + imgName + ".png";
	    JButton button = new JButton();
	    button.setToolTipText(toolTipText);
	    button.addActionListener(this);
	    button.setActionCommand(actionCommand);
	    button.setIcon(new ImageIcon(imgLoc));
	    return button;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String cmd = e.getActionCommand();
		if(NEXT.equals(cmd)) {
			State s = tree.getNext();
			if(s != null) {
				Coordinator.setActiveState(StateTracker.getActiveMatch(), s);
			}
			return;
		}
		if(PREV.equals(cmd)) {
			State s = tree.getPrevious();
			if(s != null) {
				Coordinator.setActiveState(StateTracker.getActiveMatch(), s);
			}
			return;
		}
	}
}