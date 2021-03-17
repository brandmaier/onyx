package arithmetik;

import java.math.*;

// Darstellung von beliebigen Zahlen durch Intervallarithmetik.

public class Relement implements Orderd, Field, Squarerootable, Signed, DoubleCastable
{
	static final Qelement DEFAULTEPSILON = new Qelement(1,Integer.MAX_VALUE);
	static int maxMagnitudeLength = 64;
	static BigInteger maxMagnitude = BigInteger.valueOf(1).shiftLeft(maxMagnitudeLength);
	static final Relement PI = new Relement(new Qelement(new BigInteger("3141592653589793238462"),
														 new BigInteger("1000000000000000000000")),
											new Qelement(new BigInteger("3141592653589793238463"),
														 new BigInteger("1000000000000000000000")));

	private Qelement unten;
	private Qelement oben;

	private boolean exakt;
	
	public Relement()
	{
		unten = Qelement.ZERO;
		oben = Qelement.ZERO;
		exakt = true;
	}
	public Relement(double d) {this (new Qelement(d));}
	public Relement(long l) {this (new Qelement(l));}
	public Relement(Qelement q)
	{
		unten = q;
		oben = q;
		exakt = true;
		checkSize();
	}
	public Relement(Qelement u, Qelement o)
	{
		int c = u.compareTo(o);
		if (c==-1)
		{
			unten = u;
			oben = o;
		} else {
			unten = o;
			oben = u;
		}
		exakt = (c==0);
		checkSize();
	}
	public Relement (Relement copy)
	{
		unten = copy.unten;
		oben = copy.oben;
		exakt = copy.exakt;
	}
	public Relement (Relement border1, Relement border2)
	{
		if (border1.unten.compareTo(border2.unten)==-1) unten = new Qelement(border1.unten);
		else unten = new Qelement(border2.unten);
		if (border1.oben.compareTo(border2.oben)==1) oben = new Qelement(border1.oben);
		else oben = new Qelement(border2.oben);
		exakt = (unten.compareTo(oben)==0);
		checkSize();
	}
	public Relement abs()
	{
		Qelement u = unten.abs();
		Qelement o = oben.abs();
		if (unten.compareTo(oben)==1) 
		{
			Qelement t = u;
			u = o;
			o = t;
		}
		return new Relement(u,o);
	}
	public Signed abs_abs() {return abs();}
	public Ring abs_add (Ring b) {return add((Relement)b);}
	public int abs_compareTo (Orderd b)	{return compareTo((Relement)b);}
	public Field abs_divide (Field b) {return divide((Relement)b);}
	public GcdAble abs_divide(GcdAble arg2) {return divide((Relement)arg2);}
	public GcdAble[] abs_divideAndRemainder(GcdAble arg2) 
	{
		GcdAble[] erg = new GcdAble[2]; erg[0] = divide((Relement)arg2); erg[1] = zero();
		return erg;
	}
	public GcdAble abs_gcd(GcdAble arg2) {return unit();}
	public boolean abs_isEqual (Ring b) {return compareTo((Relement)b)==0;}
	public Ring abs_multiply (Ring b) {return multiply ((Relement)b);}
	public Ring abs_negate () {return negate();}
	public Ring abs_pow(long exp) {return pow(exp);}
	public Field abs_reciprocal () {return reciprocal(); }
	public GcdAble abs_remainder(GcdAble arg2) {return zero();}
	public Signed abs_ringSignum() 
	{
		int s = signum(); 
		if (s==-1) return new Relement(-1);
		if (s==1) return new Relement(1);
		return new Relement();
	}
	public GcdAble abs_scm(GcdAble arg2) {return unit();}
	public int abs_signum() {return signum();}
	public Squarerootable abs_sqrt() {return sqrt();}
	public Ring abs_subtract (Ring b) {return subtract ((Relement)b);}
	public Ring abs_unit () {return unit ();}
	public Ring abs_zero () {return zero ();}
	public Relement add(Relement arg2)
	{
		if ((this.exakt) && (arg2.exakt)) return new Relement(unten.add(arg2.unten));
		return new Relement(unten.add(arg2.unten),oben.add(arg2.oben));
	}
	public boolean bordersClose(Relement z, Qelement epsilon)
	{
		return (   (oben.subtract(z.oben).abs().compareTo(epsilon)==-1)
				&& (unten.subtract(z.unten).abs().compareTo(epsilon)==-1));
	}
	public BigInteger ceil()
	{
		return oben.ceil();
	}
	// Verändert das CElement direkt. Falls die Magnitude der Nenner jeweils länger ist
	// als durch maxMagnitudeLength erlaubt, wird der Zähler auf maxMagnitudeLength gebracht
	// und der Nenner auf maxMagnitude. Unten wird immer ab-, oben immer aufgerundet. Die
	// Korrektur wird nicht gemacht, wenn damit eine Grenze auf 0 gesetzt wird (d.h. in der Nähe
	// von 0 wird beliebige Exaktheit zugelassen).
	public void checkSize()
	{
		if (unten.n.bitLength()>maxMagnitudeLength)
		{
			BigInteger neuZ = unten.z.shiftLeft(maxMagnitudeLength).divide(unten.n);
			if (neuZ.compareTo(BigInteger.valueOf(0))!=0) {unten = new Qelement(neuZ, maxMagnitude.shiftLeft(0)); exakt = false;}
		}
		if (oben.z.bitLength()>maxMagnitudeLength)
		{
			BigInteger neuZ = (oben.z.shiftLeft(maxMagnitudeLength).divide(oben.n)).add(BigInteger.valueOf(1));
			if (neuZ.compareTo(BigInteger.valueOf(0))!=0) {oben = new Qelement(neuZ, maxMagnitude.shiftLeft(0)); exakt = false;}
		}
	}
	public int compareTo(Relement arg2)
	{
		return subtract(arg2).signum();
	}
	public Relement divide(Relement arg2)
	{
		int sig1 = signum(); int sig2 = arg2.signum();
		if (sig2==0) 
			throw new RuntimeException("Division by zero");

		if ((sig1>=0) && (sig2>=0)) return new Relement(unten.divide(arg2.oben), oben.divide(arg2.unten));
		if ((sig1==-1) && (sig2==-1)) return new Relement(oben.divide(arg2.unten), unten.divide(arg2.oben));
		if (sig1==-1) return new Relement(unten.divide(arg2.unten),oben.divide(arg2.oben));
		return new Relement(oben.divide(arg2.oben),unten.divide(arg2.unten));
	}
	public boolean equals(Relement arg2)
	{
		return (compareTo(arg2)==0);
	}
	public boolean equalsByEpsilon(Relement arg2, Qelement epsilon)
	{
		return (epsilon.compareTo( abs().subtract(arg2.abs()).getHigher() ) == -1);
	}
	public BigInteger floor()
	{
		return unten.floor();
	}
	public Qelement getHigher()
	{
		return oben;
	}
	public Qelement getLower()
	{
		return unten;
	}
	public double getRange()
	{
		return oben.subtract(unten).toDouble();
	}
	public boolean isZero()
	{
		return (signum()==0);
	}
    public boolean isZero(Qelement epsilon)
    {
        return this.sqr().subtract(new Relement(epsilon.sqr())).signum()!=1;
    }
	public static void last50(String in)
	{
		if (in.length()>150) System.out.println(in.charAt(0)+":"+in.substring(150));
		else System.out.println(in);
	}
	public Relement multiply(Relement arg2)
	{
		if ((exakt) && (arg2.exakt)) return new Relement(unten.multiply(arg2.unten));
		int sig1 = signum(); int sig2 = arg2.signum();
		if ((sig1!=0) && (sig2!=0))
		{
			if ((sig1>=0) && (sig2>=0)) return new Relement(unten.multiply(arg2.unten), oben.multiply(arg2.oben));
			if ((sig1==-1) && (sig2==-1)) return new Relement(oben.multiply(arg2.oben), unten.multiply(arg2.unten));
			if (sig1==-1) return new Relement(unten.multiply(arg2.oben),oben.multiply(arg2.unten));
			return new Relement(oben.multiply(arg2.unten),unten.multiply(arg2.oben));
		}		
		
		// Für den Fall, dass einer der beiden Intervalle 0 enthält, eben durchgeixt:
		
		if ((exakt) || (arg2.exakt)) return new Relement();
		Relement erg = new Relement();
		erg.unten = unten.multiply(arg2.unten);
		erg.oben = erg.unten;
		Qelement kan = unten.multiply(arg2.oben);
		if (kan.compareTo(erg.unten)==-1) erg.unten = kan;
		else if (kan.compareTo(erg.oben)==1) erg.oben = kan;
		kan = oben.multiply(arg2.unten);
		if (kan.compareTo(erg.unten)==-1) erg.unten = kan;
		else if (kan.compareTo(erg.oben)==1) erg.oben = kan;
		kan = oben.multiply(arg2.unten);
		if (kan.compareTo(erg.unten)==-1) erg.unten = kan;
		else if (kan.compareTo(erg.oben)==1) erg.oben = kan;
		erg.checkSize();
		return erg;
	}
	public Relement negate()
	{
		if (exakt) return new Relement(unten.negate());
		return new Relement(oben.negate(), unten.negate());
	}
	public Relement pow(long n)
	{
		if (n==0) return new Relement(Qelement.ONE);
		if (n==1) return new Relement(this);
		long h = n/2;
		return pow(h).multiply(pow(n-h));
	}
	public Relement reciprocal()
	{
		if (signum()==0) throw new RuntimeException("Division by Zero");
		return new Relement(oben.reciprocal(), unten.reciprocal());
	}
	public int signum()
	{
		if (oben.signum() != unten.signum()) return 0;
		else return oben.signum();
	}
	public Relement sqr()
	{
		return multiply(this);
	}
	public Relement sqrt() {return sqrt(this.DEFAULTEPSILON);}
	public Relement sqrt(Qelement inEpsilon)
	{
		if (this.signum()!=1) throw new RuntimeException("Squareroot Argument was negative.");
		
		Qelement epsilon = new Qelement(inEpsilon);
		if (epsilon.n.bitLength() > this.maxMagnitudeLength-2)
			epsilon = new Qelement(BigInteger.valueOf(1),BigInteger.valueOf(2).pow(this.maxMagnitudeLength-2));
		
		Relement alt = new Relement(1);
		Relement neu = (this.add(alt)).divide(new Relement(2));
		while (!neu.bordersClose(alt,epsilon))
		{
			alt = neu;
			neu = alt.add(this.divide(alt)).divide(new Relement(2));
		}
		Qelement u,o;
		if (alt.oben.compareTo(neu.oben)   == 1) o = alt.oben; else o = neu.oben;
		if (alt.unten.compareTo(neu.unten) ==-1) u = alt.unten; else u = neu.unten;
		return new Relement(u,o);
	}
	public Relement subtract(Relement arg2)
	{
		if ((this.exakt) && (arg2.exakt)) return new Relement(unten.subtract(arg2.unten));
		return new Relement(unten.subtract(arg2.oben),oben.subtract(arg2.unten));
	}
	// liefert das arithmetische Mittel der Grenzen.
	public double toDouble()
	{
		return (unten.toDouble()+oben.toDouble()) / 2.0;
	}
	public String toString()
	{
		if (exakt) return unten.toString();
		return "["+unten+","+oben+"]";
	}
	// Absolute Hack-Methode: Verwirft die Unsicherheit.
	// Beide Grenzen werden auf ihr arithmetisches Mittel gesetzt.
	public Relement trustCenter()
	{
		Relement erg = new Relement(this);
		if (exakt) return erg;

		Qelement center = oben.add(unten).divide(Qelement.TWO);
		erg.oben = center;
		erg.unten = center;
		erg.exakt = true;
		
		return erg;
	}
	public Relement unit()
	{
		return new Relement(1);
	}
	public Relement zero()
	{
		return new Relement();
	}
	// Verschenkt natürlich die Grenzen.
	public double doubleValue()
	{
		double o = oben.doubleValue(), u = unten.doubleValue();
		return (o+u)/2;
	}

    /**
     * doubleNorm method comment.
     */
    public double doubleNorm() 
    {
    	return Math.abs(doubleValue());
    }
    public Qelement toQelement() {
        return oben.add(unten).multiply(Qelement.HALF);
    }
}
