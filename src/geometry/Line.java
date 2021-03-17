package geometry;


import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.awt.Graphics;
import java.awt.Point;

/**
 * 
 * stores line in coordinate equation:
 * 
 * a*x+b*y+c=0
 * 
 * and therefore
 * 
 * y = -(ax-c) / b
 * 
 * @author andreas
 *
 */
public class Line extends GeometricObject {

	private double a, b, c;

	public Line(double a, double b, double c) {
		this.a = a;
		this.b = b;
		this.c = c;
	}
	
/*	public Line(int m, int t)
	{
		
	}
	*/
	public Line(int x1,int y1,int x2,int y2)
	{
		this(new Point(x1,y1),new Point(x2,y2));
	}

	public Line(Point p1, Point p2) {
		double dx = p1.x - p2.x;
		double dy = p1.y - p2.y;

		a = dy;
		b = -dx;

		c = -(a * p1.x + b * p1.y);
	}

	public String toString()
	{
		return "<Line: a="+a+" b="+b+" c="+c+">";
	}
	
	public void shift(int byX, int byY)
	{
		int totalShift = -byX-byY;
//		System.out.println("SHIFT"+totalShift+" c="+c);
		this.c += totalShift*b;
	}
	
	public double[] getNormalVector()
	{
		double x,y;
		
		double TOLERANCE = 0.0000001;
		
		if (Math.abs(b)<TOLERANCE) {
			return new double[] {1.0,0.0};
		} else		if (Math.abs(a)<TOLERANCE) {
			return new double[] {0,1};
		}  else {
			x = 1;
			y = (-a-c)/b;
			double sum = Math.sqrt(x*x+y*y);
			return new double[] {-x/sum, y/sum};
		}
		
	}
	
	public double getM()
	{
		return -a/b;
	}
	
	public double getT()
	{
		return -c/b;
	}
	
	public double distance(double x, double y)
	{
	// HNF
			/*double dx = scx - tcx;
			double dy = scy - tcy;

			double a = dy;
			double b = -dx;

			double c = -(a * scx + b * scy);
*/
			double dist = (a * x + b * y + c) / (Math.sqrt(a * a + b * b));

			return dist;
	}
	
	public Point intersect(Line line) {

		if(this.a==0) {
			if (line.a==0) return null;
			return line.intersect(this);
		}
		
		double y = (line.a / this.a * this.c - line.c)
				/ (line.b - line.a / this.a * this.b);

		double x = (this.b * y + this.c) / (-this.a);
		
		return (new Point((int)Math.round(x), (int)Math.round(y)));

	}
	
	public List<Point> intersect(Triangle tri)
	{
		List<Point> result = new ArrayList<Point>();
		
		Line side1 = new Line(new Point(tri.getX(), tri.getY()+tri.getWidth()), new Point(
				tri.getX() + tri.getWidth()/2, tri.getY()));
		
		Point isect = side1.intersect(this);

		Line side2 = new Line(new Point(tri.getX()+tri.getWidth(), tri.getY()+tri.getWidth()), new Point(
				tri.getX() + tri.getWidth()/2, tri.getY()));
		
		Point isect2 = side2.intersect(this);
		
		Line side3 = new Line(new Point(tri.getX(), tri.getY()+tri.getWidth()), new Point(
				tri.getX() + tri.getWidth(), tri.getY()+tri.getHeight()));
		
		Point isect3 = side3.intersect(this);	
		
		if ((isect != null) && (isect.y >= tri.getY())
				&& (isect.y <= tri.getY() + tri.getHeight())) {
			result.add(isect);

		}
		if ((isect2 != null) && (isect2.y >= tri.getY())
				&& (isect2.y <= tri.getY() + tri.getHeight())) {
			result.add(isect2);
		}

		if ((isect3 != null) && (isect3.x >= tri.getX())
				&& (isect3.x <= tri.getX() + tri.getWidth())) {
			result.add(isect3);
		}
		
		return(result);
	}
	
	public List<Point> intersect(Rectangle rect) {
		
		List<Point> result = new ArrayList<Point>();
		
		//Line edge = new Line(new Point(scx, scy), new Point(tcx, tcy));
		
		Line top = new Line(new Point(rect.getX(), rect.getY()), new Point(
				rect.getX() + rect.getWidth(), rect.getY()));
		
		Point isect = top.intersect(this);
		
		Line bottom = new Line(new Point(rect.getX(), rect.getY()
				+ rect.getHeight()), new Point(rect.getX()
				+ rect.getWidth(), rect.getY() + rect.getHeight()));
		Point isect2 = bottom.intersect(this);
		
		Line left = new Line(new Point(rect.getX(), rect.getY()),
				new Point(rect.getX(), rect.getY() + rect.getHeight()));
		Point isect3 = left.intersect(this);

		// intersect right line
		Line right = new Line(new Point(rect.getX() + rect.getWidth(),
				rect.getY()), new Point(rect.getX() + rect.getWidth(),
				rect.getY() + rect.getHeight()));
		Point isect4 = right.intersect(this);

		if ((isect != null) && (isect.x >= rect.getX())
				&& (isect.x <= rect.getX() + rect.getWidth())) {
			result.add(isect);

		}
		if ((isect2 != null) && (isect2.x >= rect.getX())
				&& (isect2.x <= rect.getX() + rect.getWidth())) {
			result.add(isect2);
		}

		if ((isect3 != null) && (isect3.y >= rect.getY())
				&& (isect3.y <= rect.getY() + rect.getHeight())) {
			result.add(isect3);
		}

		if ((isect4 != null) && (isect4.y >= rect.getY())
				&& (isect4.y <= rect.getY() + rect.getHeight())) {
			result.add(isect4);
		}
		
		return result;
	}

	
	/**
	 * 
	 * solves the Equation:
	 * solve  (x-x0)^2/a^2+((mx+t)-y0)^2/b^2=1 for x
	 * 
	 * @param oval
	 * @return
	 */
	public List<Point> intersect(Oval oval) {
        List<Point> result = new ArrayList<Point>();

        if (Double.isInfinite(getT()))
        {
        	double x1 = oval.x0;
        	double y1 = oval.y0+oval.b;
            result.add(new Point((int)Math.round(x1),(int)Math.round(y1)));

        	double x2 = oval.x0;
        	double y2 = oval.y0-oval.b;
            result.add(new Point((int)Math.round(x2),(int)Math.round(y2)));
            
            return(result);
        }
        

        double m = getM();
        double t = getT();
        
        double D = sqr(oval.a*getM())+sqr(oval.b);
        
        double term1 = sqr(oval.a*oval.b)*(D-sqr(m*oval.x0)-2*m*t*oval.x0
        		+2*m*oval.x0*oval.y0-sqr(t)+2*t*oval.y0-sqr(oval.y0));
        
        double term2 = -sqr(oval.a)*m*t+sqr(oval.a)*m*oval.y0+sqr(oval.b)*oval.x0;
        
        double x1 = (Math.sqrt(term1)+term2)/D;
        double x2 = (-Math.sqrt(term1)+term2)/D;

        
        double y1 = m*x1+t;
        double y2 = m*x2+t;
        result.add(new Point((int)Math.round(x1),(int)Math.round(y1)));
        result.add(new Point((int)Math.round(x2),(int)Math.round(y2)));
        
        
        return(result);
	}
	
	
	
	public List<Point> intersect_timo(Oval oval) {
        List<Point> result = new ArrayList<Point>();

        
        
        double m = -a / b;
	    double c = -this.c / b;
	    double s = 1.0 / oval.a; //(oval.a*oval.a);
	    double t = 1.0 / oval.b; //(oval.b*oval.b);
	  //  this.shift(-(int)oval.x0, -(int)oval.y0);
	    
	    double denom = m*m*t + s;
	    double A = (2.0*m*c*t) / denom;
	    double B = 1.0 / denom;
	    double inSqrt = (A*A + 4*B)/4;
	    if (inSqrt <= 0) return result;
	    if (inSqrt == 0) {
	        double x = -A/2.0, y = m*x+c;
	        result.add(new Point((int)Math.round(x),(int)Math.round(y)));
	    } else {
	        double sqrt = Math.sqrt(inSqrt);
            double x1 = -A/2.0 + sqrt, y1 = m*x1+c,
                   x2 = -A/2.0 - sqrt, y2 = m*x2+c;
            //x1+=oval.x0; y1+=oval.y0;
            //x2+=oval.x0; y2+=oval.y0;
//            System.out.println(x1+","+y1);
//            System.out.println(x2+","+y2);
            result.add(new Point((int)Math.round(x1),(int)Math.round(y1)));
            result.add(new Point((int)Math.round(x2),(int)Math.round(y2)));
	    }
	    //this.shift((int)oval.x0, (int)oval.y0);
	    return result;
	    
	}
	
	public List<Point> intersect_crappy2(Oval oval) {
        List<Point> result = new ArrayList<Point>();
        
      /*  double oa = oval.a*oval.a;
        double ob = oval.b*oval.b;
        */
        double oa = oval.a;
        double ob = oval.b;
        
        double E = ob*ob+getM()*getM()*oa*oa;
        double F = 2*getM()*getT()*oa*oa;
        double G = getT()*getT()*oa*oa - oa*oa*ob*ob;
        
        double A = E;
        double B = F - 2*oval.x0*E;
        double C = oval.x0*oval.x0*E-oval.x0*(F-getM()*oval.y0*a*a)+oa*oa*
        		(getT()-2*getT()*oval.y0+getM()*oval.x0*oval.y0-ob*ob);
 
        
        double D = B*B-4*A*C;
        
        if (D > 0) {
        	double x1 = (-B+Math.sqrt(D))/(2*A);
        	double x2 = (-B-Math.sqrt(D))/(2*A);
        	double y1 = x1*getM()+(getT()-getM()*oval.x0+oval.y0);
        	double y2 = x2*getM()+(getT()-getM()*oval.x0+oval.y0);
        	result.add( new Point((int)x1,(int)y1));
        	result.add( new Point((int)x2,(int)y2));
        	
        }
       
        return result;
	}
	
	
	public List<Point> intersect_crappy(Oval oval) {
		List<Point> result = new ArrayList<Point>();

        this.shift((int)-oval.x0, (int)-oval.y0);
		
		//TODO
		try {
	
			// works for centered thing!
			/*double A = sqr(oval.b)+sqr(oval.a)*sqr(getM());
			double B = 2*sqr(oval.a)*getM()*(getT()-oval.y0)-2*sqr(oval.b)*oval.x0;
			double C = sqr(oval.b*oval.x0)+sqr(oval.a*(getT()-oval.y0))-sqr(oval.a*oval.b);
*/
			
			double A = sqr(oval.b)+sqr(oval.a)*sqr(getM());
			double B = 2*sqr(oval.a)*getM()*(getT());
			double C = sqr(oval.a*(getT()))-sqr(oval.a*oval.b);

			

	//		int ox = (int)oval.x0;
		//	int oy = (int)oval.y0;
//			System.out.println()
			//this.shift(-ox, -oy);
			//this.shift(ox,oy);
			
			
			/*double A = sqr(oval.b)+sqr(oval.a)*sqr(getM());
			double B = 2*sqr(oval.a)*getM()*getT();
			double C = sqr(oval.a*getT())-sqr(oval.a*oval.b);
*/
//			System.out.println("A "+A+" B "+B+ " C "+C);
			double D = (sqr(oval.b)-4*A*C);
//			System.out.println("M:"+getM()+ "T:"+getT());
			
//			System.out.println("");
//			System.out.println("D="+D);
			
			if (D<0) { return result; }
			
			if (D==0) {
				D = Math.sqrt(D);
				double x1=-B/(2*A);
				double y1= (int)(x1*getM()+getT());			
				result.add(new Point((int)(x1 +oval.x0),(int)(y1+oval.y0)));

				return result;
			}
			
			/*System.out.println("X0:"+;
			System.out.println("X1:"+(-B-D)/(2*A));
			*/
			D = Math.sqrt(D);
			double x1=(-B+D)/(2*A);
			double x2=(-B-D)/(2*A);
		    
			this.shift((int)+oval.x0, (int)+oval.y0);
			x1+=oval.x0;
			x2+=oval.x0;
			
			double y1= (int)(x1*getM()+getT());
			double y2 = (int)(x2*getM()+getT());
			
		/*	result.add(new Point((int)(x1 +oval.x0),(int)(y1+oval.y0)));
			result.add(new Point((int)(x2 + oval.x0),(int)(y2+oval.y0)));
			*/
			result.add(new Point((int)(x1 ),(int)(y1)));
			result.add(new Point((int)(x2 ),(int)(y2)));

			
			//double e = getM()*oval.a/oval.b;
			//double f = (getM()*oval.x0+getT()-oval.y0)/oval.b;
			
			// b^2-4*ac
			//double D = sqr(e)-sqr(f)+1;
		//	this.shift(ox,oy);
					
			
			
//			double A = 
			
			
/*			if (D < 0) return result;

			if (D == 0) {
				double x1 = (-oval.a*e*f)/(1+sqr(e))+oval.x0;
				double y1 = getM()*x1+getT();
				Point p1 = new Point(
						(int)x1,(int)y1
						);
				result.add(p1);				
				return(result);
			}
				
			double x1 = oval.a*(-e*f-D)/(1+sqr(e))+oval.x0;
			double y1 = getM()*x1+getT();
			Point p1 = new Point(
					(int)x1,(int)y1
					);
			result.add(p1);
			
			
			/*R[2].x := a*(-e*f + Dis)/(1 + sqr(e)) + c;
			R[2].y := m*R[2].x + n;
			*/
			/*
			double x2 = oval.a*(-e*f + D)/(1+sqr(e))+oval.x0;
			double y2 = getM()*x2+getT();
			Point p2 = new Point(
					(int)x2,(int)y2
					);
			result.add(p2);		*/	
			
//			double dx = 
			
			/*double a,b,c;
			
			double D =  sqr(b) - 4 * a * c;
			
			double mu = (-b +- Math.sqrt(sqr(b) - 4 * a *c))/(2 * a);*/
//			this.
			//double x = Math.sqrt( 1- sqr(oval.)/sqr(b) )
			
		} catch (Exception e) {
			//TODO
		}
		
        this.shift((int)oval.x0, (int)oval.y0);

		
		return result;
	}
	
	public List<Point> intersect(GeometricObject obj) {
		if (obj instanceof Rectangle) {
			return this.intersect((Rectangle)obj);
		} else if (obj instanceof Oval) {
			return this.intersect((Oval)obj);
		} else if (obj instanceof Triangle) {
			return this.intersect((Triangle)obj);
		} else {
			System.err.println("Not implemented yet! Intersection of geometric objects");
			return new ArrayList<Point>();
		}
	}
	
	public void paint(Graphics g)
	{
		int x1 = -1000;
		int y1 = (int) (x1*getM()+getT());
		int x2 = 1000;
		int y2 = (int) (x2*getM()+getT());
			g.drawLine(x1, y1, x2, y2);
		
	}
	
    public double getA() {return a;}
    public double getB() {return b;}
    public double getC() {return c;}
}
