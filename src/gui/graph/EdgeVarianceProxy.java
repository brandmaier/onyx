package gui.graph;

import java.awt.geom.Arc2D;

import geometry.GeometricObject;
import geometry.LineSegment;

public class EdgeVarianceProxy extends EdgeProxy
{

	/*public EdgeVarianceProxy(Edge edge) {
		super(edge);

	}*/
	@Override
	public void updateArrow(Edge edge){
		edge.arrows.clear();
		
		final double turnAng = .3; //.4;
		
		double extrarad=.5;
		
		if (edge.arcPosition == Node.NORTH) {

			double theta = Math.PI / 2;
			double turn = -turnAng;
			edge.arrows.add( new Arrow(edge.scx + edge.rad+extrarad, edge.source.getY(), theta + turn));
			edge.arrows.add( new Arrow( edge.scx - edge.rad-extrarad, edge.source.getY(), theta - turn));
		} else if (edge.arcPosition == Node.EAST) {

				double theta = Math.PI ;		//NS
				double turn = turnAng;
				edge.arrows.add( new Arrow( edge.source.getX()+edge.source.getWidth(),
						edge.source.getY()+edge.rad/2+extrarad*4, theta + turn));
				edge.arrows.add( new Arrow( edge.source.getX()+edge.source.getWidth(), 
						edge.source.getY()+edge.source.getHeight()-edge.rad/2-extrarad*4, theta - turn));
				
		} else	if (edge.arcPosition == Node.WEST) {

					double theta = 2*Math.PI ;
					double turn = -turnAng;
					edge.arrows.add( new Arrow(edge.source.getX(), 
							edge.source.getY()						  +edge.rad/2+extrarad*4, theta + turn));
					edge.arrows.add( new Arrow( edge.source.getX(),
							edge.source.getY()+edge.source.getHeight()-edge.rad/2-extrarad*4, theta - turn));

		} else { //(edge.source.getRenderingHintArcPosition() == Node.SOUTH) {


			double theta = -Math.PI / 2;
			double turn = +turnAng;
			edge.arrows.add( new Arrow( edge.scx + edge.rad+extrarad,
					edge.source.getY() + edge.source.getHeight(), theta + turn));
			edge.arrows.add( new Arrow( edge.scx - edge.rad-extrarad,
					edge.source.getY() + edge.source.getHeight(), theta - turn));


		}
	}

	@Override
	public void updateLabel(Edge edge) {
		if (edge.arcPosition == Node.NORTH) {
			edge.lx = edge.scx;
			edge.ly = edge.source.getY() - edge.rad - 2 - 5;
		} else if (edge.arcPosition == Node.SOUTH) {
			
			edge.lx = edge.scx;
			edge.ly = edge.source.getY() + edge.source.getHeight() + edge.rad + 2 + 14+5;
		} else if (edge.arcPosition == Node.EAST) {
			// TODO NS
			edge.lx = edge.scx + (int)(edge.getLabel().getWidth()*.5) + 45;
			edge.ly = edge.scy;
			
			//edge.getLabel().setXAlign(0);
		} else {
			edge.lx = edge.scx - (int)(edge.getLabel().getWidth()*.5) - 45;
			edge.ly = edge.scy;
			
			//.getLabel().setXAlign(0);
		}
		
	}

	@Override
	public void updatePath(Edge edge, GeometricObject r1, GeometricObject r2) {

		edge.scx = edge.source.getX() + edge.source.getWidth() / 2;
		edge.scy = edge.source.getY() + edge.source.getHeight() / 2;
		
		double inset = 10;		//TODO: inset 10 is nicer but there is a potential bug with the PDF/PNG export library!
		
		if (edge.arcPosition == Node.NORTH) {
			edge.lines.add(new LineSegment(edge.scx - edge.rad, edge.source.getY() - edge.rad, edge.scx
					+ edge.rad, edge.source.getY() + edge.rad));
			
			edge.edgePath = new Arc2D.Double(edge.scx - edge.rad, edge.source.getY() - edge.rad,
					2 * edge.rad, 2 * edge.rad, 0+inset, 180-2*inset, Arc2D.OPEN);


		
		} else if (edge.arcPosition == Node.SOUTH) {
			edge.lines.add(new LineSegment(edge.scx - edge.rad, edge.source.getY()
					+ edge.source.getHeight() - edge.rad, edge.scx + edge.rad, edge.source.getY()
					+ edge.source.getHeight() + edge.rad));
			
			edge.edgePath = new Arc2D.Double(edge.scx - edge.rad, edge.source.getY()
					+ edge.source.getHeight() - edge.rad, 2 * edge.rad, 2 * edge.rad, -0-inset,
					-180.00+2*inset, Arc2D.OPEN);
			
			
		} else if (edge.arcPosition == Node.EAST) {
			// TODO: 
			edge.lines.add(new LineSegment(edge.scx + edge.rad, edge.source.getY()
					+ edge.source.getHeight() - edge.rad, edge.scx + edge.rad, edge.source.getY()
					+ edge.source.getHeight() + edge.rad));
			
			edge.edgePath = new Arc2D.Double(edge.source.getX()+edge.source.getWidth()-edge.rad, 
					edge.scy-edge.rad, 2 * edge.rad, 2 * edge.rad, 270+inset,180-2*inset, Arc2D.OPEN);
			
		} else if (edge.arcPosition == Node.WEST) {
			// TODO: 
			edge.lines.add(new LineSegment(edge.scx - edge.rad, edge.source.getY()
					+ edge.source.getHeight() - edge.rad, edge.scx + edge.rad, edge.source.getY()
					+ edge.source.getHeight() + edge.rad));
			
			edge.edgePath = new Arc2D.Double(edge.source.getX()-edge.rad  ,
					edge.scy-edge.rad, 2 * edge.rad, 2 * edge.rad, 90+inset,
					180-2*inset, Arc2D.OPEN);
		/*edge.lines.add(new LineSegment(edge.scx - edge.rad, edge.source.getY()
				+ edge.source.getHeight() - edge.rad, edge.scx + edge.rad, edge.source.getY()
				+ edge.source.getHeight() + edge.rad));
		
		edge.edgePath = new Arc2D.Double(edge.scx - edge.rad, edge.source.getY()
				+ edge.source.getHeight() - edge.rad, 2 * edge.rad, 2 * edge.rad, -0,
				-180.00, Arc2D.OPEN);		*/
		}


		
		
	}	


}
