package gui.undo;

import engine.ModelRequestInterface;
import gui.graph.Node;

public class NodeTypeChangedStep extends UndoStep {

	Node node;
	private ModelRequestInterface mri;
	
	public NodeTypeChangedStep(ModelRequestInterface mri, Node node)
	{
		this.node = node;
		this.mri = mri;
	}
	
	@Override
	public void undo() {
		mri.requestSwapLatentToManifest(node);
	}
	
	public void redo() {
		mri.requestSwapLatentToManifest(node);
	}

}
