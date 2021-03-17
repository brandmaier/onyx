package gui.undo;

import gui.graph.Node;
import gui.views.ModelView;

public class NodeStateChangedStep extends UndoStep {

	private Node oldNode, newNode;
	private ModelView mv;
	
	public Node getPreviousNodeState()
	{
		return oldNode;
	}
	
	public Node getCurrentNodeState()
	{
		return newNode;
	}

	public NodeStateChangedStep(ModelView mv, gui.graph.Node node)
	{
		super();
		this.title = "Change node";
		this.mv = mv;
		this.oldNode = (Node)node.clone();
		this.newNode = node;
	}
	
	public void undo()
	{
		// little workaround TODO
		oldNode.setSelected(false);
		
		mv.getModelRequestInterface().requestRemoveNode(newNode);
		mv.getModelRequestInterface().requestAddNode(oldNode);
		mv.repaint();
	}
	
	public void redo()
	{
		mv.getModelRequestInterface().requestRemoveNode(oldNode);
		mv.getModelRequestInterface().requestAddNode(newNode);
		
		mv.repaint();
	}
	
}
