package gui.graph.decorators;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
//import javax.swing.
import java.awt.Shape;
import java.awt.Stroke;

public class ShapeDecorator implements DecoratorObject {

	Shape shape;
	private Stroke stroke = new BasicStroke(2, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_BEVEL);
	
	public ShapeDecorator(Shape shape) {
		this.shape = shape;
	}

	@Override
	public int getX() {
		return shape.getBounds().x;
	}

	@Override
	public int getY() {
		return shape.getBounds().y;
	}

	@Override
	public void setX(int x) {
		shape.getBounds().setLocation(x, getY());
	}

	@Override
	public void setY(int y) {
		shape.getBounds().setLocation(getX(), y);
		
	}

	@Override
	public int getHeight() {
		return((int)shape.getBounds().getHeight());
	}

	@Override
	public int getWidth() {
		return((int)shape.getBounds().getWidth());
	}

	@Override
	public void setHeight(int height) {
		shape.getBounds().setSize(getWidth(), height);
		
	}

	@Override
	public void setWidth(int width) {
		shape.getBounds().setSize(width, getHeight());
		
	}

	@Override
	public void paint(Graphics g) {
		// TODO Auto-generated method stub
		Graphics2D g2d = ((Graphics2D)g);
		g2d.setStroke(stroke );
		g2d.draw(shape);
	}

	@Override
	public String toXML() {
		return("<rectangleDecorator x="+getX()+" y="+getY()+" width="+getWidth()+" height="+getHeight()+"></rectangleDecorator>");
	}

}
