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
package bayes.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JToolTip;

import bayes.Chain;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.ui.InteractivePanel;

/**
 * Custom JToolTip class implementing a MCMC sample plot
 * 
 * @author brandmaier
 *
 */
public class ChainToolTip extends JToolTip
{
	
	private static final long serialVersionUID = 4757854723095566075L;
	
	Chain chain;
	
	public ChainToolTip(Chain chain) {
		super();
		setLayout(new BorderLayout());
		
		this.chain = chain;
	
		setParameter(0);
	}
	
	public void setParameter(int j) {
		// int numParams = chain.get(0).size();
		 this.removeAll();

		 
		DataTable data = new DataTable(Double.class, Double.class);
		for (int x =0; x < chain.getNumSamples(); x++) {
		    double y = chain.get(x).getValue(j);
		    data.add((double)x, y);
		}
		
		XYPlot plot = new XYPlot(data);
		
		LineRenderer lines = new DefaultLineRenderer2D();
		plot.setLineRenderers(data, lines);
		
		/*if (parameterNames==null) {
			plot.getTitle().setText("Parameter "+(j+1));
		} else {
			plot.getTitle().setText(parameterNames[j]);
		}
*/
		this.removeAll();
	

		plot.getLineRenderers(data).get(0).setColor(Color.BLUE);
		plot.getPointRenderers(data).get(0).setColor(Color.BLUE);
		
		
		this.add(new InteractivePanel(plot));
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(200,100);
	}

	public void setParameter(String paramName) {
		String[] pm = chain.getParameterNames();
		for (int i=0; i < pm.length; i++) {
			if (pm[i].equals(paramName)) {
				setParameter(i);
				
			}
		}
		
	}
}
