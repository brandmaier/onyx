package gui.undo;

import java.awt.Color;

import gui.graph.FillColorable;
import gui.graph.LineColorable;
import gui.views.ModelView;

public class FillColorChangeStep extends UndoStep {

	FillColorable colorable;
	Color color;
	ModelView mv;
	private Color redoColor;
	
	public FillColorChangeStep(ModelView modelView, FillColorable colorable)
	{
		color = colorable.getFillColor();
		this.title = "Line color changed ";
		this.colorable = colorable;
		this.mv = modelView;
	}
	
	@Override
	public void undo() {
		redoColor = colorable.getFillColor();
		colorable.setFillColor(color);
		mv.repaint();
	}
	
	public void redo()
	{
		colorable.setFillColor(redoColor);
		mv.repaint();
	}

}
