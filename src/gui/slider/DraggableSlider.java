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
