package geometry;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.GeneralPath;

public class Parabola {
	public double a,b,c;
	private int toX;
	private int startX;
	
	public Parabola(double a, double b, double c, int startX, int toX)
	{
		this.a = a;
		this.b = b;
		this.c = c;
		this.startX = startX;
		this.toX = toX;
	}
	
	public Parabola(double x1, double y1, double x2, double y2, double x3, double y3, int startX, int toX)
	{
		a = (x1*(y2-y3)+x2*(y3-y1)+x3*(y1-y2))/((x1-x2)*(x1-x3)*(x3-x2));
		b = (x1*x1*(y2-y3)+x2*x2*(y3-y1)+x3*x3*(y1-y2))/((x1-x2)*(x1-x3)*(x2-x3));
		c = (x1*x1*(x2*y3-x3*y2)+x1*(x3*x3*y2-x2*x2*y3)+x2*x3*y1*(x2-x3))/((x1-x2)*(x1-x3)*(x2-x3));

		this.startX = startX;
		this.toX = toX;
	}
	
	public boolean isClose(int x, int y, double radius)
	{
		double py = evaluate(x);
		if (Math.abs(py-y) < radius)
		{
			return true;
		}
		return false;
	}
	
	public double evaluate(double x)
	{
		return a*x*x+b*x+c;
	}
	
	public double prime(double x)
	{
		return 2*a*x+b;
	}
	
	public void draw(Graphics g)
	{
		double lastY = evaluate(startX);

/*		for (int x = startX; x < toX; x++)
		{
			double y = evaluate(x);
//			g.fillRect(x,y,1,2);
			
			g.drawLine(x-1, (int)lastY, x, (int)y);
			lastY = y;
		}
	*/	
		
		GeneralPath path = new GeneralPath();
		path.moveTo(startX, evaluate(startX));
		//Point vertex = getVertexPoint();
		/*int controlx = (toX+startX)/2;
		
		double m = (2*a*startX+b);
		double t = evaluate(startX) - m*startX;
		
		int controly = (int) (controlx*m+t);*/
		Point control = getQP();
		path.quadTo( control.x, control.y, toX,evaluate(toX));
		
		((Graphics2D)g).draw(path);
	}
	
	public Point getQP()
	{
		int controlx = (toX+startX)/2;
		
		double m = (2*a*startX+b);
		double t = evaluate(startX) - m*startX;
		
		int controly = (int) (controlx*m+t);
		
		return new Point(controlx, controly);
	}
	
	public Point getVertexPoint()
	{
		double x = -b/(2*a);
		return new Point( (int)Math.round(x),(int)Math.round(evaluate(x)) );
	}

	public double[] inverse(int y) {
		double[] result = new double[2];
		
		double term1 = Math.sqrt(y/a + (b*b)/(4*a*a)-c/a );
		
		result[0]= term1- (b/2/a);
		result[1]= -term1- (b/2/a);
		
		
		return result;
	}
}
