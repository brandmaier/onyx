package engine;

import engine.ModelRun.Warning;
import engine.backend.Model.Strategy;
import gui.graph.Edge;
import gui.graph.Node;

public interface ModelListener {
	
	public void addNode(gui.graph.Node node);
	
	public void addEdge(gui.graph.Edge edge);
	
	public void swapLatentToManifest(gui.graph.Node node);

	public void changeName(String name);
	
	// If source is -1, the edge comes from a mean triangle
	public void removeEdge(int source, int target, boolean isDoubleHeaded);

	// needs to make sure that the id of all nodes with higher id then this are reduced by one. 
	public void removeNode(int id);
	
	public void deleteModel();

	public void cycleArrowHeads(Edge edge);
	
	public void swapFixed(Edge edge);
	
	public void changeStatus(ModelRun.Status status);
	
	public void notifyOfConvergedUnitsChanged();
	
	public void setValue(Edge edge);
	
	public void notifyOfStartValueChange();
	
	public void changeParameterOnEdge(Edge edge);
	
	public void notifyOfWarningOrError(ModelRun.Warning warning);
	
	public void newData(int percentMissing, boolean isRawData);
	
	public void changeNodeCaption(Node node, String name);
	
	public void setDefinitionVariable(Edge edge);

    public void unsetDefinitionVariable(Edge edge);

    public void notifyOfClearWarningOrError(Warning warning);
    
    public void setGroupingVariable(Node node);
    
    public void unsetGroupingVariable(Node node);
    
    public void notifyOfFailedReset();

    public void addDataset(Dataset dataset, int x, int y);

    public void addDataset(double[][] dataset, String datasetName, String[] additionalVariableNames, int x, int y);
    
    public void addAuxiliaryVariable(String name, int index);
    
    public void addControlVariable(String name, int index);
    
    public void removeAuxiliaryVariable(int index);
    
    public void removeControlVariable(int index);

    public void notifyOfStrategyChange(Strategy strategy);
}
