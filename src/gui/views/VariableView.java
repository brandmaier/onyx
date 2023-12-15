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
package gui.views;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;

import engine.RawDataset;
import gui.Utilities;

public class VariableView extends View
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RawDataset dataset;
	private int iColumn;
	private BasicStroke stroke;
	
	Font font = new Font("Arial", Font.PLAIN, 10);
	FontMetrics fm;

	public VariableView(RawDataset dataset, int iColumn)
	{
		this.dataset = dataset;
		this.iColumn = iColumn;
		
		this.setSize(120, 30);
		this.setOpaque(false);
		
		this.stroke = new BasicStroke(2, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);
		
		this.setResizable(false);
		
		updateTooltip();
	}
	
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
	
		
		((Graphics2D)g).setStroke(stroke);
		g.drawOval(0, 0, this.getWidth(), this.getHeight());
		
		if (fm==null) fm = g.getFontMetrics();
		
		int sw = fm.stringWidth(dataset.getColumnName(this.iColumn));
		
		g.drawString( dataset.getColumnName(this.iColumn), this.getWidth()/2-sw/2, this.getHeight()/2+7 );
	}
	
	public void mouseDragged(MouseEvent arg0) {
		
		super.mouseDragged(arg0);
		
		if (Utilities.isRightMouseButton(arg0)) {
			
			return;
		}
	}
	
	public void mousePressed(MouseEvent arg0) {
		
		super.mousePressed(arg0);
		
	}
	
	public void updateTooltip()
	{
		this.setToolTipText(
		
			"<html><h1>"+dataset.getColumnName(this.iColumn)+"</h1>"+
			"Mean: "+ round(dataset.getColumnMean(this.iColumn),2)+"<br>"+
					"Std. dev.: "+ round(dataset.getColumnStandardDeviation(this.iColumn),2)+"<br>"+
					"Median: " + round(dataset.getColumnMedian(this.iColumn),2)+"<br>"+
					"</html>"
				
		);
		
		
	}

	private double round(double d, int i) {
		double fac = Math.pow(10, i);
		return Math.round(d*fac)/fac;
	}
	
	
}
