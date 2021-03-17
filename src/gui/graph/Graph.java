/**
 * ONYX - OpenMX GUI project
 * by T. von Oertzen, A. M. Brandmaier, and ...
 * 
 */
package gui.graph;

import engine.ModelRequestInterface;
import engine.ModelRunUnit;
import engine.ParameterReader;
import geometry.Rectangle;
import gui.Constants;
import gui.Desktop;
import gui.ReverseIterator;
import gui.frames.MainFrame;
import gui.graph.Edge.EdgeStyle;
import gui.graph.presets.Preset;
import gui.undo.EdgeDeleteStep;
import gui.undo.NodeDeleteStep;
import gui.undo.UndoStep;
import gui.views.ModelView;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import scc.Tree;

public class Graph {
	
    // if meanTreatment is implicit, all variables will be centralized before being send to the backend, which is equivalent to estimating all means
    // separately as an own parameter. If explicit, the means need to be modeled by the user. if ambique, the behavior is not yet decided, i.e., no explicit
    // modeling of means happened and no linked data column had a non-zero mean
    public enum MeanTreatment {implicit, explicit, ambique};
    private MeanTreatment meanTreatment;
    
	public MeanTreatment getMeanTreatment() {
		return meanTreatment;
	}

	public void setMeanTreatment(MeanTreatment meanTreatment) {
		this.meanTreatment = meanTreatment;
	}
	
	private boolean lockedStyle = false;
	public boolean isLockedStyle() { return lockedStyle; }

	private List<Node> nodes;
	private List<Edge> edges;
	private ModelView parent;
	private Font font;

	public Preset graphStyle;

	private VariableStack auxiliaryStack = new VariableStack(this);
	private VariableStack controlStack = new VariableStack(this);
	
	public Stroke defaultEdgeStroke;
	public EdgeStyle defaultEdgeStyle = EdgeStyle.NORMAL;
	public boolean defaultEdgeStyleHideUnitValues = true;
	public Stroke defaultNodeStroke;
	public boolean markUnconnectedNodes = true;
	public boolean hideVariances;
	
	public enum MeanStyle {TRIANGLE, HIDDEN, ANNOTATION};
	
	public MeanStyle meanStyle = MeanStyle.TRIANGLE;
	
	public Color backgroundColor = Color.white;
	
	public void setDynamicStrokeWidths() {
		
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		
		for (Edge edge : edges) {
			if (edge.isDoubleHeaded()) continue;
			min = Math.min(min, Math.abs(edge.getValue()));
			max = Math.max(max, Math.abs(edge.getValue()));
		}
		
		for (Edge edge : edges) {
			if (edge.isDoubleHeaded()) continue;
			
			double percentage = 0;
			 
			if (max>min)
				percentage = (Math.abs(edge.getValue())-min)/(max-min);
			
			edge.setStroke(new BasicStroke(1+(float)percentage*4, BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND));
			
			edge.getLabel().setFontSize( (float) (8+percentage*10) );
		}
		
	}
	
	public boolean isMultiGroup()
	{
		for (Node node : nodes) {
			if (node.isGrouping()) return true;
		}
		
		return false;
	}
	
	public boolean isMarkUnconnectedNodes() {
		return markUnconnectedNodes;
	}

	public void setMarkUnconnectedNodes(boolean markUnconnectedNodes) {
		this.markUnconnectedNodes = markUnconnectedNodes;
	}

	public static Stroke strokeThin = new BasicStroke(1, BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND);
	public static Stroke strokeMedium = new BasicStroke(2, BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND);
	public static Stroke strokeThick = new BasicStroke(3, BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND);
	public static Stroke strokeVeryThick = new BasicStroke(4, BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND);
	
	public Graph()
	{
		edges = new ArrayList<Edge>();
		nodes = new ArrayList<Node>();
		
		try {
			font = Font.createFont( Font.TRUETYPE_FONT,
					new FileInputStream("f.ttf") 
			);
		} catch (Exception e) {
			font = new Font("Sans Serif",Font.PLAIN, 12);
		}
		
	}
	
	public Graph(ModelView parent)
	{
		this();
		this.parent = parent;
	}
	
	public List<Node> getNodes()
	{
		return nodes;
	}
	
	public List<Edge> getEdges()
	{
		return edges;
	}
	
	public NodeGroup getSelectedNodes()
	{
		NodeGroup selection = new NodeGroup();
		
		for (Node node : this.getNodes()) {
			if (node.isSelected()) selection.add(node);
		}
		
		return selection;
	}
	
	public NodeGroup getObservedNodes()
	{
		NodeGroup selection = new NodeGroup();
		
		for (Node node : this.getNodes()) {
			if (node.isObserved()) selection.add(node);
		}
		
		return selection;
	}
	
	public NodeGroup getLatentNodes()
	{
		NodeGroup selection = new NodeGroup();
		
		for (Node node : this.getNodes()) {
			if (node.isLatent()) selection.add(node);
		}
		
		return selection;
	}
	
	
	public void renameNode(Node node, String caption)
	{
		node.setCaption(caption);
			Iterator<Edge> iter = edges.iterator();
			while(iter.hasNext())  
			{
				Edge edge = iter.next();
				if ((edge.source==node) || (edge.target==node))
				{
					updateEdgeLabel(edge);
				}
			}
				
	}
	
	
	public void addEdge(Edge edge)
	{
	
		if (defaultEdgeStroke != null)
			edge.setStroke(defaultEdgeStroke);
		
		if (defaultEdgeStyle != null) 
		    edge.setEdgeStyle(defaultEdgeStyle);
		
		edge.edgeStyleHideUnitValues = defaultEdgeStyleHideUnitValues;
		
		updateEdgeLabel(edge);
		this.edges.add(edge);
		
		updateRenderingHints();
		
		edge.getDefinitionVariableContainer().setGraph(this);
		
		if (lockedStyle)
			this.graphStyle.apply(this);
		
	}
	
	public void cleverEdgeLabelLayout(Edge edge)
	{
		
		//
		
		// experimental: intelligent label placement
		if (edge.source != edge.target)
		{
			int NUM = 10;
			int best = 1;
			int best_penalty = Integer.MAX_VALUE;
			for (int i=1; i < NUM; i++) {
				// try this position
				edge.setRelativeLabelPosition( ((double)i)/NUM );
				edge.update();
				edge.getLabel().updateFontMetrics();
				//edge.update();
				java.awt.Rectangle r1 = edge.getLabelRectangle();
				// calculate penalty
				int penalty = (i-(NUM/2))*(i-(NUM/2));	//prior
				for (Edge other : edges) {
					
					if (edge==other) continue;
					
					java.awt.Rectangle r2 = other.getLabelRectangle(); 


					java.awt.Rectangle isect = r1.intersection(r2);


					if (isect != null && isect.height>0 && isect.width>0) {
						penalty+= isect.height*isect.width;

					}
				}

				if (penalty < best_penalty) { best_penalty = penalty; best=i;}
				else if (penalty==best_penalty && i==NUM/2) { best=i;}
			}
			
			// set to best
			edge.edgeLabelRelativePosition=((float)best)/NUM;
		}		
	}
	
	private void updateRenderingHints() {

		
		// give rendering hints to nodes and edges
		// with a simple heuristic
		
		Iterator<Node> iterNode = nodes.iterator();
		
		while (iterNode.hasNext()) {
			Node node = iterNode.next();
			
			//if (!node.arcPositionAutoLayout) continue;
			
			List<Edge> connectingEdges = getAllEdgesAtNode(node);

			/*   up
			 * \   /
			 *  \ /
			 * 	 o  right
			 *  / \
			 * /   \
			  /     \*/
			
			int upCount = 0;
			int downCount = 0;
			int leftCount = 0;
			int rightCount = 0;
			Iterator<Edge> iter = connectingEdges.iterator();
			while(iter.hasNext())  
			{
				Edge edge = iter.next();
				
				// ignore variance edges
				if (edge.source == edge.target) continue;
				
				// ignore covariance edges
				if (edge.isDoubleHeaded()) continue;

				double angle;
				if (edge.source==node) {
					angle = Math.atan2( edge.toY-edge.fromY, edge.toX-edge.fromX);
				} else {
					angle = Math.atan2( -edge.toY+edge.fromY, -edge.toX+edge.fromX);
				}
				angle = (angle + (2*Math.PI)) % (2*Math.PI);
								
				if (angle <= 1.0/4.0*Math.PI || angle >7.0/4.0*Math.PI) { 
					rightCount++;
				} else if (angle <= 3.0/4.0*Math.PI ) { 
					downCount++;
				} else if (angle <= 5.0/4.0*Math.PI ) { 
					leftCount++;
				} else {
					upCount++;
				}
				
				/*if (edge.source==node) {
					if (edge.fromY > edge.scy) downCount++; else upCount++;
				} else {
					if (edge.toY > edge.scy) upCount++; else downCount++;
				}*/
				
			}

			/*boolean hint = upCount < downCount;
			if (upCount==downCount) {
				hint = node.isLatent();	// latents up, manifests down, if upcount==downcount
			}
			*/
			
			
			/* use this to stop left/right variance layout */
			leftCount = 100000;
			rightCount = 100000;
			
			int hint = -1;
			
	
			
			if (upCount==0) hint = Node.NORTH;
			else if (downCount==0) hint = Node.SOUTH;
			else if (rightCount==0) hint=Node.EAST;
			else if (leftCount==0) hint = Node.WEST;

			if (upCount+downCount==0) hint=Node.SOUTH;
			
			if (hint==-1)
			if ((upCount==downCount) && (upCount < leftCount) && (upCount < rightCount)) {
				if (node.isLatent()) hint=Node.NORTH; else hint=Node.SOUTH;	// latents up, manifests down, if upcount==downcount
			}
			
			// otherwise take smallest element (but still prefer up/down?)
			if (hint==-1)
				
			if ((upCount < downCount) && (upCount <= leftCount) && (upCount <= rightCount)) hint=Node.NORTH;
			else if ((downCount < upCount) && (downCount <= leftCount) && (downCount <= rightCount)) hint=Node.SOUTH;
			else if ((leftCount < downCount) && (leftCount < upCount) && (leftCount <= rightCount)) hint=Node.WEST;
			else if ((rightCount < downCount) && (rightCount < upCount) && (rightCount <= leftCount)) hint=Node.EAST;
			else hint = Node.SOUTH;

			
			// fallback default
			if (hint==-1) hint=Node.SOUTH;
			
			node.setRenderingHintArcPosition(hint);
			
		}	

		// reset bi-directional hints
		for (int i=0; i < edges.size(); i++)
		{
			edges.get(i).setRenderingHintBidirectionalOffset(0);
			
			// and set arc pos from rendering hint
			if (edges.get(i).arcPositionAutoLayout) {
				edges.get(i).arcPosition = edges.get(i).source.getRenderingHintArcPosition();
			}
		}			
		
		// give rendering hint for bi-directional single-headed arrows
		// this is quadratic in the edges...
		// add a little offset as to not confuse them with a double-headed edge

		for (int i=0; i < edges.size(); i++)
		{
			for (int j=i+1; j < edges.size(); j++)
			{
				Edge a = edges.get(i);
				Edge b = edges.get(j);
				if ((a.getSource() == b.getTarget()) && (a.getTarget()==b.getSource()))
				{
					a.setRenderingHintBidirectionalOffset(+1);
					b.setRenderingHintBidirectionalOffset(-1);
				}
			}
		}
		
		
	}

	public void addNode(Node node)
	{
		if (defaultNodeStroke != null)
			node.setStroke(defaultNodeStroke);
		
		this.nodes.add(node);
		updateRenderingHints();
		
		node.getObservedVariableContainer().setGraph(this);
		node.getGroupingVariableContainer().setGraph(this);
		
		if (lockedStyle)
			this.graphStyle.apply(this);
	}
	
	public void removeEdge(Edge edge)
	{
		if (edge == null) return;
		
		MainFrame.undoStack.add( new EdgeDeleteStep(this.parent, edge) );
		this.edges.remove(edge);
		updateRenderingHints();
	}
	
	public void removeAllEdges()
	{
		this.edges.clear();
		updateRenderingHints();
	}
	
	public void removeAllNodes()
	{
		this.nodes.clear();
		updateRenderingHints();
	}
	
	public void draw(Graphics2D g)
	{
		this.draw(g, null);
	}
	
	public void setFont(Font font)
	{
		this.font = font;
	}
	
/**
 * Draw all edges and nodes in the graph. Nodes and edges are responsible
 * for their appearance
 * @param g
 * @param nodeGroupManager 
 */
	public void draw(Graphics2D g, NodeGroupManager nodeGroupManager)
	{
		
		
		g.setFont(font);
		
		//((Graphics2D)g).setStroke(stroke);
		updateRenderingHints();
		g.setColor(Color.black);
		
		// draw all edges and all nodes
		
		Iterator<Edge> iterEdge = edges.iterator();
		Iterator<Node> iterNode = nodes.iterator();

		// validate all edges (which cascade down to connected nodes)
		iterEdge = edges.iterator();
		while (iterEdge.hasNext()) {
			Edge edge = (iterEdge.next());
			edge.validate();
		}
		// validate all (unconnected) nodes
		while (iterNode.hasNext()) {
			iterNode.next().validate();
		}
		
		// draw shadows of nodes 
		iterNode = nodes.iterator();
		while (iterNode.hasNext()) {
			iterNode.next().renderShadow( ((Graphics2D)g) );
		}
		
		// draw edges (unless variances are set to hidden)
		iterEdge = edges.iterator();
		while (iterEdge.hasNext()) {
			Edge edge = iterEdge.next();
			if (!(hideVariances && edge.source==edge.target)) 
				edge.renderConnection(g);
		}
		
		// draw labels (unless variances are set to hidden)
		iterEdge = edges.iterator();
		while (iterEdge.hasNext()) {
			Edge edge = iterEdge.next(); 
			if (!(hideVariances && edge.source==edge.target)) 			
			edge.renderLabel(g, backgroundColor);
		}
		
		// draw nodes
		iterNode = nodes.iterator();
		while (iterNode.hasNext()) {
			Node node = iterNode.next();
			node.draw(g, markUnconnectedNodes);
			
			if (nodeGroupManager != null)
				nodeGroupManager.drawAnnotation(g, node);
			
		}
		
		// draw auxiliary stack
		auxiliaryStack.draw(g);
		
		controlStack.draw(g);
		
	}

	


	public void setStroke(Stroke nodeStroke, Stroke edgeStroke) {
		
		defaultNodeStroke = nodeStroke;
		defaultEdgeStroke = edgeStroke;
		
		Iterator<Node> iterNode = nodes.iterator();
		while (iterNode.hasNext()) {
			Node node = iterNode.next();
			
			node.setStroke(nodeStroke);
			
		}

		Iterator<Edge> iterEdge = edges.iterator();
		while (iterEdge.hasNext()) {
			Edge edge = iterEdge.next();
			
			edge.setStroke(edgeStroke);
			
		}
	
	}

	public Node getNodeById(int sourceId) {
    	Iterator<Node> iterNode = nodes.iterator();
    	while (iterNode.hasNext()) {
    		Node node = iterNode.next();
    		if (node.getId()==sourceId) return node;
    	}
    	return null;
	}

	public Iterator<Node> getNodeIterator() {
		return this.nodes.iterator(); 
	}
	
	public Iterator<Node> getNodeReverseIterator() {
		return new ReverseIterator<Node>(this.nodes); 
	}

	public Iterator<Edge> getEdgeIterator() {
		return this.edges.iterator();
	}
	
	public Iterator<Edge> getEdgeReverseIterator() {
		return new ReverseIterator<Edge>(this.edges); 
	}

	public void removeNode(Node node) {
		
		if (node.isConnected()) {
			System.err.println("ERROR! Removing connected edge!");
		} else {
			MainFrame.undoStack.add( new NodeDeleteStep(this.parent, node) );
			this.nodes.remove(node);
		}
		
		
	}

	public ModelView getParentView() {
		return parent;
	}
	
	/**
	 * returns an area that is required to
	 * fully display the graph
	 */
	public Rectangle getBoundingBoxFromOrigin()
	{
		int width = 0;
		int height = 0;
		Iterator<Node> iterNode = nodes.iterator();
		while (iterNode.hasNext()) {
			Node node = iterNode.next();
			width = Math.max(width, node.getX()+node.getWidth());
			height = Math.max(height, node.getY()+node.getHeight());
		}
		
		return new Rectangle(0,0,width, height);
	}
	
	/**
	 * returns an area that is required to
	 * fully display the graph
	 */
	public Rectangle getBoundingBox()
	{
		int width = 0;
		int height = 0;
		int x = Integer.MAX_VALUE;
		int y = Integer.MAX_VALUE;
		Iterator<Node> iterNode = nodes.iterator();
		while (iterNode.hasNext()) {
			Node node = iterNode.next();
			width = Math.max(width, node.getX()+node.getWidth());
			height = Math.max(height, node.getY()+node.getHeight());
			x = Math.min(node.getX(),x);
			y = Math.min(node.getY(), y);
		}
		
		return new Rectangle(x,y,width, height);
	}
	
	/**
	 * returns a list of all edges at the node
	 * @param node
	 * @return
	 */
	public List<Edge> getAllEdgesAtNode(Node node)
	{
		List<Edge> resultEdges = new ArrayList<Edge>();
		Iterator<Edge> iterEdge = edges.iterator();
		while (iterEdge.hasNext()) {		
			Edge edge = iterEdge.next();
			
			if ((edge.source == node) || (edge.target==node))
			{
				resultEdges.add(edge);
			}
		}
		
		return(resultEdges);
	}

	public Edge getEdge(Node source, Node target, boolean isDoubleHeaded) {
		
		Iterator<Edge> iterEdge = edges.iterator();
		while (iterEdge.hasNext()) {		
			Edge edge = iterEdge.next();
			// TvO 26.10.2015: Changed the line below to find double-headed edges even if source and target are exchanged. 
			if (edge.isDoubleHeaded() == isDoubleHeaded && ((edge.getSource()==source && edge.getTarget()==target) || 
			                                    (isDoubleHeaded && edge.getSource()==target && edge.getTarget() == source)))
			{
				return edge;
			}
		}
		
		return(null);
	}

	public String toString()
	{
		return "<Graph with "+this.nodes.size()+" nodes and "+edges.size()+" edges>";
	}
	
	public void updateWithEstimates(ModelRequestInterface mri, ParameterReader paramReader) {
		
	    // TvO: The following call can come from the runner Thread when resetting the model, while changes on the model can occur concurrently
	    // from the GUI or external Thread. If that is the case, the cov is no longer valid after the call. This can also manifest as a concurrent 
	    // modification exception in the edge iterator; in this case, we are lost which edges to change, and have to exit without setting the standardized estimate.
		double[][] cov = null;
		try {
		   cov = mri.getNumericalModelDistribution(paramReader);
		} catch (Exception e) {
			System.err.println("Error in Graph.updateWithEstimates()");
			System.err.println(e);
			//return;
			int n = mri.getObservedIds().length;
			cov = new double[n][n];
			for (int i=0; i<n; i++) {
				for (int j=0; j<n; j++) {
					cov[i][j] = Double.NaN;
				}
			}
		}   

		try {
    		Iterator<Edge> iterEdge = edges.iterator();
    		while (iterEdge.hasNext()) {		
    			Edge edge = iterEdge.next();
    		
    			//if (edge.isFixed()) continue;
    		
    			// standardize estimates
    			
    			double value;
    			
    			if (!edge.isFixed())
    				value = paramReader.getParameterValue(edge.getParameterName());
    			else
    				value = edge.getValue();

    			
    			double svalue = Double.NaN ; 
    			try {
        			int inidx = edge.getTarget().getId();
        			int outidx = edge.getSource().getId();
        			
        		
        			if (edge.isDoubleHeaded()) {
                        if (cov != null && cov.length > inidx && cov.length > outidx && cov[inidx][inidx] > 0 && cov[outidx][outidx]>0 )
        					svalue = value / (Math.sqrt(cov[inidx][inidx])*Math.sqrt(cov[outidx][outidx]));
        				else 
        					svalue = Double.NaN;
        			} else {
        				if (cov != null && cov[inidx][inidx] > 0 && cov[outidx][outidx]>0 )
        					svalue = value * (Math.sqrt(cov[outidx][outidx])/Math.sqrt(cov[inidx][inidx]));
        				else 
        					svalue = Double.NaN;
        			}
        			
        			if (edge.getSource().isMeanTriangle()) {
        				svalue = Double.NaN;
        			}
    			} catch (Exception e) {}
    			edge.setValue( value, svalue ) ;
    		}
		} catch(Exception e) {
			e.printStackTrace();
		}
    		
	}

	/**
	 * someone unlinked an observed variable
	 * (e.g. by deleting the dataview)
	 * 
	 */
	public void notifyUnlink() {
		Iterator<Node> iterNode = nodes.iterator();
		while (iterNode.hasNext()) {
			Node node = iterNode.next();
			if (!node.isLatent()) {
				if (Desktop.getLinkHandler().isLinked(node.observedVariableContainer)) {
					node.setConnected(true);
				} else {
					node.setConnected(false);
				}
			}
		}
		
		for (Edge edge : edges) {
			if ((edge.isDefinitionVariable())) {
				if ( (Desktop.getLinkHandler().isLinked(edge.getDefinitionVariableContainer()))) {
					//edge.setDefinitionVariable(true); TODO: is this OK?
				} else {
					//edge.unlinkDefinitionVariable();
					Desktop.getLinkHandler().unlink(edge.getDefinitionVariableContainer());
				}
			}
		}
	}

	public ModelView getModelView() {
		return parent;
	}
	
	public boolean hasAllNodesConnected()
	{
		for (Node node : nodes)
		{
			if (!node.isMeanTriangle() && !node.isLatent() && !node.isConnected()) return false;
		}
		return true;
	}
	
	public void updateAllEdges()
	{
		for (Edge edge : edges)
		{
			edge.update();
		}
	}
	
    public void updateEdgeLabel(Edge edge)
    {
    	final String SPACE = "_";
    	
    	if (edge.isAutomaticNaming())
    	{
    	    String label = edge.getAutomaticParameterName();

    		if (edge.getParameterName() != label)
    		{
    			//edge.setParameterNameBy(label);
    			edge.setParameterName(label);
    			if (parent != null)
    				parent.getModelRequestInterface().requestChangeParameterOnEdge(edge);
    		}
    	}
    }

	public void changeEdgeStyle(EdgeStyle style) {
		// TODO Auto-generated method stub
			Iterator<Edge> iter = edges.iterator();
			while(iter.hasNext())  
			{
				Edge edge = iter.next();
				edge.setEdgeStyle(style);
			}
			defaultEdgeStyle = style;
	}
	
	public void changeEdgeStyle(EdgeStyle style, boolean hideUnitValues) {
		// TODO Auto-generated method stub
			Iterator<Edge> iter = edges.iterator();
			while(iter.hasNext())  
			{
				Edge edge = iter.next();
				edge.setEdgeStyle(style, hideUnitValues);
			}
			defaultEdgeStyle = style;
			defaultEdgeStyleHideUnitValues = hideUnitValues;
	}
	
	public void autoLayout() {
		Tree tree = new Tree(this, false);
		tree.layout();
//		tree.layoutThomas();
		
		updateAllEdges();
	}
	
	public void tidyUp() {
		
		// restore default node sizes & align nodes to grid & adjust color
		for (Node node : nodes) {
			node.setWidth(Constants.DEFAULT_NODE_SIZE);
			node.setHeight(Constants.DEFAULT_NODE_SIZE);
			node.alignToGrid(Constants.DEFAULT_GRID_SIZE);
			node.setLineColor(Color.black);
			node.setFillColor(Color.white);
			node.setFontSize(Node.DEFAULT_LABEL_FONTSIZE);

		}
		
		// for all edges, adjust color
		for (Edge edge : edges) {
			edge.setLineColor(Color.BLACK);
			edge.setLineWidth(Edge.DEFAULT_STROKEWIDTH);
			edge.getLabel().setFontSize(Edge.DEFAULT_LABEL_FONTSIZE);
		}
		
		
		updateAllEdges();
	
	}

	/**
	 * Returns the first edge from a triangle node to the node with id <code>target</code>.
	 *  
	 * @param target   target
	 * @return         edge from mean to target
	 */
    public Edge findEdgeFromTriangleToNode(int target) {
        Iterator<Edge> iter = edges.iterator();
        while(iter.hasNext())  
        {
            Edge edge = iter.next();
            if (edge.source.isMeanTriangle() && edge.target.getId() == target) return edge;
        }
        return null;
    }
    
    public void selectAll(boolean select)
    {
    	for (Node node : nodes)
    	{
    		node.setSelected(select);
    	}
    	
    	for (Edge edge : edges)
    	{
    		edge.setSelected(select);
    	}
    }

	public void selectNodes(NodeGroup nodeGroup, boolean b) {
		
		if (nodeGroup == null) return;
		
		for (Node node : nodeGroup)
		{
			node.setSelected(b);
		}
		
	}

    public String[] getAllVariableNames() {
        String[] erg = new String[nodes.size()];
        for (Node node:nodes) {
            erg[node.getId()] = node.getCaption();
        }
        return erg;
    }
    
    public void invalidate(){
    	for (Node node : nodes) {
    		node.invalidate();
    	}
    	for (Edge edge : edges) {
    		edge.invalidate();
    	}   	
    }
    
    public void validate() {
    	

    	
    	for (Node node : nodes) {
    		node.validate();
    	}
    	for (Edge edge : edges) {
    		edge.validate();
    	}
    
    }

	public EdgeStyle getEdgeStyle() {
		return(defaultEdgeStyle);
		
	}

	public boolean hasTriangles() {
		for (Node node : nodes)
		{
			if (node.isMeanTriangle()) return true;
		}
		return false;
	}

    public boolean isLackingNodePositions() {
        for (Node node : nodes)
        {
            if (node.getX() != 0 || node.getY() != 0) return false;
        }
        return true;
    }
    
    /**
     * Collects all nodes which have single-headed edges to the parameter node. 
     * 
     * @author TM
     * @param node
     * @return
     */
    public List<Node> getDirectedParentNodesOf(Node node) {
        ArrayList<Node> erg = new ArrayList<Node>();
        
        for (Edge edge:this.edges) {
            if (!edge.isDoubleHeaded() && edge.getTarget() == node && !erg.contains(edge.getSource())) erg.add(edge.getSource());
        }
        
        return erg;
    }

    /**
     * Computes a depth value for every node in the graph. 
     * 
     * @author Thomas M. 
     */
    public void computeDirectedDepths() {
        for (Node node:this.nodes) node.depth = -1; 
        int numberOfUnfinished = nodes.size();
        while (numberOfUnfinished > 0) {
            for (Node node:this.nodes) if (node.depth == -1) {
                List<Node> parents = getDirectedParentNodesOf(node);
                if (parents.size() == 0) {numberOfUnfinished--; node.depth = 0;}
                else {
                    int minimalDepth = Integer.MAX_VALUE;
                    for (Node parent:parents) {
                        if (parent.depth < minimalDepth) minimalDepth = parent.depth;
                    }
                    if (minimalDepth > -1) {numberOfUnfinished--; node.depth = minimalDepth+1;}
                }
            }
        }
    }
    
	public boolean hasNodeWithCaption(String left) {
		for (Node node : getNodes()) {
			if (node.getCaption().equals(left)) {
				return true;
			}
		}
		return(false);
	}

	public VariableStack getAuxiliaryStack() {
		return(auxiliaryStack);
	}

	public VariableStack getControlStack() {
		return(controlStack);
	}

	public void setLockedStyle(boolean b) {
		this.lockedStyle = b;
		
	}
	
}
