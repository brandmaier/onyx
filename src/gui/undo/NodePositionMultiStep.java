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

import gui.graph.Graph;
import gui.graph.Node;
import gui.views.ModelView;

/**
 * 
 * NodePositionMultiStep combines a set of Undo-Operations
 * into a single undo step that resets all node positions
 * of a graph to the previous position.
 * 
 * @author andreas
 *
 */
public class NodePositionMultiStep extends MultiStep {

	private ModelView modelView;
	private Graph graph;

	public NodePositionMultiStep(Graph graph)
	{
		this.graph = graph;
		
		modelView = graph.getParentView();
		for (Node node : graph.getNodes())
		{
			MovedStep step = new MovedStep(modelView, node);
			this.add(step);
		}
	}
	
	public void redo() {
		super.redo();
		if (modelView != null) {
			
			graph.invalidate();
			graph.validate();
			
			modelView.redraw();
		
		
		
		}
	}
	
	public void undo() {
		
		super.undo();
		
		if (modelView != null) {
			
			graph.invalidate();
			graph.validate();
			
			modelView.redraw();
		
		
		
		}
		
	}
	
}
