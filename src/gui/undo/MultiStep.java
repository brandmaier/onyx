package gui.undo;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * A multistep combines multiple UndoSteps in a single
 * step of the undo list. This can be used to undo 
 * operations that perform a set of atomic undo steps
 * that should not appear as single steps.
 * For example, changing the graph layout subsumes 
 * changing the position of possibly every single node.
 * Undoing a layout change should appear as a single
 * step resetting all node positions at once.
 * 
 * @author Andreas Brandmaier
 *
 */
public class MultiStep extends UndoStep {

	List<UndoStep> steps;
	boolean valid = true;
	
	public MultiStep()
	{
		steps = new ArrayList<UndoStep>();
	}
	
	public MultiStep(List<UndoStep> steps)
	{
		this.steps = steps;
	}
	
/*	public MultiStep(String string) {
		this();
		super.title = string;
	}*/

	public void add(UndoStep step)
	{
		if (valid == true) {
			steps.add(step);
		} else {
			System.err.println("Multistep was already executed. Cannot add another step: "+step);
		}
		
		super.title="<Multistep: ";
		for (UndoStep nstep : steps) {
			super.title+=nstep.title+"; ";
		}
		super.title+=">";
	}
	
	/**
	 * undo all steps. Last in first out.
	 */
	@Override
	public void undo() {
	
		
		if (steps != null) {
			for (int i=steps.size()-1;i>=0; i--)
			{
				UndoStep undoStep = steps.get(i);
				undoStep.undo();
			}
			valid = false;
		}

	}
	
	public void redo() {
		
		if (steps != null) {
		//	for (int i=steps.size()-1;i>=0; i--)
		for (int i=0; i < steps.size(); i++)
			{
				UndoStep undoStep = steps.get(i);
				try {
					undoStep.redo();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
					System.err.println("Error redoing multistep"+i+"/"+steps.size()+": "+undoStep.title);
				}
			}
			valid = false;
		}		
	}

}
