package gui.graph.decorators;

import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JLabel;

public class LabelDecorator extends JLabel implements DecoratorObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3339446983728175605L;
	
	public LabelDecorator(String label, Font font)
	{
		super(label);
		
		if (font != null)
			this.setFont(font);
		
		
		//this.addMouseListener(this);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
	}

	int x,y,w,h;

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

	
	@Override
	public void setHeight(int height) {
		this.h = height;
		
	}

	@Override
	public void setWidth(int width) {
		this.w = width;
	}
	
	public int  getWidth() { return this.w;}
	public int getHeight() { return this.h;}

	@Override
	public String toXML() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
