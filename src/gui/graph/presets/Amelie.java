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

	
	Color light_sea_green = new Color(22,172,145); //#526C75
	Color old_lace = new Color(250,244,228); //#519BB5
	Color tomato = new Color(236,91,70); //#F59531
	Color dark_slate = new Color(67,38,66); //#31C1F5
	Color peru = new Color(209,113,55);
	float strokeWidth = 2.5f;
	
	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = old_lace;
	}

	@Override
	public String getName() {
		return "Amelie";
	}

	@Override
	public void apply(Graph graph, Node node) {
		
		node.setStrokeWidth(strokeWidth);
		if (!node.isObserved()) {
			if (Math.round(node.getY()/10) % 2==0 ) {
				node.setFillColor(tomato);
				node.setLineColor(tomato);
			} else {
				node.setFillColor(peru);
				node.setLineColor(peru);				
			}
		} else {
			node.setFillColor(light_sea_green);
			node.setLineColor(light_sea_green);			
		}
		node.nodeFillGradient =  FillStyle.FILL;

		node.setShadow(false);
		node.setFontColor(dark_slate);
		node.setFontSize(12);
		node.setRough(false);
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth+1);
		edge.setArrowStyle(1);
		edge.setLineColor(dark_slate);
		edge.setDashStyle(null);
		/*if (edge.target==edge.source) {
			edge.setLineColor(Color.gray);
		}*/
	
		edge.setEdgeStyle(EdgeStyle.NORMAL);
		
		edge.getLabel().setFontSize(11);
		
		edge.getLabel().setColor(peru);

	}

}
