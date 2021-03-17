package bayes.priors;

public class GaussianPrior implements Distribution {

	private double var;
	private double mu;

	public GaussianPrior(double mu, double var) {
		this.mu = mu;
		this.var = var;
	}
	
	public double getDensity(double x) {
		return( 1.0/(Math.sqrt(2*Math.PI*var))*Math.exp(-(x-mu)*(x-mu)/(2*var)) );
	}
	
	public double getStartingValue() {
		return( mu );
	}
	
	public String getName() {
		return("Gaussian");
	}
}
