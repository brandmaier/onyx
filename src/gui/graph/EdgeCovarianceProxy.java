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

import geometry.GeometricObject;
import geometry.LineSegment;
import gui.frames.MainFrame;

import java.awt.Color;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.FlatteningPathIterator;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.util.Collections;
import java.util.List;

public class EdgeCovarianceProxy extends EdgeProxy {


	/*
	 * public EdgeCovarianceProxy(Edge edge) { super(edge); }
	 */

	@Override
	public void updateArrow(Edge edge) {
		edge.arrows.clear();

		/*
		 * if (MainFrame.GRAPH_DEBUGGING) { g.setColor(Color.green); for
		 * (LineSegment line : lines) { line.paint(g2d); }
		 * g.setColor(Color.black); }
		 */

		edge.arrows.add(new Arrow(edge.fromX, edge.fromY, edge.fromAngle));
		edge.arrows.add(new Arrow(edge.toX, edge.toY, edge.toAngle));
	}

	@Override
	public void updateLabel(Edge edge) {

		// calculate cumulative, relative length of the segments
		double[] cumRelativeLength = new double[edge.lines.size()];
		int i = 0;
		for (LineSegment line : edge.lines) {
			double l = line.length();
			if (i != 0)
				cumRelativeLength[i] = cumRelativeLength[i - 1];
			cumRelativeLength[i] += l;
			i++;
		}
		for (i = 0; i < edge.lines.size(); i++)
			cumRelativeLength[i] /= cumRelativeLength[cumRelativeLength.length - 1];

		// find best-matching  line segment and interpolate on it
		for (i = 0; i < cumRelativeLength.length; i++) {
			if (edge.edgeLabelRelativePosition <= cumRelativeLength[i]) {

				LineSegment line = edge.lines.get(i);

				double left = 0;
				if (i > 0)
					left = cumRelativeLength[i - 1];
				double right = cumRelativeLength[i];
				
			
				
				
				double relWithin = (edge.edgeLabelRelativePosition - left)
						/ (right - left);

				// switch left & right ?
				if (edge.source.getX() < edge.target.getX()) {
					relWithin = 1.0-relWithin;
					//System.out.println("INVERT!");
				}
				
				// set label position
				edge.lx = (int) Math.round(line.x1 + (line.x2 - line.x1) * relWithin);
				edge.ly = (int) Math.round(line.y1 + (line.y2 - line.y1) * relWithin);

				break;
			}
		}

	}

	@Override
	public void updatePath(Edge edge, GeometricObject r1, GeometricObject r2) {

		
		
		if (edge.isDoubleHeaded()) {
			r1 = r1.extrude(getShapePadding());
			r2 = r2.extrude(getShapePadding());
		}
		
		/* Set source and target of path as centers of the adjacent nodes */
		edge.scx = edge.source.getX() + edge.source.getWidth() / 2;
		edge.scy = edge.source.getY() + edge.source.getHeight() / 2;

		edge.tcx = edge.target.getX() + edge.target.getWidth() / 2;
		edge.tcy = edge.target.getY() + edge.target.getHeight() / 2;

		/***
		 * 
		 * new block for updating quadratic Bezier curves
		 * 
		 */

		// -- swap to and from coordinates such that source is always left of the other
		double leftx, lefty, rightx, righty;
		boolean swap;
		if (edge.scx < edge.tcx) {
			leftx = edge.scx;
			lefty = edge.scy;
			rightx = edge.tcx;
			righty = edge.tcy;
			swap = false;
		} else {
			leftx = edge.tcx;
			lefty = edge.tcy;
			rightx = edge.scx;
			righty = edge.scy;
			swap = true;
		}

		// start path at left node

		edge.generalPath = new GeneralPath();
		edge.generalPath.moveTo(leftx, lefty);

		// calculate orthogonal vector to direct connection of source and target

		double linex = (rightx - leftx);
		double liney = (righty - lefty);
		double orthogonalx = liney;
		double orthogonaly = -linex;
		;
		double onorm = Math.sqrt(orthogonalx * orthogonalx + orthogonaly
				* orthogonaly);
		orthogonalx /= onorm;
		orthogonaly /= onorm;
		
		boolean same = (linex==0 && liney==0);

		// if source == target, then orthogonality has to be calculated
		// differently
		if (same) {
			orthogonalx = Double.NaN;
			orthogonaly = Double.NaN; // TODO ; this is just some random guess
		}

		// if edge is controlled automatically, adapt control points
		if (edge.ctrlAutomatic) {
			edge.relctrlx1 = (leftx + orthogonalx * edge.getCurvature())
					- edge.scx;
			edge.relctrly1 = (lefty + orthogonaly * edge.getCurvature())
					- edge.scy;
			edge.relctrlx2 = (rightx + orthogonalx * edge.getCurvature() - edge.tcx);
			edge.relctrly2 = (righty + orthogonaly * edge.getCurvature() - edge.tcy);
		}
		
		if (same && edge.ctrlAutomatic) {
			edge.relctrlx1 = -50;
			edge.relctrly1 = -edge.source.getHeight() / 2 - 30;
			edge.relctrlx2 = +50;
			edge.relctrly2 = -edge.source.getHeight() / 2 - 30;
		}

		edge.ctrlx1 = edge.scx + edge.relctrlx1;
		edge.ctrly1 = edge.scy + edge.relctrly1;
		edge.ctrlx2 = edge.tcx + edge.relctrlx2;
		edge.ctrly2 = edge.tcy + edge.relctrly2;

		// draw general path with Bezier cubic curve
		edge.generalPath.curveTo(edge.ctrlx1, edge.ctrly1, edge.ctrlx2,
				edge.ctrly2, rightx, righty);

		// find intersections with shape by tracing a linearization

		PathIterator flatteningPathIterator = new FlatteningPathIterator(
				edge.generalPath.getPathIterator(new AffineTransform()), .5); 
		// Flattening: the smaller, the better
		// .5 seems very fine-grained
		// 4 seems enough ...
		// 8 too little, not fine enough

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

		edge.fromX = -999;
		edge.fromY = -999;
		edge.toX = -999;
		edge.toY = -999;
		edge.toAngle = 0;
		edge.fromAngle = 0;
	
		// intersect all these line segments to find the right spot!
		// save the intersection as edge.fromX and edge.fromY, respectively
		// edge.toX and edge.toY
		// temporarily store the linearizations as edgeLinFrom and edgeLinTo;
		//
		// important: find the last! intersection as the relevant intersection
		// there can be intermediate intersections that should not be hits
		edge.edgeLinFrom = null; edge.edgeLinTo = null;
		Point bestPoint = null;
		if (swap) Collections.reverse(edge.lines);
		
		double defFuzz = .1;
		
		double fuzziness = defFuzz; //2
		int maxJ = 8;
		for (int j=0; j<maxJ;j++) {	// do multiple, more fuzzy searches, if no point is found
		
		for (LineSegment line : edge.lines) {
			
			if (edge.edgeLinFrom != null) break;
			
			// ------ intersect with source (r1) ----------
			List<Point> isects = line.intersect_fuzzy(r1, fuzziness);
			for (Point p : isects) {

				bestPoint = p;

				edge.fromX = p.x;
				edge.fromY = p.y;

				edge.edgeLinFrom = line;

				edge.fromAngle = angleFromLine(edge.edgeLinFrom, !swap, p);
			}

			

		}
		
		if (edge.fromX == -999 && edge.fromY == -999) {
			//System.err.println("Could not find intersection!");
			fuzziness = fuzziness*2;
		} else {
			j = maxJ;
		}
		
		}
		
		Collections.reverse(edge.lines);
		
		//for (LineSegment line : edge.lines) {
		fuzziness = defFuzz;
		for (int j=0; j<maxJ;j++) {
		
		for (LineSegment line : edge.lines) {
			//LineSegment line = edge.lines.get(j);		
			
		//	if (line==edge.edgeLinFrom) continue;
			
			if (edge.edgeLinTo != null) break;
			
			List<Point>  isects = line.intersect_fuzzy(r2, fuzziness);

			for (Point p : isects) {

				if (edge.source == edge.target &&  bestPoint == p)
					continue;

				edge.toX = p.x;
				edge.toY = p.y;

				edge.edgeLinTo = line;

				edge.toAngle = angleFromLine(line,swap, p);
//				edge.toAngle = (angleFromLine(line,TOL, swap, p)+ angleFromLine(edge.lines.get(j+2),TOL, swap, p))/2;
		//		edge.toAngle =  angleFromLine(edge.lines.get(j+1),TOL, swap, p);
			}

		}
		
		if (edge.toX == -999 && edge.toY == -999) {
			//System.err.println("Could not find intersection!");
			fuzziness = fuzziness*2;
		} else {
			j = maxJ;
		}
		
		}


		edge.edgePath = edge.generalPath;
		
		// optional: cut back path

	}

	/**
	 * return the angle of a line
	 * 
	 * @param line
	 * @param TOL
	 * @param swap
	 * @param p
	 * @return
	 */
	private double angleFromLine(LineSegment line, boolean swap, Point p) {
		double toAngle = 0;
		
		double dx = (line.x2 - line.x1);
		double dy = (line.y2 - line.y1) ;
		
		toAngle = Math.atan2(dy, dx);
		
		if (swap) toAngle+=Math.PI % (2.0*Math.PI);
		
		return(toAngle);
	}

/*	private double[] getDisplacement(double x1, double y1, double x2,
			double y2, double deld) {
		final double EXTRA_DISTANCE_TO_NODE = 1;
		// double deld = edge.target.getStrokeWidth()+EXTRA_DISTANCE_TO_NODE; //
		// 1 = extra distance
		double dx = x2 - x1;
		double dy = y2 - y1;
		double dd = Math.sqrt(dx * dx + dy * dy);
		dx = dx / dd * deld;
		dy = dy / dd * deld;

		return new double[] { dx, dy };
	}*/
}
