/*
* Copyright 2023 by Timo von Oertzen and Andreas M. Brandmaier
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package gui.graph.presets;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import gui.graph.Edge;
import gui.graph.Edge.EdgeStyle;
import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Node;


public class Xmas extends Preset {

	
	Color bck = new Color(242,234,208);
			//new Color(50,50,50);
	Color cyn = new Color(94,191,173);
	Color carmesin = new Color(217,90,43);
	Color gold = new Color(242, 194, 48);
	
//	Color[] colors = new Color[] {gold, carmesin, cyn};
	
	Palette pal = OnyxPalette.xmas; 
			//OnyxPalette.pastel1;
	
	float strokeWidth = 1.5f;
	
	double[] ys = new double[] {0, 100, 200, 300, 400, 500, 600, 700, 800};
	
	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = pal.getBackgroundColor();
		
		 ys =
			    graph.getNodes().stream()
			         .mapToDouble(Node::getY)
			         .toArray();
		 
		 ys = clusterByGap(ys, 30);
	}

	@Override
	public String getName() {
		return "Christmas";
	}

	@Override
	public void apply(Graph graph, Node node) {
		
		node.setStrokeWidth(strokeWidth);
		
		if (node.isLatent()) {
	
		
			node.nodeFillGradient =  FillStyle.FILL;			
		} else {
		//	node.setFillColor(cyn);
			node.nodeFillGradient =  FillStyle.GRADIENT;
		}
		
		double cury = node.getY();
		int i = 0;
		for (i = 0; i < ys.length; i++) {
			if (cury < ys[i]) {break;}
		}
		Color col = pal.get(i);
		node.setFillColor(col);

//		node.setLineColor(Color.black);
		node.setLineColor(pal.getDarker(i, 100));
		node.setShadow(false);
		
		
		node.setFontColor(gold);
		node.setFontSize(10);
		node.setRough(false);
		
		node.setStrokeWidth(1.2f);
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth);
		edge.setArrowStyle(1);

		edge.setLineColor(gold);
		edge.setDashStyle(null);
		/*if (edge.target==edge.source) {
			edge.setLineColor(Color.gray);
		}*/
	
		edge.setEdgeStyle(EdgeStyle.NORMAL);
		
		edge.getLabel().setFontSize(11);
		
		edge.getLabel().setColor(gold);

	}
	
	// returns minimum thresholds where new color schemes begin
	 public static double[] clusterByGap(double[] values, double maxGap) {


		    double[] a = values.clone();
		    Arrays.sort(a);

		    List<Double> clusters = new ArrayList<>();
		    //clusters.add(a[0]);
		    double cur = a[0];

		    for (int i = 1; i < a.length; i++) {
		      if (a[i] - a[i - 1] > maxGap) {
		        clusters.add(cur);
		        cur = a[i];
		      }
		      
		    }
		    
		    return clusters.stream()
		            .mapToDouble(Double::doubleValue)
		            .toArray();
		  }	
	 

}
