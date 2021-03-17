package gui.undo;

import gui.graph.Movable;
import gui.graph.Resizable;
import gui.views.ModelView;
import gui.views.View;

public class ReshapeStep extends UndoStep {

	View modelView;
	int x,y,w,h;
	int redoX, redoY, redoW, redoH;
	
	public ReshapeStep(View modelView, Resizable movable) {
		super();
		this.title="Resized ";
		this.modelView = modelView;
		this.movable = movable;
		
		x = movable.getX();
		y = movable.getY();
		w = movable.getWidth();
		h = movable.getHeight();
		
	}
	
	public ReshapeStep(View modelView, Resizable movable, int x, int y, int w, int h) {
		super();
		this.title="Resized ";
		this.modelView = modelView;
		this.movable = movable;
		
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
	}

	Resizable movable;
	
	@Override
	public void undo() {
		
		redoX = movable.getX();
		redoY = movable.getY();
		redoW = movable.getWidth();
		redoH = movable.getHeight();
		
		movable.setX(x);
		movable.setY(y);
		movable.setHeight(h);
		movable.setWidth(w);
		
		modelView.repaint();

	}
	
	public void redo() {
		movable.setX(redoX);
		movable.setY(redoY);
		movable.setHeight(redoH);
		movable.setWidth(redoW);
		
		modelView.repaint();		
	}

}
