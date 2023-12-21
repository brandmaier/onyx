package gui.graph;

import java.awt.Graphics2D;

public interface NodeDrawProxy {

	void draw(Node node, Graphics2D g, boolean markUnconnectedNodes);

}
