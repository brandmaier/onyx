package gui.undo;

import gui.graph.Node;

public class NodeFontSizeChanged extends UndoStep {

	Node node;
	int fontSize;
	private int redoFontSize;
	
	public NodeFontSizeChanged(Node node, int fontSize)
	{
		this.node = node;
		this.fontSize = fontSize;
	}
	
	@Override
	public void undo() {
		redoFontSize = node.getFontSize();
		node.setFontSize(fontSize);
	}
	
	public void redo()
	{
		node.setFontSize(redoFontSize);
	}
}
