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
import java.math.*;

public class Graph
{
	int[][] edge;
	
	public Graph() {edge = new int[0][0];}
	public Graph(int[][] edge)
	{
		this.edge = new int[edge.length][edge.length];
		for (int i=0; i<edge.length; i++)
			for (int j=0; j<edge.length; j++) this.edge[i][j]=edge[i][j];
	}
	public Graph(int size) 
	{
		edge = new int[size][size];
		for (int i=0; i<size; i++)
			for (int j=0; j<size; j++) edge[i][j]=0;
	}
	public Graph(Graph copy)
	{
		edge = new int[0][0];
		for (int i=0; i<edge.length; i++)
			for (int j=0; j<edge.length; j++) edge[i][j]=0;
	}
	public Graph addEdge(int source, int target, int edgeweight)
	{
		int max = Math.max(source,target);
		Graph erg = new Graph(Math.max(max,edge.length));
		for (int i=0; i<erg.edge.length; i++)
			for (int j=0; j<erg.edge.length; j++)
				if ((i>=edge.length) || (j>=edge.length)) erg.edge[i][j]=0;
				else erg.edge[i][j] = edge[i][j];
		erg.edge[source][target] = edgeweight;
		return erg;
	}
	public Qelement[][] getConvergence(int[] startvektor)
	{
		Qelement[] mystart = new Qelement[startvektor.length];
		for (int i=0; i<mystart.length; i++)
			mystart[i] = new Qelement(new Qelement(startvektor[i]));
		return getConvergence(mystart);
	}
	public Qelement[][] getConvergence(Qelement[] startvektor)
	{
		int n = edge.length;
		int d = getPeriodicity();
		int sigma = 0;
		for (int i=0; i<n; i++)
			sigma += edge[0][i];
		RingMatrix a = new RingMatrix(new Qelement(), n ,n);
		for (int i=0; i<n; i++)
			for (int j=0; j<n; j++)
				a.entry[j][i] = new Qelement(edge[i][j],sigma);			// wird transponiert.
		RingMatrix aorg = new RingMatrix(a);
		a = a.pow(d);

		Graph neu = new Graph(n);
		for (int i=0; i<n; i++)
			for (int j=0; j<n; j++)
			{
				if (((Qelement)a.entry[i][j]).isZero()) neu.edge[j][i] = 0;			// wird transponiert.
				else neu.edge[j][i] = 1;
			}
		int[] scc = neu.getStronglyConnectedComponents();
		int anzscc = 1;
		for (int i=0; i<scc.length; i++) anzscc = Math.max(anzscc, scc[i]+1);
		boolean[] isLeaf = new boolean[anzscc];
		for (int i=0; i<anzscc; i++) isLeaf[i] = true;
		for (int i=0; i<n; i++)
		{
			for (int j=0; j<n; j++)
				if ((neu.edge[i][j]!=0) && (scc[i]!=scc[j])) isLeaf[scc[i]] = false;
		}
		
		Qelement[][] qgraph = new Qelement[n][n];
		for (int i=0; i<n; i++)
			for (int j=0; j<n; j++)
				qgraph[i][j] = new Qelement((Qelement)a.entry[j][i]);
		
		for (int i=0; i<n; i++)
			if (!isLeaf[scc[i]])
			{
				// Selfloops weg...
				Qelement fak = Qelement.ONE.subtract(qgraph[i][i]).reciprocal();
				if (!fak.isEqual(Qelement.ONE))
					for (int j=0; j<n; j++)
						qgraph[i][j] = qgraph[i][j].multiply(fak);
				qgraph[i][i] = new Qelement();
				
				for (int k=0; k<n; k++)
				{
					for (int j=0; j<n; j++)
						qgraph[k][j] = qgraph[k][j].add(qgraph[k][i].multiply(qgraph[i][j])); 
					qgraph[k][i] = new Qelement();
				}
			}
		
		// Preprocessing zu ende, jetzt Basisvektoren des Kerns bestimmen.
		for (int i=0; i<n; i++)
			for (int j=0; j<n; j++)
			{
				Qelement b = (Qelement)a.entry[i][j];
				if (i==j) b = b.subtract(new Qelement(new Qelement(1)));
				a.entry[i][j] = b;
			}
		RingMatrix r = a.findRMatrix();
		RingVector b[] = r.coreBasis();
		for (int i=0; i<b.length; i++)
			b[i] = new RingVector(b[i].scalarMultiply(((Qelement)b[i].infinityNorm()).reciprocal()));
		RingVector start = new RingVector(startvektor[0], startvektor.length);
		for (int i=1; i<=startvektor.length; i++)
			start.setValue(startvektor[i-1], i);
		start = new RingVector(start.scalarMultiply(((Qelement)start.infinityNorm()).reciprocal()));
		Qelement[][] erg = new Qelement[d][n];
		for (int i=0; i<d; i++)
			for (int j=0; j<n; j++)
				erg[i][j] = new Qelement();
		
		int[] sccZuBasis = new int[anzscc];
		for (int j=0; j<b.length; j++)
		{
			int k=0; 
			while (b[j].getValue(k+1).abs_isEqual(Qelement.ZERO)) k++;
			sccZuBasis[scc[k]] = j;
		}
		RingMatrix knotenZuBasis = new RingMatrix(Qelement.ONE, b.length, n);
		for (int i=0; i<n; i++)
			for (int j=0; j<n; j++)
			{
				int ze = sccZuBasis[scc[j]];
				knotenZuBasis.setValue(knotenZuBasis.getValue(ze+1,i+1).abs_add(qgraph[i][j]), ze+1, i+1);
			}				
		
		for (int i=0; i<d; i++)					// Für jeden Startvektor...
		{
			RingVector bew = new RingVector(knotenZuBasis.matrixMultiply(start));
			for (int k=0; k<b.length; k++)		// Für jeden Basisvektor...
			{
				Qelement fak = (Qelement)bew.getValue(k+1);
				for (int j=0; j<n; j++)
					erg[i][j] = erg[i][j].add(((Qelement)b[k].getValue(j+1)).multiply(fak));
			}
			start = new RingVector(aorg.matrixMultiply(start));			
		}
		return erg;
	}
	public Qelement[] getConvergenceAt(int[] startvektor, int[] endvektor)
	{
		Qelement[] mystart = new Qelement[startvektor.length];
		for (int i=0; i<mystart.length; i++)
			mystart[i] = new Qelement(new Qelement(startvektor[i]));
		Qelement[] myend = new Qelement[endvektor.length];
		for (int i=0; i<mystart.length; i++)
			myend[i] = new Qelement(new Qelement(endvektor[i]));
		return getConvergenceAt(mystart, myend);
	}
	public Qelement[] getConvergenceAt(Qelement[] startvektor, Qelement[] endvektor)
	{
		Qelement[][] zw = this.getConvergence(startvektor);
		Qelement[] erg = new Qelement[zw.length];
		for (int i=0; i<zw.length; i++)
		{
			erg[i] = new Qelement();
			for (int j=0; j<edge.length; j++)
				if (!endvektor[j].isZero()) erg[i] = erg[i].add(zw[i][j]);
		}
		return erg;
	}
	public Qelement getConvergenceIfConverges(int[] startvektor, int[] endvektor)
	{
		Qelement[] mystart = new Qelement[startvektor.length];
		for (int i=0; i<mystart.length; i++)
			mystart[i] = new Qelement(new Qelement(startvektor[i]));
		Qelement[] myend = new Qelement[endvektor.length];
		for (int i=0; i<mystart.length; i++)
			myend[i] = new Qelement(new Qelement(endvektor[i]));
		return getConvergenceIfConverges(mystart, myend);
	}
	public Qelement getConvergenceIfConverges(Qelement[] startvektor, Qelement[] endvektor)
	{
		Qelement[] zw = this.getConvergenceAt(startvektor, endvektor);
		Qelement erg = zw[0];
		for (int i=0; i<zw.length; i++)
			if (!erg.equals(zw[i])) return new Qelement(new Qelement(-1));
		return erg;
	}
	public int getPeriodicity()
	{
		int n = edge.length;
		BigInteger kgv = new BigInteger("1");
		for (int i=0; i<n; i++)
		{
			BigInteger ggt = new BigInteger("0");
			Hashtable h = new Hashtable();
			h.put(new Integer(i), new Boolean(true));
			for (int t=1; t<=n; t++)
			{
				Hashtable ne = new Hashtable();
				Enumeration enumr = h.keys();
				while (enumr.hasMoreElements())
				{
					int k = ((Integer)enumr.nextElement()).intValue();
					for (int z=0; z<n; z++)
						if (edge[k][z]!=0) ne.put(new Integer(z),new Boolean(true));
				}
				if (ne.containsKey(new Integer(i)))
				{
					if (ggt.equals(new BigInteger("0"))) ggt = new BigInteger(""+t); 
					else ggt = (new BigInteger(""+t)).gcd(ggt);
				}
				h = ne;
			}
			if (!ggt.equals(new BigInteger("0"))) kgv = kgv.multiply(ggt.divide(kgv.gcd(ggt)));
		}
		return kgv.intValue();
	}
	// returns an array with length n, for each vertex the number of the component 
	// it is in.
	public int[] getStronglyConnectedComponents()
	{
		int n = edge.length;
		int[] d = new int[n];
		int[] f = new int[n];
		for (int i=0; i<n; i++) {d[i]=-1; f[i]=-1;}
		Vector stack = new Vector();
		
		int t = 0;
		for (int i=0; i<n; i++)
			if (d[i]==-1)
			{
				d[i]=t++;
				stack.addElement(new Integer(i));
				while (stack.size()!=0)
				{
					int j = ((Integer)stack.lastElement()).intValue();
					int k = 0;
					while ((k<n) && ((edge[k][j]==0) || (d[k]!=-1))) k++;
					if (k==n) 
					{
						stack.removeElementAt(stack.size()-1); 
						f[j]=t++;
					} else {
						d[k]=t++;
						stack.addElement(new Integer(k));
					}
				}
			}
		
		int[] scc = new int[n];
		for (int i=0; i<n; i++) {d[i]=-1; scc[i]=-1;}
		stack = new Vector();
		
		int scccount = 0;
		t = 0;
		int	maxVal = -1;
		int maxVer = -1;
		for (int k=0; k<n; k++) 
			if (f[k]>maxVal)
			{
				maxVal = f[k];
				maxVer = k;
			}
		int i = maxVer;
		
		while (i != -1)
		{
			scc[i] = scccount++;
			d[i]=t++;
			stack.addElement(new Integer(i));
			while (stack.size()!=0)
			{
				int j = ((Integer)stack.lastElement()).intValue();
				int k = 0;
				maxVal = -1;
				maxVer = -1;
				for (k=0; k<n; k++) 
					if ((edge[j][k]!=0) && (d[k]==-1) && (f[k]>maxVal))
					{
						maxVal = f[k];
						maxVer = k;
					}
				if (maxVer==-1) 
				{
					stack.removeElementAt(stack.size()-1); 
					t++;
				} else {
					d[maxVer]=t++;
					scc[maxVer] = scc[j];
					stack.addElement(new Integer(maxVer));
				}
			}
			maxVal = -1;
			i = -1;
			for (int k=0; k<n; k++) 
				if ((d[k]==-1) && (f[k]>maxVal))
				{
					maxVal = f[k];
					i = k;
				}
		}		
		return scc;
	}
	public boolean reachesInSteps(int sou, int tar, int steps)
	{
		if (steps==0) return (sou==tar);
		boolean erg = false;
		for (int i=0; (i<edge.length) && (!erg); i++)
			if (edge[sou][i]!=0) erg |= reachesInSteps(i,tar,steps-1);
		return erg;
	}
}
