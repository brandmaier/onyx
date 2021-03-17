package bayes;

import engine.OnyxModel;

/**
 * This class links to a engine class
 * 
 * @author brandmaier
 *
 */
public class BayesianSEM extends BayesianModel
{
		OnyxModel onyxModel;
		
		public BayesianSEM(OnyxModel onyxModel) {
			super( onyxModel.getAnzPar() );
			
			// add parameter names from OnyxModel
			String[] pnames = onyxModel.getParameterNames();
			for (int i=0; i < pnames.length; i++)
				this.parameterNames[i] = pnames[i];
			
			this.onyxModel = onyxModel;
		}
	
		public double getLogLikelihood(double[][] x, ParameterSet parameterX) {
			
			onyxModel.setParameter(parameterX.params);
			//onyxModel.setData(x);
			
			//return(onyxModel.getMinusTwoLogLikelihood(parameterX.params,true));
			return(-.5*onyxModel.getMinusTwoLogLikelihood());
			
		}
		
		public String toString() {
			String result= "Onyx model with "+this.getNumParameters()+" parameters:\n";
			for (int i=0; i < this.getNumParameters(); i++)
				result += "  |-- "+this.getParameterNames()[i]+" with prior "+this.getPrior(i).getName()+"\n";
			result+="\n";
			return(result);
		}
	
}
