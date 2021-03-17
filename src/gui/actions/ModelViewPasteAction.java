package gui.actions;

import gui.frames.MainFrame;
import gui.views.ModelView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

public class ModelViewPasteAction extends AbstractAction {

	ModelView mv;
	
	public ModelViewPasteAction(ModelView mv)
	{
		this.mv = mv;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (mv.mouseAtX < 0 || mv.mouseAtY < 0 || mv.mouseAtX > mv.getWidth() || mv.mouseAtY > mv.getHeight()) {
			// SKIP
		} else {
			MainFrame.clipboard.pasteWithinBounds(this.mv, mv.mouseAtX, mv.mouseAtY, true);
			
		}
	}
	 
}
