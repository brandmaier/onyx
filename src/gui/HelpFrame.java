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
package gui;

import javax.swing.JFrame;
import javax.swing.JLabel;

public class HelpFrame extends JFrame
{

	JLabel label;
	
	public HelpFrame()
	{
		String text = "Double-click any empty space on the desktop to create a model panel."+
	"Double-click any empty space on the model panel to create a new latent variable."+
				"Double-click any empty space on the model panel and hold down shift to create an observed variable"+
	"Right-press the mouse on any variable and drag the mouse onto another variable to create a regression path"+
				"Right-press the mouse on any variable and drag the mouse onto another variable while holding down shift to create a (co)variance path.";
		
		
		label = new JLabel(text);
	}
	
}
