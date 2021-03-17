package gui.dialogs;

import engine.ModelRequestInterface;
import gui.Desktop;
import gui.graph.Edge;
import gui.graph.Node;
import gui.views.ModelView;

import java.awt.Dimension;
import java.awt.event.ActionEvent;

import javax.swing.JCheckBox;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;

public class UnivariateApproxARWizard extends Dialog {

	Desktop desktop;
	private JSpinner numObsInput;
	private JSpinner approxOrderInput;
	private JTextArea nameObsInput;
	private JTextArea timeDelayInput;
	private JTextArea nameErrInput;
	private JCheckBox uniqueResiduals;
	

	public UnivariateApproxARWizard(Desktop desktop)
	{
		super("Univariate Autoregression Wizard");
		this.desktop = desktop;
	
		Dimension d = new Dimension(150,30);
		
		// # of observations
		 numObsInput = new JSpinner(new SpinnerNumberModel(4,2,100,1));
		this.addElement("Observed time points",numObsInput);
		// observation name
		nameObsInput = new JTextArea("x");
		nameObsInput.setSize(d);
		this.addElement("Name of observed variable",nameObsInput);
		
		timeDelayInput = new JTextArea("1");
		this.addElement("Time Delay", timeDelayInput);
		
		approxOrderInput = new JSpinner(new SpinnerNumberModel(2, 1, 2, 1));
		this.addElement("Approximation Order", approxOrderInput);
		
		// error term name
		nameErrInput = new JTextArea("e");
		nameErrInput.setSize(d);
		this.addElement("Name of residual variance term ",nameErrInput);
		
		

		uniqueResiduals = new JCheckBox("unique variances across time");
		this.addElement("Residual variances", uniqueResiduals);
		
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
		
		double timedelay = Double.parseDouble(timeDelayInput.getText());

		int approxOrder=Integer.parseInt((String) approxOrderInput.getValue().toString());;
		
		
		int xOffset = 70;
		int yOffset = 100;
		
		int xDist = 120;
		
		Node[] obs = new Node[numObs];
		Node[] lat = new Node[numObs];
		for (int i=0; i < numObs; i++) {
			obs[i] = new Node(nameObsInput.getText()+i);
			obs[i].setIsLatent(false);
			obs[i].setPosition(xOffset+i*xDist, yOffset+120);
			model.requestAddNode(obs[i]);
			
			// add measurement part
			Node err = new Node("residual");
			model.requestAddNode(err);
			err.setPosition(xOffset+i*xDist, yOffset+200);
			
			// add latent process
			lat[i] = new Node("latent");
			lat[i].setPosition(xOffset+i*xDist, yOffset+30);
			model.requestAddNode(lat[i]);
			
			Edge edge0 = new Edge(lat[i],obs[i]);
			model.requestAddEdge(edge0);
			
			Edge edge1 = new Edge(err, obs[i]);
			edge1.setDoubleHeaded(false);
			Edge edge2 = new Edge(err,err);
			edge2.setDoubleHeaded(true);
			edge2.setFixed(false);
			edge2.setParameterName("\\epsilon");
			edge2.setAutomaticNaming(false);
			
			model.requestAddEdge(edge1);
			model.requestAddEdge(edge2);
			
			// add autoregression
			if (i > 0) {
				
				Node temp2 = new Node("order1"+i);
				temp2.setPosition(xOffset+i*xDist -20, yOffset+30- 30);
				model.requestAddNode(temp2);
				
				Edge edge3 = new Edge(lat[i-1],temp2);
				edge3.setDoubleHeaded(false);
				edge3.setFixed(false);
				edge3.setAutomaticNaming(false);
				edge3.setParameterName("\\beta");
				model.requestAddEdge(edge3);
				
				
				
				Edge edge4 = new Edge(temp2, lat[i]);
				edge4.setDoubleHeaded(false);
				edge4.setFixed(true);
				edge4.setValue(timedelay);
				//edge4.setAutomaticNaming(false);
				//edge4.setParameterName("\\beta");
				model.requestAddEdge(edge4);
				
				if (approxOrder > 1) {
					Node tempn = new Node("order2a_"+i);
					tempn.setPosition(xOffset+i*xDist - xDist, yOffset+30- 60);
					Node tempx = new Node("order2b_"+i);
					tempx.setPosition(xOffset+i*xDist, yOffset+30- 60);
					
					model.requestAddNode(tempn);
					model.requestAddNode(tempx);
					
					Edge e5 = new Edge(lat[i-1], tempn);
					model.requestAddEdge(e5);
					e5.setFixed(false);
					e5.setParameterName("\\beta");
					
					Edge e6 = new Edge(tempn,tempx);
					model.requestAddEdge(e6);
					e6.setFixed(false);
					e6.setParameterName("\\beta");
					
					Edge e7 = new Edge(tempx, lat[i]);
					model.requestAddEdge(e7);
					e7.setFixed(true);
					e7.setValue(0.5*timedelay*timedelay);
				}
			}
			
			Edge edge4 = new Edge(lat[i],lat[i]);
			edge4.setDoubleHeaded(true);
			model.requestAddEdge(edge4);
			//obs[i].setConnected(true); // workaround
		}
		
		this.dispose();
	}
	
	
}
