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

public class DMTrans extends DifferentialMatrix {
    public int getRows() {return child[0].getColumns();}
    public int getColumns() {return child[0].getRows();}
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        
        for (int r=0; r<val.length; r++) for (int c=0; c<val[0].length; c++) {
            val[r][c] = child[0].val[c][r];
            if (diffDepth>0) for (int i=0; i<anzPar; i++) dVal[i][r][c] = child[0].dVal[i][c][r];
            if (diffDepth>1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) ddVal[i][j][r][c] = child[0].ddVal[i][j][c][r];
        }
        return false;
    }
    public String toString() {return child[0]+"^T";}

}
