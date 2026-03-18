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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.knowm.xchart.BoxChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.markers.None;
import org.knowm.xchart.style.markers.SeriesMarkers;

import engine.Dataset;
import engine.RawDataset;
import gui.Desktop;
import gui.Utilities;
import gui.fancy.DropShadowBorder;
import gui.views.DataView;
import gui.views.View;

public class LongitudinalChart extends ChartView {

	XYChart chart;

	public LongitudinalChart(Desktop desktop, DataView dataView)
	{
		super(desktop, dataView);
		
		this.movable = true;
		this.resizable = true;
		
		this.setSize(800, 400);
		this.setVisible(true);
		this.setBackground(Color.white);
		
		this.addComponentListener(this);
		

		chart = new XYChartBuilder()
	                .width(600)
	                .height(400)
	                .title("Multi-line plot")
	                .xAxisTitle("X")
	                .yAxisTitle("Y")
	                .build(); 
		
		Dataset ds = dataView.getDataset();

		if (ds instanceof RawDataset) {
			rds = (RawDataset)ds;
		} else {
			rds = new RawDataset();
		}

		datasetChanged();
	      
	     cpanel = new XChartPanel<>(chart);
	     
	     chart.setTitle(rds.getName());
	     
	     chart.getStyler().setLegendVisible(false);
	     
/*	    cpanel.addMouseMotionListener(new MouseMotionAdapter() {

			@Override
			public void mouseDragged(MouseEvent e) {
				
				 LongitudinalChart.this.mouseDragged(e);
			}

			@Override
			public void mouseMoved(MouseEvent e) {
				
				 LongitudinalChart.this.mouseMoved(e);
			}
		
	     });
	     
	    cpanel.addMouseListener(new MouseAdapter() {
	    	 @Override
	    	 public void mousePressed(MouseEvent e) {
	    		 System.out.println("Mouse pressed");
	    		 LongitudinalChart.this.mousePressed(e);
	    	        e.consume();
	    	    }
	    	 
	    	 @Override
	    	 public void mouseReleased(MouseEvent e) {
	    		 LongitudinalChart.this.mouseReleased(e);
	    	        e.consume();
	    	    }
	    	 
	    	 @Override
	    	 public void mouseClicked(MouseEvent e) {
	    		 LongitudinalChart.this.mouseClicked(e);
	    	        e.consume();
	    	    }
	     });
	    */

	     initChartPanel(cpanel);
	    //    new SwingWrapper<>(chart).displayChart();
	}
	

	
	@Override
	public void datasetChanged() {

		removeAllSeries(chart);
		
	       for (int i = 0; i < rds.getSampleSize(); i++) {
	        	

	        	double[] row;
	        	
	        	row = rds.getData()[i];
	        
	            chart.addSeries("Series " + i, row)
	                 .setMarker(new None());
	        }

	       List<String> names = rds.getColumnNamesAsList();
	       
	       chart.setCustomXAxisTickLabelsFormatter(x -> names.get((int)Math.round(x-1)));
	       
	       
	   	     this.revalidate();
	   	  this.repaint();
	}


}
