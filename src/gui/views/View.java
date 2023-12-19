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
package gui.views;

import gui.Desktop;
import gui.Utilities;
import gui.actions.ModelViewPasteAction;
import gui.frames.MainFrame;
import gui.graph.Movable;
import gui.undo.MovedStep;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

public class View extends JPanel implements MouseListener, MouseMotionListener, KeyListener, Movable, FocusListener {
	protected static final int ICON_SIZE_X = 120;
	protected static final int ICON_SIZE_Y = 80;

	protected int mouseClickX, mouseClickY;

	boolean selected = false;
	boolean wasDragged = false;

	boolean hasTopLeftResizer = false;

	public Rectangle getSelection() {
		return selection;
	}

	private int resizeButtonSize = 20;

	private boolean resizeAction;
	protected boolean selectAction;
	private boolean moveAction;

	protected boolean movable = true;
	protected boolean resizable = true;
	protected boolean collides = true;
	protected boolean selectable = true;
	protected boolean minimizable = true;

	public boolean hideBorderDecorators = false;

	public boolean isSelectable() {
		return selectable;
	}

	public void setSelectable(boolean selectable) {
		this.selectable = selectable;
	}

	static final protected Cursor handCursor = new Cursor(Cursor.HAND_CURSOR);
	static final protected Cursor resizeSECursor = new Cursor(Cursor.SE_RESIZE_CURSOR);
	static final protected Cursor resizeECursor = new Cursor(Cursor.E_RESIZE_CURSOR);
	static final protected Cursor resizeNCursor = new Cursor(Cursor.N_RESIZE_CURSOR);
	static final protected Cursor resizeNECursor = new Cursor(Cursor.NE_RESIZE_CURSOR);
	static final protected Cursor resizeWCursor = new Cursor(Cursor.W_RESIZE_CURSOR);
	static final protected Cursor resizeSCursor = new Cursor(Cursor.S_RESIZE_CURSOR);
	static final protected Cursor resizeNWCursor = new Cursor(Cursor.NW_RESIZE_CURSOR);
	static final protected Cursor resizeSWCursor = new Cursor(Cursor.SW_RESIZE_CURSOR);

	static protected Cursor defaultCursor = new Cursor(Cursor.DEFAULT_CURSOR);
	// protected Cursor noCursor = new Cursor(Cursor.)

	public int uniconifiedWidth = -1;
	public int uniconifiedHeight = -1;

	private boolean iconified = false;

	public static final int sizeMoveArea = 15;

	// private Point selectionStart;

	protected void setMinimizable(boolean b) {
		this.minimizable = b;

	}

	public void setIconified(boolean iconify) {
		if (!minimizable)
			return;
		// System.out.println("Iconify"+iconify);

		if (iconify == true) {
			this.uniconifiedWidth = this.getWidth();
			this.uniconifiedHeight = this.getHeight();
			this.setSize(ICON_SIZE_X, ICON_SIZE_Y);
			this.iconified = true;
		} else {
			this.iconified = false;
			this.setSize(this.uniconifiedWidth, this.uniconifiedHeight);
		}

		this.notifyViewIconified(iconify);

		redraw();
	}

	public boolean isMovable() {
		return movable;
	}

	public void setMovable(boolean movable) {
		this.movable = movable;
	}

	public boolean isResizable() {
		return resizable;
	}

	public void setResizable(boolean resizable) {
		this.resizable = resizable;
	}

	public void dispose() {
		viewListeners.clear();

		this.removeKeyListener(this);
		this.removeMouseListener(this);
		this.removeMouseMotionListener(this);

	}

	List<ViewListener> viewListeners;

	// these variables are needed for the collision handling
	protected double velocityX, velocityY;
	protected int desiredX = Integer.MIN_VALUE, desiredY = Integer.MIN_VALUE;

	public int getDesiredX() {
		return desiredX;
	}

	public void setDesiredX(int desiredX) {
		this.desiredX = desiredX;
	}

	public int getDesiredY() {
		return desiredY;
	}

	public void setDesiredY(int desiredY) {
		this.desiredY = desiredY;
	}

	protected boolean collisionFlag;
	// --

	protected Desktop desktop;

	private Point selectionEndpoint;

	private Rectangle selection;
	private int resizeActionType;
	protected int viewPortOffsety;
	protected int viewPortOffsetx;


	public int minimal_width = 200;
	public int minimal_height = 200;
	public int maximal_height = 600;

	/**
	 * 
	 */
	private static final long serialVersionUID = -541937045564913739L;

	public View() {
		// this.setBackground(Color.blue);
		this.setSize(200, 200);

		// kill layout manager
		this.setLayout(null);

		// set focusable such that it can request focus for keyboard events
		this.setFocusable(true);

		// initialize view listeners
		viewListeners = new ArrayList<ViewListener>();

		// add event listeners
		this.addMouseMotionListener(this);
		this.addMouseListener(this);
		// this.addContainerListener(this);
		


		// ----

		this.setOpaque(true);

	}

	public View(Desktop desktop) {
		this();
		this.desktop = desktop;
	}

	public Desktop getDesktop() {
		return this.desktop;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

		// double-click enlarges an iconified view
		if (isIconified()) {
			if (arg0.getClickCount() == 2) {
				setIconified(false);
				arg0.consume();
				return;
			}
		// iconify, if double-click on frame
		} else { 
			if (arg0.getClickCount() == 2) {
				if ((arg0.getX() < sizeMoveArea) || (arg0.getX() > getWidth() - sizeMoveArea)
						|| (arg0.getY() < sizeMoveArea) || (arg0.getY() > getHeight() - sizeMoveArea)) {
					this.setIconified(true);
				}
			}

		}

		// don't allow clicks on the resize button
		if ((arg0.getX() >= getWidth() - resizeButtonSize) & (arg0.getY() >= getHeight() - resizeButtonSize)) {
			arg0.consume();
			return;
		}

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// request focus for keyboard events

		// this.requestFocus();

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {


		// this.requestFocus();

		// mousePressed = true;
		mouseClickX = arg0.getX();
		mouseClickY = arg0.getY();

		if (desktop != null) {
			desktop.setActiveView(this);

			// if (desktop.layoutManager != null)
			// desktop.layoutManager.dragStarted();
		}

		if (isIconified()) {
			if (Utilities.isRightMouseButton(arg0)) {
				desktop.dragSource = this;
				// System.out.println("DRAG SOURCE "+this);
			}
		}

		if (arg0.getX() > getWidth() - resizeButtonSize && arg0.getY() > getHeight() - resizeButtonSize
				&& !isIconified()) {
			resizeAction = true;
			resizeActionType = 0;

		} else if (arg0.getX() < resizeButtonSize && arg0.getY() < resizeButtonSize && !isIconified()) {
			resizeAction = true;
			resizeActionType = 1;

		} else {

			if ((arg0.getX() < sizeMoveArea) || (arg0.getX() > getWidth() - sizeMoveArea)
					|| (arg0.getY() < sizeMoveArea) || (arg0.getY() > getHeight() - sizeMoveArea)) {
				moveAction = true;
			} else if (!isIconified()) {

				if ((Utilities.isLeftMouseButton(arg0)) && (isSelectable())) {
					selectionEndpoint = new Point(arg0.getX(), arg0.getY());
					selectAction = true;

					// caught a node?
					// TODO
					// selectAction = false;
				}

			}
		}

	}

	public void paintBackground(Graphics2D g) {
		// NOTHING TO DO
	}

	/**
	 * 
	 * 
	 * 
	 */
	@Override
	public void paintComponent(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		super.paintComponent(g);

		this.paintBackground(g2d);

		// draw hatches for resizable handle in bottom right corner
		if (!hideBorderDecorators && !isIconified() && isResizable()) {

			int pad = 3;
			int step = 5;

			g2d.setColor(Color.black);

			g2d.drawLine(getWidth() - pad - resizeButtonSize, getHeight() - pad, getWidth() - pad,
					getHeight() - pad - resizeButtonSize);
			g2d.drawLine(getWidth() - pad - resizeButtonSize + step, getHeight() - pad, getWidth() - pad,
					getHeight() - pad + step - resizeButtonSize);
			g2d.drawLine(getWidth() - pad - resizeButtonSize + 2 * step, getHeight() - pad, getWidth() - pad,
					getHeight() - pad + 2 * step - resizeButtonSize);
		}

		if (!hideBorderDecorators && selectAction) {

			int x, y, w, h;
			if (selectionEndpoint.x > mouseClickX) {
				x = mouseClickX;
				w = selectionEndpoint.x - mouseClickX;
			} else {
				x = selectionEndpoint.x;
				w = mouseClickX - selectionEndpoint.x;
			}

			if (selectionEndpoint.y > mouseClickY) {
				y = mouseClickY;
				h = selectionEndpoint.y - mouseClickY;
			} else {
				y = selectionEndpoint.y;
				h = mouseClickY - selectionEndpoint.y;
			}

			g2d.setColor(Color.black);
			selection = new Rectangle(x, y, w, h);

			g2d.draw(selection);

		}

	}

	public void redraw() {
		this.invalidate();
		this.repaint();
	}

	public void viewPortChanged(ViewPortChangedEvent event) {

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

		moveAction = false;
		resizeAction = false;
		selectAction = false;

		wasDragged = false;
	}

	public boolean isIconified() {
		return iconified;
	}

	public boolean isCollidable() {
		return collides;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {

		// resize the panel
		if (!isIconified() && (resizeAction) && (isResizable())) {

			if (resizeActionType == 0) { // resize bottom-right corner

				int w = arg0.getX();
				int h = arg0.getY();

				w = Math.max(w, minimal_width);
				h = Math.max(h, minimal_height);

				notifyViewResized();

				setSize(w, h);
				arg0.consume();
			} else if (hasTopLeftResizer) { // resize top-left corner

				// TODO unfinished

				int x = arg0.getX();
				int y = arg0.getY();
				System.out.println("XY: " + x + "," + y);
				Point location = this.getLocation();
				setLocation(location.x + x - mouseClickX, location.y + y - mouseClickY);
				int diffx = x - mouseClickX;
				int diffy = y - mouseClickY;
				setSize(this.getSize().width - diffx, this.getSize().height - diffy);

				// shift all nodes ?!
				// for (Node node : )

				System.out.println("Viewport offset = " + viewPortOffsetx + "," + viewPortOffsety);

				// viewPortChanged( new ViewPortChangedEvent(
				// (viewPortOffsetx-diffx), (viewPortOffsety-diffy) ));
				viewPortChanged(new ViewPortChangedEvent(-diffx, -diffy));

				viewPortOffsetx = viewPortOffsetx - diffx;
				viewPortOffsety = viewPortOffsety - diffy;

				// notifyViewResized();

				arg0.consume();

				// return;
			}
			// return;
		}
		// move the panel

		if ((isMovable()) && (moveAction) && (Utilities.isLeftMouseButton(arg0))) {

			if (!wasDragged)
				MainFrame.undoStack.add(new MovedStep(this, this));

			Point location = this.getLocation();
			int newX = Math.max(0, location.x + arg0.getX() - mouseClickX);
			int newY = Math.max(0, location.y + arg0.getY() - mouseClickY);

			this.setLocation(newX, newY);

			// resize the workspace and zoom to current view
			if (this.desktop != null)
				this.desktop.scrollRectToVisible(new Rectangle(this.getX(), this.getY(), 50, 20));

		}

		if ((selectAction) && (Utilities.isLeftMouseButton(arg0))) {

			selectionEndpoint = new Point(arg0.getX(), arg0.getY());

		}

		this.wasDragged = true;
		this.redraw();

	}

	/*
	 * public static final int VIEWPORT = 0, // take the policy of the viewport
	 * UNCHANGED = 1, // don't scroll if it fills the visible area, otherwise
	 * take the policy of the viewport FIRST = 2, // scroll the first part of
	 * the region into view CENTER = 3, // center the region LAST = 4; // scroll
	 * the last part of the region into view public static void
	 * scroll(JComponent c, Rectangle r, int horizontalBias, int verticalBias) {
	 * Rectangle visible = c.getVisibleRect(), dest = new Rectangle(r);
	 * 
	 * if (dest.width > visible.width) { if (horizontalBias == VIEWPORT) { //
	 * leave as is } else if (horizontalBias == UNCHANGED) { if (dest.x <=
	 * visible.x && dest.x + dest.width >= visible.x + visible.width) {
	 * dest.width = visible.width; } } else { if (horizontalBias == CENTER)
	 * dest.x += (dest.width - visible.width) / 2; else if (horizontalBias ==
	 * LAST) dest.x += dest.width - visible.width;
	 * 
	 * dest.width = visible.width; } }
	 * 
	 * if (dest.height > visible.height) { // same code as above in the other
	 * direction }
	 * 
	 * if (!visible.contains(dest)) c.scrollRectToVisible(dest); }
	 */

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// change cursors
		if ((resizable)
				&& (arg0.getX() > getWidth() - resizeButtonSize && arg0.getY() > getHeight() - resizeButtonSize)) {

			setCursor(resizeSECursor);

		} else if (hasTopLeftResizer && resizable && (arg0.getX() < resizeButtonSize)
				&& (arg0.getY() < resizeButtonSize)) {

			setCursor(resizeNWCursor);

		} else if (movable && ((arg0.getX() < sizeMoveArea) || (arg0.getX() > getWidth() - sizeMoveArea)
				|| (arg0.getY() < sizeMoveArea) || (arg0.getY() > getHeight() - sizeMoveArea))) {

			setCursor(handCursor);

		} else {

			setCursor(defaultCursor);
		}

	}

	public void notifyViewResized() {
		Iterator<ViewListener> iterVl = viewListeners.iterator();
		while (iterVl.hasNext()) {
			iterVl.next().viewResized(this);
		}
	}

	public void notifyViewMoved() {
		Iterator<ViewListener> iterVl = viewListeners.iterator();
		while (iterVl.hasNext()) {
			iterVl.next().viewMoved(this);
		}
	}

	public void notifyViewIconified(boolean state) {
		Iterator<ViewListener> iterVl = viewListeners.iterator();
		while (iterVl.hasNext()) {
			iterVl.next().viewIconified(this, state);
		}
	}

	public void addViewListener(ViewListener viewListener) {
		viewListeners.add(viewListener);

	}

	/*
	 * public void clearViewListeners() { viewListeners.clear(); }
	 */

	public boolean collidesWith(View v2) {

		if ((!this.collides) || (!v2.collides))
			return false;

		int x1 = this.getLocation().x;
		int x2 = v2.getLocation().x;
		int y1 = this.getLocation().y;
		int y2 = v2.getLocation().y;
		int w1 = this.getSize().width;
		int h1 = this.getSize().height;
		int w2 = v2.getSize().width;
		int h2 = v2.getSize().height;

		if ((x1 + w1) < x2) {
			return false;
		} // view is left of v2
		if ((x1 > x2 + w2)) {
			return false;
		} // view is right of v2
		if (y1 + h1 < y2) {
			return false;
		}
		if (y1 > y2 + h2) {
			return false;
		}

		return (true);
	}

	public boolean isWithin(int x, int y) {
		return (x > this.getX() && x < this.getX() + this.getWidth() && (y > this.getY())
				&& (y < this.getY() + this.getHeight()));
	}

	public void resetVelocity() {
		this.velocityX = 0;
		this.velocityY = 0;
	}

	public void move() {
		this.setLocation(Math.max(0, (int) Math.round(getX() + velocityX)),
				Math.max(0, (int) Math.round(getY() + velocityY)));

	}

	public void setLocation(int x, int y) {
		super.setLocation(x, y);
		this.notifyViewMoved();
	}

	public void setLocation(Point p) {
		super.setLocation(p);
		this.notifyViewMoved();
	}

	public void addVelocity(double d, double e) {
		this.velocityX += d;
		this.velocityY += d;

	}

	public geometry.Rectangle getRectangle() {
		if (isIconified())
			return new geometry.Rectangle(this.getX(), this.getY(), ICON_SIZE_X, ICON_SIZE_Y);
		else
			return new geometry.Rectangle(this.getX(), this.getY(), this.getWidth(), this.getHeight());
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setX(int x) {
		this.setLocation(x, this.getY());

	}

	@Override
	public void setY(int y) {
		this.setLocation(getX(), y);

	}

	public double getVelocityX() {
		return velocityX;
	}

	public double getVelocityY() {
		return velocityY;
	}

	public Point getCenter() {
		return new Point(getX() + getWidth() / 2, getY() + getHeight() / 2);
	}

	@Override
	public void focusGained(FocusEvent e) {
		
		/*if (MainFrame.touchBarHandler!=null)
			MainFrame.touchBarHandler.setActiveView(this);
		*/
	}

	@Override
	public void focusLost(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}

}
