package arithmetik;

public class Subset
{
	int setsize;
	int subsetsize;
	int[] subset;
	Object[] set;
	
	
	public Subset(Object[] iset)
	{
		set = iset;
		setsize = set.length;
		subsetsize = 1;
		subset = new int[1];
		subset[0] = 0;
	}
	
	/**
	 * Liefert die Objekte in der aktuellen Teilmenge zurueck
	 */
	public Object[] get()
	{
		Object[] erg = new Object[subsetsize];
		for (int i = 0 ; i < subsetsize ; i++)
			erg[i] = set[subset[i]];
		return erg;
	}
	
	/**
	 * Liefert die Objekte, die nicht in der aktuellen Teilmenge sind.
	 */
	public Object[] getOthers()
	{
		Object[] erg = new Object[setsize-subsetsize];
		int i=0;
		int j=0;
		while (j < subsetsize)
		{
			if (i==subset[j]) j++;
			else erg[i-j] = set[i];
			i++;
		}
		for (int k=i; k < setsize ; k++)
			erg[k-j] = set[k];
		return erg;
	}

	/**
	 * Prueft, ob alle Teilmengen durchprobiert wurden
	 */
	public boolean lastSubset()
	{
		return (setsize == subsetsize);
	}
	
	/**
	 * generiert die naechste Teilmenge
	 */
	public void next()
	{
		if  ( !lastSubset() ) {
			
			if (subset[0] == setsize-subsetsize) 
			{ // Wir haben alle Teilmengen mit subsetsize Elt. durchprobiert
				subsetsize++;
				subset = new int[subsetsize];
				for (int i = 0; i < subsetsize; i++)
					subset[i] = i;
			} else {
				int i;
				for (i = 0 ; subset[subsetsize-1-i] == setsize-1-i ; i++);
				subset[subsetsize-1-i]++;
				for (int j = 1 ; j <= i ; j++)
					subset[subsetsize-1-i+j] = subset[subsetsize-1-i]+j;
			}
		}
	}
	
	/**
	 * Entfernt die momentane Teilmenge aus der Objektmenge
	 */
	public void remove()
	{
		Object[] newset = new Object[setsize-subsetsize];
		int i=0;
		int j=0;
		while ( j < subsetsize)
		{
			if (subset[j]==i) j++;
			else newset[i-j]=set[i];
			i++;
		}
		for (int k = i; k < setsize; k++)
			newset[k-j]=set[k];
		set=newset;
		setsize=setsize-subsetsize;
		for (int k = 0; k < subsetsize; k++)
			subset[k]=k;
	}
	
	/**
	 * Testet, ob die Haelfte aller Teilmengen (alle der Kardinalitaet <= setsize/2) schon
	 * durchprobiert wurden
	 */
	public boolean halfSubset()
	{
		return (2*subsetsize > setsize);
	}
	
	public String toString()
	{
		String erg = "[";
		for (int i = 0 ; i < subsetsize-1 ; i++)
			erg = erg + subset[i] + " ,";
		erg = erg + subset[subsetsize-1];
		erg = erg + "] of " + (setsize-1);
		return erg;
	}
}
