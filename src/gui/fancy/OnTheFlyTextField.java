package gui.fancy;

import gui.graph.Node;

import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class OnTheFlyTextField extends JTextField implements DocumentListener {

	public Node context;
	
	public OnTheFlyTextField() {
		
		this.getDocument().addDocumentListener(this);
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
//		System.out.println("CHANGED!");
		if (context != null) {
			context.setCaption( this.getText() );
		}
		
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		// TODO Auto-generated method stub
		
	}
}
