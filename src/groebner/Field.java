/*
 * Created on 30.11.2013
 */
package groebner;

public abstract class Field<F extends Field<F>> extends Ring<F> {

    public abstract F times(F second);
    public F over(F second) {return times(second.inverse());}
    public abstract F inverse();
    public abstract F one();
}
