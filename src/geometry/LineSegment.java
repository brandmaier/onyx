package geometry;

import java.awt.Graphics;
import java.awt.Point;

import java.util.Iterator;
import java.util.List;

// TODO: unfinished
public class LineSegment extends Line 
{

	public int y2;
	public int x2;
	public int y1;
	public int x1;
	
	private int leftx, lefty,rightx, righty;
	private int topx,topy, bottomx, bottomy;

	public LineSegment(int x1, int y1, int x2, int y2) {
		super(x1, y1, x2, y2);
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
		
		if (x1 < x2) {
			leftx = x1;
			lefty = y1;
			rightx = x2;
			righty = y2;
		} else {
			leftx = x2;
			lefty = y2;
			rightx = x1;
			righty = y1;
		}
		
		if (y1 < y2) {
			bottomy = y1;
			bottomx = x1;
			topy = y2;
			topx = x2;
		} else {
			bottomy = y2;
			bottomx = x2;
			topy = y1;
			topx = x1;
		}
	}

	public List<Point> intersect(Oval oval) {
		List<Point> points = super.intersect(oval);
		
		Iterator<Point> iter = points.iterator();
//		System.out.println(iter.)
		while(iter.hasNext())
		{
			Point p = iter.next();
			//System.out.println(p);
				if (p.x < leftx || p.x > rightx || p.y < bottomy || p.y > topy) iter.remove();
			
		}
		
		return points;
		
	}
	
	public List<Point> intersect_fuzzy(Oval oval, double tol) {
		List<Point> points = super.intersect(oval);
		
		Iterator<Point> iter = points.iterator();
	//	System.out.println(points.size()+" raw");
		while(iter.hasNext())
		{
			Point p = iter.next();
			//System.out.println(p);
				if (p.x < leftx-tol || p.x > rightx+tol || p.y < bottomy-tol || p.y > topy+tol) iter.remove();
			
		}
		//System.out.println(points.size()+" raw removed");
		return points;
		
	}
	
	
	public double length()
	{
		return Math.sqrt( (x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
	}
	
	public List<Point> intersect_fuzzy(Rectangle rect, double tol) {
		List<Point> points = super.intersect(rect);
		
		Iterator<Point> iter = points.iterator();
//		System.out.println(iter.)
		while(iter.hasNext())
		{
			Point p = iter.next();
			//System.out.println(p);
				if (p.x < leftx-tol || p.x > rightx+tol || p.y < bottomy-tol || p.y > topy+tol) iter.remove();
			
		}
		
		return points;
		
	}

	/**
	 * Return a list of points at the intersection of a specified GeometricObject
	 * with this LineSegment.
	 * @param r2
	 * @param tolerance
	 * @return
	 */
	public List<Point> intersect_fuzzy(GeometricObject r2, double tolerance) {
		if (r2 instanceof Oval)
		{
			return intersect_fuzzy((Oval)r2, tolerance);
		}
		
		else if (r2 instanceof Rectangle) {
			return intersect_fuzzy((Rectangle)r2, tolerance);
		/*} else if (r2 instanceof Line) {
			return intersect((Line)r2); //TODO*/
		}
		
		return null;
	}

	public void paint(Graphics g)
	{
	
			g.drawLine(x1, y1, x2, y2);
		
	}
	
	
	public double distance(double x, double y)
	{
		
		if (x < leftx)
		{
			return Math.sqrt( (x-leftx)*(x-leftx)+(y-lefty)*(y-lefty) );
		} else if (x > rightx) {
			return Math.sqrt( (x-rightx)*(x-rightx)+(y-righty)*(y-righty) );
			
		} else {
			return super.distance(x,y);
		}
		
	}



}
