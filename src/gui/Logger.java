package gui;

import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;



public class Logger extends JFrame {

	JLabel text;
	private JScrollPane scroll;
	
	Vector<String> logs = new Vector<String>();
	Vector<Long> millis = new Vector<Long>();
	public Logger()
	{
		text = new JLabel();
		text.setVerticalAlignment(SwingConstants.TOP);
		scroll = new JScrollPane(text);
		this.add(scroll);
		this.setSize(400,400);
		//this.pack();
		this.setVisible(true);
	}
	
	public void log(String message)
	{
		logs.add(message);
		millis.add(System.currentTimeMillis());
		
		String displayedText = "<html>";
		int numLogs = logs.size();
		for (int i=Math.max(0,numLogs-20); i < numLogs; i++) {
			long diff;
			if (i==0) diff = 0; else
				diff = millis.get(i)-millis.get(i-1);
				
			displayedText+="Delta="+diff +":"+logs.get(i)+"<br>";
		}
		displayedText += "</html>";
			
		text.setText(displayedText);
	}
}
