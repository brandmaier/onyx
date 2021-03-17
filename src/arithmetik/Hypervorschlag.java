package arithmetik;

import java.util.*;
import java.math.*;

// Im Gegensatz zum Vorschlag kann diese Klasse eine beliebige Zahl von Punkten nehmen.
// Für den jeweils untergeordneten Vorschlag sucht sie dann eine Zweierpotenz-Anzahl
// von Punkten heraus, die die zusätzliche Bedingung erfüllen, dass sowohl ihre Summe
// als auch ihr Produkt nahe einer ganzen Zahl sind.

public class Hypervorschlag
{
	public int grad;
	public int maxgrad;
	public int vorschlaggrad;
	public Celement[] punkt;
	private int[] auswahl;
	private Ausdruck naechsterVorschlag;
	private Vorschlag subvorschlag;
	private boolean fertig;
	private Vector schonGegeben;			// zerstört ein bisschen die Idee vom Platzsparen.
	
	public Hypervorschlag() {this(new Celement[1]);}
	public Hypervorschlag(Celement[] punkte)
	{
		this.grad = punkte.length;
		this.punkt = punkte;
		reinitialisiere();
	}
	private void baueSubvorschlag()
	{
		Celement[] sub = new Celement[vorschlaggrad];
		for (int i=0; i<vorschlaggrad; i++)
			sub[i] = punkt[auswahl[i]];
		subvorschlag = new Vorschlag(sub);		
	}
	private void erschaffeNaechstenVorschlag()
	{
		while (!subvorschlag.hasMoreElements())
		{
			fertig = !inkrementiereAuswahl();
			if (fertig) {naechsterVorschlag = null; return;}
			baueSubvorschlag();
		}
		naechsterVorschlag = new Ausdruck(((Ausdruck)(subvorschlag.nextElement())),maxgrad-vorschlaggrad);
		
		FastPolynomial[] neu = naechsterVorschlag.inorder();
		boolean warschon = false;
		for (int i=0; i<schonGegeben.size(); i++)
		{
			FastPolynomial[] alt = (FastPolynomial[])(schonGegeben.elementAt(i));
			boolean gleich = true;
			for (int k=0; k<neu.length; k++)
				if (!alt[k].equals(neu[k])) gleich = false;
			if (gleich) warschon = true;
		}
		if (warschon) erschaffeNaechstenVorschlag();
		else schonGegeben.addElement(neu);
	}
	public boolean hasMoreElements()
	{
		return !fertig;
	}
	private boolean inkrementiereAuswahl()
	{
		int i=0;
		while ((i<vorschlaggrad) && (auswahl[i]>=auswahl[i+1]-1))
		{
			if (i==0) auswahl[i]=0; else auswahl[i] = auswahl[i-1]+1;
			i++;
		}
		if (i<vorschlaggrad)
		{
			auswahl[i]++;
			return true;
		}
		if (vorschlaggrad==1) return false;
		vorschlaggrad /= 2;
		auswahl = new int[vorschlaggrad+1];				// letzter als Blocker.
		auswahl[vorschlaggrad]=grad;
		for (i=0; i<vorschlaggrad; i++)
			auswahl[i] = i;
		
		// Test auf Summe && Produkt
		Celement sum = new Celement(0,0);
		Celement prod = new Celement(1,0);
		for (i=0; i<vorschlaggrad; i++)
		{
			sum = sum.add(punkt[auswahl[i]]);
			prod = prod.multiply(punkt[auswahl[i]]);
		}
		BigInteger[] l1 = sum.includedIntegerPoints();
		BigInteger[] l2 = prod.includedIntegerPoints();
		if ((l1.length == 0) || (l2.length == 0)) inkrementiereAuswahl();
		
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
		schonGegeben = new Vector();
		maxgrad = 1;
		while (maxgrad<=grad) maxgrad *= 2;
		maxgrad /= 2;
		vorschlaggrad = maxgrad;
		auswahl = new int[vorschlaggrad+1];				// letzter als Blocker.
		auswahl[vorschlaggrad]=grad;
		for (int i=0; i<vorschlaggrad; i++)
			auswahl[i] = i;
		baueSubvorschlag();
		fertig = false;
		erschaffeNaechstenVorschlag();
	}
}
