/*
 * Created on 13.10.2013
 */
package groebner;

//import arithmetik.*;

public abstract class Polynomial<F extends Field<F>, THISTYPE extends Polynomial<F,THISTYPE>> extends Ring<THISTYPE> {
    
    public abstract Monomial<F> leadingMonomial();
    public abstract void addMonomial(Monomial<F> m);
    public abstract Polynomial<F,THISTYPE> monomialMultiply(Monomial<F> monom);
    
    public F leadingCoefficient() {return leadingMonomial().coeff;}
    public int[] leadingExponents() {return leadingMonomial().exp;}
}
