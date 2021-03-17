package gui.graph.presets;

import java.awt.Color;

import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;

public class Charlottenburg extends Preset {

	Color c1 = new Color(4,81,140);
	Color c2 = new Color(0,48,86);
	Color c3 = new Color(71,217,191);
	
	private float strokeWidth = 3f;

	@Override
	public String getName() {
		return "Charlottenburg";
	}
	
	

	@Override
	public void apply(Graph graph, Node node) {
		
		if (node.isLatent()) {		
				node.setFillColor(c1);
				node.setFontColor(Color.white);
		} else {
				node.setFillColor(c3);			
				node.setFontColor(Color.black);
		}
		
		node.setShadow(false);
	
		node.setFontSize(11);


	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth);
		edge.setArrowStyle(1);
		edge.setLineColor(c2);
	}

}
