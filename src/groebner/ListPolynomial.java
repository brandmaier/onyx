/*
 * Created on 30.11.2013
 */
package groebner;

import java.util.Arrays;
import java.util.Iterator;
import java.util.TreeSet;

import engine.Statik;

public class ListPolynomial<F extends Field<F>> extends Polynomial<F, ListPolynomial<F>> {

    private F field;
    
    // This is a sorted list which automatically sorts by compare of monomials. The method "first" returns the lowest element, "last" the leading
    // monomial. When accessing the TreeSet in a debugger, note that the order is inverted (ascending). 
    public TreeSet<Monomial<F>> monomials;
    
    public ListPolynomial(F fieldValue) {
        this.field = fieldValue.zero(); 
        monomials = new TreeSet<Monomial<F>>(); 
        if (!fieldValue.isZero()) addMonomial(new Monomial<F>(field, new int[0]));
    }
    
    public ListPolynomial(Monomial<F> monom) {
        this(monom.coeff.zero());
        addMonomial(monom);
    }
    
    public ListPolynomial(ListPolynomial<F> toCopy) {
        this(toCopy.field);
        for (Monomial<F> monomial:toCopy.monomials) addMonomial(monomial);
    }
    
    public ListPolynomial(F coeff, int variable) {
        this(coeff.zero());
        int[] exp = new int[variable+1]; exp[variable] = 1;
        Monomial<F> m = new Monomial<F>(coeff, exp);
        addMonomial(m);
    }
    
    @Override
    public ListPolynomial<F> zero() {return new ListPolynomial<F>(field.zero());}
    
    @Override
    public ListPolynomial<F> one() {return new ListPolynomial<F>(field.one());}
    
    @Override
    public F leadingCoefficient() {return monomials.last().coeff;}

    @Override
    public Monomial<F> leadingMonomial() {return monomials.last();}

    @Override
    public int[] leadingExponents() {return monomials.last().exp;}

    @Override
    public ListPolynomial<F> monomialMultiply(Monomial<F> monom) {
        ListPolynomial<F> erg = zero();
        for (Monomial<F> m: monomials) {
            erg.addMonomial(monom.multiply(m));
        }
        return erg;
    }
    
    @Override
    public void addMonomial(Monomial<F> monom) {
        Monomial<F> candidate = monomials.floor(monom);
        if (candidate != null && candidate.equals(monom)) candidate.coeff = candidate.coeff.plus(monom.coeff);
        else monomials.add(monom);
    }

    @Override
    public boolean isZero() {return monomials.size()==0;}

    @Override
    public ListPolynomial<F> negate() {
        ListPolynomial<F> erg = zero();
        Iterator<Monomial<F>> it = monomials.iterator();
        while (it.hasNext()) erg.addMonomial(it.next().negate());
        return erg;
    }

    public ListPolynomial<F> scalarMultiply(F scalar) {
        ListPolynomial<F> erg = zero();
        for (Monomial<F> m: monomials) {
            erg.addMonomial(new Monomial<F>(m.coeff.times(scalar), m.exp));
        }
        return erg;
    }
    
    // TODO: das geht natürlich besser!
    public ListPolynomial<F> add(ListPolynomial<F>[] summand) {
        if (summand.length == 0) return zero();
        if (summand.length == 1) return summand[0];
        ListPolynomial<F> erg = summand[0].plus(summand[1]);
        for (int i=2; i<summand.length; i++) erg.add(summand[i]);
        return erg;
    }
    
    @Override
    public ListPolynomial<F> plus(ListPolynomial<F> second) {
        ListPolynomial<F> erg = new ListPolynomial<F>(this);
        erg.add(second);
        return erg;
    }

    public void add(ListPolynomial<F> arg) {
        Iterator<Monomial<F>> it1 = this.monomials.descendingIterator(); 
        Iterator<Monomial<F>> it2 = arg.monomials.descendingIterator();
        Monomial<F> top1 = (it1.hasNext()?it1.next():null), top2 = (it2.hasNext()?it2.next():null);
        ListPolynomial<F> erg = zero();
        
        while (top1 != null && top2 != null) {
            if (top1.compareTo(top2) == 0) {
                F newcoeff = top1.coeff.plus(top2.coeff);
                if (!newcoeff.isZero()) erg.addMonomial(new Monomial<F>(newcoeff, Statik.copy(top1.exp)));
                top1 = (it1.hasNext()?it1.next():null); top2 = (it2.hasNext()?it2.next():null);
            }
            else if (top1.compare(top2) == 1) {erg.monomials.add(new Monomial<F>(top1)); top1 = (it1.hasNext()?it1.next():null);}
            else                              {erg.monomials.add(new Monomial<F>(top2)); top2 = (it2.hasNext()?it2.next():null);}
        }
        if (top1 != null) {erg.monomials.add(top1); while (it1.hasNext()) erg.monomials.add(it1.next()); }
        if (top2 != null) {erg.monomials.add(top2); while (it2.hasNext()) erg.monomials.add(it2.next()); }
        this.monomials = erg.monomials;
    }
    
    @Override
    public ListPolynomial<F> times(ListPolynomial<F> second) {
        ListPolynomial<F> erg = new ListPolynomial<F>(this);
        erg.multiply(second);
        return erg;
    }
    
    public void multiply(ListPolynomial<F> arg) {
        if (this.isZero() || arg.isZero()) {monomials.clear(); return;}
        ListPolynomial<F> copy = new ListPolynomial<F>(this);
        monomials.clear();
        for (Monomial<F> monom:arg.monomials) 
            add(copy.monomialMultiply(monom));
    }
    
    public Monomial<F>[] toArray() {
        Monomial<F>[] erg = monomials.toArray(new Monomial[monomials.size()]);
        return erg;
    }
    
    public String toString() {
        if (this.isZero()) return "0";
        String erg = "";
        boolean isFirst = true;
        Iterator<Monomial<F>> iter = monomials.descendingIterator();
        while (iter.hasNext()) {Monomial<F> monom =iter.next(); erg += (isFirst?"":" + ")+monom; isFirst = false;}
        return erg;
    }
}

