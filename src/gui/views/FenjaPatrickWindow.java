package gui.views;

import importexport.OpenMxExport;
import machineLearning.clustering.Clustering;
import machineLearning.clustering.ClusteringDistribution;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
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
import javax.swing.table.TableModel;

import engine.ModelRunUnit;
import engine.ParameterReader;
import engine.Preferences;
import gui.Draggable;
import gui.Utilities;
import gui.fancy.DropShadowBorder;

public class FenjaPatrickWindow extends View implements ViewListener,
		MouseListener, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3083604016712901308L;
	JLabel panel;
	//private BasicStroke stroke;
	private ParameterReader estimates;

	private ClusteringDistribution clusteringDistribution;
	
		
	
	JComponent shownObject;
	
	private JTable table;

	View dockedView;

	boolean stateOpen;
	private final boolean STATE_CLOSED = false;
	private final boolean STATE_OPEN = true;
	private JMenuItem exportResultHtml, exportResultText;
	
	JScrollPane scrollPane;
	
	//boolean with_mi=true;

	public FenjaPatrickWindow(View dockedView) {
		this.dockedView = dockedView;
		this.desktop = dockedView.desktop;
		dockedView.addViewListener(this);
		
		//am Anfang haben wir noch kein Clustering, wenn das DP Clustering durchgeführt wurde,
		//muss die dort ermittelte fullDistribution in diese Variable rein (siehe Datei dirichletProcess.DirichletProcess.java)
		clusteringDistribution = null; 
		
		clusteringDistribution = new ClusteringDistribution();
		clusteringDistribution.addSample(new Clustering(new int [] {0,0,0,0,1,1,2,2}), 5);
		clusteringDistribution.addSample(new Clustering(new int [] {0,0,0,2,0,1,1,1}), 4);
		clusteringDistribution.addSample(new Clustering(new int [] {0,1,1,2,1,1,0,0}), 3);
		clusteringDistribution.addSample(new Clustering(new int [] {0,0,2,2,2,0,0,1}), 2);
	
		this.resizable = false;
		this.movable = false;
		this.collides = false;
		this.selectable = false;

		this.setLayout(new BorderLayout());
	
	//	String[] columnNames = {"Name","Estimate","Std. error","Z"};
		
		table = new JTable( new DefaultTableModel() );
		
		
		/*((DefaultTableModel)table.getModel()).addColumn("Name");
		((DefaultTableModel)table.getModel()).addColumn("Estimate");
		((DefaultTableModel)table.getModel()).addColumn("Std. error");
		((DefaultTableModel)table.getModel()).addColumn("Z");
		
		Delete Columns and replaced with a forLoop */
		
		DefaultTableModel model = (DefaultTableModel)table.getModel();
			  		
		model.addColumn("Name");
		model.addColumn("Default Data");
		
		
		
		Clustering mean = clusteringDistribution.getMean();
		for(int i = 1; i <= clusteringDistribution.getMean().getAnzCluster(); i++)
			model.addColumn("Cluster" + i); 

		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
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

			// we want to present the mean clustering
			// delete "No estimates available"
			
			DefaultTableModel tableModel = (DefaultTableModel) table.getModel();
			
			int tr = tableModel.getRowCount();
			for (int i=0; i < tr; i++)
				tableModel.removeRow(0);
 						
			// @Patrick und Fenja: hier allgemein die Anzahl der beobachteten Variablen und deren Varianzen ermitteln
			// am besten Timo fragen woher man die Liste mit den Namen der beobachteten Variablen bekommt
			
			String[] name = new String[3];
			name [0]= "Var_1";
			name [1]= "Var_2";
			name [2]= "Co_Var";
			
			double[] defaultData = new double [3];
			defaultData[0] = 1.254;
			defaultData[1] = 2.688;
			defaultData[2] = 4.122;
			
			////////////////////////////////////////////////////////////////////////////////
			
			// hier wird geprüft ob schon ein DP Clustering Result vorliegt
			
			int numOfClusters = -1;
			double[][] result;
			if(clusteringDistribution != null) { //check for DP Clustering results
				numOfClusters = clusteringDistribution.getMean().getAnzCluster(); //get the number of clusters here
				panel.setText("Dirichlet Clustering: Mean Partition\n Cluster: " + numOfClusters);
				if(tableModel.getColumnCount() < 3) {
					//adds a column for every cluster
					for(int i = 1; i <= numOfClusters; i++)
						tableModel.addColumn("Cluster" + i);
				}
				//hier jetzt die results über die clusteringDistribution berechnen.
				//Also für jede Variable und jede ClusterID 
				//die Werte entsprechenden Werte aus den Datenpunkten in eine Varianzberechnung einfließen lassen
			} else {
				panel.setText("Dirichlet Clustering: Mean Partition\n Cluster: -");
			}
			
			//diese Zeilen sind erstmal nur dummies damit die nachfolgende Befüllung der Tabelle funktioniert
			result = new double[3][3];			
			result[0][0] = 7.356;
			result[0][1] = 9.648;
			result[0][2] = 4.548;
			result[1][0] = 10.321;
			result[1][1] = 22.357;
			result[1][2] = 3.245;
			result[2][0] = 15.755;
			result[2][1] = 60.259;
			result[2][2] = 12.486;

			
			////////////////////////////////////////////////////////////////////////////////
			
			// Add numbers and names to table
			for(int i = 1; i <= result.length; i++) {			
				tableModel.addRow(new Object[]{""});
				tableModel.setValueAt(name[i-1], i-1, 0);
				tableModel.setValueAt(defaultData [i-1], i-1, 1);
				for(int j = 1; j <= numOfClusters; j++) {
					tableModel.setValueAt(result[i-1][j-1], i-1, j+1);
				}
			}			
			
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

		int anchorX = dockedView.getX() + dockedView.getWidth() + this.getWidth();
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
			this.setSize(500, (int)(dockedView.getHeight()*0.75));
			// setSize changed from 250 to 500
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
