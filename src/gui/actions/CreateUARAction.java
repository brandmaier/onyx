package gui.actions;

import gui.Desktop;
import gui.dialogs.LGCMWizard;
import gui.dialogs.UnivariateARWizard;
import gui.views.ModelView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


public class CreateUARAction extends AbstractAction {

	Desktop desktop;
	int x,y;
	
	public CreateUARAction(Desktop desktop)
	{
		this.desktop = desktop;
	
		putValue(NAME, "Create new univariate AR");
		putValue(SHORT_DESCRIPTION, "Create an AR model on the desktop");
	}
	
	public CreateUARAction(Desktop desktop, int x, int y)
	{
		this(desktop);
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		//ModelView mv = new ModelView(desktop);
		UnivariateARWizard w = new UnivariateARWizard(desktop);
//		mv.setLocation(x, y);
//		desktop.add(mv);
	}

}
