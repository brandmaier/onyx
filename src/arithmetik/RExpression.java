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

import java.util.*;

/*

*/
public class RExpression
{
	private static Vector datenbank = new Vector();
	private static boolean datenbankAktiv = true;

	public static RExpression ZERO = new RExpression();
	public static RExpression ONE = new RExpression(Qelement.ONE);
	
	public Vector monom;

	public RExpression ()
	{
		monom = new Vector();
	}
	public RExpression(int identifierNr)
	{
		this (new RExpMonomial(new FastPolynomial(identifierNr)));
	}
	public RExpression(FastPolynomial polynomial)
	{
		monom = new Vector();
		if (!polynomial.isZero()) monom.addElement(new RExpMonomial(polynomial));
	}
	public RExpression(Qelement qelement)
	{
		this (new RExpMonomial(qelement));
	}
	public RExpression(QPolynomial polynomial)
	{
		this (new FastPolynomial(polynomial));
	}
	public RExpression(RExpMonomial monomial)
	{
		monom = new Vector();
		monom.addElement(monomial);
	}
	// Voller Copy-Konstruktor
	public RExpression(RExpression copy)
	{
		monom = new Vector();
		for (int i=0; i<copy.monom.size(); i++)
			monom.addElement(new RExpMonomial((RExpMonomial)copy.monom.elementAt(i)));
	}
	public RExpression add (RExpression arg2)
	{
		// Sonderfall: Falls beide Terme keine Wurzeln enthalten,
		// muss es schön schnell gehen.
		if (this.monom.size()==0) return new RExpression(arg2);
		if (arg2.monom.size()==0) return new RExpression(this);
		if ((this.hasNoRoot()) && (arg2.hasNoRoot())) return new RExpression
				(((RExpMonomial)monom.elementAt(0)).factor.add(((RExpMonomial)arg2.monom.elementAt(0)).factor));
		
//		String a = this.toString();
//		String b = arg2.toString();
		RExpression erg = new RExpression();
		int p1 = 0, p2 = 0;
		while ((p1 < monom.size()) && (p2 < arg2.monom.size()))
		{
			RExpMonomial m1 = (RExpMonomial)monom.elementAt(p1);
			RExpMonomial m2 = (RExpMonomial)arg2.monom.elementAt(p2);
			RExpMonomial neu = null;
			
			boolean istNull = false;
			int comp = m1.compareTo(m2);
			if (comp ==  0) 
			{
				neu = new RExpMonomial(m1.factor.add(m2.factor));
				neu.member = (Vector)m1.member.clone();
				p1++; p2++;
				if (neu.factor.isZero()) istNull = true;
			} 
			if (comp == -1) {neu = new RExpMonomial(m1); p1++; }
			if (comp ==  1) {neu = new RExpMonomial(m2); p2++; }
			if (!istNull) erg.monom.addElement(neu);
		}
		while (p1 < monom.size()) erg.monom.addElement(new RExpMonomial((RExpMonomial)monom.elementAt(p1++)));
		while (p2 < arg2.monom.size()) erg.monom.addElement(new RExpMonomial((RExpMonomial)arg2.monom.elementAt(p2++)));
		
//		String c = erg.toString();
		return erg;
	}
	// Berechnet den lazyGcd (nach Def. von FastPolynomial) aller Faktoren und sammelt
	// alle Datenbankeinträge ein, die in jedem Monom vorkommen.
	public RExpMonomial commonContent()
	{
		if (monom.size()==0) return new RExpMonomial(Qelement.ONE);
		RExpMonomial erg = new RExpMonomial((RExpMonomial)monom.elementAt(0));
		for (int i=1; i<monom.size(); i++)
			erg = erg.unite((RExpMonomial)monom.elementAt(i));
		return erg;
	}
	// Berechnet einen lazyGcd diese RExpressions mit dem übergebenen RExpMonomial, d.h. alle
	// gemeinsamen Anteile der Faktoren und alle Datenbankeinträge, die in jedem Monom vorkommen.
	// Ist das RExpMonomial 0, so wird in unverändert zurückgegeben.
	public RExpMonomial commonContent(RExpMonomial in)
	{
		RExpMonomial erg = new RExpMonomial(in);
		for (int i=0; i<monom.size(); i++)
			erg = erg.unite((RExpMonomial)monom.elementAt(i));
		return erg;
	}
	public static int databaseCompare(RExpression one, RExpression two)
	{
		int i = datenbank.indexOf(one), j = datenbank.indexOf(two);
		if (i>j) return  1;
		if (i<j) return -1;
		return 0;
	}
	public static void databaseDisable() {datenbankAktiv = false;}
	public static String databaseElementsToString()
	{
		String erg = "Database contains: \r\n";
		for (int i=0; i<datenbank.size(); i++)
		{
			erg += "["+i+"]: "+(RExpression)datenbank.elementAt(i)+"\r\n";
		}
		return erg;
	}
	public static void databaseEnable() {datenbankAktiv = true;}
	public static RExpression databaseLookup(RExpression candidate)
	{
		if (!datenbankAktiv) return candidate;
		for (int i=0; i<datenbank.size(); i++)
		{
			RExpression data = (RExpression)datenbank.elementAt(i);
			if (candidate == data) return data;						// Nicht mit isEqual suchen,
																	// sonst werden 1+sqrt(2) und
																	// 1-sqrt(2) nicht unterschieden.
		}
		datenbank.addElement(candidate);
		return candidate;
	}
	public double debugEvaluation()
	{
		double erg = 0.0;
		for (int i=0; i<monom.size(); i++)
			erg += ((RExpMonomial)monom.elementAt(i)).debugEvaluation();
		return erg;
	}
	// leitet das Polynom nach X_*identifierNr* ab. Die anderen Bezeichner werden als
	// Konstanten gehandelt. 
	public RQuotientExp derive(int identifierNr)
	{
		RQuotientExp erg = new RQuotientExp();
		for (int i=0; i<monom.size(); i++)
			erg = erg.add( ((RExpMonomial)monom.elementAt(i)).derive(identifierNr) );
		return erg;
	}
	// Diese Routine eliminiert die Wurzeln für den Test auf 0. Dafür wird
	// die 3. binomische Formel verwendet; das Ergebnis ist 0 <=> der RExpression ist 0.
	// Testbeispiel: 5 ab + 2 ac + 3d = 0
	//			<=>  a (5b + 2c) = -3d
	//			<=>  a^2 (5b + 2c)^2 = (3d)^2
	//			<=>  a^2 (5b + 2c)^2 - (3d)^2 = 0
	
	public FastPolynomial eliminateSquareRoots()
	{
		int databasepos = -1;
		
		for (int i=0; i<monom.size(); i++)
		{
			int j = -1; 
			RExpMonomial mon = (RExpMonomial)monom.elementAt(i);
			if (mon.member.size()>0)
				j = datenbank.indexOf(mon.member.lastElement());
			if (j > databasepos) databasepos = j;
		}
		
		if (databasepos==-1) 
		{
			if (monom.size()==0) return new FastPolynomial();
			else return new FastPolynomial(((RExpMonomial)monom.elementAt(0)).factor);
		}
		
		RExpression killroot = (RExpression)datenbank.elementAt(databasepos);
		RExpression noroot = new RExpression();
		RExpression roots = new RExpression();
		
		for (int i=0; i<monom.size(); i++)
		{
			RExpMonomial mon = new RExpMonomial((RExpMonomial)monom.elementAt(i));
			if ((mon.member.size()>0) && (mon.member.lastElement()==killroot)) 
			{
				mon.member.removeElementAt(mon.member.size()-1);
				roots.monom.addElement(mon);
			}
			else noroot.monom.addElement(mon);
		}
		
		double rv = this.debugEvaluation();
		FastPolynomial erg = (noroot.sqr().subtract(roots.sqr().multiply(killroot))).eliminateSquareRoots();
		String ergs = erg.toString();
		double ergv = erg.debugEvaluation();
		return erg;
	}
	// Evaluate Nr. 6 nimmt ein Array von doubles, dass alle vorkommenden
	// Variablen mit einem Wert belegt, und liefert das ausgewertete Polynom zurück.
	public double evaluate(double[] value)
	{
		double erg = 0;
		for (int i=0; i<monom.size(); i++)
			erg += ((RExpMonomial)monom.elementAt(i)).evaluate(value);
		return erg;
	}
	// Wertet diesen RExpression an Stelle value statt identifierNr aus.
	public RQuotientExp evaluate(int identiferNr, RQuotientExp value)
	{
		RQuotientExp erg = new RQuotientExp();
		for (int i=0; i<monom.size(); i++)
			erg = erg.add(((RExpMonomial)monom.elementAt(i)).evaluate(identiferNr, value));
		return erg;
	}
/**
 * Insert the method's description here.
 * Creation date: (11.01.2003 13:32:02)
 * @return arithmetik.RExpression[]
 */
public RExpression[] getAllConjugates()
{
	int[] signs = new int[getDepth()];
	for (int i=0; i<signs.length; i++) signs[i] = 0;
	Vector ergV = new Vector();
	boolean weiter = true;
	while (weiter)
	{
		ergV.addElement(getConjugate(signs));
		int i=0;
		while ((i<signs.length) && (signs[i]==1)) {signs[i]=0; i++;}
		if (i<signs.length) signs[i] = 1;
		else weiter = false;
	}
	RExpression[] erg = new RExpression[ergV.size()];
	for (int i=0; i<erg.length; i++) erg[i] = (RExpression)ergV.elementAt(i);
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (11.01.2003 13:07:56)
 * @return arithmetik.RExpression
 * @param signs int[]

	Sollte RExpression von der Form

	a + b*sqrt(a) + c*sqrt(d+e*sqrt(a)) + ....

	sein und hat signs genau soviele Einträge 0 oder 1, wie es verschiedene Wurzeln in der Darstellung
	gibt, so gibt diese Methode die durch signs angegebene Konjugierte zurück.
 
 */
public RExpression getConjugate(int[] signs) 
{
	int tiefe = getDepth();
	if (tiefe==0) return this;
	
	int[] nsigns = new int[tiefe-1];
	for (int i=0; i<nsigns.length; i++) nsigns[i] = signs[i];
	int hereSign = signs[tiefe-1];

	RExpMonomial max = null;
	int d = -1;
	for (int i=0; i<monom.size(); i++)
	{
		RExpMonomial m = (RExpMonomial)monom.elementAt(i);
		int j = m.getDepth();
		if (j>d) {max = m; d = j;}
	}
	if (max==null) return this;
	RExpression inWurzel = RExpression.ONE;
	for (int j=0; j<max.member.size(); j++) inWurzel = inWurzel.multiply((RExpression)max.member.elementAt(j));
	RExpression vorWurzel = new RExpression();
	for (int i=0; i<monom.size(); i++)
	{
		RExpMonomial m = (RExpMonomial)monom.elementAt(i);
		if (m != max) vorWurzel.monom.addElement(m);
	}
	inWurzel = inWurzel.getConjugate(nsigns);
	vorWurzel = vorWurzel.getConjugate(nsigns);
	
	if (hereSign==0) return vorWurzel.add(inWurzel.sqrt());
	else return vorWurzel.subtract(inWurzel.sqrt());
}
/**
 * Berechnet den gemeinsamen Anteil aller FastPolynomial-Vorfaktoren.
 * Creation date: (18.06.2002 08:33:49)
 * @return arithmetik.FastPolynomial
 */
public FastPolynomial getContent() 
{
	if (monom.size()==0) return new FastPolynomial(Qelement.ONE);
	FastPolynomial erg = ((RExpMonomial)monom.elementAt(0)).factor;
	for (int i=1; i<monom.size(); i++)
		erg = erg.gcd(((RExpMonomial)monom.elementAt(i)).factor);
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (11.01.2003 13:14:59)
 * @return int

 Gibt die maximale Verschachtelungstiefe der Wurzeln wieder.
 
 */
public int getDepth() 
{
	int erg = 0;
	for (int i=0; i<monom.size(); i++)
	{
		int md = ((RExpMonomial)monom.elementAt(i)).getDepth();
		if (md > erg) erg = md;
	}
	return erg;
}
	// liefert den höchsten vorkommenden Index (i.A. die Anzahl der Variablen).
	public int getHighestIndex()
	{
		int erg = 0;
		for (int i=0; i<monom.size(); i++)
			erg = Math.max(erg, ((RExpMonomial)monom.elementAt(i)).getHighestIndex());
		return 0;
	}
/**
 * Insert the method's description here.
 * Creation date: (18.06.2002 08:41:32)
 * @return arithmetik.RExpression
 */
public RExpression getPrimepart() 
{
	RExpression erg = new RExpression(this);
	FastPolynomial c = getContent();
	for (int i=0; i<erg.monom.size(); i++)
	{
		RExpMonomial m = (RExpMonomial)erg.monom.elementAt(i);
		m.factor = m.factor.divide(c);
	}
	return erg;
}
	public boolean hasNoRoot()
	{
		return ((monom.size()==0) || 
				((monom.size()==1) && (((RExpMonomial)monom.elementAt(0)).member.size()==0)));
	}
	public String infos()
	{
		return toString();
	}
/**
 * Insert the method's description here.
 * Creation date: (21.01.2003 21:07:08)
 * @return boolean
 */
public boolean isCertainlyOne() 
{
	return ((monom.size()==1) && (((RExpMonomial)monom.elementAt(0)).member.size()==0) && (((RExpMonomial)monom.elementAt(0)).factor.isUnit()));
}
	// Lässt sich später vielleicht noch ausbauen.
	public boolean isCertainlyZero()
	{
		return monom.size()==0;
	}
	public static boolean isDatabaseEnabled() {return datenbankAktiv;}
	// nicht equals überschreiben, sonst sind 1+sqrt(2) und 1sqt(2) identisch!
	public boolean isEqual(RExpression arg2)
	{
		return (subtract(arg2)).isZero();
	}
	public boolean isZero()
	{
		return eliminateSquareRoots().isZero();
	}
	// div2 muss jeden Summanten von RExpression teilen. 
	public RExpression lazyDivide(RExpMonomial arg2)
	{
		RExpression erg = new RExpression();
		for (int i=0; i<monom.size(); i++)
			erg.monom.addElement(((RExpMonomial)monom.elementAt(i)).lazyDivide(arg2));

		return erg;
	}
	// Gibt eine Liste zurück, mit welchem RExpression man den ggT
	// multiplizieren müsste, um auf den jeweiligen factor zu kommen.
	public static RExpression[] lazyGcdFaks(RExpression[] v_in)
	{
		RExpMonomial gg = lazyGcdList(v_in);
		RExpression[] f = new RExpression[v_in.length];
		for (int i=0; i<v_in.length; i++)
			f[i] = v_in[i].lazyDivide(gg);
		return f;
	}
	// Findet einen RExpression, der alle FastPolynomial- und alle RExpression-Datenbankeinträge
	// enthält, die in allen RExpressionen des Arrays v_in vorkommen. 
	public static RExpMonomial lazyGcdList(RExpression[] v_in)
	{
		int j=0;
		while ((j<v_in.length) && (v_in[j].isCertainlyZero())) j++;
		if (j==v_in.length) return new RExpMonomial();
		RExpMonomial erg = v_in[j].commonContent();
		for (int i=j+1; i<v_in.length; i++)
			if (!v_in[i].isCertainlyZero()) erg = v_in[i].commonContent(erg);

		return erg;
	}
	// Gibt eine Liste zurück, mit welchem RExpression man die jeweiligen
	// Argumente multiplizieren muss, um auf das "kgV" zu kommen.
	// Der Faktor des kgV ist das Produkt der Faktoren (Zahlen!) aller 
	// Komponenten, und somit haben die Ergebnisse als Faktoren das 
	// Produkt aller Faktoren der anderen Komponenten.
	public static RExpression[] lazyScmFaks(RExpression[] v_in)
	{
		RExpression[] erg = new RExpression[v_in.length];
		RExpression[] T = lazyGcdFaks(v_in);
		for (int i=0; i<erg.length; i++)
		{
			erg[i] = new RExpression(Qelement.ONE);
			for (int j=0; j<T.length; j++)
				if (i!=j) erg[i] = erg[i].multiply(T[j]);
		}
		return erg;
	}
	// Diese Routine teilt den RExpression durch ein RExpMonomial, wobei dessen Factor
	// ein lazyDivisor vom factor jedes Monoms sein muss und dessen Wurzel-Datenbankelement 
	// in jedem monom auftauchen müssen. Aufgerufen wird diese Routine von RQuotientenExp,
	// um zu kürzen, und zwar mit dem commonContent united mit dem commonContent eines anderen
	// RExpression. 
	protected RExpression monomialDivide(RExpMonomial arg2)
	{
		RExpression erg = new RExpression(this);
		for (int i=0; i<erg.monom.size(); i++)
		{
			RExpMonomial mon = (RExpMonomial)erg.monom.elementAt(i);
			mon.factor = mon.factor.lazyDivide(arg2.factor);
			mon.member = mon.collectSimilar(arg2)[1];
		}
		return erg;
	}
	// Multipliziert ein Monom mit RExpression
	public RExpression monomialMultiply(RExpMonomial arg2)
	{
		RExpression erg = new RExpression();
		
		for (int i=0; i<monom.size(); i++)
		{
			RExpMonomial mon = (RExpMonomial)monom.elementAt(i);
			
			Vector[] part = mon.collectSimilar(arg2);
			
			RExpression work = new RExpression(Qelement.ONE);
			for (int j=0; j<part[0].size(); j++)
				work = work.multiply((RExpression)part[0].elementAt(j));
			
			RExpMonomial rest = new RExpMonomial(arg2.factor.multiply(mon.factor));
			if (!rest.factor.isZero()) rest.member = part[1];
			
			if (part[0].size()==0) work = new RExpression(rest);
			else work = work.monomialMultiply(rest);
			
			erg = erg.add(work);
		}
		
		return erg;
	}
	// Arbeitet simpel nach Distributivgesetz.
	public RExpression multiply(RExpression arg2)
	{
		// Sonderfall: Falls beide Terme keine Wurzeln enthalten,
		// muss es schön schnell gehen.
		if (this.monom.size()==0) return new RExpression();
		if (arg2.monom.size()==0) return new RExpression();
		if ((this.hasNoRoot()) && (arg2.hasNoRoot())) return new RExpression
				(((RExpMonomial)monom.elementAt(0)).factor.multiply(((RExpMonomial)arg2.monom.elementAt(0)).factor));

		
		RExpression erg = new RExpression();
		
		for (int i=0; i<monom.size(); i++)
			erg = erg.add(arg2.monomialMultiply((RExpMonomial)monom.elementAt(i)));

		return erg;
	}
	public RExpression negate()
	{
		RExpression erg = new RExpression(this);
		for (int i=0; i<erg.monom.size(); i++)
			erg.monom.setElementAt(((RExpMonomial)erg.monom.elementAt(i)).negate(),i); 
		return erg;
	}
	public RExpression pow(long n)
	{
		if (n==0) return new RExpression(Qelement.ONE);
		if (n==1) return new RExpression(this);
		long h = n/2;
		return pow(h).multiply(pow(n-h));
	}
	public static void reinitDatabase()
	{
		datenbank.removeAllElements();
		FastPolynomial.reinitDatabase();
	}
	public RQuotientExp solveTo(int identifierNr)
	{
		return this.eliminateSquareRoots().solveTo(identifierNr);
	}
	public RExpression sqr()
	{
		return multiply(this);
	}
	public RExpression sqrt()
	{
		if (this.isCertainlyZero()) return RExpression.ZERO;
		if (this.isCertainlyOne()) return RExpression.ONE;
		FastPolynomial c = getContent();
		FastPolynomial[] d = c.findDoubleFactors();
		RExpression inRoot = this;
		if (!d[1].isUnit()) inRoot = this.getPrimepart().multiply(new RExpression(d[0]));
		RExpression neu = RExpression.databaseLookup(inRoot);
		return new RExpression(new RExpMonomial(d[1],neu));
	}
	public RExpression subtract(RExpression arg2)
	{
		return add(arg2.negate());
	}
	public QPolynomial toQPolynomial()
	{
		if (monom.size()==0) return new QPolynomial();
		return ((RExpMonomial)monom.elementAt(0)).factor.expand();
	}
	public String toString()
	{
		if (monom.size()==0) return "0";
		String erg = "";
		for (int i=0; i<monom.size(); i++)
		{
			erg += monom.elementAt(i);
			if (i<monom.size()-1) erg+=" + ";
		}
		return erg;
	}
/**
 * Insert the method's description here.
 * Creation date: (21.01.2003 21:03:16)
 * @return arithmetik.RExpression
 */
public RExpression unifiziereKonstanten() 
{
	if (monom.size()==0) return RExpression.ZERO;
	if ((hasNoRoot()) && (((RExpMonomial)monom.elementAt(0)).factor.isUnit())) return RExpression.ONE;
	return this;
}
}
