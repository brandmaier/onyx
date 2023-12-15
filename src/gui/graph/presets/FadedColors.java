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
import java.util.ArrayList;
import java.util.HashMap;

import gui.graph.Edge;
import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Node;

public class FadedColors extends Default {

	Color[] cols = new Color[] {
			new Color(169,196,216), new Color(96,163,82), new Color(215,176,133),
			new Color(167,124,164), new Color(171,216,171), new Color(215,135,111)};
	int colpointer = 0;
	
	HashMap<Node, Color> colmap = new HashMap<Node, Color>();
	
	@Override
	public String getName() {
		return "Faded Colors";
	}

	
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = Color.white;
		

	}
	
	public void init(Graph graph)
	{
		//if (colmap.isEmpty()) {
		colmap.clear();
			// map all latents
			for (Node tnode : graph.getLatentNodes()) {
				colmap.put(tnode, cols[colpointer]);
				colpointer++;
				colpointer = colpointer % cols.length;
			}
		//}
	}
	
	@Override
	public void apply(Graph graph, Node node) {
		
		node.setRough(false);
		
		
		if (node.isLatent()) {
			node.setFillColor( colmap.get(node) );
			node.setFillStyle(FillStyle.FILL);
		} else {
			for (Edge edge : graph.getAllEdgesAtNode(node)) {
				Color cc = null;
				if (edge.getSource().isLatent()) {
					cc = colmap.get(edge.getSource());
				} else if (edge.getTarget().isLatent()) {
					cc = colmap.get(edge.getTarget());
				} else {
					cc = new Color(215,128,142);
				}
				if (cc != null) {
					node.setFillColor( cc );
				}
			}
		}
	}
	
}
