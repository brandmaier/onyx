package arithmetik;

import java.math.*;
import java.util.*;

public class UnivariatePolynomial implements Ring, GcdAble
{
	private BigInteger[] coef;
	int deg;


	/**
	 * Erzeugt ein Polynom das maximal den Grad degree erreichen kann.
	 */
	public UnivariatePolynomial(int degree)
	{
		deg=-1;
		if (degree >= 0) {
			coef = new BigInteger[degree+1];
			for (int i=0; i<=degree ; i++)
                          coef[i] = BigInteger.valueOf(0);
		}
	}

	/**
	 * Erzeugt ein Polynom mit Koeffizienten coefs
	 */
	public UnivariatePolynomial(BigInteger[] coefs)
	{
		deg=-1;
		coef = new BigInteger[coefs.length];
		for (int i=0; i<coefs.length ; i++) {
			if (coefs[i] != null )
				coef[i] = coefs[i].add(BigInteger.valueOf(0));
			else
				coef[i] = BigInteger.valueOf(0);
			if  ( coef[i].signum() != 0 ) deg = i;
		}
	}

	/**
	 * Klont ein Polynom p
	 */
	public UnivariatePolynomial(UnivariatePolynomial p)
	{
		this(p.coef);
	}

	/**
	 * Erzeugt ein Polynom vom Grad maximal degree, mit Koeffizienten aus coefs.
	 */
	public UnivariatePolynomial(BigInteger[] coefs,int degree)
	{
		deg=-1;
		if (degree >= 0) {
			coef = new BigInteger[degree+1];
			for (int i=0; i<=degree; i++) {
				if (coef.length > i)
				{
					if (coef[i] != null)
						coef[i] = coefs[i].add(BigInteger.valueOf(0));
					else
						coef[i] = BigInteger.valueOf(0);
					if  ( coef[i].signum() != 0 ) deg = i;
				} else {
					coef[i]=BigInteger.valueOf(0);
				}
			}
		}
	}

	/**
	 * Kopiert p in ein Polynom vom Grad h"ochstens degree
	 */
	public UnivariatePolynomial(UnivariatePolynomial p,int degree)
	{
		this(p.coef,degree);
	}

	/**
	 * Erzeugt ein Univariates Polynomi zu einem RemainderRingPolynom
	 */
	public UnivariatePolynomial(RemainderRingPolynomial p)
	{
		coef = new BigInteger[p.deg+1];
		deg = -1;
		for (int i = 0; i <= p.deg ; i++)
		{
			coef[i] = p.coef[i].value.add(BigInteger.valueOf(0));
			if (coef[i].signum() != 0) deg = i;
		}
	}

	/**
	 * Generiert ein zuf\uFFFDlliges univariates Polynom vom Grad deg mit Koeffizienten < mx
	 */
	public static UnivariatePolynomial rndPolynomial(int deg, long mx)
	{
		UnivariatePolynomial erg = new UnivariatePolynomial(deg);
		for (int i = 0 ; i<=deg ; i++)
		{
                        erg.set( ( new BigDecimal(Math.random()*mx) ).toBigInteger(), i );
		}
		return erg;
	}


	/**
	 * Berechnet den Grad eines Polynoms neu.
	 */
	private void resetDegree()
	{
		deg = -1;
		for (int i=0 ; i < coef.length; i++) {
			if ( coef[i].signum() != 0 ) deg = i;
		}
	}

	/**
	 *  Setze Koeffizient i auf a
	 */
	public void set(BigInteger a,int i)
	{
		coef[i] = a.add(BigInteger.valueOf(0));
		if ( a.signum() == 0 )
		{
			if ( deg == i ) resetDegree();
		} else {
			deg = Math.max(deg,i);
		}
	}

	/**
	 * Liefert i-ten Koeffizienten
	 */
	public BigInteger get(int i)
	{
		if (i > deg)
		{
			return BigInteger.valueOf(0);
		} else {
			return coef[i].add(BigInteger.valueOf(0));
		}
	}

	/**
	 *  Berechnet this+b
	 */
	public UnivariatePolynomial add(UnivariatePolynomial b)
	{
		UnivariatePolynomial erg = new UnivariatePolynomial(Math.max(deg,b.deg));
		for (int i=0 ; i <= Math.max(deg,b.deg) ; i++)
		{
			erg.set(get(i).add(b.get(i)),i);
		}
		return erg;
	}

	public Ring abs_add (Ring b)
	{
		return add ((UnivariatePolynomial)b);
	}

	/**
	 * Testet this == b
	 */
	public boolean isEqual(UnivariatePolynomial b)
	{
		if (deg != b.deg) return false;
		for (int i=0 ; i <= deg ; i++)
			if ( get(i).compareTo(b.get(i)) != 0 ) return false;
		return true;
	}

	public boolean abs_isEqual (Ring b)
	{
		return isEqual((UnivariatePolynomial)b);
	}

	/**
	 * Berechnet this*bx^pow
	 */
	public UnivariatePolynomial monomialMultiply(BigInteger b,int pow)
	{
		UnivariatePolynomial erg = new UnivariatePolynomial(pow+deg);
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
	public UnivariatePolynomial multiply(UnivariatePolynomial b)
	{
		UnivariatePolynomial erg = new UnivariatePolynomial(deg+b.deg);
		for (int i=0; i<=deg; i++)
			for (int j=0; j<=b.deg; j++)
			erg.set( (get(i).multiply(b.get(j))).add(erg.get(i+j)),i+j);
		return erg;
	}

	public Ring abs_multiply (Ring b)
	{
		return multiply((UnivariatePolynomial)b);
	}

	/**
	 * liefert den Leitkoeffizienten.
	 */
	public BigInteger leadingCoefficient()
	{
		if (deg < 0 ) return BigInteger.valueOf(0);
		return coef[deg].add(BigInteger.valueOf(0));
	}

	/**
	 * liefert -this
	 */
	public UnivariatePolynomial negate()
	{
		UnivariatePolynomial erg = new UnivariatePolynomial(deg);
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
	public UnivariatePolynomial pow(long exp)
	{
		if (exp==0) return unit();
		if (exp==1) return new UnivariatePolynomial(this);
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
	public UnivariatePolynomial subtract(UnivariatePolynomial b)
	{
		return this.add(b.negate());
	}

	public Ring abs_subtract (Ring b)
	{
		return subtract((UnivariatePolynomial)b);
	}

	/**
	 * liefert 1
	 */
	public UnivariatePolynomial unit()
	{
		UnivariatePolynomial erg = new UnivariatePolynomial(0);
		erg.set(BigInteger.valueOf(1),0);
		return erg;
	}

	/**
	 * liefert x
	 */
	public UnivariatePolynomial x()
	{
		UnivariatePolynomial erg = new UnivariatePolynomial(1);
		erg.set(BigInteger.valueOf(1),1);
		return erg;
	}

	public Ring abs_unit ()
	{
		return unit();
	}

	/**
	 * liefert das Nullpolynom
	 */
	public UnivariatePolynomial zero()
	{
		return new UnivariatePolynomial(-1);
	}

	public Ring abs_zero ()
	{
		return zero();
	}

	/**
	 * Generiert einen String, der das momentane Polynom repr\uFFFDsentiert
	 */
	public String toString()
	{
		if (deg < 0) return "0";
		String erg = "" ;
		if (deg == 0) return erg + get(0);
		if ( get(0).signum() != 0) erg = erg + get(0);
		for (int i=1; i <=deg ; i++)
		{
			if ( get(i).signum() > 0 ) erg = erg + " +" + get(i) + "*x^" + i;
			else if (get(i).signum() < 0 ) erg = erg + " " + get(i) + "*x^" + i;
		}
		return erg;
	}

	/**
	 * Berechnet [this div g , this mod g]
	 * Funktioniert nur, wenn g|this oder g unit\uFFFDr
	 */
	public UnivariatePolynomial[] divideAndRemainder(UnivariatePolynomial g)
	{
		int k = deg - g.deg;
		UnivariatePolynomial[] erg = new UnivariatePolynomial[2];
		UnivariatePolynomial q = new UnivariatePolynomial(k);
		UnivariatePolynomial r = new UnivariatePolynomial(this);

		while (k>=0)
		{
			BigInteger co = r.get(r.deg).divide(g.get(g.deg));
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
		return divideAndRemainder((UnivariatePolynomial) arg2);
	}

	/**
	 * Berechnet this pseudodiv g.
	 */
	public UnivariatePolynomial[] pseudoDivision(UnivariatePolynomial g)
	{
		UnivariatePolynomial r = new UnivariatePolynomial(this);
		UnivariatePolynomial q = this.zero();
		UnivariatePolynomial erg[] = new UnivariatePolynomial[2];
		int k = r.deg-g.deg;
		if (k < 0) {
			erg[0]=r.zero();
			erg[1]=r;
			return erg;
		}
		Stack tmp = new Stack();
		while (k >= 0)
		{
			UnivariatePolynomial t = (r.unit()).monomialMultiply(r.leadingCoefficient(),k);
			tmp.push(t);
			r = r.monomialMultiply(g.leadingCoefficient(),0).subtract(t.multiply(g));
			k = r.deg - g.deg;
		}
		q = (UnivariatePolynomial)tmp.pop();
		UnivariatePolynomial f = r.unit();
		while ( !(tmp.isEmpty()) )
		{
			f = f.monomialMultiply(g.leadingCoefficient(),0);
			q = q.add( ((UnivariatePolynomial)tmp.pop()).multiply(f) );
		}
		erg[0] = q;
		erg[1] = r;
		return erg;
	}

	/**
	 * Berechnet this pseudomod g.
	 */
	public UnivariatePolynomial pseudoRemainder(UnivariatePolynomial g)
	{
		UnivariatePolynomial r = this;
		int k = r.deg - g.deg;
		if (k < 0) return r;
		while (k >= 0)
		{
			UnivariatePolynomial t = unit().monomialMultiply(r.leadingCoefficient(),k);
			r = (r.monomialMultiply(g.leadingCoefficient(),0)).subtract(t.multiply(g));
			k = r.deg - g.deg;
		}
		return r;
	}

	/**
	 * liefert this mod g
	 */
	public UnivariatePolynomial remainder(UnivariatePolynomial g)
	{
		return (divideAndRemainder(g))[1];
	}

	public GcdAble abs_remainder(GcdAble arg2)
	{
		return remainder((UnivariatePolynomial) arg2);
	}

	/**
	 * liefert this div g
	 */
	public UnivariatePolynomial divide(UnivariatePolynomial g)
	{
		return divideAndRemainder(g)[0];
	}

	public GcdAble abs_divide(GcdAble arg2)
	{
		return divide((UnivariatePolynomial) arg2);
	}

	/**
	 * liefert ggt(this,g)
	 */
	public UnivariatePolynomial gcd(UnivariatePolynomial g)
	{
		// long d = (BigInteger.valueOf(content()).gcd(BigInteger.valueOf(g.content()))).longValue();
                BigInteger d = content().gcd(g.content());
		UnivariatePolynomial u;
		UnivariatePolynomial v;
		if (deg >= g.deg)
		{
			u = primitivePart();
			v = g.primitivePart();
		} else {
			u = g.primitivePart();
			v = primitivePart();
		}
		UnivariatePolynomial r = u.pseudoRemainder(v);
		while (r.deg > 0)
		{
			u = v;
			v = r.primitivePart();
			r = u.pseudoRemainder(v);
		}
		if (r.deg == 0) return unit().monomialMultiply(d,0);
		return v.monomialMultiply(d,0);
	}

	public GcdAble abs_gcd(GcdAble arg2)
	{
		return gcd((UnivariatePolynomial) arg2);
	}

	/**
	 * liefert kgV(this,g)
	 */
	public UnivariatePolynomial scm(UnivariatePolynomial g)
	{
		return (g.multiply(this)).divide(g.gcd(this));
	}

	public GcdAble abs_scm(GcdAble arg2)
	{
		return scm((UnivariatePolynomial) arg2);
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
		return ( (deg == 0) && ( (coef[0].compareTo(BigInteger.valueOf(1)) == 0) || (coef[0].compareTo(BigInteger.valueOf(-1)) == 0) ) );
	}

	/**
	 * liefert die Ableitung von this
	 */
	public UnivariatePolynomial derive()
	{
		UnivariatePolynomial erg = new UnivariatePolynomial(deg-1);
		for (int i=1 ; i <= deg ; i++)
			erg.set(get(i).multiply(BigInteger.valueOf(i)),i-1);
		return erg;
	}

	/**
	 * Testet, ob this quadratfrei ist.
	 */
	public boolean isSquareFree()
	{
		return gcd(derive()).deg==0;
	}

	/**
	 * Liefert die quadratfreie Zerlegung von this zur\uFFFDck
	 */
	public UnivariatePolynomial[] squarefree()
	{
		Stack est = new Stack();
		UnivariatePolynomial fd = derive();
		UnivariatePolynomial u = gcd(fd);
		UnivariatePolynomial v = divide(u);
		UnivariatePolynomial w = fd.divide(u);
		UnivariatePolynomial h;
		int i = 0;
		do
		{
			fd = v.derive();
			h = v.gcd(w.subtract(fd));
			v = v.divide(h);
			w = (w.subtract(fd)).divide(h);
			est.push(h);
			est.push(new Integer(i));
			i++;
		}
		while ( !(v.isUnit()) );
		UnivariatePolynomial[]  erg = new UnivariatePolynomial[i];
		while ( !(est.isEmpty()) ) {
			int pos = ((Integer)est.pop()).intValue();
			erg[pos] = (UnivariatePolynomial)est.pop();
		}
		return erg;
	}

	/**
	 * berechnet die Max-Norm des Polynoms
	 */
	public BigInteger maxNorm()
	{
		BigInteger erg = BigInteger.valueOf(0);
		for (int i = 0 ; i <= deg ; i++)
                        // erg = (coef[i].abs()).max(erg);
			erg = erg.max(coef[i].abs());
		return erg;
	}

	/**
	 * berechnet die 1-Norm des Polynoms
	 */
	public BigInteger oneNorm()
	{
		BigInteger erg = BigInteger.valueOf(0);
		for (int i = 0 ; i <= deg ; i++)
			erg = erg.add(coef[i].abs());
		return erg;
	}

	/**
	 * berechnet die euklidische Norm des Polynoms
	 */
	public BigDecimal euclidNorm()
	{
		double erg = 0;
		for (int i = 0; i <= deg ; i++)
			erg = erg + (coef[i].pow(2)).doubleValue();
		return new BigDecimal(Math.sqrt(erg));
	}

	/**
	 * Faktorisiert this in irreduzible Faktoren, wobei this quadratfrei sein mu\uFFFD!
	 * TvO: Und, anbetracht der ersten Zeile, offenbar positiv...
	 */
	public Stack factorizeSquarefreeHensel()
	{
		if (leadingCoefficient().signum() == -1 ) return negate().factorizeSquarefreeHensel();
		Stack erg = new Stack();
		if (deg <= 1) {
			erg.push(this);
			return erg;
		}
		UnivariatePolynomial f = primitivePart();
                BigInteger fdeg = BigInteger.valueOf(f.deg);
		BigInteger ff = content();
		erg.push(unit().monomialMultiply(ff,0));

		BigInteger b = f.leadingCoefficient();
		
		
		BigInteger A = f.maxNorm();
		BigInteger B = BigInteger.valueOf(Math.round(Math.ceil(Math.sqrt(f.deg+1))));
        BigInteger two = BigInteger.valueOf(1).add(BigInteger.valueOf(1));
        B = B.multiply( two.pow(f.deg) );
		B = B.multiply(A).multiply(b);
		//double C = Math.pow(f.deg+1,f.deg*2)*Math.pow(A,f.deg*2-1);

		// TvO: Die folgende Berechnung wird gar nicht genutzt.
		/*
		
        BigInteger C = (fdeg.add(BigInteger.valueOf(1)).pow(f.deg*2)).multiply(A.pow(f.deg*2-1));
		double gamma = Math.ceil(2*Math.log(C.doubleValue())/Math.log(2));
		double primesized = Math.ceil (2*gamma*Math.log(gamma)/Math.log(2));
        BigInteger primesize = (new BigDecimal(primesized)).toBigInteger();
		*/
        BigInteger p;
		Modulus m;
		RemainderRingPolynomial fmod;
		// Probabilistische Primzahlwahl
/*		do
		{
			do
			{
				p = Math.round( Math.random()*primesize );
				m = new Modulus(p);
			} while ( !( (m.isPrime) && (p != 2) ) );
			fmod = new RemainderRingPolynomial(f,m);
		} while ( !( (fmod.isSquareFree()) && ((b % p) != 0) ) );
*/
		// Deterministische Primzahlwahl
		p = BigInteger.valueOf(3);
		m = new Modulus(p);
		fmod = new RemainderRingPolynomial(f,m);
//		System.out.println(m);
		//while ( (!fmod.isSquareFree()) || (dividesCoefs(p)) )
		while ( (!fmod.isSquareFree()) || (( (b.mod(p)).signum()) == 0) )
		{
			System.out.println("Bad Prime : " + p);
			do
			{
				p = p.add(BigInteger.valueOf(2));
				m = new Modulus(p);
			} while (!m.isPrime);
			fmod = new RemainderRingPolynomial(f,m);
//			System.out.println(m);
		}
//		System.out.println("Prime choosen : " + p);


		// TvO: Die Grenzberechnung mit doubles ist unnötig ungenau, wir testen, ob der modulus
		// des Faktortrees über 2B + 1 hinauswächst.
		/*
        double l = Math.ceil( Math.log(2*B.doubleValue()+1)/Math.log(p.doubleValue()));
        double d = Math.ceil( Math.log(l)/Math.log(2) );
		*/
		BigInteger bound = B.multiply(BigInteger.valueOf(2)).add(BigInteger.valueOf(1));

//		System.out.println("Modulo Polynomial: " + fmod);
//		System.out.println("Bound : " + B);
//		System.out.print("Starting lifting ");
		FactorTree ft = new FactorTree(f,m);
//		System.out.println("### Checking Tree -- " + ft.check() );
//		System.out.println("***Leaves :  \n" + ft.leavesToString() + "***done");
//		System.out.println(ft);
//		System.out.print("setup done ");

//		for (long i = 1 ; i <= Math.round(d) ; i++ )
		while (ft.modulusSmallerThan(bound))
		{
			ft = ft.lift();
//			System.out.println("### Checking Tree -- " + ft.check() );
//			System.out.println("***Leaves : \n" + ft.leavesToString() + "***done");
//			System.out.print(" .." + ft.modulo + "##");
//			System.out.println(ft);
		}
//		System.out.println(" done");
		RemainderRingPolynomial[] modfact = ft.getLeaves();
		m = modfact[0].modulo;
		RemainderRingPolynomial prd = new RemainderRingPolynomial(new RemainderRing(b,m));
//		System.out.print("Factor Degrees : ");
		for (int i = 0 ; i < modfact.length ; i++)
		{
//			modfact[i].makeMonic();
			prd = prd.multiply(modfact[i]);
//			System.out.print(modfact[i].deg + ", ");
//			System.out.println("Modfactor: "+ modfact[i]);
		}
//		System.out.println();
//		System.out.println("---Produkt     : "+prd);
//		System.out.println("---Produkt*inv : "+prd.monomialMultiply(ft.inverse,0));
//		System.out.println("---Produkt*lc  : "+prd.monomialMultiply(new RemainderRing(b,m),0));

		Subset modFactors = new Subset(modfact);

		while  ( !modFactors.halfSubset() )
		{
			boolean cut = false;
			RemainderRingPolynomial gs = new RemainderRingPolynomial(new RemainderRing(b,m));
			Object[] subset = modFactors.get();
			for (int i = 0 ; i < subset.length ; i++)
				gs = gs.multiply((RemainderRingPolynomial)subset[i]);
			UnivariatePolynomial gsu = (new UnivariatePolynomial(gs)).primitivePart();
//			System.out.println("Candidate Factor: (" +modFactors+ ") " + gsu);
			UnivariatePolynomial quo = f.trueDivide(gsu);
//			System.out.println(modFactors + "yields Faktor " + gsu.primitivePart() + " => " + quo);
//			System.out.print("Modular : " + gs + " .... ");
//			gs.makeMonic();
//			System.out.println(gs);
			if (!quo.isZero())
			{
//				System.out.println("---choosen---");
//				System.out.print(modFactors);
				modFactors.remove();
//				System.out.println(" cut to " + modFactors);
				erg.push(gsu);
				f = quo;
				b = f.leadingCoefficient();
				cut = true;
			}
			if (!cut) modFactors.next();
		}
		erg.push(f);
		return erg;
	}

/*	**
	 * !DontUse! Langsame BigPrime-Variante
	 * factorizeSquarefreeHensel benutzen!
	 * Faktorisiert ein Polynom
         * Die Routine Hat die Uebersetzung von long-UnivariatePolynomials auf
         * BigInteger-UnivariatePolynomials nicht ueberlebt....
	 *
	public Stack factorizeBigPrime()
	{
		Stack erg = new Stack();
		if (deg <= 1) {
			erg.push(new UnivariatePolynomial(this));
			return(erg);
		}
		RemainderRingPolynomial fmod;

		BigInteger b = leadingCoefficient();
		// Fehler bei negativem Leitkoeffizient; workaround durch negieren. TVO
                if (b.signum() == -1)
                        return (this.negate()).factorizeBigPrime();

		BigInteger A = maxNorm();
		System.out.println(A);
                Double Bd = Math.ceil( Math.sqrt(deg+1)*Math.pow(2,deg)
		BigInteger B = Math.round(Math.ceil( Math.sqrt(deg+1)*Math.pow(2,deg)*A*b ));
		System.out.println(B);

		Modulus m;
		long p;
		do {
			do {
				p = Math.round( Math.random()*(2*B-2) )+2*B+1;
				m = new Modulus(p);
			} while ( !m.isPrime );
			System.out.println("Modulus : " + m);
			fmod = new RemainderRingPolynomial(this,m);
		} while ( !fmod.isSquareFree() );

		Stack modfs = fmod.factorize();

		RemainderRingPolynomial[] modfact = new RemainderRingPolynomial[modfs.size()];
		System.out.println(modfs.size() + " Factors modulo " + m);
		RemainderRingPolynomial prd = fmod.unit();
		for (int i = 0 ; !(modfs.isEmpty()) ; i++)
		{
			modfact[i] = (RemainderRingPolynomial)modfs.pop();
			modfact[i].makeMonic();
			System.out.print(new UnivariatePolynomial(modfact[i])+ " , ");
			prd = prd.multiply(modfact[i]);
		}
		System.out.println("Produkt: " + prd );
		System.out.println("........ " + prd.monomialMultiply(fmod.leadingCoefficient(),0));
		UnivariatePolynomial fs = new UnivariatePolynomial(this);
		Subset modFactors = new Subset(modfact);

		while  ( !modFactors.halfSubset() )
		{
			boolean cut = false;
			RemainderRingPolynomial gs = new RemainderRingPolynomial(new RemainderRing(b,m));
			Object[] subset = modFactors.get();
			for (int i = 0 ; i < subset.length ; i++)
				gs = gs.multiply((RemainderRingPolynomial)subset[i]);
			UnivariatePolynomial gsu = new UnivariatePolynomial(gs);
			UnivariatePolynomial quo = fs.trueDivide(gsu.primitivePart());
//			System.out.println("Faktor " + gsu.primitivePart() + " => " + quo);
			if (!quo.isZero())
			{
//				System.out.println("---choosen---");
				modFactors.remove();
				erg.push( (gsu.primitivePart()) );
				fs = quo;
				b = fs.leadingCoefficient();
				cut = true;
			}
			if (!cut) modFactors.next();
		}
		if (negiert)
			for (int i=0; i<fs.coef.length; i++) fs.coef[i] = fs.coef[i]*-1;

		erg.push(fs);
		return erg;
	}
*/

	/**
	 * Berechnet den Inhalt von this
	 */
	public BigInteger content()
	{
		if (deg < 0) return BigInteger.valueOf(0);
		BigInteger erg = coef[0].add(BigInteger.valueOf(0));
		for (int i = 1; i <= deg ; i++)
			erg = erg.gcd(coef[i]);
		return erg;
	}

	/**
	 * Berechnet den Primitiven Anteil von this
	 */
	public UnivariatePolynomial primitivePart()
	{
		if (deg < 0) return zero();
		UnivariatePolynomial erg = new UnivariatePolynomial(deg);
		BigInteger inh = content();
		for (int i=0; i <= deg ; i++)
			erg.set(coef[i].divide(inh),i);
		return erg;
	}

	/**
	 * Liefert this div g, falls this von g geteilt wird, 0 sonst
	 */
	public UnivariatePolynomial trueDivide(UnivariatePolynomial g)
	{
		int k = deg - g.deg;
		UnivariatePolynomial erg = zero();
		UnivariatePolynomial f = new UnivariatePolynomial(this);
		if ( k < 0 ) return zero();
		while (k >= 0)
		{
			if ((f.leadingCoefficient().mod(g.leadingCoefficient()).signum()) != 0 ) return zero();
			BigInteger coef = f.leadingCoefficient().divide(g.leadingCoefficient());
			erg = erg.add(unit().monomialMultiply(coef,k));
			f = f.subtract(g.monomialMultiply(coef,k));
			k = f.deg-g.deg;
		}
		if ( !f.isZero() ) return zero();
		else return erg;
	}

	/**
	 * Testet, ob die Koeffizienten ungleich Null durch p teilbar sind
	 */
	public boolean dividesCoefs(BigInteger p)
	{
		for (int i = 0 ; i <= deg; i++)
    			if ( (coef[i].mod(p).signum() == 0) && (coef[i].signum() != 0) ) return true;
  		return false;
	}

	public QPolynomial toQPolynomial(int index)
	{
		QPolynomial erg = new QPolynomial();
		QPolynomial x = new QPolynomial(index);
		for (int i=0; i<=deg; i++)
			erg = erg.add(x.pow(i).multiply(new QPolynomial(new Qelement(coef[i]))));
		return erg;
	}
}

