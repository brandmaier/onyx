/**
 * (c) 2009 by MPIB-Berlin
 */
package gui.slider;

/**
 * @author Nannette Liske
 *
 */
public interface DraggableSlider {

	public void addListener(SliderListener listener);

	public void removeListener(SliderListener listener);

	/**
	 * @param newValue
	 * @return old value that was set before
	 */
	public double setValue(double newValue);

	/**
	 * @return current value
	 */
	public double getValue();

	/**
	 * @return whether the slider currently
	 * being dragged by the user
	 */
	public boolean isDragging();

	/**
	 * @return the same as {@link #getValue()} if the slider is <b>not</b> dragged.
	 * But if the slider is <b>currently being dragged</b>, then this is the value
	 * that has been set before dragging.
	 */
	public double getUndraggedValue();

	/**
	 * @param leftBorderMaxValue
	 */
	public double setLeftBorderMaxValue(double leftBorderMaxValue);

	public double getLeftBorderMaxValue();

	/**
	 * @param rightBorderMinValue
	 */
	public double setRightBorderMinValue(double rightBorderMinValue);

	public double getRightBorderMinValue();

	/**
	 * @param rightBorderMaxValue
	 */
	public double setRightBorderMaxValue(double rightBorderMaxValue);

	public double getRightBorderMaxValue();

	/**
	 * @param leftBorderMinValue
	 */
	public double setLeftBorderMinValue(double leftBorderMinValue);

	public double getLeftBorderMinValue();

	/**
	 * @param leftBorderMinValue
	 * @param leftBorderMaxValue
	 * @param rightBorderMinValue
	 * @param rightBorderMaxValue
	 */
	public void setBounds(double leftBorderMinValue, double leftBorderMaxValue, double rightBorderMinValue, double rightBorderMaxValue);
}
