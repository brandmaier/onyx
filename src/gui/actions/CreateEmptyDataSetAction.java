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
package gui.actions;

import gui.Desktop;
import gui.views.DataView;


import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;


public class CreateEmptyDataSetAction extends AbstractAction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4795323982037927347L;
	Desktop desktop;
	
	public CreateEmptyDataSetAction(Desktop desktop) {
		super();
		this.desktop = desktop;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		DataView view = new DataView(desktop);
		desktop.add(view);
		
	}

}
