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

public class Oldpink extends FadedColors {

	static Color c_background = new Color(248, 238, 230); //Color.getColor("#F8EEE6");
	static Color c1 = new Color(42, 34, 0); //Color.decode("#EBDCD5");
	static Color c2 = new Color(235, 220, 213); // Color.decode("#2A2303");
	
	public String getName() {
		return "Old Pink";
	}
	
	public Oldpink() {
		pal = new Palette(new Color[] {c1, c2} );
		
		super.background = c_background;
	}
	

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth);
		edge.setArrowStyle(1);
//		edge.setLineColor(c2);
		edge.setLineWidth(0.9f);
		edge.getLabel().setColor(c1);
		edge.setLineColor(c1);
		
	}
	
	@Override
	public void apply(Graph graph, Node node) {
		
		node.setRough(false);
		node.setShadow(false);
		node.setFillStyle(FillStyle.FILL);
		
		Color col = colmap.get(node);
		
		if (node.isObserved()) {
			for (Edge edge : graph.getAllEdgesAtNode(node)) {
				Color cc = null;
				if (edge.getSource().isLatent()) {
					col = colmap.get(edge.getSource());
				}
		}
		}
		
		node.setFillColor(col);
		if (col==c1) node.setFontColor(c2);
		else node.setFontColor(c1);
		//node.setFontColor(Color.black);
		
		node.setLineColor(c1);
		node.setStrokeWidth(0.9f);
		}
	
}
