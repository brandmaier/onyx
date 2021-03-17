package gui.undo;

import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;

public class EdgeHeadChangedStep extends UndoStep {

	private Edge edge;
	private ModelView mv;

	Node target;
	Node source;
	
	public EdgeHeadChangedStep(ModelView mv, gui.graph.Edge edge)
	{
		super();
		//this.title = "Changed edge"+edge.getSource().getCaption()+"->"+edge.getTarget().getCaption()+".";
		this.mv = mv;
		this.edge = edge;
	}
	
	public void undo()
	{
		mv.getModelRequestInterface().requestCycleArrowHeads(edge);
		mv.getModelRequestInterface().requestCycleArrowHeads(edge);
		mv.repaint();
	}
	
	public void redo()
	{
		mv.getModelRequestInterface().requestCycleArrowHeads(edge);
	//	mv.getModelRequestInterface().requestCycleArrowHeads(edge);
		mv.repaint();
	}
}
