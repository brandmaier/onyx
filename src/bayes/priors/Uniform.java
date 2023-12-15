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

public class Uniform implements Distribution {

	private double min;
	private double max;

	public Uniform(double min, double max)
	{
		this.min = min;
		this.max = max;
	}
	
	@Override
	public double getDensity(double x) {
		if (x >= min && x <= max)
			return( 1.0/(max-min) );
		else
			return(0);
	}
	
	public double getStartingValue() {
		return( (max-min)/2);
	}
	
	public String getName() {
		return("Uniform");
	}

}
