/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

import engine.Statik;

public class DDLnDet extends DifferentialDouble {
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        if (diffDepth==0) {val = Statik.logDeterminantOfPositiveDefiniteMatrix(matrixChild[0].val); return false;}
        int size = matrixChild[0].getRows();
        double[][] inv = new double[size][size];
        try {
            double[] logresult = new double[1];
            Statik.invertSymmetricalPositiveDefinite(matrixChild[0].val, inv, logresult);
            val = logresult[0];
        } catch (Exception e) {val = Double.NaN;}
        
        for (int i=0; i<anzPar; i++) {
            dVal[i] = 0; 
            for (int r=0; r<size; r++) for (int c=0; c<size; c++) dVal[i] += matrixChild[0].dVal[i][r][c]*inv[c][r];
            if (diffDepth>1) {double[][] am = Statik.multiply(Statik.multiply(inv, matrixChild[0].dVal[i]),inv); for (int j=0; j<anzPar; j++) { 
                ddVal[i][j] = 0; 
                for (int r=0; r<size; r++) for (int c=0; c<size; c++) ddVal[i][j] += -am[r][c]*matrixChild[0].dVal[j][r][c] + inv[r][c]*matrixChild[0].ddVal[i][j][r][c];
            }}
        }
        return false;
    }
    public String toString() {return "ln det("+matrixChild[0]+")";}
}
