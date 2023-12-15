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
package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gui.graph.Node;
import gui.views.ModelView;

import javax.swing.JButton;
import javax.swing.JDialog;

public class GroupConnectDialog extends JDialog implements ActionListener {
	
	JButton b1,b2,b3,b4;
	private Node target;
	private Node source;
	private ModelView mv;
	
	public GroupConnectDialog(ModelView mv, Node n1, Node n2)
	{
		b1 = new JButton("One to Group");
		b2 = new JButton("Group to One");
		b3 = new JButton("Group to Group");
	
		this.mv = mv;
		this.source = n1;
		this.target = n2;
		
		this.add(b1);
		this.add(b2);
		this.add(b3);
		this.pack();
		this.setVisible(true);
		
		b1.addActionListener(this);
		b2.addActionListener(this);
		b3.addActionListener(this);
	}

	@Override
	public void actionPerformed(ActionEvent paramActionEvent) {
		// TODO Auto-generated method stub
		
	}

}
