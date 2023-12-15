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

public class EdgeStateChangedStep extends UndoStep {

	private Edge oldEdge, newEdge;
	private ModelView mv;

	public EdgeStateChangedStep(ModelView mv, gui.graph.Edge edge)
	{
		super();
		this.title = "Changed edge"+edge.getSource().getCaption()+"->"+edge.getTarget().getCaption()+".";
		this.mv = mv;
		this.oldEdge = (Edge)edge.clone();
		this.newEdge = edge;
	}
	
	public void undo()
	{
		mv.getModelRequestInterface().requestRemoveEdge(newEdge);
		mv.getModelRequestInterface().requestAddEdge(oldEdge);
		mv.repaint();
	}
	
	public void redo()
	{
		mv.getModelRequestInterface().requestRemoveEdge(oldEdge);
		mv.getModelRequestInterface().requestAddEdge(newEdge);
		mv.repaint();
	}

	public Edge getPreviousEdgeState() {
		return oldEdge;
	}

	public Edge getCurrentEdgeState() {
		return newEdge;
	}

	
	
}
