package arithmetik;

import java.util.*;
import java.math.*;

// Simuliert einen Ausdruck der Form BigInteger, FastPolynomial oder Ausdruck + sqrt(Ausdruck).

public class Ausdruck
{
	public boolean bodenIstFastPolynomial;
	public boolean istBoden;
	public FastPolynomial fastPolynomialWert;
	public BigInteger bigIntegerWert;
	public Ausdruck vorn;
	public Ausdruck hinten;
	
	public Ausdruck (FastPolynomial[] inorder)
	{
		if (inorder.length==1) 
		{
			istBoden = true;
			fastPolynomialWert = inorder[0];
			bodenIstFastPolynomial = true;
		} else
		{
			istBoden = false;
			bodenIstFastPolynomial = true;
			int l = inorder.length / 2;
			FastPolynomial[] e1 = new FastPolynomial[l];
			for (int i=0; i<l; i++)
				e1[i] = inorder[i];
			FastPolynomial[] e2 = new FastPolynomial[l];
			for (int i=0; i<l; i++)
				e2[i] = inorder[l+i];
			vorn = new Ausdruck(e1);
			hinten = new Ausdruck(e2);
		}
	}
	public Ausdruck (BigInteger[] inorder)
	{
		if (inorder.length==1) 
		{
			istBoden = true;
			bigIntegerWert = inorder[0];
			bodenIstFastPolynomial = false;
		} else
		{
			istBoden = false;
			bodenIstFastPolynomial = false;
			int l = inorder.length / 2;
			BigInteger[] e1 = new BigInteger[l];
			for (int i=0; i<l; i++)
				e1[i] = inorder[i];
			BigInteger[] e2 = new BigInteger[l];
			for (int i=0; i<l; i++)
				e2[i] = inorder[l+i];
			vorn = new Ausdruck(e1);
			hinten = new Ausdruck(e2);
		}
	}
	public Ausdruck (Ausdruck copy)
	{
		bodenIstFastPolynomial = copy.bodenIstFastPolynomial;
		istBoden = copy.istBoden;
		fastPolynomialWert = copy.fastPolynomialWert;
		bigIntegerWert = copy.bigIntegerWert;
		if (!istBoden) 
		{
			vorn = new Ausdruck(copy.vorn);
			hinten = new Ausdruck(copy.hinten);
		}
	}
	public Ausdruck (Ausdruck vorn, int nullen)
	{
		Ausdruck copy;
		bodenIstFastPolynomial = vorn.bodenIstFastPolynomial;
		if (nullen == 0)
		{
			istBoden = vorn.istBoden;
			fastPolynomialWert = vorn.fastPolynomialWert;
			bigIntegerWert = vorn.bigIntegerWert;
			if (!istBoden)
			{
				this.vorn  = new Ausdruck(vorn.vorn);
				hinten = new Ausdruck(vorn.hinten);
			}
		} else 
		{
			if (bodenIstFastPolynomial)
			{
				FastPolynomial[] in = inorder();
				FastPolynomial[] out = new FastPolynomial[in.length+nullen];
				for (int i=0; i<out.length; i++)
				{
					if (i<in.length) out[i] = in[i]; 
					else out[i] = new FastPolynomial();
				}
				copy = new Ausdruck(out);
			} else 
			{
				BigInteger[] in = vorn.BigIntegerInorder();
				BigInteger[] out = new BigInteger[in.length+nullen];
				for (int i=0; i<out.length; i++)
				{
					if (i<in.length) out[i] = in[i]; 
					else out[i] = BigInteger.valueOf(0);
				}
				copy = new Ausdruck(out);
			}
			istBoden = false;
			this.vorn = new Ausdruck(copy.vorn);
			hinten = new Ausdruck(copy.hinten);
		}
	}
	public Ausdruck (Ausdruck vorn, Ausdruck hinten)
	{
		istBoden = false;
		bodenIstFastPolynomial = vorn.bodenIstFastPolynomial;
		this.vorn = vorn;
		this.hinten = hinten;
	}
	public Ausdruck (FastPolynomial fastPolynomialWert)
	{
		istBoden = true;
		bodenIstFastPolynomial = true;
		this.fastPolynomialWert = fastPolynomialWert;
	}
	public Ausdruck (BigInteger bigIntegerWert)
	{
		istBoden = true;
		bodenIstFastPolynomial = false;
		this.bigIntegerWert = bigIntegerWert;
	}
	// Diese Methode gibt ein Inorder-Feld der Bodenelemente als BigInteger zurück, falls das geht;
	// ansonste null.
	public BigInteger[] BigIntegerInorder()
	{
		if (bodenIstFastPolynomial) return null;
		if (this.istBoden) 
		{
			BigInteger[] erg = new BigInteger[1];
			erg[0] = this.bigIntegerWert;
			return erg;
		}
		BigInteger[] e1 = vorn.BigIntegerInorder();
		BigInteger[] e2 = hinten.BigIntegerInorder();
		BigInteger[] erg = new BigInteger[e1.length + e2.length];
		for (int i=0; i<e1.length; i++)
			erg[i] = e1[i];
		for (int i=0; i<e2.length; i++)
			erg[e1.length+i] = e2[i];
		return erg;
	}
	// Diese Methode gibt ein Inorder-Feld der Bodenelemente als FastPolynomiale zurück.
	public FastPolynomial[] inorder()
	{
		if (this.istBoden) 
		{
			FastPolynomial[] erg = new FastPolynomial[1];
			if (this.bodenIstFastPolynomial) erg[0] = new FastPolynomial(this.fastPolynomialWert);
			else erg[0] = new FastPolynomial(new Qelement(this.bigIntegerWert));
			return erg;
		}
		FastPolynomial[] e1 = vorn.inorder();
		FastPolynomial[] e2 = hinten.inorder();
		FastPolynomial[] erg = new FastPolynomial[e1.length + e2.length];
		for (int i=0; i<e1.length; i++)
			erg[i] = e1[i];
		for (int i=0; i<e2.length; i++)
			erg[e1.length+i] = e2[i];
		return erg;
	}
	// null als Rückgabewert: Interpolation war unmöglich.
	public static Ausdruck interpolarisation(Datenmatrix ausdruecke, int bezeichnernr)
	{
		Datenmatrix feldmatrix = new Datenmatrix(ausdruecke.dimgroesse);
		for (int i=0; i<ausdruecke.groesse; i++)
			feldmatrix.setzeElementBeiStelle(((Ausdruck)(ausdruecke.elementBeiStelle(i))).inorder(),i);
		int anzpoly = ((FastPolynomial[])(feldmatrix.elementBeiStelle(0))).length;
		int[] z = new int[feldmatrix.dim];
		for (int ueberdim = feldmatrix.dim-1; ueberdim >=0; ueberdim--)
		{
			if (feldmatrix.dimgroesse[ueberdim]>1)
			{
				FastPolynomial[] stuetz = new FastPolynomial[feldmatrix.dimgroesse[ueberdim]];
				for (int i=0; i<feldmatrix.dim; i++) z[i] = 0;

				z[ueberdim] = 0;
				FastPolynomial[] zwerg = ((FastPolynomial[])(feldmatrix.elementBei(z)));
				for (int k=0; k<anzpoly; k++)
				{
					for (int j=0; j<feldmatrix.dimgroesse[ueberdim]; j++)
					{
						z[ueberdim]=j;
						stuetz[j] = ((FastPolynomial[])(feldmatrix.elementBei(z)))[k];
					}
					zwerg[k] = interpoliereSingulaer(stuetz,ueberdim);
					if (zwerg[k] == null) return null;
				}
				z[ueberdim] = 0;
				feldmatrix.setzeElementBei(zwerg,z);

				int i=0; 
				while (i<ueberdim)
				{
					i=0;
					while ((i<ueberdim) && (z[i]>=feldmatrix.dimgroesse[i]-1))
					{
						z[i]=0;
						i++;
					}
					if (i<ueberdim)
					{
						z[i]++;
						z[ueberdim] = 0;
						zwerg = ((FastPolynomial[])(feldmatrix.elementBei(z)));
						for (int k=0; k<anzpoly; k++)
						{
							for (int j=0; j<feldmatrix.dimgroesse[ueberdim]; j++)
							{
								z[ueberdim]=j;
								stuetz[j] = ((FastPolynomial[])(feldmatrix.elementBei(z)))[k];
							}
							zwerg[k] = interpoliereSingulaer(stuetz,ueberdim);
							if (zwerg[k] == null) return null;
						}
						z[ueberdim] = 0;
						feldmatrix.setzeElementBei(zwerg,z);
					}
				}
			}
		}
		return new Ausdruck((FastPolynomial[])(feldmatrix.elementBeiStelle(0)));
	}
	// vorsicht: Verändert die Übergabe-*FastPolynomiale* stuetz!
	// Verfahren nach Newton-Interpolaristationsverfahren.
	// Falls die Ganzzahligkeit verletzt wird, gibt die Routine sofort null zurück.
	private static FastPolynomial interpoliereSingulaer(FastPolynomial[] stuetz, int bezeichnernr)
	{
		Qelement fak = new Qelement(1);
		for (int i=0; i<stuetz.length-1; i++)
		{
			for (int j=0; j<stuetz.length-i-1; j++)
			{
				stuetz[stuetz.length-1-j] = 
						(stuetz[stuetz.length-1-j].subtract(stuetz[stuetz.length-2-j])).multiply(new FastPolynomial(fak));
				if (!stuetz[stuetz.length-1-j].isIntegerFactors())
					return null;
			}
			fak = new Qelement(1,i+2);
		}
		FastPolynomial erg = new FastPolynomial(stuetz[stuetz.length-1]);
		for (int i=stuetz.length-2; i>=0; i--)
		{
			FastPolynomial f = (new FastPolynomial(bezeichnernr)).subtract(new FastPolynomial(i));
			erg = (f.multiply(erg)).add(stuetz[i]);
		}
		return erg;
	}
	public RExpression toRExpression()
	{
		if ((istBoden) && (bodenIstFastPolynomial)) return new RExpression(fastPolynomialWert);
		if ((istBoden) && (bodenIstFastPolynomial)) return new RExpression(new Qelement(bigIntegerWert));
		return (vorn.toRExpression()).add(hinten.toRExpression().sqrt());
	}
	public String toString()
	{
		if ((istBoden) && (bodenIstFastPolynomial)) return fastPolynomialWert.toString();
		if ((istBoden) && (bodenIstFastPolynomial)) return ""+bigIntegerWert;
		return vorn+"+sqrt("+hinten+")";
	}
}
