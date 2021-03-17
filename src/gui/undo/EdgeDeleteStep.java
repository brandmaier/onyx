package gui.undo;

import gui.graph.Edge;
import gui.views.ModelView;

public class EdgeDeleteStep extends UndoStep {
	
	private Edge edge;
	private ModelView mv;

	public EdgeDeleteStep(ModelView mv, gui.graph.Edge  edge)
	{
		super();
		if (edge != null)
			this.title="Delete edge "+edge.getParameterName();
		else {
			System.err.println("Potential problem in delete edge!");
		}
		this.mv = mv;
		this.edge = edge;
	}
	
	public void undo()
	{
		if (edge != null)
			mv.getModelRequestInterface().requestAddEdge(edge);
	}
	
	public void redo()
	{
		if (edge != null)
			mv.getModelRequestInterface().requestRemoveEdge(edge);
	}
	
}
