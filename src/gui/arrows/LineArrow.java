package gui.arrows;

import java.awt.BasicStroke;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class LineArrow extends Arrow {

	Stroke stroke;
	
	public LineArrow(int x1, int y1, int x2, int y2) {
		super(x1,y1,x2,y2);
	
		// set drawing style
		stroke = new BasicStroke(3, BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_BEVEL);
	}
	
	
	@Override
	public void draw(Graphics2D g) {
		
		g.setStroke(stroke);
		g.drawLine(x1,y1,x2,y2);
		
		

	}

}
