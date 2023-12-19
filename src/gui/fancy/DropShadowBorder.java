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

import gui.views.View;

import java.awt.*;

import javax.swing.border.*;

/**
 * 
 * based on ideas from :
 * 
 * A drop shadow border. Draws a 1 pixel line completely around the component,
 * and a drop shadow effect on the right and bottom sides.
 * @author Dale Anson, 25 Feb 2004
 * @version $Revision: 1.3 $
 */
public class DropShadowBorder extends AbstractBorder {


   private static final long serialVersionUID = -3581586569262057253L;

   // the width in pixels of the drop shadow
   private int _width = 1;

   // the color of the drop shadow
   private Color _color = Color.GRAY;
   
   private String _title = "";
   
   public final static int arc = 20;
   
	private Stroke stroke = new BasicStroke(1, BasicStroke.CAP_BUTT, 
            BasicStroke.JOIN_BEVEL);
   
   
   private static Font font =  new Font("Sans Serif",Font.PLAIN, 11);

   /**
    * Drop shadow with default width of 3 pixels and black color.
    */
   public DropShadowBorder() {
      this( "", 1 );
   }

   /**
    * Drop shadow, default shadow color is black.
    * @param width the width of the shadow.
    */
   public DropShadowBorder(String title, int width ) {
      this( title, width, Color.BLACK );
   }

   /**
    * Drop shadow, width and color are adjustable.
    * @param width the width of the shadow.
    * @param color the color of the shadow.
    */
   public DropShadowBorder( String title, int width, Color color ) {
	  _title = title;
      _width = width;
      _color = color;
   }

   /**
    * This implementation returns a new Insets instance where the top and left are 1, 

    * the bottom and right fields are the border width + 1.

    * @param c the component for which this border insets value applies

    * @return a new Insets object initialized as stated above.

    */
   public Insets getBorderInsets( Component c ) {
      return new Insets( 1, 1, _width + 1, _width + 1 );
   }

   /**
    * Reinitializes the <code>insets</code> parameter with this DropShadowBorder's 

    * current Insets.   

    * @param c the component for which this border insets value applies

    * @param insets the object to be reinitialized

    * @return the given <code>insets</code> object

    */
   public Insets getBorderInsets( Component c, Insets insets ) {
      insets.top = 1;
      insets.left = 1;
      insets.bottom = _width + 1;
      insets.right = _width + 1;
      return insets;
   }

   /**
    * This implementation always returns true.

    * @return true

    */
   public boolean isBorderOpaque() {
      return true;
   }

   /**
    * Paints the drop shadow border around the given component.   

    * @param c - the component for which this border is being painted

    * @param g - the paint graphics

    * @param x - the x position of the painted border

    * @param y - the y position of the painted border

    * @param width - the width of the painted border

    * @param height - the height of the painted border   

    */
   public void paintBorder( Component c, Graphics g, int x, int y, int width, int height ) {
    
		 Graphics2D g2d = (Graphics2D)g;
			// set anti-aliasing
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		     // Set rendering quality
	        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
	        // Enable fractional metrics for text
	        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);

	     
	     g2d.setStroke(stroke);
	   
	 //  Color old_color = g.getColor();
   //   int x1, y1, x2, y2;
      g.setColor( new Color(214,214,214) );

      Shape oldClip = ((Graphics2D)g).getClip();
      ((Graphics2D)g).setClip(6, 6, width, height);
      
      // draw the drop shadow
      for ( int i = 0; i <= _width; i++ ) {
        
          ((Graphics2D)g).drawRoundRect(x+i, y+i, width - _width - 1, height - _width - 1 , arc, arc);
      
      }
      
      // restore old clip
      
      g.setClip(oldClip);
     
      
      // outline the component with a 1-pixel wide line
      int arc = 20;
      g.setColor(Color.white);
      //((Graphics2D)g).fillRoundRect(x, y, width - _width - 1, height - _width - 1 , arc, arc);
      g.setColor( _color);
      g.drawRoundRect(x, y, width - _width - 1, height - _width - 1 , arc, arc);
      

      
      
      // fill in the corner rectangles with the background color of the parent
      // container
 /*     if ( c.getParent() != null ) {
         g.setColor( c.getParent().getBackground() );
         for ( int i = 0; i <= _width; i++ ) {
            x1 = x;
            y1 = y + height - i;
            x2 = x + _width;
            y2 = y1;
            g.drawLine( x1, y1, x2, y2 );
            x1 = x + width - _width;
            y1 = y + i;
            x2 = x + width ;
            y2 = y1;
            g.drawLine( x1, y1, x2, y2 );
         }
         // add some slightly darker colored triangles
         g.setColor( g.getColor().darker() );
         for ( int i = 0; i < _width; i++ ) {
            // bottom left triangle
            x1 = x + i + 1;
            y1 = y + height - _width + i;
            x2 = x + _width;
            y2 = y1;
            g.drawLine( x1, y1, x2, y2 );

            // top right triangle
            x1 = x + width - _width;
            y1 = y + i + 1;
            x2 = x1 + i ;
            y2 = y1;
            g.drawLine( x1, y1, x2, y2 );
         }
      }
      */
      // add title
      g.setFont(font);
      FontMetrics fm = g.getFontMetrics();
      int w = fm.stringWidth(_title);
      g.setColor(Color.black);
      oldClip = g.getClip();
//      g.setC
      g.setClip(x,y,width-20,height);
      
      int ypos = (int)Math.round((View.sizeMoveArea-font.getSize())/2.0);
      if (ypos < 0) ypos=0;
     
      
      g.drawString(_title, 15, View.sizeMoveArea - ypos);
      g.setClip(oldClip);
      g.setColor( Color.black );
   }
   
   public static void paintBackgroundInComponent(View view, Graphics2D g, Color color) {
	   int arc = DropShadowBorder.arc;
	    // fill
	    g.setColor(color);
	   // System.out.println(getWidth());
	    g.fillRoundRect(0, 0, view.getWidth() - 1 - 1, view.getHeight() - 1 - 1 , arc, arc);
		
   }
}
