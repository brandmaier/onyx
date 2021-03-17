package gui.actions;

import gui.Desktop;
import gui.dialogs.LGCMWizard;
import gui.dialogs.UnivariateARWizard;
import gui.dialogs.UnivariateApproxARWizard;
import gui.views.ModelView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


public class CreateApproxUARAction extends AbstractAction {

	Desktop desktop;
	int x,y;
	
	public CreateApproxUARAction(Desktop desktop)
	{
		this.desktop = desktop;
	
		putValue(NAME, "Create new approx. univariate AR");
		putValue(SHORT_DESCRIPTION, "Create an approx. AR model on the desktop");
	}
	
	public CreateApproxUARAction(Desktop desktop, int x, int y)
	{
		this(desktop);
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		//ModelView mv = new ModelView(desktop);
		UnivariateApproxARWizard w = new UnivariateApproxARWizard(desktop);
//		mv.setLocation(x, y);
//		desktop.add(mv);
	}

}
