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
