/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

public class DMDiag extends DifferentialMatrix {
    public int getRows() {return child[0].getRows();}
    public int getColumns() {return child[0].getRows();}
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        
        for (int r=0; r<val.length; r++) {
            val[r][r] = child[0].val[r][0];
            if (diffDepth>0) for (int i=0; i<anzPar; i++) dVal[i][r][r] = child[0].dVal[i][r][0];
            if (diffDepth>1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) ddVal[i][j][r][r] = child[0].ddVal[i][j][r][0];
        }
        return false;
    }
    public String toString() {return "diag("+child[0]+")";}

}
