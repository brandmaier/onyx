package gui.actions;

import gui.Desktop;
import gui.dialogs.LGCMWizard;
import gui.dialogs.SingleFactorModelWizard;
import gui.views.ModelView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


public class CreateSingleFactorModelAction extends AbstractAction {

	Desktop desktop;
	int x,y;
	
	public CreateSingleFactorModelAction(Desktop desktop)
	{
		this.desktop = desktop;
	
		putValue(NAME, "Create new single factor model");
		//putValue(SHORT_DESCRIPTION, "Create an LGCM on the desktop");
	}
	
	public CreateSingleFactorModelAction(Desktop desktop, int x, int y)
	{
		this(desktop);
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		//ModelView mv = new ModelView(desktop);
		//LGCMWizard w = new LGCMWizard(desktop);
		SingleFactorModelWizard w = new SingleFactorModelWizard(desktop);
//		mv.setLocation(x, y);
//		desktop.add(mv);
	}

}
