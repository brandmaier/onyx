package gui.linker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import engine.Dataset;
import engine.RawDataset;
import engine.ModelRequestInterface;
import gui.Desktop;
import gui.frames.MainFrame;
import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.graph.VariableContainer;
import gui.linker.LinkEvent.linkType;
import gui.undo.LinkStep;
import gui.views.ModelView;

public class LinkHandler {
	
	private List<LinkListener> linkListener = new ArrayList<LinkListener>();
	
	//TODO make private
	public HashMap<DatasetField, List<VariableContainer>> dToG;
	public HashMap<VariableContainer, DatasetField> gToD;
	
	public LinkHandler()
	{
		dToG = new HashMap<DatasetField, List<VariableContainer>>();
		gToD = new HashMap<VariableContainer, DatasetField>();
	}
	
	public static LinkHandler getGlobalLinkhandler() {
	    return Desktop.getLinkHandler();
	}

	public void link(Dataset dataset, int columnId, VariableContainer variableContainer, ModelRequestInterface mri) throws LinkException
	{
		// unlink first if linked
		if (isLinked(variableContainer))
			Desktop.getLinkHandler().unlink(variableContainer);
		
		// then start linking
		DatasetField d = new DatasetField(dataset, columnId);
		
		List<VariableContainer> graphFields = dToG.get(d);
		if (graphFields == null) {
			graphFields = new ArrayList<VariableContainer>();
			dToG.put(d, graphFields);
		}
		
		graphFields.add(variableContainer);
		
		gToD.put(variableContainer, d);
		
		variableContainer.notifyLink( new LinkEvent(linkType.LINK, variableContainer, dataset.getColumnName(columnId)));
		

	}
	
	
	public boolean isLinked(VariableContainer container)
	{
		return gToD.containsKey(container);
	}
	
	public DatasetField getDatasetField(VariableContainer container)
	{
		return gToD.get(container);
	}
	
	public boolean isLinked(Dataset dataset, int columnId)
	{
		return dToG.containsKey(new DatasetField(dataset, columnId));
	}

	public void unlink(VariableContainer container) {
	
		DatasetField d = gToD.get(container);
		gToD.remove(container);
		dToG.remove(d);
		
		container.notifyUnlink( new LinkEvent(linkType.UNLINK, container));
		notifyUnlinkListeners(container.getGraph());
	}

	private void notifyUnlinkListeners(Graph graph) {
		for (LinkListener ll : linkListener)
			ll.notifyUnlink(graph);
		
	}

	public List<VariableContainer> getVariableContainer(Dataset dataset, int j) {
		return dToG.get(new DatasetField(dataset, j));
	}
	


	
	/**
	 * unlink all connections to the specified graph
	 * 
	 * @param graph
	 */
	public void unlink(Graph graph) {
		
		Set<VariableContainer> keys = gToD.keySet();
		Iterator<VariableContainer> it = keys.iterator();
		while (it.hasNext()) {
			VariableContainer vc = it.next();
			if (vc.getGraph() == graph) {
				DatasetField datasetField = gToD.get(vc);
				dToG.remove(datasetField);
				it.remove();
			}
		}
		
		notifyUnlinkListeners(graph);
	}
	
	
	/**
	 * unlink all connections to the specified dataset
	 * 
	 */
	public void unlink(Dataset dataset)
	{
		Set<DatasetField> keys = dToG.keySet();
		Iterator<DatasetField> it = keys.iterator();
		while (it.hasNext()) {
			DatasetField datasetField = it.next();
			if (datasetField.dataset == dataset) {
				List<VariableContainer> graphFields = dToG.get(datasetField);
				for (VariableContainer graphField : graphFields) {
    				gToD.remove(graphField);
    				// TvO 15 MAR 14: Does the following line make sense? AB, TODO, check if correct please!
    				gToD.remove(datasetField);
    				graphField.notifyUnlink(new LinkEvent(linkType.UNLINK,graphField));
    				graphField.getGraph().notifyUnlink();
    				notifyUnlinkListeners(graphField.getGraph());
    				graphField.getGraph().getModelView().modelChangedEvent();
				}
			}
		}
	}

	public List<ModelView> getAllConnectedModels(Dataset dataset) {
		
		List<ModelView> results = new ArrayList<ModelView>();
		
		Set<DatasetField> keys = dToG.keySet();
		Iterator<DatasetField> it = keys.iterator();
		while (it.hasNext()) {
			DatasetField datasetField = it.next();
			if (datasetField.dataset == dataset) {
				 List<VariableContainer> gflist = dToG.get(datasetField);
				 for (int i = 0; i < gflist.size(); i++) {
					 ModelView mw= gflist.get(i).getGraph().getParentView();
					 boolean already=false;
					 for (int j=0; j < results.size(); j++) {
						 if (results.get(j)==mw) {already=true; break;}
					 }
					 if (!already) {
						 results.add(mw);
					 }
				 }
						 
			}
		}
		
		return( results );
	}

	public void addLinkListener(LinkListener ll) {
		this.linkListener.add(ll);
		
	}

	public void removeLinkListener(LinkListener ll) {
		this.linkListener.remove(ll);
		
	}

	public Element toXML(Document doc) {
		
		Element element =  doc.createElement("model");
		
		Set<DatasetField> keys = dToG.keySet();
		for (DatasetField key : keys)
		{
			List<VariableContainer> list = dToG.get(key);
			for (VariableContainer elem : list) {
				Element linkElement = doc.createElement("link");
				linkElement.setAttribute("graph", elem.getGraph().toString());
				linkElement.setAttribute("variablecontainerid", String.valueOf(elem.getId()));
				linkElement.setAttribute("dataset", key.dataset.getName());
				linkElement.setAttribute("columnid", String.valueOf(key.columnId));
				element.appendChild(linkElement);
			}
		}
		
		
		return(element);
	}

	/*
	 * @deprecated
	 */
	public void link(Dataset dataset, int i, Graph graph,
			ModelRequestInterface model, Node node) throws LinkException {
		link(dataset, i, node.getObservedVariableContainer(), model);
		
	}

    public void notifyLinkedModels(RawDataset changedDataset) {
        Set<VariableContainer> keys = gToD.keySet();
        if (keys==null) return;
        for (VariableContainer container:keys) 
            if (gToD.get(container).dataset == changedDataset) 
                container.getGraph().getModelView().modelChangedEvent();
    }

	public List<Dataset> getAllConnectedDatasets(ModelView modelView) {
		
		List<Dataset> results = new ArrayList<Dataset>();
		
		Set<VariableContainer> keys = gToD.keySet();
		Iterator<VariableContainer> it = keys.iterator();
		while (it.hasNext()) {
			VariableContainer vcField = it.next();
			
			ModelView comp = vcField.getGraph().getModelView();
			//if (vcField.getParent() instanceof ModelView) comp = (ModelView)vcField.get
			
			if (comp == modelView) {
				DatasetField df = gToD.get(vcField);
				if (df!=null)
				 results.add(df.dataset);
			 }
						 
			
		}
		
		return( results );
		
	}


	
}
