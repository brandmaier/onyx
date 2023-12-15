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
package gui.frames;

import engine.Preferences;
import gui.MessageObject;
import gui.Utilities;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class TipOfTheDayFrame extends JDialog implements ActionListener, MouseListener
{

	JButton nextTip;
	JButton close;
	JCheckBox dontshow;
    JLabel dontshowlabel;
    
    String[] tips = {
    	"Right-click to open context menus anywhere in Onyx (e.g.,on the desktop, on edges, or nodes). If you don't have a mouse with right-click, hold down CONTROL and left-click." +
    	"Latent variables can be created with a double-click on a model panel. SHIFT + Double-click creates observed variables.",
    	"Paths between variables can be created by right-dragging (press mouse button and hold down while moving the mouse) from one variable to another.",
    	"Datasets and models can be loaded by dragging them from your file browser directly onto the Onyx desktop.",
    	"Manifests variables can be created by dragging a variable from a dataset onto a model panel.",
    	"Labels on paths can be moved with the mouse by dragging them along the edge.",
    	"Model estimation is started as soon as all observed variables are connected to a dataset. To connect variables, drag them from the dataset onto the corresponding observed variable in the model.",
    	"Graphical models in Onyx can be exported to various other formats, including bitmap images (JPEG, PNG), vector formats (EPS, PDF), or script languages (OpenMx, Mplus, lavaan).",
//    	"Creating all possible edges between two sets of variables is easy, if you use node groups. Create a node group from a set of selected variables by holding"
//    	"Once all variables in your model are connected to a dataset, model fitting"
    	"Use CONTROL-Z (or CMD-Z on Mac) to undo your last action.",
    	"Right-dragging from one variable to a second variable, creates a regression path. If you hold down SHIFT while dragging, a covariance path will be created",
    	"The curvature of covariance paths can be flexibly controlled by two control points. Select <manual edge control> in an edge's context menu to show the control points.",
    	"The appearance of paths can be altered in different ways, including line color, stroke width and font size of the label."
    };
	private Image image;
	private JLabel imageLabel;
	private JLabel tipText;
	
	int tipCounter = -1;
	
	public TipOfTheDayFrame()
	{
		 URL  url =  this.getClass().getResource("/icons/mono/lightbulb32.png"); 
		 if (url != null) {
		 	image = Utilities.resizeImage(new ImageIcon(url).getImage(),24,24 );
		 }	
		
		nextTip = new JButton("Next Tip");
		close = new JButton("Close");
		dontshow = new JCheckBox("Show tips at startup");
		
		nextTip.addActionListener(this);
		close.addActionListener(this);
		dontshow.addActionListener(this);
		
		// wait for preferences to be loaded!
	/*	while(true) {
			i
			Thread.sleep(100);
		}*/
		
		dontshow.setSelected(
				Boolean.parseBoolean(
						Preferences.getAsString("ShowTipOfTheDay")));
		//dontshowlabel = new JLabel();
		imageLabel = new JLabel( new ImageIcon(image) );
		
		tipText = new JLabel();
		
		
		nextTip.addMouseListener(this);
		dontshow.addMouseListener(this);
		this.addMouseListener(this);
		tipText.addMouseListener(this);
		close.addMouseListener(this);
		
//		this.setLayout();
		tipText.setBackground(Color.WHITE);
		tipText.setBorder(BorderFactory.createEmptyBorder());
		JPanel buttonPanel = new JPanel();
		

		buttonPanel.add(dontshow);
		buttonPanel.add(nextTip);
		buttonPanel.add(close);
		
		this.add(imageLabel, BorderLayout.WEST);
		this.add(tipText, BorderLayout.CENTER);
		this.add(buttonPanel, BorderLayout.SOUTH);
		
		// restore tip
		try {
		tipCounter = Integer.parseInt(Preferences.getAsString("CurrentTipOfTheDay"))
				;
		} catch (Exception e) {}
		
		nextTip();
		
		this.setModal(true);
		this.setSize(450,270);
		this.setLocationRelativeTo(null); // center x,y
		
	}
	
	private void nextTip() {
		tipCounter++;
		tipCounter %= tips.length;
		
		this.tipText.setText(
				"<html>"+
		"<h2>Did you know...</h2><hr>"+tips[tipCounter]+
		"</html>");
		
//		this.tipText.setAlignmentY(0);
		this.tipText.setVerticalTextPosition(SwingConstants.TOP);
		this.tipText.setVerticalAlignment(SwingConstants.TOP);
		this.tipText.setPreferredSize(new Dimension(400,300));
		this.tipText.setOpaque(true);
//		this.tipText.
		
		Preferences.set("CurrentTipOfTheDay", Integer.toString( tipCounter));
		
	}

	public static void main(String[] args)
	{
		new TipOfTheDayFrame();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource()==nextTip)
		{
			nextTip();
		}
		
		if (arg0.getSource()==close) {
			this.dispose();
		}
		if (arg0.getSource()==dontshow) {
			Preferences.set("ShowTipOfTheDay", Boolean.toString(dontshow.isSelected()));
			
		}
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (Utilities.isRightMouseButton(e)) {
			JOptionPane.showMessageDialog(this, "You caught us. This is really the only place where we do not provide a context menu. We should have known.");
		}
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
