package gui.frames;

import java.awt.event.ActionEvent;

import gui.Desktop;
import gui.dialogs.Dialog;
import gui.graph.Edge;
import gui.graph.EdgeProxy;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class DeveloperControlFrame extends Dialog implements ChangeListener

{

//	private SpinnerNumberModel centerModel;
	//private JSpinner numCenter;

	Desktop desktop;
	private JCheckBox drawLinks;
	
	JSlider edgepad;
	
	public DeveloperControlFrame(Desktop desktop)
	{
		super("Developer");
		
		this.desktop = desktop;
		
		//centerModel = new SpinnerNumberModel(Edge.Curvature,1,500,1);
		//numCenter = new JSpinner(centerModel);
		
		edgepad = new JSlider();
		this.addElement("Edge Padding", edgepad);
		edgepad.setMinimum(0);
		edgepad.setMaximum(20);
		edgepad.addChangeListener(this);
		//numCenter.addChangeListener(this);
		
		drawLinks = new JCheckBox("Draw Links");
		drawLinks.addChangeListener(this);
		
	//	this.addElement("Curvature of covariance paths", numCenter);
		this.addElement("Draw links", drawLinks);
		
		this.pack();
		this.setVisible(true);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void stateChanged(ChangeEvent arg0) {
			Desktop.DRAW_LINKS_ON_DESKTOP = drawLinks.isSelected();
	//		Edge.Curvature = Integer.parseInt(numCenter.getValue().toString());
			
			EdgeProxy.ARROW_PAD = edgepad.getValue();
			desktop.repaint();
	}
	
}
