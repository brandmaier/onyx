package gui.graph.presets;

import java.awt.Color;

import gui.graph.Edge;
import gui.graph.Edge.EdgeStyle;
import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Node;


public class Cinema extends Preset {

	
	Color bck = new Color(242,234,208);
			//new Color(50,50,50);
	Color cyn = new Color(94,191,173);
	Color carmesin = new Color(217,90,43);
	Color gold = new Color(242, 194, 48);
	
	float strokeWidth = 2.5f;
	
	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = bck;
	}

	@Override
	public String getName() {
		return "Cinema";
	}

	@Override
	public void apply(Graph graph, Node node) {
		
		node.setStrokeWidth(strokeWidth);
		
		if (node.isLatent()) {
			node.setFillColor(carmesin);

			if (node.isMeanTriangle())
				node.setFillColor(gold);
		
			node.nodeFillGradient =  FillStyle.GRADIENT;			
		} else {
			node.setFillColor(cyn);
			node.nodeFillGradient =  FillStyle.GRADIENT;
		}
		
		node.setLineColor(Color.black);
		node.setShadow(false);
		
		
		node.setFontColor(Color.black);
		node.setFontSize(10);
		node.setRough(false);
		
		node.setStrokeWidth(2);
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth);
		edge.setArrowStyle(1);

		edge.setLineColor(Color.black);
		edge.setDashStyle(null);
		/*if (edge.target==edge.source) {
			edge.setLineColor(Color.gray);
		}*/
	
		edge.setEdgeStyle(EdgeStyle.NORMAL);
		
		edge.getLabel().setFontSize(11);
		
		edge.getLabel().setColor(Color.black);

	}

}
