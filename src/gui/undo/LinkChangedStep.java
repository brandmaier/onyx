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
