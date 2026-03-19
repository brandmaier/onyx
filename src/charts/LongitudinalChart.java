package charts;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;

import javax.swing.JPopupMenu;

import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.style.markers.None;


import charts.LongitudinalPlotSettingsDialog.PlotSettings;
import engine.Dataset;
import engine.RawDataset;
import gui.Desktop;

import gui.views.DataView;


public class LongitudinalChart extends ChartView {


	private LongitudinalPlotSettingsDialog.PlotSettings plotSettings;
	
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
		
		chart.getStyler().setChartBackgroundColor(Color.white);
		
		Dataset ds = dataView.getDataset();

		if (ds instanceof RawDataset) {
			rds = (RawDataset)ds;
		} else {
			rds = new RawDataset();
		}
		
		plotSettings = new PlotSettings();
		applyPlotSettings();

		datasetChanged();
	      
	     cpanel = new XChartPanel<>(chart);
	     
	     MouseListener[] ml = cpanel.getMouseListeners();
	     for (int i=0; i < ml.length; i++)
	    	 cpanel.removeMouseListener(ml[i]);
	     
	     chart.setTitle(rds.getName());
	     
	     chart.getStyler().setLegendVisible(false);
	     
//	     chart.getStyler().setLine

	     initChartPanel(cpanel);

	}
	
	
	private void openSettingsDialog() {
		new LongitudinalPlotSettingsDialog(plotSettings, updatedSettings -> {
			plotSettings = updatedSettings;
			applyPlotSettings();
			datasetChanged();
		});
	}

	private void applyPlotSettings() {
		chart.getStyler().setLegendVisible(false);
		chart.setTitle(rds.getName());
		chart.setXAxisTitle(plotSettings.xAxisLabel);
		chart.setYAxisTitle(plotSettings.yAxisLabel);
	}

	
	@Override
	public void datasetChanged() {

		removeAllSeries(chart);
		
		int[] selection = dataView.getSelectedIndices();
		
		
		
	       for (int i = 0; i < rds.getSampleSize(); i++) {
	        	

	        	double[] row;
	        	
	        	if (dataView.hasRowsSelected()) {
	        		row = new double[selection.length];
	        		for (int j=0; j < selection.length; j++)
	        			row[j] = rds.getData()[i][selection[j]];
	        	} else {
	        		row = rds.getData()[i];
	        	}
	        	
	        	
	        	
	        
	            //chart.addSeries("Series " + i, row)
	             //    .setMarker(new None());
	            
	            XYSeries series = ((XYChart)chart).addSeries("Series " + i, row);
	            series.setMarker(new None());
	            
	            series.setLineStyle(new BasicStroke());
	            
	            series.setLineWidth(plotSettings.lineThickness);
	            if (plotSettings.lineColorMode == LongitudinalPlotSettingsDialog.PlotSettings.LineColorMode.USER_DEFINED) {
	            	series.setLineColor(plotSettings.lineColor);
	            }
	        }

	       List<String> names;
	       
	       if (dataView.hasRowsSelected()) {
	    	   names = new ArrayList<String>();
	    	   for (int j=0; j < selection.length; j++)
	    		   names.add(rds.getColumnName(selection[j]));
	       } else {
	    	   names = rds.getColumnNamesAsList();
	       }
	       
	       ((XYChart)chart).setCustomXAxisTickLabelsFormatter(x -> names.get((int)Math.round(x-1)));
	       
	       
	   	     this.revalidate();
	   	  this.repaint();
	}


	@Override
	protected void populateContextMenu(JPopupMenu menu) {
		JMenuItem settingsItem = new JMenuItem("Plot settings");
		settingsItem.addActionListener(this);
		menu.add(settingsItem);
		super.populateContextMenu(menu);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() instanceof JMenuItem) {
			JMenuItem item = (JMenuItem) e.getSource();
			if ("Plot settings".equals(item.getText())) {
				openSettingsDialog();
				return;
			}
		}
		super.actionPerformed(e);
	}

}
