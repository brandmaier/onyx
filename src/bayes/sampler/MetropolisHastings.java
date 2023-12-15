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
package bayes.sampler;

import java.util.Random;

import bayes.BayesianModel;
import bayes.Chain;
import bayes.ParameterSet;
import bayes.SimpleGaussianModel;
import bayes.gui.ChainPlotWindow;
import bayes.priors.ChiSquare;
import bayes.priors.GammaDistribution;
import bayes.priors.GaussianPrior;

/**
 * 
 * awesome R example: https://theoreticalecology.wordpress.com/2010/09/17/metropolis-hastings-mcmc-in-r/
 * 
 * @author brandmaier
 *
 */

public class MetropolisHastings implements SamplingAlgorithm {
	
	ParameterSet currentState;
	BayesianModel model;
	double[][] data;
	private boolean verbose = false;
	static int rejectionCounter = 0;
	Random random = new Random();
	
	public double width = 1;
	
	public MetropolisHastings(BayesianModel model) 
	{
		this(model, null); 
	}
	
	public void setVerbose(boolean verbose)
	{
		this.verbose = verbose;
	}
	
	public MetropolisHastings(BayesianModel model, double[][] data) 
	{
		this.model = model;
		this.data = data;
		
		currentState = new ParameterSet(model.getNumParameters());
		
		
		
		// initialize state with prior-based starting value (currently, a non-random value)
		for (int i=0; i < currentState.size(); i++) {
		
			currentState.setValue(i, model.getPrior(i).getStartingValue() );
		}
	}
	
	public double acceptance(double[][] data, ParameterSet proposalParams, ParameterSet currentParams) {

		
		double llcand = model.getLogPosterior(data, proposalParams);
		double llold = model.getLogPosterior(data, currentParams);
		
		if (verbose) System.out.println("Alpha ratio: "+Math.exp(llcand-llold)+ "LLs: "+llcand+" (candidate) VS "+llold+ " (old)");
	
		if (Double.isInfinite(llold) || Double.isNaN(llold)) {
			System.err.println("Warning! State likelihood is infinity or NaN! Accepting candidate!");
			return(1);
		}
		
		if (Double.isNaN(llcand)) {
			return(0);
		}
		
		return(Math.min(1,  Math.exp(llcand-llold)));


	}
	

	public void setSeed(long seed) {
		random.setSeed(seed);
	}
	
	public ParameterSet sample() {
	
		
		
		// sample from uniform
		double U = random.nextDouble(); // Math.log(Math.random());
		
		// sample new state from proposal distribution
		ParameterSet newState = (ParameterSet) currentState.clone();
	
		for (int i=0; i < newState.size(); i++) {
			newState.setValue(i, newState.getValue(i)+width*random.nextGaussian() ); 
		}
		
		if (verbose) {
		System.out.println("Current State "+currentState);
		System.out.println("Proposal State "+newState);
		}
		double a = acceptance(data, newState, currentState);
		
		if (verbose) System.out.println("Acceptance: "+a+" random value "+U+"\n");
		
		if (U < a) {
			if (verbose) System.out.println("ACCEPT");
			currentState = newState;
			return(newState);
		} else {
			if (verbose ) System.out.println("REJECT");
			rejectionCounter = rejectionCounter + 1;
			return(currentState);
		}
	}
	
	public Chain run(int iterations, int burnin) {
		
		for (int i=0; i < burnin; i++) 
			{
				sample();
			}
			
		Chain samples = new Chain(iterations, model.getParameterNames());
		
		for (int i=0; i < iterations; i++) {
			samples.set(i, sample());
		}
		
		return(samples);
	}
	
	
}
