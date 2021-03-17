package gui.views;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import engine.Dataset;

public class DatasetListRenderer implements ListCellRenderer {

	 protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();
	private Dataset dataset;

	 public DatasetListRenderer(Dataset dataset)
	 {
		 this.dataset = dataset;
	 }
	
	@Override
	public Component getListCellRendererComponent(JList list, Object value,
		    int index, boolean isSelected, boolean cellHasFocus)
	{
		

		
		if (dataset.hasIdColumn()) {
			if (dataset.getIdColumn()==index) {
				value = value+ " [ID]";
			}
		}
		
		JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
		        isSelected, cellHasFocus);
		
		
		return renderer;
	}

}
