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
 * Created on 12.08.2010
 */
package arithmetik.differentiableDoubles;

import java.util.Random;

public abstract class DifferentialObject implements Cloneable
{
    public final static long randomSeed = 234235187412L; 

    public static double[] hashWeights;
    public int anzPar;
    public static int defaultDiffDepth;
    public int diffDepth;
    
    public double hash;

    public void setAnzParameter(int anzParameter) {
        if (anzPar != anzParameter) hash = Double.NaN;
        anzPar = anzParameter; 
        if (hashWeights == null || hashWeights.length < anzPar) {
            hashWeights = new double[anzPar]; 
            Random rand = new Random(randomSeed);
            for (int i=0; i<anzPar; i++) hashWeights[i] = rand.nextDouble();
        }
    }
    public static void setDifferentialDepth(int diffDepthIn) {
        defaultDiffDepth = diffDepthIn;
    }
    
 
    public String name;
    
    public void setName(String name) {this.name = name;}
    
    public String toString() {if (name!=null) return name; else return "...";}
    
    public abstract DifferentialObject fixParameter(int nr, double value);
    public abstract int getMaxParameterNumber();
    public abstract boolean containsParameter(int nr);

    
}

