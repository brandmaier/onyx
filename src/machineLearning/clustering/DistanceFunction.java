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
/*
 * Created on 04.05.2011
 */
package machineLearning.clustering;

public interface DistanceFunction {
    
    public final static DistanceFunction euclidean = new DistanceFunction() {
        public double distance(double[] data1, double[] data2) {
            double erg = 0; 
            for (int i=0; i<data1.length; i++) erg += (data1[i]-data2[i])*(data1[i]-data2[i]);
            return Math.sqrt(erg);
        }
    };

    public double distance(double[] data1, double[] data2);
}
