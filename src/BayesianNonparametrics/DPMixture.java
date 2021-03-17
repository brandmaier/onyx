package BayesianNonparametrics;

import java.util.SplittableRandom;

/**
 * A Dirichlet Process mixture model described by a base distribution H and a concentration parameter alpha.
 * 
 * @author Thomas J. Glassen
 */

public class DPMixture extends MixtureModel{
	
	private double alpha;
	private SplittableRandom r;
	
	public DPMixture(BaseDistribution H, double alpha){
		
		assert alpha > 0;
		
		this.H = H;
		this.alpha = alpha;
		
		long seed = System.currentTimeMillis();
		System.out.println("[DP] seed is: " + seed);		
		r = new SplittableRandom(seed);
	}

	protected void iterate(){ 
				
		double[] probabilities = new double[H.getNumOfObservations()];
		
		for(int obs=0;obs<H.getNumOfObservations();obs++){
			
			// First, remove the observation from its cluster ...
			H.removeObservationFromCluster(obs);
			int[] clusterIndices = H.getAllClusterIndices();
			
			// if there was no cluster at all, create a new one with the observation and continue with the next
			if (clusterIndices.length == 0){
				H.createClusterForObservation(obs);
				continue;
			}
			
			double[] clusterLikelihoods = H.getClusterLikelihoods(obs, clusterIndices);
			
			//...then get all cluster probabilities ...
			double probabilitySum = 0;
			for(int j=0;j<clusterIndices.length;j++) {
				probabilities[j] = clusterLikelihoods[j] * H.getClusterSize(clusterIndices[j]);
				probabilitySum += probabilities[j];
			}
			probabilities[clusterIndices.length] = H.getEmptyClusterLikelihood(obs) * alpha;
			probabilitySum += probabilities[clusterIndices.length];			
			
			//... choose a new cluster ...
			double treshold = r.nextDouble() * probabilitySum;
			
			//... and get the index of it ...
			double currentProbability = probabilities[0];
			int k = 0;
			while(currentProbability < treshold)
				currentProbability += probabilities[++k];
			
			//...then assign the new cluster
			if (k < clusterIndices.length)
				H.addObservationToCluster(clusterIndices[k], obs);				
			else
				H.createClusterForObservation(obs);	
			
		}
	}
}
