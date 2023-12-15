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

import gui.graph.Node;
import gui.views.ModelView;

public class NodeCreateStep extends UndoStep {

	private Node node;
	private ModelView mv;

	public NodeCreateStep(ModelView mv, gui.graph.Node node)
	{
		super();
		this.title = "Create node "+node.getCaption();
		this.mv = mv;
		this.node = node;
	}
	
	@Override
	public void undo() {
		this.mv.getModelRequestInterface().requestRemoveNode(node);
	}
	
	public void redo()
	{
		this.mv.getModelRequestInterface().requestAddNode(node);
	}

}
