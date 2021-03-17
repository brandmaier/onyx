package gui.undo;


import gui.graph.Edge;
import gui.views.ModelView;

public class EdgeCreateStep extends UndoStep {

	private Edge edge;
	private ModelView mv;

	public EdgeCreateStep(ModelView mv, gui.graph.Edge edge)
	{
		super();
		this.title = "Create edge "+edge.source.getCaption()+"->"+edge.target.getCaption()+".";
		this.mv = mv;
		this.edge = edge;
	}
	
	@Override
	public void undo() {
		this.mv.getModelRequestInterface().requestRemoveEdge(edge);
	}
	
	public void redo() {
		this.mv.getModelRequestInterface().requestAddEdge(edge);
	}

}
