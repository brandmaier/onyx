package bayes.engine;

import bayes.BayesianSEM;
import bayes.Chain;
import bayes.sampler.MetropolisHastings;
import engine.ModelRun;
import  engine.ModelRunUnit;
import engine.OnyxModel;
import engine.ModelRunUnit.Objective;
import engine.backend.Model;

public class BayesianModelRunUnit extends ModelRunUnit {

	MetropolisHastings sampler;
	
	int chainLength = 200;
	Chain chain;
	
	BayesianSEM bsem;
	
	
	public BayesianModelRunUnit(ModelRunUnit toCopy) {
		super(toCopy);
	}

	 public BayesianModelRunUnit(double[] starting, String name, String[] parameterNames, String[] variableNames, ModelRun modelRun) {
		 super(starting,name,parameterNames, variableNames,null, 1000, false, modelRun);
		 
		 super.name = "MCMC";
	}

	public void initEstimation(Model model) {
		 System.out.println("Initializing Bayesian SEM with "+model.getAnzPar()+" parameters");
		 bsem = new BayesianSEM((OnyxModel) model);
		 
	     
	     sampler = new MetropolisHastings(bsem);
	     
	     sampler.width=1;
	     
	     chain = new Chain( chainLength, model.getParameterNames() );
	 }
	
    public double[] getParameterValues() {
    	double[] erg = new double[bsem.getNumParameters()];
    	for (int i=0; i < erg.length; i++) erg[i] = chain.getMean(i);
    	return(erg);
    }
    
    public double getParameterValue(String parameterName) {return getParameterValue(parameterName, false);}
    
    public double getParameterValue(String parameterName, boolean fromStarting) {
        /*for (int i=0; i<parameterNames.length; i++) if (parameterNames[i].equals(parameterName)) 
        	if (fromStarting) return starting[i]; else return getParameterValues()[i];
        return Model.MISSING;
        */
    	for (int i=0; i<parameterNames.length; i++) if (parameterNames[i].equals(parameterName)) {
    		
    		return getParameterValues()[i];
    	}
    	
    	return(-123);
    }
	
    public Chain getChain() {
		return chain;
	}

	public void setChain(Chain chain) {
		this.chain = chain;
	}

	public MetropolisHastings getSampler() {
		return sampler;
	}

	public int getChainLength() {
		return chainLength;
	}

	public boolean isConverged() 
    {
    	return (chain.isFull());
    }
    
    public boolean hasWarning() {
    	return(false);			// overrides isHessianNonPositiveDefinite() checks;
    }
	 
//    public boolean performStep(Model model) {
    public boolean performStep(Model model) {
		//	 System.out.println("Running the chain!");
		 
		 if (!isConverged()) {
			 chain.run(sampler);
			if (chain.getPointer()%10==0)
				System.out.println("Chain sample"+ chain.getPointer()); 
			 
		 } else {
			 
		 }
		 
		 return(isConverged());
	    	
	 }
    
    public double getMinusTwoLogLikelihood() {
    	return Double.NaN;
    }
    
    public double[][] getParameterCovariance() {
    	return(null);
    }
    
    public int getStepsAtConvergence() {
    	return(chain.size());
    }
    
    public Objective getObjective() {
    	return(Objective.BAYESIAN);
    }
}
