package gui.views;

import importexport.EstimateHistoryExport;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import engine.ModelRun.Status;
import engine.ModelRunUnit;
import engine.OnyxModel;
import engine.Statik;
import gui.Desktop;
import gui.Utilities;

public class RunnerTableView extends View implements ComponentListener, ActionListener {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;

	private ModelView modelView;
	JScrollPane scrollPane;
	JTable table;
	DefaultTableModel tableModel;
	
	List<ModelRunUnit> runner;

    private TitledBorder titledBorder;
    public boolean isUpdating = false;
    
    private JMenuItem menuShowHistory;
    private JRadioButtonMenuItem menuUpdate;
	private JMenuItem menuDeleteView;
	
	float fontSize = 13;

	public RunnerTableView(Desktop desktop, ModelView modelView) {
		super(desktop);

		this.addComponentListener(this);
		this.addKeyListener(this);
		this.modelView = modelView;

		tableModel = new DefaultTableModel(); 
        tableModel.addColumn("Name");
        tableModel.addColumn("Fit");
        tableModel.addColumn("Steps");
        tableModel.addColumn("Time");
        tableModel.addColumn("Converged (steps)");
        final RunnerTableView fthis = this;
		table = new JTable( tableModel ) {
		    /**
             * 
             */
            private static final long serialVersionUID = 1L;

            @Override
		    public void paint(Graphics g) {
		        for (int i=0; i<50 && fthis.isUpdating; i++) try{Thread.sleep(2);} catch (Exception e) {}
		        super.paint(g);
		    }
		};
        table.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		scrollPane = new JScrollPane(table);
        table.addMouseListener(this);
        scrollPane.addMouseListener(this);
        table.setFillsViewportHeight(true);

		Font font = new Font("Monospaced", Font.PLAIN, (int)fontSize);
		table.setFont(font); 

		this.setLayout(new BorderLayout());
		titledBorder = BorderFactory.createTitledBorder("Estimation processes overview "+modelView.getName());
		this.setBorder(titledBorder);
		this.add(scrollPane, BorderLayout.CENTER);

        table.getColumnModel().getColumn(0).setPreferredWidth(400);
		setSize(800, 250);
		setOpaque(true);
		setBackground(Color.white);

		Thread updater = new Thread() {
		    public void run() {
		        while (true) {
    		        if (fthis.isUpdating) {try {fthis.update();} catch (Exception e) {}}
    		        try {
    		            Thread.sleep(3000);
    		        } catch (InterruptedException e) {}
		        }
		    }
		};
		update();
        updater.start();
        isUpdating = true;
	}
	/*
	private void adjustScrollPanesize() {
		int pad = 10;

		int x = pad;
		int y = pad;
		int w = Math.max(0, getWidth() - 2 * pad);
		int h = Math.max(0, getHeight() - 2 * pad);

		scrollPane.setPreferredSize(new Dimension(w, h));
		scrollPane.setSize(new Dimension(w, h));
		scrollPane.setLocation(x, y);
	}
*/
	public void update() {
        DefaultTableModel tableModel = (DefaultTableModel) table.getModel();

        int size = tableModel.getRowCount();
        for (int i=0; i<size; i++) tableModel.removeRow(0);

        runner = new ArrayList<ModelRunUnit>(modelView.getModelRequestInterface().getAllUnits());
        
        if (runner.size() == 0) {
            try {
                OnyxModel model = (OnyxModel)modelView.getModelRequestInterface();
                if (model.getStatus() == Status.WAITING) tableModel.addRow(new Object[]{"Estimation process is not started.","","","",""});
                if (model.getStatus() == Status.RESETTING || model.getStatus() == Status.RUNNING) tableModel.addRow(new Object[]{"Estimation is being prepared.","","","",""});
            } catch (Exception e) {tableModel.addRow(new Object[]{"No estimation process to show.","","","",""});}
        }
        
        for (int i=0; i<runner.size(); i++) {
            ModelRunUnit unit = runner.get(i);
            Object[] row = new Object[5];
            row[0] = unit.getName();
            row[1] = Statik.doubleNStellen(unit.fit, 4);
            row[2] = unit.steps+"";
            row[3] = Statik.doubleNStellen(unit.ownTime, 2)+" s";
            row[4] = (unit.isConverged()?"converged ("+unit.getStepsAtConvergence()+")" :"running");
            tableModel.addRow(row);
        }
        tableModel.fireTableDataChanged();
	}


	@Override
	public void componentHidden(ComponentEvent arg0) {
	}

	@Override
	public void componentMoved(ComponentEvent arg0) {
	}

	@Override
	public void componentResized(ComponentEvent arg0) {
		this.getLayout().layoutContainer(this);
		this.invalidate();
		this.validate();
	}

	@Override
	public void componentShown(ComponentEvent arg0) {
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {

		super.mouseClicked(arg0);
		if (arg0.isConsumed()) return;
		
		if (Utilities.isRightMouseButton(arg0)) {

			JPopupMenu menu = new JPopupMenu();

            if (menuShowHistory == null) {
                menuShowHistory = new JMenuItem("Show selected history");
                menuShowHistory.addActionListener(this);
            }
            if (menuUpdate == null) {
                menuUpdate = new JRadioButtonMenuItem("Update");
                menuUpdate.addActionListener(this);
                menuUpdate.setSelected(isUpdating);
            }
			if (menuDeleteView == null) {
				menuDeleteView = new JMenuItem("Close view");
				menuDeleteView.addActionListener(this);
			}

            menu.add(menuShowHistory);
            menu.add(menuUpdate);
			menu.add(menuDeleteView);
			
			menu.show(arg0.getComponent(), arg0.getX(), arg0.getY());
		}
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		if (arg0.getSource() == menuDeleteView) {
			this.setVisible(false);
			this.desktop.removeView(this);
		}

		if (arg0.getSource() == menuShowHistory) {
		    int row = table.getSelectedRow();
		    if (row >= 0 && row < runner.size()) {
                EstimateHistoryExport exporter = new EstimateHistoryExport(modelView);
                exporter.setExplicitRunner(runner.get(row));
    		    ScriptView view = new ScriptView(desktop, modelView, exporter);
                modelView.codeView.add(view);
                desktop.add(view);
		    }
		}
		
		if (arg0.getSource() == menuUpdate) {
		    isUpdating = !isUpdating;
		    menuUpdate.setSelected(isUpdating);
		}

	}

}
