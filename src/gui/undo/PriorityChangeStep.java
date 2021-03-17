package gui.undo;

import engine.ModelRun.Priority;
import gui.views.ModelView;

public class PriorityChangeStep extends UndoStep {

	private Priority runPriority;
	private ModelView mv;
	private Priority redoRunPriority;

	public PriorityChangeStep(ModelView mv, Priority runPriority) {
		this.runPriority = runPriority;	//TODO: do we need to clone() ?
		this.mv = mv;
		this.title="Change priority of "+mv.getName();
	}

	@Override
	public void undo() {
		redoRunPriority = mv.getModelRequestInterface().getRunPriority();
		mv.getModelRequestInterface().setRunPriority(runPriority);
		
	}
	
	public void redo(){
		mv.getModelRequestInterface().setRunPriority(redoRunPriority);
	}
	

}
