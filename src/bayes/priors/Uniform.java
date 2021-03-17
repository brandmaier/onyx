package bayes.priors;

public class Uniform implements Distribution {

	private double min;
	private double max;

	public Uniform(double min, double max)
	{
		this.min = min;
		this.max = max;
	}
	
	@Override
	public double getDensity(double x) {
		if (x >= min && x <= max)
			return( 1.0/(max-min) );
		else
			return(0);
	}
	
	public double getStartingValue() {
		return( (max-min)/2);
	}
	
	public String getName() {
		return("Uniform");
	}

}
