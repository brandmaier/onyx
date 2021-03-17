package gui.undo;

import gui.graph.Edge;

public class EdgeFontSizeChanged extends UndoStep {

	Edge edge;
	float fontSize, redoFontSize;
	
	public EdgeFontSizeChanged(Edge edge, float fontSize)
	{
		this.edge = edge;
		this.fontSize = fontSize;
	}
	
	@Override
	public void undo() {
		redoFontSize = edge.getLabel().getFontSize();
		edge.getLabel().setFontSize(fontSize);
	}
	
	public void redo() {
		edge.getLabel().setFontSize(redoFontSize);
	}

}
