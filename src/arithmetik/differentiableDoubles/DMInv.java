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

import engine.Statik;

public class DMInv extends DifferentialMatrix {
    public int getRows() {return child[0].getRows();}
    public int getColumns() {return child[0].getColumns();}
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        Statik.invert(child[0].val, val, new double[val.length][val[0].length]);
        if (diffDepth>0) for (int i=0; i<anzPar; i++) {
            Statik.multiply(Statik.multiply(val, child[0].dVal[i]),val,dVal[i]); 
            for (int r=0; r<dVal[i].length; r++) for (int c=0; c<dVal[i].length; c++) dVal[i][r][c]*= -1;
        }
        if (diffDepth>1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) { 
            double[][] am = Statik.multiply(Statik.multiply(val, child[0].ddVal[i][j]),val);
            double[][] bm = Statik.multiply(val, child[0].dVal[i]);
            double[][] cm = Statik.multiply(val, child[0].dVal[j]);
            double[][] dm = Statik.multiply(bm, Statik.multiply(cm, val));
            double[][] em = Statik.multiply(cm, Statik.multiply(bm, val));
            for (int r=0; r<val.length; r++) for (int c=0; c<val[0].length; c++) ddVal[i][j][r][c] = -am[r][c]+dm[r][c]+em[r][c];
        }
        return false;
    }
    public String toString() {return child[0]+"&^{-1}";}

}
