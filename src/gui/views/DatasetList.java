package gui.views;


import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.JList;

import engine.Dataset;
import gui.VariableTransferHandler;

public class DatasetList extends JList {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3070401140490847026L;
	Dataset dataset;
	
	public DatasetList(DefaultListModel listModel, Dataset dataset) {
		super(listModel);
		
		this.dataset = dataset;
		
		this.setCellRenderer(new DatasetListRenderer(dataset));
		
		setDragEnabled(true);
		
		setTransferHandler( new VariableTransferHandler() );
		
		this.setOpaque(false);
		
		this.setVisible(true);
	}

	public Dataset getDataset() {
		return dataset;
	}

	@Override
	public String getToolTipText(MouseEvent event) {
	
		int index = this.locationToIndex(new Point(event.getX(), event.getY()));
		if (index != -1)
		{
			return("<html>"+this.dataset.getColumnTooltip(index)+"</html>");
		} else {
			return super.getToolTipText(event);
		}
		
		
	}
	
}
