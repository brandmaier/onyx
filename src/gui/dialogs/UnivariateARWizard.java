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

public class UnivariateARWizard extends Dialog {

	Desktop desktop;
	private JSpinner numObsInput;
	private JTextArea nameObsInput;
	private JTextArea nameErrInput;
	private JCheckBox uniqueResiduals;
	
	public UnivariateARWizard(Desktop desktop)
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
			Node err = new Node("residual"+i);
			model.requestAddNode(err);
			err.setPosition(xOffset+i*xDist, yOffset+200);
			
			// add latent process
			lat[i] = new Node("latent"+i);
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
				Edge edge3 = new Edge(lat[i-1],lat[i]);
				edge3.setDoubleHeaded(false);
				edge3.setFixed(false);
				edge3.setAutomaticNaming(false);
				edge3.setParameterName("\\beta");
				model.requestAddEdge(edge3);
				
			}
			
			Edge edge4 = new Edge(lat[i],lat[i]);
			edge4.setDoubleHeaded(true);
			model.requestAddEdge(edge4);
			//obs[i].setConnected(true); // workaround
		}
		
		this.dispose();
	}
	
	
}
