package gui.actions;

import gui.Desktop;
import gui.dialogs.DualChangeScoreWizard;
import gui.dialogs.LGCMWizard;
import gui.views.ModelView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


public class CreateDCSMAction extends AbstractAction {

	Desktop desktop;
	int x,y;
	
	public CreateDCSMAction(Desktop desktop)
	{
		this.desktop = desktop;
	
		putValue(NAME, "Create new DCSM");
		putValue(SHORT_DESCRIPTION, "Create a DCSM on the desktop");
	}
	
	public CreateDCSMAction(Desktop desktop, int x, int y)
	{
		this(desktop);
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		//ModelView mv = new ModelView(desktop);
		DualChangeScoreWizard dcw = new DualChangeScoreWizard(desktop);
//		mv.setLocation(x, y);
//		desktop.add(mv);
	}

}
