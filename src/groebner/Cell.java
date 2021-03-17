/*
 * Created on 01.12.2013
 */
package groebner;

import java.util.Comparator;

import engine.Statik;

public class Cell<F extends Field<F>> implements Comparable<Cell> {
    
    public final Comparator<int[]> monomialOrder = Monomial.grevlexorder;
    public final int hashMod = 124351843;
    public final int[] hashWeights = new int[]{1%hashMod,20%hashMod,400%hashMod,8000%hashMod,160000%hashMod,3200000%hashMod,64000000%hashMod,100%hashMod,2000%hashMod,
               40000%hashMod,800000%hashMod,16000000%hashMod,1000%hashMod,2000%hashMod,400000%hashMod,8000000%hashMod};
    
    public int[] exp;
    public int polynomial;
    public int iteratorNumber;
    public ListPolynomial<F> reduced;
    
    public Cell(int[] exp) {this.exp = exp; polynomial = -1; iteratorNumber = 0; reduced = null;}

    public boolean isStarted() {return (reduced == null && iteratorNumber > 0);}
    public boolean isFinished() {return reduced != null;}
    public boolean isUnreducable() {return polynomial == -1;}
    
    @Override
    public int compareTo(Cell otherCell) {
        return monomialOrder.compare(this.exp, otherCell.exp);
    }
    
    @Override 
    public boolean equals(Object otherCell) {
        if (otherCell instanceof Cell) {
            return (compareTo((Cell)otherCell) == 0);
        }
        return false;
    }
    
    @Override
    public int hashCode() {
        int res = 0;
        for (int i=0; i<exp.length; i++) res = (res+exp[i]*(i>=hashWeights.length?1:hashWeights[i])) % hashMod;
        return res;
    }
    
    public String toString() {
        return "{"+Statik.matrixToString(exp)+"}";
    }

}
