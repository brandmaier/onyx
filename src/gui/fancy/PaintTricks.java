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
package gui.fancy;


import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.AffineTransform;

public class PaintTricks {

	  public static Color hexToColor(String hex) {
		  
	        // remove any leading "#" from the hex string
	        hex = hex.replace("#", "");

	        // parse the hex string to get individual color components
	        int red = Integer.parseInt(hex.substring(0, 2), 16);
	        int green = Integer.parseInt(hex.substring(2, 4), 16);
	        int blue = Integer.parseInt(hex.substring(4, 6), 16);

	        // create and return a Color object
	        return new Color(red, green, blue);
	    }
	
	private static Color getMixedColor(Color c1, float pct1, Color c2, float pct2) {
	    float[] clr1 = c1.getComponents(null);
	    float[] clr2 = c2.getComponents(null);
	    for (int i = 0; i < clr1.length; i++) {
	        clr1[i] = (clr1[i] * pct1) + (clr2[i] * pct2);
	    }
	    return new Color(clr1[0], clr1[1], clr1[2], clr1[3]);
	}
	
	public static void paintBorderShadow(Graphics2D g2, Shape clipShape, int shadowWidth) {

	    int sw = shadowWidth*2;
	    for (int i=sw; i >= 2; i-=2) {
	        float pct = (float)(sw - i) / (sw - 1);
	        g2.setColor(getMixedColor(Color.LIGHT_GRAY, pct,
	                                  Color.WHITE, 1.0f-pct));
	        g2.setStroke(new BasicStroke(i));
	        g2.draw(clipShape);
	    }
	}
	


	public static void shadedVerticalFill(Graphics2D g, Rectangle r, Color c1, int steps)
	{
		Shape oldClip = g.getClip();
		g.clip(r);
		
		double diameter = Math.sqrt(r.getWidth()*r.getWidth()+r.getHeight()*r.getHeight());
		double pixel_per_step = diameter/steps;
		
		int inc = 1;
		
		for (int i=steps; i >=0; i--)
		{
			Shape s = new Rectangle(r.x, r.y, r.width, (int)(
					pixel_per_step*i));
			
			g.setColor(c1);
			g.fill(s);
			
			c1 = new Color(Math.max(c1.getRed()-inc,0)
					,Math.max(c1.getGreen()-inc,0)
					,Math.max(c1.getBlue()-inc,0));
		}

		
		g.setClip(oldClip);
	}
	
	public static void shadedFill(Graphics2D g, Rectangle r, Shape curshape, Color c1, int steps)
	{

		Shape oldClip = g.getClip();
		g.clip(curshape);
		
		double diameter = Math.sqrt(r.getWidth()*r.getWidth()+r.getHeight()*r.getHeight());
		double pixel_per_step = diameter/steps;
		
		int inc = 1;
		
		 AffineTransform afx = new AffineTransform();
		 afx.rotate(Math.PI*3/4, r.getCenterX(), r.getCenterY());
		 
		for (int i=steps; i >=0; i--)
		{
			Shape s = new Rectangle((int)(r.getCenterX()-diameter/2), 
					(int)(r.getCenterY()-diameter/2), (int)diameter, (int)(
					pixel_per_step*i));

			s = afx.createTransformedShape(s);
			g.setColor(c1);
			g.fill(s);
			
			c1 = new Color(Math.max(c1.getRed()-inc,0)
					,Math.max(c1.getGreen()-inc,0)
					,Math.max(c1.getBlue()-inc,0));
		}
		

		g.setClip(oldClip);
	}
	
}
