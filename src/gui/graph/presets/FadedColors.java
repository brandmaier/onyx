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

	Palette pal = OnyxPalette.faded;
	
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
		
		colpointer = 0;
	}
	
	public void init(Graph graph)
	{
		//if (colmap.isEmpty()) {
		colmap.clear();
			// map all latents
			for (Node tnode : graph.getLatentNodes()) {
				colmap.put(tnode, pal.get(colpointer));
				colpointer++;
				colpointer = colpointer % pal.getSize();
			}
		//}
	}
	
	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth);
		edge.setArrowStyle(1);
		Color cc = Color.black;
		cc = colmap.get(edge.getSource());
		if (cc == null) cc = Color.black;
		edge.setLineColor(cc);
		
		edge.getLabel().setColor(Color.black);
		
	}
	
	@Override
	public void apply(Graph graph, Node node) {
		
		node.setRough(false);
		node.setShadow(false);
		node.setFillStyle(FillStyle.FILL);
		node.setFontColor(Color.black);
		
		if (node.isLatent()) {
			node.setFillColor( colmap.get(node) );
			node.setLineColor( Palette.darker(colmap.get(node),100) );
			
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
