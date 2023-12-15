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
import gui.graph.Resizable;
import gui.views.ModelView;
import gui.views.View;

public class ReshapeStep extends UndoStep {

	View modelView;
	int x,y,w,h;
	int redoX, redoY, redoW, redoH;
	
	public ReshapeStep(View modelView, Resizable movable) {
		super();
		this.title="Resized ";
		this.modelView = modelView;
		this.movable = movable;
		
		x = movable.getX();
		y = movable.getY();
		w = movable.getWidth();
		h = movable.getHeight();
		
	}
	
	public ReshapeStep(View modelView, Resizable movable, int x, int y, int w, int h) {
		super();
		this.title="Resized ";
		this.modelView = modelView;
		this.movable = movable;
		
		this.x = x;
		this.y = y;
		this.w = w;
		this.h = h;
		
	}

	Resizable movable;
	
	@Override
	public void undo() {
		
		redoX = movable.getX();
		redoY = movable.getY();
		redoW = movable.getWidth();
		redoH = movable.getHeight();
		
		movable.setX(x);
		movable.setY(y);
		movable.setHeight(h);
		movable.setWidth(w);
		
		modelView.repaint();

	}
	
	public void redo() {
		movable.setX(redoX);
		movable.setY(redoY);
		movable.setHeight(redoH);
		movable.setWidth(redoW);
		
		modelView.repaint();		
	}

}
