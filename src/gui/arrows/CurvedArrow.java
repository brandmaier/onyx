package gui.arrows;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;

public class CurvedArrow extends Arrow {

	public CurvedArrow(int x1, int y1, int x2, int y2) {
		super(x1,y1,x2,y2);
		
	}
	
	
	public void draw(Graphics2D g) {

    float arrowRatio = 0.5f;
    float arrowLength = 80.0f;

    BasicStroke stroke = ( BasicStroke ) g.getStroke();

    float endX = 350.0f;

    float veeX = endX - stroke.getLineWidth() * 0.5f / arrowRatio;

    // vee
    Path2D.Float path = new Path2D.Float();

    float waisting = 0.5f;

    float waistX = endX - arrowLength * 0.5f;
    float waistY = arrowRatio * arrowLength * 0.5f * waisting;
    float arrowWidth = arrowRatio * arrowLength;

    path.moveTo ( veeX - arrowLength, -arrowWidth );
    path.quadTo ( waistX, -waistY, endX, 0.0f );
    path.quadTo ( waistX, waistY, veeX - arrowLength, arrowWidth );

    // end of arrow is pinched in
    path.lineTo ( veeX - arrowLength * 0.75f, 0.0f );
    path.lineTo ( veeX - arrowLength, -arrowWidth );

    g.setColor ( Color.BLUE );
    g.fill ( path );

    // move stem back a bit
    g.setColor ( Color.RED );
    g.draw ( new Line2D.Float ( 50.0f, 0.0f, veeX - arrowLength * 0.5f, 0.0f ) );
	}
	
}
