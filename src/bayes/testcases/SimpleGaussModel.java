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
package bayes.testcases;

import bayes.Chain;
import bayes.SimpleGaussianModel;
import bayes.gui.ChainPlotWindow;
import bayes.gui.PosteriorPlotWindow;
import bayes.priors.FlatDegeneratePrior;
import bayes.priors.GaussianPrior;
import bayes.sampler.MetropolisHastings;

public class SimpleGaussModel {
	public static void main(String[] args)
	{
		double[][] data = new double[][] { {10},{20} };
		
		SimpleGaussianModel model = new SimpleGaussianModel();
		model.setPrior(0, new GaussianPrior(10, 1000));
//		model.setPrior(1, new GaussianPrior(1, 1000));
		model.setPrior(1, new FlatDegeneratePrior());
		MetropolisHastings sampler = new MetropolisHastings(model, data);
		
		sampler.width = 80;
		
		Chain samples = sampler.run(10000, 5000);
		
		// Show Chain
		for (int i=0; i < samples.getNumSamples(); i++) {
			System.out.println( i+". "+samples.get(i) );
		}
		
		// obtain averages
		System.out.println( "Mean of mu "+samples.getMean(0)+"\n");
		System.out.println( "Mean of var "+samples.getMean(1)+"\n");
		
		//System.out.println(" Rejects: "+ rejectionCounter*100.0/samples.getNumSamples()+"%");
		
		(new ChainPlotWindow(samples)).setVisible(true);;
		(new PosteriorPlotWindow(samples)).setVisible(true);
	}
}
