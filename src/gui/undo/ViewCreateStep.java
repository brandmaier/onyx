package gui.undo;


import gui.Desktop;
import gui.views.View;

public class ViewCreateStep extends UndoStep {

	private View view;
	//private Desktop desktop;

	public ViewCreateStep( View view)
	{
		super();
		this.title = "Create view "+view.getName();
		this.view = view;
		//this.desktop = desktop;
	}
	
	public void undo()
	{
		this.view.getDesktop().removeView( view );
	}
	
	public void redo()
	{
		this.view.getDesktop().add( view );
	}
	
	
}
