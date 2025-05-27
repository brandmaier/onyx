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
package gui.graph.decorators;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
//import javax.swing.
import java.awt.Shape;
import java.awt.Stroke;

public class ShapeDecorator implements DecoratorObject {

	Shape shape;
	private Stroke stroke = new BasicStroke(2, BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_BEVEL);
	
	public ShapeDecorator(Shape shape) {
		this.shape = shape;
	}

	@Override
	public int getX() {
		return shape.getBounds().x;
	}

	@Override
	public int getY() {
		return shape.getBounds().y;
	}

	@Override
	public void setX(int x) {
		shape.getBounds().setLocation(x, getY());
	}

	@Override
	public void setY(int y) {
		shape.getBounds().setLocation(getX(), y);
		
	}

	@Override
	public int getHeight() {
		return((int)shape.getBounds().getHeight());
	}

	@Override
	public int getWidth() {
		return((int)shape.getBounds().getWidth());
	}

	@Override
	public void setHeight(int height) {
		shape.getBounds().setSize(getWidth(), height);
		
	}

	@Override
	public void setWidth(int width) {
		shape.getBounds().setSize(width, getHeight());
		
	}

	@Override
	public void paint(Graphics g) {
		Graphics2D g2d = ((Graphics2D)g);
		g2d.setStroke(stroke );
		g2d.draw(shape);
	}

	@Override
	public String toXML() {
		// TODO Auto-generated method stub
		return null;
	}



}
