package gui.graph.presets;

import java.awt.Color;

import gui.graph.Edge;
import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Node;

public class Happy extends Preset {

	protected Color f1 = new Color(255,204,204), f2 = new Color(204,255,204), f3= new Color(204,204,255);
	
	private float strokeWidth = 2.5f;

	@Override
	public String getName() {
		return "Happy";
	}

	@Override
	public void apply(Graph graph, Node node) {
		boolean hasVariance=false;
		int numIn=0, numOut=0;
		for (Edge edge : graph.getEdges())
		{
			if (edge.isDoubleHeaded() && (edge.target==node || edge.source==node))
			{
				hasVariance = true;
			}
			
			if (!edge.isDoubleHeaded()) {
				if (edge.source==node) numOut++;
				if (edge.target==node) numIn++;
			}
		}
//		System.out.println(node+" "+numOut+" "+numIn);
		if (node.isLatent()) {
			if (numOut==1 && numIn==0 && hasVariance)
				node.setFillColor(f1);
			else
				node.setFillColor(f2);
		} else {
			node.setFillColor(f3);			
		}
		
		node.setShadow(true);
		node.setFontColor(Color.black);
		node.setFontSize(10);
		node.setRough(false);
		
		node.setFillStyle(FillStyle.FILL);

	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth);
		edge.setArrowStyle(1);
		edge.setLineColor(Color.black);
	}
	
	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = Color.white;
	}

}
