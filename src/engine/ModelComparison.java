/*
 * Created on 21.03.2012
 */
package engine;

import engine.ModelRun.Status;
import engine.ModelRun.Warning;
import engine.backend.Model;
import engine.backend.Model.Strategy;
import gui.graph.Edge;
import gui.graph.Node;

public class ModelComparison implements ModelListener {
    
    public enum NestingTestStatus {INVALID, VALIDATING, RESTARTVALIDATINGWHENFINISHED, VALID};
    
    NestingTestStatus nestingTestStatus;
    public OnyxModel first, second;
    boolean isNested;
    
    public ModelComparison(OnyxModel first, OnyxModel second) {
        this.first = first;
        this.second = second;

        nestingTestStatus = NestingTestStatus.INVALID;
        isNested = false;
    }
    
    public void startTestNesting() {
        if (nestingTestStatus == NestingTestStatus.VALIDATING || nestingTestStatus == NestingTestStatus.RESTARTVALIDATINGWHENFINISHED) {
            nestingTestStatus = NestingTestStatus.RESTARTVALIDATINGWHENFINISHED;
            return;
        } 
        
        final ModelComparison fthis = this; 
        Thread thread = new Thread(new Runnable(){
            public void run() {
                do {
                    System.out.println("DEBUG: Starting nesting test.");
                    fthis.nestingTestStatus = NestingTestStatus.VALIDATING;
                    if (first.anzPar != second.anzPar) {
                        try {
                            Model parent = first, nested = second;
                            if (parent.anzPar < nested.anzPar) {parent = second; nested = first;}
                            boolean isNested = parent.isNestedSubmodel(nested, 0.00001);
                            fthis.isNested = isNested;
                        } catch (Exception e) {fthis.nestingTestStatus = NestingTestStatus.RESTARTVALIDATINGWHENFINISHED;}
                    } else {isNested = false;}
                    System.out.println("DEBUG: Finished nesting test.");
                } while (fthis.nestingTestStatus == NestingTestStatus.RESTARTVALIDATINGWHENFINISHED);
                fthis.nestingTestStatus = NestingTestStatus.VALID;
                System.out.println("DEBUG: Leaving nesting thread.");
            }
        });
        thread.start();
    }
    
    public boolean nestingStatusValid() {return nestingTestStatus == NestingTestStatus.VALID;}
    
    public String getLikelihoodRatioComparison(ModelRunUnit mruParent, ModelRunUnit mruNested) {return ModelComparison.getLikelihoodRatioComparison(mruParent, mruNested, nestingStatusValid(), isNested);}
    
    /**
     * Creates a comparison string for a likelihood ratio comparison.
     * 
     * @param parent
     * @param nested
     * @return
     */
    public static String getLikelihoodRatioComparison(ModelRunUnit parent, ModelRunUnit nested, boolean nestingIsKnown, boolean isNested) {

        if (!parent.isMaximumLikelihoodObjective() || !nested.isMaximumLikelihoodObjective()) 
            return ("Likelihood Ratio comparison needs to be made on Maximum Likelihood Estimates in both models.");
        
        int df = parent.anzPar - nested.anzPar;
        if (df < 0) {ModelRunUnit t = parent; parent = nested; nested = t; df = -df;}
//        if (df == 0) return "Likelihood Ratio comparison can only be done on nested models, degrees of freedom are identical on these two models.";
        double lr = Math.max(0,nested.fit - parent.fit);
        boolean numberOfVariablesEqual = parent.modelRun.model.anzVar == nested.modelRun.model.anzVar;

        double p = Statik.chiSquareDistribution(df, 0.0, lr);
        
        lr = Statik.round(lr, 2);
        
        String erg = "degrees of freedom      = "+df+"<br>"; 
        erg +=       "-2 log likelihood ratio = "+lr+"<br>";
        if (!nestingIsKnown) {
            erg += "Nesting Status undetermined; if nested, p = "+p+"<br>";
        } else {
            if (isNested && df > 0) erg +=       "p - Value               = "+p+"<br>";
            if (!isNested || df == 0) {
                if (!isNested) {
                    erg += "Warning: Models are not nested";
                    if (!numberOfVariablesEqual) erg += " because the number of variables differ.<br>"; else erg += ".<br>";
                }
                if (df == 0) erg += "Warning: Number of parameters are equal.<br>";
                ModelRunUnit favor = (parent.aic > nested.aic?nested:parent);
                if (numberOfVariablesEqual) erg += "AIC difference is "+Statik.doubleNStellen(Math.abs(parent.aic - nested.aic),3)+" in favor of "+favor.getModelName()+".";
            }
        }
        
//        erg += " "+parent.fit+" vs "+nested.fit+"<br>";
        
        // AB 2020: add extra line to artificially increase tooltip size (on some machines,
        // the p value line vanishes 
        erg=erg+"<br>";

        return "<html>"+erg+"</html>";
    }

    public void addNode(Node node) {startTestNesting();}

    public void addEdge(Edge edge) {startTestNesting();}

    public void swapLatentToManifest(Node node) {startTestNesting();}

    public void changeName(String name) {}

    public void removeEdge(int source, int target, boolean isDoubleHeaded) {startTestNesting();}

    public void removeNode(int id) {startTestNesting();}

    public void deleteModel() {}

    public void cycleArrowHeads(Edge edge) {startTestNesting();}

    public void swapFixed(Edge edge) {startTestNesting();}

    public void changeStatus(Status status) {}

    public void notifyOfConvergedUnitsChanged() {}

    public void setValue(Edge edge) {startTestNesting();}

    public void notifyOfStartValueChange() {}

    public void changeParameterOnEdge(Edge edge) {startTestNesting();}

    public void notifyOfWarningOrError(Warning warning) {}

    public void newData(int percentMissing, boolean isRawData) {}

    public void changeNodeCaption(Node node, String name) {}

    public void setDefinitionVariable(Edge edge) {startTestNesting();}

    public void unsetDefinitionVariable(Edge edge) {startTestNesting();}

    public void notifyOfClearWarningOrError(Warning warning) {}

    public void setGroupingVariable(Node node) {startTestNesting();}

    public void unsetGroupingVariable(Node node) {startTestNesting();}

    public void notifyOfFailedReset() {}

    public void addDataset(Dataset dataset, int x, int y) {}

    public void addDataset(double[][] dataset, String datasetName, String[] additionalVariableNames, int x, int y) {}

    public void addAuxiliaryVariable(String name, int index) {}

    public void addControlVariable(String name, int index) {}

    public void removeAuxiliaryVariable(int index) {}

    public void removeControlVariable(int index) {}

    public void notifyOfStrategyChange(Strategy strategy) {}

}
