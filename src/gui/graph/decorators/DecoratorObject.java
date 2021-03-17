package gui.graph.decorators;

import java.awt.Graphics;

import gui.graph.Movable;
import gui.graph.Resizable;

public interface DecoratorObject extends Movable, Resizable
{

	int clickX = 0;
	int clickY = 0;
	
	public void paint(Graphics g);

	public String toXML();
}
