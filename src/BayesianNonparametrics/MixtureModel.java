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
