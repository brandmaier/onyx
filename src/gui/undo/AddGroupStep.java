package gui.undo;

import gui.graph.Node;
import gui.views.ModelView;

public class AddGroupStep extends UndoStep {
	private Node node;
	private ModelView mv;

	public AddGroupStep(ModelView mv, gui.graph.Node node)
	{
		super();
		this.title = "Add grouping to "+node.getCaption();
		this.mv = mv;
		this.node = node;
	}
	@Override
	public void undo() {
		this.node.removeGroupingVariable();
	}

}
