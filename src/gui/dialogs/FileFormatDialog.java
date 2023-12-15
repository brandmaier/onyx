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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class FileFormatDialog extends Dialog implements ActionListener {

	private JRadioButton radioSeparatorTab;
	private JRadioButton radioSeparatorWhitespace;
	private JRadioButton radioSeparatorComma;

	public FileFormatDialog()
	{
		super("File Format Dialog");
		
		radioSeparatorTab = new JRadioButton("Tabulator");
		radioSeparatorWhitespace = new JRadioButton("Whitespace");
		radioSeparatorComma = new JRadioButton("Comma");
//		this.add("")
		JPanel separatorPanel = new JPanel();
		separatorPanel.add(radioSeparatorComma);
		separatorPanel.add(radioSeparatorTab);
		separatorPanel.add(radioSeparatorWhitespace);
		this.add("Separator", separatorPanel);
		
		JCheckBox header = new JCheckBox("First line contains row names");
		this.add("Header", header);
		
		this.addSendButton("Load");
		
		this.pack();
		this.show();
	}
	
	public void tryToBeClever(String somedata)
	{
		
	}
	
	public static void main(String[] args)
	{
		new FileFormatDialog();
	}



	@Override
	public void actionPerformed(ActionEvent paramActionEvent) {
		
		
		this.dispose();
		
	}
	
}
