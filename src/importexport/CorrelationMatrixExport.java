package importexport;

import engine.OnyxModel;
import engine.Statik;
import gui.graph.Graph;
import gui.graph.Node;
import gui.views.ModelView;

import importexport.filters.TextFileFilter;

import java.io.File;

import javax.swing.filechooser.FileFilter;

public class CorrelationMatrixExport extends StringExport {

	public boolean correlation = false; // show covariance or correlation matrix
	public boolean observed= true;	// show observed or full matrix
	
	public CorrelationMatrixExport(ModelView modelView) {
		super(modelView, new TextFileFilter(),new String[] {"txt","dat"});
	}

	public boolean isValid() {return !modelView.hasDefinitionEdges();}
	
	@Override
	public String createModelSpec(ModelView modelView, String modelName, boolean useUniqueNames) {
		OnyxModel model = modelView.getModelRequestInterface().getModel();
		//model.get
		
		int k = model.symVal.length;
		
		if (k==0) return "Empty Model";
		
		double[][] I = new double[k][k];
		for (int i=0; i < k; i++) I[i][i] = 1;
		
		// Statik.invert
		// Statik.transpose
		// Statik.multiply
		double[][] IminAinv = Statik.invert(Statik.subtract(I, model.asyVal));
		
		double[][] IminAinvT = Statik.transpose(IminAinv);
		
		int obs = 0;
		for (Node node : modelView.getGraph().getNodes()) {
			if (node.isObserved()) obs++;
		}
		
		if (obs==0) return "No Observations";
		
		double[][] F = new double[obs][k];
		for (int i=0; i < model.filter.length; i++) {
			int idx = model.filter[i];
			F[i][idx] = 1;
		}
		
		try {
		model.setParameter(modelView.getShowingEstimate());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		double[][] cov;
		if (observed) {
		cov = 
				Statik.multiply(
				Statik.multiply(
				F,
						//Statik.multiply( Statik.multiply( F,
				Statik.multiply( Statik.multiply(IminAinv, model.symVal), IminAinvT)
			//	, Statik.transpose(F));
				),
				Statik.transpose(F))
		;
		} else {
			cov = 				Statik.multiply( Statik.multiply(IminAinv, model.symVal), IminAinvT)
					;
		}
		
		// convert into correlation matrix
		if (correlation) {
		double[] std = new double[cov.length];
		for (int i=0; i < cov.length; i++) std[i] = Math.sqrt(cov[i][i]);
		
		for (int i=0; i < cov.length; i++) {
			for (int j=0; j < cov.length; j++) {
				if (std[i] > 0 && std[j] > 0)
					cov[i][j] /= (std[i]*std[j]);
				else 
					cov[i][j] = Double.NaN;
			}
		}
		}
		
		String result = "";
		
		result+= "Variables: ";
        for (int i=0; i<model.anzFac; i++) result += model.variableNames[i]+(i==model.anzFac-1?"":", ");
		result+="\n";
		
		for (int i=0; i < cov.length; i++) 
		{
			for (int j=0; j < cov.length; j++) {
				result += Statik.doubleNStellen(cov[i][j],4)+"\t";
			}
			result+="\n";
		}
		
		return result;
	}

}
