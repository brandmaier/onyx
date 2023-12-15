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
import gui.graph.Edge.EdgeStyle;
import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Node;


public class Posh extends Preset {

	
	Color dark_blue  = new Color(33,63,93);
			//new Color(50,50,50);
	Color cyn = new Color(82,150,142);
	Color carmesin = new Color(215,80,75);
	Color gold = new Color(210,186,98);
	Color blue = new Color(50,97,149);
	
	float strokeWidth = 2.5f;
	
	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = dark_blue;
	}

	@Override
	public String getName() {
		return "Posh";
	}

	@Override
	public void apply(Graph graph, Node node) {
		
		node.setStrokeWidth(strokeWidth);
		
		if (node.isLatent()) {
			node.setFillColor(carmesin);
			node.setLineColor(carmesin);

			if (node.isMeanTriangle()) {
				node.setFillColor(gold);
				node.setLineColor(gold);
			}
		
			node.nodeFillGradient =  FillStyle.GRADIENT;			
		} else {
			node.setFillColor(cyn);
			node.setLineColor(cyn);
			node.nodeFillGradient =  FillStyle.GRADIENT;
		}
		
		//node.setLineColor(Color.dark_blue);
		node.setShadow(false);
		
		
		node.setFontColor(Color.black);
		node.setFontSize(10);
		node.setRough(false);
		
		node.setStrokeWidth(2);
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth);
		edge.setArrowStyle(1);

		edge.setLineColor(gold);
		edge.setDashStyle(null);
		/*if (edge.target==edge.source) {
			edge.setLineColor(Color.gray);
		}*/
	
		edge.setEdgeStyle(EdgeStyle.NORMAL);
		
		edge.getLabel().setFontSize(11);
		
		edge.getLabel().setColor(cyn);

	}

}
