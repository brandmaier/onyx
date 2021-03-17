package gui.undo;

public abstract class UndoStep {

	public String title;
	
	public abstract void undo();
	public void redo() throws Exception {
		throw new Exception("Not implemented yet!");//TODO needs to be abstract eventually
	}

	public String toString() { return title; }
}
