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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TreeRow {
	final private List<TNode> nodes = new ArrayList<TNode>();
	private int bestX;
	
	public void add(TNode n) {
		nodes.add(n);
	}
	
	public int width() {
		int width = 0;
		for (TNode n : nodes) {
			width += n.width();
		}
		width += (nodes.size() - 1) * Style.NodeMargin;
		return width;
	}

	public int height() {
		int height = 0;
		for (TNode n : nodes) {
			int h = n.height();
			if (height < h) height = h;
		}
		return height;
	}

	public void minimizeEdgeLength(boolean upper) {
		int x=0;
		for (TNode n : nodes) {
			n.calcBestX(upper);
			x += n.getBestX();
		}
		
		if (nodes.size() > 0) bestX = x/nodes.size();
		
		Collections.sort(nodes, new Comparator<TNode>() {
			@Override public int compare(TNode arg0, TNode arg1) {
				return arg0.getBestX() - arg1.getBestX();
			}
		});
	}

	public int getBestX() {
		return bestX;
	}

	public void layout(int center, int y) {
		int w = width();
		int x = center - w/2;
		for (TNode n : nodes) {
			n.layout(x + n.width()/2, y);
			x += n.width() + Style.NodeMargin;
		}
	}

	public void sortByX() {
		for (TNode n : nodes) {
			n.calcMeanX();
		}

		Collections.sort(nodes, new Comparator<TNode>() {
			@Override public int compare(TNode arg0, TNode arg1) {
				return arg0.getBestX() - arg1.getBestX();
			}
		});
	}

}
