package gui.actions;

import gui.Desktop;
import gui.dialogs.LDEWizard;
import gui.dialogs.LGCMWizard;
import gui.views.ModelView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


public class CreateLDEAction extends AbstractAction {

	Desktop desktop;
	int x,y;
	
	public CreateLDEAction(Desktop desktop)
	{
		this.desktop = desktop;
	
		putValue(NAME, "Create new LDE");
		putValue(SHORT_DESCRIPTION, "Create an LDE on the desktop");
	}
	
	public CreateLDEAction(Desktop desktop, int x, int y)
	{
		this(desktop);
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		//ModelView mv = new ModelView(desktop);
		LDEWizard w = new LDEWizard(desktop);
//		mv.setLocation(x, y);
//		desktop.add(mv);
	}

}
