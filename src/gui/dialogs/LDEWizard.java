package gui.dialogs;

import engine.ModelRequestInterface;
import gui.Desktop;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class LDEWizard extends Dialog implements ChangeListener {

	
	Desktop desktop;
	
	JSpinner numObsInput;
	JTextArea nameObsInput;

	private JTextArea nameErrInput;

	private JTextArea nameIceptInput;

	private JTextArea nameSlopeInput;

	private JCheckBox uniqueResiduals, latentCovariance;

	private SpinnerNumberModel centerModel;

	private JSpinner tauSpinner;

	private JTextArea timeElapInput;

	private SpinnerNumberModel embedModel;

	private JSpinner numEmbedding;
	
	public LDEWizard(Desktop desktop)
	{
		super("Latent Differential Equation Wizard");
		this.desktop = desktop;
	
		Dimension d = new Dimension(150,30);
		
		// # of observations
		 numObsInput = new JSpinner(new SpinnerNumberModel(5,2,50,1));
		 numObsInput.addChangeListener(this);
		this.addElement("Observed time points (Embedding Dimension)",numObsInput);
		
		centerModel = new SpinnerNumberModel(1,1,10,1);
		tauSpinner = new JSpinner(centerModel);
		this.addElement("Tau (Time-delay)  ", tauSpinner);
		
		/*embedModel = new SpinnerNumberModel(1,1,20,1);
		numEmbedding = new JSpinner(embedModel);
		this.addElement("Embedding dimension  ", numEmbedding);
*/
		// observation name
		timeElapInput = new JTextArea("x");
		timeElapInput.setSize(d);
		timeElapInput.setText(".3");
		this.addElement("Time elapsed",timeElapInput);
	
		
		// observation name
		nameObsInput = new JTextArea("x");
		nameObsInput.setSize(d);
		this.addElement("Name of observed variable",nameObsInput);
		
		// error term name
		nameErrInput = new JTextArea("e");
		nameErrInput.setSize(d);
		this.addElement("Name of residual variance term ",nameErrInput);
		
		// icept - slope correlation
		/*nameSlopeInput = new JTextArea("slope");
		nameSlopeInput.setSize(d);
		this.addElement("Name of slope term ",nameSlopeInput);
		nameIceptInput = new JTextArea("icept");
		nameIceptInput.setSize(d);
		this.addElement("Name of intercept term ",nameIceptInput);
*/
		uniqueResiduals = new JCheckBox("unique variances across time");
		this.addElement("Residual variances", uniqueResiduals);
	/*	
		latentCovariance = new JCheckBox("estimate covariance");
		this.addElement("covariance between icept and slope", latentCovariance);
		*/
		
		this.addSendButton("Create");
		
		this.pack();
		
		this.setVisible(true);
	
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		ModelView mv = new ModelView(desktop);
		desktop.add(mv);
		
		ModelRequestInterface model = mv.getModelRequestInterface();
		
		int numObs = Integer.parseInt((String) numObsInput.getValue().toString());

		double[] timepoints = new double[numObs];
		for (int i=0; i < numObs; i++)
		{
			timepoints[i] = i / (numObs-1.0);
		}
		
		int xOffset = 70;
		int yOffset = 100;
		
		int N = timepoints.length;
		
		// create latent nodes
		
		Node l1 = new Node("x");
		l1.setPosition(xOffset,yOffset+70);
		model.requestAddNode(l1);
		Node l2 = new Node("x'");
		l2.setPosition(xOffset+200, yOffset+10);
		model.requestAddNode(l2);
		Node l3 = new Node("x''");
		l3.setPosition(xOffset+400, yOffset+70);
		model.requestAddNode(l3);		
		
	
		Edge e = new Edge(l1, l2);
		e.setDoubleHeaded(true);
		e.setFixed(false);
		e.setParameterName("Cxx'");
		model.requestAddEdge(e);

		e = new Edge(l2, l3);
		e.setDoubleHeaded(false);
		e.setFixed(false);
		e.setParameterName("$\\zeta");
		model.requestAddEdge(e);
		
		e = new Edge(l1, l3);
		e.setDoubleHeaded(false);
		e.setFixed(false);
		e.setParameterName("$\\eta");
		model.requestAddEdge(e);
		
		
		Node[] obs = new Node[N];
		for (int i=0; i < N; i++) {
			obs[i] = new Node(nameObsInput.getText()+(i+1));
			obs[i].setIsLatent(false);
			obs[i].setPosition(xOffset+i*80, yOffset+200);
			model.requestAddNode(obs[i]);
			//obs[i].setConnected(true); // workaround
		}
		
		//int centerAt = Integer.parseInt(tauSpinner.getValue().toString())-1;
		
		double tau = Double.parseDouble(tauSpinner.getValue().toString());
		double deltaT = Double.parseDouble(timeElapInput.getText().toString());

		
		double mn = 0; 
		for (int i=0; i < N; i++) {
				mn+= (i)*tau*deltaT;
		}

		mn = mn/N;
		
		// Loadings
		for (int i=0; i < N; i++) {
			
			// L1 loadings
			Edge edge1 = new Edge(l1, obs[i],false);
			model.requestAddEdge(edge1);
			
			// L2 loadings
			double l2v = i*tau*deltaT - mn;
			
			l2v = Math.round(l2v*10000)/10000.0;
			
			Edge edge2 = new Edge(l2, obs[i],false);
			edge2.setValue( l2v);
			edge2.edgeLabelRelativePosition = ((i+1)/(float)N);
			model.requestAddEdge(edge2);
			
			// L3 loadings
			double l3v = 0.5*l2v*l2v;
			
			l3v = Math.round(l3v*10000)/10000.0;

			
			Edge edge3 = new Edge(l3, obs[i],false);
			edge3.setValue( l3v);
			edge3.edgeLabelRelativePosition = ((i+1)/(float)N);
			model.requestAddEdge(edge3);
			
		}
		
		// residual variances
		for (int i=0; i < N; i++) {
			Edge edge = new Edge(obs[i], obs[i],true);
			edge.setValue(1.0);
			if (uniqueResiduals.isSelected())
				edge.setParameterName(nameErrInput.getText()+(i+1));
			else
				edge.setParameterName(nameErrInput.getText());
			edge.setAutomaticNaming(false);
			edge.setFixed(false);
			model.requestAddEdge(edge);
		}
		
		// latent variances
		Edge edgeI = new Edge(l1, l1, true);
		//edgeI.setValue(sigmaI);
		edgeI.setFixed(false);
		//edgeI.edgeStyle = Edge.EdgeStyle.ALWAYS_LABEL;
		edgeI.setParameterName("Vx");
		edgeI.setAutomaticNaming(false);
		edgeI.setFixed(false);
		model.requestAddEdge(edgeI);
		
		Edge edgeS = new Edge(l2, l2, true);
		edgeS.setValue(1.0);
		edgeS.setParameterName("Vx'");
		edgeS.setAutomaticNaming(false);
		edgeS.setFixed(false);
		model.requestAddEdge(edgeS);
		
		Edge edgeL3 = new Edge(l3, l3, true);
		edgeL3.setValue(1.0);
		edgeL3.setParameterName("Ve");
		edgeL3.setAutomaticNaming(false);
		edgeL3.setFixed(false);
		model.requestAddEdge(edgeL3);
		
		// add mean
		
	/*	Node mean = new Node();
		mean.setPosition(xOffset+100, yOffset-60);
		mean.setTriangle(true);
		model.requestAddNode(mean);
		
		Edge edgeMi = new Edge(mean, l1);
		edgeMi.setFixed(false);
		model.requestAddEdge(edgeMi);
		
		Edge edgeMs = new Edge(mean, l2);
		edgeMs.setFixed(false);
		model.requestAddEdge(edgeMs);
		*/
	
	/*	Node slopeNode = new Node();
		Node iceptNode = new Node();
		mv.getModelRequestInterface().requestAddNode(iceptNode);
		mv.getModelRequestInterface().requestAddNode(slopeNode);

		iceptNode = 
		
		int x = 0;
		for (int i = 0; i <numObs; i++)
		{
			Node obsNode = new Node();
			mv.getModelRequestInterface().requestAddNode(obsNode);
			Node errNode = new Node();
			mv.getModelRequestInterface().requestAddNode(errNode);
			
			// add paths
			Edge edge1 = new Edge(iceptNode, obsNode);
			Edge edge2 = new Edge(slopeNode, obsNode);
			Edge edge3 = new Edge(errNode, obsNode);
			
			mv.getModelRequestInterface().requestAddEdge(edge1);
			mv.getModelRequestInterface().requestAddEdge(edge2);
			mv.getModelRequestInterface().requestAddEdge(edge3);
		}*/
	
		this.dispose();
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
		
		if (arg0.getSource() == numObsInput)
		{
			int max =  Integer.parseInt(numObsInput.getValue().toString());
			centerModel.setMaximum( max );
			if (Integer.parseInt(tauSpinner.getValue().toString()) > max) {
				tauSpinner.setValue(max);
			}
		}
		
	}
	
	
	
	
	
}
