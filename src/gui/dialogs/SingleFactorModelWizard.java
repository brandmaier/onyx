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

import engine.ModelRequestInterface;
import gui.Desktop;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SingleFactorModelWizard extends Dialog implements ChangeListener {

	
	Desktop desktop;
	
	JSpinner numObsInput;
	JTextArea nameObsInput;

	private JTextArea nameErrInput;

	private JTextArea nameIceptInput;

	private JTextArea nameSlopeInput;

	private JCheckBox itemIdentification;

	private SpinnerNumberModel centerModel = new SpinnerNumberModel(5,2,100,1);

	//private JSpinner numCenter;
	
	public SingleFactorModelWizard(Desktop desktop)
	{
		super("Single Factor Model Wizard");
		this.desktop = desktop;
	
		Dimension d = new Dimension(150,30);
		
		// # of observations
		 numObsInput = new JSpinner(centerModel);
		 numObsInput.addChangeListener(this);
		this.addElement("Observed variables",numObsInput);
		
		
		// observation name
		nameObsInput = new JTextArea("x");
		nameObsInput.setSize(d);
		this.addElement("Name of observed variable",nameObsInput);
		
		// error term name
		nameErrInput = new JTextArea("e");
		nameErrInput.setSize(d);
		this.addElement("Name of residual variance term ",nameErrInput);
		
		// icept - slope correlation
		nameSlopeInput = new JTextArea("factor");
		nameSlopeInput.setSize(d);
		this.addElement("Name of factor ",nameSlopeInput);
	
		itemIdentification = new JCheckBox("Standardized latent scale");
		this.addElement("Latent scale ", itemIdentification);
		
		this.addSendButton("Create");
		
		this.pack();
		
		this.setVisible(true);
	
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		ModelView mv = new ModelView(desktop);
		desktop.add(mv);
		

		ModelRequestInterface model = mv.getModelRequestInterface();

		int numObs = Integer.parseInt((String) numObsInput.getValue().toString());

		ArrayList<String> xnames = new ArrayList<String>();
		ArrayList<String> enames = new ArrayList<String>();
		for (int i=0; i < numObs; i++) {
		xnames.add( nameObsInput.getText()+(i+1) );
		enames.add (nameErrInput.getText()+(i+1) );
		}
		ModelFactory.createFactorModel(model, numObs, nameSlopeInput.getText(), 
				xnames, enames, -1, 70, 100, !itemIdentification.isSelected(),false,"", "");
		
		mv.setSize( 200+numObs*80, 500);
	
		this.dispose();
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		
	/*	if (arg0.getSource() == numObsInput)
		{
			int max =  Integer.parseInt(numObsInput.getValue().toString());
			centerModel.setMaximum( max );
			if (Integer.parseInt(numObsInput.getValue().toString()) > max) {
				numObsInput.setValue(max);
			}
		}
		*/
	}
	
	
	
	
	
}
