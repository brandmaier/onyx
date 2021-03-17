package bayes.priors;

public class GammaDistribution implements Distribution {

	private double scale;
	private double shape;

	public GammaDistribution(double k, double theta) {
		this.shape = k;
		this.scale = theta;
	}

	@Override
	public double getDensity(double x) {
		return(1.0/(ChiSquare.gamma(shape)*Math.pow(scale, shape)) * Math.pow(x, shape-1)*Math.exp(-x/scale));
		
	}
	
	public double getStartingValue() {
		return( scale*shape );
	}
	
	public String getName() {
		return("Gamma");
	}
}
