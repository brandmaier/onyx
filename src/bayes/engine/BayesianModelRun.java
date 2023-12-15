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
package bayes.engine;

import bayes.BayesianSEM;
import bayes.Chain;
import bayes.sampler.MetropolisHastings;
import engine.ModelRun;
import engine.ModelRunUnit;
import engine.OnyxModel;
import engine.ModelRun.Priority;
import engine.ModelRun.Status;
import engine.backend.Model;
import engine.backend.Model.warningFlagTypes;

public class BayesianModelRun extends ModelRun {

	BayesianModelRunUnit bru;

	
	public BayesianModelRun(OnyxModel model) {
		super(model);
		
		System.out.println("Bayesian Model Run initialized");
		
		double[] fakeStarting = new double[model.getAnzPar()];
		
		if (bru==null) bru = new BayesianModelRunUnit(fakeStarting,"Metropolis Hastings",
				model.getParameterNames(),model.getVariableNames(),this);
		
		
		
		bru.initEstimation(model);
	}
	
	@Override
	public void requestReset() {
		super.requestReset();
		bru.initEstimation(model);
	}

	@Override
	protected void stepUnit() {
		//System.out.println("Take a step!");
	
		boolean convergedBefore = bru.isConverged();
		
		bru.performStep(model);
		
		
		if (convergedBefore != bru.isConverged()) {
		
			System.out.println("BayesianModelRunUnit converged!");
		
		 convergedUnits.add(bru);
         model.notifyOfConvergedUnitsChanged();
         if (status != Status.RESULTSVALID && modelWorkCopy.warningFlag == warningFlagTypes.OK) {
             setStatus(Status.RESULTSVALID);
             if (holdOnNextValidEstimate) {priority = Priority.HOLD; holdOnNextValidEstimate = false;}
         }
         
		}
		
	}
	



}
