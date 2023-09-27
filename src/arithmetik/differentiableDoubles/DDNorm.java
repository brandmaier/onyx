/*
 * Created on 07.07.2010
 */
package arithmetik.differentiableDoubles;

public class DDNorm extends DDPow {
    public DDNorm() {super(0.5);}
    public String toString() {return "norm("+child[0].child[0]+")";}
}
