package gui.arrows;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

public class SimpleArrow extends Arrow
{
	Line2D.Double line;
	AffineTransform tx;
	Polygon arrowHead;

	public SimpleArrow(int x1, int y1, int x2, int y2) {
		super(x1,y1,x2,y2);

		line = new Line2D.Double(x1,y1,x2,y2);
		
		tx = new AffineTransform();
		tx.setToIdentity();
	    double angle = Math.atan2(line.y2-line.y1, line.x2-line.x1);
	    tx.translate(line.x2, line.y2);
	    tx.rotate((angle-Math.PI/2d));  
	    
	    
		

		arrowHead = new Polygon();  
		arrowHead.addPoint( 0,5);
		arrowHead.addPoint( -5, -5);
		arrowHead.addPoint( 5,-5);

		
	}
	
	@Override
	public void draw(Graphics2D g) {

//		line.
			g.drawLine(x1, y1, x2, y2);
		
			AffineTransform old = g.getTransform();
			g.setColor(Color.black);
		    g.setTransform(tx);   
		    g.fill(arrowHead);
		    g.setTransform(old);
	}
	
}