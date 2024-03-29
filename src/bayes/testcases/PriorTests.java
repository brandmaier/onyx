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

//import static org.junit.Assert.assertEquals;

import bayes.priors.ChiSquare;
import bayes.priors.GammaDistribution;
import bayes.priors.GaussianPrior;
import bayes.priors.Uniform;
//import junit.framework.TestCase;

public class PriorTests {

	public void testPriors() {
		
	/*	GammaDistribution g1 = new GammaDistribution(1, 1);
		
		assertEquals(0.3678794,g1.getDensity(1),0.00001);
		assertEquals(4.539993e-05,g1.getDensity(10),0.00001);
		
		GammaDistribution g2 = new GammaDistribution(1, 0.5);
		
		assertEquals(0.2706706,g2.getDensity(1),0.00001); // in R code: dgamma(1,1,2)
		
		ChiSquare chi = new ChiSquare(3);
		
		assertEquals(0.2075537, chi.getDensity(2),0.0001); // in R code: dchisq(2,3)
		
		GaussianPrior gp = new GaussianPrior(1, 4);
		
		assertEquals( 0.1933341, gp.getDensity(.5),0.0001); // in R code: dnorm(.5, 1,sqrt(4))
		
		GaussianPrior gp2 = new GaussianPrior(0, 1);
		
		assertEquals(6.075883e-09, gp2.getDensity(-6),0.0001); // in R code: dnorm(-6, 0,1)
		
		Uniform u = new Uniform(-1,4);
		
		assertEquals(0.2, u.getDensity(2), 0.0001); // dunif(2, min=-1,max=4)*/
	}
	
	
	
}
