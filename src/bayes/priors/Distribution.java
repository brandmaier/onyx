package bayes.priors;

public interface Distribution {

public double getDensity(double x);

public String getName();

	public double getStartingValue();
}
