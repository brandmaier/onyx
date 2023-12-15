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

public class RingVector extends RingMatrix
{
	public RingVector(int entry[])
	{
		this(Qelement.ZERO, entry.length);
		for (int i=0; i<entry.length; i++)
			this.entry[i][0] = new Qelement(entry[i]);
	}
	public RingVector(Ring ringtype, int length)
	{
		super(ringtype, length, 1);
	}
	public RingVector(RingMatrix m)
	{
		super(m);
		if (m.getColumns()!=1) throw new RuntimeException("Illegal cast from matrix to vector (more than one column).");
	}
	public Ring evalFFT(Ring r)
	{
		int n = getRows();
		Ring field = getValue(1);
		Ring zwei = field.abs_unit().abs_add(field.abs_unit());
		
		if ((!(field instanceof UnitRootComplete)) || (!(field instanceof Field)))
			throw new RuntimeException("interpolation needs field with unit roots.");
		
		Ring halb = ((Field)zwei).abs_reciprocal();
		Ring[] b = new Ring[n+2]; 
		b[n+1] = r.abs_zero(); 
		b[n] = r.abs_zero();
		
		for (int i=n-1; i>=0; i--)
		{
			b[i] = zwei.abs_multiply(r.abs_multiply(b[i+1])).abs_subtract
				(b[i+2]).abs_add(getValue(i+1));
		}
		
		return (b[0].abs_subtract(b[2])).abs_multiply(halb);
	}
	// Multipliziert den Vektor mit der schnellen Fourier-Transformation
	// mit der Fourier-Matrix (e^{2*Pi*j*k/N)}_{j,k}.
	// Wirft RuntimeException "FFT benötigt Zweierpoten", falls 
	// die Zeilenzahl keine Zweierpotenz ist.
	public RingVector FFT()
	{
		int N = this.getRows();
		Ring field = this.getValue(1);
		Ring zwei = field.abs_unit().abs_add(field.abs_unit());
		
		if (!(field instanceof UnitRootComplete)) 
			throw new RuntimeException("FFT benötigt Zahlenraum mit Einheitswurzeln");
		
		if (N == 1) 
		{
			RingVector erg = new RingVector(field, 1);
			erg.setValue(this.getValue(1),1);
			return erg;
		}
		
		if ((N%2)!=0) throw new RuntimeException("FFT benötigt Zweierpoten");
		int n = N/2;
		RingVector xg = new RingVector(field, n);
		RingVector xu = new RingVector(field, n);
		for (int i=0; i<n; i++)
		{
			xg.setValue(getValue(2*i+1),i+1);			// Gerade bzgl. i
			xu.setValue(getValue(2*i+2),i+1);			// Ungerade bzgl. i
		}
		RingVector u = xg.FFT(); RingVector v = xu.FFT();
		for (int i=1; i<=n; i++)
		{
			Field c = (Field)((UnitRootComplete)field).getUnitRoot(N,i-1);
			v.setValue(v.getValue(i).abs_multiply(c),i);
		}
		RingVector erg = new RingVector(field, N);
		for (int i=1; i<=n; i++)
		{
			erg.setValue(u.getValue(i).abs_add(v.getValue(i)), i);
			erg.setValue(u.getValue(i).abs_subtract(v.getValue(i)), i+n);
		}
		return erg;
	}
	// Diese Methode liefert einen Vektor zurück, mit dessen Hilfe durch
	// Aufruf mit evalFFT das Interpolationspolynom ausgewertet werden kann.
	// Interpoliert wird eine Funktion, die an den Tschebycheff-Nullstellen
	// die in *this* gespeicherten Werte annimmt.
	public RingVector getPolynomialRepresentation()
	{
		int n = getRows();
		Ring field = getValue(1);
		Ring zwei = field.abs_unit().abs_add(field.abs_unit());
		
		if ((!(field instanceof UnitRootComplete)) || (!(field instanceof Field)))
			throw new RuntimeException("interpolation needs field with unit roots.");
		
		Ring halb = ((Field)zwei).abs_reciprocal();
		
		RingVector f = new RingVector(field, 2*n);
		for (int i=1; i<=n; i++)
		{
			f.setValue(getValue(i),i);
			f.setValue(getValue(i),2*n+1-i);
		}
		
		((UnitRootComplete)field).getPrimitiveUnitRoot(2*n);	// initialisieren der EW
		RingVector dd = f.FFT();
		
		RingVector d = new RingVector(field, n);
		for (int i=0; i<n; i++)
		{
			Ring ew = (Ring)((UnitRootComplete)field).getUnitRoot(4*n, /*-*/i);
			Field fak = ((Field)Polynomial.multiplyRingWithInt(field.abs_unit(),2*n)).abs_reciprocal();
			fak = (Field)fak.abs_multiply(zwei).abs_multiply(ew);
			d.setValue(dd.getValue(i+1).abs_multiply(fak), i+1);
		}
		
		return d;
	}
	public Ring getValue(int pos)
	{
		return entry[pos-1][0];
	}
	public Ring norm()
	{
		if (!(entry[0][0] instanceof Squarerootable)) 
			throw new RuntimeException("Invalid Ring for norm (need Squarerootable)");
		return (Ring)((Squarerootable)this.scalarProduct(this)).abs_sqrt();
	}
	public Ring scalarProduct(RingVector arg2)
	{
		return (this.transpose().matrixMultiply(arg2).getValue(1,1));
	}
	public void setValue(Ring el, int pos)
	{
		entry[pos-1][0] = el;
	}

/**
 * Insert the method's description here.
 * Creation date: (14.12.2002 10:00:57)
 */
public RingVector() {}

/**
 * Insert the method's description here.
 * Creation date: (14.12.2002 10:00:57)
 */
public RingVector(double[] entry) 
{
	this(new DoubleWrapper(0), entry.length);
	for (int i=0; i<entry.length; i++)
		this.entry[i][0] = new DoubleWrapper(entry[i]);
}

	public RingVector(Qelement entry[])
	{
		this(Qelement.ZERO, entry.length);
		for (int i=0; i<entry.length; i++)
			this.entry[i][0] = new Qelement(entry[i]);
	}

/**
 * Insert the method's description here.
 * Creation date: (14.12.2002 12:11:54)
 * @param entry arithmetik.Ring[]
 */
public RingVector(Ring[] entry) 
{
	this.entry = new Ring[entry.length][1];
	for (int i=0; i<entry.length; i++)
			this.entry[i][0] = (entry[i]).abs_negate().abs_negate();
}

/**
 * Insert the method's description here.
 * Creation date: (07.08.2002 18:26:36)
 * @return int
 */
public int length() 
{
	return getRows();
}

/**
 * Insert the method's description here.
 * Creation date: (31.07.2004 11:23:11)
 * @return arithmetik.RingVector[]
 * @param in arithmetik.RingVector[]
 */
public static RingVector[] orthonormalize(RingVector[] in) 
{
	return orthonormalize(in, in.length);
}

/**
 * Insert the method's description here.
 * Creation date: (31.07.2004 11:23:11)
 * @return arithmetik.RingVector[]
 * @param in arithmetik.RingVector[]

	Orthogonalisiert und normiert die in-Vektoren mit Gram-Schmidt-Orthogonalisierung und hängt weitere orthonormale
	Vektoren an, bis die Dimension oder anz viele Vektoren erreicht sind.
 
 */
public static RingVector[] orthonormalize(RingVector[] in, int anz) 
{
	if (in.length==0) return new RingVector[0];
	int dim = in[0].length();
	if (dim==0) return new RingVector[0];
	Ring[] ezdvf = new Ring[5];
	ezdvf[0] = in[0].getValue(1).abs_unit();
	ezdvf[1] = ezdvf[0].abs_add(ezdvf[0]);
	ezdvf[2] = ezdvf[1].abs_add(ezdvf[0]);
	ezdvf[3] = ezdvf[2].abs_add(ezdvf[0]);
	ezdvf[4] = ezdvf[3].abs_add(ezdvf[0]);	
	int i=0;
	java.util.Random r = new java.util.Random();
	RingVector[] erg = new RingVector[Math.min(dim, anz)];
	while ((i<dim) && (i<anz))
	{
		RingVector work;
		if (i < in.length) work = in[i];
		else {
			Ring[] warr = new Ring[dim];
			for (int j=0; j<dim; j++) warr[j] = ezdvf[r.nextInt(5)];
			work = new RingVector(warr);
		}
		for (int j=0; j<i; j++)
			work = new RingVector(work.subtract(erg[j].scalarMultiply(erg[j].scalarProduct(work))));
		erg[i] = new RingVector(work.scalarMultiply( ((Field)work.norm()).abs_reciprocal() ));
		i++;
	}
	return erg;
}

/**
 * Insert the method's description here.
 * Creation date: (06.09.2003 13:32:35)
 * @return arithmetik.Ring[]
 */
public Ring[] toArray() 
{
	Ring[] erg = new Ring[entry.length];
	for (int i=0; i<erg.length; i++)
		erg[i] = entry[i][0];
	return erg;
}

/**
 * Insert the method's description here.
 * Creation date: (14.12.2002 10:03:24)
 * @return double[]
 */
public double[] toDoubleArray() 
{
	double[] erg = new double[entry.length];
	for (int i=0; i<erg.length; i++)
		erg[i] = ((DoubleCastable)entry[i][0]).doubleValue();
	return erg;
}

/**
 * Insert the method's description here.
 * Creation date: (19.05.2002 11:24:53)
 * @return java.lang.String
 */
public String toString() 
{
	String erg = "(";
	for(int i=0;i<entry.length;i++)
	{
		erg+=entry[i][0].toString();
		if(i<entry.length-1) erg += ",";
	}
	erg+=")";
	return erg;
}
}
