package arithmetik;


import java.util.*;

public class Datenmatrix
{
	private Vector daten;
	public int groesse;
	public int dim;
	public int[] dimgroesse;
	
	public Datenmatrix(int[] dimgroesse)
	{
		this.dimgroesse = new int[dimgroesse.length];
		for (int i=0; i<dimgroesse.length; i++)
			this.dimgroesse[i] = dimgroesse[i];
		dim = this.dimgroesse.length;
		groesse = 1;
		for (int i=0; i<dim; i++)
			groesse *= this.dimgroesse[i];
		daten = new Vector(groesse);
		for (int i=0; i<groesse; i++)
			daten.addElement(null);
	}
	public Object elementBei(int[] pos)
	{
		return daten.elementAt(holeStelle(pos));
	}
	public Object elementBeiStelle(int pos)
	{
		return daten.elementAt(pos);
	}
	public int holeStelle(int[] pos)
	{
		if (pos.length==0) return 0;
		int st = 0;
		int fak = 1;
		for (int i=0; i<dim; i++)
		{
			st += fak*pos[i];
			fak *= dimgroesse[i];
		}
		return st;
	}
	public void setzeElementBei(Object o, int[] pos)
	{
		daten.setElementAt(o,holeStelle(pos));
	}
	public void setzeElementBeiStelle(Object o, int pos)
	{
		daten.setElementAt(o,pos);
	}
}
