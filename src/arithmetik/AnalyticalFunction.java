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
 * Created on 20.03.2010
 */
package arithmetik;

import engine.Statik;

public abstract class AnalyticalFunction 
{
    protected double EPS = 0.0001;
    
    /**
     * Evaluates the function at val.
     * 
     * @param val
     * @return
     */
    public abstract double eval(double[] val);
    public double eval(double val) {return eval(new double[]{val});}

    /**
     * Evaluates with parameter pnr set to val and the remaining in the oder or remainingVal. Should be overwritten for efficiency. 
     * 
     * @param pnr
     * @param val
     * @param remainingVal
     * @return
     */
    public double eval(int pnr, double val, double[] remainingVal) {
        double[] rval= new double[remainingVal.length+1];
        for (int i=0; i<remainingVal.length; i++) rval[(i<pnr?i:i-1)] = remainingVal[i];
        rval[pnr] = val;
        return eval(rval);
    }
    
    /**
     * Evaluates the 1st deriviative wrt. the first parameter. Assumes there is only one. 
     * 
     * @param val
     * @return
     */
    public double evalDev(double val) {return evalDev(0, new double[] {val});}
    
    /**
     * Evaluates derivative with parameter pnr set to val and the remaining in the oder or remainingVal. Should be overwritten for efficiency. 
     * 
     * @param pnr
     * @param val
     * @param remainingVal
     * @return
     */
    public double evalDev(int pnr, int specpnr, double val, double[] remainingVal) {
        double[] rval= new double[remainingVal.length+1];
        for (int i=0; i<remainingVal.length; i++) rval[(i<specpnr?i:i-1)] = remainingVal[i];
        rval[specpnr] = val;
        return evalDev(pnr, rval);
    }

    /**
     * Evaluates the first derivative wrt. par at val. If not overwritten, derivative will be found numerically using precision epsilon.
     * 
     * @param par
     * @param val
     * @return
     */
    public double evalDev(int par, double[] val) {return evalDev(new int[]{par}, val);}
    
    /**
     * Evaluates the first derivative wrt. par at val numerically using precision epsilon.
     * 
     * @param par
     * @param val
     * @return
     */
    public double evalDevNumerically(int par, double[] val) {return evalDevNumerically(new int[]{par}, val);}

    
    /**
     * Evaluates the n-th derivative wrt. par[0],...,par[n-1] at val. If not overwritten, derivative will be found numerically using precision epsilon.  
     * @param par
     * @param val
     * @return
     */
    public double evalDev(int[] par, double[] val) {return evalDevNumerically(par, val);}
    
    /**
     * Evaluates the n-th derivative wrt. par[0],...,par[n-1] at val numerically using precision epsilon.  
     * @param par
     * @param val
     * @return
     */
    public double evalDevNumerically(int[] par, double[] val)
    {
        if (par.length==0) return eval(val);

        int[] sub = new int[par.length-1];
        for (int i=0; i<par.length-1; i++) sub[i] = par[i];
        val[par[par.length-1]] -= EPS/2.0;  double v1 = evalDevNumerically(sub, val); 
        val[par[par.length-1]] += EPS;      double v2 = evalDevNumerically(sub, val);
        val[par[par.length-1]] -= EPS/2.0;
        return (v2-v1)/EPS;
    }
    
    /** set epsilon for the precision of numerical derivatie **/
    public void setEpsilon(double eps) {this.EPS = eps;}
    /** epsilon for the precision of numerical derivatie **/
    public double getEpsilon() {return EPS;}
    /** number of parameters of this function. **/
    public abstract int anzPar();
    
    public AnalyticalFunction numericalCopy()
    {
        final AnalyticalFunction i = this;
        return new AnalyticalFunction() {
            public int anzPar() {return i.anzPar();}
            public double eval(double[] pos) {return i.eval(pos);}
        };
    }
    
    public boolean selftest() {return selftest(null);}
    public boolean selftest(double[] pos)
    {
        double[] val;
        if (pos==null) 
        {
            pos = new double[anzPar()];
            java.util.Random rand = new java.util.Random(); 
            for (int i=0; i<pos.length; i++) pos[i] = (rand.nextGaussian()-0.5)*100;  
        } 
        val = Statik.copy(pos);
        //        AnalyticalFunction num = numericalCopy();
        double[] symD = new double[anzPar()], numD = new double[anzPar()];
        double[][] symDD = new double[anzPar()][anzPar()], numDD = new double[anzPar()][anzPar()];
        for (int i=0; i<anzPar(); i++)
        {
            symD[i] = evalDev(i,val); 
            numD[i] = evalDevNumerically(i, val); Statik.copy(pos,val);
            for (int j=0; j<anzPar(); j++) {
                symDD[i][j] = evalDev(new int[]{i,j},val); 
                numDD[i][j] = evalDevNumerically(new int[]{i,j}, val); 
                Statik.copy(pos,val);
            } 
        }
        System.out.println("Value                 = "+Statik.doubleNStellen(eval(val), 3));
        System.out.println("Numerical  Derivative = "+Statik.matrixToString(numD));
        System.out.println("Symbolical Derivative = "+Statik.matrixToString(symD));
        System.out.println("Numerical  2nd Derivative = \r\n"+Statik.matrixToString(numDD));
        System.out.println("Symbolical 2nd Derivative = \r\n"+Statik.matrixToString(symDD));
        boolean erg = true;
        for (int i=0; i<numD.length; i++) 
            if ((Math.abs(numD[i]-symD[i])>0.01) && ((numD[i]==0) || (symD[i]==0) || ( (symD[i]/numD[i]>1?symD[i]/numD[i]:numD[i]/symD[i]) < 0.99))) erg = false;
        for (int i=0; i<numDD.length; i++) for (int j=0; j<numDD[i].length; j++) 
            if ((Math.abs(numDD[i][j]-symDD[i][j])>0.01) && 
                    ((numDD[i][j]==0) || (symDD[i][j]==0) || ( (symDD[i][j]/numDD[i][j]>1?symDD[i][j]/numDD[i][j]:numDD[i][j]/symDD[i][j]) < 0.99))) erg = false;
        return erg;
    }
    
    
    public AnalyticalFunction fixParameter(int nr, double val)
    {
        return fixParameters(new int[] {nr}, new double[] {val});
        /* old inner class solution
        final AnalyticalFunction fthis = this;
        final int fnr = nr;
        final double fval = val;
        return new AnalyticalFunction(){
            private final int anzPar = fthis.anzPar()-1;
            private final double[] values = new double[fthis.anzPar()];
            private void fillValues(double[] val) {for (int i=0; i<anzPar; i++) values[(i<fnr?i:i+1)] = val[i]; values[fnr]=fval;}
            public double eval(double[] val) {fillValues(val); return fthis.eval(values);}
            public double evalDev(int pnr, double[] val) {fillValues(val); return fthis.evalDev((pnr<fnr?pnr:pnr+1),values);}
            public double evalDev(int[] pnr, double[] val) 
            {
                fillValues(val);
                for (int i=0; i<pnr.length; i++) if (pnr[i]>fnr) pnr[i]++;
                double erg = fthis.evalDev(pnr,values);
                for (int i=0; i<pnr.length; i++) if (pnr[i]>fnr) pnr[i]--;
                return erg;
            }
            public int anzPar() {return anzPar;}
            @SuppressWarnings("unused")
            public AnalyticalFunction getParent() {return fthis;};
        };
        */
    }

    /**
     * Can be overridden if more efficient methods exist to compute the gradient.
     * 
     * @param vals
     * @return
     */
    public double[] getGradient(double[] vals) {
        double[] erg = new double[anzPar()];
        for (int i=0; i<erg.length; i++) erg[i] = evalDev(i, vals);
        return erg;
    }
    
    public AnalyticalFunctionPartlyFixed fixParameters(int[] nr, double[] val)
    {
        return new AnalyticalFunctionPartlyFixed(this, nr, val);
    }
    
    /**
     * returns a single Newton step assuming there is only one parameter value.
     *  
     * @param pos
     * @param target
     * @return
     */
    public double newtonStep(double pos, double target) {
        double y = eval(pos)-target; if (y==0.0) return 0.0;
        double yDev = evalDev(pos);
        double erg = -y/yDev;
        return erg;
    }
    
    public AnalyticalFunction exchangeOrderOfParameter(int[] newOrder) {
        final int[] fNewOrder = newOrder;
        final AnalyticalFunction fthis = this;
        return new AnalyticalFunction() {

            private double[] exchangeValues = new double[fthis.anzPar()];
            
            public void changeToNew(double[] val) {
                for (int i=0; i<fthis.anzPar(); i++) exchangeValues[i] = val[fNewOrder[i]];  
            }
            
            @Override
            public double eval(double[] val) {changeToNew(val); return fthis.eval(exchangeValues);}
            
            @Override
            public double evalDev(int pnr, double[] val) {changeToNew(val); return fthis.evalDev(pnr, exchangeValues);}
            
            @Override
            public double evalDev(int[] pnr, double[] val) {changeToNew(val); return fthis.evalDev(pnr, exchangeValues);}
            
            @Override
            public int anzPar() {return fthis.anzPar();}
        };
    }
    
    
}
