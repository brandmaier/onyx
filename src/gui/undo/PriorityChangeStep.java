/*
* Copyright 2023 by Timo von Oertzen and Andreas M. Brandmaier
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
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
