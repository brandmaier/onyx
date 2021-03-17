package gui.graph;

import geometry.GeometricObject;
import geometry.Line;
import geometry.LineSegment;

import java.awt.Point;
import java.awt.geom.Line2D;
import java.util.List;

public class EdgeRegression extends EdgeProxy {

	/*public EdgeRegression(Edge edge) {
		super(edge);
	}*/

	@Override
	public void updateArrow(Edge edge){
		// draw line for single headed arrow head at target
		double theta = Math.atan2(edge.dy, edge.dx);
		//drawArrow(g2d, toX, toY, theta);
		edge.arrows.clear();
		edge.arrows.add( new Arrow((int)(edge.toX), (int)(edge.toY), theta));

	//	edge.arrows.add( new Arrow((int)(edge.toX-edge.dx*2), (int)(edge.toY-edge.dy*2), theta));
	}
	
	@Override
	public void updateLabel(Edge edge) {
		
					edge.lx = (int) Math.round(edge.fromX + (edge.toX - edge.fromX)
							* edge.edgeLabelRelativePosition);
					edge.ly = (int) Math.round(edge.fromY + (edge.toY - edge.fromY)
							* edge.edgeLabelRelativePosition);

	}

	@Override
	public void updatePath(Edge edge, GeometricObject r1, GeometricObject r2) {
		
		edge.scx = edge.source.getX() + edge.source.getWidth() / 2;
		edge.scy = edge.source.getY() + edge.source.getHeight() / 2;

		edge.tcx = edge.target.getX() + edge.target.getWidth() / 2;
		edge.tcy = edge.target.getY() + edge.target.getHeight() / 2;

		if (edge.getRenderingHintBidirectionalOffset() != 0) {
			final int z = 5;
			edge.scx += edge.getRenderingHintBidirectionalOffset() * z;
			edge.scy += edge.getRenderingHintBidirectionalOffset() * z;
			edge.tcx += edge.getRenderingHintBidirectionalOffset() * z;
			edge.tcy += edge.getRenderingHintBidirectionalOffset() * z;
		}

		edge.dx = edge.tcx - edge.scx;
		edge.dy = edge.tcy - edge.scy;
		
		double norm = Math.sqrt(edge.dx*edge.dx+edge.dy*edge.dy);
		edge.dx = edge.dx/norm;
		edge.dy = edge.dy/norm;
		
		// cut source shape with edge


		Line edgeline = new Line(edge.scx, edge.scy, edge.tcx, edge.tcy);
		List<Point> lst = edgeline.intersect(r1);

		if (lst.size() == 0)
			return;

		Point selectedPoint = lst.get(0);
		for (int i = 1; i < lst.size(); i++) {
			double d1 = (lst.get(i).x - edge.tcx) * (lst.get(i).x - edge.tcx)
					+ (lst.get(i).y - edge.tcy) * (lst.get(i).y - edge.tcy);
			double d2 = (selectedPoint.x - edge.tcx) * (selectedPoint.x - edge.tcx)
					+ (selectedPoint.y - edge.tcy) * (selectedPoint.y - edge.tcy);

			if (d1 < d2)
				selectedPoint = lst.get(i);
		}

		// cut target shape with edge
		lst = edgeline.intersect(r2);
		if (lst.size() == 0)
			return;

		Point selectedPoint2 = lst.get(0);
		for (int i = 1; i < lst.size(); i++) {
			double d1 = (lst.get(i).x - edge.scx) * (lst.get(i).x - edge.scx)
					+ (lst.get(i).y - edge.scy) * (lst.get(i).y - edge.scy);
			double d2 = (selectedPoint2.x - edge.scx) * (selectedPoint2.x - edge.scx)
					+ (selectedPoint2.y - edge.scy) * (selectedPoint2.y - edge.scy);

			if (d1 < d2)
				selectedPoint2 = lst.get(i);
		}

		edge.fromX = selectedPoint.x;
		edge.fromY = selectedPoint.y;

		final double EXTRA_DISTANCE_TO_NODE = 1;
		double deld = edge.target.getStrokeWidth()+EXTRA_DISTANCE_TO_NODE;	// 1 = extra distance
		double dx = selectedPoint2.x-selectedPoint.x;
		double dy = selectedPoint2.y-selectedPoint.y;
		double dd = Math.sqrt(dx*dx+dy*dy);
		dx = dx/dd; dy = dy/dd;
		
		edge.toX = selectedPoint2.x-(int)(dx*deld);
		edge.toY = selectedPoint2.y-(int)(dy*deld);

		
		edge.lines.add(new LineSegment(edge.fromX, edge.fromY, edge.toX, edge.toY));

		
		
		edge.edgePath = new Line2D.Double(edge.fromX, edge.fromY, edge.toX-(int)Math.round(dx*3), edge.toY-(int)Math.round(dy*3));
	}

}
