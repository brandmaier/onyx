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

import java.awt.Color;
import java.util.Hashtable;
import javax.swing.JLabel;
import engine.ModelRunUnit.Objective;
import engine.externalRunner.ExternalRunUnit;

public class ContextHelpPanel extends JLabel {

/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	//	String helpText
	public final int CTXT_EMPTY_DESKTOP=0, CTXT_EMPTY_MODEL=1, CTXT_NODE_OVER=2, CTXT_EDGE_OVER=3, CTXT_PARAMETER_VIEW_OVER=4;
	
	public ContextHelpPanel() 
	{
		super();
		this.setBackground(Color.gray);
	
		//setText("JY");
		this.setHelpID(0);
	}
	
	// DEBUG TvO; tests for readability of the external runners and adds that information to the welcome message.
    public static boolean testExternalRunnerSearchNonUnit() {
        try {
            Hashtable<String,ExternalRunUnit> runner = ExternalRunUnit.getValidExternalAgents();
            for (String name : runner.keySet()) {
                ExternalRunUnit r = runner.get(name);
                String agent = ExternalRunUnit.getAgentLabel(r);
                ExternalRunUnit ru = ExternalRunUnit.getInstance(r, new double[]{1,2}, Objective.MAXIMUMLIKELIHOOD, 0.001, "ML "+agent, new String[]{"a","b"}, null, null, 2, "", null);
                if (!ru.getAgentLabel().equals(agent)) return false;
                if (2 != ru.anzPar) return false;
            }
        } catch (Exception e) {return false;}
        return true;
    }
    
	
	public void setHelpID(int id)
	{
		String txt = "";
		switch(id)
		{
		case CTXT_EMPTY_DESKTOP:
		    // DEBUG
            txt = "Welcome to Onyx! Double-click on the desktop to start a new model. Right-click anywhere to access context menus for actions.";
//            txt = "Welcome to Onyx! Left-click on the desktop to start a new model!";
			break;
		case CTXT_EMPTY_MODEL:
			txt = "Double left-click to create a new latent variable. Shift+Double-click to create a new manifest variable";
			break;
		case 2:
			txt = "Left-drag to move model panel on desktop.";
		break;
		case 3:
			txt = "Left click to show estimated parameters";
		break;
		}
		
		setText(txt);
	}

}
