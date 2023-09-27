/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

public class DDTrace extends DifferentialDouble {
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        int size = matrixChild[0].getRows();
        val = 0; for (int r=0; r<size; r++) val += matrixChild[0].val[r][r];
        if (diffDepth > 0) for (int i=0; i<anzPar; i++) {dVal[i] = 0; for (int r=0; r<size; r++) dVal[i] += matrixChild[0].dVal[i][r][r];} 
        if (diffDepth > 1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) {
            ddVal[i][j] = 0; for (int r=0; r<size; r++) ddVal[i][j] += matrixChild[0].ddVal[i][j][r][r];}
        return false;
    }
    public String toString() {return "tr("+matrixChild[0]+")";}
}
