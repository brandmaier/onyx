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

import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JLabel;

public class LabelDecorator extends JLabel implements DecoratorObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3339446983728175605L;
	
	public LabelDecorator(String label, Font font)
	{
		super(label);
		
		if (font != null)
			this.setFont(font);
		
		
		//this.addMouseListener(this);
	}
	
	public void paint(Graphics g) {
		super.paint(g);
	}

	int x,y,w,h;

	@Override
	public int getX() {
	
		return x;
	}

	@Override
	public int getY() {

		return y;
	}

	@Override
	public void setX(int x) {
		this.x=x;
	}

	@Override
	public void setY(int y) {
		this.y=y;
	}

	
	@Override
	public void setHeight(int height) {
		this.h = height;
		
	}

	@Override
	public void setWidth(int width) {
		this.w = width;
	}
	
	public int  getWidth() { return this.w;}
	public int getHeight() { return this.h;}

	@Override
	public String toXML() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
