package gui.views;

import java.awt.Color;

import javax.swing.JLabel;

public class StatusView extends gui.views.View {

	JLabel label;
	
	public StatusView() {
		
		label = new JLabel();
		label.setSize(100,100);
		this.add(label);
		this.setBackground(new Color(200,180,78));
		//this.
	}
	
	public void update(String text) {
		label.setText(text);
	}
}
