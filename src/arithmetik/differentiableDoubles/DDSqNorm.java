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

public class DDSqNorm extends DifferentialDouble {
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        int rows = matrixChild[0].getRows(), cols = matrixChild[0].getColumns();
        val = 0; for (int r=0; r<rows; r++) for (int c=0; c<cols; c++) val += matrixChild[0].val[r][c]*matrixChild[0].val[r][c];
        if (diffDepth > 0) for (int i=0; i<anzPar; i++) {
            dVal[i] = 0; for (int r=0; r<rows; r++) for (int c=0; c<cols; c++) dVal[i] += 2*matrixChild[0].val[r][c]*matrixChild[0].dVal[i][r][c];
        } 
        if (diffDepth > 1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++){
            ddVal[i][j] = 0; for (int r=0; r<rows; r++) for (int c=0; c<cols; c++) 
                ddVal[i][j] += 2*(matrixChild[0].dVal[i][r][c]*matrixChild[0].dVal[j][r][c]+matrixChild[0].val[r][c]*matrixChild[0].ddVal[i][j][r][c]);
        }
        return false;
    }
    public String toString() {return "norm_2("+child[0]+")^2";}
}
