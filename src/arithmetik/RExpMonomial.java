package arithmetik;

import java.util.*;

public class RExpMonomial
{
	FastPolynomial factor;
	Vector member;				// Enthält die referierten RExpressions; pointer ist wichtig.
	
	public RExpMonomial()
	{
		factor = new FastPolynomial(Qelement.ONE);
		member = new Vector();
	}
	public RExpMonomial(FastPolynomial factor)
	{
		this.factor = new FastPolynomial(factor);
		member = new Vector();
	}
	public RExpMonomial(FastPolynomial factor, RExpression underRoot)
	{
		this.factor = new FastPolynomial(factor);
		member = new Vector();
		member.addElement(underRoot);
	}
	public RExpMonomial(Qelement factor)
	{
		this.factor = new FastPolynomial(factor);
		member = new Vector();
	}
	public RExpMonomial (RExpMonomial copy)
	{
		this.factor = new FastPolynomial(copy.factor);
		member = (Vector)copy.member.clone();
	}
	// Gibt 2 Vektoren zurück: Einer enthält alle RExpressions, die in beiden Monomen
	// auftauchen, der zweite enthält alle, die nur einmal insgesamt auftauchten.
	// Die Member sind Datenbankelement, d.h. es werden keine Konstruktoren aufgerufen,
	// sondern die selben pointer weitergegeben !
	public Vector[] collectSimilar (RExpMonomial arg2)
	{
		Vector[] erg = new Vector[2];
		erg[0] = new Vector(); erg[1] = new Vector();
		
		int p1 = 0, p2 = 0;
		while ((p1 < member.size()) && (p2 < arg2.member.size()))
		{
			RExpression m1 = (RExpression)member.elementAt(p1);
			RExpression m2 = (RExpression)arg2.member.elementAt(p2);
			RExpression neu = null;
			
			int comp = RExpression.databaseCompare(m1,m2);
			if (comp ==  0) {erg[0].addElement(m1); p1++; p2++; }
			if (comp == -1) {erg[1].addElement(m1);; p1++; }
			if (comp ==  1) {erg[1].addElement(m2);; p2++; }
		}
		while (p1 < member.size())      erg[1].addElement((RExpression)member.elementAt(p1++));
		while (p2 < arg2.member.size()) erg[1].addElement((RExpression)arg2.member.elementAt(p2++));

		return erg;
	}
	public int compareTo(RExpMonomial arg2)
	{
		for (int i=0; (i<member.size()) && (i<arg2.member.size()); i++)
		{
			int c = RExpression.databaseCompare((RExpression)member.elementAt(i),(RExpression)arg2.member.elementAt(i));
			if (c != 0) return c;
		}
		if (member.size() > arg2.member.size()) return  1;
		if (member.size() < arg2.member.size()) return -1;
		return 0;
	}
	public double debugEvaluation()
	{
		double erg = factor.debugEvaluation();
		for (int i=0; i<member.size(); i++)
			erg *= Math.sqrt(((RExpression)member.elementAt(i)).debugEvaluation());
		return erg;
	}
	// leitet das Monom nach X_*identifierNr* ab. Die anderen Bezeichner werden als
	// Konstanten gehandelt. 
	public RQuotientExp derive(int identifierNr)
	{
		if (member.size()==0) return new RQuotientExp(new RExpression(factor.derive(identifierNr)));
		RExpression prod = new RExpression(Qelement.ONE);
		for (int i=0; i<member.size(); i++)
			prod = prod.multiply((RExpression)member.elementAt(i));
		RExpression prodsqrt = prod.sqrt();
		
		RQuotientExp erg = new RQuotientExp((new RExpression(factor.derive(identifierNr))).multiply(prodsqrt));
		erg = erg.add(prod.derive(identifierNr).divide((new RQuotientExp(Qelement.TWO)).multiply(new RQuotientExp(prodsqrt))));

		return erg;
	}
	public boolean equals(RExpMonomial arg2)
	{
		return (compareTo(arg2)==0);
	}
	// Evaluate Nr. 6 nimmt ein Array von Doubels, dass alle vorkommenden
	// Variablen mit einem Wert belegt, und liefert das ausgewertete Polynom zurück.
	public double evaluate(double[] value)
	{
		double erg = factor.evaluate(value);
		for (int i=0; i<member.size(); i++)
			erg *= Math.sqrt(((RExpression)member.elementAt(i)).evaluate(value));
		return erg;
	}
	public RQuotientExp evaluate(int identifierNr, RQuotientExp value)
	{
		RQuotientExp erg = factor.evaluate(identifierNr, value);
		for (int i=0; i<member.size(); i++)
			erg = erg.multiply
				(((RExpression)member.elementAt(i)).evaluate(identifierNr, value).sqrt());
		return erg;
	}
	// liefert den höchsten vorkommenden Index (i.A. die Anzahl der Variablen).
	public int getHighestIndex()
	{
		int erg = factor.getHighestIndex();
		for (int i=0; i<member.size(); i++)
			erg = Math.max(erg, ((RExpression)member.elementAt(i)).getHighestIndex());
		return erg;
	}
	public boolean isNoRoot()
	{
		return member.size() == 0;
	}
	// Berechnet lazyDivide auf den FastPolynomials und löscht danach alle Einträge aus *this*,
	// die in *arg2* vorkommen.
	public RExpMonomial lazyDivide(RExpMonomial arg2)
	{
		RExpMonomial erg = new RExpMonomial(factor.lazyDivide(arg2.factor));
		for (int i=0; i<member.size(); i++)
		{
			RExpression tm = (RExpression)member.elementAt(i);
			boolean rein = true;
			for (int j=0; j<arg2.member.size(); j++)
				if (tm == arg2.member.elementAt(j)) rein = false;
			if (rein) erg.member.addElement(tm);
		}
		return erg;
	}
	public RExpMonomial negate()
	{
		RExpMonomial erg = new RExpMonomial();
		erg.factor = factor.negate();
		erg.member = (Vector)member.clone();		
		return erg;
	}							   
	public String toString()
	{
		String erg = "";
		boolean eins = factor.equals(new FastPolynomial(Qelement.ONE));
		if ((member.size()==0) || (!eins)) erg += "("+factor+")";
		if ((member.size()>0) && (!eins)) erg += "*";
		if (member.size()>0)
		{
			erg += "sqrt[";
			for (int i=0; i<member.size(); i++)
			{
				erg+="("+member.elementAt(i).toString()+")";
				if (i<member.size()-1) erg += "*";
			}
			erg+= "]";
		}
		return erg;
	}
	public RExpMonomial unite(RExpMonomial arg2)
	{
		RExpMonomial erg = new RExpMonomial(this.factor.lazyGcd(arg2.factor));
		erg.member = collectSimilar(arg2)[0];
		return erg;
	}

/**
 * Insert the method's description here.
 * Creation date: (11.01.2003 13:15:43)
 * @return int

	gibt die maximale Verschachtelungstiefe wieder.
 
 */
public int getDepth() 
{
	int erg = 0;
	for (int i=0; i<member.size(); i++)
	{
		int md = ((RExpression)member.elementAt(i)).getDepth() + 1;
		if (md > erg) erg = md;
	}
	return erg;		
}
}
