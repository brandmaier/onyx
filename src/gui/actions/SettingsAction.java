/*
* Copyright 2023 by Timo von Oertzen and Andreas M. Brandmaier
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
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

 

