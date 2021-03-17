package gui.undo;

import engine.Dataset;
import engine.RawDataset;
import engine.ModelRequestInterface;
import gui.Desktop;
import gui.frames.MainFrame;
import gui.graph.Graph;
import gui.graph.Node;
import gui.graph.VariableContainer;
import gui.linker.LinkException;

public class UnlinkStep extends UndoStep {

	private VariableContainer container;
	private Dataset dataset;
	private int columnId;

	private ModelRequestInterface mri;
	
	
	
	@Override
	public void undo() {
		try {
			Desktop.getLinkHandler().link(dataset, columnId, container, mri);
		} catch (LinkException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		container.getGraph().getParentView().repaint();
	}

	public void redo() {
		Desktop.getLinkHandler().unlink(container);
		container.getGraph().getParentView().repaint();
	}

	public UnlinkStep(VariableContainer container, Dataset dataset, int columnId, ModelRequestInterface mri) {
		
		this.container = container;
		this.dataset = dataset;
		this.columnId = columnId;
		
		this.mri = mri;
		this.title="Linked VariableContainer";
	}	

}
