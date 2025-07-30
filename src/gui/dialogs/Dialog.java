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
package gui.dialogs;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

public abstract class Dialog extends JDialog implements ActionListener
{

	int rowIndex = 0;

	private JButton button;
	
	protected JPanel mainPanel;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1676308500879210531L;

	public Dialog(String title) {
		this(title, 10);
	}
	
	public Dialog(String title, int margin)
	{
		super();
		
		this.setTitle(title);
		
		 // Create a panel and add some components
        mainPanel = new JPanel();
        //mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS)); // Or use another layout
        mainPanel.setBorder(new EmptyBorder(margin, margin, margin, margin)); // top, left, bottom, right
		
        
        this.getContentPane().add(mainPanel);
        
		mainPanel.setLayout(new GridBagLayout());
	}
	
	public void addElement(String label, JComponent component)
	{
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = rowIndex;
		c.gridheight = 1;
		c.gridwidth = 1;
		c.ipadx = 2;
		c.ipady = 2;
		c.fill = GridBagConstraints.BOTH;
		
		JLabel panel = new JLabel(label);
		mainPanel.add(panel, c);
		
		//c = new GridBagConstraints();
		c.gridx = 1;
		c.gridy = rowIndex;
		
		mainPanel.add(component, c);
		
		rowIndex++;
	}
	
	public void addSendButton(String label)
	{
		//rowIndex++;
		button = new JButton(label);
		button.addActionListener(this);
		this.addElement("", button);
		
		//this.pack();
		//this.setSize(new Dimension(300,200));
		
	//	this.setLocation(null);
		setLocationRelativeTo(null);
	}
	
}
