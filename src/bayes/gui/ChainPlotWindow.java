package bayes.gui;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

import bayes.Chain;
import de.erichseifert.gral.data.DataTable;
import de.erichseifert.gral.plots.XYPlot;
import de.erichseifert.gral.plots.lines.DefaultLineRenderer2D;
import de.erichseifert.gral.plots.lines.LineRenderer;
import de.erichseifert.gral.ui.InteractivePanel;

public class ChainPlotWindow extends JFrame 
{

	public ChainPlotWindow(Chain chain) {
		this(chain, null);
	}
	
	public ChainPlotWindow(Chain chain, String[] parameterNames)
	{
		 
		 setDefaultCloseOperation(EXIT_ON_CLOSE);
		 setSize(800, 600);
		    
		 int numParams = chain.get(0).size();
		 
		 for (int j=0; j < numParams; j++) {
		  
		DataTable data = new DataTable(Double.class, Double.class);
		for (int x =0; x < chain.getNumSamples(); x++) {
		    double y = chain.get(x).getValue(j);
		    data.add((double)x, (double)y);

		}
		
		XYPlot plot = new XYPlot(data);
		
		LineRenderer lines = new DefaultLineRenderer2D();
		plot.setLineRenderers(data, lines);
		
		if (parameterNames==null) {
			plot.getTitle().setText("Parameter "+(j+1));
		} else {
			plot.getTitle().setText(parameterNames[j]);
		}

	

		plot.getLineRenderers(data).get(0).setColor(Color.BLUE);
		plot.getPointRenderers(data).get(0).setColor(Color.BLUE);
		
		getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		getContentPane().add(new InteractivePanel(plot));
		
		 }
	}
}
