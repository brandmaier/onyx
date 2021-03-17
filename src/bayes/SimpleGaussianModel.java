package bayes;

public class SimpleGaussianModel extends BayesianModel {

	public  SimpleGaussianModel() {
		super(2);
	}
	
	@Override
	public double getLogLikelihood(double[][] x, ParameterSet parameterX) {
		double mu = parameterX.getValue(0);
		double var = parameterX.getValue(1);
		
		double ll=0;
		for (int i=0; i < x.length; i++) {
				ll += Math.log( 1.0/(Math.sqrt(2*Math.PI*var))*Math.exp(-(x[i][0]-mu)*(x[i][0]-mu)/(2*var)) ) ; //TODO: translate to sum
		}
		return(ll);
	}
	
	public int getNumParameters()
	{
		return(2);
	}

}
