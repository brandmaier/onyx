/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

public class DDPow extends DifferentialDouble {
    public double exponent;
    
    public DDPow(double exponent) {this.exponent = exponent;}
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        val = Math.pow(child[0].val,exponent);
        if (diffDepth>0) for (int i=0; i<anzPar; i++) dVal[i] = child[0].dVal[i] * exponent * Math.pow(child[0].val,exponent-1);
        if (diffDepth>1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) ddVal[i][j] = exponent*
            (child[0].ddVal[i][j]*Math.pow(child[0].val,exponent-1) + child[0].dVal[i]*child[0].dVal[j]*(exponent-1)*Math.pow(child[0].val,exponent-2));
        return false;
    }
    public String toString() {return "("+child[0]+")^{"+exponent+"}";}

}
