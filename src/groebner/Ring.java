/*
 * Created on 30.11.2013
 */
package groebner;

public abstract class Ring<R extends Ring<R>> {

    public abstract R plus(R second);
    public R minus(R second) {return plus(second.negate());}
    public abstract R negate();
    public abstract boolean isZero();
    public abstract R zero();
    public abstract R one();
    public abstract R times(R second);
}
