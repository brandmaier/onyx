package gui.graph.presets;

import java.awt.Color;

import gui.graph.Edge;

import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Node;

public class Nightshift extends Preset {
//
	//Color c1 = new Color(204,170,143);
	//Color c2 = new Color(192,178,255);
	Color bg //= new Color(40,12,82);
	 = new Color(0,0,0);
	Color[] neon =  new Color[] {
			new Color(190, 90, 90), //red/brown
			new Color(150, 200, 210), // blue
			new Color(200, 220, 70), // neon green
			new Color(255, 255, 255), // white
			new Color(230, 190, 60) //orange
			
	};
	
	@Override
	public String getName() {
		
		return "Nightshift";
	}

	@Override
	public void apply(Graph graph, Node node) {
		node.setStrokeWidth(1.8f);
		
		/*if (node.isLatent()) {
			node.setFillColor(Color.);
			//node.setFillColor(c1);
		} else {
			node.setFillColor(Color.white);
			//node.setFillColor(c2);
			//node.setFillColor(Color.white);
			
		}*/
		
		node.nodeFillGradient =  FillStyle.FILL;

		int nid = (int)Math.floor(Math.random()*neon.length);
		
		node.setLineColor(neon[nid]);
		node.setFillColor(neon[nid]);
		node.setFontColor( Color.black);
		node.setShadow(false);
		node.setFontSize(10);
		node.setRough(false);
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(1.8f);
		edge.setArrowStyle(1);
		
		edge.setLineColor(neon[1]);
		edge.getLabel().setColor(neon[1]);
		
		if (edge.target==edge.source) {
			edge.setLineColor(neon[3]);
			edge.getLabel().setColor(neon[3]);
		}
		
		if (edge.source.isMeanTriangle()) {
			edge.setLineColor(neon[0]);
			edge.getLabel().setColor(neon[0]);
			
		}
		
		
		
		
		edge.getLabel().setFontSize(11);
		
	}

	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = bg;
	}
}
