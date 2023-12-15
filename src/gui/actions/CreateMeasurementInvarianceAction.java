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

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import gui.Desktop;
import gui.dialogs.MeasurementInvarianceWizard;

public class CreateMeasurementInvarianceAction extends AbstractAction {

	Desktop desktop;
	int x,y;
	
	public CreateMeasurementInvarianceAction(Desktop desktop)
	{
		this.desktop = desktop;
	
		putValue(NAME, "Create a Measurement Invariance Model");
		//putValue(SHORT_DESCRIPTION, "Create an LGCM on the desktop");
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {
		new MeasurementInvarianceWizard(desktop);
	}
	
	public CreateMeasurementInvarianceAction(Desktop desktop, int x, int y)
	{
		this(desktop);
		this.x = x;
		this.y = y;
	}
}
