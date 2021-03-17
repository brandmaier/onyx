package arithmetik;

import java.math.*;

public class Polynomial implements Ring, GcdAble
{
	Ring[] co;
	boolean ringSpecified;	// Zeigt an, ob ein Ring angegeben wurde oder 
	Polynomial() {this(0);}
	Polynomial (Ring[] coin)
	{
		if (coin.length==0)
		{
			co = new Ring[]{new BigIntWrapper(0)};
			ringSpecified = false;
		}
		co = new Ring[coin.length];
		for (int i=0; i<coin.length; i++)
			co[i] = coin[i];
		ringSpecified = true;
		clean();
	}
							// BigIntWrapper benutzt wird. Falls true, kann
							// das Polynom noch mit jedem Mist verwendet werden.
	
	Polynomial (long i)
	{
		this(new BigIntWrapper(i),0);
		ringSpecified = false;
	}
	Polynomial (Polynomial copy)
	{
		co = new Ring[copy.co.length];
		for (int i=0; i<copy.co.length; i++)
			co[i] = copy.co[i];
		ringSpecified = copy.ringSpecified;
	}
	Polynomial (Ring f, int degree) // Constructor nur mit Ring ist gefährlich in
									// Verwechslung mit Polynomial.
	{
		co = new Ring[degree+1];
		co[degree]=f;
		for (int i=0; i<degree; i++)
			co[i] = f.abs_zero();
		ringSpecified = true;
		clean();
	}
	public Ring abs_add (Ring b) {return add((Polynomial)b);}
	public GcdAble abs_divide(GcdAble arg2)  {return divide((Polynomial)arg2);}
	public GcdAble[] abs_divideAndRemainder(GcdAble arg2)  {return divideAndRemainder((Polynomial)arg2);}
	public GcdAble abs_gcd(GcdAble arg2) {return gcd((Polynomial)arg2);}
	public boolean abs_isEqual (Ring b) {return isEqual((Polynomial)b);}
	public Ring abs_multiply (Ring b) {return multiply ((Polynomial)b);}
	public Ring abs_negate () {return negate();}
	public Ring abs_pow(long exp) {return pow(exp);}
	public GcdAble abs_remainder(GcdAble arg2)  {return remainder((Polynomial)arg2);}
	public GcdAble abs_scm(GcdAble arg2) {return scm((Polynomial)arg2);}
	public Ring abs_subtract (Ring b) {return subtract ((Polynomial)b);}
	public Ring abs_unit () {return unit ();}
	public Ring abs_zero () {return zero ();}
	Polynomial add (Polynomial arg2)
	{
		Polynomial p1 = new Polynomial(this), p2 = new Polynomial(arg2);
		if (p1.co.length==0) return p2;
		if (p2.co.length==0) return p1;
		if ((p1.ringSpecified) && (!p2.ringSpecified)) p2 = p2.specifyTo(p1.getRing());
		if ((!p1.ringSpecified) && (p2.ringSpecified)) p1 = p1.specifyTo(p2.getRing());
		Polynomial erg = new Polynomial(p1.getRing(),Math.max(p2.getDegree(), p1.getDegree()));
		for (int i=0; i<erg.co.length; i++)
		{
			if (i<p1.co.length) erg.co[i] = p1.co[i]; else erg.co[i] = p1.getRing().abs_zero();
			if (i<p2.co.length) erg.co[i] = erg.co[i].abs_add(p2.co[i]);
		}
		erg.ringSpecified = p1.ringSpecified;
		erg.clean();
		return erg;
	}
	// Löscht alle hohen Grade mit Koeffizient 0, außer Nulldarstellung, die ein 0 im Koeffizientvektor hat.
	// Ändert this.
	private Polynomial clean()
	{
		int c = co.length-1;
		while ((c>=1) && (co[c].abs_isEqual(co[c].abs_zero()))) c--;
		if (c<co.length-1)
		{
			Ring[] newco = new Ring[c+1];
			for (int i=0; i<=c; i++) newco[i]=co[i];
			co = newco;
			if (c==-1) this.ringSpecified = false;
		}
		return this;
	}
	public Polynomial divide(Polynomial snd)
	{
		return (divideAndRemainder(snd))[0];
	}
	public Polynomial[] divideAndRemainder(Polynomial snd)
	{
		Ring r = this.getRing();
		if (!(r instanceof GcdAble)) 
			throw new RuntimeException("Ring in divide of Polynomial needs GcdAble");

		Polynomial[] erg = new Polynomial[]{zero(), new Polynomial(this)};
		if ((isZero()) || (co.length<snd.co.length)) return erg;

		Polynomial p1 = new Polynomial(this), p2 = new Polynomial(snd);
		if (p2.isZero()) throw new RuntimeException("Division by zero in Polynomial.");
		if ((p1.ringSpecified) && (!p2.ringSpecified)) p2 = p2.specifyTo(p1.getRing());
		if ((!p1.ringSpecified) && (p2.ringSpecified)) p1 = p1.specifyTo(p2.getRing());

		GcdAble[] fak = ((GcdAble)p1.co[p1.co.length-1]).abs_divideAndRemainder((GcdAble)p2.co[p2.co.length-1]);
		if (!((Ring)fak[1]).abs_isEqual(((Ring)fak[1]).abs_zero())) return erg;

		Polynomial rem = p1.subtract(p2.multiply(new Polynomial(((Ring)fak[0]),p1.co.length-p2.co.length)));
		rem.ringSpecified = p1.ringSpecified;
		erg = rem.divideAndRemainder(p2);
		erg[0] = erg[0].add(new Polynomial(((Ring)fak[0]),p1.co.length-p2.co.length));
		erg[0].ringSpecified = p1.ringSpecified;
		return erg;
	}
	public Ring evalAt(Ring x)
	{
		Ring erg = co[0];
		Ring r = x.abs_unit();
		for (int i=1; i<co.length; i++)
		{
			r = r.abs_multiply(x);
			erg = erg.abs_add(r.abs_multiply(co[i]));
		}
		return erg;
	}
	// euklidischer Algorithmus mit Pseudodivision.
	// Funktioniert auch für Multivariate, wobei aufgepasst werden muss, dass die gleiche Rekursionstiefe
	// der Polynome mit den gleichen Variablen belegt ist (von unten beginnend; die Tiefe muss nicht gleich sein).
	public Polynomial gcd(Polynomial arg2)
	{
		Polynomial p1 = normalizeMultivariate(), p2 = arg2.normalizeMultivariate();
		if (p1.isZero()) return p2;
		if (p2.isZero()) return p1;
		if ((p1.ringSpecified) && (!p2.ringSpecified)) p2 = p2.specifyTo(p1.getRing());
		if ((!p1.ringSpecified) && (p2.ringSpecified)) p1 = p1.specifyTo(p2.getRing());

		int i = p1.getMultivariateDepth();
		int j = p2.getMultivariateDepth();
		if (i>j) return new Polynomial(new Ring[]{(Ring)((GcdAble)p1.getContent()).abs_gcd((GcdAble)p2)});
		if (i<j) return new Polynomial(new Ring[]{(Ring)((GcdAble)p2.getContent()).abs_gcd((GcdAble)p1)});

		Ring inhalt1 = p1.getContent();
		Ring inhalt2 = p2.getContent();
		Ring inhaltGgt = (Ring)((GcdAble)inhalt1).abs_gcd((GcdAble)inhalt2);

		Polynomial eins = p1.divide(new Polynomial(new Ring[]{inhalt1}));
		Polynomial zwei = p2.divide(new Polynomial(new Ring[]{inhalt2}));

		if (eins.getDegree() < zwei.getDegree()) 
		{
			Polynomial t = eins;
			eins = zwei;
			zwei = t;
		}
		
		int g1 = eins.getDegree(), g2 = zwei.getDegree();
		while (g2!=0)
		{
			Ring l1 = eins.getLeadingCoefficient();
			Ring l2 = zwei.getLeadingCoefficient();
			Ring ggT = (Ring)((GcdAble)l1).abs_gcd((GcdAble)l2);
			Polynomial faktor1 = (new Polynomial(new Ring[]{(Ring)(((GcdAble)l1).abs_divide((GcdAble)ggT))})).multiply( (p1.getX()).pow(g1-g2) );
			Polynomial faktor2 = new Polynomial(new Ring[]{(Ring)((GcdAble)l2).abs_divide((GcdAble)ggT)});
			Polynomial neu = (eins.multiply(faktor2)).subtract(zwei.multiply(faktor1));
			neu = neu.getPrimepart().normalizeMultivariate();
			int neugrad = neu.getDegree();
			if (neugrad < g2)
			{
				eins = zwei;
				zwei = neu;
				g1 = g2;
				g2 = neugrad;
			} else {
				eins = neu;
				g1 = neugrad;
			}
		}
		if (!zwei.isZero()) return new Polynomial(new Ring[]{inhaltGgt});
		return eins.multiply(new Polynomial(new Ring[]{inhaltGgt}));
	}
	// Grad ist 0 bei Nullpolynom.
	int getDegree() 
	{	
		return co.length-1;	
	}
	Ring getRing() 
	{
		return co[0].abs_unit();
	}
/**
 * Insert the method's description here.
 * Creation date: (26.05.2002 11:41:44)
 * @return arithmetik.Polynomial
 */
public Polynomial getX() 
{
	return getXStatic(getRing());
}
	// Interpoliert ein Polynom, dass an gleichmäßigen Knoten zwischen 0 und 1
	// die Werte von v annimmt. Der Vektor muss UnitRootComplete sein, da hier das
	// Fourierverfahren angewendet wird.
	public static Polynomial interpolate(RingVector v)
	{
		int n = v.getRows();
		Ring field = v.getValue(1);
		Ring zwei = field.abs_unit().abs_add(field.abs_unit());
		
		if ((!(field instanceof UnitRootComplete)) || (!(field instanceof Field)))
			throw new RuntimeException("interpolation needs field with unit roots.");
		
		Ring halb = ((Field)zwei).abs_reciprocal();
		
		RingVector f = new RingVector(field, 2*n);
		for (int i=1; i<=n; i++)
		{
			f.setValue(v.getValue(i),i);
			f.setValue(v.getValue(i),2*n+1-i);
		}
		
		((UnitRootComplete)field).getPrimitiveUnitRoot(2*n);	// initialisieren der EW
		RingVector d = f.FFT();
		
		for (int i=0; i<n; i++)
		{
			Ring ew = (Ring)((UnitRootComplete)field).getUnitRoot(4*n, /*-*/i);
			Field fak = ((Field)Polynomial.multiplyRingWithInt(field.abs_unit(),2*n)).abs_reciprocal();
			fak = (Field)fak.abs_multiply(zwei).abs_multiply(ew);
			d.setValue(d.getValue(i+1).abs_multiply(fak), i+1);
		}
		
		Polynomial x = getXStatic(field);
		Polynomial z = new Polynomial(zwei,0);
		Polynomial[] b = new Polynomial[n+2]; 
		b[n+1] = new Polynomial(field.abs_zero(),0); 
		b[n] = new Polynomial(field.abs_zero(),0);
		
		for (int i=n-1; i>=0; i--)
		{
			b[i] = z.multiply(x.multiply(b[i+1])).subtract(b[i+2]).add(
											new Polynomial(d.getValue(i+1),0));
		}
		
		return (b[0].subtract(b[2])).scalarMultiply(halb).clean();
	}
	public boolean isEqual(Polynomial arg2)
	{
		if (co.length != arg2.co.length) return false;
		for (int i=0; i<co.length; i++)
			if (!co[i].abs_isEqual(arg2.co[i])) return false;
		return true;
	}
	public boolean isZero()
	{
		return ((co.length == 1) && (co[0].abs_isEqual(co[0].abs_zero())));
	}
	Polynomial multiply (Polynomial arg2)
	{
		Polynomial p1 = new Polynomial(this), p2 = new Polynomial(arg2);
		if (p1.co.length==0) return p1;
		if (p2.co.length==0) return p2;
		if ((p1.ringSpecified) && (!p2.ringSpecified)) p2 = p2.specifyTo(p1.getRing());
		if ((!p1.ringSpecified) && (p2.ringSpecified)) p1 = p1.specifyTo(p2.getRing());
		Polynomial erg = new Polynomial(getRing(),getDegree()+arg2.getDegree());
		erg.ringSpecified = p1.ringSpecified;
		for (int i=0; i<erg.co.length; i++)
		{
			erg.co[i] = p1.getRing().abs_zero();
			for (int j=0; j<=i; j++)
			{
				if ((j<p1.co.length) && (i-j<arg2.co.length))
					erg.co[i] = erg.co[i].abs_add(p1.co[j].abs_multiply(p2.co[i-j]));
			}
		}
		return erg.clean();
	}
	public static Ring multiplyRingWithInt(Ring r, int i)
	{
		if (i<0) return multiplyRingWithInt(r, -i).abs_negate();
		if (i==0) return r.abs_zero();
		if (i==1) return r;
		Ring erg = multiplyRingWithInt(r, i/2);
		erg = erg.abs_add(erg);
		if ((i%2)==1) return erg.abs_add(r);
		else return erg;
	}
	public Polynomial negate()
	{
		Polynomial erg = new Polynomial(getRing(), getDegree());
		erg.ringSpecified = ringSpecified;
		for (int i=0; i<co.length; i++)
			erg.co[i] = co[i].abs_negate();
		return erg;
	}
	public Polynomial pow(long i)
	{
		if (i==0) return new Polynomial(new Ring[]{getRing().abs_unit()});
		Polynomial erg = pow(i/2).sqr();
		if ((i%2)==1) return erg.multiply(this);
		else return erg;
	}
	public Polynomial remainder(Polynomial snd)
	{
		return (divideAndRemainder(snd))[1];
	}
	public Polynomial scalarMultiply(Ring r)
	{
		Polynomial p1 = new Polynomial(this);
		if ((!p1.ringSpecified) && (!(r instanceof BigIntWrapper))) 
			p1 = p1.specifyTo(r);
		Polynomial erg = new Polynomial(p1.getRing(), p1.getDegree());
		erg.ringSpecified = p1.ringSpecified;
		for (int i=0; i<erg.co.length; i++)
			erg.co[i] = p1.co[i].abs_multiply(r);
		erg.clean();
		return erg;
	}
	public Polynomial scm(Polynomial snd)
	{
		return snd.multiply(this.divide(this.gcd(snd)));
	}
	// Legt den Ring des Polynoms auf r fest.
	public Polynomial specifyTo(Ring r)
	{
		Polynomial erg = new Polynomial(r, getDegree());
		for (int i=0; i<=getDegree(); i++)
			erg.co[i] = multiplyRingWithInt(r.abs_unit(), ((BigIntWrapper)co[i]).value.intValue());
		return erg;			
	}
	Polynomial sqr() {return multiply(this);}
	Polynomial subtract (Polynomial arg2) {return add(arg2.negate());}
	public QPolynomial toQPolynomial() {return toQPolynomial(0);}
	// Geht nur mit Multivariaten Polynom über Qelement
	public QPolynomial toQPolynomial(int identifierNr)
	{
		Ring r = getRing();
		QPolynomial erg = new QPolynomial();
		for (int i=0; i<co.length; i++)
		{
			QPolynomial x = new QPolynomial(identifierNr).pow(i);
			QPolynomial fak;
			if (r instanceof Polynomial) fak = ((Polynomial)co[i]).toQPolynomial(identifierNr+1);
			else fak = new QPolynomial((Qelement)co[i]);
			erg = erg.add(x.multiply(fak));
		}
		return erg;
	}
	public String toString()
	{
		if (isZero()) return "0";
		int d = getMultivariateDepth();
		String xStr = "X"+d;
		if (d==3) xStr = "Z";
		if (d==2) xStr = "Y";
		if (d==1) xStr = "X";
		String erg = "";
		for (int i=co.length-1; i>0; i--)
			if (!co[i].abs_isEqual(co[i].abs_zero())) 
			{
				if (erg.length()>0) erg += "+";
				erg += "("+co[i]+")*"+xStr+"^"+i+" ";
			}
		if (!co[0].abs_isEqual(co[0].abs_zero()))
		{	
			if (erg.length()>1) erg += "+";
			erg += co[0];
		}
		return erg;
	}
	public Polynomial unit() {return new Polynomial(new Ring[]{getRing().abs_unit()});}
	public Polynomial zero() {return new Polynomial(new Ring[]{getRing().abs_zero()});}

/**
 * Insert the method's description here.
 * Creation date: (26.05.2002 10:13:07)
 * @return arithmetik.Ring
 */
public Ring getContent() 
{
	if (co.length == 0) return new BigIntWrapper(1);
	Ring erg = co[0];
	for (int i=0; i<co.length; i++)
		erg = (Ring)((GcdAble)erg).abs_gcd((GcdAble)co[i]);
	return erg;
}

/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 15:25:43)
 * @return arithmetik.Ring
 */
public Ring getLeadingCoefficient() 
{
	return co[co.length-1];
}

/**
 * Gibt die Tiefe der Polynomverschachtelung, also die Anzahl der Variablen im Multivariaten Polynom, wieder.
 * Creation date: (26.05.2002 10:10:20)
 * @return int
 */
public int getMultivariateDepth() 
{
	Ring r = getRing();
	if (r instanceof Polynomial) return ((Polynomial)r).getMultivariateDepth()+1;
	else return 1;
}

/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 15:27:34)
 * @return arithmetik.Ring
 */
public Ring getMultivariateLeadingCoefficient() 
{
	Ring r = getLeadingCoefficient();
	while (r instanceof Polynomial) r = ((Polynomial)r).getLeadingCoefficient();
	return r;
}

/**
 * Insert the method's description here.
 * Creation date: (26.05.2002 10:19:21)
 * @return arithmetik.Polynomial
 */
public Polynomial getPrimepart() 
{
	Polynomial erg = new Polynomial(this);
	Ring r = getContent();
	for (int i=0; i<erg.co.length; i++)
		erg.co[i] = (Ring)((GcdAble)erg.co[i]).abs_divide((GcdAble)r);
	return erg;
	
}

	public static Polynomial getXStatic() 
	{
		Polynomial erg = getXStatic(new BigIntWrapper(0));
		erg.ringSpecified = false;
		return erg;
	}

	public static Polynomial getXStatic(Ring r) 
	{
		Polynomial erg = new Polynomial(r, 1);
		erg.co[0] = r.abs_zero();
		erg.co[1] = r.abs_unit();
		return erg;
	}

/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 15:24:03)
 * @return boolean
 */
public boolean isConstant() 
{
	return (co.length <= 1);
}

/**
 * Insert the method's description here.
 * Creation date: (25.05.2002 15:29:36)
 * @param r arithmetik.Ring
 */
public Polynomial multiplyMultivariateCoefficients(Ring r) 
{
	Polynomial p = new Polynomial(this);
	for (int i=0; i<p.co.length; i++)
	{
		if (p.co[i] instanceof Polynomial) p.co[i] = ((Polynomial)p.co[i]).multiplyMultivariateCoefficients(r);
		else p.co[i] = p.co[i].abs_multiply(r);
	}
	return p;
}

/**
 * Normalisiert den ersten nicht-Polynom-Level, wenn er ein Körper ist. 
 * Creation date: (25.05.2002 15:33:38)
 * @return arithmetik.Polynomial
 */
public Polynomial normalizeMultivariate() 
{
	Ring r = getMultivariateLeadingCoefficient();
	if ((!r.abs_isEqual(r.abs_zero())) && (r instanceof Field)) return multiplyMultivariateCoefficients(((Field)r).abs_reciprocal());
	else return this;
}
}
