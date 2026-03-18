package gui.graph.decorators;

import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;


public class BoxDecorator extends ShapeDecorator {

	@Override
	public int getX() {
	
		return x;
	}

	@Override
	public int getY() {

		return y;
	}

	@Override
	public void setX(int x) {
		this.x=x;
	}

	@Override
	public void setY(int y) {
		this.y=y;
	}

	
	public BoxDecorator(int w, int h) {
		super( new Rectangle2D.Double(0,0, w, h));
	}
	
	@Override
	public String toXML() {
		return("<rectangleDecorator x="+getX()+" y="+getY()+" width="+getWidth()+" height="+getHeight()+"color="+getColor().toString()+"></rectangleDecorator>");
	}




}
