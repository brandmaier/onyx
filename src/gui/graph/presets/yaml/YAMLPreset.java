package gui.graph.presets.yaml;

import java.awt.Color;

import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.graph.presets.Preset;

public class YAMLPreset extends Preset {
	
	Color nodeLineColor, nodeFillColor, nodeFontColor,
	edgeLineColor;
	
	String name;

	public int edgeArrowType;

	@Override
	public String getName() {
		return(name);
	}

	@Override
	public void apply(Graph graph, Node node) {
		
		node.setFillColor(nodeFillColor);			
		node.setFontColor(nodeFontColor);
		
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		// TODO Auto-generated method stub
		
	}

}
