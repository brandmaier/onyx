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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;

import engine.BootstrappedDataset;
import engine.CovarianceDataset;
import engine.Dataset;
import engine.DatasetChangedListener;
import engine.Preferences;
import engine.RawDataset;
import engine.SimulatedDataset;
import engine.Statik;
import gui.Desktop;
import gui.LabeledInputBox;
import gui.Utilities;
import gui.frames.MainFrame;
import gui.graph.Edge;
import gui.graph.Node;
import gui.linker.LinkException;
import gui.undo.LinkChangedStep;
import gui.undo.LinkStep;
import importexport.CSVExport;

public class DataView extends View implements KeyListener, ActionListener,
		ListSelectionListener, DatasetChangedListener, ComponentListener,
		DocumentListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5641882195166383050L;

	private Dataset dataset;

	private JScrollPane scrollPane;
	private DatasetList list;

	private BasicStroke stroke;

	private DefaultListModel listModel;

	private JMenuItem menuDeleteView;

	private JMenuItem menuCreateModel;

	private JMenuItem menuSaveData;

	private JMenuItem menuIconify;

	private JMenuItem menuBootstrap;
	private JMenuItem menuRebootstrap;
	private JMenuItem menuResimulate;

	private JMenuItem menuCopyToClipboard;

	FontMetrics fm;

	private JMenu menuSendData, menuSendSelectedData;

	JMenuItem[] menuSend;
	JMenuItem[] menuSendSelected;

	int diamondInset = 10;

	private JMenuItem menuSetIdColumn, menuRemoveIdColumn;

	private int clickedY;

	private int clickedX;

	private File file;

	private LabeledInputBox dataNameInput;

	public DataView(Desktop desktop) {
		super(desktop);

		// create list model
		listModel = new DefaultListModel();

		this.addComponentListener(this);

		super.setSelectable(false);

		// set visual appearance
		this.setLayout(null);
		setOpaque(false);
		this.stroke = new BasicStroke(1, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND);

		this.addKeyListener(this);

	}

	public DataView(Desktop desktop, Dataset dataset) {
		this(desktop);
		this.dataset = dataset;
		updateTooltip();

		// create list element
		updateListModel();
		list = new DatasetList(listModel, dataset);
		list.addListSelectionListener(this);
		list.addMouseListener(this);
		scrollPane = new JScrollPane(list);
		list.setOpaque(true);
		scrollPane.setOpaque(true);

		this.list.addKeyListener(this);

		this.add(scrollPane, BorderLayout.CENTER);

		int anzVar = Math.max(dataset.getNumColumns(), 7);
		int height = 50 + anzVar * 15;

		adjustScrollPanesize();
		setSize(minimal_width,
				Math.min(Math.max(height, minimal_height), maximal_height));

		/*
		 * this.setVisible(true); adjustScrollPanesize(); this.validate();
		 */
	}

	public File getFile() {
		return file;
	}

	private void updateListModel() {

		// clear list model
		listModel.removeAllElements();

		if (dataset == null) {
			System.out.println("EMPTY LIST!");
			return;
		}

		for (int i = 0; i < dataset.getNumColumns(); i++) {
			this.listModel.addElement(dataset.getColumnName(i));
		}

		if (list != null) {
			list.setSize(list.getPreferredSize());
		}

	}

	private void updateTooltip() {
		if (this.dataset instanceof RawDataset) {
			this.setToolTipText("<html><h2>" + dataset.getName()
					+ "</h2>This dataset contains "
					+ ((RawDataset) dataset).getNumRows()
					+ " observations and "
					+ ((RawDataset) dataset).getNumColumns() + " variables."
					+ "</html>");
		} else {
			this.setToolTipText("<html><h2>" + dataset.getName()
					+ "</h2>This dataset contains "
					+ ((CovarianceDataset) dataset).getSampleSize()
					+ " observations " + "and "
					+ ((CovarianceDataset) dataset).getNumColumns()
					+ " variables");
		}

	}

	/**
	 * @override
	 */

	public void setIconified(boolean iconify) {
		this.scrollPane.setVisible(!iconify);
		if (iconify) {
			this.remove(scrollPane);
		} else {
			this.add(scrollPane);
			this.adjustScrollPanesize();
		}

		super.setIconified(iconify);
		
		
	}

	public void paintComponent(Graphics g) {
		if (!isIconified())
			super.paintComponent(g);

		/*
		 * if (isIconified()) { return; }
		 */

		int yhalf = this.getHeight() / 2;

		int pad = 5;

		((Graphics2D) g).setStroke(stroke);

		Polygon poly = new Polygon(new int[] { pad, diamondInset,
				getWidth() - diamondInset - pad, getWidth() - pad,
				getWidth() - diamondInset - pad, diamondInset + pad },
				new int[] { pad + yhalf, pad, pad, yhalf + pad,
						getHeight() - pad, getHeight() - pad }, 6);
		// poly.
		g.setColor(Color.white);
		g.fillPolygon(poly);
		g.setColor(Color.gray);
		g.drawPolygon(poly);

		if (fm == null) {
			fm = g.getFontMetrics();
		}

		g.setColor(Color.white);

		String name = dataset.getName();
		int maxw = getWidth() - 2 * diamondInset - 2 * pad - 10;
		int capwidth = fm.stringWidth(name);
		int cut = 4;
		while (capwidth > maxw) {

			name = dataset.getName().substring(0,
					dataset.getName().length() - cut)
					+ "...";
			capwidth = fm.stringWidth(name);
			cut += 1;

		}

		g.fillRect(10 + diamondInset, 0, capwidth + 10, 10);
		g.setColor(Color.black);
		g.drawString(name, 10 + diamondInset + 3, +10);
	}

	@Override
	public void valueChanged(ListSelectionEvent arg0) {
		
		

	}

	private void updateFromPopupMenu(DocumentEvent  arg0) {
		
		try {
			String text = arg0.getDocument()
					.getText(0, arg0.getDocument().getLength());
			
			this.dataset.setName(text);
			this.redraw();
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}

	@Override
	public void datasetChanged() {
		updateListModel();
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO
	}

	@Override
	public void componentResized(ComponentEvent e) {

		adjustScrollPanesize();

	}

	public void keyTyped(KeyEvent arg0) {

		super.keyTyped(arg0);

		/*
		 * if (arg0.getKeyChar()=='e') { DatasetEditorFrame dfe = new
		 * DatasetEditorFrame(this.dataset); dfe.setVisible(true); }
		 */

		if (arg0.getKeyChar() == 'c') {
//			System.out.println("Copy to clipboard");

			int[] ids = this.list.getSelectedIndices();
			boolean[] filter = new boolean[this.getDataset().getNumColumns()];
			for (int id : ids)
				filter[id] = true;

			copyToClipboard(filter);
		}

	}

	public void keyReleased(KeyEvent arg0) {

		// pass on event to super-class and desktop
		super.keyReleased(arg0);
		if (!arg0.isConsumed()) {
			desktop.keyReleased(arg0);
		}
	}

	public void keyPressed(KeyEvent arg0) {

		// pass on event to super-class and desktop
		super.keyPressed(arg0);
		if (!arg0.isConsumed()) {
			desktop.keyPressed(arg0);
		}
	}

	private void adjustScrollPanesize() {

		diamondInset = (int) (this.getWidth() * 0.1); // scale diamond shape,
														// 10% of width

		int pad = diamondInset + 10;

		int x = pad;
		int y = pad;
		int w = Math.max(0, getWidth() - 2 * pad);
		int h = Math.max(0, getHeight() - 2 * pad);

		// if (initalPreferredListDimension == null)
		// initalPreferredListDimension = list.getPreferredSize();
		// list.setPreferredSize(initalPreferredListDimension);
		// list.setSize(initalPreferredListDimension);
		// list.getPreferredSize();
		// list.setPreferredSize(new Dimension(w-20,h));
		// list.setSize(new Dimension(w-20,h));
		// list.setSize(d)

		scrollPane.setPreferredSize(new Dimension(w, h));
		scrollPane.setSize(new Dimension(w, h));
		scrollPane.setLocation(x, y);

		scrollPane.validate();

	}

	@Override
	public void componentShown(ComponentEvent e) {

		adjustScrollPanesize();

	}

	public boolean hasRowsSelected() {
		return (this.list.getSelectedIndices().length > 0);
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

		clickedX = arg0.getX();
		clickedY = arg0.getY();

		super.mouseClicked(arg0);
		if (arg0.isConsumed())
			return;

		if (Utilities.isRightMouseButton(arg0)) {

			JPopupMenu menu = new JPopupMenu();

			if (arg0.getSource() == list) {

				if (menuSetIdColumn == null) {
					menuSetIdColumn = new JMenuItem("Set ID Column");
					menuSetIdColumn.addActionListener(this);
				}
				if (menuRemoveIdColumn == null) {
					menuRemoveIdColumn = new JMenuItem("Remove ID Column");
					menuRemoveIdColumn.addActionListener(this);
				}

				menu.add(menuSetIdColumn);
				if (dataset instanceof RawDataset
						&& ((RawDataset) dataset).hasIdColumn()) {
					menu.add(menuRemoveIdColumn);
				}
				// menu.addSeparator();

			} else {

				dataNameInput = new LabeledInputBox("Data Set Name");
				menu.add(dataNameInput);
				dataNameInput.setText(this.getName());
				dataNameInput.getDocument().addDocumentListener(this);

				if (menuCreateModel == null) {
					menuCreateModel = new JMenuItem("Create Model From Data Set");
					menuCreateModel.addActionListener(this);
				}

				if (menuSaveData == null) {
					menuSaveData = new JMenuItem("Save Data Set");
					menuSaveData.addActionListener(this);
				}

				if (menuDeleteView == null) {
					menuDeleteView = new JMenuItem("Close Data Set");
					menuDeleteView.addActionListener(this);
				}

				if (menuIconify == null) {
					menuIconify = new JMenuItem("Iconify");
					menuIconify.addActionListener(this);
				}

				if (menuCopyToClipboard == null) {
					menuCopyToClipboard = new JMenuItem("Copy To Clipboard");
					menuCopyToClipboard.addActionListener(this);
				}

				if (menuSendData == null) {
					menuSendData = new JMenu("Send Data To Model");
				}
				if (menuSendSelectedData == null) {
					menuSendSelectedData = new JMenu(
							"Send Selected Data To Model");
				}

				menu.add(menuSendData);
				if (hasRowsSelected())
					menu.add(menuSendSelectedData);
				menuSendData.removeAll();
				menuSendSelectedData.removeAll();
				List<ModelView> mvs = desktop.getModelViews();
				menuSend = new JMenuItem[mvs.size()];
				menuSendSelected = new JMenuItem[mvs.size()];
				for (int i = 0; i < mvs.size(); i++) {
					JMenuItem jmi = new JMenuItem(mvs.get(i).getName());
					JMenuItem jmi2 = new JMenuItem(mvs.get(i).getName());
					menuSendData.add(jmi);
					menuSendSelectedData.add(jmi2);
					jmi.addActionListener(this);
					jmi2.addActionListener(this);
					menuSend[i] = jmi;
					menuSendSelected[i] = jmi2;
				}

			
				
				if (menuBootstrap == null) {
					menuBootstrap = new JMenuItem("Bootstrap Data Set");
					menuBootstrap.addActionListener(this);
				}

				if (menuRebootstrap == null) {
					menuRebootstrap = new JMenuItem(
							"Rebootstrap Data Set (in-place)");
					menuRebootstrap.addActionListener(this);
				}

				if (menuResimulate == null) {
					menuResimulate = new JMenuItem(
							"Resimulate Data Set (in-place)");
					menuResimulate.addActionListener(this);
				}

				
				menu.add(menuCreateModel);
				menu.addSeparator();
				menu.add(menuSaveData);
				menu.add(menuCopyToClipboard);

				menu.addSeparator();
				
				int numbs=0;
				if (this.dataset instanceof RawDataset)
					menu.add(menuBootstrap);
					numbs+=1;
				if (this.getDataset() instanceof BootstrappedDataset) {
					menu.add(menuRebootstrap);
					numbs+=1;
				}
				if (this.getDataset() instanceof SimulatedDataset) {
					menu.add(menuResimulate);
					numbs+=1;
				}
				
				if (numbs > 0)
					menu.addSeparator();
				
				menu.add(menuIconify);
				menu.add(menuDeleteView);

			}

			// this problem occured on a MAC 10.7.5 with Java 6. Does not seem
			// to be lethal, though (TODO)
			try {
				menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
			} catch (java.awt.IllegalComponentStateException e) {
				e.printStackTrace();
			}

		}

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		if (menuSetIdColumn == arg0.getSource()
				&& dataset instanceof RawDataset) {
			int idx = this.list.locationToIndex(new Point(clickedX, clickedY));
			((RawDataset) dataset).setIdColumn(idx);
			this.repaint();
		}

		if (menuRemoveIdColumn == arg0.getSource()
				&& dataset instanceof RawDataset) {
			((RawDataset) dataset).removeIdColumn();
			this.repaint();
		}

		if (menuSend != null) {
			for (int i = 0; i < menuSend.length; i++) {
				if (arg0.getSource() == menuSend[i]) {
					ModelView view = desktop.getModelViews().get(i);
					// System.out.println("SEND!");
					sendData(view, false);
				}
			}
		}

		if (menuSendSelected != null) {
			for (int i = 0; i < menuSendSelected.length; i++) {
				if (arg0.getSource() == menuSendSelected[i]) {
					ModelView view = desktop.getModelViews().get(i);
					// System.out.println("SEND!");
					sendData(view, true);
				}
			}
		}

		if (arg0.getSource() == menuBootstrap) {

			if (!(getDataset() instanceof RawDataset)) {
				JOptionPane.showMessageDialog(this,
						"Bootstrap is not possible from Covariance Dataset!");
				return;
			}

			DataView dv = new DataView(this.desktop, new BootstrappedDataset(
					(RawDataset) getDataset()));

			desktop.add(dv);

			dv.invalidate();
			dv.validate();
			dv.repaint();

		}

		if (arg0.getSource() == menuRebootstrap) {

			if (this.getDataset() instanceof BootstrappedDataset) {
				((BootstrappedDataset) this.getDataset()).bootstrap();

				// TODO : notify all connected models
				List<ModelView> mvs = Desktop.getLinkHandler()
						.getAllConnectedModels(this.getDataset());
				for (ModelView mv : mvs) {
					mv.modelChangedEvent();
				}
			} else {
				JOptionPane.showMessageDialog(this,
						"Rebootstrapping is not possible!");
			}
		}

		if (arg0.getSource() == menuResimulate) {

			if (this.getDataset() instanceof SimulatedDataset) {
				((SimulatedDataset) this.getDataset()).simulate();

				// TODO : notify all connected models
				List<ModelView> mvs = Desktop.getLinkHandler()
						.getAllConnectedModels(this.getDataset());
				for (ModelView mv : mvs) {
					mv.modelChangedEvent();
				}
			} else {
				JOptionPane.showMessageDialog(this,
						"Rebootstrapping is not possible!");
			}
		}

		if (arg0.getSource() == menuSaveData) {
			saveData();
		}

		if (arg0.getSource() == menuCopyToClipboard) {
			copyToClipboard();
		}

		if (arg0.getSource() == menuCreateModel) {
			ModelView mv = new ModelView(desktop);

			// TvO, 05 JAN 13: I outsourced the variable creation from the drop
			// command and used this method here to avoid double implementation.
			ArrayList<Integer> allIndices = new ArrayList<Integer>(
					dataset.getNumColumns());
			for (int i = 0; i < dataset.getNumColumns(); i++)
				allIndices.add(i);
			try {
				mv.addVariablesFromDataset(dataset, allIndices, 50, 200);
			} catch (LinkException e) {
				e.printStackTrace(System.out);
			}

			/*
			 * for (int i=0; i < dataset.getNumColumns(); i++) { int y = 100;
			 * int x = 50 +i*90; Node node = new Node(i, x,y); node.setCaption(
			 * dataset.getColumnName(i)); node.setIsLatent(false);
			 * 
			 * 
			 * mv.getModelRequestInterface().requestAddNode(node);
			 * 
			 * Edge edge = new Edge(node, node, true);
			 * edge.setParameterName("variance"+(i+1)); edge.setFixed(false);
			 * mv.getModelRequestInterface().requestAddEdge(edge);
			 * 
			 * //TODO: this is error prone! MRI might change id in the mean
			 * time!?
			 * 
			 * // link this node to variable // node.setConnected(true); try {
			 * desktop.getLinkHandler().link(dataset, i, mv.getGraph(),
			 * mv.getModelRequestInterface(), node ); } catch (LinkException le)
			 * { le.printStackTrace(); }
			 * 
			 * mv.modelChangedEvent(); }
			 */

			desktop.add(mv);

		}

		if (arg0.getSource() == menuDeleteView) {
            
		    // TvO 11 JUN 14: To remedy a null pointer in the unlink, I put this line in front of the remove commands
		    Desktop.getLinkHandler().unlink(dataset);

            desktop.remove(this);
			desktop.removeView(this);

		}

		if (arg0.getSource() == menuIconify) {
			super.setIconified(!this.isIconified());
		}

	}

	private void copyToClipboard() {
		boolean[] filter = new boolean[this.getDataset().getNumColumns()];
		for (int i = 0; i < filter.length; i++)
			filter[i] = true;
		copyToClipboard(filter);
	}

	private void copyToClipboard(boolean[] filter) {
		Clipboard clipboard = getToolkit().getSystemClipboard();

		if (dataset instanceof RawDataset) {

			String[] colNames = dataset.getColumnNames();
			String header = "";
			for (int i = 0; i < colNames.length; i++)
				if (filter[i])
					header += colNames[i]
							+ (i == colNames.length - 1 ? "" : "\t");

			double[][] matrix = ((RawDataset) dataset).getData();
			int stellen = 5;
			String data = "";
			for (int i = 0; i < matrix.length; i++) {
				for (int j = 0; j < matrix[0].length; j++)
					if (filter[j])
						data += Statik.doubleNStellen(matrix[i][j], stellen)
								+ "\t";
				data += "\n";
			}

			StringSelection outgoing = new StringSelection(header + "\n" + data);
			clipboard.setContents(outgoing, outgoing);

		} else if (dataset instanceof CovarianceDataset) {
			// TODO : NOT IMPLEMENTED YET
		}

	}

	/**
	 * iterates through all nodes of a model and all columns of the dataset and
	 * connects matching pairs
	 * 
	 * @param view
	 */
	public void sendData(ModelView view, boolean selectedOnly) {

		MainFrame.undoStack.startCollectSteps();

		// iterate over all nodes to find matches
		Iterator<Node> iterNode = view.getGraph().getNodeIterator();
		while (iterNode.hasNext()) {
			Node node = iterNode.next();
			for (int i = 0; i < dataset.getNumColumns(); i++) {
				if (selectedOnly && !list.isSelectedIndex(i))
					continue;

				if (node.getCaption().equals(dataset.getColumnName(i))) {
					// System.out.println("Connect "+node.getCaption());
					try {
						MainFrame.undoStack.add(new LinkStep(node
								.getObservedVariableContainer(), view
								.getModelRequestInterface()));
						
						Desktop.getLinkHandler().link(dataset, i,
								node.getObservedVariableContainer(),
								view.getModelRequestInterface());
					} catch (LinkException e) {
						System.out.println("Tried to connect a latent variable!");
					}
				}
			}
		}

		// iterate over all group variables
		if (dataset instanceof RawDataset)
		{
			for (Node node : view.getGraph().getNodes())
			{
				for (int i = 0; i < dataset.getNumColumns(); i++) {
					if (node.isGrouping() && node.groupName.equals(dataset.getColumnName(i))) {
					    try {
					        Desktop.getLinkHandler().link(dataset, i, node.getGroupingVariableContainer(), view.getModelRequestInterface());
					    } catch (Exception e) {System.err.println("Error in linking group variable: "+e);}
						node.setGroupingVariable((RawDataset)dataset, i);
					}
				}
			}
		}
		
		// iterate over all definition variables
		if (dataset instanceof RawDataset)
			for (Edge edge : view.getGraph().getEdges()) {
				for (int i = 0; i < dataset.getNumColumns(); i++) {
					if (!edge.isDefinitionVariable())
						continue;

					if (edge.getParameterName()
							.equals(dataset.getColumnName(i))) {
						
						MainFrame.undoStack.add(new LinkStep(edge
								.getDefinitionVariableContainer(), view
								.getModelRequestInterface()));
						try {
							Desktop.getLinkHandler().link(dataset, i,
									edge.getDefinitionVariableContainer(),
									view.getModelRequestInterface());
						} catch (LinkException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}

		MainFrame.undoStack.endCollectSteps();

		view.modelChangedEvent();

		view.redraw();
	}

	public Dataset getDataset() {
		return dataset;
	}

	public String getName() {
		if (dataset == null)
			return "(unknown)";
		return dataset.getName();
	}

	public void saveData() {
		try {

			File dir = new File(
					(String) Preferences.getAsString("DefaultWorkingPath"));

			final JFileChooser fc = new JFileChooser(dir);

			// fc.setFileFilter();
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);

			// In response to a button click:
			int returnVal = fc.showSaveDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {

				file = fc.getSelectedFile();

				Preferences.set("DefaultWorkingPath", file.getParentFile()
						.getAbsolutePath());

				String[] defaultExtensions = new String[] { "csv" };
				boolean hasExtension = false;
				for (String ext : defaultExtensions) {
					if (file.getName().endsWith("." + ext))
						hasExtension = true;
				}

				if (!hasExtension)
					file = new File(file.getAbsolutePath() + "."
							+ defaultExtensions[0]);

				System.out.println(file.toString());

				boolean ok = true;
				if (file.exists()) {
					int result = JOptionPane
							.showConfirmDialog(
									this,
									"Warning: File exists!",
									"A file with the selected name already exists. Do you want to overwrite the existing file?",
									
									/*"File exists!",
									"The selected file exists already. Do you want to overwrite the existing file?",*/
									JOptionPane.OK_CANCEL_OPTION,
									JOptionPane.WARNING_MESSAGE, null);
					if (result == JOptionPane.CANCEL_OPTION) {
						ok = false;
						file = null;
					}
				}

				if (ok) {

					CSVExport csvExport = new CSVExport(dataset);
					csvExport.export(file);
					
					desktop.mainFrame.addToRecentFiles(file);

				}

			} else {
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "An error has occured!");
		}
	}

	public void highlight(int columnId) {
		if (columnId == -1) {
			this.list.clearSelection();
		}
		this.list.setSelectedIndex(columnId);

	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		updateFromPopupMenu(arg0);

	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		
		updateFromPopupMenu(arg0);

	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		updateFromPopupMenu(arg0);
	}

}
