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
