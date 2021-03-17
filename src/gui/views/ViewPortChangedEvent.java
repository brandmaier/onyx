package gui.views;

public class ViewPortChangedEvent {

	int relx, rely;
	
	public ViewPortChangedEvent(int relx, int rely)
	{
		this.relx = relx;
		this.rely = rely;
	}
	
}
