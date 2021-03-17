/*
 * Created on 30.11.2013
 */
package groebner;

import engine.Statik;

public class DoubleField extends Field<DoubleField> {

    public final static DoubleField ZERO = new DoubleField(0.0);
    public final static DoubleField ONE = new DoubleField(1.0);
    public final static DoubleField TWO = new DoubleField(2.0);
    
    public double value;
    public DoubleField(double value) {this.value = value;}

    @Override
    public DoubleField one() {return ONE;}
    
    @Override
    public DoubleField zero() {return ZERO;}
    
    @Override
    public DoubleField times(DoubleField second) {return new DoubleField(value * second.value);}

    @Override
    public DoubleField over(DoubleField second) {return new DoubleField(value / second.value);}

    @Override
    public DoubleField plus(DoubleField second) {return new DoubleField(value + second.value);}

    @Override
    public DoubleField minus(DoubleField second) {return new DoubleField(value - second.value);}

    @Override
    public DoubleField inverse() {return new DoubleField(1.0 / value);}

    @Override
    public DoubleField negate() {return new DoubleField(-value);}
    
    @Override
    public boolean isZero() {return value == 0.0;}
    
    public String toString() {
        return Statik.doubleNStellen(value, 3);
    }
    
}
