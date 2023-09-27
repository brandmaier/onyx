/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

public class DMRoot extends DifferentialMatrix {
    
    public int row, col;
    
    public DMRoot(DifferentialDouble[][] val) {
        super(); 
        row = val.length; col = val[0].length;
        scalarChild = new DifferentialDouble[val.length*val[0].length];
        for (int i=0; i<val.length; i++) for (int j=0; j<val[0].length; j++) scalarChild[i*val[0].length+j] = val[i][j].copy(); 
    }
    public DMRoot(double[][] val) {
        super(); 
        row = val.length; col = val[0].length;
        scalarChild = new DifferentialDouble[val.length*val[0].length];
        for (int i=0; i<val.length; i++) for (int j=0; j<val[0].length; j++) scalarChild[i*val[0].length+j] = new DDConst(val[i][j]); 
    }
    public DMRoot(String[][] val) {
        super(); 
        row = val.length; col = val[0].length;
        scalarChild = new DifferentialDouble[val.length*val[0].length];
        for (int i=0; i<val.length; i++) for (int j=0; j<val[0].length; j++) scalarChild[i*val[0].length+j] = DifferentialDouble.fromString(val[i][j]); 
    }
    public DifferentialDouble getEntry(int r, int c) {return scalarChild[r*col+c];}
    public int getRows() {return row;} 
    public int getColumns() {return col;}
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        for (int r=0; r<val.length; r++) for (int c=0; c<val[0].length; c++) {
            scalarChild[r*col+c].eval(vals);
            val[r][c] = scalarChild[r*col+c].val;
            if (diffDepth>0) for (int i=0; i<anzPar; i++) dVal[i][r][c] = scalarChild[r*col+c].dVal[i];
            if (diffDepth>1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) ddVal[i][j][r][c] = scalarChild[r*col+c].ddVal[i][j];
        }
        return false;
    }
    
    public String toString() {
        String erg = "{";
        for (int i=0; i<getRows(); i++) {
            erg += "{";
            for (int j=0; j<getColumns(); j++) erg += scalarChild[i*col+j]+(j<getColumns()-1?",":"");
            erg += "}"+(i<getRows()-1?",":"");
        }
        return erg+"}";
    }    
}
