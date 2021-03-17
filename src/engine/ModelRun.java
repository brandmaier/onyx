/*
 * Created on 02.03.2012
 */
package engine;

import java.util.*;

import javax.swing.JOptionPane;

import engine.ModelRunUnit.Objective;
import engine.backend.MissingDataModelNew;
import engine.backend.Model;
import engine.backend.Model.Strategy;
import engine.backend.MultiGroupModel;
import engine.backend.RAMModel;
import engine.backend.SaturatedModel;
import engine.backend.Model.warningFlagTypes;
import engine.backend.SaturatedRAMModel;
import gui.frames.MainFrame;
import gui.graph.Edge;
import gui.graph.Graph.MeanTreatment;

public class ModelRun extends Thread {

    public final int CONVERGEDCONTAINERCAPACITY = 100;
    
    public int MINRUNS = 3;
    public int USERSTARTINGHEADSTART = 4;
    public int STEPSUNTILNEXTRANDOMUNIT = 20;
    public int MAXNONCONVERGEDUNITS = 10;
    public int MAXRUNUNITS = 200;
    
//    final int WAITINGTIMEUNIT = 20;
    final int HOLDWAITINGTIME = 10000;
    final int LOWPRIORITYWAITINGTIME = 200;
    final int FINISHEDWAITINGTIME = 50;
    
    public OnyxModel model;
    protected Model modelWorkCopy;
    public static enum Status {WAITING, RUNNING, RESETTING, RESULTSVALID, ENDINGMODELRUN, DEAD};
    public static enum Warning {COVARIANCESONSTANTSINGULAR, MODELOVERSPECIFIED, ACCELERATINGCYCLE, ERROR}; 
    public static enum Priority {HOLD, LOW, NORMAL, HIGH};
    protected Status status;
    public Priority priority;
    public int steps;
    public int anzRandomUnits;
//    private int anzConvergedUnits;
    
    public boolean modelIsConstantSingular, modelIsOverspecified, modelHasAcceleratingCycle, modelThrewUnknownError, dataValid, definitionDataValid;
    public int overspecificationIndex;
    
    public boolean holdOnNextValidEstimate;     // mostly for debugging

    public List<ModelRunUnit> runUnits;
    public List<ModelRunUnit> convergedUnits;
    // special runningUnits
    public ModelRunUnit userStartingValueUnit, arbitraryStartingValuesUnit, userLSUnit, arbitraryLSUnit;
    
    public List<ModelRunUnit> externalRunnerWaitQueue;
    
    public long bigClock;
    
    private boolean delayExternalRunners = false;

    // value that may be set early in the generation process. When not NaN, these values are returned for independent and saturated LL and the independent to saturated KL. 
    public double[][] precomputedSaturatedCov = null;
    public double[] precomputedSaturatedMean = null;
    public double precomputedSaturatedLL = Double.NaN;
    public double precomputedIndependentLL = Double.NaN;
    public double precomputedIndependentKulbackLeibler = Double.NaN;
    public double nonmissRatio = 1.0;
    public int df, independentDF;
    
    public ModelRun(OnyxModel model) {
        status = Status.WAITING;
        this.model = model;
        runUnits = Collections.synchronizedList(new ArrayList<ModelRunUnit>(2)); 
        convergedUnits = new ArrayList<ModelRunUnit>(CONVERGEDCONTAINERCAPACITY);
        externalRunnerWaitQueue = new ArrayList<ModelRunUnit>();
        priority = Priority.NORMAL;
        reset();
        this.setName("ModelFitThread");
        this.start();
    }

    private void restart() {
        runUnits.clear(); convergedUnits.clear();

        userStartingValueUnit = new ModelRunUnit(modelWorkCopy.startingValues, "ML with user starting values.", modelWorkCopy.getParameterNames(),
                modelWorkCopy.getVariableNames(), modelWorkCopy.getObservedVariables(), MINRUNS, false, this);
        userStartingValueUnit.populateDescriptives(model);
        arbitraryStartingValuesUnit = new ModelRunUnit(modelWorkCopy.getArbitraryStartingValues(), "ML with arbitrary values.", modelWorkCopy.getParameterNames(), 
                modelWorkCopy.getVariableNames(), modelWorkCopy.getObservedVariables(), MINRUNS, false, this);
        arbitraryStartingValuesUnit.populateDescriptives(model);
        userLSUnit = new ModelRunUnit(modelWorkCopy.startingValues, "LS with user starting values.", modelWorkCopy.getParameterNames(), 
                modelWorkCopy.getVariableNames(), modelWorkCopy.getObservedVariables(), true, MINRUNS, false, this);
        userLSUnit.populateDescriptives(model);
        arbitraryLSUnit = new ModelRunUnit(modelWorkCopy.getArbitraryStartingValues(), "LS with arbitrary starting values.", modelWorkCopy.getParameterNames(), 
                modelWorkCopy.getVariableNames(), modelWorkCopy.getObservedVariables(), true, MINRUNS, false, this);
        arbitraryLSUnit.populateDescriptives(model);
        try {addRunUnit(userStartingValueUnit);} catch (Exception e) {System.out.println("Warning: User Starting value runner couldn't be initiated."); e.printStackTrace(System.out); userStartingValueUnit = null;}
        try {addRunUnit(arbitraryStartingValuesUnit);} catch (Exception e) {System.out.println("Warning: Arbitrary Starting value runner couldn't be initiated."); e.printStackTrace(System.out); arbitraryStartingValuesUnit = null;}
        try {addRunUnit(userLSUnit);} catch (Exception e) {System.out.println("Warning: LS User Starting value runner couldn't be initiated."); e.printStackTrace(System.out); userLSUnit = null;}
        try {addRunUnit(arbitraryLSUnit);} catch (Exception e) {System.out.println("Warning: LS Arbitrary Starting value runner couldn't be initiated."); e.printStackTrace(System.out); arbitraryLSUnit = null;}
        steps = 0;
        anzRandomUnits=0;
        
        bigClock = System.nanoTime();
    }
    
    public long getBigClockTime() {return System.nanoTime() - bigClock;}
    
    private void createWorkModel() {

        // synchronized call
        OnyxModel workCopy = model.copy();

        // TODO TvO to himself, 26.04.2017: If the strategy is DefaultWithEMSupport, then we don't build the full tree of submodels; does that mean it is sufficient
        // to just make the modelWork Copy the workCopy like with indirect data and be done with it? Needs checking!
        if (workCopy.strategyUseEMWithSaturated || workCopy.hasAuxiliaryOrControl()) {
            workCopy.setData(model.data, model.auxiliaryData, model.controlData);
            workCopy.strategyUseEMWithSaturated = true;
            modelWorkCopy = workCopy;
        } else 
        if (workCopy.isIndirectData()) {
            modelWorkCopy = workCopy;
        } else {
            workCopy.anzPer = (workCopy.data==null?0:workCopy.data.length);
            int anzDef = workCopy.definitionVariableEdges.length;
            
            // AB: sanity check
            if (anzDef>0 && workCopy.definitionVariableData.length != workCopy.anzPer) {
            	System.err.println("Error! Inconsistency with definition variable data! Number of def.vars. is "+
            			workCopy.definitionVariableData.length+
            			" "+ workCopy.anzPer);
            	//TODO: set some status flag ?     TvO, 4.10.2015: I don't get the inconsistency, why should the #participants = #definition variables?
            	return;
            }
            
            Hashtable<FIMLDefinitionKey,Hashtable<FIMLMissingKey,Vector<FIMLMissingKey>>> hash = 
                new Hashtable<FIMLDefinitionKey,Hashtable<FIMLMissingKey,Vector<FIMLMissingKey>>>();
            for (int i=0; i<workCopy.anzPer; i++) {
                FIMLMissingKey rowMissing = new FIMLMissingKey(workCopy.data[i], i);
                FIMLDefinitionKey rowDefinition = new FIMLDefinitionKey((anzDef>0?workCopy.definitionVariableData[i]:new double[0]), i);
                
                if (!rowMissing.isAllMissing()) {
                    Hashtable<FIMLMissingKey,Vector<FIMLMissingKey>> missingHash = hash.get(rowDefinition);
                    if (missingHash == null) {missingHash = new Hashtable<FIMLMissingKey,Vector<FIMLMissingKey>>(); hash.put(rowDefinition, missingHash);}
                    
                    Vector<FIMLMissingKey> dat = missingHash.get(rowMissing);
                    if (dat == null) {dat = new Vector<FIMLMissingKey>(1); missingHash.put(rowMissing,dat); }
                    
                    dat.add(rowMissing);
                }
            }
            
            int anzDefinitionGroups = hash.size();
            Model[] definitionSubmodel = new Model[anzDefinitionGroups];
            int defSubmodelCounter = 0;
            for (FIMLDefinitionKey defkey:hash.keySet()) {
                OnyxModel sub = workCopy.copy();
    
                // Filling definition Variable values
                for (int j=0; j<sub.definitionVariableEdges.length; j++) {
                    Edge e = sub.definitionVariableEdges[j];
                    int s = e.getSource().getId(), t = e.getTarget().getId();
                    if (e.getSource().isMeanTriangle()) {
                        sub.meanVal[t] = sub.definitionVariableData[defkey.perID][j];
                    } else {
                        if (e.isDoubleHeaded()) {
                            sub.symVal[s][t] = sub.symVal[t][s] = sub.definitionVariableData[defkey.perID][j];
                        } else {
                            sub.asyVal[t][s] = sub.definitionVariableData[defkey.perID][j];
                        }
                    }
                }
                Hashtable<FIMLMissingKey,Vector<FIMLMissingKey>> missingHash = hash.get(defkey);
                int anzMissGroups = missingHash.size();
                OnyxModel[] submodel = new OnyxModel[anzMissGroups];
                int[][] observation = new int[anzMissGroups][];
                int missSubmodelCounter = 0;
                for (FIMLMissingKey misskey:missingHash.keySet()) {
                    OnyxModel subsub = sub.copy();
                    Vector<FIMLMissingKey> dat = missingHash.get(misskey); 
                
                    // Creating subdataset and making missing slots latent
                    int anzMiss = 0; for (int j=0; j<misskey.dataRow.length; j++) if (Model.isMissing(misskey.dataRow[j])) anzMiss++;
                    int[] newFilter = new int[subsub.anzVar - anzMiss];
                    double[][] newData = new double[dat.size()][subsub.anzVar - anzMiss];
                    int[] newDataForeignKey = new int[dat.size()];
                    observation[missSubmodelCounter] = new int[subsub.anzVar - anzMiss];
                    int k = 0; for (int j=0; j<misskey.dataRow.length; j++) if (!Model.isMissing(misskey.dataRow[j])) {
                        observation[missSubmodelCounter][k] = j;
                        newFilter[k] = subsub.filter[j];
                        for (int l=0; l<dat.size(); l++) {newData[l][k] = dat.elementAt(l).dataRow[j]; newDataForeignKey[l] = dat.elementAt(l).perID;}
                        k++;
                    }
                    subsub.filter = newFilter; subsub.anzVar = newFilter.length;
                    subsub.setData(newData, newDataForeignKey);
                    
                    submodel[missSubmodelCounter++] = subsub;
                }   
                if (anzMissGroups == 1) definitionSubmodel[defSubmodelCounter++] = submodel[0];
                else {
                    definitionSubmodel[defSubmodelCounter++] = new MissingDataModelNew(sub, submodel, observation, workCopy.startingValues);
                }
            }
            if (anzDefinitionGroups == 0) modelWorkCopy = workCopy;
            if (anzDefinitionGroups == 1) modelWorkCopy = definitionSubmodel[0];
            if (anzDefinitionGroups >  1) {
                modelWorkCopy = new MultiGroupModel(definitionSubmodel, workCopy.startingValues, workCopy.anzVar);
            }
            if (anzDefinitionGroups >= 1) if (modelWorkCopy instanceof MultiGroupModel) modelWorkCopy.setDataWithoutPassingToSubmodels(workCopy.data);

            int miss = 0, total = 0; 
            if (workCopy.data != null) {
                for (int i=0; i<workCopy.data.length; i++) for (int j=0; j<workCopy.data[i].length; j++) {total++; if (Model.isMissing(workCopy.data[i][j])) miss++;}
                nonmissRatio = (double)(total-miss) / ((double)total);
            }
        }
        precomputedSaturatedLL = precomputedIndependentLL = precomputedIndependentKulbackLeibler = Double.NaN;
        if (clearedForStarting()) try {
            SaturatedRAMModel saturated = workCopy.getSaturatedModel(false);
            Model satMiss;
            if (workCopy.isIndirectData()) {
                satMiss = saturated;
                saturated.setDataDistribution(workCopy.dataCov, workCopy.dataMean, workCopy.anzPer);
                saturated.computeMomentsFromDataCovarianceAndMean();
            } else {
                satMiss = new MissingDataModelNew(saturated);
                satMiss.setData(workCopy.data);
            }
            
            precomputedSaturatedCov = Statik.ensureSize(precomputedSaturatedCov, workCopy.anzVar, workCopy.anzVar);
            precomputedSaturatedMean = Statik.ensureSize(precomputedSaturatedMean,  workCopy.anzVar);
            RAMModel.getSaturatedCovarianceOfMultigroupRAMModel(workCopy, modelWorkCopy, precomputedSaturatedMean, precomputedSaturatedCov);
            saturated.setParameterToDistribution(precomputedSaturatedMean, precomputedSaturatedCov);
            satMiss.setParameter(saturated.getParameter());
            precomputedSaturatedLL = satMiss.getMinusTwoLogLikelihood(saturated.getParameter());
            precomputedIndependentLL = satMiss.getIndependentLL();
            precomputedIndependentKulbackLeibler = satMiss.getIndependentKulbackLeibler();

            // DEBUG
//            satMiss.estimateML(saturated.getParameter());
//            saturated.evaluateMuAndSigma(satMiss.getParameter());
//            
//            System.out.println("EM estimate = "+Statik.matrixToString(precomputedSaturatedMean));
//            System.out.println(Statik.matrixToString(precomputedSaturatedCov));
//            System.out.println("ML estimate = "+Statik.matrixToString(saturated.meanVal));
//            System.out.println(Statik.matrixToString(saturated.sigma));
        } catch (Exception e) {
            System.out.println("Exception on precomputations for fit indices: "+e);
        }
        // treats meanTreatment correctly. 
        df = modelWorkCopy.getRestrictedDF();
        independentDF = workCopy.anzVar*(workCopy.anzVar+1)/2 - workCopy.anzVar;
    }
    
    // returns true if successfully restarted runners. 
    private boolean reset() {
        
        delayExternalRunners = true;
        
        // TvO 09 MAR 2014: Decision that priority is not reset on model modelRun-reset
//        priority = Priority.NORMAL;
        runUnits.clear(); convergedUnits.clear(); steps = 0; anzRandomUnits=0;
        
        createWorkModel();
        modelThrewUnknownError = false;
        modelIsConstantSingular = false; 
        modelHasAcceleratingCycle = false;
        overspecificationIndex = -1; modelIsOverspecified = false;

        if (modelWorkCopy.anzVar > 0) {
            modelIsConstantSingular = modelWorkCopy.isConstantSingular();
            if (modelIsConstantSingular) 
                model.notifyOfWarning(Warning.COVARIANCESONSTANTSINGULAR);
            else model.notifyOfClearWarning(Warning.COVARIANCESONSTANTSINGULAR);
            if (modelWorkCopy instanceof RAMModel) modelHasAcceleratingCycle = ((RAMModel)modelWorkCopy).hasAcceleratingCycle();
            if (modelHasAcceleratingCycle) model.notifyOfWarning(Warning.ACCELERATINGCYCLE);
        }
        if ((modelWorkCopy.anzPar > 0) && (modelWorkCopy.anzVar > 0)) {
            if (!modelIsConstantSingular && !modelHasAcceleratingCycle) {
                try {
                    overspecificationIndex = modelWorkCopy.hessianIsConstantSingular(modelWorkCopy.startingValues);
                    modelIsOverspecified = (overspecificationIndex != -1);
                } catch (Exception e) {return false;}
            } else { overspecificationIndex = -1; modelIsOverspecified = false;}
            if (modelIsOverspecified) 
                model.notifyOfWarning(Warning.MODELOVERSPECIFIED);
            else model.notifyOfClearWarning(Warning.MODELOVERSPECIFIED);
        }

        boolean clearedForStarting = clearedForStarting(); 
        if (clearedForStarting && modelWorkCopy.anzPar > 0) {
            restart();
            emptyWaitQueue();
        }
        else if (clearedForStarting && modelWorkCopy.anzPar == 0) {
            userStartingValueUnit = new ModelRunUnit(modelWorkCopy.startingValues, "No free parameter", modelWorkCopy.getParameterNames(),
                    modelWorkCopy.getVariableNames(), modelWorkCopy.getObservedVariables(), MINRUNS, false, this);
            userStartingValueUnit.populateDescriptives(model);
            addRunUnit(userStartingValueUnit);
            userStartingValueUnit.converged = true;
            convergedUnits.add(userStartingValueUnit);
        }
        
        delayExternalRunners = false;
        model.notifyOfConvergedUnitsChanged();
        return clearedForStarting;
    }

    protected void stepUnit() {
        ModelRunUnit ru = userStartingValueUnit;
        if (userStartingValueUnit.steps >= USERSTARTINGHEADSTART) { 
            Collections.sort(runUnits, ModelRunUnit.queueComparator);
            ru = runUnits.get(0);
            for (ModelRunUnit r:runUnits) r.queueValue += r.importance;
        } else ru = userStartingValueUnit;

        if ((steps+1) % STEPSUNTILNEXTRANDOMUNIT == 0 && getAnzNotConverged() < MAXNONCONVERGEDUNITS && runUnits.size() < MAXRUNUNITS) {
            ModelRunUnit newRu = new ModelRunUnit(modelWorkCopy.getRandomStartingValues(), "ML for random starting values ("+(anzRandomUnits+1)+").", 
                    modelWorkCopy.getParameterNames(), modelWorkCopy.getVariableNames(), modelWorkCopy.getObservedVariables(), MINRUNS, true, this);
            newRu.populateDescriptives(model);
            addRunUnit(newRu);
//            if (Double.isNaN(newRu.fit)) {
//                System.out.println("Random start Fit is NaN.");
//            } else 
//                System.out.println("****************** Random start ok ******************");
            anzRandomUnits++;
        }
        
        try {
            boolean isAlreadyConverged = ru.isConverged();
            boolean nowConverged = ru.performStep(modelWorkCopy);
            if (!isAlreadyConverged && nowConverged) {
//                if (ru.isMLWithUserStartingValues()) 
//                    System.out.println("------ converged ------");
                if (ru.objective == Objective.LEASTSQUARES) {
                    ModelRunUnit newRu = new ModelRunUnit(ru);
                    Statik.copy(ru.position, newRu.starting);
                    newRu.objective = Objective.MAXIMUMLIKELIHOOD;
                    newRu.name = "ML using "+ru.name;
                    addRunUnit(newRu);
                }
                convergedUnits.add(ru);
                model.notifyOfConvergedUnitsChanged();
                if (status != Status.RESULTSVALID && modelWorkCopy.warningFlag == warningFlagTypes.OK) {
                    setStatus(Status.RESULTSVALID);
                    if (holdOnNextValidEstimate) {priority = Priority.HOLD; holdOnNextValidEstimate = false;}
                }
            }
        } catch (Exception e) {
            System.out.println("Exception occured on stepping runner "+ru.getName()+", "+e);
            e.printStackTrace();
            ru.queueValue = 0;
            ru.setWarningFlag(Model.warningFlagTypes.FAILED);
            
            // Policy as decided on October 29th: The error will not be forwarded, but the rununit will be taken out of consideration.
            /*
            model.notifyOfWarning(Warning.ERROR); modelThrewUnknownError = true;
            if (status != Status.RESETTING && status != Status.ENDINGMODELRUN)  // these will not be overwritten
            {
                setStatus(Status.WAITING);
            } else System.out.println("Status was reset to "+status+" before error occured, I will continue with that.");
            */
        }
            
        assignSuccessRanks();
        ru.assignImportance();
        ru.queueValue = ru.importance;
        fightForSurvival(ru);
        steps++;
    }
    
    public void run() {
    	try {
        while (status != Status.ENDINGMODELRUN) {
            if (status == Status.RESETTING) {
                try {
                    boolean startClearance = false;
                    while (status == Status.RESETTING) {        // makes sure that reset commands during resetting create a new reset. 
                        status = Status.RUNNING;                // call without notification, just internal to fetch re-resets
                        startClearance = reset();
                    }
                    if (status != Status.ENDINGMODELRUN) {             // ENDINGMODEL will not be overwritten.  
                        if (!startClearance) {setStatus(Status.WAITING); model.notifyOfFailedReset();}
                        else if (modelWorkCopy.anzPar == 0) setStatus(Status.RESULTSVALID);
                        else setStatus(Status.RUNNING);
                    } 
                } catch (Exception e) {
                    System.out.println("Exception occured on resetting or restarting, "+e);
                    e.printStackTrace();
                    model.notifyOfWarning(Warning.ERROR); modelThrewUnknownError = true;
                    if (status != Status.RESETTING && status != Status.ENDINGMODELRUN)  // these will not be overwritten
                    {
                        setStatus(Status.WAITING);
                    } else System.out.println("Status was reset to "+status+" before error occured, I will continue with that.");
                }
            }
            if (status == Status.RUNNING || status == Status.RESULTSVALID) {
                try {
                    if (modelWorkCopy.anzPar>0) stepUnit();
                } catch (Exception e) {
                    System.out.println("Exception occured on stepping a runner, "+e);
                    e.printStackTrace();
                    model.notifyOfWarning(Warning.ERROR); modelThrewUnknownError = true;
                    if (status != Status.RESETTING && status != Status.ENDINGMODELRUN)  // these will not be overwritten
                    {
                        setStatus(Status.WAITING);
                    } else System.out.println("Status was reset to "+status+" before error occured, I will continue with that.");
                }
            }
            try {
                while ((priority == Priority.HOLD  || status == Status.WAITING) && status != Status.ENDINGMODELRUN) {
                    try {Thread.sleep(HOLDWAITINGTIME);} catch (InterruptedException e) {}
                }
                if (priority == Priority.LOW) {
                    Thread.sleep(LOWPRIORITYWAITINGTIME);
                }
                if (priority == Priority.NORMAL) 
                    if (status == Status.RUNNING) Thread.sleep(1); else {
                        Thread.sleep(FINISHEDWAITINGTIME);
                    }

            } catch (InterruptedException e) {}
        }
        setStatus(Status.DEAD);
        
    	} catch (OutOfMemoryError err) {
    		JOptionPane.showMessageDialog(null, "We are sorry! Onyx ran out of memory.\nEstimation cannot be finished.\nPlease see the online FAQ on how to increase memory allocation for Onyx.");
    		// AB: Timo, deal with this   TvO 26.04.17: Dealt with this!
    		kill();
    	}
    }
    
    public void addRunUnit(ModelRunUnit ru) {
        runUnits.add(ru); 
        ru.initEstimation(modelWorkCopy);
        assignSuccessRanks();
        ru.assignImportance();
        ru.queueValue = ru.importance;
        ru.modelRun = this;
    }

    private void assignSuccessRanks() {
        try {
            Collections.sort(runUnits, ModelRunUnit.performanceComparator);
            int i=0; 
            for (ModelRunUnit r:runUnits) r.rank = i++;
        } catch (Exception e) {
            System.out.println("Error in assigning success ranks.");
            try {
                Collections.sort(runUnits, ModelRunUnit.performanceComparator);
            } catch (Exception e2) {
                System.out.println("failed on 2nd try again.");
            }
        }
    }
    
    public void requestReset() {
        if (status == Status.RESETTING) return;
        boolean wasDead = status == Status.DEAD;
        setStatus(Status.RESETTING);
        if (wasDead) try {start(); } 
        catch (IllegalThreadStateException e) 
        {
            System.out.println("Run start failed, "+e);
        }
    }
    
    public void invalidateDataSet() {
        dataValid = false;
        
        runUnits.clear(); convergedUnits.clear(); steps = 0; anzRandomUnits=0;
        model.notifyOfConvergedUnitsChanged();
        setStatus(Status.WAITING);
    }
    
    public Status getStatus() {return status;}
    
    public List<ModelRunUnit> getAllConvergedUnits() {
        Collections.sort(convergedUnits, ModelRunUnit.convergedComparator);
        return new ArrayList<ModelRunUnit>(convergedUnits);
    }
    
    public List<ModelRunUnit> getAllUnits() {
        List<ModelRunUnit> erg = new ArrayList<ModelRunUnit>(runUnits);
        Collections.sort(erg, ModelRunUnit.convergedComparator);
        return erg;
    }
    
    
    public double[] getEstimates() {
        if (!convergedUnits.isEmpty()) {
            List<ModelRunUnit> a = getAllConvergedUnits();
            ModelRunUnit ru = a.get(0);
            return ru.position;
        }
        return null;
    }
    
    public ModelRunUnit getBestUnit() {
        List<ModelRunUnit> a = getAllUnits();
        if (a.isEmpty()) {
            System.err.println("Trying to access runner while no runner exists!"); 
            return null;
        }
        ModelRunUnit ru = a.get(0);
        return ru;
    }

    
    public int getAnzConverged() {return convergedUnits.size();}
    public int getAnzNotConverged() {return runUnits.size() - convergedUnits.size();}

    /*
    public boolean readyForRestart() {
        return (status == Status.RUNNING || status == Status.RESTARTING || status == Status.RESULTSVALID || status == Status.ERROR || status == Status.WAITINGBECAUSEWARNING);
    }
    */
    
    private boolean clearedForStarting() {return modelWorkCopy.anzVar > 0 && !modelIsConstantSingular && !modelHasAcceleratingCycle && dataValid && definitionDataValid;}

    public void setHoldOnNextValidEstimate(boolean holdOnNextValidEstimate) {this.holdOnNextValidEstimate = holdOnNextValidEstimate;}
    
    public void kill() {
        setStatus(Status.ENDINGMODELRUN);
    }
    
    public void setStatus(Status newStatus) {
        Status oldStatus = status;
        status = newStatus;
        if (oldStatus == Status.RESULTSVALID || oldStatus == Status.WAITING) this.interrupt();
        if (oldStatus != newStatus) model.notifyOfStatusChange(newStatus);
//        System.out.println("Status has been set to "+newStatus);
    }

    public boolean isConverged() {return !convergedUnits.isEmpty();}
    public boolean isConvergedOnBestRunner() {
        return isConverged() && getBestUnit().isConverged();
    }
    public boolean isReliablyConverged() {return (isConverged() && getBestUnit().isReliable());}
    public boolean isOverspecifiedAtOptimum() {return (isConverged() && !getBestUnit().isHessianNonPositiveDefinite());}
    public int getAnzLocalOptima() {return getAnzLocalReliableOptima(false);}
    public int getAnzLocalReliableOptima() {return getAnzLocalReliableOptima(true);}
    public int getAnzLocalReliableOptima(boolean enforceReliable) {
        List<ModelRunUnit> stack = ModelRunUnit.stackEqualEstimates(getAllConvergedUnits());
        int valid = 0;
        for (ModelRunUnit mru:stack) if (mru.isMaximumLikelihoodObjective() && (!enforceReliable || mru.isReliable())) valid+= mru.anzClustered;
        return valid;
    }
    public boolean isHappy() {
        if (!isConverged()) return false;
        List<ModelRunUnit> stack = ModelRunUnit.stackEqualEstimates(getAllConvergedUnits());
        ModelRunUnit first = stack.get(0);
        for (ModelRunUnit mru:stack) {
            if (mru != first) {
                if (10*mru.steps > first.steps && mru.fit * 1.1 < first.fit && !mru.isReliable()) return false;
            }
        }
        return first.isReliable();
    }
    public boolean isBestEstimate(ModelRunUnit mru) {
        return (isConverged() && mru.isSameAs(getAllConvergedUnits().get(0)));
    }

    public Model getWorkModel() {
        return modelWorkCopy;
    }

    public String getMissingnessPatternDescription() {
        if (modelWorkCopy instanceof OnyxModel) return "Full data available.";
        if (!(modelWorkCopy instanceof MissingDataModelNew)) return "";
        return ((MissingDataModelNew)modelWorkCopy).getMissingnessPatternDescription();
    }

    /**
     * Determins whether either unit can swallow the other and returns the prey if that is the case. This occurs if both satisfy isSameAs (same objective, same fit, 
     * close positions), the predator has the better fit value, the prey is a random starting values unit, and the prey is not the only converged of the two. 
     * 
     * @param one       first unit
     * @param second    second unit
     * @return          prey (to-be-deleted unit), null if no unit can swallow the other.
     */
    private ModelRunUnit determineSwallowed(ModelRunUnit one, ModelRunUnit two) {
        if (one != two && one.isSameAs(two)) {
            ModelRunUnit predator = one, prey = two; 
            if (predator.fit > prey.fit) {predator = two; prey = one; }
            if (prey.isRandomStartingValuesUnit() && (!prey.converged || predator.converged)) {predator.anzSwallowed++; return prey;}
        }
        return null;
    }

    /**
     * Loops through all rununits and tests against changed to determine possible swallows. 
     * @param changed
     */
    private void fightForSurvival(ModelRunUnit changed) {
        for (ModelRunUnit opponent:runUnits) {
            ModelRunUnit dying = determineSwallowed(changed, opponent);
            if (dying != null) {
                runUnits.remove(dying);
                if (dying.converged) convergedUnits.remove(dying);
                model.notifyOfConvergedUnitsChanged();
                break;
            }
        }
    }

    public void addRunUnitOnQueue(ModelRunUnit runner) {
        if (runner == null || !this.isRunning()) return; 
        else addToQueueAndDepleteQueue(runner);
    }
    
    private boolean isRunning() {
        return (status == Status.RESULTSVALID || status == Status.RUNNING);
    }

    public void emptyWaitQueue() {addToQueueAndDepleteQueue(null);}
    
    private synchronized void addToQueueAndDepleteQueue(ModelRunUnit runner) {
        if (runner != null) {
            if (delayExternalRunners) externalRunnerWaitQueue.add(runner);
            else addRunUnit(runner);
        } else {
            for (int i=0; i<externalRunnerWaitQueue.size(); i++)
                addRunUnit(externalRunnerWaitQueue.get(i));
            externalRunnerWaitQueue.clear();
        }
    }
}
