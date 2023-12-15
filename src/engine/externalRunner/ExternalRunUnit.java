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
/*
 * Created on 07.04.2012
 */
package engine.externalRunner;

import importexport.Export;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import engine.ModelRun;
import engine.ModelRunUnit;
import engine.OnyxModel;
import engine.OptimizationHistory;
import engine.ParameterReader;
import engine.Statik;
import engine.ModelRunUnit.Objective;
import engine.backend.MissingDataModelNew;
import engine.backend.Model;
import engine.backend.Model.warningFlagTypes;
import gui.graph.VariableContainer;
import gui.views.ModelView;

/**
 * Implements a runner that calls an outside SEM program. The outside program is called in a separate thread that changes the converged flag or loads
 * an error message into the corresponding field.
 * 
 * @author Timo
 */
public class ExternalRunUnit extends ModelRunUnit {

//    public boolean converged = false;
    public String agentMessage = null;
    public static enum AgentStatus {NOTYETSTARTED,RUNNING,SUCCESS,FAIL};
    public AgentStatus agentStatus = AgentStatus.NOTYETSTARTED;
    public String externalModelString = null;
    public static Hashtable<String,ExternalRunUnit> allRepresentants;
    public ModelView modelView;

    public ExternalRunUnit() {
        super(null, null, Double.NaN, Objective.MAXIMUMLIKELIHOOD, 0.01, null, null, null, null, 0, false, null, null);
    }
    
    public ExternalRunUnit(double[] starting, Objective objective, double EPS, String name, String[] parameterNames, String[] variableNames, 
            int[] filter, int minRuns, ModelRun modelRun) {
        super (starting, null, Double.NaN, objective, EPS, name, parameterNames, variableNames, filter, minRuns, false, modelRun, null);
        clockTime = Double.NaN;
        ownTime = Double.NaN;
    }
    
    public ExternalRunUnit(ExternalRunUnit toCopy) {
        super(toCopy);
        converged = toCopy.converged;
        agentMessage = toCopy.agentMessage;
        startTime = toCopy.startTime;
    }
    
    public void setInitialInformation(double[] starting, Objective objective, double EPS, String name, String[] parameterNames, String[] variableNames, 
            int[] filter, int minRuns, String externalModelString, ModelView modelView) {
        anzPar = (starting==null?0:starting.length);
        this.starting = starting;
        this.position = new double[anzPar];
        this.fit = 0.0;
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
        this.modelView = modelView;
        this.varNames = variableNames;
        this.filter = filter;
        

        this.externalModelString = externalModelString;
    }
    
    @Override
    public boolean isConverged() {return converged;}
    
    public void setAgentMessage(String error) {agentMessage = error;}
    public void setConverged(boolean converged) {this.converged = converged;}
    public double getTotalTimeNeeded() {return ownTimeAtConvergence;}
    public long getStartTime() {return startTime;}
    
    @Override
    public void initEstimation(Model model)
    {
        history = (parameterNames==null?new OptimizationHistory(anzPar):new OptimizationHistory(parameterNames));
        reset();
        if (model.anzPar > 0) {
            
            final ExternalRunUnit fthis = this;
            Thread thread = new Thread() {
                public void run() {
                    fthis.makeOutsideCallFrame();
                } 
            };
            agentStatus = AgentStatus.RUNNING;
            thread.start();
            
        } else {
            model.computeLogLikelihoodDerivatives(new double[0], true); 
            populateFrom(model);
        }
    }
    
    @Override
    public boolean performStep(Model model) {
        if (agentStatus == AgentStatus.NOTYETSTARTED) 
            System.out.println("Error in outside runner: Trying to perform step before agent was initialized.");
        if (agentStatus == AgentStatus.RUNNING) return false;
        if (!converged) {
            if (objective == Objective.MAXIMUMLIKELIHOOD) model.computeLogLikelihoodDerivatives(position, true);
            else model.computeLeastSquaresDerivatives(position, true);
            if (model instanceof OnyxModel) populateDescriptives((OnyxModel)model);
            populateFrom(model);
            converged = true;
        }
        return converged;    
    }

    /**
     * Calls an outside agent to estimate the model. Sets AgentStatus to SUCCESS or FAIL if finished, including an agent message in agentMessage. 
     * If possible, also gives the steps needed for converges.
     * 
     * The ExteranlRunUnit implementation is an empty implementation that calls the block estimation method of the model. Should be 
     * overwritten to implement specific outside agents. 
     */
    protected void makeOutsideCall() {
        OnyxModel model = modelView.getModelRequestInterface().getModel().copy();
        position = (objective == Objective.MAXIMUMLIKELIHOOD?model.estimateML(starting):model.estimateLS(starting));

        // sets the agent message and status according to the status returned from the agent.
        if (model.warningFlag != warningFlagTypes.OK) {agentMessage = "Convergence Error"; agentStatus = AgentStatus.FAIL;}
        else {agentMessage = "Converged"; agentStatus = AgentStatus.SUCCESS;} 
    }
    
    private void makeOutsideCallFrame() {
        // Create the outside agent's version of the model.
        OnyxModel model = modelView.getModelRequestInterface().getModel().copy();
        
        // takes time stamp and runs outside agent
        if (modelRun != null)
        	startTime = modelRun.getBigClockTime();
        makeOutsideCall();
        if (modelRun !=  null) {
        	clockTimeAtConvergence = (double)(modelRun.getBigClockTime()) / 1000000000.0;
        	ownTimeAtConvergence = (double)(modelRun.getBigClockTime() - startTime) / 1000000000.0;
        }
        
        // populates the field of this runner with the result
        model.computeFitGradientAndHessian(position, true);
        populateDescriptives(model);
        populateFrom(model);
    }

    /** 
     * @return whether this class is working and should be added to the frontend as selection for an outside agent call.  
     */
    public static boolean isValid() {return true;}
 
    /**
     * @return the label of the class that this agent should show in the frontend.
     */
    public String getAgentLabel() {return "Onyx Monolithic";}
    public static String getAgentLabel(ExternalRunUnit representant) {
        return representant.getAgentLabel();
    }

    public static ExternalRunUnit getInstance(ExternalRunUnit representant, ParameterReader starting, Objective objective, double EPS, String name, 
            String[] variableNames, int[] filter, int minRuns, String externalModelString, ModelView modelView) {
        List<String> parNames =  starting.getParameterNames(); 
        int anzPar = parNames.size();
        String[] parameterName = new String[anzPar]; double[] startingValue = new double[anzPar];
        for (int i=0; i<anzPar; i++) {parameterName[i] = parNames.get(i); startingValue[i] = starting.getParameterValue(parNames.get(i));}
        return getInstance(representant, startingValue, objective, EPS, name, parameterName, variableNames, filter, minRuns, externalModelString, modelView);
    }
        
    @SuppressWarnings("unchecked")
    public static ExternalRunUnit getInstance(ExternalRunUnit representant, double[] startingValue, Objective objective, double EPS, String name, 
            String[] parameterName, String[] variableNames, int[] filter, int minRuns, String externalModelString, ModelView modelView) {
        Class<ExternalRunUnit> c = (Class<ExternalRunUnit>)representant.getClass();
        ExternalRunUnit erg = null;
        try {erg = c.newInstance();} catch (Exception e) {return null;}
        erg.setInitialInformation(startingValue, objective, EPS, name, parameterName, variableNames, filter, minRuns, externalModelString, modelView);
        return erg;
    }

    public static Hashtable<String, ExternalRunUnit> getValidExternalAgents() {
        if (allRepresentants != null) return allRepresentants;
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = "engine.externalRunner";
        Enumeration<URL> resources = null;
        try {
            resources = classLoader.getResources(path.replace('.', '/')+'/');
        } catch (Exception e) {
            return null;
        }
        allRepresentants = new Hashtable<String,ExternalRunUnit>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            File dir = null;
            try {dir = new File(resource.toURI()); }catch (URISyntaxException e) {}
            File[] files = dir.listFiles();
            for (File file:files){
                if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
                    try {
                        Class<?> c = Class.forName("engine.externalRunner."+file.getName().substring(0, file.getName().length() - 6));
                        boolean isValid = ((Boolean)c.getMethod("isValid").invoke(null,new Object[0])).booleanValue();
                        if (isValid) {
                            ExternalRunUnit rep = (ExternalRunUnit)c.newInstance();
                            rep.name = "Representant for "+rep.getAgentLabel();
                            allRepresentants.put(rep.getAgentLabel(), rep);
                        }
                    } catch (Exception e) {
                        System.out.println("Unavailable Class: "+file.getName());
                    }
                }
            }
        }
        return allRepresentants;
    }

    public static ExternalRunUnit getRepresentantByName(String label) {
        return getValidExternalAgents().get(label);
    }
    
    // null is the default indicator (-999). Should be overwritten if other indicator is needed.
    public String getMissingIndicator() {return null;}
    
    /** 
     * Stores the combinedDataset in a temporary file and returns the path as it should appear in a script on that operation system (with double backslashes 
     * in windows). 
     * 
     * @return
     */
    public String createTemporaryDataFile(HashMap<VariableContainer, String> nameMapping) {
        File tempFile;

        try {
            tempFile = File.createTempFile("tmpData",".csv");
        } catch (IOException e) {
            System.out.println("Error opening temporary data file.");
            e.printStackTrace(System.out);
            return null;
        }

        modelView.writeCombinedDataset(tempFile,getMissingIndicator(), true, nameMapping);
        String filename = tempFile.getAbsolutePath();
        if (filename.contains("\\")) {
            filename = filename.replaceAll("\\\\","\\\\\\\\");
        }

        return filename;
    }
}
