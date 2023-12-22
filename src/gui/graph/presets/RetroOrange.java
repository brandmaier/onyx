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

public class RetroOrange extends Preset {

	Color c1 = new Color(32,113,120);
	Color lachs = new Color(255,150,102);
	Color eier = new Color(255,225,132);
	Color sand = new Color(245,233,190);
	Color c5 = new Color(23,76,79);
	
	@Override
	public String getName() {
	//	return "Friedrichshain";
		return "Retro Orange";
	}

	@Override
	public void apply(Graph graph, Node node) {
		boolean hasVariance=false;
		int numIn=0, numOut=0;
		for (Edge edge : graph.getEdges())
		{
			if (edge.isDoubleHeaded() && (edge.target==node || edge.source==node))
			{
				hasVariance = true;
			}
			
			if (!edge.isDoubleHeaded()) {
				if (edge.source==node) numOut++;
				if (edge.target==node) numIn++;
			}
		}
		
		node.setFontSize(12);
		
		if (node.isLatent()) {
			if (numOut==1 && numIn==0 && hasVariance) {
				node.setFillColor(sand);
				node.setFontColor(c5);
				node.setLineColor(c5);
			}else {
				node.setFillColor(lachs); 
				node.setFontColor(c5);
				node.setLineColor(c5);
			}
			
		} else {
			node.setFillColor(eier);	
			node.setFontColor(c5);
			node.setLineColor(c5);
		}
		

		
		node.nodeFillGradient =  FillStyle.GRADIENT;
		node.setShadow(true);
		node.setShadow_type(0);
		node.setRough(false);

	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineColor(c5);
		edge.setLineWidth(3);
		edge.getLabel().setFontSize(12);
		edge.getLabel().setColor(Color.black);
	}
	
	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = Color.white;
	}

}
