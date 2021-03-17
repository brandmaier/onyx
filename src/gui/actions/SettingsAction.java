package gui.actions;


import gui.Desktop;
import gui.frames.Settings;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JFrame;




	public class SettingsAction extends AbstractAction {
	
		private Desktop desktop;


		public SettingsAction(Desktop desktop)
		{
			this.desktop = desktop;
			putValue(NAME, "Settings");
			putValue(SHORT_DESCRIPTION, "Settings of the application");
		}
		
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			JFrame f = new Settings(desktop);
			//f.setVisible();
			
		}
	
	}

 

