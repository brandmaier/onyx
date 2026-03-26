package gui.graph.decorators;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;


public class BoxDecorator extends ShapeDecorator {


	
	public Color getColor()
	{
		return(Color.black);
	}
	
	public BoxDecorator(int w, int h) {
		super( new Rectangle2D.Double(0,0, w, h));
	}
	
	@Override
	public String toXML() {
		return("<rectangleDecorator x="+getX()+" y="+getY()+" width="+getWidth()+" height="+getHeight()+"color="+getColor().toString()+"></rectangleDecorator>");
	}




}
