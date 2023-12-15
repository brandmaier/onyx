/*
* Copyright 2023 by Timo von Oertzen and Andreas M. Brandmaier
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package arithmetik;

import java.math.*;

// Vorzeichen nur im Zähler!
public class Qelement implements Field, Orderd, Signed, DoubleCastable
{
	BigInteger n,z;
	
	static public final Qelement ZERO = (new Qelement());
	static public final Qelement ONE  = (new Qelement(1));
	static public final Qelement TWO  = (new Qelement(2));
	static public final Qelement HALF = (new Qelement(1,2));
	
	static public int precision = -1;						// Numbers of Bits to represent. -1  
															// means no software maximum.
	
	public Qelement ()
	{
		z = BigInteger.valueOf(0);
		n = BigInteger.valueOf(1);
	}
	public Qelement(double d)
	{
		this(Math.round(d*1000), 1000);
	}
	public static Qelement fromDouble(double d, int digits) {
	    long nl = Math.round(Math.pow(10, digits));
	    return new Qelement(Math.round(d*nl),nl);
	}
	public Qelement (long i)
	{
		z = BigInteger.valueOf(i);
		n = BigInteger.valueOf(1);
	}
    public Qelement (long i, long j)
    {
        z = BigInteger.valueOf(i);
        n = BigInteger.valueOf(j);
        BigInteger g = z.gcd(n);
        z = z.divide(g);
        n = n.divide(g);
        if (n.signum() == -1) 
        {
            z = z.negate();
            n = n.negate();
        }       
    }
	// Privat - Konstruktor: Eingaben werden nicht auf Richtigkeit
	// geprüft.
	private Qelement (long i, long j, long no_meaning)
	{
		z = BigInteger.valueOf(i);
		n = BigInteger.valueOf(i);
	}
	public Qelement (Qelement v_copy)
	{
		z = new BigInteger(v_copy.z.toByteArray());
		n = new BigInteger(v_copy.n.toByteArray());
	}
	public Qelement (BigInteger z)
	{
		this.z = new BigInteger(z.toByteArray());
		n = BigInteger.valueOf(1);
	}
	public Qelement (BigInteger i, BigInteger j)
	{
		z = new BigInteger(i.toByteArray());
		n = new BigInteger(j.toByteArray());
		BigInteger g = z.gcd(n);
		z = z.divide(g);
		n = n.divide(g);
		if (n.signum()==-1) 
		{
			z = z.negate();
			n = n.negate();
		}		
	}
	private Qelement (BigInteger i, BigInteger j, long no_meaning)
	{
		z = new BigInteger(i.toByteArray());
		n = new BigInteger(j.toByteArray());
	}
	public Qelement abs()
	{
		if (isNegative()) return new Qelement (z.negate(), n, 0L);
		else return new Qelement(this);
	}
	public Signed abs_abs() {return this.abs();}
	public Ring abs_add (Ring b) {return add((Qelement)b);}
	public int abs_compareTo (Orderd b)	{return compareTo((Qelement)b);}
	public Field abs_divide (Field b) {return divide((Qelement)b);}
	public GcdAble abs_divide(GcdAble arg2) {return divide((Qelement)arg2);}
	public GcdAble[] abs_divideAndRemainder(GcdAble arg2) 
	{
		GcdAble[] erg = new GcdAble[2]; erg[0] = divide((Qelement)arg2); erg[1] = zero();
		return erg;
	}
	public GcdAble abs_gcd(GcdAble arg2) {return unit();}
	public boolean abs_isEqual (Ring b) {return compareTo((Qelement)b)==0;}
	public Ring abs_multiply (Ring b) {return multiply ((Qelement)b);}
	public Ring abs_negate () {return negate();}
	public Ring abs_pow(long exp) {return pow(exp);}
	public Field abs_reciprocal () {return reciprocal(); }
	public GcdAble abs_remainder(GcdAble arg2) {return zero();}
	public Signed abs_ringSignum() {return new Qelement(signum());}
	public GcdAble abs_scm(GcdAble arg2) {return unit();}
	public int abs_signum() {return this.signum();}
	public Ring abs_subtract (Ring b) {return subtract ((Qelement)b);}
	public Ring abs_unit () {return unit ();}
	public Ring abs_zero () {return zero ();}
	public Qelement add(Qelement arg2)
	{
		BigInteger nenggT = n.gcd(arg2.n);
		BigInteger nen1 = n.divide(nenggT);
		BigInteger nen2 = arg2.n.divide(nenggT);

		return new Qelement ((z.multiply(nen2)).add(arg2.z.multiply(nen1)),n.multiply(nen2));
	}
	public BigInteger ceil()
	{
		if (this.isNegative()) return negate().floor().negate();
		if (isInteger()) return z.divide(n);
		else return z.divide(n).add(BigInteger.valueOf(1));
	}
	public int compareTo(Qelement arg2)
	{
		return (this.subtract(arg2)).signum();
	}
	public Qelement divide(Qelement arg2) 
	{
		if (arg2.isZero()) throw new RuntimeException("Division durch Null.");
		
		BigInteger zae = z.multiply(arg2.n);
		BigInteger nen = n.multiply(arg2.z);
		
		return new Qelement (z.multiply(arg2.n),n.multiply(arg2.z));
	}
	public double doubleValue() {return toDouble();}
	public boolean equals(Qelement arg2)
	{
		return (compareTo(arg2)==0);
	}
	public BigInteger floor()
	{
		if (this.isNegative()) return negate().ceil().negate();
		else return z.divide(n);
	}
	// Liefert den 'ggT' zweier Qelemente zurück, womit der ggT des Zählers und der ggT
	// des Nenners gemeint ist.
	public static Qelement gcd(Qelement arg1, Qelement arg2)
	{
		return new Qelement(arg1.z.gcd(arg2.z),arg1.n.gcd(arg2.n),0L);
	}
	public boolean isEqual(Qelement arg2)
	{
		return ((z.equals(arg2.z)) && (n.equals(arg2.n)));
	}
	public boolean isInteger()
	{
		return (n.equals(BigInteger.valueOf(1)));
	}
	public boolean isNegative()
	{
		return (z.signum()==-1);
	}
	public boolean isZero()
	{
		return (z.signum()==0);
	}
	public Qelement multiply(Qelement arg2)
	{
		return new Qelement (z.multiply(arg2.z),n.multiply(arg2.n));
	}
	public Qelement negate()
	{
		return new Qelement (z.negate(),n,0L);
	}
	public Qelement normal()
	{
		if (z.signum()==0) return new Qelement();
		BigInteger t = z.gcd(n);
		Qelement erg = new Qelement (z.divide(t),n.divide(t),0L);
		if (n.signum()==-1) 
		{
			erg.z = erg.z.negate();
			erg.n = erg.n.negate();
		}
		if ((precision > 0) && (erg.n.bitCount() > precision))
		{
			erg.n.shiftRight(erg.n.bitCount()-precision);
			erg.z.shiftRight(erg.n.bitCount()-precision);
		}
		return erg;
	}
	public Qelement pow(long n)
	{
		if (n==0) return new Qelement(Qelement.ONE);
		if (n==1) return new Qelement(this);
		if (n<0) return reciprocal().pow(-n);
		long h = n/2;
		return pow(h).multiply(pow(n-h));
	}
	public Qelement reciprocal() 
	{
		if (z.signum()==0) throw new RuntimeException("Division durch Null.");
		return new Qelement (n,z);
	}
	public int signum()
	{
		return (z.signum());
	}
	public Qelement sqr()
	{
		return multiply(this);
	}
	public Qelement subtract(Qelement arg2)
	{
		BigInteger nenggT = n.gcd(arg2.n);
		BigInteger nen1 = n.divide(nenggT);
		BigInteger nen2 = arg2.n.divide(nenggT);

		return new Qelement ((z.multiply(nen2)).subtract(arg2.z.multiply(nen1)),n.multiply(nen2));
	}
	public double toDouble()
	{
		int MAXDIG = 50;
		long man = 0;
		String zs = z.toString();
		if (zs.length() > MAXDIG) {man -= MAXDIG - zs.length(); zs = zs.substring(0, MAXDIG);}
		String ns = n.toString();
		if (ns.length() > MAXDIG) {man += MAXDIG - ns.length(); ns = ns.substring(0, MAXDIG);}
		
		double erg = Double.valueOf(zs).doubleValue() / Double.valueOf(ns).doubleValue();
		erg *= Math.pow(10,man);
		return erg;
	}
	public String toString()
	{
		if (n.equals(BigInteger.valueOf(1))) return z.toString();
		return "("+z.toString()+"/"+n.toString()+")";
	}
	public Qelement unit()
	{
		return new Qelement(1);
	}
	public Qelement zero() 
	{
		return new Qelement();
	}

/**
 * doubleNorm method comment.
 */
public double doubleNorm() 
{
	return Math.abs(doubleValue());
}

/**
 * Insert the method's description here.
 * Creation date: (24.02.2003 09:00:59)
 * @return arithmetik.Qelement[][]
 * @param matrix arithmetik.Qelement[][]

	Liefert Arrays zurück, in denen Basiselemente des Kerns der mitgegebenen Matrix sind. Ist der Kern 0, so wird
	kein Array zurückgegeben. Pro Basisvektor sind soviele Einträge, wie die Matrix Spalten hat, und es gibt soviele
	Basisvektoren, wie die Dimension des Kerns.
 
 */
public static Qelement[][] findCoreBasis(Qelement[][] matrix) 
{
	int zeilen = matrix.length; 
	int spalten = matrix[0].length;

	Qelement[][] m = new Qelement[zeilen][spalten];
	for (int i=0; i<zeilen; i++)
		for (int j=0; j<spalten; j++)
			m[i][j] = matrix[i][j];							// Elemente an sich werden nicht geändert.

	boolean[] zeileGestrichen = new boolean[zeilen];
	for (int i=0; i<zeilen; i++) zeileGestrichen[i] = false;
	int[] pivotNr = new int[spalten];						// enthält für jede Spalte die Nummer der relevanten Zeile,
														    // oder -1, wenn leer.
	int dimKern = 0;
	
	for (int s=0; s<spalten; s++)
	{
		int piv = -1;
		int komplex = Integer.MAX_VALUE;
		for (int z=0; z<zeilen; z++) 
			if (!zeileGestrichen[z])
			{
				int k = m[z][s].getBitLength();
				if ((!m[z][s].isZero()) && (k<komplex)) {piv = z; komplex = k;}
			}
		pivotNr[s] = piv;
		if (piv==-1) dimKern++;
		else
		{
			Qelement pivEl = m[piv][s];
			zeileGestrichen[piv] = true;
			for (int z=0; z<zeilen; z++)
			{
				Qelement fak = m[z][s];
				if ((!zeileGestrichen[z]) && (!fak.isZero()))
				{
					fak = fak.divide(pivEl);
					for (int s2=s+1; s2<spalten; s2++)
						m[z][s2] = m[z][s2].subtract(m[piv][s2].multiply(fak));
					m[z][s] = ZERO;
				}
			}
		}
	}

	// jetzt Kern aufbauen
	Qelement[][] kern = new Qelement[dimKern][spalten];
	int i = 0;
	for (int s=spalten-1; s>=0; s--)
	{
		if (pivotNr[s]==-1)
		{
			for (int k=0; k<dimKern; k++) kern[k][s] = ZERO;
			kern[i++][s] = ONE;
		} else {
			for (int k=0; k<dimKern; k++)
			{
				Qelement wert = ZERO;
				for (int s2=s+1; s2<spalten; s2++) wert = wert.add(m[pivotNr[s]][s2].multiply(kern[k][s2]));
				kern[k][s] = wert.divide(m[pivotNr[s]][s]).negate();
			}
		}
	}
	return kern;
}

/**
 * Insert the method's description here.
 * Creation date: (24.02.2003 09:10:06)
 * @return int
 */
public int getBitLength() 
{
	return z.bitLength() + n.bitLength();
}

/**
 * Insert the method's description here.
 * Creation date: (12.08.2004 18:31:14)
 * @return arithmetik.Qelement
 * @param d double

	errät eine rationale Zahl, die durch d angenähert wird, durch Partialbruchzerlegung
 
 */
public static Qelement guessFromDouble(double d) 
{
	final int MAXITERATION = 20;
	final int ABBRUCHFAKTOR = 4;
	final double EPS = (1.0/20.0);
	int z = (int)Math.floor(d);
	int[] erg = new int[MAXITERATION];
	erg[0] = z;
	double work = d - z;

	int max = 0, mel = 0;
	int iteration = 1;
	while ((iteration < MAXITERATION) && (work > EPS))
	{
		double oneOverWork = 1.0 / work;
		z = (int)Math.floor(oneOverWork);
		erg[iteration] = z;
		if (z > mel) {mel = z; max = iteration;}
		work = oneOverWork - z;
		iteration++;
	}
	if (iteration < MAXITERATION) max = iteration;

	Qelement er = new Qelement(erg[max-1]);
	for (int i=max-2; i>=0; i--)
		er = er.reciprocal().add(new Qelement(erg[i]));

	return er;		
}

/**
 * Insert the method's description here.
 * Creation date: (08.05.2003 13:45:35)
 * @return int
 */
public int hashCode() 
{
	return z.hashCode()+n.hashCode();
}

/**
 * Insert the method's description here.
 * Creation date: (18.06.2002 09:43:55)
 * @return boolean
 */
public boolean isUnit() 
{
	return isEqual(Qelement.ONE);
}

/**

	Wird von CK benutzt;

	liefert eine zufällige positive ganze Zahl
	zwischen 0 und bound ausschließlich.

 * Insert the method's description here.
 * Creation date: (05.01.2003 18:07:47)
 * @return arithmetik.Qelement
 * @param bound int
 */
public static Qelement random(int bound) 
{
	return new Qelement(Math.floor(bound*Math.random()));
}

/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 13:38:21)
 * @return int
 */
public int toInt() 
{
	return floor().intValue();
}
}
