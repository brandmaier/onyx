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


public class Amelie extends Preset {

	
	Color edges = new Color(52,60+13,75); //#526C75
	Color nfill = new Color(51,90+12,12*16+5); //#519BB5
	Color orange= new Color(15*16+5,95,31); //#F59531
	Color fnt = new Color(31,13*16+1,15*16+5); //#31C1F5
	float strokeWidth = 2.5f;
	
	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = orange;
	}

	@Override
	public String getName() {
		return "Amelie";
	}

	@Override
	public void apply(Graph graph, Node node) {
		node.setStrokeWidth(strokeWidth);
		node.setFillColor(nfill);
		node.nodeFillGradient =  FillStyle.FILL;
		node.setLineColor(edges);
		node.setShadow(false);
		node.setFontColor(fnt);
		node.setFontSize(12);
		node.setRough(false);
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth+1);
		edge.setArrowStyle(1);
		edge.setLineColor(edges);
		edge.setDashStyle(null);
		/*if (edge.target==edge.source) {
			edge.setLineColor(Color.gray);
		}*/
	
		edge.setEdgeStyle(EdgeStyle.NORMAL);
		
		edge.getLabel().setFontSize(11);
		
		edge.getLabel().setColor(fnt);

	}

}
