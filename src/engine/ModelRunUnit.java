/*
 * Created on 02.03.2012
 */
package engine;

import engine.backend.Distribution;
import engine.backend.MissingDataModelNew;
import engine.backend.Model;
import engine.backend.Model.warningFlagTypes;
import engine.externalRunner.ExternalRunUnit;
import engine.externalRunner.ExternalRunUnit.AgentStatus;
import gui.frames.MainFrame;

import java.io.BufferedReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ModelRunUnit implements ParameterReader {

    public double COMPAREEPS = 0.001;
    public double COMPARERELATIVEEPS = 0.01;
    public final static int ANZPREVGAINS = 10;
    
    public int anzPar;
    public int MINRUNS;
    
    public String name;
    public boolean randomStartingValuesUnit;
    
    public double[] starting;
    public double[] position;
    
    public double fit;
    public double[] gradient;
    public double[][] hessian;
    public double[][] data;
    public String[] varNames;
    public int[] filter;

    public OptimizationHistory history;
    
    protected double EPS;
    
    public ModelRun modelRun;
    
    public int steps = 0; 
    public double lastSteplength = Double.POSITIVE_INFINITY; 
    public double lastGain = Double.POSITIVE_INFINITY;
    public double convergenceIndex;  // <=1 no convergence, ca. above 1.3 good convergence
    private Model.warningFlagTypes warningFlag;
    public double logDetHessian = Double.NEGATIVE_INFINITY;
    public double lastDamping = 1;
    public double[] prevGains;
    
    public Distribution emDistribution;
    public double[] emMethodLastPosition;
    public double emMethodLastFit;
    public boolean emMethodIsConverged;
    public int emSteps;
    
    public double ll, ls, chisqr, kulbackLeibler, chisqrIndep, klIndep, aic, 
            aicc, bic, bicadj, cfi, tli, srmr, rmsea,  rmseaDfCorrected, rmseaKL, ownTime, 
            clockTime, ownTimeAtConvergence, clockTimeAtConvergence;
    public int observedStatistics;
    public double[] rmseaCI;
    public int anzPer;
    public long startTime;
    
    public static enum Objective {MAXIMUMLIKELIHOOD, LEASTSQUARES, STARTINGVALUES, BAYESIAN};
    public Objective objective;
    
    public double importance;
    public double queueValue;
    public int rank;
    public boolean converged;
    public boolean failed;
    
    // twins are other ModelRunUnits in the same ModelRun that satisfy isSame().
    public int anzClustered;
    public int anzSwallowed;
    
    // direct copy from the model, will be passed to copies of this as pointer
    public String[] parameterNames;
    
    // array of parameter description via "from" and "to"
    public String[] parameterToAndFromDescription;
    private int stepsAtConvergence;

    final static public Comparator<ModelRunUnit> queueComparator = new Comparator<ModelRunUnit>() {
        public int compare(ModelRunUnit o1, ModelRunUnit o2) {if (o1.queueValue < o2.queueValue) return 1; if (o1.queueValue > o2.queueValue) return -1; return 0;}
    };
    final static public Comparator<ModelRunUnit> convergedComparator = new Comparator<ModelRunUnit>() {
        public int compare(ModelRunUnit o1, ModelRunUnit o2) {
            if (o1.getWarningFlag() == warningFlagTypes.FAILED && o2.getWarningFlag() != warningFlagTypes.FAILED) return 1;
            if (o2.getWarningFlag() == warningFlagTypes.FAILED && o1.getWarningFlag() != warningFlagTypes.FAILED) return -1;
            if (o1.objective == Objective.MAXIMUMLIKELIHOOD && o2.objective != Objective.MAXIMUMLIKELIHOOD) return -1; 
            if (o2.objective == Objective.MAXIMUMLIKELIHOOD && o1.objective != Objective.MAXIMUMLIKELIHOOD) return 1;
            if (o1.fit < o2.fit) return -1; if (o2.fit < o1.fit) return 1;
            if (Double.isNaN(o1.fit) && !Double.isNaN(o2.fit)) return 1; 
            if (Double.isNaN(o2.fit) && !Double.isNaN(o1.fit)) return -1;
            return 0;
        }
    };
    final static public Comparator<ModelRunUnit> performanceComparator = new Comparator<ModelRunUnit>() {
        public int compare(ModelRunUnit o1, ModelRunUnit o2) {
            if (o1.isConverged() && !o2.isConverged()) return 1; 
            if (o2.isConverged() && !o1.isConverged()) return -1;
            if (o1.hasWarning() && !o2.hasWarning()) return 1;
            if (o2.hasWarning() && !o1.hasWarning()) return -1;
            if (o1.objective == Objective.MAXIMUMLIKELIHOOD && o2.objective != Objective.MAXIMUMLIKELIHOOD) return -1; 
            if (o2.objective == Objective.MAXIMUMLIKELIHOOD && o1.objective != Objective.MAXIMUMLIKELIHOOD) return 1;
            if (o1.fit < o2.fit) return -1; if (o2.fit < o1.fit) return 1;
            if (Double.isNaN(o1.fit) && !Double.isNaN(o2.fit)) return 1; 
            if (Double.isNaN(o2.fit) && !Double.isNaN(o1.fit)) return -1;
            return 0;
        }
    }; 
    
    public ModelRunUnit(double[] starting, double[] position, double fit, Objective objective, double EPS, String name, String[] parameterNames, 
            String[] variableNames, int[] observedVariables, int minRuns, boolean randomStartingValuesUnit, ModelRun modelRun, 
            Distribution emDistribution) {
        anzPar = (starting==null?0:starting.length);
        this.randomStartingValuesUnit = randomStartingValuesUnit;
        this.starting = starting;
        this.position = position;
        this.fit = fit;
        this.gradient = new double[anzPar];
        this.hessian = new double[anzPar][anzPar];
        this.objective = objective;
        this.EPS = EPS;
        this.importance = 0;
        this.queueValue = 0;
        this.name = name;
        this.rank = -1;
        this.parameterNames = parameterNames;
        this.MINRUNS = minRuns;
        ll = ls = chisqr = kulbackLeibler = aic = aicc = bic = bicadj = cfi = tli = srmr = rmsea = rmseaDfCorrected = rmseaKL = ownTimeAtConvergence = clockTimeAtConvergence = Double.NaN; ownTime = clockTime = 0;
        rmseaCI = new double[] {Double.NaN, Double.NaN};
        startTime = System.nanoTime();
        observedStatistics = anzPer = 0;
        data = new double[0][];
        varNames = variableNames;
        filter = observedVariables;
        failed = false;
        this.modelRun = modelRun;
        this.emDistribution = (emDistribution==null?null:emDistribution.copy());
        this.emMethodLastPosition = (emDistribution==null?null:new double[emDistribution.anzVar]);
        this.emMethodLastFit = (objective==Objective.LEASTSQUARES?ls:ll);
        this.emMethodIsConverged = false;
        this.emSteps = 0;
        
        prevGains = new double[ANZPREVGAINS];
        for (int i=0; i<ANZPREVGAINS; i++) prevGains[i] = Double.POSITIVE_INFINITY;
    }
    
    public ModelRunUnit() {}
    
    public ModelRunUnit(ModelRunUnit toCopy) {
        this(Statik.copy(toCopy.starting), Statik.copy(toCopy.position), toCopy.fit, toCopy.objective, toCopy.EPS, ""+toCopy.name, toCopy.parameterNames, 
                toCopy.varNames, toCopy.filter, toCopy.MINRUNS, toCopy.randomStartingValuesUnit, toCopy.modelRun, toCopy.emDistribution);
        this.gradient = Statik.copy(toCopy.gradient);
        this.hessian = Statik.copy(toCopy.hessian);
        this.importance = toCopy.importance;
        this.queueValue = toCopy.queueValue;
        lastSteplength = toCopy.lastSteplength; 
        lastGain = toCopy.lastGain;
        convergenceIndex = toCopy.convergenceIndex;
        warningFlag = toCopy.warningFlag;
        logDetHessian = toCopy.logDetHessian;
        lastDamping = toCopy.lastDamping;
        steps = toCopy.steps;
        rank = toCopy.rank;
        ll = toCopy.ll; ls = toCopy.ls; chisqr = toCopy.chisqr; kulbackLeibler = toCopy.kulbackLeibler; 
        aic = toCopy.aic; aicc = toCopy.aicc; bic = toCopy.bic; cfi = toCopy.cfi;
        bicadj = toCopy.bicadj;
        chisqrIndep = toCopy.chisqrIndep; klIndep = toCopy.klIndep; 
        tli = toCopy.tli; srmr = toCopy.srmr; rmsea = toCopy.rmsea; rmseaDfCorrected = toCopy.rmseaDfCorrected; rmseaKL = toCopy.rmseaKL;  
        rmseaCI = toCopy.rmseaCI;
        ownTime = toCopy.ownTime; clockTime = toCopy.clockTime;
        
        if (toCopy.usesEM()) {
            if (toCopy.emDistribution == null) emDistribution = null; else emDistribution = toCopy.emDistribution.copy();
            emMethodLastPosition = Statik.copy(toCopy.emMethodLastPosition);
            emMethodLastFit = toCopy.emMethodLastFit;
            emMethodIsConverged = toCopy.emMethodIsConverged;
            emSteps = toCopy.emSteps;
        }
        
        ownTimeAtConvergence = toCopy.ownTimeAtConvergence; clockTimeAtConvergence = toCopy.clockTimeAtConvergence; anzPer = toCopy.anzPer; 
        startTime = toCopy.startTime;
        observedStatistics = toCopy.observedStatistics;
        data = toCopy.data;
        varNames = toCopy.varNames;
        failed = toCopy.failed;
        this.parameterToAndFromDescription = toCopy.parameterToAndFromDescription;
        
        // TODO descriptives
    }

    public ModelRunUnit(double[] starting, double[] position, double fit, String name, String[] parameterNames, String[] variableNames, int[] filter, int minRuns, boolean randomStarting, ModelRun modelRun) {
        this(starting, position, fit, Objective.MAXIMUMLIKELIHOOD, Model.suggestedEPS, name, parameterNames, variableNames, filter, minRuns, randomStarting, modelRun, null);
    }

    public ModelRunUnit(double[] starting, String name, String[] parameterNames, String[] variableNames, int[] filter, boolean useLS, int minRuns, boolean randomStarting, ModelRun modelRun) {
        this (starting, Statik.copy(starting), Double.NaN, (useLS?Objective.LEASTSQUARES:Objective.MAXIMUMLIKELIHOOD), Model.suggestedEPS, name, 
                parameterNames, variableNames, filter, minRuns, randomStarting, modelRun, null);
    }
    
    public ModelRunUnit(double[] starting, String name, String[] parameterNames, String[] variableNames, int[] filter, int minRuns, boolean randomStarting, ModelRun modelRun) {
        this (starting, Statik.copy(starting), Double.NaN, Objective.MAXIMUMLIKELIHOOD, Model.suggestedEPS, name, parameterNames, variableNames, 
                filter, minRuns, randomStarting, modelRun, null);
    }

    public void populateDescriptives(OnyxModel model) {
        this.parameterNames = model.getParameterNames();
        this.varNames = model.variableNames;
        this.filter = model.filter;
        this.parameterToAndFromDescription = model.getParameterToFromDescription();
    }
    
    public boolean usesEM() {
        return modelRun.model.strategyUseEMWithSaturated;
    }
    
    /**
     * Populates the RunUnit from the model so that the model is free to be used by other RunUnits.
     * 
     * @param model
     */
    public void populateFrom(Model model) {
        Statik.copy(model.getParameter(), position);
        this.fit = model.getFit(); 
        if (objective == Objective.MAXIMUMLIKELIHOOD) {
            this.ll = model.ll;
            if (model.llD != null && model.llD.length == anzPar) Statik.copy(model.llD, gradient);
            if (model.llDD != null && model.llDD.length == anzPar) Statik.copy(model.llDD, hessian);
            this.ls = model.getLeastSquares();
        } else {
            this.ls = model.ls;
            if (model.lsD != null && model.lsD.length == anzPar) Statik.copy(model.lsD, gradient);
            if (model.lsDD != null && model.lsDD.length == anzPar) Statik.copy(model.lsDD, hessian);
            try {this.ll = model.getMinusTwoLogLikelihood();} catch (Exception e) {this.ll = Double.NaN;}
        }
        if (usesEM()) {
            emDistribution = Distribution.copy(model.distributionPositionEM, emDistribution);
            if (model.emMethodLastPosition != null) 
                if (emMethodLastPosition == null) emMethodLastPosition = Statik.copy(model.emMethodLastPosition); 
                else Statik.copy(model.emMethodLastPosition, emMethodLastPosition);
            emMethodLastFit = model.emMethodLastFit;
            emMethodIsConverged = model.emMethodIsConverged;
            emSteps = model.stepsEM;
        }
        this.lastGain = model.lastGain;
        this.lastSteplength = model.lastSteplength;
        this.convergenceIndex = model.convergenceIndex;
        this.lastDamping = model.lastDamping;
        warningFlag = model.warningFlag;
        logDetHessian = model.logDetHessian;

        // previously, here was a block that recomputed all fit indices. It was moved to the function recomputeFitIndices, hoping that it's not needed
        // at every populate.

        observedStatistics = model.getObservedStatistics();
        anzPer = model.anzPer;
        data = model.data;
    }
    
    public void reset() {
        this.position = Statik.copy(starting); this.fit = Double.NaN;
        steps = 0; lastSteplength = Double.POSITIVE_INFINITY; 
        lastGain = Double.POSITIVE_INFINITY; convergenceIndex = 0;
        converged = false;
        startTime = System.nanoTime();
        ownTime = clockTime = ownTimeAtConvergence = clockTimeAtConvergence = 0.0;
        failed = false;
        history.reset();
        emMethodIsConverged = false;
        emMethodLastFit = Double.POSITIVE_INFINITY;
        emSteps = 0;

        prevGains = new double[ANZPREVGAINS];
        for (int i=0; i<ANZPREVGAINS; i++) prevGains[i] = Double.POSITIVE_INFINITY;
    }
    
    public void setStartingValues(String[] paraNames, ParameterSet values) {
        for (int i=0; i<paraNames.length; i++) starting[i] = values.getParameter(paraNames[i]);
    } 
    
    public boolean isConverged() {return isConverged(MINRUNS);}
    public boolean isConverged(int minRuns) {
        if (failed) return false;
        boolean nowConverged = false;
        // may have space for improvement
        if (modelRun.model.strategyUseClassicalOnyx == true) {
            nowConverged = (steps >= minRuns) && (lastGain/Math.min((lastDamping==0.0?1.0:Math.abs(lastDamping)), 1.0) < EPS);
        } else {
            if (usesEM()) nowConverged = emMethodIsConverged;
            else {
                int p = history.getSize()-1;
                nowConverged = (steps >= minRuns) && 
                               (lastGain/Math.min((lastDamping==0.0?1.0:Math.abs(lastDamping)), 1.0) < EPS) &&
                               (history.getLastSteplength(p) <= history.getLastSteplength(p-1)) &&
                               (lastSteplength < EPS);
            }
        }
        if (nowConverged && !converged) {
            clockTimeAtConvergence = (System.nanoTime() - startTime) / 1000000000.0;
            ownTimeAtConvergence = ownTime;
            stepsAtConvergence = steps;
        }
        converged = converged || nowConverged;
        return converged;
    }

    public boolean isMLWithUserStartingValues() {return (isMaximumLikelihoodObjective() && name.contains("ML with user starting values"));}
    
    public boolean isMaximumLikelihoodObjective() {return objective == Objective.MAXIMUMLIKELIHOOD;}
    
    // needs valid rank!
    public void assignImportance() {
        importance = 1.0/(double)(rank+1.0);
        if (isConverged()) importance /= 10;
        if (objective != Objective.MAXIMUMLIKELIHOOD) importance /= 5;
        if (hasFailed()) importance = 0;
    }
    
    public boolean hasFailed() {return warningFlag == Model.warningFlagTypes.FAILED;}

    public String toString() {return name+(failed?" (FAILED)":"");}
    
    public double getParameterValue(String parameterName) {return getParameterValue(parameterName, false);}
    public double getParameterValue(String parameterName, boolean fromStarting) {
        for (int i=0; i<parameterNames.length; i++) if (parameterNames[i].equals(parameterName)) if (fromStarting) return starting[i]; else return position[i];
        return Model.MISSING;
    }
    
    public double[][] getParameterCovariance() {
        double[][] erg = null;
        try {
            erg = Statik.invert(hessian);
        } catch (Exception e) {return null;}
        Statik.multiply(2,  erg, erg);          // since the parameter covariance matrix is the inverse of the Hessian of the -LL, not -2LL. 
        return erg;
    }
    
    public List<String> getSortedParameterNames()
    {
        List<String> list = new ArrayList<String>(parameterNames.length);
        for (int i=0; i<parameterNames.length; i++) list.add(parameterNames[i]);
        Collections.sort(list);
        return list;
    }
    
    public List<String> getParameterNames() {
        List<String> list = new ArrayList<String>(parameterNames.length);
        for (int i=0; i<parameterNames.length; i++) list.add(parameterNames[i]);
        return list;
    }
    
    public int getParameterNameIndex(String parameterName)
    {
    	 List<String> list = new ArrayList<String>(parameterNames.length);
         for (int i=0; i<parameterNames.length; i++) list.add(parameterNames[i]);
    	 return list.indexOf(parameterName);
    }

    public void initEstimation(Model model)
    {
        history = (parameterNames==null?new OptimizationHistory(anzPar):new OptimizationHistory(parameterNames));
        model.setHistory(history);
        reset();
        if (model.anzPar > 0) model.initEstimation(position, objective == Objective.MAXIMUMLIKELIHOOD);
        else model.computeLogLikelihoodDerivatives(new double[0], true);
        populateFrom(model);
    }
    
    public void copyIntoModel(Model model) {
        model.history = history;
        Statik.copy(gradient, (objective == ModelRunUnit.Objective.MAXIMUMLIKELIHOOD?model.llD:model.lsD));
        Statik.copy(hessian, (objective == ModelRunUnit.Objective.MAXIMUMLIKELIHOOD?model.llDD:model.lsDD));
        model.evaluateMuAndSigma(position);
        if (model.strategyUseEMWithSaturated) {
            if (emDistribution == null) emDistribution = model.distributionPositionEM.copy();
            model.distributionPositionEM.copyFrom(emDistribution);
            Statik.copy(emMethodLastPosition, model.emMethodLastPosition);
            model.emMethodLastFit = emMethodLastFit;
            model.emMethodIsConverged = emMethodIsConverged;
            model.stepsEM = emSteps;
        }
    }
    
    public boolean performStep(Model model) {
        // debug
//        if (isMLWithUserStartingValues()) 
//            model.logStream = System.out;

        copyIntoModel(model);
        long localTime = System.nanoTime();
        boolean ok = model.stepOptimization(EPS, objective == ModelRunUnit.Objective.MAXIMUMLIKELIHOOD);    // TODO reaction on singular sigma?
        
        // debug
//        model.logStream = null;
        
        if (!ok) {
            this.pertubateStartingValues(model); 
            model.computeFitGradientAndHessian(starting, objective == ModelRunUnit.Objective.MAXIMUMLIKELIHOOD);
            if (isMLWithUserStartingValues()) System.out.println("Failure marker in move, restarting with perturbed starting values.");
        }
        populateFrom(model);
        if (Double.isInfinite(fit) || Double.isNaN(fit)) warningFlag = warningFlagTypes.FAILED;
        if (!Double.isNaN(model.lastGain)) {
            prevGains[steps%ANZPREVGAINS] = lastGain/Math.min((lastDamping==0.0?1.0:Math.abs(lastDamping)), 1.0);
        }
        steps++;
        ownTime += (System.nanoTime() - localTime) / 1000000000.0;
        clockTime = (System.nanoTime() - startTime) / 1000000000.0;
        return isConverged();
    }
    
    public void pertubateStartingValues(Model model) {
        for (int i=0; i<starting.length; i++) {
            if (model.isErrorParameter(i)) starting[i] += model.getRandom().nextDouble();
            else starting[i] += model.getRandom().nextGaussian()*starting[i]/4.0; 
        }
        int saveSteps = steps;
        reset();
        steps = saveSteps;
    }
    
    public boolean hasWarning() {return isHessianNonPositiveDefinite();}
    
    public boolean isHessianNonPositiveDefinite() {
        return !Statik.isPositiveDefinite(hessian);
    }
    
    public boolean isFailed() {return failed;}
    
    public String getParameterDescription() {
        double[][] hessianInverse = new double[anzPar][anzPar];
        try {
            hessianInverse = Statik.invert(hessian);
        } catch (Exception e) {
            Statik.setTo(hessianInverse, Double.NaN);
        }
        String[][] vals = new String[anzPar+1][7];
        vals[0] = new String[]{"#","name","From / To","Estimate","Std.Error","lbound","rbound"};
        for (int i=0; i<anzPar; i++) {
            vals[i+1][0] = ""+i;
            vals[i+1][1] = parameterNames[i];
            vals[i+1][2] = parameterToAndFromDescription[i];
            vals[i+1][3] = ""+Statik.doubleNStellen(this.position[i],5);
            try {vals[i+1][4] = ""+Statik.doubleNStellen(Math.sqrt(2*hessianInverse[i][i]), 5);} catch (Exception e) {vals[i+1][4] = "NaN";}
            vals[i+1][5] = "";
            vals[i+1][6] = "";
        }
        String erg = Statik.makeTable(vals,"|","\r\n",true,true);
        return erg;
    }
    
    public String getShortSummary(boolean asHTML) {

        double[][] cov = getParameterCovariance();
        
        if (asHTML) {
            String params;
                
            params = "<html><h3>Estimates</h3>Estimation method: "
                    + name+"<br>"+"Fit:"+fit+"<br><hr><br>";
            for (String parameterName : getSortedParameterNames()) {
                
                int idx = getParameterNameIndex(parameterName); 
                String covString ="";
                if (idx != -1) {
                    covString = "\u00B1"+Double.toString(Math.sqrt(cov[idx][idx]));
                }
                
                params += parameterName + ":"
                        + (Math.round(getParameterValue(parameterName)*1000.0)/1000.0)
//                          + estimates.
                        + covString
                        + "<br>";
            }
            
            params += "<br><hr></html>";

            return params;
            
        } else {
            
            
            String erg = "Estimation = "+getName()+"\r\n";
            erg += "Fit = "+fit+"\r\n\r\n";
            erg += "Name\tEstimate\tStd. error\r\n";
            for (int i=0; i<anzPar; i++) 
                erg += parameterNames[i]+"\t"+position[i]+"\t"+Math.sqrt(cov[i][i])+"\r\n";
            
            return erg;
        }
    }
    
    public String getVariableDescription() {
        String[] names = new String[filter.length]; for (int i=0; i<filter.length; i++) names[i] = varNames[filter[i]];
        return RawDataset.getVariableDescription(data, modelRun.model.getImplicitlyEstimatedMeans(), names);
/*        
        final int varPerBlock = 4;
        
        String erg = "";
        int anzVar = filter.length;
        for (int i=0; i<anzVar; i+=varPerBlock) {
            String[] lines = new String[10]; for (int j=0; j<lines.length; j++) lines[j] = "";
            for (int j=i; j<i+varPerBlock && j<anzVar; j++) {
                String[] vals = RawDataset.getVariableDescriptivesArray(data, modelRun.model.getImplicitlyEstimatedMeans(), varNames[filter[j]], j, 5);
                int max = 0; 
                for (int k=0; k<vals.length; k++) max = Math.max(max, vals[k].length());
                for (int k=0; k<vals.length; k++) lines[k] += vals[k] + Statik.repeatString(" ", 3+max-vals[k].length());
            }
            for (int k=0; k<lines.length; k++) erg += lines[k] += "\r\n";
        }
        return erg;
        */
    }
    
    /**
     * Recomputes all fit indices.
     */
    public void recomputeFitIndices() {
        // srmr and kulbackLeibler are computed in populate because they need model covariance matrix directly. 
        aic = aicc = bic = bicadj = cfi = tli = rmsea = chisqr = chisqrIndep = klIndep = rmseaDfCorrected = rmseaKL = kulbackLeibler = Double.NaN;
        rmseaCI = new double[] { Double.NaN, Double.NaN}; 
        if (!Double.isNaN(ll)) {
            OnyxModel model = modelRun.model.copy(); model.evaluateMuAndSigma(position);
            aic = Model.getAIC(ll, anzPar);
            aicc = Model.getAICc(ll, anzPar, anzPer);
            bic = Model.getBIC(ll, anzPar, anzPer);
            bicadj = Model.getBICadjusted(ll, anzPar, anzPer);
            try {cfi = Model.getCFI(ll, modelRun.precomputedSaturatedLL, 
            		modelRun.precomputedIndependentLL, modelRun.df, modelRun.independentDF);} catch(Exception e) {}
            try {rmsea = Model.getRMSEA(ll, modelRun.precomputedSaturatedLL, modelRun.df, anzPer);} catch(Exception e) {}
            try {tli = Model.getTLI(ll, modelRun.precomputedSaturatedLL, modelRun.precomputedIndependentLL, modelRun.df, modelRun.independentDF);} catch(Exception e) {}
            try {chisqr = Model.getChisquare(ll, modelRun.precomputedSaturatedLL);} catch(Exception e) {}
            try {srmr = Model.getSRMR(model.sigma, modelRun.precomputedSaturatedCov);} catch(Exception e) {}
            try {kulbackLeibler = Model.getKulbackLeibler(model.mu, model.sigma, modelRun.precomputedSaturatedMean, modelRun.precomputedSaturatedCov); } 
            catch(Exception e) {System.out.println("Exception in KL computation.");}
            klIndep = modelRun.precomputedIndependentKulbackLeibler;
            try {chisqrIndep = Model.getIndependentChisquare(modelRun.precomputedSaturatedLL, modelRun.precomputedIndependentLL);} catch (Exception e) {}
            try {rmseaDfCorrected = Model.getRMSEADF(ll, modelRun.precomputedSaturatedLL, modelRun.nonmissRatio, modelRun.df, anzPer);} catch (Exception e) {rmseaDfCorrected = Double.NaN;}
            try {rmseaKL = Model.getRMSEAKL(kulbackLeibler, modelRun.nonmissRatio, modelRun.df, anzPer);} catch (Exception e) {rmseaKL = Double.NaN;}
            try {
            	rmseaCI = new double[]{Double.NaN,Double.NaN};
            			
            			//Model.getRMSEACI(ll, modelRun.precomputedSaturatedLL, modelRun.df, anzPer);
            } catch (Exception e) {}
        }
    }
    
    public String getDescription() {

    	final int digits = 3;
    	
    	recomputeFitIndices();
        String erg = getVariableDescription();
        erg += "\r\n"+getParameterDescription();

        erg += "\r\nObserved Statistics           : "+observedStatistics+"\r\n";
        erg += "Estimated Parameters          : "+this.anzPar+"\r\n";
        erg += "Non-Missing Ratio             : "+Statik.round(modelRun.nonmissRatio, digits)+"\r\n";
        erg += "Number of Observations        : "+this.anzPer+"\r\n";
        erg += "Minus Two Log Likelihood      : "+Statik.round(ll,digits)+"\r\n";
        erg += "Log Likelihood                : "+Statik.round(-0.5*ll,digits)+"\r\n";
        //if (MainFrame.DEVMODE) 
        erg += "Independent -2LL              : "+Statik.round(modelRun.precomputedIndependentLL, digits)+"\r\n";
       // if (MainFrame.DEVMODE) 
        erg += "Saturated -2LL                : "+Statik.round(modelRun.precomputedSaturatedLL, digits)+"\r\n";

        erg += "\u03C7\u00B2                            : "+Statik.round(this.chisqr,digits)+"\r\n";
        erg += "Restricted Degrees of Freedom : "+modelRun.df+"\r\n";
        erg += "AIC                           : "+Statik.round(this.aic,digits)+"\r\n";
        erg += "AICc                          : "+Statik.round(this.aicc, digits)+"\r\n";
        erg += "BIC                           : "+Statik.round(this.bic,digits)+"\r\n";
        erg += "BIC (sample-size adjusted)    : "+Statik.round(this.bicadj,digits)+"\r\n";
        erg += "Kulback-Leibler to Saturated  : "+Statik.round(this.kulbackLeibler, digits)+"\r\n";
        //if (MainFrame.DEVMODE) 
        erg += "\u03C7\u00B2 from independent           : "+Statik.round(this.chisqrIndep,digits)+"\r\n";
        erg +="Degrees of Freedom  (indep.)   :"+modelRun.independentDF+"\r\n";
        if (MainFrame.DEVMODE) erg += "Kulback-Leibler from indep.   : "+Statik.round(this.klIndep,digits)+"\r\n";
        erg += "RMSEA (df corrected)          : "+Statik.round(this.rmseaDfCorrected,digits)+"\r\n";
        erg += "RMSEA (Kulback Leibler)       : "+Statik.round(this.rmseaKL,digits)+"\r\n";
        erg += "RMSEA (classic)               : "+Statik.round(this.rmsea, digits);
        
        if (MainFrame.DEVMODE)
        erg+=" ("+Statik.round(this.rmseaCI[0],digits)+";"
        		+ Statik.round(this.rmseaCI[1], digits)+")";
        
        erg+="\r\n";
        erg += "SRMR (covariances only)       : "+Statik.round(this.srmr,digits)+"\r\n";
        erg += "CFI (to independent model)    : "+Statik.round(this.cfi, digits)+"\r\n";
        erg += "TLI (to independent model)    : "+Statik.round(this.tli,digits)+"\r\n\r\n";
        erg += "Timestamp                     : "+Statik.today(true)+"\r\n";
        erg += "Runner Individual Time        : "+ownTime+"\r\n";
        erg += "Wall Clock Time               : "+clockTime+"\r\n";
        if (isConverged()) {
            erg += "Runner Time at convergence    : "+ownTimeAtConvergence+"\r\n";
            erg += "Wall Clock at convergence     : "+clockTimeAtConvergence+"\r\n";
        }
        if (MainFrame.DEVMODE) erg += "Runner ID                     : "+this+"\r\n";
        
        erg += "\r\n";
        
        erg += (modelRun.isBestEstimate(this)?"This estimate is the best found.\r\n":"This estimate is a local optimum, better estimates exist.\r\n");
        erg += (isReliable()?"This estimate is reliably converged.\r\n":"This estimate is still improving.\r\n");
        erg += (isHessianNonPositiveDefinite()?"The model is overspecified at this estimate.\r\n":"");
        int anzLocal = modelRun.getAnzLocalOptima();
        if (anzLocal > 1) {
            erg += "There are "+anzLocal+" local maxium likelihood optima found so far, "+modelRun.getAnzLocalReliableOptima()+" of them reliable.\r\n";
        } else erg += "This is the only maximum likelihood optimum found.\r\n";
        erg += "This estimate has been found with "+this.anzClustered+" starting value sets converged in total.\r\n";
        if (modelRun.isHappy()) erg += "The overall estimation situation has stabilized.\r\n";
        if (this instanceof ExternalRunUnit) {
            ExternalRunUnit eThis = (ExternalRunUnit)this;
            erg += "This estimate was found using "+eThis.getAgentLabel()+".\r\n";
            if (eThis.agentMessage != null && eThis.agentMessage.length()>0) erg += eThis.getAgentLabel()+" produced the message: "+eThis.agentMessage+"\r\n";
            if (eThis.agentStatus == AgentStatus.FAIL) erg += "The estimation is marked as failed.";
        }
        
//        if (MainFrame.DEVMODE) {
//            erg += "\r\n";
//            erg += "Fit for saturated Model (mean): "+Statik.matrixToString(this.saturatedMean)+"\r\n";
//            erg += "Fit for saturated Model (cov) : \r\n"+Statik.matrixToString(this.saturatedCov);
//            
//            erg += "Missingness patterns : \r\n";
//            erg += modelRun.getMissingnessPatternDescription();
//        }
        
        
        return erg;
    }

	@Override
	public String getName() {
		return name;
	}

	public boolean isSameAs(ModelRunUnit comp) {
	    if (anzPar != comp.anzPar) return false;
	    if (isMaximumLikelihoodObjective() != comp.isMaximumLikelihoodObjective()) return false;
        if (isMaximumLikelihoodObjective() && Math.abs(ll-comp.ll)>COMPAREEPS) return false;
        if (!isMaximumLikelihoodObjective() && Math.abs(ls-comp.ls)>COMPAREEPS) return false;
	    for (int i=0; i<anzPar; i++) 
	        if (   Math.abs(position[i] - comp.position[i]) > COMPARERELATIVEEPS/10 &&
	               Math.abs((position[i] - comp.position[i])*2/(position[i]+comp.position[i])) > COMPARERELATIVEEPS) return false;
	    if (this instanceof ExternalRunUnit || comp instanceof ExternalRunUnit) return false;
	    
	    return true;
	}
	
	public boolean isRandomStartingValuesUnit() {
        return randomStartingValuesUnit;
    }

    @Override
	public boolean isStartingParameters() {return false;}
	
	public double getAveragePreviousGains() {
	    double sum = 0; for (int i=0; i<ANZPREVGAINS; i++) sum += prevGains[i];
	    return sum / (double)ANZPREVGAINS;
	}
	
	/**
	 * is true if the average gain in the last ANZPREVSTEPS (default to 10) steps is below EPS, and unit is converged.
	 * @return
	 */
	public boolean isReliable() {
	    return (isConverged() && getAveragePreviousGains()<EPS);
	}
	
    public static List<ModelRunUnit> stackEqualEstimates(List<ModelRunUnit> unstacked) {
        List<ModelRunUnit> erg = new ArrayList<ModelRunUnit>();
        if (unstacked != null) for (ModelRunUnit mru:unstacked) {
            boolean isIn = false;
            for (ModelRunUnit mru2:erg) if (mru2.isSameAs(mru)) {
                mru2.anzClustered += mru.anzSwallowed+1;
                isIn = true;
            }
            if (!isIn) {mru.anzClustered = mru.anzSwallowed+1; erg.add(mru);}
        }
        return erg;
    }

    public double[] getPosition() {return position;}
    public double[] getParameterValues() {return position;}
    public double getMinusTwoLogLikelihood() {return (usesEM()?emMethodLastFit:ll);}
  
    public double getModificationIndex(String parameterName) {

    	int paramIndex = -1;
    	for (int i=0; i<parameterNames.length; i++) if (parameterNames[i].equals(parameterName))
    		paramIndex=i;
    
    	if (paramIndex==-1) return(Double.NaN);
    	
    	return(getModificationIndex(paramIndex));
    }
    
    /* work in progress by AB */
    public double getModificationIndex(int paramIndex) {
   	
    	// get gradient
    	double g = gradient[paramIndex];
    	double hi = hessian[paramIndex][paramIndex];
    	
    	double[][] hessian_without_i = Statik.submatrix(hessian, paramIndex);
    	
    	double[][] e = Statik.invert(hessian_without_i);
    	
    	double[] f = new double[hessian.length-1];
    	int add = 0;
    	for (int i=0; i < f.length-1; i++) {
    		if (i==paramIndex) add = 1; 
    		f[i] = hessian[i][paramIndex+add];
    	}
    	
    	double mi = 0.5*g/(hi- Statik.multiply(f,e,f));
    	
    	return(mi);
    	
    }
    
    public OptimizationHistory getHistory() {return history;}
    public String getHistoryString() {return (history==null?"History not available":history.toString());}

    public String getModelName() {
        return modelRun.model.getName();
    }
    
    public Objective getObjective() {return this.objective;}

    public int getStepsAtConvergence() {return stepsAtConvergence;}

    public warningFlagTypes getWarningFlag() {return warningFlag;}

    public void setWarningFlag(warningFlagTypes flag) {warningFlag = flag;}

	@Override
	public double getFitIndex(FitIndex fitIndex) {
		
		recomputeFitIndices();
		
		if (fitIndex==ParameterReader.FitIndex.CFI) {
			return(cfi);
		} else if (fitIndex==ParameterReader.FitIndex.RMSEA) {
			return(rmsea);
		} else {
			return(Double.NaN);
		}
	}


}
