/**
 * (c) 2009 by MPIB-Berlin
 */
package gui.slider;

/**
 * @author Nannette Liske
 *
 */
public interface SliderListener {

	/**
	 * @param position
	 */
	void changeValue(DraggableSlider source, double position);

	/**
	 * 
	 */
	void endChanging(DraggableSlider source);

}
