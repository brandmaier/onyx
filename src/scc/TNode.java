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
import gui.graph.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TNode {
	final List<Node> nodes;
	// note: excludes doubleheaded edges
	final List<Edge> in; // we are target
	final List<Edge> internal;
	final List<Edge> out; // we are source
	// all double headed edges go here
	final List<Edge> doubleHeaded = new ArrayList<Edge>();
	private boolean upper;
	
	private int outLength = -1;
	private int inLength = -1;
	private int bestX;
	final private int meanHeight, meanWidth;
	private int midRadius, outerRadius;

	public TNode(List<Node> nodes, List<Edge> edges) {
		this.nodes = Collections.unmodifiableList(nodes);
		List<Edge> in = new ArrayList<Edge>();
		List<Edge> internal = new ArrayList<Edge>();
		List<Edge> out = new ArrayList<Edge>();
		
		for (Edge e : edges) {
			boolean src = nodes.contains(e.getSource());
			boolean dest = nodes.contains(e.getTarget());
			if (!e.isDoubleHeaded()) {
				if (src && dest) internal.add(e);
				else if (dest) in.add(e);
				else if (src) out.add(e);
			} else {
				if (src || dest) doubleHeaded.add(e);
			}
		}
		
		this.in = Collections.unmodifiableList(in);
		this.internal = Collections.unmodifiableList(internal);
		this.out = Collections.unmodifiableList(out);
		
		if (nodes.size() > 0) {
			int w=0, h=0;
			for (Node n : nodes) {
				w += n.getWidth();
				h += n.getHeight();
			}
			meanWidth = w/nodes.size();
			meanHeight = h/nodes.size();
			double diag = Math.sqrt(meanWidth*meanWidth + meanHeight*meanHeight) + Style.NodeMargin;
			midRadius = (int)(diag * Math.max(nodes.size(), 3) / (2 * Math.PI));
			outerRadius = midRadius + (int) diag/2;
		} else {
			meanWidth = meanHeight = outerRadius = midRadius = 0;
		}
	}
	
	public void guessUpper() {
		if (nodes.size() == 1 && in.size() == 0 && out.size() == 1 && doubleHeaded.size() == 1) {
			Edge e = doubleHeaded.get(0);
			if (e.getSource() == e.getTarget()) {
				setUpper(false);  // looks like an error variance
				return;
			}
		}
		setUpper(true);
	}

	public int width() {
		if (nodes.size() == 1) {
			return meanWidth;
		} else {
			return outerRadius * 2;
		}
	}

	public int height() {
		if (nodes.size() == 1) {
			return meanHeight;
		} else {
			return outerRadius * 2;
		}
	}

	/***
	 * The edges provide should be from Nodes that have already been placed
	 * by the layout algorithm.
	 * 
	 * @param edges
	 */
	public void calcBestX(boolean upper) {
		int sumX = 0;
		int countX = 0;
		final List<Edge> edges = upper? in : out;
		for (Edge e : edges) {
			Node n = upper? e.getSource() : e.getTarget();
			sumX += n.getXCenter();
			countX += 1;
		}
		if (countX == 0) countX = 1;
		bestX = sumX/countX;
	}

	public void calcMeanX() {
		int sumX = 0;
		int countX = 0;
		for (Node n : nodes) {
			sumX += n.getXCenter();
			countX += 1;
		}
		if (countX == 0) countX = 1;
		bestX = sumX/countX;
	}

	public int getBestX() {
		return bestX;
	}

	private double bestStartAngle() {
		int edges[] = new int[nodes.size()];
		for (int x=0; x < nodes.size(); x++) edges[x]=0; // unnecessary?
		for (Edge e : in) {
			edges[nodes.indexOf(e.getTarget())] += 1;
		}
		int goodness[] = new int[2 * nodes.size()];
		int maxGoodness=0;
		for (int s=0; s < nodes.size(); s++) {
			int count=0;
			for (int w=s; w < s + nodes.size()/2; w++) {
				count += edges[w % nodes.size()];
			}
			goodness[s] = count;
			goodness[s + nodes.size()] = count;
			if (maxGoodness < count) maxGoodness = count;
		}
		boolean goodArea = false;
		double bestStart = 0;
		int bestSize = 0;
		for (int s=0; s < nodes.size() * 2; s++) {
			if (goodArea == false && goodness[s] == maxGoodness) goodArea = true;
			else if (goodArea == true && goodness[s] != maxGoodness) break;
			if (goodArea == true) {
				bestStart += s;
				bestSize += 1;
			}
		}
		if (bestSize == 0) return 0;
		return -(bestStart/bestSize) * 2*Math.PI / nodes.size();
	}
	
	public void layout(int x, int y) {
		if (nodes.size() == 1) {
			nodes.get(0).setXCenter(x);
			nodes.get(0).setYCenter(y);
		} else {
			double slice = 2*Math.PI / nodes.size();
			double angle = bestStartAngle() + Math.PI + slice/2;
			for (Node n : nodes) {
				int nx = (int) (x + Math.cos(angle) * midRadius);
				int ny = (int) (y + Math.sin(angle) * midRadius);
				n.setXCenter(nx);
				n.setYCenter(ny);				
				angle += slice;
			}
		}
	}

	public boolean isUpper() {
		return upper;
	}

	public void setUpper(boolean upper) {
		this.upper = upper;
	}

	public int getOutLength() {
		return outLength;
	}

	public void setOutLength(int outLength) {
		this.outLength = outLength;
	}

	public int getInLength() {
		return inLength;
	}

	public void setInLength(int inLength) {
		this.inLength = inLength;
	}

}
