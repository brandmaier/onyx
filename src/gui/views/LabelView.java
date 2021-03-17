package gui.views;

import java.awt.Color;

import javax.swing.JLabel;

public class LabelView extends View {
	
	public String string;
	public JLabel label;
	
	public LabelView() {
		
		label = new JLabel();
		
		this.setMovable(true);
		this.setResizable(false);
		
		this.setBackground(Color.gray);
		
		label.addMouseListener(this);
	}
	
	public void setString(String s)
	{
		this.string = s;
		label.setText(s);
		label.setFont(label.getFont().deriveFont((float) 18.0));
		this.setSize(label.getPreferredSize());
	}
	
	
}
