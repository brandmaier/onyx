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

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class LatexEdgeLabel extends PlainEdgeLabel
{

	private static LaTeXParser lp = new LaTeXParser();
	private LaTeXParserTree tree;
	private Font defaultFont;
	private Font smallFont;
	private FontMetrics defaultFm;
	private FontMetrics smallFm;

	LatexEdgeLabel(String content) {
		super(content);
		
	}
	
	
	public void setContent(String content) {
		
		 if (content.equals(this.content)) return;
		
		 if (!content.startsWith("$")) {
			 this.content = content;
			 tree= null;
		 } else {
			 tree = lp.parse(content.substring(1));
		 }
		 
		 defaultFm = null; // force update of fontmetrics on next redraw
		 
		 super.setContent(content);
	}
	
	
	
	
	protected void updateFontMetrics() {
		super.updateFontMetrics();
		
		if (g== null) {
			System.err.println("Graphics context not available in LatexEdgeLabel!");
			return;
		}
		
		defaultFont = g.getFont().deriveFont((float)this.fontSize);
		smallFont = defaultFont.deriveFont( (float) (defaultFont.getSize()*0.75) );

		
		defaultFm = g.getFontMetrics(defaultFont);
		smallFm = g.getFontMetrics(smallFont);


		
		/*if (defaultFm == null || smallFm == null) 
		{
			System.err.println("Graphics context not established in LaTeXEdgeLabel!!");
			this
		}*/
		if (this.tree == null) return;
		
		this.width = 0;
		LaTeXParserTree tree = this.tree;
		while (true)
		{
			if (tree.content != null)
				width += defaultFm.stringWidth(tree.content);
			
			int updownwidth = 0;
			if (tree.up != null) {
				updownwidth += smallFm.stringWidth(tree.up.content);
			}
			if (tree.down != null) {
				updownwidth = Math.max( updownwidth,
						smallFm.stringWidth(tree.down.content));
			}
			width += updownwidth;
			
			tree = tree.next;
			
			if (tree == null) break;
		}
		
		
		
		
	}
	
/*	public int getWidth()
	{
		
	
	}
	*/
	
	public void _drawImplementation(Graphics2D g, int x, int y, Color backgroundColor)
	{
		_drawImplementation(g, x, y,true,false,backgroundColor);
		_drawImplementation(g, x, y,false,true, backgroundColor);
	}
	
	public void _drawImplementation(Graphics2D g, int x, int y, boolean outline, boolean text, Color backgroundColor)
	{
		if (tree == null || tree.content==null) 
			{
			//return;
			g.setFont(defaultFont);
			g.setColor(super.color);
			drawStringOutlined(g,  content, x - width/2 ,y, outline, text , backgroundColor, super.color);
			return;
			}
		
		if (defaultFm == null) {
			this.g = g;
			updateFontMetrics();
		}
		
		/*g.setColor(Color.white);
		g.fillRect(x-width/2,y-height,width,height);
		g.setColor(Color.black);
		*/
		int xOffset = -width/2;
		
		LaTeXParserTree tree = this.tree;
		
		while (true)
		{
			
			g.setFont(defaultFont);
	
		
		//	 g.drawString(tree.content, x+xOffset, y);
			 drawStringOutlined(g, tree.content, x+xOffset, y, outline, text,backgroundColor, super.color);
			 xOffset+=defaultFm.stringWidth(tree.content);
		
			
			g.setFont(smallFont);
			int yOffset;
			int tempXoffset = 0;
			if (tree.up != null) {
				yOffset = -defaultFont.getSize()/2;
				//g.drawString(tree.up.content,x+xOffset,y+yOffset);
				drawStringOutlined(g, tree.up.content, x+xOffset,y+yOffset, outline, text, backgroundColor, super.color);
				tempXoffset = smallFm.stringWidth(tree.up.content);
			} 
			if (tree.down != null) {
				yOffset = defaultFont.getSize()/2;
				//g.drawString(tree.down.content,x+xOffset,y+yOffset);
				drawStringOutlined(g, tree.down.content, x+xOffset,y+yOffset, outline, text, backgroundColor, super.color);
				tempXoffset = Math.max(tempXoffset, smallFm.stringWidth(tree.down.content));
			}
			//if (child.content != null)
			xOffset+=tempXoffset;
			
			tree = tree.next;
			
			if (tree==null) break;
			
		}
		
		g.setFont(defaultFont);
		
		
		
//		fm.stringWidth(str)
	}


}
