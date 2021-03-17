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
