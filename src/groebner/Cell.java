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
 * Created on 01.12.2013
 */
package groebner;

import java.util.Comparator;

import engine.Statik;

public class Cell<F extends Field<F>> implements Comparable<Cell> {
    
    public final Comparator<int[]> monomialOrder = Monomial.grevlexorder;
    public final int hashMod = 124351843;
    public final int[] hashWeights = new int[]{1%hashMod,20%hashMod,400%hashMod,8000%hashMod,160000%hashMod,3200000%hashMod,64000000%hashMod,100%hashMod,2000%hashMod,
               40000%hashMod,800000%hashMod,16000000%hashMod,1000%hashMod,2000%hashMod,400000%hashMod,8000000%hashMod};
    
    public int[] exp;
    public int polynomial;
    public int iteratorNumber;
    public ListPolynomial<F> reduced;
    
    public Cell(int[] exp) {this.exp = exp; polynomial = -1; iteratorNumber = 0; reduced = null;}

    public boolean isStarted() {return (reduced == null && iteratorNumber > 0);}
    public boolean isFinished() {return reduced != null;}
    public boolean isUnreducable() {return polynomial == -1;}
    
    @Override
    public int compareTo(Cell otherCell) {
        return monomialOrder.compare(this.exp, otherCell.exp);
    }
    
    @Override 
    public boolean equals(Object otherCell) {
        if (otherCell instanceof Cell) {
            return (compareTo((Cell)otherCell) == 0);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int res = 0;
        for (int i=0; i<exp.length; i++) res = (res+exp[i]*(i>=hashWeights.length?1:hashWeights[i])) % hashMod;
        return res;
    }
    
    public String toString() {
        return "{"+Statik.matrixToString(exp)+"}";
    }

}
