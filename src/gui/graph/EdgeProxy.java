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
package gui.graph;

import geometry.GeometricObject;

public abstract class EdgeProxy {
	
	// return the amount of padding for computing
	// the clipping region when drawing the line
	public int getShapePadding()
	{
		return 3;
	}

	
	public abstract void updateLabel(Edge edge);
	public abstract void updatePath(Edge edge, GeometricObject r1, GeometricObject r2);

	public abstract void updateArrow(Edge edge);
}
