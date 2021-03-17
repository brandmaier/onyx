package gui.undo;

import gui.graph.Edge;

public class EdgeArrowStyleChanged extends UndoStep {

	private int arrowStyle;
	private int redoArrowStyle;
	Edge edge;

	public EdgeArrowStyleChanged(Edge edge)
	{
		this.edge = edge;
		this.arrowStyle = edge.getArrowStyle();
	}
	
	@Override
	public void undo() {
		redoArrowStyle = edge.getArrowStyle();
		edge.setArrowStyle( this.arrowStyle );
	}

	public void redo() {
		edge.setArrowStyle(redoArrowStyle);
	}
}
