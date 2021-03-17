package gui.undo;

import gui.graph.Edge.EdgeStyle;
import gui.graph.Graph;

public class PathStyleStep extends UndoStep {

	EdgeStyle oldStyle;
	Graph graph;
	private EdgeStyle redoStyle;
	
	public PathStyleStep(Graph graph, EdgeStyle oldStyle)
	{
		this.graph = graph;
		this.oldStyle = oldStyle;
	}
	
	@Override
	public void undo() {
		redoStyle = graph.getEdgeStyle();
		graph.changeEdgeStyle(oldStyle);

	}

	public void redo() {
		
		graph.changeEdgeStyle(redoStyle);
	}
	
}
