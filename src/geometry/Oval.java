package geometry;

import java.awt.Graphics;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Ellipse2D.Double;
import java.util.ArrayList;
import java.util.List;

public class Oval extends GeometricObject {
	
	public double x0,y0,a,b;
	
	int x,y,width,height;

	public Oval(int x, int y, int width, int height)
	{
		a = width/2.0;
		b = height/2.0;
		x0 = x+width/2.0;
		y0 = y+height/2.0;
		
	//	System.out.println("OVAL "+a+","+b+","+x0+","+y0);
		
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	

	public Shape getShape()
	{
		return new Ellipse2D.Double(x,y,width,height);
	}

	public void shift(int byx, int byy)
	{
		x0-=byx;
		y0-=byy;
	}

	public void paint(Graphics g)
	{
		for (int ix = x; ix < x+width; ix++)
		{
		
			double iy =(int)( Math.sqrt(  ( 1- sqr(ix-x0)/sqr(a) )*sqr(b))+y0 );
	
			g.drawRect(ix, (int)iy, 1, 1);
			iy =(int)( -Math.sqrt(  ( 1- sqr(ix-x0)/sqr(a) )*sqr(b))+y0 );
			g.drawRect(ix, (int)iy, 1, 1);
		}
	}

	public int[] evaluate(int x) {
		int[] result = new int[2];
		
		double A = a*a;
		double B = -2*y0*A;
		double C = y0*y0*A-A*b*b+(x-x0)*(x-x0)*b*b;
		double D = Math.sqrt(B*B-4*A*C);
		result[0]= (int)(-(B+D)/(2*A));
		result[1] = (int)(-(B-D)/(2*A));
		
		return result;
	}

	/*public double getDistance(int x, int y)
	{
		
		
	}*/
	
	/**
	 * solve [x^2/g^2+(x^2*a+y*b+c)^2/f^2=1] for x
	 * 
	 * @param p
	 * @return
	 */
	public List<Point> intersect(Parabola p)
	{
		List<Point> result = new ArrayList<Point>();
		
		//TODO
		double f = this.a;
		double g = this.b;
		double a = p.a;
		double b = p.b;
		double c = p.c;
		
		double term2 = -2*a*b*g*g*y-2*a*c*g*g-f*f;
		double term1 = f*Math.sqrt(4*a*a*y*y*y*y+4*a*b*g*g*y+4*a*c*y*y+f*f);
		
		double x1 = + Math.sqrt(+term1+term2) / (Math.sqrt(2)*a*y);
		double x2 = - Math.sqrt(-term1+term2) / (Math.sqrt(2)*a*y);
		double x3 = + Math.sqrt(+term1+term2) / (Math.sqrt(2)*a*y);
		double x4 = - Math.sqrt(-term1+term2) / (Math.sqrt(2)*a*y);

		result.add( new Point((int)Math.round(x1), (int)Math.round(p.evaluate(x1))));
		result.add( new Point((int)Math.round(x2), (int)Math.round(p.evaluate(x2))));
		result.add( new Point((int)Math.round(x3), (int)Math.round(p.evaluate(x3))));
		result.add( new Point((int)Math.round(x4), (int)Math.round(p.evaluate(x4))));
		
		return(result);
	}

	public double distanceFromCenter(double px, double py) {
		return  (  (px-x0)*(px-x0)/(a*a)+(py-y0)*(py-y0)/(b*b) );
	}
	
	public GeometricObject extrude(int size)
	{
		return new Oval(x-size, y-size, width+2*size, height+2*size);
	}
}
