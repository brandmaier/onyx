package gui.actions;

import gui.Desktop;
import gui.dialogs.LGCMWizard;
import gui.views.ModelView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


public class CreateLGCMAction extends AbstractAction {

	Desktop desktop;
	int x,y;
	
	public CreateLGCMAction(Desktop desktop)
	{
		this.desktop = desktop;
	
		putValue(NAME, "Create new LGCM");
		putValue(SHORT_DESCRIPTION, "Create an LGCM on the desktop");
	}
	
	public CreateLGCMAction(Desktop desktop, int x, int y)
	{
		this(desktop);
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		//ModelView mv = new ModelView(desktop);
		LGCMWizard w = new LGCMWizard(desktop);
//		mv.setLocation(x, y);
//		desktop.add(mv);
	}

}
