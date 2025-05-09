/*
* Copyright 2023 by Timo von Oertzen and Andreas M. Brandmaier
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package gui.views;

import importexport.OpenMxExport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import engine.ModelRunUnit;
import engine.ParameterReader;
import engine.Preferences;
import gui.Draggable;
import gui.Utilities;
import gui.fancy.DropShadowBorder;

public class ParameterDrawer extends View implements ViewListener,
		MouseListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3083604016712901308L;
	JLabel panel;
	//private BasicStroke stroke;
	private ParameterReader estimates;
	
	JComponent shownObject;
	
	private JTable table;

	View dockedView;

	boolean stateOpen;
	private final boolean STATE_CLOSED = false;
	private final boolean STATE_OPEN = true;
	private JMenuItem exportResultHtml, exportResultText;
	
	JScrollPane scrollPane;
	
	//boolean with_mi=true;

	public ParameterDrawer(View dockedView) {
		this.dockedView = dockedView;
		this.desktop = dockedView.desktop;
		dockedView.addViewListener(this);


		this.resizable = false;
		this.movable = false;
		this.collides = false;
		this.selectable = false;

		this.setLayout(new BorderLayout());
	
	//	String[] columnNames = {"Name","Estimate","Std. error","Z"};
		
		table = new JTable( new DefaultTableModel() );
		
		((DefaultTableModel)table.getModel()).addColumn("Name");
		((DefaultTableModel)table.getModel()).addColumn("Estimate");
		((DefaultTableModel)table.getModel()).addColumn("Std. error");
		((DefaultTableModel)table.getModel()).addColumn("Z");
	//	if (with_mi)
	//	((DefaultTableModel)table.getModel()).addColumn("MI");

		
		this.setOpaque(false);

		table.addMouseListener(this);
		table.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

		scrollPane = new JScrollPane(table);
		scrollPane.addMouseListener(this);		
		//scrollPane.add(table);			
		table.setFillsViewportHeight(true);
		this.add(scrollPane, BorderLayout.CENTER);
		
		//this.setBorder(BorderFactory.createLineBorder(Color.gray, 1));
		this.setBorder( new DropShadowBorder("",2,Color.gray));

		
//		table = new JTable();
		
	/*	JScrollPane scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);
		
		table.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));		
		scrollPane = new JScrollPane(table);
*/
		
		//this.setLayout(null);

		//this.add(scrollPane, BorderLayout.CENTER);
		//this.add(scrollPane, BorderLayout.CENTER);
		//scrollPane.addMouseListener(this);
		panel = new JLabel("");
		this.add(panel, BorderLayout.NORTH);
		panel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		//panel.setBackground(Color.red);
		changeState(STATE_OPEN );
		changeState(STATE_CLOSED );
		
		/*updateSize();
		updatePosition();
*/
	}

	public void setModelRunUnit(ModelRunUnit modelRunUnit) {

		this.estimates = modelRunUnit;
		
		updateLabel();
	}
	
	public void setParameterReader(ParameterReader modelRunUnit) {

		this.estimates = modelRunUnit;

		updateLabel();

	}
	

	@Override
	public String getToolTipText(MouseEvent event) {
		return estimates.getShortSummary(true);
	}
	
	private void updateEstimates() {
		if (estimates == null) {
			panel.setText("No estimates available");

			DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

			int tr = tableModel.getRowCount();
			for (int i=0; i < tr; i++)
				tableModel.removeRow(0);
			
			tableModel.fireTableDataChanged();
			
			shownObject = panel;
		} else if (estimates instanceof ModelRunUnit) {
			
			ModelRunUnit mru = (ModelRunUnit)(estimates);
			
			panel.setText("<html>Estimation method: "
					+ mru.name+"<br>"+"Fit:"+mru.fit+"</html>");
			
			DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
			
			int tr = tableModel.getRowCount();
			for (int i=0; i < tr; i++)
				tableModel.removeRow(0);
			
			tableModel.fireTableDataChanged();
			
			double[][] cov = mru.getParameterCovariance();
			List<String> parameterNames = estimates.getSortedParameterNames();
			
			//Object[] data = new Object[cov.length][columnNames.length];
			for (int i=0; i < parameterNames.size(); i++)
			{
				Object[] data = new Object[4];
				data[0] =  parameterNames.get(i);
				data[1] = ""+estimates.getParameterValue(parameterNames.get(i));
				
//				System.out.println("Parameter "+data[0]+" added "+i);
				
				int idx = mru.getParameterNameIndex(parameterNames.get(i)); 
				//String covString ="";
				if ((idx != -1) && (cov != null)) {
					//covString = "\u00B1" +(Math.round(Math.sqrt(cov[idx][idx])*1000.0)/1000.0);
					data[2] = Double.toString(Math.sqrt(cov[idx][idx]));
				} else {
				
					data[2] = "NA";
				
				}
				
				if (cov==null) {
					data[3] = "NA";
				} else {
					data[3] = estimates.getParameterValue(parameterNames.get(i))/Math.sqrt(cov[idx][idx]);
				}
				
				////if (with_mi)
				//	data[4] = ((ModelRunUnit) estimates).getModificationIndex(parameterNames.get(i));
				
				tableModel.addRow(data);
			//	System.out.println("Add data"+data[i][0]);
			}
			
	
			
			

			//this.invalidate();
			
			
			tableModel.fireTableDataChanged();
			
			shownObject = scrollPane;
		}

		
		
		this.invalidate();
		this.validate();
		this.repaint();
	}

	private void updateLabel() {

		

		/*if (shownObject != null)
			this.remove(shownObject);
		*/
		if (stateOpen) {
			updateEstimates();
		} else {

		}

	}

	@Deprecated
	private String getParamHTMLString() {
		String params;
		if (estimates == null) {
			params = "No estimates available yet.";
		} else if (estimates instanceof ModelRunUnit){
			
			ModelRunUnit mru = (ModelRunUnit)(estimates);
			
			double[][] cov = mru.getParameterCovariance();
		

					
			params = "<html><h3>Estimates</h3>Estimation method: "
					+ mru.name+"<br>"+"Fit:"+mru.fit+"<br><hr><br>";
			for (String parameterName : estimates.getSortedParameterNames()) {
				
				int idx = mru.getParameterNameIndex(parameterName); 
				String covString ="";
				if (idx != -1) {
					//covString = "\u00B1" +(Math.round(Math.sqrt(cov[idx][idx])*1000.0)/1000.0);
					covString = "\u00B1"+Double.toString(Math.sqrt(cov[idx][idx]));
				}
				
				params += parameterName + ":"
						+ (Math.round(estimates.getParameterValue(parameterName)*1000.0)/1000.0)
//						+ estimates.
						+ covString
						+ "<br>";
			}
			
			params += "<br><hr></html>";

		} else {
			params = "Static parameter object";
		}
		
		return params;
	}
	

	
	@Override
	public String getName()
	{
		if (dockedView != null)
			return "Parameter view of "+dockedView.getName();
		else {
			return "Unconnected parameter view";
		}
	}

	public void updatePosition() {

		int anchorX = dockedView.getX();
		int anchorY = dockedView.getY() + dockedView.getHeight() / 2;

		this.setLocation(anchorX - getWidth(), anchorY - getHeight() / 2);
	}

	/*
	 * @Override public void componentHidden(ComponentEvent arg0) { // TODO
	 * Auto-generated method stub
	 * 
	 * }
	 * 
	 * 
	 * @Override public void componentMoved(ComponentEvent arg0) {
	 * updatePosition();
	 * 
	 * }
	 * 
	 * 
	 * 
	 * @Override public void componentResized(ComponentEvent arg0) {
	 * updatePosition();
	 * 
	 * }
	 * 
	 * 
	 * @Override public void componentShown(ComponentEvent arg0) { // TODO
	 * Auto-generated method stub
	 * 
	 * }
	 */

	@Override
	public void viewMoved(View view) {
		updatePosition();

	}
	
	public void paintBackground(Graphics2D g) {
		((Graphics2D)g).setRenderingHint ( RenderingHints.KEY_ANTIALIASING,
				  RenderingHints.VALUE_ANTIALIAS_ON );
		DropShadowBorder.paintBackgroundInComponent(this, g, this.getBackground());
	}

	@Override
	public void viewResized(View view) {
		updateSize();
		updatePosition();

	}

	@Override
	public void viewIconified(View view, boolean state) {
		// System.out.println("Drawer visible "+state);
		updateLabel();
		updatePosition();
		this.setVisible(!state);

	}

	@Override
	public void startedDrag(Draggable source) {
		// TODO Auto-generated method stub

	}

	@Override
	public void abortDrag() {
		// TODO Auto-generated method stub

	}

	public void mouseClicked(MouseEvent arg0) {

		if (Utilities.isRightMouseButton(arg0)) {
			
			JPopupMenu menu = new JPopupMenu();
			exportResultText = new JMenuItem("Export as Text");
			menu.add(exportResultText);
			exportResultText.addActionListener(this);
			exportResultHtml = new JMenuItem("Export as HTML");
			menu.add(exportResultHtml);
			exportResultHtml.addActionListener(this);
			menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
		}
		
		if (arg0.getClickCount() == 2) {
			//System.out.println("Mouse click"+ !stateOpen);
			changeState(!stateOpen);
			arg0.consume();
			return;
		}

		super.mouseClicked(arg0);

	}

	private void changeState(boolean stateOpen) {

		//System.out.println("Change to"+state);
		

		this.stateOpen = stateOpen;

		if (stateOpen) {
			this.scrollPane.setVisible(true);
			this.panel.setVisible(true);
		} else {
			this.panel.setVisible(false);
			this.scrollPane.setVisible(false);
			this.dockedView.requestFocus();
		}

		updateSize();
		updateLabel();
		updatePosition();

	}

	private void updateSize() {
		if (stateOpen) {
			this.setSize(250, (int)(dockedView.getHeight()*0.75));
			this.setBackground(Color.white);
		} else {
			this.setSize(10, 100);
			//this.setBackground(Color.gray);
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		
		if (arg0.getSource() == exportResultText) 
		{
		    this.getDesktop().saveEstimate(estimates, false);
		}
		
		if (arg0.getSource() == exportResultHtml) 
		{
            this.getDesktop().saveEstimate(estimates, true);
		}
		
	}

	public static void main(String[] args)
	{
		
	}

	public void clear() {
		this.table.removeAll();
		
	}
}
