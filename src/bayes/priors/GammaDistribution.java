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

public class GammaDistribution implements Distribution {

	private double scale;
	private double shape;

	public GammaDistribution(double k, double theta) {
		this.shape = k;
		this.scale = theta;
	}

	@Override
	public double getDensity(double x) {
		return(1.0/(ChiSquare.gamma(shape)*Math.pow(scale, shape)) * Math.pow(x, shape-1)*Math.exp(-x/scale));
		
	}
	
	public double getStartingValue() {
		return( scale*shape );
	}
	
	public String getName() {
		return("Gamma");
	}
}
