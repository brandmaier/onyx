package gui.actions;

import gui.frames.MainFrame;
import gui.views.ModelView;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

public class ModelViewCopyAction extends AbstractAction {

	ModelView mv;
	
	public ModelViewCopyAction(ModelView mv)
	{
		this.mv = mv;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		MainFrame.clipboard.copy(this.mv.getGraph());
		// setContents probably necessary for outside copy & paste
		this.mv.getToolkit().getSystemClipboard().setContents(this.mv, this.mv);
	}
	 
}
