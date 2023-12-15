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

public class ChiSquare implements Distribution {

	private int k;


	public ChiSquare(int k) {
		this.k = k;
	}
	
	   static double logGamma(double x) {
		      double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
		      double ser = 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
		                       + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
		                       +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5);
		      return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
		   }
		   static double gamma(double x) { return Math.exp(logGamma(x)); }
		
		   
		   @Override
		public double getDensity(double x) {
			   return( 1.0/(Math.pow(2, k/2.0)*gamma(k/2.0))   *Math.pow(x, k/2.0-1)*Math.exp(-x/2.0) );
		}

			public double getStartingValue() {
				return( k );
			}
			
			public String getName() {
				return("ChiSquare");
			}
		   
}
