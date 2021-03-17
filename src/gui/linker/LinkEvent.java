package gui.linker;

import gui.graph.VariableContainer;
import gui.linker.LinkEvent.linkType;
/**
 * LinkEvent represents events of variables of a dataset being linked to 
 * a model. This encompasses linking a variable to an observed variable
 * in a model, a definition variable or a grouping variable.
 * Particularly, LinkEvents allow models or model elements to react
 * to link events.
 * 
 * @author Andreas Brandmaier
 *
 */
public class LinkEvent {

	enum linkType {LINK, UNLINK};
	
	linkType eventLinkType;
	VariableContainer eventSource;
	String eventName;
	
	public LinkEvent(linkType eventLinkType, VariableContainer source, String eventName)
	{
		this(eventLinkType, source);
		this.eventName = eventName;
	}

	public LinkEvent(linkType eventLinkType, VariableContainer source) {
		this.eventLinkType = eventLinkType;
		this.eventSource = source;
	}

	public String getEventName() {
		return eventName;
	}

	public linkType getEventLinkType() {
		return eventLinkType;
	}

	public VariableContainer getEventSource() {
		return eventSource;
	}
}
