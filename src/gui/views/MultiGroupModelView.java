package gui.views;

import java.awt.event.KeyEvent;

import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import gui.Desktop;
import gui.graph.Graph;

public class MultiGroupModelView extends ModelView
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6304281865400699133L;
	
	
	int numGroups = 3;
	int activeGroup = 0;
	
	int offsets = 10000;

	public MultiGroupModelView(Desktop desktop) {
		super(desktop);
		setGroup(0);
		
	}
	
	public MultiGroupModelView(Desktop desktop, Graph graph, int numGroups) {
		this(desktop);
		
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		
		super.keyPressed(arg0);
		
		System.out.println(arg0.getKeyChar());
		
		if (arg0.getKeyChar() == KeyEvent.VK_SPACE) {
			cycleGroup();
		}

	}
	
	public void setGroup(int group) {
		if (group >= 0 && group < numGroups) {
			this.activeGroup = group;
			this.setName( this.getModelRequestInterface().getName()+" Group "+activeGroup+ "/"+numGroups);
		} else {
			System.err.println("Invalid group");
		}
	}
	
	public void cycleGroup() {
		int newGroup = activeGroup+1;
		if (newGroup >= numGroups) newGroup=0;
		this.setGroup( newGroup );
	}

}
