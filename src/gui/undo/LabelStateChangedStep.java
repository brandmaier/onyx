package gui.undo;

import gui.graph.Edge;
import gui.graph.LatexEdgeLabel;
import gui.graph.PlainEdgeLabel;
import gui.views.ModelView;

public class LabelStateChangedStep extends UndoStep {

	private LatexEdgeLabel oldLabel;
	private Edge edge;
	private ModelView mv;
	private LatexEdgeLabel redoLabel;

	public LabelStateChangedStep(ModelView mv, Edge edge)
	{
		super();
		this.title = "Changed edge";
		this.mv = mv;
		this.oldLabel = (LatexEdgeLabel)edge.getLabel().clone();
		this.edge = edge;
	}

	
	public void undo()
	{
		redoLabel = (LatexEdgeLabel)edge.getLabel().clone();
		edge.setLabel(oldLabel);
		mv.repaint();
	}
	
	public void redo()
	{
		edge.setLabel(redoLabel);
		mv.repaint();
	}
	
	
}
