package gui;

import javax.swing.JToolTip;



public class OnyxToolTip extends JToolTip {

	public OnyxToolTip()
	{
		// set tooltip delegate that does painting and user interaction
		setUI(new OnyxToolTipUI());
	}
}
