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
package gui.graph.presets.yaml;

import java.awt.Color;

import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.graph.presets.Preset;

public class YAMLPreset extends Preset {
	
	Color nodeLineColor, nodeFillColor, nodeFontColor,
	edgeLineColor;
	
	String name;

	public int edgeArrowType;

	@Override
	public String getName() {
		return(name);
	}

	@Override
	public void apply(Graph graph, Node node) {
		
		node.setFillColor(nodeFillColor);			
		node.setFontColor(nodeFontColor);
		
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		// TODO Auto-generated method stub
		
	}

}
