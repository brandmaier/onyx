package bayes;

import java.util.Vector;

import bayes.priors.Distribution;
import bayes.priors.FlatDegeneratePrior;

public class BayesianModel {

	//private ParameterSet params;
	
	String[] parameterNames;
	
	Distribution[] priors;
	int size;
/*	public void setParameters(ParameterSet params) {
		this.params = params;
	}
	*/
	public BayesianModel(int size) {
		System.out.println("Creating model with "+size+" parameters.");
		if (size <= 0) {
			System.err.println("Error! Model has no parameters!");
			//System.exit(-1);
		}
		this.size = size;
		
		parameterNames = new String[size];
		
		priors = new Distribution[size];
		for (int i=0; i < size; i++) {
			setPrior(i, new FlatDegeneratePrior());
			parameterNames[i] = "Unnamed Parameter "+i;
		}
		
		
	}
	
	public void setPrior(int i, Distribution d)
	{
		priors[i] = d;
	}
	
	public Distribution getPrior(int i) {
		return(priors[i]);
	}

	public double getLogLikelihood(double[][] x, ParameterSet parameterX) {
		// TODO Auto-generated method stub
		return -999;
	}
	
	public double getLogPosterior(double[][] x, ParameterSet parameterX) {
		double result = getLogLikelihood(x, parameterX);
		for (int i=0; i < priors.length; i++) {
			result = result + Math.log( priors[i].getDensity(parameterX.getValue(i)) );
		}
		return(result);
	}
	
	public int getNumParameters()
	{ 
		return(size);
	}
	
	public String[] getParameterNames() {
		return(parameterNames);
	}
	
}
