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
import gui.graph.Graph;
import gui.graph.Node;

public class Charlottenburg extends Preset {

	Color c1 = new Color(4,81,140);
	Color c2 = new Color(0,48,86);
	Color c3 = new Color(71,217,191);
	
	private float strokeWidth = 3f;

	@Override
	public String getName() {
		return "Charlottenburg";
	}
	
	

	@Override
	public void apply(Graph graph, Node node) {
		
		if (node.isLatent()) {		
				node.setFillColor(c1);
				node.setFontColor(Color.white);
		} else {
				node.setFillColor(c3);			
				node.setFontColor(Color.black);
		}
		
		node.setShadow(false);
	
		node.setFontSize(11);


	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth);
		edge.setArrowStyle(1);
		edge.setLineColor(c2);
	}

}
