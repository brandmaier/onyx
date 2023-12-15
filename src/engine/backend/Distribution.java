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
 * Created on 25.03.2017
 */
package engine.backend;

import engine.Statik;

/**
 * Wraps the first two moments of a distribution ss
 * @author timo
 */
public class Distribution {
    public int anzVar;
    public double[] mean;
    public double[][] covariance;
    
    public Distribution(double[] mean, double[][] covariance) {
        this.anzVar = mean.length;
        this.mean = mean;
        this.covariance = covariance;
    }
    
    public Distribution() {
        this.anzVar = 0;
        this.mean = new double[0];
        this.covariance = new double[0][0];
    }
    
    public Distribution(double[][] covariance) {
        this.anzVar = covariance.length;
        this.mean = new double[anzVar];
        this.covariance = covariance;
    }

    public Distribution(int anzVar) {
        this.anzVar = anzVar;
        this.mean = new double[anzVar];
        this.covariance = Statik.identityMatrix(anzVar);
    }

    public Distribution copy() {
        return new Distribution(Statik.copy(mean), Statik.copy(covariance));
    }
    
    public void copyFrom(Distribution toCopy) 
    {
        Statik.copy(toCopy.mean, mean);
        Statik.copy(toCopy.covariance, covariance);
    }
    
    public static Distribution ensureSize(Distribution erg, int anzVar) {
        if (erg == null) return new Distribution(anzVar);
        erg.anzVar = anzVar;
        erg.mean = Statik.ensureSize(erg.mean, anzVar);
        erg.covariance = Statik.ensureSize(erg.covariance, anzVar, anzVar);
        return erg;
    }
    
    public String toString() {
        String erg = Statik.matrixToString(mean, 5);
        erg += "\r\n\r\n"+Statik.matrixToString(covariance, 5);
        return erg;
    }

    public static Distribution copy(Distribution source, Distribution target) {
        if (source == null) return null;
        if (target == null) return source.copy();
        target.copyFrom(source);
        return target;
    }
}
