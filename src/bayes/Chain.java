package bayes;

import bayes.sampler.MetropolisHastings;

public class Chain {

	private ParameterSet[] params;
	private String[] parameterNames;
			
	int chainPointer = 0;

	public Chain(int size, String[] names) {
		params =  new ParameterSet[size];
		this.parameterNames = names;
	}
	
	public ParameterSet get(int idx)
	{
		return params[idx];
	}
	
	public double getMean(int idx) {
		double x = 0;
		for (int i=0; i < params.length;i++) x=x+params[i].getValue(idx);
		return( x/params.length);
	}


	public void set(int i, ParameterSet sample) {
		params[i] = sample;
	}
	
	public void add(ParameterSet sample) {
		params[chainPointer] = sample;
		chainPointer++;
	}

	public int getNumSamples() {
		return(params.length);
	}
	
	public String toString()
	{
		String s = "<Chain>";
		for (int i=0; i < getNumSamples(); i++) {
			s = s + this.get(i).toString()+"\n";
		}
		s=s+">\n";
		return(s);
	}

	public void run(MetropolisHastings sampler) {
		add(sampler.sample());
	}

	public boolean isFull() {
		return(chainPointer==params.length);
	}

	public int size() {
		return(this.params.length);
	}

	public int getPointer() {
		return(this.chainPointer);
	}

	public String[] getParameterNames() {
		return(parameterNames);
	}
}
