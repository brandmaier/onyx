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
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;

import gui.fancy.PaintTricks;
import gui.graph.Edge;
import gui.graph.Edge.EdgeStyle;
import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Node;
import gui.graph.NodeDrawProxy;


public class Comic extends Preset {

	
	Color greenish = PaintTricks.hexToColor("#03b08d");
	Color gold = PaintTricks.hexToColor("#fdcc4e");
	float strokeWidth = 2.5f;
	
	@Override
	public void apply(Graph graph)
	{
		super.apply(graph);
		graph.backgroundColor = greenish;
	}

	@Override
	public String getName() {
		return "Comic";
	}

	@Override
	public void apply(Graph graph, Node node) {
		
		node.setStrokeWidth(strokeWidth);
		
			node.setFillColor(gold);
			node.setLineColor(Color.black);	
			node.setStrokeWidth(1);
		
		node.nodeFillGradient =  FillStyle.FILL;

		node.setShadow(true);
		node.setFontColor(Color.black);
		node.setFontSize(14);
		node.setRough(false);
		
		node.setShadow_type(2);
		
		//node.setDrawProxy( getNodeDrawProxy() );
	}
	
	private NodeDrawProxy ndp;

	private NodeDrawProxy getNodeDrawProxy() {
		if (ndp==null) {
			ndp = new NodeDrawProxy() {
				
				@Override
				public void draw(Node node, Graphics2D g, boolean markUnconnectedNodes) {
					
				/*	Shape shape;
					// determine shape
					if (node.isMeanTriangle()) {
						shape = new Polygon(new int[] { node.getX(), node.getX() + node.getWidth() / 2, node.getX() + node.width },
								new int[] { node.y + node.height, node.y, node.y + node.height }, 3);
					} else {

						if (node.isLatent()) {
							shape = new Ellipse2D.Double(node.x, node.y, node.width, node.height);
						} else {

							shape = new Rectangle(node.x, node.y, node.width, node.height);

						}
					}
					*/
				}
			};
		}
		
		return(ndp);
		
	}

	@Override
	public void apply(Graph graph, Edge edge) {
		edge.setLineWidth(strokeWidth);
		edge.setArrowStyle(1);
		edge.setLineColor(Color.black);
		edge.setDashStyle(null);
		/*if (edge.target==edge.source) {
			edge.setLineColor(Color.gray);
		}*/
	
		edge.setEdgeStyle(EdgeStyle.NORMAL);
		
		edge.getLabel().setFontSize(11);
		
		edge.getLabel().setColor(Color.black);

	}

}
