package gui.actions;

import gui.Desktop;
import gui.views.DataView;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


public class CreateEmptyDataSetAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4795323982037927347L;
	Desktop desktop;
	
	public CreateEmptyDataSetAction(Desktop desktop) {
		super();
		this.desktop = desktop;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		DataView view = new DataView(desktop);
		desktop.add(view);
		
	}

}
