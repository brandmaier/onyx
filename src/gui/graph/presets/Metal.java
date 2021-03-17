package gui.graph.presets;

import java.awt.Color;

import engine.ModelRunUnit;
import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;

public class Metal extends Happy {

	protected Color f1 = new Color(133,133,133), f2 = new Color(62,107,133), f3= new Color(197,210,219);


	@Override
	public String getName() {
		return "Metal";
	}
	
	@Override
	public void apply(Graph graph, Edge edge) {
		
		super.apply(graph, edge);
		
		if (parameterReader != null) {

		double[][] cov = parameterReader.getParameterCovariance();
		
		
		if (cov != null && edge.isFree()) {
		
			
			
		int idx = parameterReader.getParameterNameIndex( edge.getParameterName() );
		
//		graph.
		double var = cov[idx][idx];
		double est = parameterReader.getParameterValue( edge.getParameterName() );
		
		double Z = Math.abs(est/Math.sqrt(var));
		
		//System.out.println("APPLY"+Z+" "+edge.getParameterName());
		
		if (Z >=  1.959964) {
			edge.setLineColor(Color.black);
		} else {
			edge.setLineColor(Color.LIGHT_GRAY);
		}
				
		}
		
		}
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
		node.setFontColor(Color.black);
		if (node.isLatent()) {
			if (numOut==1 && numIn==0 && hasVariance)
				node.setFillColor(f1);
			else {
				node.setFillColor(f2);
			node.setFontColor(Color.white); 
			}
		} else {
			node.setFillColor(f3);			
		}
		
		node.setShadow(true);
		node.setRough(false);
		node.setFontSize(10);

	}

}
