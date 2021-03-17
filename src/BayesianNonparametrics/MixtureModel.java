package BayesianNonparametrics;

/**
 * Abstract class representing the basic interface for a nonparametric Bayesian mixture model.
 * 
 * @author Thomas J. Glassen
 */

public abstract class MixtureModel {
	
	protected BaseDistribution H;
	
	public int[][] simulate(int burnIn, int numOfSamples, int thinning){
		int[][] samples = new int[numOfSamples][];
		
		for(int i = 0;i<burnIn;i++)
			iterate();
		
		for(int j = 0;j<numOfSamples;j++){			
			for(int k = 0;k<thinning;k++)
				iterate();
			samples[j] = H.getPartition().clone();
		}
		
		return samples;
	}

	protected abstract void iterate();
	
}
