package gui.graph.presets;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import gui.graph.Edge;
import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Node;

public class FadedColors extends Default {

	Color[] cols = new Color[] {
			new Color(169,196,216), new Color(96,163,82), new Color(215,176,133),
			new Color(167,124,164), new Color(171,216,171), new Color(215,135,111)};
	int colpointer = 0;
	
	HashMap<Node, Color> colmap = new HashMap<Node, Color>();
	
	@Override
	public String getName() {
		return "Faded Colors";
	}

	
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = Color.white;
		

	}
	
	public void init(Graph graph)
	{
		//if (colmap.isEmpty()) {
		colmap.clear();
			// map all latents
			for (Node tnode : graph.getLatentNodes()) {
				colmap.put(tnode, cols[colpointer]);
				colpointer++;
				colpointer = colpointer % cols.length;
			}
		//}
	}
	
	@Override
	public void apply(Graph graph, Node node) {
		
		node.setRough(false);
		
		
		if (node.isLatent()) {
			node.setFillColor( colmap.get(node) );
			node.setFillStyle(FillStyle.FILL);
		} else {
			for (Edge edge : graph.getAllEdgesAtNode(node)) {
				Color cc = null;
				if (edge.getSource().isLatent()) {
					cc = colmap.get(edge.getSource());
				} else if (edge.getTarget().isLatent()) {
					cc = colmap.get(edge.getTarget());
				} else {
					cc = new Color(215,128,142);
				}
				if (cc != null) {
					node.setFillColor( cc );
				}
			}
		}
	}
	
}
