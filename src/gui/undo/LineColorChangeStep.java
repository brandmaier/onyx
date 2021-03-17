package gui.undo;

import java.awt.Color;

import gui.graph.LineColorable;
import gui.views.ModelView;

public class LineColorChangeStep extends UndoStep {

	LineColorable colorable;
	Color color;
	ModelView mv;
	private Color redoColor;
	
	public LineColorChangeStep(ModelView modelView, LineColorable colorable)
	{
		color = colorable.getLineColor();
		this.title = "Line color changed to"+color.toString();
		this.colorable = colorable;
		this.mv = modelView;
	}
	
	@Override
	public void undo() {
		redoColor = colorable.getLineColor();
		colorable.setLineColor(color);
		mv.repaint();
	}
	
	public void redo() {
		colorable.setLineColor(redoColor);
		mv.repaint();
	}

}
