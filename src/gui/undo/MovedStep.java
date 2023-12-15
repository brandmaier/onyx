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

import gui.graph.Movable;
import gui.views.ModelView;
import gui.views.View;

public class MovedStep extends UndoStep {

	View modelView;
	int x,y;
	
	public MovedStep(View modelView, Movable movable) {
		/*super();
		this.title="Moved ";
		this.modelView = modelView;
		this.movable = movable;*/

		
		this(modelView, movable, movable.getX(), movable.getY());
	}
	
	public MovedStep(View modelView, Movable movable, int x, int y) {
		super();
		this.title="Moved node from"+x+","+y;
		this.modelView = modelView;
		this.movable = movable;
		
		this.x = x;
		this.y = y;
	}

	Movable movable;
	private int redoX;
	private int redoY;
	
	@Override
	public void undo() {
		
		redoX = movable.getX();
		redoY = movable.getY();
		
		movable.setX(x);
		movable.setY(y);
		
		modelView.repaint();

	}
	
	public void redo()
	{
		movable.setX(redoX);
		movable.setY(redoY);
		
		modelView.repaint();		
	}

	public String toString()
	{
		return "moved "+movable.toString()+" from "+x+","+y;
	}
}
