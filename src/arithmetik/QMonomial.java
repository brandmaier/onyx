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

import engine.Statik;

public class QMonomial
{
	Qelement factor;
	int[] exp;
	
	public QMonomial()
	{
		factor = Qelement.ONE;
		exp = new int[0];
	}
	public QMonomial(int identifierNr)
	{
		this(new Qelement(1), identifierNr);
	}
	public QMonomial(Qelement factor)
	{
		this.factor = factor;
		exp = new int[0];
	}
	public QMonomial (Qelement factor, int[] exponents)
	{
		this.factor = factor;
		this.exp = new int[exponents.length];
		for (int i=0; i<exponents.length; i++)
			exp[i] = exponents[i];
	}
	public QMonomial(Qelement factor, int identifierNr)
	{
		this.factor = new Qelement(factor);
		exp = new int[identifierNr+1];
		for (int i=0; i<identifierNr; i++) exp[i] = 0;
		exp[identifierNr] = 1;
	}
	public QMonomial (Qelement factor, int identifierNr, int exponent)
	{
		this(factor, identifierNr);
		exp[identifierNr] = exponent;
	}
	public QMonomial (QMonomial copy)
	{
		this.factor = new Qelement(copy.factor);
		this.exp = new int[copy.exp.length];
		for (int i=0; i<exp.length; i++)
			exp[i] = copy.exp[i];
	}
	public double debugEvaluation()
	{
		double erg = factor.toDouble();
		for (int i=0; i<exp.length; i++)
			erg *= Math.pow((i+2), exp[i]);
		return erg;
	}
	public int getExponent(int identifierNr)
	{
		if (identifierNr >= exp.length) return 0;
		else return exp[identifierNr];
	}
	// liefert den höchsten vorkommenden Index (i.A. die Anzahl der Variablen).
	public int getHighestIndex()
	{
		for (int i=exp.length-1; i>=0; i--)
			if (exp[i]!=0) return i;
		return 0;
	}
	// liefert den Totalgrad dieses Monoms, d.h. die Summe der Exponenten
	public int getTotalDegree()
	{
		int erg = 0;
		for (int i=0; i<exp.length; i++)
			erg += exp[i];
		return erg;
	}
	public boolean isConstant()
	{
		for (int i=0; i<exp.length; i++)
			if (exp[i]!=0) return false;
		return true;
	}
	public boolean isUnit()
	{
		for (int i=0; i<exp.length; i++)
			if (exp[i]!=0) return false;
		return (factor.equals(new Qelement(1)));
	}
	public int lexorderCompareTo(QMonomial arg2)
	{
		for (int i=0; (i<exp.length) || (i<arg2.exp.length); i++)
		{
			if (getExponent(i)<arg2.getExponent(i)) return -1;
			if (getExponent(i)>arg2.getExponent(i)) return 1;
		}
		return 0;
	}
	public QMonomial multiply(QMonomial arg2)
	{
		int[] erg = new int[Math.max(exp.length, arg2.exp.length)];
		for (int i=0; i<erg.length; i++)
		{
			if (i>= exp.length) erg[i] = arg2.exp[i];
			if (i>= arg2.exp.length) erg[i] = exp[i];
			if ((i< exp.length) && (i< arg2.exp.length)) erg[i] = exp[i]+arg2.exp[i];			
		}
		return new QMonomial(factor.multiply(arg2.factor), erg);
	}
	public void setExponent(int identifierNr, int exponent)
	{
		if ((exponent!=0) && (identifierNr >= exp.length))
		{
			int[] neu = new int[identifierNr+1];
			for (int i=0; i<neu.length-1; i++)
			{
				if (i<exp.length) neu[i]=exp[i];
				else neu[i] = 0;
			}
			exp = neu;
		}
		if (identifierNr<exp.length)
			exp[identifierNr] = exponent;
	}
	public String toString()
	{
		String erg = "";

		String z = "";
		for (int i=0; i<exp.length; i++)
		{
			if (exp[i]!=0) {erg += z+"X"+i; z = "*";}
			if (exp[i]>1) erg += "^"+exp[i];
		}

		boolean eins = factor.equals(Qelement.ONE);
		if ((erg.length()==0) || (!eins)) 
		{
			if (erg.length()==0) return ""+factor;
			else return factor+"*"+erg;
		}
		return erg;
	}

/**
 * Insert the method's description here.
 * Creation date: (05.01.2003 15:36:28)
 * @return arithmetik.QMonomial
 * @param q arithmetik.Qelement
 */
public QMonomial divide(Qelement q) 
{
	return new QMonomial(factor.divide(q), exp);
}

/**
 * Insert the method's description here.
 * Creation date: (13.07.2002 10:07:14)
 * @return arithmetik.QMonomial
 * @param arg2 arithmetik.QMonomial
 */
public QMonomial divide(QMonomial arg2) 
{
	int g = Math.max(getHighestIndex(), arg2.getHighestIndex());
	QMonomial erg = new QMonomial(factor.divide(arg2.factor));
	for (int i=0; i<=g; i++)
	{
		int j = getExponent(i)-arg2.getExponent(i);
		if (j<0) throw new RuntimeException("Division of Monomial by Monomial of higher degree");
		erg.setExponent(i,j);
	}
	return erg;		
}

/**
 * Insert the method's description here.
 * Creation date: (13.07.2002 09:45:03)
 * @return boolean
 * @param arg2 arithmetik.QMonomial
 */
public boolean divides(QMonomial arg2) 
{
	int g = Math.max(arg2.getHighestIndex(),getHighestIndex());
	for (int i=0; i<=g; i++)
	{
		if (arg2.getExponent(i) < getExponent(i)) return false;
	}
	return true;
}

/**
 * Insert the method's description here.
 * Creation date: (21.02.2003 14:02:33)
 * @return boolean
 * @param arg2 arithmetik.QMonomial
 */
public boolean equals(QMonomial arg2) 
{
	int ix = Math.max(exp.length, arg2.exp.length);
	for (int i=0; i<ix; i++)
	{
		if (i < exp.length)
		{
			if (i < arg2.exp.length)
			{
				if (exp[i]!=arg2.exp[i]) return false;
			}
			else if (exp[i]!=0) return false;
		} else if (arg2.exp[i]!=0) return false;
	}
	return factor.isEqual(arg2.factor);
		
}

/**
 * Insert the method's description here.
 * Creation date: (08.05.2003 13:49:56)
 * @return boolean
 * @param o java.lang.Object
 */
public boolean equals(Object o) 
{
	if (o instanceof QMonomial) return equals((QMonomial)o); else return false;
}

/**
 * Insert the method's description here.
 * Creation date: (07.05.2003 19:00:57)
 * @return arithmetik.QMonomial[]
 * @param ideal arithmetik.QMonomial[]

	erwartet die (minimalen) Erzeuger eines Monom-Ideals und liefert alle Monom zurück, die nicht in dem Ideal
	sind, falls dies endlich viele sind; ansonsten wird null zurückgegeben.
 
 */
public static QMonomial[] getRemainderOfIdeal(QMonomial[] ideal) 
{
	int ix = 0; 
	for (int i=0; i<ideal.length; i++) ix = Math.max(ix, ideal[i].getHighestIndex());
	int[] max = new int[ix+1];
	for (int i=0; i<max.length; i++) max[i] = Integer.MAX_VALUE;

	// suche nach den Erzeugern mit nur einem exponenten > 0
	for (int i=0; i<ideal.length; i++)
	{
		int val = -1, nr = -1;
		for (int j=0; j<ix+1; j++)
		{
			int ex = ideal[i].getExponent(j);
			if (ex > 0)
			{
				if (val==-1) {val = ex; nr = j;}
				else val = -2;
			}
		}
		if (val > 0) max[nr] = Math.min(max[nr],val);
	}
	int[] exps = new int[ix+1];
	for (int i=0; i<exps.length; i++) {exps[i] = 0; if (max[i]==Integer.MAX_VALUE) return null;}
	java.util.Vector erg = new java.util.Vector();
	while (exps[ix] <= max[ix])
	{
		QMonomial m = new QMonomial(Qelement.ONE, exps);
		boolean takeit = true;
		for (int i=0; i<ideal.length; i++)
			if (ideal[i].divides(m)) takeit = false;
		if (takeit) erg.addElement(m);
		exps[0]++;
		int i=0;
		while ((i<exps.length) && (exps[i] > max[i])) {exps[i] = 0; i++; if (i<exps.length) exps[i]++; else exps[i-1]=max[i-1]+1;}
	}
	QMonomial[] ergA = new QMonomial[erg.size()];
	for (int i=0; i<ergA.length; i++) ergA[i] = (QMonomial)erg.elementAt(i);
	
	return ergA;
}

/**
 * Insert the method's description here.
 * Creation date: (08.05.2003 13:42:37)
 * @return int
 */
public int hashCode() 
{
	int k=0; for (int i=0; i<exp.length; i++) k += exp[i]*Math.pow(2,i);
	
	return factor.hashCode()+k;
}

/**
 * Insert the method's description here.
 * Creation date: (13.07.2002 09:42:40)
 * @return arithmetik.QMonomial
 * @param arg2 arithmetik.QMonomial
 */
public QMonomial leastCommonMultiple(QMonomial arg2) 
{
	int g = Math.max(arg2.getHighestIndex(),getHighestIndex());
	QMonomial erg = new QMonomial(Qelement.ONE);
	for (int i=0; i<=g; i++)
	{
		int j = Math.max(arg2.getExponent(i),getExponent(i));
		if (j!=0) erg.setExponent(i, j);
	}
	return erg;
}

    /**
     * Insert the method's description here.
     * Creation date: (05.01.2003 15:42:36)
     * @return arithmetik.QMonomial
     * @param m arithmetik.Qelement
     */
    public QMonomial multiply(Qelement m) 
    {
    	return new QMonomial(factor.multiply(m), exp);
    }
    public String toDoubleString() {
        String erg = "";

        String z = "";
        for (int i=0; i<exp.length; i++)
        {
            if (exp[i]!=0) {erg += z+"X"+i; z = "*";}
            if (exp[i]>1) erg += "^"+exp[i];
        }

        boolean eins = factor.equals(Qelement.ONE);
        if ((erg.length()==0) || (!eins)) 
        {
            if (erg.length()==0) return ""+Statik.doubleNStellen(factor.toDouble(), 3);
            else return Statik.doubleNStellen(factor.toDouble(), 3)+"*"+erg;
        }
        return erg;
    }
}
