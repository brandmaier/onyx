package arithmetik;

import java.awt.*;
import java.util.*;

public class RingMatrix implements Ring
{
	public static boolean vertauschungenGeradeGlobalFlag = true;
	
	Ring[][] entry;

public RingMatrix()
{
	// Constructor for subclasses, makes no entrys.
}
/**
 * Insert the method's description here.
 * Creation date: (13.12.2002 21:45:17)
 * @param doublemat double[][]
 */
public RingMatrix(double[][] entry) 
{
	this.entry = new Ring[entry.length][entry[0].length];
	for (int i=0; i<entry.length; i++)
		for (int j=0; j<entry[i].length; j++)
			this.entry[i][j] = (new DoubleWrapper(entry[i][j]));
}
public RingMatrix(int[][] entry)
{
	this.entry = new Ring[entry.length][entry[0].length];
	for (int i=0; i<entry.length; i++)
		for (int j=0; j<entry[i].length; j++)
			this.entry[i][j] = (new Qelement(entry[i][j]));
}
public RingMatrix(Ring[][] entry)
{
	this.entry = new Ring[entry.length][entry[0].length];
	for (int i=0; i<entry.length; i++)
		for (int j=0; j<entry[i].length; j++)
			this.entry[i][j] = (entry[i][j]).abs_negate().abs_negate();
}
/**
 * Insert the method's description here.
 * Creation date: (08.05.2003 14:47:40)
 * @param diagonalElements arithmetik.Ring[]
 * @param n int
 */
public RingMatrix(Ring[] diagonalElements) 
{
	int dim = diagonalElements.length;
	entry = new Ring[dim][dim];
	Ring zero = diagonalElements[0].abs_zero();
	for(int i=0;i<dim;i++)
	 for(int j=0;j<dim;j++)
	  {
		 entry[i][j]=zero;
	  }
	for (int i=0; i<dim; i++) entry[i][i] = diagonalElements[i]; 
}
public RingMatrix(RingVector[] columnVectors)
{
	this.entry = new Ring[columnVectors[0].length()][columnVectors.length];
	for (int i=0; i<entry.length; i++)
		for (int j=0; j<entry[i].length; j++)
			this.entry[i][j] = (columnVectors[j].getValue(i+1)).abs_negate().abs_negate();	
}
public RingMatrix(Ring ringtype, int dim) 
{
	entry = new Ring[dim][dim];
	Ring zero = ringtype.abs_zero();
	for(int i=0;i<dim;i++)
	 for(int j=0;j<dim;j++)
	  {
		 entry[i][j]=zero;
	  }	  
}
public RingMatrix(Ring ringtype, int zeilen, int spalten) 
{
	entry = new Ring[zeilen][spalten];
	Ring zero = ringtype.abs_zero();
	for(int i=0;i<zeilen;i++)
	 for(int j=0;j<spalten;j++)
	  {
		 entry[i][j]=zero;
	  }	  
}
public RingMatrix(RingMatrix toCopy) 
{
	entry = new Ring[toCopy.getRows()][toCopy.getColumns()];
	for (int i=0; i<entry.length; i++)
		for (int j=0; j<entry[i].length; j++)
			entry[i][j] = (toCopy.entry[i][j]).abs_negate().abs_negate();
}
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
/**
 * Insert the method's description here.
 * Creation date: (13.08.2004 11:14:38)
 * @return arithmetik.RingVector[]
 */
public RingVector[] approximateKernelBasisOfSymmetric(double epsilon) 
{
	int dim = getRows();
	RingMatrix transform = unit();
	RingVector ev = eigenvalues(epsilon, transform);

	Vector ergvec = new Vector();
	for (int i=0; i<ev.length(); i++)
	{
		if ( ((DoubleCastable)ev.getValue(i+1)).doubleValue() < epsilon*2 )
			ergvec.addElement(transform.getColumn(i+1));			
	}
	RingVector[] erg = new RingVector[ergvec.size()];
	for (int i=0; i<erg.length; i++) erg[i] = (RingVector)ergvec.elementAt(i);

	return RingVector.orthonormalize(erg);
}
// Gibt eine (nicht orthogonale oder normale) Basis des Kerns der Matrix zurück.
public RingVector[] coreBasis()
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);

	if (!(field instanceof Field))
		throw new RuntimeException("Not valid Ring for coreBasis (needs Field and Squarerootable)");

	RingMatrix r = this.findRMatrix();
	
	Vector erg = new Vector();
	RingVector v = new RingVector(field, this.getColumns());
	RingVector c = r.solveRightUpperTriangleMatrix(v, 0);
	if (c==null) return new RingVector[0];
	int m = 1;
	while (!c.abs_isEqual(c.abs_zero())) 
	{
		erg.addElement(new RingVector(c));
		c = r.solveRightUpperTriangleMatrix(v, m++);
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
/**
 * Insert the method's description here.
 * Creation date: (13.12.2002 22:52:53)
 * @return arithmetik.Ring
 */
public Ring determinant() 
{
	System.out.println("Determinante aufgerufen");
	
	if (!(entry[0][0] instanceof GcdAble)) return developDeterminant();
	if (!(entry[0][0] instanceof Field))
	{
		Ring unit = entry[0][0].abs_unit();
		QuotientField[][] fentry = new QuotientField[entry.length][entry[0].length];
		for (int i=0; i<fentry.length; i++)
			for (int j=0; j<fentry.length; j++)
				fentry[i][j] = new QuotientField(entry[i][j],unit);
		RingMatrix M = new RingMatrix(fentry);
		QuotientField det = (QuotientField)M.determinant();
		return (Ring)((GcdAble)det.zaehler).abs_divide((GcdAble)det.nenner);
	}

	RingMatrix R = findRMatrix();
	Ring erg = entry[0][0].abs_unit();
	for (int i=1; i<=R.getRows(); i++)
		erg = erg.abs_multiply(R.getValue(i,i));
	if (!vertauschungenGeradeGlobalFlag) return erg.abs_negate();
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
// approximiert die Eigenwerte bis epsilon mit dem QR-Verfahren. 
// Rückgabe ist Vector mit den Eigenwerten. 
public RingVector eigenvalues(double epsilon)
{
	return eigenvalues(epsilon, null);
}
// approximiert die Eigenwerte bis epsilon mit dem QR-Verfahren. 
// Rückgabe ist Vector mit den Eigenwerten.

// Wenn transformationMatrix nicht null ist, wird jede Transformation dort mit eingearbeitet.
public RingVector eigenvalues(double epsilon, RingMatrix transformationMatrix)
{
	Ring r = entry[0][0];

	RingMatrix work = this;
	if (!(r instanceof Squarerootable))
	{
		if (!(r instanceof DoubleCastable)) throw new RuntimeException ("Eigenvalues must be called with squarerootable Ring, or at least DoubleCastable for double Eigenvalues.");
		DoubleWrapper[][] neu = new DoubleWrapper[entry.length][entry.length];
		for (int i=0; i<entry.length; i++)
			for (int j=0; j<entry.length; j++)
				neu[i][j] = new DoubleWrapper(((DoubleCastable)entry[i][j]).doubleValue());

		work = new RingMatrix(neu);
	}	
	
	RingMatrix A = work.transformSimilarToHessenberg(transformationMatrix);
	RingVector erg = A.eigenvaluesOfHessenberg(epsilon,transformationMatrix);

//	System.out.println("Test (sollte Diagonalmatrix ergeben) transform.transpose() * this * transform : \r\n"+transformationMatrix.transpose().matrixMultiply(this).matrixMultiply(transformationMatrix));
	/*
	if (r instanceof Orderd)		// falls die Eigenwerte sortierbar sind, wird sortiert.
	{
		Ring[] arr = erg.toArray();
		
		Arrays.sort(arr, new Comparator() {
			public boolean equals(Object o) {return (((Orderd)this).abs_compareTo((Orderd)o)==0);}
			public int compare(Object o1, Object o2) {return ((Orderd)o1).abs_compareTo((Orderd)o2);}
		});

		if (transformationMatrix != null)
		{
			int dim = getRows();
			RingMatrix n = new RingMatrix(transformationMatrix);
			boolean[] taken = new boolean[dim];
			for (int i=0; i<dim; i++) taken[i] = false;
			for (int i=0; i<dim; i++)
			{
				int j=0; 
				while ((j<dim) && ((taken[j]) || (!erg.getValue(j+1).equals((Orderd)arr[i])))) j++;
				taken[j] = true;
				for (int k=0; k<dim; k++) transformationMatrix.setValue(n.getValue(k+1,j+1),k+1,i+1);
			}	
		}
		erg = new RingVector(arr);
	}
	*/
	return erg;
}
// Berechnet die Eigenwerte einer 2x2 Matrix mit den mitgegebenen Einträgen; diese müssen
// Field und Squarerootable implementieren.
private static Ring[] eigenvalues22(Ring a11, Ring a12, Ring a21, Ring a22)
{
	Ring eins = a11.abs_unit();
	Ring zwei = eins.abs_add(eins);
	Ring p = (a11.abs_add(a22));
	Ring q = (a11.abs_multiply(a22)).abs_subtract
			 (a12.abs_multiply(a21));
	Ring vw = ((Field)p).abs_divide((Field)zwei);
	Ring iw = (Ring)((Squarerootable)(vw.abs_multiply(vw)).abs_subtract(q)).abs_sqrt();
	Ring[] erg = new Ring[2];
	erg[0] = vw.abs_add(iw);
	erg[1] = vw.abs_subtract(iw);
	return erg;
}
// Berechnet die Eigenwerte einer 2x2 Matrix mit den mitgegebenen Einträgen; diese müssen
// Field und Squarerootable implementieren.
// Ist transformationMatrix nicht null, wird dort die Transformation angegeben, um auf die Diagonalgestalt zu kommen.
public static Ring[] eigenvalues22(Ring a11, Ring a12, Ring a21, Ring a22, RingMatrix transformationMatrix)
{
	Field eins = (Field)a11.abs_unit();
	Field zwei = (Field)eins.abs_add(eins);
	Ring p = (a11.abs_add(a22));
	Ring q = (a11.abs_multiply(a22)).abs_subtract
			 (a12.abs_multiply(a21));
	Ring vw = ((Field)p).abs_divide((Field)zwei);
	Ring iw = vw.abs_multiply(vw).abs_subtract(q);

	// Für den Fall, dass wir hier mit Doubles arbeiten, werden hier noch "fast - null" Eigenwerte zugelassen
	if ((eins instanceof DoubleWrapper) && (((DoubleWrapper)iw).doubleValue()<0.0))
		iw = new DoubleWrapper(0.0);
	iw = (Ring)((Squarerootable)iw).abs_sqrt();	
			
	Ring[] erg = new Ring[2];
	erg[0] = vw.abs_add(iw);
	erg[1] = vw.abs_subtract(iw);

	if (transformationMatrix!=null)
	{
		// Berechnung der Eigenvektoren
		Field linksoben1 = (Field)a11.abs_subtract(erg[0]);
		Field rechtsunten1 = (Field)a22.abs_subtract(erg[0]);
		Field linksoben2 = (Field)a11.abs_subtract(erg[1]);
		Field rechtsunten2 = (Field)a22.abs_subtract(erg[1]);
		int i,j1,j2;
		if (eins instanceof DoubleNormable)
		{
			// Sortieren: Größter Wert wählen (numerisch am stabilsten)
			double[] n = {((DoubleNormable)linksoben1).doubleNorm(), ((DoubleNormable)a12).doubleNorm(),
			       ((DoubleNormable)a21).doubleNorm(), ((DoubleNormable)rechtsunten1).doubleNorm()};
			if (n[1] > n[2]) i = 1; else i = 2;
			if (n[0] > n[3]) j1 = 0; else j1 = 3;
			if (n[i] > n[j1]) j1 = i;

			n[0] = ((DoubleNormable)linksoben2).doubleNorm();
			n[3] = ((DoubleNormable)rechtsunten2).doubleNorm();
			if (n[0] > n[3]) j2 = 0; else j2 = 3;
			if (n[i] > n[j2]) j2 = i;
		} else {j1=0; j2=0;}
		Field[] ev1 = new Field[2]; Field[] ev2 = new Field[2]; 
		if (j1==0) ev1 = new Field[]{(Field)((Field)a12).abs_divide(linksoben1).abs_negate(), eins};
		if (j1==1) ev1 = new Field[]{eins, (Field)linksoben1.abs_divide((Field)a12).abs_negate()};
		if (j1==2) ev1 = new Field[]{(Field)rechtsunten1.abs_divide((Field)a21).abs_negate(), eins};
		if (j1==3) ev1 = new Field[]{eins, (Field)((Field)a21).abs_divide(rechtsunten1).abs_negate()};
		if (j2==0) ev2 = new Field[]{(Field)((Field)a12).abs_divide(linksoben2).abs_negate(), eins};
		if (j2==1) ev2 = new Field[]{eins, (Field)linksoben2.abs_divide((Field)a12).abs_negate()};
		if (j2==2) ev2 = new Field[]{(Field)rechtsunten2.abs_divide((Field)a21).abs_negate(), eins};
		if (j2==3) ev2 = new Field[]{eins, (Field)((Field)a21).abs_divide(rechtsunten2).abs_negate(), eins};

		Field n1 = (Field)((Squarerootable)ev1[0].abs_multiply(ev1[0]).abs_add(ev1[1].abs_multiply(ev1[1]))).abs_sqrt();
		Field n2 = (Field)((Squarerootable)ev2[0].abs_multiply(ev2[0]).abs_add(ev2[1].abs_multiply(ev2[1]))).abs_sqrt();
		
		transformationMatrix.setValue(ev1[0].abs_divide(n1),1,1);
		transformationMatrix.setValue(ev1[1].abs_divide(n1),2,1);
		transformationMatrix.setValue(ev2[0].abs_divide(n2),1,2);
		transformationMatrix.setValue(ev2[1].abs_divide(n2),2,2);

		RingMatrix t = new RingMatrix(new Ring[][]{{a11,a12},{a21,a22}});
		System.out.println("2 x 2 - Matrix : \r\n"+t);
		/*
		RingMatrix evmatrix = new RingMatrix(new Ring[][]{{erg[0],a11.abs_zero()},{a11.abs_zero(),erg[1]}});
		System.out.println("Test auf Orthogonaltransformation: 2x2 - Matrix t ist = \r\n"+t+"\r\ntransform * Eigenwertematrix * transform.transpose()= \r\n"+transformationMatrix.matrixMultiply(evmatrix).matrixMultiply(transformationMatrix.transpose()));
		*/
	}		

	
	return erg;
}
public RingVector eigenvaluesOfHessenberg(double epsilon)
{
	return eigenvaluesOfHessenberg(epsilon, null);
}

// Ist transformation nicht null, werden alle Transformationen dort gespeichert.
public RingVector eigenvaluesOfHessenberg(double epsilon, RingMatrix transformationMatrix)
{
	int dim = this.getRows();
	
	Field field = (Field)getValue(1,1);
	
	if (dim==1) 
	{
		RingVector erg = new RingVector(field, 1);
		erg.setValue(field, 1);
		return erg;
	}
	if (dim==2)
	{
		RingVector erg = new RingVector(getValue(1,1), 2);
		Ring[] ergs = this.eigenvalues22(getValue(1,1), getValue(1,2), getValue(2,1), getValue(2,2), transformationMatrix);
		erg.setValue(ergs[0], 1);
		erg.setValue(ergs[1], 2);
		return erg;
	}

	RingMatrix A = new RingMatrix(this);
	RingMatrix eins = new RingMatrix(field, 1), zwei = new RingMatrix(field, 1);

	boolean weiter = true;
	int schritt = 0;
	while (weiter)
	{
		/*
		// Shifts berechnen und auswählen
		Ring[] shiftKandidat = (RingMatrix.eigenvalues22(A.getValue(dim-1, dim-1), A.getValue(dim-1, dim), A.getValue(dim, dim-1), A.getValue(dim, dim)));
		Ring[] shiftAbstand = new Ring[2];
		shiftAbstand[0] = shiftKandidat[0].abs_subtract(A.getValue(dim,dim));
		shiftAbstand[1] = shiftKandidat[1].abs_subtract(A.getValue(dim,dim));
		int kan = 0;
		if (((DoubleNormable)shiftAbstand[1]).doubleNorm() < ((DoubleNormable)shiftAbstand[1]).doubleNorm()) kan = 1;
		RingMatrix[] qr = (A.subtract(A.unit().scalarMultiply(shiftKandidat[kan]))).qRDecompositionOfHessenberg();
		A = qr[0].transpose().matrixMultiply(A.matrixMultiply(qr[0]));
		*/
		double aNorm = ((DoubleNormable)A.twoNorm()).doubleNorm();
		for (int i=2; (i<=dim) && (weiter); i++)
		{
			double d = ((DoubleNormable)A.getValue(i,i-1)).doubleNorm();
//			if ( (d+aNorm) - aNorm == 0.0)
			if (d < epsilon*epsilon)
			{
				eins = new RingMatrix(field, i-1);
				for (int j=1; j<=i-1; j++)
					for (int k=1; k<=i-1; k++)
						eins.setValue(A.getValue(j,k),j,k);
				zwei = new RingMatrix(field, dim-i+1);
				for (int j=i; j<=dim; j++)
					for (int k=i; k<=dim; k++)
						zwei.setValue(A.getValue(j,k),j-i+1,k-i+1);
				weiter = false;
			}
		}
		if (weiter) A = A.getNextHessenbergOfQR(transformationMatrix);
		System.out.println(A);
		
		schritt++;
	}

	int es = eins.getRows(), zs = zwei.getRows();
	System.out.println("Schritt = "+schritt+", teile "+es+","+zs);
	RingMatrix transform1 = null, transform2 = null;
	if (transformationMatrix!=null)
	{
		transform1 = eins.unit();
		transform2 = zwei.unit();
	}
	
	RingVector erg = new RingVector(field, dim);
	RingVector einserg = eins.eigenvaluesOfHessenberg(epsilon,transform1);
	RingVector zweierg = zwei.eigenvaluesOfHessenberg(epsilon,transform2);

	// Erstellen der Transformationsmatrix aus den beiden Einzelteilen
	if (transformationMatrix != null)
	{
		RingMatrix ntrans = A.unit();
		for (int i=1; i<=dim; i++)
			for (int j=1; j<=dim; j++)
			{
				if ((i<=es) && (j<=es)) ntrans.setValue(transform1.getValue(i,j),i,j);
				if ((i>es) && (j>es)) ntrans.setValue(transform2.getValue(i-es,j-es),i,j);
			}

		RingMatrix zw = transformationMatrix.matrixMultiply(ntrans);
		for (int i=1; i<=dim; i++)
			for (int j=1; j<=dim; j++)
				transformationMatrix.setValue(zw.getValue(i,j),i,j);
	}
	
	int einsdim = einserg.getRows();
	int zweidim = zweierg.getRows();
	for (int i=1; i<=einsdim; i++)
		erg.setValue(einserg.getValue(i),i);
	for (int i=einsdim+1; i<=einsdim+zweidim; i++)
		erg.setValue(zweierg.getValue(i-einsdim), i);
	return erg;		
}
/**
 * Insert the method's description here.
 * Creation date: (07.08.2002 17:48:39)
 * @return arithmetik.RingVector[]
 */
public RingVector[] eigenvectors(double epsilon) 
{
	return eigenvectors(epsilon, -1);		
}
/**
 * Insert the method's description here.
 * Creation date: (07.08.2002 17:48:39)
 * @return arithmetik.RingVector[]

	gibt numberOfEigenvectors Eigenvektoren zurück; bei geordneten Ringen automatisch die Vektoren mit den größten 
	Eigenwerten. 
 
 */
public RingVector[] eigenvectors(double epsilon, int numberOfEigenvectors) 
{
	RingVector ewerte = eigenvalues(epsilon);
	RingVector[] erg = new RingVector[ewerte.length()];
	for (int i=0; (i < ewerte.length()) && ((numberOfEigenvectors==-1) || (i < numberOfEigenvectors))  ; i++)
	{
		Ring wert = ewerte.getValue(i+1);
		RingMatrix a = new RingMatrix(this);
		for (int j=0; j<a.getRows(); j++) a.setValue(a.getValue(j+1,j+1).abs_subtract(wert),j+1,j+1);

		// wir lösen a * x = 0 nährungsweise
		a = a.findRMatrix();
		Ring min = a.getValue(1,1);
		int minst = 1;
		for (int j=2; j<=a.getRows(); j++)
			if (((Orderd)a.getValue(j,j)).abs_compareTo((Orderd)min) == -1) {minst = j; min = a.getValue(j,j);}
		a.setValue(min.abs_zero(), minst,minst);
		
		RingVector[] cb = a.coreBasis();
		erg[i] = cb[0];
	}
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

	// absichern, dass alle Werte rechts unten null sind.
	for (int i=1; i<=zeilen; i++)
		for (int j=1; j<i; j++)
			R.setValue(field.abs_zero(),i,j);
	
	RingMatrix[] erg = {R,I};
	return erg;
}
// Nimmt eine (nicht notwendigerweise quadratische) Matrix und überführt sie in eine rechte
// obere Dreiecksmatrix (d.h. in jeder Zeile ist mindestens eine führende Spalte mehr 0 als
// in der vorangegangenen Zeile; es werden auch Zeilenvertauschungen vorgenommen). Ist 
// snd != null, werden im zweitern Argument die entsprechenden Aenderungen an snd mitgeführt. 
// Ist vec= null, so werden alle Zeilenvertauschungen auch an dem Vektor durchgeführt.
// in vertauschungenGeradeGlobalFlag wird gespeichert, ob eine gerade Anzahl an
// Nachbarzeilenvertauschungen vorgenommen wurde.

public RingMatrix[] findRMatrix(RingMatrix snd, RingVector vec)
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);
	
	if (!(field instanceof Field)) 
		throw new RuntimeException("Not valid Ring for Gauss (needs Field)");
	
	vertauschungenGeradeGlobalFlag = true;
	RingMatrix R = new RingMatrix(this);
	RingMatrix I;
	if (snd != null) I = new RingMatrix(snd); else I = null;
	RingVector v; 
	if (vec != null) v = new RingVector(vec); else v = null;
	
	Ring t;

	int naechsteZeile = 1;
	for (int i=1; i<=spalten; i++)
	{
		int pz = naechsteZeile;
		while ((pz<=zeilen) && (R.getValue(pz,i).abs_isEqual(field.abs_zero()))) pz++;
		if (field instanceof Signed)					// Falls wir einen Betrag haben, Spaltenpivoting
		{
			for (int j=pz+1; j<=zeilen; j++)
				if ( ((Signed)R.getValue(j,i)).abs_abs().abs_compareTo( ((Signed)R.getValue(pz,i)).abs_abs())==1)
					pz = j;
		}
		
		if (pz <= zeilen)
		{
			if (pz != naechsteZeile)
			{
				vertauschungenGeradeGlobalFlag = !vertauschungenGeradeGlobalFlag;
				for (int j=1; j<=spalten; j++)
				{
					t = R.getValue(pz, j);
					R.setValue(R.getValue(naechsteZeile,j),pz,j);
					R.setValue(t,naechsteZeile,j);
					if (I != null)
					{
						t = I.getValue(pz, j);
						I.setValue(I.getValue(naechsteZeile,j),pz,j);
						I.setValue(t,naechsteZeile,j);
					}
				}
				if (v != null)
				{
					t = v.getValue(pz);
					v.setValue(v.getValue(naechsteZeile), pz);
					v.setValue(t,naechsteZeile);
				}
			}
			for (int j=naechsteZeile+1; j<=zeilen; j++)
			{
				Ring fak = ((Field)R.getValue(j,i)).abs_divide((Field)R.getValue(naechsteZeile,i));
				for (int k=i+1; k<=spalten; k++)
				{
					Field hier = (Field)fak.abs_multiply(R.getValue(naechsteZeile,k));
					R.setValue(R.getValue(j,k).abs_subtract(hier),j,k);
				}
				R.setValue(field.abs_zero(),j,i);
				if (I != null)
				{
					for (int k=1; k<=spalten; k++)
					{
						Field hier = (Field)fak.abs_multiply(I.getValue(naechsteZeile,k));
						I.setValue(I.getValue(j,k).abs_subtract(hier),j,k);
					}
				}
				if (v != null)
				{
					Field hier = (Field)fak.abs_multiply(v.getValue(naechsteZeile));
					v.setValue(v.getValue(j).abs_subtract(hier),j);
				}
			}
			naechsteZeile++;
		}
	}

	// absichern, dass alle Werte rechts unten null sind.
	for (int i=1; i<=zeilen; i++)
		for (int j=1; j<i; j++)
			R.setValue(field.abs_zero(),i,j);

	RingMatrix[] erg = {R,I,v};
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (18.05.2002 21:37:25)
 * @return arithmetik.RingMatrix[]
 * @param v arithmetik.RingVector
 */
public RingMatrix[] findRMatrix(RingVector vec)
{	
	RingMatrix[] zw = findRMatrix(null, vec);
	RingMatrix[] erg = {zw[0],zw[2]};
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (04.08.2004 14:55:38)
 * @return arithmetik.RingVector
 * @param i int
 */
public RingVector getColumn(int j) 
{
	Ring[] vecentry = new Ring[getRows()];
	for (int i=0; i<vecentry.length; i++) vecentry[i] = entry[i][j-1];
	return new RingVector(vecentry);
}
public int getColumns()
{
	if (entry.length==0) return 0;
	else return entry[0].length;
}
/**
 * Insert the method's description here.
 * Creation date: (19.05.2002 18:15:05)
 * @return arithmetik.Ring
 */
public Ring getDiagonalProductOfRMatrix() 
{
	int lauf = Math.min(getRows(), getColumns());
	RingMatrix g = findRMatrix();
	Ring erg = getValue(1,1).abs_unit();
	for (int i=1; i<=lauf; i++)
		erg = erg.abs_multiply(g.getValue(i,i));
	return erg;
}
// Von der Hessenberg-Matrix H wird eine orthogonale Matrix Q erstellt, so dass 
// Q*this*Q.transpose() zwei Schritte  
// des QR-Verfahrens mit Raighley-Shift ist, der sich aus den Eigenwerten der unteren 2x2
// Matrix zusammensetzt; durch die beiden Schritte werden die Zahle reel belassen.
// Zurückgegeben wird H' = Q*this*Q.transpose()
// Algorithmus nach "Numerical Recipes In Fortran 77: The Art of Scientific Computing" ISBN 0-521-43064-X)
public RingMatrix getNextHessenbergOfQR()
{
	return getNextHessenbergOfQR(null);
}
// Von der Hessenberg-Matrix H wird eine orthogonale Matrix Q erstellt, so dass 
// Q*this*Q.transpose() zwei Schritte  
// des QR-Verfahrens mit Raighley-Shift ist, der sich aus den Eigenwerten der unteren 2x2
// Matrix zusammensetzt; durch die beiden Schritte werden die Zahle reel belassen.
// Zurückgegeben wird H' = Q*this*Q.transpose()
// Algorithmus nach "Numerical Recipes In Fortran 77: The Art of Scientific Computing" ISBN 0-521-43064-X)

// ist transformationMatrix nicht null, wird Q mit dieser Matrix multipliziert. 
public RingMatrix getNextHessenbergOfQR(RingMatrix transformationMatrix)
{
	int dim = this.getRows();
	Field field = (Field)this.getValue(1,1);
	Field zwei = (Field)field.abs_unit().abs_add(field.abs_unit());
	RingMatrix H = new RingMatrix(this);
	Ring a11 = H.getValue(1,1), a12 = H.getValue(1,2), a21 = H.getValue(2,1), a22 = H.getValue(2,2),
		 amm = H.getValue(dim-1,dim-1), amn = H.getValue(dim-1,dim), anm = H.getValue(dim,dim-1), ann = H.getValue(dim,dim);
	Ring a = ann.abs_subtract(a11);
	Ring b = amm.abs_subtract(a11);
	Field p = (Field)( ((Field)(a.abs_multiply(b)).abs_subtract(amn.abs_multiply(anm))).abs_divide((Field)a21) ).abs_add(a12);
	Field q = (Field)a22.abs_subtract(ann).abs_subtract(b);
	Field r = (Field)H.getValue(3,2);
	Field s = (Field)((Signed)p).abs_abs().abs_add(((Signed)q).abs_abs()).abs_add(((Signed)r).abs_abs());
	p = p.abs_divide(s); q = q.abs_divide(s); r = r.abs_divide(s);

	for (int i=1; i<=dim-1; i++)
	{
		s = (Field) ((Squarerootable)(p.abs_multiply(p)).abs_add(q.abs_multiply(q)).abs_add(r.abs_multiply(r))).abs_sqrt();
		s = (Field)s.abs_multiply((Field)((Signed)p).abs_ringSignum());
		// spalt * zeil repräsentiert die 3x3 Matrix P_i an den Zeilen und Spalten i,i+1,i+2
		Field[] spalt = new Field[3];
		Field[] zeil = new Field[3];
		Field ps = (Field)p.abs_add(s);
		spalt[0] = ps.abs_divide(s); spalt[1] = q.abs_divide(s); spalt[2] = r.abs_divide(s);
		zeil[0] = (Field)field.abs_unit(); zeil[1] = q.abs_divide(ps); zeil[2] = r.abs_divide(ps);
		// von links Q dranmultiplizieren...
		for (int k=1; k<=dim; k++)
		{
			Field sum = (Field)H.getValue(i,k).abs_add(zeil[1].abs_multiply(H.getValue(i+1,k)));
			if (i<dim-1) 
			{
				sum = (Field)sum.abs_add(zeil[2].abs_multiply(H.getValue(i+2,k)));
				H.setValue(H.getValue(i+2,k).abs_subtract(sum.abs_multiply(spalt[2])),i+2,k);
			}
			H.setValue(H.getValue(i+1,k).abs_subtract(sum.abs_multiply(spalt[1])),i+1,k);
			H.setValue(H.getValue(i,k).abs_subtract(sum.abs_multiply(spalt[0])),i,k);
		}
		// ...dann von rechts Qtrans
		for (int k=1; k<=dim; k++)
		{
			Field sum = (Field)spalt[0].abs_multiply(H.getValue(k,i)).abs_add(spalt[1].abs_multiply(H.getValue(k,i+1)));
			if (i<dim-1)
			{
				sum = (Field)sum.abs_add(spalt[2].abs_multiply(H.getValue(k,i+2)));
				H.setValue(H.getValue(k,i+2).abs_subtract(zeil[2].abs_multiply(sum)), k, i+2);
			}
			H.setValue(H.getValue(k,i+1).abs_subtract(zeil[1].abs_multiply(sum)),k,i+1);
			H.setValue(H.getValue(k,i).abs_subtract(sum), k, i);
			// Die Transformationsmatrix mitnehmen...
			if (transformationMatrix != null)
			{
				RingMatrix J = transformationMatrix;
				sum = (Field)spalt[0].abs_multiply(J.getValue(k,i)).abs_add(spalt[1].abs_multiply(J.getValue(k,i+1)));
				if (i<dim-1)
				{
					sum = (Field)sum.abs_add(spalt[2].abs_multiply(J.getValue(k,i+2)));
					J.setValue(J.getValue(k,i+2).abs_subtract(zeil[2].abs_multiply(sum)), k, i+2);
				}
				J.setValue(J.getValue(k,i+1).abs_subtract(zeil[1].abs_multiply(sum)),k,i+1);
				J.setValue(J.getValue(k,i).abs_subtract(sum), k, i);
			}				
		}
		if (i<dim-1)
		{
			p = (Field)H.getValue(i+1,i);
			q = (Field)H.getValue(i+2,i); 
			if (i<dim-2) r = (Field)H.getValue(i+3, i); else r = (Field)field.abs_zero();
			s = (Field)((Signed)p).abs_abs().abs_add(((Signed)q).abs_abs()).abs_add(((Signed)r).abs_abs());
			p = p.abs_divide(s); q = q.abs_divide(s); r = r.abs_divide(s);
		}
	}
	return H;
}
public int getRows()
{
	return entry.length;
}
/**
 * Insert the method's description here.
 * Creation date: (08.05.2003 15:36:49)
 * @return int

	Berechnet die Eigenwerte der Matrix und gibt Anzahl der positivien minus Anzahl der negativen Eigenwerte zurück.
 
 */
public int getSignature() 
{
	Ring ring = entry[0][0];
	if (!(ring instanceof Signed)) throw new RuntimeException ("Call for getSignature must be performed with orderd Ring.");

	RingVector eigen = eigenvalues(0.001);

	int erg = 0;
	for (int i=0; i<eigen.length(); i++)
	{
		int sig = ((Signed)eigen.getValue(i+1)).abs_signum();
		if (sig==1) erg++; 
		if (sig==-1) erg--;
	}

	return erg;
}
public Ring getValue(int row, int col)
{
	return entry[row-1][col-1];
}
// returns the maximal absolute value of the matrix entries
public Ring infinityNorm()
{
	Ring erg = entry[0][0].abs_zero();
	if (!(erg instanceof Signed)) 
		throw new RuntimeException ("Not valid Ring for infinityNorm (needs Signed)");
	
	for(int i=0; i<entry.length; i++)
		for(int j=0; j<entry[0].length; j++)
		{
			Signed s = ((Signed)entry[i][j]).abs_abs();
			if (s.abs_compareTo((Orderd)erg) == 1) erg = s.abs_abs();
		}	
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
	if ((zeilen==0) || (spalten == 0)) return this;
	Ring field = this.getValue(1,1);
	
	if (!(field instanceof Field)) 
		throw new RuntimeException("Not valid Ring for invert (needs Field)");
	
	if ((spalten==1) && (zeilen==1)) 
	{
		Ring[][] r = {{((Field)field).abs_reciprocal()}};
		return new RingMatrix(r);
	}
	if ((spalten==2) && (zeilen==2)) return this.invert22();
	if ((spalten==3) && (zeilen==3)) return this.invert33();
	if ((spalten==4) && (zeilen==4)) return this.invert44();
	
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
private RingMatrix invert22()
{
	Field field = (Field)this.getValue(1,1);
	RingMatrix erg = new RingMatrix(field, 2, 2);
	Field det = (Field)((entry[0][0].abs_multiply(entry[1][1])).abs_subtract(entry[0][1].abs_multiply(entry[1][0])));
	erg.entry[0][0] = ((Field)entry[1][1]).abs_divide(det);
	erg.entry[0][1] = ((Field)entry[0][1]).abs_divide((Field)det.abs_negate());
	erg.entry[1][0] = ((Field)entry[1][0]).abs_divide((Field)det.abs_negate());
	erg.entry[1][1] = ((Field)entry[0][0]).abs_divide(det);
	return erg;
}
private RingMatrix invert33()
{
	Field field = (Field)this.getValue(1,1);
	RingMatrix erg = new RingMatrix(field, 3, 3);
	Field det = (Field)(
			    (entry[0][0].abs_multiply(entry[1][1]).abs_multiply(entry[2][2])).abs_subtract
				(entry[0][0].abs_multiply(entry[1][2]).abs_multiply(entry[2][1])).abs_subtract
				(entry[1][0].abs_multiply(entry[0][1]).abs_multiply(entry[2][2])).abs_add
				(entry[1][0].abs_multiply(entry[0][2]).abs_multiply(entry[2][1])).abs_add
				(entry[2][0].abs_multiply(entry[0][1]).abs_multiply(entry[1][2])).abs_subtract
				(entry[2][0].abs_multiply(entry[0][2]).abs_multiply(entry[1][1]))
				);
	erg.entry[0][0] = ((Field)((entry[1][1].abs_multiply(entry[2][2])).abs_subtract
						  	   (entry[1][2].abs_multiply(entry[2][1])))
							   ).abs_divide(det);
	erg.entry[0][1] = ((Field)((entry[0][2].abs_multiply(entry[2][1])).abs_subtract
							   (entry[0][1].abs_multiply(entry[2][2])))
							   ).abs_divide(det);
	erg.entry[0][2] = ((Field)((entry[0][1].abs_multiply(entry[1][2])).abs_subtract
							   (entry[0][2].abs_multiply(entry[1][1])))
							   ).abs_divide(det);
	erg.entry[1][0] = ((Field)((entry[1][2].abs_multiply(entry[2][0])).abs_subtract
						  	   (entry[1][0].abs_multiply(entry[2][2])))
							   ).abs_divide(det);
	erg.entry[1][1] = ((Field)((entry[0][0].abs_multiply(entry[2][2])).abs_subtract
							   (entry[0][2].abs_multiply(entry[2][0])))
							   ).abs_divide(det);
	erg.entry[1][2] = ((Field)((entry[0][2].abs_multiply(entry[1][0])).abs_subtract
							   (entry[0][0].abs_multiply(entry[1][2])))
							   ).abs_divide(det);
	erg.entry[2][0] = ((Field)((entry[1][0].abs_multiply(entry[2][1])).abs_subtract
						  	   (entry[1][1].abs_multiply(entry[2][0])))
							   ).abs_divide(det);
	erg.entry[2][1] = ((Field)((entry[0][1].abs_multiply(entry[2][0])).abs_subtract
							   (entry[0][0].abs_multiply(entry[2][1])))
							   ).abs_divide(det);
	erg.entry[2][2] = ((Field)((entry[0][0].abs_multiply(entry[1][1])).abs_subtract
							   (entry[0][1].abs_multiply(entry[1][0])))
							   ).abs_divide(det);
	return erg;
}
private RingMatrix invert44()
{
	Field field = (Field)this.getValue(1,1);
	RingMatrix erg = new RingMatrix(field, 4, 4);
	Field det = (Field)(
			    (entry[0][0].abs_multiply(entry[1][1]).abs_multiply(entry[2][2]).abs_multiply(entry[3][3])).abs_subtract
			    (entry[0][0].abs_multiply(entry[1][1]).abs_multiply(entry[2][3]).abs_multiply(entry[3][2])).abs_subtract
			    (entry[0][0].abs_multiply(entry[2][1]).abs_multiply(entry[1][2]).abs_multiply(entry[3][3])).abs_add
			    (entry[0][0].abs_multiply(entry[2][1]).abs_multiply(entry[1][3]).abs_multiply(entry[3][2])).abs_add
			    (entry[0][0].abs_multiply(entry[3][1]).abs_multiply(entry[1][2]).abs_multiply(entry[2][3])).abs_subtract
			    (entry[0][0].abs_multiply(entry[3][1]).abs_multiply(entry[1][3]).abs_multiply(entry[2][2])).abs_subtract
			    (entry[1][0].abs_multiply(entry[0][1]).abs_multiply(entry[2][2]).abs_multiply(entry[3][3])).abs_add
			    (entry[1][0].abs_multiply(entry[0][1]).abs_multiply(entry[2][3]).abs_multiply(entry[3][2])).abs_add
			    (entry[1][0].abs_multiply(entry[2][1]).abs_multiply(entry[0][2]).abs_multiply(entry[3][3])).abs_subtract
			    (entry[1][0].abs_multiply(entry[2][1]).abs_multiply(entry[0][3]).abs_multiply(entry[3][2])).abs_subtract
			    (entry[1][0].abs_multiply(entry[3][1]).abs_multiply(entry[0][2]).abs_multiply(entry[2][3])).abs_add
			    (entry[1][0].abs_multiply(entry[3][1]).abs_multiply(entry[0][3]).abs_multiply(entry[2][2])).abs_add
			    (entry[2][0].abs_multiply(entry[0][1]).abs_multiply(entry[1][2]).abs_multiply(entry[3][3])).abs_subtract
			    (entry[2][0].abs_multiply(entry[0][1]).abs_multiply(entry[1][3]).abs_multiply(entry[3][2])).abs_subtract
			    (entry[2][0].abs_multiply(entry[1][1]).abs_multiply(entry[0][2]).abs_multiply(entry[3][3])).abs_add
			    (entry[2][0].abs_multiply(entry[1][1]).abs_multiply(entry[0][3]).abs_multiply(entry[3][2])).abs_add
			    (entry[2][0].abs_multiply(entry[3][1]).abs_multiply(entry[0][2]).abs_multiply(entry[1][3])).abs_subtract
			    (entry[2][0].abs_multiply(entry[3][1]).abs_multiply(entry[0][3]).abs_multiply(entry[1][2])).abs_subtract
			    (entry[3][0].abs_multiply(entry[0][1]).abs_multiply(entry[1][2]).abs_multiply(entry[2][3])).abs_add
			    (entry[3][0].abs_multiply(entry[0][1]).abs_multiply(entry[1][3]).abs_multiply(entry[2][2])).abs_add
			    (entry[3][0].abs_multiply(entry[1][1]).abs_multiply(entry[0][2]).abs_multiply(entry[2][3])).abs_subtract
			    (entry[3][0].abs_multiply(entry[1][1]).abs_multiply(entry[0][3]).abs_multiply(entry[2][2])).abs_subtract
			    (entry[3][0].abs_multiply(entry[2][1]).abs_multiply(entry[0][2]).abs_multiply(entry[1][3])).abs_add
			    (entry[3][0].abs_multiply(entry[2][1]).abs_multiply(entry[0][3]).abs_multiply(entry[1][2]))
				);

	
	erg.entry[0][0] = ((Field)(
			    (entry[1][1].abs_multiply(entry[2][2]).abs_multiply(entry[3][3])).abs_subtract
				(entry[1][1].abs_multiply(entry[2][3]).abs_multiply(entry[3][2])).abs_subtract
				(entry[2][1].abs_multiply(entry[1][3]).abs_multiply(entry[3][2])).abs_add
				(entry[2][1].abs_multiply(entry[1][3]).abs_multiply(entry[3][2])).abs_add
				(entry[3][1].abs_multiply(entry[1][2]).abs_multiply(entry[2][3])).abs_subtract
				(entry[3][1].abs_multiply(entry[1][3]).abs_multiply(entry[2][2]))
				)).abs_divide(det);
	erg.entry[0][1] = ((Field)(
			    (entry[0][1].abs_multiply(entry[2][3]).abs_multiply(entry[3][2])).abs_subtract
				(entry[0][1].abs_multiply(entry[2][2]).abs_multiply(entry[3][3])).abs_add
				(entry[2][1].abs_multiply(entry[0][2]).abs_multiply(entry[3][3])).abs_subtract
				(entry[2][1].abs_multiply(entry[0][3]).abs_multiply(entry[3][2])).abs_subtract
				(entry[3][1].abs_multiply(entry[0][2]).abs_multiply(entry[3][3])).abs_add
				(entry[3][1].abs_multiply(entry[0][3]).abs_multiply(entry[2][2]))
				)).abs_divide(det);
	erg.entry[0][2] = ((Field)(
			    (entry[0][1].abs_multiply(entry[1][2]).abs_multiply(entry[3][3])).abs_subtract
				(entry[0][1].abs_multiply(entry[1][3]).abs_multiply(entry[3][2])).abs_subtract
				(entry[1][1].abs_multiply(entry[0][2]).abs_multiply(entry[3][3])).abs_add
				(entry[1][1].abs_multiply(entry[0][3]).abs_multiply(entry[3][2])).abs_add
				(entry[3][1].abs_multiply(entry[0][2]).abs_multiply(entry[1][3])).abs_subtract
				(entry[3][1].abs_multiply(entry[0][3]).abs_multiply(entry[1][2]))
				)).abs_divide(det);
	erg.entry[0][3] = ((Field)(
			    (entry[0][1].abs_multiply(entry[1][3]).abs_multiply(entry[2][2])).abs_subtract
				(entry[0][1].abs_multiply(entry[1][2]).abs_multiply(entry[2][3])).abs_add
				(entry[1][1].abs_multiply(entry[0][2]).abs_multiply(entry[2][3])).abs_subtract
				(entry[1][1].abs_multiply(entry[0][3]).abs_multiply(entry[2][2])).abs_subtract
				(entry[2][1].abs_multiply(entry[0][2]).abs_multiply(entry[1][3])).abs_add
				(entry[2][1].abs_multiply(entry[0][3]).abs_multiply(entry[1][2]))
				)).abs_divide(det);
	erg.entry[1][0] = ((Field)(
			    (entry[1][0].abs_multiply(entry[2][3]).abs_multiply(entry[3][2])).abs_subtract
				(entry[1][0].abs_multiply(entry[2][2]).abs_multiply(entry[3][3])).abs_add
				(entry[2][0].abs_multiply(entry[1][2]).abs_multiply(entry[3][3])).abs_subtract
				(entry[2][0].abs_multiply(entry[1][3]).abs_multiply(entry[3][2])).abs_subtract
				(entry[3][0].abs_multiply(entry[1][2]).abs_multiply(entry[2][3])).abs_add
				(entry[3][0].abs_multiply(entry[1][3]).abs_multiply(entry[2][2]))
				)).abs_divide(det);
	erg.entry[1][1] = ((Field)(
			    (entry[0][0].abs_multiply(entry[2][2]).abs_multiply(entry[3][3])).abs_subtract
				(entry[0][0].abs_multiply(entry[2][3]).abs_multiply(entry[3][2])).abs_subtract
				(entry[2][0].abs_multiply(entry[0][2]).abs_multiply(entry[3][3])).abs_add
				(entry[2][0].abs_multiply(entry[0][3]).abs_multiply(entry[3][2])).abs_add
				(entry[3][0].abs_multiply(entry[0][2]).abs_multiply(entry[2][3])).abs_subtract
				(entry[3][0].abs_multiply(entry[0][3]).abs_multiply(entry[2][2]))
				)).abs_divide(det);
	erg.entry[1][2] = ((Field)(
			    (entry[0][0].abs_multiply(entry[1][3]).abs_multiply(entry[3][2])).abs_subtract
				(entry[0][0].abs_multiply(entry[1][2]).abs_multiply(entry[3][3])).abs_add
				(entry[1][0].abs_multiply(entry[0][2]).abs_multiply(entry[3][3])).abs_subtract
				(entry[1][0].abs_multiply(entry[0][3]).abs_multiply(entry[3][2])).abs_subtract
				(entry[3][0].abs_multiply(entry[0][2]).abs_multiply(entry[1][3])).abs_add
				(entry[3][0].abs_multiply(entry[0][3]).abs_multiply(entry[3][2]))
				)).abs_divide(det);
	erg.entry[1][3] = ((Field)(
			    (entry[0][0].abs_multiply(entry[1][2]).abs_multiply(entry[2][3])).abs_subtract
				(entry[0][0].abs_multiply(entry[1][3]).abs_multiply(entry[2][2])).abs_subtract
				(entry[1][0].abs_multiply(entry[0][2]).abs_multiply(entry[2][3])).abs_add
				(entry[1][0].abs_multiply(entry[0][3]).abs_multiply(entry[2][2])).abs_add
				(entry[2][0].abs_multiply(entry[0][2]).abs_multiply(entry[1][3])).abs_subtract
				(entry[2][0].abs_multiply(entry[0][3]).abs_multiply(entry[1][2]))
				)).abs_divide(det);
	erg.entry[2][0] = ((Field)(
			    (entry[1][0].abs_multiply(entry[2][1]).abs_multiply(entry[3][3])).abs_subtract
				(entry[1][0].abs_multiply(entry[2][3]).abs_multiply(entry[3][1])).abs_subtract
				(entry[2][0].abs_multiply(entry[1][1]).abs_multiply(entry[3][3])).abs_add
				(entry[2][0].abs_multiply(entry[1][3]).abs_multiply(entry[3][1])).abs_add
				(entry[3][0].abs_multiply(entry[1][1]).abs_multiply(entry[2][3])).abs_subtract
				(entry[3][0].abs_multiply(entry[1][3]).abs_multiply(entry[2][1]))
				)).abs_divide(det);
	erg.entry[2][1] = ((Field)(
			    (entry[0][0].abs_multiply(entry[2][3]).abs_multiply(entry[3][1])).abs_subtract
				(entry[0][0].abs_multiply(entry[2][1]).abs_multiply(entry[3][3])).abs_add
				(entry[2][0].abs_multiply(entry[0][1]).abs_multiply(entry[3][3])).abs_subtract
				(entry[2][0].abs_multiply(entry[0][3]).abs_multiply(entry[3][1])).abs_subtract
				(entry[3][0].abs_multiply(entry[0][1]).abs_multiply(entry[2][3])).abs_add
				(entry[3][0].abs_multiply(entry[0][3]).abs_multiply(entry[2][1]))
				)).abs_divide(det);
	erg.entry[2][2] = ((Field)(
			    (entry[0][0].abs_multiply(entry[1][1]).abs_multiply(entry[3][3])).abs_subtract
				(entry[0][0].abs_multiply(entry[1][3]).abs_multiply(entry[3][1])).abs_subtract
				(entry[1][0].abs_multiply(entry[0][1]).abs_multiply(entry[3][3])).abs_add
				(entry[1][0].abs_multiply(entry[0][3]).abs_multiply(entry[3][1])).abs_add
				(entry[3][0].abs_multiply(entry[0][1]).abs_multiply(entry[1][3])).abs_subtract
				(entry[3][0].abs_multiply(entry[0][3]).abs_multiply(entry[1][1]))
				)).abs_divide(det);
	erg.entry[2][3] = ((Field)(
			    (entry[0][0].abs_multiply(entry[1][3]).abs_multiply(entry[2][1])).abs_subtract
				(entry[0][0].abs_multiply(entry[1][1]).abs_multiply(entry[2][3])).abs_add
				(entry[1][0].abs_multiply(entry[0][1]).abs_multiply(entry[2][3])).abs_subtract
				(entry[1][0].abs_multiply(entry[0][3]).abs_multiply(entry[2][1])).abs_subtract
				(entry[2][0].abs_multiply(entry[0][1]).abs_multiply(entry[1][3])).abs_add
				(entry[2][0].abs_multiply(entry[0][3]).abs_multiply(entry[1][1]))
				)).abs_divide(det);
	erg.entry[3][0] = ((Field)(
			    (entry[1][0].abs_multiply(entry[2][2]).abs_multiply(entry[3][1])).abs_subtract
				(entry[1][0].abs_multiply(entry[2][1]).abs_multiply(entry[3][2])).abs_add
				(entry[2][0].abs_multiply(entry[1][1]).abs_multiply(entry[3][2])).abs_subtract
				(entry[2][0].abs_multiply(entry[1][2]).abs_multiply(entry[3][1])).abs_subtract
				(entry[3][0].abs_multiply(entry[1][1]).abs_multiply(entry[2][2])).abs_add
				(entry[3][0].abs_multiply(entry[1][2]).abs_multiply(entry[2][1]))
				)).abs_divide(det);
	erg.entry[3][1] = ((Field)(
			    (entry[0][0].abs_multiply(entry[2][1]).abs_multiply(entry[3][2])).abs_subtract
				(entry[0][0].abs_multiply(entry[2][2]).abs_multiply(entry[3][1])).abs_subtract
				(entry[2][0].abs_multiply(entry[0][1]).abs_multiply(entry[3][2])).abs_add
				(entry[2][0].abs_multiply(entry[0][2]).abs_multiply(entry[3][1])).abs_add
				(entry[3][0].abs_multiply(entry[0][1]).abs_multiply(entry[2][2])).abs_subtract
				(entry[3][0].abs_multiply(entry[0][2]).abs_multiply(entry[2][1]))
				)).abs_divide(det);
	erg.entry[3][2] = ((Field)(
			    (entry[0][0].abs_multiply(entry[1][2]).abs_multiply(entry[3][1])).abs_subtract
				(entry[0][0].abs_multiply(entry[1][1]).abs_multiply(entry[3][2])).abs_add
				(entry[1][0].abs_multiply(entry[0][1]).abs_multiply(entry[3][2])).abs_subtract
				(entry[1][0].abs_multiply(entry[0][2]).abs_multiply(entry[3][1])).abs_subtract
				(entry[3][0].abs_multiply(entry[0][1]).abs_multiply(entry[1][2])).abs_add
				(entry[3][0].abs_multiply(entry[0][2]).abs_multiply(entry[1][1]))
				)).abs_divide(det);
	erg.entry[3][3] = ((Field)(
			    (entry[0][0].abs_multiply(entry[1][1]).abs_multiply(entry[2][2])).abs_subtract
				(entry[0][0].abs_multiply(entry[1][2]).abs_multiply(entry[2][1])).abs_subtract
				(entry[1][0].abs_multiply(entry[0][1]).abs_multiply(entry[2][2])).abs_add
				(entry[1][0].abs_multiply(entry[0][2]).abs_multiply(entry[2][1])).abs_add
				(entry[2][0].abs_multiply(entry[0][1]).abs_multiply(entry[1][2])).abs_subtract
				(entry[2][0].abs_multiply(entry[0][2]).abs_multiply(entry[1][1]))
				)).abs_divide(det);
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (28.03.2003 15:10:42)
 * @return boolean
 */
public boolean isPositiveDefinite() 
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);
	
	if (!(field instanceof Signed)) 
		throw new RuntimeException("Cannot test on positive Definiteness (needs signed Ring)");
	if (spalten != zeilen) return false;

	for (int i=spalten; i>0; i--)
	{
		RingMatrix m = subMatrix(0,i-1,0,i);
		Signed det = (Signed)m.determinant();
		if (det.abs_signum()==-1) return false;
	}
	return true;
}
/**

	Christian Klein

 * Liefert die Position des ersten Eintrages != 0 in der i-ten Zeile, -1, wenn die Zeile 0 ist
 */
public int isRowZero(int i)
{
  for (int j = 0; j < getColumns(); j++)
    if ( !(entry[i][j].abs_isEqual(entry[i][j].abs_zero())) )
       return j;
  return -1;
}

public boolean isSymetric()
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	for (int i=1; i<=zeilen; i++)
		for (int j=1; j<i; j++)
			if (!getValue(i,j).abs_isEqual(getValue(j,i))) return false;
	return true;
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
// returns the sum of the absolute value of all matrix entries
public Ring oneNorm()
{
	Ring erg = entry[0][0].abs_zero();
	if (!(erg instanceof Signed)) 
		throw new RuntimeException ("Not valid Ring for infinityNorm (needs Signed)");
	
	for(int i=0; i<entry.length; i++)
		for(int j=0; j<entry[0].length; j++)
			erg = erg.abs_add( ((Signed)entry[i][j]).abs_abs() );
	
	return erg;	
}
/**
 * Insert the method's description here.
 * Creation date: (04.08.2004 14:53:05)
 * @return arithmetik.RingMatrix

	Orthonormalisisert die Spaltenvektoren
 
 */
public RingMatrix orthonormalize() 
{
	int zeilen = this.getRows(), spalten = this.getColumns();
	RingVector[] in = new RingVector[spalten];
	for (int i=1; i<=in.length; i++) in[i-1] = this.getColumn(i);
	RingVector[] out = RingVector.orthonormalize(in);
	RingMatrix erg = new RingMatrix(out);
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
		throw new RuntimeException("Not valid Ring for QRPartition (needs field, squarrootable, signed)");
	
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
// QR-Zerlegung einer Matrix in Hessenberggestalt durch Givens-Rotationen
public RingMatrix[] qRDecompositionOfHessenberg()
{
	int spalten = this.getColumns();
	int zeilen  = this.getRows();
	Ring field  = this.getValue(1,1);
	Ring zwei   = field.abs_unit().abs_add(field.abs_unit());
	
	if (!(field instanceof Squarerootable)) throw new RuntimeException("QRDecomposition needs Squarerootable.");
	
	RingMatrix Qtrans = this.unit();
	RingMatrix R = new RingMatrix(this);
	for (int i=1; i<=zeilen-1; i++)
	{
		Field abs;
		if (field instanceof Complex)
			abs = (Field)((Squarerootable)((R.getValue(i,i).abs_multiply((Ring)((Complex)R.getValue(i,i)).conjugate())).abs_add
					                  (R.getValue(i+1,i).abs_multiply((Ring)((Complex)R.getValue(i+1,i)).conjugate())) )).abs_sqrt();
		else
			abs = (Field)((Squarerootable)((R.getValue(i,i).abs_multiply(R.getValue(i,i))).abs_add
					                  (R.getValue(i+1,i).abs_multiply(R.getValue(i+1,i))) )).abs_sqrt();
		Field c = ((Field)R.getValue(i,i)).abs_divide(abs);
		Field s = ((Field)R.getValue(i+1,i)).abs_divide(abs);
		
		RingMatrix altQ = new RingMatrix(Qtrans);
		RingMatrix altR = new RingMatrix(R);
		for(int z=i;z<=i+1;z++)
			for(int j=1;j<=spalten;j++)
			{
				Field val1 = c; Field val2 = s;
				if (z==i+1) 
				{
					val1 = (Field)s.abs_negate(); val2 = c;
					if (field instanceof Complex) {val1 = (Field)((Complex)val1).conjugate(); val2 = (Field)((Complex)val2).conjugate();}
				}
				Field sum = (Field)altQ.getValue(i,j).abs_multiply(val1).abs_add(altQ.getValue(i+1,j).abs_multiply(val2));
				Qtrans.setValue(sum,z,j);
				sum = (Field)altR.getValue(i,j).abs_multiply(val1).abs_add(altR.getValue(i+1,j).abs_multiply(val2));
				R.setValue(sum,z,j);
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
public void setValue(Ring el, int row, int col)
{
	entry[row-1][col-1] = el;
}
/**
 * Insert the method's description here.
 * Creation date: (30.07.2004 09:34:03)
 * @return arithmetik.RingMatrix[]
 */
public RingMatrix[] singularValueDecomposition(double eps) 
{
	int zeilen = this.getRows();
	int spalten = this.getColumns();
	RingMatrix A = this;
	if (zeilen < spalten) A = A.transpose();
	RingMatrix AA = A.transpose().matrixMultiply(A);

	System.out.println(AA);
	int dim = AA.getRows();
	RingMatrix V = AA.unit();
	RingVector sigma = AA.eigenvalues(eps,V);
	RingMatrix Sigma = AA.unit();
	for (int i=0; i<dim; i++) 
	{
		Ring sv = sigma.getValue(i+1);
		double dn = ((DoubleNormable)sv).doubleNorm();
		if (dn*dn < eps) Sigma.setValue(sv.abs_zero(),i+1,i+1); 
		else Sigma.setValue((Ring)((Squarerootable)sv).abs_sqrt(),i+1,i+1);
	}
	RingMatrix U = A.matrixMultiply(V);
	U = U.orthonormalize();

	if (A==this) return new RingMatrix[]{U,Sigma,V};
	else return new RingMatrix[]{V.transpose(),Sigma,U.transpose()};
}
/**
 * Insert the method's description here.
 * Creation date: (30.07.2004 09:34:03)
 * @return arithmetik.RingMatrix[]

	Arbeitet statt mit A^T A mit | 0   A |
								 | A^T O |
 
 */
public RingMatrix[] singularValueDecomposition2(double eps) 
{
	Ring eins = getValue(1,1);
	Ring wurzelzwei = (Ring)((Squarerootable)eins.abs_add(eins)).abs_sqrt();
	int zeilen = this.getRows();
	int spalten = this.getColumns();
	RingMatrix A = this;
	if (zeilen < spalten) A = A.transpose();
	int zeilenA = A.getRows();
	int spaltenA = A.getColumns();
	RingMatrix AA = new RingMatrix(eins.abs_zero(),zeilen+spalten,zeilen+spalten);
	for (int i=1; i<=zeilenA; i++)
		for (int j=1; j<=spaltenA; j++) 
		{
			Ring v = A.getValue(i,j);
			AA.setValue(v,i,j+zeilenA);
			AA.setValue(v,j+zeilenA,i);
		}
	
	System.out.println(AA);
	int dim = AA.getRows();
	RingMatrix EV = AA.unit();
	RingVector sigma = AA.eigenvalues(eps,EV);
	System.out.println("Eigenvektoren: \r\n"+EV);
	RingMatrix U = new RingMatrix(eins,zeilenA,spaltenA);
	RingMatrix Sigma = new RingMatrix(eins,spaltenA,spaltenA);
	RingMatrix V = new RingMatrix(eins,spaltenA,spaltenA);

	Vector uvecs = new Vector();
	Vector vvecs = new Vector();
	
	for (int i=0; i<dim; i++) 
	{
		Ring sv = sigma.getValue(i+1);
		double dn = ((DoubleCastable)sv).doubleValue();
		if ((dn*dn > eps) && (dn > 0.0))
		{
			RingVector uvec = new RingVector(eins,zeilenA);
			RingVector vvec = new RingVector(eins,spaltenA);
			for (int j=1; j<=zeilenA; j++) uvec.setValue(EV.getValue(j,i+1).abs_multiply(wurzelzwei),j);
			for (int j=zeilenA+1; j<=dim; j++) vvec.setValue(EV.getValue(j,i+1).abs_multiply(wurzelzwei),j-zeilenA);
			Sigma.setValue(sv,uvecs.size()+1,uvecs.size()+1);
			uvecs.addElement(uvec);
			vvecs.addElement(vvec);
		}
	}
	RingVector[] uvecsarr = new RingVector[uvecs.size()]; for (int i=0; i<uvecsarr.length; i++) uvecsarr[i] = (RingVector)uvecs.elementAt(i);
	RingVector[] vvecsarr = new RingVector[vvecs.size()]; for (int i=0; i<vvecsarr.length; i++) vvecsarr[i] = (RingVector)vvecs.elementAt(i);
	RingVector[] uvecsorth = RingVector.orthonormalize(uvecsarr,spaltenA);
	RingVector[] vvecsorth = RingVector.orthonormalize(vvecsarr,spaltenA);
	U = new RingMatrix(uvecsorth);
	V = new RingMatrix(vvecsorth);

	if (A==this) return new RingMatrix[]{U,Sigma,V};
	else return new RingMatrix[]{V.transpose(),Sigma,U.transpose()};
}
/* ------------------------------ C.K. ------------------------------ */

/**
 * Loest ein LGS this*x=b, indem immer mehr Gleichungen beruecksichtigt werden, bis das LGS vollen
 * Rang hat.
 * Ausserdem wird eine Liste der benoetigten Gleichungen zurueckgegeben.
 * Aufruf mit solveIter(b,rg,needed)
 * Wobei this,b das LGS repraesentieren, needed enthaellt am Ende
 * die Liste der benoetigten Gleichungen. Hat das System keine eindeutige Loesung, so wird NULL
 * zurueckgeliefert. (Ebenfalls, wenn die Matrix keine Elemente vom Typ FIELD enthaellt.
 * !!! Zerstoert die Matrix und den Vektor b !!!
 */
public Field[] solveIter(Field[] b, int[] needed)
{

	Ring field = getValue(1,1);
	if (!(field instanceof Field)) return null;
	Field zero = ((Field) field.abs_zero());

	int rng = 0;
	// Es werden nicht mehr als min(#zeilen,#spalten) Gleichungen benoetigt!
	int fullrng = getColumns();
	if (getRows() < getColumns()) return null;
	// An welcher Position steht das erste von Null verschiedene Element in der Zeile?
	int[] fc = new int[fullrng];
	for (int i=0; i<fullrng; i++) needed[i]=-1;
	// Gehe durch alle Zeilen, bis LGS vollen Rang hat oder alle Zeilen beruecksichtig wurden.
	int ndptr = 0;
	for (int i=0; (i<getRows() &&  rng < fullrng); i++)
	{
		// Versuche die aktuelle Zeile auf 0 zu bringen, indem mit allen bisherigen Zeilen abziehen
//			System.out.println("Betrachte Zeile : " + i + "(ndptr = "+ndptr+")");
		for (int j=0 ; j < ndptr ; j++)
		{
			// Wir muessen nur mit einer bisherigen Zeile verwursteln, wenn an der Stelle noch keine 0 steht.
//                        System.out.print("Pruefe Elt. "+fc[j]+" fuer Zeile "+ needed[j] +" : ");
			Field pivot = (Field)entry[i][fc[j]];
//                      System.out.println(pivot);
			if (!(pivot.abs_isEqual(zero)))
			{
				Field fac = pivot.abs_divide((Field)entry[needed[j]][fc[j]]);
				// Ziehe die needed[j]-te Zeile von der aktuellen Zeile ab.
				subtractRow(i,needed[j],fac);
//					System.out.println("Zeile: " +i + " - " + fac + "*" + needed[j]);
				// Noch b anpassen...
				b[i]=(Field)b[i].abs_subtract(fac.abs_multiply(b[needed[j]]));
//					System.out.println("b : " + i + " - " + fac + "*" + needed[j]);
//					System.out.print(i + "th Row afterwards: ");
//					for (int tmp = 0; tmp < entry[i].length ; tmp++)
//						System.out.print(" " + entry[i][tmp] + " |");
//					System.out.println("| " + b[i]);
			}
		}
		// Die Zeile wird nur dann beruecksichtigt, wenn sie nun ungleich Null ist...
		// Wir suchen das erste Element != 0
		int first = isRowZero(i);
		if (first > -1)
		{
			fc[ndptr]=first;
//                      System.out.println("Zeile "+ i + " ist ungleich null an Stelle " + first);
//                      System.out.println(" Momentane Matrix ist : \n" + this);
//                      System.out.println("-----------------------------------------");
			needed[ndptr]=i;
			ndptr++;
			rng++;
			// Ziehe die Zeile nun noch von allen bisherigen ab...
			for (int j=0 ; j <ndptr-1 ; j++)
			{
				Field subpiv = (Field)entry[i][first];
				Field pivot = (Field)entry[needed[j]][first];
				if (!(pivot.abs_isEqual(zero)))
				{
					Field fac = pivot.abs_divide(subpiv);
					// Ziehe die aktuelle Zeile von der needed[j]-te Zeile ab.
					subtractRow(needed[j],i,fac);
//						System.out.println("Zeile: " +needed[j] + " - " + fac + "*" + i);
					// Noch b anpassen...
					b[needed[j]]=(Field)b[needed[j]].abs_subtract(fac.abs_multiply(b[i]));
//						System.out.println("b : " + needed[j] + " - " + fac + "*" + i);
//						System.out.print(needed[j] + "th Row afterwards: ");
//						for (int tmp = 0; tmp < entry[needed[j]].length ; tmp++)
//							System.out.print(" " + entry[needed[j]][tmp] + " |");
//						System.out.println("| " + b[needed[j]]);

				}
			}
		}
	}
	if (rng < fullrng) return null; // Es gab nicht genug unabhaengige Gleichungen...
	// Nun muessen wir nur noch die Loesung zusammenbasteln...
	// Die Matrix hat bereits "permutierte" Diagonalgestalt!
	Field[] erg = new Field[fullrng];
//        System.out.println(" Fertige Matrix (Rang "+rng+") : \n" + this);
//        System.out.print( " b : " );
//        for (int i = 0 ; i < b.length; i++)
//          System.out.print(" :: " + b[i]);
//        System.out.println();

	for (int i=0; i < fullrng ; i++)
	{
		int row = needed[i];
		int col = fc[i];
//		System.out.println("1. Lsg : b["+row+"]/A["+row+"]["+col+"]");
//		System.out.println(" -- " + b[row] + " -- " + entry[row][col]);
		erg[col]= b[row].abs_divide((Field)entry[row][col]);
	}
	return erg;
}
// Setzt bei Freiheiten immer 0 ein.
public RingVector solveRightUpperTriangleMatrix(RingVector b)
{
	return solveRightUpperTriangleMatrix(b, -1);
}
// Erwaretet oberer rechte Dreiecksmatrix (d.h. jede Zeile hat mindestens eine führende Null
// mehr als die darüberliegenden). Setzt bei Freiheiten immer 0 ein, außer bei useFree, dort 1.
// Gibt null zurück, falls keine Lösung existiert.

// Die Methode läuft nicht richtig, wenn die oben genannte Bedingung (anstieg der führenden Nullen) 
// nicht streng erfüllt ist.

public RingVector solveRightUpperTriangleMatrix(RingVector b, int useFree)
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);
	int freiheit = 0;
	int schonBelegt = 0;
	int gewaehlteFreiheiten = 0;
	RingVector erg = new RingVector(field, this.getColumns());
	
	for (int z=zeilen; z>=1; z--)
	{
		int anznullen = z-1;					// alles links davon gehen wir aus, dass es null ist.
		while ((anznullen<spalten) && (getValue(z,anznullen+1).abs_isEqual(field.abs_zero()))) anznullen++;
		if ((anznullen==spalten) && (!b.getValue(z).abs_isEqual(field.abs_zero()))) return null;

		if (anznullen < spalten)
		{
			for (int i=spalten-schonBelegt; i>anznullen+1; i--) 
			{
				if (useFree == gewaehlteFreiheiten) erg.setValue(field.abs_unit(), spalten-schonBelegt);
				else erg.setValue(field.abs_zero(), spalten-schonBelegt);
				schonBelegt++;
				gewaehlteFreiheiten++;
			}
			Ring sum = field.abs_zero();
			for (int i=anznullen+2; i<=spalten; i++)
				sum = sum.abs_add(erg.getValue(i).abs_multiply(getValue(z, i)));
			erg.setValue(((Field)b.getValue(z).abs_subtract(sum)).abs_divide((Field)getValue(z,anznullen+1)),anznullen+1);
			schonBelegt++;
		}
	}
	return erg;
}
// Solves the equation system with the cg-method. If print is not zero, progress is 
// reported to it. If epsilon is not zero, b must contain DoubleCastables, and the progress
// is terminated if x changes less than epsilon in infinitynorm. if preconditioning is true,
// the cg-method will be done with the diagonal of *this* as preconditioning.
public RingVector solveWithCGMethod(RingVector b, double epsilon, boolean preconditioning, Printable print)
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);
	
	if ((epsilon>0.0) && (!(field instanceof DoubleCastable))) return null;
	
	RingMatrix B = null;
	if (preconditioning)
	{
		B = new RingMatrix(field,zeilen,spalten);
		for (int i=1; i<=spalten; i++)
			B.setValue(((Field)this.getValue(i,i)).abs_reciprocal(),i,i);
	}
	
	Field alpha = (Field)field.abs_zero();
	Field beta = (Field)field.abs_zero();
	
	RingVector x = new RingVector(field, spalten);
	for (int i=1; i<=spalten; i++)
		x.setValue(field.abs_zero(), i);
	
	RingVector r = new RingVector(b.negate());
	RingVector w;
	if (preconditioning) w = new RingVector(B.matrixMultiply(r));
	else w = r;
	RingVector s = new RingVector(w);
	Field oldrsqr = (Field)r.scalarProduct(w);
	
	boolean stop = false; int n = 0;
	while (!stop)
	{
		n++;
		RingVector As = new RingVector (this.matrixMultiply(s));
		alpha = ((Field)r.scalarProduct(w)).abs_divide( (Field)As.scalarProduct(s) );
		RingVector oldX = new RingVector(x);
		x = new RingVector(x.subtract(s.scalarMultiply(alpha)));
		r = new RingVector(r.subtract(As.scalarMultiply(alpha)));
		if (preconditioning) w = new RingVector(B.matrixMultiply(r));
		else w = r;
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
			print.println("Step "+n+", x = "+x+", dist = "+dist);
		}
	}
	return x;
}
public RingVector solveWithGauss(RingVector b)
{
	RingMatrix[] zw = this.findRMatrix(b);
	return zw[0].solveRightUpperTriangleMatrix((RingVector)zw[1]);
}
// 0 : Kein Pivoting
// 1 : Spaltenpivoting
// 2 : volles Pivoting
public RingVector solveWithGauss(RingVector b, int strat)
{
	int strategy = strat;				// could be altered if no orderd ring.
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	if ((spalten==0) || (zeilen==0)) return new RingVector(new Qelement(), 0);
	Ring field = this.getValue(1,1);
	
	if (!(field instanceof Field)) 
		throw new RuntimeException("Not valid Ring for Gauss (needs Field)");
	
	if (spalten != zeilen)
		throw new RuntimeException("Matrix must be quadric.");

	if (!(field instanceof Signed))
		strategy = 0;
		
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
/**
 * Zieht fac*Spalte subrow von der Spalte workrow ab
 */
public void subtractRow(int workrow,int subrow,Ring fac)
{
	for (int i=0; i < getColumns(); i++)
		// Sollte man nur fuer entry != 0 machen....
		entry[workrow][i] = entry[workrow][i].abs_subtract(fac.abs_multiply(entry[subrow][i]));
}
/**
 * Insert the method's description here.
 * Creation date: (14.12.2002 16:37:29)
 * @return double
 */
public double toDouble() 
{
	if ((entry.length!=1) || (entry[0].length!=1)) throw new RuntimeException("Matrix cast to double failed since matrix has more than one Element");
	return ((DoubleCastable)entry[0][0]).doubleValue();
}
public String toDoubleString()
{
	boolean reel = (entry[0][0] instanceof DoubleCastable);
	boolean complex = (entry[0][0] instanceof Complex);
	if (!(reel || complex)) 
		throw new RuntimeException ("Method toDoubleString must have DoubleCastable or Complex as Ring");
	
	String erg = "[";
	for(int i=0;i<entry.length;i++)
	{
		erg+="[";
		for(int j=0;j<entry[0].length;j++)
		{
			if (reel) erg+=((DoubleCastable)entry[i][j]).doubleValue();
			if (complex)
			{
				erg+=((Complex)entry[i][j]).reelValue();
				double v = ((Complex)entry[i][j]).imagValue();
				if (v!=0.0) erg += "+"+v+"*I";
			}
			if(j<entry[0].length-1)
				erg+=",";
		}
		erg+="]";
		if(i<entry.length-1)
			erg+=",";		
	}
	erg+="]";
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
public String toString()
{
	String erg = "(";
	for(int i=0;i<entry.length;i++)
	{
		erg+="[";
		for(int j=0;j<entry[0].length;j++)
		{
			erg+=entry[i][j].toString();
			if(j<entry[0].length-1)
				erg+=",";
		}
		erg+="]";
		if(i<entry.length-1)
			erg+=",\r\n";		
	}
	erg+=")";
	return erg;
}
/**
 * Insert the method's description here.
 * Creation date: (13.12.2002 22:56:48)
 * @return arithmetik.Ring
 */
public Ring trace() 
{
	Ring erg = entry[0][0].abs_zero();
	for (int i=1; i<=getRows(); i++)
		erg = erg.abs_add(getValue(i,i));
	return erg;
}
// Benötigt ein Feld mit Quadratwurzeln.
// Transformiert mit Orthogonalen Matrizen (Householder) in die Hessenbergform, d.h. 
// eine rechte obere Dreiecksmatrix, in der die Elemente unterhalb der Diagonalen 
// noch besetzt sind. Eventuell gegebene Symetrie bleibt erhalten.
public RingMatrix transformSimilarToHessenberg()
{
	return transformSimilarToHessenberg(null);
}
// Benötigt ein Feld mit Quadratwurzeln.
// Transformiert mit Orthogonalen Matrizen (Householder) in die Hessenbergform, d.h. 
// eine rechte obere Dreiecksmatrix, in der die Elemente unterhalb der Diagonalen 
// noch besetzt sind. Eventuell gegebene Symetrie bleibt erhalten.
// Ist transformationMatrix nicht null, wird die Transformation hier gemerkt.
public RingMatrix transformSimilarToHessenberg(RingMatrix transformationMatrix)
{
	int spalten = this.getColumns();
	int zeilen = this.getRows();
	Ring field = this.getValue(1,1);
	Ring zwei = field.abs_unit().abs_add(field.abs_unit());
	
	if (!(field instanceof Squarerootable)) throw new RuntimeException("transformSimilarToHessenberg must be called with squarerootable Ring!");
	
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
			if (transformationMatrix!=null) 
			{
				RingMatrix zw = transformationMatrix.matrixMultiply(U);
				for (int i=1; i<=zeilen; i++)
					for (int j=1; j<=spalten; j++)
						transformationMatrix.setValue(zw.getValue(i,j),i,j);
			}
		}
	}
	return erg;
}
// Transponiert die Matrix; falls die Einträge Complex implementieren, werden sie konjugiert.
public RingMatrix transpose()
{
	RingMatrix erg = new RingMatrix(entry[0][0],getColumns(),getRows());
	boolean complex = (entry[0][0] instanceof Complex);
	for(int i=0;i<entry.length;i++)
		for(int k=0;k<entry[0].length;k++)
		{
			if (complex) erg.entry[k][i] = (Ring)((Complex)entry[i][k]).conjugate();
			else erg.entry[k][i]=entry[i][k];
		}
	return erg;
}
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
/**
 * Insert the method's description here.
 * Creation date: (08.05.2003 14:50:07)
 * @return arithmetik.RingMatrix
 * @param ringtype arithmetik.Ring
 * @param dim int
 */
public static RingMatrix unit(Ring ringtype, int dim) 
{
	RingMatrix erg = new RingMatrix(ringtype, dim);
	for (int i=0; i<dim; i++) erg.entry[i][i] = ringtype.abs_unit();
	return erg;
}
public RingVector vectorMultiply(RingVector b)
{
	return new RingVector(this.matrixMultiply(b));
}
public RingMatrix zero()
{
	return new RingMatrix(entry[0][0],entry.length,entry[0].length);
}
}
