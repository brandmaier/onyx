package gui.graph.presets;

import javax.swing.Icon;

import engine.ModelRunUnit;
import engine.ParameterReader;

import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;

public abstract class Preset {

	ParameterReader parameterReader;
	
	public void init(Graph graph)
	{
		
	}

	public void apply(Graph graph) {
		
		init(graph);

		for (Edge edge : graph.getEdges()) {
			apply(graph,edge);
		}
		
		for (Node node: graph.getNodes()) {
			apply(graph,node);

		}

	}

	public abstract String getName();
	
	public abstract void apply(Graph graph, Node node);
	public abstract void apply(Graph graph, Edge edge);

	public void apply(Graph graph, ParameterReader showingEstimate) {
		this.parameterReader = showingEstimate;
		apply(graph);
	}
	
}
