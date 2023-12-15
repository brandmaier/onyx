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

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.util.Vector;

import engine.RawDataset;
import gui.Desktop;
import gui.linker.DatasetField;
import gui.linker.LinkHandler;

public class VariableStack implements Movable, Resizable {

	int x=20,y=20, width=50, height=50;
	Graph graph;
	boolean hidden = true;
	
	public VariableStack(Graph graph) {
		this.graph = graph;
	}
	
	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	Vector<VariableContainer> vcontainer = new Vector<VariableContainer>();
	
	@Override
	public int getX() {
		return(x);
	}

	@Override
	public int getY() {
		return(y);
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	}
	
	
	public void draw(Graphics2D g)
	{
		if (isHidden()) return;
		
		// outer shape
		final Shape shape = new Rectangle(this.x, this.y, this.width, this.height);
		
		final int stack_step_offset = 5;
		final int xinset = 4+vcontainer.size();
		

		final Color c1 = new Color(120,150,199);
		g.setColor(c1);
		g.fill(shape);
		g.setColor(Color.black);
		g.draw(shape);
		
		int ht = 10-Math.max(5, vcontainer.size()/2);
		
		// draw stack
		for (int i=0; i < vcontainer.size(); i++) {
			Shape sh = new Rectangle(this.x+xinset+i, this.y+this.height-10-ht*i, this.width-2*xinset, ht);
		//	renderShadow(g, sh);
			g.setColor(Color.white);
			g.draw(sh);
			g.fill(sh);
		}
		
		
		g.drawString(Integer.toString(vcontainer.size()),this.x+20, this.y+20);
		
		final FontMetrics fm = g.getFontMetrics();
		
		g.drawString("AUX", this.x+this.width/2- fm.stringWidth("AUX")/2, this.y+this.height+15);
	}

	@Override
	public int getHeight() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getWidth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setHeight(int height) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setWidth(int width) {
		// TODO Auto-generated method stub
		
	}
	
	public boolean isPointWithin(int xp, int yp) {
		
		if (isHidden()) return(false);
		
		return (xp >= x) & (xp < x + width) & (yp >= y) & (yp < y + height);
	}
	
	public void renderShadow(Graphics2D g, Shape shape) {
		

		AffineTransform at = new AffineTransform();
		at.translate(2, 2);
		Shape tShape = at.createTransformedShape(shape);
		/*
		 * g.draw(tShape);
		 */

		g.setColor(new Color(100, 100, 100));
		g.fill(tShape);

		at.translate(1, 1);
		tShape = at.createTransformedShape(shape);
		g.setColor(new Color(200, 200, 200));
		g.fill(tShape);
	}

	public VariableContainer addVariableContainer() {
		
		
		
		VariableContainer vcont = new VariableContainer(graph, this);
		vcontainer.add(vcont);
		return(vcont);
	}
	
	public VariableContainer get(int i) {
		return(vcontainer.get(i));
	}
	
	public int size() {
		return(vcontainer.size());
	}

	public void removeAll() {
		
		for (int i=0; i < vcontainer.size(); i++)
		{
			VariableContainer cnt = vcontainer.get(i);
			Desktop.getLinkHandler().unlink(cnt);
		}
		
		vcontainer.removeAllElements();
		
	}

	public double[][] getRawData(LinkHandler linkHandler, Graph graph2) {

		if (this.size()==0) return null;
		
		int nCol = this.size();
		int nRow = -1;
		double[][] result = null;
		
		for (int i=0; i < nCol; i++) {
			DatasetField ldf = this.get(i).getLinkedDatasetField();
			double[] col;
			if (ldf.dataset instanceof RawDataset) {
				col = ((RawDataset)ldf.dataset).getColumn(ldf.columnId);
			} else {
				System.err.println("Non-raw dataset linked in Variable stack. Could not return raw data.");
				return(null);
			}
			
			if (result==null) {
				nRow = col.length;
				result = new double[nRow][nCol];
			} else {
				 for(int j=0; j < nRow; j++) {
					 result[j][i] = col[j];
				 }
			}
		}
		
		return(result);
	}

}
