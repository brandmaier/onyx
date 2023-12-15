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

import engine.OnyxModel;

/**
 * This class links to a engine class
 * 
 * @author brandmaier
 *
 */
public class BayesianSEM extends BayesianModel
{
		OnyxModel onyxModel;
		
		public BayesianSEM(OnyxModel onyxModel) {
			super( onyxModel.getAnzPar() );
			
			// add parameter names from OnyxModel
			String[] pnames = onyxModel.getParameterNames();
			for (int i=0; i < pnames.length; i++)
				this.parameterNames[i] = pnames[i];
			
			this.onyxModel = onyxModel;
		}
	
		public double getLogLikelihood(double[][] x, ParameterSet parameterX) {
			
			onyxModel.setParameter(parameterX.params);
			//onyxModel.setData(x);
			
			//return(onyxModel.getMinusTwoLogLikelihood(parameterX.params,true));
			return(-.5*onyxModel.getMinusTwoLogLikelihood());
			
		}
		
		public String toString() {
			String result= "Onyx model with "+this.getNumParameters()+" parameters:\n";
			for (int i=0; i < this.getNumParameters(); i++)
				result += "  |-- "+this.getParameterNames()[i]+" with prior "+this.getPrior(i).getName()+"\n";
			result+="\n";
			return(result);
		}
	
}
