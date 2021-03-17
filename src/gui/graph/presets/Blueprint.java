package gui.graph.presets;

import java.awt.Color;

import gui.graph.Edge;

import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Node;

public class Blueprint extends Preset {
//
	//Color c1 = new Color(204,170,143);
	//Color c2 = new Color(192,178,255);
	Color c3 = new Color(102,127,102);
	
	@Override
	public String getName() {
		return "Blueprint";
	}

	@Override
	public void apply(Graph graph, Node node) {
		node.setStrokeWidth(1.8f);
		
		
		node.setFillStyle(FillStyle.NONE);
		//node.nodeFillGradient =  FillStyle.FILL;

		node.setLineColor(Color.white);
		node.setFontColor(Color.white);
		node.setShadow(false);
		node.setFontSize(10);
		node.setRough(true);
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(1.8f);
		edge.setArrowStyle(1);
		
		if (edge.target==edge.source) {
			edge.setLineColor(Color.white);
		}
		
		//if (edge.source.isMeanTriangle()) {
			edge.setLineColor(Color.white);
			//edge.setDashStyle(new float[]{1});
	//	}
		
		edge.getLabel().setFontSize(11);
		edge.getLabel().setColor(Color.white);
		
	}

	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = new Color(84,113,232);
	}
}
