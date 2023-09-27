/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

import arithmetik.AnalyticalFunction;
import engine.Statik;

public abstract class DifferentialMatrix extends DifferentialObject {
    
    public double[][] val;
    public double[][][] dVal;
    public double[][][][] ddVal;
    
    public DifferentialMatrix[] child;
    public DifferentialDouble[] scalarChild;
    
//    public int rows, columns;

    public DifferentialMatrix() {this(0);}
    public DifferentialMatrix(int size) {this(size,size);}
    public DifferentialMatrix(int rows, int columns) {
//        val = new double[rows][columns];
//        dVal = new double[anzPar][rows][columns];
//        ddVal = new double[anzPar][anzPar][rows][columns];
    }
    
    public static DMRoot matrix(DifferentialDouble[][] val) {return new DMRoot(val);}
    public static DMRoot matrix(String[][] val) {return new DMRoot(val);}
    public static DMRoot columnVector(DifferentialDouble[] val) {
        DifferentialDouble[][] arg = new DifferentialDouble[val.length][1];
        for (int i=0; i<val.length; i++) arg[i][0] = val[i];
        return new DMRoot(arg);
    }
    public static DMRoot rowVector(DifferentialDouble[] val) {
        DifferentialDouble[][] arg = new DifferentialDouble[1][val.length];
        for (int i=0; i<val.length; i++) arg[0][i] = val[i];
        return new DMRoot(arg);
    }
    public static DMRoot matrix(double[][] val) {return new DMRoot(val);}
    public static DifferentialMatrix manualNode() {return new DifferentialMatrix(){public String toString(){return "Manual Matrix";}};}
    public static DMAdd add(DifferentialMatrix a, DifferentialMatrix b) {DMAdd erg = new DMAdd(); erg.child = new DifferentialMatrix[]{a,b}; return erg;}
    public DMAdd plus(DifferentialMatrix a) {return add(this,a);}
    public DMAdd plus(double[][] a) {return add(this, new DMRoot(a));}
    public static DMNeg negate(DifferentialMatrix a) {DMNeg erg = new DMNeg(); erg.child = new DifferentialMatrix[]{a}; return erg;}
    public DMNeg neg() {return negate(this);}
    public static DMAdd subtract(DifferentialMatrix a, DifferentialMatrix b) {DMAdd erg = new DMAdd(); erg.child = new DifferentialMatrix[]{a,b.neg()}; return erg;}
    public DMAdd minus(DifferentialMatrix a) {return subtract(this,a);}
    public DMAdd minus(double[][] a) {return minus(new DMRoot(a));}
    public static DMMult multiply(DifferentialMatrix a, DifferentialMatrix b) {DMMult erg = new DMMult(); erg.child = new DifferentialMatrix[]{a,b}; return erg;}
    public DMMult times(DifferentialMatrix a) {return multiply(this,a);}
    public DMMult times(double[][] a) {return times(new DMRoot(a));}
    public static DMInv invert(DifferentialMatrix a) {DMInv erg = new DMInv(); erg.child = new DifferentialMatrix[]{a}; return erg;}
    public DMInv inv() {return invert(this);}
    public static DMMult divide(DifferentialMatrix a, DifferentialMatrix b) {DMMult erg = new DMMult(); erg.child = new DifferentialMatrix[]{a,b.inv()}; return erg;}
    public DMMult over(DifferentialMatrix a) {return divide(this,a);}
    public DMMult over(double[][] a) {return times(Statik.invert(a));}
    public static DMTrans transpose(DifferentialMatrix a) {DMTrans erg = new DMTrans(); erg.child = new DifferentialMatrix[]{a}; return erg;}
    public DMTrans trans() {return transpose(this);}
    public static DMSMult scalarMultiply(DifferentialDouble a, DifferentialMatrix b) 
        {DMSMult erg = new DMSMult(); erg.scalarChild = new DifferentialDouble[]{a}; erg.child = new DifferentialMatrix[]{b}; return erg;}
    public static DMDiag diagonal(DifferentialMatrix a) {DMDiag erg = new DMDiag(); erg.child = new DifferentialMatrix[]{a}; return erg;}
    public DMDiag diag() {return diagonal(this);}
    public static DMSub submatrix(DifferentialMatrix a, int[] selectedRows, int[] selectedColumns) 
        {DMSub erg = new DMSub(); erg.child = new DifferentialMatrix[]{a};  erg.selectedRows = Statik.copy(selectedRows); erg.selectedColumns = Statik.copy(selectedColumns); return erg;}
    public DMSub submatrix(int[] selectedRows, int[] selectedColumns) {return submatrix(this, selectedRows, selectedColumns);}
    public DMSub submatrix(int[] selected) {return (this.getColumns()==1?submatrix(this,selected,new int[]{0}):submatrix(this, selected, selected));}
    
    public DDDet det() {return DifferentialDouble.determinant(this);}
    public DDTrace tr() {return DifferentialDouble.trace(this);}
    public DDTrace scale() {return tr();}
    public DDSqNorm squaredNorm() {return DifferentialDouble.squaredNorm(this);}
    public DDPow norm() {return DifferentialDouble.norm(this);}
    public DDLnDet lnDet() {return DifferentialDouble.lnDeterminant(this);}
    public DDTrPr traceTimes(DifferentialMatrix a) {return DifferentialDouble.traceProduct(this, a);}
    public DifferentialDouble getEntry(int row, int column) {return DifferentialDouble.getMatrixEntry(this, row, column);}
    public DifferentialDouble getEntry(int ix) {if (this.getColumns()==1) return this.getEntry(ix,0); else return getEntry(0,ix);}
    
    public int getRows() {if (val != null) return val.length; else return 0;}
    public int getColumns() {if ((val!=null) && (val.length>0)) return val[0].length; else return 0;}
    
    public String toString() {return ""+val;}
    public String toDevString() {
        String erg = val+" 1st = ("; 
        for (int i=0; i<anzPar; i++) erg += dVal[i]+(i==(anzPar-1)?"), 2nd = [(":", ");
        for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) erg += ddVal[i][j]+(j==anzPar-1?(i==anzPar-1?")]":"), ("):", ");
        return erg;
    }
    
    protected void prepMatrices(int rows, int columns) {
        if ((val==null) || (val.length!=rows) || (val[0].length!=columns)) val = new double[rows][columns];
        if ((diffDepth>0) && (anzPar>0) && ((dVal==null) || (dVal.length!=anzPar) || (dVal[0].length!=rows) || (dVal[0][0].length!=columns))) dVal = new double[anzPar][rows][columns];
        if ((diffDepth>1) && (anzPar>0) && ((ddVal==null) || (ddVal.length!=anzPar) || (ddVal[0].length!=anzPar) || (ddVal[0][0].length!=rows) || (ddVal[0][0][0].length!=columns))) 
            ddVal = new double[anzPar][anzPar][rows][columns];
    }
    
    public void eval(double[] vals) {
        invalidateTree();
        setAnzParameter(vals.length); 
        double hash = 0.0; for (int i=0; i<anzPar; i++) hash += hashWeights[i] * vals[i];
        eval(vals, hash);
    }
    protected boolean eval(double[] vals, double hash) {
        setAnzParameter(vals.length);
        if (diffDepth != defaultDiffDepth) {this.hash = Double.NaN; diffDepth = defaultDiffDepth; }
        prepMatrices(getRows(), getColumns());
        if (hash == this.hash) return true;
        this.hash = hash;
        
        if (child!=null) for (int i=0; i<child.length; i++) child[i].eval(vals, hash);
        if (scalarChild!=null) for (int i=0; i<scalarChild.length; i++) scalarChild[i].eval(vals, hash);
        return false;
    }
    
//    public void reset() {reset(val.length, val[0].length);} 
    public void reset(int rows, int columns) {
        prepMatrices(rows, columns);
        hash = Double.NaN;
        for (int r=0; r<rows; r++) for (int c=0; c<columns; c++) {
            val[r][c] = Double.NaN;
            if (diffDepth>0) for (int i=0; i<anzPar; i++) dVal[i][r][c] = 0;
            if (diffDepth>1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) ddVal[i][j][r][c] = 0;
        }
    }

    
    public int getMaxParameterNumber() {
        int erg=-1;
        if (child!=null) for (int i=0; i<child.length; i++) erg = Math.max(erg, child[i].getMaxParameterNumber());
        if (scalarChild!=null) for (int i=0; i<scalarChild.length; i++) erg = Math.max(erg, scalarChild[i].getMaxParameterNumber());
        return erg;
    }

    public DifferentialMatrix fixParameter(int nr, double value) {
        if (child!=null) for (int i=0; i<child.length; i++) child[i] = child[i].fixParameter(nr, value);
        if (scalarChild!=null) for (int i=0; i<scalarChild.length; i++) scalarChild[i] = scalarChild[i].fixParameter(nr, value);
        return this;
    }
    public DifferentialMatrix fixParameterAndReduceHigher(int nr, double value) {
        invalidateTree();
        fixParameterAndReduceHigherRecursive(nr, value);
        invalidateTree();
        return this;
    }
    protected DifferentialMatrix fixParameterAndReduceHigherRecursive(int nr, double value) {
        if (hash == 0.0) return this;
        if (child!=null) for (int i=0; i<child.length; i++) child[i] = child[i].fixParameterAndReduceHigherRecursive(nr, value);
        if (scalarChild!=null) for (int i=0; i<scalarChild.length; i++) scalarChild[i] = scalarChild[i].fixParameterAndReduceHigherRecursive(nr, value);
        hash = 0.0;
        return this;
    }
    public void invalidateTree() {
        hash = Double.NaN;
        if (child!=null) for (int i=0; i<child.length; i++) child[i].invalidateTree();
        if (scalarChild!=null) for (int i=0; i<scalarChild.length; i++) scalarChild[i].invalidateTree();
    }

    public boolean containsParameter(int nr) {
        if (child!=null) for (int i=0; i<child.length; i++) if (child[i].containsParameter(nr)) return true;
        if (scalarChild!=null) for (int i=0; i<scalarChild.length; i++) if (scalarChild[i].containsParameter(nr)) return true;
        return false;
    }
    
    public DifferentialMatrix copy() {
        try {
            DifferentialMatrix erg = (DifferentialMatrix)clone(); 
            if (child != null) {erg.child = new DifferentialMatrix[child.length]; for (int i=0; i<child.length; i++) erg.child[i] = child[i].copy();}
            if (scalarChild != null) {
                erg.scalarChild = new DifferentialDouble[scalarChild.length];
                for (int i=0; i<scalarChild.length; i++) erg.scalarChild[i] = scalarChild[i].copy();
            }
            return erg;
        } catch (Exception e) {throw new RuntimeException("Problems creating a copy of "+this+".");}
    }
    
    public DifferentialMatrix substitute(int[] pnr, DifferentialDouble[] vals) {
        invalidateTree();
        substituteRecursively(pnr, vals);
        invalidateTree();
        return this;
    }
    
    public DifferentialMatrix substituteRecursively(int[] pnr, DifferentialDouble[] vals) {
        if (hash == 0.0) return this;
        if (child!=null) for (int i=0; i<child.length; i++) child[i] = child[i].substituteRecursively(pnr, vals);
        if (scalarChild!=null) for (int i=0; i<scalarChild.length; i++) scalarChild[i] = scalarChild[i].substituteRecursively(pnr, vals);
        hash = 0.0;
        return this;
    }
    
    
    public boolean selfTest() {return selfTest(null);}
    public boolean selfTest(double[] pos) {
        boolean childOk = true;
        if (child!=null) for (int i=0; i<child.length; i++) childOk = (childOk && child[i].selfTest(pos));
        if (scalarChild!=null) for (int i=0; i<scalarChild.length; i++) childOk = (childOk && scalarChild[i].selfTest(pos));
        
        final DifferentialMatrix fthis = this;
        AnalyticalFunction func = new AnalyticalFunction() {
            public int anzPar() {return anzPar;}
            public double eval(double[] val) {
                fthis.eval(val); double erg = 0; 
                for (int i=0; i<fthis.val.length; i++) for (int j=0; j<fthis.val[0].length; j++) erg += fthis.val[i][j]; 
                return erg;
            }
            public double evalDev(int[] par, double[] val) {
                fthis.eval(val); double erg = 0;
                for (int i=0; i<fthis.val.length; i++) for (int j=0; j<fthis.val[0].length; j++) {
                    if (par.length==0) erg += fthis.val[i][j];
                    if (par.length==1) erg += fthis.dVal[par[0]][i][j];
                    if (par.length==2) erg += fthis.ddVal[par[0]][par[1]][i][j];
                }
                return erg;
            }
        };
        boolean local = func.selftest(pos);
        System.out.println((local?"__OKAY__":"FAILURE")+" for "+this.getClass().getName()+" "+this);
        return childOk && local;
    }

    public static void main(String[] args) {
    }

    public static DMRoot identity(int i) {
        return DifferentialMatrix.matrix(Statik.identityMatrix(i));        
    }
    public static DifferentialMatrix vector(double[] err) {
        return DifferentialMatrix.matrix(Statik.transpose(new double[][]{err}));
    }
    public static DifferentialMatrix vector(String[] err) {
        DifferentialDouble[][] mat = new DifferentialDouble[err.length][1]; 
        for (int i=0; i<err.length; i++) mat[i][0] = DifferentialDouble.fromString(err[i]);
        return DifferentialMatrix.matrix(mat);
    }
    public static DMRoot vector(DifferentialDouble[] val) {
        DifferentialDouble[][] mat = new DifferentialDouble[val.length][1];
        for (int i=0; i<val.length; i++) mat[i][0] = val[i];
        return DifferentialMatrix.matrix(mat);
    }

}
