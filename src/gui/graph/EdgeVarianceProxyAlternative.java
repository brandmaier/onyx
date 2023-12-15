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
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.PathIterator;
import java.util.ArrayList;
import java.util.List;

import geometry.GeometricObject;
import geometry.LineSegment;
import geometry.Rectangle;

public class EdgeVarianceProxyAlternative extends EdgeProxy
{

	public double theta; // angle of path
	private double arcCentX;
	private double arcCentY;
	private double a1x;
	private double a1y;
	private double a2x;
	private double a2y;
	
	@Override
	public void updateArrow(Edge edge){
		edge.arrows.clear();
		
		final double turnAng = .3; //.4;
		
		double extrarad=.5;
		
		if (edge.source.renderingHintArcPosition == Node.NORTH) {

			double theta = Math.PI / 2;
			double turn = -turnAng;
			edge.arrows.add( new Arrow(edge.scx + edge.rad+extrarad, edge.source.getY(), theta + turn));
			edge.arrows.add( new Arrow( edge.scx - edge.rad-extrarad, edge.source.getY(), theta - turn));

		} else if (edge.source.renderingHintArcPosition == Node.SOUTH) {


			double theta = -Math.PI / 2;
			double turn = +turnAng;
			edge.arrows.add( new Arrow( edge.scx + edge.rad+extrarad,
					edge.source.getY() + edge.source.getHeight(), theta + turn));
			edge.arrows.add( new Arrow( edge.scx - edge.rad-extrarad,
					edge.source.getY() + edge.source.getHeight(), theta - turn));


		} else {
			//TODO NF
		}
		
		edge.arrows.clear();
		double turn=-.5;//.3;
		
		edge.arrows.add( new Arrow(a1x,a1y, (Math.PI+theta) + turn));
		edge.arrows.add( new Arrow( a2x, a2y, (Math.PI+theta) - turn));
	}

	@Override
	public void updateLabel(Edge edge) {
		/*if (edge.source.isRenderingHintArcPositionUp()) {
			edge.lx = edge.scx;
			edge.ly = edge.source.getY() - edge.rad - 2 - 5;
		} else {
			edge.lx = edge.scx;
			edge.ly = edge.source.getY() + edge.source.getHeight() + edge.rad + 2 + 14+5;
		}*/

//		edge.label.
		int lw = edge.getLabel().getWidth();
		int lh = edge.getLabel().getHeight();
		
		
		edge.yOffsetLabel = 0;
		// intersect label with ray from source //
		//Rectangle rect = new Rectangle(0,0,lw,lh);
		//double rot = (4*Math.PI-theta)%(2*Math.PI);
		
		//LineSegment line = new LineSegment(lw/2, lh/2, (int)(lw/2+Math.cos(theta)*100), (int)(lh/2+Math.sin(theta)*100));
		//List<java.awt.Point> cut = line.intersect_fuzzy(rect, 1);//line.intersect(rect);
		
		
	/*	if (cut.size() == 1) {
			xoff = cut.get(0).x;
			yoff = cut.get(0).y;
		} else {
			System.err.println("No or too many cut points!");
			for (int i = 0; i < cut.size(); i++) {
				System.out.println(i+"."+cut.get(i));
			}
		}*/
		//System.out.println(Math.cos(theta)+ " - "+Math.sin(theta));
		
		double xshift = Math.cos(theta);  // from -1 (left) to +1 (right) 
		double yshift = Math.sin(theta);  // from -1 (north) to +1 (south)
		
		// if xshift == -1, xoff is -lw, if xshift == +1, xoff=0
		// if yshift == -1, yoff is -lh, if yshift == +1, yoff=0
		
		double xoff = ((xshift-1)/2.0) * lw;
		double yoff = ((yshift-1)/2.0)*lh;
		
		final int dist = 45;
		System.out.println("Theta "+theta+" Offset"+xoff+";"+yoff+" lw"+lw+" lh"+lh);
		edge.lx = (int) (edge.scx+Math.cos(theta)*dist + xoff + lw/2);	//lw/2 compensate for default centering
		edge.ly = (int) (edge.scy+Math.sin(theta)*dist + yoff+12);
		
	}

	@Override
	public void updatePath(Edge edge, GeometricObject r1, GeometricObject r2) {

		edge.rad = (edge.source.getWidth()+edge.source.getHeight()/2) / 3;
		
		edge.scx = edge.source.getX() + edge.source.getWidth() / 2;
		edge.scy = edge.source.getY() + edge.source.getHeight() / 2;
		
		double inset = 10;		//TODO: inset 10 is nicer but there is a potential bug with the PDF/PNG export library!
		
		edge.fromX = edge.scx;
		edge.fromY = edge.scy;
		if (edge.ctrlAutomatic) {
		
			if (edge.getSource().renderingHintArcPosition==Node.NORTH) {
				edge.relctrlx1 = 0; 
				edge.relctrly1 = -edge.source.getHeight() / 2 - 50;
			} else {
				edge.relctrlx1 = 0; 
				edge.relctrly1 = +edge.source.getHeight() / 2 + 50;				
			}
		
		}
		edge.ctrlx1 = edge.scx + edge.relctrlx1;
		edge.ctrly1 = edge.scy + edge.relctrly1;
		
		if (edge.getSource().renderingHintArcPosition==Node.NORTH) {
			edge.lines.add(new LineSegment(edge.scx - edge.rad, edge.source.getY() - edge.rad, edge.scx
					+ edge.rad, edge.source.getY() + edge.rad));
			
			edge.edgePath = new Arc2D.Double(edge.scx - edge.rad, edge.source.getY() - edge.rad,
					2 * edge.rad, 2 * edge.rad, 0+inset, 180-2*inset, Arc2D.OPEN);
		
		} else {
			edge.lines.add(new LineSegment(edge.scx - edge.rad, edge.source.getY()
					+ edge.source.getHeight() - edge.rad, edge.scx + edge.rad, edge.source.getY()
					+ edge.source.getHeight() + edge.rad));
			
			edge.edgePath = new Arc2D.Double(edge.scx - edge.rad, edge.source.getY()
					+ edge.source.getHeight() - edge.rad, 2 * edge.rad, 2 * edge.rad, -0-inset,
					-180.00+2*inset, Arc2D.OPEN);
	
		}

		double dy = edge.ctrly1-edge.fromY;
		double dx = edge.ctrlx1-edge.fromX;
		theta = Math.atan2(dy,dx);
		//double thetaDeg = theta* 180/Math.PI;
		//System.out.println("Winkel "+theta+" ;"+thetaDeg);
		
		final double dist = 23;
		arcCentX = Math.cos(theta)* dist;
		arcCentY = Math.sin(theta)*dist;
		
		//System.out.println(arcCentX+","+arcCentY);
		
		double inward = 0;
		double turnA = 1.4;
		
		/*
		 *  angles in JAVA  
        |90  
        |
180-----------0 
        |
        |270
		 */
		
		double fromSeg = (360-(theta)/(2*Math.PI)*360-10) % 360;
		double toSeg = (360-(theta)/(2*Math.PI)*360+10) % 360;
		
		edge.edgePath = new Arc2D.Double(edge.scx +arcCentX - edge.rad, edge.scy + arcCentY - edge.rad, 2 * edge.rad, 2 * edge.rad, fromSeg,
				180-2*inset, Arc2D.OPEN);
		
		
		
		System.out.println(fromSeg+"->"+toSeg +":: "+ 
		((360-(theta/(2*Math.PI)*360))%360)
		);
		
		edge.lines.clear();
		/*edge.lines.add(new LineSegment((int)(edge.scx +arcCentX - edge.rad), (int)(edge.source.getY() 
				+ arcCentY - edge.rad), edge.scx + edge.rad, edge.source.getY()
				+ edge.source.getHeight() + edge.rad));
		*/
		PathIterator flatteningPathIterator = new FlatteningPathIterator(
				edge.edgePath.getPathIterator(new AffineTransform()), 30);
		double[] coords = new double[6];
		flatteningPathIterator.currentSegment(coords);
		double lastx = coords[0];
		double lasty = coords[1];
		while (!flatteningPathIterator.isDone()) {
			coords = new double[6];
			flatteningPathIterator.currentSegment(coords);
			flatteningPathIterator.next();

			edge.lines.add(new LineSegment((int) lastx, (int) lasty,
					(int) coords[0], (int) coords[1]));
			lastx = coords[0];
			lasty = coords[1];
		}
		//System.out.println("NUMBA"+edge.lines.size());
		
		// calculate placement for arrow heads
	
		a1x = edge.scx + arcCentX+ Math.cos(theta+turnA)* (edge.rad-inward);
		a1y = edge.scy + arcCentY+ Math.sin(theta+turnA)*(edge.rad-inward);
		
		a2x = edge.scx + arcCentX+ Math.cos(theta-turnA)* (edge.rad-inward);
		a2y = edge.scy + arcCentY+ Math.sin(theta-turnA)*(edge.rad-inward);
		
		((Arc2D.Double)edge.edgePath).setAngles(edge.scx + arcCentX+ Math.cos(theta+turnA), 
				edge.scy + arcCentY+ Math.sin(theta+turnA), edge.scx + arcCentX+ Math.cos(theta-turnA), edge.scy + arcCentY+ Math.sin(theta-turnA));
		
	}	


}
