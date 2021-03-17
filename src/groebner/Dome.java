/*
 * Created on 01.12.2013
 */
package groebner;

import java.util.Hashtable;
import java.util.Stack;

/**
 * 
 * @author timo
 */
public class Dome<F extends Field<F>> {
    
    public F fieldOne;
    
    public Hashtable<Cell<F>, Cell<F>> cells;
    public Stack<Cell<F>> unfinished;
    public Monomial<F>[][] generator;
    public int steps;
    
    public Dome(Monomial<F>[][] generator, F fieldRepresentant) {
        cells = new Hashtable<Cell<F>, Cell<F>>();
        this.generator = generator;
        unfinished = new Stack<Cell<F>>();
        fieldOne = fieldRepresentant.one();
        steps = 0;
    }

    public static int[] getExponentTimesLeading(int[] target, int[] leading, int[] add) {
        int[] erg = new int[target.length];
        for (int i=0; i<erg.length; i++) erg[i] = target[i] - leading[i] + add[i];
        return erg;
    }
    
    public void stepUntilFinish() {
        while (!step()) steps++;
    }
    
    public boolean step() {
        if (unfinished.isEmpty()) return true;
        Cell<F> cell = unfinished.peek();
        cell.iteratorNumber++;
        if (cell.iteratorNumber >= generator[cell.polynomial].length) finalizeTopCell();
        else {
            int[] newExp = getExponentTimesLeading(cell.exp, generator[cell.polynomial][0].exp, generator[cell.polynomial][cell.iteratorNumber].exp);
            enterCell(newExp);
        }
        return false;
    }
    
    public void finalizeTopCell() {
        Cell<F> cell = unfinished.pop();
        Monomial<F>[] pol = generator[cell.polynomial];
        F negativeOneOverLeadingCoefficient = pol[0].coeff.inverse().negate();
        ListPolynomial<F>[] toMerge = new ListPolynomial[pol.length-1];
        for (int i=1; i<pol.length; i++) {
            int[] newExp = getExponentTimesLeading(cell.exp, pol[0].exp, pol[i].exp);
            Cell<F> key = new Cell<F>(newExp);
            toMerge[i-1] = cells.get(key).reduced.scalarMultiply(pol[i].coeff.times(negativeOneOverLeadingCoefficient));
        }
        cell.reduced = toMerge[0].add(toMerge);
    }
    
    private void enterSPolynomial(int one, int two) {
        int[] kgv = new int[generator[one][0].exp.length];
        for (int i=0; i<generator[one][0].exp.length; i++) kgv[i] = Math.max(generator[one][0].exp[i], generator[two][0].exp[i]);
        for (int i=1; i<generator[one].length; i++) enterCell(getExponentTimesLeading(kgv, generator[one][0].exp, generator[one][i].exp));
        for (int i=1; i<generator[two].length; i++) enterCell(getExponentTimesLeading(kgv, generator[two][0].exp, generator[two][i].exp));
    }
    
    public ListPolynomial<F> collectSPolynomial(int one, int two) {
        boolean ggTOne = true;
        for (int i=0; i<generator[one][0].exp.length; i++) if (generator[one][0].exp[i]>0 && generator[two][0].exp[i]>0) ggTOne = false;
        if (ggTOne) return new ListPolynomial<F>(fieldOne.zero());
        
        enterSPolynomial(one, two);
        stepUntilFinish();
        int[] kgv = new int[generator[one][0].exp.length];
        for (int i=0; i<generator[one][0].exp.length; i++) kgv[i] = Math.max(generator[one][0].exp[i], generator[two][0].exp[i]);
        ListPolynomial<F>[] toMerge = new ListPolynomial[generator[one].length+generator[two].length-2];
        F leadingCoefficientOne = generator[one][0].coeff.negate();
        F leadingCoefficientTwo = generator[two][0].coeff;
        int inC = 0;
        for (int i=1; i<generator[one].length; i++) {
            int[] newExp = getExponentTimesLeading(kgv, generator[one][0].exp, generator[one][i].exp);
            Cell<F> key = new Cell<F>(newExp);
            toMerge[inC++] = cells.get(key).reduced.scalarMultiply(generator[one][i].coeff.times(leadingCoefficientTwo));
        }
        for (int i=1; i<generator[two].length; i++) {
            int[] newExp = getExponentTimesLeading(kgv, generator[two][0].exp, generator[two][i].exp);
            Cell<F> key = new Cell<F>(newExp);
            toMerge[inC++] = cells.get(key).reduced.scalarMultiply(generator[one][i].coeff.times(leadingCoefficientOne));
        }
        ListPolynomial<F> erg = toMerge[0].add(toMerge);
        return erg;
    }
    
    public void extendToGroebner() {
        int c1 = 0, c2 = 1;
        while (c1 < generator.length-1) {
            ListPolynomial<F> reducedS = collectSPolynomial(c1,c2);
            if (!reducedS.isZero()) {
                Monomial<F>[][] newGenerator = new Monomial[generator.length+1][];
                for (int i=0; i<generator.length; i++) newGenerator[i] = generator[i];
                newGenerator[generator.length] = reducedS.toArray();
                generator = newGenerator;
            }
            c2++; if (c2 >= generator.length) {c1++; c2 = c1+1;}
        }
    }
    
    public Cell<F> enterCell(int[] exp) {
        Cell<F> erg = new Cell<F>(exp);
        Cell<F> alreadyThere = cells.get(erg);
        if (alreadyThere != null) return alreadyThere;
        cells.put(erg, erg);
        selectGenerator(erg);
        if (erg.polynomial == -1) {
            erg.reduced = new ListPolynomial<F>(fieldOne.zero());
            erg.reduced.addMonomial(new Monomial<F>(fieldOne, exp));
        } else {
            unfinished.add(erg);
        }
        return erg;
    }
    
    public void selectGenerator(Cell<F> cell) {
        int bestNr = -1, bestAnz = Integer.MAX_VALUE;
        for (int i=0; i<generator.length; i++)
            if (Monomial.isSmaller(generator[i][0].exp, cell.exp) && generator[i].length < bestAnz) {bestAnz = generator[i].length; bestNr = i;}
        cell.polynomial = bestNr;
    }
    
    public String toString() {
        return "Dome size = "+cells.size()+", unfinished = "+unfinished.size()+".";
    }
}
