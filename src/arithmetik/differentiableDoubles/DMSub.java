/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

public class DMSub extends DifferentialMatrix {
    
    public int[] selectedRows,selectedColumns;
    
    public int getRows() {return selectedRows.length;}
    public int getColumns() {return selectedColumns.length;}
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        for (int i=0; i<selectedRows.length; i++) for (int j=0; j<selectedColumns.length; j++) {
            val[i][j] = child[0].val[selectedRows[i]][selectedColumns[j]];
            if (diffDepth>0) for (int k=0; k<anzPar; k++) dVal[k][i][j] = child[0].dVal[k][selectedRows[i]][selectedColumns[j]];
            if (diffDepth>1) for (int k=0; k<anzPar; k++) for (int l=0; l<anzPar; l++)
                ddVal[k][l][i][j] = child[0].ddVal[k][l][selectedRows[i]][selectedColumns[j]];
        }
        return false;
    }
    public String toString() {
        String erg = "sub([";
        for (int i=0; i<selectedRows.length; i++) erg+=selectedRows[i]+(i<selectedRows.length-1?",":"");
        erg += "]x[";
        for (int i=0; i<selectedColumns.length; i++) erg+=selectedColumns[i]+(i<selectedColumns.length-1?",":"");
        erg += "];"+child[0];
        return erg;
    }

}
