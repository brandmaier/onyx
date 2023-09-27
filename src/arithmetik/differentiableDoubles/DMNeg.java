/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

public class DMNeg extends DifferentialMatrix {
    public int getRows() {return child[0].getRows();}
    public int getColumns() {return child[0].getColumns();}
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        
        for (int r=0; r<val.length; r++) for (int c=0; c<val[0].length; c++) {
            val[r][c] = -child[0].val[r][c];
            if (diffDepth>0) for (int i=0; i<anzPar; i++) dVal[i][r][c] = -child[0].dVal[i][r][c];
            if (diffDepth>1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) ddVal[i][j][r][c] = -child[0].ddVal[i][j][r][c];
        }
        return false;
    }
    public String toString() {return "&-"+child[0];}

}
