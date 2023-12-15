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
package gui;

import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JToolTip;
import javax.swing.SwingUtilities;
import javax.swing.plaf.metal.MetalToolTipUI;

public class OnyxToolTipUI extends MetalToolTipUI {

	  ImageIcon icon ;
	
	  public void paint(Graphics g, JComponent c)
	    {
	      FontMetrics metrics = c.getFontMetrics( c.getFont() ) ;
	      Dimension size = c.getSize() ;
	      g.setColor( c.getBackground() ) ;
	      g.fillRect( 0, 0, size.width, size.height ) ;
	      int x = 3 ;
	      if( icon != null )
	      {
	        icon.paintIcon( c, g, 0, 0 ) ;
	        x += icon.getIconWidth() + 1 ;
	      }
	      g.setColor( c.getForeground() ) ;
	      g.drawString( ((JToolTip)c).getTipText(), x, metrics.getHeight() ) ;
	    }
	 
	    public Dimension getPreferredSize(JComponent c)
	    {
	      FontMetrics metrics = c.getFontMetrics(c.getFont());
	      String tipText = ((JToolTip)c).getTipText() ;
	      if( tipText == null )
	      {
	        tipText = "";
	      }
	      int width = SwingUtilities.computeStringWidth( metrics, tipText ) ;
	      int height = metrics.getHeight() ;
	      if( icon != null )
	      {
	        width += icon.getIconWidth() + 1 ;
	        height = icon.getIconHeight() > height ? icon.getIconHeight() : height + 4 ;
	      }
	      return new Dimension( width + 6, height ) ;
	    }
	    
}
