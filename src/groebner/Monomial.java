/*
 * Created on 13.10.2013
 */
package groebner;

import java.util.*;

import engine.Statik;

public class Monomial<F extends Field<F>> implements Comparable<Monomial<F>> {
    
    // Einige Standart - Monomordnungen
    // reine Lexikographische Ordnung
    public final static Comparator lexorder = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            if ((!(o1 instanceof int[])) || (!(o2 instanceof int[]))) throw new RuntimeException("compare in lexorder must be called with int[]");
            int[] eins = (int[])o1, zwei = (int[])o2;
            int i;
            for (i=0; (i<eins.length) && (i<zwei.length); i++) 
            {
                if (eins[i]>zwei[i]) return 1;
                if (eins[i]<zwei[i]) return -1;
            }
            if (i==eins.length) for (int j=i; j<zwei.length; j++) if (zwei[j]>0) return -1;
            if (i==zwei.length) for (int j=i; j<eins.length; j++) if (eins[j]>0) return 1;
            return 0;
        }
        public boolean equals(Object o1) {return (compare(this,o1)==0);}
    };
    // graduiert lexikographische Ordnung   
    public final static Comparator grlexorder = new Comparator() {
        public int compare(Object o1, Object o2)
        {
            if ((!(o1 instanceof int[])) || (!(o2 instanceof int[]))) throw new RuntimeException("compare in lexorder must be called with int[]");
            int[] eins = (int[])o1, zwei = (int[])o2;
            int s1=0, s2=0;
            for (int i=0; i<eins.length; i++) s1 += eins[i];
            for (int i=0; i<zwei.length; i++) s2 += zwei[i];
            if (s1 > s2) return 1;
            if (s1 < s2) return -1;
            return lexorder.compare(o1,o2);
        }
        public boolean equals(Object o1) {return (compare(this,o1)==0);}
    };
    // graduierte umgekehrt lexikographische Ordnung
    public final static Comparator<int[]> grevlexorder = new Comparator<int[]>() {
        public int compare(int[] o1, int[] o2)
        {
            int[] eins = (int[])o1, zwei = (int[])o2;
            int s1=0, s2=0;
            for (int i=0; i<eins.length; i++) s1 += eins[i];
            for (int i=0; i<zwei.length; i++) s2 += zwei[i];
            if (s1 > s2) return 1;
            if (s1 < s2) return -1;

            if (eins.length > zwei.length) for (int i=eins.length-1; i>=zwei.length; i--) if (eins[i] > 0) return -1;
            if (zwei.length > eins.length) for (int i=zwei.length-1; i>=eins.length; i--) if (zwei[i] > 0) return 1;
            for (int i=Math.min(eins.length-1,zwei.length-1); i>=0; i--)
            {
                if (zwei[i]>eins[i]) return 1;
                if (zwei[i]<eins[i]) return -1;
            }
            return 0;
        }
//      public boolean equals(int[] o1) {return (compare(this,o1)==0);}
    };
    
    
    private Comparator<int[]> defaultMonomialOrder = grevlexorder;
    
    public F coeff;
    public int[] exp;
    
    public Monomial() {this.coeff = null; this.exp = null;}
    public Monomial(F coeff, int[] exp) {this.coeff = coeff; this.exp = exp;}
    
    public Monomial(Monomial<F> toCopy) {
        exp = Statik.copy(toCopy.exp);
        coeff = toCopy.coeff;
    }
    public Monomial<F> multiply(Monomial<F> mon2) {
        Monomial<F> erg = new Monomial<F>();
        erg.coeff = coeff.times(mon2.coeff);
        erg.exp = new int[Math.max(mon2.exp.length, exp.length)]; 
        for (int i=0; i<erg.exp.length; i++) erg.exp[i] = (i < exp.length?exp[i]:0) + (i < mon2.exp.length?mon2.exp[i]:0);
        return erg;
    }
    
    public int compare(Monomial<F> monomial2) {return compare(monomial2, defaultMonomialOrder);}
    public int compare(Monomial<F> monomial2, Comparator<int[]> monomialOrder) {
        return monomialOrder.compare(this.exp, monomial2.exp);
    }

    @Override
    public int compareTo(Monomial<F> monomial2) {
        return this.compare(monomial2);
    }
    
    public boolean isDivisibleBy(Monomial<F> monomial) {
        for (int i=0; i<exp.length; i++) if (exp[i] > monomial.exp[i]) return false;
        return true;
    }
    
    public Polynomial<F,?> reduceBy(Polynomial<F,?> p) {
        Monomial<F> lead = p.leadingMonomial();
        int[] diff = new int[exp.length];
        for (int i=0; i<exp.length; i++) {diff[i] = exp[i] - lead.exp[i]; if (diff[i] < 0) return null;}
        return p.monomialMultiply(new Monomial<F>(coeff.over(lead.coeff), diff));
    }
    public Monomial<F> negate() {
        Monomial<F> erg = new Monomial<F>(this);
        erg.coeff = erg.coeff.negate();
        return erg;
    }
    
    public static boolean isSmaller(int[] first, int[] second) {
        for (int i=0; i<first.length; i++) if (first[i] > second[i]) return false;
        return true;
    }
    
    public String toString() {
        String erg = coeff+" X^{";
        for (int i=0; i<exp.length; i++) erg += exp[i]+(i==exp.length-1?"":" ");
        erg += "}";
        return erg;
    }
}
