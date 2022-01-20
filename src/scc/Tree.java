/*
  Copyright 2013 Joshua Nathaniel Pritikin

  This file is free software: you can redistribute it and/or modify it
  under the terms of the GNU Lesser General Public License as
  published by the Free Software Foundation, either version 3 of the
  License, or (at your option) any later version.

  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU Lesser General Public License for more details.

  You should have received a copy of the GNU Lesser General Public
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/>.
*/

package scc;

import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tree {
	final private Graph graph;
	final private List<Node> nodes;
	final private List<Edge> edges;
	final private Map<Node,TNode> tnodeMap = new HashMap<Node,TNode>();
	final private int midY;
	final private boolean firstTime;
	
	final private List<TNode> tnodes = new ArrayList<TNode>();
	
	private int meanY(List<Node> nodes) {
		if (nodes.size() == 0) return 0;
		int y=0;
		for (Node n : nodes) {
			y += n.getYCenter();
		}
		return y / nodes.size();
	}
	
	public Tree(Graph graph, boolean firstTime) {
		this.graph = graph;
		this.nodes = graph.getNodes();
		this.edges = graph.getEdges();
		midY = meanY(nodes);
		this.firstTime = firstTime;
	}
	
	private Deque<Node> tarjanStack;
	private int tarjanIndex;
	final private int TARJAN_UNDEF = -1;
	
	/*
	 * Here we use Tarjan's (1972) strongly connected components algorithm to turn
	 * a digraph into a tree where some of the nodes in the tree are
	 * strongly connected components (contain one or more cycles).
	 */
	private void tarjan() {
		assert(tnodes.size() == 0);
		
		tarjanStack = new ArrayDeque<Node>();
		tarjanIndex = 0;
		for (Node v : nodes) {
			v.setTarjanIndex(TARJAN_UNDEF);
		}
		for (Node v : nodes) {
			if (v.getTarjanIndex() == TARJAN_UNDEF) {
				strongConnect(v);
			}
		}
		assert(tarjanStack.size() == 0);
	}
	
	private void strongConnect(Node v) {
		v.setTarjanIndex(tarjanIndex);
		v.setTarjanLowLink(tarjanIndex);
		++tarjanIndex;
		tarjanStack.push(v);
		for (Edge e : edges) {
			if (e.getSource() != v) continue;
			Node w = e.getTarget();
			if (w.getTarjanIndex() == TARJAN_UNDEF) {
				strongConnect(w);
				v.setTarjanLowLink(Math.min(v.getTarjanLowLink(), w.getTarjanLowLink()));
			} else if (tarjanStack.contains(w)) {
				v.setTarjanLowLink(Math.min(v.getTarjanLowLink(), w.getTarjanIndex()));
			}
		}
		
		if (v.getTarjanLowLink() == v.getTarjanIndex()) {
			List<Node> scc = new ArrayList<Node>();
			while (true) {
				Node w = tarjanStack.pop();
				scc.add(w);
				if (w == v) break;
			}
			TNode tn = new TNode(scc, edges);
			if (firstTime) {
				tn.guessUpper();
			} else {
				tn.setUpper(meanY(scc) < midY);
			}
			tnodes.add(tn);
		}
	}

	private int calcOutLength(Edge start) {
		Deque<Edge> path = new ArrayDeque<Edge>();
		path.push(start);
		return calcOutLength(path);
	}
	
	private int calcOutLength(Deque<Edge> path) {
		TNode endpoint = tnodeMap.get(path.peek().getTarget());
		int maxLen = 0;
		for (Edge e : endpoint.out) {
			path.push(e);
			int len = calcOutLength(path);
			path.pop();
			if (maxLen < len) maxLen = len;
		}
		endpoint.setOutLength(maxLen);
		return maxLen+1;
	}
	
	private void calcOutLength() {
		for (TNode n : tnodes) {
			if (n.getOutLength() != -1) continue;
			int maxLen = 0;
			for (Edge e : n.out) {
				int len = calcOutLength(e);
				if (maxLen < len) maxLen = len;
			}
			n.setOutLength(maxLen);
		}
	}
	
	private void calcInLength() {
		for (TNode n : tnodes) {
			if (n.getInLength() != -1) continue;
			calcInLength(n);
		}
	}

	private int calcInLength(TNode n) {
		if (n.in.size() == 0) {
			n.setInLength(0);
		} else {
			int maxLen = 0;
			for (Edge e : n.in) {
				int len = calcInLength(tnodeMap.get(e.getSource()));
				if (maxLen < len) maxLen = len;
			}
			n.setInLength(1 + maxLen);
		}
		return n.getInLength();
	}

	private void setUpper(List<Edge> edges, boolean upper) {
		for (Edge e : edges) {
			TNode n = tnodeMap.get(e.getTarget());
			n.setUpper(upper);
			setUpper(n.out, upper);
		}
	}
	
	private void setUpper() {
		for (TNode n : tnodes) {
			if (n.in.size() != 0) continue;
			setUpper(n.out, n.isUpper());
		}
	}
	
	private int divideVertical() {
		int maxLen = 0;
		for (TNode n : tnodes) {
			int len = Math.max(n.getInLength(), n.getOutLength());
			if (maxLen < len) maxLen = len;
		}
		return maxLen;
	}

	private int clamp(int n, int l, int h)
	{
		assert(l <= h);
	    return (n > h ? h : (n < l ? l : n));
	}
	
	private void place() {
		setUpper();
		int middle = divideVertical();

		List<TreeRow> grid = new ArrayList<TreeRow>();
		for (TNode n : tnodes) {
			int at;
			if (n.getInLength() == 0) {
				at = n.isUpper()? 0 : middle*2;
			} else if (n.getOutLength() == 0) {
				at = middle;
			} else {
				at = n.isUpper()? middle - n.getOutLength() : middle + n.getOutLength();
			}
			while (grid.size() <= at) {
				grid.add(new TreeRow());
			}
			TreeRow row = grid.get(at);
			row.add(n);
		}

		int width=0;
		//int height = Style.LineSpacing * (grid.size()-1);
		for (int hx=0; hx < grid.size(); hx++) {
			TreeRow line = grid.get(hx);
			int w = line.width();
			if (width < w) width = w;
			//height += line.height();
		}

		int y = Style.Top;
		for (int hx = 0; hx < grid.size(); hx ++) {
			TreeRow line = grid.get(hx);
			int slack = width - line.width();
			int center = Style.Left + line.width()/2 + slack/2;

			if (hx == 0) {
			    line.sortByX();
			} else {
				line.minimizeEdgeLength(hx <= middle);
				// This visual optimization is not worth it?
				int to = clamp(line.getBestX(), center - slack/2, center + slack/2);
				//System.err.println("y " + y + " hx " + hx + " center "+center+" best-x "+line.getBestX() + " to "+to);
				center = to;
			}
			
			y += line.height() / 2;
			//System.err.println(hx + " y "+y);
			line.layout(center, y);
			y += Style.LineSpacing + line.height() / 2;
		}
	}
	
	/**
	 * This is (very) loosely based on Boker and McArdle (2001)
	 */
	public void layout() {
		tarjan();
		
		for (TNode tn : tnodes) {
			for (Node n : tn.nodes) {
				tnodeMap.put(n, tn);
			}
		}
		

		calcOutLength();
		calcInLength();
		place();
		
		// This doesn't work here. Not sure why.
		//graph.updateRenderingHints();
	}
	
	/**
	 * This is the new layout algorithm by Thomas Mangold. 
	 */
	public void layoutThomas() {
	    // TODO implement
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
