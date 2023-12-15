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
import gui.views.ModelView;

public class EdgeDeleteStep extends UndoStep {
	
	private Edge edge;
	private ModelView mv;

	public EdgeDeleteStep(ModelView mv, gui.graph.Edge  edge)
	{
		super();
		if (edge != null)
			this.title="Delete edge "+edge.getParameterName();
		else {
			System.err.println("Potential problem in delete edge!");
		}
		this.mv = mv;
		this.edge = edge;
	}
	
	public void undo()
	{
		if (edge != null)
			mv.getModelRequestInterface().requestAddEdge(edge);
	}
	
	public void redo()
	{
		if (edge != null)
			mv.getModelRequestInterface().requestRemoveEdge(edge);
	}
	
}
