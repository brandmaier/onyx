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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.HashSet;

import engine.ModelRequestInterface;
import engine.RawDataset;
import engine.Statik;
import geometry.Oval;
import gui.Utilities;
import gui.fancy.PaintTricks;
import gui.fancy.Rough;
import gui.linker.LinkEvent;
import gui.linker.LinkException;

public class Node implements Cloneable, FillColorable, LineColorable, Movable, Resizable, VariableContainerListener {

	public static final int MIN_NODE_HEIGHT = 30;
	public static final int MIN_NODE_WIDTH = 30;
	public static final int DEFAULT_LABEL_FONTSIZE = 10;

	// properties for drawing
	public int x = -1;
	private int y = -1;
	private int width = -1, height = -1;
	private String caption;
	private String shortenedCaption;
	private Shape shape;
	private Color lineColor, fillColor;

	public double pseudoR2 = -1;

	public Image image;

	private boolean captionValid = false;

	// hash counter uniquely identifies a node
	// across all ModelViews
	private static int HASH_ID_COUNTER = 0;
	private int hashCounter;

	private boolean shadow = false;
	private int shadow_type = 1;
	// private boolean gradient = true;

	private boolean hidden = false;

	public int clickX, clickY; // last click of a user within node
	public int oldHeight, oldWidth, oldX, oldY; // last height/width before
												// resize

	private boolean latent = true;

	private boolean triangle = false;

	private boolean multiplication = false;

	private boolean isNormalized = false;

	// Depth of the node in the graph object. Needs to be set using the
	// computeDirectedDephts in the Graph class.
	public int depth = -1;

	// only meaningful if latent = false and triangle = false.
	// indicates whether there is a grouping variable associated with this
	// observed variable, and in which column the grouping is.
	public boolean groupingVariable = false;
	public double groupValue;
	public String groupName;
	final int groupingSymbolSize = 12;

	VariableContainer groupingVariableContainer;
	
	private NodeDrawProxy drawProxy = null;

	public VariableContainer getGroupingVariableContainer() {
		return groupingVariableContainer;
	}

	public VariableContainer getObservedVariableContainer() {
		return observedVariableContainer;
	}

	VariableContainer observedVariableContainer;

	Font font = new Font("Arial", Font.PLAIN, DEFAULT_LABEL_FONTSIZE);
	FontMetrics fm;

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	public boolean isShadow() {
		return shadow;
	}

	public void setShadow(boolean shadow) {
		this.shadow = shadow;
	}

	// Used for mean triangles only, stores all outgoing edges to remove
	// selectively.
	public HashSet<Edge> meanEdges = new HashSet<Edge>();

	/**
	 * must be increasing, corresponds to column in matrix representation, mean
	 * triangles have increasing IDs higher than matrix size.
	 */
	private int id;

	Stroke stroke = new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);;;

	boolean connected;

	boolean selected;

	public enum AnchorType {
		N, NE, E, SE, S, SW, W, NW
	};

	int anchorRadius = 4; // 2

	Rectangle[] anchors;
	public int renderingHintArcPosition; // 0 is up, 90 is right, 180 is down
	public static final int NORTH = 0;
	public static final int EAST = 90;
	public static final int SOUTH = 180;
	public static final int WEST = 270;

	private Oval oval;
	private boolean missingData;

	private int tarjanIndex;
	private int tarjanLowLink;

	public FillStyle nodeFillGradient = FillStyle.GRADIENT;

	public FillStyle getFillStyle() {
		return nodeFillGradient;
	}

	public void setFillStyle(FillStyle nodeFillGradient) {
		this.nodeFillGradient = nodeFillGradient;
	}

	private Color fontColor = Color.black;
	public int labelPosY;
	public int labelPosX;
	private boolean valid;
	public double dragRatio;

	private boolean rough = false;
	private int rough_seed = (int) (Math.random() * 100000); // no proper randomization necessary
	// private float roughness = 1;
	private Paint myPaint;
	private int multiplicatonNodeStyle = 2;

	public int getHashCounter() {
		return hashCounter;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public Object clone() {
		Node node;
		try {
			node = (Node) super.clone();
			node.setNextHashCounterID(); // TODO: really necessary?
			node.connected = false; // TvO: Added to indicate that a copied node is not selected.
			node.groupingVariableContainer = (VariableContainer) groupingVariableContainer.clone(node);
			node.groupingVariableContainer.addVariableContainerListener(node);
			node.observedVariableContainer = (VariableContainer) observedVariableContainer.clone(node);
			node.observedVariableContainer.addVariableContainerListener(node);
			return (node);
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return (null);
		}

	}

	public boolean isNormalized() {
		return isNormalized;
	}

	public void setNormalized(boolean normalized) {
		isNormalized = normalized;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public int getX() {
		return x;
	}

	public int getXCenter() {
		return x + width / 2;
	}

	public void setXCenter(int x) {
		setX(x - width / 2);
	}

	public int getYCenter() {
		return y + height / 2;
	}

	public void setYCenter(int y) {
		setY(y - height / 2);
	}

	public Stroke getStroke() {
		return stroke;
	}

	public void setStroke(Stroke stroke) {
		this.stroke = stroke;
	}

	public void setX(int x) {
		this.x = x;
		updateAnchors();
		valid = false;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
		updateAnchors();
		valid = false;
	}

	public void setId(int id) {
		// if (this.id == -1)
		this.id = id;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		width = Math.max(MIN_NODE_WIDTH, width);
		this.width = width;
		updateAnchors();
		captionValid = false;
		valid = false;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		height = Math.max(MIN_NODE_HEIGHT, height);
		this.height = height;
		captionValid = false;
		updateAnchors();
		valid = false;
	}

	public String toString() {
		return "<Node with ID " + getId() + " and caption: " + getCaption() + " and hash ID:" + hashCounter + ">";
	}

	public String getCaption() {
		if (isMeanTriangle())
			return "const";
		return caption;
	}

	public void setCaption(String caption) {
		if (isMeanTriangle())
			throw new RuntimeException("Attempt to set caption of mean triangle");

		this.caption = Edge.renderText(caption);
		captionValid = false;
		valid = false;
	}

	public String getUniqueName(boolean useUniqueName) {
		if (!useUniqueName || !isObserved() || observedVariableContainer == null
				|| observedVariableContainer.getUniqueName() == null)
			return caption;
		else
			return observedVariableContainer.getUniqueName();
	}

	public void updateShape() {
		if (this.isMeanTriangle()) {

			Polygon p = new Polygon(new int[] { this.x, this.x + this.width / 2, this.x + this.width },
					new int[] { this.y + this.height, this.y, this.y + this.height }, 3);
			this.shape = p;

		} else {

			if (this.isLatent()) {

				// g2d.drawOval(this.x, this.y, this.width, this.height);

				shape = new Ellipse2D.Float(this.x, this.y, this.width, this.height);
			} else {

				shape = new Rectangle(this.x, this.y, this.width, this.height);

			}

		}
	}

	/**
	 * generates a shortened caption that actually fits into the shape. This has to
	 * be called, whenever the caption is changed.
	 */
	public void updateCaption() {
		if (fm != null) {
			String proposal = caption;
			// while (sw)
			int pad = 8;

			int sw = fm.stringWidth(caption) + pad;
			int i = 1;
			while (sw > this.getWidth()) {
				int m = caption.length() / 2;

				proposal = caption.substring(0, m - i) + ".." + caption.substring(m + i, caption.length());
				sw = fm.stringWidth(proposal) + pad;

				i += 1;
				if (m - i < 0)
					break;
			}
			this.shortenedCaption = proposal;
		} else {
			this.shortenedCaption = caption;
		}

	}

	public int getId() {
		return id;
	}

	public boolean isLatent() {
		return latent;
	}

	public boolean isMeanTriangle() {
		return triangle;
	}

	public void setIsLatent(boolean isLatent) {
		this.latent = isLatent;

		// this.anchors =

		updateAnchors();
		valid = false;
	}

	/**
	 * Bounding box of a node is its size if it is deselected, otherwise it includes
	 * the area covered by node handles The bounding box is given in coordinates of
	 * the parent View.
	 * 
	 * @return
	 */
	public Rectangle getBoundingBoxOnParent() {
		if (isSelected()) {
			return new Rectangle(getX(), getY(), getX() + getWidth(), getY() + getHeight());
		} else {
			return new Rectangle(getX() - anchorRadius, getY() - anchorRadius, getX() + getWidth() + 2 * anchorRadius,
					getY() + getHeight() + 2 * anchorRadius);
		}
	}

	public void validate() {
		if (valid)
			return;

		updateShape();
		updateAnchors();
		updateCaption();

		valid = true;
	}

	public void updateAnchors() {

		int anchorWidth = anchorRadius * 2 + 1;

		oval = new Oval(x, y, width, height);

		if (this.isMeanTriangle()) {

			anchors = new Rectangle[8];

			// N
			anchors[0] = new Rectangle(this.x + this.width / 2 - anchorRadius, this.y - anchorRadius, anchorWidth,
					anchorWidth);

			anchors[1] = new Rectangle(-1, -1, -1, -1);

			// E
			anchors[2] = new Rectangle(this.x + this.width - anchorRadius, this.y + this.height - anchorRadius,
					anchorWidth, anchorWidth);

			anchors[3] = new Rectangle(-1, -1, -1, -1);

			// S
			anchors[4] = new Rectangle(this.x + this.width / 2 - anchorRadius, this.y + this.height - anchorRadius,
					anchorWidth, anchorWidth);

			anchors[5] = new Rectangle(-1, -1, -1, -1);

			// W
			anchors[6] = new Rectangle(this.x - anchorRadius, this.y + this.height - anchorRadius, anchorWidth,
					anchorWidth);

			anchors[7] = new Rectangle(-1, -1, -1, -1);

		} else

		if (this.isLatent()) {
			anchors = new Rectangle[8];

			// N
			anchors[0] = new Rectangle(this.x + this.width / 2 - anchorRadius, this.y - anchorRadius, anchorWidth,
					anchorWidth);

			// NE
			int x = (int) (this.x + this.width * 0.8);
			int y = this.oval.evaluate(x)[0];
			anchors[1] = new Rectangle(x - anchorRadius, y - anchorRadius, anchorWidth, anchorWidth);

			// E
			anchors[2] = new Rectangle(this.x + this.width - anchorRadius, this.y + this.height / 2 - anchorRadius,
					anchorWidth, anchorWidth);

			// SE
			x = (int) (this.x + this.width * 0.8);
			y = this.oval.evaluate(x)[1];
			anchors[3] = new Rectangle(x - anchorRadius, y - anchorRadius, anchorWidth, anchorWidth);

			// S
			anchors[4] = new Rectangle(this.x + this.width / 2 - anchorRadius, this.y + this.height - anchorRadius,
					anchorWidth, anchorWidth);

			// SW
			x = (int) (this.x + this.width * 0.2);
			y = this.oval.evaluate(x)[1];
			anchors[5] = new Rectangle(x - anchorRadius, y - anchorRadius, anchorWidth, anchorWidth);

			// W
			anchors[6] = new Rectangle(this.x - anchorRadius, this.y + this.height / 2 - anchorRadius, anchorWidth,
					anchorWidth);

			// NW
			x = (int) (this.x + this.width * 0.2);
			y = this.oval.evaluate(x)[0];
			anchors[7] = new Rectangle(x - anchorRadius, y - anchorRadius, anchorWidth, anchorWidth);

		} else {
			anchors = new Rectangle[8];

			// N
			anchors[0] = new Rectangle(this.x + this.width / 2 - anchorRadius, this.y - anchorRadius, anchorWidth,
					anchorWidth);

			// NE
			anchors[1] = new Rectangle(this.x + this.width - anchorRadius, this.y - anchorRadius, anchorWidth,
					anchorWidth);

			// E
			anchors[2] = new Rectangle(this.x + this.width - anchorRadius, this.y + this.height / 2 - anchorRadius,
					anchorWidth, anchorWidth);

			// SE
			if (!isGrouping())
				anchors[3] = new Rectangle(this.x + this.width - anchorRadius, this.y + this.height - anchorRadius,
						anchorWidth, anchorWidth);
			else
				anchors[3] = new Rectangle(-1, -1, -1, -1);

			// S
			anchors[4] = new Rectangle(this.x + this.width / 2 - anchorRadius, this.y + this.height - anchorRadius,
					anchorWidth, anchorWidth);

			// SW
			anchors[5] = new Rectangle(this.x - anchorRadius, this.y + this.height - anchorRadius, anchorWidth,
					anchorWidth);

			// W
			anchors[6] = new Rectangle(this.x - anchorRadius, this.y + this.height / 2 - anchorRadius, anchorWidth,
					anchorWidth);

			// NW
			anchors[7] = new Rectangle(this.x - anchorRadius, this.y - anchorRadius, anchorWidth, anchorWidth);

		}
	}

	public Node() {

		this.x = 0;
		this.y = 0;
		this.width = gui.Constants.DEFAULT_NODE_SIZE; // 50
		this.height = gui.Constants.DEFAULT_NODE_SIZE; // 30
		this.setCaption("?");
		// this.caption = "x" + nodeCounter;
		// nodeCounter += 1;
		this.id = -1;
		setIsLatent(true);
		this.selected = false;
		// set drawing style

		this.lineColor = Color.black;
		this.fillColor = Color.white;

		setNextHashCounterID();
		updateAnchors();

		// add variable container listener to react to link events
		observedVariableContainer = new VariableContainer(null, this);
		groupingVariableContainer = new VariableContainer(null, this);

		observedVariableContainer.addVariableContainerListener(this);
		groupingVariableContainer.addVariableContainerListener(this);
	}

	private void setNextHashCounterID() {

		this.hashCounter = HASH_ID_COUNTER;
		HASH_ID_COUNTER += 1;
	}

	public Node(int id) {
		this();
		this.id = id;
	}

	public Node(String caption) {
		this();
		this.setCaption(caption);
	}

	public Node(String caption, boolean latent) {
		this(caption);

		this.setIsLatent(latent);
	}

	public Node(int id, int x, int y) {
		this(id);
		this.x = x;
		this.y = y;
		updateAnchors();
	}

	public Node(String caption, int id, int x, int y) {
		this(id, x, y);
		this.setCaption(caption);

	}

	public Node(String caption, int id, int x, int y, boolean latent) {
		this(caption, id, x, y);
		this.latent = latent;
		updateAnchors();
	}

	public int getAnchorAtPoint(int x, int y) {
		for (int i = 0; i < anchors.length; i++) {
			if (anchors[i].contains(x, y))
				return i;
		}
		return -1;
	}

	public void draw(Graphics2D g, boolean markUnconnectedNodes) {
		
		if (!captionValid) {
			updateCaption();
			captionValid = true;
		}
		
		if (getDrawProxy()!=null) {
			getDrawProxy().draw(this, g, markUnconnectedNodes);
			return;
		}
		



		if (fm == null) {
			fm = g.getFontMetrics(font);
		}

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		Color borderColor;
		if ((markUnconnectedNodes) && (!isConnected() && !isLatent())) {
			borderColor = Color.gray;
		} else {
			borderColor = lineColor;
		}

		Color clrHi = fillColor; // new Color(255, 255, 255);

		final int darker = 30;

	/*	int red = Math.max(fillColor.getRed() - darker, 0);
		int blue = Math.max(fillColor.getBlue() - darker, 0);
		int green = Math.max(fillColor.getGreen() - darker, 0);
*/
		// determine shape
		if (this.isMeanTriangle()) {
			shape = new Polygon(new int[] { this.x, this.x + this.width / 2, this.x + this.width },
					new int[] { this.y + this.height, this.y, this.y + this.height }, 3);
		} else {

			if (this.isLatent()) {
				shape = new Ellipse2D.Double(this.x, this.y, this.width, this.height);
			} else {

				shape = new Rectangle(this.x, this.y, this.width, this.height);

			}
		}

		Stroke oldStroke = g2d.getStroke();

		// selection glow
		if (selected) {
			Utilities.paintGlow(g2d, shape, 8, ((BasicStroke) stroke).getLineWidth());
		}

		if (stroke != null) {

			g2d.setStroke(this.stroke);
		}

		if (this.isMeanTriangle()) {

			// g2d.setColor(borderColor);

			// other than the rectangle/circle gradient!

			if (nodeFillGradient == FillStyle.GRADIENT) {

				Rectangle fillbounds = new Rectangle(x, y, width, height);
				PaintTricks.shadedFill(g2d, fillbounds, shape, clrHi, 30);

			} else if (nodeFillGradient == FillStyle.FILL || nodeFillGradient == FillStyle.R2) {
				g2d.setColor(fillColor);
				g2d.fill(shape);
			} else {
				// g2d.setColor(Color.WHITE);
			}

			g.setColor(borderColor);
			if (this.rough) {
				Rough.draw(g2d, shape);
			} else {
				g2d.draw(shape);
			}

			g.setColor(fontColor);
			g.drawString("1", this.x + this.width / 2 - 3, this.y + this.height * 2 / 3);

		} else {

			if (this.isLatent()) {

				if (nodeFillGradient == FillStyle.NONE) {

				} else if (nodeFillGradient == FillStyle.GRADIENT) {

					Rectangle fillbounds = new Rectangle(x, y, width, height);
					PaintTricks.shadedFill(g2d, fillbounds, shape, clrHi, 30);

				} else if (nodeFillGradient == FillStyle.HAND) {

					// if (myPaint == null)
					Rectangle fillbounds = new Rectangle(x, y, width, height);
					myPaint = Rough.createHatchedPaint(fillColor, width, fillbounds, rough_seed);
					Paint oldPaint = g2d.getPaint();
					g2d.setPaint(myPaint);
					g2d.fill(shape);
					g2d.setPaint(oldPaint);

				} else {
					g2d.setColor(fillColor);
					g2d.fill(shape);
				}

				g2d.setColor(borderColor);
				// draw shape
				if (rough) {
					Rough.draw(g2d, shape, rough_seed);
				} else {
					g2d.draw(shape);
				}

			} else { // observed variable

				if (nodeFillGradient == FillStyle.NONE) {
					// g2d.setColor(Color.white);
				} else if (nodeFillGradient == FillStyle.GRADIENT) {
					// g2d.setPaint(new GradientPaint(x, y, clrHi, x + width, y
					// + height, clrLo));
					Rectangle fillbounds = (Rectangle) shape;
					PaintTricks.shadedFill(g2d, fillbounds, shape, clrHi, 30);
				} else if (nodeFillGradient == FillStyle.R2) {

					double percentage = Math.max(Math.min(1.0, pseudoR2), 0);
					System.out.println("Percentage of R2: " + percentage);

					Rectangle fillbounds = new Rectangle(x, (int) (y + (height - height * percentage)), width,
							(int) (height * percentage));
					g2d.setColor(fillColor);
					g2d.fill(fillbounds);

				} else if (nodeFillGradient == FillStyle.HAND) {

					// if (myPaint == null)
					Rectangle fillbounds = new Rectangle(x, (int) (y + (height)), width, (int) (height));
					myPaint = Rough.createHatchedPaint(fillColor, width, fillbounds, rough_seed);
					Paint oldPaint = g2d.getPaint();
					g2d.setPaint(myPaint);
					g2d.fill(shape);
					g2d.setPaint(oldPaint);

				} else {
					g2d.setColor(fillColor);
					g2d.fill(shape);
				}

				// draw shape
				g2d.setColor(borderColor);
				if (rough) {
					Rough.draw(g2d, shape, rough_seed);
				} else {
					g2d.draw(shape);
				}

				if (isNormalized) {
					final int dist = 5;
					Shape shape2 = new Rectangle(this.x + dist, this.y + dist, this.width - 2 * dist,
							this.height - 2 * dist);
					g2d.draw(shape2);
				}

				if (groupingVariable) {

					final int lx = this.x + this.width, ly = this.y + this.height;

					g.setColor(Color.black);
					Polygon poly = new Polygon(new int[] { lx - groupingSymbolSize, lx, lx + groupingSymbolSize, lx },
							new int[] { ly, ly - groupingSymbolSize, ly, ly + groupingSymbolSize }, 4);

					if (isGroupingVariableConnected())
						g.setColor(Color.black);
					else
						g.setColor(Color.white);
					g2d.fillPolygon(poly);
					g.setColor(Color.black);
					g2d.drawPolygon(poly);

					if (isGroupingVariableConnected())
						g.setColor(Color.white);

					String caption = Statik.doubleNStellen(groupValue, 0);
					g2d.drawString(caption, lx - fm.stringWidth(caption) / 2, ly + font.getSize() / 2);
					g.setColor(Color.black);

				}

				/*
				 * g2d.setColor(Color.white); g2d.fillRect(this.x, this.y, this.width,
				 * this.height); g2d.setColor(borderColor); g2d.drawRect(this.x, this.y,
				 * this.width, this.height);
				 */
				if (missingData)
					g2d.drawOval(this.x, this.y, this.width, this.height);

				/* -- TODO: move this */
				/*
				 * int size = 16; if (!isLatent() && !isConnected()) { if (imageMissing == null)
				 * { URL url = this.getClass().getResource("/icons/mono/stop32.png"); if (url !=
				 * null) { imageMissing = MessageObject.resizeImage(new
				 * ImageIcon(url).getImage(),size,size ); } }
				 * 
				 * int space = 2; g2d.drawImage(imageMissing, this.x+this.width-size+space,
				 * this.y+this.height-size+space,size,size, null); //g2d.drawIm }
				 */
			}

		}

		if (isMultiplicationNode()) {

			float scl = 1;
			if (multiplicatonNodeStyle == 1)
				scl = .75f;
			if (multiplicatonNodeStyle == 2)
				scl = .5f;

			int x1 = (int) Math.round(this.x + this.width / 2 - scl * Math.sqrt(0.5) * (this.width / 2));
			int x2 = (int) Math.round(this.x + this.width / 2 + scl * Math.sqrt(0.5) * (this.width / 2));
			int y1 = (int) Math.round(this.y + this.height / 2 - scl * Math.sqrt(0.5) * (this.height / 2));
			int y2 = (int) Math.round(this.y + this.height / 2 + scl * Math.sqrt(0.5) * (this.height / 2));

			g2d.drawLine(x1, y1, x2, y2);
			g2d.drawLine(x2, y1, x1, y2);

			if (multiplicatonNodeStyle >= 2) {
				scl = .75f;
				x1 = (int) Math.round(this.getXCenter() - scl * Math.sqrt(0.5) * (this.width / 2));
				x2 = (int) Math.round(this.getXCenter() + scl * Math.sqrt(0.5) * (this.width / 2));
				y1 = (int) Math.round(this.y + this.height / 2);
				y2 = (int) Math.round(this.y + this.height / 2);

				g2d.drawLine(x1, y1, x2, y2);
				// g2d.drawLine(x2, y1, x1, y2);

				x1 = (int) Math.round(this.getXCenter());
				x2 = (int) Math.round(this.getXCenter());
				y1 = (int) Math.round(this.getYCenter() - scl * Math.sqrt(0.5) * (this.height / 2));
				y2 = (int) Math.round(this.getYCenter() + scl * Math.sqrt(0.5) * (this.height / 2));
				g2d.drawLine(x1, y1, x2, y2);
			}

			// } else {
			/*
			 * Font f = g2d.getFont(); g2d.setFont(g2d.getFont().deriveFont(24.0f));
			 * g2d.drawString("*", this.getXCenter()-4, this.getYCenter()+13);
			 * g2d.setFont(f);
			 */

			// }
		}

		// if there is an image, paint over everything else (so far)

		if (image != null) {
			int space = 2;
			int size = this.width;
			g2d.setClip(shape);
			// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			// RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			// g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			// RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
			g2d.drawImage(image, this.x + this.width - size + space, this.y + this.height - size + space, size, size,
					null);
			g2d.setClip(null);
		}

		if (stroke != null)
			g2d.setStroke(oldStroke);

		// draw caption
		labelPosX = (this.x + this.width / 2) - fm.stringWidth(this.shortenedCaption) / 2;
		labelPosY = (this.y + height / 2 + font.getSize() / 2);

		if (!isMeanTriangle() && !isMultiplicationNode()) {
			if (fm != null) {
				g2d.setColor(fontColor);
				g2d.setFont(font);
				g2d.drawString(this.shortenedCaption, labelPosX, labelPosY);
			} else {
				g2d.drawString("ERROR", this.x, this.y + height / 2);
			}
		}

		// if selected, draw anchors
		if (this.isSelected()) {
			for (int i = 0; i < anchors.length; i++) {
				g2d.fill(anchors[i]);
			}
		}

	}

	public boolean isGroupingVariableConnected() {
		return (!latent && !triangle && groupingVariable && groupingVariableContainer.isConnected());
	}

	public boolean isPointWithin(int xp, int yp) {
		return (xp >= x) & (xp < x + width) & (yp >= y) & (yp < y + height);
	}

	public boolean isPointOnGroupingVariable(int xp, int yp) {
		return (!latent && !triangle && groupingVariable && xp >= x + width - groupingSymbolSize
				&& xp <= x + width + groupingSymbolSize && yp >= y + height - groupingSymbolSize
				&& yp <= y + height + groupingSymbolSize);
	}

	public void alignToGrid(int gridSize) {
		int toX = (int) Math.round(this.x / (float) gridSize) * gridSize;
		int toY = (int) Math.round(this.y / (float) gridSize) * gridSize;

		int toW = (int) Math.round(this.width / (float) gridSize) * gridSize;
		int toH = (int) Math.round(this.height / (float) gridSize) * gridSize; // int
																				// w
																				// =

		setX(toX);
		setY(toY);

		setWidth(toW);
		setHeight(toH);
	}

	public boolean isWithinRectangle(Rectangle selection) {
		return ((this.x >= selection.x) && (this.x + this.width < selection.x + selection.width)
				&& (this.y >= selection.y) && (this.y + this.height < selection.y + selection.height));

	}

	/*
	 * public boolean isRenderingHintArcPositionUp() { return
	 * renderingHintArcPositionUp; }
	 * 
	 * public void setRenderingHintArcPositionUp(boolean renderingHintArcPositionUp)
	 * { this.renderingHintArcPositionUp = renderingHintArcPositionUp; valid =
	 * false; }
	 */
	public int getRenderingHintArcPosition() {
		return renderingHintArcPosition;
	}

	public void setRenderingHintArcPosition(int renderingHintArcPosition) {
		this.renderingHintArcPosition = renderingHintArcPosition;
		valid = false;
	}

	public void setTriangle(boolean b) {
		this.triangle = b;

		if (b) {
			this.caption = "";
		}

		updateAnchors();
		valid = false;

	}

	// added by TvO 09.10.2018
	public void setAsMultiplication(boolean value) {
		multiplication = value;
	}

	public Color getLineColor() {
		return lineColor;
	}

	public void setLineColor(Color color) {
		if (color == null)
			return;
		this.lineColor = color;
	}

	public Color getFillColor() {
		return fillColor;
	}

	public void setFillColor(Color color) {
		if (color == null)
			return;
		this.fillColor = color;
	}

	public void setMissing(boolean hasColumnMissing) {
		this.missingData = hasColumnMissing;
	}

	public void setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		updateAnchors();
		valid = false;
	}

	public void renderShadow(Graphics2D g) {

		if (!this.shadow)
			return;

		updateShape();

		if (getShadow_type() == 1) {
		
		AffineTransform at = new AffineTransform();
		at.translate(2, 2);
		Shape tShape = at.createTransformedShape(shape);
		/*
		 * g.draw(tShape);
		 */

		g.setColor(new Color(100, 100, 100));
		g.fill(tShape);

		at.translate(1, 1);
		tShape = at.createTransformedShape(shape);
		g.setColor(new Color(200, 200, 200));
		g.fill(tShape);
		
		} else if (getShadow_type() == 2) {
			AffineTransform at = new AffineTransform();
			at.translate(6, 6);
			Shape tShape = at.createTransformedShape(shape);	
			g.setColor(Color.black);
			g.fill(tShape);
		}
	}

	public void addMeanEdge(Edge edge) {
		meanEdges.add(edge);
	}

	public void removeMeanEdge(Edge edge) {
		meanEdges.remove(edge);
	}

	public Edge[] getMeanEdges() {
		return meanEdges.toArray(new Edge[] {});
	}

	public boolean hasMeanEdge() {
		return meanEdges.size() > 0;
	}

	public int getTarjanIndex() {
		return tarjanIndex;
	}

	public void setTarjanIndex(int tarjanIndex) {
		this.tarjanIndex = tarjanIndex;
	}

	public int getTarjanLowLink() {
		return tarjanLowLink;
	}

	public void setTarjanLowLink(int tarjanLowLink) {
		this.tarjanLowLink = tarjanLowLink;
	}

	public boolean isObserved() {
		return !isLatent() && !isMeanTriangle();
	}

	public boolean isGrouping() {
		return (!latent && !triangle && groupingVariable);
	}

	public boolean isManifest() {
		return (!latent && !triangle);
	}

	public void unlinkGrouping() {
		setGroupingVariable(null, -1);
	}

	public void setGroupingVariable(RawDataset dataset, int index) {

		if (dataset == null)
			index = -1;
//		this.groupingColumn = index;
//		this.groupingDataset = dataset;
		this.groupingVariable = true;
		if (index != -1)
			this.groupName = dataset.getColumnName(index);
		else
			this.groupName = null;
	}

	public void removeGroupingVariable() {
		this.unlinkGrouping();
		this.groupingVariable = false;
	}

	public void toggleGrouping() {
		if (!isManifest())
			return;
		setGrouping(!isGrouping());
	}

	public void setGrouping(boolean state) {
		if (!state) {
			removeGroupingVariable();
		} else {
			setGroupingVariable(null, -1);
		}
	}

	public void setStrokeWidth(float newValue) {
		stroke = new BasicStroke(newValue, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL);
	}

	public float getStrokeWidth() {
		return ((BasicStroke) stroke).getLineWidth();
	}

	public void setFontSize(int newValue) {
		this.font = new Font("Arial", Font.PLAIN, newValue);
		fm = null;
		valid = false;
	}

	public int getFontSize() {
		return this.font.getSize();
	}

	public void setFontColor(Color c4) {
		this.fontColor = c4;
	}

	public Font getFont() {
		return this.font;
	}

	public void invalidate() {
		valid = false;
	}

	@Override
	public void notifyUnlink(LinkEvent event) {
		if (event.getEventSource() == observedVariableContainer) {
			this.setConnected(false);
			this.setMissing(false);

		}
		if (event.getEventSource() == groupingVariableContainer) {
			this.unlinkGrouping();
		}
	}

	@Override
	public void notifyLink(LinkEvent event) throws LinkException {

		// TvO 15 MAR 14: restricted this reaction to cases where the event was started
		// for the observed variable, not the grouping.
		if (event.getEventSource() == this.observedVariableContainer) {

			ModelRequestInterface mri = event.getEventSource().getGraph().getParentView().getModelRequestInterface();
			mri.requestChangeNodeCaption(this, event.getEventName());

			LinkException ln = new LinkException("Node is not manifest!");

			if (this.isLatent() || this.isMeanTriangle())
				throw ln;
			// System.err.println("Node is not manifest!"); //TODO should this be an
			// exception again?
			else
				this.setConnected(true);
		}
	}

	public void setRough(boolean b) {
		this.rough = b;
	}

	public Edge getVarianceComponent(Graph graph) {
		for (Edge edge : graph.getEdges()) {
			if (edge.getSource() == edge.getTarget() && edge.getTarget() == this) {
				return (edge);

			}
			// if ((edge.getTarget()==this) && !edge.isDoubleHeaded() &&
			// !edge.getSource().isMeanTriangle()) {
			// hasIncomingVariance = true;
			// }
		}
		return (null);
	}

	public boolean isMultiplicationNode() {
		return multiplication;
	}

	public boolean isRough() {
		return (rough);
	}

	public Color getFontColor() {
		return this.fontColor;
	}

	public NodeDrawProxy getDrawProxy() {
		return drawProxy;
	}

	public void setDrawProxy(NodeDrawProxy drawProxy) {
		this.drawProxy = drawProxy;
	}

	public int getShadow_type() {
		return shadow_type;
	}

	public void setShadow_type(int shadow_type) {
		this.shadow_type = shadow_type;
	}

}
