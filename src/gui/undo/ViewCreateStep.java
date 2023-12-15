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


import gui.Desktop;
import gui.views.View;

public class ViewCreateStep extends UndoStep {

	private View view;
	//private Desktop desktop;

	public ViewCreateStep( View view)
	{
		super();
		this.title = "Create view "+view.getName();
		this.view = view;
		//this.desktop = desktop;
	}
	
	public void undo()
	{
		this.view.getDesktop().removeView( view );
	}
	
	public void redo()
	{
		this.view.getDesktop().add( view );
	}
	
	
}
