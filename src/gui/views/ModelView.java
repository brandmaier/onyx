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

import importexport.CorrelationMatrixExport;
import importexport.EPSExport;
import importexport.EstimateHistoryExport;
import importexport.EstimateTextExport;
import importexport.Export;
import importexport.JPEGExport;
import importexport.LISRELMatrixTextExport;
import importexport.LaTeXExport;
import importexport.LavaanExport;
import importexport.MatrixTextExport;
import importexport.MplusExport;
import importexport.OnyxJavaExport;
import importexport.OpenMxExport;
import importexport.OpenMxMatrixExport;
import importexport.PDFExport;
import importexport.PNGExport;
import importexport.SVGExport;
import importexport.SaveZIP;
import importexport.SemExport;
import importexport.XMLExport;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTextField;
import javax.swing.JToolTip;
import javax.swing.KeyStroke;
import javax.swing.ToolTipManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.BadLocationException;

import parallelProcesses.ParallelProcess;
import parallelProcesses.ParallelProcessView;

//import com.sun.javafx.tk.quantum.MasterTimer;
//import com.sun.org.apache.bcel.internal.generic.GETSTATIC;

import bayes.Chain;
import bayes.ParameterSet;
import bayes.engine.BayesianModelRun;
import bayes.engine.BayesianModelRunUnit;
import engine.CombinedDataset;
import engine.CovarianceDataset;
import engine.Dataset;
import engine.ModelListener;
import engine.ModelRequestInterface;
import engine.ModelRun;
import engine.ModelRun.Priority;
import engine.ModelRun.Status;
import engine.ModelRun.Warning;
import engine.ModelRunUnit;
import engine.ModelRunUnit.Objective;
import engine.OnyxModel;
import engine.ParameterReader;
import engine.Preferences;
import engine.RawDataset;
import engine.SimulatedDataset;
import engine.Statik;
import engine.backend.Model;
import engine.backend.Model.Strategy;
import engine.externalRunner.ExternalRunUnit;
import geometry.Rectangle;
import gui.Constants;
import gui.Desktop;
import gui.EngineHelper;
import gui.ImageLoaderWorker;
import gui.LabeledInputBox;
import gui.MessageObject;
import gui.MessageObjectContainer;
import gui.TransferableVariableList;
import gui.Utilities;
import gui.actions.DesktopPasteAction;
import gui.actions.ModelViewCopyAction;
import gui.actions.ModelViewPasteAction;
import gui.fancy.DropShadowBorder;
import gui.frames.EdgeStyleFrame;
import gui.frames.LineStyleFrame;
import gui.frames.MainFrame;
import gui.frames.TimeseriesMeanPlot;
import gui.graph.VariableStack;
import gui.graph.Edge;
import gui.graph.FillStyle;
import gui.graph.Graph;
import gui.graph.Graph.MeanTreatment;
import gui.graph.Node;
import gui.graph.Node.AnchorType;
import gui.graph.NodeGroup;
import gui.graph.NodeGroupManager;
import gui.graph.VariableContainer;
import gui.graph.presets.Cinema;
import gui.graph.presets.Blueprint;
import gui.graph.presets.Default;
import gui.graph.presets.FlatRainbow;
import gui.graph.presets.RetroOrange;
import gui.graph.presets.Camouflage;
import gui.graph.presets.Julian;
import gui.graph.presets.Modern;
import gui.graph.presets.Mint;
import gui.graph.presets.Neon;
import gui.graph.presets.Happy;
import gui.graph.presets.Nightshift;
import gui.graph.presets.Preset;
import gui.graph.presets.FadedColors;
import gui.graph.presets.Schoeneberg;
import gui.graph.presets.Sketch;
import gui.graph.presets.Ylva;
import gui.graph.presets.Posh;
import gui.graph.presets.Chalk;
import gui.graph.presets.Gray;
import gui.graph.presets.Metal;
import gui.linker.DatasetField;
import gui.linker.LinkException;
import gui.linker.LinkHandler;
import gui.linker.LinkListener;
import gui.undo.AddGroupStep;
import gui.undo.EdgeArrowStyleChanged;
import gui.undo.EdgeCreateStep;
import gui.undo.EdgeHeadChangedStep;
import gui.undo.EdgeStateChangedStep;
import gui.undo.FillColorChangeStep;
import gui.undo.LineColorChangeStep;
import gui.undo.LinkChangedStep;
import gui.undo.LinkStep;
import gui.undo.MovedStep;
import gui.undo.MultiStep;
import gui.undo.NodeCreateStep;
import gui.undo.NodePositionMultiStep;
import gui.undo.NodeRenameStep;
import gui.undo.NodeStateChangedStep;
import gui.undo.NodeTypeChangedStep;
import gui.undo.PriorityChangeStep;
import gui.undo.ReshapeStep;
import gui.undo.UndoStack;
import gui.undo.UnlinkStep;
import gui.graph.decorators.*;

/**
 * 
 * ModelView is the central class of the GUI. It enables users to visually
 * modify a SEM in graph form. All changes are send as notifications to the
 * backend.
 * 
 * Some important functions are: - populateMenu() - actionPerformed() -
 * modelChangedEvent()
 * 
 * @author brandmaier
 *
 */
public class ModelView extends View implements ModelListener, ActionListener, DropTargetListener, DocumentListener,
		KeyListener, MouseMotionListener, MouseListener, Transferable, ClipboardOwner, LinkListener {

	enum DRAGTYPE {
		CREATE_SINGLEHEADED_PATH, CREATE_DOUBLEHEADED_PATH, MOVE_ANCHOR, MOVE_CONTROL, MOVE_EDGE_LABEL, MOVE_NODES,
		NONE, MOVE_VARIANCE
	}

	public static enum showPolicyType {
		BESTLS, BESTML, MANUAL, STARTING
	}

	private static final double EDGE_CLICK_RADIUS = 6;
	private static long IMMEDIATEREDRAWTIME = 2000000000;

	private static final int DIRECTTYPE_TIMEDELAY_BUFFER_RESET = 1000; // milliseconds

	public Vector<Integer> guides_vertical = new Vector<Integer>();
	public Vector<Integer> guides_horizontal = new Vector<Integer>();
	int guide_vertical_active = -1;
	int guide_horizontal_active = -1;

	private MessageObject messageNonPositiveDefiniteDataSet = new MessageObject(
			"Data covariance matrix is not positive definite.", "warning");
	private MessageObject messageSwitchedToImplicitMeanTreatment = new MessageObject(
			"One or more variables with non-zero means exists; means are added implicitly. You may switch to an explicit mean structure.",
			"information");
	private MessageObject messageSwitchedToExplicitMeanTreatment = new MessageObject(
			"Means are treated explicitly now.", "information");
	private MessageObject messageDatasetHasNoParticipants = new MessageObject("Data set has no observations.", "error");
	private MessageObject messageMultipleDatasetsWithoutID = new MessageObject(
			"Multiple data sets without identifier column are used, those will be treated as independent.", "warning");
	private MessageObject messageAddVariables = new MessageObject(
			"Add observed variables by dragging them from a dataset onto the model!", "information");
	private MessageObject messageNoGroupsOrDefinitionOnCovariance = new MessageObject(
			"Definition variables and groups are not permitted on covariance data sets.", "information");
	private MessageObject messageGroupingHasMissing = new MessageObject(
			"Group variables have missingness, for these cases, grouped variables will be ignored.", "warning");
	private MessageObject messageDefinitionHasMissing = new MessageObject(
			"Definition variables are not available for all observations.", "information");
	private MessageObject messageError = new MessageObject("An unspecified error has occured!", "error");
	private MessageObject messageObjectRunning = new MessageObject("Model parameters are being estimated right now!",
			"gears");
	private MessageObject messageObjectNA = new MessageObject("One or more columns have only missing data!", "error");
	private MessageObject messageOverspecified = new MessageObject("Model is overspecified.", "warning");
	private MessageObject messageSingular = new MessageObject("The model-implied covariance matrix is singular!",
			"warning");
	private MessageObject messageAcceleratingCycle = new MessageObject(
			"The single-headed edges form a cylce of product greater than one.", "warning");
	private MessageObject msgConnectGroupVariables = new MessageObject(
			"Connect all group indicator variables to obtain parameter estimates!", ImageLoaderWorker.INFORMATION,
			this);
	private MessageObject msgConnectDefinitionVariables = new MessageObject(
			"Connect all definition variables to obtain parameter estimates!", ImageLoaderWorker.INFORMATION, this);
	private MessageObject msgConnectObservedVariables = new MessageObject(
			"Connect all observed variables to obtain parameter estimates!", ImageLoaderWorker.INFORMATION, this);
	private MessageObject messageObjectAllMissing = new MessageObject(
			"There is at least one data row without any observed data! This may bias your fit statistics!",
			ImageLoaderWorker.WARNING, this);

	public static Preset[] presets;
	private static final long serialVersionUID = -2465936243089035720L;
	public final static int THINSTROKE = 0, MEDIUMSTROKE = 1, THICKSTROKE = 2;

	static {
		presets = new Preset[16];
		presets[0] = new Default();

		presets[1] = new Modern();
		
		presets[2] = new Cinema();
		presets[3] = new Happy();
		presets[4] = new Schoeneberg();
		presets[5] = new Chalk();
		presets[6] = new FadedColors();
		// presets[7] = new Charlottenburg();
		presets[7] = new Metal();
		presets[8] = new Camouflage();
		presets[9] = new FlatRainbow();
		presets[10] = new Sketch();	
		presets[11] = new Neon();
		presets[12] = new Blueprint();
		presets[13] = new RetroOrange();
		presets[14] = new Mint();
		presets[15] = new Posh();
		//presets[16] = new Ylva();
		/*
		 * presets[16] = new Amelie();
		 */
	}
	private JMenu arrow;

	private JMenuItem arrow1, arrow2, arrow3;
	private int atomicModelChangeCount;

	boolean atomicOperationInProgress; //
	// private Border border;
	public List<ScriptView> codeView = new ArrayList<ScriptView>();
	private boolean commandOrControlDown;
	private List<ModelRunUnit> currentEstimates;
	private List<ModelRunUnit> currentEstimatesShownInMenu;
	public List<DecoratorObject> decorators = new ArrayList<DecoratorObject>();
	// private boolean directType;

	private String directTypeBuffer;
	private Edge directTypeEdge;
	private long directTypeTimestamp;

	private int dragAnchor;

	private Edge dragCtrl;

	private int dragCtrlInt;

	private DecoratorObject dragDecorator;

	private boolean dragDetected;

	private Edge draggedLabelEdge;
	private Node dragNode;
	private DRAGTYPE dragType = DRAGTYPE.NONE;
	private int drawEdgeToY = -1, drawEdgeToX = -1, drawEdgeFromX = -1, drawEdgeFromY = -1;
	private double edgeLabelRelativePositionPrev;
	EdgeStyleFrame edgeStyleFrame;
	private File file;
	/**
	 * 
	 */
	private Graph graph;

	private int gridSize = Constants.DEFAULT_GRID_SIZE;

	private Stroke gridStroke;

	public boolean hideMessageObjectContainer = false;
	private boolean holdPathUntilNextClick;
	// private KeyStroke[] ks;
	private LabeledInputBox labelsizeInput;
	private long lastMousePressedTime;
	private boolean lockToGrid;;

	private JPopupMenu menu;

	private JMenuItem menuActivateManuaLEdgeControl;

	private JMenuItem menuAddTriangle;
	private JMenuItem menuAddMultiplication;

	private JMenuItem[] menuAgentItems;;

	private JMenuItem menuAgents;

	private LabeledInputBox menuAnzRowsSimulation;

	private LabeledInputBox menuanzSamplesDPClustering;
	private LabeledInputBox menuAnzBurninDPClustering;
	private LabeledInputBox menuAlphaDPClustering;
	private LabeledInputBox menuPriorStrengthDPClustering;

	private LabeledInputBox menuBurninPreClusteringDPClustering;
	private LabeledInputBox menuSamplesPreClusteringDPClustering;
	private LabeledInputBox menuThinningPreClusteringDPClustering;

	private JMenuItem menuAutoLayout;

	JMenuItem menuBackgroundColor;

	private JMenuItem menuClone;

	private JMenu menuShowingEstimate;

	private Edge menuContextEdge;

	private Node menuContextNode;

	private JMenuItem menuCreateLatent;

	private JMenuItem menuCreateManifest;

	private JMenu menuCreatePath;

	private JMenuItem menuDeleteEdge;

	// this dummy is needed to trigger loading routines in MessageObject...
	// private static final MessageObject dummy = new MessageObject();

	private JMenuItem menuDeleteModel;
	private JMenuItem menuDeleteNode;
	private JMenuItem menuEdgeColor;
	private JMenuItem menuExport;

	private JMenuItem menuExportJPEG, menuExportPNG, menuExportPDF, menuExportEPS;

	private JMenuItem menuExportLaTeX;

	private JMenuItem menuExportMplus, menuExportLavaan, menuExportSem;
	private JMenuItem menuExportSVG;
	JMenuItem[] menuGraphPresets;
	private JMenu menuGraphPresetsMenu;
	private JMenuItem menuIconify;
	private JMenuItem menuNodeColor;
	private JMenuItem menuNodeFillColor;
	private JMenu menuNodeFillStyle;
	private JMenuItem menuNodeFillStyleFill, menuNodeFillStyleGradient, menuNodeFillStyleNone;
	// private boolean shiftDown;

	private LabeledInputBox menuPercentMissingSimulation;

	private JRadioButtonMenuItem menuPriorityHigh, menuPriorityNormal, menuPriorityLow, menuPriorityHold;
	private JRadioButtonMenuItem menuStrategyClassic, menuStrategyDefault, menuStrategyDefaultWithEMSupport;
	private JRadioButtonMenuItem menuMeanTreatmentExplicit, menuMeanTreatmentSaturated;
	private JMenuItem[] menuRunnerItems;
	private JMenuItem menuRunners;
	private JMenuItem menuAllRunner;
	private JMenuItem menuSaveModel;
	private JMenuItem menuLoadStartingValues;

	private JMenuItem menuPrior;
	private JMenuItem menuPriorGaussian;
	private JMenuItem menuPriorGamma;
	private JMenuItem menuPriorChi2;
	private JMenuItem menuPriorUniform;

	JMenuItem menuSelectEdgeStyle;

	private JMenuItem menuSetDefaultStartingValues;
	private JMenu menuShowCode;
	private JMenuItem menuShowCorrelationMatrix;

	private JMenu menuShowHide;

	private JMenuItem menuShowHideCovariances;

	private JMenuItem menuShowHideRegressions;
	private JMenuItem menuShowHideVariances;
	private JMenuItem menuShowLavaanCode;
	private JMenuItem menuShowMPlusCode;
	private JMenuItem menuShowSemCode;
	private JMenuItem menuShowOnyxJavaCode;
	private JMenuItem menuShowOpenMXCode;
	private JMenuItem menuShowOpenMXMatrixCode;
	private JMenuItem menuShowRAMMatrices;
	private JMenuItem menuShowLISRELMatrices;
	private JMenuItem menuShowTextOutput;
	private JMenuItem menuShowTextHistory;
	private JMenu menuSimulation;
	private JMenuItem menuSimulationStart;
	private JMenuItem menuSimulationCovarianceDataset;
	private JMenu menuDPClustering;
	private JMenuItem menuDPClusteringStart;
	private JMenuItem menuPreClusteringDPClustering;
	private JMenuItem menuToggleDoPreClusteringDPClustering;

	/* JMenuItem thinStroke, mediumStroke, thickStroke; */
	JMenu menuStroke;
	JMenuItem menuSwapGrouping;
	JMenuItem menuSwapLatent;
	private JMenuItem menuSwapManifestLatent;

	private JMenuItem menuSwapNormalized;
	private JMenuItem menuResetToDefaults;
	private JMenuItem menuToggleAutomaticNaming;

	private JMenuItem menuToggleEdgeHeads;
	private JMenuItem menuToggleFixed;
	private JMenuItem menuToggleLockToGrid;
	private JMenuItem menuToggleMarkUnconnectedManifests;
	private JMenuItem menuToggleShowGrid;

	private JMenuItem menuPaste, menuCopy, menuFlip, menuUndo, menuPasteDontMess;

	JMenuItem menuUnlinkGrouping;

	private JMenuItem menuUnlinkNode, menuShowStartingValues, menuShowBestML, menuShowBestLS;

	private MessageObjectContainer messageObjectContainer;

	private LabeledInputBox modelName;

	public int mouseAtX;

	public int mouseAtY;

	private ModelRequestInterface mri;

	private LabeledInputBox nameInput;

	private boolean newNodeIsLatent;

	private LabeledInputBox nodeGroupInput;

	private NodeGroupManager nodeGroupManager;

	private int nodeIdCounter;

	private LabeledInputBox nodeNameInput;

	private JTextField onTheFlyTextArea;

	ParameterDrawer parameterView;

	private Edge pressedEdge;

	private Node pressedNode;
	private List<Node> selectedNodes;
	// data concerning the grid
	private boolean showGrid;

	private ParameterReader showingEstimate;

	public showPolicyType showPolicy;

	private MessageObject sparklingObject;

	ParameterReader startingValues;

	private LabeledInputBox svalueInput;

	private LabeledInputBox thicknessInput;

	private boolean unsavedChanges = false;

	private LabeledInputBox valueInput;

	private double zoom = 1.0;
	// private String nodeNameBeforeChangedInPopupMenu;
	// private NodeStateChangedStep pendingNodeStateUndoStep;
	private EdgeStateChangedStep pendingEdgeStateChangedUndoStep;
	private Vector<NodeStateChangedStep> pendingNodeStateUndoSteps = new Vector<NodeStateChangedStep>();
	private Vector<EdgeStateChangedStep> pendingEdgeStateChangedUndoSteps = new Vector<EdgeStateChangedStep>();
	private JMenu menuSelectSaturaredModel;
	private JMenuItem[] menuSend;
	private Object menuSelectSaturatedModel;
	private boolean shiftDown;
	private LabeledInputBox menuGridSizeInput;
	private JMenuItem menuFlipHorizontal;
	private JMenuItem menuFlipVertically;
	// private JMenu menuCopyPaste;
	private JMenu menuModifyGraph;

	private CombinedDataset combinedData;
	private JMenu menuEdit;
	private JMenuItem menuRedo;
	private JMenuItem menuSelectAll;
	// private JMenu menuFile;
	private JMenuItem menuSaveAsModel;
	private JMenu menuSaveScript;
	private JMenu menuSaveImage;
	private JMenu menuSave;
	private JMenuItem menuSaveStartingValues;
	private JMenuItem menuSaveCurrentEstimate;
	private JCheckBoxMenuItem menuShowStandardizedEstimates;
	private boolean showStandardizedEstimates;
	private JMenuItem menuRemoveDefinitionStatus;
	private JMenuItem menuMakeDefinition;
	private JMenuItem menuUnlinkDefinition;
	private JMenuItem menuDashStyle;
	private JMenuItem menuLatentScores;
	private JMenuItem menuCreatePathCovariance;
	private JMenuItem menuCreatePathRegression;
	private JMenuItem menuRemoveAllAux;
	private JMenu menuAux;
	private AbstractButton menuShowAux;
	private JMenuItem menuCreatePathVariance;
	private JRadioButtonMenuItem menuStrategyMCMC;
	private bayes.gui.ChainToolTip chainToolTip;
	private String chainToolTipParameterName;
	private JMenuItem menuAddImage;
	private JMenuItem menuSaveModelAndData;
	private JMenuItem menuNodeFillStyleR2;
	private JMenuItem menuNodeFillStyleHand;
	private JMenuItem menuEdgeLabelColor;
	private JMenuItem menuNodeFontColor;
	private JCheckBoxMenuItem menuKeepStyle;

	public ModelView(Desktop desktop) {
		super(desktop);

		super.hasTopLeftResizer = true;

		// create new Graph representation for nodes and edges
		graph = new Graph(this);

		// establish link to the backend
		mri = new OnyxModel();
		((OnyxModel) mri).addModelListener(this);

		String name = OnyxModel.defaultName;
		if (desktop != null) {
			int i = desktop.getNumberForUnnamedModel();
			if (i > 1) {
				name += " " + i;
			}
		}

		mri.requestChangeModelName(name);

		startingValues = mri.getStartingValuesUnit();
		showPolicy = showPolicyType.MANUAL;

		// default tooltip TODO: update when model name changes!
		this.setToolTipText("<html>" + mri.getName() + "</html>");

		this.addKeyListener(this);

		// register action map
		getActionMap().put("paste", new ModelViewPasteAction(this));
		getActionMap().put("copy", new ModelViewCopyAction(this));

		// create key strokes
		final KeyStroke pasteStroke1 = KeyStroke.getKeyStroke(KeyEvent.VK_V,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		final KeyStroke pasteStroke2 = KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.SHIFT_DOWN_MASK // AB:17.11.2015
																												// changed
																												// constant
																												// to
																												// InputEvent
																												// constant
		);
		final KeyStroke copyStroke1 = KeyStroke.getKeyStroke(KeyEvent.VK_C,
				Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());
		final KeyStroke copyStroke2 = KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, InputEvent.CTRL_DOWN_MASK // AB:17.11.2015
																											// changed
																											// constant
																											// to
																											// InputEvent
																											// constant
		);

		// register input map
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(pasteStroke1, "paste");
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(pasteStroke2, "paste");
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(copyStroke1, "copy");
		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(copyStroke2, "copy");

		messageObjectContainer = new MessageObjectContainer();

		// activate dnd support for connection between ModelView and DataView
		new DropTarget(this, this);

		// set visual appearance
		this.setSize(600, 400);
		this.setLocation(100, 100);
		this.setOpaque(false);
		this.setBackground(Color.white);
		this.gridStroke = new BasicStroke(1, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

		this.setUnsavedChanges(false);

		// node groups
		nodeGroupManager = new NodeGroupManager();

		// set View options
		this.movable = true;
		this.resizable = true;

		// add a docked View for parameters
		parameterView = new ParameterDrawer(this);
		if (desktop != null) {
			desktop.add(parameterView);
		}
		parameterView.updatePosition();

		// establish node counter for automatically labelling nodes
		nodeIdCounter = 1;

		// model creation is also a model change
		modelChangedEvent();

		// set status for new nodes to latent
		newNodeIsLatent = true;

		try {
			lockToGrid = Boolean.parseBoolean(Preferences.getAsString("lockToGridDefault"));
		} catch (Exception e) {
			lockToGrid = false;
		}
		try {
			showGrid = Boolean.parseBoolean(Preferences.getAsString("showGridDefault"));
		} catch (Exception e) {
			showGrid = false;
		}
		try {
			String style = Preferences.getAsString("preset");
			if (style.equals("Tempelhof")) {
				this.graph.graphStyle = new Gray();
			} else if (style.equals("SimpleblackAndWhiteStyle")) {
				this.graph.graphStyle = new Modern();
			} else
				this.graph.graphStyle = new Default();
		} catch (Exception e) {
			this.graph.graphStyle = new Default();
		}

		graph.setMeanTreatment(Graph.MeanTreatment.ambique);
		mri.setMeanTreatment(graph.getMeanTreatment());

		Desktop.getLinkHandler().addLinkListener(this);

	}

	@Override
	public void actionPerformed(ActionEvent e) {

		if (e.getSource() == menuAddImage) {
			addImageToNode();
		}

		if (e.getSource() == menuShowAux) {
			graph.getAuxiliaryStack().setHidden(!graph.getAuxiliaryStack().isHidden());
			this.repaint();
		}

		if (e.getSource() == menuRemoveAllAux) {
			graph.getAuxiliaryStack().removeAll();
			modelChangedEvent();
		}

		if (e.getSource() == menuSelectAll) {
			selectAll();
		}

		if (e.getSource() == menuUnlinkDefinition) {
			for (Edge edge : getSelectedEdges()) {
				LinkHandler.getGlobalLinkhandler().unlink(edge.getDefinitionVariableContainer());
			}
			this.repaint();
		}

		if (e.getSource() == menuMakeDefinition) {
			for (Edge edge : getSelectedEdges()) {
				edge.getDefinitionVariableContainer().setActive(true);
				// set definition variable snippet
				if (edge.isFree())
					mri.requestSwapFixed(edge);
				mri.requestSetDefinitionVariable(edge, edge.getParameterName());
				// ---
			}
			this.repaint();
		}

		if (e.getSource() == menuRemoveDefinitionStatus) {
			for (Edge edge : getSelectedEdges()) {
				// this is an unlink:
				LinkHandler.getGlobalLinkhandler().unlink(edge.getDefinitionVariableContainer());
				// and this is the removal:
				edge.getDefinitionVariableContainer().setActive(false);
				mri.requestUnsetDefintionVariable(edge);
			}
			this.repaint();
		}

		if (menuSend != null) {
			for (int i = 0; i < menuSend.length; i++) {
				if (e.getSource() == menuSend[i]) {

					// find right model
					ModelView mv = desktop.getModelViews().get(i);
					// link models
					mv.getModelRequestInterface().linkSaturatedModel(this.getModelRequestInterface().getModel());
				}
			}
		}

		if (e.getSource() == menuShowStandardizedEstimates) {
			showStandardizedEstimates = !menuContextEdge.isShowStandardizedEstimate();
			for (Edge edge : getSelectedEdges()) {
				// edge.set
				edge.setShowStandardizedEstimate(showStandardizedEstimates);
			}
			this.redraw();
		}
		if (e.getSource()== menuKeepStyle) {
			this.getGraph().setLockedStyle(!this.getGraph().isLockedStyle() );
		}
 
		if (menuGraphPresets != null) {
			for (int i = 0; i < menuGraphPresets.length; i++) {
				if (e.getSource() == menuGraphPresets[i]) {

					// TODO MainFrame.undoStack.add(new GraphStateChanged(this, graph));

					presets[i].apply(graph, this.showingEstimate);

					graph.graphStyle = presets[i];

					// TODO: update default style
					Preferences.set("styleDefault", presets[i].getName());

					repaint();
				}
			}
		}

		if (e.getSource() == arrow1) {
			MainFrame.undoStack.startCollectSteps();
			for (Edge edge : getSelectedEdges()) {
				MainFrame.undoStack.add(new EdgeArrowStyleChanged(edge));
				edge.setArrowStyle(0);
			}
			MainFrame.undoStack.endCollectSteps();
			repaint();
		}

		if (e.getSource() == arrow2) {
			MainFrame.undoStack.startCollectSteps();
			for (Edge edge : getSelectedEdges()) {
				MainFrame.undoStack.add(new EdgeArrowStyleChanged(edge));
				edge.setArrowStyle(1);
			}
			MainFrame.undoStack.endCollectSteps();
			repaint();
		}

		if (e.getSource() == arrow3) {
			MainFrame.undoStack.startCollectSteps();
			for (Edge edge : getSelectedEdges()) {
				MainFrame.undoStack.add(new EdgeArrowStyleChanged(edge));
				edge.setArrowStyle(2);
			}
			MainFrame.undoStack.endCollectSteps();
			repaint();
		}

		if (e.getSource() == menuCreatePathVariance) {
			Node node = menuContextNode;
			Edge edge = new Edge(node, node);
			edge.setDoubleHeaded(true);
			edge.setFixed(false);
			// edge.setFixed(!free);
			this.mri.requestAddEdge(edge);
		}

		if (e.getSource() == menuCreatePath || e.getSource() == menuCreatePathCovariance
				|| e.getSource() == menuCreatePathRegression) {
			// System.out.println("Create path!");
			// startPathDrag(menuContextNode, mouseClickX, mouseClickY);
			holdPathUntilNextClick = true;
			dragNode = menuContextNode;
			dragNode.clickX = mouseClickX - menuContextNode.getX();
			dragNode.clickY = mouseClickY - menuContextNode.getY();

			dragNode.oldX = dragNode.getX();
			dragNode.oldY = dragNode.getY();
			dragNode.oldWidth = dragNode.getWidth();
			dragNode.oldHeight = dragNode.getHeight();

			if (// e.getSource() == menuCreatePath ||
			e.getSource() == menuCreatePathRegression) {
				dragType = DRAGTYPE.CREATE_SINGLEHEADED_PATH;
			} else {
				dragType = DRAGTYPE.CREATE_DOUBLEHEADED_PATH;
			}
		}

		if (menuAgentItems != null)
			for (int i = 0; i < menuAgentItems.length; i++) {
				if (e.getSource().equals(menuAgentItems[i])) {

					ExternalRunUnit rep = ExternalRunUnit.getRepresentantByName(menuAgentItems[i].getText());

					// if (rep instanceof OpenMxRunUnit) RConnection.askForRInterpreter(this);
					ExternalRunUnit newRun = ExternalRunUnit.getInstance(rep,
							getModelRequestInterface().getStartingValuesUnit(), Objective.MAXIMUMLIKELIHOOD, 0.01,
							"ML " + rep.getAgentLabel(), mri.getVariableNames(), mri.getObservedIds(), 3, "", this);
					getModelRequestInterface().addRunner(newRun);
				}
			}

		if (e.getSource() == menuToggleAutomaticNaming) {
			boolean automaticNaming = !menuContextEdge.isAutomaticNaming();

			MultiStep undo = new MultiStep();
			for (Edge edge : getSelectedEdges()) {

				undo.add(new EdgeStateChangedStep(this, edge));

				edge.setAutomaticNaming(automaticNaming);

				// go through MRI
				graph.updateEdgeLabel(edge);
			}

			MainFrame.undoStack.add(undo);
		}

		// if (e.getSource() == )
		if (e.getSource() == menuShowHideVariances) {
			getGraph().hideVariances = !getGraph().hideVariances;
			this.repaint();
		}

		if (e.getSource() == menuActivateManuaLEdgeControl) {

			menuContextEdge.setActiveControl(!menuContextEdge.getActiveControl());
			this.repaint();
		}

		if (e.getSource() == menuSetDefaultStartingValues) {

			setDefaultStartingValues();

		}

		if (e.getSource() == menuToggleMarkUnconnectedManifests) {

			this.graph.markUnconnectedNodes = !this.graph.markUnconnectedNodes;
			this.repaint();
		}

		// private JMenuItem menuPaste, menuCopy, menuFlip, menuUndo;
		if (e.getSource() == menuCopy) {
			MainFrame.clipboard.copy(graph);
		}

		if (e.getSource() == menuPaste) {
			MainFrame.clipboard.pasteWithinBounds(this, mouseClickX, mouseClickY, true);
		}

		if (e.getSource() == menuPasteDontMess) {
			MainFrame.clipboard.pasteWithinBounds(this, mouseClickX, mouseClickY, false);
		}

		if (e.getSource() == menuUndo) {
			MainFrame.undoStack.undo();
		}

		if (e.getSource() == menuRedo) {
			// TODO TvO: to be implemented
			MainFrame.undoStack.redo();
		}

		if (e.getSource() == menuSelectAll) {
			selectAll();
		}

		if (e.getSource() == menuFlipHorizontal) {
			graph.getSelectedNodes().flipHorizontally(graph);
		}

		if (e.getSource() == menuFlipVertically) {
			graph.getSelectedNodes().flipVertically(graph);
		}

		if (e.getSource() == menuLatentScores) {

			double[][] data = mri.getLatentAndMissingScores(getShowingEstimate());
			List<String> names = new ArrayList<String>();
			String[] nameArray = mri.getVariableNames();
			for (String n : nameArray)
				names.add(n);
			Dataset dataset = new RawDataset(data, names);
			dataset.setName(getName() + " scores");
			DataView dv = new DataView(this.desktop, dataset);

			desktop.add(dv);

		}

		if (e.getSource() == menuSwapLatent) {
			MainFrame.undoStack.startCollectSteps();
			for (Node node : getSelectedNodes()) {
				MainFrame.undoStack.add(new NodeTypeChangedStep(this.getModelRequestInterface(), node));
				mri.requestSwapLatentToManifest(node);
			}
			MainFrame.undoStack.endCollectSteps();
			this.redraw();
		}

		if (e.getSource() == menuCreateLatent) {
			createNewNode(mouseClickX, mouseClickY, true);
		}

		if (e.getSource() == menuCreateManifest) {
			createNewNode(mouseClickX, mouseClickY, false);
		}

		if (e.getSource() == menuSwapNormalized) {
			boolean state = menuContextNode.isNormalized();

			for (Node node : getSelectedNodes())
				node.setNormalized(!state);

			this.modelChangedEvent();
			this.redraw();
		}

		if (e.getSource() == menuSwapGrouping) {

			boolean state = menuContextNode.isGrouping();

			MainFrame.undoStack.startCollectSteps();

			for (Node node : getSelectedNodes()) {
				if (!state)
					MainFrame.undoStack.add(new AddGroupStep(this, node));
				node.setGrouping(!state);
				node.updateAnchors();
			}
			// ((Node) menuContextNode).toggleGrouping();
			// menuContextNode.updateAnchors();

			MainFrame.undoStack.endCollectSteps();

			this.modelChangedEvent();
			this.redraw();
		}

		if (e.getSource() == menuUnlinkGrouping) {
			for (Node node : getSelectedNodes())
				node.unlinkGrouping();
			this.modelChangedEvent();
			this.redraw();
		}

		if (e.getSource() == menuDeleteModel) {
			requestDeleteView();
		}

		if (e.getSource() == menuToggleShowGrid) {
			toggleShowGrid();
		}

		if (e.getSource() == menuResetToDefaults) {
			graph.tidyUp();
			redraw();
		}

		if (e.getSource() == menuAutoLayout) {
			MainFrame.undoStack.add(new NodePositionMultiStep(graph));
			graph.autoLayout();
			redraw();
		}

		if (e.getSource() == menuClone) {
			desktop.cloneModelView(this);
		}

		if (e.getSource() == menuToggleLockToGrid) {
			lockToGrid = !lockToGrid;
			Preferences.set("lockToGridDefault", "" + lockToGrid);
		}

		if (e.getSource() == menuExportMplus) {
			Export exp = new MplusExport(this);
			exp.export();
		}

		if (e.getSource() == menuExportLavaan) {
			Export exp = new LavaanExport(this);
			exp.export();
		}

		if (e.getSource() == menuExportSem) {
			Export exp = new SemExport(this);
			exp.export();
		}

		if (e.getSource() == menuExportPDF) {
			Export exp = new PDFExport(this);
			exp.export();
		}

		if (e.getSource() == menuExportEPS) {
			Export exp = new EPSExport(this);
			exp.export();
		}

		if (e.getSource() == menuExport) {
			Export exp = new OpenMxExport(this);
			exp.export();
		}

		if (e.getSource() == menuExportLaTeX) {
			Export exp = new LaTeXExport(this);
			exp.export();
		}

		if (e.getSource() == menuExportSVG) {
			Export exp = new SVGExport(this);
			exp.export();
		}

		if (e.getSource() == menuSwapManifestLatent) {
			newNodeIsLatent = !newNodeIsLatent;
			menuSwapManifestLatent.setText((newNodeIsLatent ? "New variable: Manifest" : "New variable: Latent"));
		}

		if (e.getSource() == menuAddTriangle) {

			Node node = new Node(-1, mouseClickX, mouseClickY);
			node.setTriangle(true);
			this.getModelRequestInterface().requestAddNode(node);

		}

		if (e.getSource() == menuAddMultiplication) {

			Node node = new Node(-1, mouseClickX, mouseClickY);
			node.setIsLatent(true);
			node.setAsMultiplication(true);
			this.getModelRequestInterface().requestAddNode(node);

		}

		/*
		 * if (e.getSource() == thinStroke) setStroke(THINSTROKE); if (e.getSource() ==
		 * mediumStroke) setStroke(MEDIUMSTROKE); if (e.getSource() == thickStroke)
		 * setStroke(THICKSTROKE);
		 */
		if (e.getSource() == menuSelectEdgeStyle) {
			if (edgeStyleFrame == null) {
				edgeStyleFrame = new EdgeStyleFrame(this);
			}
			setUnsavedChanges(true);
			edgeStyleFrame.setVisible(true);
		}

		if (e.getSource() == menuDeleteEdge) {
			for (Edge edge : getSelectedEdges())
				mri.requestRemoveEdge(edge);
		}

		if (e.getSource() == menuDeleteNode) {

			setAtomicOperationInProgress(true);

			MainFrame.undoStack.startCollectSteps();

			boolean ok = true;

			for (Node node : getSelectedNodes()) {

				if (node.isMeanTriangle()) {
					List<Edge> edges = graph.getAllEdgesAtNode(node);
					for (int i = 0; i < edges.size(); i++) {
						try {
							ok = ok & mri.requestRemoveEdge(edges.get(i));
						} catch (Exception ee) {
							ee.printStackTrace();
						}
					}
				}

				try {
					ok = ok & mri.requestRemoveNode(node);
				} catch (Exception ee) {
					ee.printStackTrace();
				}

			}

			MainFrame.undoStack.endCollectSteps();

			setAtomicOperationInProgress(false);

			if (!ok) {
				MessageObject mo = new MessageObject("Error! Backend could not delete a node!",
						ImageLoaderWorker.ERROR);
				this.messageObjectContainer.add(mo);
				this.redraw();
			}
		}

		if (e.getSource() == menuToggleEdgeHeads) {

			// supress undos temporary because backend
			// sends delete and add edge commands
			MainFrame.undoStack.lock();

			MultiStep ms = new MultiStep();

			for (Edge edge : getSelectedEdges()) {

				if (edge.isVarianceEdge() || edge.getSource().isMeanTriangle())
					continue;

				ms.add(new EdgeHeadChangedStep(this, edge));
				mri.requestCycleArrowHeads(edge);

				edge.setActiveControl(false);
			}

			// finally add the real undo action to the undo stack

			MainFrame.undoStack.unlock();

			MainFrame.undoStack.add(ms);
		}

		if (e.getSource() == menuSaveCurrentEstimate) {
			if (getShowingEstimate() != null)
				getDesktop().saveEstimate(getShowingEstimate(), false);
		}
		if (e.getSource() == menuSaveStartingValues) {
			if (mri.getStartingValuesUnit() != null)
				getDesktop().saveEstimate(mri.getStartingValuesUnit(), false);
		}

		if (e.getSource() == menuSaveModelAndData) {

			// XMLExport.save(mri, graph);
			Export export;

			export = new SaveZIP(this, true);

			this.file = export.export();

			desktop.mainFrame.addToRecentFiles(this.file);
			setUnsavedChanges(false);
		}

		if (e.getSource() == menuSaveModel) {

			// XMLExport.save(mri, graph);
			Export export;

			if (MainFrame.DEFAULT_SAVEMODE_XML)
				export = new XMLExport(this);
			else
				export = new SaveZIP(this);

			if (this.file == null)
				this.file = export.export();
			else
				try {
					export.export(this.file);
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(this, "Error! File could not be saved!");
				}

			desktop.mainFrame.addToRecentFiles(this.file);
			setUnsavedChanges(false);
		}

		if (e.getSource() == menuSaveAsModel) {
			// XMLExport.save(mri, graph);

			Export export;

			if (MainFrame.DEFAULT_SAVEMODE_XML)
				export = new XMLExport(this);
			else
				export = new SaveZIP(this);
			this.file = export.export();
			desktop.mainFrame.addToRecentFiles(this.file);
			setUnsavedChanges(false);
		}

		if (e.getSource() == menuLoadStartingValues)
			loadStartingValues();

		if (e.getSource() == menuDashStyle) {

			LineStyleFrame lsf = new LineStyleFrame(this, getSelectedEdges());

		}

		if (e.getSource() == menuEdgeLabelColor) {
			setUnsavedChanges(true);
			Color newColor = JColorChooser.showDialog(this, "Choose Font Color", menuContextEdge.getLabel().getColor());

			MultiStep mstep = new MultiStep();

			for (Edge edge : getSelectedEdges()) {
				// mstep.add(new EdgeLabelColorChangeStep(this, edge));
				edge.getLabel().setColor(newColor);
			}

			MainFrame.undoStack.add(mstep);

			this.redraw();

		}

		if (e.getSource() == menuNodeFontColor) {
			setUnsavedChanges(true);
			Color newColor = JColorChooser.showDialog(this, "Choose Font Color", menuContextNode.getFontColor());

			MultiStep mstep = new MultiStep();

			for (Node node : getSelectedNodes()) {
				node.setFontColor(newColor);
			}

			MainFrame.undoStack.add(mstep);

			this.redraw();

		}

		if (e.getSource() == menuEdgeColor) {

			setUnsavedChanges(true);
			Color newColor = JColorChooser.showDialog(this, "Choose Path Color", menuContextEdge.getLineColor());

			MultiStep mstep = new MultiStep();

			for (Edge edge : getSelectedEdges()) {
				mstep.add(new LineColorChangeStep(this, edge));
				edge.setLineColor(newColor);
			}

			MainFrame.undoStack.add(mstep);

			this.redraw();
		}

		if (e.getSource() == menuBackgroundColor) {
			setUnsavedChanges(true);
			Color newColor = JColorChooser.showDialog(this, "Choose Background Color", getGraph().backgroundColor);

			this.getGraph().backgroundColor = newColor;
			this.redraw();
		}

		if (e.getSource() == menuNodeFillStyleGradient) {
			for (Node node : getSelectedNodes()) {
				node.nodeFillGradient = FillStyle.GRADIENT;
			}
		}

		if (e.getSource() == menuNodeFillStyleNone) {
			for (Node node : getSelectedNodes()) {
				node.nodeFillGradient = FillStyle.NONE;
			}
		}

		if (e.getSource() == menuNodeFillStyleFill) {
			for (Node node : getSelectedNodes()) {
				node.nodeFillGradient = FillStyle.FILL;
			}
		}

		if (e.getSource() == menuNodeFillStyleR2) {
			for (Node node : getSelectedNodes()) {
				node.nodeFillGradient = FillStyle.R2;
			}
		}

		if (e.getSource() == menuNodeFillStyleHand) {
			for (Node node : getSelectedNodes()) {
				node.nodeFillGradient = FillStyle.HAND;
			}
		}

		if (e.getSource() == menuNodeColor) {

			openNodeLineColorPicker();
		}

		if (e.getSource() == menuNodeFillColor) {

			openNodeFillColorPicker();
		}

		if (e.getSource() == menuUnlinkNode) {
			setUnsavedChanges(true);

			for (Node node : getSelectedNodes()) {
				DatasetField df = Desktop.getLinkHandler().getDatasetField(node.getObservedVariableContainer());

				// if node is not linked, continue
				if (df == null)
					continue;

				MainFrame.undoStack
						.add(new UnlinkStep(node.getObservedVariableContainer(), df.dataset, df.columnId, mri));

				Desktop.getLinkHandler().unlink(node.getObservedVariableContainer());

			}

			desktop.repaint();

		}

		if (e.getSource() == menuIconify) {
			super.setIconified(!this.isIconified());
		}

		if (e.getSource() == menuToggleFixed) {

			MultiStep mstep = new MultiStep();

			boolean state = menuContextEdge.isFixed();
//			boolean state = getSelectedEdges().get(0).isFixed();
			for (Edge edge : getSelectedEdges()) {
				mstep.add(new EdgeStateChangedStep(this, edge));
				if (state == edge.isFixed())
					mri.requestSwapFixed(edge);

				edge.update();
			}

			MainFrame.undoStack.add(mstep);

			for (Edge edge : getSelectedEdges()) {
				graph.cleverEdgeLabelLayout(edge);
			}

		}

		if (e.getSource() == menuSimulationStart) {
			int anzRows = 0, percentMissing = 0;
			try {
				String content = menuAnzRowsSimulation.getDocument().getText(0,
						menuAnzRowsSimulation.getDocument().getLength());
				anzRows = Integer.parseInt(content);
				String contentMiss = menuPercentMissingSimulation.getDocument().getText(0,
						menuPercentMissingSimulation.getDocument().getLength());
				percentMissing = Integer.parseInt(contentMiss);
			} catch (Exception ex) {
			}

			if (anzRows > 0 && percentMissing >= 0 && percentMissing < 100) {
				mri.setParameter(this.showingEstimate);
				mri.requestCreateData(anzRows, percentMissing, true);
			}
		}

		if (e.getSource() == menuSimulationCovarianceDataset) {
			int anzRows = 0, percentMissing = 0;
			try {
				String content = menuAnzRowsSimulation.getDocument().getText(0,
						menuAnzRowsSimulation.getDocument().getLength());
				anzRows = Integer.parseInt(content);
			} catch (Exception ex) {
			}

			if (anzRows > 0 && percentMissing >= 0 && percentMissing < 100) {
				mri.setParameter(this.showingEstimate);
				mri.requestCreateData(anzRows, percentMissing, false);
			}
		}

		if (e.getSource() == menuDPClusteringStart) {

			int anzSamples = 5, anzBurnin = 0;
			double priorStrength = 2, alpha = 1.0;
			int preClusteringBurnin = 50, preClusteringSamples = 50, preClusteringThinning = 1;
			boolean doPreClustering = true;
			try {
				String content = menuanzSamplesDPClustering.getDocument().getText(0,
						menuanzSamplesDPClustering.getDocument().getLength());
				anzSamples = Integer.parseInt(content);
				content = menuAnzBurninDPClustering.getDocument().getText(0,
						menuAnzBurninDPClustering.getDocument().getLength());
				anzBurnin = Integer.parseInt(content);
				content = menuPriorStrengthDPClustering.getDocument().getText(0,
						menuPriorStrengthDPClustering.getDocument().getLength());
				priorStrength = Double.parseDouble(content);
				content = menuAlphaDPClustering.getDocument().getText(0,
						menuAlphaDPClustering.getDocument().getLength());
				alpha = Double.parseDouble(content);

				// get sampling parameters for Pre-Clustering
				content = menuBurninPreClusteringDPClustering.getDocument().getText(0,
						menuBurninPreClusteringDPClustering.getDocument().getLength());
				preClusteringBurnin = Integer.parseInt(content);
				content = menuSamplesPreClusteringDPClustering.getDocument().getText(0,
						menuSamplesPreClusteringDPClustering.getDocument().getLength());
				preClusteringSamples = Integer.parseInt(content);
				content = menuThinningPreClusteringDPClustering.getDocument().getText(0,
						menuThinningPreClusteringDPClustering.getDocument().getLength());
				preClusteringThinning = Integer.parseInt(content);
				doPreClustering = menuToggleDoPreClusteringDPClustering.isSelected();
			} catch (Exception ex) {
			}

			if (anzSamples > 0 && anzBurnin >= 0 && priorStrength >= 0 && alpha > 0.0) {
				mri.setParameter(this.showingEstimate);
				ParallelProcess process = mri.requestClusterWithDirichletProcess(anzSamples, anzBurnin, alpha,
						priorStrength, doPreClustering, preClusteringBurnin, preClusteringSamples,
						preClusteringThinning);
				if (process != null)
					addProgressView(new ParallelProcessView(desktop, process));
			}
		}

		if (e.getSource() == menuStrategyClassic) {
			mri.setStrategy(Model.Strategy.classic);
		}
		if (e.getSource() == menuStrategyDefaultWithEMSupport) {
			mri.setStrategy(Model.Strategy.defaultWithEMSupport);
		}

		if (e.getSource() == menuStrategyMCMC) {
			mri.setStrategy(Model.Strategy.MCMC);
		}

		// TODO Old version of this line set the strategy to default if menuPriorityHigh
		// was selected, I guess this is a bug; AB, please check!
//        if (e.getSource() == menuPriorityHigh) {
//            mri.setStrategy(Model.Strategy.defaul);
//        }
		if (e.getSource() == menuStrategyDefault) {
			mri.setStrategy(Model.Strategy.defaul);
		}

		if (e.getSource() == menuPriorityHigh) {
			mri.setRunPriority(Priority.HIGH);
		}

		if (e.getSource() == menuPriorityNormal) {
			MainFrame.undoStack.add(new PriorityChangeStep(this, mri.getRunPriority()));

			mri.setRunPriority(Priority.NORMAL);
		}

		if (e.getSource() == menuPriorityLow) {
			MainFrame.undoStack.add(new PriorityChangeStep(this, mri.getRunPriority()));

			mri.setRunPriority(Priority.LOW);
		}

		if (e.getSource() == menuPriorityHold) {
			MainFrame.undoStack.add(new PriorityChangeStep(this, mri.getRunPriority()));

			mri.setRunPriority(Priority.HOLD);
		}

		boolean hasRunners = this.currentEstimatesShownInMenu != null;

		if (hasRunners) {
			// for (int i = 0; i < this.currentEstimatesShownInMenu.size(); i++) {
			for (int i = 0; i < this.menuRunnerItems.length; i++) { // TODO: Serious! here we a concurrency problem
																	// sometimes. Is this the right solution?

				if (e.getSource() == menuRunnerItems[i]) {
					/*
					 * parameterView.setParameterReader(currentEstimatesShownInMenu .get(i));
					 * graph.updateWithEstimates(currentEstimatesShownInMenu .get(i));
					 * showingEstimate = currentEstimatesShownInMenu.get(i); this.redraw();
					 */
					ModelRunUnit runner = currentEstimatesShownInMenu.get(i);
					showPolicy = showPolicyType.MANUAL;
					updateShownEstimates(runner);
				}
			}
		}

		if (e.getSource() == menuMeanTreatmentExplicit) {
			graph.setMeanTreatment(Graph.MeanTreatment.explicit);
			mri.setMeanTreatment(graph.getMeanTreatment());
			modelChangedEvent();
		}
		if (e.getSource() == menuMeanTreatmentSaturated) {

			if (graph.hasTriangles()) {

				JOptionPane.showMessageDialog(this,
						"The model has an explicit mean structure. Delete the explicit mean structure before setting mean treatment to saturated. ",
						"Mean Treatment Error", JOptionPane.ERROR_MESSAGE);

			} else {

				graph.setMeanTreatment(Graph.MeanTreatment.implicit);
				mri.setMeanTreatment(graph.getMeanTreatment());
				modelChangedEvent();

			}

		}

		if (e.getSource() == menuShowStartingValues) {
			showPolicy = showPolicyType.STARTING;
			updateShownEstimates(mri.getStartingValuesUnit());
		}
		if (e.getSource() == menuAllRunner) { // TODO continue
			RunnerTableView view = new RunnerTableView(desktop, this);
			this.desktop.add(view);
		}
		if (e.getSource() == menuShowBestML) {
			// showPolicy could be set to BESTML (will automatically switch to
			// best ML estimate) if we want that behavior.
			showPolicy = showPolicyType.MANUAL;
			if (mri.getAnzConverged() > 0) {
				ModelRunUnit mru = (currentEstimates == null || currentEstimates.size() == 0 ? null
						: currentEstimates.get(0));
				if (mru != null)
					updateShownEstimates(mru);
			}
		}
		if (e.getSource() == menuShowBestLS) {
			// showPolicy could be set to BESTSL (will automatically switch to
			// best LS estimate) if we want that behavior.
			showPolicy = showPolicyType.MANUAL;
			if (mri.getAnzConverged() > 0) {
				ModelRunUnit mru = getBestLeastSquaresEstimate();
				if (mru != null)
					updateShownEstimates(mru);
			}
		}

		if (e.getSource() == menuExportJPEG) {
			Export exp = new JPEGExport(this, false);
			exp.export();
		}

		if (e.getSource() == menuExportPNG) {
			Export exp = new PNGExport(this);
			exp.export();
		}

		if (e.getSource() == menuShowOpenMXCode) {
			ScriptView view = new ScriptView(desktop, this, new OpenMxExport(this));
			this.codeView.add(view);
			this.desktop.add(view);
		}

		if (e.getSource() == menuShowOpenMXMatrixCode) {
			ScriptView view = new ScriptView(desktop, this, new OpenMxMatrixExport(this));
			this.codeView.add(view);
			this.desktop.add(view);
		}

		if (e.getSource() == menuShowMPlusCode) {
			ScriptView view = new ScriptView(desktop, this, new MplusExport(this));
			this.codeView.add(view);
			this.desktop.add(view);
		}
		if (e.getSource() == menuShowLavaanCode) {
			ScriptView view = new ScriptView(desktop, this, new LavaanExport(this));
			this.codeView.add(view);
			this.desktop.add(view);
		}
		if (e.getSource() == menuShowSemCode) {
			ScriptView view = new ScriptView(desktop, this, new SemExport(this));
			this.codeView.add(view);
			this.desktop.add(view);
		}
		if (e.getSource() == menuShowRAMMatrices) {
			ScriptView view = new ScriptView(desktop, this, new MatrixTextExport(this));
			this.codeView.add(view);
			this.desktop.add(view);
		}
		if (e.getSource() == menuShowLISRELMatrices) {
			ScriptView view = new ScriptView(desktop, this, new LISRELMatrixTextExport(this));
			this.codeView.add(view);
			this.desktop.add(view);
		}

		if (e.getSource() == menuShowCorrelationMatrix) {
			ScriptView view = new ScriptView(desktop, this, new CorrelationMatrixExport(this));

			this.codeView.add(view);
			this.desktop.add(view);
		}

		if (e.getSource() == menuShowOnyxJavaCode) {
			ScriptView view = new ScriptView(desktop, this, new OnyxJavaExport(this));
			this.codeView.add(view);
			this.desktop.add(view);
		}
		if (e.getSource() == menuShowTextOutput) {
			ScriptView view = new ScriptView(desktop, this, new EstimateTextExport(this));
			this.codeView.add(view);
			this.desktop.add(view);
		}
		if (e.getSource() == menuShowTextHistory) {
			ScriptView view = new ScriptView(desktop, this, new EstimateHistoryExport(this));
			this.codeView.add(view);
			this.desktop.add(view);
		}
	}

	public void openNodeFillColorPicker() {
		MultiStep mstep = new MultiStep();

		setUnsavedChanges(true);
		Color newColor = JColorChooser.showDialog(this, "Choose Fill Color", menuContextNode.getFillColor());

		for (Node node : getSelectedNodes()) {

			mstep.add(new FillColorChangeStep(this, node));
			node.setFillColor(newColor);
		}

		MainFrame.undoStack.add(mstep);
		/*
		 * if (!this.graph.graphStyle.supportsFillColor()) { JOptionPane
		 * .showMessageDialog( null,
		 * "You are using a graph style that does not support fill colors for variables. Please select a different graph style. "
		 * ); }
		 */
		this.redraw();
	}

	public void openNodeLineColorPicker() {
		MultiStep mstep = new MultiStep();

		setUnsavedChanges(true);
		Color newColor = JColorChooser.showDialog(this, "Choose Line Color", menuContextNode.getLineColor());
		for (Node node : getSelectedNodes()) {
			// if (node.isSelected())
			mstep.add(new LineColorChangeStep(this, node));

			node.setLineColor(newColor);
		}

		MainFrame.undoStack.add(mstep);

		this.redraw();
	}

	private void addImageToNode() {
		File dir = new File((String) Preferences.getAsString("DefaultWorkingPath"));
		final JFileChooser fc = new JFileChooser(dir);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.addChoosableFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(".txt");
			}
		});
		fc.setAcceptAllFileFilterUsed(true);

		fc.setDialogTitle("Load Parameters");
		int returnVal = fc.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				BufferedImage img = ImageIO.read(file);
				menuContextNode.image = img;
				this.repaint();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void loadStartingValues() {
		File dir = new File((String) Preferences.getAsString("DefaultWorkingPath"));
		final JFileChooser fc = new JFileChooser(dir);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.addChoosableFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return null;
			}

			@Override
			public boolean accept(File file) {
				return file.getName().endsWith(".txt");
			}
		});
		fc.setAcceptAllFileFilterUsed(true);

		fc.setDialogTitle("Load Parameters");
		int returnVal = fc.showSaveDialog(this);

		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			Preferences.set("DefaultWorkingPath", file.getParentFile().getAbsolutePath());
			if (!file.getName().equals("")) {
				OnyxModel model = mri.getModel();
				double[] par = model.getParametersFromSummary(file);
				for (Edge ed : graph.getEdges()) {
					if (ed.isFree()) {
						int ix = 0;
						while (ix < par.length && !model.getParameterNames()[ix].equals(ed.getParameterName()))
							ix++;
						if (ix < par.length) {
							ed.setValue(par[ix]);
							mri.requestSetValue(ed);
						}
					}
				}
			}
		}

	}

	private void activateEstimate(int i) {
		activateEstimate(i, true);
	}

	private void activateEstimate(int i, boolean forcePolicyChange) {
		List<ModelRunUnit> estimates = stackCurrentEstimates();
		if (estimates != null) {
			if (i - 1 >= estimates.size())
				return;
			ParameterReader pm = null;
			if (i >= 1) {
				if (forcePolicyChange)
					showPolicy = showPolicyType.MANUAL;
				pm = estimates.get(i - 1);
			} else if (i == 0) {
				if (forcePolicyChange)
					showPolicy = showPolicyType.STARTING;
				pm = mri.getStartingValuesUnit();
			}

			/*
			 * showingEstimate = pm; parameterView.setParameterReader(pm);
			 * graph.updateWithEstimates(pm); this.redraw();
			 */
			updateShownEstimates(pm);
		}
	}

	public void addDecorator(DecoratorObject dec) {
		this.decorators.add(dec);
		this.repaint();
	}

	@Override
	public void addEdge(Edge edge) {

		MainFrame.undoStack.add(new EdgeCreateStep(this, edge));

		setUnsavedChanges(true);

		graph.addEdge(edge);
		modelChangedEvent();
		this.redraw();

	}

	@Override
	public void addNode(Node node) {

		MainFrame.undoStack.add(new NodeCreateStep(this, node));

		setUnsavedChanges(true);
		boolean switchedToExplicitMean = node.isMeanTriangle()
				&& graph.getMeanTreatment() != Graph.MeanTreatment.explicit;
		if (switchedToExplicitMean) {
			graph.setMeanTreatment(Graph.MeanTreatment.explicit);
			mri.setMeanTreatment(graph.getMeanTreatment());
		}

		// by definition, addNode has to increment all node ids in the
		// graph that are higher than or equal to id
		int id = node.getId();
		Iterator<Node> iterNode = graph.getNodeIterator();
		while (iterNode.hasNext()) {
			Node otherNode = iterNode.next();
			if (otherNode.getId() >= id) {
				otherNode.setId(otherNode.getId() + 1);
			}
		}
		graph.addNode(node);
		modelChangedEvent();
		// message needs to be after modelChangeEvent to avoid clearing.
		if (switchedToExplicitMean)
			messageObjectContainer.addOnce(messageSwitchedToExplicitMeanTreatment);
		this.redraw();
	}

	@Deprecated
	public double[][] assembleData() {

		// RawDataset dataset = (RawDataset)dataset;

		LinkHandler link = Desktop.getLinkHandler();

		Set<Dataset> uniqueDataset = new HashSet<Dataset>();

		// are all observed variables connected?
		int numObserved = 0;
		int numConnected = 0;
		List<Node> observedNodes = new ArrayList<Node>();
		for (Node node : graph.getNodes()) {
			if (node.isObserved()) {
				numObserved += 1;
				if (node.isConnected()) {
					numConnected += 1;
					DatasetField df = link.getDatasetField(node.getObservedVariableContainer());
					if (df != null)
						uniqueDataset.add(df.dataset);

				}
				observedNodes.add(node);
			}

		}

		// construct a dataset
		if (numObserved != numConnected) {
			messageObjectContainer
					.add(new MessageObject("Connect all manifest variables in order to obtain parameter estimates!",
							ImageLoaderWorker.INFORMATION, this));

			return null;
		}

		if (numObserved == 0) {
			messageObjectContainer
					.add(new MessageObject("Add observed variables by dragging them from a dataset onto the model!",
							ImageLoaderWorker.INFORMATION, this));
			return null;
		}

		// TODO do all datasets have the same number of rows ?!

		double[][] data = null;

		if (uniqueDataset.size() == 1) {

			int[] filter = mri.getObservedIds();

			Node linkNode = graph.getNodeById(filter[0]);
			// GraphField gf = new GraphField(graph, linkNode);
			DatasetField df = link.getDatasetField(linkNode.getObservedVariableContainer());

			if (df == null) {

				messageObjectContainer
						.add(new MessageObject("An error occurred with the mapping of your variables. Blame Andy.",
								ImageLoaderWorker.ERROR, this));

				System.err.println("An error occurred with the variable mapping! Variable is linked to NULL dataset");
				return null;
			}

			if (!(df.dataset instanceof RawDataset)) {
				JOptionPane.showMessageDialog(this,
						"An error has occurred during assemblin data when linking dataset and model!");

				return (null);
			}

			RawDataset rdataset = (RawDataset) df.dataset;

			data = new double[rdataset.getNumRows()][numObserved];

			// fill data

			for (int i = 0; i < filter.length; i++) {
				Node node = graph.getNodeById(filter[i]);
				DatasetField datasetField = link.getDatasetField(node.getObservedVariableContainer());
				if (datasetField == null)
					return null;
				for (int j = 0; j < rdataset.getNumRows(); j++) {

					if (!(datasetField.dataset instanceof RawDataset)) {
						JOptionPane.showMessageDialog(this, "An error has occurred during linking dataset and model!");
						return (null);
					}

					data[j][i] = ((RawDataset) datasetField.dataset).get(j, datasetField.columnId);
				}

			}

		} else {

			// combine multiple datasets

			// (1) pre-checks: do all datasets have ID columns ?
			for (Dataset dataset : uniqueDataset) {
				if (!dataset.hasIdColumn()) {

					messageObjectContainer.add(new MessageObject(
							"If multiple datasets are associated with a single model, all datasets must have an identifier column!",
							ImageLoaderWorker.ERROR, this));
					return null;
				}
			}
			// (2) get the union of all ID columns and create
			// a mapping of IDS to real column indices

			HashMap<Double, Integer> idMap = new HashMap<Double, Integer>();

			TreeSet<Double> unionIds = new TreeSet<Double>();
			for (Dataset dataset : uniqueDataset) {

				// RawDataset

				double[] ids = ((RawDataset) dataset).getColumn(((RawDataset) dataset).getIdColumn());
				for (double id : ids) {
					unionIds.add(id);
				}
			}

			int k = 0;
			for (Double key : unionIds) {
				idMap.put(key, k);
				k++;
			}

			// (3) create the union dataset
			int N = unionIds.size();

			data = new double[N][numObserved];
			for (int i = 0; i < N; i++) {
				for (int j = 0; j < numObserved; j++) {
					data[i][j] = Model.MISSING;
				}
			}

			// (4) fill the union dataset

			int[] filter = mri.getObservedIds();
			for (int i = 0; i < filter.length; i++) {
				Node node = graph.getNodeById(filter[i]);
				DatasetField datasetField = link.getDatasetField(node.getObservedVariableContainer());
				if (datasetField == null)
					return null;

				RawDataset rdataset = (RawDataset) datasetField.dataset;

				int idColumn = rdataset.getIdColumn();

				for (int j = 0; j < rdataset.getNumRows(); j++) {

					int row = idMap.get(rdataset.get(j, idColumn));
					data[row][i] = rdataset.get(j, datasetField.columnId);
				}
			}

		}

		// perform missingness due to group membership. Missing grouping variables will
		// cause
		// a warning and will be considered wrong for all group.
		int[] filter = mri.getObservedIds();
		for (int i = 0; i < filter.length; i++) {
			Node node = graph.getNodeById(filter[i]);
			if (node.isGroupingVariableConnected()) {
				for (int j = 0; j < data.length; j++) {
					DatasetField df = node.getGroupingVariableContainer().getLinkedDatasetField();
					double group = ((RawDataset) df.dataset).get(j, df.columnId);
					// double group = node.groupingDataset.get(j, node.groupingColumn);
					if (Model.isMissing(group)) {
						messageObjectContainer.addOnce(messageGroupingHasMissing);

						System.out.println("Grouping variable at node " + node.getCaption() + " has missingness.");
					} else if (group != node.groupValue)
						data[j][i] = Model.MISSING;
				}
			}
		}

		if (graph.getMeanTreatment() == Graph.MeanTreatment.implicit) {
			for (int var = 0; var < filter.length; var++) {
				double sum = 0.0;
				int anz = 0;
				for (int i = 0; i < data.length; i++)
					if (!Model.isMissing(data[i][var])) {
						anz++;
						sum += data[i][var];
					}
				if (anz > 0) {
					double mean = sum / (double) anz;
					for (int i = 0; i < data.length; i++)
						if (!Model.isMissing(data[i][var]))
							data[i][var] -= mean;
				}
			}
		}

		// TvO: Normalization removed by decision on 24 FEB 2014
		// if (node.isNormalized()) {
		// double sum = 0, sqsum = 0;
		// int anz = 0;
		// for (int j = 0; j < data.length; j++) {
		// if (!Model.isMissing(data[j][i])) {
		// anz++;
		// sum += data[j][i];
		// sqsum += data[j][i] * data[j][i];
		// }
		// }
		// if (anz > 0) {
		// double mean = sum / (double) anz, stdv = Math.sqrt(sqsum
		// / (double) anz - mean * mean);
		// for (int j = 0; j < data.length; j++) {
		// if (!Model.isMissing(data[j][i]))
		// data[j][i] = (data[j][i] - mean) / stdv;
		// }
		// }
		// }

		return data;
	}

	/**
	 * DocumentListener method that waits for events from a JTextfield for value
	 * changes of edges
	 * 
	 * @param arg0
	 */
	@Override
	public void changedUpdate(DocumentEvent arg0) {

		updateFromPopupMenu(arg0);
	}

	@Override
	public void changeName(String name) {
		setUnsavedChanges(true);
	}

	@Override
	public void changeNodeCaption(Node node, String text) {
		this.graph.renameNode(node, text);

	}

	@Override
	public void changeParameterOnEdge(Edge edge) {
		setUnsavedChanges(true);

	}

	/**
	 * 
	 * A notification of change of status of the backend.
	 * 
	 * 
	 * @Override
	 * 
	 */
	public void changeStatus(Status status) {

		if (status == Status.RUNNING) {

			// Nothing...

		} else if (status == Status.RESULTSVALID) {

			if (messageObjectContainer.contains(messageObjectRunning))
				messageObjectContainer.remove(messageObjectRunning);
			// messageObjectContainer.
			this.repaint();

			List<ModelRunUnit> estimates = mri.getAllConvergedUnits();
			if (estimates != null) {
				// TvO: Behavior changed, estimates will not be shown here; this
				// will be done by notifyOfConvergedUnitsChanged.
				if (estimates.size() > 0) {
					// updateShownEstimates(estimates.get(0));
					// showingEstimate = estimates.get(0);
					// parameterView.setParameterReader(estimates.get(0));
					// graph.updateWithEstimates(estimates.get(0));
					// this.redraw();
				}
			} else {
				System.err.println("PARAMETER SET IS NULL (FROM BACKEND). STATUS RESULTSVALID");
			}
			// redraw();

		} else if (status == Status.DEAD) {
			if (messageObjectContainer.contains(messageObjectRunning))
				messageObjectContainer.remove(messageObjectRunning);
			MessageObject messageObjectError = new MessageObject(
					"All computations in the backend have been terminated!", ImageLoaderWorker.ERROR);
			messageObjectContainer.addOnce(messageObjectError);
			this.repaint();
		} else if (status == Status.RESETTING) {
			showingEstimate = null;
			if (messageObjectContainer.contains(messageOverspecified)) {
				messageObjectContainer.remove(messageOverspecified);
			}
			// messageObjectContainer.clear();
			// recheckModelState();

			// messageObjectRunning.set

		} else if (status == Status.WAITING) {

			// Nothing to do

		}

	}

	public void clearModel() {
		/*
		 * for (Edge edge : graph.getEdges()) { mri.requestRemoveEdge(edge); } for (Node
		 * node : graph.getNodes()) { mri.requestRemoveNode(node); }
		 */
		int ne = graph.getEdges().size();
		for (int i = 0; i < ne; i++) {
			mri.requestRemoveEdge(graph.getEdges().get(0));
		}
		int nn = graph.getNodes().size();
		for (int i = 0; i < nn; i++) {
			mri.requestRemoveNode(graph.getNodes().get(0));
		}
	}

	private void connect(Node dragNode, Node node, MouseEvent arg0) {

		// only allow self-reference if we are sure
		// that drag was not an accidentally gone wrong right-click
		if ((dragNode == node) & (System.currentTimeMillis() - lastMousePressedTime < 200)) {
			return;
		}

		// do not allow edges to mean triangles
		if (node.isMeanTriangle()) {
			return;
		}

		// multi group edge ?
		List<Integer> sourceGroups = nodeGroupManager.getActiveGroupMembership(dragNode);
		List<Integer> targetGroups = nodeGroupManager.getActiveGroupMembership(node);

		MainFrame.undoStack.startCollectSteps();

		if (sourceGroups.size() > 0 || targetGroups.size() > 0) {

			List<Node> source = new ArrayList<Node>();
			List<Node> target = new ArrayList<Node>();

			if (targetGroups.size() == 1) {
				target = nodeGroupManager.get(targetGroups.get(0));
			} else {
				target.add(node);
			}

			if (sourceGroups.size() == 1) {
				source = nodeGroupManager.get(sourceGroups.get(0));
			} else {
				source.add(dragNode);
			}

			if (arg0.isAltDown() && source instanceof NodeGroup && target instanceof NodeGroup) {
				// connect 1:1

				((NodeGroup) source).sort();
				((NodeGroup) target).sort();

				int k = Math.min(source.size(), target.size());

				for (int i = 0; i < k; i++) {
					Node from = source.get(i);
					Node to = target.get(i);
					Edge edge = new Edge(from, to);
					boolean doubleHeaded = arg0.isShiftDown() || dragNode == node
							|| dragType == DRAGTYPE.CREATE_DOUBLEHEADED_PATH;

					// boolean free = arg0.isAltDown() || arg0.isAltGraphDown();

					edge.setDoubleHeaded(doubleHeaded);
					// edge.setFixed(!free);
					this.mri.requestAddEdge(edge);
				}

			} else {

				// connect all
				for (Node from : source) {
					for (Node to : target) {
						Edge edge = new Edge(from, to);
						boolean doubleHeaded = arg0.isShiftDown() || dragNode == node
								|| dragType == DRAGTYPE.CREATE_DOUBLEHEADED_PATH;
						;
						edge.setDoubleHeaded(doubleHeaded);
						this.mri.requestAddEdge(edge);
					}
				}

			}

		} else {

			Edge edge = new Edge(dragNode, node);

			boolean doubleHeaded = arg0.isShiftDown() || dragNode == node
					|| dragType == DRAGTYPE.CREATE_DOUBLEHEADED_PATH;
			edge.setDoubleHeaded(doubleHeaded);

			this.mri.requestAddEdge(edge);

		}

		MainFrame.undoStack.endCollectSteps();
	}

	private void createNewNode(int x, int y, boolean latent) {
		MainFrame.undoStack.startCollectSteps();

		// create a new node and send request to model
		Node node = new Node("x" + nodeIdCounter, latent);
		nodeIdCounter += 1;
		node.setX(x - node.getWidth() / 2);
		node.setY(y - node.getHeight() / 2);

		if (this.lockToGrid)
			node.alignToGrid(gridSize);

		for (ScriptView cv : codeView) {
			cv.delayUpdate = true;
		}

		mri.requestAddNode(node);

		// add with a variance
		Edge edge = new Edge(node, node, true);
		edge.setFixed(false);
		mri.requestAddEdge(edge);

		for (ScriptView cv : codeView) {
			cv.updateDelayed();
		}

		MainFrame.undoStack.endCollectSteps();
	}

	private void createPath(MouseEvent arg0) {
		// did user hit another node?
		Iterator<Node> iterNode = graph.getNodeIterator();
		Node targetNode = null;
		while (iterNode.hasNext()) {
			Node node = iterNode.next();
			if (node.isPointWithin(drawEdgeToX, drawEdgeToY)) {
				targetNode = node;
				break;
			}
		}

		boolean ok = true;
		// Connect !
		if (targetNode == null) {
			/*
			 * targetNode = new Node(); targetNode.setX(drawEdgeToX);
			 * targetNode.setY(drawEdgeToY); targetNode.setCaption("x" + nodeIdCounter);
			 * nodeIdCounter++; this.getModelRequestInterface().requestAddNode(targetNode);
			 */
			ok = false;
			dragNode = null;
			drawEdgeToX = -1;
			drawEdgeToY = -1;
			this.repaint();
		}

		if (targetNode != null && targetNode.isMeanTriangle())
			ok = false;
		if (dragNode != null && dragNode.isMeanTriangle() && arg0.isShiftDown())
			ok = false;

		if (ok)
			connect(dragNode, targetNode, arg0);

	}

	@Override
	public void cycleArrowHeads(Edge edge) {
		// edge.setDoubleHeaded(!edge.isDoubleHeaded());
		modelChangedEvent();
		redraw();
	}

	@Override
	public void deleteModel() {
		// called from the backend; pass request on to desktop, which
		// in turn calls dispose() on ModelView

		Desktop.getLinkHandler().unlink(getGraph());
		desktop.removeView(this);
	}

	@Override
	public void dispose() {
		super.dispose();

		this.desktop.removeView(parameterView);

		// remove all child views
		for (View view : codeView) {
			try {
				view.getDesktop().removeView(view);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// prepare for GC
		mri = null;
		this.decorators.clear();
		if (this.codeView != null)
			this.codeView.clear();
		if (this.currentEstimates != null)
			this.currentEstimates.clear();
		if (this.currentEstimatesShownInMenu != null)
			this.currentEstimatesShownInMenu.clear();
		if (this.selectedNodes != null)
			this.selectedNodes.clear();
		this.graph = null;
		this.parameterView = null;

		// this.desktop.remove(parameterView);

		this.parameterView = null;

		Desktop.getLinkHandler().removeLinkListener(this);

		Desktop.getLinkHandler().unlink(this.graph);

		// this.desktop.redraw();
		// this.removeAll();
		// TODO
	}

	private void dragAnchors(MouseEvent arg0) {

		// TODO: align to grid
		/*
		 * if (lockToGrid) { dragNode.alignToGrid(gridSize); }
		 */

		if (dragAnchor == AnchorType.S.ordinal()) {

			int height = dragNode.oldHeight + arg0.getY() - dragNode.getY() - dragNode.clickY;
			dragNode.setHeight(height);
			// this.redraw();
		} else if (dragAnchor == AnchorType.SE.ordinal()) {

			int xdiff = arg0.getY() - dragNode.getY() - dragNode.clickY;
			int ydiff = arg0.getX() - dragNode.getX() - dragNode.clickX;

			if (shiftDown)
				if (Math.abs(xdiff) < Math.abs(ydiff)) {
					xdiff = (int) (ydiff / dragNode.dragRatio);
				} else {
					ydiff = (int) (xdiff * dragNode.dragRatio);
				}

			int height = dragNode.oldHeight + xdiff;
			int width = dragNode.oldWidth + ydiff;

			dragNode.setHeight(height);
			dragNode.setWidth(width);
			this.redraw();
		} else if (dragAnchor == AnchorType.E.ordinal()) {
			int width = dragNode.oldWidth + arg0.getX() - dragNode.getX() - dragNode.clickX;
			dragNode.setWidth(width);
			// this.redraw();
		} else if (dragAnchor == AnchorType.NE.ordinal()) {

			int y = arg0.getY() - dragNode.clickY;
			int h = dragNode.oldHeight + (dragNode.oldY - y);

			if (h > Node.MIN_NODE_HEIGHT) {
				dragNode.setY(y);
				dragNode.setHeight(h);
			}

			int width = dragNode.oldWidth + arg0.getX() - dragNode.getX() - dragNode.clickX;
			dragNode.setWidth(width);

		} else if (dragAnchor == AnchorType.N.ordinal()) {

			int y = arg0.getY() - dragNode.clickY;
			int h = dragNode.oldHeight + (dragNode.oldY - y);

			if (h > Node.MIN_NODE_HEIGHT) {
				dragNode.setY(y);
				dragNode.setHeight(h);
			}

			// this.redraw();

		} else if (dragAnchor == AnchorType.NW.ordinal()) {

			int y = arg0.getY() - dragNode.clickY;
			int h = dragNode.oldHeight + (dragNode.oldY - y);

			if (h > Node.MIN_NODE_HEIGHT) {
				dragNode.setY(y);
				dragNode.setHeight(h);
			}

			int x = arg0.getX() - dragNode.clickX;
			int w = dragNode.oldWidth + (dragNode.oldX - x);
			if (w >= Node.MIN_NODE_WIDTH) {
				dragNode.setX(x);
				dragNode.setWidth(w);
			}

		} else if (dragAnchor == AnchorType.W.ordinal()) {

			int x = arg0.getX() - dragNode.clickX;
			int w = dragNode.oldWidth + (dragNode.oldX - x);
			if (w >= Node.MIN_NODE_WIDTH) {
				dragNode.setX(x);
				dragNode.setWidth(w);
			}

			// this.redraw();

		} else {

			int x = arg0.getX() - dragNode.clickX;
			int w = dragNode.oldWidth + (dragNode.oldX - x);
			if (w >= Node.MIN_NODE_WIDTH) {
				dragNode.setX(x);
				dragNode.setWidth(w);
			}

			int height = dragNode.oldHeight + arg0.getY() - dragNode.getY() - dragNode.clickY;
			dragNode.setHeight(height);

		}

	}

	@Override
	public void dragEnter(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dragExit(DropTargetEvent dte) {
		// TODO Auto-generated method stub

	}

	/*
	 * private void startPathDrag(Node clickedNode, int x, int y) {
	 * 
	 * 
	 * }
	 */

	@Override
	public void dragOver(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	public void addVariablesFromDataset(Dataset dataset, List<Integer> indices, int preferredX, int preferredY)
			throws LinkException {

		setAtomicOperationInProgress(true);
		MainFrame.undoStack.startCollectSteps();

		final int stepsize = 100;

		int xstep = -stepsize;
		int ystep = 0;

		for (int i = 0; i < indices.size(); i++) {

			// create node
			Node node = new Node();

			// locate next position
			boolean positionOK = false;
			while (!positionOK) {
				xstep += stepsize;
				if (preferredX + xstep + gui.Constants.DEFAULT_NODE_SIZE > getWidth()) {
					xstep = 0;
					ystep += stepsize;
				}
				node.setX(preferredX + xstep);
				node.setY(preferredY + ystep);
				// System.out.println("Trying: "+node.getX()+","+node.getY());

				positionOK = true;
				// collision free?
				// for (Node onode : graph.getNodes()) {
				// if (onode.getBoundingBoxOnParent().intersects(node.getBoundingBoxOnParent()))
				// positionOK=false;
				// }

			}

			// System.out.println(node.getX()+","+node.getY());

			// update node

			node.setIsLatent(false);
			mri.requestAddNode(node);

			if (this.lockToGrid)
				node.alignToGrid(gridSize);

			MainFrame.undoStack.add(new LinkStep(node.getObservedVariableContainer(), mri));
			Desktop.getLinkHandler().link(dataset, indices.get(i), node.getObservedVariableContainer(), mri);

			// create a variance
			Edge edge = new Edge(node, node, true);
			edge.setFixed(false);

			// TODO TvO 05 JAN 13: This seems not optimal; the parameter name is first used
			// and checked, and only changed to the default name late.
			edge.setParameterName(node.getCaption() + "-variance");
			mri.requestAddEdge(edge);

		} // end for

		MainFrame.undoStack.endCollectSteps();
		setAtomicOperationInProgress(false);

		// TODO check: TvO, 5JAN13: This line seems necessary, but was not included in
		// the original.
		modelChangedEvent();
	}

	@Override
	public void drop(DropTargetDropEvent dtde) {

		if (isIconified()) {
			dtde.rejectDrop();
			return;
		}

		try {
			Transferable tr = dtde.getTransferable();

			Node existingNode = getNodeAt(dtde.getLocation().x, dtde.getLocation().y);

			Edge existingEdge = null;
			if (existingNode == null) {
				existingEdge = getEdgeAt(dtde.getLocation().x, dtde.getLocation().y, 5);
			}

			// drag&drop of an image (copied to cache)?
			/*
			 * if (dtde.isDataFlavorSupported(DataFlavor.imageFlavor)) { if (existingNode !=
			 * null) { dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
			 * dtde.getDropTargetContext().dropComplete(true); Image imf =
			 * (Image)tr.getTransferData(DataFlavor.imageFlavor); existingNode.image = imf;
			 * repaint(); return; }
			 * 
			 * }
			 */
			// drag&drop of a file to a node
			if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
				if (existingNode != null) {
					dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
					List<File> fileList = (List<File>) tr.getTransferData(DataFlavor.javaFileListFlavor);
					if (fileList != null && fileList.toArray() instanceof File[]) {
						File[] files = (File[]) fileList.toArray();
						existingNode.image = ImageIO.read(files[0]);
						repaint();
						return;
					}

					dtde.getDropTargetContext().dropComplete(true);
				}
			}

			// if existing node is not manifest then reject
			if ((existingNode != null) && (existingNode.isLatent())) {
				dtde.rejectDrop();
				return;
			}

			VariableStack aux = null;
			if (graph.getAuxiliaryStack().isPointWithin(dtde.getLocation().x, dtde.getLocation().y)) {
				aux = graph.getAuxiliaryStack();
				VariableContainer v = aux.addVariableContainer();

				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				dtde.getDropTargetContext().dropComplete(true);

				RawDataset dataset = null;
				try {
					dataset = (RawDataset) tr.getTransferData(TransferableVariableList.datasetFlavor);
				} catch (ClassCastException e) {
					dtde.rejectDrop();
				}
				if (dataset == null)
					return;
				@SuppressWarnings("unchecked")
				List<Integer> index = (List<Integer>) tr.getTransferData(TransferableVariableList.integerListFlavor);

				if (index.size() != 1) {
					dtde.rejectDrop();

					return;
				}

				Desktop.getLinkHandler().link(dataset, index.get(0), v, mri);
				modelChangedEvent();

				System.out.println("AUX detected! Model changed event sent!");
				return;
			}

			// clicked on an edge?

			if (existingEdge != null) {

				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				dtde.getDropTargetContext().dropComplete(true);

				RawDataset dataset = null;
				try {
					dataset = (RawDataset) tr.getTransferData(TransferableVariableList.datasetFlavor);
				} catch (ClassCastException e) {
					dtde.rejectDrop();
				}
				if (dataset == null)
					return;
				@SuppressWarnings("unchecked")
				List<Integer> index = (List<Integer>) tr.getTransferData(TransferableVariableList.integerListFlavor);

				if (index.size() != 1) {
					dtde.rejectDrop();

					return;
				}

				// existingEdge.setDefinitionVariable(dataset, index.get(0), true);

				setUnsavedChanges(true);

				// set definition variable snippet
				// List<Edge> targetEdges = getSelectedEdges();
				Edge targetEdge = existingEdge;
				// for (Edge targetEdge : getSelectedEdges()) {
				targetEdge.setAutomaticNaming(false);
				if (targetEdge.isFree())
					mri.requestSwapFixed(targetEdge);
				mri.requestSetDefinitionVariable(targetEdge, targetEdge.getParameterName());

				// force path name to dataset column name
				targetEdge.setParameterNameByUser(dataset.getColumnName(index.get(0)));

				Desktop.getLinkHandler().link(dataset, index.get(0), targetEdge.getDefinitionVariableContainer(), mri);
				// }
				// --

				this.redraw();

				this.modelChangedEvent();

				return;

			} else { // it's not an edge clicked on...

				/*
				 * dropped on a node or background
				 */

				dtde.acceptDrop(DnDConstants.ACTION_COPY_OR_MOVE);
				dtde.getDropTargetContext().dropComplete(true);

				// TODO: check cast safety

				Dataset dataset = (Dataset) tr.getTransferData(TransferableVariableList.datasetFlavor);

				@SuppressWarnings("unchecked")
				List<Integer> indices = (List<Integer>) tr.getTransferData(TransferableVariableList.integerListFlavor);

				if (existingNode != null) {

					if (indices.size() != 1) {
						dtde.rejectDrop();
						return;
					}

					// TODO the existingNode.isGrouping could be taken out to
					// allow creation of grouping variable by drag & drop.
					// TODO the modelRequestInterface is not informed about the
					// grouping variable. Necessary?
					// TODO grouping is not added to the undoStack.
					if (existingNode.isGrouping()
							&& existingNode.isPointOnGroupingVariable(dtde.getLocation().x, dtde.getLocation().y)) {

						if (dataset instanceof CovarianceDataset) {
							JOptionPane.showMessageDialog(this,
									"Cannot apply covariance dataset to multiple group model");
							return;
						}

						List<Node> relevantNodes;
						// is target node part of a selection?
						if (getSelectedNodes().contains(existingNode)) {
							relevantNodes = getSelectedNodes();
						} else { // otherwise just that node
							relevantNodes = new ArrayList<Node>(1);
							relevantNodes.add(existingNode);
						}

						// Node targetNode = existingNode;
						//
						for (Node targetNode : relevantNodes) {
							Desktop.getLinkHandler().link(dataset, indices.get(0),
									targetNode.getGroupingVariableContainer(), mri);
							targetNode.setGroupingVariable((RawDataset) dataset, indices.get(0));
						}

						setUnsavedChanges(true);

						modelChangedEvent();
					} else {

						MainFrame.undoStack.add(new LinkStep(existingNode.getObservedVariableContainer(), mri));

						// link dataset column to observed variable

						Desktop.getLinkHandler().link(dataset, indices.get(0),
								existingNode.getObservedVariableContainer(), mri);

						// TODO: which is the correct LinkStep ?
						// MainFrame.undoStack.add(new
						// LinkChangedStep(existingNode.getObservedVariableContainer(),this.getModelRequestInterface()));

					}

					modelChangedEvent();

				} else if (aux != null) {

					// VariableContainer cnt = new VariableContainer(this.graph, this);

					/*
					 * TODO TvO: Changed interface according to agreement, needs adaptation here
					 * this.getModelRequestInterface().requestAddAuxiliaryVariable( dataset,
					 * indices.get(0) );
					 */

					this.repaint();

				} else { // existing node is null

					// TvO: Outsources adding of new variables to allow call from other sources.
					addVariablesFromDataset(dataset, indices, dtde.getLocation().x, dtde.getLocation().y);

				}

				desktop.notifyDropEvent();

				redraw();

			}

		} catch (Exception e) {
			// TODO
			e.printStackTrace();
		}
	}

	@Override
	public void dropActionChanged(DropTargetDragEvent dtde) {
		// TODO Auto-generated method stub

	}

	/**
	 * forces display of a tooltip immediately
	 * 
	 * 
	 * @param arg0 a mouse event
	 */
	private void forceTooltip(MouseEvent arg0) {

		int oldDelay = ToolTipManager.sharedInstance().getInitialDelay();
		ToolTipManager.sharedInstance().setInitialDelay(0);

		ToolTipManager.sharedInstance().mouseMoved(arg0);
		ToolTipManager.sharedInstance().setInitialDelay(oldDelay);
	}

	private ModelRunUnit getBestLeastSquaresEstimate() {
		return getBestLeastSquaresEstimate(currentEstimates);
	}

	private ModelRunUnit getBestLeastSquaresEstimate(List<ModelRunUnit> list) {
		if (list == null || list.size() == 0)
			return null;
		ModelRunUnit mru = null;
		for (ModelRunUnit mru2 : list)
			if (!mru2.isMaximumLikelihoodObjective())
				mru = mru2;
		return mru;
	}

	private DecoratorObject getDecoratorAt(int x, int y) {
		for (DecoratorObject d : decorators) {
			if (x >= d.getX() && x <= d.getX() + d.getWidth() && y >= d.getY() && y <= d.getY() + d.getHeight()) {
				return d;
			}
		}
		return null;
	}

	/**
	 * returns Edge with label at coordinate (x,y). If multiple labels are close,
	 * select the last added edge
	 */
	private Edge getEdgeWithLabelAt(int x, int y) {
		Iterator<Edge> iterEdge = graph.getEdgeReverseIterator();
		Edge best = null;
		while (iterEdge.hasNext()) {
			Edge edge = iterEdge.next();
			// System.out.println(edge.getLabel().getWidth()+ "
			// "+edge.getLabel().getHeight());
			if (edge.isOnLabel(x, y)) {
				return (edge);
			}
		}

		return (best);
	}

	/**
	 * returns edge at coordinate (x,y). If multiple edges are close, select the
	 * last added edge (corresponds to top edge when drawing; also see getNodeAt()
	 * for same behavior).
	 */
	private Edge getEdgeAt(int x, int y, double range) {
		// hit an edge?
		Iterator<Edge> iterEdge = graph.getEdgeReverseIterator();
		Edge best = null;
		double bestDist = Double.MAX_VALUE;
		while (iterEdge.hasNext()) {
			Edge edge = iterEdge.next();
			double dist = Math.abs(edge.distanceFromPoint(x, y));

			if (!edge.isDoubleHeaded()) {
				int xMinBound = Math.min(edge.fromX, edge.toX);
				int xMaxBound = Math.max(edge.fromX, edge.toX);

				if (xMaxBound - xMinBound > 4) {
					if ((xMinBound > x) || (x > xMaxBound)) {
						dist = Double.MAX_VALUE;
					}
				} else {
					int yMinBound = Math.min(edge.fromY, edge.toY);
					int yMaxBound = Math.max(edge.fromY, edge.toY);
					if ((yMinBound > y) || (y > yMaxBound)) {
						dist = Double.MAX_VALUE;
					}
				}
			}

			if (dist < bestDist) {
				bestDist = dist;
				best = edge;
			}
		}

		if (bestDist <= range)
			return best;
		else
			return null;
	}

	public File getFile() {
		return file;
	}

	public Graph getGraph() {
		return graph;
	}

	public MessageObjectContainer getMessageObjectContainer() {
		return messageObjectContainer;
	}

	public ModelRequestInterface getModelRequestInterface() {
		return this.mri;
	}

	public String getName() {
		if (mri == null)
			return "(starting)";
		return mri.getName();
	}

	/*
	 * public void setStroke(int stroke) { if (stroke == THINSTROKE)
	 * this.graph.setStroke(Graph.strokeThin, Graph.strokeMedium); else if (stroke
	 * == MEDIUMSTROKE) this.graph.setStroke(Graph.strokeMedium, Graph.strokeThick);
	 * else if (stroke == THICKSTROKE) this.graph.setStroke(Graph.strokeThick,
	 * Graph.strokeVeryThick); MainFrame.updateProps("defaultStroke", "" + stroke);
	 * this.redraw(); }
	 */

	/**
	 * returns node on coordinates (x,y). If there is no node, null is returned. If
	 * multiple nodes are on the same coordinate, the last-added node is selected
	 * (corresponds to top node when drawing the graph).
	 * 
	 * @param x
	 * @param y
	 * @return Node
	 */
	public Node getNodeAt(int x, int y) {

		Iterator<Node> iterNode = graph.getNodeReverseIterator();
		while (iterNode.hasNext()) {
			Node node = iterNode.next();
			if (node.isPointWithin(x, y) || node.isPointOnGroupingVariable(x, y)) {

				return (node);
			}
		}

		return (null);
	}

	public ParameterDrawer getParameterView() {
		return this.parameterView;
	}

	private List<Edge> getSelectedEdges() {
		ArrayList<Edge> list = new ArrayList<Edge>();
		for (Edge edge : graph.getEdges()) {
			if (edge.isSelected())
				list.add(edge);
		}
		return (list);
	}

	private List<Node> getSelectedNodes() {
		ArrayList<Node> list = new ArrayList<Node>();
		for (Node node : graph.getNodes()) {
			if (node.isSelected())
				list.add(node);
		}
		return (list);
	}

	public ParameterReader getShowingEstimate() {
		return showingEstimate;
	}

	/*
	 * this is called each time a tooltip is invoked (non-Javadoc)
	 * 
	 * @see javax.swing.JComponent#createToolTip()
	 */
	@Override
	public JToolTip createToolTip() {

		if (isBayesianEstimate()) {
			// create a custom JToolTip for displaying the posterior distribution
			List<ModelRunUnit> units = mri.getAllConvergedUnits();
			if (units.size() == 0) {
				System.out.println("No converged units!");
				return (super.createToolTip());
			}

			BayesianModelRunUnit bmr = (BayesianModelRunUnit) units.get(0); // TODO this needs to be more generic!
			Chain chain = bmr.getChain();

			// if (chainToolTip==null)
			chainToolTip = new bayes.gui.ChainToolTip(chain);
			chainToolTip.setComponent(this);

			chainToolTip.setParameter(chainToolTipParameterName);

			return (chainToolTip);
		} else {
			// for non-Bayesian estimates, create a standard text-based tooltip
			return (super.createToolTip());
		}

	}

	private boolean isBayesianEstimate() {
		// TODO needs to be improved
		return (this.mri.getStrategy() == Strategy.MCMC);
	}

	@Override
	public String getToolTipText(MouseEvent event) {

		int x = event.getX();
		int y = event.getY();

		if (isBayesianEstimate()) {

			List<ModelRunUnit> units = mri.getAllConvergedUnits();
			if (units.size() == 0)
				return "Chain not converged";

			Edge edge = getEdgeAt(x, y, 4);
			if (edge != null) {
				String paramName = edge.getParameterName();
				this.chainToolTipParameterName = paramName;
				/*
				 * if (chainToolTip != null) chainToolTip.setParameter(paramName); else
				 * System.err.println("getTooltipText() - chainToolTip is null!");
				 */
			}

			// return("NONE");

			return ("NONE");
		} else {

			MessageObject mo = messageObjectContainer.getMessageObjectAt(x, y);

			if (mo != null) {
				return mo.getTextMessage();
			}

			Node node = getNodeAt(x, y);

			if (node != null) {
				String idString = " (#" + node.getId() + ")";
				if (node.isSelected()) {
					List<Node> selected = getSelectedNodes();
					if (selected.size() >= 1) {
						boolean isConnected = true;
						for (Node n : selected)
							if (!n.isConnected())
								isConnected = false;
						String erg = "<html>";
						if (isConnected && combinedData != null)
							erg += combinedData.getDataDistributionString(selected);
						/*
						 * { int[] columnIDs = new int[selected.size()]; String[] varNames = new
						 * String[selected.size()]; int i = 0; for (Node n : selected) { varNames[i] =
						 * n.getCaption(); columnIDs[i++] = Desktop.getLinkHandler()
						 * .getDatasetField(n.getObservedVariableContainer()).columnId; } erg +=
						 * Desktop.getLinkHandler().getDatasetField(
						 * node.getObservedVariableContainer()).dataset.getDataDistribution(columnIDs,
						 * varNames);
						 * 
						 * if (erg.length() > 0) erg += "<br><br>"; }
						 */
						erg += mri.getModelDistribution(selected, this.showingEstimate);
						return erg + "</html>";
					}
				}

				if (node.isConnected()) {
					DatasetField df = Desktop.getLinkHandler().getDatasetField(node.getObservedVariableContainer());

					double pr2 = computeR2(node);
					// calculate pseudo R^2 (only if component has a residual variance component)
					String pseudor2 = "";
					if (!Double.isNaN(pr2)) {

						pseudor2 = "\nR^2: " + Math.round(pr2 * 1000) / 1000.0 + "\n";
					}

					return "<html>" + df.dataset.getColumnTooltip(df.columnId) + pseudor2 + "</html>";
				} else {
					if (node.isLatent()) {

						Edge covEdge = null;
						for (Edge edge : graph.getEdges()) {
							if (edge.getSource() == edge.getTarget() && edge.getTarget() == node) {
								covEdge = edge;
								break;
							}
						}
						String pseudor2 = "";
						if (covEdge != null) {
							double modeVar = EngineHelper.getModelCovariance(this, node.getCaption());
							double pr2 = 1 - (covEdge.getValue() / modeVar);
							pseudor2 = "\nR^2: " + Math.round(pr2 * 1000) / 1000.0 + "\n";
						}

						// if (pr2==0.0)

						return "Latent variable " + node.getCaption() + idString + "\n" + pseudor2;
					} else if (node.isMeanTriangle()) {
						return "Constant term";
					} else {
						return "Observed variable " + node.getCaption() + idString + " without associated data";
					}
				}
			}

			// TODO edge check
			Edge edge = getEdgeAt(x, y, 4);
			if (edge != null) {

				if (edge.isDefinitionVariable()) {
					String str = "Definition variable ";
					if (edge.getDefinitionVariableContainer().isConnected()) {

						DatasetField df = edge.getDefinitionVariableContainer().getLinkedDatasetField();

						str += df.dataset.getColumnName(df.columnId);
					} else {
						str += " [unconnected]";
					}

					return (str);
				}

				String name = edge.getParameterName();
				if (name.equals(""))
					name = "<unnamed>";
				String str = name + "=" + edge.getValue();
				if (showStandardizedEstimates) {
					str += "(" + edge.getStandardizedValue() + ") ";
				}

				if (edge.isFixed()) {
					str += " [fixed]";
				}

				return (str);
			}

			return null;
			// return super.getToolTipText(event);
		}
	}

	public double computeR2(Node node) {

		DatasetField df = Desktop.getLinkHandler().getDatasetField(node.getObservedVariableContainer());

		if (df == null)
			return (Double.NaN);

		// TODO: this should happen somewhere else
		// (1) check whether the variable has a residual variance component
		Edge varianceComponent = node.getVarianceComponent(this.getGraph());

		if (varianceComponent == null)
			return (Double.NaN);

		/*
		 * double obsVar = df.dataset.getColumnStandardDeviation(df.columnId);
		 * double[][] cov = EngineHelper.getCovarianceMatrix(this, true); OnyxModel
		 * model = this.getModelRequestInterface().getModel(); String[] nms =
		 * model.getObservedVariableNames(); String colNm =
		 * df.dataset.getColumnName(df.columnId); int fInd = -1; for (int i=0; i <
		 * nms.length; i++) { if (nms[i].equals(colNm)) {fInd=i; break;} } if (fInd !=
		 * -1) obsVar = cov[fInd][fInd]; else obsVar = Double.NaN;
		 */
		// obsVar = obsVar*obsVar;

		double modeVar = EngineHelper.getModelCovariance(this, node.getCaption());

		double pr2 = 1 - (varianceComponent.getValue() / modeVar);
		return pr2;
	}

	@Override
	public Object getTransferData(DataFlavor flavor) {
		if (flavor.equals(DataFlavor.imageFlavor)) {
			JPEGExport exp = new JPEGExport(this, false);
			return exp.getImage();
		}
		return null;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[] { DataFlavor.imageFlavor };
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {

		updateFromPopupMenu(arg0);
	}

	public boolean isAtomicOperationInProgress() {
		return atomicOperationInProgress;
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor arg0) {
		return arg0.equals(DataFlavor.imageFlavor);
	}

	public boolean isGridShown() {
		return showGrid;
	}

	public boolean isUnsavedChanges() {
		return unsavedChanges;
	}

	@Override
	/**
	 * If Modelview does not consume a key event, it is passed on to the Desktop
	 */
	public void keyPressed(KeyEvent arg0) {

		super.keyPressed(arg0);

		// System.out.println("Model view key pressed "+arg0);

		commandOrControlDown = arg0.isControlDown() || arg0.isMetaDown();
		shiftDown = arg0.isShiftDown();

		/*
		 * direct typing of edge values with number keys
		 */
		Edge edgeUnderMouse = getEdgeAt(mouseAtX, mouseAtY, EDGE_CLICK_RADIUS);
		if (edgeUnderMouse != null) {
			if (directTypeEdge != edgeUnderMouse) {
				directTypeBuffer = "";
			}

			directTypeEdge = edgeUnderMouse;

		}

		/*
		 * control curvature of edge with up/down arrows
		 */

		for (Edge edge : graph.getEdges()) {

			if (!edge.isSelected())
				continue;

			if (arg0.getKeyCode() == 38) { // arrow
				MainFrame.undoStack.add(new EdgeStateChangedStep(this, edge));
				edge.setCurvature(edge.getCurvature() + 10);
			} else if (arg0.getKeyCode() == 40) { // arrow

				// if (directTypeEdge != null) {
				MainFrame.undoStack.add(new EdgeStateChangedStep(this, edge));
				edge.setCurvature(edge.getCurvature() - 10);
				// }
			}

		}
		this.redraw();

		/*
		 * C&C-style assign variables to a group
		 */
		int asciiCode = (int) arg0.getKeyCode();
		if (asciiCode >= 48 && asciiCode <= 57) {

			int groupId = asciiCode - 48;

			if (commandOrControlDown) {

				NodeGroup nodeGroup = new NodeGroup(getSelectedNodes());
				if (!nodeGroupManager.contains(nodeGroup)) {
					nodeGroupManager.set(groupId, nodeGroup);
				}
				nodeGroupManager.setActive(groupId, true);
				repaint();

			} else if (arg0.isAltDown()) {

				activateEstimate(groupId);

			} else {

				nodeGroupManager.toggleActive(groupId);

				if (nodeGroupManager.isActive(groupId)) {
					graph.selectAll(false);
					graph.selectNodes(nodeGroupManager.get(groupId), true);
				} else {
					graph.selectNodes(nodeGroupManager.get(groupId), false);
				}

				repaint();
			}
		}

		if (!arg0.isConsumed()) {
			desktop.keyPressed(arg0);
		}

	}

	@Override
	public void keyReleased(KeyEvent arg0) {

		super.keyReleased(arg0);

		commandOrControlDown = arg0.isControlDown() || arg0.isMetaDown();
		shiftDown = arg0.isShiftDown();

		// REMOVE / DELETE key
		if (arg0.getKeyCode() == KeyEvent.VK_BACK_SPACE || arg0.getKeyCode() == KeyEvent.VK_DELETE) {
			removeAllSelectedNodesAndEdges();
			arg0.consume();
		}

		/*
		 * if (commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_L) {
		 * LabelDecorator ld = new LabelDecorator(); this.addDecorator(ld);
		 * ld.setX(mouseAtX); ld.setY(mouseAtY); }
		 */

		if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			int prevDelay = ToolTipManager.sharedInstance().getInitialDelay();
			ToolTipManager.sharedInstance().setInitialDelay(0);
			// ToolTipManager.sharedInstance().setInitialDelay(1000);
			ToolTipManager.sharedInstance().mouseMoved(new MouseEvent(this, 0, 0, 0,
					// 0, 0, // X-Y of the mouse for the tool tip
					mouseAtX, mouseAtY, 0, false));
			ToolTipManager.sharedInstance().setInitialDelay(prevDelay);
			arg0.consume();
		}

		/*
		 * experimental: time series plot
		 */
		if (commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_Q) {
			// new TimeseriesMeanPlot(this.getModelRequestInterface());
		}

		/*
		 * cycle through presets with CMD+L
		 */
		if (commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_L) {
			int style = 0;
			for (int i = 0; i < presets.length; i++) {
				if (presets[i] == getGraph().graphStyle) {
					style = i;
				}
			}
			style += 1;
			style %= presets.length;
			getGraph().graphStyle = presets[style];
			getGraph().graphStyle.apply(getGraph(), this.showingEstimate);

			repaint();
			arg0.consume();
		}

		/*
		 * if (commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_D) {
		 * 
		 * this.repaint(); }
		 */

		if (commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_R) {
			this.parameterView.stateOpen = !this.parameterView.stateOpen;
			this.parameterView.redraw();
			arg0.consume();
		}

		if (arg0.getKeyCode() == KeyEvent.VK_EQUALS) {
			for (Edge edge : getSelectedEdges()) {
				this.getModelRequestInterface().requestSwapFixed(edge);
			}
			arg0.consume();
		}

		/*
		 * flip selected sub graph with CMD+F
		 */
		if (commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_F) {

			MultiStep ms = new MultiStep();

			for (Node node : graph.getSelectedNodes()) {
				ms.add(new NodeStateChangedStep(this, node));
			}

			MainFrame.undoStack.add(ms);

			if (arg0.isShiftDown()) {
				graph.getSelectedNodes().flipVertically(graph);
			} else {
				graph.getSelectedNodes().flipHorizontally(graph);
			}
			graph.updateAllEdges();
			setUnsavedChanges(true);
			repaint();
			arg0.consume();

		}

		if (commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_G) {
			toggleShowGrid();
			lockToGrid = this.showGrid;
			arg0.consume();
		}

		/*
		 * select all with CMD+A
		 */
		if (commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_A) {
			selectAll();
			arg0.consume();
		}

		if (commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_Z) {
			MainFrame.undoStack.undo();
			repaint();
			arg0.consume();
		}

		if (commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_Y) {
			MainFrame.undoStack.redo();
			repaint();
			arg0.consume();
		}

		// save file
		if (commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_S) {

			if (file != null) { // SAVE
				XMLExport export = new XMLExport(this);
				export.export(this.file);
				desktop.mainFrame.addToRecentFiles(this.file);
				setUnsavedChanges(false);
			} else { // SAVE AS
				XMLExport export = new XMLExport(this);
				this.file = export.export();
				desktop.mainFrame.addToRecentFiles(this.file);
				setUnsavedChanges(false);
			}
			arg0.consume();
		}

		if (commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_T) {
			graph.tidyUp();
			this.repaint();
			arg0.consume();

		}

		if ((commandOrControlDown) && arg0.getKeyCode() == KeyEvent.VK_K) {
			ScriptView view = new ScriptView(desktop, this, new OpenMxExport(this));
			this.codeView.add(view);
			this.desktop.add(view);
			arg0.consume();
		}

		// boolean cmd = arg0.isControlDown() || arg0.isMetaDown();

		if ((commandOrControlDown) && arg0.getKeyCode() == KeyEvent.VK_M) {
			// boolean b =
			for (Edge edge : getSelectedEdges()) {
				edge.setActiveControl(!edge.getActiveControl());
			}
			arg0.consume();
		}

		// ALT+'1'
		/*
		 * if (arg0.getKeyCode()==KeyEvent.VK_0 && arg0.isAltDown()) {
		 * activateEstimate(0); }
		 * 
		 * if (arg0.getKeyCode()==KeyEvent.VK_1 && arg0.isAltDown()) {
		 * activateEstimate(1); }
		 * 
		 * // ALT+'3' if (arg0.getKeyCode()==KeyEvent.VK_2 && arg0.isAltDown()) {
		 * activateEstimate(2); }
		 */

		if (!arg0.isConsumed()) {
			desktop.keyReleased(arg0);
		}
	}

	@Override
	public void keyTyped(KeyEvent arg0) {

		super.keyTyped(arg0);

		// enable directyping of edge values?
		// if (arg0.getKeyChar()=='=') {
		commandOrControlDown = arg0.isControlDown() || arg0.isMetaDown();

		// System.out.println(arg0);

		Edge edgeUnderMouse = getEdgeAt(mouseAtX, mouseAtY, EDGE_CLICK_RADIUS);

		// delete buffer if edge was changed
		if (edgeUnderMouse != directTypeEdge) {
			directTypeBuffer = "";
		}

		// if (edgeUnderMouse != null) // maybe remove this if-clause
		directTypeEdge = edgeUnderMouse;

		if (edgeUnderMouse != null) {

			/*
			 * if (!directType) { directType = true; directTypeTimestamp =
			 * System.currentTimeMillis(); directTypeBuffer = ""; }
			 */
			// Timeout?
			if (directTypeTimestamp + DIRECTTYPE_TIMEDELAY_BUFFER_RESET < System.currentTimeMillis()) {
				// directType = false;
				directTypeBuffer = "";
			}

			directTypeTimestamp = System.currentTimeMillis();

			// if (directType) {

			/*
			 * if (arg0.getKeyChar()=='+') {
			 * //System.out.println("Update font size"+(edgeUnderMouse.getLabel().
			 * getFontSize()+2)); edgeUnderMouse.getLabel().setFontSize(
			 * edgeUnderMouse.getLabel().getFontSize()+2); repaint(); return; }
			 * 
			 * if (arg0.getKeyChar()=='-') { edgeUnderMouse.getLabel().setFontSize(
			 * Math.max(6,edgeUnderMouse.getLabel().getFontSize()-2)); repaint(); return; }
			 */
			// if (arg0.getKeyChar().equals='') {};
			// if (arg0.getKeyChar() != '=')
			if (directTypeBuffer == null)
				directTypeBuffer = "";
			directTypeBuffer += arg0.getKeyChar();

			try {
				// System.out.println("Parsing Buffer: "+directTypeBuffer);
				Double dvalue = Double.parseDouble(directTypeBuffer);

				// TvO 10.12.2012: Added requestSetValue for fixed edges.
				edgeUnderMouse.setValue(dvalue);
				this.getModelRequestInterface().requestChangeParameterOnEdge(edgeUnderMouse);
				this.getModelRequestInterface().requestSetValue(edgeUnderMouse);
				this.redraw();

			} catch (NumberFormatException e) {
				if (directTypeBuffer != null && directTypeBuffer.length() > 0) {
					if (!directTypeBuffer.equals("-"))
						directTypeBuffer = directTypeBuffer.substring(0, directTypeBuffer.length() - 1);
				}
			} catch (Exception e) {
				e.printStackTrace();
				if (directTypeBuffer != null && directTypeBuffer.length() > 0)
					directTypeBuffer = directTypeBuffer.substring(0, directTypeBuffer.length() - 1);
			}

			arg0.consume();
			return; // TODO - really break here?
			// }

		}

		// pass on event to super-class and desktop
		super.keyTyped(arg0);
		if (!arg0.isConsumed()) {
			desktop.keyTyped(arg0);

		}

	}

	@Override
	public void lostOwnership(Clipboard arg0, Transferable arg1) {
		// Ignored so far
	}

	public List<Edge> getDefinitionEdges() {
		List<Edge> definitionEdges = new ArrayList<Edge>();
		Iterator<Edge> edges = graph.getEdgeIterator();
		while (edges.hasNext()) {
			Edge edge = edges.next();
			if (edge.isDefinitionVariable()) {
				definitionEdges.add(edge);
			}
		}
		return definitionEdges;
	}

	/**
	 * 
	 * check model sanity, collect data and start fit process.
	 * 
	 * @return
	 */
	public boolean modelChangedEvent() {

		// TODO: this is an experimental feature
		// currently deactivated
		// update visual guides
		// updateGuides();

		if (atomicOperationInProgress) {
			atomicModelChangeCount++;
			return false; // what is the definition of the return TODO
		}

		// remove all model messages
		messageObjectContainer.clear();
		redraw();

		// clear drawer
		parameterView.clear();

		// check whether AUX variables
		VariableStack aux = graph.getAuxiliaryStack();
		VariableStack ctr = graph.getControlStack();

		double[][] auxRaw = aux.getRawData(Desktop.getLinkHandler(), graph);
		double[][] ctrRaw = ctr.getRawData(Desktop.getLinkHandler(), graph);

		// assemble dataset

		combinedData = new CombinedDataset(Desktop.getLinkHandler(), graph.getNodes(), getDefinitionEdges(),
				graph.getMeanTreatment() != Graph.MeanTreatment.explicit);
		if (!combinedData.allObservedConnected)
			messageObjectContainer.addOnce(msgConnectObservedVariables);
		if (combinedData.anzObs == 0)
			messageObjectContainer.addOnce(this.messageAddVariables);
		if (combinedData.isAllConnected() && combinedData.anzObs > 0 && combinedData.anzPer == 0)
			messageObjectContainer.addOnce(this.messageDatasetHasNoParticipants);
		if (combinedData.hasMultipleDatasetsPartlyWithoutId())
			messageObjectContainer.addOnce(messageMultipleDatasetsWithoutID);
		if (combinedData.hasNonPositiveDefiniteCovarianceDataset)
			messageObjectContainer.addOnce(messageNonPositiveDefiniteDataSet);
		if (!combinedData.allRawHaveMeanZero && graph.getMeanTreatment() == Graph.MeanTreatment.ambique) {
			graph.setMeanTreatment(Graph.MeanTreatment.implicit);
			mri.setMeanTreatment(graph.getMeanTreatment());
			messageObjectContainer.addOnce(messageSwitchedToImplicitMeanTreatment);
		}
		if (combinedData.hasCovarianceDatasets && (combinedData.hasDefinitionVariables() || combinedData.hasGroups)) {
			messageObjectContainer.addOnce(messageNoGroupsOrDefinitionOnCovariance);
		} else {
			if (combinedData.allDefinitionConnected && !combinedData.noMissingInDefinitions)
				messageObjectContainer.addOnce(this.messageDefinitionHasMissing);
			if (!combinedData.allDefinitionConnected)
				messageObjectContainer.addOnce(msgConnectDefinitionVariables);
			if (!combinedData.allGroupingConnected)
				messageObjectContainer.addOnce(msgConnectGroupVariables);
			if (!combinedData.noMissingInGroupings)
				messageObjectContainer.addOnce(this.messageGroupingHasMissing);
		}

		// if (combinedData.)

		if (combinedData.isValidForRawFit()) {
			if (combinedData.getCentralizationMeans() != null)
				mri.setImplicitlyEstimatedMeans(combinedData.getCentralizationMeans());
			messageObjectContainer.addOnce(messageObjectRunning);

			if (!combinedData.isAllColumnsNonNA()) {

				messageObjectContainer.addOnce(messageObjectNA);
				return false;
			}

			// check number of all missing cases
			int numcases = combinedData.getNumCompletelyMissingCases();
			if (numcases > 0) {
				messageObjectContainer.addOnce(messageObjectAllMissing);
				messageObjectAllMissing.setTextMessage("There are " + numcases
						+ " data row(s) without any observed data! This may bias your fit statistics!");
				// System.out.println("NUMCASES > 0");
			} else {
				// ...
			}

			// TODO TvO to AB: at this position, you can call mri.triggerRun with four data
			// sets, data, definition, auxiliary, and control.
			if (combinedData.hasDefinitionVariables()) {
				mri.triggerRun(combinedData.rawData, combinedData.definitionData, auxRaw, ctrRaw);
			} else {
				mri.triggerRun(combinedData.rawData, null, auxRaw, ctrRaw);
			}

			this.repaint();
			return true;
		}
		if (combinedData.isValidForMomentFit()) {
			messageObjectContainer.addOnce(messageObjectRunning);
			mri.triggerRun(combinedData.cov, combinedData.mean, combinedData.anzPer);
			this.repaint();
			return true;
		}
		return false;

		/*
		 * TvO 13 MAR 14: PART BELOW DEPRECATED
		 * 
		 * // are all groups set? // TvO 09 MAR 14: The commented lines seemed wrong,
		 * replaced by addOnce and return false. for (Node node : graph.getNodes()) { if
		 * (node.isGrouping()) { if (!node.isGroupingVariableConnected()) {
		 * messageObjectContainer.addOnce(msgConnectGroupVariables); return false; } //
		 * if (!messageObjectContainer.contains(msgConnectGroupVariables)) //
		 * messageObjectContainer // .add(msgConnectGroupVariables); // // if
		 * (!node.isGroupingVariableConnected()) return false; } }
		 * 
		 * if (data == null) return false;
		 * 
		 * // assemble definition data; TvO: Outsourced to method. List<Edge>
		 * definitionEdges = getDefinitionEdges();
		 * 
		 * //System.out.println(definitionEdges);
		 * 
		 * // - assemble data matching definition edges double[][]
		 * definitionVariableData = null; if (data != null) { definitionVariableData =
		 * new double[data.length][definitionEdges .size()]; for (int i = 0; i <
		 * definitionEdges.size(); i++) { RawDataset dataset =
		 * definitionEdges.get(i).definitionDataset; int index =
		 * definitionEdges.get(i).definitionColumn; if
		 * (definitionEdges.get(i).definitionDataset == null) return(false); for (int j
		 * = 0; j < data.length; j++) { definitionVariableData[j][i] = dataset.get(j,
		 * index); } } }
		 * 
		 * if (data != null) { if (definitionEdges.isEmpty()) { mri.triggerRun(data); }
		 * else { // mri.triggerRun(data, definitionVariableData, //
		 * definitionEdges.toArray(new Edge[] {})); mri.triggerRun(data,
		 * definitionVariableData); }
		 * 
		 * } // mri.triggerRun(data); //mri.triggerRun(dataCov, dataMean, anzPer);
		 * 
		 * messageObjectContainer.addOnce(messageObjectRunning); this.repaint();
		 * 
		 * return (true);
		 */
	}

	private void updateGuides() {

		// collect horizontal & vertical guides
		guides_horizontal.clear();
		guides_vertical.clear();

		for (Node node : graph.getNodes()) {
			guides_horizontal.addElement(node.getYCenter());
			guides_vertical.addElement(node.getYCenter());
		}

	}

	public void mouseClicked(MouseEvent arg0) {

		// ABX
//		System.out.println(arg0.getWhen()+":"+arg0.getClickCount()+"Clicked"+arg0);

		this.requestFocus();

		super.mouseClicked(arg0);
		if (arg0.isConsumed()) {

			return;
		}

		if (isIconified()) {
			// desktop.mouseClicked(arg0);
			return;
		}

		// was this a left-click on the model-view ?

		if (Utilities.isLeftMouseButton(arg0)) {

			Node selectedNode = getNodeAt(arg0.getX(), arg0.getY());
			if (selectedNode == null) {

				if (!messageObjectContainer.isPointWithin(arg0.getX(), arg0.getY())) {

					if (arg0.getClickCount() == 2) {

						if (pressedEdge == null && pressedNode == null)

							createNewNode(arg0.getX(), arg0.getY(), !arg0.isShiftDown());

					} else {

					}

				} else {
					// force a tooltip on single-left click on message object

					this.forceTooltip(arg0);
				}
			} else {

				// left-click on node, toggles selection
				// selectedNode.setSelected(!selectedNode.isSelected());
				// redraw();
				// System.out.println("T");

			}

		}

		// update selected nodes
		// selectedNodes = getSelectedNodes(); no!

		// System.out.println("right mouse:" + Utilities.isRightMouseButton(arg0));

		// otherwise: context !
		if (Utilities.isRightMouseButton(arg0)) {

			// this.desktop.requestFocus();
			// this.desktop.mainFrame.requestFocus();

			populateMenu(arg0);

		}

	}

	public void mouseDragged(MouseEvent arg0) {

		// move drag over to desktop
		desktop.mouseDraggedOnModelView(this, arg0);

		// System.out.println("DRAG"+dragType+","+arg0);

		dragDetected = true;

		// if no node is selected, go to default drag behavior from View class
		if (dragType == DRAGTYPE.NONE) {
			super.mouseDragged(arg0);
			arg0.consume();
			return;
		} else {

			if (isIconified()) {
				return;
			}

			// left-drags for anchors, edge labels and control points
			if (Utilities.isLeftMouseButton(arg0)) {

				if (dragType == DRAGTYPE.MOVE_ANCHOR) {
					dragAnchors(arg0);
					this.redraw();
				} else if (dragType == DRAGTYPE.MOVE_VARIANCE) {

					Node node = pressedEdge.getSource();
					float dx = arg0.getX() - node.getXCenter();
					float dy = arg0.getY() - node.getYCenter();
					double ang = (1 - Math.atan2(dy, dx) / Math.PI) * 180;

					if (ang >= 45 && ang < 135)
						pressedEdge.arcPosition = Node.SOUTH;
					else if (ang >= 135 && ang <= 215)
						pressedEdge.arcPosition = Node.EAST;
					else if (ang > 215 && ang <= 305)
						pressedEdge.arcPosition = Node.NORTH;
					else
						pressedEdge.arcPosition = Node.WEST;

					pressedEdge.arcPositionAutoLayout = false; // mode to a different position; needs to be called only
																// once...

					this.redraw();

				} else if (dragType == DRAGTYPE.MOVE_EDGE_LABEL) {

					// drag label here

					double dy;

					// decide to which direction of the edge the label is moved
					if (draggedLabelEdge.source.getY() < draggedLabelEdge.target.getY()) {
						dy = (-mouseClickY + arg0.getY()) / 100.0 + edgeLabelRelativePositionPrev;
					} else {
						dy = (+mouseClickY - arg0.getY()) / 100.0 + edgeLabelRelativePositionPrev;
					}
					dy = Math.max(Math.min(dy, 0.9), 0.1);

					this.draggedLabelEdge.edgeLabelRelativePosition = dy;
					this.redraw();

				}

				else if (dragType == DRAGTYPE.MOVE_CONTROL) {
					dragCtrl.ctrlAutomatic = false;
					dragCtrl.setCtrlPoint(dragCtrlInt, arg0.getX(), arg0.getY());

					this.redraw();
				}
			}

			// move nodes with left button
			if (dragType == DRAGTYPE.MOVE_NODES) {

				int dragStartX = dragNode.getX();
				int dragStartY = dragNode.getY();

				if (selectedNodes == null) {
					selectedNodes = new ArrayList<Node>();
				}

				for (Node node : selectedNodes) {

					// otherwise drag node
					int toX = arg0.getX() - dragNode.clickX + (-dragStartX + node.getX());
					int toY = arg0.getY() - dragNode.clickY + (-dragStartY + node.getY());

					// do not allow leaving the drawable area with a
					// node
					int pad = 8;
					toX = Math.max(pad, toX);
					toY = Math.max(pad, toY);
					toX = Math.min(toX, this.getWidth() - node.getWidth() - pad);
					toY = Math.min(toY, this.getHeight() - node.getHeight() - pad);

					if (lockToGrid) {
						toX = Math.round(toX / gridSize) * gridSize;
						toY = Math.round(toY / gridSize) * gridSize;
					}

					node.setX(toX);
					node.setY(toY);

					// clever update?
					for (Edge edge : graph.getEdges()) {
						if (edge.source == node || edge.target == node) {
							graph.cleverEdgeLabelLayout(edge);
						}
					}

					// update guides
					guide_vertical_active = -1;
					guide_horizontal_active = -1;
					for (int gv : guides_vertical) {
						if (Math.abs(node.getYCenter() - gv) < 5)
							guide_vertical_active = gv;
					}
					for (int gv : guides_horizontal) {
						if (Math.abs(node.getXCenter() - gv) < 5)
							guide_horizontal_active = gv;
					}

				}
				this.redraw();

				// }

			}

			// any button
			if (dragType == DRAGTYPE.CREATE_SINGLEHEADED_PATH || dragType == DRAGTYPE.CREATE_DOUBLEHEADED_PATH) {

				drawEdgeToX = arg0.getX();
				drawEdgeToY = arg0.getY();
				redraw();

			}

		}

		// decorator
		if (dragDecorator != null) {
			// otherwise drag node
			int toX = arg0.getX() - dragDecorator.clickX;
			// + (-dragStartX + dragDecorator.getX());
			int toY = arg0.getY() - dragDecorator.clickY;
			// + (-dragStartY + dragDecorator.getY());

			dragDecorator.setX(toX);
			dragDecorator.setY(toY);
		}

	}

	public void mouseMoved(MouseEvent arg0) {

		super.mouseMoved(arg0);

		this.mouseAtX = arg0.getX();
		this.mouseAtY = arg0.getY();

		Node hit = null;
		for (Node node : graph.getNodes()) {
			if (node.isPointWithin(mouseAtX, mouseAtY)) {
				hit = node;

				this.setCursor(handCursor);

				DatasetField df = Desktop.getLinkHandler().getDatasetField(node.getObservedVariableContainer());
				if (df == null)
					continue;
				for (View view : desktop.getViews()) {
					if (view instanceof DataView) {
						((DataView) view).highlight(-1);
						if (((DataView) view).getDataset() == df.dataset) {
							((DataView) view).highlight(df.columnId);
							// break;
						}
					}
				}
				break;
			}
		}

		for (Edge edge : graph.getEdges()) {
			if (edge.distanceFromPoint(mouseAtX, mouseAtY) < EDGE_CLICK_RADIUS) {
				this.setCursor(handCursor);
				break;
			}
		}

		/*
		 * if (hit == null) {
		 * MainFrame.getContextHelpPanel().setHelpID(ContextHelpPanel.CTXT_) }
		 */

		if (dragType == DRAGTYPE.CREATE_SINGLEHEADED_PATH || dragType == DRAGTYPE.CREATE_DOUBLEHEADED_PATH) {

			drawEdgeToX = arg0.getX();
			drawEdgeToY = arg0.getY();
			redraw();

		}

	}

	/**
	 * mouse pressed on an object, selects object
	 * 
	 * 
	 */
	public void mousePressed(MouseEvent arg0) {

		super.mousePressed(arg0);

		// System.out.println(arg0);

		lastMousePressedTime = System.currentTimeMillis();

		// update information for potential right-drag
		drawEdgeFromX = arg0.getX();
		drawEdgeFromY = arg0.getY();

		// end left-click path by "add path" from menu
		if (dragType == DRAGTYPE.CREATE_SINGLEHEADED_PATH || dragType == DRAGTYPE.CREATE_DOUBLEHEADED_PATH) {
			createPath(arg0);
			dragType = DRAGTYPE.NONE;
			drawEdgeToX = -1;
			drawEdgeToY = -1;
		}

		setCursor(defaultCursor);
		this.dragAnchor = -1;
		/*
		 * // anchor clicked?
		 */
		Iterator<Node> iterNode = graph.getNodeIterator();
		while (iterNode.hasNext()) {
			Node node = iterNode.next();
			if (node.isSelected()) {
				int dragAnchor = node.getAnchorAtPoint(arg0.getX(), arg0.getY());
				if ((dragAnchor != -1)) {

					dragNode = node;
					dragNode.clickX = arg0.getX() - node.getX();
					dragNode.clickY = arg0.getY() - node.getY();
					dragNode.oldHeight = dragNode.getHeight();
					dragNode.oldWidth = dragNode.getWidth();
					dragNode.oldX = dragNode.getX();
					dragNode.oldY = dragNode.getY();
					dragNode.dragRatio = dragNode.getHeight() / (double) dragNode.getWidth();

					// anchorPressed = true;
					dragType = DRAGTYPE.MOVE_ANCHOR;

					super.selectAction = false;

					this.dragAnchor = dragAnchor;

					switch (dragAnchor) {
					case 0:
						setCursor(resizeNCursor);
						break;
					case 1:
						setCursor(resizeNECursor);
						break;
					case 2:
						setCursor(resizeECursor);
						break;
					case 3:
						setCursor(resizeSECursor);
						break;
					case 4:
						setCursor(resizeSCursor);
						break;
					case 5:
						setCursor(resizeSWCursor);
						break;
					case 6:
						setCursor(resizeWCursor);
						break;
					case 7:
						setCursor(resizeNWCursor);
						break;

					}

				}
			}
		}
		/*
		 * // click on a label?
		 */

		Iterator<Edge> iterEdge = graph.getEdgeIterator();
		boolean breakWhile = false;
		while (!breakWhile && iterEdge.hasNext()) {
			Edge edge = iterEdge.next();
			if (edge.isOnLabel(arg0.getX(), arg0.getY())) {
				// System.out.println("Drag Label!");
				this.draggedLabelEdge = edge;
				this.edgeLabelRelativePositionPrev = edge.edgeLabelRelativePosition;
				this.dragType = DRAGTYPE.MOVE_EDGE_LABEL;
				// The return at this position removed the functionality to select that edge on
				// a right click. TvO replaced it by breakWhile = true
				breakWhile = true;
				// return;
			}
		}

		// clicked a node or edge ?
		pressedNode = getNodeAt(arg0.getX(), arg0.getY());
		if (pressedNode == null) { // let nodes override edges
			pressedEdge = getEdgeAt(arg0.getX(), arg0.getY(), EDGE_CLICK_RADIUS);
			if (pressedEdge != null && pressedEdge.isVarianceEdge())
				dragType = DRAGTYPE.MOVE_VARIANCE;
		} else
			pressedEdge = null;

		// clicked on a decorator?
		dragDecorator = getDecoratorAt(arg0.getX(), arg0.getY());

		boolean pressedCtrl = false;
		// clicked on a ctrl point?
		for (Edge edge : graph.getEdges()) {
			int ct = edge.getCtrlPoint(arg0.getX(), arg0.getY());
			if (ct != -1) {
				dragCtrl = edge;
				dragCtrlInt = ct;
				dragType = DRAGTYPE.MOVE_CONTROL;
				MainFrame.undoStack.add(new EdgeStateChangedStep(this, edge));
				// System.out.println("Drag Point"+arg0.getX()+","+ arg0.getY());
				pressedCtrl = true;
			}

		}

		// remove all selections
		boolean clear = true;
		if ((pressedNode != null) && (pressedNode.isSelected()))
			clear = false;
		if ((pressedEdge != null) && (pressedEdge.isSelected()))
			clear = false;
		if (arg0.isShiftDown())
			clear = false;

		if (clear) {
			for (Node node : graph.getNodes())
				node.setSelected(false);
			for (Edge edge : graph.getEdges())
				edge.setSelected(false);
			this.redraw();
		}

		// TODO: move this block to mouseReleased()
		/*
		 * if (pressedNode != null) { if (pressedNode.isSelected()) { if
		 * (onTheFlyTextArea == null) {onTheFlyTextArea = new OnTheFlyTextField();
		 * this.add(onTheFlyTextArea); }
		 * 
		 * onTheFlyTextArea.setVisible(true);
		 * 
		 * onTheFlyTextArea.setText( pressedNode.getCaption());
		 * onTheFlyTextArea.setSize(onTheFlyTextArea.getPreferredSize());
		 * //onTheFlyTextArea.addText
		 * 
		 * onTheFlyTextArea.setLocation( pressedNode.labelPosX-6,
		 * pressedNode.labelPosY-pressedNode.getFontSize()-6 );
		 * 
		 * onTheFlyTextArea.setFont(pressedNode.getFont()); } else { if (pressedNode !=
		 * dragNode) { if (onTheFlyTextArea!= null) onTheFlyTextArea.setVisible(false);
		 * } }
		 * 
		 * } else { //this.remove(onTheFlyTextArea); onTheFlyTextArea.setVisible(false);
		 * }
		 */

		// click on a node ?
		if (dragType != DRAGTYPE.MOVE_ANCHOR && pressedNode != null) {
			// Node node = clickedNode;
			// if (node.isPointWithin(arg0.getX(), arg0.getY())) {

			dragNode = pressedNode;
			dragNode.clickX = arg0.getX() - pressedNode.getX();
			dragNode.clickY = arg0.getY() - pressedNode.getY();

			dragNode.oldX = dragNode.getX();
			dragNode.oldY = dragNode.getY();
			dragNode.oldWidth = dragNode.getWidth();
			dragNode.oldHeight = dragNode.getHeight();

			pressedNode.setSelected(true);
			this.redraw();

			super.selectAction = false;

			// if (!anchorPressed)
			setCursor(handCursor);

			if (Utilities.isLeftMouseButton(arg0))
				dragType = DRAGTYPE.MOVE_NODES;
			else
				dragType = DRAGTYPE.CREATE_SINGLEHEADED_PATH;
			// dragType = DRAGTYPE.CREATE_PATH;
			// return;
			// }

		}

		// green controls for changing Bezier curves
		if (pressedEdge != null) {

			if (pressedEdge.isSelected()) {

				if (pressedEdge.isDoubleHeaded() && Utilities.isLeftMouseButton(arg0)
						&& pressedEdge.getSource() != pressedEdge.getTarget())
					pressedEdge.setActiveControl(!pressedEdge.getActiveControl());
			}

			pressedEdge.setSelected(true);
			this.redraw();

			super.selectAction = false;
		}

		if (pressedNode == null && pressedEdge == null && !pressedCtrl) {

			for (Edge edge : graph.getEdges())
				edge.setActiveControl(false);

		}

		// if (pressedNode != null) {

		// }

		selectedNodes = getSelectedNodes();
		// System.

		/*
		 * if (dragType != DRAGTYPE.MOVE_CONTROL) { for (Edge edge : graph.getEdges()) {
		 * edge.setActiveControl(false); } }
		 */

		if (pressedEdge == null && pressedNode == null)
			desktop.mousePressedOnModelView(this, arg0);

		/*if (MainFrame.touchBarHandler != null) {
			if (pressedNode != null)
				MainFrame.touchBarHandler.setActiveView(pressedNode);
			else if (pressedEdge != null)
				MainFrame.touchBarHandler.setActiveView(pressedEdge);
			else if (MainFrame.touchBarHandler != null)
				MainFrame.touchBarHandler.setActiveView(this);
		}*/

	}

	public void mouseExited(MouseEvent e) {
		super.mouseExited(e);

		this.mouseAtX = -1;
		this.mouseAtY = -1;
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {

		super.mouseReleased(arg0);

		guide_vertical_active = -1;
		guide_horizontal_active = -1;

		// if (isIconified()) {

		desktop.mouseReleasedOnModelView(this, arg0);
		if (arg0.isConsumed())
			return;

		// abort right-drags
		if (Utilities.isRightMouseButton(arg0) && System.currentTimeMillis() - lastMousePressedTime < 100) {

			// we assume that the user accidentally right-dragged instead of
			// right-clicked, so let's show them a menu if drag length it less
			// 1/X second
			populateMenu(arg0);
			arg0.consume();
		}
		//

		if (!arg0.isConsumed() && dragDetected && (getSelection() != null)) {

			for (Node node : graph.getNodes()) {

				if (node.isWithinRectangle(getSelection())) {

					node.setSelected(true);
				}
			}

			for (Edge edge : graph.getEdges()) {
				if (edge.isWithinRectangle(getSelection())) {
					edge.setSelected(true);
				}
			}

		}

		// update selected nodes
		// selectedNodes = getSelectedNodes(); no!

		// right-drag released?
		if (!arg0.isConsumed() && (!holdPathUntilNextClick) && (drawEdgeToX != -1)) {

			createPath(arg0);

		}

		if (dragNode != null) {
			boolean wh = ((dragNode.oldHeight != dragNode.getHeight() || dragNode.oldWidth != dragNode.getWidth()));
			boolean xy = (dragNode.oldX != dragNode.getX() || dragNode.oldY != dragNode.getY());

			// System.out.println(xy+" "+wh);

			// create UNDO step for reshaping a single node
			if (wh)
				MainFrame.undoStack.add(new ReshapeStep(this, dragNode, dragNode.oldX, dragNode.oldY, dragNode.oldWidth,
						dragNode.oldHeight));

			// create UNDO step for moving multiple nodes
			if (xy && !wh) {
				MainFrame.undoStack.startCollectSteps();
				// int ox = dragNode.oldX;
				for (Node node : getSelectedNodes())
					MainFrame.undoStack.add(new MovedStep(this, node, dragNode.oldX + (node.getX() - dragNode.getX()),
							dragNode.oldY + (node.getY() - dragNode.getY())));
				MainFrame.undoStack.endCollectSteps();
			}

		}

		// reset variables

		draggedLabelEdge = null;
		dragDecorator = null;
		dragAnchor = -1;
		if (!holdPathUntilNextClick) {
			drawEdgeToX = -1;
			drawEdgeToY = -1;
			dragNode = null;
		}
		dragCtrl = null;
		dragDetected = false;

		dragType = DRAGTYPE.NONE;

		if (arg0.getButton() == MouseEvent.BUTTON1) {
			menuContextNode = null;
			menuContextEdge = null;
		}

		if (holdPathUntilNextClick)
			holdPathUntilNextClick = false;

		// repaint
		redraw();

		setCursor(defaultCursor);

	}

	@Override
	public void newData(int percentMissing, boolean isRawData) {
		int[] filter = mri.getModel().filter;
		String[] allNames = getGraph().getAllVariableNames();
		String[] columnNames = new String[filter.length];

		for (int i = 0; i < filter.length; i++)
			columnNames[i] = allNames[filter[i]];

		// create new object and view and add them to desktop
		OnyxModel model = mri.getModel().copy();
		Dataset dataset = null;
		if (isRawData)
			dataset = new SimulatedDataset(Arrays.asList(columnNames), model, percentMissing);
		else {
			model.evaluateMuAndSigma(null);
			dataset = new CovarianceDataset(model.anzPer, model.mu, model.sigma, Arrays.asList(columnNames));
			dataset.setName("Simulated Covariance Data Set");
		}
		addDataset(dataset, -1, -1);
	}

	@Override
	public synchronized void addDataset(double[][] dataset, String datasetName, String[] additionalVariableNames, int x,
			int y) {
		int[] filter = mri.getModel().filter;
		String[] allNames = getGraph().getAllVariableNames();
		String[] columnNames = new String[filter.length + additionalVariableNames.length];

		for (int i = 0; i < filter.length; i++)
			columnNames[i] = allNames[filter[i]];
		for (int i = 0; i < additionalVariableNames.length; i++)
			columnNames[i + filter.length] = additionalVariableNames[i];

		// create new object and view and add them to desktop
		RawDataset rawDataset = new RawDataset(dataset, Arrays.asList(columnNames));
		rawDataset.setName(datasetName);
		addDataset(rawDataset, x, y);
	}

	@Override
	public void addDataset(Dataset dataset, int x, int y) {
		if (dataset.isValid()) {
			DataView dv = new DataView(desktop, dataset);

			if (x != -1 && y != -1)
				dv.setLocation(x, y);
			desktop.add(dv);
			desktop.validate();
			desktop.repaint();
		}
	}

	public void addProgressView(ParallelProcessView view) {
		desktop.add(view);
		desktop.validate();
		desktop.repaint();
	}

	@Override
	public void notifyOfClearWarningOrError(Warning warning) {

		if (warning == Warning.ERROR) {
			if (messageObjectContainer.contains(messageError)) {
				messageObjectContainer.remove(messageError);
				repaint();
			}
		} else if (warning == Warning.COVARIANCESONSTANTSINGULAR) {
			if (messageObjectContainer.contains(messageSingular)) {
				messageObjectContainer.remove(messageSingular);
				repaint();
			}
			// messageObjectContainer.add(messageObjectWarning);
		} else if (warning == Warning.MODELOVERSPECIFIED) {
			// if (m)
			// MessageObject messageObjectWarning
			if (messageObjectContainer.contains(messageOverspecified)) {
				messageObjectContainer.remove(messageOverspecified);
				repaint();
			}
		}

	}

	/**
	 * this is called, whenever another ModelRunUnit has converged. If a
	 * ModelRunUnit is better than any we had before, notify the user of this new
	 * better set of estimates
	 * 
	 */
	@Override
	public void notifyOfConvergedUnitsChanged() {

		if (mri.getAnzConverged() > 0) {

			// did we find the first estimate?
			if ((currentEstimates == null) || (currentEstimates.size() == 0)) {
				ModelRunUnit mru = mri.getAllConvergedUnits().get(0);
				if (showPolicy == showPolicyType.MANUAL
						|| (showPolicy == showPolicyType.BESTML && mru.isMaximumLikelihoodObjective())
						|| (showPolicy == showPolicyType.BESTLS && !mru.isMaximumLikelihoodObjective()))
					updateShownEstimates(mru);
			} else if (ModelRunUnit.convergedComparator.compare(mri.getAllConvergedUnits().get(0),
					currentEstimates.get(0)) == -1
					&& !mri.getAllConvergedUnits().get(0).isSameAs(currentEstimates.get(0))) {

				// We found a better estimate than we had before

				if (sparklingObject == null) {
					sparklingObject = new MessageObject("New results are available!", ImageLoaderWorker.SPARKLING);
				}
				if (!this.messageObjectContainer.contains(sparklingObject)
						&& mri.getBigClockTime() > IMMEDIATEREDRAWTIME) {
					// if (!this.messageObjectContainer.contains(sparklingObject)) {
					messageObjectContainer.add(sparklingObject);
					this.redraw();
				}

				ModelRunUnit mru = mri.getAllConvergedUnits().get(0);
				if (mri.getBigClockTime() < IMMEDIATEREDRAWTIME && showPolicy != showPolicy.BESTLS)
					updateShownEstimates(mru);
				else {
					if (showPolicy == showPolicyType.BESTML && mru.isMaximumLikelihoodObjective())
						updateShownEstimates(mru);
					if (showPolicy == showPolicyType.BESTLS) {
						mru = getBestLeastSquaresEstimate(mri.getAllConvergedUnits());
						if (mru != null)
							updateShownEstimates(mru);
					}
				}

			}

		} else {
			// the converged are reset to empty (restart of estimate)
			updateShownEstimates(mri.getStartingValuesUnit());
		}

		// update current estimates
		currentEstimates = mri.getAllConvergedUnits();
	}

	/*
	 * private void updateEdgeValueFromMenu(DocumentEvent arg0) { try {
	 * menuContextEdge.setValue(Double.parseDouble(arg0.getDocument() .getText(0,
	 * arg0.getDocument().getLength()))); mri.requestSetValue(menuContextEdge);
	 * 
	 * } catch (NumberFormatException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (BadLocationException e) { // TODO
	 * Auto-generated catch block e.printStackTrace(); } modelChangedEvent(); }
	 */

	@Override
	public void notifyOfStartValueChange() {
		// TODO

	}

	@Override
	public void viewPortChanged(ViewPortChangedEvent event) {

		// move all nodes
		for (Node node : graph.getNodes()) {
			node.setX(node.getX() + event.relx);
			node.setY(node.getY() + event.rely);
		}

		repaint();
	}

	@Override
	public void notifyOfWarningOrError(Warning warning) {

		if (warning == Warning.ERROR) {
			if (!messageObjectContainer.contains(messageError)) {
				messageObjectContainer.add(messageError);
				repaint();
			}
		} else if (warning == Warning.COVARIANCESONSTANTSINGULAR) {
			if (!messageObjectContainer.contains(messageSingular)) {
				messageObjectContainer.add(messageSingular);
				repaint();
			}
			// messageObjectContainer.add(messageObjectWarning);
		} else if (warning == Warning.ACCELERATINGCYCLE) {
			if (!messageObjectContainer.contains(messageAcceleratingCycle)) {
				messageObjectContainer.add(messageAcceleratingCycle);
				repaint();
			}
		} else if (warning == Warning.MODELOVERSPECIFIED) {
			// if (m)
			// MessageObject messageObjectWarning
			if (!messageObjectContainer.contains(messageOverspecified)) {
				messageObjectContainer.add(messageOverspecified);
				repaint();
			}
		}

	}

	/**
	 * if one or more nodes were unlinked, show starting values again, and request
	 * invalidation of data set in the runner.
	 * 
	 */
	@Override
	public void notifyUnlink(Graph graph) {

		// are we concerned with this, at all?
		if (graph != this.graph)
			return;

		activateEstimate(0, false);
		mri.requestInvalidateDataSet();

	}

	public void paintBackground(Graphics2D g) {

		DropShadowBorder.paintBackgroundInComponent(this, g, graph.backgroundColor);
	}

	@Override
	public void paintComponent(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		
		// set anti-aliasing
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	     // Set rendering quality
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        // Enable fractional metrics for text
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        
		// paint everything inherited from View (e.g., selection box)
		super.paintComponent(g);

		if (!hideBorderDecorators && isResizable()) {
			Shape movableBorderRect = new java.awt.Rectangle(sizeMoveArea, sizeMoveArea, getWidth() - 2 * sizeMoveArea,
					getHeight() - 2 * sizeMoveArea);
			final Color lgray = new Color(230, 230, 230);
			g2d.setColor(lgray);
			// Stroke os = g2d.getStroke();
			// g2d.setStroke(new BasicStroke(15));
			g2d.draw(movableBorderRect);
			// g2d.setStroke(os);
		}

		if (isIconified()) {

			Rectangle bounds = this.graph.getBoundingBoxFromOrigin();

			// if graph is empty, don't paint anything
			if ((bounds.getWidth() == 0) || (bounds.getHeight() == 0)) {
				return;
			}

			int maxbound = Math.max(bounds.getWidth(), bounds.getHeight());

			Image image = this.createImage(maxbound, maxbound);

			Graphics gtemp = image.getGraphics();
			Graphics2D gtemp2d = (Graphics2D) gtemp;
			gtemp2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			gtemp.setColor(Color.white);

			// double ratio = ICON_SIZE_X / ICON_SIZE_Y;

			int bw, bh;

			bw = maxbound;
			bh = maxbound;

			gtemp.fillRect(0, 0, bw, bh);
			g2d.scale(zoom, zoom);
			gtemp2d.translate(viewPortOffsetx, viewPortOffsety);

			this.graph.draw(gtemp2d, nodeGroupManager);
			g2d.scale(1, 1);
			// gtemp2d.translate(-viewPortOffsetx, -viewPortOffsety);

			final int pad = 16;
			g.drawImage(image, pad, pad, ICON_SIZE_X - pad * 2, ICON_SIZE_Y - pad * 2, this);

			return;
		}

		// draw grid if active
		if (showGrid) {

			g.setColor(Color.LIGHT_GRAY);
			((Graphics2D) g).setStroke(gridStroke);
			int pad = 1 * gridSize;
			for (int x = pad; x < getWidth() - pad; x += gridSize) {
				g.drawLine(x, pad, x, getHeight() - pad);
			}
			for (int y = pad; y < getHeight() - pad; y += gridSize) {
				g.drawLine(pad, y, getWidth() - pad, y);
			}

		}

		// draw guides
		if (guide_horizontal_active != -1) {
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(0, guide_horizontal_active, this.getWidth(), guide_horizontal_active);
		}
		if (guide_vertical_active != -1) {
			g.setColor(Color.LIGHT_GRAY);
			g.drawLine(0, guide_vertical_active, this.getWidth(), guide_vertical_active);
		}

		// draw Graph with all nodes and edges
		this.graph.draw(g2d, nodeGroupManager);

		// draw edge selection for creating a new edge
		if (drawEdgeToX != -1) {
			g.drawLine(drawEdgeFromX, drawEdgeFromY, drawEdgeToX, drawEdgeToY);
		}

		// draw decorators
		for (DecoratorObject dec : decorators) {
			dec.paint(g);
		}

		// g.fillRect(0, 0, 100, 100);

		// draw message objects in top right corner
		if (!hideMessageObjectContainer) {
			messageObjectContainer.setLocation(this.getWidth() - 20 - messageObjectContainer.getWidth(), 20);
			messageObjectContainer.draw(g);
		}

		// development
		/*
		 * for (Edge edge : graph.getEdges()) { g2d.setColor(Color.red);
		 * g2d.draw(edge.getLabelRectangle()); }
		 */

	}

	private void populateMenu(MouseEvent arg0) {
		menu = new JPopupMenu();

		pendingEdgeStateChangedUndoStep = null;
		pendingNodeStateUndoSteps.clear();

		menu.removeAll();

		// node under the mouse? if yes, then context for node
		boolean nodeUnderMouse = false;
		Iterator<Node> iterNode = graph.getNodeIterator();
		while (iterNode.hasNext()) {
			Node node = iterNode.next();
			if (node.isPointWithin(arg0.getX(), arg0.getY())
					|| node.isPointOnGroupingVariable(arg0.getX(), arg0.getY())) {
				nodeUnderMouse = true;

				menuContextNode = node;
				
				break;
			}
		}
		

		Edge edgeUnderMouse = null;
		edgeUnderMouse = getEdgeAt(arg0.getX(), arg0.getY(), EDGE_CLICK_RADIUS);
		if (edgeUnderMouse == null) {
			Edge temp = getEdgeWithLabelAt(arg0.getX(), arg0.getY());
			edgeUnderMouse = temp;
			if (temp != null)
				temp.setSelected(true);
		}
		System.out.println(edgeUnderMouse);
		if (edgeUnderMouse==null)
			System.out.println("Edge set to NULL");
		
		menuContextEdge = edgeUnderMouse;

		// store state of all selected nodes
		for (Node node : getSelectedNodes())
			pendingNodeStateUndoSteps.add(new NodeStateChangedStep(this, node));
		for (Edge edge : getSelectedEdges())
			pendingEdgeStateChangedUndoSteps.add(new EdgeStateChangedStep(this, edge));

		menu.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
				// System.out.println(pen)

				MainFrame.undoStack.startCollectSteps();

				if (pendingNodeStateUndoSteps.size() > 0) {

					// if name has changed, do name change UNDO action
					/*
					 * if (!menuContextNode.getCaption().equals(
					 * pendingNodeStateUndoStep.getPreviousNodeState().getCaption())) {
					 * MainFrame.undoStack.add( new NodeRenameStep(getModelRequestInterface(),
					 * menuContextNode,
					 * pendingNodeStateUndoStep.getPreviousNodeState().getCaption()) ); }
					 */
					// if font size has changed, do statechange

					for (NodeStateChangedStep pendingNode : pendingNodeStateUndoSteps) {

						// font size changed or name changed?
						boolean fontSizeChanged = pendingNode.getPreviousNodeState().getFontSize() != pendingNode
								.getCurrentNodeState().getFontSize();
						boolean nameChanged = !pendingNode.getPreviousNodeState().getCaption()
								.equals(pendingNode.getCurrentNodeState().getCaption());

						// System.out.println(pendingNode.getPreviousNodeState().getCaption()+"
						// "+fontSizeChanged+" "+nameChanged);

						if (fontSizeChanged) {
							MainFrame.undoStack.add(new gui.undo.NodeFontSizeChanged(pendingNode.getCurrentNodeState(),
									pendingNode.getPreviousNodeState().getFontSize()));
						}

						if (nameChanged) {
							MainFrame.undoStack.add(new NodeRenameStep(getModelRequestInterface(), menuContextNode,
									pendingNode.getPreviousNodeState().getCaption()));
						}

					}

				}

				for (EdgeStateChangedStep pendingEdgeState : pendingEdgeStateChangedUndoSteps) {
					boolean fontSizeChanged = pendingEdgeState.getPreviousEdgeState().getLabel()
							.getFontSize() != pendingEdgeState.getCurrentEdgeState().getLabel().getFontSize();

					if (fontSizeChanged) {
						MainFrame.undoStack.add(new gui.undo.EdgeFontSizeChanged(pendingEdgeState.getCurrentEdgeState(),
								pendingEdgeState.getPreviousEdgeState().getLabel().getFontSize()));
					}

					boolean vchange = (pendingEdgeState.getCurrentEdgeState().getValue() != pendingEdgeState
							.getPreviousEdgeState().getValue());
					boolean nchange = !pendingEdgeState.getCurrentEdgeState().getParameterName()
							.equals(pendingEdgeState.getPreviousEdgeState().getParameterName());

					if (vchange || nchange) {
						MainFrame.undoStack.add(pendingEdgeState);
					}

				}

				/*
				 * if (menuContextEdge != null && pendingEdgeStateChangedUndoStep != null) {
				 * 
				 * 
				 * 
				 * 
				 * }
				 */

				/*
				 * if (pendingFontState) {
				 * 
				 * MultiStep ms = new MultiStep();
				 * 
				 * 
				 * for (Edge edge : getSelectedEdges()) { ms.add(new
				 * LabelStateChangedStep(ModelView.this, edge));
				 * edge.getLabel().setFontSize((float)newValue); }
				 * 
				 * for (Node node : getSelectedNodes()) { ms.add(new
				 * NodeStateChangedStep(ModelView.this, node)); node.setFontSize((int)newValue);
				 * }
				 * 
				 * MainFrame.undoStack.add(ms); }
				 */

				MainFrame.undoStack.endCollectSteps();

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent arg0) {
				// TODO Auto-generated method stub

			}
		});

		if (graph.getAuxiliaryStack().isPointWithin(arg0.getX(), arg0.getY())) {

			if (menuAux == null)
				menuAux = new JMenu("Auxiliary Variables");

			if (menuRemoveAllAux == null) {
				menuRemoveAllAux = new JMenuItem("Remove All Auxiliary Variables");
				menuAux.add(menuRemoveAllAux);
				menuRemoveAllAux.addActionListener(this);
				;
			}

			menu.add(menuAux);
		}

		if (nodeUnderMouse) {

			if (!menuContextNode.isMeanTriangle()) {
				nodeNameInput = new LabeledInputBox("Variable Name");
				menu.add(nodeNameInput);
				nodeNameInput.setText(menuContextNode.getCaption());
				nodeNameInput.getDocument().addDocumentListener(this);
			}
			if (menuContextNode.isGrouping()) {
				nodeGroupInput = new LabeledInputBox("Group");
				menu.add(nodeGroupInput);
				nodeGroupInput.setText(Statik.doubleNStellen(menuContextNode.groupValue, 0));
				nodeGroupInput.getDocument().addDocumentListener(this);
			}
			// nodeNameInput.addKeyListener(this);

			if (menuCreatePath == null) {
				menuCreatePath = new JMenu("Add Path");
				menuCreatePath.addActionListener(this);

				menuCreatePathVariance = new JMenuItem("Add Variance");
				menuCreatePathCovariance = new JMenuItem("Add Covariance");
				menuCreatePathRegression = new JMenuItem("Add Regression");
				menuCreatePathVariance.addActionListener(this);
				menuCreatePathCovariance.addActionListener(this);
				menuCreatePathRegression.addActionListener(this);
				menuCreatePath.add(menuCreatePathRegression);
				menuCreatePath.add(menuCreatePathCovariance);
				menuCreatePath.add(menuCreatePathVariance);
			}

			if (menuSwapLatent == null) {
				menuSwapLatent = new JMenuItem("Swap Latent / Manifest");
				menuSwapLatent.addActionListener(this);
			}

			if (menuContextNode.isLatent()) {
				menuSwapLatent.setText("Change to Observed");
			} else {
				menuSwapLatent.setText("Change to Latent");
			}

			menu.add(menuCreatePath);
			menu.add(menuSwapLatent);

			if (menuContextNode.isManifest()) {
				if (menuSwapGrouping == null) {
					menuSwapGrouping = new JMenuItem("");
					menuSwapGrouping.addActionListener(this);
				}
				menuSwapGrouping.setText((menuContextNode.isGrouping() ? "Remove Grouping" : "Add Grouping"));
				menu.add(menuSwapGrouping);

				if (menuSwapNormalized == null) {
					menuSwapNormalized = new JMenuItem("");
					menuSwapNormalized.addActionListener(this);
				}
				menuSwapNormalized
						.setText((menuContextNode.isNormalized() ? "Remove z-Transform" : "Apply z-Transform"));
				menu.add(menuSwapNormalized);
			}

			if (menuUnlinkGrouping == null && menuContextNode.isGrouping()) {
				menuUnlinkGrouping = new JMenuItem("Unlink Grouping");
				menuUnlinkGrouping.addActionListener(this);
			}
			if (menuContextNode.isGrouping())
				menu.add(menuUnlinkGrouping);

			if (!menuContextNode.isLatent()) {
				if (menuUnlinkNode == null) {
					menuUnlinkNode = new JMenuItem("Unlink Variable");
					menuUnlinkNode.addActionListener(this);
				}
				menu.add(menuUnlinkNode);
			}

			// if (menuDeleteNode == null) {
			// menuDeleteNode = new JMenuItem("Delete Variable");
			// menuDeleteNode.addActionListener(this);
			// }
			// menu.add(menuDeleteNode);

			if (menuNodeColor == null) {
				menuNodeColor = new JMenuItem("Change Line Color");
				menuNodeColor.addActionListener(this);
			}

			if (menuNodeFillColor == null) {
				menuNodeFillColor = new JMenuItem("Change Fill Color");
				menuNodeFillColor.addActionListener(this);
			}

			if (menuNodeFontColor == null) {
				menuNodeFontColor = new JMenuItem("Change Font Color");
				menuNodeFontColor.addActionListener(this);
			}

			if (menuNodeFillStyle == null) {
				menuNodeFillStyle = new JMenu("Change Fill Style");
				menuNodeFillStyleGradient = new JMenuItem("Gradient");
				menuNodeFillStyleFill = new JMenuItem("Plain");
				menuNodeFillStyleR2 = new JMenuItem("Boker's R2");
				menuNodeFillStyleHand = new JMenuItem("Hand");
				menuNodeFillStyleNone = new JMenuItem("None");
				menuNodeFillStyle.add(menuNodeFillStyleGradient);
				menuNodeFillStyle.add(menuNodeFillStyleFill);
				menuNodeFillStyle.add(menuNodeFillStyleR2);
				menuNodeFillStyle.add(menuNodeFillStyleHand);
				menuNodeFillStyle.add(menuNodeFillStyleNone);
				menuNodeFillStyleGradient.addActionListener(this);
				menuNodeFillStyleFill.addActionListener(this);
				menuNodeFillStyleNone.addActionListener(this);
				menuNodeFillStyleHand.addActionListener(this);
				menuNodeFillStyleR2.addActionListener(this);
			}

			if (menuAddImage == null) {
				menuAddImage = new JMenuItem("Add Image");
				menuAddImage.addActionListener(this);
			}

			thicknessInput = new LabeledInputBox("Thickness");

			thicknessInput.setText(String.valueOf(menuContextNode.getStrokeWidth()));
			thicknessInput.getDocument().addDocumentListener(this);

			labelsizeInput = new LabeledInputBox("Font Size");

			labelsizeInput.setText(String.valueOf(menuContextNode.getFontSize()));
			labelsizeInput.getDocument().addDocumentListener(this);

			JMenu Customize = new JMenu("Customize Variable");
			menu.add(Customize);
			Customize.add(labelsizeInput);
			Customize.add(thicknessInput);
			Customize.addSeparator();
			Customize.add(menuNodeColor);
			Customize.add(menuNodeFillColor);
			Customize.add(menuNodeFontColor);
			Customize.add(menuNodeFillStyle);
			Customize.addSeparator();
			Customize.add(menuAddImage);

			// menuCustomize.add(menuNodeColor);

			if (menuDeleteNode == null) {
				menuDeleteNode = new JMenuItem("Delete Variable");
				menuDeleteNode.addActionListener(this);
			}

		} else {
			// hit an edge?
			/*
			 * Iterator<Edge> iterEdge = graph.getEdgeIterator(); Edge best = null; double
			 * bestDist = Double.MAX_VALUE; while (iterEdge.hasNext()) { Edge edge =
			 * iterEdge.next(); double dist = Math.abs(edge.distanceFromPoint(arg0.getX(),
			 * arg0.getY())); if (dist < bestDist) { bestDist = dist; best = edge; } }
			 */

			if (edgeUnderMouse != null)
				pendingEdgeStateChangedUndoStep = new EdgeStateChangedStep(this, edgeUnderMouse);
			// closest edge close enough?
			if (edgeUnderMouse != null) {

				if (!edgeUnderMouse.isFixed() || edgeUnderMouse.isDefinitionVariable()) {
					nameInput = new LabeledInputBox("Path Name");

					// nameInput.addKeyListener(this);
					menu.add(nameInput);
					nameInput.setText(edgeUnderMouse.getParameterName());
					nameInput.getDocument().addDocumentListener(this);
				}

				if (edgeUnderMouse.isFixed() && !edgeUnderMouse.isDefinitionVariable()) {

					valueInput = new LabeledInputBox("Path Value");
					menu.add(valueInput);
					valueInput.setText(Double.toString(edgeUnderMouse.getValue()));
					valueInput.getDocument().addDocumentListener(this);

				} else if (!edgeUnderMouse.isDefinitionVariable()) {
					svalueInput = new LabeledInputBox("Starting Value");
					menu.add(svalueInput);
					double svalue = getModelRequestInterface().getStartingValuesUnit()
							.getParameterValue(edgeUnderMouse.getParameterName());
					svalueInput.setText(Double.toString(svalue));

					svalueInput.getDocument().addDocumentListener(this);
				}

				menu.addSeparator();

				JMenu customizeEdge = new JMenu("Customize Path");

				thicknessInput = new LabeledInputBox("Thickness");
				customizeEdge.add(thicknessInput);
				thicknessInput.setText(String.valueOf(edgeUnderMouse.getStrokeWidth()));
				thicknessInput.getDocument().addDocumentListener(this);

				labelsizeInput = new LabeledInputBox("Font Size");
				customizeEdge.add(labelsizeInput);
				labelsizeInput.setText(String.valueOf(edgeUnderMouse.getLabel().getFontSize()));
				labelsizeInput.getDocument().addDocumentListener(this);

				menuEdgeLabelColor = new JMenuItem("Change Font Color");
				menuEdgeLabelColor.addActionListener(this);

				if (arrow == null) {
					arrow = new JMenu("Arrowhead");
					arrow1 = new JMenuItem("V-Shaped");
					arrow2 = new JMenuItem("Filled");
					arrow3 = new JMenuItem("Pointy");
					arrow.add(arrow1);
					arrow.add(arrow2);
					arrow.add(arrow3);
					arrow1.addActionListener(this);
					arrow2.addActionListener(this);
					arrow3.addActionListener(this);
				}

				if (menuEdgeColor == null) {
					menuEdgeColor = new JMenuItem("Change Path Color");
					menuEdgeColor.addActionListener(this);

				}

				if (menuDashStyle == null) {
					menuDashStyle = new JMenuItem("Change Dash Style");
					menuDashStyle.addActionListener(this);
				}

				customizeEdge.addSeparator();

				customizeEdge.add(arrow);
				customizeEdge.add(menuEdgeColor);
				customizeEdge.add(menuEdgeLabelColor);
				customizeEdge.add(menuDashStyle);

				customizeEdge.addSeparator();

				if (menuDeleteEdge == null) {
					menuDeleteEdge = new JMenuItem("Delete Path");
					menuDeleteEdge.addActionListener(this);
				}
				// menu.add(menuDeleteEdge);

				if (menuToggleAutomaticNaming == null) {
					menuToggleAutomaticNaming = new JCheckBoxMenuItem("Automatic Parameter Naming");
					menuToggleAutomaticNaming.addActionListener(this);

				}
				if (edgeUnderMouse != null)
					menuToggleAutomaticNaming.setSelected(edgeUnderMouse.isAutomaticNaming());

				if (menuToggleEdgeHeads == null) {
					menuToggleEdgeHeads = new JMenuItem("Toggle Path Heads");
					menuToggleEdgeHeads.addActionListener(this);
				}

				if (menuActivateManuaLEdgeControl == null) {
					menuActivateManuaLEdgeControl = new JCheckBoxMenuItem("Show Control Points");
					menuActivateManuaLEdgeControl.addActionListener(this);
				}

				// if (menuContextEdge.getActiveControl()) {
				menuActivateManuaLEdgeControl.setSelected(menuContextEdge.getActiveControl());
				// }

				String fixedLabel = "";
				if (edgeUnderMouse.isFixed()) {
					fixedLabel = "Free Parameter";
				} else {
					fixedLabel = "Fix Parameter";
				}

				menuToggleFixed = new JMenuItem(fixedLabel);
				menuToggleFixed.addActionListener(this);

				if (!edgeUnderMouse.isDefinitionVariable())
					menu.add(menuToggleFixed);

				if (!edgeUnderMouse.source.isMeanTriangle() && !edgeUnderMouse.isVarianceEdge())
					menu.add(menuToggleEdgeHeads);

				if (menuMakeDefinition == null) {
					menuMakeDefinition = new JMenuItem("Add Definition Variable");
					menuMakeDefinition.addActionListener(this);
				}

				// menu.add(menuMakeDefinition);

				customizeEdge.add(menuActivateManuaLEdgeControl);

				customizeEdge.addSeparator();

				customizeEdge.add(menuToggleAutomaticNaming);

				menu.add(customizeEdge);

				if (menuDeleteEdge == null) {
					menuDeleteEdge = new JMenuItem("Delete Path");
					menuDeleteEdge.addActionListener(this);
				}

			}
		}

		if (menuStrategyClassic == null) {
			menuStrategyClassic = new JRadioButtonMenuItem("Classic");
			menuStrategyClassic.addActionListener(this);
		}
		if (menuStrategyDefault == null) {
			menuStrategyDefault = new JRadioButtonMenuItem("Default");
			menuStrategyDefault.addActionListener(this);
		}
		if (menuStrategyDefaultWithEMSupport == null) {
			menuStrategyDefaultWithEMSupport = new JRadioButtonMenuItem("EM supported");
			menuStrategyDefaultWithEMSupport.addActionListener(this);
		}

		if (menuStrategyMCMC == null) {
			menuStrategyMCMC = new JRadioButtonMenuItem("Bayesian (MCMC)");
			menuStrategyMCMC.addActionListener(this);
		}

		if (menuPriorityHigh == null) {
			menuPriorityHigh = new JRadioButtonMenuItem("High");
			menuPriorityHigh.addActionListener(this);
		}
		if (menuPriorityNormal == null) {
			menuPriorityNormal = new JRadioButtonMenuItem("Normal");
			menuPriorityNormal.addActionListener(this);
		}
		if (menuPriorityLow == null) {
			menuPriorityLow = new JRadioButtonMenuItem("Low");
			menuPriorityLow.addActionListener(this);
		}
		if (menuPriorityHold == null) {
			menuPriorityHold = new JRadioButtonMenuItem("Pause");
			menuPriorityHold.addActionListener(this);
		}

		Model.Strategy strategy = getModelRequestInterface().getStrategy();
		if (strategy == Model.Strategy.classic)
			menuStrategyClassic.setSelected(true);
		else
			menuStrategyClassic.setSelected(false);
		if (strategy == Model.Strategy.defaul)
			menuStrategyDefault.setSelected(true);
		else
			menuStrategyDefault.setSelected(false);
		if (strategy == Model.Strategy.defaultWithEMSupport)
			menuStrategyDefaultWithEMSupport.setSelected(true);
		else
			menuStrategyDefaultWithEMSupport.setSelected(false);
		if (strategy == Model.Strategy.MCMC)
			menuStrategyMCMC.setSelected(true);
		else
			menuStrategyMCMC.setSelected(false);

		// System.out.println(getModelRequestInterface().getRunPriority());
		menuPriorityHigh.setSelected(false);
		if (getModelRequestInterface().getRunPriority() == Priority.HIGH)
			menuPriorityHigh.setSelected(true);
		menuPriorityHold.setSelected(false);
		if (getModelRequestInterface().getRunPriority() == Priority.HOLD)
			menuPriorityHold.setSelected(true);
		menuPriorityLow.setSelected(false);
		if (getModelRequestInterface().getRunPriority() == Priority.LOW)
			menuPriorityLow.setSelected(true);
		menuPriorityNormal.setSelected(false);
		if (getModelRequestInterface().getRunPriority() == Priority.NORMAL)
			menuPriorityNormal.setSelected(true);

		JMenu priorityMenu = new JMenu("Priority");
		priorityMenu.add(menuPriorityHigh);
		priorityMenu.add(menuPriorityNormal);
		priorityMenu.add(menuPriorityLow);
		priorityMenu.addSeparator();
		priorityMenu.add(menuPriorityHold);

		JMenu menuStrategy = new JMenu("Strategy");
		menuStrategy.add(menuStrategyClassic);
		menuStrategy.add(menuStrategyDefault);
		menuStrategy.add(menuStrategyDefaultWithEMSupport);
		menuStrategy.addSeparator();
		if (MainFrame.DEVMODE)
			menuStrategy.add(menuStrategyMCMC);
		menuStrategy.addSeparator();
		menuStrategy.add(priorityMenu);

		if (menuDeleteModel == null) {
			menuDeleteModel = new JMenuItem("Close Model");
			menuDeleteModel.addActionListener(this);
		}

		if (menuToggleShowGrid == null) {
			menuToggleShowGrid = new JCheckBoxMenuItem("Display Grid");
			menuToggleShowGrid.addActionListener(this);
		}
		// if (showGrid) {
		menuToggleShowGrid.setSelected(showGrid);
		// }

		if (menuToggleLockToGrid == null) {
			menuToggleLockToGrid = new JCheckBoxMenuItem("Lock to Grid");
			menuToggleLockToGrid.addActionListener(this);
		}
		menuToggleLockToGrid.setSelected(lockToGrid);

		if (menuResetToDefaults == null) {
			menuResetToDefaults = new JMenuItem("Reset All Elements To Default");
			menuResetToDefaults.addActionListener(this);
		}

		if (menuAutoLayout == null) {
			menuAutoLayout = new JMenuItem("Apply Auto-Layout");
			menuAutoLayout.addActionListener(this);
		}

		if (menuShowAux == null) {
			menuShowAux = new JMenuItem("");
			menuShowAux.addActionListener(this);
		}
		if (graph.getAuxiliaryStack().isHidden()) {
			menuShowAux.setText("Show Auxiliary Variable Stack");
		} else {
			menuShowAux.setText("Hide Auxiliary Variable Stack");
		}

		if (menuSaveModel == null) {
			menuSaveModel = new JMenuItem("Save Model");
			menuSaveModel.addActionListener(this);
		}

		if (menuSaveAsModel == null) {
			menuSaveAsModel = new JMenuItem("Save Model As");
			menuSaveAsModel.addActionListener(this);
		}

		if (menuSaveModelAndData == null) {
			menuSaveModelAndData = new JMenuItem("Save Model & Data");
			menuSaveModelAndData.addActionListener(this);
		}

		if (menuLoadStartingValues == null) {
			menuLoadStartingValues = new JMenuItem("Load Starting Values");
			menuLoadStartingValues.addActionListener(this);
		}

		if (menuDPClustering == null) {
			menuDPClustering = new JMenu("DP Clustering");
			menuDPClustering.addActionListener(this);
			menuDPClusteringStart = new JMenuItem("Start Clustering");
			menuDPClusteringStart.addActionListener(this);
			menuDPClustering.add(menuDPClusteringStart);
			menuAnzBurninDPClustering = new LabeledInputBox("     Burnin:");
			menuAnzBurninDPClustering.setText("0");
			menuAnzBurninDPClustering.getDocument().addDocumentListener(this);
			menuAnzBurninDPClustering.addActionListener(this);
			menuDPClustering.add(menuAnzBurninDPClustering);
			menuanzSamplesDPClustering = new LabeledInputBox("     Samples:");
			menuanzSamplesDPClustering.setText("5");
			menuanzSamplesDPClustering.getDocument().addDocumentListener(this);
			menuanzSamplesDPClustering.addActionListener(this);
			menuDPClustering.add(menuanzSamplesDPClustering);
			menuAlphaDPClustering = new LabeledInputBox("     Alpha:");
			menuAlphaDPClustering.setText("1.0");
			menuAlphaDPClustering.getDocument().addDocumentListener(this);
			menuAlphaDPClustering.addActionListener(this);
			menuDPClustering.add(menuAlphaDPClustering);
			menuPriorStrengthDPClustering = new LabeledInputBox("     Prior Weight:");
			menuPriorStrengthDPClustering.setText("2");
			menuPriorStrengthDPClustering.getDocument().addDocumentListener(this);
			menuPriorStrengthDPClustering.addActionListener(this);
			menuDPClustering.add(menuPriorStrengthDPClustering);

			// menu Pre-Clustering
			menuPreClusteringDPClustering = new JMenu("Pre-Clustering");
			menuPreClusteringDPClustering.addActionListener(this);
			menuDPClustering.add(menuPreClusteringDPClustering);
			menuToggleDoPreClusteringDPClustering = new JCheckBoxMenuItem("do Pre-Clustering");
			menuToggleDoPreClusteringDPClustering.addActionListener(this);
			menuToggleDoPreClusteringDPClustering.setSelected(true);
			menuPreClusteringDPClustering.add(menuToggleDoPreClusteringDPClustering);
			menuBurninPreClusteringDPClustering = new LabeledInputBox("     Burnin:");
			menuBurninPreClusteringDPClustering.setText("50");
			menuBurninPreClusteringDPClustering.getDocument().addDocumentListener(this);
			menuBurninPreClusteringDPClustering.addActionListener(this);
			menuPreClusteringDPClustering.add(menuBurninPreClusteringDPClustering);
			menuSamplesPreClusteringDPClustering = new LabeledInputBox("     Samples:");
			menuSamplesPreClusteringDPClustering.setText("50");
			menuSamplesPreClusteringDPClustering.getDocument().addDocumentListener(this);
			menuSamplesPreClusteringDPClustering.addActionListener(this);
			menuPreClusteringDPClustering.add(menuSamplesPreClusteringDPClustering);
			menuThinningPreClusteringDPClustering = new LabeledInputBox("     Thinning:");
			menuThinningPreClusteringDPClustering.setText("1");
			menuThinningPreClusteringDPClustering.getDocument().addDocumentListener(this);
			menuThinningPreClusteringDPClustering.addActionListener(this);
			menuPreClusteringDPClustering.add(menuThinningPreClusteringDPClustering);
		}

		if (menuSimulation == null) {
			menuSimulation = new JMenu("Simulation");
			menuSimulation.addActionListener(this);
		}

		if (menuSimulationStart == null) {
			menuSimulationStart = new JMenuItem("Start Simulation");
			menuSimulationStart.addActionListener(this);
		}
		menuSimulation.add(menuSimulationStart);

		if (menuSimulationCovarianceDataset == null) {
			menuSimulationCovarianceDataset = new JMenuItem("Create Covariance Dataset");
			menuSimulationCovarianceDataset.addActionListener(this);
		}
		if (MainFrame.DEVMODE)
			menuSimulation.add(menuSimulationCovarianceDataset);

		if (menuAnzRowsSimulation == null) {
			menuAnzRowsSimulation = new LabeledInputBox("     Data Rows: ");
			menuAnzRowsSimulation.setText("100");
			menuAnzRowsSimulation.getDocument().addDocumentListener(this);
			menuAnzRowsSimulation.addActionListener(this);
		}
		menuSimulation.add(menuAnzRowsSimulation);

		if (menuPercentMissingSimulation == null) {
			menuPercentMissingSimulation = new LabeledInputBox("     Missing [%]: ");
			menuPercentMissingSimulation.setText("0");
			menuPercentMissingSimulation.getDocument().addDocumentListener(this);
			menuPercentMissingSimulation.addActionListener(this);
		}
		menuSimulation.add(menuPercentMissingSimulation);

		if (menuExportJPEG == null) {
			menuExportJPEG = new JMenuItem("JPEG");
			menuExportJPEG.addActionListener(this);
		}

		if (menuExportPNG == null) {
			menuExportPNG = new JMenuItem("PNG");
			menuExportPNG.addActionListener(this);
		}

		if (menuExportLaTeX == null) {
			menuExportLaTeX = new JMenuItem("LaTeX");
			menuExportLaTeX.addActionListener(this);
		}

		if (menuExportSVG == null) {
			menuExportSVG = new JMenuItem("SVG");
			menuExportSVG.addActionListener(this);
		}

		if (menuExportPDF == null) {
			menuExportPDF = new JMenuItem("PDF");
			menuExportPDF.addActionListener(this);
		}

		if (menuExportEPS == null) {
			menuExportEPS = new JMenuItem("EPS");
			menuExportEPS.addActionListener(this);
		}

		if (menuShowOpenMXCode == null) {
			menuShowOpenMXCode = new JMenuItem("OpenMx (Path)");
			menuShowOpenMXCode.addActionListener(this);
		}

		if (menuShowOpenMXMatrixCode == null) {
			menuShowOpenMXMatrixCode = new JMenuItem("OpenMx (Matrix)");
			menuShowOpenMXMatrixCode.addActionListener(this);
		}

		if (menuShowMPlusCode == null) {
			menuShowMPlusCode = new JMenuItem("Mplus");
			menuShowMPlusCode.addActionListener(this);
		}
		if (menuShowLavaanCode == null) {
			menuShowLavaanCode = new JMenuItem("lavaan");
			menuShowLavaanCode.addActionListener(this);
		}
		if (menuShowSemCode == null) {
			menuShowSemCode = new JMenuItem("sem package");
			menuShowSemCode.addActionListener(this);
		}
		if (menuShowRAMMatrices == null) {
			menuShowRAMMatrices = new JMenuItem("RAM Matrices");
			menuShowRAMMatrices.addActionListener(this);
		}
		if (menuShowLISRELMatrices == null) {
			menuShowLISRELMatrices = new JMenuItem("LISREL Matrices");
			menuShowLISRELMatrices.addActionListener(this);
		}

		if (menuShowCorrelationMatrix == null) {
			menuShowCorrelationMatrix = new JMenuItem("Covariance Matrix");
			menuShowCorrelationMatrix.addActionListener(this);
		}

		if (menuShowOnyxJavaCode == null) {
			menuShowOnyxJavaCode = new JMenuItem("Onyx Java");
			menuShowOnyxJavaCode.addActionListener(this);
		}

		if (menuShowCode == null) {
			menuShowCode = new JMenu("Show Script");
			menuShowCode.addActionListener(this);
			menuShowCode.add(menuShowOpenMXCode);
			menuShowCode.add(menuShowOpenMXMatrixCode);

			menuShowCode.add(menuShowLavaanCode);
			menuShowCode.add(menuShowSemCode);
			menuShowCode.addSeparator();
			menuShowCode.add(menuShowMPlusCode);
			menuShowCode.addSeparator();
			if (MainFrame.DEVMODE) {
				menuShowCode.add(menuShowOnyxJavaCode);
				menuShowCode.addSeparator();
			}
			menuShowCode.add(menuShowRAMMatrices);
			menuShowCode.add(menuShowLISRELMatrices);
			menuShowCode.add(menuShowCorrelationMatrix);
		}

		if (menuIconify == null) {
			menuIconify = new JMenuItem("Iconify");
			menuIconify.addActionListener(this);
		}

		if (menuSelectEdgeStyle == null) {
			menuSelectEdgeStyle = new JMenuItem("Change Path Style");
			menuSelectEdgeStyle.addActionListener(this);
		}

		if (menuStroke == null) {
			menuStroke = new JMenu("Change Line Weight");
		}

		if (menuCopy == null) {
			menuCopy = new JMenuItem("Copy");
			menuCopy.addActionListener(this);
		}
		if (menuPaste == null) {
			menuPaste = new JMenuItem("Paste and Rename");
			menuPaste.addActionListener(this);
		}
		if (menuPasteDontMess == null) {
			menuPasteDontMess = new JMenuItem("Paste");
			menuPasteDontMess.addActionListener(this);
		}

		if (menuFlipHorizontal == null) {
			menuFlipHorizontal = new JMenuItem("Flip horizontally");
		}
		if (menuFlipVertically == null) {
			menuFlipVertically = new JMenuItem("Flip vertically");
			menuFlipHorizontal.addActionListener(this);
			menuFlipVertically.addActionListener(this);
		}
		/*
		 * if (menuCopyPaste == null) { menuCopyPaste = new JMenu("Copy&Paste");
		 * 
		 * 
		 * }
		 */
		if (menuFlip == null) {
			menuFlip = new JMenu("Flip");
			menuFlip.add(menuFlipHorizontal);
			menuFlip.add(menuFlipVertically);
		}

		/*
		 * if (thinStroke == null) { thinStroke = new JMenuItem("Thin");
		 * thinStroke.addActionListener(this); menuStroke.add(thinStroke); } if
		 * (mediumStroke == null) { mediumStroke = new JMenuItem("Medium");
		 * mediumStroke.addActionListener(this); menuStroke.add(mediumStroke); } if
		 * (thickStroke == null) { thickStroke = new JMenuItem("Thick");
		 * thickStroke.addActionListener(this); menuStroke.add(thickStroke); }
		 */
		if (menuExportMplus == null) {
			menuExportMplus = new JMenuItem("Mplus");
			menuExportMplus.addActionListener(this);
		}

		if (menuExportLavaan == null) {
			menuExportLavaan = new JMenuItem("lavaan");
			menuExportLavaan.addActionListener(this);
		}

		if (menuExportSem == null) {
			menuExportSem = new JMenuItem("sem (package)");
			menuExportSem.addActionListener(this);
		}

		if (menuExport == null) {
			menuExport = new JMenuItem("OpenMx");
			menuExport.addActionListener(this);
		}

		if (menuAddTriangle == null) {
			menuAddTriangle = new JMenuItem("Constant");
			menuAddTriangle.addActionListener(this);
		}

		if (menuAddMultiplication == null) {
			menuAddMultiplication = new JMenuItem("Multiplication");
			menuAddMultiplication.addActionListener(this);
		}

		if (menuCreateLatent == null) {
			menuCreateLatent = new JMenuItem("Latent");
			menuCreateLatent.addActionListener(this);
		}

		if (menuCreateManifest == null) {
			menuCreateManifest = new JMenuItem("Observed");
			menuCreateManifest.addActionListener(this);
		}

		// menuRemoveDefinitionStatus = new JMenuItem();
		if (menuRemoveDefinitionStatus == null) {
			menuRemoveDefinitionStatus = new JMenuItem("Remove Definition Variable");
			menuRemoveDefinitionStatus.addActionListener(this);
		}

		/*
		 * if (menuSwapManifestLatent == null) { menuSwapManifestLatent = new JMenuItem(
		 * (newNodeIsLatent ? "New Nodes: Manifests" : "New Nodes: Latent"));
		 * menuSwapManifestLatent.addActionListener(this); }
		 */
		if (menuClone == null) {
			menuClone = new JMenuItem("Clone Model");
			menuClone.addActionListener(this);
		}

		if (menuShowHide == null) {
			menuShowHide = new JMenu("Show/Hide");
			menuShowHideVariances = new JMenuItem("Show/Hide Variances");
			menuShowHideCovariances = new JMenuItem("Show/Hide Covariances");
			menuShowHideRegressions = new JMenuItem("Show/Hide Regressions");

			menuShowHide.add(menuShowHideVariances);
			menuShowHide.add(menuShowHideCovariances);
			menuShowHide.add(menuShowHideRegressions);
			menuShowHideVariances.addActionListener(this);
			menuShowHideCovariances.addActionListener(this);
			menuShowHideRegressions.addActionListener(this);
			menuShowHide.addActionListener(this);
		}

		if (menuAgents == null) {
			menuAgents = new JMenu("Start External Estimate");
			menuAgents.addActionListener(this);
		}
		menuAgents.removeAll();

		try {
			Hashtable<String, ExternalRunUnit> agents = ExternalRunUnit.getValidExternalAgents();
			if (agents.size() > 0) {
				menuAgentItems = new JMenuItem[agents.size()];
				int i = 0;
				for (String agentLabel : agents.keySet()) {
					menuAgentItems[i] = new JMenuItem(agentLabel);
					menuAgentItems[i].addActionListener(this);

					menuAgents.add(menuAgentItems[i]);
					i++;
				}
			}
		} catch (Exception e) {
		}

		if (menuRunners == null) {
			menuRunners = new JMenu("Select Estimate");
			menuRunners.addActionListener(this);
		}

		menuRunners.removeAll();

		boolean hasRunners = this.currentEstimatesShownInMenu != null;
		// boolean hasRunners = (this.currentEstimates != null);
		if (hasRunners) {
			currentEstimatesShownInMenu = stackCurrentEstimates();
			int numEst = currentEstimatesShownInMenu.size();
			menuRunnerItems = new JMenuItem[numEst];

			for (int i = 0; i < numEst; i++) {
				/*
				 * parameterView.setParameterSet(estimates.get(0));
				 * graph.updateWithEstimates(estimates.get(0)); this.redraw();
				 */
				String name = currentEstimatesShownInMenu.get(i).name + " ("
						+ Math.round(currentEstimatesShownInMenu.get(i).fit * 1000.0) / 1000.0 + ")";
				// if (i==0) name += " (BEST)";
				if (showingEstimate instanceof ModelRunUnit
						&& currentEstimatesShownInMenu.get(i).isSameAs((ModelRunUnit) showingEstimate))
					name += " *";
				menuRunnerItems[i] = new JMenuItem(name);
				menuRunnerItems[i].addActionListener(this);

				if (currentEstimatesShownInMenu.get(i).hasWarning()) {
					Icon warningIcon = new ImageIcon(MessageObject.imageTable.get(ImageLoaderWorker.WARNING));
					menuRunnerItems[i].setIcon(warningIcon);
				}

				menuRunners.add(menuRunnerItems[i]);
			}
		}

		if (menuAllRunner == null) {
			menuAllRunner = new JMenuItem("Show All Runners");
			menuAllRunner.addActionListener(this);
		}
		if (menuShowStartingValues == null) {
			menuShowStartingValues = new JMenuItem("Show Starting Values");
			menuShowStartingValues.addActionListener(this);
		}
		if (menuShowBestML == null) {
			menuShowBestML = new JMenuItem("Show Best ML Estimate");
			menuShowBestML.addActionListener(this);
		}
		if (menuShowBestLS == null) {
			menuShowBestLS = new JMenuItem("Show Best LS Estimate");
			menuShowBestLS.addActionListener(this);
		}
		if (menuShowTextOutput == null) {
			menuShowTextOutput = new JMenuItem("Show Estimate Summary");
			menuShowTextOutput.addActionListener(this);
		}
		if (menuShowTextHistory == null) {
			menuShowTextHistory = new JMenuItem("Show Estimate History");
			menuShowTextHistory.addActionListener(this);
		}

		if (menuSelectSaturatedModel == null) {
			menuSelectSaturaredModel = new JMenu("Select explicit saturated model");
			menuSelectSaturaredModel.addActionListener(this);
		}

		if (!nodeUnderMouse && edgeUnderMouse == null) {

			modelName = new LabeledInputBox("Model Name: ");
			modelName.setText(this.mri.getName());
			modelName.getDocument().addDocumentListener(this);

			menu.add(modelName);

		}

		if (menuSaveImage == null) {
			menuSaveImage = new JMenu("Export Image");
			menuSaveImage.add(menuExportJPEG);
			menuSaveImage.add(menuExportPNG);
			menuSaveImage.add(menuExportPDF);
			menuSaveImage.add(menuExportEPS);
			menuSaveImage.add(menuExportSVG);

		}
		if (menuSaveScript == null) {
			menuSaveScript = new JMenu("Export Script");
			menuSaveScript.add(menuExportLaTeX);
			menuSaveScript.add(menuExport);
			menuSaveScript.add(menuExportMplus);
			menuSaveScript.add(menuExportLavaan);
			menuSaveScript.add(menuExportSem);
		}

		menuSelectSaturaredModel.removeAll();
		List<ModelView> mvs = desktop.getModelViews();
		menuSend = new JMenuItem[mvs.size()];
		for (int i = 0; i < mvs.size(); i++) {
			if (mvs.get(i) == this)
				continue;
			JMenuItem jmi = new JMenuItem(mvs.get(i).getName());
			menuSelectSaturaredModel.add(jmi);
			jmi.addActionListener(this);
			menuSend[i] = jmi;
		}

		if (menuUndo == null) {
			menuUndo = new JMenuItem("Undo");
			menuUndo.addActionListener(this);
		}

		if (menuRedo == null) {
			menuRedo = new JMenuItem("Redo");
			menuRedo.addActionListener(this);
		}
		// menuRedo.setEnabled(false);

		if (menuSelectAll == null) {
			menuSelectAll = new JMenuItem("Select All");
			menuSelectAll.addActionListener(this);
		}

		if (menuModifyGraph == null) {
			menuModifyGraph = new JMenu("Modify Graph");
			// menuModifyGraph.add(menuCopyPaste);
			menuModifyGraph.add(menuFlip);

		}

		if (menuPrior == null) {
			menuPrior = new JMenu("Prior");

			menuPriorGaussian = new JMenuItem("Normal");
			menuPriorChi2 = new JMenuItem("Chi Square");
			menuPriorGamma = new JMenuItem("Gamma");
			menuPriorUniform = new JMenuItem("Uniform");

			menuPrior.add(menuPriorGaussian);
			menuPrior.add(menuPriorChi2);
			menuPrior.add(menuPriorUniform);
			menuPrior.add(menuPriorGamma);

			menuPriorGaussian.addActionListener(this);
			menuPriorChi2.addActionListener(this);
			menuPriorGamma.addActionListener(this);
			menuPriorUniform.addActionListener(this);
		}

		if (strategy.equals(Strategy.MCMC)) {
			menu.addSeparator();
			menu.add(menuPrior);
			menu.addSeparator();
		}

		// menuShowStandardizedEstimates.setEnabled(false);
		// menuShowStandardizedEstimates.setSelected(showStandardizedEstimates);
		// menuShowStandardizedEstimates.setSelected(false);

		if (menuSaveStartingValues == null) {
			menuSaveStartingValues = new JMenuItem("Save Starting Values");
			menuSaveStartingValues.addActionListener(this);
		}

		if (menuSaveCurrentEstimate == null) {
			menuSaveCurrentEstimate = new JMenuItem("Save Current Estimate");
			menuSaveCurrentEstimate.addActionListener(this);
		}

		if (menuBackgroundColor == null) {
			menuBackgroundColor = new JMenuItem("Change Background Color");
			menuBackgroundColor.addActionListener(this);
		}

		JMenu menuCreateVariable = new JMenu("Create Variable");

		menuCreateVariable.add(menuAddTriangle);
		menuCreateVariable.add(menuCreateLatent);
		menuCreateVariable.add(menuCreateManifest);
		if (MainFrame.DEVMODE)
			menuCreateVariable.add(menuAddMultiplication);

		if (menuToggleMarkUnconnectedManifests == null) {
			menuToggleMarkUnconnectedManifests = new JCheckBoxMenuItem("Mark Unconnected Observed Variables");
			menuToggleMarkUnconnectedManifests.addActionListener(this);

		}
		menuToggleMarkUnconnectedManifests.setSelected(graph.markUnconnectedNodes);

		/**
		 * Menu Grid
		 */
		JMenu menuGrid = new JMenu("Change Grid Properties");
		menuGrid.add(menuToggleShowGrid);
		menuGrid.add(menuToggleLockToGrid);
		if (menuGridSizeInput == null) {
			menuGridSizeInput = new LabeledInputBox("Grid Size");
			menuGridSizeInput.getDocument().addDocumentListener(this);
		}
		menuGridSizeInput.setText(Integer.toString(gridSize));

		menuGrid.add(menuGridSizeInput);

		/**
		 * CUSTOMIZE MODEL
		 */
		JMenu menuCustomizeModel = new JMenu("Customize Model");

		menuCustomizeModel.add(menuSelectEdgeStyle);

		if (menuGraphPresetsMenu == null) {
			menuGraphPresets = new JMenuItem[presets.length];
			menuGraphPresetsMenu = new JMenu("Apply Diagram Style");

			for (int i = 0; i < menuGraphPresets.length; i++) {
				menuGraphPresets[i] = new JMenuItem(presets[i].getName());
				menuGraphPresets[i].addActionListener(this);
				menuGraphPresetsMenu.add(menuGraphPresets[i]);
			}

		}
		
		if (menuKeepStyle==null) {
			menuKeepStyle = new JCheckBoxMenuItem("Keep Style on Edit");
			menuKeepStyle.addActionListener(this);
		}
		menuKeepStyle.setSelected(getGraph().isLockedStyle());


		menuCustomizeModel.add(menuGraphPresetsMenu);
		menuCustomizeModel.add(menuKeepStyle);
		menuCustomizeModel.addSeparator();
		menuCustomizeModel.add(menuToggleMarkUnconnectedManifests);
		menuCustomizeModel.add(menuShowHideVariances);

		menuCustomizeModel.addSeparator();
		menuCustomizeModel.add(menuBackgroundColor);

		menuCustomizeModel.add(menuGrid);

		menuCustomizeModel.addSeparator();
		menuCustomizeModel.add(menuResetToDefaults);
		menuCustomizeModel.add(menuAutoLayout);

		if (MainFrame.DEVMODE) {
			menuCustomizeModel.addSeparator();
			menuCustomizeModel.add(menuShowAux);
		}
		/*
		 * Menu EDIT
		 */
		menuEdit = new JMenu("Edit");

		menuEdit.add(menuCopy);
		menuEdit.add(menuPasteDontMess);
		menuEdit.add(menuPaste);
		menuEdit.addSeparator();
		menuEdit.add(menuUndo);
		menuEdit.add(menuRedo);
		menuEdit.addSeparator();
		menuEdit.add(menuSelectAll);
		menuEdit.addSeparator();
		menuEdit.add(menuClone);

		// }

		/*
		 * Menu FILE
		 */
		if (menuSave == null) {
			menuSave = new JMenu("File");
			menuSave.add(menuSaveModel);
			menuSave.add(menuSaveAsModel);
			if (MainFrame.DEVMODE)
				menuSave.add(menuSaveModelAndData);

			menuSave.addSeparator();
			menuSave.add(menuSaveImage);
			menuSave.add(menuSaveScript);
			menuSave.addSeparator();
			menuSave.add(menuLoadStartingValues);
			menuSave.add(menuSaveStartingValues);
			menuSave.add(menuSaveCurrentEstimate);
		}

		/*
		 * mean Treatment
		 */

		JMenu menuMeanTreatment = new JMenu("Mean Structure");

		/*
		 * Menu ADvanced
		 */

		JMenu menuEstimation = new JMenu("Estimation");

		if (menuLatentScores == null) {
			menuLatentScores = new JMenuItem("Obtain Latent / Missing Scores");
			menuLatentScores.addActionListener(this);
		}

		menuEstimation.add(menuShowBestML);
		menuEstimation.add(menuShowBestLS);
		menuEstimation.add(menuShowStartingValues);
		if (hasRunners) {
			menuEstimation.add(menuRunners);
		}
		if (menuShowingEstimate == null) {
			menuShowingEstimate = new JMenu("Current Estimate");
			menuShowingEstimate.addActionListener(this);
		}

		menuEstimation.addSeparator();
		menuEstimation.add(menuShowTextHistory);
		menuEstimation.addSeparator();

		menuEstimation.add(menuAllRunner);
		menuEstimation.add(menuStrategy);
		// menuAdvanced.add(menuShowingEstimate);
		menuEstimation.addSeparator();

		if (ExternalRunUnit.getValidExternalAgents().size() > 0)
			menuEstimation.add(menuAgents);

		menuEstimation.addSeparator();
		menuEstimation.add(menuLatentScores);

		/*
		 * if (menuSetDefaultStartingValues == null) { menuSetDefaultStartingValues =
		 * new JMenuItem("Set Default Starting Values");
		 * menuSetDefaultStartingValues.addActionListener(this); }
		 */

		// menuAdvanced.addSeparator();

		// menuShowingEstimate.add(menuSetDefaultStartingValues);

		// if (MainFrame.DEVMODE) {
		// menuAdvanced.add(menuSelectSaturaredModel);

		// menuAdvanced.addSeparator();
		// }
		if (menuShowStandardizedEstimates == null) {
			menuShowStandardizedEstimates = new JCheckBoxMenuItem("Show Standardized Estimates");
			menuShowStandardizedEstimates.addActionListener(this);
		}

		if (edgeUnderMouse != null) {

			if (menuContextEdge.isShowStandardizedEstimate()) {
				menuShowStandardizedEstimates.setSelected(true);
			} else {
				menuShowStandardizedEstimates.setSelected(false);
			}

			menu.add(menuShowStandardizedEstimates);
		}

		/*
		 * TOP-LEVEL MENU
		 */

		menu.addSeparator();
		menu.add(menuCreateVariable);

		if (nodeUnderMouse)
			menu.add(menuDeleteNode);
		if (edgeUnderMouse != null && menuDeleteEdge != null) {
			menu.add(menuDeleteEdge);

			if (edgeUnderMouse.isDefinitionVariable()) {
				menu.add(menuRemoveDefinitionStatus);

				// if (edgeUnderMouse != null) {
				if (edgeUnderMouse.isDefinitionVariable()) {
					menuUnlinkDefinition = new JMenuItem("Unlink Definition Variable");
					menuUnlinkDefinition.addActionListener(this);
					menu.add(menuUnlinkDefinition);
				}
				// }
			} else {
				menu.add(menuMakeDefinition);
			}
		}

		menu.addSeparator();
		menu.add(menuCustomizeModel);
		menu.addSeparator();

		menu.add(menuSave);
		menu.add(menuEdit);
		menu.add(menuShowCode);
		menu.addSeparator();
		menu.add(menuSimulation);
		if (MainFrame.DEVMODE)
			menu.add(menuDPClustering);
		menu.addSeparator();

		menu.add(menuMeanTreatment);
		menu.add(menuEstimation);
		menu.add(menuShowTextOutput);

		if (menuMeanTreatmentExplicit == null) {
			menuMeanTreatmentExplicit = new JRadioButtonMenuItem("Explicit Means");
			menuMeanTreatmentExplicit.addActionListener(this);
		}
		if (menuMeanTreatmentSaturated == null) {
			menuMeanTreatmentSaturated = new JRadioButtonMenuItem("Saturated Means");
			menuMeanTreatmentSaturated.addActionListener(this);
		}
		menuMeanTreatmentExplicit.setSelected(graph.getMeanTreatment().equals(Graph.MeanTreatment.explicit));
		menuMeanTreatmentSaturated.setSelected(graph.getMeanTreatment().equals(Graph.MeanTreatment.implicit));
		menuMeanTreatment.add(menuMeanTreatmentExplicit);
		menuMeanTreatment.add(menuMeanTreatmentSaturated);

		menu.addSeparator();

		menu.add(menuIconify);
		menu.add(menuDeleteModel);

		// ADD esoteric key listeners to capture key strokes that would otherwise close
		// the popup
		KeyListener esokl = new KeyListener() {

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ALT_GRAPH) {
					e.consume();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ALT_GRAPH) {
					e.consume();
				}

			}

			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ALT_GRAPH) {
					e.consume();
				}
			}
		};
		menu.addKeyListener(esokl);
		/*
		 * if (nodeNameInput != null) nodeNameInput.addKeyListener(esokl); if (nameInput
		 * != null) nameInput.addKeyListener(esokl); if (modelName != null)
		 * modelName.addKeyListener(esokl);
		 */

		// constrain maximal width of popup menu
		Dimension size = menu.getPreferredSize();
		size.width = Math.min(size.width, 250);
		menu.setPopupSize(size);

		if (modelName != null)
			modelName.textField.setCaretPosition(0);
		if (nameInput != null)
			nameInput.textField.setCaretPosition(0);
		if (nodeNameInput != null)
			nodeNameInput.textField.setCaretPosition(0);
		if (valueInput != null)
			valueInput.textField.setCaretPosition(0);
		if (svalueInput != null)
			svalueInput.textField.setCaretPosition(0);

		// show and request focus
		menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
		this.desktop.mainFrame.requestFocus();
		this.requestFocusInWindow();
		menu.requestFocusInWindow();
		if (nodeNameInput != null)
			nodeNameInput.requestFocusInWindow();

	}

	public void remove(DecoratorObject dec) {
		this.decorators.remove(dec);
		this.repaint();
	}

	/**
	 * removes all nodes and all edges between them
	 * 
	 * TODO: move this to Graph class
	 */
	public void removeAllSelectedNodesAndEdges() {

		MainFrame.undoStack.startCollectSteps();

		// delete all selected edges
		for (Edge edge : getSelectedEdges()) {
			mri.requestRemoveEdge(edge);
		}

		// compile list of all nodes to be deleted

		List<Node> nodes = new ArrayList<Node>();
		for (Node node : graph.getNodes()) {
			if (node.isSelected()) {
				nodes.add(node);
			}
		}

		// find all edges adjacent to the selected nodes
		List<Edge> edges = new ArrayList<Edge>();

		for (Node node : nodes) {
			for (Edge edge : graph.getEdges()) {
				if (edge.getSource() == node || edge.getTarget() == node) {
					if (!edges.contains(edge)) {
						edges.add(edge);
					}
				}
			}

		}

		// delete all edges
		for (Edge edge : edges) {
			mri.requestRemoveEdge(edge);
		}

		// delete all nodes
		for (Node node : nodes) {
			mri.requestRemoveNode(node);
		}

		MainFrame.undoStack.endCollectSteps();
	}

	@Override
	public void removeEdge(int source, int target, boolean isDoubleHeaded) {
		setUnsavedChanges(true);

		Edge edge;
		if (source != -1) {
			Node snode = graph.getNodeById(source);
			Node tnode = graph.getNodeById(target);
			edge = graph.getEdge(snode, tnode, isDoubleHeaded);
		} else {
			edge = graph.findEdgeFromTriangleToNode(target);
		}
		graph.removeEdge(edge);

		modelChangedEvent();
	}

	@Override
	public void removeNode(int id) {
		setUnsavedChanges(true);

		Node node = graph.getNodeById(id);

		// MainFrame.undoStack.startCollectSteps();
		/*
		 * MainFrame.undoStack.add(new
		 * LinkChangedStep(node.getObservedVariableContainer(),
		 * this.getModelRequestInterface())); MainFrame.undoStack.add(new
		 * LinkChangedStep(node.getGroupingVariableContainer(),
		 * this.getModelRequestInterface()));
		 */
		Desktop.getLinkHandler().unlink(node.getObservedVariableContainer());
		// Desktop.getLinkHandler().unlink(node.getGroupingVariableContainer());

		graph.removeNode(node);

		// MainFrame.undoStack.endCollectSteps();

		// by definition, removeNode has to decrement all node ids in the
		// graph that are higher than id
		Iterator<Node> iterNode = graph.getNodeIterator();
		while (iterNode.hasNext()) {
			Node otherNode = iterNode.next();
			if (otherNode.getId() > id) {
				otherNode.setId(otherNode.getId() - 1);
			}
		}

		// remove node from Nodegroup
		for (int i = 0; i < NodeGroupManager.SIZE; i++) {
			NodeGroup ng = nodeGroupManager.get(i);
			if (ng != null) {
				if (ng.contains(node)) {
					ng.remove(node);

				}
			}
		}

		// if last triangle is removed
		// change mean treatment if all triangles are gone
		if (graph.getMeanTreatment() != Graph.MeanTreatment.explicit) {

			if (!graph.hasTriangles()) {
				graph.setMeanTreatment(Graph.MeanTreatment.ambique);
				mri.setMeanTreatment(graph.getMeanTreatment());
			}
		}

		modelChangedEvent();
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {

		updateFromPopupMenu(arg0);

	}

	public void requestDeleteView() {
		if (isUnsavedChanges()) {
			int result = JOptionPane.showConfirmDialog(this,
					"Do you want to save your unsaved changes in model '" + this.getName() + "' before closing it?",
					"Save Model", JOptionPane.YES_NO_CANCEL_OPTION);
			boolean exportAborted = false;
			if (result == JOptionPane.YES_OPTION) {
				XMLExport export = new XMLExport(this);
				if (file != null) {
					export.export(file);
					desktop.mainFrame.addToRecentFiles(file);
				} else {
					if (export.export() == null)
						exportAborted = true;
				}
				if (!exportAborted) {
					setUnsavedChanges(false);
				}
			}
			if (result != JOptionPane.CANCEL_OPTION && !exportAborted)
				mri.requestDeleteModel();
		} else
			mri.requestDeleteModel();
	}

	public void selectAll() {
		this.graph.selectAll(true);
		this.redraw();

	}

	public void setAtomicOperationInProgress(boolean atomicOperationInProgress) {
		this.atomicOperationInProgress = atomicOperationInProgress;
		if (!atomicOperationInProgress) {
			if (atomicModelChangeCount > 0) {
				modelChangedEvent();
			}
			atomicModelChangeCount = 0;

		}
	}

	private void setDefaultStartingValues() {
		for (Edge edge : graph.getEdges()) {

			if (edge.isFree()) {

				if (edge.isDoubleHeaded()) {
					if (edge.source == edge.target) {
						edge.setValue(1);
						getModelRequestInterface().requestChangeParameterOnEdge(edge);
					} else {
						edge.setValue(0.1);
						getModelRequestInterface().requestChangeParameterOnEdge(edge);
					}
				} else {
					edge.setValue(.5);
					getModelRequestInterface().requestChangeParameterOnEdge(edge);
				}

			}

		}

	}

	@Override
	public void setDefinitionVariable(Edge edge) {
		edge.getDefinitionVariableContainer().setActive(true);

	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setGridShown(boolean grid) {
		this.showGrid = grid;

	}

	@Override
	public void setName(String name) {
		mri.requestChangeModelName(name);
	}

	public String getTitle() {
		String title = mri.getName();
		if (unsavedChanges)
			title += " *";
		title += " - ";
		if (mri == null || showingEstimate == null || showingEstimate.isStartingParameters())
			title += "Starting values";
		else {
			currentEstimatesShownInMenu = stackCurrentEstimates();
			if (showingEstimate.getObjective() == Objective.MAXIMUMLIKELIHOOD)
				title += "Maximum Likelihood Estimate";
			if (showingEstimate.getObjective() == Objective.LEASTSQUARES)
				title += "Least Squares Estimate";

			int rank = 1;
			for (ParameterReader pr : currentEstimatesShownInMenu) {
				if (pr == showingEstimate)
					break;
				if (pr.getObjective().equals(showingEstimate.getObjective()))
					rank++;
			}
			String agentLabel = "";
			if (showingEstimate instanceof ExternalRunUnit)
				agentLabel = ", " + ((ExternalRunUnit) showingEstimate).getAgentLabel();
			if (rank == 1)
				title += " (best" + agentLabel + ")";
			else
				title += " (alternative " + rank + agentLabel + ")";
		}
		return title;
	}

	public void updateTitle() {
		String title = getTitle();
		this.setBorder(new DropShadowBorder(title, 3, Color.gray));
	}

	public void setUnsavedChanges(boolean b) {
		this.unsavedChanges = b;
		updateTitle();
	}

	@Override
	public void setValue(Edge edge) {
		setUnsavedChanges(true);

	}

	private List<ModelRunUnit> stackCurrentEstimates() {
		return ModelRunUnit.stackEqualEstimates(currentEstimates);
	}

	@Override
	public void swapFixed(Edge edge) {

		// if (edge.isFree())

		this.redraw();
		modelChangedEvent();
	}

	@Override
	public void swapLatentToManifest(Node node) {

		node.setIsLatent(!node.isLatent());

		setUnsavedChanges(true);
		// node.setIsLatent(!node.isLatent());
		modelChangedEvent();
		this.redraw();

	}

	private void toggleShowGrid() {
		showGrid = !showGrid;
		Preferences.set("showGridDefault", "" + showGrid);
		redraw();
	}

	private void updateFromPopupMenu(DocumentEvent arg0) {

		if ((menuGridSizeInput != null) && arg0.getDocument() == menuGridSizeInput.getDocument()) {
			double newValue;
			try {
				newValue = Double.parseDouble(arg0.getDocument().getText(0, arg0.getDocument().getLength()));
				newValue = Math.min(100, newValue);
				newValue = Math.max(5, newValue);

				this.gridSize = (int) Math.round(newValue);

				this.repaint();

			} catch (Exception e) {

			}
		}

		if ((thicknessInput != null) && arg0.getDocument() == thicknessInput.getDocument()) {
			double newValue;
			try {
				newValue = Double.parseDouble(arg0.getDocument().getText(0, arg0.getDocument().getLength()));
				newValue = Math.min(10, newValue);
				newValue = Math.max(0.5, newValue);

				MultiStep ms = new MultiStep();

				for (Edge edge : getSelectedEdges()) {
					ms.add(new EdgeStateChangedStep(this, edge));
					edge.setLineWidth((float) newValue);
					// menuContextEdge.setStrokeWidth((float)newValue);
				}

				for (Node node : getSelectedNodes()) {
					ms.add(new NodeStateChangedStep(this, node));
					node.setStrokeWidth((float) newValue);
				}

				MainFrame.undoStack.add(ms);

				this.repaint();
			} catch (NumberFormatException e) {
				// don't do anything
				// e.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if ((labelsizeInput != null) && arg0.getDocument() == labelsizeInput.getDocument()) {
			double newValue;
			try {
				newValue = Double.parseDouble(arg0.getDocument().getText(0, arg0.getDocument().getLength()));
				newValue = Math.min(64, newValue);
				newValue = Math.max(7, newValue);

				// menuContextEdge.setStrokeWidth((float)newValue);

				// create UNDO only on menu close
				for (Edge edge : getSelectedEdges()) {
					// ms.add(new LabelStateChangedStep(ModelView.this, edge));
					edge.getLabel().setFontSize((float) newValue);
				}

				for (Node node : getSelectedNodes()) {
					// ms.add(new NodeStateChangedStep(ModelView.this, node));
					node.setFontSize((int) newValue);
				}

				this.repaint();
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		// update starting value of parameter on an Edge
		if ((svalueInput != null) && arg0.getDocument() == svalueInput.getDocument()) {
			try {
				double newValue = Double.parseDouble(arg0.getDocument().getText(0, arg0.getDocument().getLength()));

				// pass change to backend
				// TvO, 10.12.2012: Added first line to ensure the value is changed on a fixed
				// edge.
				for (Edge edge : getSelectedEdges()) {
					mri.requestSetValue(edge);
					mri.requestSetStartingValue(edge.getParameterName(), newValue);
				}

				// TODO WARNING, DOES THIS WORK?

				// update all matching edges (TODO: check mechanism for automatically updating
				// this! e.g., backend informs all changed edges automatically?!)
				for (Edge edge : this.graph.getEdges()) {
					for (Edge edge2 : getSelectedEdges()) {
						if (edge.getParameterName().equals(edge2.getParameterName())) {
							edge.setValue(newValue);
						}
					}
				}

			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}

			modelChangedEvent();
			this.redraw();
		}

		// update value of parameter on an Edge
		if ((valueInput != null) && arg0.getDocument() == valueInput.getDocument()) {
			try {

				String content = arg0.getDocument().getText(0, arg0.getDocument().getLength());

				if (content.contains(":")) {

					String[] tok = content.split(":");

					if (tok.length == 2) {
						Double lbound = Double.parseDouble(tok[0]);
						Double ubound = Double.parseDouble(tok[1]);

						int numEdges = getSelectedEdges().size();

						// TODO: sort edges first!

						List<Edge> tempEdges = getSelectedEdges();
						int i = 0;

						for (Edge edge : tempEdges) {

							double newValue = lbound + i * (ubound - lbound) / (numEdges - 1);
							i++;

							edge.setValue(newValue);

							mri.requestSetValue(edge);

						}

					} else {
						// Do not react

					}

				} else {

					double newValue = Double.parseDouble(content);

					// MultiStep ms = new MultiStep();

					for (Edge edge : getSelectedEdges()) {
						// ms.add(new EdgeStateChangedStep(this, edge));
						edge.setValue(newValue);
					}

					for (Edge edge : getSelectedEdges()) {
						mri.requestSetValue(edge);

					}

					// MainFrame.undoStack.add(ms);

				}

				// }
				// }
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				// e.printStackTrace();
			}
			modelChangedEvent();
			// this.repaint();
			// this.redraw();
		}

		// update model name
		if ((modelName != null) && arg0.getDocument() == modelName.getDocument()) {
			String text;
			try {
				text = arg0.getDocument().getText(0, arg0.getDocument().getLength());

				// did name change really?
				// System.out.println(arg0);
				if (!this.getModelRequestInterface().getName().equals(text)) {
					this.mri.requestChangeModelName(text);
					desktop.makeUniqueModelName(this);
				}

			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if ((nameInput != null) && arg0.getDocument() == nameInput.getDocument()) {

			try {
				String text = arg0.getDocument().getText(0, arg0.getDocument().getLength());

				text = text.replaceAll(" ", "_");

				for (Edge edge : getSelectedEdges()) {
					edge.setParameterNameByUser(text);
					mri.requestChangeParameterOnEdge(edge);
				}
				this.redraw();
				modelChangedEvent(); // AB: added here to show working tooltip, estimation did start anyhow.

			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

		if ((nodeNameInput != null) && arg0.getDocument() == nodeNameInput.getDocument()) {
			String text;
			try {
				text = arg0.getDocument().getText(0, arg0.getDocument().getLength());

				text = text.replaceAll(" ", "_");

				// this.graph.renameNode(menuContextNode, text);
				this.mri.requestChangeNodeCaption(menuContextNode, text);

			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		if ((nodeGroupInput != null) && arg0.getDocument() == nodeGroupInput.getDocument()) {
			String text;
			try {
				text = arg0.getDocument().getText(0, arg0.getDocument().getLength());

				try {
					double value = Double.parseDouble(text);
					for (Node node : graph.getSelectedNodes())
						node.groupValue = value;
					modelChangedEvent();
				} catch (Exception e) {

				}

			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}

	}

	private void updateShownEstimates(ParameterReader pr) {

		/*
		 * /*parameterView.setParameterReader(currentEstimatesShownInMenu .get(i));
		 * graph.updateWithEstimates(currentEstimatesShownInMenu .get(i));
		 * showingEstimate = currentEstimatesShownInMenu.get(i); this.redraw();
		 */
		if (parameterView != null)
			parameterView.setParameterReader(pr);

		if (graph != null)
			graph.updateWithEstimates(mri, pr);

		showingEstimate = pr;
		updateTitle();

		for (ScriptView cv : codeView) {
			if (cv.isEstimateView())
				cv.update();
		}

		if (currentEstimates != null && currentEstimates.size() > 0 && currentEstimates.get(0) == pr) {
			if (messageObjectContainer.contains(sparklingObject))
				messageObjectContainer.remove(sparklingObject);
		}

		updateNodeR2();

		this.redraw();
	}

	public void updateNodeR2() {
		for (Node node : graph.getNodes()) {

			node.pseudoR2 = computeR2(node);
		}

	}

	@Override
	public void unsetDefinitionVariable(Edge edge) {
		edge.getDefinitionVariableContainer().setActive(false);

	}

	@Override
	public void setGroupingVariable(Node node) {
		node.getGroupingVariableContainer().setActive(true);

	}

	@Override
	public void unsetGroupingVariable(Node node) {
		node.getGroupingVariableContainer().setActive(false);

	}

	public void notifyOfFailedReset() {
		if (messageObjectContainer != null && messageObjectContainer.contains(messageObjectRunning))
			messageObjectContainer.remove(messageObjectRunning);
	}

	public void writeCombinedDataset(File f) {
		writeCombinedDataset(f, null, false, null);
	}

	/**
	 * Writes the combined data set out. If respectMeanTreatment and the field
	 * isExplicitMean is true, the dataset will be mean corrected.
	 * 
	 * 
	 * @param f
	 * @param missingIndicator
	 * @param respectMeanTreatment
	 */
	public void writeCombinedDataset(File f, String missingIndicator, boolean respectMeanTreatment,
			HashMap<VariableContainer, String> nameMapping) {
		combinedData
				.createDataset(respectMeanTreatment && graph.getMeanTreatment() != MeanTreatment.explicit, nameMapping)
				.save(f, missingIndicator);
	}

	public CombinedDataset getCombinedDataset() {
		return combinedData;
	}

	public boolean hasDefinitionEdges() {
		Iterator<Edge> edges = graph.getEdgeIterator();
		while (edges.hasNext()) {
			Edge edge = edges.next();
			if (edge.isDefinitionVariable())
				return true;
		}
		return false;
	}

	@Override
	public void addAuxiliaryVariable(String variableName, int index) {

		VariableStack aux = graph.getAuxiliaryStack();

		VariableContainer vcont = aux.addVariableContainer();

		/*
		 * TODO TvO: Changed interface according to agreement, needs adaptation here try
		 * { Desktop.getLinkHandler().link(dataset, index, vcont, mri); } catch
		 * (LinkException e) { // TODO Auto-generated catch block e.printStackTrace(); }
		 */

		modelChangedEvent();
	}

	@Override
	public void addControlVariable(String variableName, int variableID) {

		VariableStack aux = graph.getControlStack();

		VariableContainer vcont = aux.addVariableContainer();

		/*
		 * TODO TvO: Changend interface, needs treatment try {
		 * Desktop.getLinkHandler().link(dataset, index, vcont, mri); } catch
		 * (LinkException e) { e.printStackTrace(); }
		 */

		modelChangedEvent();

	}

	@Override
	public void notifyOfStrategyChange(Strategy strategy) {
		modelChangedEvent();
	}

	@Override
	public void removeAuxiliaryVariable(int variableID) {
		// TODO Auto-generated method stub
	}

	@Override
	public void removeControlVariable(int variableID) {
		// TODO Auto-generated method stub
	}
}
