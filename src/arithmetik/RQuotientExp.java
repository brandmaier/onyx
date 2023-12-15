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
	Darstellung von rationalen Funktionen als ein 
	ZählerRExpression und ein NennerRExpression. Ist der ZählerRExpression
	0, so muss der NennerRExpression genau 1 sein. Ein Nenner-
	RExpression = 0 repräsentiert NAN.
*/

public class RQuotientExp implements Field, Squarerootable
{
	public static RuntimeException ZeroByZeroException = new RuntimeException("Division von Nullpolynom durch Nullpolynom");
	public static RuntimeException LongOverflowException = new RuntimeException("Overflow in einem Long");

	public static RQuotientExp ZERO = new RQuotientExp();
	public static RQuotientExp ONE = new RQuotientExp(Qelement.ONE);
	
	public RExpression zaehler,nenner;

	public RQuotientExp ()
	{
		zaehler = new RExpression();
		nenner = new RExpression(Qelement.ONE);
	}
	public RQuotientExp (int identifierNr)
	{
		zaehler = new RExpression(identifierNr);
		nenner = new RExpression(Qelement.ONE);
	}
	public RQuotientExp (Qelement factor)
	{
		zaehler = new RExpression(factor);
		nenner = new RExpression(Qelement.ONE);
	}
	public RQuotientExp (QPolynomial numerator)
	{
		zaehler = new RExpression(numerator);
		nenner = new RExpression(Qelement.ONE);
	}
	public RQuotientExp (RExpression numerator)
	{
		zaehler = new RExpression(numerator);
		nenner = new RExpression(Qelement.ONE);
	}
	public RQuotientExp (RExpression numerator, RExpression denomiator)
	{
		zaehler = new RExpression(numerator);
		nenner = new RExpression(denomiator);
		clean();
	}
	// Das selbe, nur ohne clean.
	public RQuotientExp (RExpression numerator, RExpression denomiator, String nomeaning)
	{
		zaehler = new RExpression(numerator);
		nenner = new RExpression(denomiator);
	}
	public RQuotientExp (RQuotientExp copy)
	{
		zaehler = new RExpression(copy.zaehler);
		nenner = new RExpression(copy.nenner);
	}
	public Ring abs_add (Ring b) {return add((RQuotientExp)b);}
	public Field abs_divide (Field b) {return divide((RQuotientExp)b);}
	public GcdAble abs_divide(GcdAble arg2) {return divide((RQuotientExp)arg2);}
	public GcdAble[] abs_divideAndRemainder(GcdAble arg2) 
	{
		RQuotientExp[] erg = {divide((RQuotientExp)arg2), zero()};
		return erg;
	}
	public GcdAble abs_gcd(GcdAble arg2) {return unit();}
	public boolean abs_isEqual (Ring b) {return isEqual((RQuotientExp)b);}
	public Ring abs_multiply (Ring b) {return multiply ((RQuotientExp)b);}
	public Ring abs_negate () {return negate();}
	public Ring abs_pow(long exp) {return pow(exp);}
	public Field abs_reciprocal () {return reciprocal(); }
	public GcdAble abs_remainder(GcdAble arg2) {return zero();}
	public GcdAble abs_scm(GcdAble arg2) {return unit();}
	public Squarerootable abs_sqrt() {return sqrt();}
	public Ring abs_subtract (Ring b) {return subtract ((RQuotientExp)b);}
	public Ring abs_unit () {return unit ();}
	public Ring abs_zero () {return zero ();}
	public RQuotientExp add (RQuotientExp arg2)
	{
		RQuotientExp erg = new RQuotientExp();
		
		RExpMonomial ggt = nenner.commonContent();
		ggt = ggt.unite(arg2.nenner.commonContent());
		
		RExpression rest1 = nenner.monomialDivide(ggt);
		RExpression rest2 = arg2.nenner.monomialDivide(ggt);
		
		erg.zaehler = (zaehler.multiply(rest2)).add(arg2.zaehler.multiply(rest1));
		erg.nenner  = nenner.multiply(rest2);
		
		erg.clean();
		return erg;
	}
	// Diese Prozedur sorgt für die Eindeutige Nulldarstellung.
	// TvO: Hier liese sich ein Kürzen einbauen, vielleicht jedenfalls
	// ein kürzen von a/a = 1/1.
	// 5.99: Ist eingebaut.
	public void clean()
	{
		if (isCertainlyZero()) {zaehler = new RExpression(); nenner = new RExpression(Qelement.ONE); return; }
		
		RExpMonomial ggt = zaehler.commonContent();
		ggt = ggt.unite(nenner.commonContent());
		
		zaehler = zaehler.monomialDivide(ggt);
		nenner  = nenner.monomialDivide(ggt);
	}
	public double debugEvaluation()
	{
		return (zaehler.debugEvaluation() / nenner.debugEvaluation());
	}
	// leitet das Polynom nach X_*identifierNr* ab. Die anderen Bezeichner werden als
	// Konstanten gehandelt. 
	public RQuotientExp derive(int identifierNr)
	{
		RQuotientExp erg = (zaehler.derive(identifierNr).multiply(new RQuotientExp(nenner))).subtract
			(nenner.derive(identifierNr).multiply(new RQuotientExp(zaehler)));
		erg = erg.divide(new RQuotientExp(nenner.sqr()));
		return erg;
	}
	// Division durch 0 ergibt NAN ( Nenner = 0 ).
	public RQuotientExp divide (RQuotientExp arg2)
	{
		RQuotientExp erg = new RQuotientExp();

		erg.zaehler = zaehler.multiply(arg2.nenner );
		erg.nenner  = nenner .multiply(arg2.zaehler);

		erg.clean();
		return erg;
	}
	// Evaluate Nr. 6 nimmt ein Array von doubles, dass alle vorkommenden
	// Variablen mit einem Wert belegt, und liefert das ausgewertete Polynom zurück.
	public double evaluate(double[] value)
	{
		return zaehler.evaluate(value) / nenner.evaluate(value);
	}
/**
 * Insert the method's description here.
 * Creation date: (19.06.2002 11:42:47)
 * @return arithmetik.RQuotientExp
 * @param varnr int[]
 * @param val arithmetik.RQuotientExp[]
 */
public RQuotientExp evaluate(int[] varnr, RQuotientExp[] val) 
{
	RQuotientExp erg = this;
	for (int i=0; i<varnr.length; i++)
		erg = erg.evaluate(varnr[i], val[i]);
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (14.03.01 16:36:07)
 * @return arithmetik.RQuotientExp
 * @param bezeichnerNr int
 * @param value arithmetik.RQuotientExp
 */
public RQuotientExp evaluate(int bezeichnerNr, RQuotientExp value) 
{
	return zaehler.evaluate(bezeichnerNr, value).divide(nenner.evaluate(bezeichnerNr, value));
}
/**
 * Insert the method's description here.
 * Creation date: (11.01.2003 13:37:07)

	Verlangt die selben Voraussetzungen wie getConjugate in RExpression, und dass der Nenner
	keine Wurzeln hat.
 
 */
public RQuotientExp[] getAllConjugates()
{
	RExpression[] zwerg = zaehler.getAllConjugates();
	RQuotientExp[] erg = new RQuotientExp[zwerg.length];
	for (int i=0; i<erg.length; i++)
	{
		erg[i] = new RQuotientExp(zwerg[i]);
		erg[i].nenner = nenner;
	}
	return erg;
}
	
	public String infos()
	{
		return toString();
	}
	public boolean isCertainlyZero()
	{
		return zaehler.isCertainlyZero();
	}
	public boolean isEqual(RQuotientExp arg2)
	{
		return (subtract(arg2).isZero());
	}
	public boolean isZero()
	{
		boolean erg = zaehler.isZero();
		if ((erg) && (nenner.isZero())) throw ZeroByZeroException;
		else return erg;
	}
	public RQuotientExp multiply (RQuotientExp arg2)
	{
		RQuotientExp erg = new RQuotientExp();

		if (isCertainlyZero() || arg2.isCertainlyZero()) return erg;

		erg.zaehler = zaehler.multiply(arg2.zaehler);
		erg.nenner  = nenner .multiply(arg2.nenner );

		erg.clean();
		return erg;
	}
	public RQuotientExp negate()
	{
		return new RQuotientExp(zaehler.negate(),nenner,"");
	}
/**
 * Insert the method's description here.
 * Creation date: (11.01.2003 13:37:07)
 */
public void newMethod() {}
	public RQuotientExp pow (long n)
	{
		if (n==0) return new RQuotientExp(Qelement.ONE);
		RQuotientExp erg = pow(n/2).sqr();
		if ((n%2)==1) return erg.multiply(this);
		return erg;
	}
	public RQuotientExp reciprocal()
	{
		return new RQuotientExp(nenner, zaehler);
	}
/**
 * 
 * Creation date: (17.06.2002 08:41:10)
 * @param equations arithmetik.RQuotientExp[]
 * @param var int[]

	Gibt einen Vector von RQuotientExp[], in denen jeweils in der selben Reihenfolge wie var die Lösungen
	stehen. Gleichungen, die einen Nullausdruck gleichen, werden ignoriert. Sind die Gleichungen (bis auf Nullzeilen)
	leer, so wird eine Lösung zurückgegeben, in der alle vars 0 sind, und falls var auch ein Array der Länge 0 ist,
	genau ein leeres Array.
 
 */
public static Vector solveSystem(RQuotientExp[] equation, int[] var) 
{
	Vector nenner = new Vector();
	Vector zaehler = new Vector();
	for (int i=0; i<equation.length; i++)
		if (!equation[i].isZero()) 
		{
			FastPolynomial z = equation[i].zaehler.eliminateSquareRoots();
			FastPolynomial n = equation[i].nenner.eliminateSquareRoots();
			FastPolynomial ggT = z.gcd(n);
			zaehler.addElement(z.divide(ggT));
			nenner.addElement(n.divide(ggT));
		}

	if (zaehler.size()==0)
	{
		RQuotientExp[] nullen = new RQuotientExp[var.length];
		for (int i=0; i<nullen.length; i++) nullen[i] = ZERO;
		Vector erg = new Vector(); erg.addElement(nullen);
		return erg;
	}	
	Vector[] eq = new Vector[zaehler.size()];
	FastPolynomial[] ne = new FastPolynomial[nenner.size()];
	FastPolynomial[] za = new FastPolynomial[zaehler.size()];
	for (int i=0; i<eq.length; i++) 
	{
		za[i] = (FastPolynomial)zaehler.elementAt(i); 
		eq[i] = za[i].member;
		ne[i] = (FastPolynomial)nenner.elementAt(i);
	}
	Vector erg = QPolynomial.solveSystem(eq, var, false);
	if (erg==null) return null;

	// Testen...
	for (int i=0; i<erg.size(); i++)
	{
		RQuotientExp[] zwerg = (RQuotientExp[])erg.elementAt(i);
		int j=0;
		while ((j<za.length) && (za[j].evaluate(var, zwerg).isZero()) && (!ne[j].evaluate(var, zwerg).isZero())) 
			j++;
		if (j<za.length) {erg.removeElementAt(i); i--;}		
	}
	return erg;
}
/**
 * Gibt einen Vector mit allen Lösungen des Gleichungssystems aus, die sich durch Wurzeln ausdrücken lassen in
 * der Beschränkung wie in QPolynomial.solveEasy definiert.
 * Creation date: (17.06.2002 08:41:10)
 * @param equations arithmetik.RQuotientExp[]
 * @param var int[]
 */
public static Vector solveSystemEasy(RQuotientExp[] equation, int[] var) 
{
	Vector[] eq = new Vector[equation.length];
	for (int i=0; i<eq.length; i++)
		eq[i] = equation[i].toZeroRepresentingFastPolynomial().member;
	Vector zw = QPolynomial.solveSystemEasy(eq, var);

	// Alle Lösungen müssen jetzt noch einmal getestet werden, ob sie das Ursprungssystem erfüllen.
	for (int i=0; i<zw.size(); i++)
	{
		RQuotientExp[] erg = (RQuotientExp[])zw.elementAt(i);
		int j=0;
		while ((j<equation.length) && (equation[j].evaluate(var, erg).isZero())) j++;
		if (j<equation.length)
		{
			zw.removeElementAt(i);
			i--;
		}
	}
	return zw;
}
	public RQuotientExp solveTo(int identifierNr)
	{
		return this.zaehler.solveTo(identifierNr);
	}
	// Berechnet bei hinreichend dichtem Startvektor durch Newton-Approximation
	// Eine Auflösung eines zweier-Block Nährungsweise. Es wird eine RuntimeException
	// geworfen, falls die Jakobi-Matrix mit keinen Werten invertierbar ist.
	// Die Unbekannten werden aus identiferNr gezogen, und es wird bis zur Genauigkeit eps 
	// approximiert.
	public static double[] solveTwoApprox(RQuotientExp[] x, double[] start, int[] identifierNr, double eps)
	{
		if ((x[0].nenner.isZero()) || (x[1].nenner.isZero()))
			throw new RuntimeException("Denominator of expressions for solveTwoApprox were constantly zero");
		RQuotientExp f11 = x[0].derive(identifierNr[0]);
		RQuotientExp f12 = x[0].derive(identifierNr[1]);
		RQuotientExp f21 = x[1].derive(identifierNr[0]);
		RQuotientExp f22 = x[1].derive(identifierNr[1]);
		RQuotientExp det = f11.multiply(f22).subtract(f12.multiply(f21));
		
		System.out.println("f11 = "+f11);
		System.out.println("f12 = "+f12);
		System.out.println("f21 = "+f21);
		System.out.println("f22 = "+f22);
		System.out.println("det = "+det);
		
		if (det.isZero()) throw new RuntimeException("Determinant of Jacobian Matrix is constantly zero");
		
		double[] xk = new double[2];
		xk[0] = start[0];
		xk[1] = start[1];
		double[] xs = new double[Math.max(identifierNr[0], identifierNr[1])+1];
		for (int i=0; i<xs.length; i++) xs[i] = 0.0;
		double[] move = new double[2]; boolean first = true;
		double epssqr = eps*eps;
		while ((first) || (move[0]*move[0]+move[1]*move[1] > epssqr)) 
		{
			first = false;
			boolean isok = false;
			while (!isok)							// Falls zufällig eine Stelle nahe 0
			{
				xs[identifierNr[0]] = xk[0];
				xs[identifierNr[1]] = xk[1];
				double f11ev = f11.evaluate(xs);
				double f12ev = f12.evaluate(xs);
				double f21ev = f21.evaluate(xs);
				double f22ev = f22.evaluate(xs);
				double p1ev = x[0].evaluate(xs);
				double p2ev = x[1].evaluate(xs);
				move[0] = (f22ev*p1ev - f12ev*p2ev) / (f11ev*f22ev-f12ev*f21ev);
				move[1] = (f11ev*p2ev - f21ev*p1ev) / (f11ev*f22ev-f12ev*f21ev);
				if ((Double.isNaN(move[0])) || (Double.isNaN(move[1]))) 
				{
					isok = false;
					xk[0] += (Math.random()/eps) - (eps/2.0);
					xk[1] += (Math.random()/eps) - (eps/2.0);
				} else isok = true;
			}
			xk[0] = xk[0] - move[0];
			xk[1] = xk[1] - move[1];
		}
		return xk;
	}
	public RQuotientExp sqr()
	{
		return multiply(this);
	}
	public RQuotientExp sqrt()
	{
		return new RQuotientExp(zaehler.sqrt(),nenner.sqrt(),"");
	}
	public RQuotientExp subtract(RQuotientExp arg2)
	{
		return add(arg2.negate());
	}
	public String toString()
	{
		if (nenner.isEqual(new RExpression(Qelement.ONE))) return zaehler.toString();
		return "[ " + zaehler + " ] / [ " + nenner + " ]";
	}
/**
 * Gibt ein FastPolynomial zurück, dass 0 ist dann und genau dann, wenn der
 * RExpression des Zählers zu 0 auswertet, während der Nenner an dieser Stelle
 * nicht zu 0 auswertet.
 * Creation date: (15.06.2002 21:09:00)
 * @return arithmetik.FastPolynomial
 */
public FastPolynomial toZeroRepresentingFastPolynomial() 
{
	FastPolynomial z = zaehler.eliminateSquareRoots();
	FastPolynomial n = nenner.eliminateSquareRoots();
	FastPolynomial ggT = z.gcd(n);
	return (z.divide(ggT));
}
	public RQuotientExp unit()
	{
		return new RQuotientExp(Qelement.ONE);
	}
	public RQuotientExp zero()
	{
		return new RQuotientExp();
	}
}
