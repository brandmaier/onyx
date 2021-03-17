package gui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import gui.Desktop;
import gui.dialogs.MeasurementInvarianceWizard;

public class CreateMeasurementInvarianceAction extends AbstractAction {

	Desktop desktop;
	int x,y;
	
	public CreateMeasurementInvarianceAction(Desktop desktop)
	{
		this.desktop = desktop;
	
		putValue(NAME, "Create a Measurement Invariance Model");
		//putValue(SHORT_DESCRIPTION, "Create an LGCM on the desktop");
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		new MeasurementInvarianceWizard(desktop);
	}
	
	public CreateMeasurementInvarianceAction(Desktop desktop, int x, int y)
	{
		this(desktop);
		this.x = x;
		this.y = y;
	}
}
