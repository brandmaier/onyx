package arithmetik;

import java.util.*;
import java.math.*;

// Diese Klasse ist eine Aufzählung von Ausdrücken, die die mitgegebenen Nährungen von 
// Nullstellen durch Wurzel ausdrücken könnten. Die Rückgabe erfolgt in Objekten 
// der Klasse *Ausdruck* mit BigIntegers als Boden.

public class Vorschlag implements Enumeration
{
	public int grad;
	public Celement[] punkt;
	private int[] permutation;
	private Ausdruck naechsterVorschlag;
	private Vorschlag ungewurzelt;
	private Vorschlag gewurzelt;
	private Ausdruck ungewurzeltVorschlag;
	private boolean fertig;
	private int einsgradZaehler;
	private BigInteger einsgradBigInteger[];
//	private Vector schonGegeben;			// zerstört ein bisschen die Idee vom Platzsparen.
	
	public boolean zeigeFortschritt;
	
	public Vorschlag() {this(1, new Celement[1]);}
	public Vorschlag(Celement[] punkte) {this(punkte.length,punkte);}
	public Vorschlag(int grad, Celement[] punkte)
	{
		this.grad = grad;
		this.punkt = punkte;
		permutation = new int[grad];
		reinitialisiere();
		zeigeFortschritt = false;
	}
	private void baueSubvorschlaege()
	{
		Celement[] aussen = new Celement[grad/2];
		Celement[] innen = new Celement[grad/2];
		int z=0;
		for (int i=0; i<grad; i++)
			if (permutation[i]>i)
			{
				aussen[z] = (punkt[i].add(punkt[permutation[i]])).divide(new Celement(2,0));
				innen[z] = (punkt[i].subtract(aussen[z])).sqr();
				z++;
			}
		ungewurzelt = new Vorschlag(grad/2 , aussen);
		gewurzelt = new Vorschlag(grad/2 , innen);	
	}
	private void erschaffeNaechstenVorschlag()
	{
		if (grad==1)
		{
			if (einsgradZaehler == einsgradBigInteger.length) 
			{
				naechsterVorschlag = null;
				fertig = true;
				return;
			}
			naechsterVorschlag = new Ausdruck(einsgradBigInteger[einsgradZaehler++]);
		} else {		
			if (ungewurzeltVorschlag == null) 
			{
				while (!ungewurzelt.hasMoreElements())
				{
					if (!this.inkrementierePermutation(grad/2))
					{
						naechsterVorschlag = null;
						fertig = true;
						return;
					}
					baueSubvorschlaege();
				}
				ungewurzeltVorschlag = ((Ausdruck)(ungewurzelt.nextElement()));
				gewurzelt.reinitialisiere();
			}

			while (!gewurzelt.hasMoreElements())
			{
				while (!ungewurzelt.hasMoreElements()) 
				{
					if (!this.inkrementierePermutation(grad/2))
					{
						naechsterVorschlag = null;
						fertig = true;
						return;
					}
					baueSubvorschlaege();
				}
				ungewurzeltVorschlag = ((Ausdruck)(ungewurzelt.nextElement()));
				gewurzelt.reinitialisiere();
			}
			Ausdruck gewurzeltVorschlag = ((Ausdruck)(gewurzelt.nextElement()));
			naechsterVorschlag = new Ausdruck(ungewurzeltVorschlag,gewurzeltVorschlag);
		}
	}
	public boolean hasMoreElements()
	{
		return !fertig;
	}
	// erster Aufruf mit grad/2
	private boolean inkrementierePermutation(int workat)
	{
		int z = workat;
		int i=-1;
		while (z>0)
		{
			i++;
			if (permutation[i] > i) z--;
		}
		int k = permutation[i];
		permutation[k]=-1;
		k++;
		while ((k<grad) && (permutation[k]!=-1)) k++;
		if (k<grad) 
		{
			permutation[i] = k;
			permutation[k] = i;
			return true;
		}
		if (workat==1) return false;
		permutation[i]=-1;
		boolean erg = this.inkrementierePermutation(workat-1);
		if (!erg) return false;
		i=0;
		while (permutation[i]!=-1) i++;
		k=i+1;
		while (permutation[k]!=-1) k++;
		permutation[i]=k;
		permutation[k]=i;
			
		if ((zeigeFortschritt) && (workat==grad/2))
		{
			for (int m=0; m<grad; m++)
				System.out.print(permutation[m]+",");
			System.out.println();
		}
			
		return true;
	}
	public Object nextElement()
	{
		if ((fertig) || (naechsterVorschlag == null))
		{
			fertig = true;
			return null;
		}
		Ausdruck b = this.naechsterVorschlag;
		erschaffeNaechstenVorschlag();
		return b;
	}
	public void reinitialisiere()
	{
//		schonGegeben = new Vector();
		if (grad >= 2)
		{
			for (int i=0; i<grad; i += 2)
			{
				permutation[i]=i+1;
				permutation[i+1]=i;
			}
			naechsterVorschlag = null;
			baueSubvorschlaege();
			fertig = false;
		}
		else 
		{
			einsgradZaehler = 0;
			einsgradBigInteger = punkt[0].includedIntegerPoints();
		}
		erschaffeNaechstenVorschlag();
	}
}
