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

import javax.swing.Icon;

import engine.ModelRunUnit;
import engine.ParameterReader;

import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;

public abstract class Preset {

	ParameterReader parameterReader;
	
	public void init(Graph graph)
	{
		
	}

	public void apply(Graph graph) {
		
		init(graph);

		for (Edge edge : graph.getEdges()) {
			apply(graph,edge);
		}
		
		for (Node node: graph.getNodes()) {
			apply(graph,node);

		}

	}

	public abstract String getName();
	
	public abstract void apply(Graph graph, Node node);
	public abstract void apply(Graph graph, Edge edge);

	public void apply(Graph graph, ParameterReader showingEstimate) {
		this.parameterReader = showingEstimate;
		apply(graph);
	}

	
}
