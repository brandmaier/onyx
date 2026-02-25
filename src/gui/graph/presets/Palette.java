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
	
	public Color getDarker(int i, int j)
	{
		return(darker(get(i),j));

	}

	public static Color darker(Color col, int j) {
		return(		
				new Color( Math.max(0,col.getRed()-j), 
				Math.max(0, col.getGreen()-j),
				Math.max(0, col.getBlue()-j)
				));
	}
}
