package parallelProcesses;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;

import parallelProcesses.ParallelProcessHandler.ProcessStatus;
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
import gui.fancy.DropShadowBorder;
import gui.frames.MainFrame;
import gui.graph.Edge;
import gui.graph.Node;
import gui.linker.LinkException;
import gui.undo.LinkChangedStep;
import gui.undo.LinkStep;
import gui.views.View;

public class ParallelProcessView extends View implements ActionListener 
{

	/**
	 * 
	 */
    private static final long serialVersionUID = 23987234602893423L;

	private ParallelProcess process;

	private JProgressBar progressBar;
	private JButton terminateButton;
	private JButton pauseResumeButton;
	private JLabel processLabel;
	FontMetrics fm;

	public ParallelProcessView(Desktop desktop) {
		super(desktop);
		super.setSelectable(false);
		super.setResizable(false); // AB?

		progressBar = new JProgressBar(0,100);
		terminateButton = new JButton("Terminate");
		pauseResumeButton = new JButton("Pause");
		terminateButton.addActionListener(this);
		pauseResumeButton.addActionListener(this);
		processLabel = new JLabel("Initialization");
		//processLabel.setOpaque(true);
		
		this.minimal_height = 50;
		
		JPanel back = new JPanel();
		back.setOpaque(false);
		//JPanel back = this;
		
		this.add(back);
		back.getInsets().set(3,7,3,7); 
		back.setSize(350,70);
		this.setSize(350,70);
		
		Border padding = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		back.setBorder(padding);
		
		this.setBorder( new DropShadowBorder("",2,Color.gray));
		
		//padding = new DropShadowBorder("Process", 10);
		//this.setBorder(padding);
		//back.setBorder(padding);

        back.setLayout(new BorderLayout());
        JPanel buttonPanel = new JPanel(new FlowLayout());
        back.add(progressBar, BorderLayout.CENTER);
        back.add(buttonPanel, BorderLayout.EAST);
        back.add(processLabel, BorderLayout.NORTH);
        buttonPanel.add(pauseResumeButton);
        buttonPanel.add(terminateButton);
		
        this.setToolTipText("Progress Report");
		setOpaque(false);

		buttonPanel.setOpaque(false);
		buttonPanel.setBackground(Color.red);
		//this.setBackground(Color.green);
		this.processLabel.setOpaque(false);
		//setOpaque(true);
        //setSize(300,40);
		

		
		final ParallelProcessView fthis = this;
		(new Thread(new Runnable() {
            
            @Override
            public void run() {
                boolean goon = true;
                while (goon) {
                    double progress = process.getProgress();
                    fthis.progressBar.setValue((int)Math.round(100*progress));
                    fthis.progressBar.validate();
                    fthis.progressBar.repaint();
                    if (process.getStatus() == ProcessStatus.DEAD) {
                        getDesktop().removeView(fthis);
                        goon = false;
                    }
                    try {
                        if (goon) Thread.sleep(100);
                    } catch (InterruptedException e) {}
                }
            }
        })).start();
	}
	
	public ParallelProcessView(Desktop desktop, ParallelProcess process) {
		this(desktop);
		this.process = process;
		processLabel.setText("Creating "+process.getTargetName());
		process.setMainParallelProcessView(this);
	}
	
	public void paintBackground(Graphics2D g) {

		DropShadowBorder.paintBackgroundInComponent(this, g, this.getBackground());
	}
	
	@Override
	public void actionPerformed(ActionEvent arg0) {

        if (arg0.getSource() == pauseResumeButton) {
            if (process.getStatus() == ProcessStatus.RUNNING) {
                process.requestTransferToStatus(ProcessStatus.PAUSED);
                pauseResumeButton.setText("Resume");
            } 
            else if (process.getStatus() == ProcessStatus.PAUSED) {
                process.requestTransferToStatus(ProcessStatus.RUNNING);
                pauseResumeButton.setText("Pause");
            } 
        }

        if (arg0.getSource() == terminateButton) {
            process.requestTransferToStatus(ProcessStatus.RUNNING);
            process.requestTransferToStatus(ProcessStatus.FINISHED);
        }
	}
}
