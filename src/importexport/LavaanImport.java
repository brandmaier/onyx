package importexport;

import java.util.HashMap;

import javax.swing.JOptionPane;

import engine.ModelRequestInterface;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;

public class LavaanImport extends Import {
	
	ModelView mv = null;
	
	public LavaanImport(ModelView mv)
	{
		this.mv = mv;
	}
	
	/** uses lavaanify()-output as input**/
	public void parse(String text) {
		
		int problemCount = 0;
		
		ModelRequestInterface mri = mv.getModelRequestInterface();
		
		String[] lines = text.split("\n");
		
		for (String line : lines)
		{
			//  id     lhs op     rhs user group free ustart exo label plabel
			String[] tokens = line.split("\t");
			
			final String ops = tokens[2];
			final String left = tokens[1];
			final String right = tokens[3];
			final double start = Double.parseDouble(tokens[7]);
			final String label = tokens[9];
			final String plabel = tokens[10];
			int free = Integer.parseInt(tokens[6]);
			
			HashMap<String, Integer> lookup = new HashMap<String, Integer>();
			
			
				// regression
				Node leftNode;
				if (!mv.getGraph().hasNodeWithCaption(left)) {
					leftNode = new Node(left);
					mri.requestAddNode(leftNode);
					lookup.put(left, leftNode.getId());
				} else {
					leftNode = mv.getGraph().getNodeById(lookup.get(left));
				}
				Node rightNode;
				if (!mv.getGraph().hasNodeWithCaption(right)) {
					rightNode = new Node(right);
					rightNode.setIsLatent(true);
					mri.requestAddNode(rightNode);
					lookup.put(right, rightNode.getId());
				} else {
					rightNode = mv.getGraph().getNodeById(lookup.get(right));
				}
				
			if (ops.equals("=~")) {
				Edge e1 = new Edge(leftNode,rightNode);
				e1.setDoubleHeaded(false);
				e1.setFixed(free==0);
				if (!label.equals(""))
					e1.setParameterName(label);
				mri.requestAddEdge(e1);
				
			} else if (ops.equals("~~")) {
				
				Edge e1 = new Edge(leftNode,rightNode);
				e1.setDoubleHeaded(true);
				e1.setFixed(free==0);
				if (!label.equals(""))
					e1.setParameterName(label);
				mri.requestAddEdge(e1);
				
			} else {
				System.err.println("Unknown operator encountered!");
				problemCount++;
				// Unknown OP
			}
			
		}
		
		
		if (problemCount>0) {
			JOptionPane.showMessageDialog(null, "There were problems during the import.");
		}
	}

}
