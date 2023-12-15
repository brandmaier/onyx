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


import java.awt.Graphics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class NodeGroupManager {

	public static int SIZE = 10;
	
	NodeGroup[] groups;
	boolean active[];
	
	int activeGroup = 0;
	
	int iter = -1;
	
	public NodeGroupManager()
	{
		groups = new NodeGroup[SIZE];
		active = new boolean[SIZE];
	}

	public boolean contains(NodeGroup nodeGroup) {
		// TODO Auto-generated method stub
		return false;
	}
	
	public void setActive(int i, boolean a)
	{
		active[i] = a;
		
		debug();
	}

	private void debug() {
		for (int i=0; i < 10; i++)
		{
			System.out.print(active[i]+" ");
		}
		System.out.println();
	}

	/*public void add(NodeGroup nodeGroup) {
		this.groups.add(nodeGroup);
	}*/
	
	/*public void nextActiveGroup()
	{
		activeGroup+=1;
		activeGroup %= groups.length;
	}*/

	public void set(int groupId, NodeGroup nodeGroup) {
		if ((groupId > 0) && (groupId < groups.length)) {
			groups[groupId] = nodeGroup;
		} else {
			System.err.println("Warning! Tried to add node group larger than node group capacity!");
		}
		
	}

	public void drawAnnotation(Graphics g, Node node) {
		
		// collect ids from all nodegroups containing node
		
		Vector<Integer> ids = new Vector<Integer>();
		int i = 0;
		for (NodeGroup ng : groups)
		{
			if (ng != null && active[i] && ng.contains(node)) {
				
				ids.add( i );
			}
			i++;
		}
		
		// draw annotation
		String str = "";
		for (int ii=0; ii < ids.size(); ii++) { 
			str=str+Integer.toString(ids.get(ii));
			if (ii < (ids.size()-1)) str+=",";
			}
		
		int offset = 0;
		if (node.isObserved()) {
			offset += 5;
		}
		
		g.drawString(str, node.getX()+node.getWidth()+offset, node.getY()+node.getHeight());
		
	}

	public void toggleActive(int groupId) {
		setActive(groupId, !active[groupId]);
	}

	public NodeGroup get(int groupId) {
		return groups[groupId];
	}

	public boolean isActive(int groupId) {
		return active[groupId];
	}

	public List<Integer> getActiveGroupMembership(Node dragNode) {
		List<Integer> memberIn = new ArrayList<Integer>();
		for (int i=0; i < groups.length; i++)
		{
			if (active[i] && groups[i] != null && groups[i].contains(dragNode))
			{
				memberIn.add(i);
			}
		}
		return memberIn;
	}

/*	@Override
	public boolean hasNext() {
		return (iter >= this.groups.length);
	}

	@Override
	public NodeGroup next() {
		iter++;
		return get(iter);
	}

	@Override
	public void remove() {
		
		groups[iter] = null;
	}*/
	
}
