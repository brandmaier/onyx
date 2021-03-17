package gui.graph;

import gui.linker.LinkEvent;
import gui.linker.LinkException;

public interface VariableContainerListener {

	void notifyUnlink(LinkEvent event);
	void notifyLink(LinkEvent event) throws LinkException;

}
