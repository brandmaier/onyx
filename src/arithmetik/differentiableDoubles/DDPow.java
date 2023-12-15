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

public class DDPow extends DifferentialDouble {
    public double exponent;
    
    public DDPow(double exponent) {this.exponent = exponent;}
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        val = Math.pow(child[0].val,exponent);
        if (diffDepth>0) for (int i=0; i<anzPar; i++) dVal[i] = child[0].dVal[i] * exponent * Math.pow(child[0].val,exponent-1);
        if (diffDepth>1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) ddVal[i][j] = exponent*
            (child[0].ddVal[i][j]*Math.pow(child[0].val,exponent-1) + child[0].dVal[i]*child[0].dVal[j]*(exponent-1)*Math.pow(child[0].val,exponent-2));
        return false;
    }
    public String toString() {return "("+child[0]+")^{"+exponent+"}";}

}
