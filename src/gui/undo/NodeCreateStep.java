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
