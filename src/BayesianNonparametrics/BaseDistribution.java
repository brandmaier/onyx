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

import java.util.Arrays;

/**
 * Abstract class representing the basic interface and bookkeeping mechanisms for managing
 * a base distribution of a nonparametric Bayesian model.
 * 
 * @author Thomas J. Glassen
 */

public abstract class BaseDistribution {	
	
	private int[] partition;
	private int[] clusterSizes;
	private int numOfClusters;
	private int firstEmptyClusterIndex;
	
	protected void initPartition(int numOfObservations){
		partition = new int[numOfObservations];
		clusterSizes = new int[numOfObservations+1];
		Arrays.fill(partition, -1);
		numOfClusters = 0;
		firstEmptyClusterIndex = 0;
	}
	
	public void setClusterIndexOfObservation(int clusterIndex, int observationIndex) {
		if(partition[observationIndex] != -1) {
			if(clusterSizes[partition[observationIndex]]-- == 1) {
				numOfClusters--;
				if(partition[observationIndex] < firstEmptyClusterIndex)
					firstEmptyClusterIndex = partition[observationIndex];
			}
		}
		if(clusterIndex != -1) {
			if(clusterSizes[clusterIndex]++ == 0){
				numOfClusters++;
				if(clusterIndex == firstEmptyClusterIndex)
					saveFirstEmptyClusterIndex(clusterIndex + 1);
			}
		}
		partition[observationIndex] = clusterIndex;
	}
	
	private int[] getNSpecificIndices(int[] arr, int N, int conditionValue, boolean parity) {
		int[] indices = new int[N];
		int counter = 0;
		for(int i=0;i<arr.length;i++) {
			if(parity && arr[i] == conditionValue)
					indices[counter++] = i;
			else if(!parity && arr[i] != conditionValue)
					indices[counter++] = i;
			
			if(counter == N)
				break;
		}
		return indices;
	}
	
	private void saveFirstEmptyClusterIndex(int startFrom) {
		int i = startFrom;
		while(clusterSizes[i] > 0)
			i++;
		firstEmptyClusterIndex = i;
	}
	
	public int getFirstEmptyClusterIndex(){
		return firstEmptyClusterIndex;
	}
	
	protected int[] getAllObservationIndicesOfCluster(int clusterIndex) {
		return getNSpecificIndices(partition, clusterSizes[clusterIndex],clusterIndex,true);
	}
	
	public int[] getAllClusterIndices(){
		return getNSpecificIndices(clusterSizes, numOfClusters,0,false);	
	}
	
	public int getClusterIndexOfObservation(int observationIndex) {
		return partition[observationIndex];
	}
	
	public int[] getPartition(){
		return partition;
	}
	
	public int getClusterSize(int clusterIndex) {
		return clusterSizes[clusterIndex];
	}
	
	public int getNumOfObservations(){
		return partition.length;
	}

	public int getNumOfClusters(){
		return numOfClusters;
	}
		
	public void removeObservationFromCluster(int observationIndex){
		setNewClusterForObservation(-1,observationIndex);
	}
	
	public void addObservationToCluster(int clusterIndex, int observationIndex){		
		setNewClusterForObservation(clusterIndex,observationIndex);
	}
	
	public void createClusterForObservation(int observationIndex){
		setNewClusterForObservation(getFirstEmptyClusterIndex(),observationIndex);
	}
		
	public double getEmptyClusterLikelihood(int observationIndex){
		return getClusterLikelihoods(observationIndex, new int[] {getFirstEmptyClusterIndex()})[0];
	}
	
	protected abstract void setNewClusterForObservation(int newClusterIndex, int observationIndex);
	public abstract double[] getClusterLikelihoods(int observationIndex, int[] clusterIndices);
}
