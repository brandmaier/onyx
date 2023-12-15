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

public class SparseMatrix extends RingMatrix
{
	Ring zero;
	Hashtable entryT;
	int columns;
	int rows;
	
/*
public RingMatrix transposeWrongAxis()
{
	int s = getRows(), z = getColumns();
	RingMatrix erg = new RingMatrix(entry[0][0],z,s);
	for(int i=0;i<s;i++)
		for(int k=0;k<z;k++)
			erg.entry[z-k-1][s-i-1]=entry[i][k];
	return erg;
}
// returns the squareroot of the sum of the squares of all entrys. Returns zeros if the entrys
// are not squarerootable
public Ring twoNorm()
{
	Ring erg = entry[0][0].abs_zero();
	if (!(erg instanceof Squarerootable)) return erg;
	
	for(int i=0; i<entry.length; i++)
		for(int j=0; j<entry[0].length; j++)
			erg = erg.abs_add( entry[i][j].abs_multiply(entry[i][j]) );
	
	return (Ring)((Squarerootable)erg).abs_sqrt();	
}
public RingMatrix unit()
{
	RingMatrix erg = new RingMatrix(entry[0][0],entry.length,entry[0].length);
	for(int i=0;i<entry[0].length;i++)
	{
		erg.entry[i][i]=entry[0][0].abs_unit();
	}
	return erg;
}
public RingMatrix zero()
{
	return new RingMatrix(entry[0][0],entry.length,entry[0].length);
}
*/
public SparseMatrix(int[][] entry)
{
	this(Qelement.ZERO, entry.length, entry[0].length);
	for (int i=0; i<entry.length; i++)
		for (int j=0; j<entry[i].length; j++)
			if (entry[i][j] != 0) setValue(new Qelement(entry[i][j]),i+1,j+1);
}
public SparseMatrix(Ring[][] entry)
{
	this(entry[0][0], entry.length, entry[0].length);
	for (int i=0; i<entry.length; i++)
		for (int j=0; j<entry[i].length; j++)
			if (!entry[i][j].abs_isEqual(zero)) setValue(entry[i][j],i+1,j+1);
}
public SparseMatrix(Ring ringtype, int dim) {this(ringtype,dim,dim);}
public SparseMatrix(Ring ringtype, int rows, int columns)
{
	zero = ringtype.abs_zero();
	entryT = new Hashtable();
	this.columns = columns;
	this.rows = rows;
}
public SparseMatrix(RingMatrix toConvert) 
{
	this(toConvert.entry[0][0], toConvert.entry.length, toConvert.entry[0].length);
	for (int i=0; i<toConvert.entry.length; i++)
		for (int j=0; j<toConvert.entry[i].length; j++)
			if (!toConvert.entry[i][j].abs_isEqual(zero)) setValue(toConvert.entry[i][j],i+1,j+1);
}
public SparseMatrix(SparseMatrix toCopy) 
{
	this(toCopy.zero, toCopy.rows, toCopy.columns);
	
	Enumeration enumr = toCopy.entryT.keys();
	while (enumr.hasMoreElements())
	{
		Tupel c = (Tupel)enumr.nextElement();
		entryT.put(c, toCopy.entryT.get(c));
	}
}
/*
public Ring abs_add (Ring b)
{
	return add((RingMatrix) b);
}
public boolean abs_isEqual (Ring b)
{
	for(int i=0;i<entry.length;i++)
		for(int j=0;j<entry[0].length;j++)
			if(!entry[i][j].abs_isEqual(((RingMatrix)b).entry[i][j]))
				return false;
	return true;
	
}
public Ring abs_multiply(Ring A) 
{
	return matrixMultiply((RingMatrix)A);
}
public Ring abs_negate()
{
	return negate();
}
public Ring abs_pow(long exp)
{
	Ring erg=null;
	erg = pow(exp);
	return erg;
}
public Ring abs_subtract (Ring b)
{
	return subtract((RingMatrix) b);
}
public Ring abs_unit ()
{	
	return unit();
}
public Ring abs_zero ()
{
	return zero();
}
public RingMatrix add(RingMatrix B)
{
	int zeilen = entry.length;
	int spalten = entry[0].length;

	RingMatrix erg = new RingMatrix(entry[0][0],zeilen,spalten);
	
	for(int i=0;i<zeilen;i++)
		for(int j=0;j<spalten;j++)
		{
			erg.entry[i][j]=entry[i][j].abs_add(B.entry[i][j]);
		}
	return erg;		
}
// Gibt eine (nicht orthogonale oder normale) Basis des Kerns der Matrix zurück.
public RingVector[] coreBase()
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);

	if (!(field instanceof Field))
		throw new RuntimeException("Not valid Ring for coreProjectMatrix (needs Field and Squarerootable)");

	Vector erg = new Vector();
	RingVector b = new RingVector(field, spalten);
	for (int i=1; i<=spalten; i++)
		b.setValue(field.abs_zero(), i);
	RingVector c = this.solveRightUpperTriangleMatrix(b, 0);
	int m = 1;
	while (!c.abs_isEqual(b)) 
	{
		erg.addElement(new RingVector(c));
		c = this.solveRightUpperTriangleMatrix(b, m++);
	}
	RingVector[] ergarr = new RingVector[erg.size()];
	for (int i=0; i<ergarr.length; i++)
		ergarr[i] = (RingVector)erg.elementAt(i);
	
	return ergarr;	
}
// Gibt von einem homogenen Gleichungssystem eine Matrix zurück, die alle Vektoren auf den
// Kern der Matrix projeziert.
public RingMatrix coreProjectMatrix()
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);

	if ((!(field instanceof Field)) && (!(field instanceof Squarerootable)))
		throw new RuntimeException("Not valid Ring for coreProjectMatrix (needs Field and Squarerootable)");

	RingMatrix erg = (new RingMatrix(field, spalten, spalten)).unit();
	boolean[] ersetzt = new boolean[spalten];
	for (int i=0; i<spalten; i++)
		ersetzt[i] = false;
	RingVector b = new RingVector(field, spalten);
	for (int i=1; i<=spalten; i++)
		b.setValue(field.abs_zero(), i);
	RingVector c = this.solveRightUpperTriangleMatrix(b, 0);
	int m = 0;
	while (!c.abs_isEqual(b)) 
	{
		m++;
		int k=0; while ((ersetzt[k]) || (c.getValue(k+1).abs_isEqual(field.abs_zero()))) k++;
		ersetzt[k] = true;
		for (int i=1; i<=spalten; i++)
			erg.setValue(c.getValue(i), i, k+1);
		c = this.solveRightUpperTriangleMatrix(b, m++);
	}
	
	RingMatrix A = new RingMatrix(field, spalten, spalten);
	for (int i=1; i<=spalten; i++)
		if (ersetzt[i-1]) A.setValue(field.abs_unit(), i, i);
	
	erg = erg.matrixMultiply(A).matrixMultiply(erg.invert());
	
	return erg;
}
public RingMatrix delRowCol(int rowToDelete, int colToDelete)
{
	int newRows = entry.length-1;
	int newCols = entry[0].length-1;
	RingMatrix erg = new RingMatrix(entry[0][0], newRows, newCols);
	
	for(int i=0;i<rowToDelete;i++)
	{
		for(int k=0;k<colToDelete;k++)
		{
			erg.entry[i][k]=entry[i][k];
		}
		for(int k=colToDelete+1;k<newCols+1;k++)
		{
			erg.entry[i][k-1]=entry[i][k];
		}
	}
	for(int i=rowToDelete+1;i<newRows+1;i++)
	{
		for(int k=0;k<colToDelete;k++)
		{
			erg.entry[i-1][k]=entry[i][k];
		}
		for(int k=colToDelete+1;k<newCols+1;k++)
		{
			erg.entry[i-1][k-1]=entry[i][k];
		}
	}
	return erg;
}
public Ring developDeterminant()
{
	if(entry.length==1)
		return entry[0][0];
	Ring erg = entry[0][0].abs_zero();
	for(int i=0;i<entry.length;i++)
	{
		if(i%2 == 0)
			erg=erg.abs_add(entry[i][0].abs_multiply(delRowCol(i,0).developDeterminant()));
		else
			erg=erg.abs_subtract(entry[i][0].abs_multiply(delRowCol(i,0).developDeterminant()));
	}
	return erg;
}
// approximiert die Eigenwerte bis epsilon. Rückgabe ist Vector mit den Eigenwerten.
public RingVector eigenvalues(double epsilon)
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();

	RingMatrix A = this.transformSimilarToHessenberg();
	
	boolean weiter = true;
	while (weiter)
	{
		RingMatrix[] qr = A.qRDecompositionOfTridiagonal();
		A = qr[1].matrixMultiply(qr[0]);
		weiter = false;
		for (int i=1; i<=zeilen-1; i++)
			if ( ((DoubleCastable)A.getValue(i+1,i)).doubleValue() > epsilon) weiter = true;
	}
	RingVector erg = new RingVector(A.getValue(1,1),zeilen);
	for (int i=1; i<=zeilen; i++)
		erg.setValue(A.getValue(i,i),i);
	return erg;
}
// eliminiert n-te spalte
public RingMatrix eliminateColum(int n)
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);

	RingMatrix erg = new RingMatrix(field, zeilen, spalten-1);
	for (int i=1; i<=zeilen; i++)
		for (int j=1; j<=spalten; j++)
		{
			if (j<n) erg.setValue(getValue(i,j),i,j);
			if (j>n) erg.setValue(getValue(i,j),i,j-1);
		}
	return erg;
}
public RingMatrix findRMatrix()
{
	return (findRMatrix(null))[0];
}
public RingMatrix[] findRMatrix(RingMatrix snd)
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);
	
	if (!(field instanceof Field)) 
		throw new RuntimeException("Not valid Ring for Gauss (needs Field)");
	
	RingMatrix R = new RingMatrix(this);
	RingMatrix I;
	if (snd != null) I = new RingMatrix(snd);
	else I = null;
	
	Ring t;

	for (int i=1; i<spalten; i++)
	{
		int pz = i;
		while ((pz<=spalten) && (R.getValue(pz,i).abs_isEqual(field.abs_zero()))) pz++;
		if (pz <= spalten)
		{
			if (pz != i)
			{
				for (int j=1; j<=spalten; j++)
				{
					t = R.getValue(pz, j);
					R.setValue(R.getValue(i,j),pz,j);
					R.setValue(t,i,j);
					if (I != null)
					{
						t = I.getValue(pz, j);
						I.setValue(I.getValue(i,j),pz,j);
						I.setValue(t,i,j);
					}
				}
			}
			for (int j=i+1; j<=zeilen; j++)
			{
				Ring fak = ((Field)R.getValue(j,i)).abs_divide((Field)R.getValue(i,i));
				for (int k=i; k<=spalten; k++)
				{
					Field hier = (Field)fak.abs_multiply(R.getValue(i,k));
					R.setValue(R.getValue(j,k).abs_subtract(hier),j,k);
				}
				if (I != null)
				{
					for (int k=1; k<=spalten; k++)
					{
						Field hier = (Field)fak.abs_multiply(I.getValue(i,k));
						I.setValue(I.getValue(j,k).abs_subtract(hier),j,k);
					}
				}
			}			
		}
	}
	RingMatrix[] erg = {R,I};
	return erg;
}
*/
public int getColumns()
{
	return columns;
}
public int getRows()
{
	return rows;
}
public Ring getValue(int row, int col)
{
	Tupel t = new Tupel(row,col);
	Object o = entryT.get(t);
	if (o == null) return zero; else return (Ring)o;
}
/*
// returns the sum of the absolute value of all matrix entries, or zero if the entrys are
// not signed.
public Ring infinityNorm()
{
	Ring erg = entry[0][0].abs_zero();
	if (!(erg instanceof Signed)) 
		throw new RuntimeException ("Not valid Ring for infinityNorm (needs Signed)");
	
	for(int i=0; i<entry.length; i++)
		for(int j=0; j<entry[0].length; j++)
			erg = erg.abs_add( ((Signed)entry[i][j]).abs_abs() );
	
	return erg;	
}
public long intlog(long k)
{	
	long erg=1;
	long log=0;
	while(2*erg<k)
	{
		erg*=2;
		log++;
	}
	return log;
}
public long intpow(long base, long k)
{	
	long erg=1;
	for(long i=0; i<k;i++)
		erg*=base;
	return erg;
}
public RingMatrix invert()
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);
	
	if (!(field instanceof Field)) 
		throw new RuntimeException("Not valid Ring for invert (needs Field)");
	
	RingMatrix[] z = this.findRMatrix(this.unit());
	z = (z[0].transposeWrongAxis().transpose()).findRMatrix(z[1].transposeWrongAxis().transpose());
	z[0] = z[0].transposeWrongAxis().transpose();
	z[1] = z[1].transposeWrongAxis().transpose();
	for (int i=1; i<=zeilen; i++)
	{
		Field val = ((Field)z[0].getValue(i,i)).abs_reciprocal();
		for (int j=1; j<=spalten; j++)
			z[1].setValue(z[1].getValue(i,j).abs_multiply(val), i,j);
	}
	return z[1];
}
// TVO: Erweitert auf Multiplikation von 2 Matrizen verschiedener Größen.
// Forderung: Zahl der Spalten von this = Zahl der Zeilen von A.
public RingMatrix matrixMultiply(RingMatrix A) 
{
	int zeilen = getRows();
	int lauflaenge = getColumns();
	int spalten = A.getColumns();
	
	if (A.getRows() != lauflaenge) 
		throw new RuntimeException ("Matrices for multiplication: 1st arguments columns must be same than 2nd arguments rows.");

	RingMatrix erg = new RingMatrix(entry[0][0],zeilen,spalten);
	
	for(int i=0;i<zeilen;i++)
		for(int j=0;j<spalten;j++)
		{
			Ring sum = entry[0][0].abs_zero();
			for(int k=0;k<lauflaenge;k++)
			{
				sum = sum.abs_add(entry[i][k].abs_multiply(A.entry[k][j]));
			}
			erg.entry[i][j]=sum;
		}
	return erg;	
}
public RingMatrix negate()
{	
	int zeilen = entry.length;
	int spalten = entry[0].length;

	RingMatrix erg = new RingMatrix(entry[0][0],zeilen,spalten);
	
	for(int i=0;i<zeilen;i++)
		for(int j=0;j<spalten;j++)
		{
			erg.entry[i][j]=entry[i][j].abs_negate();
		}
	return erg;			
}
public RingMatrix pow(long exp)
{
	if(exp==0)
		return unit();
	
	if(entry.length!=entry[0].length)
		throw new RuntimeException("Matrix RowColMismatch");
	
	long n = intlog(exp);
	
	RingMatrix erg = new RingMatrix(this);
	
	for(int i=0;i<n;i++)
	{
		erg=erg.matrixMultiply(erg);
	}
	
	erg=erg.matrixMultiply(this.pow(exp-intpow(2,n)));
	

	return erg;			
}
// gibt eine obere Dreiecksmatrix R zurück, und eine Matrix U, deren Spaltenvektoren
// u_i jeweils für eine Matrix Q_I = u_i transpose(u_i) stehen, deren Produkt
// Q wiederum eine orthogonale Matrix ist, so dass QR = this.
public RingMatrix[] qRDecomposition()
{	
	int j,k;
	
	Ring field = entry[0][0];
	
	if (!((field instanceof Field) && (field instanceof Squarerootable) && (field instanceof Signed))) 
		throw new RuntimeException("Not valid Ring for QRPartition (needs field, squarrootable)");
	
	final Ring zwei = field.abs_unit().abs_add(field.abs_unit());
	int n = this.getRows();
	if (n != this.getColumns()) 
		throw new RuntimeException("QRDecomposition must be called with a quadratic Matrix.");
	
	RingMatrix R = new RingMatrix(this);
	RingMatrix U = new RingMatrix(field, n, n);
	
	for (int i=1; i<=n; i++)
	{
		Ring lambda = ((Signed)R.getValue(i,i)).abs_ringSignum();
		lambda = lambda.abs_negate();
		System.out.println("lambda_"+i+": "+lambda);
		Ring sum = field.abs_zero();
		for (k=i; k<=n; k++)
			sum = sum.abs_add(R.getValue(k,i).abs_multiply(R.getValue(k,i)));
		System.out.println("mu_"+i+"': "+sum);
		Ring mu = (Ring)((Squarerootable)sum).abs_sqrt();
		System.out.println("mu_"+i+": "+mu);
		if (mu.abs_isEqual(mu.abs_zero())) 
		{
			mu = mu.abs_unit(); 
			for (k=i+1; k<=n; k++)
				U.setValue(field.abs_zero(), k, i);
		} else {
			Ring sigma = zwei.abs_multiply(mu).abs_multiply
				(mu.abs_add( ((Signed)R.getValue(i,i)).abs_abs()) );
			sigma = (Ring)((Squarerootable)sigma).abs_sqrt();
			System.out.println("sigma_"+i+": "+sigma);
			U.setValue( ((Field)R.getValue(i,i).abs_subtract(lambda.abs_multiply(mu))).abs_divide((Field)sigma) ,i,i);
			for (k=i+1; k<=n; k++)
				U.setValue( ((Field)getValue(k,i)).abs_divide((Field)sigma), k,i);
		}
		R.setValue(lambda.abs_multiply(mu), i,i);
		for (k=i+1; k<=n; k++)
			R.setValue(field.abs_zero(),k,i);
		
		for (j=i+1; j<=n; j++)
		{
			Ring beta = field.abs_zero();
			for (k=i; k<=n; k++)
				beta = beta.abs_add(R.getValue(k,j).abs_multiply(U.getValue(k,i)));
//			System.out.println("beta_"+i+j+": "+beta);
			for (k=i; k<=n; k++)
				R.setValue(R.getValue(k,j).abs_subtract(zwei.abs_multiply(beta).abs_multiply(U.getValue(k,i))), k,j);
		}
	}
//	U.setValue(field.abs_unit(),n,n);
	RingMatrix[] erg = {R,U};
	return erg;
}
// QR-Zerlegung einer Matrix in Tridiagonalgestalt durch Givens-Rotationen
public RingMatrix[] qRDecompositionOfTridiagonal()
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);
	Ring zwei = field.abs_unit().abs_add(field.abs_unit());
	
	if (!(field instanceof Squarerootable)) return null;
	
	RingMatrix Qtrans = this.unit();
	RingMatrix R = new RingMatrix(this);
	for (int i=1; i<=zeilen-1; i++)
	{
		Field abs = (Field)((Squarerootable)((R.getValue(i,i).abs_multiply(R.getValue(i,i))).abs_add
					                  (R.getValue(i+1,i).abs_multiply(R.getValue(i+1,i))) )).abs_sqrt();
		Field c = ((Field)R.getValue(i,i)).abs_divide(abs);
		Field s = ((Field)R.getValue(i+1,i)).abs_divide(abs);
		
		Field e = ((Field)R.getValue(i,i+1).abs_add(field.abs_zero()));
		R.setValue(abs,i,i); 
		R.setValue(c.abs_multiply(e).abs_add
				  (s.abs_multiply(R.getValue(i+1,i+1))),i,i+1);
		if (i<zeilen-1) R.setValue(s.abs_multiply(R.getValue(i+1,i+2)),i,i+2);
		R.setValue(field.abs_zero(),i+1,i);
		R.setValue(c.abs_multiply(R.getValue(i+1,i+1)).abs_subtract
				  (s.abs_multiply(e)),i+1,i+1);
		if (i<zeilen-1) R.setValue(c.abs_multiply(R.getValue(i+1,i+2)),i+1,i+2);
		
		RingMatrix base = new RingMatrix(Qtrans);
		for(int z=i;z<=i+1;z++)
			for(int j=1;j<=spalten;j++)
			{
				Field val1 = c; Field val2 = s;
				if (z==i+1) {val1 = (Field)s.abs_negate(); val2 = c;}
				Field sum = (Field)base.getValue(i,j).abs_multiply(val1).abs_add(base.getValue(i+1,j).abs_multiply(val2));
				Qtrans.setValue(sum,z,j);
			}
	}
	RingMatrix[] erg = new RingMatrix[2];
	erg[0]=Qtrans.transpose(); erg[1] = R;
	return erg;
}
public RingMatrix revealQFromQRDecomposition()
{
	final Ring eins = this.getValue(1,1).abs_unit();
	final Ring zwei = eins.abs_add(eins);

	int n = this.getRows();
	RingMatrix Q = new RingMatrix(this.getValue(1,1),n,n);
	Q = Q.unit();
	for (int i=1; i<=n; i++)
	{
		RingVector u = new RingVector(this.subMatrix(0,n-1,i-1,i-1));
//		System.out.println(i+"te Householder: "+
//			Q.unit().subtract(u.matrixMultiply(u.transpose()).scalarMultiply(zwei)).toDoubleString() );
		Q = Q.matrixMultiply (Q.unit().subtract(
			u.matrixMultiply(u.transpose()).scalarMultiply(zwei) ));
	}
	return Q;
}
public RingMatrix scalarMultiply(Ring lambda)
{
	int zeilen = entry.length;
	int spalten = entry[0].length;

	RingMatrix erg = new RingMatrix(entry[0][0],zeilen,spalten);
	
	for(int i=0;i<zeilen;i++)
		for(int j=0;j<spalten;j++)
		{
			erg.entry[i][j]=lambda.abs_multiply(entry[i][j]);
		}
	return erg;			
}
*/
public void setValue(Ring el, int row, int col)
{
	if (el.abs_isEqual(zero)) return;
	Tupel t = new Tupel(row, col);
	entryT.put(t, el);
}
// Solves the equation system with the cg-method. If print is not zero, progress is 
// reported to it. If epsilon is not zero, b must contain DoubleCastables, and the progress
// is terminated if x changes less than epsilon in infinitynorm. if preconditioning is true,
// the cg-method will be done with the diagonal of *this* as preconditioning (not yet implemented). 
// Equation system will be solved multiplied with A^{T}, i.e. A must not necessarily be symetric
// positive definit, but must be regular
public RingVector solveWithCGMethod(RingVector b, double epsilon, boolean preconditioning, Printable print)
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	
	if ((epsilon>0.0) && (!(zero instanceof DoubleCastable))) 
		throw new RuntimeException("CG-Methode mustn't have epsiolon != 0 if Ring is not DoubleCastable.");
	
	if (!(zero instanceof Field))
		throw new RuntimeException("Invalid Ring for CG-Method (needs field)");
	
	SparseMatrix Atrans = (SparseMatrix)this.transpose();
	RingVector hb = Atrans.vectorMultiply(b);
	
	RingMatrix B = null;
	/*
	if (preconditioning)
	{
		B = new RingMatrix(field,zeilen,spalten);
		for (int i=1; i<=spalten; i++)
			B.setValue(((Field)this.getValue(i,i)).abs_reciprocal(),i,i);
	}
	*/
	Field alpha = (Field)zero;
	Field beta = (Field)zero;
	
	RingVector x = new RingVector(zero, spalten);
	for (int i=1; i<=spalten; i++)
		x.setValue(zero, i);
	
	RingVector r = new RingVector(hb.negate());
	RingVector w;
//	if (preconditioning) w = new RingVector(B.matrixMultiply(r));
	/*else*/ w = r;
	RingVector s = new RingVector(w);
	Field oldrsqr = (Field)r.scalarProduct(w);
	
	boolean stop = false; int n = 0;
	while (!stop)
	{
		n++;
		RingVector As = Atrans.vectorMultiply(this.vectorMultiply(s));
		alpha = ((Field)r.scalarProduct(w)).abs_divide( (Field)As.scalarProduct(s) );
		RingVector oldX = new RingVector(x);
		x = new RingVector(x.subtract(s.scalarMultiply(alpha)));
		r = new RingVector(r.subtract(As.scalarMultiply(alpha)));
//		if (preconditioning) w = new RingVector(B.matrixMultiply(r));
		/*else*/ w = r;
		Field rsqr = (Field)r.scalarProduct(w);
		beta = (Field)(rsqr.abs_divide(oldrsqr));
		oldrsqr = (Field)rsqr.abs_add(rsqr.abs_zero());
		s = new RingVector(w.add(s.scalarMultiply(beta)));
		Field dist = (Field)x.subtract(oldX).infinityNorm();
		stop = (dist.abs_isEqual(dist.abs_zero()));
		if ((epsilon != 0.0) && (((DoubleCastable)dist).doubleValue() < epsilon)) stop = true;			
		if (n > spalten) stop = true;
		if (beta.abs_isEqual(beta.abs_zero())) stop = true;
		
		if (print!=null)
		{
//			print.println("Step "+n+", x = "+x+", dist = "+dist);
			print.println("Step "+n+", dist = "+((DoubleCastable)dist).doubleValue());
		}
	}
	return x;
}
/*
public RingVector solveWithgauss(RingVector b)
{
	return solveWithGauss(b, 1);
}
// 0 : Kein Pivoting
// 1 : Spaltenpivoting
// 2 : volles Pivoting
public RingVector solveWithGauss(RingVector b, int strategy)
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);
	
	if (!(field instanceof Field)) 
		throw new RuntimeException("Not valid Ring for Gauss (needs Field)");
	
	if (spalten != zeilen)
		throw new RuntimeException("Matrix must be quadric.");
	
	RingVector spaltenpermut = new RingVector(new BigIntWrapper(0), spalten);
	for (int i=1; i<=spalten; i++) spaltenpermut.setValue(new BigIntWrapper(i), i);

	RingVector ergVec = new RingVector(b);
	RingMatrix R = new RingMatrix(this);
	
	Ring t;

	for (int i=1; i<zeilen; i++)
	{
		int pz = i, ps = i;
		if (strategy == 1)
		{
			for (int j=i+1; j<=zeilen; j++)
				if ( ((Signed)R.getValue(j,ps)).abs_abs().abs_compareTo( ((Signed)R.getValue(pz,ps)).abs_abs())==1)
					pz = j;
		}
		while ((pz<=zeilen) && (R.getValue(pz,ps).abs_isEqual(field.abs_zero()))) pz++;
		if (pz>zeilen) throw new RuntimeException("Equitation System is not solveable.");
		if (strategy == 2)
		{
			for (int j=i; j<=zeilen; j++)
				for (int k=i; k<=spalten; k++)
					if ( ((Signed)R.getValue(j,k)).abs_abs().abs_compareTo( ((Signed)R.getValue(pz,ps)).abs_abs())==1)
					{pz = j; ps = k; }
		}
		if (pz != i)
		{
			for (int j=1; j<=spalten; j++)
			{
				t = R.getValue(pz, j);
				R.setValue(R.getValue(i,j),pz,j);
				R.setValue(t,i,j);
			}
			
			t = ergVec.getValue(i);
			ergVec.setValue(ergVec.getValue(pz),i);
			ergVec.setValue(t, pz);
		}
		if (ps != i)
		{
			for (int j=1; j<=zeilen; j++)
			{
				t = R.getValue(j, ps);
				R.setValue(R.getValue(j,i),j,pz);
				R.setValue(t,j,i);
			}
			
			t = spaltenpermut.getValue(i);
			spaltenpermut.setValue(spaltenpermut.getValue(ps),i);
			spaltenpermut.setValue(t, ps);
		}
		for (int j=i+1; j<=zeilen; j++)
		{
			Ring fak = ((Field)R.getValue(j,i)).abs_divide((Field)R.getValue(i,i));
			Ring hier = fak.abs_multiply(ergVec.getValue(i));
			ergVec.setValue(ergVec.getValue(j).abs_subtract(hier),j);
			for (int k=i; k<=spalten; k++)
			{
				hier = fak.abs_multiply(R.getValue(i,k));
				R.setValue(R.getValue(j,k).abs_subtract(hier),j,k);
			}
			
		}
	}
	
	System.out.println(R);
	
	ergVec = R.solveRightUpperTriangleMatrix(ergVec);
	
	for (int i=1; i<=spalten; i++)
	{
		int k = ((BigIntWrapper)spaltenpermut.getValue(i)).value.intValue();
		if (k>i)
		{
			t = ergVec.getValue(i);
			ergVec.setValue(ergVec.getValue(k),i);
			ergVec.setValue(t, k);
		}
	}
	
	return ergVec;
}
public RingVector solveWithUFromQRDecomposition(RingVector b)
{
	final Ring eins = this.getValue(1,1).abs_unit();
	final Ring zwei = eins.abs_add(eins);
	
	RingVector y = new RingVector(b);
	int n = y.getRows();
	for (int i=1; i<=n; i++)
	{
		RingVector u = new RingVector(this.subMatrix(0,n-1,i-1,i-1));
		y = new RingVector(y.subtract(u.scalarMultiply(u.scalarProduct(y).abs_multiply(zwei))));
	}
	
	return y;
}
// Beide Grenzen jeweils eingeschlossen.		
// Änderung tvo 26.11.00
public RingMatrix subMatrix(int startRow, int endRow, int startCol, int endCol)
{
	int newRows = endRow-startRow+1;
	int newCols = endCol-startCol+1;
	RingMatrix erg = new RingMatrix(entry[0][0], newRows, newCols);

	for(int i=startRow;i<=endRow;i++)
		for(int k=startCol;k<=endCol;k++)
		{
			erg.entry[i-startRow][k-startCol]=entry[i][k];
		}
	return erg;
}
public RingMatrix subtract(RingMatrix B)
{
	int zeilen = entry.length;
	int spalten = entry[0].length;

	RingMatrix erg = new RingMatrix(entry[0][0],zeilen,spalten);
	
	for(int i=0;i<zeilen;i++)
		for(int j=0;j<spalten;j++)
		{
			erg.entry[i][j]=entry[i][j].abs_subtract(B.entry[i][j]);
		}
	return erg;		
}
public String toDoubleString()
{
	if (!(entry[0][0] instanceof Relement)) 
		throw new RuntimeException ("Method toDoubleString reserved for Relement as ring elements");
	
	String erg = "(";
	for(int i=0;i<entry.length;i++)
	{
		erg+="[";
		for(int j=0;j<entry[0].length;j++)
		{
			erg+=((Relement)entry[i][j]).toDouble();
			if(j<entry[0].length-1)
				erg+=",";
		}
		erg+="]";
		if(i<entry.length-1)
			erg+=",";		
	}
	erg+=")";
	return erg;
}
public Panel toPanel()
{
	Panel erg = new Panel(new java.awt.GridLayout(getRows(), getColumns(), 5, 5));
	for (int i=0; i<entry.length; i++)
		for (int j=0; j<entry[i].length; j++)
			erg.add(new Label(entry[i][j].toString()));
	return erg;
}
*/
public String toString()
{
	String erg = "[";
	Enumeration enumr = entryT.keys();
	while (enumr.hasMoreElements())
	{
		Tupel i = (Tupel)enumr.nextElement();
		erg += "a_{"+i.getRow()+","+i.getColumn()+"}="+((Ring)entryT.get(i)).toString()+" ";
	}
	return erg;
}
/*
public RingMatrix transformSimilarToHessenberg()
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);
	Ring zwei = field.abs_unit().abs_add(field.abs_unit());
	
	if (!(field instanceof Squarerootable)) return null;
	
	RingMatrix erg = new RingMatrix(this);
	for (int s = 1; s<=spalten-2; s++)
	{
		RingVector u = new RingVector(field, zeilen);
		
		Field sum = (Field)field.abs_zero();
		for (int i=zeilen; i>=1; i--)
		{
			if (i>=s+1) sum = (Field)sum.abs_add(erg.getValue(i,s).abs_multiply(erg.getValue(i,s))); 
			if (i>s+1) u.setValue(erg.getValue(i,s),i); 
			if (i==s+1) u.setValue(erg.getValue(i,s).abs_subtract((Ring)((Squarerootable)sum).abs_sqrt()),i);
			if (i<s+1) u.setValue(field.abs_zero(),i);
		}
		Field absolut = (Field)u.twoNorm();
		if (!absolut.abs_isEqual(field.abs_zero()))
		{
			u = new RingVector(u.scalarMultiply( absolut.abs_reciprocal() ));
			RingMatrix U = this.unit().subtract(u.matrixMultiply(u.transpose()).scalarMultiply(zwei));
		
			erg = U.matrixMultiply(erg.matrixMultiply(U.transpose()));
		}
	}
	return erg;
}
*/
public RingMatrix transpose()
{
	SparseMatrix erg = new SparseMatrix(zero, columns, rows);
	Enumeration enumr = entryT.keys();
	while (enumr.hasMoreElements())
	{
		Tupel t = (Tupel)enumr.nextElement();
		erg.setValue((Ring)entryT.get(t), t.getColumn(), t.getRow());
	}
	return erg;
}
/*
// Setzt bei Freiheiten immer 0 ein.
public RingVector solveRightUpperTriangleMatrix(RingVector b)
{
	return solveRightUpperTriangleMatrix(b, -1);
}
// Setzt bei Freiheiten immer 0 ein, außer bei der (useFree)ten Freiheit (dort 1).
public RingVector solveRightUpperTriangleMatrix(RingVector b, int useFree)
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);
	int frei = useFree;

	Vector[] zei = new Vector[spalten+1];
	for (int s=0; s<spalten+1; s++) zei[s] = new Vector();
	for (int z=1; z<=zeilen; z++)
	{
		int anznull = 0; 
		while ((anznull<spalten) && (getValue(z,anznull+1).abs_isEqual(field.abs_zero()))) anznull++;
		zei[anznull].addElement(new Integer(z));
	}			
	
	RingVector x = new RingVector(field, spalten);
	int s = spalten;
	int zvec = spalten, zpos = 0;
	while (zvec>=0)
	{
		if (zpos < zei[zvec].size())
		{
			int z = ((Integer)zei[zvec].elementAt(zpos++)).intValue();
			while (zvec<s-1)
			{
				if (frei==0) x.setValue(field.abs_unit(),s--);
				else x.setValue(field.abs_zero(),s--);
				frei--;
			}
			Field sum = (Field)b.getValue(z);
			for (int i=s+1; i<=spalten; i++)
				sum = (Field)sum.abs_subtract(getValue(z,i).abs_multiply(x.getValue(i)));
			if (zvec==s-1) x.setValue(sum.abs_divide((Field)getValue(z,zvec+1)), s--);
			else
			{
				if ((zvec>s-1) && (!sum.abs_isEqual(field.abs_zero())))
					throw new RuntimeException("System of equitations is not solveable");
			}
		} else {zpos = 0; zvec--;}		
	}
	while (0<s)
	{
		if (frei==0) x.setValue(field.abs_unit(),s--);
		else x.setValue(field.abs_zero(),s--);
		frei--;
	}
	return x;
}
*/

public RingVector vectorMultiply(RingVector b)
{
	RingVector erg = new RingVector(zero, rows);
	Enumeration enumr= entryT.keys();
	while (enumr.hasMoreElements())
	{
		Tupel i = (Tupel)enumr.nextElement();
		erg.entry[i.getRow()-1][0] = erg.entry[i.getRow()-1][0].abs_add
			(((Ring)entryT.get(i)).abs_multiply(b.getValue(i.getColumn())));
	}
	return erg;
}
}
