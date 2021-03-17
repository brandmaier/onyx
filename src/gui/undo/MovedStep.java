package gui.undo;

import gui.graph.Movable;
import gui.views.ModelView;
import gui.views.View;

public class MovedStep extends UndoStep {

	View modelView;
	int x,y;
	
	public MovedStep(View modelView, Movable movable) {
		/*super();
		this.title="Moved ";
		this.modelView = modelView;
		this.movable = movable;*/

		
		this(modelView, movable, movable.getX(), movable.getY());
	}
	
	public MovedStep(View modelView, Movable movable, int x, int y) {
		super();
		this.title="Moved node from"+x+","+y;
		this.modelView = modelView;
		this.movable = movable;
		
		this.x = x;
		this.y = y;
	}

	Movable movable;
	private int redoX;
	private int redoY;
	
	@Override
	public void undo() {
		
		redoX = movable.getX();
		redoY = movable.getY();
		
		movable.setX(x);
		movable.setY(y);
		
		modelView.repaint();

	}
	
	public void redo()
	{
		movable.setX(redoX);
		movable.setY(redoY);
		
		modelView.repaint();		
	}

	public String toString()
	{
		return "moved "+movable.toString()+" from "+x+","+y;
	}
}
