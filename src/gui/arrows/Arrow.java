package gui.arrows;

import java.awt.Graphics2D;


public abstract class Arrow {

	protected int x1,y1,x2,y2;
	
	
	
	public Arrow(int x1, int y1, int x2, int y2) {
		super();
		this.x1 = x1;
		this.y1 = y1;
		this.x2 = x2;
		this.y2 = y2;
	}



	public abstract void draw(Graphics2D g);
	
}


