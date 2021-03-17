package gui.views;

import gui.Draggable;

public interface ViewListener {

	public void viewMoved(View view);
	public void viewResized(View view);
	public void viewIconified(View view, boolean state);
	
	public void startedDrag(Draggable source);
	public void abortDrag();
}
