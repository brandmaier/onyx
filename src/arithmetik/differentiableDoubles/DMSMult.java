/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

import engine.Statik;

public class DMSMult extends DifferentialMatrix {
    
    public int getRows() {return child[0].getRows();}
    public int getColumns() {return child[1].getColumns();}
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        Statik.multiply(scalarChild[0].val, child[0].val, val);
        if (diffDepth>0) for (int i=0; i<anzPar; i++) 
            Statik.add(Statik.multiply(scalarChild[0].val, child[0].dVal[i]), Statik.multiply(scalarChild[0].dVal[i],child[0].val), dVal[i]);
        if (diffDepth>1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) { 
            double[][] am = Statik.multiply(scalarChild[0].ddVal[i][j], child[0].val);
            double[][] bm = Statik.multiply(scalarChild[0].dVal[i],child[0].dVal[j]);
            double[][] cm = Statik.multiply(scalarChild[0].dVal[j],child[0].dVal[i]);
            double[][] dm = Statik.multiply(scalarChild[0].val, child[0].ddVal[i][j]);
            for (int r=0; r<val.length; r++) for (int c=0; c<val[0].length; c++) ddVal[i][j][r][c] = am[r][c]+bm[r][c]+cm[r][c]+dm[r][c];
        
        }
        return false;
    }
    public String toString() {return child[0]+" &* "+child[1];}

}
