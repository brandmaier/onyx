package gui.frames;

import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.graph.Edge.EdgeStyle;
import gui.undo.PathStyleStep;
import gui.views.ModelView;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.RenderingHints;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

public class EdgeStyleFrame extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1216464638640813073L;
	JRadioButton style1, style2, style3, style4, style5, style6;
	ButtonGroup group;
	
	Vector<Graph> graphs = new Vector<Graph>();
	
	JCheckBox checkExplicitOnes;
	
	public JButton select;
	
	ModelView mv;

	public EdgeStyleFrame(ModelView view) {
		
		super("Select path style");
		
		this.mv = view;
		
		this.setBackground(Color.white);
		
		this.setLayout(new GridBagLayout());

		// create radio buttons
		style1 = new JRadioButton("Normal");
		style2 = new JRadioButton("Plain");
		style3 = new JRadioButton("Label only");
		style4 = new JRadioButton("Values only");
//		style5 = new JRadioButton("Full");
		style5 = new JRadioButton("Simplified");
		
		group = new ButtonGroup();
		group.add(style1);
		group.add(style2);
		group.add(style3);
		group.add(style4);
		group.add(style5);
	//	group.add(style6);

		checkExplicitOnes = new JCheckBox("Hide unit values");
		if (view != null)
			checkExplicitOnes.setSelected(view.getGraph().defaultEdgeStyleHideUnitValues); 
		else
			checkExplicitOnes.setSelected(true);
		checkExplicitOnes.addActionListener(this);
		
		this.add( new JLabel(""));


		// header
		GridBagConstraints c1 = new GridBagConstraints();
		//c1.gridwidth = 100;
		//c1.gridwidth = h;
		c1.gridx = 0;
		c1.gridy = 0;
		GridBagConstraints c2 = new GridBagConstraints();
		//c2.gridwidth = 200;
		//c2.gridwidth = h;		
		c2.gridx = 1;
		c2.gridy = 0;
		GridBagConstraints c3 = new GridBagConstraints();
		//c3.gridwidth = 200;
		//c3.gridwidth = h;
		c3.gridx = 2;
		c3.gridy = 0;
		GridBagConstraints c4 = new GridBagConstraints();
		//c3.gridwidth = 200;
		//c3.gridwidth = h;
		c4.gridx = 3;
		c4.gridy = 0;

		this.add(new JLabel(""), c1);
		this.add(new JLabel("Path with unit value"), c2);
		this.add(new JLabel("Path with non-unit value"), c3);
		this.add(new JLabel("Free parameter"), c4);

		
		// create graphs for styles
		
		createStyleRow(style1, 1, EdgeStyle.NORMAL);
		createStyleRow(style2, 2, EdgeStyle.PLAIN);
		createStyleRow(style3, 3, EdgeStyle.ALWAYS_LABEL);
		createStyleRow(style4, 4, EdgeStyle.ALWAYS_VALUE);
		//createStyleRow(style4, 4, EdgeStyle.FULL);
		createStyleRow(style5, 5, EdgeStyle.SIMPLIFIED);
		
		GridBagConstraints cx = new GridBagConstraints();
		cx.gridx = 1;
		cx.gridy = 6;
		
		this.add(checkExplicitOnes, cx);
		
		cx = new GridBagConstraints();
		cx.gridx = 0;
		cx.gridy = 7;
		
		select = new JButton("Select");
		select.addActionListener(this);
		this.add(select, cx);
		
		
		
		/*this.add(style2);
		this.add(style3);
		this.add(style4);
		this.add(style5);
		*/

		// show
		setSize(800, 600);
		setVisible(true);
	}

	private void createStyleRow(JRadioButton radio, int row, Edge.EdgeStyle style) {
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
		GridBagConstraints c3 = new GridBagConstraints();
		//c3.gridwidth = 200;
		//c3.gridwidth = h;
		c3.gridx = 2;
		c3.gridy = row;
		GridBagConstraints c4 = new GridBagConstraints();
		//c3.gridwidth = 200;
		//c3.gridwidth = h;
		c4.gridx = 3;
		c4.gridy = row;
		
		this.add(radio,c1);
		JPanel g1a = createGraphElement(true, 1, style);
		g1a.setPreferredSize(new Dimension(w,h));
		this.add(g1a,c2);
		JPanel g1b = createGraphElement(true,  3.14, style);
		g1b.setPreferredSize(new Dimension(w,h));
		this.add(g1b,c3);
		JPanel g1c = createGraphElement(false, 2.87, style);
		g1c.setPreferredSize(new Dimension(w,h));
		this.add(g1c,c4);
	}

	private JPanel createGraphElement(boolean fixed,
			double edgeValue, Edge.EdgeStyle style) {

		int x = 10;
		int y = 10;
		int rad = 30;

		Node source = new Node("x", 0, x, y);
		Node target = new Node("y", 1, x+140, y);
		source.setWidth(rad);
		source.setHeight(rad);
		target.setWidth(rad);
		target.setHeight(rad);
		
		Edge edge;
		if (fixed) {
			edge = new Edge(source, target, false);
			edge.setFixed(fixed);
		} else {
			edge = new Edge(source, target, false);
			edge.setFixed(fixed);
		}


		Graph g = new Graph();
		graphs.add(g);
		g.addNode(source);
		g.addNode(target);

		//edge.setEdgeStyle(style);

		g.addEdge(edge);
		
		//edge.drawOutline = false;

		edge.setParameterName("z");
		edge.setValue(edgeValue);
		
		g.changeEdgeStyle(style);
		
		return (new GraphPanel(g));

	}

	public static void main(String[] args) {
		new EdgeStyleFrame(null);
	}

	public class GraphPanel extends JPanel {
		Graph graph;

		public GraphPanel(Graph g) {
			
			this.graph = g;
		}

		public void paintComponent(Graphics g) {
		     Graphics2D g2d =((Graphics2D)g);
		     
		     g2d.setRenderingHint ( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );

			super.paintComponent(g2d);
			g.drawRect(0, 0, this.getWidth()-1, this.getHeight()-1);
			graph.draw(g2d);
		}

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		if (arg0.getSource() == checkExplicitOnes) {
			
			for (Graph g : graphs){
				g.changeEdgeStyle(g.getEdgeStyle(), checkExplicitOnes.isSelected());
				
			}
			repaint();
			
			this.mv.getGraph().changeEdgeStyle(this.mv.getGraph().getEdgeStyle(),checkExplicitOnes.isSelected());
			
			return;
		}
		
		EdgeStyle oldStyle = mv.getGraph().getEdgeStyle();
		
		if (style1.isSelected())
			this.mv.getGraph().changeEdgeStyle(EdgeStyle.NORMAL);
		if (style2.isSelected())
			this.mv.getGraph().changeEdgeStyle(EdgeStyle.PLAIN);
		if (style3.isSelected())
			this.mv.getGraph().changeEdgeStyle(EdgeStyle.ALWAYS_LABEL);
		if (style4.isSelected())
			this.mv.getGraph().changeEdgeStyle(EdgeStyle.ALWAYS_VALUE);
		if (style5.isSelected())
			this.mv.getGraph().changeEdgeStyle(EdgeStyle.SIMPLIFIED);
		
		
		if (oldStyle != mv.getGraph().getEdgeStyle()) {
			MainFrame.undoStack.add( new PathStyleStep(mv.getGraph(), oldStyle));
		}
		
		mv.redraw();
		this.setVisible(false);
		this.dispose();
	}

}
