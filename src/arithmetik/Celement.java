package arithmetik;

// Komplexe Zahlen mit einem Unsicherheitsfaktor, dargestellt durch 
// zwei RElement.

import java.util.*;
import java.math.*;

public class Celement implements Field, DoubleNormable, Squarerootable, Complex
{
	static final Celement ZERO = (new Celement());
	static final Celement ONE  = (new Celement(1));
	static final Celement TWO  = (new Celement(2));
	static final Celement HALF = (new Celement(Qelement.HALF));
	
	static final Qelement DEFAULEPSILON = new Qelement(1,Integer.MAX_VALUE);
		
	Relement reel;
	Relement imag;
	
	public Celement () {this(0,0);}
	public Celement (double reel)
	{
		this(reel,0);
	}
	public Celement (double reel, double imag) 
	{
		this(new Relement(reel),new Relement(imag));
	}
	public Celement (long reel)
	{
		this(reel,0);
	}
	public Celement (long reel, long imag)
	{
		this(new Relement(reel),new Relement(imag));
	}
	public Celement (Celement copy)
	{
		this(copy.reel, copy.imag);
	}
	public Celement (Qelement q)
	{
		this(new Relement(q), new Relement());
	}
	public Celement (Relement reel, Relement imag)
	{
		this.reel = reel;
		this.imag = imag;
	}
	public Relement abs()
	{
		return (reel.sqr().add(imag.sqr())).sqrt();
	}
	public Ring abs_add (Ring b) {return add((Celement)b);}
	public Field abs_divide (Field b) {return divide((Celement)b);}
	public GcdAble abs_divide(GcdAble arg2) {return divide((Celement)arg2);}
	public GcdAble[] abs_divideAndRemainder(GcdAble arg2) 
	{
		GcdAble[] erg = new GcdAble[2]; erg[0] = divide((Celement)arg2); erg[1] = zero();
		return erg;
	}
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 13:11:56)
 * @return double
 */
public double abs_doubleNorm() 
{
	return imagValue() * imagValue() + reelValue() * reelValue();
}
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 12:59:45)
 * @return arithmetik.Complex
 * @param reel double
 * @param imag double
 */
public Complex abs_fromDouble(double reel, double imag) 
{
	return fromDouble(reel, imag);
}
	public GcdAble abs_gcd(GcdAble arg2) {return unit();}
	public boolean abs_isEqual (Ring b) {return equals((Celement)b);}
	public Ring abs_multiply (Ring b) {return multiply ((Celement)b);}
	public Ring abs_negate () {return negate();}
	public Ring abs_pow(long exp) {return pow(exp);}
	public Field abs_reciprocal () {return reciprocal(); }
	public GcdAble abs_remainder(GcdAble arg2) {return zero();}
	public GcdAble abs_scm(GcdAble arg2) {return unit();}
	public Squarerootable abs_sqrt() {return (Squarerootable)this.sqrt(); }
	public Ring abs_subtract (Ring b) {return subtract ((Celement)b);}
	public Ring abs_unit () {return unit ();}
	public Ring abs_zero () {return zero ();}
	public Celement add (Celement z)
	{
		return new Celement(reel.add(z.reel),imag.add(z.imag));
	}
	// liefert wahr falls beide Grenzen beider Zahlen (Imaginaer- und 
	// Realteil) höchstens epsilon voneinander entfernt sind.
	public boolean bordersClose(Celement z, Qelement epsilon)
	{
		return (   (this.reel.getHigher().subtract(z.reel.getHigher()).abs().compareTo(epsilon)==-1)
				&& (this.reel.getLower ().subtract(z.reel.getLower() ).abs().compareTo(epsilon)==-1)
				&& (this.imag.getHigher().subtract(z.imag.getHigher()).abs().compareTo(epsilon)==-1)
				&& (this.imag.getLower ().subtract(z.imag.getLower() ).abs().compareTo(epsilon)==-1) );
	}
	public Complex conjugate()
	{
		return new Celement(reel, imag.negate());
	}
	public Celement divide(Celement z)
	{
		return multiply(z.reciprocal());
	}
	// Verschenkt die Grenzen
	public double doubleNorm()
	{
		return abs().doubleValue();
	}
	public boolean equals (Celement z)
	{
		return (subtract(z).isZero());
	}
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 12:56:31)
 * @param reel double
 * @param imag double
 */
public static Celement fromDouble(double reel, double imag) 
{
	return new Celement(reel, imag);	
}
	public double getRange()
	{
		double r = reel.getRange(), i = imag.getRange();
		return Math.sqrt(r*r+i*i);
	}
	public double imagValue() {return imag.doubleValue();}
	public BigInteger[] includedIntegerPoints()
	{
		if (imag.signum()!=0) return new BigInteger[0];
		
		// start und ende enthalten die Intervallgrenzen, start eingeschlossen, ende ausgeschlossen.
		BigInteger start = reel.floor();
		if (!(new Qelement(start)).equals(reel.getLower()) ) start = start.add(BigInteger.valueOf(1));
		BigInteger ende = reel.ceil();
		if ((new Qelement(ende)).equals(reel.getHigher()) ) ende = ende.add(BigInteger.valueOf(1));
		
		int bereich = ende.subtract(start).intValue();
		
		BigInteger[] erg = new BigInteger[bereich];
		for (int i=0; i<bereich; i++)
			erg[i] = BigInteger.valueOf(i).add(start);
		return erg;				
	}
	public boolean isZero()
	{
		return ((reel.signum() == 0) && (imag.signum()==0));
	}
	public boolean isZero(Qelement epsilon)
	{
		return reel.sqr().add(imag.sqr()).subtract(new Relement(epsilon.sqr())).signum()!=1;
	}
	public Celement multiply (Celement z)
	{
		return new Celement(reel.multiply(z.reel).subtract(imag.multiply(z.imag)),
							reel.multiply(z.imag).add(imag.multiply(z.reel)));
	}
	public Celement negate ()
	{
		return new Celement(reel.negate(),imag.negate());
	}
	public Celement pow(long n)
	{
		if (n==0) return new Celement(1);
		if (n==1) return new Celement(this);
		long h = n/2;
		return pow(h).multiply(pow(n-h));
	}
	public Celement reciprocal ()
	{
		Relement abssqr = reel.sqr().add(imag.sqr());
		return new Celement (reel.divide(abssqr),imag.divide(abssqr).negate());
	}
	public double reelValue() {return reel.doubleValue();}
	public Celement sqr()
	{
		return multiply(this);
	}
	public Celement sqrt() {return sqrt(this.DEFAULEPSILON);}
	public Celement sqrt(Qelement epsilon)
	{
		Celement alt = new Celement(1,1);
		Celement neu = alt.add(this.divide(alt)).divide(new Celement(2));
		while (!neu.bordersClose(alt,epsilon))
		{
			alt = neu;
			neu = alt.add(this.divide(alt)).divide(new Celement(2));
		}
		Qelement rh, rl, ih, il;
		if (alt.reel.getHigher().compareTo(neu.reel.getHigher()) == 1) rh = alt.reel.getHigher(); else rh = neu.reel.getHigher();
		if (alt.reel.getLower ().compareTo(neu.reel.getHigher()) ==-1) rl = alt.reel.getLower (); else rl = neu.reel.getLower ();
		if (alt.imag.getHigher().compareTo(neu.imag.getHigher()) == 1) ih = alt.imag.getHigher(); else ih = neu.imag.getHigher();
		if (alt.imag.getLower ().compareTo(neu.imag.getHigher()) ==-1) il = alt.imag.getLower (); else il = neu.imag.getLower ();
		return new Celement( new Relement(rl,rh), new Relement(il,ih));
	}
	public Celement subtract (Celement z)
	{
		return new Celement(reel.subtract(z.reel),imag.subtract(z.imag));
	}
	public String toDoubleString()
	{
		String erg = "("+reel.toDouble();
		if (!(imag.toDouble() < 0.0)) erg += "+";
		return erg+imag.toDouble()+"i +- "+getRange()+") ";
	}
	public String toString()
	{
		return "("+reel+"+"+imag+"i)";
	}
	// Hack ^10!
	public Celement trustCenter()
	{
		Celement erg = new Celement(this);
		erg.reel = reel.trustCenter();
		erg.imag = imag.trustCenter();
		return erg;
	}
	public Celement unit()
	{
		return new Celement(1,0);
	}
	public Celement zero()
	{
		return new Celement(0,0);
	}
}
