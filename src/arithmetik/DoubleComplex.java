package arithmetik;

import java.util.*;
import java.math.*;

// Implementiert Signed mit abs_abs = absolute und abs_signum(x) = x / absolute(x);
// Implementiert Orderd mit Vergleich der Beträge
public class DoubleComplex implements Field, Squarerootable, UnitRootComplete, 
							DoubleNormable, Complex, Signed
{
	public double reel;
	public double imag;
	
	static DoubleComplex[] unitRoots = {new DoubleComplex(1,0)};
	
	public DoubleComplex () {this(0,0);}
	public DoubleComplex (double reel) {this(reel,0.0);}
	public DoubleComplex (double reel, double imag)
	{
		this.reel = reel;
		this.imag = imag;
	}
	public DoubleComplex (DoubleComplex copy) {this(copy.reel, copy.imag);}
	public Signed abs_abs() {return new DoubleComplex(absolute(), 0.0);}
	public Ring abs_add (Ring b) {return add((DoubleComplex)b);}
	public int abs_compareTo(Orderd snd) {return (new DoubleWrapper(absolute())).abs_compareTo(new DoubleWrapper(((DoubleComplex)snd).absolute()));}
	public Field abs_divide (Field b) {return divide((DoubleComplex)b);}
	public GcdAble abs_divide(GcdAble arg2) {return divide((DoubleComplex)arg2);}
	public GcdAble[] abs_divideAndRemainder(GcdAble arg2) 
	{
		GcdAble[] erg = new GcdAble[2]; erg[0] = divide((DoubleComplex)arg2); erg[1] = new DoubleComplex(0,0);;
		return erg;
	}
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 13:12:39)
 * @return double
 */
public double abs_doubleNorm() 
{
	return reel*reel+imag*imag;
}
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 13:00:27)
 * @return arithmetik.Complex
 * @param reel double
 * @param imag double
 */
public Complex abs_fromDouble(double reel, double imag) 
{
	return new DoubleComplex(reel, imag);
}
	public GcdAble abs_gcd(GcdAble arg2) {return new DoubleComplex(1,0);}
	public boolean abs_isEqual (Ring b) {return isEqual((DoubleComplex)b);}
	public Ring abs_multiply (Ring b) {return multiply ((DoubleComplex)b);}
	public Ring abs_negate () {return negate();}
	public Ring abs_pow(long exp) {return pow(exp);}
	public Field abs_reciprocal () {return reciprocal(); }
	public GcdAble abs_remainder(GcdAble arg2) {return new DoubleComplex(0,0);}
	public Signed abs_ringSignum() {return this.divide((DoubleComplex)this.abs_abs());}
	public GcdAble abs_scm(GcdAble arg2) {return new DoubleComplex(1,0);}
	public int abs_signum() {if (reel<0) return -1; else return 1;}
	public Squarerootable abs_sqrt() {return sqrt();}
	public Ring abs_subtract (Ring b) {return subtract ((DoubleComplex)b);}
	public Ring abs_unit () {return new DoubleComplex(1,0);}
	public Ring abs_zero () {return new DoubleComplex(0,0);}
	public double absolute()
	{
		return Math.sqrt(reel*reel+imag*imag);
	}
	public DoubleComplex add (DoubleComplex z)
	{
		return new DoubleComplex(reel+z.reel,imag+z.imag);
	}
	public Complex conjugate()
	{
		return new DoubleComplex(reel, -imag);
	}
	public DoubleComplex divide(DoubleComplex z)
	{
		return multiply(z.reciprocal());
	}
	public double doubleNorm()
	{
		return this.absolute();
	}
	// liefert erste n-te EW
	public UnitRootComplete getPrimitiveUnitRoot(int n) {return getUnitRoot(n,1);}
	// liefert k-te n-te EW
	public UnitRootComplete getUnitRoot(int n, int k)
	{
		if ((unitRoots.length % n)!=0)
		{
			int gcd = ((new BigInteger(""+unitRoots.length)).gcd(new BigInteger(""+n))).intValue();
			int nl = (n/gcd)*unitRoots.length;
			double alpha = (2*Math.PI)/nl;
			unitRoots = new DoubleComplex[nl];
			for (int i=0; i<nl; i++)
				unitRoots[i] = new DoubleComplex(Math.cos(alpha*i),Math.sin(alpha*i));
		}
		int j = k; 
		while (j<0) j += n;
		while (j>=n) j -= n;
		return new DoubleComplex(unitRoots[(unitRoots.length/n)*j]);
	}
	public double imagValue() {return imag;}
	public boolean isEqual(DoubleComplex arg2)
	{
		return ((reel==arg2.reel) && (imag==arg2.imag));
	}
/**
 * Insert the method's description here.
 * Creation date: (10.06.2002 17:35:06)
 * @return boolean
 */
public boolean isNotLegalNummeric() 
{
	return ((Double.isNaN(reel)) || (Double.isInfinite(reel)) || (Double.isInfinite(imag)) || (Double.isInfinite(imag)));
}
	public boolean isReal(double eps)
	{
		if ((imag < -eps) || (imag > eps)) return false;
		return true;
	}
	public DoubleComplex multiply (DoubleComplex z)
	{
		DoubleComplex erg = new DoubleComplex(reel*z.reel-imag*z.imag,reel*z.imag+imag*z.reel);
		return erg;
	}
	public DoubleComplex negate ()
	{
		return new DoubleComplex(-reel,-imag);
	}
	public DoubleComplex pow(long i)
	{
		if (i==0) return (DoubleComplex)abs_unit();
		DoubleComplex erg = pow(i/2).sqr();
		if ((i%2)==1) return erg.multiply(this);
		else return erg;
	}
	public DoubleComplex reciprocal ()
	{
		double betragsqr = reel*reel+imag*imag;
		return new DoubleComplex (reel/betragsqr,-imag/betragsqr);
	}
	public double reelValue() {return reel;}
	public DoubleComplex sqr()
	{
		return multiply(this);
	}
	// Mit der Unstetigkeitslinie über die negative reele Achse,
	// d.h. alle Ergebnisse haben einen positiven reelen Wert.
	public DoubleComplex sqrt()
	{
		double betrag = Math.sqrt(absolute());
		double alpha = Math.atan2(imag,reel);
		if (alpha<0) alpha += 2*Math.PI;
		double beta = alpha/2;
		double x = betrag * Math.cos(beta);
		double y = betrag * Math.sin(beta);
		if (x<0) return new DoubleComplex(-x,-y);
		else return new DoubleComplex(x,y);
	}
	public DoubleComplex subtract (DoubleComplex z)
	{
		return new DoubleComplex(reel-z.reel,imag-z.imag);
	}
	public String toString()
	{
		return reel+" + "+imag+"i";
	}
}
