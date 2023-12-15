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
 * Created on 09.10.2014
 */
package machineLearning.preprocessor;

public abstract class Preprocessor {
    
    protected double[][] data; 
    protected int[] target;
    
    public Preprocessor() {}
    public Preprocessor(double[][] data, int[] target) {setDataAndTarget(data, target);}
    public void setDataAndTarget(double[][] data, int[] target) {
        this.data = data; 
        this.target = target;
    }
    
    public void train() {train(0, data.length);}
    public abstract void train(int start, int end);
    public abstract double[] transform(double[] in);
    public double[][] transform(double[][] in) {
        double[][] erg = new double[in.length][];
        for (int i=0; i<erg.length; i++) erg[i] = transform(in[i]);
        return erg;
    }

}
