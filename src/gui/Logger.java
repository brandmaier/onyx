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
