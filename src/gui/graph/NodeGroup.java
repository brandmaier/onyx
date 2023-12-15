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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import engine.ModelRequestInterface;
import engine.Statik;


public class NodeGroup extends ArrayList<Node>  {

	//nodes;
	
	public NodeGroup()
	{
		super();
	}
	
	public NodeGroup(List<Node> nodes)
	{
		this();
		for (Node node : nodes) this.add(node);
	}
	
/*	public void add(Node node)
	{
		nodes.add(node);
	}
	
	public void remove(Node node)
	{
		nodes.remove(node);
	}
	*/
	public void connect(ModelRequestInterface mri, Node source)
	{
		for (int i=0; i < size(); i++)
		{
			Node node = get(i);
			Edge edge = new Edge(source, node);
			mri.requestAddEdge(edge);
		}
	}
	
	/*public Rectangle getBoundingBox()
	{
		int xmin = Integer.MAX_VALUE;
		int xmax = Integer.MIN_VALUE;
		int ymin = Integer.MAX_VALUE;
		int ymax = Integer.MIN_VALUE;
	}*/
	
	public Point getCenter()
	{
		int x = 0;
		int y = 0;
		for (Node node : this)
		{
			x += node.getX();
			y += node.getY();
		}
		
		x /= size();
		y /= size();
	
		return new Point(x,y);
	}
	
	
	public void flipHorizontally(Graph graph)
	{
		Point center = getCenter();
		for (Node node: this) {
			node.setY( 2*center.y-node.getY());
			
			for (Edge edge : graph.getEdges()) {
				if (edge.source==node || edge.target == node) {
					edge.ctrly1 = 2*center.y - edge.ctrly1;
					edge.ctrly2 = 2*center.y - edge.ctrly2;
				}
			}
		}
		
		
		
	}
	
	public void flipVertically(Graph graph)
	{
		Point center = getCenter();
		for (Node node: this) {
			node.setX( 2*center.x-node.getX());
		
		
		for (Edge edge : graph.getEdges()) {
			if (edge.source==node || edge.target == node) {
				edge.ctrlx1 = 2*center.x - edge.ctrlx1;
				edge.ctrlx2 = 2*center.x - edge.ctrlx2;
			}
		}
		}
	}	
	
	public void select(boolean status)
	{
		for (Node node : this) node.setSelected(status);
	}

	   public static double[][] pcaTransformationMatrix(double[][] cov, double minimalVariance) {
	        int dim = cov.length;
	        
	        double[][] q = Statik.identityMatrix(dim); double[] ev = new double[dim]; 
	        Statik.eigenvalues(cov, 0.000001, ev, q);
	        
	        int dim2 = 0; for (int i=0; i<dim; i++) if (Math.abs(ev[i]) >= minimalVariance) dim2++;
	        if (dim2==0) return new double[0][];
	        
	        double[] work = new double[dim];
	        Statik.copy(ev, work);
	        Arrays.sort(ev); int[] ixList = new int[dim], ixListInv = new int[dim];
	        for (int i=0; i<dim2; i++) ixList[i] = dim - 1 - Arrays.binarySearch(ev, work[i]);
	        for (int i=0; i<dim2; i++) ixListInv[ixList[i]] = i;

	        double[][] erg = new double[dim][dim2];
	        for (int i=0; i<dim2; i++) for (int j=0; j<dim; j++) erg[j][ixList[i]] = q[j][i];
	        
	        return erg;   
	    }
	
	public void sort() {
		
		double[][] matrix = new double[size()][2];
		for (int i=0; i < size(); i++) { matrix[i][0]= get(i).getX(); matrix[i][1]=get(i).getY();}

		double[][] cov = Statik.multiply(  Statik.transpose(matrix), matrix );
		
		double [][] transform = pcaTransformationMatrix(cov, 1.0);
		
		double[] axis = transform[0];
		
		double[] resort = Statik.multiply( matrix,axis );
		
		
		class NodeGroupComparator implements java.util.Comparator<Node>
		{

			public HashMap<Node, Double> map = new HashMap<Node, Double>();
			
			@Override
			public int compare(Node arg0, Node arg1) {
				if ( map.get(arg0) > map.get(arg1) )
					{
					  return -1;
					} else if ( map.get(arg0) < map.get(arg1) )
					{
						return 1;
					} else {
						return 0;
					}
			}
			
		}
		
		NodeGroupComparator nc = new NodeGroupComparator();
		for (int i=0; i < size();i++)
		{
			nc.map.put(get(i), resort[i]);
		}

		Collections.sort(this, nc);
	}

	/*public boolean contains(Node node) {
		return nodes.contains(node);
	}*/
	
}
