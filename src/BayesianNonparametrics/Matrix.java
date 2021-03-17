package BayesianNonparametrics;

/*
 * The routines det() inv() and gaussian() in this class are from Tao Pang (2006):
 * http://www.physics.unlv.edu/~pang/comp4/Det.java
 * http://www.physics.unlv.edu/~pang/comp4/Inverse.java
 */

public class Matrix {
	
	double[][] m;	
	double[][] backup;
	int nDims;
	
	public Matrix(double[][] m) {
		nDims = m.length;
		this.m = createCopyOfMatrix(m);
		backup = createCopyOfMatrix(m);
	}
	
	public Matrix(int nDims) {
		this.nDims = nDims;
		this.m = new double[nDims][nDims];
		backup = new double[nDims][nDims];
	}
	
	private double[][] createCopyOfMatrix(double[][] m){
		int nDims = m.length;
		double[][] temp = new double[nDims][nDims];
		for(int i = 0; i < nDims; i++)
			for(int j = 0; j < nDims; j++)
				temp[i][j] = m[i][j];
		return temp;
	}
	
	public void setTo(double[][] m) {
		for(int i = 0; i < nDims;i++)
			for(int j = 0; j < nDims;j++)
				this.m[i][j] = m[i][j];
	}
	
	public void multiplyWith(double s){
		for(int i = 0; i < nDims;i++)
			for(int j = 0; j < nDims;j++)
				m[i][j] *= s; 
	}
	
	private void saveBackup() {
		for(int i = 0; i < nDims;i++)
			for(int j = 0; j < nDims;j++)
				backup[i][j] = m[i][j];
	}
	
	private void loadBackup() {
		for(int i = 0; i < nDims;i++)
			for(int j = 0; j < nDims;j++)
				m[i][j] = backup[i][j];
	}
	
	// Method to evaluate the determinant of a matrix.
	  public double det() {		
		
		double[][] a = m;
		 
	    int n = a.length;
	    int index[] = new int[n];

	 // Transform the matrix into an upper triangle
	    saveBackup();
	    gaussian(a, index);	    

	 // Take the product of the diagonal elements
	    double d = 1;
	    for (int i=0; i<n; ++i) d = d*a[index[i]][i];

	 // Find the sign of the determinant
	    int sgn = 1;
	    for (int i=0; i<n; ++i) {
	      if (i != index[i]) {
	        sgn = -sgn;
	        int j = index[i];
	        index[i] = index[j];
	        index[j] = j;
	      }
	    }
	    loadBackup();
	    return sgn*d;
	 }
	  
	// An example of performing matrix inversion through the
	// partial-pivoting Gaussian elimination.
	 public double[][] inv() {
		 
		 	double a[][] = m;
		 
		    int n = a.length;
		    double x[][] = new double[n][n];
		    double b[][] = new double[n][n];
		    int index[] = new int[n];
		    for (int i=0; i<n; ++i) b[i][i] = 1;
	
		 // Transform the matrix into an upper triangle
		saveBackup();
	    gaussian(a, index);	    
	
	 // Update the matrix b[i][j] with the ratios stored
	    for (int i=0; i<n-1; ++i)
	      for (int j=i+1; j<n; ++j)
	        for (int k=0; k<n; ++k)
	          b[index[j]][k]
	            -= a[index[j]][i]*b[index[i]][k];
	
	 // Perform backward substitutions
	    for (int i=0; i<n; ++i) {
	      x[n-1][i] = b[index[n-1]][i]/a[index[n-1]][n-1];
	      for (int j=n-2; j>=0; --j) {
	        x[j][i] = b[index[j]][i];
	        for (int k=j+1; k<n; ++k) {
	          x[j][i] -= a[index[j]][k]*x[k][i];
	        }
	        x[j][i] /= a[index[j]][j];
	      }
	    }
	  loadBackup();
	  return x;
	  }
	
	// Method to carry out the partial-pivoting Gaussian
	// elimination.  Here index[] stores pivoting order.	
	  public void gaussian(double a[][],
	    int index[]) {
	    int n = index.length;
	    double c[] = new double[n];
	
	 // Initialize the index
	    for (int i=0; i<n; ++i) index[i] = i;
	
	 // Find the rescaling factors, one from each row
	    for (int i=0; i<n; ++i) {
	      double c1 = 0;
	      for (int j=0; j<n; ++j) {
	        double c0 = Math.abs(a[i][j]);
	        if (c0 > c1) c1 = c0;
	      }
	      c[i] = c1;
	    }
	
	 // Search the pivoting element from each column
	    int k = 0;
	    for (int j=0; j<n-1; ++j) {
	      double pi1 = 0;
	      for (int i=j; i<n; ++i) {
	        double pi0 = Math.abs(a[index[i]][j]);
	        pi0 /= c[index[i]];
	        if (pi0 > pi1) {
	          pi1 = pi0;
	          k = i;
	        }
	      }
	
	   // Interchange rows according to the pivoting order
	  int itmp = index[j];
	  index[j] = index[k];
	  index[k] = itmp;
	  for (int i=j+1; i<n; ++i) {
	    double pj = a[index[i]][j]/a[index[j]][j];
	
	 // Record pivoting ratios below the diagonal
	    a[index[i]][j] = pj;
	
	 // Modify other elements accordingly
	        for (int l=j+1; l<n; ++l)
	          a[index[i]][l] -= pj*a[index[j]][l];
	      }
	    }
	  }
}
