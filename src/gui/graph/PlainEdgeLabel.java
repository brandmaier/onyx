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
package gui.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.Graphics2D;

public class PlainEdgeLabel implements Cloneable {

	protected String content;
	protected Graphics g;
	protected FontMetrics fm;
	protected Font font;
	protected int width = -1, height = -1;
	protected float fontSize = 12;
	protected Color color = Color.black;
	
	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	protected float xAlign = .5f; // .5 = center ; 0 = left, 1 = right
	
	public void setXAlign(float xAlign) 
	{
		xAlign = Math.max(Math.min(xAlign, 1),0);
		this.xAlign = xAlign;
	}
	
	public PlainEdgeLabel clone()
	{
		try {
			return (PlainEdgeLabel)super.clone();
		} catch (CloneNotSupportedException e) {
			
			e.printStackTrace();
			return(null);
		}
	}
	
	PlainEdgeLabel(String content)
	{

		setContent(content);
	}

	public void setContent(String content) {
		this.content = content;
		
		if (fm!=null) {
			updateFontMetrics();
		}
	}
	
	public int getHeight()
	{
		return height;
	}
	
	public int getWidth()
	{
		/*labelWidth = fm.stringWidth(label);
		labelHeight = fm.getHeight();*/
		return width;
	} 
	
	public void draw(Graphics2D g, int lx, int ly, Color backgroundColor)
	{
		if (this.g == null) {
			this.g = g;
		}
		
		if (fm == null || width == -1 || height == -1) {
			


			updateFontMetrics();
		}
		

		_drawImplementation(g, lx, ly, backgroundColor);
		
	}
	
	public void setGraphics(Graphics g)
	{
		this.g = g;
	}

	protected void _drawImplementation(Graphics2D g, int lx, int ly, Color backgroundColor) {
		Font old = g.getFont();
		g.setFont(this.font);
	//	System.out.println(this.font.getSize()+"s");
		drawStringOutlined(g,  content, lx - (int)(width*xAlign) ,ly, backgroundColor, color );
		g.setFont(old);
	}

	protected void updateFontMetrics() {
		
		if (g== null){System.err.println("No font metrics available!"); fm=null; return; }
		
		this.font = this.g.getFont().deriveFont((float)fontSize);
		
		fm = this.g.getFontMetrics(this.font);	
		
		width = fm.stringWidth(content);
		height = fm.getHeight();
	}

	public static void drawStringOutlined(Graphics2D g, String text, int x, int y, Color backgroundColor, Color fontColor)
	{
		drawStringOutlined(g, text, x, y, true, true, backgroundColor, fontColor);
	}
	
	public static void drawStringOutlined(Graphics2D g, String text, int x, int y, 
			boolean outline, boolean dotext, Color backgroundColor, Color fontColor)
	{
		if (outline) {
		Font f = g.getFont();
		
		GlyphVector glyphvec = f.createGlyphVector(g.getFontMetrics(f).getFontRenderContext(), text);
		
		//Shape s = glyphvec.getOutline(x,y);
		
		final int rand=2;
		Rectangle2D bnds = glyphvec.getVisualBounds();
		if (bnds.getWidth()>0) {
		bnds = new Rectangle2D.Double(x+bnds.getX()-rand, 
				y+bnds.getY()-rand, 
				2*rand+bnds.getWidth(),2*rand+bnds.getHeight());
		g.setColor(backgroundColor);
		
		g.fill(bnds);
		}
		
		final Stroke backStroke = new BasicStroke(8, BasicStroke.CAP_BUTT,
				BasicStroke.JOIN_BEVEL);
		
		Stroke old = g.getStroke();
		g.setStroke(backStroke);
		//g.setColor(Color.white);
		//g.setColor(Color.GREEN);
		//((Graphics2D)g).draw(s);
		g.setStroke(old);
		}
		
		if (dotext) {
		
		g.setColor(fontColor);
			g.drawString(text, x, y);
		}
	}
	
	/*@deprecated*/
	public static void drawStringOutlinedOld(Graphics g, String text, int x, int y)
	{
		int d = 1;
		g.setColor(Color.white);
		g.drawString(text, x-d, y+d);
		g.drawString(text, x-d, y-d);
		g.drawString(text, x+d, y+d);
		g.drawString(text, x+d, y-d);

		g.drawString(text, x-2, y+2);
		g.drawString(text, x-2, y-2);
		g.drawString(text, x+2, y+2);
		g.drawString(text, x+2, y-2);

		g.drawString(text, x-2, y+1);
		g.drawString(text, x-2, y-1);
		g.drawString(text, x+2, y+1);
		g.drawString(text, x+2, y-1);
		g.drawString(text, x-1, y+2);
		g.drawString(text, x-1, y-2);
		g.drawString(text, x+1, y+2);
		g.drawString(text, x+1, y-2);
		
		g.setColor(Color.black);
		g.drawString(text, x, y);		
	}

	public void setFontSize(float i) {
		this.fontSize = i;
		fm = null; // invalidate graphics context to force update of font size on next redraw
		
	}
	
	public float getFontSize()
	{
		return this.fontSize;
	}

	
	/*public int setFontSize(int size)
	{
		this.fontSize = size;
		
		// this triggers a reset of the font on next redraw()
		fm = null;
	}*/
	
}
