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

import gui.Desktop;
import gui.graph.Node;
import gui.linker.DatasetField;
import gui.linker.LinkException;
import gui.views.ModelView;

public class NodeDeleteStep extends UndoStep {

	private Node node;
	private ModelView mv;
	//private DatasetField groupingContainer;
	//private DatasetField variableContainer;

	public NodeDeleteStep(ModelView mv, gui.graph.Node node)
	{
		super();
		this.title="delete node "+node.getCaption();
		this.mv = mv;
		this.node = node;
		//groupingContainer = Desktop.getLinkHandler().getDatasetField(node.getGroupingVariableContainer());
		//variableContainer = Desktop.getLinkHandler().getDatasetField(node.getObservedVariableContainer());
	}
	
	public void undo()
	{
		mv.getModelRequestInterface().requestAddNode(node);
	}
	
	public void redo()
	{
		mv.getModelRequestInterface().requestRemoveNode(node);
		
		// re-link variable to dataset
		/*if (groupingContainer != null) 
			try {
				Desktop.getLinkHandler().link(groupingContainer.dataset, groupingContainer.columnId, 
						node.getGroupingVariableContainer(), mv.getModelRequestInterface());
			} catch (LinkException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		if (variableContainer != null) 
				try {
					Desktop.getLinkHandler().link(variableContainer.dataset, variableContainer.columnId, 
							node.getObservedVariableContainer(), mv.getModelRequestInterface());
				} catch (LinkException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	
		*/
	}
	
}
