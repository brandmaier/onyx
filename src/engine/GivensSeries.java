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
 * Created on 04.02.2014
 */
package engine;

public class GivensSeries {
    
    public boolean adjacentIJs;     // if true, only the i value is stored in ij, the j is assumed to be j = i+1
    
    public int size;
    
    public double[] scValues;
    public int[] ijValues;
    
    public double s,c;
    public int ix1,ix2;
    public int ix;
    
    public GivensSeries(int initialSize, boolean adjacentIJs) {
        this.adjacentIJs = adjacentIJs;
        ijValues = new int[(adjacentIJs?initialSize:2*initialSize)];
        scValues = new double[2*initialSize];
        size = 0;
    }
    
    public void clear() {size = 0;}
    
    private void extendArrays() {
        double[] newSC = new double[(int)Math.round(1.5*scValues.length)];
        int[] newIJ = new int[(int)Math.round(1.5*ijValues.length)];
        Statik.copy(scValues, newSC);
        Statik.copy(ijValues, newIJ);
        scValues = newSC;
        ijValues = newIJ;
    }
    
    public void add(double s, double c, int i, int j) {
        if (2*size+2 >= scValues.length) extendArrays();
        scValues[2*size] = s; scValues[2*size+1] = c;
        if (adjacentIJs) ijValues[size] = i;
        else {ijValues[2*size] = i; ijValues[2*size+1] = j;}
        size++;
    }
    
    public void setTo(int i) {
        ix = i;
        s = scValues[2*i]; c = scValues[2*i+1];
        if (adjacentIJs) {ix1 = ijValues[i]; ix2 = ix1+1;}
        else {ix1 = ijValues[2*i]; ix2 = ijValues[2*i+1];}
    }
    
    public boolean next() {if (ix < size-1) setTo(ix+1); else return false; return true;}
    public boolean prev() {if (ix > 0) setTo(ix-1); else return false; return true;}

    public double[] multiply(double[] vector) {return multiply(vector, false, new double[vector.length]);}
    public double[] multiply(double[] vector, double[] erg) {return multiply(vector, false, erg);}
    public double[] multiply(double[] vector, boolean inverse) {return multiply(vector, inverse, new double[vector.length]);}
    public double[] multiply(double[] vector, boolean inverse, double[] erg) {
        if (erg != vector) Statik.copy(vector, erg);
        if (inverse) ix = size; else ix = -1;
        while (inverse?prev():next())  {
            double sl = (inverse?-s:s);
            double t = erg[ix1]*c - erg[ix2]*sl;
            erg[ix2] = erg[ix1]*sl + erg[ix2]*c;
            erg[ix1] = t;
        }
        return erg;
    }
    
}
