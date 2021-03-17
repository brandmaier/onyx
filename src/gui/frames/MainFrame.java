package gui.frames;

import importexport.EstimateTextExport;
import importexport.OpenMxImport;
import parallelProcesses.ParallelProcessView;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import engine.Preferences;
import engine.RawDataset;
import engine.ModelRequestInterface;
import engine.ModelRun.Priority;
import engine.backend.Model.Strategy;
import gui.CheckForUpdates;
import gui.Clipboard;
import gui.ContextHelpPanel;
import gui.Desktop;
import gui.FileLoadingException;
import gui.Logger;
import gui.TouchBarHandler;
import gui.actions.*;
import gui.graph.Edge;
import gui.graph.Node;
import gui.graph.decorators.ShapeDecorator;
import gui.tutorial.TutorialFirstSteps;
import gui.tutorial.TutorialView;
import gui.undo.UndoStack;
import gui.views.ScriptView;
import gui.views.DataView;
import gui.views.ModelView;
import gui.views.MultiGroupModelView;

/**
 * 
 * MainFrame is the main window (JFrame) of Onyx. It contains the desktop and
 * other singleton classes that represent unique concepts within Onyx, e.g., the
 * undo stack or a user settings object.
 * 
 * 
 * 
 * @author andreas
 * 
 */
public class MainFrame extends JFrame implements ActionListener, KeyListener,
		WindowListener, FocusListener {

	/**
	 * 
	 */
	
	public static final String MAJOR_VERSION = "1.0";
	public static final int SVN_VERSION = 947 + 1 + 92;
	
	
	/**
	 * flags for debugging
	 */
	
	public final static boolean GRAPH_DEBUGGING = false;
	public final static boolean LAYOUT_DEBUGGING = false;
	
	public final static boolean WITH_BAYES = false;
	
	// ---
	
	public final static String[] tutorialFilenames = 
		{
		"/data/demo01.dat",
		"/data/tameCFAData.csv",
		"/data/FactorModelDataSet.csv",
		"/data/demo03.dat",
		"/data/DualLGCM.csv"};
	public final static String[] tutorialMenunames = 
		{"Simple Regression Example",
		"Confirmatory Factor Example",
		"Confirmatory Factor Example II",
		"User Guide Factor Example",
		"Latent Growth Curve Example",
		};
	public final static int tutorialNum = tutorialFilenames.length;
	
	// ---
	
	public static final String CHAR_ENCODING = "UTF-8";
	
	private static final long serialVersionUID = 1937754663566169796L;
	
	public static final boolean DEFAULT_SAVEMODE_XML = true ;

    public static boolean DEVMODE = false;
	
	private Desktop desktop;

	public static TouchBarHandler touchBarHandler;

	private JMenuBar menuBar;
	private static ContextHelpPanel contextHelpPanel;

	private JMenuItem editUndo;

	//private JMenuItem loadTutorial1;
	JMenuItem loadTutorial2;
	JMenuItem loadTutorial3;
	JMenuItem loadTutorial4;
	
	JCheckBoxMenuItem menuUpdates;

	JMenuItem helpDocumentation;

	JMenu loadRecent;

	
	public static UndoStack undoStack = new UndoStack();
	public static Clipboard clipboard = new Clipboard();
//	public static Logger logger = new Logger();

	List<File> recentFiles = new ArrayList<File>();
	final int recentFileMaxSize = 10;
	private JMenuItem menuTutorial;
	private JMenuItem[] loadTutorialX;
	private JMenuItem menuTip;
	private TipOfTheDayFrame tip;
	
	//public static Preferences preferences;

	static {
		contextHelpPanel = new ContextHelpPanel();
	}

	public MainFrame() {
		super(WelcomeFrame.OMEGA + "nyx");
		//super.setFont(new Font("MS Mincho",Font.PLAIN, 12));

		// set look and feel of operating system
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		this.addFocusListener(this);

		// create components
		desktop = new Desktop(this);
		// this.setBackground(Color.green);
		JScrollPane scrollPane = new JScrollPane(desktop);
		scrollPane
				.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane
				.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

		this.getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		contextHelpPanel.setVisible(false);
		this.getContentPane().add(contextHelpPanel, BorderLayout.SOUTH);



		// Enable tool tips for the entire application
		ToolTipManager.sharedInstance().setEnabled(true);
		ToolTipManager.sharedInstance().setDismissDelay(1000 * 60 * 60); // 1
																			// hour

		// set exit strategy
		this.setDefaultCloseOperation(EXIT_ON_CLOSE);

		// add desktop as keylistener
		this.addKeyListener(desktop);


		// determine window size and make visible 
		// AB 04/2015: deprecated -- will be overriden later!!
		/*Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		int width = Math.min((int)screenSize.getWidth(), 1024);
		int height = Math.min((int)screenSize.getHeight(), 800);
		if (width==0) width=300; if (height==0) height=200; // just to be safe in case getScreenSize() is faulty
		
		this.setSize(new Dimension(width, height));
		 */
		

		// load recent files in reverse order
		// "0" is the most recent
		// recentFiles = new ArrayList<File>();
		for (int i = recentFileMaxSize - 1; i >= 0; i--) {
			String key = "recent" + i;
			if (Preferences.contains(key)) {
				// recentFiles.add( );
				addToRecentFiles(new File(Preferences.getAsString(key)),
						false);
			}

			// System.out.println(key+
			// " "+userPreferences.contains("recent"+i));
		}

		// create menu
		this.createMenuBar();
		
		updateRecentFiles();

		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(this);

		// fullscreen
		// TvO: Insets Behavior works well under windows, unix and mac unknonw.
		// AB: broken on Unix (reported in 04/2015)
		// Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
		//Dimension sz = Toolkit.getDefaultToolkit().getScreenSize();
		//this.setBounds(insets.left, insets.top, sz.width-(insets.left+insets.right), sz.height - (insets.top+insets.bottom));
		// this.setLocationRelativeTo(null); // center frame on screen
		this.setExtendedState(JFrame.MAXIMIZED_BOTH);

		if (Preferences.getAsString("CheckForUpdates").equals("true"))
		{
			Thread t = new  Thread(new CheckForUpdates(this));
			t.start();
		}


		desktop.setLocation(10, 10);
		
		this.setVisible(true);


		
		if (MainFrame.DEVMODE) {
			touchBarHandler = new TouchBarHandler(this);
			touchBarHandler.setActiveView(this);
		}
		
		// DEBUG
	
		/*MultiGroupModelView mg = new MultiGroupModelView(this.desktop);
		desktop.add(mg);
		*/
		/*desktop.loadModel(new File("./simple.xml"));
		desktop.loadModel(new File("./factormodel.xml"));
		try {
			desktop.loadData(new File("./holdoutset-group.csv"));
		} catch (FileLoadingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		*/
	/*	ModelView mv = new ModelView(this.desktop);
		desktop.add(mv);
		Node node = new Node("X");
		node.setIsLatent(false);
		node.setPosition(50, 50);
		mv.getModelRequestInterface().requestAddNode(node);
		*/
	/*	try {
			BufferedImage img = ImageIO.read(new File("/Users/brandmaier/Downloads/jsslogo.jpg"));
			node.image = img;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		
	/*	Edge edge = new Edge(node,node,true);
		edge.setDoubleHeaded(true);
		edge.setFixed(false);
		mv.getModelRequestInterface().requestAddEdge(edge);
		
		
		mv.getModelRequestInterface().setStrategy(Strategy.MCMC);
		*/
		/*
		node.setPosition(180, 120);
		Node node2 = new Node("Y");
		node2.setPosition(60,120);
		
		mv.getModelRequestInterface().requestAddNode(node2);
		Edge edge = new Edge(node,node2,true);
		mv.getModelRequestInterface().requestAddEdge(edge);
		
		Node node3 = new Node("Z");
		node3.setPosition(150, 250);
		mv.getModelRequestInterface().requestAddNode(node3);
		Edge edge2 = new Edge(node2,node3,true);
	
		mv.getModelRequestInterface().requestAddEdge(edge2);
		edge2.ctrlx1 += 100;
		edge2.ctrly1 -=100;
		edge2.ctrlAutomatic = false;
		/*ShapeDecorator sd = new ShapeDecorator(new Rectangle(3,3,50,50));
		mv.addDecorator(sd);*/
		
	}
	
	public Desktop getDesktop() {
		return desktop;
	}

	private void updateRecentFiles() {
		loadRecent.removeAll();

		for (int i = 0; i < recentFiles.size(); i++) {
			LoadFileAction lfa = new LoadFileAction(desktop,
					recentFiles.get(i), 0, 0);

			loadRecent.add(lfa);
		}

		// update properties
		for (int i = 0; i < recentFileMaxSize; i++) {
			if (i < recentFiles.size()) {
				Preferences.set("recent" + i, recentFiles.get(i)
						.getAbsolutePath());
			} else {
				Preferences.set("recent" + i, null);
			}
		}
	}

	/**
	 * creates a Menu for the recent files with context position for all
	 * actions.
	 * 
	 * @param x
	 * @param y
	 */
	public JMenu getLoadRecent(int x, int y) {
		JMenu loadRecent = new JMenu("Recent Files");

		for (int i = 0; i < recentFiles.size(); i++) {
			LoadFileAction lfa = new LoadFileAction(desktop,
					recentFiles.get(i), x, y);

			loadRecent.add(lfa);
		}
		return loadRecent;
	}

	/**
	 * create the static menubar and all items
	 */
	private void createMenuBar() {
		menuBar = new JMenuBar();
		this.setJMenuBar(menuBar);

		JMenu fileMenu = new JMenu(WelcomeFrame.OMEGA+"nyx");
		menuBar.add(fileMenu);
		// JMenu editMenu = new JMenu("Edit");
		// menuBar.add(editMenu);
		// JMenu viewMenu = new JMenu("View");
		// menuBar.add(editMenu);

		// JMenu editMenu = new JMenuItem("Edit");

		/*
		 * edit = new JMenu("Edit");
		 * 
		 * menuBar.add(edit);
		 */

		menuBar.add(Box.createHorizontalGlue());

		JMenu helpMenu = new JMenu("Help");
		menuBar.add(helpMenu);

		helpDocumentation = new JMenuItem("Manual");
		helpDocumentation.addActionListener(this);


		JMenu loadTutorial = new JMenu("Load Tutorial Data");

		loadTutorialX = new JMenuItem[tutorialNum];
		for (int i = 0; i < tutorialNum; i++)
		{
			loadTutorialX[i] = new JMenuItem(tutorialMenunames[i]);
			loadTutorial.add(loadTutorialX[i]);
			loadTutorialX[i].addActionListener(this);
		}
		
		loadRecent = new JMenu("Recent Files");



		fileMenu.add(new CreateEmptyModelAction(desktop));
		fileMenu.add(new LoadAction(desktop));
		fileMenu.add(loadTutorial);
		//fileMenu.add(new LoadModelAction(desktop));
		fileMenu.add(loadRecent);
		// fileMenu.add(menuImport);

		//if (MainFrame.DEVMODE) {
		 fileMenu.addSeparator();
		 fileMenu.add(new SettingsAction(this.desktop));
		//}
		fileMenu.addSeparator();
		fileMenu.add(new ExitAction());

		/*
		 * editUndo = new JMenuItem("Undo"); edit.add(editUndo);
		 * editUndo.addActionListener(this);
		 */
		
		menuUpdates = new JCheckBoxMenuItem("Check For Updates On Startup");
		menuUpdates.addActionListener(this);
		updateMenuUpdates();
		
		menuTutorial = new JMenuItem("Interactive Tutorial");
		menuTutorial.addActionListener(this);
		
		menuTip = new JMenuItem("Show Tip Of The Day");
		menuTip.addActionListener(this);
		
		helpMenu.add(helpDocumentation);
		helpMenu.add(menuTutorial);
		helpMenu.add(menuTip);
		helpMenu.addSeparator();
		helpMenu.add(menuUpdates);
		helpMenu.addSeparator();
		helpMenu.add(new AboutAction());
		
		

	}

	private void updateMenuUpdates() {
		if (Preferences.getAsString("CheckForUpdates").equals("true"))
		{
			menuUpdates.setSelected(true);
		} else {
			menuUpdates.setSelected(false);
		}
		
	}

	public void addToRecentFiles(File file) {
		addToRecentFiles(file, true);
	}

	public void addToRecentFiles(File file, boolean update) {
		if (file == null | recentFiles == null)
			return;

		// delete its appearance
		recentFiles.remove(file);

		// add as first file

		recentFiles.add(0, file);

		if (recentFiles.size() > recentFileMaxSize) {
			recentFiles.remove(recentFiles.size() - 1);
		}

		// rebuild submenu
		// for (int i=0; i < loadRecent.getItemCount();i++ )
		// {
		// loadRecent.getItem(i).removeActionListener(this);
		// }
		if (update)
			updateRecentFiles();
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {


		if (arg0.getSource() == menuTip) {
			tip = new TipOfTheDayFrame();
			tip.setVisible(true);
		}
		
		if (arg0.getSource() == menuTutorial) {
			boolean ok = desktop.clear();
			if (ok) {
			 TutorialFirstSteps tut = new TutorialFirstSteps(this.desktop);
			 this.desktop.moveToFront(tut.getView());
			}
		}
		
		if (arg0.getSource() == menuUpdates) {
			
			System.out.println(Preferences.get("CheckForUpdates"));
			if (Preferences.get("CheckForUpdates").equals("true"))
			{
				Preferences.set("CheckForUpdates", "false");
			} else {
				Preferences.set("CheckForUpdates", "true");				
			}
			
			updateMenuUpdates();
			
		}

		if (arg0.getSource() == helpDocumentation) {
			try {
				java.awt.Desktop.getDesktop().browse(
						new URI("http://onyx.brandmaier.de/manual.html?x="
								+ "doc"));
			} catch (IOException e) {
				
				JOptionPane
						.showMessageDialog(
								this,
								"An error has occured! You can manually access the documentation at out website: onyx.brandmaier.de");
				e.printStackTrace();
			} catch (URISyntaxException e) {
				
				JOptionPane
						.showMessageDialog(
								this,
								"An error has occured! You can manually access the documentation at out website: onyx.brandmaier.de");

				e.printStackTrace();
			}
		}

		if (arg0.getSource() == editUndo) {

			undoStack.undo();

		}

		for (int i=0; i < tutorialNum; i++)
		{
			if (arg0.getSource() == loadTutorialX[i])
			{
				InputStream iStream = this.getClass().getResourceAsStream(
						tutorialFilenames[i]);

				// System.out.println(url.getFile());
				if (iStream != null) {
					try {
						desktop.loadData(iStream, tutorialMenunames[i]);
					} catch (FileLoadingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	
		/*
		 * if (arg0.getSource() == menuImport) { OpenMxImport omx = new
		 * OpenMxImport(desktop); omx.loadModel(this); }
		 */
		// if (arg0.getSource() == menuExpo)

	}


	


	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyReleased(KeyEvent e) {
		boolean commandOrControlDown = e.isControlDown() || e.isMetaDown();
		if (commandOrControlDown && e.getKeyCode() == KeyEvent.VK_Z) {
			MainFrame.undoStack.undo();
		}
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

		// System.out.println("Key typed on frame"+e);
	}

	@Override
	public void dispose() {

	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		setOnyxInBackground(false);

	}

	HashMap<ModelView, Priority> prio = new HashMap<ModelView, Priority>();
	
	private void setOnyxInBackground(boolean b) {
		
		if (!Preferences.getAsBoolean("HoldWhenInBackground")) return;
	
		List<ModelView> mvs = this.getDesktop().getModelViews();
		
		if (b) {
			// set to background, store all current priorities and set current priority to HOLD
			prio.clear();
			for (ModelView mv : mvs) {
				Priority pr = mv.getModelRequestInterface().getRunPriority();
				prio.put(mv, pr);
				if (mv.getModelRequestInterface().getAllConvergedUnits().size() > 0) 
					mv.getModelRequestInterface().setRunPriority(Priority.HOLD);
			}
			
			
		} else {
			// recover & restore all priorities
			for (ModelView mv : mvs) {
				Priority pr = prio.get(mv);
				if (pr != null)
					mv.getModelRequestInterface().setRunPriority( pr );
			}
		}
		


		
	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		boolean hasUnsavedChanges = desktop.hasUnsavedChanges();
		if (hasUnsavedChanges) {
			int result = JOptionPane.showConfirmDialog(this,
					"Do you want to save all changes?", "Exit Onyx",
					JOptionPane.YES_NO_CANCEL_OPTION);
			if (result == JOptionPane.CANCEL_OPTION)
				return;
			if (result == JOptionPane.YES_OPTION)
				desktop.saveAllUnsaved();
		}

		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		//System.out.println("DEACTIVATED");
		
		setOnyxInBackground(true);

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	public static ContextHelpPanel getContextHelpPanel() {
		return contextHelpPanel;
	}

    public void setDeveloperMode(boolean developerMode) {
        DEVMODE = developerMode;        
    }

	@Override
	public void focusGained(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void focusLost(FocusEvent e) {
		// TODO Auto-generated method stub
		
	}

}
