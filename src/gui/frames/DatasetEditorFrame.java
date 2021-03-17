package gui.frames;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import engine.RawDataset;

public class DatasetEditorFrame extends JFrame implements TableModelListener
{
	JTable table;
	private RawDataset dataset;
	
	public DatasetEditorFrame(RawDataset dataset)
	{
		this.dataset = dataset;
		
		this.setSize(500,500);
		this.setLocationRelativeTo(null);
	
		//dataset.getData();
		Object[][]  data = new Object[dataset.getNumRows()][dataset.getNumColumns()];
		for (int i=0; i < dataset.getNumRows(); i++)
		{
			for (int j=0; j < dataset.getNumColumns(); j++)
			{
				data[i][j] = dataset.get(i,j);
			}
		}
		
		table = new JTable( data, dataset.getColumnNames() );
		table.getModel().addTableModelListener(this);
		  
		this.add(table, BorderLayout.CENTER);
		
		
	}

	@Override
	public void tableChanged(TableModelEvent arg0) {
		 int row = arg0.getFirstRow();
	        int column = arg0.getColumn();
	        TableModel model = (TableModel)arg0.getSource();
	        String columnName = model.getColumnName(column);
	        Object data = model.getValueAt(row, column);
		
	        try {
	        	double d = Double.parseDouble(data.toString());
	        	this.dataset.setColumn(row, column, d);
	        } catch (Exception e) {}
	}
	
	
}
