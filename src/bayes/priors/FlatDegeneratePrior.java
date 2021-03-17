package bayes.priors;

public class FlatDegeneratePrior implements Distribution 
{

	@Override
	public double getDensity(double x) {
		return (1);
	}
	
	public double getStartingValue() {
		return(1);
	}
	
	public String getName() {
		return("Flat");
	}

}
