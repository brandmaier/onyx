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
