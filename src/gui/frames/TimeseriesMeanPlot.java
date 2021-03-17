package gui.frames;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;


import engine.Dataset;
import engine.ModelListener;
import engine.ModelRun.Status;
import engine.ModelRun.Warning;
import engine.backend.Model.Strategy;
import engine.backend.RAMModel;
import engine.OnyxModel;
import engine.Statik;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.View;



public class TimeseriesMeanPlot extends View implements ModelListener
{
	ChartPanel chartPanel; 
		double[] x, mean, upper, lower;
		private OnyxModel model;

		public TimeseriesMeanPlot(OnyxModel model)
		{
			this.model = model;
			
			model.addModelListener(this);
			
			
			this.setSize(500,500);
			
			updatePoints();
			
			this.setVisible(true);
			
			
			
		}
		
		
		public void updatePoints()
		{
			System.out.println("Update points!");
			
			double[] modelmean = model.meanVal;
			mean = calc(modelmean);
			modelmean[0] += Math.sqrt(model.symVal[0][0]); 
			modelmean[1] += Math.sqrt(model.symVal[1][1]); 
			upper = calc(modelmean);
			modelmean[0] -= 2*Math.sqrt(model.symVal[0][0]); 
			modelmean[1] -= 2*Math.sqrt(model.symVal[1][1]); 			
			lower = calc(modelmean);
			
			x = new double[mean.length];
			for (int i=0; i < mean.length;i++) {
				x[i] = i;
			}
			
	/*		final XYSeries series = new XYSeries("Mean");
			final XYSeries series_up = new XYSeries("Upper");
			final XYSeries series_low = new XYSeries("Lower");
			for (int i=0; i < mean.length;i++) {
				series.add(x[i], mean[i]);
				series_up.add(x[i],upper[i]);
				series_low.add(x[i],lower[i]);
			}
			
//			series_up.
			
		    XYSeriesCollection data = new XYSeriesCollection(series);
		    data.addSeries(series_up);
		    data.addSeries(series_low);
		    final JFreeChart chart = ChartFactory.createXYLineChart(
		        "Change over Time",
		        "Time", 
		        "Observed value", 
		        data,
		        PlotOrientation.VERTICAL,
		        false,
		        true,
		        false
		    );

		    // ((org.jfree.chart.plot.XYPlot)chart.getPlot());//;.getRenderer();
		    XYPlot p = chart.getXYPlot();
		    p.getRenderer().setStroke( new BasicStroke(4));
		    
		    p.setBackgroundPaint(Color.white);
		    p.setDomainGridlinePaint(Color.gray);
		    p.setRangeGridlinePaint(Color.gray);
		    */
		    //if (chartPanel == null)
		    //	{
		/*    this.removeAll();
		    	chartPanel = new ChartPanel(chart);
		  */  	
			
		   // JPanel panel = new JPanel();
		    //this.removeAll();
		    this.add(chartPanel);
		    //	}
		    
		   // chartPanel.setChart(chart);
		    
		    Dimension dim = new Dimension(this.getSize().width-50 ,
		    		this.getSize().height-50);
		    chartPanel.setSize(dim);
		    chartPanel.setLocation(25,25);
		    //this.setContentPane(panel);
		   // this.pack();
		    
		    
		    
		    this.repaint();
		    this.chartPanel.repaint();
		    this.setVisible(true);
		}
		
		public double[] calc(double[] mn)
		{
			
		double[][] imina = model.iMinusAInv;
		// = model.meanVal;
		
		int[] filt = model.filter;
		
		// System.out.println(imina.length+" vs" + mn.length);
		
		double[] temp = Statik.multiply(
				model.iMinusAInv, mn );
//		temp = Statik.multiply(temp,
		//System.out.println(temp);
	/*	for (int i=0; i < temp.length;i++)
		{
			System.out.print(temp[i]+" ");
		}
		*/
		double[] result = new double[filt.length];
		for (int i = 0; i < filt.length; i++)
		{
			result[i] = temp[filt[i]];
		}
		
		return(result);
		}


		@Override
		public void addNode(Node node) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void addEdge(Edge edge) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void swapLatentToManifest(Node node) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void changeName(String name) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void removeEdge(int source, int target, boolean isDoubleHeaded) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void removeNode(int id) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void deleteModel() {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void cycleArrowHeads(Edge edge) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void swapFixed(Edge edge) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void changeStatus(Status status) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void notifyOfConvergedUnitsChanged() {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void setValue(Edge edge) {
		//	System.out.println("UPDATE!");
			updatePoints();
		}


		@Override
		public void notifyOfStartValueChange() {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void changeParameterOnEdge(Edge edge) {
			System.out.println("UPDATE!");
			updatePoints();
			
		}


		@Override
		public void notifyOfWarningOrError(Warning warning) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void newData(int percentMissing, boolean isRawData) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void changeNodeCaption(Node node, String name) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void setDefinitionVariable(Edge edge) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void unsetDefinitionVariable(Edge edge) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void notifyOfClearWarningOrError(Warning warning) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void setGroupingVariable(Node node) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void unsetGroupingVariable(Node node) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void notifyOfFailedReset() {
			// TODO Auto-generated method stub
			
		}



        @Override
        public void addAuxiliaryVariable(String name, int index) {
            // TODO Auto-generated method stub
            
        }


        @Override
        public void addControlVariable(String name, int index) {
            // TODO Auto-generated method stub
            
        }


        @Override
        public void removeAuxiliaryVariable(int index) {
            // TODO Auto-generated method stub
            
        }


        @Override
        public void removeControlVariable(int index) {
            // TODO Auto-generated method stub
            
        }


        @Override
        public void notifyOfStrategyChange(Strategy strategy) {
            // TODO Auto-generated method stub
            
        }


		@Override
		public void addDataset(Dataset dataset, int x, int y) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void addDataset(double[][] dataset, String datasetName, String[] additionalVariableNames, int x, int y) {
			// TODO Auto-generated method stub
			
		}
	
}
