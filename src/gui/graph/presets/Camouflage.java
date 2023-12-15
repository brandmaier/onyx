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

import gui.graph.Edge;

import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Node;


public class Camouflage extends Preset {

	
	int miny, maxy;
	
	int strokeWidth = 2;
	
	@Override
	public void apply(Graph graph)
	{
		
		graph.backgroundColor = Color.white;
		
		miny = Integer.MAX_VALUE; maxy=Integer.MIN_VALUE;
		for (Node node : graph.getNodes()) {
			miny = Math.min(node.getY(), miny);
			maxy = Math.max(node.getY(), maxy);
		}
		
		super.apply(graph);
	}

	@Override
	public String getName() {
		return "Camouflage";
		//return "Dahlem";
	}

	@Override
	public void apply(Graph graph, Node node) {
		node.setStrokeWidth(strokeWidth);
		
		int max = 255;
		int x = (int)Math.round(max*((node.getY()-miny) / ((double)maxy-miny)));
		System.out.println((node.getY()-miny)+"--"+x);
		x = Math.max(Math.min(x, 255),0);
		//Color c = new Color(204,155, x);		// orange to violett
		Color c = new Color(x, 209, 119);
		
		node.setFillColor(c);
		node.nodeFillGradient =  FillStyle.GRADIENT;
		node.setLineColor(Color.black);
		node.setShadow(true);
		node.setFontColor(Color.black);
		node.setFontSize(10);
		node.setRough(false);
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth+1);
		edge.setArrowStyle(0);
		edge.setLineColor(Color.black);
		/*if (edge.target==edge.source) {
			edge.setLineColor(Color.gray);
		}*/
		
		edge.getLabel().setFontSize(11);
	}

}
