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

import bayes.priors.GaussianPrior;
import bayes.sampler.MetropolisHastings;
import engine.ModelRunUnit;
import engine.ModelRunUnit.Objective;
import engine.backend.Model;
import engine.externalRunner.ExternalRunUnit;
import engine.externalRunner.ExternalRunUnit.AgentStatus;
import engine.OnyxModel;

public class BayesianRunner extends ModelRunUnit {

	public AgentStatus agentStatus = AgentStatus.NOTYETSTARTED;
	public Chain chain;
	
    public BayesianRunner() {
        super(null, null, Double.NaN, Objective.MAXIMUMLIKELIHOOD, 0.01, null, null, null, null, 0, false, null, null);
    }
    
    public boolean isConverged() {return converged;}
    
    @Override
    public void initEstimation(Model model)
    {
    	BayesianSEM bsem = new BayesianSEM( ((OnyxModel)model) );
    	
    	for (int i=0; i < bsem.size; i++) {
    		 bsem.setPrior(i, new GaussianPrior(0, 100));
    	}
    	
        final MetropolisHastings sampler = new MetropolisHastings(bsem, data);
	    
       final ModelRunUnit fthis = this;
        Thread thread = new Thread() {
            public void run() {
            	chain = sampler.run(5000, 100);
            	agentStatus = AgentStatus.SUCCESS;
            	converged = true;
            } 
        };
        agentStatus = AgentStatus.RUNNING;
        thread.start();
	   
    }
    
    @Override
    public boolean performStep(Model model) {
    	// NOTHING TODO right?
    	return(converged);
    }
}
