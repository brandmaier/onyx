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

import engine.ModelRequestInterface;
import gui.graph.Node;

public class NodeRenameStep extends UndoStep {

	String name = "";
	private ModelRequestInterface mri;
	private Node node;
	private String redoName;
	
	
	public NodeRenameStep(ModelRequestInterface mri, Node node, String name)
	{
		this.name = name;
		this.mri = mri;
		this.node = node;
	}
	
	@Override
	public void undo() {
		this.redoName = node.getCaption();
		this.mri.requestChangeNodeCaption(node, name);
		
	}
	
	public void redo() {
		this.mri.requestChangeNodeCaption(node, redoName);
	}

}
