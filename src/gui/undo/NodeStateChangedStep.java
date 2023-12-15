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

public class NodeStateChangedStep extends UndoStep {

	private Node oldNode, newNode;
	private ModelView mv;
	
	public Node getPreviousNodeState()
	{
		return oldNode;
	}
	
	public Node getCurrentNodeState()
	{
		return newNode;
	}

	public NodeStateChangedStep(ModelView mv, gui.graph.Node node)
	{
		super();
		this.title = "Change node";
		this.mv = mv;
		this.oldNode = (Node)node.clone();
		this.newNode = node;
	}
	
	public void undo()
	{
		// little workaround TODO
		oldNode.setSelected(false);
		
		mv.getModelRequestInterface().requestRemoveNode(newNode);
		mv.getModelRequestInterface().requestAddNode(oldNode);
		mv.repaint();
	}
	
	public void redo()
	{
		mv.getModelRequestInterface().requestRemoveNode(oldNode);
		mv.getModelRequestInterface().requestAddNode(newNode);
		
		mv.repaint();
	}
	
}
