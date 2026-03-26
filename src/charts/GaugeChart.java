package charts;

import java.awt.Color;
import java.awt.event.MouseListener;

import org.knowm.xchart.DialChart;
import org.knowm.xchart.DialChartBuilder;
import org.knowm.xchart.XChartPanel;
import org.knowm.xchart.style.HeatMapStyler;

import engine.Dataset;
import engine.ModelListener;
import engine.ModelRunUnit;
import engine.ParameterReader.FitIndex;
import engine.ModelRun.Status;
import engine.ModelRun.Warning;
import engine.backend.Model.Strategy;
import gui.Desktop;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;

public class GaugeChart extends ChartView implements ModelListener {

	DialChart dc; 
	
	public GaugeChart(Desktop desktop, ModelView modelView) {
		super(desktop, modelView);
		
		this.movable = true;
		this.resizable = true;
		
		this.setSize(800, 400);
		this.setVisible(true);
		this.setBackground(Color.white);
		
		this.addComponentListener(this);
			

		chart = new DialChartBuilder()
	                .width(600)
	                .height(400)
	                .title("Model Fit")
	                .build(); 
		chart.getStyler().setChartBackgroundColor(Color.white);
		
		
		dc = (DialChart)chart;

		dc.getStyler().setLowerColor(Color.green);
		dc.getStyler().setMiddleColor(Color.yellow);
		dc.getStyler().setUpperColor(Color.red);
		dc.getStyler().setLowerFrom(0);
		dc.getStyler().setLowerTo(0.05);
		dc.getStyler().setMiddleFrom(0.05);
		dc.getStyler().setMiddleTo(0.08);
		dc.getStyler().setUpperFrom(0.08);
		dc.getStyler().setUpperTo(1);
		dc.getStyler().setArrowColor(Color.black);
		
		double vals[] = new double[] {0, 0.05, 0.08, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9,1 }; 
		String nams[] = new String[vals.length];
		for (int i=0; i < nams.length; i++) nams[i] = String.valueOf(vals[i]);
		dc.getStyler().setAxisTickValues(vals);
		dc.getStyler().setAxisTickLabels(nams);
		
		cpanel = new XChartPanel<>(chart);
	     
	     MouseListener[] ml = cpanel.getMouseListeners();
	     for (int i=0; i < ml.length; i++)
	    	 cpanel.removeMouseListener(ml[i]);
	     
	     chart.setTitle(modelView.getTitle());
	     
	   //  chart.getStyler().setLegendVisible(true);
		
	     
	    
		update();
		

	     initChartPanel(cpanel);
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
		update();
		
	}

	private void update() {
		
		removeAllSeries(dc);
		
		ModelRunUnit modelRunUnit = (ModelRunUnit) modelView.getShowingEstimate();
		
		if (modelRunUnit == null) return;
		
		double value = modelRunUnit.getFitIndex(FitIndex.RMSEA);
		
		value = Math.min(1, value);
		value = Math.max(0, value);
		
		dc.addSeries("Model Fit", value,	"RMSEA");
		
		this.revalidate();
		this.repaint();
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
