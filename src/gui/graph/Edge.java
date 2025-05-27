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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import geometry.GeometricObject;
import geometry.LineSegment;
import geometry.Oval;
import geometry.Rectangle;
import geometry.Triangle;
import gui.Utilities;
import gui.linker.LinkEvent;

/**
 * 
 * represents a path between two nodes
 * 
 * 
 * Coordinates of the center of the source object (scx,scy) and the target
 * object (tcx, tcy)
 * 
 * 
 * @author andreas
 * 
 */
public class Edge implements Cloneable, LineColorable, VariableContainerListener {

	GeometricObject r1, r2;

	private int Curvature = 50; // Andys initial suggestion was 80

	public Node source, target;

	private Color color;

	protected int arrowStyle = 0;

	double varianceAngle = 0;

	VariableContainer definitionVariableContainer;

	private boolean rough = false;

	public enum NamingConventions {
		TO, ARROW
	};

	public NamingConventions automaticVariableNamingConvention = NamingConventions.ARROW;

	public VariableContainer getDefinitionVariableContainer() {
		return definitionVariableContainer;
	}

	public int getArrowStyle() {
		return arrowStyle;
	}

	public void setArrowStyle(int arrowStyle) {
		this.arrowStyle = arrowStyle;
	}


	boolean valid = false;

	boolean selected = false;

	// determine path-type
	public Shape edgePath = null;
	public GeneralPath generalPath;

	public final static EdgeProxy edgeRegressionProxy = new EdgeRegression();
	public final static EdgeProxy edgeVarianceProxy = new EdgeVarianceProxy();

	public final static EdgeProxy edgeCovarianceProxy = new EdgeCovarianceProxy();

	public static final int DEFAULT_LABEL_FONTSIZE = 10;

	private static final int VARIANCE_CTRL_POINT = 2;

	private static final int COVARIANCE_CTRL_POINT_1 = 0;

	private static final int COVARIANCE_CTRL_POINT_2 = 1;

	private EdgeProxy proxy = edgeRegressionProxy;

	double value;

	public double arcPosition;
	public boolean arcPositionAutoLayout = true;

	int rad = 15; // radius for variance slings

	public static float DEFAULT_STROKEWIDTH = 3;
	private Stroke stroke = new BasicStroke(DEFAULT_STROKEWIDTH, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);

	float lineWidth = DEFAULT_STROKEWIDTH;

	int yOffsetLabel = -5;

	public Vector<LineSegment> lines = new Vector<LineSegment>();

	private PlainEdgeLabel edgeLabel;

	public boolean ctrlAutomatic = true;

	public EdgeStyle edgeStyle = EdgeStyle.NORMAL;
	public boolean edgeStyleHideUnitValues = true;

	private String parameterName;
	private boolean doubleHeaded = false;
	private boolean fixed;

	private float[] dash = null;

	// label coordinates
	public int lx, ly;

	Font font = new Font("Arial", Font.PLAIN, DEFAULT_LABEL_FONTSIZE);
	FontMetrics fm;

	public int scx, scy, tcx, tcy; // centers of sources and targets

	public int fromX, fromY, toX, toY; // connector points

	public double fromAngle = 0, toAngle = 0; // angles for arrows,

	public List<Arrow> arrows = new ArrayList<Arrow>();

	double dx, dy;

	// definitionColumn is used by engine , this should rather be solved
	// internally in engine TODO: Timo
	public int definitionColumn;

	// TODO new field for definition variable name
	public String definitionVariableName;

	boolean automaticNaming;
	String label;

	// Bezier control points
	public double ctrlx1, ctrly1, ctrlx2, ctrly2;
	public double relctrlx1, relctrly1, relctrlx2, relctrly2;
	int ctrls = 6;
	boolean ctrlActive = false;

	private int renderingHintBidirectionalOffset = 0;
	private String renderedParameterName;

	public double edgeLabelRelativePosition = .5; // center label on edge

	// TODO: only temporary: remove later
	public LineSegment edgeLinFrom;
	public LineSegment edgeLinTo;

	private boolean showStandardizedEstimate;

	public boolean isShowStandardizedEstimate() {
		return showStandardizedEstimate;
	}

	public void setShowStandardizedEstimate(boolean showStandardizedEstimate) {
		this.showStandardizedEstimate = showStandardizedEstimate;
	}

	private double standardizedValue;

	/*
	 * public double toDX;
	 * 
	 * public double toDY;
	 */

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public enum EdgeStyle {
		PLAIN, ALWAYS_LABEL, FULL, ALWAYS_VALUE, NORMAL, SIMPLIFIED
	}

	public boolean isAutomaticNaming() {
		return automaticNaming;
	}

	public void setAutomaticNaming(boolean automaticNaming) {
		this.automaticNaming = automaticNaming;

		/*
		 * if (automaticNaming) {
		 * this.edgeLabel.setContent(getAutomaticParameterName()); }
		 */
	}

	public Node getTarget() {
		return target;
	}

	public void setTarget(Node target) {
		this.target = target;
		update();
	}

	public Object clone() {
		Edge edge;
		try {
			edge = (Edge) super.clone();
			edge.edgeLabel = new LatexEdgeLabel(edgeLabel.content);
			edge.lines = new Vector<LineSegment>();
			edge.definitionVariableContainer = (VariableContainer) definitionVariableContainer.clone(edge);
			edge.definitionVariableContainer.addVariableContainerListener(edge);
			return (edge);
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return (null);
		}

	}

	public Node getSource() {
		return source;
	}

	public void setSource(Node source) {
		this.source = source;
		update();
	}

	public boolean isLoop() {
		return source == target;
	}

	public void invalidate() {
		this.valid = false;
		if (getSource() != null) {
			getSource().invalidate();
		}
		if (getTarget() != null) {
			getTarget().invalidate();
		}
	}

	public int getRenderingHintBidirectionalOffset() {
		return renderingHintBidirectionalOffset;
	}

	public void setRenderingHintBidirectionalOffset(int renderingHintBidirectionalOffset) {
		this.renderingHintBidirectionalOffset = renderingHintBidirectionalOffset;
	}

	public boolean isFixed() {
		return fixed;
	}

	public boolean isFree() {
		return !fixed;
	}

	public void setFixed(boolean fixed) {
		this.fixed = fixed;
	}

	public String getParameterName() {

		return parameterName;
	}

	public void setParameterNameByUser(String parameterName) {
		this.automaticNaming = false;
		this.parameterName = parameterName;

		this.renderedParameterName = renderText(parameterName);
	}

	public void setParameterName(String parameterName) {
		// this.automaticNaming = false;
		this.parameterName = parameterName;

		this.renderedParameterName = renderText(parameterName);
	}

	/**
	 * creates an automatically generated name for the edge that depends on the
	 * captions of the adjacent nodes.
	 * 
	 * @return
	 */
	public String getAutomaticParameterName() {

		if (source == null | target == null) {
			return "unnamed parameter";
		}

		char SPACE = '_';
		String parName = "";
		if (!isDoubleHeaded()) {

			if (automaticVariableNamingConvention == NamingConventions.ARROW)
				parName = source.getCaption() + "->" + target.getCaption();
			else
				parName = source.getCaption() + SPACE + "TO" + SPACE + target.getCaption();

		} else {
			// label = edge.source.getCaption()+"<->"+edge.target.getCaption();
			if (source == target) {
				parName = "VAR" + SPACE + source.getCaption();
			} else {
				parName = "COV" + SPACE + source.getCaption() + SPACE + target.getCaption();
			}
		}
		return parName;
	}

	public String getAutomaticLatexParameterName() {
		String parName = "";
		if (!isDoubleHeaded()) {
			// label = edge.source.getCaption()+"->"+edge.target.getCaption();
			parName = "\\lambda_{" + source.getCaption() + "," + target.getCaption() + "}";
		} else {
			// label = edge.source.getCaption()+"<->"+edge.target.getCaption();
			if (source == target) {
				parName = "VAR_{" + source.getCaption() + "}";
			} else {
				parName = "COV_{" + source.getCaption() + "," + target.getCaption() + "}";
			}
		}
		return parName;
	}

	// returns true if a double headed arrow, i.e., in the covariance matrix
	public boolean isDoubleHeaded() {
		return doubleHeaded;
	}

	public void setDoubleHeaded(boolean isDoubleHeaded) {
		this.doubleHeaded = isDoubleHeaded;

		update();
	}

	private void updateProxy() {
		if (source == target) {
			proxy = edgeVarianceProxy;
		} else {
			if (isDoubleHeaded()) {
				proxy = edgeCovarianceProxy;
			} else {
				proxy = edgeRegressionProxy;
			}
		}

	}

	// returns the value associated to this edge in the currently active
	// parameter set, or Model.MISSING if no
	// parameter set is active.
	public double getValue() {
		return value;
	}

	public double getStandardizedValue() {
		return standardizedValue;
	}

	public void setValue(double value) {
		this.value = value;
		this.standardizedValue = Double.NaN;

		// TvO 23.11.2023 removed this warning since setting value on free paths might
		// make sense at some points (e.g., scripts).
//		if (!this.isFixed()) {
//			System.err.println("Warning! Used Edge.setValue() on a non-fixed path.");
//		}
	}

	public void setValue(double value, double svalue) {
		this.value = value;
		this.standardizedValue = svalue;
	}

	public PlainEdgeLabel getLabel() {
		return edgeLabel;
	}

	public void setLabel(PlainEdgeLabel label) {
		edgeLabel = label;
	}

	public Edge() {
		fixed = true;
		value = 1.0;
		parameterName = "";
		renderedParameterName = "";

		color = Color.black;

		automaticNaming = true;

		// edgeLabel = new PlainEdgeLabel("");
		edgeLabel = new LatexEdgeLabel("");

		updateProxy();

		// add variable container listener to react to link events
		definitionVariableContainer = new VariableContainer(null, this);
		definitionVariableContainer.addVariableContainerListener(this);

	}

	public Edge(Node source, Node target) {

		this();

		this.source = source;
		this.target = target;

		/*
		 * scx = source.getX() + source.getWidth() / 2; scy = source.getY() +
		 * source.getHeight() / 2;
		 * 
		 * tcx = target.getX() + target.getWidth() / 2; tcy = target.getY() +
		 * target.getHeight() / 2;
		 */
		update();

	}

	public Edge(Node source, Node target, boolean isDoubleHeaded) {
		this(source, target);
		this.setDoubleHeaded(isDoubleHeaded);
	}

	public Edge(Node source, Node target, boolean isDoubleHeaded, double value) {
		this(source, target, isDoubleHeaded);
		this.value = value;
	}

	public Edge(Node source, Node target, boolean isDoubleHeaded, String parameterName, double value) {
		this(source, target, isDoubleHeaded, value);
		this.fixed = false;
		this.parameterName = parameterName;
		this.renderedParameterName = renderText(parameterName);
		this.automaticNaming = false;
	}

	/*
	 * private Point computeAnchor(Node source, Node target) { // intersect top line
	 * 
	 * scx = source.getX() + source.getWidth() / 2; scy = source.getY() +
	 * source.getHeight() / 2;
	 * 
	 * tcx = target.getX() + target.getWidth() / 2; tcy = target.getY() +
	 * target.getHeight() / 2;
	 * 
	 * Line top = new Line(new Point(source.getX(), source.getY()), new Point(
	 * source.getX() + source.getWidth(), source.getY())); Line edge = new Line(new
	 * Point(scx, scy), new Point(tcx, tcy)); Point isect = top.intersect(edge);
	 * 
	 * // intersect bottom line Line bottom = new Line(new Point(source.getX(),
	 * source.getY() + source.getHeight()), new Point(source.getX() +
	 * source.getWidth(), source.getY() + source.getHeight())); Point isect2 =
	 * bottom.intersect(edge);
	 * 
	 * // intersect left line Line left = new Line(new Point(source.getX(),
	 * source.getY()), new Point(source.getX(), source.getY() +
	 * source.getHeight())); Point isect3 = left.intersect(edge);
	 * 
	 * // intersect right line Line right = new Line(new Point(source.getX() +
	 * source.getWidth(), source.getY()), new Point(source.getX() +
	 * source.getWidth(), source.getY() + source.getHeight())); Point isect4 =
	 * right.intersect(edge);
	 * 
	 * Point selectedPoint = null; if ((isect != null) && (isect.x >= source.getX())
	 * && (isect.x <= source.getX() + source.getWidth())) { selectedPoint = isect;
	 * 
	 * } if ((isect2 != null) && (isect2.x >= source.getX()) && (isect2.x <
	 * source.getX() + source.getWidth())) { if (Math.abs(isect2.y - tcy) <
	 * Math.abs(isect.y - tcy)) { selectedPoint = isect2;
	 * 
	 * } }
	 * 
	 * if ((isect3 != null) && (isect3.y >= source.getY()) && (isect3.y <
	 * source.getY() + source.getHeight())) { selectedPoint = isect3;
	 * 
	 * 
	 * }
	 * 
	 * if ((isect4 != null) && (isect4.y >= source.getY()) && (isect4.y <
	 * source.getY() + source.getHeight())) { if (Math.abs(isect4.x - tcx) <
	 * Math.abs(isect3.x - tcx) || selectedPoint == null) { selectedPoint = isect4;
	 * 
	 * } }
	 * 
	 * return(selectedPoint);
	 * 
	 * }
	 */
	public void drawArrow(Graphics2D g2d, Arrow arrow) {
		this.drawArrow(g2d, arrow.x, arrow.y, arrow.theta);
	}

	public void drawArrow(Graphics2D g2d, double toX, double toY, double theta) {

		GeneralPath arrow = new GeneralPath();

		int barb = 10;

		if (arrowStyle == 2)
			barb = 12;

		// head at target
		double phi = Math.toRadians(30);
		double x1, y1, x2, y2, rho = theta + phi;

		x1 = toX - barb * Math.cos(rho);
		y1 = toY - barb * Math.sin(rho);
		// g2d.draw(new Line2D.Double(toX, toY, x, y));
		arrow.moveTo(x1, y1);
		arrow.lineTo(toX, toY);
		rho = theta - phi;
		x2 = toX - barb * Math.cos(rho);
		y2 = toY - barb * Math.sin(rho);
		// g2d.draw(new Line2D.Double(toX, toY, x, y));
		arrow.lineTo(x2, y2);

		if (arrowStyle == 0) {
			// g2d.setStroke(arrowStroke);
			g2d.draw(arrow);
		} else if (arrowStyle == 1) {
			arrow.lineTo(x1, y1); // draw line to close shape
			// arrow.transform( new Affine);
			// g2d.draw(arrow);
			g2d.fill(arrow);
		} else if (arrowStyle == 2) {
			double x3, y3, x4, y4;
			double phi2 = Math.toRadians(15);

			x3 = toX - 5 * Math.cos(theta + phi2);
			y3 = toY - 5 * Math.sin(theta + phi2);

			x4 = toX - 5 * Math.cos(theta - phi2);
			y4 = toY - 5 * Math.sin(theta - phi2);

			arrow.lineTo(x3, y3);
			arrow.lineTo(x4, y4);
			arrow.lineTo(x1, y1);
			g2d.fill(arrow);
		}

		/*
		 * Polygon poly = new Polygon(); poly.addPoint(toX, toY);
		 * 
		 * double phi = Math.toRadians(30); double x, y, rho = theta + phi; int barb =
		 * 15; x = toX - barb * Math.cos(rho); y = toY - barb * Math.sin(rho);
		 * poly.addPoint((int) Math.round(x), (int) Math.round(y)); // g2d.draw(new
		 * Line2D.Double(toX, toY, x, y)); rho = theta - phi; x = toX - barb *
		 * Math.cos(rho); y = toY - barb * Math.sin(rho); // g2d.draw(new
		 * Line2D.Double(toX, toY, x, y));
		 * 
		 * poly.addPoint((int) Math.round(x), (int) Math.round(y));
		 * 
		 * g2d.fill(poly);
		 */

		/*
		 * AffineTransform tx = new AffineTransform();
		 * 
		 * int size = 9;
		 * 
		 * Polygon arrowHead = new Polygon(); arrowHead.addPoint( 0,size);
		 * arrowHead.addPoint( -size, -size); arrowHead.addPoint( size,-size);
		 * 
		 * tx.setToIdentity(); double angle = Math.atan2(toY-fromY, toX-fromX);
		 * tx.translate(toX, toY); tx.rotate((angle-Math.PI/2d));
		 * 
		 * Shape arr = tx.createTransformedShape(arrowHead); g2d.fill(arr);
		 */

		// }

	}

	public void draw(Graphics g, Color backgroundColor) {
		renderConnection(g);
		renderLabel(g, backgroundColor);
	}

	public String getLabelinStyle() {
		String label = "";
		String valueString = Double.toString(Math.round(this.getValue() * 100) / 100.0);

		if (showStandardizedEstimate // && isFree()
		// && !Double.isNaN(standardizedValue)
		) {
			if (Double.isNaN(standardizedValue)) {
				valueString += "(NaN)";
			} else
				valueString += "(" + (Math.round(this.getStandardizedValue() * 100) / 100.0) + ")";
		}

		if (this.edgeStyle == EdgeStyle.ALWAYS_LABEL) {
			if (!this.fixed)
				label = this.getRenderedParameterName();
		} else if (this.edgeStyle == EdgeStyle.ALWAYS_VALUE) {
			if (!this.edgeStyleHideUnitValues || this.getValue() != 1)
				label = valueString;
			else
				label = "";
		} else if (this.edgeStyle == EdgeStyle.FULL) {
			label = this.getRenderedParameterName() + " = " + valueString;
		} else if (this.edgeStyle == EdgeStyle.NORMAL) {
			if (!this.edgeStyleHideUnitValues || this.getValue() != 1)
				label = "" + valueString;
			if (!this.isFixed()) {
				label = getRenderedParameterName() + " = " + valueString;
			}
		} else if (this.edgeStyle == EdgeStyle.PLAIN) {
			label = "";
		} else if (this.edgeStyle == EdgeStyle.SIMPLIFIED) {
			if (!this.edgeStyleHideUnitValues || this.getValue() != 1)
				label = "" + valueString;
			if (!this.isFixed()) {
				label = getRenderedParameterName();// + " = " + valueString;
			}
		}
		return label;
	}

	public String getLabelInLaTeXStyle() {
		String p = renderedParameterName;
		if (automaticNaming)
			renderedParameterName = getAutomaticLatexParameterName();
		else
			renderedParameterName = parameterName;
		String erg = getLabelinStyle();
		renderedParameterName = p;
		return erg;

	}

	public void renderLabel(Graphics g, Color backgroundColor) {
		Graphics2D g2d = (Graphics2D) g;
		// draw a label

		if (isDefinitionVariable()) {

			int anchorX = lx;
			int anchorY = ly;
			final int size = 8;
			/*
			 * int labelWidth = size * 2; int labelHeight = size * 2;
			 */
			g.setColor(Color.black);
			Polygon poly = new Polygon(new int[] { anchorX - size, anchorX, anchorX + size, anchorX },
					new int[] { anchorY, anchorY - size, anchorY, anchorY + size }, 4);

			if (getDefinitionVariableContainer().isConnected()) {
				g2d.fillPolygon(poly);
			} else {
				g2d.setColor(Color.white);
				g2d.fillPolygon(poly);
				g2d.setColor(Color.black);
				g2d.drawPolygon(poly);
			}

			if (isDefinitionVariable()) {
				edgeLabel.setContent(getParameterName());
				yOffsetLabel = size * 2 + 5;
				edgeLabel.draw(g2d, lx, ly + yOffsetLabel, backgroundColor);
			}

		} else {

			edgeLabel.draw(g2d, lx, ly + yOffsetLabel, backgroundColor);

		}

	}

	public void renderShadow(Graphics g) {

	}

	public void renderConnection(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;

		g.setColor(color);

		if (stroke != null)
			g2d.setStroke(stroke);

		if (fm == null) {
			fm = g.getFontMetrics();
		}

		update();

		// draw selection glow
		if (selected) {
			Utilities.paintGlow(g2d, edgePath, 8, ((BasicStroke) stroke).getLineWidth());
		}

		// clipping ?
		Shape oldClip = g2d.getClip();
		if (isDoubleHeaded() && source != target) {

			/*Area area;
			if (oldClip != null) {
				area = new Area(oldClip);
			} else {
				area = new Area(new Rectangle2D.Double(0, 
						0, 
						this.toX + this.fromX, 
						this.toY + this.fromY)); 
				    // TODO:
					// crude
					// bug
					// fix
					// BUG
			}
			area.subtract(new Area(r1.extrude(EdgeProxy.getShapePadding()).getShape()));
			area.subtract(new Area(r2.extrude(EdgeProxy.getShapePadding()).getShape()));
	*/

			Area area = new Area(new Rectangle2D.Double(0, 
					0, 
					this.toX + this.fromX, 
					this.toY + this.fromY)); 
			    // TODO:
				// crude
				// bug
				// fix
				// BUG
		
		area.subtract(new Area(r1.extrude(edgeCovarianceProxy.getShapePadding()).getShape()));
		area.subtract(new Area(r2.extrude(edgeCovarianceProxy.getShapePadding()).getShape()));
			
			
			
			g2d.setClip(area);
		}

		// draw path
		g2d.setColor(color);
		g2d.setStroke(stroke);

		if (!rough)
			g2d.draw(edgePath);
		else
			gui.fancy.Rough.draw(g2d, edgePath);

		//g2d.setClip(oldClip);
		g2d.setClip(null);

		// draw arrow heads
		for (Arrow arrow : arrows) {
			if (!Double.isNaN(arrow.theta))
				drawArrow(g2d, arrow);
		}

		// draw arrow orientation
		/*
		 * g2d.setColor(Color.pink); int sc=2; g2d.drawLine( this.edgeLinFrom.x1,
		 * this.edgeLinFrom.y1, this.edgeLinFrom.x2*sc, this.edgeLinFrom.y2*sc);
		 * g2d.drawLine( this.edgeLinTo.x1, this.edgeLinTo.y1, this.edgeLinTo.x2*sc,
		 * this.edgeLinTo.y2*sc);
		 */

		// draw Bezier control points
		if (ctrlActive) {
			g.setColor(Color.green);
			g.drawOval((int) ctrlx1 - ctrls, (int) ctrly1 - ctrls, ctrls * 2 + 1, ctrls * 2 + 1);
			g.drawLine(fromX, fromY, (int) ctrlx1, (int) ctrly1);

			// draw second control point only for edges that have different
			// source and target
			// otherwise one control point suffices
			if (getSource() != getTarget()) {
				g.drawOval((int) ctrlx2 - ctrls, (int) ctrly2 - ctrls, ctrls * 2 + 1, ctrls * 2 + 1);
				g.drawLine(toX, toY, (int) ctrlx2, (int) ctrly2);
			}

			g.setColor(Color.black);

		}

	}

	private String getRenderedParameterName() {
		return this.renderedParameterName;
	}

	public Stroke getStroke() {
		return stroke;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public EdgeStyle getEdgeStyle() {
		return edgeStyle;
	}

	public void setEdgeStyle(EdgeStyle edgeStyle) {
		this.edgeStyle = edgeStyle;
	}

	public void setRelativeLabelPosition(double r) {
		this.edgeLabelRelativePosition = r;
		update();
	}

	public void validate() {
		if (valid)
			return;

		if (getSource() != null)
			getSource().validate();
		if (getTarget() != null)
			getTarget().validate();
		update();
		valid = true;
	}

	public void update() {

		// target and source can be null if
		// an edge is created from a file
		// or a script. In that case, we just
		// skip the update and have to make
		// sure that it is run later.
		if (source == null || target == null)
			return;

		// label content updates
		if (isDefinitionVariable()) {

		} else {
			String label = getLabelinStyle();
			edgeLabel.setContent(label);
		}

		/*
		 * determine geometric objects of nodes TODO: this should not be updated
		 * everytime!
		 */

		if (source.isMeanTriangle()) {
			r1 = new Triangle(source.getX(), source.getY(), source.getWidth(), source.getHeight());

		} else if (!source.isLatent()) {
			r1 = new Rectangle(source.getX(), source.getY(), source.getWidth(), source.getHeight());
		} else {
			r1 = new Oval(source.getX(), source.getY(), source.getWidth(), source.getHeight());
		}

		if (!target.isLatent()) {
			r2 = new Rectangle(target.getX(), target.getY(), target.getWidth(), target.getHeight());
		} else {
			r2 = new Oval(target.getX(), target.getY(), target.getWidth(), target.getHeight());
		}

		// drawing updates
		lines.clear();

		updateProxy(); // TODO: not necessary here ?!

		proxy.updatePath(this, r1, r2);
		proxy.updateLabel(this);
		proxy.updateArrow(this);

	}

	public static String renderText(String text) {

		// if (!text.startsWith("$")) return(text);

		text = text.replaceAll("\\\\alpha", "\u03B1");
		text = text.replaceAll("\\\\beta", "\u03B2");
		text = text.replaceAll("\\\\gamma", "\u03B3");
		text = text.replaceAll("\\\\delta", "\u03B4");
		text = text.replaceAll("\\\\epsilon", "\u03B5");
		text = text.replaceAll("\\\\zeta", "\u03B6");
		text = text.replaceAll("\\\\eta", "\u03B7");
		text = text.replaceAll("\\\\theta", "\u03B8");
		text = text.replaceAll("\\\\iota", "\u03B9");
		text = text.replaceAll("\\\\kappa", "\u03BA");
		text = text.replaceAll("\\\\lambda", "\u03BB");
		text = text.replaceAll("\\\\mu", "\u03BC");
		text = text.replaceAll("\\\\nu", "\u03BD");
		text = text.replaceAll("\\\\xi", "\u03BE");
		text = text.replaceAll("\\\\omicron", "\u03BF");
		text = text.replaceAll("\\\\pi", "\u03C0");
		text = text.replaceAll("\\\\rho", "\u03C1");
		text = text.replaceAll("\\\\sigma", "\u03C3");
		text = text.replaceAll("\\\\tau", "\u03C4");
		text = text.replaceAll("\\\\upsilon", "\u03C5");
		text = text.replaceAll("\\\\chi", "\u03C7");
		text = text.replaceAll("\\\\phi", "\u03C6");
		text = text.replaceAll("\\\\psi", "\u03C8");
		text = text.replaceAll("\\\\omega", "\u03C9");

		// upper case
		text = text.replaceAll("\\\\Alpha", "\u0391");
		text = text.replaceAll("\\\\Beta", "\u0392");
		text = text.replaceAll("\\\\Gamma", "\u0393");
		text = text.replaceAll("\\\\Delta", "\u0394");
		text = text.replaceAll("\\\\Theta", "\u0398");
		text = text.replaceAll("\\\\Lambda", "\u039B");
		text = text.replaceAll("\\\\Mu", "\u039C");
		text = text.replaceAll("\\\\Nu", "\u039D");
		text = text.replaceAll("\\\\Xi", "\u039E");
		text = text.replaceAll("\\\\Omicron", "\u039F");
		text = text.replaceAll("\\\\Pi", "\u03A0");
		text = text.replaceAll("\\\\Rho", "\u03A1");
		text = text.replaceAll("\\\\Sigma", "\u03A3");
		text = text.replaceAll("\\\\Tau", "\u03A4");
		text = text.replaceAll("\\\\Upsilon", "\u03A5");
		text = text.replaceAll("\\\\Phi", "\u03A6");
		text = text.replaceAll("\\\\Xi", "\u03A7");
		text = text.replaceAll("\\\\Psi", "\u03A8");
		text = text.replaceAll("\\\\Omega", "\u03A9");

		// others

		// text = text.replaceAll("\\^2$", "\u00B2");

		// eastereggs

		text = text.replaceAll("\\\\blacksun", "\u2600");
		text = text.replaceAll("\\\\cloud", "\u2601");
		text = text.replaceAll("\\\\umbrella", "\u2602");
		text = text.replaceAll("\\\\snowman", "\u2603");
		text = text.replaceAll("\\\\blackstar", "\u2605");
		text = text.replaceAll("\\\\star", "\u2606");
		text = text.replaceAll("\\\\poison", "\u2620");
		text = text.replaceAll("\\\\smile", "\u263A");
		text = text.replaceAll("\\\\scissors", "\u2702");
		text = text.replaceAll("\\\\checkmark", "\u2713");
		text = text.replaceAll("\\\\circleone", "\u2780");

		// text = text.replaceAll("\\\\apple", "\uF8FF");

		return (text);
	}

	public int getCtrlPoint(int x, int y) {

		if (!ctrlActive)
			return -1;

		/*
		 * if (getSource()==getTarget()) { if ((x - ctrlx1) * (x - ctrlx1) + (y -
		 * ctrly1) * (y - ctrly1) < ctrls ctrls) { return VARIANCE_CTRL_POINT; } else {
		 * return -1; } }
		 */

		if ((x - ctrlx1) * (x - ctrlx1) + (y - ctrly1) * (y - ctrly1) < ctrls * ctrls) {
			return COVARIANCE_CTRL_POINT_1;
		} else if ((x - ctrlx2) * (x - ctrlx2) + (y - ctrly2) * (y - ctrly2) < ctrls * ctrls) {
			return COVARIANCE_CTRL_POINT_2;
		}

		return -1;
	}

	public void setCtrlPoint(int point, double x, double y) {

		if (point == COVARIANCE_CTRL_POINT_1) {
			relctrlx1 = x - scx;
			relctrly1 = y - scy;
		} else if (point == COVARIANCE_CTRL_POINT_2) {
			relctrlx2 = x - tcx;
			relctrly2 = y - tcy;
		} else if (point == VARIANCE_CTRL_POINT) {
			relctrlx1 = x - scx;
			relctrly1 = y - tcx;
		}

	}

	public void setRelativeCtrlPoint(int point, double x, double y) {

		if (point == COVARIANCE_CTRL_POINT_1) {
			relctrlx1 = x;
			relctrly1 = y;
		} else if (point == COVARIANCE_CTRL_POINT_2) {
			relctrlx2 = x;
			relctrly2 = y;
		} else if (point == VARIANCE_CTRL_POINT) {
			relctrlx1 = x;
			relctrly1 = y;
		}

	}

	public String toString() {
		return "<EDGE from " + source.getId() + " to " + target.getId() + " fixed:" + fixed + ">";
	}

	/**
	 * distance point from line using Hesse Normal Form
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public double distanceFromPoint(int x, int y) {

		if (!isDoubleHeaded()) {
			// HNF
			double dx = scx - tcx;
			double dy = scy - tcy;

			double a = dy;
			double b = -dx;

			double c = -(a * scx + b * scy);

			double dist = (a * x + b * y + c) / (Math.sqrt(a * a + b * b));

			return dist;

		} else {

			if (source == target) {

				if (arcPosition == Node.NORTH) {
					double d = Math.sqrt((source.getY() - y) * (source.getY() - y) + (scx - x) * (scx - x));
					return Math.abs(d - rad);
				} else if (arcPosition == Node.SOUTH) {
					double d = Math
							.sqrt((source.getY() + source.getHeight() - y) * (source.getY() + source.getHeight() - y)
									+ (scx - x) * (scx - x));
					return Math.abs(d - rad);

				} else if (arcPosition == Node.WEST) {

					double d = Math.sqrt((scy - y) * (scy - y)

							+ (source.getX() - x) * (source.getX() - x));
					return Math.abs(d - rad);

				} else if (arcPosition == Node.EAST) {

					double d = Math.sqrt((scy - y) * (scy - y)

							+ (source.getX() + source.getWidth() - x) * (source.getX() + source.getWidth() - x));
					return Math.abs(d - rad);

				} else {
					// TODO NF
					System.err.println("TODO Edge.java 1090");
					return Double.MAX_VALUE;
				}

			} else {

				/*
				 * if (parabola==null) return Double.MAX_VALUE;
				 * 
				 * double py = parabola.evaluate(x); return Math.abs(py-y);
				 */
				double best = Double.MAX_VALUE;
				for (LineSegment line : lines) {
					double dist = line.distance(x, y);
					if (!Double.isNaN(dist)) {
						best = Math.min(Math.abs(best), dist);
					}
				}
				// System.out.println("Best"+ best);
				return best;

			}

		}
	}

	// returns true if edge weight is a definition variable.
	public boolean isDefinitionVariable() {
		return definitionVariableContainer.isActive();
	}

	public boolean isOnLabel(int x, int y) {

		int lwh = edgeLabel.getWidth() / 2;
		return ((x >= lx - lwh) && (x <= lx + lwh) && (y >= ly - edgeLabel.getHeight() + yOffsetLabel)
				&& (y <= ly + yOffsetLabel));

	}

	public Color getLineColor() {
		return color;
	}

	public void setLineColor(Color color) {
		this.color = color;
	}

	public float[] getDashStyle() {
		return (dash);
	}

	public void setDashStyle(float[] dash) {

		this.dash = dash;
		if (!(this.stroke instanceof BasicStroke)) {
			System.err.println("Stroke is not a BasicStroke!");
			return;
		}

		BasicStroke stroke = (BasicStroke) this.stroke;

		BasicStroke nstroke = new BasicStroke(stroke.getLineWidth(), stroke.getEndCap(), stroke.getLineJoin(),
				stroke.getMiterLimit(), dash, stroke.getDashPhase());
		this.stroke = nstroke;
	}

	public Object getQP() {
		// TODO Auto-generated method stub
		return null;
	}



	public boolean getActiveControl() {
		return ctrlActive;
	}

	public void setActiveControl(boolean b) {
		this.ctrlActive = b;
	}

	public float getStrokeWidth() {
		return ((BasicStroke) stroke).getLineWidth();
	}

	public boolean isWithinRectangle(java.awt.Rectangle selection) {
		if (lines == null)
			return false;
		for (LineSegment line : lines) {
			if (selection.intersectsLine(line.x1, line.y1, line.x2, line.y2))
				return true;
			// TODO: add contains line?!
		}
		return false;
	}

	public java.awt.Rectangle getLabelRectangle() {

		int lwh = edgeLabel.getWidth() / 2;

		return new java.awt.Rectangle(lx - lwh, ly - edgeLabel.getHeight() + yOffsetLabel, edgeLabel.getWidth(),
				edgeLabel.getHeight());
	}

	public int getCurvature() {
		return Curvature;
	}

	public void setCurvature(int d) {
		this.Curvature = d;
	}

	public boolean isVarianceEdge() {
		return isDoubleHeaded() && getSource() == getTarget();
	}

	@Override
	public void notifyUnlink(LinkEvent event) {

	}

	@Override
	public void notifyLink(LinkEvent event) {

	}

	public void setEdgeStyle(EdgeStyle style, boolean hideUnitValues) {
		this.setEdgeStyle(style);
		this.edgeStyleHideUnitValues = hideUnitValues;

	}

	public void setLineWidth(float lw) {
		this.lineWidth = lw;
		stroke = new BasicStroke(lw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, BasicStroke.JOIN_MITER,
				getDashStyle(), 0);

	}

}
