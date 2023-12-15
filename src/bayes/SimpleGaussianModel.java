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
package bayes;

public class SimpleGaussianModel extends BayesianModel {

	public  SimpleGaussianModel() {
		super(2);
	}
	
	@Override
	public double getLogLikelihood(double[][] x, ParameterSet parameterX) {
		double mu = parameterX.getValue(0);
		double var = parameterX.getValue(1);
		
		double ll=0;
		for (int i=0; i < x.length; i++) {
				ll += Math.log( 1.0/(Math.sqrt(2*Math.PI*var))*Math.exp(-(x[i][0]-mu)*(x[i][0]-mu)/(2*var)) ) ; //TODO: translate to sum
		}
		return(ll);
	}
	
	public int getNumParameters()
	{
		return(2);
	}

}
