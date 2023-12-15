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
