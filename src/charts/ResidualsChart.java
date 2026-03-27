package charts;

import java.awt.Color;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import javax.swing.JFrame;

import org.knowm.xchart.HeatMapChart;
import org.knowm.xchart.HeatMapChartBuilder;
import org.knowm.xchart.HeatMapSeries;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.style.HeatMapStyler;

import engine.Dataset;
import engine.ModelListener;
import engine.ModelRun.Status;
import engine.ModelRun.Warning;
import engine.ModelRunUnit;
import engine.backend.Model.Strategy;
import gui.Desktop;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.DataView;
import gui.views.ModelView;

public class ResidualsChart extends ChartView implements ModelListener {

	public ResidualsChart(Desktop desktop, ModelView modelView) {
		super(desktop, modelView);
		
		updateTitle("Residuals");
		
		this.movable = true;
		this.resizable = true;
		
		this.setSize(800, 400);
		this.setVisible(true);
		this.setBackground(Color.white);
		
		this.addComponentListener(this);
			

		chart = new HeatMapChartBuilder()
	                .width(600)
	                .height(400)
	                .title("Residuals plot")
	                .xAxisTitle("Observed Variables")
	                .yAxisTitle("Observed Variables")
	                .build(); 
		chart.getStyler().setChartBackgroundColor(Color.white);
		
		
		
	     cpanel = new XChartPanel<>(chart);
	     
	     MouseListener[] ml = cpanel.getMouseListeners();
	     for (int i=0; i < ml.length; i++)
	    	 cpanel.removeMouseListener(ml[i]);
	     
	     chart.setTitle(modelView.getTitle());
	     
	     chart.getStyler().setLegendVisible(true);
		
	     
	     
	     Color[] rangeColors = new Color[] {Color.white, Color.black};
	     ((HeatMapStyler)chart.getStyler()).setRangeColors(rangeColors);
	     Function<Double, String> formatter = d -> String.format("%.3f", d);
//		((HeatMapStyler)chart.getStyler()).setHeatMapDecimalValueFormatter(formatter );
		((HeatMapStyler)chart.getStyler()).setHeatMapValueDecimalPattern("#.###");
		((HeatMapStyler)chart.getStyler()).setShowValue(true);
		((HeatMapStyler)chart.getStyler()).setMax(1);
//	     chart.getStyler().setvisi
		update();
		

	     initChartPanel(cpanel);
	}
	
	List<Number[]>  data;

	private void update() {
		
		//super.removeAllSeries(chart);
		
		ModelRunUnit modelRunUnit = (ModelRunUnit) modelView.getShowingEstimate();
		
		if (modelRunUnit==null) {
			cpanel.setVisible(false);
			return;
		} else {
			cpanel.setVisible(true);
		}
		double[][] residuals = modelRunUnit.getStandardizedResiduals();
		
	        
		String[] variableNames = modelView.getModelRequestInterface().getModel().getObservedVariableNames();
		List<String> varNames = new ArrayList<String>();
		for (int i=0; i < variableNames.length; i++) varNames.add(variableNames[i]);
		
		  data = new ArrayList<>();
		  for (int i=0; i < residuals.length; i++) {
			
		
			  for (int j=0; j < residuals.length; j++) {
				  Number[] row =  new Number[3];
				  row[0] = i;
				  row[1] = j;
				  row[2] = Math.abs( residuals[i][j] );
				  data.add( row );
				 // System.out.println("Residuals :"+residuals[i][j]);
			  }
			  
		  }

		HeatMapChart hmc = (HeatMapChart)chart;
		
		
	//	int[] ydata;
//		int[] xdata;
	//	if (hmc.getHeatMapSeries() == null)
		try {
			hmc.addSeries("Name", varNames, varNames, data);
		} catch (Exception e) {
			hmc.updateSeries("Name", varNames, varNames, data);
		}

		
		
	     this.revalidate();
	   	  this.repaint();
	}
	
	private static List<List<Number>> toNumberMatrix(double[][] matrix) {
	    List<List<Number>> result = new ArrayList<>();
	    for (double[] row : matrix) {
	        List<Number> rowList = new ArrayList<>();
	        for (double v : row) {
	            rowList.add(v);
	        }
	        result.add(rowList);
	    }
	    return result;
	}
	
	public static void main(String[] args) {
		
		JFrame frame = new JFrame();
		frame.setSize(400,400);
		frame.setVisible(true);
		
		
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
		//if (status == Status.RESULTSVALID)
			update();
		

	}

	@Override
	public void notifyOfConvergedUnitsChanged() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setValue(Edge edge) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void notifyOfStartValueChange() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void changeParameterOnEdge(Edge edge) {
		// TODO Auto-generated method stub
		
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
	public void addDataset(Dataset dataset, int x, int y) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addDataset(double[][] dataset, String datasetName, String[] additionalVariableNames, int x, int y) {
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

}
