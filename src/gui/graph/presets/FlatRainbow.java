package gui.graph.presets;

import java.awt.Color;

public class FlatRainbow extends FadedColors {

	public String getName() {
		return "Rainbow";
	}
	
	public FlatRainbow() {
		cols = new Color[] {
				new Color(241,90,90), new Color(240,196,25), new Color(78,186,111),
				new Color(45,149,191), new Color(149,91,165), new Color(149,91,54)};
	}
}
