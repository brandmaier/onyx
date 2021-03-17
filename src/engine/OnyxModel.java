/*
 * Created on 27.02.2012
 */
package engine;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.StringReader;
import java.util.*;

import bayes.engine.BayesianModelRun;
import parallelProcesses.ParallelProcess;
import parallelProcesses.ParallelProcessHandler;
import parallelProcesses.ParallelProcessHandler.ProcessStatus;
//import sun.java2d.loops.ProcessPath.ProcessHandler;
import clustering.Clustering;
import dirichletProcess.ChineseRestaurant;
import dirichletProcess.DirichletProcess;
import dirichletProcess.SEMLikelihoodFunction;
import engine.ModelRun.Priority;
import engine.ModelRun.Status;
import engine.ModelRun.Warning;
import engine.backend.Model;
import engine.backend.RAMModel;
import gui.Desktop;
import gui.graph.*;
import gui.graph.Graph.MeanTreatment;
import gui.views.ModelView;


public class OnyxModel extends RAMModel implements ModelRequestInterface {
    
	public static final String defaultName = "Unnamed Model";
	
    public ModelListener[] modelListener;
    public String name = defaultName;

    public ModelRun modelRun;
    
    // used only for runUntil, in triggerRun used from the GUI, the missingness is placed already. 
    public List<Node> groupingNodes;
    
    public Edge[] definitionVariableEdges;
    public double[][] definitionVariableData;
    
    // contains latents and observed
    public String[] variableNames;
    public String[] auxiliaryVariableNames;
    public String[] controlVariableNames;
    
    private Graph.MeanTreatment meanTreatment;
    
    private double[] implicitlyEstimatedMeans;
    
    private int DPClusteringIndexNumber = 0;

	private ModelRun frequentistModelRun;
    
    public OnyxModel() {
        super(new int[0][0], new double[0][0], new int[0][0], new double[0][0], new int[0], new double[0], 0);
        startingValues = new double[0];
        modelListener = new ModelListener[0];
        definitionVariableEdges = new Edge[0];
        definitionVariableData = new double[anzPer][];
        variableNames = new String[0];
        auxiliaryVariableNames = new String[0]; controlVariableNames = new String[0];
        implicitlyEstimatedMeans = new double[0];
        modelRun = new ModelRun(this);
        this.setStrategy(Strategy.defaul);    // replace with classical Onyx Strategy if needed.
    }
    
    public OnyxModel(OnyxModel toCopy) {
        super(toCopy);
        startingValues = Statik.copy(toCopy.startingValues);
        modelListener = toCopy.modelListener;
        modelRun = toCopy.modelRun;
        definitionVariableEdges = toCopy.definitionVariableEdges;
        definitionVariableData = toCopy.definitionVariableData;
        meanTreatment = toCopy.meanTreatment;
        name = "Copy of "+toCopy.name;
        variableNames = Statik.copy(toCopy.variableNames);
        auxiliaryVariableNames = Statik.copy(toCopy.auxiliaryVariableNames); 
        controlVariableNames = Statik.copy(toCopy.controlVariableNames);
        implicitlyEstimatedMeans = Statik.copy(toCopy.implicitlyEstimatedMeans);
        copyStrategy(toCopy);
        lastChosenStrategyPreset = toCopy.lastChosenStrategyPreset;
    }
    
    public OnyxModel(RAMModel ramModel) {
        super(ramModel);
        meanTreatment = MeanTreatment.ambique;
        modelListener = new ModelListener[0];
        definitionVariableEdges = new Edge[0];
        definitionVariableData = new double[anzPer][];
        implicitlyEstimatedMeans = new double[anzVar];
        variableNames = new String[anzVar];
        for (int i=0; i<anzVar; i++) variableNames[i] = "X"+i;
        auxiliaryVariableNames = new String[anzAux];
        for (int i=0; i<anzAux; i++) auxiliaryVariableNames[i] = "AUX"+i;
        controlVariableNames = new String[anzCtrl];
        for (int i=0; i<anzCtrl; i++) controlVariableNames[i] = "CTRL"+i;
        startingValues = ramModel.getParameter();
        modelRun = new ModelRun(this);
        // TvO 02.11.2017: I think (!) this line can go out, it causes Threading-problems. Data should only be non-null in a script call, never from the GUI.
//        if (ramModel.data != null) this.triggerRun(ramModel.data);
        copyStrategy(ramModel);
    }
    
    public static OnyxModel load(File file) {
        Desktop desktop = new Desktop();
        ModelView mv = desktop.loadModel(file);
        ModelRequestInterface zwerg = mv.getModelRequestInterface();
        if (zwerg instanceof OnyxModel) {
            OnyxModel erg = ((OnyxModel)zwerg);
            List<Edge> defList = mv.getDefinitionEdges();
            erg.definitionVariableEdges = new Edge[defList.size()];
            defList.toArray(erg.definitionVariableEdges);
            erg.meanTreatment = mv.getGraph().getMeanTreatment();

            // TvO 26.06.2018: added to get list of nodes with grouping.
            erg.groupingNodes = new ArrayList<Node>();
            for (Node n:mv.getGraph().getNodes()) 
                if (n.isGrouping()) erg.groupingNodes.add(n);
            if (erg.groupingNodes.size()==0) erg.groupingNodes = null;
            
            return erg; 
        } else return null;
    }
    
    @Override
    public void setStrategy(Strategy strategy) {
        super.setStrategy(strategy);
        for (int i=0; i<modelListener.length; i++) modelListener[i].notifyOfStrategyChange(strategy);
//        if (modelRun != null) modelRun.requestReset();
        
        if (strategy == Strategy.MCMC) {
        	frequentistModelRun = modelRun;
        	modelRun = new BayesianModelRun(this);
        	
        } else {
        	if (frequentistModelRun != null)
        		{ 
        		modelRun = frequentistModelRun;
        		modelRun.requestReset(); 
        		}
        }
    }
    
    /**
     * Sets the double array data in the model from the Dataset object, restricted
     * to the actual data. Sets DefinitionVariableData according to the definition
     * variables. Finally, replaces cells in mainData with MISSING if the groups
     * are not in accordance to grouping variables.
     * 
     * @param data  Dataset object with all data. May be replaced by multiple datasets
     * and a linkhandler later.
     */
    public void setData(RawDataset data) {
        double[][] mainData = data.getData(this.getObservedVariableNames());
        if (definitionVariableEdges != null) {
            int anzDef = definitionVariableEdges.length;
            String[] definitionVariableNames = new String[anzDef];
            for (int i=0; i<anzDef; i++) definitionVariableNames[i] = definitionVariableEdges[i].definitionVariableName;
            definitionVariableData = data.getData(definitionVariableNames);
        }
        if (groupingNodes != null && groupingNodes.size() != 0) {
            for (Node groupNode:groupingNodes) {
                double[][] column = data.getData(new String[]{groupNode.groupName});
                for (int i=0; i<mainData.length; i++) {
                    if (Model.isMissing(column[i][0])) modelRun.definitionDataValid = false;
                    if (column[i][0] != groupNode.groupValue) mainData[i][groupNode.getId()] = MISSING;
                }
            }
        }
        double[][] auxiliaryData = data.getData(auxiliaryVariableNames);
        double[][] controlData = data.getData(controlVariableNames);
        this.setData(mainData);
    }
    
    public boolean hasDefinitionVariables() {
        return (definitionVariableEdges != null && definitionVariableEdges.length > 0);
    }
    
    public boolean hasAuxiliaryVariables() {return auxiliaryVariableNames.length > 0; }
    public boolean hasControlVariables() {return controlVariableNames.length > 0; }

    public String[] getObservedVariableNames() {
        String[] erg = new String[anzVar];
        for (int i=0; i<filter.length; i++) erg[i] = variableNames[filter[i]];
        return erg;
    }

    public synchronized OnyxModel copy() {
        return new OnyxModel(this);
    }

    public void addModelListener(ModelListener listener) {
    	for (int i=0; i < modelListener.length; i++) if (listener == modelListener[i]) return; //added AB 25 Apr 2019
    	
        ModelListener[] na = new ModelListener[modelListener.length+1];
        for (int i=0; i<modelListener.length; i++) na[i] = modelListener[i];
        na[na.length-1] = listener; 
        modelListener = na;
    }
    
    private int findOrAddParameterNumber(String parameterName) {
        int pnr = this.getParameterNumber(parameterName);
        if (pnr == -1) {
            pnr = anzPar;
            anzPar++;
            this.addParameterName(parameterName);
            double[] nPosition = new double[anzPar]; Statik.copy(position, nPosition); nPosition[anzPar-1] = pnr;
            double[] nStarting = new double[anzPar]; Statik.copy(startingValues, nStarting); nStarting[anzPar-1] = pnr;
            position = nPosition;
            startingValues = nStarting;
        }
        return pnr;
    }
    
    /*
    private void setRunnerStatus(Status status) {
        modelRun.setStatus(status);
        for (int i=0; i<modelListener.length; i++) modelListener[i].changeStatus(Status.RESTARTING);
    }
    */
    
    @Override
    public void setImplicitlyEstimatedMeans(double[] implicitlyEstimatedMeans) {this.implicitlyEstimatedMeans = implicitlyEstimatedMeans;}
    public double[] getImplicitlyEstimatedMeans() {return implicitlyEstimatedMeans;}

    private boolean existsEdge(Edge edge) {
        int target = edge.target.getId(), source = edge.source.getId();
        if (edge.source.isMeanTriangle() && (meanVal[target] != 0.0 || meanPar[target] != NOPARAMETER)) return true;
        if (symVal[target][source] != 0.0 || symPar[target][source] != NOPARAMETER ||
        	symVal[source][target] != 0.0 || symPar[source][target] != NOPARAMETER) return true;
        if (!edge.isDoubleHeaded() && (asyVal[target][source] != 0.0 || asyPar[target][source] != NOPARAMETER)) return true;
        return false;
    }
    
    public synchronized boolean requestAddEdge(Edge edge) {
        
    	if (edge.target==null || edge.source == null) {
    		System.err.println("Iinvalid edge with undefined source and/or target.");
    		return false;
    	}
    	
        if (edge.target.isMeanTriangle()) return false;
        if (edge.isFixed() && edge.getValue() == 0.0) return false;
        if (existsEdge(edge)) return false;
        if (edge.source == edge.target && !edge.isDoubleHeaded()) return false;
        
        int pnr = Model.NOPARAMETER;
        if (!edge.isFixed()) {
            pnr = findOrAddParameterNumber(edge.getParameterName());
            startingValues[pnr] = edge.getValue();
        } 
        int target = edge.target.getId(), source = edge.source.getId();
        
        if (edge.source.isMeanTriangle()) {
            meanPar[target] = pnr;
            meanVal[target] = edge.getValue();
            edge.source.addMeanEdge(edge);
        } else
        if (!edge.isDoubleHeaded()) {
            asyPar[target][source] = pnr;
            asyVal[target][source] = edge.getValue();
        } else {
            symPar[target][source] = symPar[source][target] = pnr;
            symVal[target][source] = symVal[source][target] = edge.getValue();
        }
        modelRun.modelHasAcceleratingCycle = hasAcceleratingCycle();
        if (modelRun.modelHasAcceleratingCycle) notifyOfWarning(Warning.ACCELERATINGCYCLE); else notifyOfClearWarning(Warning.ACCELERATINGCYCLE);
        for (int i=0; i<modelListener.length; i++) {modelListener[i].addEdge(edge); modelListener[i].notifyOfStartValueChange();}
        if (edge.isDefinitionVariable()) requestSetDefinitionVariable(edge, edge.getParameterName());
        modelRun.requestReset(); 
        return true;
    }

    public synchronized void requestAddNode(Node node) {
        anzFac++;
        double[][] nAsyVal = new double[anzFac][anzFac], nSymVal = new double[anzFac][anzFac];
        int[][] nAsyPar = new int[anzFac][anzFac], nSymPar = new int[anzFac][anzFac];
        double[] nMeanVal = new double[anzFac]; int[] nMeanPar = new int[anzFac];
        int[] nFilter = new int[(node.isLatent()?anzVar:anzVar+1)];
        String[] nVarNames = new String[anzFac];
        
        Statik.copy(asyVal, nAsyVal); Statik.copy(symVal, nSymVal); Statik.copy(asyPar,nAsyPar); Statik.copy(symPar, nSymPar); 
        Statik.copy(meanVal, nMeanVal); Statik.copy(meanPar, nMeanPar); Statik.copy(filter,nFilter); Statik.copy(variableNames, nVarNames);
        if (isMultiplicationVariable != null || node.isMultiplicationNode()) {
            boolean[] nIsMultiplicationVariable = new boolean[anzFac]; 
            if (isMultiplicationVariable != null) Statik.copy(isMultiplicationVariable, nIsMultiplicationVariable);
            isMultiplicationVariable = nIsMultiplicationVariable;
            isMultiplicationVariable[anzFac-1] = node.isMultiplicationNode();
        }
        
        
        asyPar = nAsyPar; asyVal = nAsyVal; symPar = nSymPar; symVal = nSymVal; meanVal = nMeanVal; meanPar = nMeanPar; filter = nFilter; 
        variableNames = nVarNames;
        for (int i=0; i<anzFac; i++) {
            asyPar[i][anzFac-1] = asyPar[anzFac-1][i] = NOPARAMETER; 
            symPar[i][anzFac-1] = symPar[anzFac-1][i] = NOPARAMETER;
            asyVal[i][anzFac-1] = asyVal[anzFac-1][i] = 0; 
            symVal[i][anzFac-1] = symVal[anzFac-1][i] = 0;
        }
        asyPar[anzFac-1][anzFac-1] = NOPARAMETER; symPar[anzFac-1][anzFac-1] = NOPARAMETER; 
        asyVal[anzFac-1][anzFac-1] = 0; symVal[anzFac-1][anzFac-1] = 0;
        meanPar[anzFac-1] = NOPARAMETER; meanVal[anzFac-1] = 0;
        variableNames[anzFac-1] = node.getCaption();
        if (!node.isLatent()) {
            filter[anzVar] = anzFac-1;
            anzVar++;
        }

        node.setId(anzFac-1);
        for (int i=0; i<modelListener.length; i++) modelListener[i].addNode(node);
        if (!node.isLatent()) invalidateDataSet(); 
        modelRun.requestReset();
    }

    public synchronized void requestSwapLatentToManifest(Node node) {
        anzVar += (node.isLatent()?+1:-1);
        int[] nFilter = new int[anzVar];
        
        if (node.isLatent()) {
            Statik.copy(filter, nFilter);
            nFilter[anzVar-1] = node.getId();
            Arrays.sort(nFilter);
        } else {
            Statik.subvector(filter, nFilter, node.getId());
        }
        filter = nFilter;
        
        for (int i=0; i<modelListener.length; i++) modelListener[i].swapLatentToManifest(node);
        invalidateDataSet(); modelRun.requestReset();
    }

    public synchronized boolean requestCycleArrowHeads(Edge edge) {
        if (edge.source.isMeanTriangle()) return false;
        int os = edge.source.getId(), ot = edge.target.getId();
        int small = os, large = ot; if (large < small) {small = ot; large = os;}
        
        // two single-headed edges, can't change anything
        if ((asyPar[os][ot] != NOPARAMETER || asyVal[os][ot] != 0) &&
        	(asyPar[ot][os] != NOPARAMETER || asyVal[ot][os] != 0)) return false;

        // 0 = double headed, 1 = smaller -> greater, 2 = greater -> smaller;
        int status = (edge.isDoubleHeaded()?0:(os <= ot?1:2));
        int nStatus = status+1; if (nStatus > 2) nStatus = 0;
        if (os == ot && nStatus == 2) nStatus = 0;
        if (os != ot  && nStatus == 1 && (asyPar[large][small] != NOPARAMETER || asyVal[large][small] != 0)) nStatus = 2;
        if (os != ot  && nStatus == 2 && (asyPar[small][large] != NOPARAMETER || asyVal[small][large] != 0)) nStatus = 0;
        
        
        for (int i=0; i<modelListener.length; i++) modelListener[i].removeEdge(edge.getSource().getId(), edge.getTarget().getId(), edge.isDoubleHeaded());
        if (status == 0) {
            int ns = os, nt = ot; 
            if (ns < nt) {int t = ns; ns = nt; nt = t;}
            if (nStatus==1) {int t = ns; ns = nt; nt = t;}
            asyPar[nt][ns] = symPar[ot][os]; asyVal[nt][ns] = symVal[ot][os];
            symPar[os][ot] = symPar[ot][os] = NOPARAMETER; symVal[os][ot] = symVal[ot][os] = 0;
            if (edge.source.getId() != ns) {Node t = edge.source; edge.source = edge.target; edge.target = t;}
            edge.setDoubleHeaded(false);
        } else {
            if ((os < ot && status == 2) || (os > ot && status == 1)) {int t = os; os = ot; ot = t;}
            if (nStatus==0) {
                symPar[os][ot] = symPar[ot][os] = asyPar[ot][os];  
                symVal[os][ot] = symVal[ot][os] = asyVal[ot][os]; 
                edge.setDoubleHeaded(true);
            }
            else {
                asyPar[os][ot] = asyPar[ot][os]; 
                asyVal[os][ot] = asyVal[ot][os]; 
                if (edge.source.getId() != ot) {Node t = edge.source; edge.source = edge.target; edge.target = t;}
                edge.setDoubleHeaded(false);
            }
            asyPar[ot][os] = NOPARAMETER; asyVal[ot][os] = 0;
        }
        modelRun.modelHasAcceleratingCycle = hasAcceleratingCycle();
        if (modelRun.modelHasAcceleratingCycle) notifyOfWarning(Warning.ACCELERATINGCYCLE); else notifyOfClearWarning(Warning.ACCELERATINGCYCLE);
        
        for (int i=0; i<modelListener.length; i++) modelListener[i].addEdge(edge);
        modelRun.requestReset();
        
        return true;
    }
    
    public synchronized boolean requestRemoveNode(Node node) {
        int id = node.getId();
        if (id >= anzFac) {
            if (!node.isMeanTriangle()) return false;
            for (Edge edge:node.meanEdges) {
                removeEdge(id, edge.target.getId(), false, true);
            }
        }
        
        // remove corresponding edges
        if (meanPar[id] != NOPARAMETER || meanVal[id] != 0) removeEdge(-1, id, false, true);
        for (int i=0; i<anzFac; i++) {
            if (asyPar[id][i] != NOPARAMETER || asyVal[id][i] != 0) removeEdge(i, id, false, false);
            if (i != id && asyPar[i][id] != NOPARAMETER || asyVal[i][id] != 0) removeEdge(id, i, false, false);
            if (symPar[id][i] != NOPARAMETER || symVal[id][i] != 0) removeEdge(i, id, true, false);
        }
        
        meanPar = Statik.subvector(meanPar, id); meanVal = Statik.subvector(meanVal, id);
        asyPar = Statik.submatrix(asyPar, id); asyVal = Statik.submatrix(asyVal, id);
        symPar = Statik.submatrix(symPar, id); symVal = Statik.submatrix(symVal, id);
        variableNames = Statik.subvector(variableNames, id);
        if (isManifest(id)) {
            int fid = -1; for (int i=0; i<filter.length; i++) if (filter[i]==id) fid = i;
            filter = Statik.subvector(filter, fid);
            anzVar--;
        }
        for (int i=0; i<filter.length; i++) if (filter[i] > id) filter[i]--;
        
        anzFac--;
        if (!node.isLatent()) invalidateDataSet();
        if (isMultiplicationVariable != null) isMultiplicationVariable = Statik.subvector(isMultiplicationVariable, id);

        for (int i=0; i<modelListener.length; i++) modelListener[i].removeNode(id);
        
        modelRun.requestReset();

        return true;
    }
    
    private synchronized void removeParameterIfNoLongerInModel(int pnr) {
        if (pnr != NOPARAMETER) {
            boolean isIn = false;
            for (int i=0; i<anzFac; i++) {
                isIn = isIn || (meanPar[i] == pnr);
                for (int j=0; j<anzFac; j++) {
                    isIn = isIn || (asyPar[i][j] == pnr);
                    isIn = isIn || (symPar[i][j] == pnr);
                }
            }
            if (!isIn) this.fixParameter(pnr);
        }
    }
    
    public synchronized boolean requestRemoveEdge(Edge edge) {
        if (edge.isDefinitionVariable()) requestUnsetDefintionVariable(edge);
        if (edge.source.isMeanTriangle()) edge.source.removeMeanEdge(edge);
        return removeEdge(edge.source.getId(), edge.target.getId(), edge.isDoubleHeaded(), edge.source.isMeanTriangle());
    }
    private synchronized boolean removeEdge(int source, int target, boolean isDoubleHeaded, boolean isMeanEdge) {

        int scol = source, tcol = target;
        int pnr = NOPARAMETER;
        
        if (isMeanEdge) {
            if (meanPar[tcol]==NOPARAMETER && meanVal[tcol]==0) return false;
            pnr = meanPar[tcol]; meanPar[tcol] = NOPARAMETER; meanVal[tcol] = 0;
        } else {
            if (isDoubleHeaded) {
                // REMARK: check for existence in the following line temporarily removed, allowing to remove edges even if they are constant and set to zero.
//                if (symPar[scol][tcol]==NOPARAMETER && symVal[scol][tcol]==0) return false;
                pnr = symPar[scol][tcol]; symPar[scol][tcol] = symPar[tcol][scol] = NOPARAMETER; symVal[scol][tcol] = symVal[tcol][scol] = 0;
            } else {
                // REMARK: check for existence in the following line temporarily removed, allowing to remove edges even if they are constant and set to zero.
//                if (asyPar[tcol][scol]==NOPARAMETER && asyVal[tcol][scol]==0) return false;
                pnr = asyPar[tcol][scol]; asyPar[tcol][scol] = NOPARAMETER; asyVal[tcol][scol] = 0;
            }
        }

        removeParameterIfNoLongerInModel(pnr);

        modelRun.modelHasAcceleratingCycle = hasAcceleratingCycle();
        if (modelRun.modelHasAcceleratingCycle) notifyOfWarning(Warning.ACCELERATINGCYCLE); else notifyOfClearWarning(Warning.ACCELERATINGCYCLE);
        
        for (int i=0; i<modelListener.length; i++) {modelListener[i].removeEdge(source, target, isDoubleHeaded); modelListener[i].notifyOfStartValueChange();}
        modelRun.requestReset();
        return true;
    }
    
    public synchronized void requestChangeModelName(String name) {
        if (!this.name.equals(name)) {
            this.name = name;
            for (int i=0; i<modelListener.length; i++) modelListener[i].changeName(name);
        }
    }
    
    public synchronized void requestChangeNodeCaption(Node node, String name) {
        variableNames[node.getId()] = name;
        for (int i=0; i<modelListener.length; i++) modelListener[i].changeNodeCaption(node, name);
    }
    
    public synchronized void requestDeleteModel() {
        killModelRun();
        for (int i=0; i<modelListener.length; i++) modelListener[i].deleteModel();
    }
    
    public synchronized boolean requestSwapFixed(Edge edge) {
        if (edge.isDefinitionVariable()) return false;
        
        if (edge.isFixed()) {
            boolean isNew = getParameterNumber(edge.getParameterName()) == -1; 
            int pnr = findOrAddParameterNumber(edge.getParameterName());
            if (edge.source.isMeanTriangle()) meanPar[edge.target.getId()] = pnr; 
            else if (edge.isDoubleHeaded()) symPar[edge.target.getId()][edge.source.getId()] 
                                               = symPar[edge.source.getId()][edge.target.getId()] = pnr; 
            else asyPar[edge.target.getId()][edge.source.getId()] = pnr;
            if (isNew) {
                position[pnr] = edge.getValue();
                startingValues[pnr] = edge.getValue();
            }
            edge.setFixed(false);
        } else {
            int pnr = getParameterNumber(edge.getParameterName());
            if (edge.source.isMeanTriangle()) {meanVal[edge.target.getId()] = edge.getValue(); meanPar[edge.target.getId()] = NOPARAMETER;} 
            else if (edge.isDoubleHeaded()) {
                symPar[edge.target.getId()][edge.source.getId()] = symPar[edge.source.getId()][edge.target.getId()] = NOPARAMETER; 
                symVal[edge.target.getId()][edge.source.getId()] = symVal[edge.source.getId()][edge.target.getId()] = edge.getValue();
            }
            else {asyPar[edge.target.getId()][edge.source.getId()] = NOPARAMETER; 
            asyVal[edge.target.getId()][edge.source.getId()] = edge.getValue();}
                        
            removeParameterIfNoLongerInModel(pnr);
            edge.setFixed(true);
        }
        
        modelRun.modelHasAcceleratingCycle = hasAcceleratingCycle();
        if (modelRun.modelHasAcceleratingCycle) notifyOfWarning(Warning.ACCELERATINGCYCLE); else notifyOfClearWarning(Warning.ACCELERATINGCYCLE);
        
        for (int i=0; i<modelListener.length; i++) {modelListener[i].swapFixed(edge); modelListener[i].notifyOfStartValueChange();}
        modelRun.requestReset();

        return true;
    }
    
    public synchronized boolean requestChangeParameterOnEdge(Edge edge) {
        if (edge.isDefinitionVariable()) return false;
        if (!edge.isFixed()) {
            
            int oldPnr = -1, s = edge.source.getId(), t = edge.target.getId();
            if (edge.source.isMeanTriangle()) {oldPnr = meanPar[t]; meanPar[t] = Model.NOPARAMETER;}
            else if (edge.isDoubleHeaded()) {oldPnr = symPar[t][s]; symPar[t][s] = symPar[s][t] = Model.NOPARAMETER;}
            else {oldPnr = asyPar[t][s]; asyPar[t][s] = Model.NOPARAMETER;}
            if (oldPnr == -1) return false;
            removeParameterIfNoLongerInModel(oldPnr);

            boolean isNew = getParameterNumber(edge.getParameterName()) == -1; 
            int pnr = findOrAddParameterNumber(edge.getParameterName());
            if (edge.source.isMeanTriangle()) meanPar[t] = pnr; 
            else if (edge.isDoubleHeaded()) symPar[t][s] = symPar[s][t] = pnr; 
            else asyPar[t][s] = pnr;
            if (isNew) {
                position[pnr] = edge.getValue();
                startingValues[pnr] = edge.getValue();
            }
            modelRun.modelHasAcceleratingCycle = hasAcceleratingCycle();
            if (modelRun.modelHasAcceleratingCycle) notifyOfWarning(Warning.ACCELERATINGCYCLE); else notifyOfClearWarning(Warning.ACCELERATINGCYCLE);
            
            for (int i=0; i<modelListener.length; i++) {modelListener[i].changeParameterOnEdge(edge); modelListener[i].notifyOfStartValueChange();}
            modelRun.requestReset();
        }
        return true;
    }

    public synchronized boolean requestSetValue(Edge edge) {
        
        int s = edge.source.getId(), t = edge.target.getId();
        if (edge.source.isMeanTriangle()) {meanVal[t] = edge.getValue();}
        else if (edge.isDoubleHeaded()) symVal[t][s] = symVal[s][t] = edge.getValue();
        else asyVal[t][s] = edge.getValue();
        if (edge.isFree()) requestSetStartingValue(edge.getParameterName(), edge.getValue());
        
        modelRun.modelHasAcceleratingCycle = hasAcceleratingCycle();
        if (modelRun.modelHasAcceleratingCycle) notifyOfWarning(Warning.ACCELERATINGCYCLE); else notifyOfClearWarning(Warning.ACCELERATINGCYCLE);
        for (int i=0; i<modelListener.length; i++) modelListener[i].setValue(edge);
        modelRun.requestReset();
        return true;
    }

/*   
    public synchronized boolean requestSetStartingValues(ParameterSet values) {
        if (values.getLength() != anzPar) return false;
        for (int i=0; i<paraNames.length; i++) startingValues[i] = values.getParameter(paraNames[i]);
        return true;
    }
  */
    
    public synchronized boolean requestSetStartingValue(String parameterName, double value) {
        int pnr = getParameterNumber(parameterName);
        if (pnr == -1) return false;
        startingValues[pnr] = value;
        for (int i=0; i<modelListener.length; i++) {modelListener[i].notifyOfStartValueChange();}
        return true;
    }
    
    public synchronized void requestInvalidateDataSet() {invalidateDataSet();}
    public synchronized void invalidateDataSet() {
        data = null;
        modelRun.invalidateDataSet();
    }
    
    @Override
    public void requestAddAuxiliaryVariable(String variableName) {
        String[] newNames = new String[auxiliaryVariableNames.length+1];
        Statik.copy(auxiliaryVariableNames, newNames);
        newNames[newNames.length-1] = variableName;
        auxiliaryVariableNames = newNames;
        int index = newNames.length-1;
        
        for (int i=0; i<modelListener.length; i++) 
        	modelListener[i].addAuxiliaryVariable(variableName, index);
    }

    @Override
    public void requestAddControlVariable(String variableName) {
        String[] newNames = new String[controlVariableNames.length+1];
        Statik.copy(controlVariableNames, newNames);
        newNames[newNames.length-1] = variableName;
        controlVariableNames = newNames;
        int index = newNames.length-1;

        for (int i=0; i<modelListener.length; i++) 
        	modelListener[i].addControlVariable(variableName, index);
    }

    @Override
    public void requestRemoveAuxiliaryVariable(int index) {
        if (index >= auxiliaryVariableNames.length) return;
        String[] newNames = new String[auxiliaryVariableNames.length-1];
        int j=0; for (int i=0; i<newNames.length; i++) if (i!=index) newNames[j++] = auxiliaryVariableNames[i];
        auxiliaryVariableNames = newNames;
        
        for (int i=0; i<modelListener.length; i++) modelListener[i].removeAuxiliaryVariable(index);
    }

    @Override
    public void requestRemoveControlVariable(int index) {
        if (index >= controlVariableNames.length-1) return;
        String[] newNames = new String[controlVariableNames.length-1];
        int j=0; for (int i=0; i<newNames.length; i++) if (i!=index) newNames[j++] = controlVariableNames[i];
        controlVariableNames = newNames;
        
        for (int i=0; i<modelListener.length; i++) modelListener[i].removeControlVariable(index);
    }

    public synchronized void restartFit() {
        modelRun.requestReset();
    }

    public synchronized void triggerRun() {triggerRun(data, null);}
    public synchronized void triggerRun(double[][] data) {triggerRun(data, null);}
    public synchronized void triggerRun(double[][] data, double[][] definitionVariableData) {triggerRun(data, definitionVariableData, null, null);}
    public synchronized void triggerRun(double[][] data, double[][] definitionVariableData, double[][] auxiliaryData, double[][] controlData) {
        if (data != null) this.setData(Statik.copy(data), Statik.copy(auxiliaryData), Statik.copy(controlData));
        if (definitionVariableData != null) 
            this.definitionVariableData = definitionVariableData;

        if (modelRun.getStatus() == Status.DEAD) modelRun = new ModelRun(this);
        modelRun.dataValid = (data != null && data.length > 0 && data[0].length == anzVar);
        modelRun.definitionDataValid = checkDefinitionVariables();
        
        modelRun.requestReset();
    }
    
    private boolean checkDefinitionVariables() {
        if (definitionVariableEdges == null || definitionVariableEdges.length==0) 
            return true;
        
        if (definitionVariableData == null || definitionVariableData.length != anzPer || 
                definitionVariableEdges.length != definitionVariableData[0].length) return false;
        
        for (int i=0; i<anzPer; i++) for (int j=0; j<definitionVariableData[i].length; j++)
            if (Model.isMissing(definitionVariableData[i][j])) return false;
        
        return true;
    }

    public synchronized void triggerRun(double[][] dataCov, double[] dataMean, int anzPer) {
        this.setDataDistribution(dataCov, dataMean, anzPer);
        this.definitionVariableEdges = new Edge[0];
        
        modelRun.definitionDataValid = true;
        modelRun.dataValid = (dataCov.length == anzVar && dataCov[0].length == anzVar 
                && dataMean.length == anzVar);
        modelRun.requestReset();
    }
    
    public boolean addRunner(ModelRunUnit runner) {
        try {
            modelRun.addRunUnitOnQueue(runner);
        } catch (Exception e) {e.printStackTrace(); return false;}
        return true;
    }
    
    public synchronized String getName() {return name;}

    public boolean isEmpty() {return anzFac == 0;}
    
    public synchronized int[] getObservedIds() {
        return Statik.copy(filter);
    }

    public ModelRun.Status getStatus() {
        return modelRun.getStatus();
    }
    
    @Override
    public double[][] getLatentAndMissingScores(ParameterReader parameter) {
        Model model = modelRun.getWorkModel().copy();
        model.evaluateMuAndSigma(parameter.getParameterValues());
        double[][] erg = RAMModel.getAllScoresOfMultigroupRAMModel(model, anzPer);
        if (meanTreatment == MeanTreatment.implicit && implicitlyEstimatedMeans != null) 
            for (int i=0; i<erg.length; i++) for (int j=0; j<filter.length; j++) if (!Model.isMissing(implicitlyEstimatedMeans[j])) erg[i][filter[j]] += implicitlyEstimatedMeans[j]; 
        return erg;
    }
    
    /*
    public ParameterSet getEstimates() {
        double[] est = modelRun.getEstimates(); if (est==null) return null;
        ParameterSet erg = new ParameterSet(paraNames, est);
        return erg;
    }
    */
    public double[] getBestEstimates() {
        return modelRun.getEstimates();
    }
    public ModelRunUnit getBestEstimateRunner() {
        return modelRun.getBestUnit();
    }
    
    public void notifyOfStatusChange(ModelRun.Status status) {
        for (int i=0; i<modelListener.length; i++) modelListener[i].changeStatus(status);
    }

    public void notifyOfConvergedUnitsChanged() {
        for (int i=0; i<modelListener.length; i++) modelListener[i].notifyOfConvergedUnitsChanged();
    }
    public OptimizationHistory getBestRunnerHistory() {
        return modelRun.getBestUnit().history;
    }
    
    public void notifyOfWarning(ModelRun.Warning warning) {
        for (int i=0; i<modelListener.length; i++) modelListener[i].notifyOfWarningOrError(warning);
    }
    
    public void notifyOfClearWarning(ModelRun.Warning warning) {
        for (int i=0; i<modelListener.length; i++) modelListener[i].notifyOfClearWarningOrError(warning);
    }

	public synchronized void setRunPriority(engine.ModelRun.Priority priority) {
	    modelRun.priority = priority;
	}
	
	public List<ModelRunUnit> getAllConvergedUnits() {
	    return modelRun.getAllConvergedUnits();
	}
	
	public List<ModelRunUnit> getAllUnits() {
	    return modelRun.getAllUnits();
	}
	
	public int getAnzConverged() {return modelRun.getAnzConverged();}
	
	public void hold() {
	    modelRun.priority = Priority.HOLD;
	}
	
    public void holdOnNextValidEstimate() {modelRun.setHoldOnNextValidEstimate(true);}
	
	public synchronized void killModelRun() {
	    modelRun.kill();
	}
	
	public ParameterReader getStartingValuesUnit() {
	    return new StartingValueParameterReader(this);
	}
	
	public double[] getArbitraryStartingValues() {
	    double[] erg = new double[anzPar];
	    for (int i=0; i<anzFac; i++) if (meanPar[i]!=NOPARAMETER) erg[meanPar[i]] = 0;
	    for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (asyPar[i][j]!=NOPARAMETER) erg[asyPar[i][j]] = 0.95;
	    for (int i=0; i<anzFac; i++) if (symPar[i][i]!=NOPARAMETER) erg[symPar[i][i]] = 1;
	    for (int i=0; i<anzVar; i++) if (symPar[filter[i]][filter[i]]!=NOPARAMETER) erg[symPar[filter[i]][filter[i]]] = 3.055;
        for (int i=0; i<anzFac; i++) for (int j=i+1; j<anzFac; j++) if (symPar[i][j]!=NOPARAMETER) erg[symPar[i][j]] = 0.1;
        return erg;
	}
	
	public String toString() {
	    String erg = name + " ["+getStatus()+"] ";
        if (modelRun.modelThrewUnknownError) erg += "(stopped on error)";
        if (modelRun.modelIsConstantSingular) erg += "(singular)";
        if (modelRun.modelIsOverspecified) erg += "(overspecified)";
        return erg;
	}

    public boolean isCovarianceConstantSingular() {return modelRun.modelIsConstantSingular;}
    public boolean isOverspecified() {return modelRun.modelIsOverspecified;}
    public boolean isError() {return modelRun.modelThrewUnknownError;}

    public void requestCreateData(int anzPer, int percentMissing, boolean isRawData) {
//        createData(anzPer);
//        triggerRun(data);
        this.anzPer = anzPer;
        for (int i=0; i<modelListener.length; i++) modelListener[i].newData(percentMissing, isRawData);
//        setParameter(startingValues);
    }

    @Override
    public String[] getVariableNames() {return variableNames;}
    @Override
    public int[] getObservedVariables() {return filter;}

	@Override
	public Priority getRunPriority() {
		return modelRun.priority;
	}

	/**
	 * Returns for every parameter a String description in the style "x --> y", "x <-> x", or "mean x", possibly adding "(+other)"
	 * @return
	 */
    public String[] getParameterToFromDescription() {
        String[] erg =new String[anzPar];
        for (int i=0; i<anzFac; i++) if (meanPar[i] != NOPARAMETER) {
            int pnr = meanPar[i];
            if (erg[pnr]!=null) {if (!erg[pnr].endsWith("other)")) erg[pnr] += " (+other)";}
            else erg[pnr] = "mean "+variableNames[i];
        }
        for (int i=0; i<anzFac; i++) for (int j=i; j<anzFac; j++) if (symPar[i][j] != NOPARAMETER) {
            int pnr = symPar[i][j];
            if (erg[pnr]!=null) {if (!erg[pnr].endsWith("other)")) erg[pnr] += " (+other)";}
            else erg[pnr] = variableNames[j]+" <-> "+variableNames[i];
        }
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (asyPar[i][j] != NOPARAMETER) {
            int pnr = asyPar[i][j];
            if (erg[pnr]!=null) {if (!erg[pnr].endsWith("other)")) erg[pnr] += " (+other)";}
            else erg[pnr] = variableNames[j]+" --> "+variableNames[i];
        }
        for (int i=0; i<anzPar; i++) if (erg[i] == null) erg[i] = "(unused parameter)";
        return erg;
    }

	@Override
	public void removeModelListener(ModelListener listener) {
		
        ModelListener[] na = new ModelListener[modelListener.length-1];
        
        int toI = 0;
        for (int i=0; i<modelListener.length; i++) {
        	if (modelListener[i] == listener) {
        		
        	} else {
        		na[toI] = modelListener[i];
        		toI++;
        	}
        	
        }
 
        modelListener = na;
		
	}

    @Override
    public void requestSetGroupingVariable(Node node, String groupingVariableName, double group) {
        node.setGrouping(true);
        node.groupName = groupingVariableName;
        node.groupValue = group;
        groupingNodes.add(node);
        for (int i=0; i<modelListener.length; i++) modelListener[i].setGroupingVariable(node);
    }

    @Override
    public void requestUnsetGroupingVariable(Node node) {
        node.setGrouping(false);
        node.groupName = null;
        node.groupValue = Double.NaN;
        groupingNodes.remove(node);
        for (int i=0; i<modelListener.length; i++) modelListener[i].unsetGroupingVariable(node);
    }

	@Override
	public void requestSetDefinitionVariable(Edge edge, String name) {
	    edge.definitionVariableName = name;
	    int ix = -1; 
	    for (int i=0; i<definitionVariableEdges.length; i++) if (definitionVariableEdges[i]==edge) ix = i;
	    if (ix == -1) {
    	    Edge[] newDVE = new Edge[definitionVariableEdges.length+1];
    	    for (int i=0; i<definitionVariableEdges.length; i++) newDVE[i] = definitionVariableEdges[i];
    	    newDVE[newDVE.length-1] = edge;
    	    definitionVariableEdges = newDVE;
    	    ix = newDVE.length-1;
	    }
	    edge.definitionColumn = ix;
        for (int i=0; i<modelListener.length; i++) modelListener[i].setDefinitionVariable(edge);
	}
	
	@Override
	public void requestUnsetDefintionVariable(Edge edge) {
	    Edge[] newDVE = new Edge[definitionVariableEdges.length-1];
        for (int i=0; i<newDVE.length; i++) 
            newDVE[i] = definitionVariableEdges[(i<edge.definitionColumn?i:i-1)];
        definitionVariableEdges = newDVE;
	    edge.definitionVariableName = null;
	    edge.definitionColumn = -1;
        for (int i=0; i<modelListener.length; i++) modelListener[i].unsetDefinitionVariable(edge);
	}
	
	public OnyxModel getModel() {return this;}
    
	public String getModelDistribution(List<Node> selected, ParameterReader parameter) {
	    if (parameter != null) {
    	    List<String> names = parameter.getSortedParameterNames();
    	    for (String parName:names) setParameter(parName, parameter.getParameterValue(parName));
	    }
	    evaluateMuAndSigma();
	    String erg = "<table>\r\n<tr><td>Variable</td><td>Model Mean</td><td>Model Covariance</td></tr>";
	    for (Node n:selected) {
            erg += "<tr><td>"+n.getCaption()+"</td>";
           
            if (meanTreatment == Graph.MeanTreatment.implicit) {
            	erg += "<td>N/A</td>";
            } else {
            	erg += "<td>"+Statik.doubleNStellen(meanBig[n.getId()],2)+"</td>";
            }
            
	        erg += "<td>"; for (Node m:selected) erg += Statik.doubleNStellen(sigmaBig[n.getId()][m.getId()],2)+"&nbsp;&nbsp;&nbsp;"; erg += "</td>";
	        erg += "</tr>\r\n";
	    }
        erg += "</table>\r\n";
	    return erg;
	}
	
	public double[][] getNumericalModelDistribution(ParameterReader parameter) {
		if (parameter != null) {
		    List<String> names = parameter.getSortedParameterNames();
	        for (String parName:names) setParameter(parName, parameter.getParameterValue(parName));
		}
		evaluateMuAndSigma();
		/*double[][] result = new double[selected.size()][selected.size()];
		for (Node n:selected) {

		}
		sigmaBig[n.getId()][m.getId()]*/
		return(sigmaBig);
	}

    public void setParameter(ParameterReader parameter) {
        List<String> names = parameter.getSortedParameterNames();
        for (String parName:names) try {setParameter(parName, parameter.getParameterValue(parName));} catch (Exception e) {
        	System.err.println("Could not set parameter name!");
        	/**
        	 * 
        	 * TODO @TIMO why does this happen?
        	 * 
        	 * 
        	 */
        	e.printStackTrace();
        } 
    }
    
    public long getBigClockTime() {return modelRun.getBigClockTime();}

    public String getMatrixDescription() {
        String erg = "Variables: ";
        for (int i=0; i<anzFac; i++) erg += variableNames[i]+(i==anzFac-1?"":", ");
        return erg + "\r\n\r\n"+super.getMatrixDescription();
    }
    
    public String getLISRELMatrixDescription() {
        String erg = "Variables: ";
        for (int i=0; i<anzFac; i++) erg += variableNames[i]+(i==anzFac-1?"":", ");
        return erg + "\r\n\r\n"+super.getLISRELMatrixDescription();
    }
    
    public void setStartingValues(double[] starting) {Statik.copy(starting, startingValues);}
    
    public static enum Until {CONVERGED, RELIABLYCONVERGED, CONVERGENCESTABILIZED, TIMEOUT};
    public void runUntilReliablyConverged(double[][] data) {runUntil(data, Until.RELIABLYCONVERGED,Long.MAX_VALUE/10000000L);}
    public void runUntilConverged(double[][] data) {runUntil(data, Until.CONVERGED,Long.MAX_VALUE/10000000L);}
    public boolean runUntil(double[][] data, Until until) {return runUntil(data, until, Long.MAX_VALUE/10000000L);}
    public boolean runUntil(double[][] data, long millis) {return runUntil(data, Until.CONVERGED, millis);}
    public boolean runUntilConverged(RawDataset data) {return runUntil(data, Until.CONVERGED);}
    public boolean runUntil(RawDataset data, Until until, long millis) {this.setData(data); return runUntil(this.data, this.definitionVariableData, this.auxiliaryData, this.controlData, null, null, -1, until, millis);}
    public boolean runUntil(RawDataset data, Until until) {return runUntil(data, until, Long.MAX_VALUE/10000000L);}
    public boolean runUntil(RawDataset data, long millis) {return runUntil(data, Until.CONVERGED, millis);}
    public boolean runUntil(double[][] data, Until until, long millis) {return runUntil(data, null, null, null, null, null, -1, until, millis);}
    public boolean runUntil(double[][] data, double[][] definitionVariableData, double[][] auxiliaryData, double[][] controlData, Until until, long millis) {return runUntil(data, definitionVariableData, auxiliaryData, controlData, null, null, -1, until, millis);}
    public boolean runUntil(double[][] dataCov, double[] dataMean, int anzPer, Until until, long millis) {return runUntil(null, null, null, null, dataCov, dataMean, anzPer, until, millis);}
    public boolean runUntil(double[][] dataCov, double[] dataMean, int anzPer, long millis) {return runUntil(null, null, null, null, dataCov, dataMean, anzPer, Until.CONVERGED, millis);}
    private boolean runUntil(double[][] data, double[][] definitionVariableData, double[][] auxiliaryData, double[][] controlData, double[][] dataCov, double[] dataMean, int anzPer, Until until, long millis) {

        implicitlyEstimatedMeans = Statik.ensureSize(implicitlyEstimatedMeans, anzVar);
        if (meanTreatment != MeanTreatment.explicit) {
            for (int var=0; var<anzVar; var++) {
                double sum = 0.0; int anz = 0;
                for (int i=0; i<data.length; i++) if (!Model.isMissing(data[i][var])) {anz++; sum += data[i][var];}
                if (anz > 0) {
                    implicitlyEstimatedMeans[var] = sum / (double)anz;
                    for (int i=0; i<data.length; i++) 
                        if (!Model.isMissing(data[i][var])) data[i][var] -= implicitlyEstimatedMeans[var];
                }
            }
        }
        
        if (data != null) triggerRun(data, definitionVariableData, auxiliaryData, controlData);
        else triggerRun(dataCov, dataMean, anzPer);
        long startTime = System.nanoTime();
        boolean goon = true;
        boolean timeout = false;
        while (goon && !timeout) {
            try {
                long time = System.nanoTime() - startTime;
                long sleep = 1000; 
                if (time < 1000000000) sleep = 100;
                if (time < 1000000) sleep = 10;
                Thread.sleep(sleep);
                
                if ((until == Until.CONVERGED && this.modelRun.isConvergedOnBestRunner()) ||
                    (until == Until.RELIABLYCONVERGED && this.modelRun.isReliablyConverged()) ||
                    (until == Until.CONVERGENCESTABILIZED && modelRun.isHappy())) goon = false;
                timeout = time > millis*1000000L;
            } catch (Exception e) {
                return false;
            }
        }
        modelRun.kill();
        
        if (timeout) 
            if (until != Until.TIMEOUT) return false; else return modelRun.isConverged();        
        
        return true;
    }

    public boolean runFor(double[][] data, int millis) {return runUntil(data, Until.TIMEOUT, millis);}

    public String getOnyxJavaCode() {
        String erg = "// Automatically generated script code for Onyx Model "+getName()+".\r\n\r\n";
        erg += "// Model generation\r\n";
        erg += "int[] meanParameter = new int[]{";
        for (int i=0; i<anzFac; i++) erg += meanPar[i]+(i==anzFac-1?"};\r\n":",");
        erg += "double[] meanValue = new double[]{";
        for (int i=0; i<anzFac; i++) erg += meanVal[i]+(i==anzFac-1?"};\r\n":",");
        erg += "int[][] asymmetricParameter = new int[][]{{";
        for (int i=0; i<anzFac; i++) { 
            for (int j=0; j<anzFac; j++) erg += asyPar[i][j]+(j==anzFac-1?"}":",");
            erg += (i==anzFac-1?"};\r\n":",\r\n                                        {");
        }
        erg += "double[][] asymmetricValue = new double[][]{{";
        for (int i=0; i<anzFac; i++) {   
            for (int j=0; j<anzFac; j++) erg += asyVal[i][j]+(j==anzFac-1?"}":",");
            erg += (i==anzFac-1?"};\r\n":",\r\n                                           {");
        }
        erg += "int[][] symmetricParameter = new int[][]{{";
        for (int i=0; i<anzFac; i++) { 
            for (int j=0; j<anzFac; j++) erg += symPar[i][j]+(j==anzFac-1?"}":",");
            erg += (i==anzFac-1?"};\r\n":",\r\n                                       {");
        }
        erg += "double[][] symmetricValue = new double[][]{{";
        for (int i=0; i<anzFac; i++) {   
            for (int j=0; j<anzFac; j++) erg += symVal[i][j]+(j==anzFac-1?"}":",");
            erg += (i==anzFac-1?"};\r\n":",\r\n                                          {");
        }
        erg += "int[] filter = new int[]{";
        for (int i=0; i<anzVar; i++) erg += filter[i]+(i==anzVar-1?"};":",");
        
        erg += "\r\n";
        erg += "RAMModel ramModel = new RAMModel(symmetricParameter,symmetricValue,asymmetricParameter,\r\n";
        erg += "                                 asymmetricValue,meanParameter,meanValue,filter);\r\n";
        erg += "OnyxModel model = new OnyxModel(ramModel); \r\n";
        
        erg += "\r\n\r\n";
        erg += "// set data set \r\n";
        erg += "double[][] data = YOURDATASET; \r\n";
        
        erg += "\r\n\r\n";
        erg += "// Starting model run\r\n";
        erg += "// Settings: CONVERGED             = standard convergence criterion,\r\n";
        erg += "//           RELIABLYCONVERGED     = convergence criterion stable for multiple steps,\r\n";
        erg += "//           CONVERGENCESTABILIZED = all runners settled in local minima.\r\n"; 
        erg += "OnyxModel.Until until = OnyxModel.Until.CONVERGED;\r\n";
        erg += "//OnyxModel.Until until = OnyxModel.Until.RELIABLYCONVERGED;\r\n";
        erg += "//OnyxModel.Until until = OnyxModel.Until.CONVERGENCESTABILIZED;\r\n\r\n";
        erg += "// use this line for running until criterion is satisfied. \r\n";
        erg += "boolean success = model.runUntil(data, until);\r\n";
        erg += "// use these lines for running until criterion is satisfied or process timed out. \r\n";
        erg += "//long timeout = YOURTIMEOUTCHOICE\r\n"; 
        erg += "//boolean success = model.runUntil(data, until, timeout);\r\n";
        
        erg += "\r\n";
        erg += "// read results\r\n";
        erg += "ModelRunUnit bestEstimate = model.getBestEstimateRunner();\r\n";
        erg += "String description = bestEstimate.getDescription();\r\n";
        erg += "double[] parameterValues = bestEstimate.getPosition();\r\n";
        erg += "double minusTwoLogLikelihood = bestEstimate.getMinusTwoLogLikelihood();\r\n";

        return erg;
    }

    // start changes by AB for Model Link //
    
    public void linkSaturatedModel(Model model)
    {
    	this.linkedSaturatedModel = model;
    }
    
    public void unlinkSaturatedModel(Model model)
    {
    	this.linkedSaturatedModel = null;
    }
    
    Model linkedSaturatedModel;
    
    public double getSaturatedLL() {
    	if (linkedSaturatedModel != null) {
    		System.out.println("Get saturated ll");
    		return linkedSaturatedModel.getMinusTwoLogLikelihood();
    	}
		return super.getSaturatedLL();
    }

    public double[] getParametersFromSummary(File shortSummary) {
        try {
            double[] erg = new double[anzPar];
            getParametersFromSummary(new BufferedReader(new FileReader(shortSummary)), erg);
            return erg;
        } catch (FileNotFoundException e) {throw new RuntimeException("File not Found: "+shortSummary.getName());}
    }
    public double getFitFromSummary(File shortSummary) {
        try {
            return getParametersFromSummary(new BufferedReader(new FileReader(shortSummary)), null);
        } catch (FileNotFoundException e) {throw new RuntimeException("File not Found: "+shortSummary.getName());}
    }
    public double getParametersFromSummary(String shortSummary, double[] erg) {
        BufferedReader reader = new BufferedReader(new StringReader(shortSummary));
        return getParametersFromSummary(reader, erg);
    }    
    public double getParametersFromSummary(BufferedReader reader, double[] erg) {
        double fit = MISSING;
        if (erg != null) Statik.setTo(erg,  MISSING);
        try {
            while (reader.ready()) {
                String line = reader.readLine();
                if (line.startsWith("Fit = ")) fit = Double.parseDouble(line.substring(6).trim());
                if (line.startsWith("Minus Two Log Likelihood")) {
                    int ix = line.indexOf(":"); fit = Double.parseDouble(line.substring(ix+1).trim());
                }
                if (erg != null)
                    for (int i=0; i<anzPar; i++) {
                        if (line.startsWith(paraNames[i]) && Character.isWhitespace(line.charAt(paraNames[i].length()))) 
                            // read in from short summary file
                            try {
                                int ix1 = line.indexOf("\t"), ix2 = line.indexOf("\t", ix1+1);
                                erg[i] = Double.parseDouble(line.substring(ix1+1, ix2));
                            } catch (Exception e) {}
                        else if (line.contains(paraNames[i]) && line.contains("|")) 
                            try {
                                // read in from long summary file
                                int ix1 = line.indexOf('|'); ix1 = line.indexOf('|',ix1+1); ix1 = line.indexOf('|',ix1+1);
                                int ix2 = line.indexOf('|', ix1+1);
                                String val = line.substring(ix1+1,ix2).trim();
                                erg[i] = Double.parseDouble(val);
                            } catch (Exception e) {}
                    }
            }
        } catch (Exception e) {}
        return fit;
    }

    public void notifyOfFailedReset() {
        for (int i=0; i<modelListener.length; i++) modelListener[i].notifyOfFailedReset();
    }

    public void setMeanTreatment(Graph.MeanTreatment meanTreatment) {this.meanTreatment = meanTreatment;}
    public MeanTreatment getMeanTreatment() {return meanTreatment;}
    
    /** Computes the degrees of freedom between the actual and a saturated model. */
    @Override
    public int getRestrictedDF()
    {
        return getObservedStatistics() - anzPar;
    }

    @Override
    public int getObservedStatistics() {
        if (meanTreatment == MeanTreatment.ambique || meanTreatment == MeanTreatment.implicit) return anzVar*(anzVar+1)/2;
        else return anzVar+anzVar*(anzVar+1)/2;
    }

    @Override
    public ParallelProcess requestClusterWithDirichletProcess(int anzIterations, int anzBurnin, double alphaDirichlet, double priorStrength) {
        return requestClusterWithDirichletProcess(anzIterations, anzBurnin, alphaDirichlet, priorStrength, false, 0, 0, 0);
    }
    @Override
    public ParallelProcess requestClusterWithDirichletProcess(int anzIterations, int anzBurnin, double alphaDirichlet, double priorStrength, boolean doPreClustering, int preClusteringBurnin, int preClusteringSamples, int preClusteringThinning) {
        if (this.isIndirectData) {System.out.println("Dirichlet Clustering impossible on non-raw data set."); return null;}
        if (!this.modelRun.dataValid) {System.out.println("Dirichlet Clustering impossible before data is valid."); return null;}
        
        final OnyxModel copy = this.copy();
        SEMLikelihoodFunction likelihood = new SEMLikelihoodFunction(copy.data, copy, this.dataCov, this.dataMean, priorStrength);
        final ChineseRestaurant crp = new ChineseRestaurant(likelihood, data.length, alphaDirichlet, anzBurnin);
        crp.DEBUGFLAG = false;
        final int anzSteps = (anzBurnin + anzIterations) * data.length;
        
        DirichletProcess process = new DirichletProcess(crp, copy, this, anzSteps, doPreClustering, preClusteringBurnin, preClusteringSamples, preClusteringThinning, alphaDirichlet, "DP Clustering "+(++DPClusteringIndexNumber));
        ParallelProcessHandler.currentParallelProcessHandler.addProcess(process);

        (new Thread(process)).start();
        return process;
    }
    
    

}
