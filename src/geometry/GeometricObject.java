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
package geometry;

import java.awt.Point;
import java.util.Iterator;
import java.util.List;

public class GeometricObject {

	protected static double sqr(double x)
	{
		return x*x;
	}
	
	public static Point closestPoint(List<Point> points, Point point)
	{
		Point bestPoint = null;
		double bestDistance = Double.MAX_VALUE;
		Iterator<Point> iterPoint = points.iterator();
		while (iterPoint.hasNext()) {
			Point curPoint = iterPoint.next();
			double d = curPoint.distance(point);
			if (d < bestDistance) {
				bestDistance = d;
				bestPoint = curPoint;
			}
		}
		
		return bestPoint;
	}
	
	public GeometricObject extrude(int size) {
//		throw new Exception("Not implemented yet!")
		System.err.println("Not implemented yet!");
		return(null);
	}
	
	public java.awt.Shape getShape()
	{
		System.err.println("Not implemented yet!");
		return(null);
	}
}
