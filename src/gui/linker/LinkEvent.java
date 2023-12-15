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
