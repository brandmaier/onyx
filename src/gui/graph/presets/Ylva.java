package gui.graph.presets;

import java.awt.Color;

import gui.graph.Edge;
import gui.graph.Edge.EdgeStyle;
import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Node;


public class Ylva extends Preset {

	
	Color nfill = new Color(130,37,12);
	Color bright = new Color(250,229,223);
	Color blue = new Color(2,19,79);
	int strokeWidth = 2;
	
	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = blue;
	}

	@Override
	public String getName() {
		return "Ylva";
		//return "Dahlem";
	}

	@Override
	public void apply(Graph graph, Node node) {
		node.setStrokeWidth(strokeWidth);
		node.setFillColor(nfill);
		node.nodeFillGradient =  FillStyle.FILL;
		node.setLineColor(bright);
		node.setShadow(false);
		node.setFontColor(blue);
		node.setFontSize(11);
		node.setRough(false);
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth+1);
		edge.setArrowStyle(1);
		edge.setLineColor(bright);
		edge.setDashStyle(null);
		/*if (edge.target==edge.source) {
			edge.setLineColor(Color.gray);
		}*/
	
		edge.setEdgeStyle(EdgeStyle.NORMAL);
		
		edge.getLabel().setFontSize(11);
		
		edge.getLabel().setColor(bright);

	}

}
