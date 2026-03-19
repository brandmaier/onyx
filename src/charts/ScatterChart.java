package charts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.util.Arrays;
import java.util.Map;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import org.knowm.xchart.BoxChart;
import org.knowm.xchart.BoxChartBuilder;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.internal.chartpart.Chart;
import org.knowm.xchart.internal.series.MarkerSeries;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.knowm.xchart.style.markers.Marker;
import org.knowm.xchart.style.markers.None;
import org.knowm.xchart.style.markers.SeriesMarkers;

import engine.Dataset;
import engine.RawDataset;
import gui.Desktop;
import gui.Utilities;
import gui.fancy.DropShadowBorder;
import gui.views.DataView;
import gui.views.View;

public class ScatterChart extends ChartView {

	XYChart chart;
	private PlotSettingsDialog.PlotSettings plotSettings;

	public ScatterChart(Desktop desktop, DataView dataView)
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
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Scatter);
		chart.getStyler().setChartTitleVisible(true);
		//chart.getStyler().setLegendPosition(LegendPosition.InsideSW);
		//chart.getStyler().setMarkerSize(16);
		


		
		Dataset ds = dataView.getDataset();

		if (ds instanceof RawDataset) {
			rds = (RawDataset)ds;
		} else {
			rds = new RawDataset();
		}
		
		plotSettings = new PlotSettingsDialog.PlotSettings();
		applyPlotSettings();


		datasetChanged();
	      
	     cpanel = new XChartPanel<>(chart);
	     

 
	     chart.getStyler().setLegendVisible(false);
	

	     initChartPanel(cpanel);
	     
	  //   initChartPanel()

	}
	
	private void openSettingsDialog() {
		new PlotSettingsDialog(plotSettings, updatedSettings -> {
			plotSettings = updatedSettings;
			applyPlotSettings();
			datasetChanged();
		});
	}

	private void applyPlotSettings() {
	//	chart.getStyler().setTheme(plotSettings.theme);
		chart.getStyler().setPlotGridLinesVisible(plotSettings.gridLinesVisible);
		chart.getStyler().setMarkerSize(plotSettings.symbolSize);
		chart.getStyler().setLegendVisible(false);
		chart.setTitle(rds.getName());
	}

	private Marker markerForType(PlotSettingsDialog.PlotSettings.SymbolType symbolType) {
		switch (symbolType) {
		case DIAMOND:
			return SeriesMarkers.DIAMOND;
		case RECTANGLE:
			return SeriesMarkers.SQUARE;
		case CIRCLE:
		default:
			return SeriesMarkers.CIRCLE;
		}
	}

	@Override
	public void datasetChanged() {

		removeAllSeries(chart);
		
		
		
	     int[] sel = dataView.getSelectedIndices() ;
	     
	     if (sel.length < 2) return;
	     

	        	double[] row1 = rds.getColumn(sel[0]);
	        	double[] row2 = rds.getColumn(sel[1]);
	        	

	        
	        XYSeries series = chart.addSeries("Data", row1, row2);
	          // .setMarker(SeriesMarkers.CIRCLE);
	            
	            
	   	     chart.setTitle(rds.getColumnName(sel[0])+ " vs "+rds.getColumnName(sel[1]));
	 	   
	   	     
	 		series.setXYSeriesRenderStyle(XYSeries.XYSeriesRenderStyle.Scatter);
			series.setMarker(markerForType(plotSettings.symbolType));
			series.setMarkerColor(plotSettings.symbolColor);

			this.revalidate();
			this.repaint();
	   	     
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
