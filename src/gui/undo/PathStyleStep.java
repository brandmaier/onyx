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

import gui.graph.Edge.EdgeStyle;
import gui.graph.Graph;

public class PathStyleStep extends UndoStep {

	EdgeStyle oldStyle;
	Graph graph;
	private EdgeStyle redoStyle;
	
	public PathStyleStep(Graph graph, EdgeStyle oldStyle)
	{
		this.graph = graph;
		this.oldStyle = oldStyle;
	}
	
	@Override
	public void undo() {
		redoStyle = graph.getEdgeStyle();
		graph.changeEdgeStyle(oldStyle);

	}

	public void redo() {
		
		graph.changeEdgeStyle(redoStyle);
	}
	
}
