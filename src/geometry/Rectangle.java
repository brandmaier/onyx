package geometry;

import java.awt.Point;
import java.awt.Shape;
import java.util.ArrayList;
import java.util.List;

public class Rectangle extends GeometricObject {
	public int x,y,width,height;

	public Rectangle(int x, int y, int width, int height) {
		super();
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public Shape getShape()
	{
		return new java.awt.Rectangle(x,y,width, height);
	}
	

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public List<Point> intersect(Parabola parabola) {
		List<Point> points = new ArrayList<Point>();
		
		double y1 = parabola.evaluate(x);
		double y2 = parabola.evaluate(x+width);
		
		double[] x3 = parabola.inverse(y);
		double[] x4 = parabola.inverse(y+height);
		
		if ((y1 > y) && (y1 < y+height)) 
			points.add(new Point(x, (int)Math.round(y1)));
		if ((y2 > y) && (y2 < y+height))
		points.add(new Point(x+width, (int)Math.round(y2)));
		
		if ((x3[0] > x) && (x3[0] < x+width))
		points.add(new Point((int)Math.round(x3[0]),y));
		if ((x3[1] > x) && (x3[1] < x+width))
		points.add(new Point( (int)Math.round(x3[1]),y));
		if ((x4[0] > x) && (x4[0] < x+width))
		points.add(new Point( (int)Math.round(x4[0]),y+height));
		if ((x4[1] > x) && (x4[1] < x+width))
		points.add(new Point( (int)Math.round(x4[1]),y+height));
		
		return(points);
	}

	public int getCenterY() {
		return this.y+this.height/2;
	}

	public int getCenterX() {
		return this.x+this.width/2;
	}
	
	public Point getCenter()
	{
		return new Point(getCenterX(), getCenterY());
	}
	
	public GeometricObject extrude(int size)
	{
		return new Rectangle(x-size, y-size, width+2*size, height+2*size);
	}
}
