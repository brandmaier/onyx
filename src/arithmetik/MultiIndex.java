package arithmetik;

import java.util.*;
import java.math.*;

/**
 * Verwaltet eine (lexikographisch sortierte) Menge von Multiindizies.
 */
public class MultiIndex
{
	// Hier werden die Multiindizies drinn gespeichert...
	private Vector idx;
	
	// ***** Konstruktoren *****
	
	/**
	 * Erzeuge eine (leere) Menge von Multiindizies
	 */
	public MultiIndex()
	{
		idx = new Vector();
	}
	
	/**
	 * Erzeugt die Menge der (Multi)-Indizies [0] bis [d]
	 */
	public MultiIndex(int d)
	{
		idx = new Vector();
		for (int i = 0; i <= d; i++)
		{
			int[] t = new int[1];
			t[0] = i;
			idx.addElement(t);
		}
	}

	
	// ***** Methoden *****
	
	/**
	 * Vergleicht index mit dem Multiindex an Position i und liefert
	 * -1, falls index <_lex Multiindex an Position i
	 *  0, falls index =_lex Multiindex an Position i
	 * +1, falls index >_lex Multiindex an Position i
	 * Falls kein Multiindex an Position i steht, wird +1 zurueckgeliefert.
	 */
	public int compare(int[] index, int i)
	{
		int[] tmp = (int[])idx.elementAt(i);
		int j;
		// Teste von vorne nach hinten auf Gleichheit.
		for (j = 0 ; j < Math.min(index.length,tmp.length) ; j++)
		{
			if (index[j] < tmp[j]) return -1;
			else if (index[j] > tmp[j]) return 1;
		}
		// Ist ein Index kuerzer als der andere, teste, ob der andere nur noch Nullen enthaellt...
		// Dann sind sie gleich, anderenfalls ist der andere groesser.
		if (index.length < tmp.length) {
			for (; j < tmp.length ; j++)
				if (tmp[j] != 0) return -1;
			return 0;
		} else if (index.length > tmp.length) {
			for (; j < index.length ; j++)
				if (index[j] != 0) return +1;
			return 0;
		}
		// Beide sind gleichlang und enthalten dieselben Zahlen
		return 0;
	}
	
	/**
	 * Vergleicht die Stelle i-1 und i mit index und liefert
	 * -1, falls index < Multiindex(i-1)
	 *  1, falls index > Multiindex(i)
	 *  0, falls Multiindex(i-1) < index < Multiindex(i)
	 * -2, falls Multiindex(i-1) = index
	 *  2, falls Multiindex(i) = index
	 */
	public int checkAtPos(int[] index, int i)
	{
		// Wir haben das erste Element der Liste...
		if (i == 0)
		{
			int cmp = compare(index,i);
			// Entweder ist index kleiner als der kleinste index in der Liste...
			if (cmp == -1) return 0;
			// Oder gleich
			else if (cmp == 0) return 2;
			//oder groesser
			else return 1;
		} else if (i-1 == idx.size()-1) // Wir haben das letzte Element der Liste
		{
			int cmp = compare(index,i-1);
			// Ist der index kleiner als der groesste index in der liste?
			if (cmp == -1) return -1;
			// oder gleich
			else if (cmp == 0) return -2;
			// oder groesser
			else return 0;
		} else if ( (0 < i) && (i < idx.size()) ) // Wir sind mitten in der Liste
		{
			int cmpm = compare(index,i-1);
			// index(i-1) = index
			if (cmpm == 0) return -2;
			// index(i-1) > index
			else if (cmpm == -1) return -1;
			int cmp0 = compare(index,i);
			// index(i) = index
			if (cmp0 == 0) return 2;
			// index(i) < index
			else if (cmp0 == 1) return 1;
			// bleibt nur noch index(i-1) < index < index(i)
			return 0;
		}
		else return 0;
	}
	
	/**
	 * Fuegt index in die Menge der Multiindizies ein
	 */
	public void insert(int[] index)
	{
		// Ist die Menge noch leer?
		if (idx.size() == 0) { idx.insertElementAt(index,0); return; }
		// Wir fangen vorne mit dem einfuegen an...
		int inspos = 0;
		// Unsere Schrittweite ist Listenlaenge/2
		int step = idx.size()/2;
		while (true) {
			int cmp = checkAtPos(index,inspos);
			// Vielleicht ist der Index ja schon in der Liste...
			if ((cmp == 2) || (cmp == -2)) return;
			// Vielleicht sind wir aber auch schon an der richtigen Einfuegeposition...
			if (cmp == 0) { idx.insertElementAt(index,inspos); return; }
			// Ansonsten muessen wir woanders weitersuchen...
			if (cmp == -1) inspos -= step;
			else if (cmp == +1) inspos += step;
			// Die Schrittweite wird halbiert...
			step = step/2;
			// Runden ist bloed....
			if (step == 0) step = 1;
		}
	}
	
	public String toString()
	{
		String erg = "{ ";
		for (int i = 0 ; i < idx.size() ; i++)
		{
			erg = erg + "(";
			for (int j = 0; j < ((int[])idx.elementAt(i)).length ; j++)
				erg = erg + ((int[])idx.elementAt(i))[j] + ",";
			erg = erg + "); ";
		}
		return erg + " }";
		
	}
	
	/**
	 * Liefert Index an Stelle i
	 */
	public int[] getIndex(int i)
	{
		// Gibts den Index ueberhaupt??
		if ( (i < 0) || (i >= idx.size()) )
			throw new RuntimeException("Index " + i + " existiert nicht in MultiIndex.getIndex() !");
		return (int[])idx.elementAt(i);
	}
	
	/**
	 * Liefert die Position eines Index in der Liste.
	 * -1, falls der Index nicht in der Liste ist.
	 */
	public int getPos(int[] index)
	{
		// Ist die Menge noch leer?
		if (idx.size() == 0) return -1;
		// Wir fangen vorne mit dem suchen an...
		int seekpos = 0;
		// Unsere Schrittweite ist Listenlaenge/2
		int step = idx.size()/2;
		while (true) {
			int cmp = checkAtPos(index,seekpos);
			// Ist der Index gefunden?
			if (cmp == 2) return seekpos;
			if (cmp == -2) return seekpos-1;
			// Oder sollte er hier stehen, tut es aber nicht, sprich er fehlt in der Liste?
			if (cmp == 0) return -1;
			// Ansonsten muessen wir woanders weitersuchen...
			if (cmp == -1) seekpos -= step;
			else if (cmp == +1) seekpos += step;
			// Die Schrittweite wird halbiert...
			step = step/2;
			// Runden ist bloed....
			if (step == 0) step = 1;
		}
	}
	
	/** Wieviele Indizies gibts eigentlich?
	 */
	public int size()
	{
		return idx.size();
	}
	
	/** Liefert den Teilindex der Elemente aus part
	 */
	public void trim(int[] part)
	{
		Vector tmp = idx;
		idx = new Vector();
		for (int i = 0 ; i < part.length ; i++)
			if (part[i] >= 0 )
				idx.addElement(tmp.elementAt(part[i]));
	}
	
	/**
	 * Berechnet Alle (Multi-)Potenzen (e_1,...,e_n), deren Summe <= d ist,
	 * und fuer die ein a_1 und a_2 existiert, so dass (a_1,a_2,e_3,...,e_n) \in this gilt.
	 */
	public MultiIndex dsupp(int d)
	{
		MultiIndex erg = new MultiIndex();
		// Wir muessen uns jeden Index anschauen...
		for (int i = 0; i < idx.size() ; i++) {
			int[] mdeg = (int[])idx.elementAt(i);
			// Praktischerweise berechnen wir die summe ueber die e_3,...,e_n nur einmal...
			int esum = 0;
			for (int j = 2 ; j < mdeg.length ; j++)
				esum += mdeg[j];
			// Nun muessen wir alle passenden Elemente des 2er-Supports erzeugen
			for (int i1 = 0 ; i1+esum < d ; i1++)
				for (int i2 = 0 ; i1+i2+esum < d; i2++)
				{
					int[] elt = new int[Math.max(2,mdeg.length)];
					elt[0] = i1;
					elt[1] = i2;
					for (int j = 3; j < mdeg.length ; j++)
						elt[j] = mdeg[j];
					erg.insert(elt);
				}
		}
		return erg;
	}

}
