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
package gui.graph.presets;

import java.awt.Color;

import gui.graph.Edge;

import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Node;

public class Mint extends Preset {
//
	//Color c1 = new Color(204,170,143);
	//Color c2 = new Color(192,178,255);
	Color c3 = new Color(102,127,102);
	
	@Override
	public String getName() {
		//return "Tempelhof";
		return "Mint";
	}

	@Override
	public void apply(Graph graph, Node node) {
		node.setStrokeWidth(1.8f);
		
		if (node.isLatent()) {
			node.setFillColor(Color.white);
			//node.setFillColor(c1);
		} else {
			node.setFillColor(Color.white);
			//node.setFillColor(c2);
			//node.setFillColor(Color.white);
			
		}
		
		node.nodeFillGradient =  FillStyle.FILL;

		node.setLineColor(Color.black);
		node.setFontColor(Color.black);
		node.setShadow(false);
		node.setFontSize(10);
		node.setRough(false);
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(1.8f);
		edge.setArrowStyle(1);
		
		if (edge.target==edge.source) {
			edge.setLineColor(Color.black);
		}
		
		if (edge.source.isMeanTriangle()) {
			edge.setLineColor(c3);
			edge.setDashStyle(new float[]{1});
		}
		
		edge.getLabel().setFontSize(11);
		edge.getLabel().setColor(Color.black);
		
	}

	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = new Color(204,255,204);
	}
}
