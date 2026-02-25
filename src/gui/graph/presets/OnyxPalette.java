package gui.graph.presets;

import java.awt.Color;

public final class OnyxPalette {

	final static Palette red = new Palette(new Color[] {Color.red});
	final static Palette bright = new Palette(new Color[] {
			new Color(220,85,210),
			new Color(90,130,200),
			new Color(180,220,160),
			new Color(250,220,10),
			new Color(230,230,230)
		});
	final static Palette pastel1 = new Palette(new Color[] {
			new Color(240,194,204),
			new Color(240,150,180),
			new Color(150,215,215),
			new Color(155,195,175)
	}, 		new Color(220,240,241));
	final static Palette bluey = new Palette(new Color[] {
			new Color(73,120,165),
			new Color(105,160,185),
			new Color(112,170,190),
			new Color(205,205,215),
			new Color(180,180,190)

	},			new Color(252,255,252));
	final static Palette bluey_old = new Palette(new Color[] {
			new Color(73,120,165),
			new Color(105,160,185),
			new Color(112,170,190),
			new Color(205,215,200),
			new Color(196,200,193)

	},			new Color(252,255,252));
	
	final static Palette faded = new Palette(new Color[] {
			new Color(169,196,216), new Color(96,163,82), new Color(215,176,133),
			new Color(167,124,164), new Color(171,216,171), new Color(215,135,111)
	}
	);
	public static final Palette rainbow = new Palette(
			new Color[] {
					new Color(241,90,90), new Color(240,196,25), new Color(78,186,111),
					new Color(45,149,191), new Color(149,91,165), new Color(149,91,54)}
			);
	
	public static final Palette xmas = new Palette(new Color[] {
			new Color(41,165,131),
			new Color(201,4,11),
			new Color(40,86,11),
			new Color(252,25,52),
			new Color(192,146,10)
	}, new Color(4,4,3));
}
