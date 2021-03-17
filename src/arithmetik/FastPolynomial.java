package arithmetik;

import java.util.*;

/*
*/

public class FastPolynomial 
{
	static int NOT_FOUND = -1;
	
	private static Vector datenbank = new Vector();
	private static boolean datenbankAktiv = true;
	
	public Qelement factor;
	public Vector<QPolynomial> member;				// mit QPolynomials; die Identität der Polynome wird
										// getestet (mit ==), die Pointer also als Verweise 
										// auf die Datenbank.
	
	public FastPolynomial()
	{
		factor = new Qelement();
		member = new Vector<QPolynomial>();
	}
	public FastPolynomial(int identifierNr)
	{
		this.factor = new Qelement(Qelement.ONE);
		member = new Vector<QPolynomial>();
		QPolynomial newPol = databaseLookup(new QPolynomial(identifierNr));
		member.addElement(newPol);
	}
	public FastPolynomial(FastPolynomial copy)
	{
		factor = new Qelement(copy.factor);
		member = (Vector<QPolynomial>)copy.member.clone();
	}
	public FastPolynomial(Qelement factor)
	{
		this.factor = factor;
		member = new Vector<QPolynomial>();
	}
	public FastPolynomial(QPolynomial polynomial)
	{
		member = new Vector<QPolynomial>();
		// Konstant ?
		if (polynomial.isConstant()) 
		{
			factor = new Qelement(polynomial.leadingFactor());
		} else 
		{
			factor = new Qelement(polynomial.leadingFactor());
			QPolynomial newPol = databaseLookup(polynomial.normalize());
			member.addElement(newPol);
		}
	}
	public FastPolynomial (String polynomial) {
	    this(new QPolynomial(polynomial));
	}
	public FastPolynomial add(FastPolynomial arg2)
	{
		FastPolynomial[] arg = new FastPolynomial[2]; arg[0]=this; arg[1]=arg2;
		FastPolynomial[] faks = lazyGcdRemaindersOfList(arg);
		FastPolynomial erg = new FastPolynomial();

		QPolynomial newQPolynomial = (faks[0].expand()).add(faks[1].expand());
		if (newQPolynomial.isZero()) return new FastPolynomial();
		
		erg = new FastPolynomial();
		erg.factor = new Qelement(newQPolynomial.leadingFactor());
		if (!newQPolynomial.isConstant())
		{
			newQPolynomial = this.databaseLookup(newQPolynomial.normalize());
			erg.member.addElement(newQPolynomial);
		}

		erg = erg.multiply(lazyGcd(arg2));
		return erg;
	}
	public static String databaseElementsToString()
	{
		String erg = "Database contains: \r\n";
		for (int i=0; i<datenbank.size(); i++)
		{
			erg += "["+i+"]: "+(QPolynomial)datenbank.elementAt(i)+"\r\n";
		}
		return erg;
	}
	// schaut nach, unter welcher Nummer das übergebene Polynom gespeichert ist.
	// Falls es noch nicht aufgenommen ist, wird NOT_FOUND (=-1) zurückgegeben.
	public static QPolynomial databaseLookup(QPolynomial candidate)
	{
		if (!datenbankAktiv) return candidate;
		for (int i=0; i<datenbank.size(); i++)
		{
			QPolynomial data = (QPolynomial)datenbank.elementAt(i);
			if (candidate.equals(data)) return data;
		}
		datenbank.addElement(candidate);
		return candidate;
	}
	public static void databaseDisable() {datenbankAktiv = false;}
	public static void databaseEnable() {datenbankAktiv = true;}
	public static boolean isDatabaseEnabled() {return datenbankAktiv;}
	public double debugEvaluation()
	{
		return expand().debugEvaluation();
	}
	// leitet das Polynom nach X_*identifierNr* ab. Die anderen Bezeichner werden als
	// Konstanten gehandelt. 
	public FastPolynomial derive(int identifierNr)
	{
		FastPolynomial erg = new FastPolynomial();

		QPolynomial newQPolynomial = expand().derive(identifierNr);
		
		erg = new FastPolynomial();
		erg.factor = new Qelement(newQPolynomial.leadingFactor());
		if (!newQPolynomial.isConstant())
		{
			newQPolynomial = this.databaseLookup(newQPolynomial.normalize());
			erg.member.addElement(newQPolynomial);
		}
		return erg;
	}
	public boolean equals(FastPolynomial arg2)
	{
		FastPolynomial[] arg = new FastPolynomial[2]; arg[0]=this; arg[1]=arg2;
		FastPolynomial[] faks = lazyGcdRemaindersOfList(arg);
		return (faks[0].expand()).equals(faks[1].expand());
	}
	// Nimmt ein Array von doubles, dass alle vorkommenden
	// Variablen mit einem Wert belegt, und liefert das ausgewertete Polynom zurück.
	public double evaluate(double[] value)
	{
		return expand().evaluate(value);
	}
	// Wertet das Polynom an einer durch ein RQoutientExp gegebenen Stelle aus.
	public RQuotientExp evaluate(int identifierNr, RQuotientExp value)
	{
		return expand().evaluate(identifierNr, value);
	}
	public QPolynomial expand ()
	{
		QPolynomial erg = new QPolynomial(factor);
		for (int i=0; i<member.size(); i++)
			erg = erg.multiply((QPolynomial)member.elementAt(i));
		return erg;
	}
	// liefert den höchsten vorkommenden Index (i.A. die Anzahl der Variablen).
	public int getHighestIndex()
	{
		int erg = 0;
		for (int i=0; i<member.size(); i++)
			erg = Math.max(erg, ((QPolynomial)member.elementAt(i)).getHighestIndex());
		return erg;
	}
	public String infos()
	{
		QPolynomial ex = expand();
		String erg = "("+member.size()+","+ex.debugEvaluation()+")";
		return erg;
	}
	public boolean isConstant() 
	{
		return (member.size()==0);
	}
	public boolean isIntegerFactors()
	{
		return this.expand().isIntegerFactors();
	}
	public boolean isZero()
	{
		return (factor.isZero());
	}
	// VORSICHT: Muss Teiler sein!
	// Falls kein Teiler, werden alle Mitglieder abgezogen, die auch im
	// 2. Vorhanden sind. Auf diese Weise ergibt a div (a div b) den
	// ggT und (a div b) den factor, mit dem multipliziert werden muss,
	// um auf den ggT zu kommen.
	public FastPolynomial lazyDivide(FastPolynomial arg2)
	{
		FastPolynomial work = new FastPolynomial(arg2);
		FastPolynomial erg = new FastPolynomial(this);
		erg.factor = erg.factor.divide(arg2.factor);

		for (int i=0; i<erg.member.size(); i++)
		{
			int j=0;
			while ((j<work.member.size()) && (work.member.elementAt(j)!=erg.member.elementAt(i)))
				j++;
			if (j<work.member.size())
			{
				erg.member.setElementAt(erg.member.lastElement(),i);
				erg.member.removeElementAt(erg.member.size()-1);
				work.member.setElementAt(null, j);
				i--;
			}
		}
		return erg;
	}
	// Berechnet die gemeinsamen Mitglieder von arg1 und arg2; factor wird der ggT der
	// beiden factoren.
	public FastPolynomial lazyGcd(FastPolynomial arg2)
	{
		boolean thiszero = this.isZero();
		boolean arg2zero = arg2.isZero();
		
		if ((thiszero) && (arg2zero)) return new FastPolynomial(Qelement.ONE);
		if (this.isZero()) return arg2;
		if (arg2.isZero()) return this;
		
		FastPolynomial work = new FastPolynomial(arg2);
		FastPolynomial erg = new FastPolynomial();
		erg.factor = Qelement.gcd(this.factor,arg2.factor);
		
		for (int i=0; i<this.member.size(); i++)
		{
			int j=0;
			while ((j<work.member.size()) && (work.member.elementAt(j)!=this.member.elementAt(i)))
				j++;
			if (j<work.member.size())
			{
				work.member.setElementAt(null, j);
				erg.member.addElement(this.member.elementAt(i));
			}
		}
		return erg;
	}
	// Berechnet die gemeinsamen Mitglieder aller in der Liste übergebenen FastPolynomial.
	// Der factor ist der ggT aller Qelemente.
	// Listenlänge muss mindestens 2 sein.
	public static FastPolynomial lazyGcdOfList(FastPolynomial[] in)
	{
		if (in.length == 0) return new FastPolynomial(Qelement.ONE);
		if (in.length == 1) return new FastPolynomial(in[0]);
		FastPolynomial erg = in[0].lazyGcd(in[1]); 
		for (int i=2; i<in.length; i++)
			erg = erg.lazyGcd(in[i]);

		return erg;
	}
	// Gibt eine Liste zurück, mit welchem FastPolynomial man jeweils den "ggT"
	// multiplizieren muss, um auf das Argument zu kommen, wobei die
	// factoren der Elemente nach der ggT-Definition aus Qelement gebildet werden.
	public static FastPolynomial[] lazyGcdRemaindersOfList(FastPolynomial[] in)
	{
		FastPolynomial[] erg = new FastPolynomial[in.length];
		FastPolynomial t = lazyGcdOfList(in);
		for (int i=0; i<erg.length; i++)
			erg[i] = in[i].lazyDivide(t);
		return erg;
	}
	// Gibt eine Liste zurück, mit welchem FastPolynomial man die jeweiligen
	// Argumente multiplizieren muss, um auf das "kgV" zu kommen.
	// Der factor des kgV ist das Produkt der factoren aller Komponenten, und somit 
	// haben die Ergebnisse als factoren das Produkt aller factoren der anderen Komponenten.
	public static FastPolynomial[] lazyScmFactorOfList(FastPolynomial[] in)
	{
		FastPolynomial[] erg = new FastPolynomial[in.length];
		FastPolynomial[] t = lazyGcdRemaindersOfList(in);
		for (int i=0; i<erg.length; i++)
		{
			erg[i] = new FastPolynomial(Qelement.ONE);
			for (int j=0; j<t.length; j++)
				if (i!=j) erg[i] = erg[i].multiply(t[j]);
		}
		return erg;
	}
	public FastPolynomial multiply(FastPolynomial arg2)
	{
		if ((this.isZero()) || (arg2.isZero())) return new FastPolynomial();
		FastPolynomial erg = new FastPolynomial(Qelement.ONE);
		erg.factor = factor.multiply(arg2.factor);
		
		for (int i=0; i<member.size(); i++)
			erg.member.addElement(member.elementAt(i));
		for (int i=0; i<arg2.member.size(); i++)
			erg.member.addElement(arg2.member.elementAt(i));
		return erg;
	}
	public FastPolynomial negate()
	{
		FastPolynomial erg = new FastPolynomial(this);
		erg.factor = erg.factor.negate();
		return erg;
	}
	public FastPolynomial pow(long n)
	{
		if (n==0) return new FastPolynomial(Qelement.ONE);
		if (n==1) return new FastPolynomial(this);
		long h = n/2;
		return pow(h).multiply(pow(n-h));
	}
	public static void reinitDatabase()
	{
		datenbank = new Vector();
	}
	public RQuotientExp solveTo(int identifierNr)
	{
		for (int i=0; i<member.size(); i++)
		{
			RQuotientExp erg = ((QPolynomial)member.elementAt(i)).solveTo(identifierNr);
			if (erg != null) return erg;
		}
		return null;
	}
	public FastPolynomial sqr()
	{
		return multiply(this);
	}
	public FastPolynomial subtract(FastPolynomial arg2)
	{
		return add(arg2.negate());
	}
	public String toString()
	{
		if (member.size()==0) return factor.toString();
		String erg = factor+" * ";
		for (int i=0; i<member.size(); i++)
		{
			erg += "("+member.elementAt(i)+")";
			if (i < member.size()-1) erg += " * ";
		}
		return erg;
//		return expand().toString();
	}

/**
 * Insert the method's description here.
 * Creation date: (17.06.2002 10:27:07)
 * @param in java.util.Vector
 */
public FastPolynomial(Vector in) 
{
	factor = new Qelement(1);
	member = new Vector();
	for (int i=0; i<in.size(); i++)
	{
		QPolynomial p = (QPolynomial)in.elementAt(i);
		factor = factor.multiply(p.leadingFactor());
		if (!p.isConstant()) member.addElement(p.normalize());
	}
}

/**
 * Insert the method's description here.
 * Creation date: (15.06.2002 21:38:04)
 * @return arithmetik.FastPolynomial
 * @param snd arithmetik.FastPolynomial
 */
public FastPolynomial divide(FastPolynomial snd) 
{
	return divideAndRemainder(snd)[0];
}

/**
 * Insert the method's description here.
 * Creation date: (15.06.2002 21:21:05)
 * @return arithmetik.FastPolynomial[]
 * @param snd arithmetik.FastPolynomial
 */
public FastPolynomial[] divideAndRemainder(FastPolynomial snd) 
{
	FastPolynomial[] erg = new FastPolynomial[2];

	FastPolynomial div = new FastPolynomial(factor.divide(snd.factor));
	QPolynomial[] work2 = new QPolynomial[snd.member.size()];
	for (int i=0; i<work2.length; i++) work2[i] = new QPolynomial((QPolynomial)snd.member.elementAt(i));
	for (int i=0; i<member.size(); i++)
	{
		QPolynomial work = new QPolynomial((QPolynomial)member.elementAt(i));
		for (int j=0; j<work2.length; j++)
		{
			QPolynomial zu = work.gcd(work2[j]);
			work = work.divide(zu);
			work2[j] = work2[j].divide(zu);
		}
		div = div.multiply(new FastPolynomial(work));
	}
	QPolynomial nen = new QPolynomial(Qelement.ONE);
	for (int i=0; i<work2.length; i++)
		nen = nen.multiply(work2[i]);
	if (nen.subtract(new QPolynomial(Qelement.ONE)).isZero()) return new FastPolynomial[]{div, new FastPolynomial()};
	QPolynomial[] dar = div.expand().divideAndRemainder(nen);
	return new FastPolynomial[]{new FastPolynomial(dar[0]), new FastPolynomial(dar[1])};
}

/**
 * Setzt für die angegegebenen Variablennummern die angegebenen Werte val ein.
 * Creation date: (19.06.2002 11:12:25)
 * @return arithmetik.FastPolynomial
 * @param varnr int[]
 * @param val arithmetik.RQuotientExp[]
 */
public RQuotientExp evaluate(int[] varnr, RQuotientExp[] val) 
{
	RQuotientExp erg = new RQuotientExp(new RExpression(this));
	for (int i=0; i<varnr.length; i++)
		erg = erg.evaluate(varnr[i],val[i]);
	return erg;
}

/**
 * Sucht doppelte Faktoren in dem Polynom, eliminiert diese und schreibt sie (einfach) in das 2. FastPolynomial.
 * in jedem Fall ist this = erg[0]*erg[1]^2.
 * Creation date: (18.06.2002 09:03:06)
 * @return arithmetik.FastPolynomial[]


	METHODE ZEITWEISE KORRUMPIERT, UM GGT-BERECHNUNG ZU VERMEIDEN


 
 */
public FastPolynomial[] findDoubleFactors() 
{
	FastPolynomial[] erg = new FastPolynomial[]{new FastPolynomial(this), new FastPolynomial(Qelement.ONE)};
	for (int i=0; i<erg[0].member.size(); i++)
	{
		int j = i+1;
		while ((j<erg[0].member.size()) && (erg[0].member.elementAt(i)!=erg[0].member.elementAt(j))) j++;
		if (j<erg[0].member.size())
		{
			erg[1].member.addElement(erg[0].member.elementAt(i));
			erg[0].member.removeElementAt(j);
			erg[0].member.removeElementAt(i);
			i--;
		}
	}
	return erg;
	/*
	
	FastPolynomial[] erg = new FastPolynomial[]{new FastPolynomial(this), new FastPolynomial(Qelement.ONE)};
	for (int i=0; i<erg[0].member.size(); i++)
	{
		int j = i+1;
		while ((j<erg[0].member.size()) && (erg[0].member.elementAt(i)!=erg[0].member.elementAt(j))) j++;
		if (j<erg[0].member.size())
		{
			erg[1].member.addElement(erg[0].member.elementAt(i));
			erg[0].member.removeElementAt(j);
			erg[0].member.removeElementAt(i);
			i--;
		}
	}
	for (int i=0; i<erg[0].member.size(); i++)
	{
		for (int j=i+1; j<erg[0].member.size(); j++)
		{
			QPolynomial gcd = ((QPolynomial)erg[0].member.elementAt(i)).gcd((QPolynomial)erg[0].member.elementAt(j));
			if (!gcd.isUnit())
			{
				erg[1].multiply(new FastPolynomial(gcd));
				QPolynomial p = ((QPolynomial)erg[0].member.elementAt(i)).divide(gcd);
				QPolynomial q = ((QPolynomial)erg[0].member.elementAt(j)).divide(gcd);
				if (q.isUnit()) erg[0].member.removeElementAt(j); else erg[0].member.setElementAt(q,j);
				if (p.isUnit())
				{
					erg[0].member.removeElementAt(i);
					i--;
					j = erg[0].member.size();
				} else erg[0].member.setElementAt(p,i);
			}
		}
	}
	Vector v = erg[0].member;
	erg[0] = new FastPolynomial(erg[0].factor);
	for (int i=0; i<v.size(); i++)
	{
		FastPolynomial[] z = ((QPolynomial)v.elementAt(i)).findDoubleFactors();
		erg[0] = erg[0].multiply(z[0]);
		erg[1] = erg[1].multiply(z[1]);
	}
	return erg;
	*/
}

/**
 * Insert the method's description here.
 * Creation date: (15.06.2002 21:14:19)
 * @return arithmetik.FastPolynomial
 * @param snd arithmetik.FastPolynomial
 */
public FastPolynomial gcd(FastPolynomial snd) 
{
	FastPolynomial erg = new FastPolynomial(Qelement.ONE);
	QPolynomial[] work2 = new QPolynomial[snd.member.size()];
	for (int i=0; i<work2.length; i++) work2[i] = new QPolynomial((QPolynomial)snd.member.elementAt(i));
	for (int i=0; i<member.size(); i++)
	{
		QPolynomial work = new QPolynomial((QPolynomial)member.elementAt(i));
		for (int j=0; j<work2.length; j++)
		{
			QPolynomial zu;
			if (work == work2[j]) zu = work; else zu = work.gcd(work2[j]);
			work = work.divide(zu);
			work2[j] = work2[j].divide(zu);
			erg = erg.multiply(new FastPolynomial(zu));
		}
	}
		
	return erg;
}

/**
 * Insert the method's description here.
 * Creation date: (16.06.2002 11:59:33)
 * @return int
 * @param index int
 */
public int getDegreeIn(int index) 
{
	if (isZero()) return -1;
	int erg = 0;
	for (int i=0; i<member.size(); i++)
		erg += ((QPolynomial)member.elementAt(i)).getDegreeIn(index);
	return erg;		
}

/**
 * Insert the method's description here.
 * Creation date: (18.06.2002 09:42:00)
 * @return boolean
 */
public boolean isUnit() 
{
	return ((member.size()==0) && (factor.isUnit()));
}

/**
 * Insert the method's description here.
 * Creation date: (15.06.2002 21:38:50)
 * @return arithmetik.FastPolynomial
 * @param snd arithmetik.FastPolynomial
 */
public FastPolynomial remainder(FastPolynomial snd) 
{
	return divideAndRemainder(snd)[1];
}

/**
 * Insert the method's description here.
 * Creation date: (16.06.2002 11:53:08)
 * @return arithmetik.FastPolynomial
 * @param snd arithmetik.FastPolynomial
 * @param index int
 */
public FastPolynomial resultant(FastPolynomial snd, int index) 
{
	FastPolynomial erg = new FastPolynomial(factor.pow(snd.getDegreeIn(index)).multiply(snd.factor.pow(getDegreeIn(index))));
	for (int i=0; i<member.size(); i++)
		for (int j=0; j<member.size(); j++)
			erg = erg.multiply (((QPolynomial)member.elementAt(i)).resultant((QPolynomial)snd.member.elementAt(j),index));
	return erg;
}
}
