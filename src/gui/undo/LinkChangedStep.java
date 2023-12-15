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
import gui.Desktop;
import gui.graph.Graph;
import gui.graph.Node;
import gui.graph.VariableContainer;
import gui.linker.DatasetField;
import gui.linker.LinkException;
/**
 * @deprecated
 */

public class LinkChangedStep extends UndoStep {

	private VariableContainer container;
	private DatasetField linkField;
//	private boolean link;
	private ModelRequestInterface mri;

	public LinkChangedStep(VariableContainer container, ModelRequestInterface mri) {
		this.title = "link changed";
		this.container = container;
		this.mri = mri;

		linkField = Desktop.getLinkHandler().getDatasetField(container);
		
	}

	@Override
	public void undo() {
			
			try {
				Desktop.getLinkHandler().link(linkField.dataset, linkField.columnId, container, mri);
			} catch (LinkException e) {
				e.printStackTrace();
			}
		
			container.getGraph().getParentView().repaint();
	}

	public void redo() {
		Desktop.getLinkHandler().unlink(container);
		
		container.getGraph().getParentView().repaint();
	}
	
}
