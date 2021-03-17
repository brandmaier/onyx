package arithmetik;

import java.util.*;

public class Tupel implements Comparable
{
	int[] data;

	public final static Comparator lexorder = new Comparator() 
	{
		public int compare(Object o1, Object o2)
		{
			int[] eins = ((Tupel)o1).data, zwei = ((Tupel)o2).data;

			for (int i=0; i<eins.length; i++)
			{
				if (eins[i]>zwei[i]) return 1;
				if (zwei[i]>eins[i]) return -1;
			}
			return 0;
		}
		public boolean equals(Object o1) {return true;}
	};

	public final static Comparator grevlexorder = new Comparator() 
	{
		public int compare(Object o1, Object o2)
		{
			int[] eins = ((Tupel)o1).data, zwei = ((Tupel)o2).data;

			int erg = 0, s1=0, s2=0, e=0, z=0;
			for (int i=eins.length-1; i>=0; i--)
			{
				e = eins[i]; z = zwei[i]; s1 += e; s2 += z;
				if (erg==0)
				{
					if (z>e) erg = 1;
					if (z<e) erg = -1;
				}
			}
			if (s1 > s2) return 1;
			if (s1 < s2) return -1;
			return erg;
		}
		public boolean equals(Object o1) {return true;}
	};
/**
 * Insert the method's description here.
 * Creation date: (09.01.2003 19:28:46)
 * @param data int[]
 */
public Tupel(int[] data) 
{
	this.data = new int[data.length];
	for (int i=0; i<data.length; i++) this.data[i] = data[i];
}
/**
 * Insert the method's description here.
 * Creation date: (09.01.2003 19:28:46)
 * @param data int[]
 */
public Tupel(int[] data, int neudata) 
{
	this.data = new int[data.length+1];
	for (int i=0; i<data.length; i++) this.data[i] = data[i];
	this.data[data.length] = neudata;
}
	Tupel(int row, int col) 
	{
		data = new int[]{row, col};
	}
/**
 * Insert the method's description here.
 * Creation date: (09.10.2003 17:11:58)
 * @return int
 * @param eins arithmetik.Tupel
 * @param zwei arithmetik.Tupel
 */
public int compareTo(Object arg2) 
{
	Tupel zwei = (Tupel)arg2;
	int s = 0;
	for (int i=0; i<this.data.length; i++) s += this.data[i] - zwei.data[i];
	if (s!=0) {
		if (s>0) return 1; else return -1;
	}
	for (int i=this.data.length-1; i>=0; i--)
	{
		if (this.data[i]<zwei.data[i]) return 1;
		if (zwei.data[i]<this.data[i]) return -1;
	}
	/*
	for (int i=0; i<this.data.length; i++)
	{
		if (this.data[i]>zwei.data[i]) return 1;
		if (zwei.data[i]>this.data[i]) return -1;
	}
	*/
	return 0;
}
	public boolean equals(Object z)
	{
		if (!(z instanceof Tupel)) return false;
		int[] zData = ((Tupel)z).data;
		for (int i=0; i<data.length; i++)
		{
			if (i>= zData.length) return true;
			if (data[i] != zData[i]) return false;
		}
		return true;
	}
/**
 * Insert the method's description here.
 * Creation date: (09.01.2003 19:32:48)
 * @return int
 */
public int getColumn() 
{
	return data[1];
}
/**
 * Insert the method's description here.
 * Creation date: (09.01.2003 19:32:25)
 * @return int
 */
public int getRow() 
{
	return data[0];
}
	public int hashCode() 
	{
		if (data.length==0) return 0;
		int v = data[0];
		int multiplier = 16;
		for (int i=1; i<data.length; i++)
		{
			v += data[i]*multiplier;
			multiplier *= multiplier;
		}
		return (new Integer(v)).hashCode();
	}
/**
 * Insert the method's description here.
 * Creation date: (07.02.2003 09:58:57)
 * @return java.lang.String
 */
public String toString() 
{
	String erg = "(";
	for (int i=0; i<data.length-1; i++)
		erg += data[i]+",";
	erg += data[data.length-1]+")";
	return erg;
}
}
