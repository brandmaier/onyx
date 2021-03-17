package bayes.priors;

public class ChiSquare implements Distribution {

	private int k;


	public ChiSquare(int k) {
		this.k = k;
	}
	
	   static double logGamma(double x) {
		      double tmp = (x - 0.5) * Math.log(x + 4.5) - (x + 4.5);
		      double ser = 1.0 + 76.18009173    / (x + 0)   - 86.50532033    / (x + 1)
		                       + 24.01409822    / (x + 2)   -  1.231739516   / (x + 3)
		                       +  0.00120858003 / (x + 4)   -  0.00000536382 / (x + 5);
		      return tmp + Math.log(ser * Math.sqrt(2 * Math.PI));
		   }
		   static double gamma(double x) { return Math.exp(logGamma(x)); }
		
		   
		   @Override
		public double getDensity(double x) {
			   return( 1.0/(Math.pow(2, k/2.0)*gamma(k/2.0))   *Math.pow(x, k/2.0-1)*Math.exp(-x/2.0) );
		}

			public double getStartingValue() {
				return( k );
			}
			
			public String getName() {
				return("ChiSquare");
			}
		   
}
