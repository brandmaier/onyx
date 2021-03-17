package gui.graph.presets;

import java.awt.Color;

import gui.graph.Edge;
import gui.graph.Edge.EdgeStyle;
import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Node;


public class Sketch extends Preset {

	

	
	int strokeWidth = 2;
	
	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = Color.white;
	}

	@Override
	public String getName() {
		return "Sketch";
		//return "Dahlem";
	}

	@Override
	public void apply(Graph graph, Node node) {
		node.setStrokeWidth(strokeWidth);
		//node.setFillColor(Color.white);
		node.nodeFillGradient =  FillStyle.HAND;
		node.setLineColor(Color.black);
		node.setShadow(false);
		node.setFontColor(Color.black);
		node.setFontSize(11);
		
		//node.setFill
		
		node.setRough(true);
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth+1);
		edge.setArrowStyle(0);
		edge.setLineColor(Color.black);
		edge.setDashStyle(null);
		/*if (edge.target==edge.source) {
			edge.setLineColor(Color.gray);
		}*/
	
		edge.setEdgeStyle(EdgeStyle.NORMAL);
		
		edge.getLabel().setColor(Color.black);
		
		edge.getLabel().setFontSize(11);
		
		
		
	//	edge.setDashStyle(Edge.);;
	}

}
