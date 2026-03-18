package charts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.knowm.xchart.BoxChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;

import gui.Desktop;
import gui.Utilities;
import gui.views.View;

public class ChartView  extends View implements ComponentListener {

	protected XChartPanel cpanel;
	
	public ChartView(Desktop desktop)
	{
		super(desktop);
	}
	
	protected void initChartPanel(XChartPanel cpanel)
	{
	    cpanel.setPreferredSize(new Dimension(200,200));

	     
	     JPanel panel = new JPanel();
	     panel.setPreferredSize(new Dimension(400,400));
	     this.setLayout(new BorderLayout());
	     this.add(panel, BorderLayout.CENTER);
	     
	     
	    panel.setLayout(new BorderLayout());
	     panel.add(cpanel, BorderLayout.CENTER);

	     JPanel spacer = new JPanel();
	     spacer.setOpaque(false);
	     spacer.setPreferredSize(new Dimension(20, 20)); // width, height
	     JPanel spacer2 = new JPanel();
	     spacer2.setOpaque(false);
	     spacer2.setPreferredSize(new Dimension(20, 20)); // width, height
	     JPanel spacer3 = new JPanel();
	     spacer3.setOpaque(false);
	     spacer3.setPreferredSize(new Dimension(20, 20)); // width, height
	     JPanel spacer4 = new JPanel();
	     spacer4.setPreferredSize(new Dimension(20, 20)); // width, height
	     spacer4.setOpaque(false);

	     panel.add(spacer, BorderLayout.NORTH);
	     panel.add(spacer2, BorderLayout.SOUTH);
	     panel.add(spacer3, BorderLayout.EAST);
	     panel.add(spacer4, BorderLayout.WEST);
	     
	     
	}
	
	@Override
	public void paintComponent(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		
		// set anti-aliasing
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	     // Set rendering quality
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // Enable fractional metrics for text
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
		// paint everything inherited from View (e.g., selection box)
		super.paintComponent(g);

	
		Shape movableBorderRect = new java.awt.Rectangle(sizeMoveArea, sizeMoveArea, getWidth() - 2 * sizeMoveArea,
					getHeight() - 2 * sizeMoveArea);
		final Color lgray = new Color(230, 230, 230);
		g2d.setColor(lgray);
			
		g2d.draw(movableBorderRect);
			
		
		
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {

		super.mouseClicked(arg0);
		if (arg0.isConsumed())
			return;

		if (Utilities.isRightMouseButton(arg0)) {

			JPopupMenu menu = new JPopupMenu();
		
			menu.add(new JMenuItem("Placeholder"));
			
			menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
		}
	}

	@Override
	public void componentResized(ComponentEvent e) {
		cpanel.revalidate();
		cpanel.repaint();
	
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

}
