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
package BayesianNonparametrics;

/**
 * Represents a multivariate normal base distribution with normal-inverse-Wishart prior on its parameters.
 * The likelihood of a draw from the posterior base distribution is calculated via the multivariate t distribution.
 * 
 * @author Thomas J. Glassen
 */

public class DependentNormalsBase extends BaseDistribution{

	private double[][] observations;	//[observation][dimension-value]
	
	private int nDims;					//number of dimensions
	private int maxExpectedNumOfClusters;
	
	//cluster prior information
	private double[] priorMu;			//[dimension]
	private double[][] priorPsi;		//[dimension][dimension]
	private double priorKappa;
	private double priorNu;
	private double[][] priorInvSigma;	//[dimension][dimension]
	private double priorLLSum;
	
	//cluster posterior information
	private double[][] postMu;			//[cluster][dimension]
	private double[] postKappa;			//[cluster]
	private double[] postNu;			//[cluster]	
	private double[][][] postInvSigma;	//[cluster][dim][dim]
	private double[][] dimMeans; 		//[cluster][dim]
	private double[] postLLSum;			//[cluster]
	
	//variables for temporary computations
	private double[][] postPsiStar;		//[dim][dim]
	private Matrix m;
	
	public DependentNormalsBase(double[] priorMu, double[][] priorPsi, double priorKappa, double priorNu, double[][] observations) {
		
		this.priorMu = priorMu; this.priorPsi = priorPsi; this.priorKappa = priorKappa; this.priorNu = priorNu;
		
		maxExpectedNumOfClusters = (int) Math.ceil(Math.log(observations.length));
		
		nDims = priorMu.length;
		postMu = new double[maxExpectedNumOfClusters][nDims]; postNu = new double[maxExpectedNumOfClusters]; postKappa = postNu.clone();
		postInvSigma = new double[maxExpectedNumOfClusters][nDims][nDims]; postPsiStar = new double[nDims][nDims];
		dimMeans = new double[maxExpectedNumOfClusters][nDims]; postLLSum = new double[maxExpectedNumOfClusters];
				
		//precalculate the inverse of the prepared Psi = the parameter Sigma for the multivariate-t-distribution
		//and the part of the log likelihood sum which is independent of the predicted x.
		m = new Matrix(priorPsi);
		m.multiplyWith( (priorKappa + 1) / ( priorKappa * (priorNu - nDims + 1) ));
		priorInvSigma = m.inv();
		priorLLSum = logGamma((priorNu + nDims) / 2) - (logGamma(priorNu / 2) + Math.log(priorNu * Math.PI) * nDims/2 + Math.log(Math.abs(m.det())) * 0.5);
				
		setObservations(observations);
	}
	
	public void setObservations(double[][] observations){		
		for(int cluster = 0; cluster < maxExpectedNumOfClusters; cluster++)
			initClusterInformation(cluster);		
		initPartition(observations.length);
		this.observations = observations;
	}
	
	private void initClusterInformation(int cluster) {
		postNu[cluster] = priorNu; postKappa[cluster] = priorKappa; postLLSum[cluster] = priorLLSum;
		for(int i = 0; i < nDims; i++) {
			postMu[cluster][i] = priorMu[i]; 
			dimMeans[cluster][i] = 0;
			for(int j = 0; j < nDims; j++)
				postInvSigma[cluster][i][j] = priorInvSigma[i][j];
		}
	}
	
	/*
	 * Increases the size of the arrays, if more clusters are needed
	 */
	private void increaseSizeOfArrays() {		
		int oldSize = postNu.length;
		maxExpectedNumOfClusters = Math.min(oldSize * 2, observations.length + 1);
				
		double[][] newPostMu = new double[maxExpectedNumOfClusters][nDims], newDimMeans = new double[maxExpectedNumOfClusters][nDims];
		double[] newPostNu = new double[maxExpectedNumOfClusters], newPostKappa = newPostNu.clone(), newPostLLSum = newPostNu.clone();
		double[][][] newPostInvSigma = new double[maxExpectedNumOfClusters][nDims][nDims];
			
		for(int i=0;i < oldSize;i++ ) {
			newPostNu[i] = postNu[i];
			newPostKappa[i] = postKappa[i];
			newPostLLSum[i] = postLLSum[i];						
			for(int j=0;j < nDims;j++ ) {
				newPostMu[i][j] = postMu[i][j];
				newDimMeans[i][j] = dimMeans[i][j];
				for(int k=0;k < nDims;k++)
					newPostInvSigma[i][j][k] = postInvSigma[i][j][k];
			}
		}		
		postNu = newPostNu;
		postKappa = newPostKappa;
		postLLSum = newPostLLSum;
		postMu = newPostMu;
		dimMeans = newDimMeans;
		postInvSigma = newPostInvSigma;
		
		for(int i=oldSize; i < maxExpectedNumOfClusters; i++) {
			initClusterInformation(i);
		}
	}
	
	/*
	 * Updates the posterior cluster information according 
	 * to the case when the likelihood is multivariate normal and the prior distribution is a normal-inverse-Wishart:
	 * https://en.wikipedia.org/wiki/Conjugate_prior#Continuous_distributions 
	 */
	protected void setNewClusterForObservation(int newClusterIndex, int observationIndex){
		int oldClusterIndex = getClusterIndexOfObservation(observationIndex);
		if (newClusterIndex == oldClusterIndex)
			return;
		else if(newClusterIndex > -1 && oldClusterIndex > -1)
			setNewClusterForObservation(-1, observationIndex);
		
		//check if we have to increase the size of the arrays first
		if(newClusterIndex == maxExpectedNumOfClusters - 1)
			increaseSizeOfArrays();
		
		//set new cluster index
		setClusterIndexOfObservation(newClusterIndex,observationIndex);
		
		//now we have to update the posterior information of the old or the new cluster ...
		
		int index = (newClusterIndex == -1 ? oldClusterIndex : newClusterIndex);
		int[] observationIndicesOfCluster = getAllObservationIndicesOfCluster(index);
		int n = observationIndicesOfCluster.length; 
				
		if(n > 0) {			
			postKappa[index] = priorKappa + n;																			//update posterior kappa
			postNu[index] = priorNu + n;																				//update posterior nu
			
			for(int dim=0;dim<nDims;dim++) {
				//update dimension mean
				if(newClusterIndex == -1)
					dimMeans[index][dim] = (dimMeans[index][dim] * (n + 1) - observations[observationIndex][dim]) / n;					
				else
					dimMeans[index][dim] = (dimMeans[index][dim] * (n - 1) + observations[observationIndex][dim]) / n;		
				postMu[index][dim] = (priorKappa * priorMu[dim] + n * dimMeans[index][dim]) / (priorKappa + n); 		//update posterior mu
			}
		
			//update posterior Psi and multiply Matrix with scalar		
			for(int i=0;i<nDims;i++){
				for(int j=0;j<nDims;j++) {				
					postPsiStar[i][j] = priorPsi[i][j] + (priorKappa * n) / (priorKappa + n) * (dimMeans[index][i] - priorMu[i]) * (dimMeans[index][j] - priorMu[j]);
					for(int k = 0; k < n;k++) {
						double[] obs = observations[observationIndicesOfCluster[k]];
						postPsiStar[i][j] += (obs[i] - dimMeans[index][i]) * (obs[j] - dimMeans[index][j]);
					}
					postPsiStar[i][j] *= (postKappa[index] + 1) / ( postKappa[index] * (postNu[index] - nDims + 1) );
				}
			}
			//precalculate determinant and inverse of the prepared Psi = the parameter Sigma for the multivariate-t-distribution
			double tempDetSigma = 0;
			if(nDims == 2) { //if we have only 2 dims then calculate determinant and inverse directly
				tempDetSigma = postPsiStar[0][0] * postPsiStar[1][1] - postPsiStar[0][1] * postPsiStar[1][0];
				double factor = 1 / tempDetSigma;
				postInvSigma[index][0][0] = factor * postPsiStar[1][1];
				postInvSigma[index][1][0] = -1 * factor * postPsiStar[1][0];
				postInvSigma[index][0][1] = -1 * factor * postPsiStar[0][1];
				postInvSigma[index][1][1] = factor * postPsiStar[0][0];
			}else {
				m.setTo(postPsiStar);
				tempDetSigma = m.det();
				postInvSigma[index] = m.inv();
			}
			//prepare the part of the log likelihood sum which is independent of the predicted x.
			//If we do this here, we have to do it only once for an unchanged cluster
			postLLSum[index] = logGamma((postNu[index] + nDims) / 2) - (logGamma(postNu[index] / 2) + Math.log(postNu[index] * Math.PI) * nDims/2 + Math.log(Math.abs(tempDetSigma)) * 0.5);
		}else
			initClusterInformation(index);
	}
	
	/*
	 * Calculates the likelihood for every specified cluster by
	 * computing the density of x in the corresponding posterior predictive distribution
	 * according to the case when the likelihood is multivariate normal and the prior distribution is a normal-inverse-Wishart
	 * https://en.wikipedia.org/wiki/Conjugate_prior#Continuous_distributions
	 */
	public double[] getClusterLikelihoods(int observationIndex, int[] clusterIndices) {
		double[] likelihoods = new double[clusterIndices.length];
		for(int cluster = 0;cluster<clusterIndices.length;cluster++){
			int i = clusterIndices[cluster];		
			likelihoods[cluster] = densityInMultivariateTDistibution(observations[observationIndex], postNu[i] - nDims + 1, postMu[i], 
										postLLSum[i], postInvSigma[i]);
		}
		return likelihoods;
	}
	
	/*
	 * this function is from Robert Sedgewick and Kevin Wayne (2011)
	 * published at http://introcs.cs.princeton.edu/java/91float/Gamma.java.html
	 */
	private double logGamma(double x) {
		double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
		double ser = 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
	                     + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
	                     +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5);
		return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
	}

	/*
	 * This function computes the density at the position x under the multivariate
	 * t-distribution according to:
	 * https://en.wikipedia.org/wiki/Multivariate_t-distribution
	 */		
	private double densityInMultivariateTDistibution(double[] x, double nu, double[] mu, double lLSum, double[][] invSigma) {
		return Math.exp(lLSum + Math.log(1 + multipyDevVectorTMatrixAndDevVector(x,mu, invSigma) / nu) * (nu + nDims) / -2);
	}
	
	/*
	 * Computes (x - mu)^T * Sigma^{-1} * (x - mu)
	 */
	private double multipyDevVectorTMatrixAndDevVector(double[] v1, double[] v2, double[][] m) {
		double r2 = 0;
		for(int i = 0; i < v1.length;i++) {
			double r1 = 0;
			for(int j = 0; j < v1.length;j++)
				r1 += (v1[j] - v2[j]) * m[j][i];
			r2 += r1 * (v1[i] - v2[i]);
		}
		return r2;
	}
}
