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
package bayes.priors;

public class GaussianPrior implements Distribution {

	private double var;
	private double mu;

	public GaussianPrior(double mu, double var) {
		this.mu = mu;
		this.var = var;
	}
	
	public double getDensity(double x) {
		return( 1.0/(Math.sqrt(2*Math.PI*var))*Math.exp(-(x-mu)*(x-mu)/(2*var)) );
	}
	
	public double getStartingValue() {
		return( mu );
	}
	
	public String getName() {
		return("Gaussian");
	}
}
