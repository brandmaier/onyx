package gui.views;

import java.util.List;

import engine.ModelRequestInterface;

public class ModelComparisonView extends View
{
	
	private static final long serialVersionUID = 1L;
	
	List<ModelView> connectedModelViews;
	ModelRequestInterface mri;
	
	public ModelComparisonView(ModelRequestInterface mri)
	{
		this.mri = mri;
		this.selectable = false;
		this.resizable = true;
	}
	
	
	public void connect(ModelView mv)
	{
		//TODO
		connectedModelViews.add(mv);
		update();
	}
	
	public void disconnect(ModelView mv)
	{
		//TODO
		connectedModelViews.remove(mv);
		update();
	}
	
	public void update()
	{
		//TODO: connect to interface here
		this.invalidate();
		this.repaint();
	}
	
}
