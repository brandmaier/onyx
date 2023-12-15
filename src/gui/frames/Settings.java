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
package gui.frames;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import engine.Preferences;
import gui.Desktop;

public class Settings 
 extends JFrame implements ActionListener
 {

	JComboBox<String> box ;
	
	String[] items = new String[] {"None","Globe","Crumpled Paper","Board","Desk",WelcomeFrame.OMEGA+"nyx"};

	Desktop desktop;
	JButton ok;
	
		public Settings(Desktop desktop)
		{
		setTitle("Settings");
			
		this.desktop = desktop;
			
			box= new JComboBox<String>(
					items);
			
			box.addActionListener(this);
			
			ok = new JButton("Done");
			ok.addActionListener(this);
			
			JLabel lab1 = new JLabel("Background Image");
			
			this.setLayout(new GridBagLayout());
			
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridx=0;
			gbc.gridy=0;
			
			this.add(lab1, gbc);
			
			gbc.gridx=1;
			gbc.gridy=0;
			
			this.add(box, gbc);

			gbc.gridy=1;
			JPanel sep = new JPanel();
			sep.setSize(1, 50);
			this.add(sep, gbc);
			
			gbc.gridx=1;
			gbc.gridy=2;
			this.add(ok, gbc);
			
			//setLocation(null);
			setSize(300,200);
			
			setVisible(true);
			
			
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			
			
		update();
		
		if (e.getSource() == ok)
			this.dispose();
			
		}
		public void update() {
			int idx = box.getSelectedIndex();
			
			if (idx == 0) {
				Preferences.set("BackgroundImage", "");
			} else {
				Preferences.set("BackgroundImage", "#"+idx);
			}
			
			desktop.updateBackgroundImage();
		}
	
	
}
