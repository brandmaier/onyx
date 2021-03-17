package gui.actions;

import gui.Desktop;
import gui.frames.MainFrame;
import gui.views.ModelView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


public class CreateEmptyModelAction extends AbstractAction {

	Desktop desktop;
	int x,y;
	
	public CreateEmptyModelAction(Desktop desktop)
	{
		this.desktop = desktop;
	
		putValue(NAME, "Create Empty Model");
		putValue(SHORT_DESCRIPTION, "Create an empty model on the desktop");
	}
	
	public CreateEmptyModelAction(Desktop desktop, int x, int y)
	{
		this(desktop);
		this.x = x;
		this.y = y;
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {

		MainFrame.undoStack.startCollectSteps();
		ModelView mv = new ModelView(desktop);
		MainFrame.undoStack.endCollectSteps();

		mv.setLocation(x, y);
		desktop.add(mv);
	}

}
