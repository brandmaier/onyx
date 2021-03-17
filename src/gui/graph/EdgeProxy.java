package gui.graph;

import geometry.GeometricObject;

public abstract class EdgeProxy {

	
	public static int ARROW_PAD = 3;//3
	
	/*Edge edge;
	
	public EdgeProxy(Edge edge)
	{
		this.edge = edge;
	}*/
	
	public abstract void updateLabel(Edge edge);
	public abstract void updatePath(Edge edge, GeometricObject r1, GeometricObject r2);

	public abstract void updateArrow(Edge edge);
	//public abstract GeneralPath getPath();
}
