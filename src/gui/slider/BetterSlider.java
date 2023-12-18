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
package gui.slider;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;

/**
 * @author Nannette Liske
 *
 */
public abstract class BetterSlider extends JComponent implements DraggableSlider {

	private static final long serialVersionUID = 1L;

	private static final int BUTTON_WIDTH = 12;
	private static final int BUTTON_HEIGHT = 18;
	private static final String BUTTON_IMAGE = "images/sliderbtn.gif"; //$NON-NLS-1$

	private static final int MARGIN_LEFT = 12;
	private static final int MARGIN_RIGHT = 12;

	private static final int TRACK_HEIGHT = 3;

	private static final int RIGHT_STEP_DISTANCE = 10;
	private static final int LEFT_STEP_DISTANCE = -10;

	private double leftBorderMinValue;
	private double leftBorderMaxValue;
	private static final double LEFT_BORDER_RELATIVE_DISTANCE = 0.1;

	private double rightBorderMinValue;
	private double rightBorderMaxValue;
	private static final double RIGHT_BORDER_RELATIVE_DISTANCE = 0.1;

	private final JButton button;
	
	private boolean isInteger;

	private final Collection<SliderListener> listeners = new HashSet<SliderListener>(1);

	private double currentValue;
	private double rightBorderValue;
	private double leftBorderValue;

	private static final int FONT_SIZE = 10;
	private static final Font LABEL_FONT = new Font("Verdana", Font.PLAIN, FONT_SIZE); //$NON-NLS-1$

	private static final float DRAG_FACTOR = 5.0f;

	private static final int RULER_HEIGHT = 8;

	private static final Color RULER_BIG_COLOR = Color.BLACK;
	private static final Stroke RULER_BIG_STROKE = new BasicStroke(2f);
	private static final int RULER_BIG_LENGTH = RULER_HEIGHT;

	private static final Color RULER_NORMAL_FIVE_COLOR = Color.BLACK;
	private static final Stroke RULER_NORMAL_FIVE_STROKE = new BasicStroke(1.5f);
	private static final int RULER_NORMAL_FIVE_LENGTH = RULER_HEIGHT - 1;

	private static final Color RULER_NORMAL_COLOR = Color.BLACK;
	private static final Stroke RULER_NORMAL_STROKE = new BasicStroke(1.25f);
	private static final int RULER_NORMAL_LENGTH = RULER_HEIGHT - 2;

	private static final Color RULER_SMALL_FIVE_COLOR = Color.DARK_GRAY;
	private static final Stroke RULER_SMALL_FIVE_STROKE = new BasicStroke(0.75f);
	private static final int RULER_SMALL_FIVE_LENGTH = RULER_HEIGHT - 4;

	private static final Color RULER_SMALL_COLOR = Color.GRAY;
	private static final Stroke RULER_SMALL_STROKE = new BasicStroke(0.5f);
	private static final int RULER_SMALL_LENGTH = RULER_HEIGHT - 6;

	private static final Color RULER_TEXT_COLOR = Color.BLACK;

	private static final int SPACE_BETWEEN_TEXTS = 10;

	private float sliderPercentPosition;
	private int sliderTrackWidth;

	private boolean dragging;

	private int precision;

	private double undraggedValue;

	public BetterSlider(final double left, final double right, final double value) {
		this(left, left, right, right, value);
	}

	public BetterSlider(final double leftMin, final double leftMax, final double rightMin, final double rightMax) {
		this(leftMin, leftMax, rightMin, rightMax, leftMin);
	}

	public BetterSlider(final double leftMin, final double leftMax, final double rightMin, final double rightMax, final double value) {
		leftBorderMinValue = leftMin;
		leftBorderMaxValue = leftMax;
		rightBorderMinValue = rightMin;
		rightBorderMaxValue = rightMax;
		setLayout(null);
		add(button = createSliderButton());
		currentValue = value;
		undraggedValue = currentValue;
		isInteger = false;
	}

	private JButton createSliderButton() {
		final JButton button = new JButton(new ImageIcon(getClass().getClassLoader().getResource(BUTTON_IMAGE)));
		button.setSize(BUTTON_WIDTH, BUTTON_HEIGHT);

		final Point dragTouch = new Point(0,0);

		button.addMouseListener(new MouseAdapter(){
			@Override
			public void mousePressed(final MouseEvent e) {
				if (! isEnabled()) {
					return;
				}
				if (e.getButton() == MouseEvent.BUTTON1) {
					dragTouch.setLocation(e.getX(), e.getY());
					beginDragging();
				}
			}
			@Override
			public void mouseReleased(final MouseEvent e) {
				if (! isEnabled()) {
					return;
				}
				if (isDragging()) {
					endDragging();
				}
			}
		});
		button.addMouseMotionListener(new MouseMotionAdapter(){
			@Override
			public void mouseDragged(final MouseEvent e) {
				if (! isEnabled()) {
					return;
				}
				if (isDragging()) {
					doDragging((e.getX() - dragTouch.x)/DRAG_FACTOR);
				}
			}
		});
		button.addKeyListener(new KeyAdapter(){
			@Override
			public void keyTyped(final KeyEvent e) {
				if (! isEnabled()) {
					return;
				}
				final int dist;
				switch(e.getKeyCode()) {
				case KeyEvent.VK_RIGHT:
				case KeyEvent.VK_KP_RIGHT:
				case KeyEvent.VK_UP:
				case KeyEvent.VK_KP_UP:
				case KeyEvent.VK_PAGE_UP:
					dist = RIGHT_STEP_DISTANCE;
					break;
				case KeyEvent.VK_LEFT:
				case KeyEvent.VK_KP_LEFT:
				case KeyEvent.VK_DOWN:
				case KeyEvent.VK_KP_DOWN:
				case KeyEvent.VK_PAGE_DOWN:
					dist = LEFT_STEP_DISTANCE;
					break;
				default:
					dist = 0;
				}
				if (dist != 0) {
					beginDragging();
					doDragging(dist);
					endDragging();
				}
			}
		});
		return button;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	@Override
	public void setEnabled(final boolean enabled) {
		super.setEnabled(enabled);
		button.setEnabled(enabled);
	}

	private void doDragging(final float dist) {
		final double diff = (rightBorderValue - leftBorderValue) * ((double)dist / (double)sliderTrackWidth);
		setNewValue(currentValue+diff, true);
	}

	private void setNewValue(final double newValue, final boolean invokeListeners) {
		currentValue = newValue;
		if (!dragging) {
			undraggedValue = currentValue;
		}

		if (currentValue < leftBorderMinValue) {
			currentValue = leftBorderMinValue;
		} else if (currentValue > rightBorderMaxValue) {
			currentValue = rightBorderMaxValue;
		}
		if (invokeListeners) {
			for (final SliderListener listener: listeners) {
				listener.changeValue(this, currentValue);
			}
		}
		doLayout();
		repaint();
	}

	/* (non-Javadoc)
	 * @see test.DraggableSlider#isDragging()
	 */
	public boolean isDragging() {
		return dragging;
	}

	private void endDragging() {
		if (dragging) {
			for (final SliderListener listener: listeners ) {
				listener.endChanging(this);
			}
		}
		dragging = false;
		undraggedValue = currentValue;
	}

	private void beginDragging() {
		dragging = true;
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getMinimumSize()
	 */
	@Override
	public Dimension getMinimumSize() {
		return new Dimension(100, 4 + FONT_SIZE + BUTTON_HEIGHT + RULER_HEIGHT);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#getPreferredSize()
	 */
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(150, 4 + FONT_SIZE + BUTTON_HEIGHT + RULER_HEIGHT);
	}

	/* (non-Javadoc)
	 * @see javax.swing.JComponent#paintComponent(java.awt.Graphics)
	 */
	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		g2d.setColor(Color.GRAY);
		g2d.draw3DRect(MARGIN_LEFT, 1 + (BUTTON_HEIGHT - TRACK_HEIGHT)/2, sliderTrackWidth-1, TRACK_HEIGHT, true);

		drawRuler(g2d);
	}

	/**
	 * @param g2d
	 */
	private void drawRuler(final Graphics2D g2d) {
		final double valueDiff = rightBorderValue - leftBorderValue;

		final double rulerTick = Math.pow(10, Math.floor(Math.log10(valueDiff)));
		final double rulerTickFive = rulerTick * 5;

		final double rulerBiggerTick = rulerTick * 10;

		final double rulerSmallerTick = rulerTick / 10;
		final double rulerSmallerTickFive = rulerTick / 2;

		final double tickRounding = rulerTick / 100;

		final double offset = leftBorderValue % rulerTick; // nur ungef�hr!

		final int nrTicks = (int)Math.ceil(valueDiff / rulerTick);
		final double useTick;
		if (sliderTrackWidth > 500 || (sliderTrackWidth > 100 && nrTicks < 5) || nrTicks < 3) {
			useTick = rulerSmallerTick;
		} else if (sliderTrackWidth > 200 || (sliderTrackWidth > 100 && nrTicks < 10) || nrTicks < 5) {
			useTick = rulerSmallerTickFive;
		} else {
			useTick = rulerTick;
		}

		final List<String> displayValues = new ArrayList<String>();
		final List<Double> displayPositions = new ArrayList<Double>();

		for (double tick = leftBorderValue - offset; tick <= rightBorderValue + tickRounding; tick += useTick) {
			if (tick <= leftBorderValue - tickRounding) {
				continue;
			}
			final double position = (tick-leftBorderValue)/valueDiff;
			if (tickIsMultipleOf(tick, rulerTick, tickRounding)) {
				displayValues.add(displayValueToString(Math.round(tick / rulerTick) * rulerTick));
				displayPositions.add(position);

				if (tickIsMultipleOf(tick, rulerTickFive, tickRounding)) {
					if (tickIsMultipleOf(tick, rulerBiggerTick, tickRounding)) {
						drawRulerLine(g2d, position, RULER_BIG_COLOR, RULER_BIG_STROKE, RULER_BIG_LENGTH); // big tick
					} else {
						drawRulerLine(g2d, position, RULER_NORMAL_FIVE_COLOR, RULER_NORMAL_FIVE_STROKE, RULER_NORMAL_FIVE_LENGTH); // normal 5-tick
					}
				} else {
					drawRulerLine(g2d, position, RULER_NORMAL_COLOR, RULER_NORMAL_STROKE, RULER_NORMAL_LENGTH); // normal tick
				}
			} else {
				if (tickIsMultipleOf(tick, rulerSmallerTickFive, tickRounding)) {
					drawRulerLine(g2d, position, RULER_SMALL_FIVE_COLOR, RULER_SMALL_FIVE_STROKE, RULER_SMALL_FIVE_LENGTH); // small 5-tick
				} else {
					drawRulerLine(g2d, position, RULER_SMALL_COLOR, RULER_SMALL_STROKE, RULER_SMALL_LENGTH); // small tick
				}
			}
		}
		showRulerValue(g2d, displayPositions, displayValues);
	}

	protected abstract String displayValueToString(final double value);

	/**
	 * @param g2d
	 * @param d
	 */
	private void showRulerValue(final Graphics2D g, final List<Double> relativePositions, final List<String> values) {
		final int top = 3 + FONT_SIZE + BUTTON_HEIGHT + RULER_HEIGHT;
		final List<Rectangle2D> bounds = new ArrayList<Rectangle2D>(values.size());

		g.setColor(RULER_TEXT_COLOR);
		g.setFont(LABEL_FONT);

		int wholeWidth = 0;
		for (final String text: values) {
			final Rectangle2D bound = g.getFontMetrics().getStringBounds(text, g);
			bounds.add(bound);
			wholeWidth += bound.getWidth() + SPACE_BETWEEN_TEXTS;
		}
		if (wholeWidth <= getWidth()) {
			for (int i=0; i < values.size(); i++) {
				final Rectangle2D bound = bounds.get(i);
				g.drawString(values.get(i), (float)Math.min(getWidth() - bound.getWidth(), Math.max(0, MARGIN_LEFT + relativePositions.get(i) * sliderTrackWidth - bound.getCenterX())), top);
			}
		} else {
			for (int i=0; i < values.size(); i+=values.size()-1) { // only first and last
				final Rectangle2D bound = bounds.get(i);
				g.drawString(values.get(i), (float)Math.min(getWidth() - bound.getWidth(), Math.max(0, MARGIN_LEFT + relativePositions.get(i) * sliderTrackWidth - bound.getCenterX())), top);
			}
		}
	}

	private void drawRulerLine(final Graphics2D g, final double relativePosition, final Color color, final Stroke stroke, final int length) {
		g.setColor(color);
		g.setStroke(stroke);
		final double x = MARGIN_LEFT + relativePosition * sliderTrackWidth;
		final double y = 2 + BUTTON_HEIGHT;
		g.draw(new Line2D.Double(x, y, x, y + length));
	}

	private boolean tickIsMultipleOf(final double tick, final double quotient, final double rounding) {
		return Math.abs((tick+rounding/2) % quotient) <= rounding;
	}

	/* (non-Javadoc)
	 * @see java.awt.Container#doLayout()
	 */
	@Override
	public void doLayout() {
		computeDisplayPosition();
		locateSliderButton();
		updatePrecision();
		super.doLayout();
	}

	/**
	 * 
	 */
	private void updatePrecision() {
		precision = Math.max(1,(int)(Math.min(1-Math.log10(rightBorderValue - leftBorderValue),0) + Math.ceil(Math.log10(sliderTrackWidth))));
	}

	/**
	 * set the slider button to the new position
	 */
	private void locateSliderButton() {
		button.setLocation(Math.round(MARGIN_LEFT + sliderPercentPosition * sliderTrackWidth - BUTTON_WIDTH / 2.0f), 1);
	}

	/**
	 * computes the new (relative) position
	 */
	private void computeDisplayPosition() {
		sliderTrackWidth = getWidth() - MARGIN_LEFT - MARGIN_RIGHT;

		leftBorderValue = currentValue - Math.abs(currentValue * LEFT_BORDER_RELATIVE_DISTANCE);
		if (leftBorderValue < leftBorderMinValue) {
			leftBorderValue = leftBorderMinValue;
		} else if (leftBorderValue > leftBorderMaxValue) {
			leftBorderValue = leftBorderMaxValue;
		}
		rightBorderValue = currentValue + Math.abs(currentValue * RIGHT_BORDER_RELATIVE_DISTANCE);
		if (rightBorderValue < rightBorderMinValue) {
			rightBorderValue = rightBorderMinValue;
		} else if (rightBorderValue > rightBorderMaxValue) {
			rightBorderValue = rightBorderMaxValue;
		}

		sliderPercentPosition = (float)(((isInteger?Math.round(currentValue):currentValue) - leftBorderValue) / (rightBorderValue - leftBorderValue));
	}

	/**
	 * only for testing purposes
	 */
	public static void main(final String[] args) {
		final JFrame frame = new JFrame();
		frame.setBounds(100,100,500,400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new GridLayout());
		frame.getContentPane().add(new BetterSlider(0.0,5.0, 10.0,15.0){
			private static final long serialVersionUID = 1L;

			@Override
			protected String displayValueToString(final double value) {
				return String.valueOf(value);
			}
		});
		frame.pack();
		frame.setVisible(true);
	}

	/* (non-Javadoc)
	 * @see test.DraggableSlider#addListener(test.SliderListener)
	 */
	public void addListener(final SliderListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see test.DraggableSlider#removeListener(test.SliderListener)
	 */
	public void removeListener(final SliderListener listener) {
		listeners.remove(listener);
	}

	/* (non-Javadoc)
	 * @see test.DraggableSlider#getUndraggedValue()
	 */
	public double getUndraggedValue() {
		return undraggedValue;
	}

	/* (non-Javadoc)
	 * @see test.DraggableSlider#getValue()
	 */
	public double getValue() {
		return currentValue;
	}

	/* (non-Javadoc)
	 * @see test.DraggableSlider#setValue(double)
	 */
	public double setValue(final double newValue) {
		final double result = currentValue;
        setNewValue(newValue, false);
		return result;
	}

	/* (non-Javadoc)
	 * @see view.slider.DraggableSlider#setLeftBorderMaxValue(double)
	 */
	public double setLeftBorderMaxValue(final double leftBorderMaxValue) {
		final double result = this.leftBorderMaxValue;
		this.leftBorderMaxValue = leftBorderMaxValue;
		setNewValue(currentValue, false);
		return result;
	}

	/* (non-Javadoc)
	 * @see view.slider.DraggableSlider#setRightBorderMinValue(double)
	 */
	public double setRightBorderMinValue(final double rightBorderMinValue) {
		final double result = this.rightBorderMinValue;
		this.rightBorderMinValue = rightBorderMinValue;
		setNewValue(currentValue, false);
		return result;
	}

	/* (non-Javadoc)
	 * @see view.slider.DraggableSlider#setRightBorderMaxValue(double)
	 */
	public double setRightBorderMaxValue(final double rightBorderMaxValue) {
		final double result = this.rightBorderMaxValue;
		this.rightBorderMaxValue = rightBorderMaxValue;
		setNewValue(currentValue, false);
		return result;
	}

	/* (non-Javadoc)
	 * @see view.slider.DraggableSlider#setLeftBorderMinValue(double)
	 */
	public double setLeftBorderMinValue(final double leftBorderMinValue) {
		final double result = this.leftBorderMinValue;
		this.leftBorderMinValue = leftBorderMinValue;
		setNewValue(currentValue, false);
		return result;
	}

	public void setBounds(final double leftBorderMinValue, final double leftBorderMaxValue, final double rightBorderMinValue, final double rightBorderMaxValue) {
		this.leftBorderMinValue = leftBorderMinValue;
		this.leftBorderMaxValue = leftBorderMaxValue;
		this.rightBorderMinValue = rightBorderMinValue;
		this.rightBorderMaxValue = rightBorderMaxValue;
		setNewValue(currentValue, false);
	}

	/* (non-Javadoc)
	 * @see view.slider.DraggableSlider#getLeftBorderMinValue()
	 */
	public double getLeftBorderMinValue() {
		return leftBorderMinValue;
	}

	/* (non-Javadoc)
	 * @see view.slider.DraggableSlider#getLeftBorderMaxValue()
	 */
	public double getLeftBorderMaxValue() {
		return leftBorderMaxValue;
	}

	/* (non-Javadoc)
	 * @see view.slider.DraggableSlider#getRightBorderMinValue()
	 */
	public double getRightBorderMinValue() {
		return rightBorderMinValue;
	}

	/* (non-Javadoc)
	 * @see view.slider.DraggableSlider#getRightBorderMaxValue()
	 */
	public double getRightBorderMaxValue() {
		return rightBorderMaxValue;
	}

	/**
	 * @return the current precision due to the current slider bounds
	 */
	public int getPrecision() {
		return precision;
	}
}
