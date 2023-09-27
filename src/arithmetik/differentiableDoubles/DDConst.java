/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

public class DDConst extends DifferentialDouble {
    
    public double constant;
    
    public DDConst(double val) {super(); this.constant = val;}
    public boolean eval(double[] vals, double hash) {
        if (super.eval(vals, hash)) return true;
        val = constant;
        return false;
    }
    
    public String toString() {return " "+constant+" ";}    
}
