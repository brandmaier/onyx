/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

public class DDMatrixEntry extends DifferentialDouble {
    public int row, column;
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        int size = matrixChild[0].getRows();
        val = matrixChild[0].val[row][column]; 
        if (diffDepth > 0) for (int i=0; i<anzPar; i++) dVal[i] = matrixChild[0].dVal[i][row][column]; 
        if (diffDepth > 1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) ddVal[i][j] = matrixChild[0].ddVal[i][j][row][column];
        return false;
    }
    public String toString() {return "("+matrixChild[0]+")_{"+row+";"+column+"}";}
}
