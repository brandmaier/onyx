package gui.undo;

import gui.graph.Edge;
import gui.views.ModelView;

public class EdgeStateChangedStep extends UndoStep {

	private Edge oldEdge, newEdge;
	private ModelView mv;

	public EdgeStateChangedStep(ModelView mv, gui.graph.Edge edge)
	{
		super();
		this.title = "Changed edge"+edge.getSource().getCaption()+"->"+edge.getTarget().getCaption()+".";
		this.mv = mv;
		this.oldEdge = (Edge)edge.clone();
		this.newEdge = edge;
	}
	
	public void undo()
	{
		mv.getModelRequestInterface().requestRemoveEdge(newEdge);
		mv.getModelRequestInterface().requestAddEdge(oldEdge);
		mv.repaint();
	}
	
	public void redo()
	{
		mv.getModelRequestInterface().requestRemoveEdge(oldEdge);
		mv.getModelRequestInterface().requestAddEdge(newEdge);
		mv.repaint();
	}

	public Edge getPreviousEdgeState() {
		return oldEdge;
	}

	public Edge getCurrentEdgeState() {
		return newEdge;
	}

	
	
}
