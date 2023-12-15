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
 * Created on 10.10.2014
 */
package machineLearning.preprocessor;

import java.util.Vector;

import engine.Statik;

public class SingleCombinedFeatures extends Preprocessor {

    public enum Feature {SUM, STDV, MEANCOMPONENT, LDACOMPONENT};
    private Vector<Feature> components;
    private double[] meanFactor;
    private double[] ldaFactor;
    
    public SingleCombinedFeatures(Feature[] components) {this.components = new Vector<Feature>(components.length); for (Feature c:components) this.components.add(c);}
    
    @Override
    public void train(int start, int end) {
        if (components.contains(Feature.MEANCOMPONENT) || components.contains(Feature.LDACOMPONENT)) {
            int g1 = target[start], g2 = 2398463; int j=start+1; while (j<end) if (target[j]!=g1) {g2 = target[j]; j=end; } else j++;
            if (g2 == 2398463) throw new RuntimeException("Training Range of CSP contains targets of only one group.");
            if (g1>g2) {int t = g1; g1=g2; g2=t;}
            
            int dim = data[start].length, anzPer = end-start;
            double[][] cov1=new double[dim][dim], cov2 = new double[dim][dim]; double[] mean1 = new double[dim], mean2 = new double[dim];
            for (int i=start; i<end; i++) {
                if (target[i] == g1) {mean1 = Statik.add(mean1, data[i]); cov1 = Statik.add(cov1,Statik.multiply(data[i], data[i], true));}
                if (target[i] == g2) {mean2 = Statik.add(mean2, data[i]); cov2 = Statik.add(cov2,Statik.multiply(data[i], data[i], true));}
            }
            mean1 = Statik.multiply(1/(double)(anzPer), mean1); mean2 = Statik.multiply(1/(double)(anzPer), mean2);
            cov1 = Statik.subtract(Statik.multiply(1/(double)(anzPer-1), cov1), Statik.multiply(anzPer/(double)(anzPer-1), Statik.multiply(mean1, mean1, true)));
            cov2 = Statik.subtract(Statik.multiply(1/(double)(anzPer-1), cov2), Statik.multiply(anzPer/(double)(anzPer-1), Statik.multiply(mean2, mean2, true)));

            double[][] covTotal = Statik.add(cov1, cov2);
            meanFactor = Statik.subtract(mean2, mean1); meanFactor = Statik.multiply(1.0/Statik.norm(meanFactor), meanFactor);
            if (components.contains(Feature.LDACOMPONENT)) {
                ldaFactor = Statik.solveSymmetricalPositiveDefinite(covTotal, meanFactor, null);
                ldaFactor = Statik.multiply(1.0/Statik.norm(ldaFactor), ldaFactor);
            }
        }
    }

    @Override
    public double[] transform(double[] in) {
        double[] erg = new double[components.size()];
        double sum = 0; for (int i=0; i<in.length; i++) sum += in[i];
        for (int i=0; i<erg.length; i++) {
            if (components.elementAt(i) == Feature.SUM) {erg[i] = sum;}
            if (components.elementAt(i) == Feature.STDV) {
                erg[i] = 0; for (int j=0; j<in.length; j++) erg[i] += in[j]*in[j]; 
                erg[i] = erg[i]/((double)in.length) - Math.pow(sum/((double)in.length),2);
                erg[i] = Math.sqrt(erg[i]);
            }
            if (components.elementAt(i) == Feature.MEANCOMPONENT) {erg[i] = 0; for (int j=0; j<in.length; j++) erg[i] += in[j]*meanFactor[j];}
            if (components.elementAt(i) == Feature.LDACOMPONENT) {erg[i] = 0; for (int j=0; j<in.length; j++) erg[i] += in[j]*ldaFactor[j];}
        }
        return erg;
    }

}
