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

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import gui.Desktop;
import gui.frames.MainFrame;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;

public class MeasurementInvarianceWizard extends Dialog {

	private Desktop desktop;
	List<ModelView> modelViews;

	JButton ok;

	final String[] invarianceTypes = new String[]{"Configural","Weak/Metric","Strong/Scalar", "Strict"};
	JComboBox<String> invarianceType;

	JComboBox<String> sourceModel;
	
	JComboBox<String> datasets;
	JComboBox<String> variables;
	
	public MeasurementInvarianceWizard(Desktop desktop) {
		
		super("Create CFA-based Measurement Invariance Model");
		this.desktop = desktop;
		 modelViews = desktop.getModelViews();
		
		String[] items = new String[modelViews.size()];
		for (int i = 0; i < items.length; i++)
			items[i] = modelViews.get(i).getName();
		
		sourceModel = new JComboBox<String>(items);
		
		invarianceType = new JComboBox<String>(invarianceTypes);
		
		ok = new JButton("Done");
		ok.addActionListener(this);
		
		// layout:
		
		JLabel label = new JLabel("Source Model");
		this.add(label);
		this.add(sourceModel);
		JLabel label2 = new JLabel("Invariance Type");
		this.add(label2);
		this.add(invarianceType);
		
		this.add(ok);
		
		this.pack();
		this.setVisible(true);
	}

	
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// OK, let's roll...
		
		ModelView sourceModelView = modelViews.get(sourceModel.getSelectedIndex());
		int type = invarianceType.getSelectedIndex();
		
		// (1) create a clone & rename it & resize it
		ModelView model = desktop.cloneModelView(sourceModelView);
		model.setName(model.getName()+ invarianceTypes[invarianceType.getSelectedIndex()]);
		model.setSize(new Dimension(model.getWidth(),model.getHeight()*2));
		
		
		// (2) copy existing structure to double it
		model.selectAll();
		MainFrame.clipboard.copy(model.getGraph());
		geometry.Rectangle bounds = model.getGraph().getBoundingBox();
		MainFrame.clipboard.paste(model.getModelRequestInterface(), bounds.x, bounds.y+bounds.height + 20);
		model.getGraph().getSelectedNodes().flipHorizontally(model.getGraph());
		
		// (3) add grouping variables
		List<Node> nodes = model.getGraph().getNodes();
		for (int i=0; i < nodes.size(); i++) {
		
			Node cur = nodes.get(i);
			if (cur.isObserved()) {
				cur.setGrouping(true);
				cur.groupValue = (i<nodes.size()/2)?0:1;
			}
		}
		
		
		// potentially add mean structure
		List<Node> observed = model.getGraph().getObservedNodes();
		List<Node> latent = model.getGraph().getLatentNodes();
		boolean[] observedHasMean = new boolean[observed.size()];
		boolean[] latentHasMean = new boolean[latent.size()];
		for (int i=0; i < observed.size(); i++) {
			//if (!observed.get(i).hasMeanEdge()
		}
		
		
		// establish constraints
		if (type >= 1) {
			// establish factor loading constraints

			List<Edge> edges = model.getGraph().getEdges();
			int offset = edges.size()/2;
			
			for (int i=0; i< edges.size()/2; i++) {
				Edge edge_a = edges.get(i);
				Edge edge_b = edges.get(i+ offset);
				
				if (edge_a.getSource().isLatent() && edge_a.getTarget().isObserved() && !edge_a.isDoubleHeaded()
						&& edge_a.isFree()) {
					String parameterName = edge_a.getParameterName();
					edge_a.setParameterName(parameterName);
					edge_b.setParameterName(parameterName);
					model.getModelRequestInterface().requestChangeParameterOnEdge(edge_a);
					model.getModelRequestInterface().requestChangeParameterOnEdge(edge_b);
				}
			}
		}
		
		if (type >= 2) {
			// establish mean constraints from mean to observed

			List<Edge> edges = model.getGraph().getEdges();
			int offset = edges.size()/2;
			
			for (int i=0; i< edges.size()/2; i++) {
				Edge edge_a = edges.get(i);
				Edge edge_b = edges.get(i+ offset);
			
				if (edge_a.getSource().isMeanTriangle() && edge_a.getTarget().isObserved() &&
						edge_a.isFree()) {
					String parameterName = edge_a.getParameterName();
					edge_a.setParameterName(parameterName);
					edge_b.setParameterName(parameterName);
					model.getModelRequestInterface().requestChangeParameterOnEdge(edge_a);
					model.getModelRequestInterface().requestChangeParameterOnEdge(edge_b);
				}
			}
			
			// free latent means in all but the first group.
			for (int i=0; i< edges.size(); i++) {
				Edge edge_a = edges.get(i);
				Edge edge_b = edges.get(i+ offset);
				if (edge_a.getSource().isMeanTriangle() && edge_a.getTarget().isLatent() &&
						!edge_a.isDoubleHeaded()) {
					
					edge_a.setFixed(true);
					edge_a.setValue(0);
					model.getModelRequestInterface().requestChangeParameterOnEdge(edge_a);
					edge_b.setFixed(false);
//					edge_b.setValue(0);
					model.getModelRequestInterface().requestChangeParameterOnEdge(edge_b);
				}
				
			}
			
			
		} else {
			// make sure that factor means are zero
			List<Edge> edges = model.getGraph().getEdges();
			int offset = edges.size()/2;
			
			for (int i=0; i< edges.size(); i++) {
				Edge edge_a = edges.get(i);
				Edge edge_b = edges.get(i+ offset);
			
				if (edge_a.getSource().isMeanTriangle() && edge_a.getTarget().isLatent() &&
						!edge_a.isDoubleHeaded()) {
					edge_a.setFixed(true);
					edge_a.setValue(0);
					model.getModelRequestInterface().requestChangeParameterOnEdge(edge_a);
					edge_b.setFixed(true);
					edge_b.setValue(0);
					model.getModelRequestInterface().requestChangeParameterOnEdge(edge_b);
				}
			}
		}
		
		if (type >= 3) {
			// establish residual variance constraints

			List<Edge> edges = model.getGraph().getEdges();
			int offset = edges.size()/2;
			
			for (int i=0; i< edges.size()/2; i++) {
				Edge edge_a = edges.get(i);
				Edge edge_b = edges.get(i+ offset);
			
				if (edge_a.getSource().isObserved() && edge_a.getTarget().isObserved() &&
						edge_a.isFree() && edge_a.isDoubleHeaded()) {
					String parameterName = edge_a.getParameterName();
					edge_a.setParameterName(parameterName);
					edge_b.setParameterName(parameterName);
					model.getModelRequestInterface().requestChangeParameterOnEdge(edge_a);
					model.getModelRequestInterface().requestChangeParameterOnEdge(edge_b);
				}
			}
			
		}
		
		this.dispose();
	}

}
