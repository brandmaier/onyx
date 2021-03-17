package gui.actions;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;


public class AboutAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6018756448581986089L;

	public AboutAction()
	{
		putValue(NAME, "About Onyx");
		putValue(SHORT_DESCRIPTION, "Display information about the program");

	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		JFrame aboutFrame = new gui.frames.AboutFrame();
		aboutFrame.setVisible(true);
		
	}

}
