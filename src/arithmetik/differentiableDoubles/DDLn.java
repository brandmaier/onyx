/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

public class DDLn extends DifferentialDouble {
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        val = Math.log(child[0].val);
        if (diffDepth>0) for (int i=0; i<anzPar; i++) dVal[i] = child[0].dVal[i]/child[0].val;
        if (diffDepth>1) for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) ddVal[i][j] = 
            (child[0].ddVal[i][j]*child[0].val-child[0].dVal[i]*child[0].dVal[j])/(child[0].val*child[0].val);
        return false;
    }
    public String toString() {return "ln("+child[0]+")";}

}
