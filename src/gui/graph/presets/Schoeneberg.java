package gui.graph.presets;

import java.awt.Color;

import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;

public class Schoeneberg extends Modern {

	double min, max;
	
	@Override
	public String getName() {
		return "Modern with Dynamic Stroke Width";
	}


	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.setDynamicStrokeWidths();
		graph.backgroundColor = Color.white;
	}
	
	/*public void pre() {
		
	}*/

}
