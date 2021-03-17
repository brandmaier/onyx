package gui.undo;

import gui.graph.Graph;
import gui.graph.Node;
import gui.views.ModelView;

/**
 * 
 * NodePositionMultiStep combines a set of Undo-Operations
 * into a single undo step that resets all node positions
 * of a graph to the previous position.
 * 
 * @author andreas
 *
 */
public class NodePositionMultiStep extends MultiStep {

	private ModelView modelView;
	private Graph graph;

	public NodePositionMultiStep(Graph graph)
	{
		this.graph = graph;
		
		modelView = graph.getParentView();
		for (Node node : graph.getNodes())
		{
			MovedStep step = new MovedStep(modelView, node);
			this.add(step);
		}
	}
	
	public void redo() {
		super.redo();
		if (modelView != null) {
			
			graph.invalidate();
			graph.validate();
			
			modelView.redraw();
		
		
		
		}
	}
	
	public void undo() {
		
		super.undo();
		
		if (modelView != null) {
			
			graph.invalidate();
			graph.validate();
			
			modelView.redraw();
		
		
		
		}
		
	}
	
}
