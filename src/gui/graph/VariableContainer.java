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
    package gui.graph;

import gui.linker.DatasetField;
import gui.linker.LinkEvent;
import gui.linker.LinkException;
import gui.linker.LinkHandler;

import java.util.Vector;

/**
 * Generic container class for variables determined by columns of a dataset.
 * This is particularly useful for grouping variables (on nodes) 
 * and definition variables (on edges).
 *
 * Importantly, a container can be active or inactive
 * and connected or unconnected.
 * 
 * @author brandmaier
 *
 */
public class VariableContainer implements Cloneable {

	private Vector<VariableContainerListener> listeners = new Vector<VariableContainerListener>();
	private String name;	// name is a unique node / edge name provided by CombinedDataset in its constructor.
	private Graph graph;	// the graph the variable container belongs to
	private int id;
	private Object parent;	// parent object in a graph (e.g., node, edge, ...)
	private boolean connected;
	private boolean active = false;
	private static int ID_COUNTER = 0;	// global id counter for variable containers
	
	public Object clone(Object parent)
	{
		VariableContainer copy;
		try {
			copy = (VariableContainer)super.clone();
			copy.listeners = new Vector<VariableContainerListener>();
			//System.out.println("Requesting Var Container "+ID_COUNTER);
			copy.connected = false; // added to indicate that connected containers are not linked
			this.id = ID_COUNTER;
			this.parent = parent;
			ID_COUNTER++;
			
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
		
		return(copy);
	}
	
	public VariableContainer(Graph graph, Object parent)
	{
		this.graph = graph;
		this.parent = parent;
		
		this.id = ID_COUNTER;
		ID_COUNTER++;
	}
	
	public void addVariableContainerListener(VariableContainerListener vcl)
	{
		this.listeners.add(vcl);
	}
	
	public void removeVariableContainerListener(VariableContainerListener vcl)
	{
		this.listeners.remove(vcl);
	}

	public void notifyUnlink(LinkEvent event) {
		setConnected(false);
		for (VariableContainerListener listener : listeners) listener.notifyUnlink(event);
	}
	
	public void notifyLink(LinkEvent event) throws LinkException {
		setConnected(true);
		for (VariableContainerListener listener : listeners) listener.notifyLink(event);
	}
	
	
	public DatasetField getLinkedDatasetField() {
		return(LinkHandler.getGlobalLinkhandler().getDatasetField(this));
	}

	public Graph getGraph() {
		return(graph);
	}

	public int getId() {
		return id;
	}
	
	public String getUniqueName() {
	    return name;
	}
	
	public void setUniqueName(String name) {this.name = name;}

	public Object getParent() {
		return parent;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}
}
