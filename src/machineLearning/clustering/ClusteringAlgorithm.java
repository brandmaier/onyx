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

import java.util.Random;

public abstract class ClusteringAlgorithm {
    
    double[][] data;
    int[] lastClustering;
    int anzVar;
    int anzPer;
    
    Random random = new Random();
    
    public ClusteringAlgorithm() {this.data = null; anzPer =0; anzVar = 0;}
    public ClusteringAlgorithm(double[][] data) {this.data = data; anzPer = data.length; anzVar = data[0].length;}
    
    public void setData(double[][] data) {this.data = data; anzPer = data.length; anzVar = data[0].length;}
    
    public abstract int[] cluster();    
}
