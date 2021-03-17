package gui.actions;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;




	public class ExitAction extends AbstractAction {
	
		public ExitAction()
		{
	
			putValue(NAME, "Quit");
			putValue(SHORT_DESCRIPTION, "Quit the application");
		}
		
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			//TODO: everything is closed ?
			
			System.exit(0);
			
		}
	
	}

 

