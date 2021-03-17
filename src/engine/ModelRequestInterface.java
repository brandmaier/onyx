package engine;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import parallelProcesses.ParallelProcess;
import gui.graph.*;
import engine.ModelRun.*;
import engine.ModelRunUnit.Objective;
import engine.backend.Model;

public interface ModelRequestInterface {

    /**
     * Adds a node to the model following the description in the Node object. The new node ID will be stored in the Node object. 
     * 
     * @param node  Node object containing all information about the to-be-added edge.
     */
	public void requestAddNode(Node node);
	
	/**
	 * Adds an edge to the model following the description in the Edge object. If the edge is a definition variable, this method will
	 * call requestSetDefinitionVariable with the current parameterName in the edge.
	 * 
	 * @param edge     Edge object that contains information about the to-be-added edge, will be mirrowed to the model listener call.
	 * @return         true if the adding was successful, false if it was rejected. 
	 */
	public boolean requestAddEdge(Edge edge);
	
	/**
	 * Swaps the status of the node in the backend from latent to manifest or vice versa.
	 *  
	 * @param node
	 */

	public void requestSwapLatentToManifest(Node node);

	
	/**
	 * request a change of the model name
	 * 
	 * @param name
	 */
	public void  requestChangeModelName(String name);
	
	/**
	 * returns model name
	 * 
	 * @return
	 */
	public String getName();
	
	/**
	 * Cycles the arrow head from double headed to smaller -> greater to greater -> smaller back to double-headed. If the next status is blocked by 
	 * an existing edge, it is skipped. If both next stati are blocked, the method returns false. If source and target are identical, swap is only between
	 * double headed and single headed, returning false if blocked. Otherwise, the method returns true. The method calls removeEdge and addEdge on ModelListener.
	 * 
	 * @param edge     Edge that head's gets circled
	 * @return         true if the circling worked, false otherwise.
	 */
	public boolean requestCycleArrowHeads(Edge edge);
	
	/**
	 * Removes the corresponding node from the model if existent. Otherwise, it returns false, but the node is not in the model anyway.
	 * 
	 * @param node
	 * @return     true if successful, false otherwise
	 */
	public boolean requestRemoveNode(Node node);

    /**
     * Removes the corresponding edge from the model if existent. Otherwise, it returns false, but the edge is not in the model anyway.
     * 
     * @param edge
     * @return     true if successful, false otherwise. 
     */
    public boolean requestRemoveEdge(Edge edge);

    /**
     * Remove model and all related information!
     */
	public void requestDeleteModel();
	

	/**
	 * Triggers run. All data sets are ordered correctly. 
	 * 
	 * @param data                     observed variables
	 * @param definitionVariableData   definition variables (can be null)
	 */
	public void triggerRun(double[][] data, double[][] definitionVariableData, double[][] auxiliaryVariableData, double[][] controlVariableData);
	public void triggerRun(double[][] data, double[][] definitionVariableData);
    public void triggerRun(double[][] data);
    public void triggerRun();
    public void triggerRun(double[][] dataCov, double[] dataMean, int anzPer);

    /**
     * Sets the priority,
     * HOLD: No computations will be done
     * LOW : Long waiting times are included
     * NORMAL: Thread.yield is called after each big iteration
     * HIGH: Thread is not interrupted 
     * 
     */
    public void setRunPriority(Priority priority);
    
    /**
     * returns the priority (HOLD; LOW; NORMAL; HIGH)
     */
    public Priority getRunPriority();
    
	/**
	 * Swaps the fixed/free status of an edge if possible. Returns false if edge is a definition variable.
	 * The parameterName may be old or new when switching from fixed to free. If it is new, starting values will be set according to value, otherwise, starting values will not be changed.  
	 * 
	 * @param edge
	 */
	public boolean requestSwapFixed(Edge edge);
	
	/**
	 * returns integer list with IDs of all observed variables in the order as they should appear in the data set. 
	 * 
	 * @return
	 */
	public int[] getObservedIds();
	
	
	/**
	 * Changes the starting values set. Values will be copied. Returns false if array is of wrong length. 
	 * 
	 * @param values
	 * @return true if successful
	 */
//	public boolean requestSetStartingValues(ParameterSet values);
	
	/**
	 * Changes a single parameter in the starting values.
	 * 
	 * @param parameterName
	 * @param value
	 * @return
	 */
	public boolean requestSetStartingValue(String parameterName, double value);
	
	/**
	 * 
	 * Makes an edge a definition variable. The definitionVariableName will be set,
	 * and the definitionVariableColumn will be corrected.
	 * 
     * @param node             
     * @param definitionVariableName   name of the definition variable in the data set
     * @return                         index of the definition variable for the definitionVariable
     *                                 DataSet in triggerRun
	 * 
	 */
	public void requestSetDefinitionVariable(Edge edge, String definitionVariableName);
	
	/**
	 * Removes definition variable from edge, making it constant one in the Edge object.
	 * All definitionVariableColumns stored in the model will be corrected without notification.
	 *
	 * @param edge
	 */
	public void requestUnsetDefintionVariable(Edge edge);
	
	/**
	 * activates grouping indicator in the node object, fills slots and forwards to listeners.
	 * Is not important for the fit process as the group-corresponding missing are 
	 * inserted before calling triggerRun, but exist for consistency.
	 * 
	 * @param node             
	 * @param groupingVariableName     Variable name of the group in the data set
	 * @Param group                    Group that this variable has    
	 */
	public void requestSetGroupingVariable(Node node, String groupingVariableName, double group);
	
	/**
	 * Removes grouping indicator.
	 * @param node
	 */
	public void requestUnsetGroupingVariable(Node node);
	
	/**
	 * Collects the result of the so far best run if it is converged. Returns null if none have converged. 
	 *   @deprecated
	 * @return
	 */
//	public ParameterSet getEstimates();
	
	/**
	 * sets the value of an edge. Usually used for fixed edges. 
	 * On free edges, the starting value of the parameter will be changed.
	 */
	public boolean requestSetValue(Edge edge);
	
	/**
	 * sets a new parameter name on this edge. The parameter name can be old or new. Returns false if the edge label is a definition variable. On fixed edge label, the
	 * method will not change the parameter starting value, but will echo the event.  
	 * 
	 * @param edge
	 * @return
	 */
	public boolean requestChangeParameterOnEdge(Edge edge);
    

	/**
	 * Returns all converged runs as ModelRunUnit
	 * 
	 * @return
	 */
	public List<ModelRunUnit> getAllConvergedUnits();
    
	/**
	 * Returns the number of converged units
	 * 
	 * @return
	 */
    public int getAnzConverged();
    
    /**
     * Sets the fitting process on hold. Identical to setPriority(Priority.HOLD).
     */
    public void hold();
    
    /**
     * Kills the fitting process.
     * 
     */
    public void killModelRun();
    
    /**
     * Returns a ParameterReader implementation that gives the starting Values. 
     *  
     * @return
     */
    public ParameterReader getStartingValuesUnit();
    
    public boolean isEmpty();
    
    /**
     * @return whether the covariance matrix of the model is singular on all parameter sets
     */
    public boolean isCovarianceConstantSingular();
    /**
     * @return whether the model is overspecified
     */
    public boolean isOverspecified();
    /**
     * @return whether the runner hit an unexpected error.
     */
    public boolean isError();
    
    /**
     * returns the Model that corresponds to this request interface. 
     * @param mri2
     * @return
     */
    public OnyxModel getModel();

    /**
     * Adds the specified runner to the runners. 
     * This is the method to start an outside Agent, using that Agent's runUnit constructor. 

     * @param runner typically a subclass of ExternalRunUnit, will be added to the runner chain and reported back if terminated.
     * 
     * @return false if an error occured on adding this runner. 
     */
    public boolean addRunner(ModelRunUnit runner);

    /**
     * Requests to replace the data of the model by a simulated data set of given size. Will echo as dataChanged. 
     * 
     * @param anzPer number of participants
     */
    public void requestCreateData(int anzPer, int percentMissing, boolean isRawDataset);
    
    /**
     * Requests to replace the name of the variable associated with this node by a new name.
     * @param node
     */
    public void requestChangeNodeCaption(Node node, String name);
    
    
    /**
     * add a model listener
     */
    public void addModelListener(ModelListener listener);    
    
    /**
     * removes a model listener
     */
    public void removeModelListener(ModelListener listener);

    /**
     * Returns a string that describes the model distribution of the selected nodes (data distribution only if connected), using the parameterReader, in the format
     * 
     *  Model Mean  Model Covariance
     *  49          18  2   2
     *  42          2   15  2
     *  0           2   2   20
     *  
     * @param selected
     * @return
     */
    public String getModelDistribution(List<Node> selected, ParameterReader parameter);

    /**
     * Returns the model predicted distribution as array of array of doubles;
     * @return
     */
    public double[][] getNumericalModelDistribution(ParameterReader parameter);
    
    /**
     * Sets the parameterReader as parameters for the template model.
     * @param parameter
     */
    public void setParameter(ParameterReader parameter);
    
    /**
     * Returns time in nano seconds since last restart of the estimation process.
     * @return
     */
    public long getBigClockTime();
    
    /**
     * Links a model as saturated model
     */
    public void linkSaturatedModel(Model model);
    
    /** 
     * Sets the strategy to a default strategy package
     * @param strategy Target Strategy.
     */
    public void setStrategy(Model.Strategy strategy);
    
    /**
     * Gets the last strategy that was set by setStrategy. Does not check for manual changes to a preset.
     * @return strategy
     */
    public Model.Strategy getStrategy();
    
    /**
     * Returns an array with all variable names. 
     * @return
     */
    public String[] getVariableNames();

    /**
     * Returns a list of all run units. 
     * @return
     */
    public List<ModelRunUnit> getAllUnits();

    
    /**
     * Invalidates the data set and stops all running estimation processes. 
     */
    public void requestInvalidateDataSet();
    
    /**
     * Sets the model mean treatment. 
     * @param meanTreatment
     */
    public void setMeanTreatment(Graph.MeanTreatment meanTreatment);
    
    /**
     * Returns a list of all scores (original data + missing data imputation + latent scores) in the order of the data set for
     * the participants, and in the order of getVariableNames() for the variables. 
     * @return
     */
    public double[][] getLatentAndMissingScores(ParameterReader parameter);
    
    /**
     * May pass a vector of data means to the modelRequestInterface that will then be used to return scores from manifest variables including the means. 
     */
    public void setImplicitlyEstimatedMeans(double[] implicitlyEstimatedMeans);

    /**
     * Starts a separate Thread which clusters the data using a Dirichlet Process. Returns the ParallelProcess in which the clustering is started.
     * 
     * @param anzSamples
     * @param anzBurnin
     * @param alphaDirichlet
     * @param priorStrength
     */
    public ParallelProcess requestClusterWithDirichletProcess(int anzSamples, int anzBurnin, double alphaDirichlet, double priorStrength);
    public ParallelProcess requestClusterWithDirichletProcess(int anzSamples, int anzBurnin, double alphaDirichlet, double priorStrength, boolean doPreClustering, int preClusteringBurnin, int preClusteringSamples, int preClusteringThinning);
    
    /**
     * Adds an auxiliary variable to the model.
     * 
     * @param variableName
     */
    public void requestAddAuxiliaryVariable(String variableName);
    
    /**
     * Adds a control variable to the model.
     * 
     * @param dataset
     * @param index
     */
    public void requestAddControlVariable(String variableName);
    /**
     * Removes an auxiliary variable from the model.
     * 
     * @param index
     */
    public void requestRemoveAuxiliaryVariable(int index);
    /**
     * Removes a control variable from the model.
     * 
     * @param index
     */
    public void requestRemoveControlVariable(int index);

}