/*
* Copyright 2023 by Timo von Oertzen and Andreas M. Brandmaier
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
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
