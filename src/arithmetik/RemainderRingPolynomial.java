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
import java.util.*;

public class RemainderRingPolynomial implements Ring, GcdAble
{
	private final BigInteger i1 = BigInteger.valueOf(1);
	private final BigInteger i0 = BigInteger.valueOf(0);
	private final BigInteger i2 = BigInteger.valueOf(2);
	
	/** 
	 * Die Koeffizienten des Polynoms
	 */
	RemainderRing[] coef;
	/**
	 * Der Grad des Polynoms
	 */
	int deg;
	/**
	 * Der Modul des Polynoms
	 */
	final Modulus modulo;
	
	/**
	 * Erzeugt ein Polynom modulo p das maximal den Grad degree erreichen kann.
	 */
	public RemainderRingPolynomial(int degree, Modulus p)
	{
		deg=-1;
		modulo = p;
		if (degree >= 0) {
			coef = new RemainderRing[degree+1];
			for (int i=0; i<=degree ; i++) coef[i]= new RemainderRing(0,p);
		}
	}

	/**
	 * Erzeugt ein konstantes Polynom mit Koeffizienten b
	 */
	public RemainderRingPolynomial(RemainderRing b)
	{
		modulo = b.modulo;
		if (b.isZero()) deg=-1;
		else
		{
			deg=0;
			coef = new RemainderRing[1];
			coef[0]=new RemainderRing(b);
		}
	}
		
	/**
	 * Erzeugt ein Polynom modulo p mit Koeffizienten coefs
	 */
	public RemainderRingPolynomial(RemainderRing[] coefs)
	{
		deg=-1;
		modulo = coefs[0].modulo;
		coef = new RemainderRing[coefs.length];
		for (int i=0; i<coefs.length ; i++) {
			coef[i] = new RemainderRing(coefs[i]);
			if  ( !(coef[i].isZero()) ) deg = i;
		}
	}
	
	public RemainderRingPolynomial(RemainderRing[] coefs, Modulus mod)
	{
		deg=-1;
		modulo = mod;
		if (coefs != null)
		{
			coef = new RemainderRing[coefs.length];
			for (int i=0; i<coefs.length ; i++) {
				coef[i] = new RemainderRing(coefs[i]);
				if  ( !(coef[i].isZero()) ) deg = i;
			}
		}
	}

	/**
	 * Klont ein Polynom p
	 */
	public RemainderRingPolynomial(RemainderRingPolynomial p)
	{
		this(p.coef, p.modulo);
	}
	
	/**
	 * Erzeugt ein Polynom modulo p vom Grad maximal degree, mit Koeffizienten aus coefs.
	 */
	public RemainderRingPolynomial(RemainderRing[] coefs,int degree)
	{
		deg=-1;
		modulo=coefs[0].modulo;
		if (degree >= 0) {
			coef = new RemainderRing[degree+1];
			for (int i=0; i<=degree; i++) {
				if (coef.length > i) 
				{
					coef[i]= new RemainderRing(coefs[i]);
				     if  ( !(coef[i].isZero()) ) deg = i;
				} else {
					coef[i]=new RemainderRing(0,modulo);
				}
			}
		}
	}
	
	/**
	 * Kopiert p in ein Polynom vom Grad h"ochstens degree
	 */
	public RemainderRingPolynomial(RemainderRingPolynomial p,int degree)
	{
		this(p.coef,degree);
	}

	
	/**
	 * liefert zu einem univariaten Polynom das zugehörige Polynom modulo m
	 */
	public RemainderRingPolynomial(UnivariatePolynomial f,Modulus m)
	{
		if (f.isZero())
		{
			deg = -1;
			coef = new RemainderRing[0];
			modulo = m;
		} else {
			deg = -1;
			modulo = m;
			coef = new RemainderRing[f.deg+1];
			for (int i = 0; i <= f.deg ; i++)
			{
				coef[i] = new RemainderRing(f.get(i),m);
				if ( !(coef[i].isZero()) ) deg = i;
			}
		}
	}
			
	
	/**
	 * Berechnet den Grad eines Polynoms neu.
	 */
	private void resetDegree()
	{
		deg = -1;
		for (int i=0 ; i < coef.length; i++) {
			if ( !(coef[i].isZero()) ) deg = i;
		}
	}
	
	/**
	 *  Erzeugt ein zufälliges Polynom modulo mod vom Grad höchstens deg.
	 */
	public static RemainderRingPolynomial rndPolynomial(int deg, Modulus mod)
	{
		RemainderRingPolynomial erg = new RemainderRingPolynomial(deg,mod);
		for (int i = 0 ; i<=deg ; i++)
		{
			long mx = mod.modulo.subtract(BigInteger.valueOf(1)).longValue();
			erg.set(new RemainderRing(Math.round(Math.random()*mx),mod),i);
		}
		return erg;
	}
		
	/**
	 * Berechnet this/c
	 */
	public RemainderRingPolynomial divide(RemainderRing c)
	{
		if (c.isZero()) throw new RuntimeException("Division durch 0!");
		RemainderRingPolynomial erg = new RemainderRingPolynomial(this);
		for (int i=0; i <=deg ; i++)
			erg.coef[i]=coef[i].divide(c);
		return erg;
	}
	
	/**
	 *  Setze Koeffizient i auf a
	 */
	public void set(RemainderRing a,int i)
	{
		if ( !(a.modulo.isEqual(modulo)) ) throw new WrongFieldException();
		coef[i] = new RemainderRing(a);
		if ( a.isZero() )
		{
			if ( deg == i ) resetDegree();
		} else {
			deg = Math.max(deg,i);
		}
	}
	
	/**
	 * Liefert i-ten Koeffizienten
	 */
	public RemainderRing get(int i)
	{
		if (i > deg)
		{
			return new RemainderRing(0,modulo);
		} else {
			return new RemainderRing(coef[i]);
		}
	}
	
	/**
	 *  Berechnet this+b
	 */
	public RemainderRingPolynomial add(RemainderRingPolynomial b)
	{
		if ( !(b.modulo.isEqual(modulo)) ) throw new WrongFieldException("addieren");
		RemainderRingPolynomial erg = new RemainderRingPolynomial(Math.max(deg,b.deg),modulo);
		for (int i=0 ; i <= Math.max(deg,b.deg) ; i++)
		{
			erg.set(get(i).add(b.get(i)),i);
		}
		return erg;
	}
	
	public Ring abs_add (Ring b)
	{
		return add ((RemainderRingPolynomial)b);
	}
	
	/**
	 * Testet this == b
	 */
	public boolean isEqual(RemainderRingPolynomial b)
	{
		if ( !(b.modulo.isEqual(modulo)) ) throw new WrongFieldException("vergleichen");
		if (deg != b.deg) return false;
		for (int i=0 ; i <= deg ; i++)
			if ( !(this.get(i).isEqual(b.get(i))) ) return false;
		return true;
	}
	
	public boolean abs_isEqual (Ring b)
	{
		return isEqual((RemainderRingPolynomial)b);
	}

	/**
	 * Berechnet this*bx^pow
	 */
	public RemainderRingPolynomial monomialMultiply(RemainderRing b,int pow)
	{
		if ( !(b.modulo.isEqual(modulo)) ) throw new WrongFieldException("multiplizieren");
		RemainderRingPolynomial erg = new RemainderRingPolynomial(pow+deg,modulo);
		for ( int i = 0; i <= deg ; i++)
		{
			erg.set(b.multiply(get(i)),i+pow);
		}
		return erg;
	}
		
	// Das hier sollte durch Karatsuba oder FFT-Multiplikation verbessert werden!
	/**
	 * Berechnet this*b
	 */
	public RemainderRingPolynomial multiply(RemainderRingPolynomial b)
	{
		if ( !(b.modulo.isEqual(modulo)) ) throw new WrongFieldException("multiplizieren");
		RemainderRingPolynomial erg = new RemainderRingPolynomial(deg+b.deg,modulo);
		for (int i=0; i<=deg; i++)
			for (int j=0; j<=b.deg; j++)
			erg.set(this.get(i).multiply(b.get(j)).add(erg.get(i+j)),i+j);
		return erg;
	}

	public Ring abs_multiply (Ring b)
	{
		return multiply((RemainderRingPolynomial)b);
	}
	
	/**
	 * liefert den Teil von this zwischen beg und end.
	 * (Intern fuer karatsubaOld)
	 */
	private RemainderRingPolynomial getPartOld(int beg,int end)
	{
		RemainderRingPolynomial erg = new RemainderRingPolynomial(end-beg,modulo);
		for (int i = 0 ; i <=end-beg ; i++)
			erg.set(this.get(beg+i),i);
		return erg;
	}

	/**
	 * liefert den Leitkoeffizienten.
	 */
	public RemainderRing leadingCoefficient()
	{
		if (deg < 0 ) return new RemainderRing(modulo);
		return new RemainderRing(coef[deg]);
	}

	/**
	 * Berechnet this*g mit Karatsuba, ist aber ineffizient
	 * !! Nicht Benutzen !!
	 */
	public RemainderRingPolynomial karatsubaOld(RemainderRingPolynomial g)
	{
		int n = Math.max(this.deg/2,g.deg/2);
		if ( n <= 1 ) return this.multiply(g);
		RemainderRingPolynomial f0 = this.getPartOld(0,n-1);
		RemainderRingPolynomial f1 = this.getPartOld(n,this.deg);
		RemainderRingPolynomial g0 = g.getPartOld(0,n-1);
		RemainderRingPolynomial g1 = g.getPartOld(n,this.deg);
		RemainderRingPolynomial fg0 = f0.karatsubaOld(g0);
		RemainderRingPolynomial fg1 = f1.karatsubaOld(g1);
		RemainderRingPolynomial fg = (f0.add(f1)).karatsubaOld(g0.add(g1));
		RemainderRingPolynomial fgx = fg1.monomialMultiply(new RemainderRing(1,modulo),2*n);
		RemainderRingPolynomial fgxh = (fg.subtract(fg0).subtract(fg1)).monomialMultiply(new RemainderRing(1,modulo),n);
		return fgx.add(fgxh).add(fg0);
	}
	
	/**
	 * liefert -this
	 */
	public RemainderRingPolynomial negate()
	{
		RemainderRingPolynomial erg = new RemainderRingPolynomial(deg,modulo);
		for (int i=0; i<=deg; i++)
			erg.set(get(i).negate(),i);
		return erg;
	}
			
	public Ring abs_negate ()
	{
		return negate();
	}
	
	/**
	 * berechnet this^exp
	 */
	public RemainderRingPolynomial pow(long exp)
	{
		if (exp==0) return unit();
		if (exp==1) return new RemainderRingPolynomial(this);
		long h = exp/2;
		return pow(h).multiply(pow(exp-h));
	}

	public Ring abs_pow(long exp)
	{
		return pow(exp);
	}
	
	/**
	 * Berechnet this-b
	 */
	public RemainderRingPolynomial subtract(RemainderRingPolynomial b)
	{
		return this.add(b.negate());
	}
	
	public Ring abs_subtract (Ring b)
	{
		return subtract((RemainderRingPolynomial)b);
	}
	
	/**
	 * liefert die Einheit modulo dem momentanen Modul
	 */
	public RemainderRingPolynomial unit()
	{
		RemainderRingPolynomial erg = new RemainderRingPolynomial(0,modulo);
		erg.set(new RemainderRing(1,modulo),0);
		return erg;
	}
	
	/**
	 * liefert x modulo dem momentanen Modul
	 */
	public RemainderRingPolynomial x()
	{
		RemainderRingPolynomial erg = new RemainderRingPolynomial(1,modulo);
		erg.set(new RemainderRing(1,modulo),1);
		return erg;
	}
	
	public Ring abs_unit ()
	{
		return unit();
	}
	
	/**
	 * liefert das Nullpolynom modulo dem momentanen Modul
	 */
	public RemainderRingPolynomial zero()
	{
		return new RemainderRingPolynomial(-1,modulo);
	}
	
	public Ring abs_zero ()
	{
		return zero();
	}
	
	/**
	 * Generiert einen String, der das momentane Polynom repräsentiert
	 */
	public String toString()
	{
		if (deg < 0) return "0 mod " +  modulo.modulo;
		String erg = "" + get(0).value;
		for (int i=1; i <=deg ; i++)
			if ( !(get(i).isZero()) ) erg = erg + " + " + get(i).value + "x^" + i;
		return erg + " mod " + modulo.modulo;
	}
	
	/**
	 * Berechnet [this div g , this mod g]
	 */
	public RemainderRingPolynomial[] divideAndRemainder(RemainderRingPolynomial g)
	{
		if ( !(g.modulo.isEqual(modulo)) ) throw new WrongFieldException("dividieren");
		int k = deg - g.deg;
		RemainderRingPolynomial[] erg = new RemainderRingPolynomial[2];
		RemainderRingPolynomial q = new RemainderRingPolynomial(k,modulo); 
		RemainderRingPolynomial r = new RemainderRingPolynomial(this);

		while (k>=0)
		{
			RemainderRing co = r.leadingCoefficient().divide(g.leadingCoefficient());
			q.set(co,k);
			r = r.subtract(g.monomialMultiply(co,k));
			k = r.deg-g.deg;
		}
		erg[0]=q;
		erg[1]=r;
		return erg;
	}
	
	public GcdAble[] abs_divideAndRemainder(GcdAble arg2)
	{
		return divideAndRemainder((RemainderRingPolynomial) arg2);
	}
	
	/**
	 * liefert this mod g
	 */
	public RemainderRingPolynomial remainder(RemainderRingPolynomial g)
	{

		if ( !(g.modulo.isEqual(modulo)) ) throw new WrongFieldException("dividieren");
		int k = deg - g.deg;
		RemainderRingPolynomial q = new RemainderRingPolynomial(k,modulo); 
		RemainderRingPolynomial r = new RemainderRingPolynomial(this);
		while (k>=0)
		{
			RemainderRing co = r.get(r.deg).divide(g.get(g.deg));
			q.set(co,k);
			r = r.subtract(g.monomialMultiply(co,k));
			k = r.deg-g.deg;
		}
		return r;
	}
	
	public GcdAble abs_remainder(GcdAble arg2)
	{
		return remainder((RemainderRingPolynomial) arg2);
	}
	
	/**
	 * liefert this div g
	 */
	public RemainderRingPolynomial divide(RemainderRingPolynomial g)
	{
		return divideAndRemainder(g)[0];
	}

	public GcdAble abs_divide(GcdAble arg2)
	{
		return divide((RemainderRingPolynomial) arg2);
	}

	/**
	 * liefert ggt(this,g)
	 */
	public RemainderRingPolynomial gcd(RemainderRingPolynomial g)
	{
		if ( !(g.modulo.isEqual(modulo)) ) throw new WrongFieldException("ggT berechnen");
		if (g.deg < 0) return new RemainderRingPolynomial(this);
		if (deg < 0) return new RemainderRingPolynomial(g);
		RemainderRingPolynomial r0 = new RemainderRingPolynomial(this);		
		RemainderRingPolynomial r1 = new RemainderRingPolynomial(g);
		while (r1.deg >= 0)
		{
			RemainderRingPolynomial r2 = r0.remainder(r1);
			r0 = new RemainderRingPolynomial(r1);
			r1 = new RemainderRingPolynomial(r2);
		}
		return r0;
	}
	
	/**
	 * liefert die normalisierte Form von this zurück.
	 */
	public RemainderRingPolynomial normalize()
	{
		if (isZero()) return this;
		return divide(leadingCoefficient());
	}
	
	/**
	 * Eingabe: g
	 * Ausgabe: [s,t,ggt] mit ggt=ggt(this,g) und ggt = s*f + t*g
	 */
	public RemainderRingPolynomial[] extGcd(RemainderRingPolynomial g)
	{
		RemainderRing p0 = leadingCoefficient();
		RemainderRingPolynomial r0 = normalize();
		RemainderRingPolynomial s0 = new RemainderRingPolynomial(p0.reciprocal());
		RemainderRingPolynomial t0 = new RemainderRingPolynomial(new RemainderRing(modulo));
		RemainderRing p1 = g.leadingCoefficient();
		RemainderRingPolynomial r1 = g.normalize();
		RemainderRingPolynomial s1 = new RemainderRingPolynomial(new RemainderRing(modulo));
		RemainderRingPolynomial t1 = new RemainderRingPolynomial(p1.reciprocal());
		RemainderRingPolynomial q1;
		int i = 1;
		while ( !(r1.isZero()) )
		{
			RemainderRingPolynomial[] rr = r0.divideAndRemainder(r1);
			r0=r1;
			q1 = rr[0];
			p1 = rr[1].leadingCoefficient();
			r1 = rr[1].normalize();
			RemainderRingPolynomial st=s1;
			RemainderRingPolynomial tt=t1;
			if ( !(p1.isZero()) )
			{
				s1 = (s0.subtract(q1.multiply(s1))).divide(p1);
				t1 = (t0.subtract(q1.multiply(t1))).divide(p1);
			}
			s0 = st;
			t0 = tt;
		}
		RemainderRingPolynomial[] erg = new RemainderRingPolynomial[3];
		erg[0]=s0;
		erg[1]=t0;
		erg[2]=r0;
		return erg;
	}

	public GcdAble abs_gcd(GcdAble arg2)
	{
		return gcd((RemainderRingPolynomial) arg2);
	}
	
	/**
	 * liefert kgV(this,g)
	 */
	public RemainderRingPolynomial scm(RemainderRingPolynomial g)
	{
		return (g.multiply(this)).divide(g.gcd(this));
	}
	
	public GcdAble abs_scm(GcdAble arg2)
	{
		return scm((RemainderRingPolynomial) arg2);
	}
	
	/**
	 * Testet, ob this das Nullpolynom ist
	 */
	public boolean isZero()
	{
		return (deg < 0);
	}
	
	/**
	 * Testet, ob this eine Einheit ist
	 */
	public boolean isUnit()
	{
		return (deg == 0);
	}

	/**
	 * Normiert this
	 */
	public void makeMonic()
	{
		if (coef[deg] != coef[deg].unit()) {
			RemainderRing inverse = coef[deg].reciprocal();
			for (int i = 0 ; i <= deg ; i++)
				coef[i]=coef[i].multiply(inverse);
		}
	}

	/**
	 * liefert die Ableitung von this
	 */
	public RemainderRingPolynomial derive()
	{
		RemainderRingPolynomial erg = new RemainderRingPolynomial(deg-1,modulo);
		for (int i=1 ; i <= deg ; i++)
			erg.set(get(i).multiply(new RemainderRing(i,modulo)),i-1);
		return erg;
	}
	
	/**
	 * Testet, ob this quadratfrei ist.
	 */
	public boolean isSquareFree()
	{
		return gcd(derive()).isUnit();
	}
	
	/**
	 * Berechnet p-te Wurzel aus this, falls this eine p-te Potenz ist.
	 */
	private RemainderRingPolynomial modRootShift()
	{
		RemainderRingPolynomial erg;
		if (deg < 0 )
			erg = zero();
		else {
			erg = new RemainderRingPolynomial(deg/modulo.modulo.intValue(),modulo);
			for (int i = 0, j=0 ; i <= deg ; i=i+modulo.modulo.intValue(), j=j+1)
				erg.set(get(i),j);
		}
		return erg;
	}
	
	/**
	 * Berechnet this^p
	 */
	private RemainderRingPolynomial modPowerShift()
	{
		RemainderRingPolynomial erg;
		if (deg < 0)
			erg = zero();
		else {
			erg = new RemainderRingPolynomial(deg*modulo.modulo.intValue(),modulo);
			for (int i = 0 , j = 0 ; i <= deg ; i++, j=j+modulo.modulo.intValue())
				erg.set(get(i),j);
		}
		return erg;
	}
	
	/**
	 * Berechnet this^exp modulo mod
	 */
	public RemainderRingPolynomial modPow(BigInteger exp, RemainderRingPolynomial mod)
	{
		RemainderRingPolynomial power = this.remainder(mod);
		RemainderRingPolynomial erg = unit();
		BigInteger two = BigInteger.valueOf(2);
		while (exp.signum() != 0)
		{
			if ( (exp.mod(two)).signum() == 1)
				erg = (erg.multiply(power)).remainder(mod);
			power = (power.multiply(power)).remainder(mod);
			exp = exp.divide(two);
		}
		return erg;
	}
	
	/**
	 * Liefert einen Stack mit den quadratfreien Faktoren und ihrer Vielfachheit
	 * Zuerst wird der Faktor gepusht und dann die Vielfachheit
	 */
	private Stack squareFreeFactors()
	{
		RemainderRingPolynomial b = derive();
		Stack erg = new Stack();
		int multiplicity;
		if (b.isZero()) {
			Stack rootErg = modRootShift().squareFreeFactors();
			while (!(rootErg.empty())) {
				multiplicity = ((Integer)rootErg.pop()).intValue();
				erg.push(rootErg.pop());
				erg.push(new Integer(multiplicity*modulo.modulo.intValue()));
			}
			return erg;
		}
		RemainderRingPolynomial c = gcd(b);
		RemainderRingPolynomial w = divide(c);
		int pow = 1;
		while ( !(w.isUnit()) ) 
		{
			RemainderRingPolynomial y = w.gcd(c);
			RemainderRingPolynomial z = w.divide(y);
			if ( !(z.isUnit()) ) {
				z.makeMonic();
				erg.push(z);
				erg.push(new Integer(pow));
			}
			w = y;
			c = c.divide(y);
			pow++;
		}
		if ( !(c.isUnit()) )
		{
			Stack ergt = c.modRootShift().squareFreeFactors();
			while (!(ergt.empty())) {
				multiplicity = ((Integer)ergt.pop()).intValue();
//				erg.push(ergt.peek());											// TvO: "peek" statt pop wahrscheinlich falsch
				erg.push(ergt.pop());											// TvO: "peek" statt pop wahrscheinlich falsch
				erg.push(new Integer(multiplicity*modulo.modulo.intValue()));
			}
		}
		return erg;
	}
	
	/** Zerlegt ein quadratfreies Polynom in Faktoren, die das Produkt aller irreduziblen
	 * Faktoren gleichen Grades sind und liefert die Faktoren zusammen mit dem Grad zurück
	 * Zuerst wird der Faktor auf den Stack gepusht und dann der Grad...
	 */
	private Stack distinctDegreeFactors()
	{
		Stack erg = new Stack();
		RemainderRingPolynomial h=x();
		RemainderRingPolynomial x=x();
		RemainderRingPolynomial f=new RemainderRingPolynomial(this);
		RemainderRingPolynomial g;
		int i=0;
		while ( (!(f.isUnit())) && (f.deg >= 2*i)) {
			i++;
			h = h.modPow(modulo.toBigInt(), this);
			g = (h.subtract(x)).gcd(f);
			f = f.divide(g);
			if ( !(g.isUnit()) ) {
				g.makeMonic();
				erg.push(g);
				erg.push(new Integer(i));
			}
		}
		if ( !(f.isUnit()) ) 
		{
			erg.push(f);
			erg.push(new Integer(f.deg));
		}
		return erg;
	}
	
	/**
	 * Liefert einen echten Faktor zurück, wenn bekannt ist, das alle Faktoren genau Grad degree
	 * besitzen...
	 */
	private RemainderRingPolynomial splitEqualDegree(int degree)
	{
		if (deg <= degree) return this;
		while (true) {
			RemainderRingPolynomial a = this.rndPolynomial(deg-1,modulo);
			RemainderRingPolynomial g = gcd(a);
			if ( !(g.isUnit()) )
				return g;
			BigInteger pow = modulo.toBigInt().pow(degree);
			pow = pow.subtract(BigInteger.valueOf(1));
			pow = pow.divide(BigInteger.valueOf(2));
			RemainderRingPolynomial fe = a.modPow(pow,this);
			g = this.gcd(fe.subtract(new RemainderRingPolynomial(new RemainderRing(1,modulo))));
			if ( (!(g.isUnit())) && (!(this.isEqual(g))) )
				return g;
		}
	}
	
	/**
	 * Liefert alle irreduziblen Faktoren des Polynoms zurück
	 */
	public Stack factorize()
	{
		if (deg <= 1) {Stack erg = new Stack(); erg.push(this); return erg;}
		if ( !(modulo.isPrime) ) throw new NotPrimeException(modulo);
		RemainderRingPolynomial tmp1 = new RemainderRingPolynomial(this);
		RemainderRingPolynomial tmp2;
		RemainderRing leitkoeffizient = tmp1.coef[deg];							// TvO: Wird später hinzugefügt.
		tmp1.makeMonic();
		if (tmp1.deg<=1) {
			Stack erg = new Stack();
			erg.push(tmp1);
			return erg;
		}
		// Zuerst besorgen wir uns die quadratfreie Faktorisierung...
		Stack squareFree = tmp1.squareFreeFactors();
		Stack equalDegree;
		Stack erg = new Stack();
		while (!(squareFree.empty()))
		{
			int multiplicity = ((Integer)squareFree.pop()).intValue();
			tmp1 = (RemainderRingPolynomial)squareFree.pop();
			if (tmp1.deg == 1) {
				tmp1.makeMonic();
				for (int i = 1 ; i <= multiplicity ; i++)
					erg.push(tmp1);
			} else {
				equalDegree = tmp1.distinctDegreeFactors();
				while (!(equalDegree.isEmpty()))
				{
					int degree = ((Integer)equalDegree.pop()).intValue();
					tmp1 = (RemainderRingPolynomial)equalDegree.pop();
					if (tmp1.deg <= degree)
					{
						tmp1.makeMonic();
						for (int i = 1 ; i <= multiplicity ; i++)
							erg.push(tmp1);
					} else {
						tmp2 = tmp1.splitEqualDegree(degree);
						tmp1 = tmp1.divide(tmp2);
						equalDegree.push(tmp1);
						equalDegree.push(new Integer(degree));
						equalDegree.push(tmp2);
						equalDegree.push(new Integer(degree));
					}
				}
			}
		}
		if (erg.empty()) erg.push(new RemainderRingPolynomial(leitkoeffizient));			// TvO
		else
		{
			tmp1 = (RemainderRingPolynomial)erg.pop();
			tmp1 = tmp1.multiply(new RemainderRingPolynomial(leitkoeffizient));
			erg.push(tmp1);
		}
		return erg;
	}

	
	/** Eingabe: [g,h,s,t] modulo m mit this = g*h mod m und s*g+t*h = 1 mod m
	 * Ausgabe: [fp,gp,hp,sp,tp] mit fp = this mod m^2 fp = gp*hp mod m^2 und sp*gp+tp*hp = 1 mod m^2
	 */
	public RemainderRingPolynomial[] henselStep(RemainderRingPolynomial[] inputs, Modulus ms)
	{
		RemainderRingPolynomial[] erg = new RemainderRingPolynomial[5];
		RemainderRingPolynomial f = lift(ms);
		RemainderRingPolynomial g = inputs[0].lift(ms);
		RemainderRingPolynomial h = inputs[1].lift(ms);
		RemainderRingPolynomial s = inputs[2].lift(ms);
		RemainderRingPolynomial t = inputs[3].lift(ms);
//		System.out.println();
//		System.out.println("Deg f ,g ,h ,s ,t  :"+f.deg +","+g.deg +","+h.deg+","+s.deg+","+t.deg);
		RemainderRingPolynomial e = f.subtract(g.multiply(h));
		RemainderRingPolynomial[] qr = (s.multiply(e)).divideAndRemainder(h);
		RemainderRingPolynomial gp = g.add(t.multiply(e)).add(qr[0].multiply(g));
		RemainderRingPolynomial hp = h.add(qr[1]);
		RemainderRingPolynomial b = (s.multiply(gp)).add(t.multiply(hp)).subtract(hp.unit());
		RemainderRingPolynomial[] cd = (s.multiply(b)).divideAndRemainder(hp);
		RemainderRingPolynomial sp = s.subtract(cd[1]);
		RemainderRingPolynomial tp = t.subtract(t.multiply(b)).subtract(cd[0].multiply(gp));
		erg[0] = f;
		erg[1] = gp;
		erg[2] = hp;
		erg[3] = sp;
		erg[4] = tp;
//		System.out.println("Deg f*,g*,h*,s*,t* :"+f.deg +","+gp.deg +","+hp.deg+","+sp.deg+","+tp.deg);
		return erg;
	}
		
	/**
	 * Liftet ein Polynom modulo n zu einem Polynom modulo m
	 */
	public RemainderRingPolynomial lift(Modulus m)
	{
		RemainderRingPolynomial erg = new RemainderRingPolynomial(deg,m);
		for (int i = 0; i <=deg ; i++)
			erg.set(this.get(i).lift(m),i);
		return erg;
	}
		
}
