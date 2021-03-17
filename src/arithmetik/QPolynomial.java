package arithmetik;

import java.applet.*;
import java.awt.*;
import java.util.*;
import java.math.*;

import engine.Statik;

/*
	Klasse für Polynome über Qelement.
	
	Die Polynome sind 
	nach den Potenzen lexikographisch geordnet (höchster zuerst).
*/

public class QPolynomial implements Ring
{
	private final static int KONVERGENZHAEUFIGKEIT = 1;

	private Vector monom;		// Der größte (führende) nach hinten, der kleinste (konstante) vorne

	private static Hashtable gaertnerPolynomialTable = new Hashtable();

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
			if (i==eins.length)	for (int j=i; j<zwei.length; j++) if (zwei[j]>0) return -1;
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
//		public boolean equals(int[] o1) {return (compare(this,o1)==0);}
	};
			


	public final static QPolynomial ZERO = new QPolynomial();					// Speicherverringerung
	public final static QPolynomial ONE = new QPolynomial(new Qelement(1));
	public final static QPolynomial TWO = new QPolynomial(new Qelement(2));
	/*	
	// liefert in erg[0] den ggV (Produkt aller QPolynomiale) zurück, und in den anderen Argumenten
	// eine Liste, mit was der jeweilige QPolynomial malgenommen werden muss, um auf den 
	// kgV zu kommen. Entsprechend wird erwartet, dass die inidzierung der Argument-
	// liste bei 1 anfängt !!!
	public static QPolynomial[] kgV(QPolynomial[] arg)
	{
		int anz = arg.length;
		int[] nr = new int[anz];
		QPolynomial[] wert = new QPolynomial[anz];
		QPolynomial[] erg = new QPolynomial[anz];
		int bisher=0;
		int k,i,j;

		for (i=1; i<anz; i++)
		{
			
			k = 0;
			for (j=1; j<=bisher; j++)
				if (arg[i].is_equal(wert[j])) k = j;
			if (k == 0)
			{ wert[++bisher] = new QPolynomial(arg[i]); nr[i]=bisher; }
			else nr[i]=k;
		}

		for (i=1; i<anz; i++)
		{
			erg[i] = new QPolynomial((Qelement)1);
			for (j=1; j<=bisher; j++)
				if (j!=nr[i]) erg[i] = erg[i].mal(wert[j]);
		}

		erg[0] = new QPolynomial((Qelement)1);
		for (j=1; j<=bisher; j++)
			erg[0] = erg[0].mal(wert[j]);

		return erg;
	}
*/
								// (z.B. konstante) nach vorne (i.e. pos 0)
	
	public QPolynomial()
	{
		monom = new Vector();
	}
/**
 * Insert the method's description here.
 * Creation date: (29.05.2002 08:54:12)
 * @param in long[][]
 */
public QPolynomial(long[][] in) 
{
	this(fromArray(in));
}
	public QPolynomial (int identifierNr)
	{
		monom = new Vector();
		monom.addElement(new QMonomial(identifierNr));
	}
	public QPolynomial(Qelement qelement)
	{
		monom = new Vector();
		if (!qelement.isZero()) monom.addElement(new QMonomial(qelement));
	}
	public QPolynomial (QMonomial m)
	{
		monom = new Vector();
		if (!m.factor.isZero()) monom.addElement(m);
	}
	public QPolynomial (QPolynomial copy)
	{
		monom = new Vector();
		for (int i=0; i<copy.monom.size(); i++)
			monom.addElement(new QMonomial((QMonomial)copy.monom.elementAt(i)));
	}
/**
 * Insert the method's description here.
 * Creation date: (06.01.2003 11:08:12)
 * @param in java.lang.String
 */
public QPolynomial(String in) 
{
	QPolynomial erg = fromString(in);
	monom = erg.monom;
}
/**
 * abs_add method comment.
 */
public Ring abs_add(Ring b) 
{
	return add((QPolynomial)b);
}
/**
 * abs_isEqual method comment.
 */
public boolean abs_isEqual(Ring b) 
{
	return equals((QPolynomial)b);
}
/**
 * abs_multiply method comment.
 */
public Ring abs_multiply(Ring b) 
{
	return multiply((QPolynomial)b);
}
/**
 * abs_negate method comment.
 */
public Ring abs_negate()
{
	return negate();
}
/**
 * abs_pow method comment.
 */
public Ring abs_pow(long exp) 
{
	return pow(exp);
}
/**
 * abs_subtract method comment.
 */
public Ring abs_subtract(Ring b) 
{
	return subtract((QPolynomial)b);
}
/**
 * abs_unit method comment.
 */
public Ring abs_unit() 
{
	return new QPolynomial(Qelement.ONE);
}
/**
 * abs_zero method comment.
 */
public Ring abs_zero() 
{
	return new QPolynomial();
}
/**
 * Insert the method's description here.
 * Creation date: (05.01.2003 17:56:45)
 * @return arithmetik.QPolynomial
 * @param mon arithmetik.QMonomial
 */
public QPolynomial add(QMonomial mon) 
{
	return add(new QPolynomial(mon));
}
	public QPolynomial add (QPolynomial arg2)
	{
		QPolynomial erg = new QPolynomial();
		int p1 = 0, p2 = 0;
		while ((p1 < monom.size()) && (p2 < arg2.monom.size()))
		{
			QMonomial m1 = (QMonomial)monom.elementAt(p1);
			QMonomial m2 = (QMonomial)arg2.monom.elementAt(p2);
			QMonomial neu = null;
			
			int comp = m1.lexorderCompareTo(m2);
			if (comp ==  0) {neu = new QMonomial(m1.factor.add(m2.factor), m1.exp); p1++; p2++; }
			if (comp == -1) {neu = new QMonomial(m1); p1++; }
			if (comp ==  1) {neu = new QMonomial(m2); p2++; }
			if (!neu.factor.isZero())
				erg.monom.addElement(neu);
		}
		while (p1 < monom.size()) erg.monom.addElement(new QMonomial((QMonomial)monom.elementAt(p1++)));
		while (p2 < arg2.monom.size()) erg.monom.addElement(new QMonomial((QMonomial)arg2.monom.elementAt(p2++)));
		
		return erg.unifiziereKonstanten();
	}
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 12:34:37)
 * @return arithmetik.Complex[]
 * @param system arithmetik.QPolynomial[]

	Approximiert die im Sinne der Konvergenz naheste Nullstelle des Systems.
 
 */
public static Complex[] approximateZeroOfSymmetricSystem(QPolynomial[] system, Complex[] startpunkt, double epsilon, int maxIterations) 
{
	int dim = 0;
	for (int i=0; i<system.length; i++) dim = Math.max(dim, system[i].getHighestIndex()+1);
	if ((dim!=system.length) || (dim!=startpunkt.length))
		throw new RuntimeException("ApproximateZerosOfSystem called with assymetric system ("+startpunkt.length+" start values given, "+dim+" variables given, "+system.length+" equations)");
	

	QPolynomial[][] jacobianPolynomials = new QPolynomial[dim][dim];
	for (int i=0; i<dim; i++)
		for (int j=0; j<dim; j++)
			jacobianPolynomials[i][j] = system[i].derive(j);

	Complex[][] jacobian = new Complex[dim][dim];

	Complex[] position = new Complex[dim];
	for (int i=0; i<dim; i++) 
	{
		position[i] = startpunkt[i];
		if (startpunkt[i].reelValue()==0.0) position[i] = (Complex)position[i].abs_add(position[i].abs_fromDouble(0.0,0.1));
	}
	
	Complex[] wert = new Complex[dim];
	double norm = 0.0;
	for (int i=0; i<dim; i++) {wert[i] = system[i].evaluate(startpunkt); norm += wert[i].abs_doubleNorm();}

	int anzIteration = 0;	
	while ((norm > epsilon) && (anzIteration < maxIterations))
	{
		for (int i=0; i<dim; i++)
			for (int j=0; j<dim; j++)
				jacobian[i][j] = jacobianPolynomials[i][j].evaluate(position);

		RingMatrix jacMat = new RingMatrix(jacobian);
		RingVector korrektur = jacMat.solveWithGauss(new RingVector(wert),1);

		for (int i=0; i<dim; i++) position[i] = (Complex)position[i].abs_subtract(korrektur.getValue(i+1));
		norm = 0.0;
		for (int i=0; i<dim; i++) {wert[i] = system[i].evaluate(position); norm += wert[i].abs_doubleNorm();}
		anzIteration++;
	}

	if (norm > epsilon) throw new RuntimeException("approximateZerosOfSystem failed to converge (last norm = "+norm+")");

	return position;
}
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 12:34:37)
 * @return arithmetik.Complex[]
 * @param system arithmetik.QPolynomial[]

	Approximiert die im Sinne der Konvergenz naheste Nullstelle des Systems.
 
 */
public static Complex[] approximateZeroOfSymmetricSystem(QPolynomial[] system, double epsilon, int maxIterations) 
{
	int dim = 0;
	for (int i=0; i<system.length; i++) dim = Math.max(dim, system[i].getHighestIndex()+1);
	if (dim!=system.length)
		throw new RuntimeException("ApproximateZerosOfSystem called with assymetric system ("+dim+" variables given, "+system.length+" equations)");

	// simples Startsystem mit 0 als Lösung.
	QPolynomial[] startsystem = new QPolynomial[dim];
	for (int i=0; i<dim; i++) startsystem[i] = new QPolynomial(i);
	
	double lambdapos = 0.0; int steppotenz = 0;

	Complex[] position = new Complex[dim];
	for (int i=0; i<dim; i++) position[i] = new DoubleComplex(0.0,0.0);

/*
	Complex plambda = new Complex((1-Math.cos(lambdapos+step*(1.0-lambdapos)))/2.0,Math.sin(lambdapos+step*(1.0-lambdapos)));
	Complex 1minusplambda = new Complex((1+Math.cos(lambdapos+step*(1.0-lambdapos)))/2.0,Math.sin(lambdapos+step*(1.0-lambdapos)));
*/
	Qelement plambda = new Qelement(lambdapos+Math.pow(0.5,steppotenz)*(1.0-lambdapos));
	Qelement einsMinusPlambda = new Qelement(1-(lambdapos+Math.pow(0.5,steppotenz)*(1.0-lambdapos)));
	QPolynomial[] aktuellesSystem = new QPolynomial[dim];
	for (int i=0; i<dim; i++) aktuellesSystem[i] = system[i].multiply(plambda).add(startsystem[i].multiply(einsMinusPlambda));

	int anzIteration = 0;	
	while ((lambdapos < 1.0) && (anzIteration < maxIterations))
	{
		try {
			Complex[] zwerg = approximateZeroOfSystem(aktuellesSystem, position, epsilon, maxIterations);
			position = zwerg;
			lambdapos += Math.pow(0.5,steppotenz)*(1.0-lambdapos);
			steppotenz--;
		} catch (Exception e) 
		{
			steppotenz++;
		}
		 
		plambda = new Qelement(lambdapos+Math.pow(0.5,steppotenz)*(1.0-lambdapos));
		einsMinusPlambda = new Qelement(1-(lambdapos+Math.pow(0.5,steppotenz)*(1.0-lambdapos)));
		for (int i=0; i<dim; i++) aktuellesSystem[i] = system[i].multiply(plambda).add(startsystem[i].multiply(einsMinusPlambda));
		anzIteration++;
	}

	if (lambdapos < 1.0) throw new RuntimeException("Pathtracking faild to reach target system (ended with lambda = "+lambdapos+")");

	return position;
}
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 12:34:37)
 * @return arithmetik.Complex[]
 * @param system arithmetik.QPolynomial[]

	Approximiert die im Sinne der Konvergenz naheste Nullstelle des Systems.
 
 */
public static Complex[] approximateZeroOfSystem(QPolynomial[] system, Complex[] startpunkt, double epsilon, int maxIterations) 
{
	int anzvars = 0;
	for (int i=0; i<system.length; i++) anzvars = Math.max(anzvars, system[i].getHighestIndex()+1);
	int dim = system.length;

	QPolynomial[] eingesetztesSystem = new QPolynomial[dim];
	for (int i=0; i<dim; i++)
	{
		eingesetztesSystem[i] = system[i];
		for (int j=dim; j<anzvars; j++) eingesetztesSystem[i] = eingesetztesSystem[i].evaluate(j, new Qelement(startpunkt[j].reelValue()));
	}

	Complex[] nStartpunkt = new Complex[dim];
	for (int i=0; i<dim; i++) nStartpunkt[i] = startpunkt[i];
	Complex[] zwerg = approximateZeroOfSymmetricSystem(eingesetztesSystem,nStartpunkt, epsilon, maxIterations);
	Complex[] erg = new Complex[startpunkt.length];
	for (int i=0; i<startpunkt.length; i++)
		if (i<dim) erg[i] = zwerg[i]; else erg[i] = startpunkt[i];

	return erg;
}
	
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 12:34:37)
 * @return arithmetik.Complex[]
 * @param system arithmetik.QPolynomial[]

	Approximiert die im Sinne der Konvergenz naheste Nullstelle des Systems.
 
 */
public static Complex[] approximateZeroOfSystem(QPolynomial[] system, double epsilon, int maxIterations) 
{
	return approximateZeroOfSystem(system, 0, epsilon, maxIterations);
}
/**
 * Insert the method's description here.
 * Creation date: (11.08.2004 12:34:37)
 * @return arithmetik.Complex[]
 * @param system arithmetik.QPolynomial[]

	Approximiert irgendeine Nullstelle des Systems.
	einsetzungsKonstante beschreibt die Art der Einsetzung für die überzähligen Variablen. 
 
 */
public static Complex[] approximateZeroOfSystem(QPolynomial[] system, int einsetzungsKonstante, double epsilon, int maxIterations) 
{
	int anzvars = 0;
	for (int i=0; i<system.length; i++) anzvars = Math.max(anzvars, system[i].getHighestIndex()+1);
	int dim = system.length;

	Complex[] erg = new Complex[anzvars];
	int restEinsetzung = einsetzungsKonstante;
	for (int i = dim; i<anzvars; i++) 
	{
		int w = restEinsetzung / 2;
		if (i==anzvars-1) w = restEinsetzung;
		restEinsetzung -= w;
		erg[i] = new DoubleComplex(w,0.0);
	}		

	QPolynomial[] eingesetztesSystem = new QPolynomial[dim];
	for (int i=0; i<dim; i++)
	{
		eingesetztesSystem[i] = system[i];
		for (int j=dim; j<anzvars; j++) eingesetztesSystem[i] = eingesetztesSystem[i].evaluate(j, new Qelement(erg[j].reelValue()));
	}

	Complex[] zwerg = approximateZeroOfSymmetricSystem(eingesetztesSystem,epsilon, maxIterations);
	for (int i=0; i<dim; i++)
		erg[i] = zwerg[i];

	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (12.07.2002 23:24:41)
 * @return arithmetik.QPolynomial[]
 * @param ideal arithmetik.QPolynomial[]
 */
public static QPolynomial[] computeGroebnerBasis(QPolynomial[] ideal) 
{
	// Da die Hashtables kein null als value kriegen dürfen.
	Integer dummy = new Integer(0);
	
	Hashtable b = new Hashtable();
	for (int i=0; i<ideal.length; i++)
		for (int j=i+1; j<ideal.length; j++)
			b.put(new Tupel(i,j),dummy);

	QPolynomial[] erg = new QPolynomial[ideal.length];
	for (int i=0; i<ideal.length; i++) 
		erg[i] = ideal[i].makeCoefficientIntegerAndMinimal();
	
	while (b.size() > 0)
	{
		int[] ix = ((Tupel)(b.keys().nextElement())).data;
		QMonomial fi = erg[ix[0]].getLeadingMonomial();
		QMonomial fj = erg[ix[1]].getLeadingMonomial();
		QMonomial kgv = fi.leastCommonMultiple(fj);
		
		boolean takeit = true;
		for (int k=0; k<erg.length; k++)
			if ((k!=ix[0]) && (k!=ix[1]) && 
				(!b.containsKey(new Tupel(ix[0],k))) && (!b.containsKey(new Tupel(ix[1],k))) &&
				(!b.containsKey(new Tupel(k,ix[0]))) && (!b.containsKey(new Tupel(k,ix[1]))) &&
				(erg[k].getLeadingMonomial().divides(kgv))) takeit = false;

		boolean relativePrime = true;
		int g = Math.max(fi.getHighestIndex(),fj.getHighestIndex());
		for (int k=0; k<=g; k++)
			if ((fi.getExponent(k)>0) && (fj.getExponent(k)>0)) {relativePrime = false; k=g+1;}

		if (relativePrime) takeit = false;

		if (takeit)
		{
			QPolynomial u = erg[ix[0]].monomialMultiply(kgv.divide(fi));
			QPolynomial v = erg[ix[1]].monomialMultiply(kgv.divide(fj));
			QPolynomial[] dar = u.subtract(v).multiDivideAndRemainder(erg);
			QPolynomial s = dar[dar.length-1];
			if (!s.isZero())
			{
				QPolynomial[] nerg = new QPolynomial[erg.length+1];
				for (int i=0; i<erg.length; i++) nerg[i] = erg[i];
				nerg[nerg.length-1] = s.makeCoefficientIntegerAndMinimal();
				for (int i=0; i<erg.length; i++) b.put(new Tupel(i,erg.length),dummy);
				erg = nerg;
			}
		}
		b.remove(new Tupel(ix));
	}
	return erg;		
}
/**
 * Insert the method's description here.
 * Creation date: (12.07.2002 23:24:41)
 * @return arithmetik.QPolynomial[]
 * @param ideal arithmetik.QPolynomial[]
 */
public static QPolynomial[] computeGroebnerBasis(QPolynomial[] ideal, Comparator ordnung) 
{
	// Da die Hashtables kein null als value kriegen dürfen.
	Integer dummy = new Integer(0);
	
	Hashtable b = new Hashtable();
	for (int i=0; i<ideal.length; i++)
		for (int j=i+1; j<ideal.length; j++)
			b.put(new Tupel(i,j),dummy);

	QPolynomial[] erg = new QPolynomial[ideal.length];
	for (int i=0; i<ideal.length; i++) 
		erg[i] = ideal[i].makeCoefficientIntegerAndMinimal();
	
	while (b.size() > 0)
	{
		int[] ix = ((Tupel)(b.keys().nextElement())).data;
		QMonomial fi = erg[ix[0]].getLeadingMonomial(ordnung);
		QMonomial fj = erg[ix[1]].getLeadingMonomial(ordnung);
		QMonomial kgv = fi.leastCommonMultiple(fj);
		
		boolean takeit = true;
		for (int k=0; k<erg.length; k++)
			if ((k!=ix[0]) && (k!=ix[1]) && 
				(!b.containsKey(new Tupel(ix[0],k))) && (!b.containsKey(new Tupel(ix[1],k))) &&
				(!b.containsKey(new Tupel(k,ix[0]))) && (!b.containsKey(new Tupel(k,ix[1]))) &&
				(erg[k].getLeadingMonomial(ordnung).divides(kgv))) takeit = false;

		boolean relativePrime = true;
		int g = Math.max(fi.getHighestIndex(),fj.getHighestIndex());
		for (int k=0; k<=g; k++)
			if ((fi.getExponent(k)>0) && (fj.getExponent(k)>0)) {relativePrime = false; k=g+1;}

		if (relativePrime) takeit = false;

		if (takeit)
		{
			QPolynomial u = erg[ix[0]].monomialMultiply(kgv.divide(fi));
			QPolynomial v = erg[ix[1]].monomialMultiply(kgv.divide(fj));
			QPolynomial[] dar = u.subtract(v).multiDivideAndRemainder(erg, ordnung);
			QPolynomial s = dar[dar.length-1];
			if (!s.isZero())
			{
				QPolynomial[] nerg = new QPolynomial[erg.length+1];
				for (int i=0; i<erg.length; i++) nerg[i] = erg[i];
				nerg[nerg.length-1] = s.makeCoefficientIntegerAndMinimal();
				for (int i=0; i<erg.length; i++) b.put(new Tupel(i,erg.length),dummy);
				erg = nerg;
			}
		}
		b.remove(new Tupel(ix));
	}
	return erg;		
}
	// Berechnet eine Subresultantenkette fuer zwei multivariate Polynome this und g als
	// univariates Polynom in der Hauptvariablen i und liefert das letzte Polynom der Kette.
	// Zusaetzlich wird gamma=ggt(LK(this,i),LK(g,i)) benoetigt.
	// Algor. 1 aus Paper
	public QPolynomial computeSimpleSPRS(QPolynomial gIn,int i)
	{
		QPolynomial f;
		QPolynomial g;
		if (this.getDegreeIn(i) >= gIn.getDegreeIn(i) )
		{
			//f = normalize();
			f = new QPolynomial(this);
			//g = gIn.normalize();
			g = new QPolynomial(gIn);
		} else
		{
			//f = gIn.normalize();
			f = new QPolynomial(gIn);
			// g = normalize();
			g = new QPolynomial(this);
		}
		QPolynomial l = new QPolynomial(Qelement.ONE);
		QPolynomial psi = new QPolynomial(Qelement.ONE);
		while (true)
		{
//			System.out.println("-- Subresultant calling pseudoRemainder of");
//			System.out.println(f + " and " + g);
//			System.out.println("in Variable " + i);
			QPolynomial[] restarray = (f.normalize()).pseudoRemainder(g.normalize(),i);
			QPolynomial r = restarray[1];
			QPolynomial e = restarray[0];
/*			System.out.println("-----------------------------Subresultant---------");
			System.out.println("f / g = ? + e * r");
			System.out.println(f + " / " + g);
			System.out.println(e + " * " + r);
*/			if (r.isZero())
			{
				return g;
			}
			int delta = f.getDegreeIn(i)-g.getDegreeIn(i);
			f = new QPolynomial(g);
			QPolynomial tmp1 = e.multiply(r);
			QPolynomial tmp2 = psi.pow(delta);
			tmp2 = l.multiply(tmp2);
			g = tmp1.divide(tmp2);
//			g = g.normalize();
			// g = (e.multiply(r)).divide(l.multiply(psi.pow(delta)));
			l = f.getLeadingCoefficient(i);
			if (delta != 0)
			{
				if (delta == 1)
				{
					psi = new QPolynomial(l);
				} else {
					psi = (l.pow(delta)).divide(psi.pow(delta-1));
				}
			}
 		}
	}
	/**
	 * Berechnet das letzte Polynom der Subresultanten-PRS von this und gin in i,
	 * wobei gammaIn = ggt(lk(f,i),lk(g,i) ist.
	 * Algor. 3 aus Paper
	 */

	public QPolynomial computeSubresultantPRS(QPolynomial gIn,int i, QPolynomial gammaIn)
	{
		QPolynomial f;
		QPolynomial g;
		if (getDegreeIn(i) >= gIn.getDegreeIn(i) )
		{
			f = new QPolynomial(this);
			g = new QPolynomial(gIn);
		} else
		{
			f = new QPolynomial(gIn);
			g = new QPolynomial(this);
		}
		QPolynomial gamma = new QPolynomial(gammaIn);
//		System.out.println("CSPRS called, var " +i);
//		System.out.println("f     = " +f);
//		System.out.println("g     = " +g);
//		System.out.println("gamma = " +gamma);
		// Berechne den f mod g in x_i
                  f=f.normalize();
	              g=g.normalize();
		QPolynomial[] restarray = f.pseudoRemainder(g,i);
		QPolynomial r = restarray[1];
		QPolynomial e = restarray[0];
		// Wenn der rest 0 ist, sind wir schon fertig....
		if (r.isZero()) return g; //f;
		QPolynomial l = g.getLeadingCoefficient(i);
		int delta = f.getDegreeIn(i)-g.getDegreeIn(i);
		QPolynomial G = gamma.gcd2(e);
		QPolynomial gammaI = e.divide(G);
		f = new QPolynomial(g);
		g = (G.multiply(r)).divide(gamma);
		QPolynomial Z;
		QPolynomial psi;
		if (delta > 0)
		{
			psi = (l.pow(delta)).divide(gamma);
//			System.out.println("delta > 0");
		} else
		{
			restarray = f.pseudoRemainder(g,i);
			r = restarray[1];
			e = restarray[0];
			if (r.isZero()) return g; //f;
			delta = f.getDegreeIn(i)-g.getDegreeIn(i);
			Z = (gammaI.pow(delta+1)).multiply(e).multiply(gamma.pow(delta));
			G = Z.gcd2(l);
			f = new QPolynomial(g);
			g = (G.multiply(r)).divide(l);
			l = f.getLeadingCoefficient(i);
			psi = (gammaI.multiply(l)).pow(delta).multiply(gamma.pow(delta-1));
			gammaI = Z.divide(G);
//			System.out.println("! delta > 0");
		}
		QPolynomial kpsi = new QPolynomial(Qelement.ONE);
		while (true)
		{
			restarray = f.pseudoRemainder(g,i);
			r = restarray[1];
			e = restarray[0];
			if (r.isZero()) return g;// f;
			delta = f.getDegreeIn(i)-g.getDegreeIn(i);
			Z = gammaI.pow(delta+1).multiply(e);
			QPolynomial N = l.multiply(psi.pow(delta));
			G = Z.gcd2(N);
			f = new QPolynomial(g);
//			System.out.println(" G = " +G);
//			System.out.println(" r = " +r);
//			System.out.println(" N = " +N);
			g = (G.multiply(r)).divide(N);
                        g = g.normalize();
			l = f.getLeadingCoefficient(i);
			if (delta == 1)
			{
				kpsi = gammaI.multiply(l);
			} else
			{
				kpsi = ((gammaI.multiply(l)).pow(delta)).divide(kpsi.pow(delta-1));
			}
			gammaI = Z.divide(G).normalize();
		}
	}
/**
 * Insert the method's description here.
 * Creation date: (07.05.2003 16:54:25)
 * @param ideal arithmetik.QPolynomial[]

	Bivariat in X0 und X1
 
 */
public static int countReelZeros(QPolynomial[] ideal) 
{
	QPolynomial[] gb = computeGroebnerBasis(ideal,grlexorder);

	System.out.println("Groebnerbasis fertig");

	QMonomial[] rest = QMonomial.getRemainderOfIdeal(getLeadingTermIdeal(gb,grlexorder));

	int ix = -1; for (int i=0; i<rest.length; i++) ix = Math.max(ix, rest[i].getHighestIndex());
	final int fix = ix;

	Arrays.sort(rest, new Comparator() {
		public int compare(Object o1, Object o2) 
		{
			QMonomial eins = (QMonomial)o1;
			QMonomial zwei = (QMonomial)o2;
			return grlexorder.compare(eins.exp,zwei.exp);
		}
	});

	Hashtable restHash = new Hashtable();
	for (int i=0; i<rest.length; i++) restHash.put(rest[i],new Integer(i));

	// erstellen der Matrizen für die Multiplikation mit  X_i
	RingMatrix[] initial = new RingMatrix[ix+1];
	for (int i=0; i<=ix; i++)
	{
		QMonomial x = new QMonomial(i);
		Qelement[][] mat = new Qelement[rest.length][rest.length];
		for (int j=0; j<rest.length; j++)
		{
			for (int l=0; l<rest.length; l++) mat[l][j] = Qelement.ZERO;
			QMonomial multerg = rest[j].multiply(x);
			Object k = restHash.get(multerg);
			if (k==null)
			{
				QPolynomial[] pol = (new QPolynomial(multerg)).multiDivideAndRemainder(gb,grlexorder);
				QPolynomial r = pol[pol.length-1];
				for (int l=0; l<r.monom.size(); l++)
				{
					QMonomial mon = (QMonomial)r.monom.elementAt(l);
					Qelement f = mon.factor;
					mon.factor = Qelement.ONE;
					k = restHash.get(mon);
					mat[((Integer)k).intValue()][j] = f;
				}
			} else {
				mat[((Integer)k).intValue()][j] = Qelement.ONE;
			}
		}	
		initial[i] = new RingMatrix(mat);	
	}

	Qelement[][] zielmatrix = new Qelement[rest.length][rest.length];

	int[] max = new int[ix+1];
	for (int i=0; i<rest.length; i++)
		for (int j=0; j<=ix; j++) max[j] = Math.max(max[j], 2*rest[i].getExponent(j));

	Hashtable nutzraum = new Hashtable();
	
	for (int i=0; i<rest.length; i++)
		for (int j=0; j<rest.length; j++)
		{
			int[] p = new int[ix+1];
			for (int k=0; k<=ix; k++) p[k] = rest[i].getExponent(k)+rest[j].getExponent(k);

			Tupel t = new Tupel(p);
			if (nutzraum.containsKey(t))
			{
				Vector v = (Vector)nutzraum.get(t);
				v.addElement(new int[]{i,j});
			} else 
			{
				Vector v = new Vector(); v.addElement(new int[]{i,j});
				nutzraum.put(t,v);
			}
		}
		
	Hashtable matrizen = new Hashtable();
	int[] pos = new int[ix+1]; for (int i=0; i<pos.length; i++) pos[i] = 0;
	
	RingMatrix mat = RingMatrix.unit(Qelement.ONE,rest.length);
	matrizen.put(new Tupel(pos), mat);
	for (int i=0; i<ix+1; i++)
	{
		pos[i] = 1;
		matrizen.put(new Tupel(pos), initial[i]);
		pos[i] = 0;
	}
	
	while (pos[ix] <= max[ix])
	{
		Tupel t = new Tupel(pos);
		boolean leerlauf = !nutzraum.containsKey(t);
		
		if (!leerlauf)
		{
			Vector v = (Vector)nutzraum.get(t);
			Qelement spur = (Qelement) mat.trace();
			for (int i=0; i<v.size(); i++)
			{
				int[] po = (int[])v.elementAt(i);
				zielmatrix[po[0]][po[1]] = spur;
			}

			matrizen.put(t,mat);
			System.out.println(t);
		}

		pos[0]++;
		int i=0;
		while ((i<pos.length) && (pos[i] > max[i])) {pos[i] = 0; i++; if (i<pos.length) pos[i]++; else pos[i-1]=max[i-1]+1;}
		if ((!leerlauf) && (i>0) && (i<pos.length))
		{
			pos[i]--;
			mat = (RingMatrix)matrizen.get(new Tupel(pos));
			pos[i]++;
		}
		if ((i<pos.length) && (!leerlauf)) mat = mat.matrixMultiply(initial[i]);
	}

	RingMatrix zielM = new RingMatrix(zielmatrix);

	int sig = zielM.getSignature();

	return sig;
}
	public double debugEvaluation()
	{
		double erg = 0.0;
		for (int i=0; i<monom.size(); i++)
			erg += ((QMonomial)monom.elementAt(i)).debugEvaluation();
		return erg;
	}
	// leitet das Polynom nach X_*identifierNr* ab. Die anderen Bezeichner werden als
	// Konstanten gehandelt. 
	public QPolynomial derive(int identifierNr)
	{
		QPolynomial erg = ZERO;
		for (int i=0; i<monom.size(); i++)
		{
			QMonomial m = new QMonomial((QMonomial)monom.elementAt(i));
			int ex = m.getExponent(identifierNr);
			if (ex>=1)
			{
				m.factor = m.factor.multiply(new Qelement(ex));
				m.setExponent(identifierNr, ex-1);
				erg = erg.add(new QPolynomial(m));
			}
		}
		return erg.unifiziereKonstanten();
	}
/**
 * Insert the method's description here.
 * Creation date: (07.02.2003 11:24:00)
 * @return arithmetik.QPolynomial
 * @param matrix arithmetik.QPolynomial[][]

	Eine angepasste Determinanten - Routine für eine Matrix aus QPolynomials
 
 */
public static QPolynomial determinant(QPolynomial[][] inMatrix) 
{
	int size = inMatrix.length;
	QPolynomial[][] matrix = new QPolynomial[size][size];
	for (int i=0; i<size; i++)
		for (int j=0; j<size; j++) matrix[i][j] = inMatrix[i][j];
	
	if (size==0) return ONE;
	if (size==1) return matrix[0][0];
	if (size==2) return matrix[0][0].multiply(matrix[1][1]).subtract(matrix[0][1].multiply(matrix[1][0]));

	int[] nnZeile = new int[size], nnSpalte = new int[size];		// Anzahl nicht-nullen in den Zeilen/Spalten;
																	// -1 heisst Zeile/Spalte ignorieren
	for (int i=0; i<size; i++) {nnZeile[i] = 0; nnSpalte[i] = 0;}																	
	
	for (int i=0; i<size; i++)
		for (int j=0; j<size; j++)
			if (!matrix[i][j].isZero()) {nnZeile[i]++; nnSpalte[j]++;}
			
	Vector nenner = new Vector();
	Vector zaehler = new Vector();
	for (int anzelim = 0; anzelim<size-3; anzelim++)
	{
		int min = Integer.MAX_VALUE, nr = -1;
		boolean minZeile = true;
		for (int i=0; i<size; i++) if ((nnZeile[i]!=-1) && (nnZeile[i]<min)) {min = nnZeile[i]; nr = i;}
		for (int i=0; i<size; i++) if ((nnSpalte[i]!=-1) && (nnSpalte[i]<min)) {min = nnSpalte[i]; nr = i; minZeile = false;}
		
		if (minZeile)
		{
			int zeile = nr;
			int spalte = -1;
			int mingrad = Integer.MAX_VALUE;
			for (int i=0; i<size; i++) 
			{
				if ((nnSpalte[i]!=-1) && (!matrix[zeile][i].isZero()))
				{
					int deg = matrix[zeile][i].getTotalDegree();
					if (deg < mingrad) {spalte = i; mingrad = deg;}
				}
			}
			QPolynomial pivot = matrix[zeile][spalte];
			boolean positive = true;
			for (int i=0; i<zeile; i++) if (nnZeile[i]!=-1) positive = !positive;
			for (int i=0; i<spalte; i++) if (nnSpalte[i]!=-1) positive = !positive;
			if (positive) zaehler.addElement(pivot); else zaehler.addElement(pivot.negate());
			for (int i=0; i<size; i++)
				if ((i!=spalte) && (!matrix[zeile][i].isZero()) && (nnSpalte[i]!=-1))
				{
					QPolynomial gcd = pivot.gcd(matrix[zeile][i]);
					QPolynomial rpiv = pivot.divide(gcd);
					QPolynomial rloc = matrix[zeile][i].divide(gcd);

					nenner.addElement(rpiv);
					for (int j=0; j<size; j++)
						if ((j!=zeile) && (nnZeile[j]!=-1))
						{
							if (matrix[j][i].isZero()) {nnSpalte[i]++; nnZeile[j]++;}
							matrix[j][i] = matrix[j][i].multiply(rpiv).subtract(matrix[j][spalte].multiply(rloc));
							if (matrix[j][i].isZero()) {nnSpalte[i]--; nnZeile[j]--;}
						}
				}

			for (int i=0; i<size; i++)
			{
				if ((nnZeile[i]!=-1) && (!matrix[i][spalte].isZero())) nnZeile[i]--;
				if ((nnSpalte[i]!=-1) && (!matrix[zeile][i].isZero())) nnSpalte[i]--;
			}
			nnZeile[zeile] = -1;
			nnSpalte[spalte] = -1;
		} else {
			int spalte = nr;
			int zeile = -1;
			int mingrad = Integer.MAX_VALUE;
			for (int i=0; i<size; i++) 
			{
				if ((nnZeile[i]>-1) && (!matrix[i][spalte].isZero()))
				{
					int deg = matrix[i][spalte].getTotalDegree();
					if (deg < mingrad) {zeile = i; mingrad = deg;}
				}
			}
			QPolynomial pivot = matrix[zeile][spalte];
			boolean positive = true;
			for (int i=0; i<zeile; i++) if (nnZeile[i]!=-1) positive = !positive;
			for (int i=0; i<spalte; i++) if (nnSpalte[i]!=-1) positive = !positive;
			if (positive) zaehler.addElement(pivot); else zaehler.addElement(pivot.negate());
			for (int i=0; i<size; i++)
				if ((i!=zeile) && (!matrix[i][spalte].isZero()) && (nnZeile[i]!=-1))
				{
					QPolynomial gcd = pivot.gcd(matrix[i][spalte]);
					QPolynomial rpiv = pivot.divide(gcd);
					QPolynomial rloc = matrix[i][spalte].divide(gcd);

					nenner.addElement(rpiv);
					for (int j=0; j<size; j++)
						if ((j!=spalte) && (nnSpalte[j]!=-1))
						{
							if (matrix[i][j].isZero()) {nnSpalte[j]++; nnZeile[i]++;}
							matrix[i][j] = matrix[i][j].multiply(rpiv).subtract(matrix[zeile][j].multiply(rloc));
							if (matrix[i][j].isZero()) {nnSpalte[j]--; nnZeile[i]--;}
						}
				}
			for (int i=0; i<size; i++)
			{
				if ((nnZeile[i]!=-1) && (!matrix[i][spalte].isZero())) nnZeile[i]--;
				if ((nnSpalte[i]!=-1) && (!matrix[zeile][i].isZero())) nnSpalte[i]--;
			}
			nnZeile[zeile] = -1;
			nnSpalte[spalte] = -1;
		}
	}

	// verbleibende 3x3-Matrix bauen...
	QPolynomial[][] dd = new QPolynomial[3][3];
	int zeile = 0;
	for (int i=0; i<size; i++)
		if (nnZeile[i]!=-1)
		{
			int spalte = 0;
			for (int j=0; j<size; j++)
				if (nnSpalte[j]!=-1) dd[zeile][spalte++] = matrix[i][j];
			zeile++;
		}
	
	// Determinante von 3x3 durch Standartformel
	QPolynomial det = dd[0][0].multiply(dd[1][1]).multiply(dd[2][2]);
	det = det.add(    dd[0][1].multiply(dd[1][2]).multiply(dd[2][0]));
	det = det.add(    dd[0][2].multiply(dd[1][0]).multiply(dd[2][1]));
	det = det.subtract(dd[0][2].multiply(dd[1][1]).multiply(dd[2][0]));
	det = det.subtract(dd[0][1].multiply(dd[1][0]).multiply(dd[2][2]));
	det = det.subtract(dd[0][0].multiply(dd[1][2]).multiply(dd[2][1]));
	
	for (int i=0; i<zaehler.size(); i++)
		det = det.multiply( (QPolynomial)zaehler.elementAt(i) );
	for (int i=0; i<nenner.size(); i++)
		det = det.divide( (QPolynomial)nenner.elementAt(i) );
		
	return det;
}
/**
 * Teilt das Polynom durch q

	TvO: Die Methode divide in QMonomial existierte nicht.
	Ich habe sie so eingeführt, dass sie ein new Monomial
	zurückliefert, dass dann entsprechend in den Vector
	eingesetzt wird.
 
 */
    // (c) Klex
    public QPolynomial divide(Qelement q)
    {
    	QPolynomial erg = new QPolynomial();
    	for (int i = 0; i < monom.size() ; i++)
    		erg.monom.addElement(((QMonomial)monom.elementAt(i)).divide(q));
    
    	return erg;
    }
	public QPolynomial divide(QPolynomial arg2)
	{
		return divideAndRemainder(arg2)[0];
	}
	// liefert Divident und Rest zurück, wobei der Rest definiert ist als erstes auftreten,
	// dass das zweite Argument eine höhere Potenz in irgendeiner Unbekannten hat.
	// Es gilt in jedem Fall *res[0]* * *arg2* + *res[1]* = *this*
	public QPolynomial[] divideAndRemainder(QPolynomial arg2)
	{
		return multiDivideAndRemainder(new QPolynomial[]{arg2});
	}
/**
 * Löst das Polynom nach var auf, solange der Grad in var <= 2 ist.
 * Creation date: (16.06.2002 11:20:52)
 * @return arithmetik.RQuotientExp
 * @param grad int
 */
public RQuotientExp easySolveTo(int var) {
	return null;
}
public boolean equals(QPolynomial arg2)
{
	if (monom.size()!=arg2.monom.size()) return false;
	for (int i=0; i<monom.size(); i++)
		if ( !((QMonomial)monom.elementAt(i)).equals((QMonomial)arg2.monom.elementAt(i))) return false;
	return true;
}
	// Evaluate Nr. 6 nimmt ein Array von doubles, dass alle vorkommenden
	// Variablen mit einem Wert belegt, und liefert das ausgewertete Polynom zurück.
	public double evaluate(double[] value)
	{
		double erg = 0;
		for (int i=0; i<monom.size(); i++)
		{
			QMonomial m = (QMonomial)monom.elementAt(i);
			double zerg = m.factor.toDouble();
			for (int j=0; j<m.exp.length; j++)
			{
				int n = m.getExponent(j);
				if (n!=0) zerg *= Math.pow(value[j],n);
			}
			erg += zerg;
		}
		return erg;
	}
// Diese Methode liefert einen neues Polynom zurück, in dem die Variablen 
// mit den Nummern *identifierNrs* durch *values* ersetzt wurden.
public QPolynomial evaluate(int[] identifierNrs, Qelement[] values)
{
	QPolynomial erg = new QPolynomial();
	for (int i=0; i<monom.size(); i++)
	{
		QMonomial m = new QMonomial((QMonomial)monom.elementAt(i));
		for (int j=0; j<identifierNrs.length; j++)
		{
			int n = m.getExponent(identifierNrs[j]);
			m.setExponent(identifierNrs[j], 0);
			m.factor = m.factor.multiply(values[j].pow(n));
		}
		erg = erg.add(new QPolynomial(m));
	}
	return erg.unifiziereKonstanten();
}
	// Evaluate Nr. 6 nimmt ein Array von Complex, dass alle vorkommenden
	// Variablen mit einem Wert belegt, und liefert das ausgewertete Polynom zurück.
	public Complex evaluate(Complex[] value)
	{
		Complex erg = (Complex)value[0].abs_zero();
		for (int i=0; i<monom.size(); i++)
		{
			QMonomial m = (QMonomial)monom.elementAt(i);
			Complex zerg = value[0].abs_fromDouble(m.factor.toDouble(), 0.0);
			for (int j=0; j<m.exp.length; j++)
			{
				int n = m.getExponent(j);
				if (n!=0) zerg = (Complex)zerg.abs_multiply(value[j].abs_pow(n));
			}
			erg = (Complex)erg.abs_add(zerg);
		}
		return erg;
	}
    // Diese Methode liefert einen neues Polynom zurück, in dem die Variable 
    // mit der Nummer *identifierNr* durch *value* ersetzt wurde und alle anderen
    // Variablen durch 0. Im Gegensatz
    // zur obenstehenden evaluate - Methode nimmt sie einen CElemenet
    // und liefert auch einen solchen zurück.
    public Celement evaluate(int identifierNr, Celement value)
    {
        Celement erg = new Celement();

        for (int i=0; i<monom.size(); i++)
        {
            QMonomial m = new QMonomial((QMonomial)monom.elementAt(i));
            int n = m.getExponent(identifierNr);
            erg = erg.add((new Celement(m.factor)).multiply(value.pow(n)));
            // debug
            String ergs = erg.toDoubleString();
            ergs = ""+ergs;
        }
        return erg;
    }
    // Diese Methode liefert einen neues Polynom zurück, in dem die Variable 
    // mit der Nummer *identifierNr* durch *value* ersetzt wurde und alle anderen
    // Variablen durch 0. Im Gegensatz
    // zur obenstehenden evaluate - Methode nimmt sie einen CElemenet
    // und liefert auch einen solchen zurück.
    public Relement evaluate(int identifierNr, Relement value)
    {
        Relement erg = new Relement();

        for (int i=0; i<monom.size(); i++)
        {
            QMonomial m = new QMonomial((QMonomial)monom.elementAt(i));
            int n = m.getExponent(identifierNr);
            erg = erg.add((new Relement(m.factor)).multiply(value.pow(n)));
        }
        return erg;
    }
	// Diese Methode liefert einen neues Polynom zurück, in dem die Variable 
	// mit der Nummer *identifierNr* durch *value* ersetzt wurde.
	public QPolynomial evaluate(int identifierNr, Qelement value)
	{
		QPolynomial erg = new QPolynomial();
		for (int i=0; i<monom.size(); i++)
		{
			QMonomial m = new QMonomial((QMonomial)monom.elementAt(i));
			int n = m.getExponent(identifierNr);
			m.setExponent(identifierNr, 0);
			m.factor = m.factor.multiply(value.pow(n));
			erg = erg.add(new QPolynomial(m));
		}
		return erg.unifiziereKonstanten();
	}
	// Diese Methode liefert einen neues Polynom zurück, in dem die Variable 
	// mit der Nummer *identifierNr* durch *value* ersetzt wurde. Im Gegensatz
	// zur obenstehenden evaluate - Methode nimmt sie einen QPolynomial
	// liefert auch einen solchen zurück.
	public QPolynomial evaluate(int identifierNr, QPolynomial value)
	{
		QPolynomial erg = new QPolynomial();
		for (int i=0; i<monom.size(); i++)
		{
			QMonomial m = new QMonomial((QMonomial)monom.elementAt(i));
			int n = m.getExponent(identifierNr);
			m.setExponent(identifierNr, 0);
			erg = erg.add(value.pow(n).monomialMultiply(m));
		}
		return erg.unifiziereKonstanten();
	}
	// Diese 4. Evalutate - Methode nimmt einen RExpression und liefert auch einen solchen
	// zurück.
	public RExpression evaluate(int identifierNr, RExpression value)
	{
		RExpression erg = new RExpression();

		for (int i=0; i<monom.size(); i++)
		{
			QMonomial m = new QMonomial((QMonomial)monom.elementAt(i));
			int n = m.getExponent(identifierNr);
			erg = erg.add((new RExpression(m.factor)).multiply(value.pow(n)));
		}
		return erg;
	}
	// Und Nummero 5 nimmt RQuotientExp und  liefert auch einen solchen
	// zurück.
	public RQuotientExp evaluate(int identifierNr, RQuotientExp value)
	{
		RQuotientExp erg = new RQuotientExp();

		for (int i=0; i<monom.size(); i++)
		{
			QMonomial m = new QMonomial((QMonomial)monom.elementAt(i));
			int n = m.getExponent(identifierNr);
			m.setExponent(identifierNr, 0);
			RQuotientExp sum = new RQuotientExp(new QPolynomial(m));
			erg = erg.add(sum.multiply(value.pow(n)));
		}
		return erg;
	}
	/**

		von CK
		
	 * Berechnet supp_2(this) := Alle (Multi-)Potenzen (e_0,...,e_len-1), deren Summe < deg(this) ist,
	 * und fuer die ein a_0 und a_1 existiert, so dass (a_0,a_1,e_2,...,e_len-1) \in supp(this) gilt.
	 */
	public MultiIndex f_2supp(int len)
	{
//		System.out.println(" f_2supp called with length " + len );
		MultiIndex erg = new MultiIndex();
		int deg = getTotalDegree();
		// Wir muessen fuer alle Monome Multiindizies erzeugen
		for (int i = 0 ; i < monom.size() ; i++)
		{
			int[] temp = ( (QMonomial) monom.elementAt(i) ).exp;
			// Zuerst brauchen wir den Absolutgrad von x_2,...,x_len
			int partdeg = 0;
			for (int j = 2; j < temp.length ; j++)
				partdeg += temp[j];
			// Nun fuegen wir alle Monome hinzu, die wir mit dem Postfix x_2,...,x_len kriegen
			for (int i0 = 0; i0 + partdeg < deg ; i0++)
				for (int i1 = 0; i1 + partdeg < deg ; i1++)
				{
					int[] ind = new int[len];
					ind[0]=i0;
					ind[1]=i1;
					for (int j  = 2; j < len ; j++)
					{
						if (j < temp.length)
							ind[j]=temp[j];
						else  ind[j] = 0;
					}
					erg.insert(ind);
				}
		}
		return erg;
	}
        /** f_bivariate berechnet eine passende bivariate Faktorisierung
         * Ausgabe ist eine Faktorisierung [g_1,...,g_n][m_1,...,m_n] mit this = prod g_i^ m_i
         */

        public Object[][] f_bivariate()
        {
		System.out.println("---sfb--");
		QPolynomial[] g = this.factorBivariate(0,1);
		System.out.println("---sfb--");
          System.out.println("Bivariates Polynom " + this + " faktorisiert in :");
          for (int i=0; i < g.length ; i++)
            System.out.print(g[i] + " :: ");
          System.out.println();
		int size = 0;
		QPolynomial[] eg = new QPolynomial[g.length];
		int[] m = new int[g.length];
		for (int i = 0; i < g.length ; i++)
		{
			if (!g[i].isConstant())
			{
				int j;
				for (j = 0; (j < size) && (!eg[j].equals(g[i])) ; j++);
				if (j == size)
				{
					eg[size]=g[i];
					m[size]=1;
					size++;
				} else
				{
					m[j]++;
				}
			}
		}
		Object[][] erg = new Object[2][size];
		for (int i = 0; i < size ; i++)
		{
			erg[0][i]=eg[i];
			erg[1][i]=new Integer(m[i]);
		}

		for (int i=0; i<erg[0].length; i++)
		{
			System.out.println("Faktor "+i+": "+erg[0][i]);
			System.out.println("Anzahl "+i+": "+erg[1][i]);
		}
		
		return erg;
	}
	/**

		von CK
	
	 * Liefert eine Faktorisierung [ [f1,...,fn],[m1,...,mn] ] von this mit
	 * f = \prod fi^mi, die mit hoher Wahrscheinlichkeit korrekt ist, oder NULL
	 * in diesem Fall muss der Algorithmus neugestartet werden. (Evtl. r1 erhoehen)
	 * i,j geben die Groesse der Menge an, aus der Zufallszahlen gewaehlt werden.
	 */
	private Object[][] f_factorMultivariate(int r1)
	{
		int n = getHighestIndex();

		// Generiere die benoetigten Zufallszahlen und substituiere
		QPolynomial[] t = new QPolynomial[n+1];
                QPolynomial fsubst = new QPolynomial(this);
		for (int i=2 ; i < n+1 ; i++)
		{
			Qelement u = Qelement.random(r1);
			Qelement v = Qelement.random(r1);
			Qelement w = Qelement.random(r1);
			if (u.isZero()) t[i] = new QPolynomial();
			else t[i] = new QPolynomial((new QMonomial(Qelement.ONE,0,1)).multiply(u));
			if (!v.isZero()) t[i] = t[i].add( (new QMonomial(Qelement.ONE,1,1)).multiply(v));
			if (!w.isZero()) t[i] = t[i].add(new QPolynomial(w));
			if (t[i].isZero() || t[i].isConstant())
				i--;   // Wir wollen nicht durch 0 oder eine Konstante substituieren...
			else {
//				System.out.println(" Substitution fuer x_"+i+" : " + t[i]);
				fsubst = fsubst.evaluate(i,t[i]);
			}
		}

                // Nun brauchen wir die Faktorisierung diese Bivariaten Polynoms...
//				System.out.println("-----------start-bivariate----------");
				// Das bivariate Polynom soll unitaer sein.
				Qelement blf = (fsubst.leadingFactor()).reciprocal();
				Object[][] fb = (fsubst.multiply(blf)).f_bivariate();

				// univariate = gleich zurückgeben
				
				if ((fb[0].length ==1) && (((Integer)fb[1][0]).intValue() == 1) )
				{
					Object[][] erg = new Object[2][1];
					erg[0][0] = new QPolynomial(this);
					erg[1][0] = new Integer(1);
					return erg;
				}
				
				// Ausserdem muss das eigentliche Polynom angepasst werden...
				QPolynomial f = this.multiply(blf);
				QPolynomial[] g = new QPolynomial[fb[0].length];
				int[] m = new int[fb[0].length];
				for (int i = 0; i < fb[0].length ; i++)
                {
                  g[i]=(QPolynomial) fb[0][i];
                  // Mache die Faktoren primitiv
                  g[i]=g[i].divide(g[i].leadingFactor());
                  m[i] = ( (Integer) fb[1][i]).intValue();
                }
//				System.out.println(" fsubst : " + fsubst);
//				System.out.println(" lf     : " + blf);
				
				// Nun muessen wir noch die Konstanten faktoren "fixen"
                // Zuerst berechnen wir das Produkt der faktoren

//				QPolynomial prod = new QPolynomial(Qelement.ONE);
//				for (int i = 0; i < g.length ; i++)
//					prod = prod.multiply(g[i]);
				// Und dann bestimmen wir die Konstante und multiplizieren mit ihr.
//				QPolynomial plf = fsubst.divide(prod);
				// Suche passenden Faktor (m_i=1)
//				int fakpos;
//				for (fakpos = 0 ; (fakpos < m.length) && (m[fakpos] != 1) ; fakpos++);
//				if (fakpos == m.length)
//					throw new RuntimeException("Kein einfacher Faktor!! factor_bivariate verbessern!!");
//				g[fakpos]=g[fakpos].multiply(plf);

//				System.out.println("-----------stop-bivariate-----------");

                // Nun koennen wir liften...

//				System.out.println("Bivariates Polynom : " + fsubst);
//				System.out.println(" Lifting Polynom " +this);
//				System.out.print(" Faktoren : ");
//				for (int i=0; i< g.length; i++)
//					System.out.print(" :: " + g[i] + " :: ");
//				System.out.println();
//				System.out.println(" Calling f_hensel");

				QPolynomial lifted[] = f.f_hensel(g,m,t);
				if (lifted == null) return null;

				Object[][] erg = new Object[2][g.length];
				for (int i = 0; i < g.length; i++)
				{
					erg[0][i]=lifted[i].divide(lifted[i].leadingFactor());
					erg[1][i]=new Integer(m[i]);
				}

				return erg;
	}

//		von CK

/** Liftet die Faktorisierung eines Multivariaten Polynoms
	 * Input: g[],m[] zu liftende bivariate Faktorisierung, this = prod(g_i^m_i)
	 *        t[][] : Zugehoerige Substitution, t[0]=[u2,...,un-1], t[1]=..v.., t[2]=..w..
	 * Output: fertig geliftete Faktorisierung (null, falls die Eingabe nicht "stark genug" ist.
	 */
	public QPolynomial[] f_hensel(QPolynomial[] g, int[] m, QPolynomial[] t)
	{
		int n = getHighestIndex();
		int d = getTotalDegree();
		MultiIndex E = new MultiIndex(d);
		QPolynomial[] gs = new QPolynomial[g.length];
		for (int i = 0; i < g.length; i++)
			gs[i] = new QPolynomial(g[i]);

		// Step 2 aus Paper
		// TvO: Hier wird einfach angenommen, dass alle Variablennummern zwischen 0 und n auch besetzt sind.
		for (int j = 2; j < n+1 ; j++)
		{
//			System.out.println(" j = "+j+" in f_hensel");
			// h ist f substituiert
			// Die Variablen x_0,...,x_j-1 bleiben gleich,
			// x_j wird durch u_j*x_0 + v_j*x_1  + w_j - y ersetzt (y = x_j)
			QPolynomial subst = t[j];
			subst=subst.subtract(new QPolynomial(j));
			QPolynomial h = this.evaluate(j,subst);
			// Die Variablen x_j+1,... werden durch u_ix_0+v_ix_1+w_i ersetzt
			for (int i = j+1; i < n+1 ; i++)
			{
				subst = t[i];
				h = h.evaluate(i,subst);
			}

			// Nun werden noch die substituierten Faktoren gs[] berechnet.
			// Wenn j = 2 ist, sind dies gerade die g_i, ansonsten haben sie die Form
			// gs_i = g_i(x_0,...,x_j-2, u_j-1x_0 + v_j-1x_1 + w_j-1 - x_j-1)
			// mit den g_i aus dem letzten schleifendurchlauf.
			if (j > 2)
				for (int i = 0; i < gs.length ; i++)
				{
					subst = t[j-1];
					subst = subst.subtract( new QPolynomial(j-1) );
//					System.out.println("gs["+i+"] = " + gs[i]);
					gs[i] = gs[i].evaluate(j-1,subst);
//					System.out.println("gs["+i+"] = " + gs[i] + " Nach substitution von x_"+i+" durch "+subst);
				}

			// Nun muss noch der Start-MultiIndex generiert werden.
			// Er besteht aus allen e aus N^j, mit \sum e <=d und
			// \exist a0,a1 : (a0,a1,e2,e_j-2)\in E

			MultiIndex Enew = new MultiIndex();
			if (j == 2)  // Hier ist die Bedingung leer, und wir brauchen alle Tupel mit a1+a2<=d
			{
				for (int a0 = 0 ; a0 <= d ; a0++)
					for (int a1 = 0; a0+a1 <= d ; a1++)
					{
						int[] tidx = new int[j];
						tidx[0] = a0;
						tidx[1] = a1;
						Enew.insert(tidx);
					}
			} else {
				for (int i = 0; i < E.size() ; i++)
				{
					int[] tmp = E.getIndex(i);
					int isum = 0;
					for (int i1 = 2 ; i1 < tmp.length ; i1++) // Falls j=3 passiert hier nix
						isum += tmp[i1];
					for (int a0 = 0 ; a0+isum <= d ; a0++)
						for (int a1 = 0; a0+a1+isum <= d ; a1++)
							for (int e = 0; e+a0+a1+isum <= d; e++)
							{
								int[] tidx = new int[j];
								tidx[0] = a0;
								tidx[1] = a1;
								tidx[j-1] = e;
								for (int i1 = 2 ; i1 < tmp.length ; i1++) // Falls j=3 passiert hier nix
									tidx[i1] = tmp[i1];
								Enew.insert(tidx);
							}
				}
			}
			E = Enew;
			// Nun kommt das eigentliche liften...
			for (int k = 0; k < d ; k++)
			{
//				System.out.print(" (k = "+k+") : Lifting Factors : ");
//				for (int i2=0; i2 < gs.length ; i2++)
//					System.out.print(" :: " + gs[i2] + " :: ");
//				System.out.println();
				gs = h.f_lifting(j,k,m,gs,E);
				if (gs == null) return null;
//				System.out.print("\n lifted to : ");
//				for (int i2=0; i2 < gs.length ; i2++)
//					System.out.print(" :: " + gs[i2] + " :: ");
//				System.out.println();
			}
		}

		// Nun muessen wir nur noch die Faktoren zusammenbasteln und diese zurueckgeben.
		// TvO: t[0] und t[1] sind beiden null. Da stimmt irgendwas nicht...
		
		QPolynomial[] erg = new QPolynomial[gs.length];
		for (int i = 0; i < gs.length; i++)
		{
			// Die Faktoren haben die Form g(x_0,...,x_n-1, u_nx_0 +v_nx_1+w_n -x_n)
			QPolynomial subst = t[n];
			subst = subst.subtract(new QPolynomial(n));
//			System.out.println("last sub is: " + subst);
//			System.out.println("in factor : " + gs[i]);
			erg[i] = gs[i].evaluate(n,subst);
		}
		return erg;
	}
	/**

		von CK
	
	 * Liftet eine Faktorisierung g[]^m[] modulo (x_j)^k+1 zu einer modulo (x_j)^k+2
	 * E ist hierbei die Menge der "wichtigen" Multiindizies. Ist k=0, so wird E mit einem
	 * sinnvollen Wert initialisiert.
	 * Sollten die Daten nicht "stark genug" sein, so wird NULL zurueckgeliefert.
	 */
	public QPolynomial[] f_lifting(int j, int k, int[] m, QPolynomial[] g, MultiIndex E)
	{
//		System.out.println("----Lifting called, j= " +j);
//		System.out.print(" Faktoren: ");
//		for (int i = 0; i < g.length ;  i++)
//			System.out.print(" :: " + g[i] + " :: ");
//		System.out.println();

		// Berechne die benoetigten S_i
		MultiIndex[] S = new  MultiIndex[g.length];
		for (int i = 0; i < g.length; i++)
			S[i] = g[i].f_2supp(j);
		// Wir brauchen die g'_i mit g'_i = g_i(x0,...,xj-1,0)
		QPolynomial[] gs = new QPolynomial[g.length];
		for (int i = 0; i < g.length; i++)
			gs[i]=g[i].evaluate(j,Qelement.ZERO);
//		System.out.print(" gs : (x_"+j+" = 0) ");
//		for (int i = 0; i < gs.length ;  i++)
//			System.out.print(" :: " + gs[i] + " :: ");
//		System.out.println();

		// Des weiteren brauchen wir das Produkt ueber g_i^m_i
		QPolynomial prodgi = new QPolynomial(Qelement.ONE);
		for (int i = 0; i < g.length; i++)
			prodgi = prodgi.multiply(g[i].pow(m[i]));
		// Davon brauchen wir auch noch die Einschraenkung auf x0,...,xj-1
		QPolynomial prodgis = prodgi.evaluate(j,Qelement.ZERO);
		// Last but not least muessen wir noch wissen, wieviele Eintraege es in den S_i gibt
		int Ssize = 0;
		for (int i = 0; i < S.length; i++)
		{
			Ssize += S[i].size();
		}

		// Nun koennen wir die Matrix bauen.
		// Sie hat Ssize Spalten (da Ssize unbekannte) und E.size() Zeilen.
		Qelement[][] aMatrix = new Qelement[E.size()][Ssize];
		for (int i1 = 0 ; i1 < E.size() ; i1 ++)
			for (int i2 = 0; i2 < Ssize ; i2 ++)
				aMatrix[i1][i2] = Qelement.ZERO;

		// Nun berechnen wir fuer jede Unbekannte den zugehoerigen Koeffizienten
		// (Welcher ein Polynom ist), und fuellen dann die zugehoerige Spalte anhand
		// der Monome.

		int colpos = 0;
		for (int i = 0; i < S.length ; i++)
		{
			// Ein Teil des Koeffizienten aendert sich nicht : m_i*prodgis/g'_i
			QPolynomial ccoef = new QPolynomial ( new Qelement(m[i]) );
			ccoef = ccoef.multiply(prodgis);
//			System.out.println(" Teile " + ccoef + " durch gs[" + i + "] = " + gs[i]);
			if (!gs[i].isZero()) ccoef = ccoef.divide(gs[i]);
//			else System.out.println("**********\n!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Division durch null in f_lifting, gs["+i+"] = " + gs[i]+ "\n**********");
			for (int i2 = 0; i2 < S[i].size() ; i2++)
			{
				// Um den richtigen Koeffizienten zu kriegen, muessen wir nur noch mit
				// x^s multiplizieren
				int[] s = S[i].getIndex(i2);
				QMonomial mul = new QMonomial(Qelement.ONE,s);
				QPolynomial col = ccoef.monomialMultiply(mul);
				// Nun muessen wir nur noch die Koeffizienten von col anhand der Position
				// ihres Exponenten in E in die Matrix einsortieren.
				for (int i3 = 0; i3 < col.monom.size() ; i3++)
				{
					QMonomial temp = (QMonomial) col.monom.elementAt(i3);
					int rowpos = E.getPos(temp.exp);
					if (rowpos >= 0 ) // <0 bedeutet, das der Exponent nicht in E vorkommt
						aMatrix[rowpos][colpos] = temp.factor;
				}
				// Wir sind eine Spalte weiter...
				colpos++;
			}
		}

		// Nun koennen wir die Rechte Seite des	Gleichungssystems aufstellen.
		// Sie hat die Form this - prodgi

		QPolynomial rs = this.subtract(prodgi);
//		System.out.println("Rechte Seite : " + rs);
//		System.out.println("E : " + E);
		Qelement[] b = new Qelement[E.size()];
		for (int  i=0 ; i < E.size() ; i++)
			b[i] = Qelement.ZERO;
		// Wir muessen jetzt wieder die Monome von rs durchgehen und entsprechend E einsortieren...
		for (int i = 0; i < rs.monom.size() ; i++)
		{
			QMonomial temp = (QMonomial) rs.monom.elementAt(i);
			// Nur Eintraege mit deg_y = k+1 sind interessant...
//			System.out.print("Rechte Seite fuer Monom " + temp + " highestIndex : " + temp.getHighestIndex() + ", exp : ");
//			for (int ij = 0 ; ij <temp.exp.length ; ij ++)
//				System.out.print(" :: " + temp.exp[ij]);
//			System.out.println();
			if (temp.getHighestIndex() == j)
				if (temp.exp[j] == k+1)
				{
					// Wo steht der Eintrag in E
					// E ist aus N^j, also muessen wir den j+1ten Eintrag loeschen.
					int[] ex = new int[j];
					for (int i2 = 0; i2 < j ; i2++)
						ex[i2] = temp.exp[i2];
					int rowpos = E.getPos(ex);
					if (rowpos >= 0)
						b[rowpos] = temp.factor;
//					System.out.println("Pos. " + rowpos);
				}
		}

		// Nun koennen wir das LGS loesen
		// needed enthaellt zum schluss die zum Loesen benoetigten Eintraege.
		RingMatrix A = new RingMatrix(aMatrix);
		int[] needed = new int[E.size()];
		for (int i = 0; i < needed.length ; i++)
			needed[i] = -1;

		System.out.println("Matrix : \n" + A);
		System.out.println("Rechte Seite:");
		for (int i = 0; i < b.length ; i++)
			System.out.println(":: " + b[i]);
		System.out.println(" Loese LGS : ");

		Field[] solve = A.solveIter(b, needed);
		if (solve == null) return null;
		System.out.print(" Loesung des LGS ist : ");
		for (int i = 0; i < solve.length ; i++)
			System.out.print(" :: " + solve[i] + " :: ");
		System.out.println();
		

		// wenn k = 0 ist, berechne E
		if (k == 0)
			E.trim(needed);

		// berechne die gelifteten Faktoren
		QPolynomial[] erg = new QPolynomial[g.length];
		for (int i = 0; i < g.length ; i++)
			erg[i] = g[i];
		int pos = 0;
		for (int i = 0; i < g.length ; i++)
		{
			erg[i] = new QPolynomial(g[i]);
//			System.out.println(" Lifting g["+i+"] : " + g[i]);
			for (int i2 = 0; i2 < S[i].size() ; i2++)
			{
				int[] ex = new int[j+1];
				int[] ti = S[i].getIndex(i2);
				for (int l = 0; l < j ; l++ )
					ex[l] = ti[l];
				ex[j] = k+1;
				QMonomial mul = new QMonomial((Qelement)solve[pos],ex);
//				System.out.println(" Adding " + mul);
				// Hier gehts weiter, noch g*_is*x^sy^k+1 addieren (=mul)
				erg[i] = erg[i].add(mul);
				// pos ist die Position von g*_is im Loesungsvektor
				pos++;
			}
//			System.out.println("...done, g*["+i+"] = " + erg[i]);
		}
		return erg;
	}
/**
* Insert the method's description here.
* Creation date: (18.05.2002 21:30:22)
* @return arithmetik.QPolynomial
*/
public QPolynomial[] factorBivariate(int xIndex, int yIndex)
{
	if (getDegreeIn(xIndex)<1) return factorUnivariate();
	if (getDegreeIn(yIndex)<1) return factorUnivariate();
	Vector ergv = new Vector();

	QPolynomial f = new QPolynomial(this);
	QPolynomial x = new QPolynomial(xIndex);
	while (f.getCoefficient(xIndex,0).isZero())
	{
		ergv.addElement(x);
		f = f.divide(x);
	}		
	
	QPolynomial der = f.derive(xIndex);
	System.out.println(der);
	QPolynomial gcd = f.gcd(der);
	if ((gcd.getDegreeIn(xIndex)>0) || (gcd.getDegreeIn(yIndex)>0))
	{
		QPolynomial[] gcdFaks = gcd.factorBivariate(xIndex, yIndex);
		for (int i=0; i<gcdFaks.length; i++)
			ergv.addElement(gcdFaks[i]);
	}
	
	System.out.println(gcd);
	f = f.divide(gcd);
	System.out.println("Quadratfrei: "+f);
	int m = f.getDegreeIn(xIndex);
	int n = f.getDegreeIn(yIndex);
	if (m<1) return factorUnivariate();
	if (n<1) return factorUnivariate();
	
	Qelement[][] mat = new Qelement[4*m*n][2*m*n+m+n];			// zeilen * spalten
//	Qelement[][] mat = new Qelement[4*m*n][4*m*n];				// zeilen * spalten
	for (int i=0; i<mat.length; i++)
		for (int j=0; j<mat[0].length; j++)
			mat[i][j] = Qelement.ZERO;
	
	for (int k=0; k<=2*m-1; k++)
		for (int l=0; l<=2*n-1; l++)
		{
//			System.out.println("1. Durchlauf: ");
//				System.out.println("Grenzen: "+Math.max(0, k-m)+" "+Math.min(m-1,k)+" "+
//								   Math.max(0,l-n+1)+" "+Math.min(n,l+1));
			for (int i=Math.max(0, k-m); i<= Math.min(m-1,k); i++)
				for (int j=Math.max(0,l-n+1); j <= Math.min(n,l+1); j++)
				{
//						System.out.println(k+" "+l+" "+i+" "+j);
					mat[k*2*n+l][i*(n+1)+j] = 
					  (new Qelement(-l+2*j-1)).multiply(f.getCoefficient(xIndex, k-i).getCoefficient
											(yIndex, l-j+1).leadingFactor());
				}
//				System.out.println("2. Durchlauf: ");
//				System.out.println("Grenzen: "+Math.max(0, k-n+1)+" "+Math.min(m,k+1)+" "+
//						Math.max(0,l)+" "+Math.min(n-1,l));
			for (int i=Math.max(0, k-m+1); i<= Math.min(m,k+1); i++)
				for (int j=Math.max(0,l-n); j <= Math.min(n-1,l); j++)
				{
//						System.out.println(k+" "+l+" "+i+" "+j);
					mat[k*2*n+l][m*(n+1)+j+i*n] = 
					  (new Qelement(k-2*i+1)).multiply(f.getCoefficient(xIndex, k-i+1).getCoefficient(yIndex, l-j).leadingFactor());
				}
		}
	System.out.println("matrix fertig");

//	RingMatrix matrix = new RingMatrix(mat);
//		System.out.println(matrix);
	Qelement[][] c = Qelement.findCoreBasis(mat);

//	RingVector[] c = matrix.coreBasis();
//	int r = c.length - 4*m*n + 2*m*n+m+n;				// Abzüglich der zusätzlichen Spalten.
	int r = c.length;

	System.out.println(r+" Faktoren des Polynoms festgestellt.");
	
	QPolynomial charPol = new QPolynomial(), fx = f.derive(xIndex), g = new QPolynomial();
	RingMatrix A = new RingMatrix(new Qelement(), 1,1);
	
	boolean allesOk = false;
	while (!allesOk)									// Schleife der Randomisierung
	{
		System.out.println("Suche neues g zufällig....");
		g = new QPolynomial();
		QPolynomial[] gi = new QPolynomial[r];
		int l = 0;
		for (int k=0; k<c.length; k++)
		{
			QPolynomial giloc = new QPolynomial();
			
			for (int i=0; i<=m-1; i++)
				for (int j=0; j<=n; j++)
				{
					int[] e = {i,j};
//					Qelement fac = (Qelement)c[k].getValue(i*(n+1)+j+1);
					Qelement fac = (Qelement)c[k][i*(n+1)+j];
					if (!fac.isZero()) giloc = giloc.add(new QPolynomial(new QMonomial(fac, e)));
				}
//			System.out.println("Manipuliere Basisvektoren...");
//			long[][] gman = new long[0][0];
//			if (k==0) gman = new long[][]{{-12,1,0},{-8,1,1	},{-19,1,2},{-12,3,1},{-2,5,0},{1,3,0}};
//			if (k==1) gman = new long[][]{{12,1,0},{10,1,1},{18,1,2},{12,3,1},{2,5,0}};
//			if (k==2) gman = new long[][]{{-18,1,0},{-12,1,1},{-22,1,2},{-14,3,1},{-2,5,0}};
//			giloc = QPolynomial.fromArray(gman);
			if (!giloc.isZero()) 
			{
				gi[l++] = giloc;
				System.out.println(l+". Basisvektor: "+giloc);
				long zufall = (long)Math.round(Math.random()*m*n*2);
				System.out.println("Zufallsfaktor = "+zufall);
				g = g.add(giloc.multiply(new QPolynomial(new Qelement(zufall))));
			}
		}

		System.out.println("g ist : "+g);
//		long[][] gman = {{2,1,1},{-1,1,2},{1,3,0}};
//		g = QPolynomial.fromArray(gman);
//		System.out.println("g manipuliert zu : "+g);
		
//		long[][] gman = {{1,0,1}};
//		g = QPolynomial.fromArray(gman);
//		System.out.println("g manipuliert zu : "+g);

/*
// 		Alternative Berechnung von Eg(x)
		QPolynomial gminusalphafx = g.subtract((new QPolynomial(3)).multiply(fx));
		System.out.println("g - alpha fx  = "+gminusalphafx);
		int h1xdeg = gminusalphafx.getDegreeIn(xIndex)-1, h1ydeg = gminusalphafx.getDegreeIn(yIndex);
		int h2xdeg = f.getDegreeIn(xIndex)-1, h2ydeg = f.getDegreeIn(yIndex);
		
		RQuotientExp[][] mat3 = new RQuotientExp[(h1xdeg+h2xdeg+2)*(h1ydeg+h2ydeg+1)][(h1xdeg+1)*(h1ydeg+1)+(h2xdeg+1)*(h2ydeg+1)];
		for (int i=0; i<mat3.length; i++)
			for (int j=0; j<mat3[0].length; j++)
				mat3[i][j] = new RQuotientExp();
				
		for (int i=0; i<=h1xdeg+h2xdeg+1; i++)
			for (int j=0; j<=h1ydeg+h2ydeg; j++)
			{
				for (int i1=0; i1<=h1xdeg; i1++)
					for (int j1=0; j1<=h1ydeg; j1++)
					{
						if ((i-i1>=0) && (i-i1<=h2xdeg+1) && (j-j1>=0) && (j-j1<=h2ydeg))
							mat3[i*(h1ydeg+h2ydeg+1)+j][i1*(h1ydeg+1)+j1] = new RQuotientExp(f.getCoefficient(xIndex,i-i1).getCoefficient(yIndex,j-j1));
					}
				for (int i2=0; i2<=h2xdeg; i2++)
					for (int j2=0; j2<=h2ydeg; j2++)
					{
						if ((i-i2>=0) && (i-i2<=h1xdeg+1) && (j-j2>=0) && (j-j2<=h1ydeg))
							mat3[i*(h1ydeg+h2ydeg+1)+j][((h1xdeg+1)*(h1ydeg+1))+i2*(h2ydeg+1)+j2] = new RQuotientExp(gminusalphafx.getCoefficient(xIndex,i-i2).getCoefficient(yIndex,j-j2).negate());
					}
			}
		RingMatrix malter = new RingMatrix(mat3);
		System.out.println("\r\nAlternativmatrix:\r\n "+malter+"\r\n");
		RingMatrix aev1 = new RingMatrix(malter);
		RQuotientExp wert = new RQuotientExp(new Qelement(3,2));
		for (int i=1; i<=aev1.getRows(); i++)
			for (int j=1; j<=aev1.getColumns(); j++)
				aev1.setValue(((RQuotientExp)aev1.getValue(i,j)).evaluate(3,wert),i,j);
		System.out.println("Ausgewertete Matrix = "+aev1);
		System.out.println("Obere Dreiecksmatrix:\r\n "+malter.findRMatrix());
		RQuotientExp altpol = (RQuotientExp)malter.getDiagonalProductOfRMatrix();
		QPolynomial rem = altpol.zaehler.toQPolynomial().remainder(altpol.nenner.toQPolynomial());
		QPolynomial div = altpol.zaehler.toQPolynomial().divide(altpol.nenner.toQPolynomial());
		System.out.println("Rest = "+rem);
		System.out.println("Alternatives Polynom Eg(x) = "+div);
		RingMatrix aev = new RingMatrix(malter.findRMatrix());
		for (int i=1; i<=aev.getRows(); i++)
			for (int j=1; j<=aev.getColumns(); j++)
				aev.setValue(((RQuotientExp)aev.getValue(i,j)).evaluate(3,wert),i,j);
		RingVector sol = aev.solveRightUpperTriangleMatrix(new RingVector(new RQuotientExp(), aev.getRows()), 0);
		System.out.println("Ausgewertete r = "+aev);
		
		System.out.println("Ergebnisvec = "+sol);
		System.out.println("ausgewertet Matrix * erg = "+aev1.vectorMultiply(sol));
		System.out.println("dreiecksmatrix * erg = "+aev.vectorMultiply(sol));
		System.out.println("Ausgewertete Matrix -> R : "+aev1.findRMatrix());
		RingVector sol2 = aev1.findRMatrix().solveRightUpperTriangleMatrix(new RingVector(new RQuotientExp(), aev.getRows()), 0);
		System.out.println("Lösung der ausgewerteten Matrix : "+sol2);
		
*/		


		// Step 3
		System.out.println("Zuendung der 3. Stufe");
		A = new RingMatrix(new Qelement(), r,r);
		QPolynomial[] gjfxmodf = new QPolynomial[r];
		for (int i = 0; i<r; i++)
			gjfxmodf[i] = gi[i].multiply(fx).remainder(f);
		for (int i=0; i<r; i++)
		{
			QPolynomial ggi = g.multiply(gi[i]).remainder(f);
			Qelement[][] mat2 = new Qelement[(m+1)*(n+1)][(m+1)*(n+1)];
			for (int j=0; j<mat2.length; j++)
				for (int k=0; k<mat2[0].length; k++)
					mat2[j][k] = Qelement.ZERO;
			
			Qelement[] loevec = new Qelement[(m+1)*(n+1)];
			for (int j=0; j<loevec.length; j++) loevec[j] = Qelement.ZERO;
			
			for (int j=0; j<=m; j++)
				for (int k=0; k<=n; k++)
				{
					loevec[j*(n+1)+k] = ggi.getCoefficient(xIndex,j).getCoefficient(yIndex,k).leadingFactor();
					
					for (l=0; l<r; l++)
						mat2[j*(n+1)+k][l] = gjfxmodf[l].getCoefficient(xIndex,j).getCoefficient(yIndex,k).leadingFactor();	
				}
			RingMatrix rm = new RingMatrix(mat2);
			RingVector loe = new RingVector(loevec);
			System.out.println("Gleichungssystem = "+rm);
			System.out.println("Lösungsvektor = "+loe);
			RingMatrix rod[] = rm.findRMatrix(loe);
			System.out.println("Rechte Obere Dreieck = "+rod[0]);
			System.out.println("Lösungsvektor = "+rod[1]);				
				
			RingVector a = rm.solveWithGauss(new RingVector(loevec));
			if (a == null) throw new RuntimeException("No Solution for A");
			for (int j=1; j<=r; j++)
				A.setValue(a.getValue(j),j,i+1);
		}
		System.out.println("Ergebnismatrix = "+A);
		
//Neues Gleichungslösen
/*
		for (int i=0; i<r; i++)
		{
                        int aktZeile=0;
                        int frontZeile=0;
                        boolean aktZeileOK=false;
                        int j=0,k=0;
			QPolynomial ggi = g.multiply(gi[i]).remainder(f);
			Qelement[][] mat2 = new Qelement[(m+1)*(n+1)][r];
			Qelement[] loevec = new Qelement[(m+1)*(n+1)];
                        while(aktZeile<r)
                        {
  			  loevec[j*(n+1)+k] = ggi.getCoefficient(0,j).getCoefficient(1,k).leadingFactor();
			    for (l=0; l<r; l++)
	                                  mat2[j*(n+1)+k][l] = gjfxmodf[l].getCoefficient(0,j).getCoefficient(1,k).leadingFactor();
                          if(!mat2[aktZeile][aktZeile].isZero())
                          {
                            loevec[aktZeile]=loevec[aktZeile].divide(mat2[aktZeile][aktZeile]);
	                            for(l=0;l<r;l++)
                            {
                              mat2[aktZeile][l]=mat2[aktZeile][l].divide(mat2[aktZeile][aktZeile]);
                            }
                            for(l=0;l<aktZeile;l++)
                            {
	                             for(m=0;m<r;m++)
                              {
                                loevec[aktZeile]=loevec[aktZeile].subtract(mat2[aktZeile][m].multiply(loevec[l]));
                                mat2[aktZeile][m]=mat2[aktZeile][m].subtract(mat2[aktZeile][l].multiply(mat2[l][m]));
                              }
                            }
                            aktZeileOK=true;
                          }
                          else
                          {
	                            aktZeileOK=false;
                            if(!mat2[frontZeile][aktZeile].isZero())
                            {
                              Qelement dummy;
                              for(l=0;l<r;l++)
                              {
	                                dummy=mat2[aktZeile][l];
                                mat2[aktZeile][l]=mat2[frontZeile][l];
                                mat2[aktZeile][l]=dummy;
                                dummy=loevec[aktZeile];
	                                loevec[aktZeile]=loevec[frontZeile];
                                loevec[frontZeile]=dummy;
                              }
                            }
	                          }
                          k++;
                          if(k>n) {k=0;j++;}
                          frontZeile++;
	                          if(aktZeileOK)
                           aktZeile++;
                          System.out.println("AktZeile: "+aktZeile);

                          for(int u=0;u<aktZeile;u++)
                          {
                            String Zeile="";
                            for(int t=0;t<r;t++)
                            {
                              Zeile=Zeile+" "+mat2[u][t].toString()+"|";
                            }
                            Zeile = Zeile + "  ||| "+loevec[u];
                            System.out.println(Zeile);
                          }
                        }
                        while(aktZeile>0)
	                        {
                          aktZeile--;
                           for(l=aktZeile+1;l<r;l++)
                            {
                               loevec[aktZeile]=loevec[aktZeile].subtract(mat2[aktZeile][l].multiply(loevec[l]));
                               mat2[aktZeile][l]=mat2[aktZeile][l].zero();
                            }
                        }
                          System.out.println("Aktuelle Matrix");
                          for(int u=0;u<r;u++)
                          {
                            String Zeile="";
                            for(int t=0;t<r;t++)
                            {
                              Zeile=Zeile+" "+mat2[u][t].toString()+"|";
                            }
                            Zeile = Zeile + "  ||| "+loevec[u];
                            System.out.println(Zeile);
                          }
        		for (j=0; j<r; j++)
				A.setValue(loevec[j],j+1,i+1);
		}
		
		System.out.println("Ergebnismatrix (Tobi) = "+A);
*/
		RingMatrix IxminA = new RingMatrix(new RQuotientExp(), r, r);
		for (int i=1; i<=r; i++) 
			for (int j=1; j<=r; j++)
			{
				if (i!=j) IxminA.setValue(new RQuotientExp((Qelement)A.getValue(i,j)),i,j);
				else IxminA.setValue((new RQuotientExp((Qelement)A.getValue(i,j))).subtract
						(new RQuotientExp(0)),i,j);
			}

		System.out.println("Matrix fürs charakteristische Polynom = "+IxminA);	
		RQuotientExp charPolRat = (RQuotientExp)IxminA.developDeterminant();
		charPol = charPolRat.zaehler.toQPolynomial();
		System.out.println("Charakteristisches Polynom: "+charPol);
		
		allesOk = charPol.gcd(charPol.derive(0)).equals(new QPolynomial(Qelement.ONE));

		System.out.println("Ist inseperabel : "+allesOk);

		// Manipulation von allesOk
//		allesOk = true;
	}

	QPolynomial charPolInt = charPol.makeCoefficientInteger();
	System.out.println("Integer - Polynom: "+charPolInt);

	QPolynomial[] faktoren = charPolInt.factorUnivariate();	

	System.out.println("Univariate Faktorisierung ergab "+faktoren.length+" Faktoren");
	
	QPolynomial f0 = new QPolynomial(f);

//	Vector absFaktoren = new Vector();

	// Der letzte Faktor der univariaten Faktorisierung wird ausgelassen, da jeder Faktor in jedem Schritt
	// rausdividiert wird und somit der verbleibende Rest irreduzibel sein muss. Dieser Faktor hat dann als
	// einziges einen Leitkoeffizienten, der von 1 verschieden ist.	
	for (int i=0; i<faktoren.length-1; i++)
	{
		QPolynomial phi = faktoren[i];
		System.out.println("Faktor phi = "+phi);
		// 5a
/*		
		QPolynomial qphi = phi.toQPolynomial(0);
		Qalgebraic lambda = (new Qalgebraic(qphi)).getX();
		Polynomial f0p = f0.toQalgebraicPolynomial(qphi);
		Polynomial gp = g.toQalgebraicPolynomial(qphi);
		Polynomial fxp = fx.toQalgebraicPolynomial(qphi);
		Polynomial lambdafxp = fxp.multiply(new Polynomial(new Polynomial(lambda,0),0));
		Polynomial f1 = f0p.gcd(gp.subtract(lambdafxp));
		f1 = f1.normalizeMultivariate();
		absFaktoren.addElement(f1);
		System.out.println("Absolute Faktorengruppe: "+f1);
*/
		// 5b
		int d = phi.getDegreeIn(0);
		QPolynomial fxtotphiofgoverfx = new QPolynomial();
		for (int j=0; j<=d; j++)
			fxtotphiofgoverfx = fxtotphiofgoverfx.add(fx.pow(d-j).multiply(g.pow(j)).multiply(phi.getCoefficient(0,j)));
		System.out.println("fxtotphiofgoverfx = "+fxtotphiofgoverfx);
		System.out.println("wird gcd mit = "+f0);
		System.out.println("Divident ist "+fxtotphiofgoverfx.divide(f));
		System.out.println("Remainder ist: "+fxtotphiofgoverfx.remainder(f));
		QPolynomial fac = f0.gcd(fxtotphiofgoverfx);
		if (fxtotphiofgoverfx.isZero()) fac = f0;
		System.out.println("Faktor ergibt : "+fac);
		ergv.addElement(fac);
		// 5c
		f0 = f0.divide(fac);
	}

	ergv.addElement(f0);
	QPolynomial[] erg = new QPolynomial[ergv.size()];
	for (int i=0; i<ergv.size(); i++) erg[i] = (QPolynomial)ergv.elementAt(i);
	return erg;
}
/**
* Faktorisiert ein Bivariates Polynom vollständig. Eine Faktorisierung über dem algebraischen Abschluss
* von Q kann auch berechnet werden (ist im moment auskommentiert)
*
* Creation date: (18.05.2002 21:30:22)
* @return arithmetik.QPolynomial
*/
public QPolynomial[] factorBivariateKurz(int xIndex, int yIndex)
{
	// Vorbereitung

	if (isZero()) return new QPolynomial[]{new QPolynomial()};				// Falls 0, ein Nullpolynom zurück
	Vector ergv = new Vector();
	
	QPolynomial f = new QPolynomial(this);									// Quadratfrei machen
	QPolynomial der = f.derive(xIndex);
	QPolynomial gcd = f.gcd(der);
	if ((gcd.getDegreeIn(xIndex)>0) || (gcd.getDegreeIn(yIndex)>0))			// gcd selbst faktorisieren und merken
	{
		QPolynomial[] gcdFaks = gcd.factorBivariate(xIndex, yIndex);
		for (int i=0; i<gcdFaks.length; i++)
			ergv.addElement(gcdFaks[i]);
	}
	
	f = f.divide(gcd);
	int m = f.getDegreeIn(xIndex);
	int n = f.getDegreeIn(yIndex);
	if ((m<1) || (n<1))
	{
		QPolynomial[] faks;
		if (m<1) faks = factorUnivariate();								// Falls jetzt univariat, dort lösen
		else faks = factorUnivariate();
		for (int i=0; i<faks.length; i++)
			ergv.addElement(faks[i]);
		
		QPolynomial[] erg = new QPolynomial[ergv.size()];
		for (int i=0; i<ergv.size(); i++) erg[i] = (QPolynomial)ergv.elementAt(i);
		return erg;
	}
		
	
	// Schritt 1
	
	Qelement[][] mat = new Qelement[4*m*n][4*m*n];							// zeilen * spalten  Matrix initialisieren
	for (int i=0; i<mat.length; i++)
		for (int j=0; j<mat[0].length; j++)
			mat[i][j] = new Qelement();
	
	for (int k=0; k<=2*m-1; k++)
		for (int l=0; l<=2*n-1; l++)
		{
			for (int i=Math.max(0, k-m); i<= Math.min(m-1,k); i++)
				for (int j=Math.max(0,l-n+1); j <= Math.min(n,l+1); j++)
				{
					mat[k*2*n+l][i*(n+1)+j] = 
					  (new Qelement(-l+2*j-1)).multiply(f.getCoefficient(xIndex, k-i).getCoefficient
											(yIndex, l-j+1).leadingFactor());
				}
			for (int i=Math.max(0, k-m+1); i<= Math.min(m,k+1); i++)
				for (int j=Math.max(0,l-n); j <= Math.min(n-1,l); j++)
				{
					mat[k*2*n+l][m*(n+1)+j+i*n] = 
					  (new Qelement(k-2*i+1)).multiply(f.getCoefficient(xIndex, k-i+1).getCoefficient(yIndex, l-j).leadingFactor());
				}
		}
	
	RingMatrix matrix = new RingMatrix(mat);
	RingVector[] c = matrix.coreBasis();
	int r = c.length - 4*m*n + 2*m*n+m+n;				// Abzüglich der zusätzlichen Spalten.
	
	QPolynomial charPol = new QPolynomial(), fx = f.derive(xIndex), g = new QPolynomial();
	RingMatrix A = new RingMatrix(new Qelement(),1,1);
	
	boolean allesOk = false;
	while (!allesOk)									// Schleife der Randomisierung
	{
		// Schritt 2
		
		g = new QPolynomial();							// g wählen
		QPolynomial[] gi = new QPolynomial[r];
		int l = 0;
		for (int k=0; k<c.length; k++)
		{
			QPolynomial giloc = new QPolynomial();
			
			for (int i=0; i<=m-1; i++)
				for (int j=0; j<=n; j++)
				{
					int[] e = {i,j};
					Qelement fac = (Qelement)c[k].getValue(i*(n+1)+j+1);
					if (!fac.isZero()) giloc = giloc.add(new QPolynomial(new QMonomial(fac, e)));
				}
			if (!giloc.isZero()) 
			{
				gi[l++] = giloc;
				long zufall = (long)Math.round(Math.random()*m*n*2);
				g = g.add(giloc.multiply(new QPolynomial(new Qelement(zufall))));
			}
		}

		// Schritt 3
		A = new RingMatrix(new Qelement(), r,r);							// Matrix A vorbereiten
		QPolynomial[] gjfxmodf = new QPolynomial[r];
		for (int i = 0; i<r; i++)
			gjfxmodf[i] = gi[i].multiply(fx).remainder(f);
		for (int i=0; i<r; i++)
		{
			QPolynomial ggi = g.multiply(gi[i]).remainder(f);
			Qelement[][] mat2 = new Qelement[(m+1)*(2*n+1)][(m+1)*(2*n+1)];
			for (int j=0; j<mat2.length; j++)
				for (int k=0; k<mat2[0].length; k++)
					mat2[j][k] = new Qelement();
			
			Qelement[] loevec = new Qelement[(m+1)*(2*n+1)];
			for (int j=0; j<loevec.length; j++) loevec[j] = new Qelement();
			
			for (int j=0; j<=m; j++)
				for (int k=0; k<=2*n; k++)
				{
					loevec[j*(2*n+1)+k] = ggi.getCoefficient(xIndex,j).getCoefficient(yIndex,k).leadingFactor();
					
					for (l=0; l<r; l++)
						mat2[j*(2*n+1)+k][l] = gjfxmodf[l].getCoefficient(xIndex,j).getCoefficient(yIndex,k).leadingFactor();	
				}
			RingMatrix rm = new RingMatrix(mat2);
				
			RingVector a = rm.solveWithGauss(new RingVector(loevec));
			if (a == null) throw new RuntimeException("No Solution for A");
			for (int j=1; j<=r; j++)
				A.setValue(a.getValue(j),j,i+1);
		}
		RingMatrix IxminA = new RingMatrix(new RQuotientExp(), r, r);
		for (int i=1; i<=r; i++) 
			for (int j=1; j<=r; j++)
			{
				if (i!=j) IxminA.setValue(new RQuotientExp((Qelement)A.getValue(i,j)),i,j);
				else IxminA.setValue((new RQuotientExp((Qelement)A.getValue(i,j))).subtract
						(new RQuotientExp(0)),i,j);
			}

		RQuotientExp charPolRat = (RQuotientExp)IxminA.developDeterminant();
		charPol = charPolRat.zaehler.toQPolynomial();
		
		allesOk = charPol.gcd(charPol.derive(0)).equals(new QPolynomial(Qelement.ONE));		// Test: Ist Eg(x) inseperabel?
	}

	// Schritt 4

	QPolynomial charPolInt = charPol.makeCoefficientInteger();
	UnivariatePolynomial charPolUni = charPol.makeCoefficientInteger().toUnivariatePolynomial();
	
	Stack faktoren = charPolUni.factorizeSquarefreeHensel();
	QPolynomial f0 = new QPolynomial(f);
	
	// Schritt 5
	
	while (!faktoren.isEmpty())
	{
		UnivariatePolynomial phi = (UnivariatePolynomial)faktoren.pop();		// Erster koeffizient = konstante
		// 5a
/*
		// Dieser Teil würde die absoluten Faktoren berechnen, falls dies nötig ist
	
		QPolynomial qphi = phi.toQPolynomial(0);
		Qalgebraic lambda = (new Qalgebraic(qphi)).getX();
		Polynomial f0p = f0.toQalgebraicPolynomial(qphi);
		Polynomial gp = g.toQalgebraicPolynomial(qphi);
		Polynomial fxp = fx.toQalgebraicPolynomial(qphi);
		Polynomial lambdafxp = fxp.multiply(new Polynomial(new Polynomial(lambda,0),0));
		Polynomial f1 = f0p.gcd(gp.subtract(lambdafxp));
		f1 = f1.normalizeMultivariate();
		absFaktoren.addElement(f1);
		System.out.println("Absolute Faktorengruppe: "+f1);
*/
		// 5b
		
		QPolynomial fxtotphiofgoverfx = new QPolynomial();
		for (int j=0; j<=phi.deg; j++)
			fxtotphiofgoverfx = fxtotphiofgoverfx.add(fx.pow(phi.deg-j).multiply(g.pow(j)).multiply(new QPolynomial(new Qelement(phi.get(j)))));
		QPolynomial fac = f0.gcd(fxtotphiofgoverfx);
		ergv.addElement(fac);
		f0 = f0.divide(fac);
	}

	// Schritt 6
	
	QPolynomial[] erg = new QPolynomial[ergv.size()];
	for (int i=0; i<ergv.size(); i++) erg[i] = (QPolynomial)ergv.elementAt(i);
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (06.01.2003 19:49:15)
 * @return arithmetik.QPolynomial[]
 */
public QPolynomial[] factorize() 
{
	final int MULTIVARIATESUBSTITUEBORDER = 5;

	int[] inds = getAllIndizes();
	if (inds.length==0) return new QPolynomial[]{new QPolynomial(this)};
	if (inds.length==1) return factorUnivariate();
	if (inds.length==2) return factorBivariate(inds[0],inds[1]);

	Object[][] zwerg = factorMultivariate(MULTIVARIATESUBSTITUEBORDER);
	int anzFak = 0;
	for (int i=0; i<zwerg[0].length; i++)
		anzFak += ((Integer)zwerg[1][i]).intValue();

	QPolynomial[] erg = new QPolynomial[anzFak];
	int k = 0;
	for (int i=0; i<zwerg[0].length; i++)
	{
		int a = ((Integer)zwerg[1][i]).intValue();
		for (int j=0; j<a; j++)
			erg[k++] = (QPolynomial)zwerg[0][i];
	}
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (08.01.2003 10:53:47)
 * @return arithmetik.RemainderRingPolynomial[]
 * @param mod long

	nimmt ein univariates Polynom und faktorisiert es bezüglich einem Modulus. Haben einige der Koeffizienten
	einen Nenner ungleich 1, stimmt der Inhalt des Ergebnispolynoms nicht mehr (um Division durch 0 zu vermeiden).
 
 */
public RemainderRingPolynomial[] factorizeModulo(long mod) 
{
	BigInteger a = BigInteger.ONE;
	for (int i=0; i<monom.size(); i++)
	{
		QMonomial mon = (QMonomial)monom.elementAt(i);
		BigInteger z = mon.factor.n;
		a = z.multiply(a.divide(z.gcd(a)));
	}
	
	Modulus m = new Modulus(BigInteger.valueOf(mod));

	RemainderRing nenner = new RemainderRing(a,m);
//	RemainderRingPolynomial nenPol = new RemainderRingPolynomial(nenner.reciprocal());
	RemainderRingPolynomial toFactorize = new RemainderRingPolynomial(multiply(new Qelement(a)).toUnivariatePolynomial(),m);
//	toFactorize = toFactorize.multiply(nenPol);
	
	Stack s = toFactorize.factorize();

	RemainderRingPolynomial[] erg = new RemainderRingPolynomial[s.size()];
	int i = 0;
	while (!s.empty()) erg[i++] = (RemainderRingPolynomial)s.pop();
	
	return erg;	
}
/**

	von CK

 * Insert the method's description here.
 * Creation date: (05.01.2003 18:11:06)
 * @return java.lang.Object[][]
 * @param r int
 */
public Object[][] factorMultivariate(int r) 
{
	Object[][] erg = null;
	int r1 = r;
	while (erg == null)
	{
		erg = f_factorMultivariate(r1);
		r1++;
		if (erg == null)
		{
			System.out.println("Unlucky Substitution in factorMultivariate()");
			System.out.println("Trying new one from {" + Math.pow(2,r1) + ",...," + -Math.pow(2,r1) + "}");
		}
	}
	return erg;
}
/**
 * Faktorisiert die Polynome so weit wir irgend möglich
 * über paarweisen ggT und finden von multiple Faktoren. Werden 2 gleich Faktoren innerhalb einer
 * Gleichnung gefunden, wird einer eliminiert. Werden 2 gleiche Faktoren über 2 Gleichungen gefunden,
 * werden sie danach auch auf den selben Pointer zeigen. Am Input-Array und an den Vectoren wird
 * rumgebastelt, an den Polynomen selber aber nicht.
 * Creation date: (16.06.2002 09:01:30)
 * @return arithmetik.RQuotientExp[]
 * @param equation java.util.Vector[]
 * @param variables int[]
 */
public static void factorSystemSimple(Vector[] equation) 
{
	for (int zeile = 0; zeile < equation.length; zeile++)
	{
		int i=0;
		while (i<equation[zeile].size())
		{
			boolean skiprest = false;								// Wenn was mit einem Faktor gemacht wird, Rest ignorieren
			
			// erstmal Content rausholen.
			QPolynomial p = (QPolynomial)equation[zeile].elementAt(i);
			int max = p.getHighestIndex();
			QPolynomial c = p.getContent(max);
			if (!c.isUnit())
			{
				equation[zeile].setElementAt(c, i);
				equation[zeile].addElement(p.divide(c));
				skiprest = true;
			}
			// Falls kein Content, nach Quadratfaktoren in der Variable suchen
			if (!skiprest)
			{
				QPolynomial g = p.gcd(p.derive(max));
				if (!g.isUnit()) {equation[zeile].setElementAt(p.divide(g), i); skiprest = true;}
			}
			// Durch den Rest der Zeile durchgehen und eventuelle gcds bearbeiten (doppelte verwerfen)
			for (int j=i+1; (j<equation[zeile].size()) && (!skiprest); j++)
			{
				QPolynomial q = (QPolynomial)equation[zeile].elementAt(j);
				QPolynomial g = p.gcd(q);
				if (!g.isUnit()) 
				{
					equation[zeile].removeElementAt(j); 
					equation[zeile].setElementAt(p.divide(g),i); 
					equation[zeile].addElement(q.divide(g));
					equation[zeile].addElement(g);
					skiprest = true;
				}
			}
			// Durch die restlichen Zeilen gehen und in jeder auf gcd durchschauen
			for (int z2 = zeile+1; (z2 < equation.length) && (!skiprest); z2++)
			{
				for (int j=0; (j<equation[z2].size()) && (!skiprest); j++)
				{
					QPolynomial q = (QPolynomial)equation[z2].elementAt(j);
					QPolynomial g = p;
					if (p!=q) g = p.gcd(q);									// Wenn sie gleich sind, ist der ggt alles
					if (!g.isUnit())
					{
						equation[z2].setElementAt(q.divide(g),j);
						equation[z2].addElement(g);
						equation[zeile].setElementAt(p.divide(g),i);
						equation[zeile].addElement(g);
						skiprest = true;
					}
				}
			}
			if (!skiprest) i++;
		}
	}
}
/**
 * Insert the method's description here.
 * Creation date: (20.05.2002 21:36:30)
 * @return arithmetik.QPolynomial[]
 * @param index int

	Rahmenmethode für univariate Faktorisierung. this muss univariat sein, der Rest ist egal.
	Zurückgegeben wird ein Array der Faktoren, wobei gleiche Faktoren durch gleiche Pointer
	direkt hintereinander realisiert sind.
 	
 */
public QPolynomial[] factorUnivariate()
{
	int index = this.getHighestIndex();

    BigInteger a = BigInteger.ONE;
    for (int i = 0; i < monom.size(); i++)
    {
        QMonomial mon = (QMonomial) monom.elementAt(i);
        BigInteger z = mon.factor.n;
        a = z.multiply(a.divide(z.gcd(a)));
    }
    
    if (leadingFactor().signum() == -1) a = a.negate();
	QPolynomial[] sqrfree = (multiply(new Qelement(a))).squarefree(index);

    Vector erg = new Vector();
    Qelement consts = new Qelement(BigInteger.valueOf(1), a);
	for (int pnr=0; pnr<sqrfree.length; pnr++)
	{
	    UnivariatePolynomial u = sqrfree[pnr].toUnivariatePolynomial();
	    Stack s = u.factorizeSquarefreeHensel();

	    while (!s.empty())
	    {
	        QPolynomial q = ((UnivariatePolynomial) s.pop()).toQPolynomial(index);
	        if (q.isConstant())  consts = consts.multiply(q.leadingFactor().pow(pnr+1));
	        else for (int i=0; i<=pnr; i++) erg.addElement(q);				// pnr ist Häufigkeit des Ursprungsfaktors
	    }
	}

    QPolynomial[] ergA = new QPolynomial[erg.size()];
    for (int i = 0; i < ergA.length; i++)
        ergA[i] = (QPolynomial) erg.elementAt(i);

	if (ergA.length==0) return new QPolynomial[]{new QPolynomial(consts)};
	else ergA[0] = ergA[0].multiply(consts);
        
    return ergA;
}
	/**

		von CK
	
	 * filter(i,pot) liefert alle Monome, fuer die deg_i=pot gilt...
	*/
	public QPolynomial filter(int i,int pot)
	{
		QPolynomial erg = new QPolynomial();
		for (int j = 0 ; j < monom.size() ; j++)
		{
			QMonomial tmp = (QMonomial)monom.elementAt(j);
			if (tmp.getExponent(i) == pot) erg.add(new QPolynomial(tmp));
		}
		return erg;
	}
/**
 * Insert the method's description here.
 * Creation date: (18.10.2002 08:55:22)
 * @return arithmetik.QPolynomial
 */
 
public QPolynomial[] findCombinationPolynomials(int index, Printable out) 
{
	int d1 = getDegreeIn(index);
	int d2 = d1*(d1-1)/2;

	QPolynomial[] sigma = new QPolynomial[d1+1], s = new QPolynomial[2*d2+1], beta1 = new QPolynomial[2*d2+1], 
				  beta2 = new QPolynomial[d2+1], gamma1 = new QPolynomial[2*d2+1], gamma2 = new QPolynomial[d2+1];
				  
	for (int i=1; i<=d1; i++) sigma[i] = getCoefficient(index,d1-i);

	for (int i=1; i<=d1; i++) 
	{
		s[i] = new QPolynomial();
		for (int j=1; j<i; j++)
			if (j%2!=0) s[i] = s[i].add     (sigma[j].multiply(s[i-j]));
			else        s[i] = s[i].subtract(sigma[j].multiply(s[i-j]));
		if (i%2!=0) s[i] = s[i].add     (sigma[i].multiply(new QPolynomial(new Qelement(i))));
		else        s[i] = s[i].subtract(sigma[i].multiply(new QPolynomial(new Qelement(i))));

		if (out!=null) out.println("s["+i+"] = "+s[i]);
	}
	for (int i=d1+1; i<=2*d2; i++) 
	{
		s[i] = new QPolynomial();
		for (int j=1; j<=d1; j++)
			if (j%2!=0) s[i] = s[i].add     (sigma[j].multiply(s[i-j]));
			else        s[i] = s[i].subtract(sigma[j].multiply(s[i-j]));

		if (out != null) out.println("s["+i+"] = "+s[i]);
	}
	for (int i=1; i<=d2; i++)
	{
		beta1[2*i-1] = new QPolynomial();
		beta1[2*i] = new QPolynomial();
		for (int j=1; j<2*i; j++)
			if (j % 2 == 1)	beta1[2*i] = beta1[2*i].subtract(s[j].multiply(s[2*i-j]).multiply(new QPolynomial(new Qelement(Statik.binomialCoefficientBig(2*i,j)))));
			else            beta1[2*i] = beta1[2*i].add(s[j].multiply(s[2*i-j]).multiply(new QPolynomial(new Qelement(Statik.binomialCoefficientBig(2*i,j)))));
		beta1[2*i] = beta1[2*i].add( (new QPolynomial(new Qelement(2*d1))).multiply(s[2*i]));
		
		beta2[i] = new QPolynomial();
		for (int j=1; j<i; j++)
			beta2[i] = beta2[i].add(s[j].multiply(s[i-j]).multiply(new QPolynomial(new Qelement(Statik.binomialCoefficientBig(i,j)))));
		beta2[i] = beta2[i].add( (new QPolynomial((new Qelement(2*d1)).subtract(Qelement.TWO.pow(i)))).multiply(s[i]));
		beta2[i] = beta2[i].multiply(new QPolynomial(Qelement.HALF));

		if (out != null)
		{
			out.println("beta1["+(2*i-1)+"] ="+ beta1[2*i-1]);
			out.println("beta1["+2*i+"] ="+ beta1[2*i]);
			out.println("beta2["+i+"] ="+ beta2[i]);
		}
	}
	QPolynomial addPolynom = (new QPolynomial(index)).pow(d2);
	QPolynomial subPolynom = (new QPolynomial(index)).pow(d2);
	for (int i=1; i<=d2; i++)
	{
		gamma1[2*i - 1] = new QPolynomial();
		gamma1[2*i] = new QPolynomial();
		for (int j=0; j<2*i-2; j++)
			gamma1[2*i] = gamma1[2*i].add(gamma1[2*i-1-j].multiply(beta1[1+j]));

		gamma1[2*i] = gamma1[2*i].add (beta1[2*i]);

		gamma1[2*i] = gamma1[2*i].multiply(new QPolynomial(new Qelement(-1,2*i)));

		subPolynom = subPolynom.add( (new QPolynomial(index)).pow(d2-i).multiply(gamma1[2*i]) );
		
		gamma2[i] = new QPolynomial();
		for (int j=0; j<i-1; j++)
		{
			if (j%2==0) gamma2[i] = gamma2[i].add     (gamma2[i-1-j].multiply(beta2[1+j]));
			else		gamma2[i] = gamma2[i].subtract(gamma2[i-1-j].multiply(beta2[1+j]));
		}
		if (i%2!=0) gamma2[i] = gamma2[i].add     (beta2[i]);
		else        gamma2[i] = gamma2[i].subtract(beta2[i]);

		gamma2[i] = gamma2[i].multiply(new QPolynomial(new Qelement(1,i)));
		
		addPolynom = addPolynom.add( (new QPolynomial(index)).pow(d2-i).multiply(gamma2[i]) );

		if (out!=null)
		{
			out.println("gamma1["+(2*i-1)+"] = "+gamma1[2*i-1]);
			out.println("gamma1["+2*i+"] = "+gamma1[2*i]);
			out.println("gamma2["+i+"] = "+gamma2[i]);
		}
	}
		
//		erg = erg.add( ((new QPolynomial(index)).pow(d2-i)).multiply(gk1[i]).divide(
//				new QPolynomial(new Qelement(Statik.faculty(i)))));
	
	// Dieser Teil für das symbolische 8er-Polynom:
	// Wir erstellen ein array: 2 arrays mit je d2 arrays von Tupeln der Form [exp2,...,expInd,factor]
	// für jedes Monom von gamma.
/*	
	int ind = getHighestIndex();
	String rep = "{{";

	for (int i=1; i<=d2; i++)
	{
		rep += "{";
		for (int j=0; j<gamma1[2*i].monom.size(); j++)	
		{
			rep += "{";
			QMonomial mon = (QMonomial)gamma1[2*i].monom.elementAt(j);
			rep += mon.getExponent(2);
			for (int k=3; k<=ind; k++) rep += ","+mon.getExponent(k);
			rep += ","+mon.factor+"}";
			if (j<gamma1[2*i].monom.size()-1) rep += ",";
		}
		rep += "}";
		if (i< d2) rep += ",";
	}
	rep += "},{";
	for (int i=1; i<=d2; i++)
	{
		rep += "{";
		for (int j=0; j<gamma2[i].monom.size(); j++)	
		{
			rep += "{";
			QMonomial mon = (QMonomial)gamma2[i].monom.elementAt(j);
			rep += mon.getExponent(2);
			for (int k=3; k<=ind; k++) rep += ","+mon.getExponent(k);
			rep += ","+mon.factor+"}";
			if (j<gamma2[i].monom.size()-1) rep += ",";
		}
		rep += "}";
		if (i< d2) rep += ",";
	}
	rep += "}}";

	if (out!=null) out.println(rep);
*/	
	return new QPolynomial[]{subPolynom,addPolynom};
}
/**
 * Insert the method's description here.
 * Creation date: (09.01.2003 19:41:19)
 * @return arithmetik.QPolynomial[]
 * @param index int
 */
private QPolynomial[] findCombinationPolynomialsWithDeg8(int index) 
{
	return null;
}
/**
 * Sucht doppelte Faktoren in dem Polynom, eliminiert diese und schreibt sie (einfach) in das 2. FastPolynomial.
 * in jedem Fall ist this = erg[0]*erg[1]^2.
 * Creation date: (18.06.2002 08:47:39)
 * @return arithmetik.FastPolynomial[]
 */
public FastPolynomial[] findDoubleFactors() 
{
	int index = getHighestIndex();
	QPolynomial content = this.getContent(index);
	FastPolynomial[] erg = {new FastPolynomial(Qelement.ONE),new FastPolynomial(Qelement.ONE)};
	if (!content.isUnit()) erg = content.findDoubleFactors();
	Vector multipleFaktoren = new Vector();			// enthält einfach Faktoren, dann doppelt, dann dreifache...
	
	QPolynomial work = this.divide(content);
	QPolynomial multiple = work.gcd(work.derive(index));
	multipleFaktoren.addElement(work.divide(multiple));
	work = multiple;
	
	while (!multiple.isUnit())
	{
		multiple = work.gcd(work.derive(index));
		multipleFaktoren.addElement(work.divide(multiple));
		work = multiple;
	}
	for (int i=multipleFaktoren.size()-1; i>=0; i--)
	{
		QPolynomial p = (QPolynomial)multipleFaktoren.elementAt(i);
		if (i<multipleFaktoren.size()-1) p = p.divide((QPolynomial)multipleFaktoren.elementAt(i+1));

		if ((i%2)==0) erg[0] = erg[0].multiply(new FastPolynomial(p));
		for (int j=1; j<=i; j+=2) erg[1] =erg[1].multiply(new FastPolynomial(p));
	}		
	return erg;
}
/**
 * Erwartet ein univariates Polynom vom Grad <= 3 und sucht nach einer rationalen Nullstelle mit Cardanos Formel.
 * Creation date: (14.07.2002 18:17:28)
 */
public Vector findRationalZeros3() 
{
	int ix = getHighestIndex();
	QPolynomial work = makeCoefficientInteger();
	Qelement qe = work.getCoefficient(ix, 3).leadingFactor();
	for (int i=0; i<work.monom.size(); i++)
	{
		QMonomial mon = (QMonomial)work.monom.elementAt(i);
		mon.factor = mon.factor.multiply(qe.pow(2-mon.getExponent(ix)));
	}

	if (getDegreeIn(ix)==1) {Vector erg = new Vector(); erg.addElement(work.getCoefficient(ix,0).leadingFactor().divide(qe)); return erg;}
	if (getDegreeIn(ix)==2)
	{
		double p = work.getCoefficient(ix,1).leadingFactor().doubleValue();
		double q = work.getCoefficient(ix,0).leadingFactor().doubleValue();
		Qelement e1 = new Qelement((int)Math.round( (p/2.0)+Math.sqrt( Math.pow((p/2.0),2) - q )));
		if (evaluate(ix, e1).isZero())
		{
			Qelement e2 = new Qelement((int)Math.round( (p/2.0)+Math.sqrt( Math.pow((p/2.0),2) - q )));
			Vector erg = new Vector(); erg.addElement(e1); erg.addElement(e2); return erg;
		} 
		return new Vector();	
	}	

	double[] erg = new double[3]; int num = 0;
    double A = (work.getCoefficient(ix, 2)).leadingFactor().doubleValue();
    double B = (work.getCoefficient(ix, 1)).leadingFactor().doubleValue();
    double C = (work.getCoefficient(ix, 0)).leadingFactor().doubleValue();
	
	    /*  substitute x = y - A/3 to eliminate quadric term:
	    x^3 + 3px + 2q = 0 */
	
	double p = (1.0/3.0) * ((- 1.0/3.0) * A*A + B);
	double q = (1.0/2.0) * ((2.0/27.0) * A * A * A - (1.0/3.0) * A * B + C);
	
	/* use Cardano's formula */
	
	double cb_p = p * p * p;
	double D = q * q + cb_p;
	
	if ((-0.1<D) && (D<0.1)) 
	{
		if ((-0.1<q) && (q<0.1)) /* one triple solution */
		{
			erg[ 0 ] = 0;
			num = 1;
		} else /* one single and one double solution */ {
	        double u = Statik.cbrt(-q);
			erg[ 0 ] = 2 * u;
			erg[ 1 ] = - u;
			num = 2;
		}
	} else if (D < 0) /* Casus irreducibilis: three real solutions */
	{
		double phi = 1.0/3 * Math.acos(-q / Math.sqrt(-cb_p));
		double t = 2 * Math.sqrt(-p);
	
		erg[ 0 ] =   t * Math.cos(phi);
		erg[ 1 ] = - t * Math.cos(phi + Math.PI / 3);
		erg[ 2 ] = - t * Math.cos(phi - Math.PI / 3);
		num = 3;
	} else /* one real solution */ {
		double sqrt_D = Math.sqrt(D);
		double u = Statik.cbrt(sqrt_D - q);
		double v = - Statik.cbrt(sqrt_D + q);
	
		erg[ 0 ] = u + v;
		num = 1;
	}
	
    /* resubstitute */
	
    double sub = (1.0/3.0) * A;

    Vector qerg = new Vector();
	for (int i = 0; i < num; ++i)
	{
		erg[ i ] -= sub;
		Qelement e = new Qelement((int)Math.round(erg[i]));
		e = e.divide(qe);
		QPolynomial c = evaluate(ix, e);
		if (c.isZero()) qerg.addElement(e);
	}
	return qerg;
}
/**
 * Insert the method's description here.
 * Creation date: (08.01.2003 18:51:08)
 * @return arithmetik.RExpression[]

	Findet alle durch Quadratwurzeln ausdrückbaren Nullstellen von this.
	Liefert die Ergebnisse als RQuotientExp, und zwar alle Ergebnisse, aber jeweils nur eine
	Konjugierte und Darstellungsform (d.h. für jeden über dem Grundkörper irreduziblen Faktor 
	genau ein Ergebnis). Die Ergebnisse haben im Zähler die Form 
	a + b * sqrt(c) + d * sqrt(e + f * sqrt(c)) + ...
	und im Nenner ein QPolynomial.
 
 */
public RQuotientExp[] findSquarerootZeros(int index) 
{	
	int deg = getDegreeIn(index);
	if (deg <=2 ) 
	{
		RQuotientExp lsg = findSquarerootZerosWithDegTwo(index);
		if (lsg == null) return new RQuotientExp[0];
		return new RQuotientExp[]{lsg};
	}
	if (isInAllExponentsEven(index)) return findSquarerootZerosWithEvenExponents(index);
	
	QPolynomial[] faktor = factorize();

	Vector ergV = new Vector();
	for (int i=0; i<faktor.length; i++)
	{
		deg = faktor[i].getDegreeIn(index);
		RExpression zwerg = null;
		boolean notSolvable = false;
		boolean alreadySolved = false;
		if (deg <=2) 
		{
			RQuotientExp z = faktor[i].findSquarerootZerosWithDegTwo(index);
			if (z==null) notSolvable = true;
			else {ergV.addElement(z); alreadySolved = true;}
		}
		if ((!alreadySolved) && (!notSolvable) && (faktor[i].isInAllExponentsEven(index)))
		{
			RQuotientExp[] lsg = faktor[i].findSquarerootZerosWithEvenExponents(index);
			if (lsg.length==0) notSolvable = true;
			else {ergV.addElement(lsg[0].zaehler); alreadySolved = true;}
		}

		int k = 4;
		while (k < deg) k *= 2;							// Grad Zweierpotenz?
		
		if ((!notSolvable) && (!alreadySolved) && (k==deg) && (faktor[i].isProbablySquarerootsolvable(index)))		// Schneller Test?
		{
			QPolynomial nenner = QPolynomial.ONE;
			QPolynomial work = faktor[i];
			QPolynomial leitkoeffizient = faktor[i].getLeadingCoefficient(index).normalize();
			if (!leitkoeffizient.isConstant())
			{
				int ix = leitkoeffizient.getHighestIndex();
				nenner = leitkoeffizient.divide(leitkoeffizient.gcd(leitkoeffizient.derive(ix)));
				while (nenner.getDegreeIn(ix)/leitkoeffizient.getDegreeIn(ix)<deg) nenner = nenner.sqr();
				work = QPolynomial.ZERO;
				QPolynomial nennerPower = QPolynomial.ONE;
				for (int j=0; (!notSolvable) && (j<=deg); j++)
				{
					QPolynomial[] divAndRem = faktor[i].getCoefficient(index,j).divideAndRemainder(nennerPower);
					if (!divAndRem[1].isZero()) notSolvable = true;
					work = work.add(divAndRem[0]);
					nennerPower = nennerPower.multiply(nenner);
				}
			}

			QPolynomial faktorOhneZweithoechsten = work;
			QPolynomial konstante = work.getCoefficient(index,deg-1).multiply(new Qelement(1,deg)).negate();
			if ((!notSolvable) && (!faktor[i].getCoefficient(index,deg-1).isZero()))			// zweithöchsten eliminieren
			{
				QPolynomial subst = (new QPolynomial(index)).add(konstante);
				faktorOhneZweithoechsten = work.evaluate(index, subst);
				if (faktorOhneZweithoechsten.isInAllExponentsEven(index))
				{
					RQuotientExp[] lsg = faktorOhneZweithoechsten.findSquarerootZerosWithEvenExponents(index);
					if (lsg.length==0) notSolvable = true;
					else zwerg = lsg[0].zaehler;
				}
			}
			if ((!notSolvable) && (zwerg==null) && (deg==4)) 
			{
				RQuotientExp lsg = faktorOhneZweithoechsten.findSquarerootZerosWithDegree4(index);
				if (lsg == null) notSolvable = true;
				else zwerg = lsg.zaehler;
			}
//			if ((zwerg==null) && (deg==8)) zwerg = faktor[i].findSquarerootZerosWithDegree8(index);
			if ((!notSolvable) && (zwerg == null))
			{
				QPolynomial[] addsub = work.findCombinationPolynomials(index, null);
				QPolynomial[] faksub = addsub[0].factorize();
				QPolynomial[] fakadd = addsub[1].factorize();
				Vector lsgadd = new Vector();
				for (int m=0; m<fakadd.length; m++)
					if (fakadd[m].getDegreeIn(index)<deg)
					{
						RQuotientExp[] l = fakadd[m].findSquarerootZeros(index);
						for (int j=0; j<l.length; j++)
						{
							RQuotientExp[] l2 = l[j].getAllConjugates();
							for (k=0; k<l2.length; k++) lsgadd.addElement(l2[k]);
						}
					}
				Vector lsgsub = new Vector();
				for (int m=0; m<faksub.length; m++)
					if (faksub[m].getDegreeIn(index)==(deg/2))
					{
						RQuotientExp[] l = faksub[m].findSquarerootZeros(index);
						for (k=0; k<l.length; k++) lsgsub.addElement(l[k]);
					}

				boolean solved = false;
				for (int j=0; (!solved) && (j<lsgadd.size()); j++)
					for (k=0; (!solved) && (k<lsgsub.size()); k++)
					{
						RQuotientExp kan = ((RQuotientExp)lsgadd.elementAt(j)).add(((RQuotientExp)lsgsub.elementAt(k)).sqrt());
						kan = kan.multiply(new RQuotientExp(new QPolynomial(Qelement.HALF)));
						if (work.evaluate(index,kan).isZero()) {solved = true; ergV.addElement(kan);}
					}
			}
			if ((!notSolvable) && (zwerg != null))
			{
				zwerg = zwerg.add(new RExpression(konstante));
				ergV.addElement(new RQuotientExp(zwerg,new RExpression(nenner)));
			}				
		}		
	}
	RQuotientExp[] erg = new RQuotientExp[ergV.size()];
	for (int i=0; i<erg.length; i++) erg[i] = (RQuotientExp)ergV.elementAt(i);
	
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (08.01.2003 20:07:52)
 * @return arithmetik.RQuotientExp[]
 * @param index int

	Liefert entweder null oder eine durch Quadratwurzeln ausdrückbaren Nullstellen von this, 
	wenn this in index Grad 4 hat und irreduzibel ist.
	Liefert die Ergebnisse als RQuotientExp. Das Ergebniss hat im Zähler die Form 
	a + b * sqrt(c) + d * sqrt(e + f * sqrt(c)) + ...
	und im Nenner ein QPolynomial.
 
 */
private RQuotientExp findSquarerootZerosWithDegree4(int index)
{
	QPolynomial[] koeff = new QPolynomial[5];
	for (int i=0; i<=4; i++) koeff[i] = getCoefficient(index, i);

	// Nenner eliminieren
	for (int i=0; i<=2; i++) koeff[i] = koeff[i].multiply(koeff[4].pow(3-i));
	// Konstante eliminieren
	koeff[2] = ((new QPolynomial(new Qelement(-6))).multiply(koeff[3].pow(2))).add(koeff[2]);
	koeff[1] = ((new QPolynomial(new Qelement(8))).multiply(koeff[3].pow(3))).subtract((new QPolynomial(new Qelement(2))).multiply(koeff[2]).multiply(koeff[3])).add(koeff[1]);
	koeff[0] = ((new QPolynomial(new Qelement(-3))).multiply(koeff[3].pow(4))).add(koeff[2].multiply(koeff[3].pow(2))).add(koeff[0]);
	
	// Das feste Polynom zur Bestimmung von b in sqrt(a)+sqrt(b+c*sqrt(a)) wird zusammengesetzt.
	// Dieses Polynom ist 64 b^3 + 64p b^2 + (20p^2 - 16 r) b + (2p^3+q^2-8rp)

	// Bestimmungspolynom aus der Diss: -4 k1 X^3 + (k2^2 - k0^2) X^2 - 8 k1 k2 X + 16 k1
	// Elimination des Nenners ergibt: X^3 - (1/4) (k2^2 - k0^2) X^2 + 2 k1^2 k2 X - 4 k1^3       und Nullstellen durch k1

	// Bestimmungspolynom aus der Diss nach Korrektur: k1 X^3 + (k2^2 - 4*k0) X^2 - 2 k1 k2 X + k1^2
	// Elimination des Nenners ergibt: X^3 + (k2^2 - 4*k0) X^2 - 2 k1^2 k2 X + k1^4       und Nullstellen durch k1

	int adnr = getHighestIndex()+1;
	QPolynomial ad = new QPolynomial(adnr);
	
	QPolynomial adbestimm = ad.pow(3);
	adbestimm = adbestimm.add(ad.pow(2).multiply(koeff[2].sqr().subtract(koeff[0].multiply(new Qelement(4)))));
	adbestimm = adbestimm.subtract(ad.pow(1).multiply(Qelement.TWO).multiply(koeff[1].sqr().multiply(koeff[2])));
	adbestimm = adbestimm.add(ad.pow(0).multiply(koeff[1].pow(4)));
	
	Vector adlsg = adbestimm.getLinearZeros(adnr);			// enthält RQuotientExp mit 1 als Nenner

	if (adlsg.size() == 0) return null;

	// Einfacher als in der Diss (besonders ohne die Quadratwurzel) nehmen wir
	// d = x (da das Vorzeichen vor d und a gemeinsam vertauscht werden kann), 
	// b = k1 / 4x , c = -k2 / 2 - b , a = 1
	// Die Nullstellen muss noch durch k1 geteilt werden.
	// Die Lösung ist dann sqrt(b) + sqrt(c + d * sqrt(b));
	Vector ergV = new Vector();
	for (int i=0; i<adlsg.size(); i++)
	{
		QPolynomial d = ((RQuotientExp)adlsg.elementAt(i)).zaehler.toQPolynomial().divide(koeff[1]);
		QPolynomial b = koeff[1].negate().divide(d.multiply(new Qelement(4)));
		QPolynomial c = koeff[2].negate().divide(new QPolynomial(Qelement.TWO)).subtract(b);

		RExpression sqrtb = (new RExpression(b)).sqrt();
		RExpression lsg = sqrtb.add( (new RExpression(c)).add((new RExpression(d)).multiply(sqrtb)).sqrt() );
		
		lsg = lsg.add(new RExpression(koeff[3]));
		RQuotientExp lsgq = new RQuotientExp(lsg, new RExpression(koeff[4]));
		if (evaluate(index, lsg).isZero()) return lsgq;
	}
	return null;
}
/**
 * Insert the method's description here.
 * Creation date: (08.01.2003 19:02:27)
 * @return arithmetik.RExpression
 * @param index int

 	Liefert null, falls this constant und ungleich 0 ist, und sonst die durch Quadratwurzeln 
 	ausdrückbaren Nullstellen von this, 
	wenn this in index Grad 2,1,0 oder -1 hat und irreduzibel ist.
	Liefert die Ergebnisse als RQuotientExp. Das Ergebniss hat im Zähler die Form 
	a + b * sqrt(c)
	und im Nenner ein QPolynomial.
 	
 */
private RQuotientExp findSquarerootZerosWithDegTwo(int index) 
{
	if (isZero()) return RQuotientExp.ZERO;

	QPolynomial a = getCoefficient(index,0);
	QPolynomial b = getCoefficient(index,1);
	QPolynomial c = getCoefficient(index,2);

	if (c.isZero())
	{
		if (b.isZero()) return null;
		return new RQuotientExp(new RExpression(a.negate()),new RExpression(b));
	}

	RExpression p = new RExpression(b.multiply(new Qelement(1,2)));
	RExpression q = new RExpression(c.multiply(a));
	RExpression w = (p.sqr().subtract(q)).sqrt();
	RExpression l = p.negate().add(w);
	RQuotientExp erg = new RQuotientExp(l,new RExpression(c));
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (08.01.2003 18:38:24)
 * @param index int

 	Erwartet ein Polynom, dass in Xindex nur gerade Exponenten hat. Alle X^2 werden durch X substituiert,
 	und solve wird aufgerufen; um alle Ergebnisse wird eine Wurzel gesetzt.

	Aufgrund der Normalform muss der Nenner von findSquarerootZeros (laut Spezifikation ein QPolynomial)
	ein Quadrat sein. Ansonsten wird die zugehörige Lösung gekillt.
 */
private RQuotientExp[] findSquarerootZerosWithEvenExponents(int index) 
{
	QPolynomial p = new QPolynomial(this);
	for (int i=0; i<monom.size(); i++)
	{
		QMonomial m = (QMonomial)p.monom.elementAt(i);
		m.setExponent(index, m.getExponent(index)/2);
	}
	RQuotientExp[] solveErg = p.findSquarerootZeros(index);
	Vector ergV = new Vector();

	for (int i=0; i<solveErg.length; i++)
	{
		QPolynomial nen = solveErg[i].nenner.toQPolynomial();
		QPolynomial sqrt = nen.sqrt();
		if (sqrt != null)
		{
			Qelement f = sqrt.leadingFactor();
			ergV.addElement(new RQuotientExp(solveErg[i].zaehler.multiply(new RExpression(f.reciprocal())).sqrt(),
									         new RExpression(sqrt)));
		}
	}
	RQuotientExp[] erg = new RQuotientExp[ergV.size()];
	for (int i=0; i<erg.length; i++) erg[i] = (RQuotientExp)ergV.elementAt(i);
	return erg;		
}
   public static QPolynomial fromArray(long m[][])
    {
		QPolynomial ergebnis = new QPolynomial();
		for(int i=0;i<m.length;i++)
        {
			int[] n = new int[m[i].length-1];
            for(int j=1;j<m[i].length;j++)
              n[j-1]=(int)m[i][j];
            QMonomial mon = new QMonomial(new Qelement(m[i][0]),n);
			ergebnis = ergebnis.add(new QPolynomial(mon));
        }
		return ergebnis.unifiziereKonstanten();
    }
/**
 * Insert the method's description here.
 * Creation date: (06.01.2003 11:10:43)
 * @return arithmetik.QPolynomial
 * @param in java.lang.String
 */
private static QPolynomial fromString(String in) 
{
	final int NIX = 0, PLUS = 5, MINUS = 4, MAL = 3, GETEILT = 2, HOCH = 1;
	String s = Statik.loescheRandWhitespaces(in);
	boolean klammerAussen = ((s.charAt(0)=='(') && (s.charAt(s.length()-1)==')'));
	int klammertiefe = 0, pos = 0, nPos = -1, naechstesZeichen = NIX;
	while (pos < s.length())
	{
		if (s.charAt(pos)=='(') klammertiefe++;
		if (s.charAt(pos)==')') klammertiefe--;
		if (klammertiefe==0)
		{
			if ((naechstesZeichen <= HOCH) && (s.charAt(pos)=='^')) {naechstesZeichen = HOCH; nPos = pos;}
			if ((naechstesZeichen <= GETEILT) && (s.charAt(pos)=='/')) {naechstesZeichen = GETEILT; nPos = pos;}
			if ((naechstesZeichen <= MAL) && (s.charAt(pos)=='*')) {naechstesZeichen = MAL; nPos = pos;}
			if ((naechstesZeichen <= MINUS) && (s.charAt(pos)=='-')) {naechstesZeichen = MINUS; nPos = pos;}
			if ((naechstesZeichen <= PLUS) && (s.charAt(pos)=='+')) {naechstesZeichen = PLUS; nPos = pos; pos = s.length()+2;}
		}
		pos++;
	}
	if (naechstesZeichen==NIX)
	{
		if (klammerAussen) return fromString(s.substring(1,s.length()-1));
		if (s.charAt(0)=='X')
		{
			int nr = 0;
			if (s.length()>1)
			{
				try {
					nr = Integer.parseInt(s.substring(1));
				} catch (Exception e) {throw new RuntimeException("String constructor for QPolynom had noninteger variable number: "+s);}
			}
			return new QPolynomial(nr);
		} else {
			BigInteger l = BigInteger.valueOf(0);
			try {
				l = new BigInteger(s);
			} catch (Exception e) {throw new RuntimeException("String constructor for QPolynom had noninteger number value: "+s);}
			return new QPolynomial(new Qelement(l));
		}
	}
	if (nPos==0)
	{
		if (naechstesZeichen==MINUS) return fromString(s.substring(1)).negate();
		if (naechstesZeichen==PLUS)  return fromString(s.substring(1));
		throw new RuntimeException("String constructor for QPolynomial started with illegal sign: "+s);
	}
	String s1 = Statik.loescheRandWhitespaces(s.substring(0,nPos)), 
		   s2 = Statik.loescheRandWhitespaces(s.substring(nPos+1));
	if (naechstesZeichen==HOCH)
	{
		long exp = 0;
		try {
			exp = Long.parseLong(s2);
		} catch (Exception e) {throw new RuntimeException("String constructor for QPolynom had noninteger exponent: "+s);}
		return fromString(s1).pow(exp);
	}
	QPolynomial p1 = fromString(s1), p2 = fromString(s2);
	if (naechstesZeichen==GETEILT) return p1.divide(p2);
	if (naechstesZeichen==MAL) return p1.multiply(p2);
	if (naechstesZeichen==MINUS) return p1.subtract(p2);
	if (naechstesZeichen==PLUS) return p1.add(p2);
	return null;
}
/**
 * Insert the method's description here.
 * Creation date: (16.10.2002 18:15:24)
 * @return arithmetik.QPolynomial
 * @param v java.util.Vector
 */
public static QPolynomial gaertnerPolynomial(int[] v) 
{
	if (v.length==1) return new QPolynomial(v[0]);

	int[] vsort = new int[v.length];
	for (int i=0; i<v.length; i++) vsort[i] = v[i];
	Arrays.sort(vsort);
	HashableArrays hv = new HashableArrays(vsort);

	if (gaertnerPolynomialTable.containsKey(hv)) return ((QPolynomial)gaertnerPolynomialTable.get(hv)).unifiziereKonstanten();

	int nk = vsort[vsort.length-1];

	int[] w = new int[vsort.length-1];
	String s = "gaertner (";
	for (int i=0; i<vsort.length-1; i++) 
	{
		w[i] = vsort[i];
		s += vsort[i]+",";
	}
	s += vsort[vsort.length-1]+")";
	
	QPolynomial erg = (new QPolynomial(nk)).multiply(gaertnerPolynomial(w));
	for (int i=0; i<w.length; i++)
	{
		if (i>0) w[i-1] -= nk;
		w[i] += nk;
		erg = erg.subtract(gaertnerPolynomial(w));
	}
	gaertnerPolynomialTable.put(hv,erg);
	System.out.println(s+" = "+erg);
	return erg.unifiziereKonstanten();
}
/**
 * Insert the method's description here.
 * Creation date: (21.02.2003 10:01:37)
 * @return arithmetik.QPolynomial
 * @param arg arithmetik.QPolynomial[]

	Berechnet den Größten Gemeinsamen Teiler eines Arrays aus Polynomen.
	Der Algorithmus ist probabilistisch: Er erzeugt eine Linearkombination des zweiten bis letzten Arguments
	über ganzen Zahlen, jeweils mit einem Faktor zwischen 1 und 1000. Dann wird
	der gcd des ersten Arguments und dieses Linearfaktors genommen und getestet.
 
 */
public static QPolynomial gcd(QPolynomial[] arg) 
{
	if (arg.length==0) return ONE;
	Vector nnarg = new Vector(); for (int i=0; i<arg.length; i++) if (!arg[i].isZero()) nnarg.addElement(arg[i]);
	
	if (nnarg.size()==0) return ONE;	
	if (nnarg.size()==1) return ((QPolynomial)nnarg.elementAt(0)).normalize();
	QPolynomial zw = (QPolynomial)nnarg.elementAt(1);
	for (int i=2; i<nnarg.size(); i++)
		zw = zw.add(((QPolynomial)nnarg.elementAt(i)).multiply(new Qelement((int)Math.round(Math.random()*1000+1))));

	QPolynomial probGgt = ((QPolynomial)nnarg.elementAt(0)).gcd(zw);
	if (probGgt.isUnit()) return ONE;
	if (probGgt.isZero()) return ZERO;
	for (int i=1; i<nnarg.size(); i++)
		if (!((QPolynomial)nnarg.elementAt(i)).isDivisibleBy(probGgt)) return gcd(arg);			// Im Fehlerfall einfach noch mal.
	
	return probGgt;	
}
/**
 * Insert the method's description here.
 * Creation date: (21.02.2003 09:02:23)
 * @return arithmetik.QPolynomial
 * @param arg2 arithmetik.QPolynomial

	Berechnet den ggT mit Subresultantenketten wie in
	http://triton.mathematik.tu-muenchen.de/~kaplan/ca/spock/doku_html/node74.html
	in der 2. genannten Version beschrieben. Für das etwas mysteriöse "epsilon * r" wird
	der Rest der Pseudodivision genommen.
 
 */
public QPolynomial gcd(QPolynomial arg2) 
{
	// Subresultantenketten gehen irgendwo schief. Weitergabe an klassischen Algorithmus.
	return gcd_old(arg2);
	/*
	if (isZero()) return arg2.normalize();
	if (arg2.isZero()) return this.normalize();
	if ((isConstant()) || (arg2.isConstant())) return new QPolynomial(Qelement.ONE);

	int ix = Math.max(getHighestIndex(),arg2.getHighestIndex());
	int delta = getDegreeIn(ix) - arg2.getDegreeIn(ix);
	QPolynomial f,g;
	if (delta < 1) {f = normalize(); g = arg2.normalize();} else {f = arg2.normalize(); g = normalize();}
//	if (delta < 1) {f = this; g = arg2;} else {f = arg2; g = this;}

	QPolynomial cf = f.getContent(ix), cg = g.getContent(ix);
	QPolynomial inhaltGgt = cf.gcd(cg);
	f = f.divide(cf); g = g.divide(cg);

	QPolynomial r = f.pseudoRemainder2(g,ix).normalize();
//	QPolynomial r = f.pseudoRemainder2(g,ix);
	if (r.isZero()) return g.getPrimepart(ix).multiply(inhaltGgt).normalize();

	QPolynomial l = g.getLeadingCoefficient(ix);
	QPolynomial gamma = l.gcd(f.getLeadingCoefficient(ix));
	f = g;
	g = r.divide(gamma);
	QPolynomial psi;
	if (delta > 0) psi = l.pow(delta).divide(gamma);
	else
	{
		r = f.pseudoRemainder2(g,ix).normalize();
//		r = f.pseudoRemainder2(g,ix);
		if (r.isZero()) return g.getPrimepart(ix).multiply(inhaltGgt).normalize();
		delta = f.getDegreeIn(ix) - g.getDegreeIn(ix);
		f = g;
		g = r.multiply(gamma.pow(delta)).divide(l).normalize();
//		g = r.multiply(gamma.pow(delta)).divide(l);
		l = f.getLeadingCoefficient(ix);
		psi = l.pow(delta).multiply(gamma.pow(delta-1));
	}

	while (true)
	{
		r = f.pseudoRemainder2(g,ix).normalize();
//		r = f.pseudoRemainder2(g,ix);
		if (r.isZero()) return g.getPrimepart(ix).multiply(inhaltGgt).normalize();
		int fdeg = f.getDegreeIn(ix);
		int gdeg = g.getDegreeIn(ix);
		delta = fdeg - gdeg;
		f = g;
//		g = r.divide(l.multiply(psi.pow(delta)));
		g = r.divide(l.multiply(psi.pow(delta))).normalize();
		l = f.getLeadingCoefficient(ix);
		if (delta==1) psi = l; else psi = l.pow(delta).divide(psi.pow(delta-1));
	}
	*/
}
	// euklidischer Algorithmus mit Pseudodivision
	public QPolynomial gcd_old(QPolynomial arg2)
	{
		if (isZero()) return arg2.normalize();
		if (arg2.isZero()) return this.normalize();
		if ((isConstant()) || (arg2.isConstant())) return new QPolynomial(Qelement.ONE);
		
		int index = Math.max(getHighestIndex(), arg2.getHighestIndex());
		
		QPolynomial inhalt1 = this.getContent(index);
		QPolynomial inhalt2 = arg2.getContent(index);
		QPolynomial inhaltGgt = inhalt1.gcd(inhalt2);
		// Normalisieren zur Vermeidung großer Zahlen
		QPolynomial eins = this.divide(inhalt1).normalize();
		QPolynomial zwei = arg2.divide(inhalt2).normalize();

		if (eins.getDegreeIn(index) < zwei.getDegreeIn(index)) 
		{
			QPolynomial t = eins;
			eins = zwei;
			zwei = t;
		}
		
		int g1 = eins.getDegreeIn(index), g2 = zwei.getDegreeIn(index);
		while (g2!=0)
		{
			QPolynomial l1 = eins.getLeadingCoefficient(index);
			QPolynomial l2 = zwei.getLeadingCoefficient(index);
			QPolynomial ggT = l1.gcd(l2);
			l1 = (l1.divide(ggT)).multiply( (new QPolynomial(index)).pow(g1-g2) );
			l2 = l2.divide(ggT);
			QPolynomial neu = (eins.multiply(l2)).subtract(zwei.multiply(l1));
			neu = neu.getPrimepart(index).normalize();
			int neugrad = neu.getDegreeIn(index);
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
		if (!zwei.isZero()) return inhaltGgt;
		return inhaltGgt.multiply(eins);
	}
	// Berechnet den gcd von zwei Polynomen this und g.
	QPolynomial gcd2(QPolynomial gIn)
	{
		if (isZero()) return gIn.normalize();
		if (gIn.isZero()) return this.normalize();
		if ((isConstant()) || (gIn.isConstant())) return new QPolynomial(Qelement.ONE);

		QPolynomial g = new QPolynomial(gIn);
		QPolynomial f = new QPolynomial(this);
		// Hauptvariable auswaehlen
		// Spaeter hier WAS INTELLIGENTES einfuegen!
		int i = f.getHighestIndex();
/*		System.out.println("-------------");
		System.out.println("Variable:" +i);
		System.out.println("f: " + f);
		System.out.println("g: " + g);
		System.out.println("-------------");
*/		QPolynomial fContent = f.getContent2(i);
		f = f.divide(fContent);
		QPolynomial gContent = g.getContent2(i);
		g = g.divide(gContent);
		QPolynomial gamma = (f.getLeadingCoefficient(i)).gcd2(g.getLeadingCoefficient(i));
		QPolynomial c = fContent.gcd2(gContent);
/*		System.out.println("-------------");
		System.out.println("Variable:" +i);
		System.out.println("f: " + f);
		System.out.println("g: " + g);
		System.out.println("cont(f,i) " +fContent);
		System.out.println("cont(g,i) " +gContent);
		System.out.println("C(fc,gc)  " +c);
		System.out.println("lcC(f,g)  " +gamma);
		System.out.println("-------------");
*/		// Hier wird nun das letzte Polynom in unserer PRS bestimmt...
		// Fuer unsere Subresultanten-PRS brauchen wir noch den ggT der Leitkoeffizienten
//		System.out.println("Computing Subresultant in " + i);
//		QPolynomial lkggT = (f.getLeadingCoefficient(i)).gcd2(g.getLeadingCoefficient(i));
//		System.out.println("lkggt: "+ lkggT);
//		System.out.println("f : " + f);
//		System.out.println("g : " + g);
		f = f.computeSubresultantPRS(g,i,gamma);
//		f = f.computeSimpleSPRS(g,i);
//		System.out.println("prs : " + f);
		if ( f.getDegreeIn(i)==0 )
		{
			return c;
		}
		f = (gamma.multiply(f)).divide(f.getLeadingCoefficient(i));
		f = f.divide(f.getContent2(i));
		return c.multiply(f);
	}
/**
 * Insert the method's description here.
 * Creation date: (12.01.2003 10:45:29)
 * @return arithmetik.QPolynomial[]
 * @param pol arithmetik.QPolynomial[]
 * @param index int

 
 */
public static QPolynomial[] generalizedResultant(QPolynomial[] pol, int index) 
{
	if (pol.length==0) return new QPolynomial[]{ZERO};
	if (pol.length==1) return new QPolynomial[]{pol[0]};

	QPolynomial allCommon = pol[0];
	for (int i=1; i<pol.length; i++) allCommon = allCommon.gcd(pol[i]);
	if (!allCommon.isUnit()) return new QPolynomial[]{ZERO};

	// Zusammenstellen des zweiten Polynoms zu u_1 p_1 + u_2 p_2 + ... + u_n p_n
	int maxInd = -1;
	for (int i=0; i<pol.length; i++) {int j = pol[i].getHighestIndex(); if (j>maxInd) maxInd=j;}
	QPolynomial snd = ZERO;
	for (int i=1; i<pol.length; i++)
		snd = snd.add((new QPolynomial(maxInd+i)).multiply(pol[i]));

	// Leitkoeffizienten berechnen, der wird aus res wieder rausdividert
	// ein gcd der Leitkoeffizienten hier ist nach Voraussetzung nicht in allen Polynomen enthalten
	QPolynomial gcdleit = pol[0].getLeadingCoefficient(index).gcd(snd.getLeadingCoefficient(index));
		
	QPolynomial res = (pol[0].resultant(snd, index)).expand();
	res = res.divide(gcdleit);
	Hashtable ergH = new Hashtable();

	for (int i=0; i<res.monom.size(); i++)
	{
		QMonomial m = new QMonomial((QMonomial)res.monom.elementAt(i));
		int[] degs = new int[pol.length-1];
		for (int j=0; j<degs.length; j++) {degs[j] = m.getExponent(maxInd+1+j); m.setExponent(maxInd+1+j,0);}
		Tupel t = new Tupel(degs);
		QPolynomial p = (QPolynomial)ergH.get(t);
		if (p==null) ergH.put(t,new QPolynomial(m));
		else         ergH.put(t,(new QPolynomial(m)).add(p));
	}
	QPolynomial[] erg = new QPolynomial[ergH.size()];
	Enumeration enu = ergH.elements();
	int i=0;
	while (enu.hasMoreElements()) erg[i++] = (QPolynomial)enu.nextElement();
	return erg;
}
public int[] getAllIndizes() 
{
	int[] anz = new int[getHighestIndex()+1];
	for (int i=0; i<monom.size(); i++)
	{
		QMonomial m = (QMonomial)monom.elementAt(i);
		for (int j=0; j<m.exp.length; j++)
			if (m.exp[j]>0) anz[j]++;
	}
	int gesInd = 0;
	for (int i=0; i<anz.length; i++)
		if (anz[i]>0) gesInd++;
	int[] erg = new int[gesInd];
	int j=0;
	for (int i=0; i<anz.length; i++)
		if (anz[i]>0) erg[j++] = i;
	return erg;
}
	/**
	 * Die Polynome müssen Univariat sein.
	 * Berechnet die Bezout-Koeffizienten a,b und den ggT r so, dass a*this+b*arg2 = ggT und a und b
	 * jeweils minimal vom Grad sind.
	 * Creation date: (22.05.2002 09:47:31)
	 * @return arithmetik.QPolynomial[]
	 */
	public QPolynomial[] getBezout(QPolynomial arg2)
	{
		int index = this.getHighestIndex();
		
		if (isZero()) return new QPolynomial[]{new QPolynomial(), new QPolynomial(arg2.leadingFactor().reciprocal()), arg2.normalize()};
		if (arg2.isZero()) return new QPolynomial[]{new QPolynomial(this.leadingFactor().reciprocal()), new QPolynomial(), this.normalize()};
		if (isConstant()) return new QPolynomial[]{new QPolynomial(this.leadingFactor().reciprocal()), new QPolynomial(), new QPolynomial(Qelement.ONE)};
		if (arg2.isConstant()) return new QPolynomial[]{new QPolynomial(), new QPolynomial(arg2.leadingFactor().reciprocal()), new QPolynomial(Qelement.ONE)};
		
		// Normalisieren zur Vermeidung großer Zahlen
		QPolynomial normaler1 = new QPolynomial(this.leadingFactor());
		QPolynomial normaler2 = new QPolynomial(arg2.leadingFactor());
		QPolynomial eins = this.divide(normaler1);
		QPolynomial zwei = arg2.divide(normaler2);

		QPolynomial a1 = normaler1                     ,a2 = new QPolynomial(Qelement.ZERO); 
		QPolynomial b1 = new QPolynomial(Qelement.ZERO),b2 = normaler2;
		if (eins.getDegreeIn(index) < zwei.getDegreeIn(index)) 
		{
			QPolynomial t = eins;
			eins = zwei;
			zwei = t;
			a1 = new QPolynomial(Qelement.ZERO); a2 = normaler1; 
			b1 = normaler2; b2 = new QPolynomial(Qelement.ZERO);
		}

		int g1 = eins.getDegreeIn(index), g2 = zwei.getDegreeIn(index);
		while (g2!=0)
		{
			QPolynomial[] divAndRem = eins.divideAndRemainder(zwei);

			eins = zwei;
			zwei = divAndRem[1];
			QPolynomial neua2 = a1.subtract(divAndRem[0].multiply(a2));
			QPolynomial neub2 = b1.subtract(divAndRem[0].multiply(b2));
			a1 = a2;
			b1 = b2;
			a2 = neua2;
			b2 = neub2;
			g1 = eins.getDegreeIn(index); g2 = zwei.getDegreeIn(index);
		}
		if (!zwei.isZero()) return new QPolynomial[]{a2.divide(new QPolynomial(zwei.leadingFactor())),
													 b2.divide(new QPolynomial(zwei.leadingFactor())),new QPolynomial(Qelement.ONE)};
		return new QPolynomial[]{a1,b1,eins};
	}
	// Liefert den Koeffizienten zu einer gegebenen Variable in gegebener Potenz
	public QPolynomial getCoefficient(int index, int power)
	{
		QPolynomial erg = new QPolynomial();
		for (int i=0; i<monom.size(); i++)
		{
			QMonomial mon = (QMonomial)monom.elementAt(i);
			if (mon.getExponent(index)==power)
			{
				QMonomial mondot = new QMonomial(mon);
				mondot.setExponent(index, 0);
				erg = erg.add(new QPolynomial(mondot));
			}
		}
		return erg.unifiziereKonstanten();
	}
	// liefert den Konstanten Koeffizienten.
	public Qelement getConstant()
	{
		if (monom.size() == 0) return new Qelement();
		QMonomial m = (QMonomial)monom.firstElement();
		if (m.isConstant()) return m.factor;
		return new Qelement();
	}
	// liefert den Inhalt eines Polynoms (GCD aller Koeffizienten) bezüglich x_index
	public QPolynomial getContent(int index)
	{
		int g = getDegreeIn(index);
		
		QPolynomial[] coeffs = new QPolynomial[g+1];
		for (int i=0; i<coeffs.length; i++) coeffs[i] = ZERO;

		for (int i=0; i<monom.size(); i++)
		{
			QMonomial mon = (QMonomial)monom.elementAt(i);
			QMonomial mondot = new QMonomial(mon);
			int p = mondot.getExponent(index);
			mondot.setExponent(index, 0);
			coeffs[p] = coeffs[p].add(new QPolynomial(mondot));
		}
		return gcd(coeffs);
	}
	// liefert den Inhalt eines Polynoms (GCD aller Koeffizienten) bezüglich x_index mit gcd2
	public QPolynomial getContent2(int index)
	{
		int g = getDegreeIn(index);
		QPolynomial erg = new QPolynomial();
		for (int i=0; i<=g; i++)
			erg = erg.gcd2(getCoefficient(index, i));
		return erg;
	}
	// liefert den Grad des Polynoms in der angegebenen Unbekannten.
	public int getDegreeIn(int index)
	{
		int erg = 0;
		for (int i=0; i<monom.size(); i++)
			erg = Math.max(erg, ((QMonomial)monom.elementAt(i)).getExponent(index));
		return erg;
	}
/**
 * Gibt das Minimalpolynom vom Grad 2^deg für einen allgemeinen Wurzelausdruck aus (ohne konstantes Glied),
 * d.h. von der Form x1*sqrt(x2) + x3*sqrt(x4+x5*sqrt(x6)) + ...
 * Creation date: (21.06.2002 09:19:34)
 */
public static QPolynomial getGeneralQuadraticMinimalPolynomial(int deg) 
{
	int varnr = 1;
	RExpression[] wurzel = new RExpression[deg];
	for (int i=0; i<deg; i++)
	{
		wurzel[i] = new RExpression(varnr++);
		for (int j=0; j<i; j++)
			wurzel[i] = wurzel[i].add((new RExpression(varnr++)).multiply(wurzel[j]));
		wurzel[i] = wurzel[i].sqrt();
	}
	RExpression ex = new RExpression();
	for (int i=0; i<deg; i++)
		ex = ex.add(wurzel[i]);
	ex = (new RExpression(0)).subtract(ex);
	return ex.eliminateSquareRoots().expand().unifiziereKonstanten();		
}
	// liefert den höchsten vorkommenden Index (i.A. die Anzahl der Variablen minus 1).
	public int getHighestIndex()
	{
		int erg = 0;
		for (int i=0; i<monom.size(); i++)
			erg = Math.max(erg, ((QMonomial)monom.elementAt(i)).getHighestIndex());
		return erg;
	}
	// liefert den führenden Koeffizienten zu der gegebenen Variable
	public QPolynomial getLeadingCoefficient(int index)
	{
		return getCoefficient(index, this.getDegreeIn(index));
	}
/**
	// Liefert das nach lexikographischer Ordung höchste Monom
 * Creation date: (13.07.2002 10:05:43)
 */
public QMonomial getLeadingMonomial() 
{
	if (monom.size() == 0) return new QMonomial();
	return ((QMonomial)monom.lastElement());
}
/**
	// Liefert das nach lexikographischer Ordung höchste Monom
 * Creation date: (13.07.2002 10:05:43)
 */
public QMonomial getLeadingMonomial(Comparator monomOrdnung) 
{
	if (monom.size() == 0) return new QMonomial();
	if (monomOrdnung == lexorder) return getLeadingMonomial();

	QMonomial erg = (QMonomial)monom.elementAt(0);
	for (int i=1; i<monom.size(); i++)
	{
		QMonomial m = (QMonomial)monom.elementAt(i);
		if (monomOrdnung.compare(m.exp,erg.exp)==1) erg = m;
	}
	
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (07.05.2003 18:39:36)
 * @return arithmetik.QMonomial[]
 * @param ideal arithmetik.QPolynomial[]
 */
public static QMonomial[] getLeadingTermIdeal(QPolynomial[] ideal) 
{
	Vector exp = new Vector();
	for (int i=0; i<ideal.length; i++) 
	{
		QMonomial m = new QMonomial(ideal[i].getLeadingMonomial());
		m.factor = Qelement.ONE;
		boolean takeit = true;
		for (int j=0; j<exp.size(); j++)
		{
			QMonomial m2 = (QMonomial)exp.elementAt(j);
			if (m2.divides(m)) takeit = false;
		}			
		if (takeit) exp.addElement(m);
	}
	QMonomial[] erg = new QMonomial[exp.size()];
	for (int i=0; i<erg.length; i++) erg[i] = (QMonomial)exp.elementAt(i);
	return erg;	
}
/**
 * Insert the method's description here.
 * Creation date: (07.05.2003 18:39:36)
 * @return arithmetik.QMonomial[]
 * @param ideal arithmetik.QPolynomial[]
 */
public static QMonomial[] getLeadingTermIdeal(QPolynomial[] ideal, Comparator ordnung) 
{
	Vector exp = new Vector();
	for (int i=0; i<ideal.length; i++) 
	{
		QMonomial m = new QMonomial(ideal[i].getLeadingMonomial(ordnung));
		m.factor = Qelement.ONE;
		boolean takeit = true;
		for (int j=0; j<exp.size(); j++)
		{
			QMonomial m2 = (QMonomial)exp.elementAt(j);
			if (m2.divides(m)) takeit = false;
		}			
		if (takeit) exp.addElement(m);
	}
	QMonomial[] erg = new QMonomial[exp.size()];
	for (int i=0; i<erg.length; i++) erg[i] = (QMonomial)exp.elementAt(i);
	return erg;	
}
/**
 * Insert the method's description here.
 * Creation date: (21.06.2002 12:19:29)
 * @return java.util.Vector
 * @param index int

	Liefert einen Vector (RQuotientExp) aller Nullstellen von this zurück, die durch einen Linearfaktor entstehen.
 
 */
public Vector getLinearZeros(int index) 
{
	QPolynomial[] faks = factorize();
	Vector erg = new Vector();
	for (int i=0; i<faks.length; i++)
		if (faks[i].getDegreeIn(index)==1)
			erg.addElement(new RQuotientExp(new RExpression(faks[i].getCoefficient(index,0).negate()),
										    new RExpression(faks[i].getCoefficient(index,1))));
	return erg;			
}
	// liefert den Primpart eines Polynoms (Polynom durch Inhalt) bezüglich x_index
	public QPolynomial getPrimepart(int index)
	{
		if (isZero()) return ZERO;
		return divide(getContent(index));
	}
	// liefert den Totalgrad des Polynoms, d.h. die maximale Summe aller Grade
	public int getTotalDegree()
	{
		int erg = 0;
		for (int i=0; i<monom.size(); i++)
			erg = Math.max(erg, ((QMonomial)monom.elementAt(i)).getTotalDegree());
		return erg;
	}
	// Verwandelt das Polynom in ein neues Polynom mit dem selben Grad in index, dass alle
	// Nullstellen in index zum Quadrat hat.
	public QPolynomial graeffeSqr(int index)
	{
		QPolynomial snd = new QPolynomial(this);
		for (int i=0; i<snd.monom.size(); i++)
		{
			QMonomial mon = (QMonomial)snd.monom.elementAt(i);
			if (mon.exp[index]%2 == 1) mon.factor = mon.factor.negate();
		}
		QPolynomial erg = multiply(snd);
		for (int i=0; i<erg.monom.size(); i++)
		{
			QMonomial mon = (QMonomial)erg.monom.elementAt(i);
			mon.exp[index] = mon.exp[index]/2;
		}
		return erg.unifiziereKonstanten();
	}
/**
 * Insert the method's description here.
 * Creation date: (13.08.2004 11:25:49)
 * @return arithmetik.QPolynomial[]
 * @param points arithmetik.Complex[][]
 * @param maxDegree int

	Errät ein polynomielles Gleichungsystem, auf dem die Punkte liegen, bis zum Grad maxDegree.
 
 */
public static QPolynomial[] guessSystem(Complex[][] points, int maxDegree) 
{
	final double EPSILON = 0.001;
	Complex eins = (Complex)points[0][0].abs_unit();
	int anzpoint = points.length;
	int anzvar = points[0].length;
	Vector monome = new Vector();
	
	int[] potenz = new int[anzvar];
	int[] potenzsumme = new int[anzvar];
	Complex[][] wert = new Complex[anzvar][anzpoint];
	for (int i=0; i<anzvar; i++)
		for (int j=0; j<anzpoint; j++)
			wert[i][j] = eins;
	Vector matrix = new Vector();
		
	while (true)
	{
		monome.addElement(new QMonomial(Qelement.ONE,potenz));
		Complex[] wertKopie = new Complex[anzpoint];
		for (int i=0; i<anzpoint; i++) wertKopie[i] = wert[anzvar-1][i];
		matrix.addElement(wertKopie);
		
		int k = anzvar-1;
		while ((k>=0) && (potenzsumme[k]==maxDegree)) k--;
		if (k<0) break;
		potenz[k]++; potenzsumme[k]++;
		for (int i=0; i<anzpoint; i++) wert[k][i] = (Complex)wert[k][i].abs_multiply(points[i][k]);
		for (int i=k+1; i<anzvar; i++)
		{
			potenz[i] = 0;
			potenzsumme[i] = potenzsumme[k];
			for (int j=0; j<anzpoint; j++) wert[i][j] = wert[k][j];
		}
	}

	int anzmonome = monome.size();

	double[][] AAtrans = new double[anzmonome][anzmonome];
	for (int i=0; i<anzmonome; i++)
		for (int j=0; j<anzmonome; j++)
		{
			double summe = 0.0;
			for (int k=0; k<anzpoint; k++) 
				summe += ((Complex)((Complex[])matrix.elementAt(i))[k].abs_multiply( ((Complex[])matrix.elementAt(j))[k].conjugate() )).reelValue();
			AAtrans[i][j] = summe;
		}
	RingMatrix AAtransMat = new RingMatrix(AAtrans);

	RingVector[] kernel = AAtransMat.approximateKernelBasisOfSymmetric(EPSILON*EPSILON);
	QPolynomial[] erg = new QPolynomial[kernel.length];

	for (int i=0; i<erg.length; i++)
	{
		erg[i] = QPolynomial.ZERO;
		double fak = 0.0;
		for (int j=0; j<anzmonome; j++) 
		{
			double work = ((DoubleWrapper)kernel[i].getValue(j+1)).doubleValue();
			Qelement koeff = Qelement.ZERO;
			if (Math.abs(work) > EPSILON)
			{
				if (fak == 0.0) fak = work;
				work = work / fak;
				koeff = Qelement.guessFromDouble(work);
			}
			erg[i] = erg[i].add( ((QMonomial)monome.elementAt(j)).multiply(koeff) );
		}
	}
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (06.02.2003 10:06:42)
 * @return arithmetik.QPolynomial
 */
public QPolynomial homogenize() 
{
	return homogenize(getHighestIndex()+1);
}
/**
 * Insert the method's description here.
 * Creation date: (06.02.2003 10:07:15)
 * @return arithmetik.QPolynomial
 * @param index int

	Gibt ein homogenes Polynom durch homogenisierung mit X_index zurück. 
 
 */
public QPolynomial homogenize(int index) 
{
	int deg = getTotalDegree();
	QPolynomial erg = new QPolynomial();
	for (int i=0; i<monom.size(); i++)
	{
		QMonomial m = new QMonomial((QMonomial)monom.elementAt(i));
		int d = m.getTotalDegree();
		m.setExponent(index,m.getExponent(index)+deg-d);
		erg.add(new QPolynomial(m));
	}
	return erg;
}
	/**

		von CK. 
	
	 * Setzt x_id = 0 in this (i.e. streicht alle Monome, die grad_id > 0 haben

		das heisst, evaluiert p mit x_id = 0.
	 
	 */
	public QPolynomial idToZero(int id)
	{
		QPolynomial erg = new QPolynomial();
		for (int i = 0 ; i < monom.size() ; i++)
		{
			QMonomial tmp = (QMonomial)monom.elementAt(i);
			if (tmp.getExponent(id) == 0) erg = erg.add(new QPolynomial(tmp));
		}
		return erg;
	}

/**
 * Interpoliert das Polynom.

 * Eingabe ist folgendermaßen aufgebaut: Zuerst ein Integer i für die Nummer der äußersten Variablen. Dann ein
 * Array von deg_i (f) vielen Qelements, die die Stellen angeben, an denen f ausgewertet wurde. Dann
 * ein Array von deg_i (f) vielen Vectoren, die genauso aufgebaut sind, für die inneren Variablen, oder ein
 * genausogroßes Array von QElements mit den Werten an dieser Stelle.
 * Creation date: (13.07.2002 14:48:28)
 * @return arithmetik.QPolynomial
 * @param stellen java.util.Vector
 * @param werte java.util.Vector
 */
public static QPolynomial interpolate(Vector eingabe) 
{
	int ix = ((Integer)eingabe.elementAt(0)).intValue();
	Qelement[] stelle = (Qelement[])eingabe.elementAt(1);
	Qelement[] werte = null; if (eingabe.elementAt(2) instanceof Qelement[]) werte = (Qelement[])eingabe.elementAt(2);
	Vector[] weiter = null; if (eingabe.elementAt(2) instanceof Vector[]) weiter = (Vector[])eingabe.elementAt(2);
	QPolynomial[][] phi = new QPolynomial[stelle.length][stelle.length];
	for (int i=0; i<stelle.length; i++)
	{
		if (werte!=null) phi[0][i] = new QPolynomial(werte[i]);
		if (weiter!=null) phi[0][i] = interpolate(weiter[i]);
	}
	QPolynomial x = new QPolynomial(ix);
	for (int i=1; i<stelle.length; i++)
		for (int j=i; j<stelle.length; j++)
		{
			QPolynomial n = new QPolynomial(stelle[j-i].subtract(stelle[j]));
			phi[i][j] = (x.negate().add(new QPolynomial(stelle[j-i]))).divide(n).multiply(phi[i-1][j]).add(
				        (x.subtract    (new QPolynomial(stelle[j]))).divide(n).multiply(phi[i-1][j-1]));
		}
	return phi[stelle.length-1][stelle.length-1].unifiziereKonstanten();
}
/**
 * Insert the method's description here.
 * Creation date: (08.01.2003 12:21:50)
 * @return boolean
 */
public boolean isBivariate() 
{
	int erg1 = -1;
	int erg2 = -2;
	for (int i=0; i<monom.size(); i++)
	{
		QMonomial m = (QMonomial)monom.elementAt(i);
		for (int j=0; j<m.exp.length; j++)
			if (m.exp[j]>0)
			{
				if ((erg1>-1) && (erg2>-1) && (j!=erg1) && (j!=erg2)) return false;
				if (erg1==-1) erg1 = j;
				if ((erg2==-1) && (j!=erg1)) erg2 = j;
			}
	}
	return true;
}
	public boolean isConstant() 
	{
		return ((monom.size()==1) && (((QMonomial)monom.elementAt(0)).isConstant()));
	}
/**
 * Insert the method's description here.
 * Creation date: (21.02.2003 10:13:34)
 * @return boolean
 * @param arg2 arithmetik.QPolynomial
 */
public boolean isDivisibleBy(QPolynomial arg2) 
{
	return remainder(arg2).isZero();
}
/**
 * Insert the method's description here.
 * Creation date: (08.01.2003 18:41:58)
 * @return boolean

	Gibt true zurück, wenn alle Exponenten des Polynoms in Xindex gerade sind, sonst false.
 
 */
public boolean isInAllExponentsEven(int index) 
{
	for (int i=0; i<monom.size(); i++)
		if (((QMonomial)monom.elementAt(i)).getExponent(index) % 2 == 1) return false;
	return true;
}
	// liefert wahr zurück, wenn der Nenner aller Faktoren 1 ist.
	public boolean isIntegerFactors()
	{
		for (int i=0; i<monom.size(); i++)
			if (!((QMonomial)monom.elementAt(i)).factor.isInteger()) return false;
		return true;
	}
/**
 * Insert the method's description here.
 * Creation date: (08.01.2003 11:37:35)
 * @return boolean

	Erwartet ein irreduzibles Polynom in Xix mit weiteren Variablen, die zufällig eingesetzt werden.
 
	Gibt true zurück, wenn das Polynom bezgl. Xix wahrscheinlich duch Quadratwurzeln auflösbar 
	ist, und false, wenn es sicherlich nicht durch Quadratwurzeln auflösbar ist.
 */
public boolean isProbablySquarerootsolvable(int ix) 
{
	final int ANZLOOPS = 5, RANDOMRANGE = 10;
	int ANZPRIMZAHLEN = 6;
	int deg = getDegreeIn(ix);
	if (deg <=2) return true;
	if (deg <32) ANZPRIMZAHLEN = 8;
	if (deg <16) ANZPRIMZAHLEN = 10;
	if (deg <8) ANZPRIMZAHLEN = 20;

	int loops = 1;
	if (!isUnivariate()) loops = ANZLOOPS;
	for (int lnr=0; lnr<loops; lnr++)
	{
		QPolynomial p = this;
		if (loops>1)
		{
			int anzNr = getHighestIndex();
			int[] nrs = new int[anzNr];
			Qelement[] vals = new Qelement[anzNr];
			int j = 0;
			for (int i=0; i<=anzNr; i++)
			{
				if (i!=ix)
				{
					nrs[j] = i;
					vals[j] = new Qelement(Math.round(Math.random()*RANDOMRANGE));
					j++;
				}
			}
			p = evaluate(nrs,vals);
		}
			
		final int[] primzahlen = {2,3,5,7,11,13,17,19,23,29,31,37,41,43,47,53,59,61,67,71,73,79,83,89,97};
		for (int i=0; i<ANZPRIMZAHLEN; i++)
		{
			RemainderRingPolynomial[] modfac = p.factorizeModulo(primzahlen[i]);
			for (int j=0; j<modfac.length; j++)
			{
				int d = modfac[j].deg;
				int k = 1;
				while (k < d) k *= 2;
				if ((d>2) && (k!=d)) return false;
			}
		}
	}
	return true;	
}
	public boolean isUnit()
	{
		return ((monom.size()==1) && (((QMonomial)monom.elementAt(0)).isUnit()));
	}
/**
 * Insert the method's description here.
 * Creation date: (08.01.2003 12:21:50)
 * @return boolean
 */
public boolean isUnivariate() 
{
	int erg = -1;
	for (int i=0; i<monom.size(); i++)
	{
		QMonomial m = (QMonomial)monom.elementAt(i);
		for (int j=0; j<m.exp.length; j++)
			if (m.exp[j]>0)
			{
				if (erg==-1) erg = j;
				if (erg!=j) return false;
			}
	}
	return true;
}
	public boolean isZero()
	{
		return (monom.size()==0);
	}
	// Liefert den nach lexikographischer Ordung höchsten Koeffizienten
	public Qelement leadingFactor()
	{
		if (monom.size() == 0) return new Qelement();
		return getLeadingMonomial().factor;
	}
	// Liefert den nach lexikographischer Ordung höchsten Koeffizienten
	public Qelement leadingFactor(Comparator ordnung)
	{
		if (monom.size() == 0) return new Qelement();
		return getLeadingMonomial(ordnung).factor;
	}
	// Mulitpliziert das Polynom mit dem kgV der Nenner aller Koeffizienten.
	public QPolynomial makeCoefficientInteger()
	{
		BigInteger a = BigInteger.ONE;
		for (int i=0; i<monom.size(); i++)
		{
			QMonomial mon = (QMonomial)monom.elementAt(i);
			BigInteger z = mon.factor.n;
			a = z.multiply(a.divide(z.gcd(a)));
		}
		return multiply(new QPolynomial(new Qelement(a)));		
	}
/**
 * Insert the method's description here.
 * Creation date: (08.05.2003 13:08:35)
 * @return arithmetik.QPolynomial

	Multipliziert mit dem kgV aller Nenner,
	Dividiert dann durch den ggT aller Zähler.
 
 */
public QPolynomial makeCoefficientIntegerAndMinimal() 
{
	QPolynomial erg = makeCoefficientInteger();
	
	BigInteger a = BigInteger.ONE;
	for (int i=0; i<monom.size(); i++)
	{
		QMonomial mon = (QMonomial)monom.elementAt(i);
		BigInteger z = mon.factor.z;
		if (i==0) a = z; else a = z.gcd(a);
		if (a.equals(BigInteger.ONE)) return erg;
	}
	return erg.multiply(new Qelement(BigInteger.ONE,a));		
}
	public QPolynomial monomialMultiply(QMonomial arg2)
	{
		QPolynomial erg = new QPolynomial();
		for (int i=0; i<monom.size(); i++)
			erg.monom.addElement(((QMonomial)monom.elementAt(i)).multiply(arg2));
		return erg.unifiziereKonstanten();
	}
/**
 * 

	Liefert auf Eingabe snd[0],...,snd[n-1] ein Array a[0],...,a[n-1],r zurück, so dass

	this = a[0]*snd[0] + a[1]*snd[1] + ... + a[n-1]*snd[n-1] + r ist und

	a[i]!=0 => deg(a[i]*snd[i]) <= deg(this)	und kein Monom von r wird von einem Leitmonom von a[0],...,a[n-1] geteilt.
 
 * Creation date: (09.07.2002 11:32:06)
 * @return arithmetik.QPolynomial[]
 * @param snd arithmetik.QPolynomial[]
 */
public QPolynomial[] multiDivideAndRemainder(QPolynomial[] snd) 
{
	int n = snd.length;
	QPolynomial erg[] = new QPolynomial[n+1];
	for (int i=0; i<=n; i++) 
	{
		erg[i] = new QPolynomial();
		if ((i<n) && (snd[i].isZero())) throw new RuntimeException("Division by zero");
	}
	QPolynomial work = new QPolynomial(this);

	while (!work.isZero())
	{
		QMonomial mon = work.getLeadingMonomial();
		QMonomial m2, sb = new QMonomial();
		boolean ok = false;
		int nr = 0;
		while ((nr < snd.length) && (!ok))
		{
			m2 = snd[nr].getLeadingMonomial();
			sb = new QMonomial(mon.factor.divide(m2.factor));
			int ix = Math.max(m2.exp.length, mon.exp.length);
			ok = true;
			int i = 0;
			while ((i<=ix) && (ok))
			{
				sb.setExponent(i,mon.getExponent(i)-m2.getExponent(i));
				if (sb.getExponent(i)<0) ok = false;
				i++;
			}
			if (!ok) nr++;
		}
		if (ok)
		{
			work = work.subtract(snd[nr].monomialMultiply(sb));
			erg[nr] = erg[nr].add(new QPolynomial(sb));
		} else {
			QPolynomial ad = new QPolynomial(mon);
			work = work.subtract(ad);
			erg[erg.length-1] = erg[erg.length-1].add(ad);
		}
	}
	return erg;	
}
/**
 * 

	Liefert auf Eingabe snd[0],...,snd[n-1] ein Array a[0],...,a[n-1],r zurück, so dass

	this = a[0]*snd[0] + a[1]*snd[1] + ... + a[n-1]*snd[n-1] + r ist und

	a[i]!=0 => deg(a[i]*snd[i]) <= deg(this)	und kein Monom von r wird von einem Leitmonom von a[0],...,a[n-1] geteilt.
 
 * Creation date: (09.07.2002 11:32:06)
 * @return arithmetik.QPolynomial[]
 * @param snd arithmetik.QPolynomial[]
 */
public QPolynomial[] multiDivideAndRemainder(QPolynomial[] snd, Comparator ordnung) 
{
	int n = snd.length;
	QPolynomial erg[] = new QPolynomial[n+1];
	for (int i=0; i<=n; i++) 
	{
		erg[i] = new QPolynomial();
		if ((i<n) && (snd[i].isZero())) throw new RuntimeException("Division by zero");
	}
	QPolynomial work = new QPolynomial(this);

	while (!work.isZero())
	{
		QMonomial mon = work.getLeadingMonomial(ordnung);
		QMonomial m2, sb = new QMonomial();
		boolean ok = false;
		int nr = 0;
		while ((nr < snd.length) && (!ok))
		{
			m2 = snd[nr].getLeadingMonomial(ordnung);
			sb = new QMonomial(mon.factor.divide(m2.factor));
			int ix = Math.max(m2.exp.length, mon.exp.length);
			ok = true;
			int i = 0;
			while ((i<=ix) && (ok))
			{
				sb.setExponent(i,mon.getExponent(i)-m2.getExponent(i));
				if (sb.getExponent(i)<0) ok = false;
				i++;
			}
			if (!ok) nr++;
		}
		if (ok)
		{
			work = work.subtract(snd[nr].monomialMultiply(sb));
			erg[nr] = erg[nr].add(new QPolynomial(sb));
		} else {
			QPolynomial ad = new QPolynomial(mon);
			work = work.subtract(ad);
			erg[erg.length-1] = erg[erg.length-1].add(ad);
		}
	}
	return erg;	
}
/**
 * Insert the method's description here.
 * Creation date: (05.01.2003 15:41:38)
 * @return arithmetik.QPolynomial
 * @param m arithmetik.Qelement
 */
public QPolynomial multiply(Qelement m)
{
	QPolynomial erg = new QPolynomial();
	for (int i = 0; i < monom.size() ; i++)
		erg.monom.addElement(((QMonomial)monom.elementAt(i)).multiply(m));

	return erg.unifiziereKonstanten();
}
	public QPolynomial multiply(QPolynomial arg2)
	{
		QPolynomial erg = new QPolynomial();
		for (int i=0; i<monom.size(); i++)
			erg = erg.add(arg2.monomialMultiply((QMonomial)monom.elementAt(i)));
		return erg.unifiziereKonstanten();
	}
/**
 * Insert the method's description here.
 * Creation date: (06.02.2003 09:58:48)
 * @return arithmetik.QPolynomial
 * @param in arithmetik.QPolynomial[]
 * @param index int

	Berechneet die Multiresultante bzgl. der ersten in.length-1 vorkommenden Variablen.  
 */
public static QPolynomial multiResultant(QPolynomial[] in) 
{
	if (in.length==0) return ONE;
	if (in.length==1) return in[0];

	int maxInd = -1;
	for (int i=0; i<in.length; i++) maxInd = Math.max(maxInd, in[i].getHighestIndex());
	boolean[] kommtVor = new boolean[maxInd+1]; 
	for (int i=0; i<=maxInd; i++) kommtVor[maxInd] = false;

	for (int i=0; i<in.length; i++)
		for (int j=0; j<in[i].monom.size(); j++)
		{
			QMonomial m = (QMonomial)in[i].monom.elementAt(j);
			for (int k=0; k<=maxInd; k++) 
				if (m.getExponent(k)>0) kommtVor[k] = true;
		}

	int[] vars = new int[in.length-1];
	int c = 0;
	for (int i=0; i<kommtVor.length; i++) if (kommtVor[i]) vars[c++] = i;
	if (c==in.length-1) return multiResultant(in, vars);
	else throw new RuntimeException("To many equations to compute multiresultant.");
}
/**
 * Insert the method's description here.
 * Creation date: (06.02.2003 09:58:48)
 * @return arithmetik.QPolynomial
 * @param in arithmetik.QPolynomial[]
 * @param index int

	Berechnet die Multiresultante bzgl. toElim, d.h. diese Variablen werden eliminiert.
	Die Größe von toElim muss genau eins kleiner sein als die Größe von in. Durch unendliche
	Ferne Lösungen können (wenige) zusätzliche Nullstellen entstehen.
 */
public static QPolynomial multiResultant(QPolynomial[] in, int[] toElim) 
{
	if (in.length-1!=toElim.length) 
		throw new RuntimeException("Multiresultant must be called with one variable less than equations");
	if (in.length==1) return in[0];

	int homoVar = -1;
	for (int i=0; i<in.length; i++) homoVar = Math.max(homoVar, in[i].getHighestIndex());
	homoVar++;

	int[] toElimAndHomo =new int[toElim.length+1];
	for (int i=0; i<toElim.length; i++) toElimAndHomo[i] = toElim[i];
	toElimAndHomo[toElim.length] = homoVar;
	
	// Die folgende Schleife misst die Totalgrade bzgl. toElim
	int[] di = new int[in.length];
	for (int i=0; i<in.length; i++)
	{
		di[i] = 0;
		for (int j=0; j<in[i].monom.size(); j++)
		{
			QMonomial m = (QMonomial)in[i].monom.elementAt(j);
			int mdeg = 0; for (int k=0; k<toElim.length; k++) mdeg += m.getExponent(toElim[k]);
			di[i] = Math.max(di[i],mdeg);
		}
	}
		
	QPolynomial[] homoPol = new QPolynomial[in.length];
	for (int i=0; i<in.length; i++) 
	{
		homoPol[i] = ZERO;
		for (int j=0; j<in[i].monom.size(); j++)
		{
			QMonomial m = new QMonomial((QMonomial)in[i].monom.elementAt(j));
			int mdeg = 0; for (int k=0; k<toElim.length; k++) mdeg += m.getExponent(toElim[k]);
			m.setExponent(homoVar,di[i]-mdeg);
			homoPol[i] = homoPol[i].add(new QPolynomial(m));
		}
	}

	int d = 1; 
	for (int i=0; i<in.length; i++) d += di[i]-1;
	
	Vector astrichzeilen = new Vector();					// Hier werden die Zeilennummern gespeichert, aus denen
															// Astrich zusammengestrichen wird.
	Vector nummerZuExs = new Vector();						// Hier werden die Grad-Tupel zu den Zeilennummern gespeichert

	int asize = (int)Statik.binomialCoefficient(d+in.length-1,in.length-1);
	int[] exs = new int[toElimAndHomo.length];
	for (int i=0; i<exs.length; i++) exs[i] = 0;
	exs[0] = d;
	Hashtable[] aZeilen = new Hashtable[asize];				// Für jede Zeile eine Hashtable monom -> Wert
	Hashtable exsZuNummer = new Hashtable();				// Für jedes Monom die Zeilennummer
	int zeile = 0;
	int snr = 0;
	boolean weiter = true;
	while (weiter)
	{
		nummerZuExs.addElement(exs);
		exsZuNummer.put(new Tupel(exs),new Integer(zeile));
		while (exs[snr] < di[snr]) snr++;

		// Test, ob Monom reduziert
		for (int i=snr+1; i<exs.length; i++) 
			if (exs[i] >= di[i]) 
			{
				astrichzeilen.addElement(new Integer(zeile));
				i = exs.length;
			}

		aZeilen[zeile] = new Hashtable();
		for (int i=0; i<homoPol[snr].monom.size(); i++)
		{
			QMonomial m = new QMonomial((QMonomial)homoPol[snr].monom.elementAt(i));
			int[] nexs = new int[exs.length];
			for (int j=0; j<exs.length; j++) 
			{
				nexs[j] = m.getExponent(toElimAndHomo[j])+exs[j];
				m.setExponent(toElimAndHomo[j],0);
			}
			nexs[snr] -= di[snr];
			Tupel tu = new Tupel(nexs);
			QPolynomial add = new QPolynomial(m);
			QPolynomial bisher = (QPolynomial)aZeilen[zeile].get(tu);
			if (bisher==null) aZeilen[zeile].put(tu,add);
			else aZeilen[zeile].put(tu,bisher.add(add));
		}
		
		zeile++;
		// exs weiterzählen
		int bruch = exs.length-2;
		while ((bruch >= 0) && (exs[bruch]==0)) bruch--;
		if (bruch<0) weiter = false; 							// Ende
		else
		{
			exs[bruch]--; 
			exs[bruch+1] = exs[exs.length-1]+1;
			for (int i=bruch+2; i<exs.length; i++) exs[i] = 0;
		}
	}

	int astrichsize = astrichzeilen.size();
	int[] vonAstrichZuA = new int[astrichsize];
	int[] vonAzuAstrich = new int[asize];
	for (int i=0; i<asize; i++) vonAzuAstrich[i] = -1;
	for (int i=0; i<astrichsize; i++)
	{
		vonAstrichZuA[i] = ((Integer)astrichzeilen.elementAt(i)).intValue();
		vonAzuAstrich[vonAstrichZuA[i]] = i;
	}		
	
	QPolynomial[][] A = new QPolynomial[asize][asize];
	for (int i=0; i<asize; i++)
		for (int j=0; j<asize; j++) A[i][j] = ZERO;

	QPolynomial[][] Astrich = new QPolynomial[astrichsize][astrichsize];
	for (int i=0; i<astrichsize; i++)
		for (int j=0; j<astrichsize; j++) Astrich[i][j] = ZERO;
	
	for (int i=0; i<asize; i++)
	{
		Enumeration enu = aZeilen[i].keys();
		while (enu.hasMoreElements())
		{
			Tupel t = (Tupel)enu.nextElement();
			int spalte = ((Integer)exsZuNummer.get(t)).intValue();
			QPolynomial entry = (QPolynomial)aZeilen[i].get(t);
			A[i][spalte] = entry;
			if ((vonAzuAstrich[i]!=-1) && (vonAzuAstrich[spalte]!=-1))
				Astrich[vonAzuAstrich[i]][vonAzuAstrich[spalte]] = entry;
		}			
	}

	QPolynomial detStrich = determinant(Astrich);
	if (detStrich.isZero())								// Berchnen res(all i: F_i - uX_i)
	{
		int uix = homoVar+1;
		QPolynomial u = new QPolynomial(uix);
		
		// auf Astrich sind die selben Pointer
		for (int i=0; i<asize; i++) A[i][i] = A[i][i].subtract(u);	

		detStrich = determinant(Astrich);
		int lex = 1;
		QPolynomial coeffStrich = detStrich.getCoefficient(uix,lex++);
		while (coeffStrich.isZero()) coeffStrich = detStrich.getCoefficient(uix,lex++);
		lex--;
		QPolynomial det = determinant(A);
		return det.getCoefficient(uix,lex).divide(coeffStrich);
	} else {
		QPolynomial det = determinant(A);
		return det.divide(detStrich);
	}		
}
	public QPolynomial negate()
	{
		QPolynomial erg = new QPolynomial(this);
		for (int i=0; i<erg.monom.size(); i++)
		{
			QMonomial mon = (QMonomial)erg.monom.elementAt(i);
			mon.factor = mon.factor.negate();
		}
		return erg;
	}
	// Diese Methode setzt alle Variablen ausser X_*identifierNr* auf 0 und approximiert
	// dann die restlichen Nullstellen mit Hilfe des Newtonverfahrens bis auf die Genauigkeit
	// Epsilon. Das Verfahren reagiert unsicher bei vermeintlicher Konvergenz. 
	public Celement[] newtonalgorithm(int identifierNr, Qelement epsilon)
	{
		QPolynomial work = new QPolynomial(this);
		for (int i=0; i<work.monom.size(); i++)
		{
			QMonomial mon = (QMonomial)work.monom.elementAt(i);
			int k = mon.getExponent(identifierNr);
			mon.exp = new int[0];
			mon.setExponent(identifierNr, k);
		}
		
		int grad = getLeadingMonomial().getExponent(identifierNr);

		QPolynomial abl = work.derive(identifierNr);
		System.out.println("Polynom  : "+work);
		System.out.println("Ableitung: "+abl);

		if (grad==0) return new Celement[0];
		Celement[] erg = new Celement[grad];
		
		for (int i=0; i<grad; i++)
		{
			Celement p = new Celement(new Relement(Qelement.HALF),new Relement(Qelement.HALF));
			Celement np = new Celement(new Relement(Qelement.HALF),new Relement(Qelement.HALF));
			Celement diff = new Celement(1);
			
			while (!diff.isZero(epsilon))
			{
				p = new Celement(np.reel,np.imag);
				Celement summe = new Celement(0,0);
				for (int j=0; j<i; j++)
					summe = summe.add( (p.subtract(erg[j])).trustCenter().reciprocal() );
				try {
					diff = ( (abl.evaluate(identifierNr,p)).divide(work.evaluate(identifierNr,p).trustCenter()).subtract(summe)).trustCenter().reciprocal();
				} catch (Exception e)
				{
					System.out.println("Fehler :"+e);
				}
				np = p.subtract(diff); 
			}
			erg[i] = new Celement(new Relement(p.reel,np.reel),new Relement(p.imag,np.imag));
		}
		System.out.println("Newton liefert:");
		for (int i=0; i<erg.length; i++)
			System.out.println ("Nullstelle "+i+": "+erg[i].toDoubleString());
		
		return erg;
	}
	
	/**
	 * approximates a reel zero if start is in the convergence region of the zero, using Newton's method
	 * 
	 * @param ix
	 * @param epsilon
	 * @param start
	 * @return
	 */
	public Relement approximateReelZero(int ix, Qelement epsilon, Relement start) {
        QPolynomial work = new QPolynomial(this);
        for (int i=0; i<work.monom.size(); i++)
        {
            QMonomial mon = (QMonomial)work.monom.elementAt(i);
            int k = mon.getExponent(ix);
            mon.exp = new int[0];
            mon.setExponent(ix, k);
        }

        QPolynomial dev = work.derive(ix);
        
        Relement p = start;
        Relement np = start;
        Relement diff = new Relement(1);
        
        while (!diff.isZero(epsilon))
        {
            p = new Relement(np);
            try {
                Relement devv = dev.evaluate(ix,p).trustCenter();
                if (devv.signum() == 0) diff = new Relement(0); else 
                diff = work.evaluate(ix, p).divide(devv);
            } catch (Exception e) {System.out.println("Error in Newton's Method on univariate polynomial :"+e); }
            np = p.subtract(diff); 
        }
        return np;
	}
	
	public QPolynomial normalize()
	{
		Qelement fac = leadingFactor();
		QPolynomial erg = new QPolynomial();
		for (int i=0; i<monom.size(); i++)
		{
			QMonomial m = (QMonomial)monom.elementAt(i);
			erg.monom.addElement(new QMonomial(m.factor.divide(fac), m.exp));
		}
		return erg.unifiziereKonstanten();
	}
/**
 * Insert the method's description here.
 * Creation date: (05.01.2003 15:51:33)
 * @return int

	Erzeugt, glaube ich, von CK. Bin unsicher, 
	wofür es benutzt wird.
	Taucht in der Dokumentation nicht auf.

	CK: Bivariat in Variablen 0 und 1
 
 */
public int numbersOfFactors() 
{
		QPolynomial f = new QPolynomial(this);
		int index = f.getHighestIndex();
		QPolynomial der = f.derive(0);
		System.out.println(der);
		QPolynomial gcd = f.gcd(der);
		System.out.println(gcd);
//		f = f.divide(gcd);
		System.out.println("Quadratfrei: "+f);
		int m = f.getDegreeIn(0);
		int n = f.getDegreeIn(1);
		if ((m<1) || (n<1)) {throw new RuntimeException ("Polynomial must be bivariate for numbersOfFactors()");}

//		Qelement[][] mat = new Qelement[4*m*n][2*m*n+m+n];			// zeilen * spalten
		Qelement[][] mat = new Qelement[4*m*n][4*m*n];				// zeilen * spalten
		for (int i=0; i<mat.length; i++)
			for (int j=0; j<mat[0].length; j++)
				mat[i][j] = new Qelement();

		for (int k=0; k<=2*m-1; k++)
			for (int l=0; l<=2*n-1; l++)
			{
//				System.out.println("1. Durchlauf: ");
//				System.out.println("Grenzen: "+Math.max(0, k-m)+" "+Math.min(m-1,k)+" "+
//								   Math.max(0,l-n+1)+" "+Math.min(n,l+1));
				for (int i=Math.max(0, k-m); i<= Math.min(m-1,k); i++)
					for (int j=Math.max(0,l-n+1); j <= Math.min(n,l+1); j++)
					{
//						System.out.println(k+" "+l+" "+i+" "+j);
						mat[k*2*n+l][i*(n+1)+j] =
						  (new Qelement(-l+2*j-1)).multiply(f.getCoefficient(0, k-i).getCoefficient
												(1, l-j+1).leadingFactor());
					}
//				System.out.println("2. Durchlauf: ");
//				System.out.println("Grenzen: "+Math.max(0, k-n+1)+" "+Math.min(m,k+1)+" "+
//						   Math.max(0,l)+" "+Math.min(n-1,l));
				for (int i=Math.max(0, k-m+1); i<= Math.min(m,k+1); i++)
					for (int j=Math.max(0,l-n); j <= Math.min(n-1,l); j++)
					{
//						System.out.println(k+" "+l+" "+i+" "+j);
						mat[k*2*n+l][m*(n+1)+j+i*n] =
						  (new Qelement(k-2*i+1)).multiply(f.getCoefficient(0, k-i+1).getCoefficient(1, l-j).leadingFactor());
					}
			}
		System.out.println("matrix fertig");
		RingMatrix matrix = new RingMatrix(mat);
//		System.out.println(matrix);
		RingVector[] c = matrix.coreBasis();
		return c.length - 4*m*n + 2*m*n+m+n;
}
	public QPolynomial pow (long n)
	{
		if (n==0) return ONE;
		if (n==1) return new QPolynomial(this);
		if (n%2 == 0) return pow(n/2).sqr();
		else return pow(n/2).sqr().multiply(this);
	}
	// liefert alpha, beta und r zurück, so dass 
	// (1)	alpha * this - beta * arg2 = r
	// (2)  alpha die Unbekannte x_index nicht mehr enthält,
	// (3)  deg(beta) = deg(this)-deg(arg2) oder beta=0, falls der Ausruck negativ,
	// (4)  deg(r) < deg(arg2)
	
	// Falls deg(this)<deg(arg2) in x_index, wird alpha=1,
	// beta=0 und r = this.
	public QPolynomial[] pseudoDivide(QPolynomial arg2, int index)
	{
		int diff = this.getDegreeIn(index)-arg2.getDegreeIn(index);
		if (diff < 0) return new QPolynomial[]{ONE,ZERO,new QPolynomial(this)};

		QPolynomial[] erg;
		QPolynomial alphadot = arg2.getLeadingCoefficient(index);
		QPolynomial betadot = this.getLeadingCoefficient(index);
		QMonomial xToDiff = new QMonomial(Qelement.ONE);
		xToDiff.setExponent(index, diff);
		betadot = betadot.multiply(new QPolynomial(xToDiff));
		erg = ((alphadot.multiply(this)).subtract(betadot.multiply(arg2))).pseudoDivide(arg2, index);
		erg[1] = erg[1].add(erg[0].multiply(betadot));
		erg[0] = erg[0].multiply(alphadot);
		return erg;
	}
//			NEU			NEU			NEU
	
//	Berechnet q,r mit a*this=g*q+r als Univariates Polynom in x_i.
//	Es gilt: (result[0],result[1])=(q,r)
//	(Sprich Pseudodivision)
	public QPolynomial[] pseudoDivide2(QPolynomial g,int i)
	{
		QPolynomial f = new QPolynomial(this);
		// Hier werden die Koeffizienten zwischengespeichert...
		Stack koeffizienten = new Stack();
		// Unsere Ergebnis-Polynome
		QPolynomial result[] = new QPolynomial[2];
		// Anfangs ist f unser Rest.
		result[1] = new QPolynomial (this);
		int k = result[1].getDegreeIn(i)-g.getDegreeIn(i);
		// Wenn deg(f)<deg(g), dann sind wir fertig und es gilt q=0,r=f
		if (k<0) return result;
		// Ansonsten muessen wir rechnen...
		while (k >= 0)
		{
			// t = LK(r in x_i) * x_i^k
			QPolynomial t = new QPolynomial((result[1].getLeadingCoefficient(i)).monomialMultiply(new QMonomial(Qelement.ONE,i,k)));
			// Wir sammeln unsere Koeffizienten auf dem Stack, da wir sie zum Schluss noch mit einem neuen Faktor multiplizieren muessen
			koeffizienten.push(t);
			// r =lk(g in i) * r - t*g
			result[1] = (g.getLeadingCoefficient(i).multiply(result[1])).subtract(t.multiply(g));
			// k = deg(r) - deg(g)
			k = result[1].getDegreeIn(i)-g.getDegreeIn(i);
		 }
		 // Nun muessen wir unser q nur noch aus den Elementen auf dem Stack rekonstruieren.
		 result[0] = new QPolynomial((QPolynomial)koeffizienten.pop());
		 f = new QPolynomial (Qelement.ONE);
		 // Nun nehmen wir alle Koeffizienten vom Stack und multiplizieren sie mit dem entgueltigen Faktor.
		 while (!(koeffizienten.empty()))
		 {
		 	f = f.multiply(g.getLeadingCoefficient(i));
		 	// q = q+ pop * f
		 	result[0] = result[0].add( ((QPolynomial)koeffizienten.pop()).multiply(f) );
		}
		return result;
	}
/**
 * Insert the method's description here.
 * Creation date: (05.01.2003 19:02:47)
 * @return arithmetik.QPolynomial
 * @param arg2 arithmetik.QPolynomial


	wird von CK benutzt, und zwar scheinbar synonym zu pseudoDivide2, also leite ich das erstmal weiter.
 
 */
public QPolynomial[] pseudoRemainder(QPolynomial arg2, int index) 
{
	return pseudoDivide2(arg2, index);
}
/**
 * Insert the method's description here.
 * Creation date: (21.02.2003 09:10:15)
 * @return arithmetik.QPolynomial
 * @param g arithmetik.QPolynomial

	Berechnet die Pseudodivision ohne die Teile, die für alpha und den Quotienten benötigt werden.
 
 */
public QPolynomial pseudoRemainder2(QPolynomial g, int ix) 
{
	QPolynomial r = this;
	int gdeg = g.getDegreeIn(ix);
	int delta = r.getDegreeIn(ix) - gdeg;

	if (delta < 0) return new QPolynomial(this);

	QPolynomial x = new QPolynomial(ix);

	while (delta >= 0)
	{
		r = (g.getLeadingCoefficient(ix).multiply(r)).subtract(r.getLeadingCoefficient(ix).multiply(g).multiply(x.pow(delta)));
		delta = r.getDegreeIn(ix) - gdeg;
		if (r.isZero()) delta = -1 - gdeg;
	}
		
	return r;
}
	// Diese Methode setzt alle Variablen ausser X_*identifierNr* auf 0 und approximiert
	// dann die restlichen Nullstellen mit Hilfe des QR-Verfahrens bis auf die Genauigkeit
	// Epsilon.
	public Celement[] qralgorithm(int index, Qelement epsilon)
	{
		QPolynomial work = new QPolynomial();
		for (int i=0; i<monom.size(); i++)
		{
			QMonomial m = new QMonomial((QMonomial)monom.elementAt(i));
			int k = m.getExponent(index);
			m.setExponent(index, 0);
			if (m.isConstant()) {m.setExponent(index, k); work = work.add(new QPolynomial(m));}
		}
		work = work.normalize();
		int grad = work.getDegreeIn(index);
		if (grad == 0) return new Celement[0];
		RingMatrix mat = new RingMatrix(new DoubleComplex(), grad);
		for (int i=2; i<=grad; i++)
			mat.setValue(new DoubleComplex(1.0), i, i-1);
		for (int i=0; i<work.monom.size(); i++)
		{
			QMonomial mon = (QMonomial)work.monom.elementAt(i);
			int k = mon.getExponent(index);
			if (k<grad) mat.setValue(new DoubleComplex(-mon.factor.doubleValue()), 1, grad-k);
		}
		RingVector v = mat.eigenvaluesOfHessenberg(epsilon.doubleValue());
		Celement[] erg = new Celement[v.getRows()];
		for (int i=0; i<erg.length; i++)
		{
			DoubleComplex d = (DoubleComplex)v.getValue(i+1);
			Qelement realteilUnten = epsilon.multiply(new Qelement((int)Math.floor(d.reel/epsilon.doubleValue())));
			Qelement realteilOben = epsilon.multiply(new Qelement((int)Math.ceil(d.reel/epsilon.doubleValue())));
			Qelement imagteilUnten = epsilon.multiply(new Qelement((int)Math.floor(d.imag/epsilon.doubleValue())));
			Qelement imagteilOben = epsilon.multiply(new Qelement((int)Math.ceil(d.imag/epsilon.doubleValue())));
			erg[i] = new Celement(new Relement(realteilUnten,realteilOben), new Relement(imagteilUnten, imagteilOben));
		}
		return erg;
	}
	public QPolynomial remainder(QPolynomial arg2)
	{
		return divideAndRemainder(arg2)[1];
	}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 14:00:03)
 * @param c java.util.Comparator
 */
public QPolynomial resort(Comparator c) 
{
	QPolynomial erg = new QPolynomial(this);
	QMonomial[] mons = new QMonomial[erg.monom.size()]; for (int i=0; i<erg.monom.size(); i++) mons[i] = (QMonomial)erg.monom.elementAt(i);
	final Comparator fc = c;
	Arrays.sort(mons,new Comparator(){
		public int compare(Object o1, Object o2) {return fc.compare( ((QMonomial)o1).exp, ((QMonomial)o2).exp);}
		public boolean equals(Object o1) {return (compare(this,o1)==0);}
	});
	erg.monom = new Vector();
	for (int i=0; i<mons.length; i++) erg.monom.addElement(mons[i]);
	return erg;
}
	// Liefert die Resultante der beiden Polynome bezüglich x_index.

	// funktioniert; die zweite Version arbeitet mit der divide-Methode statt mit pseudodivide
	public FastPolynomial resultant(QPolynomial arg2, int index)
	{
//		String f = this.toString();
//		String g = arg2.toString();
		if ((this.isZero()) || (arg2.isZero())) return new FastPolynomial();
		int l = this.getDegreeIn(index); 
		int m = arg2.getDegreeIn(index);
		if (m>l)
		{
			FastPolynomial erg = arg2.resultant(this, index);
			if ((m*l)%2 == 1) return erg.negate(); else return erg;
		}
		if (m==0) return (new FastPolynomial(arg2.getLeadingCoefficient(index))).pow(l);
		QPolynomial[] pseud = this.pseudoDivide(arg2, index);
		FastPolynomial erg = arg2.resultant(pseud[2], index);
//		String res = erg.toString();
//		String alpha = pseud[0].toString();
//		String r = pseud[2].toString();
		int k = pseud[2].getDegreeIn(index);
		erg = erg.multiply((new FastPolynomial(arg2.getLeadingCoefficient(index))).pow(l-k));
		if ((m*l) % 2 == 1) erg = erg.negate();
		erg = erg.divide((new FastPolynomial(pseud[0])).pow(m));
		return erg;
	}
	// Liefert die Resultante der beiden Polynome bezüglich x_index.

	// funktioniert nur bei univariaten Polynomen, arbeitet mit divide statt pseudodivide
	public FastPolynomial resultant2(QPolynomial arg2, int index)
	{
		if ((this.isZero()) || (arg2.isZero())) return new FastPolynomial();

		QPolynomial h = new QPolynomial(this);
		QPolynomial s = new QPolynomial(arg2);
		FastPolynomial res = new FastPolynomial(Qelement.ONE);

		while (s.getDegreeIn(index)>0)
		{
			QPolynomial r = h.remainder(s);
			res = res.multiply((new FastPolynomial(s.getLeadingCoefficient(index))).pow(h.getDegreeIn(index)-r.getDegreeIn(index)));
			if ((h.getDegreeIn(index)*s.getDegreeIn(index))%2 == 1) res = res.negate();
			h = s;
			s = r;
		}
		if ((h.isZero()) || (s.isZero())) return new FastPolynomial();
		return res.multiply(new FastPolynomial(s).pow(h.getDegreeIn(index)));
	}
/**
 * Insert the method's description here.
 * Creation date: (21.06.2002 11:35:08)
 * @return arithmetik.RQuotientExp
 * @param index int
 */
public RQuotientExp solve4(int index) 
{
	if (getDegreeIn(index)!=4) throw new RuntimeException ("Solve4 must be called with a polynomial of degree 4 ("+toString()+", varnr = "+index+")");

	QPolynomial[] koeff = new QPolynomial[5];
	for (int i=0; i<=4; i++) koeff[i] = getCoefficient(index, i);

	// Nenner eliminieren
	for (int i=0; i<=2; i++) koeff[i] = koeff[i].multiply(koeff[4].pow(3-i));
	// Konstante eliminieren
	koeff[2] = ((new QPolynomial(new Qelement(-6))).multiply(koeff[3].pow(2))).add(koeff[2]);
	koeff[1] = ((new QPolynomial(new Qelement(8))).multiply(koeff[3].pow(3))).subtract((new QPolynomial(new Qelement(2))).multiply(koeff[2]).multiply(koeff[3])).add(koeff[1]);
	koeff[0] = ((new QPolynomial(new Qelement(-3))).multiply(koeff[3].pow(4))).add(koeff[2].multiply(koeff[3].pow(2))).add(koeff[0]);
	
	// Das feste Polynom zur Bestimmung von b in sqrt(a)+sqrt(b+c*sqrt(a)) wird zusammengesetzt.
	// Dieses Polynom ist 64 b^3 + 64p b^2 + (20p^2 - 16 r) b + (2p^3+q^2-8rp) 

	int bnr = getHighestIndex()+1;
	QPolynomial b = new QPolynomial(bnr);
	
	QPolynomial bbestimm =  b.pow(3).multiply(new QPolynomial(new Qelement(64)));
	bbestimm = bbestimm.add(b.pow(2).multiply(koeff[2].multiply(new QPolynomial(new Qelement(64)))));
	bbestimm = bbestimm.add(b.pow(1).multiply(koeff[2].pow(2).multiply(new QPolynomial(new Qelement(20))).add
											 (koeff[0].multiply(new QPolynomial(new Qelement(-16))))));
	
	bbestimm = bbestimm.add(b.pow(0).multiply(koeff[2].pow(3).multiply(new QPolynomial(new Qelement(2))).add
											 (koeff[1].pow(2).multiply(new QPolynomial(new Qelement(1))).add
											 (koeff[2].multiply(koeff[0]).multiply(new QPolynomial(new Qelement(-8)))))));
	
	Vector blsg = bbestimm.getLinearZeros(bnr);

	if (blsg.size() == 0) return null;

	for (int i=0; i<blsg.size(); i++)
	{
		RQuotientExp p = new RQuotientExp(new RExpression(koeff[2]));
		RQuotientExp q = new RQuotientExp(new RExpression(koeff[1]));
		RQuotientExp lb = (RQuotientExp)blsg.elementAt(i);
		RQuotientExp la = (p.negate().subtract(lb)).multiply(new RQuotientExp(new RExpression(Qelement.HALF)));
		RQuotientExp lc = q.divide(p.multiply(new RQuotientExp(Qelement.TWO)).add(
								   lb.multiply(new RQuotientExp(new Qelement(4)))));
		RQuotientExp lsg = la.sqrt().add( (lb.add(lc.multiply(la.sqrt()))).sqrt() );
		lsg = lsg.add(new RQuotientExp(new RExpression(koeff[3])));
		lsg = lsg.divide(new RQuotientExp(new RExpression(koeff[4])));
		if (evaluate(index, lsg).isZero()) return lsg;
	}
	return null;
}
/**
 * Gibt einen Vector mit allen Nullstellen des Polynoms zurück, die durch Wurzeln ausdrückbar sind.
 * läuft bis Polynomgrad 2.
 * Creation date: (17.06.2002 10:12:02)
 * @return arithmetik.RQuotientExp
 * @param index int
 */
public Vector solveEasy(int index) 
{
	int d = getDegreeIn(index);
	if (d > 2) return new Vector();
	QPolynomial a = getCoefficient(index, 2);
	QPolynomial b = getCoefficient(index, 1);
	QPolynomial c = getCoefficient(index, 0);
	Vector erg = new Vector();
	if (a.isZero()) erg.addElement(new RQuotientExp(new RExpression(c.negate()), new RExpression(b)));
	else {
		RExpression zaehler1 = (new RExpression(b.sqr().subtract(a.multiply(c).multiply(new QPolynomial(new Qelement(4)))))).sqrt().subtract(new RExpression(b));
		RExpression zaehler2 = (new RExpression(b.sqr().add     (a.multiply(c).multiply(new QPolynomial(new Qelement(4)))))).sqrt().subtract(new RExpression(b));
		erg.addElement(new RQuotientExp(zaehler1, new RExpression(a.multiply(new QPolynomial(Qelement.TWO)))));
		erg.addElement(new RQuotientExp(zaehler2, new RExpression(a.multiply(new QPolynomial(Qelement.TWO)))));
	}
	return erg;	
}
/**
 * Insert the method's description here.
 * Creation date: (10.02.2003 11:12:57)
 * @return arithmetik.RQuotientExp[]
 * @param eqn arithmetik.QPolynomial[]
 * @param var int[]

	Ruft nur die private SolveSystem-Methode mit den nötigen Parametern auf. Gibt einen Vector mit
	RQuotientExp[] zurück, die jeweils in der selben Reihenfolge wie var alle Lösungen des Gleichungssystems enthalten.
 
 */
public static Vector solveSystem(QPolynomial[] eqn, int[] var) 
{
	Vector[] eqns = new Vector[eqn.length];
	for (int i=0; i<eqn.length; i++) {eqns[i] = new Vector(); eqns[i].addElement(eqn[i]);}
	return solveSystem(eqns, var, false);
}
/**
 * Insert the method's description here.
 * Creation date: (09.02.2003 10:14:51)
 * @return arithmetik.RQuotientExp[]
 * @param equation java.util.Vector[]
 * @param variable int[]

 	gibt ein Vector von RQuotientExp[] zurück, die alle Lösungen des Gleichungssystems enthalten.
 
 */
protected static Vector solveSystem(Vector[] equation, int[] variable, boolean allFactorized) 
{
	if (variable.length==0) return new Vector();
	System.out.println("solveSytem aufgerufen mit ");
	for (int i=0; i<equation.length; i++)
	{
		String s = "Gleichung "+i+" :";
		for (int j=0; j<equation[i].size(); j++)
		{
			if (j!=0) s += " * ";
			s = s+ "{ "+equation[i].elementAt(j)+" }";
		}
		System.out.println(s);
	}
	String s = "Variablen : ";
	for (int i=0; i<variable.length; i++)
		s = s+="X"+variable[i]+", ";
	System.out.println(s);

	int anz = 0; for (int i=0; i<equation.length; i++) anz += equation[i].size();
	anz *= variable.length;
	
	Tupel[]	faktorenInVar = new Tupel[anz];				// 4-Tupel: Gl, faknr, variable, deg
	int nr = 0;
	for (int i=0; i<equation.length; i++)
		for (int j=0; j<equation[i].size(); j++)
		{
			QPolynomial p = (QPolynomial)equation[i].elementAt(j);
			for (int k=0; k<variable.length; k++) faktorenInVar[nr++] = new Tupel(new int[]{i,j,k,p.getDegreeIn(variable[k])});
		}

	Arrays.sort(faktorenInVar, new Comparator(){
		public int compare(Object o1, Object o2) {if (((Tupel)o1).data[3]<((Tupel)o2).data[3]) return -1;
												  if (((Tupel)o1).data[3]>((Tupel)o2).data[3]) return 1;
												  return 0;}
		public boolean equals(Object o) {return (compare(this,o)==0);}
												  
		});

	// Test, ob eins einfach umgedreht werden kann. Wenn noch nicht faktorisert
	// wurde, werden nur die mit Grad <=2 probiert, ansonsten faktorisieren wir lieber erst.
	for (int i=0; i<faktorenInVar.length; i++)
	{
		QPolynomial p = (QPolynomial)equation[faktorenInVar[i].data[0]].elementAt(faktorenInVar[i].data[1]);
		int ix = variable[faktorenInVar[i].data[2]];
		int deg = faktorenInVar[i].data[3];
		if ((deg>0) && (((allFactorized) && (faktorenInVar[i].data[3]<=2)) || (p.isProbablySquarerootsolvable(ix))))
		{
			RQuotientExp[] lsg = p.findSquarerootZeros(ix);
			
			Vector erg = new Vector();
			for (int j=0; j<lsg.length; j++)
			{
				Vector rekErgVec = solveSystemTestPartial(equation, variable, ix, lsg[j]);
				for (int l=0; l<rekErgVec.size(); l++)
				{
					RQuotientExp[] rekErg = (RQuotientExp[])rekErgVec.elementAt(l);
					int vnr=0; while (variable[vnr]!=ix) vnr++;	
					for (int k=0; k<rekErg.length; k++)
						if (k!=vnr) rekErg[vnr] = rekErg[vnr].evaluate(variable[k], rekErg[k]);
					erg.addElement(rekErg);
				}
			}
			return erg;
		}
	}

	// Muss noch faktorisiert werden?
	if (!allFactorized)
	{
		Vector[] neuEq = new Vector[equation.length];
		for (int i=0; i<equation.length; i++)
		{
			neuEq[i] = new Vector();
			for (int j=0; j<equation[i].size(); j++)
			{
				QPolynomial[] faks = ((QPolynomial)equation[i].elementAt(j)).factorize();
				for (int k=0; k<faks.length; k++) neuEq[i].addElement(faks[k]);
			}
		}
		return solveSystem(neuEq, variable, true);
	}

	// Jetzt alle Kombinationen von Faktoren durchlaufen
	Vector ergV = new Vector();
	int[] faknr = new int[equation.length];
	for (int i=0; i<faknr.length; i++) faknr[i] = 0;
	boolean weiter = true;
	while (weiter)
	{
		Vector irEqn = new Vector();
		for (int i=0; i<equation.length; i++)
		{
			QPolynomial p = (QPolynomial)equation[i].elementAt(faknr[i]);
			boolean schonDrin = false;
			for (int j=0; (!schonDrin) && (j<irEqn.size()); j++) 
				if (p.equals((QPolynomial)irEqn.elementAt(j))) schonDrin = true;
			if (!schonDrin)	irEqn.addElement(p);
		}

		for (int varnr = 0; varnr < variable.length; varnr++)
		{
			int[] neuvar = new int[Math.min(variable.length-1,irEqn.size()-1)];	

			int i=0; int j=0;
			for (j=0; (j<variable.length) && (i<neuvar.length); j++)
				if (j!=varnr) neuvar[i++] = variable[j];

			// alle überzähligen Variablen werden auf 0 gesetzt; wenn es allgemein eine gültige
			// Lösung gibt, dann auch eine an der Stelle 0.
			int von = j;
			for (j=von; j<variable.length; j++)
				if (j!=varnr)
				{
					for (int k=0; k<irEqn.size(); k++)
					{
						QPolynomial p = (QPolynomial)irEqn.elementAt(k);
						p = p.evaluate(variable[j],Qelement.ZERO);
						irEqn.setElementAt(p,k);
					}
				}

			Vector[] rekEquation = new Vector[irEqn.size()];		// für rekursiven Aufruf
			for (i=0; i<irEqn.size(); i++) 
			{
				rekEquation[i] = new Vector(); 
				rekEquation[i].addElement(irEqn.elementAt(i));
			}

			// wir könnten jetzt noch zuviele Gleichungen haben, da gehen wir
			// alle Kombinationen durch

			boolean[] auswahl = new boolean[irEqn.size()];
			for (i=0; i<neuvar.length+1; i++) auswahl[i] = true;
			for (i=neuvar.length+1; i<irEqn.size(); i++) auswahl[i] = false;
			QPolynomial[] rek = new QPolynomial[neuvar.length+1];			
			
			boolean inWeiter = true;
			QPolynomial resultant = ZERO;
			while (inWeiter)
			{
				i=0;
				for (j=0; j<auswahl.length; j++) 
					if (auswahl[j]) rek[i++] = (QPolynomial)irEqn.elementAt(j);

				resultant = resultant.gcd(multiResultant(rek, neuvar));
									
				// auswahl weiterzählen
				int anzTrues = 0;
				while ((anzTrues < auswahl.length) && (auswahl[auswahl.length-1-anzTrues])) 
					anzTrues++;

				int erstesWiedertrue = auswahl.length-anzTrues-1;
				while ((erstesWiedertrue >= 0) && (!auswahl[erstesWiedertrue])) erstesWiedertrue--;

				if (erstesWiedertrue>=0)
				{
					auswahl[erstesWiedertrue] = false;
					for (i=0; i<anzTrues+1; i++) auswahl[erstesWiedertrue+1+i] =  true;
					for (i=erstesWiedertrue+anzTrues+1; i<auswahl.length; i++) auswahl[i] = false;
				} else inWeiter = false;
			}
			RQuotientExp[] partial = resultant.findSquarerootZeros(variable[varnr]);
			for (i=0; i<partial.length; i++)
			{
				Vector lsgVec = solveSystemTestPartial(rekEquation, neuvar, variable[varnr], partial[i]);
				for (int l=0; l<lsgVec.size(); l++)
				{
					RQuotientExp[] lsg = (RQuotientExp[])lsgVec.elementAt(l);
					RQuotientExp[] erg = new RQuotientExp[variable.length];
					int k=0;
					for (j=0; j<lsg.length; j++) 
						if (j!=varnr) erg[j] = lsg[k++];
						else erg[j] = partial[i];
					while (j<erg.length) 
						if (j!=varnr) erg[j++] = RQuotientExp.ZERO;
						else erg[j++] = partial[i];
					ergV.addElement(erg);
				}
			}
		}

		// faknr weiterzählen
		int i=0; 
		while ((i < faknr.length) && (faknr[i] == equation[i].size()-1)) {faknr[i]=0; i++;}
		if (i == faknr.length) weiter = false; else faknr[i]++;
	}
	
	// ergV zurück (ist leer, falls nix gefunden wurde)
	return ergV;
}
/**
 * Löst das Gleichungssystem easy, falls möglich. Erwartet teilerfremde Teilgleichungen, und falls 2 Faktoren
 * identisch sind, sollen sie auch durch das selbe Objekt repräsentiert werden. Gibt alle Lösungen in einem Vektor
 * zurück, wobei jeder Vektor die Lösungen an den Stellen hat wie die Variabeln in variables.
 * Creation date: (16.06.2002 09:26:42)
 * @return arithmetik.RQuotientExp[]
 * @param equation java.util.Vector[]
 * @param variables int[]
 */
public static Vector solveSystemEasy(Vector[] equation, int[] variables) 
{
	System.out.println("solveSytemEasy aufgerufen mit ");
	for (int i=0; i<equation.length; i++)
	{
		String s = "Gleichung "+i+" :";
		for (int j=0; j<equation[i].size(); j++)
		{
			if (j!=0) s += " * ";
			s = s+ "{ "+equation[i].elementAt(j)+" }";
		}
		System.out.println(s);
	}
	String s = "Variablen : ";
	for (int i=0; i<variables.length; i++)
		s = s+="X"+variables[i]+", ";
	System.out.println(s);
	// Vorfaktorisiern, Elimination doppelter Faktoren
	factorSystemSimple(equation);

	// Array für jeden Faktor der ersten Gleichung, in dem die Zeilen stehen, die diesen Faktoren enthalten.
	Vector[] zeilenRaus = new Vector[equation[0].size()];
	for (int i=0; i<equation[0].size(); i++) 
		zeilenRaus[i] = new Vector();

	// Für jede Variable ihre Stelle im variables-Array und der maximal Grad, indem diese Variable vorkommt.
	// wird später sortiert nach dem Maxgrad.
	int[][] stelleMaxgrad = new int[variables.length][2];
	for (int i=0; i<variables.length; i++) {stelleMaxgrad[i][0] = i; stelleMaxgrad[i][1] = -1;}

	// Aufbau der beiden Arrays
	for (int i=1; i<equation.length; i++)
	{
		boolean[] faelltWeg = new boolean[equation[0].size()];
		for (int j=0; j<equation[0].size(); j++) faelltWeg[j] = false;
		for (int j=0; j<equation[i].size(); j++)
		{
			QPolynomial p = (QPolynomial)equation[i].elementAt(j);
			for (int k=0; k<equation[0].size(); k++)
				if (equation[0].elementAt(k) == p) faelltWeg[k] = true;
			int min = Integer.MAX_VALUE;
			int minvar = -1;
			for (int k=0; k<variables.length; k++)
			{
				int g = p.getDegreeIn(variables[k]);
				stelleMaxgrad[k][1] = Math.max(stelleMaxgrad[k][1], g);
			}
		}
		for (int j=0; j<equation[0].size(); j++) 
		if (faelltWeg[j]) zeilenRaus[j].addElement(new Integer(i));
	}
	Arrays.sort(stelleMaxgrad, new Comparator(){
		public int compare(Object o1, Object o2) {if (((int[])o1)[1]<((int[])o2)[1]) return 1;
									   if (((int[])o1)[1]>((int[])o2)[1]) return -1;
									   return 0;}
		public boolean equals(Object o) 		{return compare(this,o)==0;}
	});

	Vector ergvec = new Vector();
	while (equation[0].size()>0)
	{
		// Suche nach dem Faktor, der am meisten Gleichungen eliminieren kann, und darunter den mit dem geringsten
		// Totalgrad.
		QPolynomial pivotp = (QPolynomial)equation[0].lastElement();
		// Dieses int enthält die Anzahl der Zeilen, die pivotp enthalten und demzufolge ignoriert werden können
		int maxZeilen = zeilenRaus[equation[0].size()-1].size();

		// Suche nach der Pivotvariable (ihre Stelle im stelleMaxgrad-Array wird in pivotvar gespeichert)
		int pivotvar = -1, grad = Integer.MAX_VALUE;
		for (int i=0; i<stelleMaxgrad.length-maxZeilen; i++)
		{
			int g = pivotp.getDegreeIn(variables[stelleMaxgrad[i][0]]);
			if ((g>0) && (g < grad)) {pivotvar = i; grad = g;}
		}
		
		// Falls pivotp konstant ist in allen Variablen, die eigentlich nicht eliminiert werden sollten,
		// muss die schlechteste dieser Variablen gegen eine getauscht werden, die in pivotp vorkommt.
		if (pivotvar == -1)
		{
			int i=stelleMaxgrad.length-maxZeilen;
			while (pivotp.getDegreeIn(variables[stelleMaxgrad[i][0]])==0) i++;
			pivotvar = stelleMaxgrad.length-maxZeilen-1;
			int t = stelleMaxgrad[i][0];
			stelleMaxgrad[i][0] = stelleMaxgrad[pivotvar][0];
			stelleMaxgrad[pivotvar][0] = t;
		}

		// pivotp wird gelöst.
		Vector lsgpivot = pivotp.solveEasy(variables[stelleMaxgrad[pivotvar][0]]);

		// Falls es Lösungen von pivotp gibt, sind alle konjugiert zueinander, und es genügt, eine zu testen.
		if (lsgpivot.size()>0)
		{
			RQuotientExp lsg = (RQuotientExp)lsgpivot.elementAt(0);
			// Für den rekursiven Aufruf werden die Variablen vorbereitet. Genommen werden die vorderen aus dem
			// Array stelleMaxgrad außer der Pivotvariable.
			int[] neuvar = new int[variables.length-maxZeilen-1];
			int j = 0;
			for (int i=0; i<stelleMaxgrad.length-maxZeilen; i++)
				if (i!=pivotvar) neuvar[j++] = variables[stelleMaxgrad[i][0]];

			// Und vorbereiten der neuen Zeilen als resultante von fp und der anderen Zeile. Die in zeilenRaus
			// markierten Zeilen und die 0te werden ausgelassen.
			FastPolynomial fp = new FastPolynomial(pivotp);
			RQuotientExp[] neueq = new RQuotientExp[equation.length - maxZeilen - 1];
			j = 0;
			for (int i=1; i<equation.length; i++)
			{
				boolean istDrin = false;
				for (int k=0; k<zeilenRaus[equation[0].size()-1].size(); k++) if (i == ((Integer)zeilenRaus[equation[0].size()-1].elementAt(k)).intValue()) istDrin = true;
				if (!istDrin)
				{
					FastPolynomial res = fp.resultant(new FastPolynomial(equation[i]), variables[stelleMaxgrad[pivotvar][0]]);
					neueq[j++] = (new RQuotientExp(new RExpression(res))).evaluate(variables[pivotvar], lsg);
				}
			}

			// jetzt den Rest lösen, falls noch ein Rest da ist (ansonsten bleibt lsgandere null und wird auch nicht 
			// mehr benutzt).
			if (neuvar.length>0) 
			{
				Vector lsgandere = RQuotientExp.solveSystemEasy(neueq, neuvar);

				// Für alle Lösungen muss jetzt getestet werden, ob sie das Ursprungsystem lösen.
				for (int lsgannr = 0; lsgannr<lsgandere.size(); lsgannr++)
				{
					RQuotientExp[] dieseLoesungAndere = (RQuotientExp[])lsgandere.elementAt(lsgannr);
					RQuotientExp[] erg = new RQuotientExp[variables.length];
					j=0;
					for (int i=0; i<stelleMaxgrad.length-maxZeilen; i++)
						if (i!=pivotvar) 
						{
							erg[stelleMaxgrad[i][0]] = dieseLoesungAndere[j];
							lsg = lsg.evaluate(variables[stelleMaxgrad[i][0]],dieseLoesungAndere[j]);
							j++;
						}
					for (int i=stelleMaxgrad.length-maxZeilen; i<stelleMaxgrad.length; i++)
						erg[i] = new RQuotientExp();

					erg[stelleMaxgrad[pivotvar][0]] = lsg;

					int i=0;
					while ((i<equation.length) && ((new FastPolynomial(equation[i])).evaluate(variables,erg).isZero())) i++;
					if (i>= equation.length) ergvec.addElement(erg);					
				}
			}
		}

		// pivotp aus allen Gleichungen entfernen; falls dabei eine leer wird, kann sofort zurückgesprungen werden.
		for (int i=1; i<equation.length; i++)
		{
			for (int j=0; j<equation[i].size(); j++)
				if (equation[i].elementAt(j)==pivotp) 
				{
					equation[i].removeElementAt(j);
					j--;
					if (equation[i].size()==0) return ergvec;
				}
		}
		// insbesondere wird pivotp aus der ersten Gleichung gelöscht:
		equation[0].removeElementAt(equation[0].size()-1);
	}

	return ergvec;
	
		
	
	
		
	
	

	/*
		
	
	int[] vars = new int[equation.length];
	for (int i=0; i<vars.length; i++)
		vars[i] = maxgrad[i][0];
	Vector qual = new Vector();
	for (int i=0; i<qualitaet.length; i++) qual.addElement(qualitaet[i]);
	boolean weiter = true;
	while (weiter)
	{
		int[] best = (int[])qual.lastElement();
		QPolynomial p = (QPolynomial)equation[best[0]].elementAt(best[1]);
		if (best[2]>4) return null;										// Easysolve geht schief, nichts mehr unter grad 4 da.
		Vector wegzeilen = new Vector();
		wegzeilen.addElement(new Integer(best[0]));
		int i = qual.size()-2;
		while ((i>0) && (((int[])qual.elementAt(i))[2]==best[2]))
		{
			int off = ((int[])qual.elementAt(i))[0];
			boolean schondrin = false;
			for (int j=0; j<wegzeilen.size(); j++) if (off==((Integer)wegzeilen.elementAt(j)).intValue()) schondrin = true;
			if (!schondrin) wegzeilen.addElement(new Integer(off));
		}
		Vector[] neuzeilen = new Vector[equation.length - wegzeilen.size()];
		i = 0;
		for (int j=0; j<equation.length; j++)
		{
			boolean schondrin = false;
			for (int k=0; k<wegzeilen.size(); k++) if (j==((Integer)wegzeilen.elementAt(k)).intValue()) schondrin = true;
			if (!schondrin) neuzeilen[i++] = new Vector(equation[j]);
		}
		int[] neuvar = new int[vars.length-1];
		i=0;
		for (int j=0; j<vars.length; j++)
			if (vars[j]!=best[3]) neuvar[i++] = vars[j];

			
			// Fehler: Die Lösung muss zunächs eingesetzt werden.
			
			RQuotientExp[] rest = solveMinimalSystemEasy(neuzeilen, neuvar);


		
		if (rest != null)
		{
			weiter = false;
			RQuotientExp lsg = p.easySolveTo(best[3]);
			RQuotientExp[] erg = new RQuotientExp[variables.length];
			for (i=0; i<variables.length; i++)
				erg[i] = new RQuotientExp();
			int j = 0;
			int e = -1;
			while ((e==-1) && (j<variables.length)) if (variables[j]==best[3]) e = j;
			
			erg[e] = lsg;
			
			for (i=0; i<neuvar.length; i++)
			{
				j = 0;
				e = -1;
				while ((e==-1) && (j<variables.length)) if (variables[j]==neuvar[i]) e = j;
				if (e!=-1) erg[e] = rest[i];
			}
			return erg;
		} else {
			equation[best[0]].removeElementAt(best[1]);
			boolean zeileWurdeLeer = (equation[best[0]].size()==0);
			for (i=0; i<wegzeilen.size(); i++)
			{
				Vector v = equation[((Integer)wegzeilen.elementAt(i)).intValue()];
				v.removeElement(p);
				if (v.size()==0) zeileWurdeLeer = true;
			}
			if (zeileWurdeLeer)	return null;											// Keine Kombination erfolgreich.
			qual.removeElementAt(qual.size()-1);
		}
	}
*/
}
/**
 * Löst das Gleichungssystem easy, falls möglich. Erwartet teilerfremde Teilgleichungen, und falls 2 Faktoren
 * identisch sind, sollen sie auch durch das selbe Objekt repräsentiert werden.
 * Creation date: (16.06.2002 09:26:42)
 * @return arithmetik.RQuotientExp[]
 * @param equation java.util.Vector[]
 * @param variables int[]
 */
public static RQuotientExp[] solveSystemEasy_alt(Vector[] equation, int[] variables) 
{
	System.out.println("solveSytemEasy aufgerufen mit ");
	for (int i=0; i<equation.length; i++)
	{
		String s = "Gleichung "+i+" :";
		for (int j=0; j<equation[i].size(); j++)
			s = s+ " * "+equation[i].elementAt(j);
		System.out.println(s);
	}
	String s = "Variablen : ";
	for (int i=0; i<variables.length; i++)
		s = s+="X"+variables[i]+", ";
	System.out.println(s);
	// Vorfaktorisiern, Elimination doppelter Faktoren
	factorSystemSimple(equation);

	// Array für jeden Faktor der ersten Gleichung, in dem die Zeilen stehen, die diesen Faktoren enthalten.
	Vector[] zeilenRaus = new Vector[equation[0].size()];
	for (int i=0; i<equation[0].size(); i++) 
		zeilenRaus[i] = new Vector();

	// Für jede Variable ihre Stelle im variables-Array und der maximal Grad, indem diese Variable vorkommt.
	// wird später sortiert nach dem Maxgrad.
	int[][] stelleMaxgrad = new int[variables.length][2];
	for (int i=0; i<variables.length; i++) {stelleMaxgrad[i][0] = i; stelleMaxgrad[i][1] = -1;}

	// Aufbau der beiden Arrays
	for (int i=1; i<equation.length; i++)
	{
		boolean[] faelltWeg = new boolean[equation[0].size()];
		for (int j=0; j<equation[0].size(); j++) faelltWeg[j] = false;
		for (int j=0; j<equation[i].size(); j++)
		{
			QPolynomial p = (QPolynomial)equation[i].elementAt(j);
			for (int k=0; k<equation[0].size(); k++)
				if (equation[0].elementAt(k) == p) faelltWeg[k] = true;
			int min = Integer.MAX_VALUE;
			int minvar = -1;
			for (int k=0; k<variables.length; k++)
			{
				int g = p.getDegreeIn(variables[k]);
				stelleMaxgrad[k][1] = Math.max(stelleMaxgrad[k][1], g);
			}
		}
		for (int j=0; j<equation[0].size(); j++) 
		if (faelltWeg[j]) zeilenRaus[j].addElement(new Integer(i));
	}
	Arrays.sort(stelleMaxgrad, new Comparator(){
		public int compare(Object o1, Object o2) {if (((int[])o1)[1]<((int[])o2)[1]) return 1;
									   if (((int[])o1)[1]>((int[])o2)[1]) return -1;
									   return 0;}
		public boolean equals(Object o) 		{return compare(this,o)==0;}
	});

	// Endlosschleife, in der alle Faktoren von equation[0] durchprobiert werden, ob sich
	// Lösungen ergeben.
	while (true)
	{
		// Suche nach dem Faktor, der am meisten Gleichungen eliminieren kann, und darunter den mit dem geringsten
		// Totalgrad.
		int maxZeilen = 0;
		for (int i=0; i<equation[0].size(); i++) maxZeilen = Math.max(maxZeilen, zeilenRaus[i].size());
		int pivot = -1, mingrad = Integer.MAX_VALUE;
		for (int i=0; i<equation[0].size(); i++)
		{
			if ((zeilenRaus[i].size()==maxZeilen) && (((QPolynomial)equation[0].elementAt(i)).getTotalDegree()<mingrad))
			{
				mingrad = ((QPolynomial)equation[0].elementAt(i)).getTotalDegree();
				pivot = i;
			}
		}
		QPolynomial pivotp = (QPolynomial)equation[0].elementAt(pivot);

		// Suche nach der Pivotvariable (ihre Stelle im stelleMaxgrad-Array wird in pivotvar gespeichert)
		int pivotvar = -1, grad = Integer.MAX_VALUE;
		for (int i=0; i<stelleMaxgrad.length-maxZeilen; i++)
		{
			int g = pivotp.getDegreeIn(variables[stelleMaxgrad[i][0]]);
			if ((g>0) && (g < grad)) {pivotvar = i; grad = g;}
		}
		// Falls pivotp konstant ist in allen Variablen, die eigentlich nicht eliminiert werden sollten,
		// muss die schlechteste dieser Variablen gegen eine getauscht werden, die in pivotp vorkommt.
		if (pivotvar == -1)
		{
			int i=stelleMaxgrad.length-maxZeilen;
			while (pivotp.getDegreeIn(variables[stelleMaxgrad[i][0]])==0) i++;
			pivotvar = stelleMaxgrad.length-maxZeilen-1;
			int t = stelleMaxgrad[i][0];
			stelleMaxgrad[i][0] = stelleMaxgrad[pivotvar][0];
			stelleMaxgrad[pivotvar][0] = t;
		}

		// pivotp wird gelöst.
		RQuotientExp lsg = (RQuotientExp)pivotp.solveEasy(variables[stelleMaxgrad[pivotvar][0]]).elementAt(0);

		RQuotientExp[] lsgandere = null;
		// weiter lohnt sich nur, wenn nicht schon lsg null ist
		if (lsg != null)
		{
			// Für den rekursiven Aufruf werden die Variablen vorbereitet. Genommen werden die vorderen aus dem
			// Array stelleMaxgrad außer der Pivotvariable.
			int[] neuvar = new int[variables.length-maxZeilen-1];
			int j = 0;
			for (int i=0; i<stelleMaxgrad.length-maxZeilen; i++)
				if (i!=pivotvar) neuvar[j++] = variables[stelleMaxgrad[i][0]];

			// Und vorbereiten der neuen Zeilen als resultante von fp und der anderen Zeile. Die in zeilenRaus
			// markierten Zeilen und die 0te werden ausgelassen.
			FastPolynomial fp = new FastPolynomial(pivotp);
			RQuotientExp[] neueq = new RQuotientExp[equation.length - maxZeilen - 1];
			j = 0;
			for (int i=1; i<equation.length; i++)
			{
				boolean istDrin = false;
				for (int k=0; k<zeilenRaus[pivot].size(); k++) if (i == ((Integer)zeilenRaus[pivot].elementAt(k)).intValue()) istDrin = true;
				if (!istDrin)
				{
					FastPolynomial res = fp.resultant(new FastPolynomial(equation[i]), variables[stelleMaxgrad[pivotvar][0]]);
					neueq[j++] = (new RQuotientExp(new RExpression(res))).evaluate(variables[pivotvar], lsg);
				}
			}

			// jetzt den Rest lösen, falls noch ein Rest da ist (ansonsten bleibt lsgandere null und wird auch nicht 
			// mehr benutzt).
			if (neuvar.length>0) 
			{
				lsgandere = (RQuotientExp[])RQuotientExp.solveSystemEasy(neueq, neuvar).elementAt(0); 
				if (lsgandere == null) lsg = null;
			}
		}
		
		// Falls alles gut gegangen ist, wird die Lösung hier zusammengesetzt; außer lsg werden alle andere nur
		// an die richtige Stelle abgeschrieben, in lsg werden alle anderen noch eingesetzt.
		if (lsg!=null)
		{
			RQuotientExp[] erg = new RQuotientExp[variables.length];
			int j=0;
			for (int i=0; i<stelleMaxgrad.length-maxZeilen; i++)
				if (i!=pivotvar) 
				{
					erg[stelleMaxgrad[i][0]] = lsgandere[j];
					lsg = lsg.evaluate(variables[stelleMaxgrad[i][0]],lsgandere[j]);
					j++;
				}
			for (int i=stelleMaxgrad.length-maxZeilen; i<stelleMaxgrad.length; i++)
				erg[i] = new RQuotientExp();

			erg[stelleMaxgrad[pivotvar][0]] = lsg;
			
			return erg;	
		}

		if (equation[0].size()==1) return null;
		
		Vector[] neuzr = new Vector[zeilenRaus.length-1];
		int i=0;
		for (int j=0; j<zeilenRaus.length; j++)
			if (j!=pivot) neuzr[i++] = zeilenRaus[j];

		equation[0].removeElementAt(pivot);
	}
	
		
	
	
		
	
	

	/*
		
	
	int[] vars = new int[equation.length];
	for (int i=0; i<vars.length; i++)
		vars[i] = maxgrad[i][0];
	Vector qual = new Vector();
	for (int i=0; i<qualitaet.length; i++) qual.addElement(qualitaet[i]);
	boolean weiter = true;
	while (weiter)
	{
		int[] best = (int[])qual.lastElement();
		QPolynomial p = (QPolynomial)equation[best[0]].elementAt(best[1]);
		if (best[2]>4) return null;										// Easysolve geht schief, nichts mehr unter grad 4 da.
		Vector wegzeilen = new Vector();
		wegzeilen.addElement(new Integer(best[0]));
		int i = qual.size()-2;
		while ((i>0) && (((int[])qual.elementAt(i))[2]==best[2]))
		{
			int off = ((int[])qual.elementAt(i))[0];
			boolean schondrin = false;
			for (int j=0; j<wegzeilen.size(); j++) if (off==((Integer)wegzeilen.elementAt(j)).intValue()) schondrin = true;
			if (!schondrin) wegzeilen.addElement(new Integer(off));
		}
		Vector[] neuzeilen = new Vector[equation.length - wegzeilen.size()];
		i = 0;
		for (int j=0; j<equation.length; j++)
		{
			boolean schondrin = false;
			for (int k=0; k<wegzeilen.size(); k++) if (j==((Integer)wegzeilen.elementAt(k)).intValue()) schondrin = true;
			if (!schondrin) neuzeilen[i++] = new Vector(equation[j]);
		}
		int[] neuvar = new int[vars.length-1];
		i=0;
		for (int j=0; j<vars.length; j++)
			if (vars[j]!=best[3]) neuvar[i++] = vars[j];

			
			// Fehler: Die Lösung muss zunächs eingesetzt werden.
			
			RQuotientExp[] rest = solveMinimalSystemEasy(neuzeilen, neuvar);


		
		if (rest != null)
		{
			weiter = false;
			RQuotientExp lsg = p.easySolveTo(best[3]);
			RQuotientExp[] erg = new RQuotientExp[variables.length];
			for (i=0; i<variables.length; i++)
				erg[i] = new RQuotientExp();
			int j = 0;
			int e = -1;
			while ((e==-1) && (j<variables.length)) if (variables[j]==best[3]) e = j;
			
			erg[e] = lsg;
			
			for (i=0; i<neuvar.length; i++)
			{
				j = 0;
				e = -1;
				while ((e==-1) && (j<variables.length)) if (variables[j]==neuvar[i]) e = j;
				if (e!=-1) erg[e] = rest[i];
			}
			return erg;
		} else {
			equation[best[0]].removeElementAt(best[1]);
			boolean zeileWurdeLeer = (equation[best[0]].size()==0);
			for (i=0; i<wegzeilen.size(); i++)
			{
				Vector v = equation[((Integer)wegzeilen.elementAt(i)).intValue()];
				v.removeElement(p);
				if (v.size()==0) zeileWurdeLeer = true;
			}
			if (zeileWurdeLeer)	return null;											// Keine Kombination erfolgreich.
			qual.removeElementAt(qual.size()-1);
		}
	}
*/
}
/**
 * Insert the method's description here.
 * Creation date: (09.02.2003 18:24:26)
 * @return arithmetik.RQuotientExp[]
 * @param equation java.util.Vector[]
 * @param variable int[]
 * @param ix int
 * @param partial arithmetik.RQuotientExp

	Nur von solveSystem aufgerufen. Setzt eine Partiallösung ein, ruft solve Rekursiv auf, und liefert die Rückgabe
	weiter.
 
 */
private static Vector solveSystemTestPartial(Vector[] equation, int[] variable, int ix, RQuotientExp partial) 
{
	int neuVarLength = variable.length;
	for (int i=0; i<variable.length; i++) if (variable[i] == ix) neuVarLength--;
	
	int[] neuVar = new int[neuVarLength];
	int k=0;
	for (int j=0; j<variable.length; j++) if (variable[j]!=ix) neuVar[k++] = variable[j];

	RQuotientExp[] neuEqu = new RQuotientExp[equation.length];
	for (k=0; k<equation.length; k++)
	{
		neuEqu[k] = RQuotientExp.ONE;
		for (int m=0; m<equation[k].size(); m++)
		{
			QPolynomial f = (QPolynomial)equation[k].elementAt(m);
			neuEqu[k] = neuEqu[k].multiply(f.evaluate(ix,partial));
		}
	}
	Vector rekErgVec = RQuotientExp.solveSystem(neuEqu, neuVar);
	Vector ergV = new Vector();
	for (int i=0; i<rekErgVec.size(); i++)
	{
		RQuotientExp[] rekErg = (RQuotientExp[])rekErgVec.elementAt(i);
		RQuotientExp[] erg = new RQuotientExp[neuVarLength+1];
		int l=0;
		for (k=0; k<variable.length; k++)
		{
			if (variable[k]==ix) erg[k] = partial;
			else erg[k] = rekErg[l++];
		}
		ergV.addElement(erg);
	}
	return ergV;
}
/*	
	// Diese Methode fordert ein quadratfreies, garantiert univariates Polynom mit ganzzahligen
	// Koeffizienten und liefert alle Nullstellen im euklidischen 
	// Körper in Ausrücken zurück.
	// Sonderfälle werden hier nicht beachtet.
	private Ausdruck[] solveUnivariate(int index)
	{
		int grad = getDegreeIn(index);
		
		BigInteger lf = BigInteger.valueOf(2).multiply(this.leadingFactor().z);
		Celement lfc = new Celement(new Qelement(lf));
		
		Celement[] punkte = this.qralgorithm(index, new Qelement(1,10000));
		for (int i=0; i<punkte.length; i++)
			punkte[i] = punkte[i].multiply(lfc);
		
		Vector erg = new Vector();
		Hashtable schonUntersuchteInteger = new Hashtable();
		for (int i=0; i<punkte.length-1; i++)
			for (int j=i+1; j<punkte.length; j++)
			{
				Celement wert = punkte[i].add(punkte[j]);
				BigInteger[] bi = wert.includedIntegerPoints();
				if ((bi.length==1) && (!schonUntersuchteInteger.containsKey(bi[0])))
				{
					schonUntersuchteInteger.put(bi[0],bi[0]);
					QPolynomial p = this.evaluate(index, (new QPolynomial(index)).subtract(new QPolynomial(new Qelement(bi[0],lf))));
					p = p.graeffeSqr();
					QPolynomial pdot = p.derive(index);
					QPolynomial ggt = p.gcd(pdot);			// nur der ggt ist interessant
					ggt = ggt.makeCoefficientInteger();
					Ausdruck[] res = ggt.solveUnivariate(index);
					for (int i=0; i<res.length; i++)
						erg.addElement(new Ausdruck(
				}
					
			}
		
		
		
		
	}
*/
	// Diese Methode löst ein vorgegebenes Polynom in mehreren Unbekannten nach einer
	// Unbekannten auf, sofern dies im euklidischen Körper möglich ist. Falls unmöglich,
	// wird null zurückgegeben.
	// Das Polynom wird quadratfrei und ganzzahlig gemacht. Die Nullstellen dürfen Nenner haben,
	// diese werden im Ablauf hinzumultipliziert.	
	public RQuotientExp solveTo(int index)
	{
		QPolynomial sqrfree = (this.squarefree(index))[0];

		int grad = sqrfree.getDegreeIn(index);
	
		// Sonderfall: Konstantes Glied ist 0
		QPolynomial p0 = sqrfree.getCoefficient(index, 0);
		if (p0.isZero()) return new RQuotientExp();
		// Sonderfall: Grad == 0, aber p != 0
		if (grad == 0) return null;
		// Sonderfall: Grad == 1
		if (grad == 1)
		{
			QPolynomial p1 = sqrfree.getCoefficient(index, 1);
			return (new RQuotientExp(p0.negate())).divide(new RQuotientExp(p1));
		}
		if (grad == 2)
		{
			QPolynomial p1 = sqrfree.getCoefficient(index, 1);
			QPolynomial p2 = sqrfree.getCoefficient(index, 2);
			RExpression vorWurz = new RExpression(new FastPolynomial(p1.negate()));
			RExpression unterWurz = vorWurz.sqr().subtract(new RExpression(new FastPolynomial(p0.multiply(p2).multiply(new QPolynomial(new Qelement(4))))));
			RExpression nenner = (new RExpression(new FastPolynomial(p2.multiply(new QPolynomial(Qelement.TWO)))));
			return new RQuotientExp(vorWurz.add(unterWurz.sqrt()),nenner);
		}
		// Sonderfall: Alle ungeraden Koeffizienten gleich null.
		// In der Schleife zum Testen wird gleich der ggT der Nenner alle Koeffizienten berechnet.
		BigInteger a = BigInteger.valueOf(1);
		boolean nurgerade = true;
		for (int i=0; i<sqrfree.monom.size(); i++)
		{
			QMonomial mon = (QMonomial)sqrfree.monom.elementAt(i);
			a = a.multiply(a.divide(a.gcd(mon.factor.n)));
			nurgerade = (nurgerade) && (mon.getExponent(index)%2 == 0);
		}
		if (nurgerade)
		{
			QPolynomial neu = new QPolynomial(sqrfree);
			for (int i=0; i<monom.size(); i++)
			{
				QMonomial mon = (QMonomial)neu.monom.elementAt(i);
				mon.setExponent(index, mon.getExponent(index)/2);
			}
			System.out.println("Neuaufruf mit : "+neu);
			RQuotientExp res = neu.solveTo(index);
			System.out.println("Neuaufruf liefert : "+res);
			if (res == null) return null;
			else return res.sqrt();
		}
		
		// und los der allgemeine Fall:
		
		QPolynomial poly = sqrfree.multiply(new QPolynomial(new Qelement(a)));
		int unspecs = poly.getHighestIndex();
		int[] maxKoeff = new int[unspecs+1];
		for (int i=0; i<unspecs+1; i++)
			maxKoeff[i] = poly.getDegreeIn(i);
		maxKoeff[index] = 1;
		RExpression nennerPol = new RExpression(new FastPolynomial(poly.getCoefficient(index, grad)));

		Datenmatrix matrix = new Datenmatrix(maxKoeff);
		
		int[] z = new int[unspecs+1];					// In diesem Array werden Werte 
									    				// für die anderen Variablen gezählt.
		for (int i=0; i<unspecs+1; i++) z[i] = 0;
		
		QPolynomial work = new QPolynomial(poly);
		for (int j=0; j<unspecs+1; j++)
			if (j!=index) work = work.evaluate(j,new Qelement(z[j]));
				
//		Celement[] punkte = work.newtonalgorithm(index, new Qelement(1,100));
		Celement[] punkte = work.qralgorithm(index, new Qelement(1,100000));
		Celement q = new Celement(work.leadingFactor());
		for (int i=0; i<punkte.length; i++)
			punkte[i] = punkte[i].multiply(q);
		matrix.setzeElementBei(new Hypervorschlag(punkte),z);
		int i=0;
		while (i<unspecs+1)
		{
			i=0;
			while ((i<unspecs+1) && (z[i]>=maxKoeff[i]-1))
			{
				z[i]=0;
				i++;
			}
			if (i<unspecs+1) 
			{
				z[i]++;
				work = new QPolynomial(poly);
				for (int j=0; j<unspecs+1; j++)
					if (j!=index) work = work.evaluate(j,new Qelement(z[j]));
				
//				punkte = work.newtonalgorithm(index, new Qelement(1,100));
				punkte = work.qralgorithm(index, new Qelement(1,100));
				q = new Celement(work.leadingFactor());
				for (int j=0; j<punkte.length; j++)
					punkte[j] = punkte[j].multiply(q);
				matrix.setzeElementBei(new Hypervorschlag(punkte),z);
			}
		}
		Datenmatrix ausdrucksmatrix = new Datenmatrix(maxKoeff);
		for (i=0; i<matrix.groesse; i++)
		{
			Hypervorschlag vor = (Hypervorschlag)(matrix.elementBeiStelle(i));
			if (!vor.hasMoreElements()) return null;		// raus, falls einer gar nichts zu bieten hat.
			ausdrucksmatrix.setzeElementBeiStelle(vor.nextElement(),i);
		}

		Vector schonBetrachtet = new Vector();
		
		RExpression kandidat;
		Ausdruck kandaus = Ausdruck.interpolarisation(ausdrucksmatrix, index);
		if (kandaus != null)
		{
			kandidat = kandaus.toRExpression();
			System.out.println();
			System.out.println("Folgender Kandidat wird getestet:");
			System.out.println(kandidat);
			if (poly.evaluate(index, kandidat).isZero()) return new RQuotientExp(kandidat, nennerPol);
			System.out.println("Test schlug fehl.");
			schonBetrachtet.addElement(kandidat);
		}

		i = 0;
		while (i<matrix.groesse)
		{
			i=0;
			Hypervorschlag vor = (Hypervorschlag)(matrix.elementBeiStelle(i));
			while ((i<matrix.groesse) && (!vor.hasMoreElements()))
			{
				vor.reinitialisiere();
				ausdrucksmatrix.setzeElementBeiStelle(vor.nextElement(),i);
				i++;
				if (i<matrix.groesse) vor = (Hypervorschlag)(matrix.elementBeiStelle(i));
			}
			if (i<matrix.groesse)
			{
				ausdrucksmatrix.setzeElementBeiStelle(vor.nextElement(),i);
				
				// ***
				// Interpolarisation der einzelnen Polynome
				// und Test auf Korrektheit. Falls positiv, kann das Ergebniss direkt zurückgegeben
				// werden (es wird nur eine Nullstelle gesucht, unabhängig von möglichen weiteren).
				// ***
	
				kandaus = Ausdruck.interpolarisation(ausdrucksmatrix, index);
				if (kandaus != null)
				{
					kandidat = kandaus.toRExpression();
					boolean test = true;
					for (int l=0; l<schonBetrachtet.size(); l++)
						if (kandidat.equals((RExpression)(schonBetrachtet.elementAt(l)))) test = false;
					if (test)
					{
						System.out.println();
						System.out.println("Folgender Kandidat wird getestet:");
						System.out.println(kandidat);
						if (poly.evaluate(index, kandidat).isZero()) return new RQuotientExp(kandidat, nennerPol);
						System.out.println("Test schlug fehl.");
						schonBetrachtet.addElement(kandidat);
					} else System.out.println("Kandidat erneut abgelehnt.");
				} else System.out.println("Interpolation schlug fehl (Ganzzahligkeit)");
			}			
		}
		return null;		// Wenn keine Nullstelle gefunden: Zurück ohne Ergebniss.
	}
	public QPolynomial sqr()
	{
		return multiply(this);
	}
/**
 * Insert the method's description here.
 * Creation date: (18.01.2003 11:12:58)
 * @return arithmetik.QPolynomial

	Falls this = c * p * p ist, wobei c konstant ist, wird c*p zurückgegeben. Ansonsten null.
 
 */
public QPolynomial sqrt() 
{
	if (isConstant()) return new QPolynomial(this);
	
	int index = getHighestIndex();
	QPolynomial con = getContent(index);
	QPolynomial consqrt = con.sqrt();
	if (consqrt==null) return null;

	QPolynomial primepart = divide(con);
	QPolynomial div = this.divide(this.gcd(derive(index)));

	int deg1 = this.getDegreeIn(index);
	int deg2 = div.getDegreeIn(index);

	if ((deg1 % deg2)!=0) return null;
	div = div.normalize().pow(deg1 / (2*deg2));

	QPolynomial[] divAndRem = this.divideAndRemainder(div);
	if (!divAndRem[1].isZero()) return null;
	
	return consqrt.multiply(divAndRem[0]);
}
// Faktorisiert das Polynom bzgl. Xidentifier zu quadratfreien Faktoren. Die Rückgabe gibt jeden Faktor genau einmal,
// nach Häufigkeit des Auftauchens sortiert, d.h. this = erg[0] * erg[1]^2 * erg[2]^3 * ... erg[n-1]^n

public QPolynomial[] squarefree(int identifierNr)
{
	if (isZero()) return new QPolynomial[]{ZERO};
	Vector erg = new Vector();

	QPolynomial p1 = gcd(derive(identifierNr));		// Enthält Faktoren, die mind. 2 mal auftauchen, in Vielfachheit
	QPolynomial letzterFaktor = divide(p1);			// Enthält alle Faktoren genau 1 mal.
	QPolynomial p2;						
	while (!p1.isConstant())
	{
		p2 = p1.gcd(p1.derive(identifierNr));		// Enthält Faktoren, die mind. 3 mal auftauchen, in Vielfachheit
		p1 = p1.divide(p2);							// Enthält Faktoren, die mind. 2 mal auftauchen, genau 1 mal
		erg.addElement(letzterFaktor.divide(p1));	// Genau die Faktoren, die 1 mal auftauchen, 1 mal
		letzterFaktor = p1;							// wie oben, nur mit 2
		p1 = p2;									// wie oben, nur mit 2
	}
	QPolynomial[] ergA = new QPolynomial[erg.size()+1];
	for (int i=0; i<ergA.length-1; i++) ergA[i] = (QPolynomial)erg.elementAt(i);
	ergA[ergA.length-1] = letzterFaktor;
	
	return ergA;	
}
	public QPolynomial subtract(QPolynomial arg2)
	{
		return add(arg2.negate());
	}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 13:34:21)
 * @return java.util.Vector
 */
public Vector toModulMomialList() 
{
	int index = this.getHighestIndex();
	Vector erg = new Vector();
	for (int i=monom.size()-1; i>=0; i--)
	{
		QMonomial m = (QMonomial)monom.elementAt(i);
		int[] exps = new int[index+1];
		for (int j=0; j<index+1; j++) exps[j] = m.getExponent(j);
		ModulMonomial mon = new ModulMonomial( ((Qelement)m.factor).toInt(), 0, exps);
		erg.addElement(mon);		
	}
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (18.09.2003 13:34:21)
 * @return java.util.Vector
 */
public Vector toModulMomialList(int anzVar) 
{
	Vector erg = new Vector();
	for (int i=monom.size()-1; i>=0; i--)
	{
		QMonomial m = (QMonomial)monom.elementAt(i);
		int[] exps = new int[anzVar];
		for (int j=0; j<anzVar; j++) exps[j] = m.getExponent(j);
		ModulMonomial mon = new ModulMonomial( ((Qelement)m.factor).toInt(), 0, exps);
		erg.addElement(mon);		
	}
	return erg;
}
	// Wandelt in Multivariates Polynomial um.
	public Polynomial toPolynomial()
	{
		int maxlen = 0;
		for (int i=0; i<monom.size(); i++)
			maxlen = Math.max(maxlen, ((QMonomial)monom.elementAt(i)).exp.length);
		
		Polynomial erg = new Polynomial(0);
		for (int i=0; i<monom.size(); i++)
		{
			QMonomial m = (QMonomial)monom.elementAt(i);
			int c = maxlen-1;
			Polynomial summant;
			if (m.exp.length-1==c) summant = new Polynomial(m.factor,m.exp[c]);
			else summant = new Polynomial(m.factor,0);
			c--;
			while (c>m.exp.length-1) {summant = new Polynomial(summant, 0); c--;}
			while (c>=0) summant = new Polynomial(summant, m.exp[c--]);
			erg = erg.add(summant);
		}
		return erg;
	}
	// Wandelt in Multivariates Polynomial mit Qalgebraics um.
	private Polynomial toQalgebraicPolynomial(QPolynomial mod)
	{
		int maxlen = 0;
		for (int i=0; i<monom.size(); i++)
			maxlen = Math.max(maxlen, ((QMonomial)monom.elementAt(i)).exp.length);
		
		Polynomial erg = new Polynomial(0);
		for (int i=0; i<monom.size(); i++)
		{
			QMonomial m = (QMonomial)monom.elementAt(i);
			int c = maxlen-1;
			Polynomial summant;
			if (m.exp.length-1==c) summant = new Polynomial(new Qalgebraic(mod,m.factor),m.exp[c]);
			else summant = new Polynomial(new Qalgebraic(mod,m.factor),0);
			c--;
			while (c>m.exp.length-1) {summant = new Polynomial(summant, 0); c--;}
			while (c>=0) summant = new Polynomial(summant, m.exp[c--]);
			erg = erg.add(summant);
		}
		return erg;
	}
	public String toString() 
	{
		if (monom.size()==0) return "0";
		String erg="";
		
		for (int i=0; i<monom.size(); i++)
		{
			erg += monom.elementAt(i).toString();
			if (i<monom.size()-1) erg += " + ";
		}
		return erg;
	}
	// Ist this ein univariates Polynom mit ganzzahligen Koeffizienten, wird ein
	// "univariatePolynomial" erzeugt.
	public UnivariatePolynomial toUnivariatePolynomial()
	{
		int index = this.getHighestIndex();
		int deg = this.getDegreeIn(index);
		BigInteger[] coeffs = new BigInteger[deg+1];
		for (int i=0; i<monom.size(); i++)
		{
			QMonomial m = (QMonomial)monom.elementAt(i);
			coeffs[m.getExponent(index)] = ((Qelement)m.factor).floor();
		}
		return new UnivariatePolynomial(coeffs);
	}
    /**
     * Insert the method's description here.
     * Creation date: (15.12.2002 23:13:13)
     * @return arithmetik.QPolynomial
    
    	Schaut, ob das Polynom konstant 0, 1 oder 2 ist, und liefert in dem Fall die statische 0, 1 oder 2 zurück,
    	sonst this. Benutzt zum sparen von Speicher.
     
     */
    private QPolynomial unifiziereKonstanten() 
    {
    	if (!isConstant()) return this;
    	if (isZero()) return ZERO;
    	Qelement q = leadingFactor();
    	if (q.equals(Qelement.ONE)) return ONE;
    	if (q.equals(Qelement.TWO)) return TWO;
    	return this;
    }

    /**
     * Computes Sturm's Sequence of a univariate polynomial, the remainder sequence of f and f' with alternating signs. If gcd(f,f') is not of degree zero, all polynomials in the sequence will be divided by the gcd.
     * 
     * This sequence can be used to count the number of reel zeros in [a,b] by counting the sign changes of the sequence at a minus the sign changes at b. 
     * 
     * @param ix
     * @return
     */
    public QPolynomial[] getSturmsSequence(int ix) {
        if (this.getDegreeIn(ix)==0) return new QPolynomial[]{this};
        Vector<QPolynomial> erg = new Vector<QPolynomial>();
        QPolynomial fN1 = this;
        QPolynomial fN2 = this.derive(ix);
        erg.add(fN1); erg.add(fN2);
        while (fN2.getDegreeIn(ix)>=1) {
            QPolynomial neu = fN1.remainder(fN2);
            fN1 = fN2;
            fN2 = neu.negate();
            erg.add(fN2);
        }
        if (fN2.isZero()) {
            int l = erg.size()-1;
            erg.removeElementAt(l);
            QPolynomial gcd = erg.elementAt(l-1);
            QPolynomial[] ergA = new QPolynomial[l];
            for (int i=0; i<l; i++) ergA[i] = erg.elementAt(i).divide(gcd);
            return ergA;
        } else {
            QPolynomial[] ergA = new QPolynomial[erg.size()];
            return erg.toArray(ergA);
        }
    }
    
    private static int countSignSwitches(QPolynomial[] sturmsSequence, int ix, Qelement a) {
        int sign = sturmsSequence[0].evaluate(ix, a).leadingFactor().signum();
        int erg = 0;
        for (int i=1; i<sturmsSequence.length; i++) {
            int newSign = sturmsSequence[i].evaluate(ix, a).leadingFactor().signum();
            if (newSign != 0 && sign != 0 && newSign != sign) erg += 1;
            if (newSign != 0) sign = newSign;
        }
        return erg;
    }
    
    /**
     * Approximates all zeros of a univariate polynomial in [a,b] with precision 2^{-precision}. 
     * 
     * Runs iteratively by first singling the zeros, then continuing to halve the intervals until precision is reached and neighboring intervals don't share boundaries. 
     * 
     * @param ix    index of the univariate variable
     * @param a
     * @param b
     * @param precision
     * @return
     */
    public Qelement[] getZerosBetween(int ix, Qelement a, Qelement b, int precision, int moreNewtonPrecision) {
       int halfings = (int)Math.ceil(Math.log(b.toDouble()-a.toDouble())/Math.log(2)) + precision; 
       QPolynomial[] sturmsSequence = this.getSturmsSequence(ix);
       int countA = countSignSwitches(sturmsSequence, ix, a);
       int countB = countSignSwitches(sturmsSequence, ix, b);
       if (countA - countB == 0) return new Qelement[0];
       Vector<Qelement[]> incomingIntervals = new Vector<Qelement[]>(); incomingIntervals.addElement(new Qelement[]{a,b});
       Vector<int[]> incomingIntervalsVals = new Vector<int[]>(); incomingIntervalsVals.addElement(new int[]{countA, countB, 0});
       Vector<Qelement[]> intervals = new Vector<Qelement[]>(); Vector<int[]> intervalsVals = new Vector<int[]>();
       while (incomingIntervals.size()>0) {
           int p = incomingIntervals.size()-1;
           Qelement[] iv = incomingIntervals.elementAt(p); int[] ivv = incomingIntervalsVals.elementAt(p);
           if (ivv[0]-ivv[1] == 1) {
               intervals.add(iv); intervalsVals.add(ivv); 
               incomingIntervals.removeElementAt(p); incomingIntervalsVals.removeElementAt(p);
           } else {
               Qelement center = iv[0].add(iv[1]).divide(Qelement.TWO);
               int countC = countSignSwitches(sturmsSequence, ix, center);
               if (countC == ivv[0]) {iv[0] = center; ivv[0] = countC; ivv[2]++;}
               else if (countC == ivv[1]) {iv[1] = center; ivv[1] = countC; ivv[2]++;}
               else {incomingIntervals.add(new Qelement[]{iv[0], center}); incomingIntervalsVals.add(new int[]{ivv[0], countC, ivv[2]+1}); iv[0] = center; ivv[0] = countC; ivv[2]++; }
           }
       }
       int anz = intervals.size();
       int finished = 0;
       while (finished < anz) {
           for (int i=0; i<anz; i++) {
               Qelement[] iv = intervals.elementAt(i); int[] ivv = intervalsVals.elementAt(i);
               if (iv[0]!=iv[1]) {
                   boolean half = (ivv[2] < halfings || (i>0 && intervals.elementAt(i-1)[1]==iv[0]) || (i<anz-1 && intervals.elementAt(i+1)[0]==iv[1]));
                   Qelement center = iv[0].add(iv[1]).divide(Qelement.TWO);
                   if (half) {
                       int countC = countSignSwitches(sturmsSequence, ix, center);
                       if (countC == ivv[0]) {iv[0] = center; ivv[0] = countC; ivv[2]++;}
                       else {iv[1] = center; ivv[1] = countC; ivv[2]++;}
                   } else {iv[0] = iv[1] = center; finished++;}
               }
           }
       }
       Qelement[] erg = new Qelement[anz];
       for (int i=0; i<anz; i++) {
           if (moreNewtonPrecision==0) erg[i] = intervals.elementAt(i)[0];
           else erg[i] = approximateReelZero(ix, Qelement.HALF.pow(halfings+moreNewtonPrecision), new Relement(intervals.elementAt(i)[0])).toQelement();
       }

       return erg;
    }

    /**
     * This function computes the derivate wrt x and y for the -2ll function K*(ln(x) + (s/x)) + ln(|Sigma|) + Tr(Sigma^{-1}), where Sigma = {{y,ax+by},{ax+by},y}} is a 2x2 covariance matrix with linear equal variances. The x is an error parameter
     * that is supported from the outside by K measures of average variance s. 
     * Then function puts the variables as (x,y,a,b,K,s).  
     * 
     * Correction compared to the paper / board: in the X0^5 coefficient, changed (X5+2) to (X5+X4+1).
     * 
     * @return
     */
    public static QPolynomial[] getTwoByTwoLikelihoodDerivatives() {
        QPolynomial[] erg = new QPolynomial[]{
                new QPolynomial("X1^3*(1-X3^2)^2-X0*X1^2*3*(1-X3^2)*X2*X3+X0^2*X1*(3*X2^2*X3^2-X2^2)+X0^3*X2^3*X3+X1^2*(X3^2-1)-X0^2*X2^2"),
                new QPolynomial( "X1^4*X0*X4*(1-X3^2)^2+X1^3*X0^2*(X2*X3*(X3^2-1)*(4*X4+2))+X1^2*X0^3*(X2^2*(3*X3^2-1)*(2*X4+2))"
                                +"+X1*X0^4*(X2^3*X3*(4*X4+6))+X0^5*(X2^4*(X5+X4+1))"
                                +"+X1^4*(-X4*X5*(1-X3^2)^2)+X1^3*X0*(-4*X4*X5*X2*X3*(X3^2-1))+X1^2*X0^2*(-2*X4*X5*X2^2*(3*X3^2-1)+4*X2*X3)"
                                +"+X1*X0^3*(-4*X4*X5*X2^3*X3+4*X2^2)+X0^4*(-X4*X5*X2^4)"
                                )
        };
        /*
        // DEBUG
        QPolynomial x = new QPolynomial("X0");
        QPolynomial cov = new QPolynomial("X0*X2+X1*X3");
        QPolynomial det = (new QPolynomial("X1^2")).subtract(cov.sqr());
        QPolynomial detdev = det.derive(0);
        QPolynomial first = det.sqr().multiply(new QPolynomial("X4")).multiply(x.subtract(QPolynomial.ONE));
        QPolynomial second = x.sqr().multiply(detdev).multiply(det.subtract(new QPolynomial("2*X1")));
        QPolynomial test = first.add(second);
        QPolynomial test2 = erg[1].evaluate(5, Qelement.ONE);
        QPolynomial check = test.subtract(test2);
        System.out.println("Check = "+check);
        */
        return erg;
    }
    public String toDoubleString() {
        if (monom.size()==0) return "0";
        String erg="";
        
        for (int i=0; i<monom.size(); i++)
        {
            erg += ((QMonomial)monom.elementAt(i)).toDoubleString();
            if (i<monom.size()-1) erg += " + ";
        }
        return erg;
    }

}
