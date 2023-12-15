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

import gui.Desktop;
import gui.Utilities;
import gui.frames.MainFrame;
import gui.graph.Edge;
import gui.graph.Node;
import importexport.CorrelationMatrixExport;
import importexport.EstimateHistoryExport;
import importexport.EstimateTextExport;
import importexport.LISRELMatrixTextExport;
import importexport.LavaanExport;
import importexport.MatrixTextExport;
import importexport.MplusExport;
import importexport.OnyxJavaExport;
import importexport.OpenMxExport;
import importexport.OpenMxMatrixExport;
import importexport.SemExport;
import importexport.StringExport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.util.LinkedList;

import javax.swing.BorderFactory;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.Highlighter.HighlightPainter;

import engine.Dataset;
import engine.ModelListener;
import engine.ModelRun.Status;
import engine.ModelRun.Warning;
import engine.RawDataset;
import engine.backend.Model.Strategy;
import external.diff_match_patch.Diff;
import external.diff_match_patch.Operation;

public class ScriptView extends View implements ModelListener, ComponentListener,
		ActionListener, KeyListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private ModelView modelView;
	JScrollPane scrollPane;
	JTextArea textArea;

	StringExport exporter;

	//private int diamondInset;

	private JMenuItem menuDeleteView;

	private JMenu menuExport;
	private JMenuItem menuExportOpenMxPath, menuExportOpenMxMatrix, menuExportMPlus, menuExportLavaan, menuExportSem, menuExportMatrices, menuExportLISRELMatrices, 
	                  menuExportOnyxJava, menuExportRunner, menuExportHistory;

	private JMenuItem menuSaveData;
    private JRadioButtonMenuItem menuUpdate;
	private TitledBorder titledBorder;
	
	boolean delayUpdate = false;
	private boolean isUpdating = true;
	int delayedUpdates = 0;

//	private JMenuItem menuEPS;
	
	float fontSize = 13;

	private JMenuItem menuCovariance;

	private JMenuItem menuLatents;

	private boolean highlight = true;

	private JMenuItem menuExportCovarianceMatrices;

	public ScriptView(Desktop desktop, ModelView modelView, StringExport exporter) {
		super(desktop);

		this.exporter = exporter;
		this.modelView = modelView;
		
		
		this.addComponentListener(this);
		this.addKeyListener(this);


		textArea = new JTextArea("Welcome!");
	//	textArea = new JEditorPane() {
			
	//	};
		//textArea.setContentType("text/html");
		scrollPane = new JScrollPane(textArea);

		Font font = new Font("Monospaced", Font.PLAIN, (int)fontSize);
		textArea.setFont(font);

		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);
		//textArea.setL
		
		this.setLayout(new BorderLayout());
		// this.setBorder( BorderFactory.createBevelBorder(1));
		titledBorder = BorderFactory.createTitledBorder(exporter.getHeader());
		this.setBorder(titledBorder);
		this.add(scrollPane, BorderLayout.CENTER);

		textArea.addMouseListener(this);

		// TODO: make sure that this gets removed when View is closed!!
		this.modelView.getModelRequestInterface().addModelListener(this);

		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);

		//textArea.setBorder(BorderFactory.createLineBorder(Color.black, 1));
		//textArea.set

		
		int textWidth = exporter.getTextWidth();
		int width = Math.max(500,Math.min(desktop.getWidth(), (int)Math.round(0.7 * font.getSize() * textWidth)));
		
		setSize(width, 250);
		setOpaque(true);
		setBackground(Color.white);
		// setBackground(Color.red);

		// this.addMouseListener(this);

		/*
		 * this.invalidate(); this.validate(); this.update();
		 */
		this.update();
		Highlighter h = textArea.getHighlighter();
		h.removeAllHighlights();
		
		this.textArea.addKeyListener(this);
	}
	
	
	public void keyReleased(KeyEvent arg0) {

		/*
		 * if (arg0.getKeyCode()==17 || arg0.getKeyCode()==157)
		 * commandOrControlDown = false; if (arg0.getKeyCode()==16) shiftDown =
		 * false;
		 */

		//boolean commandOrControlDown = arg0.isControlDown() || arg0.isMetaDown();	
	//if (commandOrControlDown && arg0.getKeyCode() == KeyEvent.VK_W) {
		/*Export export = new PDFExport(this);
		export.export();*/
		//super.exportToPDF();
//
//		System.out.println(arg0);
		if (arg0.getKeyChar() == '+') {
			fontSize+=1;
			
			this.textArea.setFont(this.textArea.getFont().deriveFont( (float)fontSize ));
			this.repaint();
		//	System.out.println("INC"+fontSize);
		} else if (arg0.getKeyChar()=='-') {
			fontSize-=1;
			this.textArea.setFont(this.textArea.getFont().deriveFont( (float)fontSize));
			this.repaint();
		}
	
	if (!arg0.isConsumed()) {
		desktop.keyReleased(arg0);
	}
	}

	/*private void adjustScrollPanesize() {

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

	
		 

		scrollPane.setPreferredSize(new Dimension(w, h));
		scrollPane.setSize(new Dimension(w, h));
		scrollPane.setLocation(x, y);
	}
*/
	public void update() {
		update(true);
	}

	private void update(boolean updateHighlight) {
        // if updating is turned off, the method exits without changes
        if (!isUpdating) return;

		if (exporter == null) {
			System.err.println("Warning! Called ScriptView.update() without exporter");
			return;
		}
		

		if (exporter instanceof CorrelationMatrixExport) {
			this.setHighlight(false);
		} else {
			this.setHighlight(true);
		}

	    if (titledBorder != null) titledBorder.setTitle(exporter.getHeader());
	    this.repaint();
	    
		if (delayUpdate) {
			delayedUpdates++;
			return;
		}
		
		String oldRepresentation = this.textArea.getText();
		if (oldRepresentation == null) return;

		String representation;
		
		try {
		representation = exporter.createModelSpec(modelView,modelView.getName(), false);
		} catch (Exception e) {
			representation = "An error occured!";
			e.printStackTrace();
		}
		
		if (oldRepresentation.equals(representation)) {
			return;
		}

		// update text field
		this.textArea.setText(representation);

		final HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(
				Color.YELLOW);

		/*
		 * update highlights, if text has changed
		 */
		Highlighter h = textArea.getHighlighter();
		h.removeAllHighlights();

		if (updateHighlight && hasHighlight()) {
			external.diff_match_patch dmp = new external.diff_match_patch();
			LinkedList<Diff> result = dmp.diff_main(oldRepresentation,
					representation);

			// if there are differences...
			if (!(result.size() == 1 && result.get(0).operation == Operation.EQUAL)) {

				// System.out.println("Update!"+ result.size()+" differences");

				// h.removeAllHighlights();

				int index = 0;
				for (Diff diff : result) {
					//System.out.println(diff.operation+diff.text);
					if (diff.operation == Operation.INSERT) {
						try {
							h.addHighlight(index, index + diff.text.length(),
									painter);

						} catch (BadLocationException e) {
							System.out.println("Bad Location!");
							e.printStackTrace();
						}
						// diff.
						index += diff.text.length();
					} else if (diff.operation == Operation.EQUAL) {
						index += diff.text.length();
					}

					// System.out.println(diff);

				}

			}

		}

        /*if (this.hasFocus()) System.out.println("SCRIPT VIEW HAS FOCUS"); else System.out.println("Script view has no focus.");
        if (textArea.hasFocus()) System.out.println("TEXTAREA VIEW HAS FOCUS"); else System.out.println("Text Area has no focus.");
		*/
	}

	public boolean hasHighlight() {
		return highlight;
	}
	
	public void setHighlight(boolean h) {
		highlight = h;
	}


	public void addPluginMenu(JPopupMenu menu)
	{
		if (exporter instanceof CorrelationMatrixExport) {
		
		if (menuCovariance == null) {
			menuCovariance = new JMenuItem("Toggle Covariance/Correlation");
			menuCovariance.addActionListener(this);
		}
		
		if (menuLatents == null) {
			menuLatents = new JMenuItem("Show/Hide Latents");
			menuLatents.addActionListener(this);
		}
		
		menu.addSeparator();
		menu.add(menuCovariance);
		menu.add(menuLatents);
	
		}
	}
	
	@Override
	public void addNode(Node node) {
		update();
	}

	@Override
	public void addEdge(Edge edge) {
		update();

	}

	@Override
	public void swapLatentToManifest(Node node) {
		update();

	}

	@Override
	public void changeName(String name) {
		update();

	}

	@Override
	public void removeEdge(int source, int target, boolean isDoubleHeaded) {
		update();

	}

	@Override
	public void removeNode(int id) {
		update();

	}
	
	public void dispose()
	{
		super.dispose();
		this.modelView.codeView.remove(this);

	}

	@Override
	public void deleteModel() {
		update();

	}

	@Override
	public void cycleArrowHeads(Edge edge) {
		update();

	}

	@Override
	public void swapFixed(Edge edge) {
		update();

	}

	@Override
	public void changeStatus(Status status) {
		//update();	TODO: change?

	}

	@Override
	public void notifyOfConvergedUnitsChanged() {
		update();
	}

	@Override
	public void setValue(Edge edge) {
//		System.out.println("SET VALUE");
		update();

	}

	@Override
	public void notifyOfStartValueChange() {
		update();

	}

	@Override
	public void changeParameterOnEdge(Edge edge) {
		update();

	}

	@Override
	public void notifyOfWarningOrError(Warning warning) {}

    @Override
    public void notifyOfClearWarningOrError(Warning warning) {}
	
	
	@Override
	public void newData(int n, boolean isRawData) {
		// ignore

	}

	@Override
	public void changeNodeCaption(Node node, String name) {
		update();

	}

	@Override
	public void componentHidden(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		// adjustScrollPanesize();

		this.getLayout().layoutContainer(this);
		this.invalidate();
		this.validate();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

		super.mouseClicked(arg0);
		if (arg0.isConsumed())
			return;
		
		textArea.getHighlighter().removeAllHighlights();

		if (Utilities.isRightMouseButton(arg0)) {

			JPopupMenu menu = new JPopupMenu();

			if (menuSaveData == null) {
				menuSaveData = new JMenuItem("Save script");
				menuSaveData.addActionListener(this);
			}

			if (menuDeleteView == null) {
				menuDeleteView = new JMenuItem("Close script");
				menuDeleteView.addActionListener(this);
			}

			if (menuExport == null) {
				menuExport = new JMenu("Type of script");
			}

            if (menuExportMPlus == null) {
                menuExportMPlus = new JMenuItem("MPlus");
                menuExportMPlus.addActionListener(this);

            }
            if (menuExportLavaan == null) {
                menuExportLavaan = new JMenuItem("Lavaan");
                menuExportLavaan.addActionListener(this);

            }
            if (menuExportSem == null) {
                menuExportSem = new JMenuItem("sem package");
                menuExportSem.addActionListener(this);

            }

            if (menuExportOpenMxPath == null) {
                menuExportOpenMxPath = new JMenuItem("OpenMx (Path)");
                menuExportOpenMxPath.addActionListener(this);
            }
            if (menuExportOpenMxMatrix == null) {
                menuExportOpenMxMatrix = new JMenuItem("OpenMx (Matrix)");
                menuExportOpenMxMatrix.addActionListener(this);
            }

            if (menuExportMatrices == null) {
                menuExportMatrices = new JMenuItem("RAM Notation");
                menuExportMatrices.addActionListener(this);
            }

            if (menuExportLISRELMatrices == null) {
                menuExportLISRELMatrices = new JMenuItem("LISREL Notation");
                menuExportLISRELMatrices.addActionListener(this);
            }
            

            if (menuExportCovarianceMatrices == null) {
                menuExportCovarianceMatrices = new JMenuItem("Covariance Matrix");
                menuExportCovarianceMatrices.addActionListener(this);
            }

            if (menuExportOnyxJava == null) {
                menuExportOnyxJava = new JMenuItem("Onyx Java");
                menuExportOnyxJava.addActionListener(this);
            }

            if (menuExportRunner == null) {
                menuExportRunner = new JMenuItem("Text Estimate");
                menuExportRunner.addActionListener(this);
            }

            if (menuExportHistory == null) {
                menuExportHistory = new JMenuItem("Estimation History");
                menuExportHistory.addActionListener(this);
            }
            menuExport.add(menuExportOpenMxPath);
            menuExport.add(menuExportOpenMxMatrix);
            menuExport.add(menuExportMPlus);
            menuExport.add(menuExportLavaan);
            menuExport.add(menuExportSem);
            menuExport.add(menuExportMatrices);
            menuExport.add(menuExportLISRELMatrices);
            menuExport.add(menuExportCovarianceMatrices);
            if (MainFrame.DEVMODE) menuExport.add(menuExportOnyxJava);
            menuExport.add(menuExportRunner);
            menuExport.add(menuExportHistory);

            if (menuUpdate == null) {
                menuUpdate = new JRadioButtonMenuItem("Update");
                menuUpdate.addActionListener(this);
                menuUpdate.setSelected(isUpdating);
            }
            
			menu.add(menuSaveData);
			menu.add(menuExport);
            menu.add(menuUpdate);
			menu.add(menuDeleteView);
			
		
			
			addPluginMenu(menu);

			menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());

		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == menuDeleteView) {
			this.setVisible(false);
			this.desktop.removeView(this);
			this.modelView.getModelRequestInterface().removeModelListener(this);
			this.modelView.codeView.remove(this);
		}

		if (arg0.getSource() == menuSaveData) {
			if (exporter != null)
				exporter.export();

		}

		if (arg0.getSource() == menuExportOpenMxPath) {
			exporter = new OpenMxExport(this.modelView);
			isUpdating = true; update(false);
		}

        if (arg0.getSource() == menuExportOpenMxMatrix) {
            exporter = new OpenMxMatrixExport(this.modelView);
            isUpdating = true; update(false);
        }

        if (arg0.getSource() == menuExportMPlus) {
            exporter = new MplusExport(this.modelView);
            isUpdating = true; update(false);
        }

        if (arg0.getSource() == menuExportLavaan) {
            exporter = new LavaanExport(this.modelView);
            isUpdating = true; update(false);
        }
        
        if (arg0.getSource() == menuExportCovarianceMatrices) {
            exporter = new CorrelationMatrixExport(this.modelView);
            isUpdating = true; update(false);
        }

        if (arg0.getSource() == menuExportSem) {
            exporter = new SemExport(this.modelView);
            isUpdating = true; update(false);
        }

        if (arg0.getSource() == menuExportMatrices) {
            exporter = new MatrixTextExport(this.modelView);
            isUpdating = true; update(false);
        }

        if (arg0.getSource() == menuExportLISRELMatrices) {
            exporter = new LISRELMatrixTextExport(this.modelView);
            isUpdating = true; update(false);
        }
        
        if (arg0.getSource() == menuExportOnyxJava) {
            exporter = new OnyxJavaExport(this.modelView);
            isUpdating = true; update(false);
        }
        
        if (arg0.getSource() == menuExportRunner) {
            exporter = new EstimateTextExport(this.modelView);
            isUpdating = true; update(false);
        }
        
        if (arg0.getSource() == menuExportHistory) {
            exporter = new EstimateHistoryExport(this.modelView);
            isUpdating = true; update(false);
        }


        
        if (arg0.getSource()==menuLatents) {
//        	()
        	if (exporter instanceof CorrelationMatrixExport) {
        		CorrelationMatrixExport cme = ((CorrelationMatrixExport)exporter);
        		cme.observed = !cme.observed;
        		//this.redraw();
        		isUpdating = true; update();
        	}
        }
        
        if (arg0.getSource()==menuCovariance) {
//        	()
        	if (exporter instanceof CorrelationMatrixExport) {
        		CorrelationMatrixExport cme = ((CorrelationMatrixExport)exporter);
        		cme.correlation = !cme.correlation;
        		//this.redraw();
        		isUpdating = true; update();
        	}
        }
        
        if (arg0.getSource() == menuUpdate) {
            isUpdating = !isUpdating;
            update();
        }
        menuUpdate.setSelected(isUpdating);
	}

	@Override
	public void setDefinitionVariable(Edge edge) {update();}
    @Override
    public void unsetDefinitionVariable(Edge edge) {update();}
    @Override
    public void setGroupingVariable(Node node) {update();}
    @Override
    public void unsetGroupingVariable(Node node) {update();}
	

	public void updateDelayed() {
	
		delayUpdate = false;
		if (delayedUpdates > 0) update();
		delayedUpdates=0;	
	}



    public boolean isEstimateView() {
        return (exporter instanceof EstimateTextExport);
    }


    @Override
    public void notifyOfFailedReset() {
        // TODO Auto-generated method stub
        
    }


	@Override
	public void addDataset(Dataset dataset, int x, int y) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void addDataset(double[][] dataset, String name, String[] additionalVariableNames, int x, int y) {
		// TODO Auto-generated method stub
		
	}


    @Override
    public void addAuxiliaryVariable(String name, int index) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void addControlVariable(String name, int index) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void removeAuxiliaryVariable(int index) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void removeControlVariable(int index) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void notifyOfStrategyChange(Strategy strategy) {
        // TODO Auto-generated method stub
        
    }

}
