package gui;

import engine.ModelRequestInterface;
import gui.frames.MainFrame;
import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.views.ModelView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * The clipboard can store edges and nodes
 * and allows a copy/paste functionality within Onyx
 * 
 * @author andreas
 *
 */
public class Clipboard extends ArrayList<Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int minx = Integer.MAX_VALUE;
	int miny = Integer.MAX_VALUE;
	int maxx = Integer.MIN_VALUE;
	int maxy = Integer.MIN_VALUE;
	
	public Clipboard()
	{
	
	}
	
	/**
	 * clear all contents of the clipboard
	 */
	public void clear()
	{
		super.clear();
		minx = Integer.MAX_VALUE;
		miny = Integer.MAX_VALUE;
		maxx = Integer.MIN_VALUE;
		maxy = Integer.MIN_VALUE;
	}
	
	public void copy(Graph graph)
	{
		this.clear();
		
		for (Node node : graph.getNodes())
		{
			if (node.isSelected()) {
				this.add(node);
				minx = Math.min(minx, node.getX());
				miny = Math.min(miny, node.getY());
				maxx = Math.max(maxx, node.getX()+node.getWidth());
				maxy = Math.max(maxy, node.getY()+node.getHeight());
				
			}
		}
		
		for (Edge edge : graph.getEdges())
		{
			//if (edge.source.isSelected() && edge.target.isSelected())
			if (edge.isSelected())
			{	
				this.add(edge);
			}
		}
	}
	
	public void paste(ModelRequestInterface mri)
	{
		this.paste(mri, minx-10, miny-10);
	}

	public void pasteWithinBounds(ModelView modelView, int x, int y, boolean renameParameters)
	{
		
		int shiftX = Math.max(0, x+(maxx-minx) - modelView.getWidth());
		int shiftY = Math.max(0,  y+(maxy-miny) - modelView.getHeight());
		
		x = x - shiftX;
		y = y - shiftY;
		
		if (x < 0) x=0;
		if (y < 0) y=0;
		
		
		this.paste(modelView.getModelRequestInterface(), x, y, renameParameters);
	}
	
	public void paste(ModelRequestInterface mri, int x, int y) {
		paste(mri, x, y, true);
	}
	
	public void paste(ModelRequestInterface mri, int x, int y, boolean renameParameters)
	{
		
		MainFrame.undoStack.startCollectSteps();
		
		int offsetX = -minx+x;
		int offsetY = -miny+y;
		
		HashMap<Node, Node> map = new HashMap<Node, Node>();
		
		for (Object movable : this)
		{
			if (movable instanceof Node) {
				Node node = (Node) (movable);
				Node nodeCopy = (Node) node.clone();
				nodeCopy.setId(-1);
				map.put(node, nodeCopy);
				nodeCopy.setX( nodeCopy.getX()+offsetX);
				nodeCopy.setY( nodeCopy.getY()+offsetY);
				if (!node.isMeanTriangle()) {
					if (renameParameters)
						nodeCopy.setCaption( node.getCaption()+"'");
				}
				
				node.setSelected(false); // deselect source
				nodeCopy.setSelected(true); // select destination
				
				mri.requestAddNode(nodeCopy);
				
				
			} else if (movable instanceof Edge) {
			
				Edge edge = (Edge)movable;
				
				// only copy edges if their adjacent nodes were copied, too
				boolean srcOK = this.contains(edge.getSource());
				boolean trgOK = this.contains(edge.getTarget());
				if (!srcOK || !trgOK) continue;
				

				// copy edge
				Edge edgeCopy = (Edge)edge.clone();
				// map edge to copied nodes
				edgeCopy.source = map.get(edge.source);
				edgeCopy.target = map.get(edge.target);
				edgeCopy.setValue( edge.getValue() ); // TODO: why is this necessary?
				
				
				if (edge.isAutomaticNaming()) {
					edgeCopy.setParameterName(edgeCopy.getAutomaticParameterName());
				} else {
					if (renameParameters) {
						edgeCopy.setParameterNameByUser(edge.getParameterName()+"'");
					} else {
						//edgeCopy.setParameterNameByUser(edge.getParameterName());
					}
				}
				
				try {
				mri.requestAddEdge(edgeCopy);
				
				// adjust starting  parameter value of the copied edge to be the same as the origin
				// without the following statement the starting value of the clone will be the
				// currently showing value of the origin
				mri.requestSetStartingValue(edgeCopy.getParameterName(),mri.getStartingValuesUnit().getParameterValue(edge.getParameterName()));
				
				} catch (Exception e) {
					// something didn't work while copying an edge
					e.printStackTrace();
					
				}
				
				edgeCopy.update();
			} else {
			
				System.err.println("Unknown object in clipboard");
			}
		}
		
		
		MainFrame.undoStack.endCollectSteps();
		
		
	}
	
	
	
}
