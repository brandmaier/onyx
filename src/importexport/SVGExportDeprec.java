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
package importexport;

import importexport.filters.SVGFileFilter;

import java.io.File;

import javax.swing.filechooser.FileFilter;

import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.views.ModelView;

/**
 * see also http://www.w3schools.com/svg/svg_examples.asp
 * @author andreas
 *
 */
public class SVGExportDeprec extends Export{

	public SVGExportDeprec(ModelView modelView) {
		super(modelView, new SVGFileFilter(),new String[] {"svg","xml"});
	}

    public boolean isValid() {return !modelView.hasDefinitionEdges();}

	@Override
	public void export(File file) {
		
		Graph graph = modelView.getGraph();
		
		String svg = "<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">";
		
		for (Node node : graph.getNodes())
		{
			if (node.isMeanTriangle()) {
				
			}else {
				if (node.isLatent()) {
					svg+="<circle cx=\""+node.getXCenter()+
							"\" cy=\""+node.getYCenter()+
							"\" r=\""+node.getWidth()/2+"\" stroke=\"black\" stroke-width=\"2\" fill=\"white\" />";
					
					svg+="<text text-anchor=\"middle\" x=\""+node.getXCenter()+"\" y=\""+node.getYCenter()+"\">"+node.getCaption()+"</text>";
					
				} else {
					svg+="<rect x=\""+node.getX()+"\" y=\""+node.getY()+
							"\" width=\""+node.getWidth()+"\" height=\""+
							node.getHeight()+"\" style=\"fill:rgb(0,0,255);stroke-width:1;stroke:rgb(0,0,0)\" />";					
				}
			}
		}
		
		

		
		for (Edge edge : graph.getEdges())
		{
			if (!edge.isDoubleHeaded()) {
				svg+="<line x1=\""+edge.fromX+"\" y1=\""+
			edge.fromY+"\" x2=\""+edge.toX+"\" y2=\""+edge.toY+"\" "+
			"style=\"stroke:rgb(255,0,0);stroke-width:2\""+
			" />";
			} else {
				if (edge.source==edge.target) 
				{
					
				} else {
				/*svg+="  <path d=\"M "+edge.fromX+" "+edge.fromY+" q "+
						edge.parabola.getQP().x+" "+edge.parabola.getQP().y+
						" "+edge.toX+" "+edge.toY+"\" stroke=\"black\" stroke-width=\"5\" fill=\"none\" />";
				*/}
			}
		}
//		<line x1="0" y1="0" x2="200" y2="200" style="stroke:rgb(255,0,0);stroke-width:2" />
		//<text x="0" y="15" fill="red">I love SVG</text>
		
		// triangle:   <polyline points="20,20 40,25 60,40 80,120 120,140 200,180" style="fill:none;stroke:black;stroke-width:3" /
		// or: <polygon points="200,10 250,190 160,210" style="fill:lime;stroke:purple;stroke-width:1" />
		svg+="</svg>";
		
		
		System.out.println(svg);
	}


	
}
