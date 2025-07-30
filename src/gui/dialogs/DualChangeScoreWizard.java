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
import engine.OnyxModel;
import engine.backend.RAMModel;
import gui.Desktop;
import gui.frames.TimeseriesMeanPlot;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DualChangeScoreWizard  extends Dialog implements ChangeListener {

	private Desktop desktop;
	
	JSpinner numObsInput;
	JTextArea nameObsInput;

	private JTextArea nameErrInput;

	private JTextArea nameIceptInput;

	private JTextArea nameSlopeInput;

	private JCheckBox uniqueResiduals, latentCovariance;

	private SpinnerNumberModel centerModel;

	private JSpinner numCenter;
	

	public DualChangeScoreWizard(Desktop desktop)
	{
		super("Dual Change Score Model Wizard");
		this.desktop = desktop;
	
		Dimension d = new Dimension(150,30);
		
		// # of observations
		 numObsInput = new JSpinner(new SpinnerNumberModel(5,2,100,1));
		 numObsInput.addChangeListener(this);
		this.addElement("Observed time points",numObsInput);
		
		centerModel = new SpinnerNumberModel(1,1,5,1);
		numCenter = new JSpinner(centerModel);
		this.addElement("Observation centered at ", numCenter);
		
		// observation name
		nameObsInput = new JTextArea("x");
		nameObsInput.setSize(d);
		this.addElement("Name of observed variable",nameObsInput);
		
		// error term name
		nameErrInput = new JTextArea("$\\sigma^2_{e}");
		nameErrInput.setSize(d);
		this.addElement("Name of residual variance term ",nameErrInput);
		
		// icept - slope correlation
		nameSlopeInput = new JTextArea("\\etaS");
		nameSlopeInput.setSize(d);
		this.addElement("Name of slope term ",nameSlopeInput);
		nameIceptInput = new JTextArea("\\eta0");
		nameIceptInput.setSize(d);
		this.addElement("Name of intercept term ",nameIceptInput);

		uniqueResiduals = new JCheckBox("unique variances across time");
		this.addElement("Residual variances", uniqueResiduals);
		
		latentCovariance = new JCheckBox("estimate covariance");
		this.addElement("covariance between icept and slope", latentCovariance);
		latentCovariance.setSelected(true);
		
		this.addSendButton("Create");
		
		this.pack();
		
		this.setVisible(true);
		
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		ModelView mv = new ModelView(desktop);
		mv.setSize(600,650);
		desktop.add(mv);
		
		ModelRequestInterface model = mv.getModelRequestInterface();
		
		int numObs = Integer.parseInt((String) numObsInput.getValue().toString());

		double[] timepoints = new double[numObs];
		for (int i=0; i < numObs; i++)
		{
			timepoints[i] = i / (numObs-1.0);
		}
		
		int xOffset = 70;
		int yOffset = 100;
		
		int N = timepoints.length;
		
		//System.out.println("Rebuild with "+N+ " time points");
		
		Node icept = new Node(nameIceptInput.getText());
		icept.setPosition(xOffset,yOffset+70);
		model.requestAddNode(icept);
		Node slope = new Node(nameSlopeInput.getText());
		slope.setPosition(xOffset+200, yOffset+70);
		model.requestAddNode(slope);
		
		
		if (latentCovariance.isSelected()) {
			Edge e = new Edge(icept, slope);
			e.setDoubleHeaded(true);
			e.setFixed(false);
			e.setParameterNameByUser("$\\sigma_{\\eta0.\\etaS}");
			model.requestAddEdge(e);
		}
		
		Node[] obs = new Node[N];
		Node[] eta = new Node[N];
		Node[] deltaeta = new Node[N-1];
		for (int i=0; i < N; i++) {
			obs[i] = new Node(nameObsInput.getText()+(i+1));
			obs[i].setIsLatent(false);
			obs[i].setPosition(xOffset+i*80, yOffset+380);
			model.requestAddNode(obs[i]);
			
			eta[i] = new Node("\\eta"+(i+1));
			eta[i].setPosition(xOffset+i*80, yOffset+380-100);
			model.requestAddNode(eta[i]);
			
			Edge obseta = new Edge(eta[i],obs[i]);
			model.requestAddEdge(obseta);
			
			if (i < (N-1)) {
			 deltaeta[i] = new Node("\\Delta\\eta"+i);
			 deltaeta[i].setPosition(xOffset+(i+1)*80, yOffset+380-200);
			 model.requestAddNode(deltaeta[i]);
			}
			//obs[i].setConnected(true); // workaround
		}
		
		for (int i=1; i < N; i++) {
			Edge etaeta = new Edge( eta[i-1], eta[i]);
			model.requestAddEdge(etaeta);
			Edge detaeta = new Edge(deltaeta[i-1],eta[i]);
			model.requestAddEdge(detaeta);
			Edge beta = new Edge(eta[i-1],deltaeta[i-1]);
			beta.setFixed(false);
			beta.setParameterNameByUser("$\\beta");
			model.requestAddEdge(beta);
		}
		
	//	int centerAt = Integer.parseInt(numCenter.getValue().toString())-1;
		
		Edge edge1 = new Edge(icept, eta[0],false);
		model.requestAddEdge(edge1);
		
		// Loadings
		for (int i=0; i < N-1; i++) {
		
			
			//int loading = i;
			
			//if (loading != 0) {
				Edge edge2 = new Edge(slope, deltaeta[i],false);
				edge2.setValue(1);
				edge2.setParameterNameByUser("$\\alpha");
				edge2.setFixed(false);
				edge2.edgeLabelRelativePosition = ((i+1)/(float)N);
				model.requestAddEdge(edge2);
			//}
		}
		
		// residual variances
		for (int i=0; i < N; i++) {
			Edge edge = new Edge(obs[i], obs[i],true);
			edge.setValue(1.0);
			if (uniqueResiduals.isSelected())
				edge.setParameterName(nameErrInput.getText()+(i+1));
			else
				edge.setParameterName(nameErrInput.getText());
			edge.setAutomaticNaming(false);
			edge.setFixed(false);
			model.requestAddEdge(edge);
		}
		
		// latent variances
		Edge edgeI = new Edge(icept, icept, true);
		//edgeI.setValue(sigmaI);
		edgeI.setFixed(false);
		//edgeI.edgeStyle = Edge.EdgeStyle.ALWAYS_LABEL;
		edgeI.setParameterName("$\\sigma^2_{\\eta0}");
		edgeI.setAutomaticNaming(false);
		edgeI.setFixed(false);
		model.requestAddEdge(edgeI);
		
		Edge edgeS = new Edge(slope, slope, true);
		edgeS.setValue(1.0);
		edgeS.setParameterName("$\\sigma^2_{\\etaS}");
		edgeS.setAutomaticNaming(false);
		edgeS.setFixed(false);
		model.requestAddEdge(edgeS);
		
		// add mean
		
		Node mean = new Node();
		mean.setPosition(xOffset+100, yOffset-60);
		mean.setTriangle(true);
		model.requestAddNode(mean);
		
		Edge edgeMi = new Edge(mean, icept);
		edgeMi.setFixed(false);
		edgeMi.setParameterNameByUser("$\\mu_{\\eta0}");
		model.requestAddEdge(edgeMi);
		
		Edge edgeMs = new Edge(mean, slope);
		edgeMs.setFixed(false);
		edgeMs.setParameterNameByUser("$\\mu_{\\etaS}");
		model.requestAddEdge(edgeMs);
		
	/*	
		TimeseriesMeanPlot tmp = new TimeseriesMeanPlot((OnyxModel)model);
		tmp.setLocation(this.getLocation().x+300, this.getLocation().y);
		this.desktop.add(tmp);*/
		
		this.dispose();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	public static void main(String[] args) {
		
	}
}
