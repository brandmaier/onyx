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
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

public class DDPara extends DifferentialDouble {
    
    public int nr;
    
    public DDPara(int nr) {super(); this.nr = nr;}
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        val = vals[nr]; 
        if (diffDepth>0) dVal[nr] = 1;
        return false;
    }
    public String toString() {return "X"+nr;}
    public int getMaxParameterNumber() {return nr;}
    public boolean containsParameter(int nr) {return (this.nr == nr);}
    public DifferentialDouble fixParameter(int nr, double value) {if (this.nr!=nr) return this; else return new DDConst(value);}
    protected DifferentialDouble fixParameterAndReduceHigherRecursive(int nr, double value) {
        if (this.hash == 0.0) return this;
        this.hash = 0.0;
        if (this.nr<nr) return this; else if (this.nr==nr) return new DDConst(value); else return new DDPara(this.nr-1);
    }
    public DifferentialDouble substituteRecursively(int[] pnr, DifferentialDouble[] vals) {
        if (hash == 0.0) return this;
        this.hash = 0.0;
        for (int i=0; i<pnr.length; i++) if (this.nr == pnr[i]) {
            DifferentialDouble erg = vals[i].copy();
            erg.hash = 0.0;
            return erg;
        }
        return this;
    }
    
}
