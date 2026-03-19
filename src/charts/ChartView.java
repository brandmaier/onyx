package charts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileFilter;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.BoxChart;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.internal.chartpart.Chart;

import engine.DatasetChangedListener;
import engine.Preferences;
import engine.RawDataset;
import gui.Desktop;
import gui.Utilities;
import gui.fancy.DropShadowBorder;
import gui.views.DataView;
import gui.views.View;

public class ChartView  extends View implements ActionListener, ComponentListener, DatasetChangedListener {

	protected Chart chart;
	protected XChartPanel cpanel;
	protected RawDataset rds;
	
	protected DataView dataView;
	private JMenuItem menuClose;
	private JMenuItem menuSave;
	
	
	protected void removeAllSeries(Chart chart)
	{
		Map<String, XYSeries> ser = chart.getSeriesMap();

		// copy the keys before starting removal
		for (String name : new ArrayList<>(ser.keySet())) {
		    chart.removeSeries(name);
		}
		
		
	}
	
	public void updateTitle(String title) {
		this.setBorder(new DropShadowBorder(title, 3, Color.gray));
	}
	
	public static boolean contains(int[] x, int v) {
	    for (int n : x) {
	        if (n == v) {
	            return true;
	        }
	    }
	    return false;
	}
	
	public ChartView(Desktop desktop, DataView dataView)
	{
		super(desktop);
		this.dataView = dataView;
		
		updateTitle("");
	}
	
	protected void initChartPanel(JComponent cpanel)
	{
	    cpanel.setPreferredSize(new Dimension(200,200));

	     
	     JPanel panel = new JPanel();
	     panel.setOpaque(false);
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
		
			populateContextMenu(menu);
			
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

	@Override
	public void datasetChanged() {
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if (e.getSource() == menuClose)
			this.getDesktop().removeView(this);
		//Desktop.getLinkHandler().unlink(getGraph());
		
		else if (e.getSource() == menuSave) {
			
	File dir = new File(Preferences.getAsString("DefaultWorkingPath"));
			
			
			final JFileChooser fc = new JFileChooser(dir);

			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			//fc.setFileHidingEnabled(false);
//			fc.setFileFilter(fileFilter);
		
			//fc.addChoosableFileFilter(new FileFilter());
			fc.setAcceptAllFileFilterUsed(true);
			
			
			fc.setDialogTitle("Export plot");
			//In response to a button click:
			int returnVal = fc.showSaveDialog(this );

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				String fpath = file.getAbsolutePath();
				try {
					BitmapEncoder.saveBitmapWithDPI(chart, fpath,BitmapFormat.PNG, 300);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			}
		}
	}
	
	protected void populateContextMenu(JPopupMenu menu) {
		
		//menu.addSeparator();
		menuSave = new JMenuItem("Save as PNG");
		menuSave.addActionListener(this);
		menu.add(menuSave);
		menu.addSeparator();
		
		menuClose = new JMenuItem("Close");
		menu.add(menuClose);
		menuClose.addActionListener(this);
	}


}
