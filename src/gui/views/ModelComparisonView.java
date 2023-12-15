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
package gui.views;

import java.util.List;

import engine.ModelRequestInterface;

public class ModelComparisonView extends View
{
	
	private static final long serialVersionUID = 1L;
	
	List<ModelView> connectedModelViews;
	ModelRequestInterface mri;
	
	public ModelComparisonView(ModelRequestInterface mri)
	{
		this.mri = mri;
		this.selectable = false;
		this.resizable = true;
	}
	
	
	public void connect(ModelView mv)
	{
		//TODO
		connectedModelViews.add(mv);
		update();
	}
	
	public void disconnect(ModelView mv)
	{
		//TODO
		connectedModelViews.remove(mv);
		update();
	}
	
	public void update()
	{
		//TODO: connect to interface here
		this.invalidate();
		this.repaint();
	}
	
}
