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
