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
