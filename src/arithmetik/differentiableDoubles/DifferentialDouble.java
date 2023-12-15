/*
* Copyright 2023 by Timo von Oertzen and Andreas M. Brandmaier
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

import java.util.Random;

import arithmetik.AnalyticalFunction;
import engine.Statik;

public abstract class DifferentialDouble extends DifferentialObject 
{
    public double val;
    public double[] dVal;
    public double[][] ddVal;
    
    public DifferentialDouble[] child;
    public DifferentialMatrix[] matrixChild;
    
    public final static DDConst zero = new DDConst(0.0); 
    public final static DDConst one = new DDConst(1.0); 
    
    public DifferentialDouble() {
//        val = Double.NaN;
//        dVal = new double[anzPar];
//        ddVal = new double[anzPar][anzPar];
    }
    
    public static DifferentialDouble manualNode() {return new DifferentialDouble(){};}
    public static DDConst constant(double val) {if (val==0.0) return zero; if (val==1.0) return one; return new DDConst(val);}
    public static DDPara parameter(int pnr) {return new DDPara(pnr);}
    public static DifferentialDouble add(DifferentialDouble a, DifferentialDouble b) 
                        {if (a==zero) return b; if (b==zero) return a; DDAdd erg = new DDAdd(); erg.child = new DifferentialDouble[]{a,b}; return erg;}
    public DifferentialDouble plus(DifferentialDouble a) {return add(this,a);}
    public DifferentialDouble plus(double a) {return add(this, constant(a));}
    public static DDNeg negate(DifferentialDouble a) {DDNeg erg = new DDNeg(); erg.child = new DifferentialDouble[]{a}; return erg;}
    public DDNeg neg() {return negate(this);}
    public static DifferentialDouble subtract(DifferentialDouble a, DifferentialDouble b) 
                        {if (a==zero) return negate(b); if (b==zero) return a; DDAdd erg = new DDAdd(); erg.child = new DifferentialDouble[]{a,b.neg()}; return erg;}
    public DifferentialDouble minus(DifferentialDouble a) {return subtract(this,a);}
    public DifferentialDouble minus(double a) {return plus(constant(-a));}
    public static DifferentialDouble multiply(DifferentialDouble a, DifferentialDouble b) 
                        {if (a==zero || b==zero) return zero; if (a==one) return b; if (b==one) return a; DDMult erg = new DDMult(); erg.child = new DifferentialDouble[]{a,b}; return erg;}
    public DifferentialDouble times(DifferentialDouble a) {return multiply(this,a);}
    public DifferentialDouble times(double a) {return times(constant(a));}
    public static DifferentialDouble invert(DifferentialDouble a) 
                        {if (a==one) return one; DDInv erg = new DDInv(); erg.child = new DifferentialDouble[]{a}; return erg;}
    public DifferentialDouble inv() {return invert(this);}
    public static DifferentialDouble divide(DifferentialDouble a, DifferentialDouble b) 
                        {if (a==zero) return zero; if (a==one) return b.inv(); if (b==one) return a; DDMult erg = new DDMult(); erg.child = new DifferentialDouble[]{a,b.inv()}; return erg;}
    public DifferentialDouble over(DifferentialDouble a) {return divide(this,a);}
    public DifferentialDouble over(double a) {return times(constant(1/a));}
    public DDExp exp() {DDExp erg = new DDExp(); erg.child = new DifferentialDouble[]{this}; return erg;}
    public DDLn ln() {DDLn erg = new DDLn(); erg.child = new DifferentialDouble[]{this}; return erg;}
    public DDPow pow(double exponent) {DDPow erg = new DDPow(exponent); erg.child = new DifferentialDouble[]{this}; return erg;}
    public DDPow sqr() {return pow(2.0);}
    public DDPow sqrt() {return pow(0.5);}
    
    public DMSMult times(DifferentialMatrix a) {return DifferentialMatrix.scalarMultiply(this,a);}
    public static DDLnDet lnDeterminant(DifferentialMatrix a) {DDLnDet erg = new DDLnDet(); erg.matrixChild = new DifferentialMatrix[]{a}; return erg;}
    public static DDDet determinant(DifferentialMatrix a) {DDDet erg = new DDDet(); erg.matrixChild = new DifferentialMatrix[]{a}; return erg;}
    public static DDTrace trace(DifferentialMatrix a) {DDTrace erg = new DDTrace(); erg.matrixChild = new DifferentialMatrix[]{a}; return erg;}
    public static DDSqNorm squaredNorm(DifferentialMatrix a) {DDSqNorm erg = new DDSqNorm(); erg.matrixChild = new DifferentialMatrix[]{a}; return erg;}
    public static DDNorm norm(DifferentialMatrix a) {DDNorm erg = new DDNorm(); erg.child = new DifferentialDouble[]{a.squaredNorm()}; return erg;}
    public static DDTrPr traceProduct(DifferentialMatrix a, DifferentialMatrix b) {DDTrPr erg = new DDTrPr(); erg.matrixChild = new DifferentialMatrix[]{a,b}; return erg;}
    public static DDMatrixEntry getMatrixEntry(DifferentialMatrix a, int row, int column) {
        DDMatrixEntry erg = new DDMatrixEntry(); erg.matrixChild = new DifferentialMatrix[]{a}; erg.row = row; erg.column = column; return erg; 
    }
    
    public String toString() {return ""+val;}
    public String toDevString() {
        String erg = val+" 1st = ("; 
        for (int i=0; i<anzPar; i++) erg += dVal[i]+(i==(anzPar-1)?"), 2nd = [(":", ");
        for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) erg += ddVal[i][j]+(j==anzPar-1?(i==anzPar-1?")]":"), ("):", ");
        return erg;
    }
    
    public void eval(double[] vals) {
        setAnzParameter(vals.length);
        invalidateTree();
        double hash = 0.0; for (int i=0; i<anzPar; i++) hash += hashWeights[i] * vals[i];
        eval(vals, hash);
    }
    /** returns true if already evaluated. */
    protected boolean eval(double[] vals, double hash) {
        setAnzParameter(vals.length);
        if (diffDepth != defaultDiffDepth) {this.hash = Double.NaN; diffDepth = defaultDiffDepth; }
        prepMatrices();
        if (hash == this.hash) return true;
        this.hash = hash;
        if (child!=null) for (int i=0; i<child.length; i++) child[i].eval(vals, hash);
        if (matrixChild!=null) for (int i=0; i<matrixChild.length; i++) matrixChild[i].eval(vals, hash);
        return false;
    }

    protected void prepMatrices() {
        if ((diffDepth>0) && (anzPar>0) && ((dVal==null) || (dVal.length!=anzPar))) dVal = new double[anzPar];
        if ((diffDepth>1) && (anzPar>0) && ((ddVal==null) || (ddVal.length!=anzPar) || (ddVal[0].length!=anzPar))) ddVal = new double[anzPar][anzPar];
    }
    
    public void reset() {
        prepMatrices();
        hash = Double.NaN;
        val = Double.NaN;
        if (diffDepth>0) for (int i=0; i<anzPar; i++) dVal[i] = 0;
        if (diffDepth>1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) ddVal[i][j] = 0;
    }
    
    public int getMaxParameterNumber() {
        int erg=-1;
        if (child!=null) for (int i=0; i<child.length; i++) erg = Math.max(erg, child[i].getMaxParameterNumber());
        if (matrixChild!=null) for (int i=0; i<matrixChild.length; i++) erg = Math.max(erg, matrixChild[i].getMaxParameterNumber());
        return erg;
    }
    
    public boolean containsParameter(int nr) {
        int erg=-1;
        if (child!=null) for (int i=0; i<child.length; i++) if (child[i].containsParameter(nr)) return true;
        if (matrixChild!=null) for (int i=0; i<matrixChild.length; i++) if (matrixChild[i].containsParameter(nr)) return true;
        return false;
    }
    
    public DifferentialDouble fixParameter(int nr, double value) {
        if (child!=null) for (int i=0; i<child.length; i++) child[i] = child[i].fixParameter(nr, value);
        if (matrixChild!=null) for (int i=0; i<matrixChild.length; i++) matrixChild[i] = matrixChild[i].fixParameter(nr, value);
        return this;
    }
    public DifferentialDouble fixParameterAndReduceHigher(int nr, double value) {
        invalidateTree();
        fixParameterAndReduceHigherRecursive(nr, value);
        invalidateTree();
        return this;
    }
    protected DifferentialDouble fixParameterAndReduceHigherRecursive(int nr, double value) {
        if (hash == 0.0) return this;
        if (child!=null) for (int i=0; i<child.length; i++) child[i] = child[i].fixParameterAndReduceHigherRecursive(nr, value);
        if (matrixChild!=null) for (int i=0; i<matrixChild.length; i++) matrixChild[i] = matrixChild[i].fixParameterAndReduceHigherRecursive(nr, value);
        hash = 0.0;
        return this;
    }
    public void invalidateTree() {
        hash = Double.NaN;
        if (child!=null) for (int i=0; i<child.length; i++) child[i].invalidateTree();
        if (matrixChild!=null) for (int i=0; i<matrixChild.length; i++) matrixChild[i].invalidateTree();
    }
    
    
    public DifferentialDouble copy() {
        try {
            DifferentialDouble erg = (DifferentialDouble)clone(); 
            if (child != null) {erg.child = new DifferentialDouble[child.length]; for (int i=0; i<child.length; i++) erg.child[i] = child[i].copy();}
            if (matrixChild != null) {
                erg.matrixChild = new DifferentialMatrix[matrixChild.length]; 
                for (int i=0; i<matrixChild.length; i++) erg.matrixChild[i] = matrixChild[i].copy();
            }
            return erg;
        } catch (Exception e) {throw new RuntimeException("Problems creating a copy of "+this+".");}
    }
    
    public DifferentialDouble substitute(int[] pnr, DifferentialDouble[] vals) {
        invalidateTree();
        substituteRecursively(pnr, vals);
        invalidateTree();
        return this;
    }
    
    public DifferentialDouble substituteRecursively(int[] pnr, DifferentialDouble[] vals) {
        if (hash == 0.0) return this;
        if (child!=null) for (int i=0; i<child.length; i++) child[i] = child[i].substituteRecursively(pnr, vals);
        if (matrixChild!=null) for (int i=0; i<matrixChild.length; i++) matrixChild[i] = matrixChild[i].substituteRecursively(pnr, vals);
        hash = 0.0;
        return this;
        
    }
    
    public static DifferentialDouble fromString(String in) {
        String r = Statik.loescheRandWhitespaces(in);
        if (r.substring(0, 1).equals("X")) return new DDPara(Integer.parseInt(r.substring(1)));
        else return new DDConst(Double.parseDouble(r));
    }
    
    public boolean selfTest(int anzPar) {
        Random rand = new Random();
        double[] pos = new double[anzPar]; 
        for (int i=0; i<anzPar; i++) pos[i] =  rand.nextDouble()-0.5;
        return selfTest(pos);
    }
    public boolean selfTest(double[] pos) {
        boolean childOk = true;
        if (child!=null) for (int i=0; i<child.length; i++) childOk = (childOk && child[i].selfTest(pos));
        if (matrixChild!=null) for (int i=0; i<matrixChild.length; i++) childOk = (childOk && matrixChild[i].selfTest(pos));
        
        final DifferentialDouble fthis = this;
        AnalyticalFunction func = new AnalyticalFunction() {
            public int anzPar() {return anzPar;}
            public double eval(double[] val) {fthis.eval(val); return fthis.val;}
            public double evalDev(int[] par, double[] val) {
                fthis.eval(val);
                if (par.length==0) return fthis.val;
                if (par.length==1) return fthis.dVal[par[0]];
                if (par.length==2) return fthis.ddVal[par[0]][par[1]];
                
                return Double.NaN;
            }
        };
        boolean local = func.selftest(pos);
        System.out.println((local?"__OKAY__":"FAILURE")+" for "+this.getClass().getName()+" "+this);
        return childOk && local;
    }
    
    public static void main(String[] args) {
        DifferentialDouble v = DifferentialDouble.parameter(0);
        DifferentialDouble m = DifferentialDouble.parameter(1);
        DifferentialDouble a = DifferentialDouble.parameter(2);
        DifferentialDouble n = v.pow(-0.5).times( m.minus(a).sqr().over(v).times(-0.5).exp() );

        n.eval(new double[]{1.0,2.0,3.0});
        System.out.println(n.toDevString());
    }

}
