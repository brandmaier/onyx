/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

import engine.Statik;

public class DDDet extends DifferentialDouble {
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        if (diffDepth==0) {val = Statik.determinant(matrixChild[0].val); return false;}
        int rows = matrixChild[0].getRows(), cols = matrixChild[0].getColumns();
        double[][] inv = new double[rows][cols], work = new double[rows][cols];
        val = Statik.invert(matrixChild[0].val, inv, work);
        for (int i=0; i<anzPar; i++) {
            dVal[i] = 0; 
            for (int r=0; r<rows; r++) for (int c=0; c<rows; c++) dVal[i] += matrixChild[0].dVal[i][r][c]*inv[r][c]*val;
            if (diffDepth>1) {
                double[][] am = Statik.multiply(Statik.multiply(inv, matrixChild[0].dVal[i]),inv);
                double av = 0; for (int r=0; r<rows; r++) for (int c=0; c<rows; c++) av += inv[r][c]*matrixChild[0].dVal[i][r][c];
                for (int j=0; j<anzPar; j++) { 
                    double a = 0; for (int r=0; r<rows; r++) for (int c=0; c<rows; c++) a += -am[r][c]*matrixChild[0].dVal[j][r][c]+inv[r][c]*matrixChild[0].ddVal[i][j][r][c];
                    double b = 0; for (int r=0; r<rows; r++) for (int c=0; c<rows; c++) b += inv[r][c]*matrixChild[0].dVal[j][r][c];
                    ddVal[i][j] = (a+b*av)*val;
                }
            }
        }
        return false;
    }
    public String toString() {return "det("+child[0]+")";}
}
