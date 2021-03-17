package gui.undo;

import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.views.ModelView;

public class GraphStateChanged extends MultiStep {

	private Graph graph;

	public GraphStateChanged(ModelView mv, Graph graph)
	{
		this.graph = graph;
		
		for (Node node : graph.getNodes()) {
			this.add(new NodeStateChangedStep(mv, node));
		}
		
		for (Edge edge : graph.getEdges()) {
			this.add(new EdgeStateChangedStep(mv, edge));
		}
	}
	

}
