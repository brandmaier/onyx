/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

public class DDPara extends DifferentialDouble {
    
    public int nr;
    
    public DDPara(int nr) {super(); this.nr = nr;}
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        val = vals[nr]; 
        if (diffDepth>0) dVal[nr] = 1;
        return false;
    }
    public String toString() {return "X"+nr;}
    public int getMaxParameterNumber() {return nr;}
    public boolean containsParameter(int nr) {return (this.nr == nr);}
    public DifferentialDouble fixParameter(int nr, double value) {if (this.nr!=nr) return this; else return new DDConst(value);}
    protected DifferentialDouble fixParameterAndReduceHigherRecursive(int nr, double value) {
        if (this.hash == 0.0) return this;
        this.hash = 0.0;
        if (this.nr<nr) return this; else if (this.nr==nr) return new DDConst(value); else return new DDPara(this.nr-1);
    }
    public DifferentialDouble substituteRecursively(int[] pnr, DifferentialDouble[] vals) {
        if (hash == 0.0) return this;
        this.hash = 0.0;
        for (int i=0; i<pnr.length; i++) if (this.nr == pnr[i]) {
            DifferentialDouble erg = vals[i].copy();
            erg.hash = 0.0;
            return erg;
        }
        return this;
    }
    
}
