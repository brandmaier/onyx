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

import java.awt.event.ActionEvent;

import gui.Desktop;
import gui.dialogs.Dialog;
import gui.graph.Edge;
import gui.graph.EdgeProxy;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DeveloperControlFrame extends Dialog implements ChangeListener

{

//	private SpinnerNumberModel centerModel;
	//private JSpinner numCenter;

	Desktop desktop;
	private JCheckBox drawLinks;
	
	JSlider edgepad;
	
	public DeveloperControlFrame(Desktop desktop)
	{
		super("Developer");
		
		this.desktop = desktop;
		
		//centerModel = new SpinnerNumberModel(Edge.Curvature,1,500,1);
		//numCenter = new JSpinner(centerModel);
		
		edgepad = new JSlider();
		this.addElement("Edge Padding", edgepad);
		edgepad.setMinimum(0);
		edgepad.setMaximum(20);
		edgepad.addChangeListener(this);
		//numCenter.addChangeListener(this);
		
		drawLinks = new JCheckBox("Draw Links");
		drawLinks.addChangeListener(this);
		
	//	this.addElement("Curvature of covariance paths", numCenter);
		this.addElement("Draw links", drawLinks);
		
		this.pack();
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
			Desktop.DRAW_LINKS_ON_DESKTOP = drawLinks.isSelected();
	//		Edge.Curvature = Integer.parseInt(numCenter.getValue().toString());
			
			EdgeProxy.ARROW_PAD = edgepad.getValue();
			desktop.repaint();
	}
	
}
