package gui.graph.presets;

import java.awt.Color;

public class Palette {

	Color[] colors;
	private Color background_color = Color.white;
	
	public Color getBackgroundColor() {
		return background_color;
	}

	public Palette(Color[] colors) {
		this.colors = colors;
	}

	public Palette(Color[] colors, Color bgcolor) {
		this(colors);
		this.background_color = bgcolor;
	}

	Color get(int i) {
		return this.colors[i % colors.length];
	}
	
	int getSize() {
		return colors.length;
	}
}
