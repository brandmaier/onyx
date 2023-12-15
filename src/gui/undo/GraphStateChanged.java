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
package gui.undo;

import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.views.ModelView;

public class GraphStateChanged extends MultiStep {

	private Graph graph;

	public GraphStateChanged(ModelView mv, Graph graph)
	{
		this.graph = graph;
		
		for (Node node : graph.getNodes()) {
			this.add(new NodeStateChangedStep(mv, node));
		}
		
		for (Edge edge : graph.getEdges()) {
			this.add(new EdgeStateChangedStep(mv, edge));
		}
	}
	

}
