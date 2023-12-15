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
package gui.frames;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import gui.graph.Edge;
import gui.graph.Edge.EdgeStyle;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

public class LineStyleFrame extends JFrame implements ActionListener
{

	JRadioButton style1, style2, style3, style4, style5;
	ButtonGroup group;
	
	public enum LineStyle {
		NORMAL, DASHED, DOTTED
	}
	
//	float[] dash = new float[] {10.0f};
	
	public static final List<float[]> dashes = new ArrayList<float[]>(3);
	static{
	 dashes.add(null);
	 dashes.add(new float[]{4f});
	 dashes.add(new float[]{10f,5f});
	}
	
	float strokeWidth = 4f;

	JComponent parent;
	
	JButton select;
	
	List<Edge> edges;
	
	public LineStyleFrame(JComponent parent,List<Edge> edges)
	{
		this.parent = parent;
		this.edges = edges;
		
		this.setBackground(Color.white);
		
		this.setLayout(new GridBagLayout());
		style1 = new JRadioButton("Normal");
		style2 = new JRadioButton("Dotted");
		style3 = new JRadioButton("Dashed");
		
		group = new ButtonGroup();
		group.add(style1);
		group.add(style2);
		group.add(style3);
		
		createStyleRow(style1, 0, LineStyle.NORMAL);
		createStyleRow(style2, 1, LineStyle.DASHED);
		createStyleRow(style3, 2, LineStyle.DOTTED);

		GridBagConstraints cx = new GridBagConstraints();
		cx.gridx = 0;
		cx.gridy = 7;
		
		select = new JButton("Select");
		select.addActionListener(this);
		this.add(select, cx);
		
		this.setSize(500,200);
		this.pack();
		
		setVisible(true);
	}
	
	private void createStyleRow(JRadioButton radio, int row, LineStyle style) {
		int w = 200; int h = 100;

		GridBagConstraints c1 = new GridBagConstraints();
		//c1.gridwidth = 100;
		//c1.gridwidth = h;
		c1.gridx = 0;
		c1.gridy = row;
		GridBagConstraints c2 = new GridBagConstraints();
		//c2.gridwidth = 200;
		//c2.gridwidth = h;		
		c2.gridx = 1;
		c2.gridy = row;

		
		this.add(radio,c1);
		JPanel g1a = createGraphElement(true, row, style);
		g1a.setPreferredSize(new Dimension(w,h));
		this.add(g1a,c2);

	}
	
	private JPanel createGraphElement(boolean fixed,
			int strokeValue, LineStyle style) {
		
		JPanel pn = new Mypanel(strokeValue);
		pn.setSize(200,50);
		
		return (pn);
	}
	
	class Mypanel extends JPanel
	{
		int type;
		public Mypanel(int type)
		{
			this.type= type;
		}
		
		public void paint(Graphics g) {
			int height = this.getHeight();
			int width = this.getWidth();
			
			 BasicStroke st = new BasicStroke(strokeWidth,
			            BasicStroke.CAP_BUTT,
			            BasicStroke.JOIN_MITER,
			           10f, LineStyleFrame.dashes.get(type), 0.0f);
			
			((Graphics2D)g).setStroke( st);
			g.drawLine(0,height/2, width, height/2);
		}
	}
	
	public static void main(String[] args)
	{
		new LineStyleFrame(null, null);
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		int choice = 1;
		if (style1.isSelected())
			choice = 0;
		if (style2.isSelected())
			choice = 1;
		if (style3.isSelected())
			choice = 2;
			
		for (Edge edge : edges) {
			edge.setDashStyle(dashes.get(choice));
		}
		
		if (parent != null)
		parent.repaint();
		
		this.dispose();
	}
	
}
