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
package gui;

import engine.CovarianceDataset;
import engine.Dataset;
import engine.OnyxModel;
import engine.ParameterReader;
import engine.Preferences;
import engine.RawDataset;
import engine.ModelRun.Priority;
import engine.backend.Model;
import gui.actions.CreateApproxUARAction;
import gui.actions.CreateDCSMAction;
import gui.actions.CreateEmptyModelAction;
import gui.actions.CreateLDEAction;
import gui.actions.CreateLGCMAction;
import gui.actions.CreateMeasurementInvarianceAction;
import gui.actions.CreateSingleFactorModelAction;
import gui.actions.CreateUARAction;
import gui.actions.DesktopPasteAction;
import gui.actions.LoadAction;
import gui.frames.DeveloperControlFrame;
import gui.frames.MainFrame;
import gui.graph.Edge;
import gui.graph.Graph;
import gui.graph.Node;
import gui.graph.VariableContainer;
import gui.linker.DatasetField;
import gui.linker.LinkException;
import gui.linker.LinkHandler;
import gui.tutorial.TutorialView;
import gui.undo.ViewCreateStep;
import gui.views.DataView;
import gui.views.ModelView;
import gui.views.View;
import gui.views.ViewConnection;
import gui.views.ViewContainer;
import gui.views.ViewListener;
import gui.views.WorkspaceLoader;
import importexport.OpenMxExport;
import importexport.OpenMxImport;
import importexport.SPSSImport;
import importexport.SaveZIP;
import importexport.LavaanImport;
import importexport.OnyxModelRestoreXMLHandler;
import importexport.XMLExport;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import javax.swing.JFileChooser;
import javax.swing.JLayeredPane;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

import javax.swing.JPopupMenu;
import javax.swing.RepaintManager;
import javax.swing.ToolTipManager;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import bayes.Chain;
import bayes.ParameterSet;
import scc.Tree;

public class Desktop extends JLayeredPane
		implements KeyListener, MouseListener, MouseMotionListener, ViewListener, ViewContainer, ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1357590227323235281L;
	public static boolean DRAW_LINKS_ON_DESKTOP = false;
	private List<View> views;

	private final static LinkHandler linkHandler = new LinkHandler();; // handles links between data and model
	private Stroke traceStroke;
	private int mouseDragY;
	private int mouseDragX;

	private List<DesktopListener> desktopListeners = new ArrayList<DesktopListener>();

	private List<ViewConnection> viewConnections;

	public List<ViewConnection> getViewConnections() {
		return viewConnections;
	}

	public View dragSource;

	private View activeView;
	public MainFrame mainFrame;

	private JMenuItem closeAll;
	private JMenuItem removeLRedge;
	private ViewConnection contextLRedge;
	private JMenuItem[] loadTutorialX;
	private Image bgimg;
	private JMenuItem menuSaveWorkspace;

	public static LinkHandler getLinkHandler() {
		return linkHandler;
	}

	public Desktop() {
		this(null);
	}

	public Desktop(MainFrame mainFrame) {
		super();
		this.mainFrame = mainFrame;
		// store views
		views = new ArrayList<View>();

		this.setSize(new Dimension(800, 600));
		// this.setBackground( new Color(170,176,191)); // Andys background
		this.setBackground(Color.white);

		try{
			updateBackgroundImage();
		} catch (Exception e) {}
		
		// this.setOpaque(true);
		this.setVisible(true);

		// kill layout manager
		this.setLayout(null);

		// set trace stroke
		traceStroke = new BasicStroke(2, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		// View Connections
		viewConnections = new ArrayList<ViewConnection>();

		// enable Tooltips, empty default
		this.setToolTipText("");

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		this.addKeyListener(this);
		this.setBackground(Color.white);
		this.setOpaque(true);

		DesktopTransferHandler dth = new DesktopTransferHandler(this);
		this.setTransferHandler(dth);
	}

	public void updateBackgroundImage() {
		// bg image
		bgimg = null;
		try {
			if (Preferences.get("BackgroundImage") != "") {

				String img = (String) Preferences.get("BackgroundImage");
				if (img.startsWith("#")) {
					img = img.substring(1);
					Integer img_nr = Integer.parseInt(img);
					URL url = this.getClass().getResource("/images/backgrounds/dts" + String.valueOf(img_nr) + ".jpg");
					bgimg = new ImageIcon(url).getImage();
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("Could not set background image!");
		}

		this.invalidate();
		this.repaint();
	}

	/**
	 * Return a unique model name that does not match any of the existing model
	 * names. This is achieved by adding numbers to the proposed model name.
	 * 
	 * @param proposedName
	 * @return unique model name
	 */
	public String getUniqueModelName(String proposedName) {
		int i = getNumberForUnnamedModel(proposedName);
		if (i <= 1) {
			return proposedName;
		} else {
			return proposedName + i;
		}
	}

	public int getNumberForUnnamedModel() {
		return getNumberForUnnamedModel(OnyxModel.defaultName);
	}

	public int getNumberForUnnamedModel(String prefix) {
		// collect all indices of unnamed models
		Vector<Integer> taken = new Vector<Integer>();
		for (View view : views) {
			if (!(view instanceof ModelView))
				continue;

			// ModelView modelView = (ModelView)view;
			if (view.getName().startsWith(prefix)) {
				String postfix = view.getName().substring(prefix.length(), view.getName().length()).trim();
				try {

					Integer i = Integer.parseInt(postfix);
					if (i > 0)
						taken.add(i);
				} catch (Exception e) {
					taken.add(1);
				}
			}
		}

		if (taken.size() == 0)
			return 1;

		// find a free spot, start counting at "2"
		java.util.Collections.sort(taken);

		for (int i = 0; i < taken.size(); i++) {
			if (!taken.contains(i + 1))
				return (i + 1);
		}
		return taken.size() + 1;

	}

	public void add(View view) {
		// MainFrame.logger.log("started adding "+view.getName());

		if (views.contains(view)) {
			System.err.println("View " + view.getName() + " has already been added!");
			return;
		}

		// @undo anchor
		MainFrame.undoStack.add(new ViewCreateStep(view));

		super.add(view);
		this.views.add(view);

		view.addViewListener(this);
		this.reorganizeDesktop();

		view.addFocusListener(view);

		view.requestFocus();

		for (DesktopListener dl : desktopListeners)
			dl.viewAdded();

		while (true) {
			boolean uniquePos = true;

			// is position unique ?
			for (View other : views) {
				if (other != view && other.getX() == view.getX() && other.getY() == view.getY()) {
					uniquePos = false;
				}
			}

			// if not unique then
			if (!uniquePos) {
				view.setX(view.getX() + 5);
				view.setY(view.getY() + 5);
			}
			// break?
			if (uniquePos) {
				break;
			}
		}

		super.moveToFront(view);

		// if (view instanceof TutorialView) this.setComponentZOrder(view, 1000);
		// Tutorial always on toP!
		for (View v : views) {
			if (v instanceof TutorialView)
				super.moveToFront(v);
		}

		this.validate();
		this.repaint();
	}

	public void saveAllUnsaved() {
		for (View v : views) {
			if (v instanceof ModelView) {
				ModelView mv = (ModelView) v;
				if (mv.isUnsavedChanges()) {
					XMLExport export = new XMLExport(mv);
					if (mv.getFile() != null)
						export.export(mv.getFile());
					else
						export.export();
				}
			}
		}
	}

	private void reorganizeDesktop() {

		// check whether more space is needed, if yes: resize desktop
		int maxX = 0, maxY = 0;

		Iterator<View> iterView = views.iterator();
		while (iterView.hasNext()) {
			View view = iterView.next();
			maxX = Math.max(maxX, view.getLocation().x + view.getWidth());
			maxY = Math.max(maxY, view.getLocation().y + view.getHeight());
		}

		if ((maxX > getWidth()) | (maxY > getHeight())) {
			this.setSize(maxX, maxY);
			this.setPreferredSize(new Dimension(maxX, maxY));
			this.revalidate(); // this let's the scrollpane now that we updated the size

			// this.scrollRectToVisible(aRect);
		}

		// start the layout manager again!
		// if (layoutManager.isSleeping())
		// layoutManager.interrupt();
	}

	public void removeView(View view) {
		// --- remove view from AWT/SWING
		this.remove(view);
		this.views.remove(view);

		// --- call destructor
		view.dispose();

		// --- remove all elements in Desktop that pertain to this view

		// delete all view connections
		Iterator<ViewConnection> it = viewConnections.iterator();
		while (it.hasNext()) {
			ViewConnection vc = it.next();
			if (vc.getFrom() == view || vc.getTo() == view)
				it.remove();

		}

		// --- redraw
		this.validate();
		this.repaint();
	}

	public List<View> getViews() {
		return this.views;
	}

	/*
	 * public void paint(Graphics g) { System.out.println("Paint NOW");
	 * super.paint(g); }
	 */
	/*
	 * public void paintChildren(Graphics g) { Graphics2D g2d = (Graphics2D)g;
	 * g2d.setRenderingHint ( RenderingHints.KEY_ANTIALIASING,
	 * RenderingHints.VALUE_ANTIALIAS_ON );
	 * 
	 * //g.drawLine(0, 0, 100,100); super.paintChildren(g2d); }
	 */

	/*
	 * private boolean hasOpenDataViews() { for (int i=0; i < views.size(); i++) {
	 * if (views.get(i) instanceof DataView) { DataView dataView =
	 * (DataView)views.get(i); if (!dataView.isIconified()) return true; } }
	 * return(false); }
	 */

	/**
	 * 
	 * clones a given ModelView and clones the containing Graph and underlying Model
	 *
	 * 
	 * @param mv
	 */
	public ModelView cloneModelView(ModelView mv) {
		MainFrame.undoStack.startCollectSteps();

		ModelView newMv = new ModelView(this);

		newMv.getModelRequestInterface().setRunPriority(Priority.HOLD);

		this.add(newMv);
		newMv.setSize(mv.getSize());
		newMv.setLocation(10, 10);
		// this.reorganizeDesktop();

		newMv.setAtomicOperationInProgress(true);

		newMv.setName("Clone of " + mv.getName());

		Graph graph = mv.getGraph();
		Graph newGraph = newMv.getGraph();

		Iterator<Node> iterNode = graph.getNodeIterator();
		while (iterNode.hasNext()) {
			Node node = (iterNode.next());
			Node copyNode = (Node) node.clone();
			copyNode.setSelected(false);
			copyNode.setConnected(false);
			newMv.getModelRequestInterface().requestAddNode(copyNode);

			// relink nodes
			if (Desktop.getLinkHandler().isLinked(node.getObservedVariableContainer())) {
				DatasetField datasetField = Desktop.getLinkHandler()
						.getDatasetField(node.getObservedVariableContainer());
				try {
					if (datasetField != null)
						Desktop.getLinkHandler().link(datasetField.dataset, datasetField.columnId,
								copyNode.getObservedVariableContainer(), newMv.getModelRequestInterface());
					else {
						// copyNode.setConnected(false);
						System.err.println("Could not esablish dataset link on node " + node);
					}
				} catch (LinkException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			} else {
				// System.out.println("Node is not linked in graph"+node);
			}
		}

		Iterator<Edge> iterEdge = graph.getEdgeIterator();
		while (iterEdge.hasNext()) {
			Edge edge = iterEdge.next();

			// update edge references to new nodes
			edge = (Edge) edge.clone();
			edge.source = newGraph.getNodeById(edge.source.getId());
			edge.target = newGraph.getNodeById(edge.target.getId());

			newMv.getModelRequestInterface().requestAddEdge(edge);
		}

		newMv.setAtomicOperationInProgress(false);

		this.repaint();

		MainFrame.undoStack.endCollectSteps();

		newMv.getModelRequestInterface().setRunPriority(Priority.NORMAL);

		return (newMv);

	}

	public void paintComponent(Graphics g) {
		RepaintManager rm = RepaintManager.currentManager(this);
		/*
		 * if (hasOpenDataViews()) { rm.markCompletelyDirty(this); }
		 */

		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
		g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

		// paint a background
		if (bgimg != null) {
			double imgratio = bgimg.getHeight(null) / (double) bgimg.getWidth(null);
			double frratio = this.getHeight() / (double) this.getWidth();
			if (imgratio <= frratio) {
				g2d.drawImage(bgimg, 0, 0, (int) (this.getHeight() / imgratio), (int) this.getHeight(), null);

			} else {
				g2d.drawImage(bgimg, 0, 0, (int) this.getWidth(), (int) (this.getWidth() * imgratio), null);
			}
		}

		// paint desired View locations for debugging reasons
		if (MainFrame.LAYOUT_DEBUGGING) {
			g2d.setColor(Color.red);
			for (View view : views) {
				g2d.drawRect(view.getDesiredX(), view.getDesiredY(), view.getWidth(), view.getHeight());
				if (view.getName() != null)
					g2d.drawString(view.getName() + " " + view.getDesiredX() + "," + view.getDesiredY(),
							view.getDesiredX(), view.getDesiredY());

			}
			g2d.setColor(Color.black);
		}
		// paint all Views

		super.paintComponent(g2d);

		// draw links between datasets and nodes
		if (DRAW_LINKS_ON_DESKTOP) {
			g.setColor(new Color(220, 220, 220));
			for (int i = 0; i < views.size(); i++) {
				if (views.get(i) instanceof DataView) {
					DataView dataView = (DataView) views.get(i);

					if (dataView.isIconified())
						continue;

					for (int j = 0; j < dataView.getDataset().getNumColumns(); j++) {

						List<VariableContainer> graphFields = linkHandler.getVariableContainer(dataView.getDataset(),
								j);

						if (graphFields != null) {

							for (VariableContainer graphField : graphFields) {
								ModelView view = graphField.getGraph().getParentView();

								if (view.isIconified())
									continue;

								int fromX = dataView.getX() + dataView.getWidth() / 2;
								int fromY = dataView.getY() + dataView.getHeight() / 2;

								if (graphField.getParent() instanceof Node) {

									Node node = (Node) graphField.getParent();

									int toX = view.getX() + node.getXCenter();
									int toY = view.getY() + node.getYCenter();

									if (toX > view.getX() + view.getWidth())
										continue;
									if (fromX > view.getY() + view.getHeight())
										continue;

									g2d.setStroke(traceStroke);

									// TODO draw links
									g2d.drawLine(fromX, fromY, toX, toY);
								}

//	    				 System.out.println(fromX+","+fromX+"->"+toX+","+toY);

							}
						}
					}
				}
			}

		}

		// draw links between connected Views

		Iterator<ViewConnection> iterConn = viewConnections.iterator();
		while (iterConn.hasNext()) {
			ViewConnection conn = iterConn.next();
			conn.paint(g, rm, this);
		}

		// draw currently dragging connection
		if (dragSource != null && mouseDragX != -1 && mouseDragY != -1)
			g2d.drawLine(dragSource.getCenter().x, dragSource.getCenter().y, mouseDragX, mouseDragY);

		/*
		 * if (dragSource != null) { System.out.println(mouseDragX+" "+ mouseDragY+" "+
		 * dragSource.getX()+" "+ dragSource.getY() ); g2d.drawLine(mouseDragX,
		 * mouseDragY, dragSource.getX(), dragSource.getY()); }
		 */

		// paint edge
		/*
		 * if (mouseDragX != -1) { g2d.setColor(Color.black);
		 * g2d.drawLine(mousePressedX, mousePressedY, mouseDragX, mouseDragY); }
		 */

	}

	@Override
	public String getToolTipText(MouseEvent event) {

		Iterator<ViewConnection> iterViewConnections = viewConnections.iterator();
		while (iterViewConnections.hasNext()) {
			ViewConnection vc = iterViewConnections.next();
			if (event.getPoint().distance(vc.getCenter()) < 8) {
				return (vc.getModelComparisonString());
			}
		}

		return null;
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

		// MainFrame.logger.log("mouse clicked"+arg0);

		if (Utilities.isLeftMouseButton(arg0)) {

			Iterator<ViewConnection> iterViewConnections = viewConnections.iterator();
			while (iterViewConnections.hasNext()) {
				ViewConnection vc = iterViewConnections.next();
				if (arg0.getPoint().distance(vc.getCenter()) < 8) {
					/*
					 * System.out.println(vc.getModelComparisonString()); Action toolTipAction =
					 * this.getActionMap().get("postTip"); ActionEvent postTip = new
					 * ActionEvent(this, ActionEvent.ACTION_PERFORMED, "");
					 * toolTipAction.actionPerformed( postTip );
					 */
					// PopupFactory.getSharedInstance().getPopup(paramComponent1, paramComponent2,
					// paramInt1, paramInt2)
					int prevDelay = ToolTipManager.sharedInstance().getInitialDelay();
					ToolTipManager.sharedInstance().setInitialDelay(0);
					// ToolTipManager.sharedInstance().setInitialDelay(1000);
					ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(this, 0, 0, 0,
							// 0, 0, // X-Y of the mouse for the tool tip
							vc.getCenter().x, vc.getCenter().y, 0, false));
					ToolTipManager.sharedInstance().setInitialDelay(prevDelay);
				}
			}

		}

		// Invoke Default action
		if (Utilities.isLeftMouseButton(arg0) && arg0.getClickCount() == 2) {

			MainFrame.undoStack.startCollectSteps();
			ModelView mv = new ModelView(this);
			MainFrame.undoStack.endCollectSteps();

			mv.setLocation(arg0.getX(), arg0.getY());

			this.add(mv);
			this.validate();
			this.repaint();
		}

		if (Utilities.isRightMouseButton(arg0)) {

			// open context menu for desktop
			JPopupMenu menu = new JPopupMenu();

			// was any of the LR connections clicked?
			for (ViewConnection conn : viewConnections) {
				if (conn.isOnLabel(arg0.getX(), arg0.getY())) {
					removeLRedge = new JMenuItem("Remove model comparison");
					removeLRedge.addActionListener(this);
					contextLRedge = conn;
					menu.add(removeLRedge);
					menu.addSeparator();
				}
			}

			JMenu create = new JMenu("Create new model");
			menu.add(create);

			create.add(new CreateEmptyModelAction(this, arg0.getX(), arg0.getY()));
			create.add(new CreateLGCMAction(this, arg0.getX(), arg0.getY()));
			create.add(new CreateUARAction(this, arg0.getX(), arg0.getY()));
			if (MainFrame.DEVMODE) {
				create.add(new CreateApproxUARAction(this, arg0.getX(), arg0.getY()));
			}
			create.add(new CreateSingleFactorModelAction(this, arg0.getX(), arg0.getY()));
			create.add(new CreateDCSMAction(this, arg0.getX(), arg0.getY()));
			create.add(new CreateLDEAction(this, arg0.getX(), arg0.getY()));
			create.add(new CreateMeasurementInvarianceAction(this, arg0.getX(), arg0.getY()));

			JMenu loadTutorial = new JMenu("Load tutorial data");

			loadTutorialX = new JMenuItem[MainFrame.tutorialNum];
			for (int i = 0; i < MainFrame.tutorialNum; i++) {
				loadTutorialX[i] = new JMenuItem(MainFrame.tutorialMenunames[i]);
				loadTutorial.add(loadTutorialX[i]);
				loadTutorialX[i].addActionListener(this);
			}

			menu.add(new LoadAction(this, null, null, arg0.getX(), arg0.getY()));
			menu.add(loadTutorial);
			// menu.add(new LoadDataAction(this,arg0.getX(),arg0.getY()));

			menu.add(mainFrame.getLoadRecent(arg0.getX(), arg0.getY()));

			menu.add(new DesktopPasteAction(this, arg0.getX(), arg0.getY()));

			if (menuSaveWorkspace == null) {
				menuSaveWorkspace = new JMenuItem("Save workspace");
				menuSaveWorkspace.addActionListener(this);
			}
			menu.addSeparator();
			menu.add(menuSaveWorkspace);
			menu.addSeparator();

			if (closeAll == null) {
				closeAll = new JMenuItem("Close all panels");
				closeAll.addActionListener(this);
			}

			menu.add(closeAll);

			menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
		}

	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		//mousePressedX = arg0.getX();
		//mousePressedY = arg0.getY();

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

		// System.out.println("Mouse released");

		//currentDragSource = null;

		mouseDragX = -1;
		mouseDragY = -1;

	//	mousePressedX = arg0.getX();
	//	mousePressedY = arg0.getY();

		if (dragSource != null) {
			this.dragSource = null;
			repaint();
		}

	}

	@Override
	public void viewMoved(View view) {
		reorganizeDesktop();

		this.repaint();
	}

	@Override
	public void startedDrag(Draggable source) {

	//	currentDragSource = source;
		// System.out.println("Desktop notified of drag action!");
	}

	@Override
	public void abortDrag() {
		//currentDragSource = null;
	}

	public void load() {
		load(0, 0);
	}

	public void load(int x, int y) {
		// open file dialog

		final JFileChooser fc;

		File dir = new File((String) Preferences.getAsString("DefaultWorkingPath"));
		fc = new JFileChooser(dir);

		// In response to a button click:
		int returnVal = fc.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();

			Preferences.set("DefaultWorkingPath", file.getParentFile().getAbsolutePath());

			try {
				// loadData(file, file.getName(), x,y);

				// importFromBuffer( new BufferedReader(new FileReader(file)), file,
				// file.getName(),x,y);
				importFromFile(file, file.getName(), x, y);

				if (mainFrame != null)
					mainFrame.addToRecentFiles(file);

			} catch (FileNotFoundException e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, e.getMessage());
			}
		}
	}

	/*
	 * @SuppressWarnings("unchecked")
	 * 
	 * @Deprecated public static Vector loadDataMatrix(InputStream istream, char
	 * delimiter, boolean repeatedDelimiter) { return loadDataMatrix(new
	 * BufferedReader(new InputStreamReader(istream)), delimiter,
	 * repeatedDelimiter); }
	 */
	/**
	 * 
	 * modified from Timos Statik.java
	 * 
	 * @param istream
	 * @param delimiter
	 * @param repeatedDelimiter
	 * @return
	 */
	/*
	 * @SuppressWarnings("unchecked")
	 * 
	 * @Deprecated public static Vector loadDataMatrix(BufferedReader input, char
	 * delimiter, boolean repeatedDelimiter) { Vector erg = new Vector();
	 * 
	 * try { String line = ""; while (input.ready() && line != null) { line =
	 * Statik.loescheRandWhitespaces(input.readLine()); if (line != null) { while
	 * ((line.length()==0) && (input.ready())) line =
	 * Statik.loescheRandWhitespaces(input.readLine()); if (line.length()>0) {
	 * Vector vline = new Vector(); String[] content = line.split(""+delimiter); for
	 * (int i=0; i<content.length; i++) if ((repeatedDelimiter==false) ||
	 * (content[i].length()>0)) vline.addElement(content[i]); erg.addElement(vline);
	 * } } } input.close(); } catch (Exception e)
	 * {System.out.println("Error reading from file."); return null;}
	 * 
	 * return erg; }
	 */
	public DataView loadData(File file, String name, int x, int y) throws FileLoadingException {
		try {
			return loadData(new FileInputStream(file), name, x, y);
		} catch (Exception e) {
			throw new FileLoadingException(e.getMessage());
		}
	}

	public DataView loadData(String string, String name, int x, int y) {
		try {
			return loadData(new FileInputStream(string), name, x, y);
		} catch (Exception e) {
			System.out.println("Error reading from file: " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public DataView loadData(InputStream file, String name) throws FileLoadingException {
		return loadData(file, name, 0, 0);
	}

	public DataView loadData(InputStream file, String name, int x, int y) throws FileLoadingException {
		return loadData(new BufferedReader(new InputStreamReader(file)), name, x, y);
	}

	public DataView loadData(BufferedReader file, String name, int x, int y) throws FileLoadingException {
		return initiateDataView(file, name, x, y);
	}

	public DataView initiateDataView(String table, String name) {
		return initiateDataView(table, name, 0, 0);
	}

	public DataView initiateDataView(String table, String name, int x, int y) {
		// create new object and view and add them to desktop
		Dataset dataset = null;
		try {
			dataset = RawDataset.createDatasetFromString(table, name);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.toString());
			e.printStackTrace();
			return null;
		} // new RawDataset(table, name);

		return initiateDataView(dataset, x, y);
	}

	public DataView initiateDataView(Dataset dataset, int x, int y) {
		DataView dv = new DataView(this, dataset);

		dv.setLocation(x, y);
		this.add(dv);
		dv.invalidate();
		dv.validate();
		dv.repaint();

		return dv;
	}

	public DataView initiateDataView(BufferedReader table, String name) {
		return initiateDataView(table, name, 0, 0);
	}

	public DataView initiateDataView(BufferedReader table, String name, int x, int y) {
		// create new object and view and add them to desktop
		Dataset dataset = null;
		try {
			dataset = RawDataset.createDatasetFromReader(table, name);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, e.toString());
			e.printStackTrace();
			return null;
		}

		DataView dv = new DataView(this, dataset);
		dv.setLocation(x, y);
		this.add(dv);
		dv.invalidate();
		dv.validate();
		dv.repaint();

		return dv;
	}

	@Deprecated // should use initiateDataView(String table, String name)
	public DataView loadData(Vector<Vector<String>> vec, String name) throws FileLoadingException {
		return loadData(vec, name, 0, 0);
	}

	@Deprecated // should use initiateDataView(String table, String name, int x, int y)
	public DataView loadData(Vector<Vector<String>> vec, String name, int x, int y) throws FileLoadingException {

		// detect if we have a header and set column names accordingly
		Vector<String> firstRow = vec.get(0);
		String str = "";
		for (int i = 0; i < firstRow.size(); i++)
			str += firstRow.get(i);
		boolean firstRowIsHeader = false;
//			if (str.contains("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ#")) {
		// System.out.println(str);
		if (Pattern.matches(".*\\p{Alpha}*.*", str)) {
			firstRowIsHeader = true;
		}
		List<String> columnNames = new ArrayList<String>();
		if (firstRowIsHeader) {
			for (int i = 0; i < firstRow.size(); i++) {
				System.out.println("First row" + firstRow.get(i).trim() + "*");
				columnNames.add(firstRow.get(i).trim());
			}
		} else {
			for (int i = 0; i < firstRow.size(); i++) {
				columnNames.add("Col. " + i);
			}
		}

		// create data object
		int numColumns = firstRow.size();
		int numRows = vec.size() - (firstRowIsHeader ? 1 : 0);
		double data[][] = new double[numRows][numColumns];

		for (int i = 0; i < numRows; i++) {
			int rowi = i + (firstRowIsHeader ? 1 : 0);
			Vector<String> vrow = vec.get(rowi);

			if (vrow.size() != firstRow.size()) {
				throw new FileLoadingException("Inconsistent column sizes in row " + rowi + ". Header has "
						+ firstRow.size() + " columns and row " + rowi + " has " + vrow.size() + " columns");
			}

			for (int j = 0; j < numColumns; j++) {
				try {
					data[i][j] = Double.parseDouble(vrow.get(j));
				} catch (Exception e) {
					data[i][j] = Model.MISSING;
				}
			}
		}

		// create new object and view and add them to desktop
		RawDataset dataset = new RawDataset(data, columnNames); // Dataset.createRandomDataset(10, 10);

		dataset.setName(name);

		DataView dv = new DataView(this, dataset);
		dv.setLocation(x, y);
		this.add(dv);
		// this.add(dv);
		// dv.invalidate();dv.repaint();
		// this.redraw();
		dv.invalidate();
		dv.validate();
		dv.repaint();

		return dv;
	}

	public DataView createDataView(double[][] data, List<String> columnNames) {
		RawDataset dataset = new RawDataset(data, columnNames); // Dataset.createRandomDataset(10, 10);
		DataView dv = new DataView(this, dataset);
		this.add(dv);
		// this.add(dv);
		// dv.invalidate();dv.repaint();
		this.validate();
		this.repaint();

		return dv;
	}

	public void loadModel() {
		loadModel(0, 0);
	}

	public void loadModel(int x, int y) {
		File dir = new File((String) Preferences.getAsString("DefaultWorkingPath"));

		final JFileChooser fc = new JFileChooser(dir);

		// In response to a button click:
		int returnVal = fc.showOpenDialog(null);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			Preferences.set("DefaultWorkingPath", file.getParentFile().getAbsolutePath());

			loadModel(file, x, y);
		}
	}

	public ModelView loadModel(String string) {
		return loadModel(string, 0, 0);
	}

	public ModelView loadModel(String string, int x, int y) {
		return loadModel(new InputSource(new StringReader(string)), x, y);
	}

	public ModelView loadModel(File file) {
		return loadModel(file, 0, 0);
	}

	public ModelView loadModel(File file, int x, int y) {

		if (file == null || !file.exists())
			return null;
		if (mainFrame != null)
			mainFrame.addToRecentFiles(file);

		if (file.getAbsolutePath().toLowerCase().endsWith(".r")) {
			OpenMxImport omx = new OpenMxImport(this);
			ModelView mv = omx.loadModel(file);
			mv.setLocation(x, y);
			makeUniqueModelName(mv);
			return mv;
		} else {

			String is = "";
			try {
				is = Utilities.readFileContentsUTF8(file);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ModelView mv = loadModel(is, x, y);
			mv.setFile(file);
			mv.setUnsavedChanges(false);
			makeUniqueModelName(mv);
			return mv;
		}

		// return null;
	}

	public void makeUniqueModelName(ModelView mv) {
		String proposedName = mv.getName();
		if (!getUniqueModelName(proposedName).equals(proposedName)) {
			mv.getModelRequestInterface().requestChangeModelName("");
			mv.getModelRequestInterface().requestChangeModelName(getUniqueModelName(proposedName));
		}
	}

	public ModelView loadModel(InputSource inputSource) {
		return loadModel(inputSource, 0, 0);
	}

	public ModelView loadModel(InputSource inputSource, int x, int y) {

		if (inputSource == null)
			return null;

		MainFrame.undoStack.startCollectSteps();
		ModelView mv = new ModelView(this);
		MainFrame.undoStack.endCollectSteps();

		// load model

		try {

			XMLReader xmlReader = XMLReaderFactory.createXMLReader();

			xmlReader.setContentHandler(new OnyxModelRestoreXMLHandler(mv));

			// parser.parse(file);
			mv.setAtomicOperationInProgress(true);
			xmlReader.parse(inputSource);
			mv.setAtomicOperationInProgress(false);

		} catch (SAXParseException e) {
			System.err.println("Fehler beim Parsen!");
			e.printStackTrace();

			Reader rs = inputSource.getCharacterStream();

			BufferedReader br = new BufferedReader(rs);
			String text;
			try {
				text = br.readLine();
			} catch (IOException e1) {

				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			// catch (SAXParseException e) {

			// JOptionPane.showMessageDialog(null, "An error occured during the exection of
			// your R script: "+output);
			// }

			JOptionPane.showMessageDialog(this,
					"The model could not be loaded. The file could not be parsed successfully:\n"
							+ inputSource.toString(),
					"Import/Load Error", JOptionPane.ERROR_MESSAGE);

			mv.dispose();

			return (null);
		} catch (SAXException e) {
			JOptionPane.showMessageDialog(this,
					"The model could not be loaded. The file could not be parsed successfully.", "Import/Load Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			mv.dispose();
			return (null);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "The file could not be loaded.", "Import/Load Error",
					JOptionPane.ERROR_MESSAGE);
			e.printStackTrace();
			mv.dispose();
			return (null);

		}

		mv.setLocation(x, y);
		this.add(mv);

		// some post-processing checks

		// TvO: This warning seems to pop up whenever a .xml file is dragged onto the
		// desktop; I disable it for now
		// TODO repair
		/*
		 * boolean warn=false; for (Node node : mv.getGraph().getNodes()) { if
		 * (node.getX() < 0 || node.getY() < 0 || node.getX() > mv.getWidth() ||
		 * mv.getY() > mv.getHeight()) warn=true; } if (warn)
		 * JOptionPane.showMessageDialog(null,
		 * "Warning! One or more variables may be outside the visible area!","Warning",
		 * JOptionPane.WARNING_MESSAGE);
		 */

		return (mv);
	}

	public DataView loadData(File file) throws FileLoadingException {
		return loadData(file, 0, 0);
	}

	public DataView loadData(File file, int x, int y) throws FileLoadingException {

		if (file == null)
			return null;

		if (mainFrame != null)
			mainFrame.addToRecentFiles(file);
		try {
			return loadData(new FileInputStream(file), file.getName(), x, y);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public void mouseDragged(MouseEvent arg0) {

	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// mouseX = arg0.getX();
		// mouseY = arg0.getY();
	}

	/**
	 * returns a list of all views of type ModelView that are on the desktop
	 * 
	 * @return
	 */
	public List<ModelView> getModelViews() {
		List<ModelView> list = new ArrayList<ModelView>();
		for (int i = 0; i < getViews().size(); i++) {
			if (getViews().get(i) instanceof ModelView) {
				list.add((ModelView) getViews().get(i));
			}
		}
		return list;
	}

	/**
	 * returns a list of all views of type DataView that are on the desktop
	 * 
	 * @return
	 */
	public List<DataView> getDataViews() {
		List<DataView> list = new ArrayList<DataView>();
		for (int i = 0; i < getViews().size(); i++) {
			if (getViews().get(i) instanceof DataView) {
				list.add((DataView) getViews().get(i));
			}
		}
		return list;
	}

	@Override
	public void viewIconified(View view, boolean state) {}

	public void mouseReleasedOnModelView(ModelView modelView, MouseEvent arg0) {

		// System.out.println("RELEASE");

		int x = arg0.getPoint().x + modelView.getX(); // - dragSource.getX();
		int y = arg0.getPoint().y + modelView.getY(); // - dragSource.getY();
		/*
		 * System.out.println(""); System.out.println(arg0.getPoint()+
		 * " on desktop "+x+","+y);
		 */

		View dragTarget = null;
		Iterator<View> iterView = this.views.iterator();
		while (iterView.hasNext()) {
			View view = iterView.next();
			// System.out.println("Try "+view+":"+view.getX()+","+view.getY());
			if (view.isWithin(x, y)) {
				dragTarget = view;
				// System.out.println("*");
				break;
			}
		}

		if ((dragTarget instanceof ModelView)) {

			// create a new connection
			if ((Utilities.isRightMouseButton(arg0)) && (dragTarget != this.dragSource) && (dragTarget != null)
					&& (dragSource != null)) {
				ViewConnection conn = new ViewConnection(dragSource, dragTarget);
				ViewConnection conn_reverse = new ViewConnection(dragTarget, dragSource);
				if (!viewConnections.contains(conn) && !viewConnections.contains(conn_reverse)) {
					this.viewConnections.add(conn);
					conn.initiateModelComparison();
					this.repaint();
					// System.out.println("Established new connection!");
					// System.out.println("Released\n"+dragSource+"\n"+dragTarget);
				}
			}

		}

		if (dragSource != null) {
			this.dragSource = null;
			repaint();
		}

	}

	public void mousePressedOnModelView(ModelView modelView, MouseEvent arg0) {

		if (Utilities.isRightMouseButton(arg0)) {
			// if (modelView instanceof ModelView)
			this.dragSource = modelView;
			mouseDragX = -1;
			mouseDragY = -1;
		}

	}

	@Override
	public void viewResized(View view) {
		// empty
	}

	public void notifyDropEvent() {
		this.repaint();
	}

	@Override
	public void keyPressed(KeyEvent arg0) {

		// System.out.println("Desktop: Key pressed "+arg0);

		// arg0.consume();

	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		/*
		 * if (activeView != null) { activeView.keyReleased(arg0); }
		 * 
		 * arg0.consume();
		 */

		boolean commandOrControlDown = arg0.isControlDown() || arg0.isMetaDown();

		if ((commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_V)
				|| (arg0.isShiftDown() && arg0.getKeyCode() == KeyEvent.VK_INSERT)) {
			Clipboard clipboard = getToolkit().getSystemClipboard();
			Transferable incoming = clipboard.getContents(this);
			getTransferHandler().importData(this, incoming);
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

		/*
		 * if (arg0.getKeyChar() == 'l') { LabelView lv = new LabelView();
		 * lv.setX(mouseX); lv.setY(mouseY); lv.setString("Label"); this.add(lv); }
		 */

		/*
		 * if (arg0.getKeyChar() == 'z') { MainFrame.undoStack.undo(); arg0.consume(); }
		 */

		if (arg0.getKeyChar() == 's') {
			// new DeveloperControlFrame(this);
			try {
				store();
			} catch (ParserConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		if (arg0.getKeyChar() == '#') {
			new DeveloperControlFrame(this);
		}

		/*
		 * if (arg0.getKeyChar() == 't') { TutorialFirstSteps tut = new
		 * TutorialFirstSteps(this);
		 * 
		 * }
		 */

		// arg0.consume();

	}

	/**
	 * returns True if any of the models on the desktop has unsaved changes.
	 * 
	 * @return
	 */
	public boolean hasUnsavedChanges() {
		boolean result = false;
		for (View view : views) {
			if (view instanceof ModelView) {
				ModelView mv = (ModelView) view;
				if (mv.isUnsavedChanges()) {
					result = true;
					break;
				}
			}
		}
		return result;
	}

	public View getActiveView() {
		return activeView;
	}

	public void setActiveView(View view) {
		activeView = view;

		this.moveToFront(view);
		if (view instanceof ModelView) {
			ModelView mv = (ModelView) view;
			this.moveToFront(mv.getParameterView());
		}
	}

	public Point getLocationOfMouseRelativeToDesktop() {
		Point erg = MouseInfo.getPointerInfo().getLocation();

		Point location;
		try {
			location = this.getLocationOnScreen();
		} catch (Exception e) {
			return new Point(0, 0); // TODO: better fault behavior
		}
		erg.x -= location.x;
		erg.y -= location.y;
		if (erg.x < 0 || erg.x > this.getSize().width || erg.y < 0 || erg.y > this.getSize().height)
			return new Point(0, 0);
		return erg;
	}

	/**
	 * Determines input type, defaults to DATA. Can handle Onyx, SPSS, OpenMx, raw,
	 * and covariance data
	 * 
	 * @param input
	 * @return
	 */
	public static ImportType determineType(String input) {
		ImportType type;

		final byte[] zipByteArray = new byte[] { 80, 75, 03, 04 }; // HEX = 50, 4B, 03, 04
		String zipValue = "PK";
		try {
			zipValue = new String(zipByteArray, "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// System.out.println(input.substring(0, 4));
		if (input.startsWith(zipValue))
			type = ImportType.ONYX_CONTAINER;
		else if (input.toLowerCase().contains("specificationType=\"ONYX\"".toLowerCase()))
			type = ImportType.ONYX;
		else if (input.contains("mxModel"))
			type = ImportType.OPENMX;
		else if (input.contains("lavaan"))
			type = ImportType.LAVAAN;
		else if (input.substring(0, 8).contains("@(#)") || input.startsWith("$FL2"))
			type = ImportType.SPSS;
		else {
			boolean isCov = true;
			try {
				new CovarianceDataset(input);
			} catch (Exception e) {
				isCov = false;
			}
			/*
			 * String[] lines = input.split("\n"); int[] nums = new int[lines.length]; for
			 * (int i=0; i < lines.length;i++) nums[i] = lines[i].split("\t").length;
			 * boolean raw = false; for (int i=1; i < lines.length;i++) if (nums[i] <=
			 * nums[i-1]) raw=true; for (int i=0; i < lines.length;i++) System.out.println(
			 * i+" => "+nums[i]);
			 */
			if (!isCov) {
				type = ImportType.RAWDATA;
			} else {
				type = ImportType.COVARIANCEDATA;
			}
		}

		return type;
	}

	public static enum ImportType {
		UNKNOWN, RAWDATA, ONYX, OPENMX, MPLUS, SPSS, COVARIANCEDATA, LAVAAN, ONYX_CONTAINER
	};

	public void importString(String s) {
		Point p = getLocationOfMouseRelativeToDesktop();
		importString(s, null, "Anonymous Dataset", p.x, p.y, ImportType.UNKNOWN);
	}

	public void importString(String s, String name) {
		Point p = getLocationOfMouseRelativeToDesktop();
		importString(s, null, name, p.x, p.y, ImportType.UNKNOWN);
	}

	public void importString(String s, String name, ImportType type) {
		Point p = getLocationOfMouseRelativeToDesktop();
		importString(s, null, name, p.x, p.y, type);
	}

	public void importString(String s, File file) {
		Point p = getLocationOfMouseRelativeToDesktop();
		importString(s, file, file.getName(), p.x, p.y, ImportType.UNKNOWN);
	}

	public void importString(String s, File file, String name, int x, int y) {
		importString(s, file, name, x, y, ImportType.UNKNOWN);
	}

	public void importString(String s, File file, ImportType type) {
		Point p = getLocationOfMouseRelativeToDesktop();
		importString(s, file, file.getName(), p.x, p.y, type);
	}

	public void importString(String fileContent, File file, String name, int x, int y, ImportType type) {

		if (type == ImportType.UNKNOWN)
			type = determineType(fileContent);

		if (type == ImportType.RAWDATA) {
			if (name == null)
				initiateDataView(fileContent, "Anonymous Dataset", x, y);
			else
				initiateDataView(fileContent, name, x, y);
		} else if (type == ImportType.OPENMX) {
			OpenMxImport omx = new OpenMxImport(this);
			ModelView mv = omx.loadModelFromString(fileContent);
			mv.setLocation(x, y);
			makeUniqueModelName(mv);
			mv.setUnsavedChanges(false);
		} else if (type == ImportType.LAVAAN) {
			ModelView mv = new ModelView(this);
			LavaanImport imp = new LavaanImport(mv);
			this.add(mv);
		} else if (type == ImportType.ONYX_CONTAINER) {

			// storage for BufferedImages
			List<BufferedImage> bufimgList = new ArrayList<BufferedImage>();
			List<Integer> bufimgidList = new ArrayList<Integer>();

			ModelView mv = null;

			FileInputStream input;
			try {
				ZipFile zf = new ZipFile(file);

				Enumeration<ZipEntry> entries = (Enumeration<ZipEntry>) zf.entries();

				while (entries.hasMoreElements()) {

					ZipEntry entry = (ZipEntry) entries.nextElement();

					System.out.println(
							"Loading " + entry.getName() + " Size: " + entry.getSize() + " From " + entry.getTime());

					if (entry.getName().endsWith(".csv")) {

						try {
							loadData(zf.getInputStream(entry), entry.getName());
						} catch (FileLoadingException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					} else if (entry.getName().endsWith(".png")) {
						try {

							BufferedImage img = ImageIO.read(zf.getInputStream(entry));

							String[] elements = entry.getName().split("_");

							String idstr = elements[2].substring(0, elements[2].length() - 4);

							Integer id = Integer.parseInt(idstr);

							System.out.println("Loading " + id + " / " + idstr);

							bufimgList.add(img);
							bufimgidList.add(id);

						} catch (Exception e) {
							e.printStackTrace();
						}

					} else if (entry.getName().endsWith(".xml")) {
						System.out.println("Model " + entry.getName());
						// String xmlContent = IOUtils.toString(sourceFile);

						/*
						 * final StringBuilder sb = new StringBuilder(); final char[] buffer = new
						 * char[1024]; //# 1024 or 8192 final InputStreamReader isr = new
						 * InputStreamReader(zip); while (isr.read(buffer, 0, buffer.length) >= 0) {
						 * sb.append(new String(buffer)); System.out.println("ADD "+new String(buffer));
						 * } fileContent = sb.toString();
						 */
						BufferedReader br = new BufferedReader(new InputStreamReader(zf.getInputStream(entry)));

						// fileContent = br.lines().collect(Collectors.joining()); // requires JAVA8
						fileContent = new String();
						for (String line; (line = br.readLine()) != null; fileContent += line)
							;

						// System.out.println(fileContent);
						// System.out.println("-");
						mv = loadModel(fileContent, x, y);
						this.add(mv);
					}

				}

				zf.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// add images
			for (int i = 0; i < bufimgList.size(); i++) {
				if (mv != null) {

					Node node = mv.getGraph().getNodeById(bufimgidList.get(i));
					node.image = bufimgList.get(i);
				}
			}

		} else if (type == ImportType.ONYX) {

			// convert to UTF-8
			try {
				fileContent = new String(fileContent.getBytes(), MainFrame.CHAR_ENCODING);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ModelView mv = loadModel(fileContent, x, y);
			if (mv.getGraph() != null && mv.getGraph().isLackingNodePositions()) {
				Tree tree = new Tree(mv.getGraph(), true);
				tree.layout();
			}
			if (file != null)
				mv.setFile(file);
			mv.setUnsavedChanges(false);
			mv.setLocation(x, y);
			makeUniqueModelName(mv);
			mv.setUnsavedChanges(false);
		} else if (type == ImportType.SPSS) {
			SPSSImport simp = new SPSSImport();
			RawDataset ds = null;
			try {

				ds = simp.importSPSS(new FileInputStream(file));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (ds != null) {
				this.initiateDataView(ds, x, y);
			}

			ds.setName(file.getName());
		} else if (type == ImportType.COVARIANCEDATA) {
			// JOptionPane.showMessageDialog(this, "Not implemented yet!");
			CovarianceDataset ds = null;
			try {
				ds = new CovarianceDataset(fileContent);
			} catch (Exception e) {
			}
			if (ds != null) {
				this.initiateDataView(ds, x, y);
			}

			ds.setName((file == null ? "Anonymous Dataset" : file.getName()));
		}

	}

	public void importFromFile(File file, String name) throws IOException {
		Point p = getLocationOfMouseRelativeToDesktop();
		importFromFile(file, name, p.x, p.y);
	}

	public void importFromFile(File file, String name, int x, int y) throws IOException {

		BufferedReader br = new BufferedReader(
			    new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
		
	//	BufferedReader br = new BufferedReader(new FileReader(file));

		StringBuilder stringBuilder = new StringBuilder();

		String line = "";
		while ((line = br.readLine()) != null) {
			// buffer.append(line);
			stringBuilder.append(line);
			stringBuilder.append("\n");
		}

		br.close();

		String message = stringBuilder.toString();
		importString(message, file, name, x, y);

	}

	public void addDesktopListener(DesktopListener dl) {
		this.desktopListeners.add(dl);
	}

	public void restore() {
		// TODO clear workspace

		new WorkspaceLoader(this);
	}

	/**
	 * Save workspace
	 * 
	 * @throws ParserConfigurationException
	 */
	public void store() throws ParserConfigurationException {
		// prepare XML output
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("desktop");
		doc.appendChild(rootElement);

		// store all models
		for (View view : getViews()) {
			if (view instanceof ModelView) {
				ModelView modelView = (ModelView) view;
				XMLExport xmlExport = new XMLExport(modelView);
				Element modelViewElement = doc.createElement("modelview");
				rootElement.appendChild(modelViewElement);
				modelViewElement.setAttribute("x", String.valueOf(modelView.getX()));
				modelViewElement.setAttribute("y", String.valueOf(modelView.getY()));
				modelViewElement.setAttribute("w", String.valueOf(modelView.getWidth()));
				modelViewElement.setAttribute("h", String.valueOf(modelView.getHeight()));

				Element modelElement = xmlExport.export(doc);

				modelViewElement.appendChild(modelElement);
			}
		}

		// store all datasets
		for (View view : getViews()) {
			if (view instanceof DataView) {
				DataView dataView = (DataView) view;
				Element dataViewElement = doc.createElement("dataview");
				rootElement.appendChild(dataViewElement);
				dataViewElement.setAttribute("x", String.valueOf(dataView.getX()));
				dataViewElement.setAttribute("y", String.valueOf(dataView.getY()));
				dataViewElement.setAttribute("w", String.valueOf(dataView.getWidth()));
				dataViewElement.setAttribute("h", String.valueOf(dataView.getHeight()));
				// dataViewElement.setAttribute("filename", String.valueOf(dataView.getN))
//    			dataViewElement
				// content to XML / BASE64

			}
		}

		// store all links
		Element linkElement = Desktop.getLinkHandler().toXML(doc);
		rootElement.appendChild(linkElement);

		// output
		try {
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource source = new DOMSource(doc);

			File file = new File("test.test");

			StreamResult result = new StreamResult(file);

			transformer.transform(source, result);
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public boolean clear() {

		if (views.size() > 0) {
			String[] options = { "Yes", "No" };
			int n = JOptionPane.showOptionDialog(this,
					"Starting the tutorial requires an empty desktop. If you proceed, all models and datasets are closed now. Do you like to continue and clear the desktop?",
					"Clear desktop", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[1]);

			if (n == 1) {
				return false;
			}
		}

		for (int i = views.size() - 1; i >= 0; i--) {
			if (views.size() > 0) // this looks strange but some views kill their children and we might terminate
									// earlier than expected
				this.removeView(views.get(0));

		}

		return true;
	}

	public void mouseDraggedOnModelView(ModelView view, MouseEvent arg0) {

		mouseDragX = arg0.getX() + view.getX();
		mouseDragY = arg0.getY() + view.getY();

		if (dragSource != null) {
			this.repaint();
		}

	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		if (arg0.getSource() == menuSaveWorkspace) {

			SaveZIP saveZip = new SaveZIP(this.getModelViews(), this.getDataViews(), this);
			saveZip.export();

		}

		if (arg0.getSource() == removeLRedge) {

			viewConnections.remove(contextLRedge);
			repaint();
		}

		for (int i = 0; i < MainFrame.tutorialNum; i++) {
			if (arg0.getSource() == loadTutorialX[i]) {
				InputStream iStream = this.getClass().getResourceAsStream(MainFrame.tutorialFilenames[i]);
				if (iStream != null) {
					try {
						loadData(iStream, MainFrame.tutorialMenunames[i]);
					} catch (FileLoadingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		if (arg0.getSource() == closeAll) {
			Vector<View> deleteViews = new Vector<View>();
			for (View view : views) {
				if (view instanceof ModelView || view instanceof DataView)
					deleteViews.add(view);
			}
			for (int i = deleteViews.size() - 1; i >= 0; i--) {
				View view = deleteViews.get(i);
				removeView(view);
			}
		}

	}

	public void saveEstimate(ParameterReader estimates, boolean asHTML) {
		try {
			File dir = new File((String) Preferences.getAsString("DefaultWorkingPath"));
			final JFileChooser fc = new JFileChooser(dir);

			// In response to a button click:
			int returnVal = fc.showSaveDialog(null);

			File file;
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				file = fc.getSelectedFile();
				Preferences.set("DefaultWorkingPath", file.getParentFile().getAbsolutePath());
				if (!file.getName().contains("."))
					file = new File(file.getAbsolutePath() + ".txt");
			} else {
				return;
			}

			String content = estimates.getShortSummary(asHTML);

			OpenMxExport.createFile(file, content);

		} catch (Exception e) {
			JOptionPane.showMessageDialog(this, "The estimate could not be saved: \n" + e.toString(), "Export Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

}
