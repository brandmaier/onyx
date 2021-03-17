package gui.fancy;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.TexturePaint;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.JFrame;

import gui.graph.FillStyle;
import gui.graph.Node;

/**
 * This class encapsulates drawing routines for drawing hand-drawn lines and shapes.
 * Inspired by http://openaccess.city.ac.uk/1274/
 * 
 * @author brandmaier
 *
 */
public class Rough extends JFrame {

	public Rough() {
		setSize(500,500);
		setVisible(true);
	}
	
	public void paint(Graphics g)
	{
		Shape shape = new Line2D.Float(50, 50, 50, 500);
		
		Rough.line((Graphics2D)g,0f,0f,500f,500f);
		
	//	Rough.draw((Graphics2D)g, shape);
		
		shape = new Ellipse2D.Float(200,200,150,150);
		
		Rough.draw((Graphics2D)g, shape,1,2);
		
		shape = new Ellipse2D.Float(100,250,80,80);
	//	Rough.draw((Graphics2D)g, shape,1,1);
		
		int trix = 50, triy=50;
		int triw=100; int trih=100;
		Polygon p = new Polygon(new int[] { trix,
				trix + triw / 2, trix + triw }, 
				new int[] {
				triy + trih, triy, triy + trih }, 3);
		Rough._draw((Graphics2D)g, p);
		
		
		Paint myPaint = createHatchedPaint(Color.BLACK, 500, new Rectangle2D.Float(0,0,200,200), -1);
		Graphics2D g2 = (Graphics2D)g;
		g2.setPaint(myPaint);
		shape = new Ellipse2D.Float(100,250,80,80);

		shape = new Ellipse2D.Float(200,200,150,150);
		g2.fill(shape);
		
		
		Node n = new Node();
		n.setIsLatent(false);
		n.setFillColor(Color.BLUE);
		n.setFillStyle(FillStyle.HAND);
		n.setPosition(150, 150);
		n.draw(g2, false);
	}
	
	static Random rand = new Random();
	
	static float roughness=.5f; //.1
	static float bowing = .1f; //.1
	
	/** Generates a random offset scaled around the given range. Note that the offset can exceed
	 *  the given maximum or minimum depending on the sketchiness of the renderer settings.
	 *  @param minVal Approximate minimum value around which the offset is generated.
	 *  @param maxVal Approximate maximum value around which the offset is generated.
	 */
	private static float getOffset(float minVal, float maxVal)
	{
		return roughness*(rand.nextFloat()*(maxVal-minVal)+minVal);
}
	
	/*public static void rect(Graphics2D g) {
		
	}*/
	
	public static void draw(Graphics2D g, Shape shape) {
			draw(g, shape, -1,2);
	}
	
	public static void draw(Graphics2D g, Shape shape, int seed) {
		draw(g,shape,seed,2);
	}
		
	public static void draw(Graphics2D g, Shape shape, int seed, int iterations) {
		if (seed>0) {
			rand.setSeed(seed);
		}
		for (int i=0; i<iterations;i++)
			_draw(g,shape);
	}
	
	private static void _draw(Graphics2D g, Shape shape) {
		
		boolean COHERENCE = false;
		
		float roughness_factor = 1;
		
		if (shape instanceof Ellipse2D) {
			COHERENCE = true;
			roughness_factor = .7f;
		} else {
			roughness_factor = 1;
		}
		
		float[] coords = new float[6];
		float[] coords_prev = new float[6];
		
		PathIterator pi = shape.getPathIterator(null);
		
		float[] start = null; 
		//pi.currentSegment(start);
		
		int cubcount = 0;
		
		int i=0;
		while (!pi.isDone()) {
			int type = pi.currentSegment(coords); pi.next();
		
			System.out.println("Path Segment" + type+" ");
			
			//line(coords[0]);
		//	System.out.println(coords[0]+","+coords[1]+","+coords[2]+","+coords[3]+ " type: "+type);
			if (type == PathIterator.SEG_MOVETO)
			{
				coords_prev[0] = coords[0]+(rand.nextInt(10)-5)*roughness_factor;
				coords_prev[1] = coords[1]+(rand.nextInt(10)-5)*roughness_factor;
				
				if (start==null) {
					start = new float[6];
					for (int j=0; j < 6;j++) start[j] = coords[j];
					start[0] = coords_prev[0];
					start[1] = coords_prev[1];
				}
				
				
			} else if (type == PathIterator.SEG_QUADTO) {
				
				line(g, coords_prev[0],coords_prev[1],coords[2],coords[3]);
				
				coords_prev[0] = coords[0];
				coords_prev[1] = coords[1];
				
				
			} else if (type == PathIterator.SEG_CUBICTO) {
				
				cubcount++;
				
				//line(g, coords_prev[0],coords_prev[1],coords[4],coords[5]);
				float coords_prev_x = coords_prev[0];
				float coords_prev_y = coords_prev[1];
				
				if (COHERENCE) {
					
				} else {
					coords_prev_x = coords_prev_x + (rand.nextInt(10)-5)*roughness_factor;
					coords_prev_y = coords_prev_y + (rand.nextInt(10)-5)*roughness_factor;
				}
				
				/*float coords_to_x = coords_prev[0] + rand.nextInt(10)-5;
				float coords_to_y = coords_prev[1] + rand.nextInt(10)-5;
				*/

				
				if (!(COHERENCE && cubcount==4)) {
					coords[4] = coords[4] + (rand.nextInt(10)-5)*roughness_factor;
					coords[5] = coords[5] + (rand.nextInt(10)-5)*roughness_factor;
				} else {
					coords[4] = start[0];
					coords[5] = start[1];
				}
				
				
				GeneralPath gp = new GeneralPath();
				gp.moveTo(coords_prev_x, coords_prev_y);
				gp.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);
				
				g.draw(gp);
				
				/* gp = new GeneralPath();
				gp.moveTo(coords_prev[0], coords_prev[1]);
				gp.curveTo(coords[0],coords[1],coords[2],coords[3],coords[4],coords[5]);
				*/
				g.draw(gp);
				
				coords_prev[0] = coords[4];
				coords_prev[1] = coords[5];
				
			} else if (type == PathIterator.SEG_CLOSE) {
				if (shape.getClass()==Polygon.class)
				line(g, coords[0], coords[1], start[0], start[1]);
			}
			else {
			//if (i>0)
				line(g, coords_prev[0],coords_prev[1],coords[0],coords[1]);
				
				coords_prev[0] = coords[0];
				coords_prev[1] = coords[1];
			}
			
			
			 i++;
			
			/*coords_prev[0] = coords[0];
			coords_prev[1] = coords[1];*/
			/*coords_prev[2] = coords[2];
			coords_prev[3] = coords[3];
			coords_prev[4] = coords[4];
			coords_prev[5] = coords[5];*/
		}
	}

	public static void line(Graphics2D g, float x1, float y1, float x2, float y2) {
		
		float maxOffset = 100;

		
		float lenSq = (x1-x2)*(x1-x2) + (y1-y2)*(y1-y2);
		float offset = maxOffset;

		if (maxOffset*maxOffset*100 > lenSq)
		{
			offset = (float)Math.sqrt(lenSq)/10;
		}
		
		float halfOffset = offset/2;
		float divergePoint = 0.2f + rand.nextFloat()*0.2f;
		
		// This is the midpoint displacement value to give slightly bowed lines.
					float midDispX = bowing*maxOffset*(y2-y1)/200;
					float midDispY = bowing*maxOffset*(x1-x2)/200;

					midDispX = getOffset(-midDispX,midDispX);
					midDispY = getOffset(-midDispY,midDispY);

					
					
					//for (int i=0; i < 2; i++) {
					
					GeneralPath gp = new GeneralPath();
						
					// start
					gp.moveTo(     x1 + getOffset(-offset,offset), y1 +getOffset(-offset,offset));
				/*	
					gp.lineTo(midDispX+x1+(x2 -x1)*divergePoint + getOffset(-offset,offset), 
							midDispY+y1 + (y2-y1)*divergePoint +getOffset(-offset,offset));
					gp.lineTo(midDispX+x1+2*(x2-x1)*divergePoint + getOffset(-offset,offset), 
							midDispY+y1+ 2*(y2-y1)*divergePoint +getOffset(-offset,offset)); 
					// end
					gp.lineTo(x2 + getOffset(-offset,offset), 
							y2 +getOffset(-offset,offset));
					*/
					gp.quadTo(midDispX+x1+(x2 -x1)*divergePoint + getOffset(-offset,offset), 
							midDispY+y1 + (y2-y1)*divergePoint +getOffset(-offset,offset),
							x2 + getOffset(-offset,offset), 
							y2 +getOffset(-offset,offset)
					);
					
					g.draw(gp);
					
					//}
					
					/*GeneralPath gp2 = new GeneralPath();
					
					gp2.moveTo(x1 + getOffset(-halfOffset,halfOffset), y1 +getOffset(-halfOffset,halfOffset));
					gp.lineTo(midDispX+x1+(x2 -x1)*divergePoint + getOffset(-halfOffset,halfOffset), midDispY+y1 + (y2-y1)*divergePoint +getOffset(-halfOffset,halfOffset));
					gp.lineTo(midDispX+x1+2*(x2-x1)*divergePoint + getOffset(-halfOffset,halfOffset), midDispY+y1+ 2*(y2-y1)*divergePoint +getOffset(-halfOffset,halfOffset)); 
					gp.lineTo(x2 + getOffset(-halfOffset,halfOffset), y2 +getOffset(-halfOffset,halfOffset));
					
					g.draw(gp2);*/
		
	}
	
	public static Paint createHatchedPaint(Color color, int checkerSize, Rectangle2D anchorRect, int seed) {
		double density = 2;//1.5;
		int wd=8/2;
		int ydiff = 40;
		
		double w=anchorRect.getWidth();
		double h=anchorRect.getHeight();

		if (seed>=0)
			rand.setSeed(seed);
		
		BufferedImage bufferedImage = new BufferedImage((int)w, (int)h, BufferedImage.TYPE_INT_ARGB);

		Graphics2D g2 = bufferedImage.createGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, // Anti-alias!
				RenderingHints.VALUE_ANTIALIAS_ON);

		double virtualh = h+ydiff;
	
		int numLines=(int)(virtualh/density);
		
		g2.setStroke(new BasicStroke(1));
		g2.setColor(color);
		
		for (int i=0; i < numLines; i++) {
			double y = density*i-ydiff;
			double y2 = y + rand.nextGaussian()*(2*wd+1)-wd+ydiff;

			g2.drawLine(0, (int)y, (int)w, (int)y2);
		}

		
		
		
		// paint with the texturing brush
//		Rectangle2D rect = new Rectangle2D.Double(0, 0, 2 * s, 2 * s);
		return new TexturePaint(bufferedImage, anchorRect);
		//return(new TexturePaint((BufferedImage)bufferedImage.getScaledInstance(200, 300, 0),anchorRect));

	}
	
	public static void main(String[] args) {
		
		Rough rr = new Rough();
		
		
		
	}
}
