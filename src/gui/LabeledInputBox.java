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


import gui.frames.MainFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.text.Document;

public class LabeledInputBox extends 
AbstractButton
//JMenuItem

implements KeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2956688432230812085L;
	public JTextField textField;
	JLabel label;
	JPanel pnl;
	
	final JMenuItem dummyMenuItem = new JMenuItem(); // a dummy, to retrieve the right Font
	
	/*public Dimension getPreferredSize()
	{
		Dimension dim = super.getPreferredSize();
		dim.height += 10;
		return dim;
	}*/
	
	
	public LabeledInputBox(String labelText)
	{
		label = new JLabel();
		//label.setText("     "+labelText);	//AB: This is a silly fix - I try to fix it with a strut
		label.setText(labelText);
		label.setBackground(Color.white);
		textField = new JTextField();
		textField.setFont( dummyMenuItem.getFont());
		label.setFont( dummyMenuItem.getFont());
		
		/*setHorizontalTextPosition(11);
		setHorizontalAlignment(10);
		updateUI();
		*/
		
		//this.setDefaultLocale(l)
//		JPanel holder = new JPanel();
//		dummyMenuItem.set
		
		this.setLayout( new BoxLayout(this, BoxLayout.LINE_AXIS));
	
		setBorder(null);
	//	this.setAlignmentX(0);
		
		int hMargin = 21; //AB:  21 seems OK for OSX 10.9.2, 31 seems ok for Windows 7
		LookAndFeel lf = UIManager.getLookAndFeel();
		if (lf.getName().equals("Windows")) {	//AB: ugly bugfix for windows UI delegate
			hMargin = 31;
		}
		
		this.add(Box.createHorizontalStrut(hMargin));	
		this.add(label);
		this.add(Box.createHorizontalStrut(4));
		this.add(textField);
		//this.add(Box.createHorizontalStrut(5));
		this.add(Box.createHorizontalGlue());
		
		this.textField.addKeyListener(this);
		
		//createMouseListener();
	}
	
	 private void createMouseListener() {
	        this.addMouseListener(new java.awt.event.MouseAdapter() {
	            @Override
	            public void mousePressed(java.awt.event.MouseEvent evt) {
	                requestFocusInWindow();
	            }

	            @Override
	            public void mouseReleased(java.awt.event.MouseEvent evt) {
	                requestFocusInWindow();
	            }

	            @Override
	            public void mouseClicked(java.awt.event.MouseEvent evt) {
	                requestFocusInWindow();
	            }
	        });
	    }
	
	public void addKeyListener(KeyListener listener)
	{
		this.textField.addKeyListener(listener);
	}

	public Document getDocument() {
		return this.textField.getDocument();
	}

	public void setText(String parameterName) {
		this.textField.setText(parameterName);
		
	}

	@Override
	public void keyTyped(KeyEvent paramKeyEvent) {
		

		intercept(paramKeyEvent);
	}

	private void intercept(KeyEvent paramKeyEvent) {
		//MainFrame.logger.log(paramKeyEvent.toString());
		if (paramKeyEvent.getKeyCode()==KeyEvent.VK_ALT_GRAPH
				|| paramKeyEvent.getKeyCode()==KeyEvent.VK_ALT)
		
			paramKeyEvent.consume();
		
	}

	@Override
	public void keyPressed(KeyEvent paramKeyEvent) {
	//	paramKeyEvent.consume();
		intercept(paramKeyEvent);
	}

	@Override
	public void keyReleased(KeyEvent paramKeyEvent) {
		
		intercept(paramKeyEvent);

		
	}

}
