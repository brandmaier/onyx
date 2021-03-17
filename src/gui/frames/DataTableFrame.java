package gui.frames;

import java.awt.BorderLayout;

import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

import engine.RawDataset;
import engine.DatasetChangedListener;


public class DataTableFrame extends JInternalFrame implements DatasetChangedListener {


	private static final long serialVersionUID = -5843321268466889646L;
	RawDataset dataset;
	JTable table;
	
	JScrollPane scrollPane;
	
	public DataTableFrame(RawDataset dataset) {
		


		// wrap data in Object[][]
		double[][] data = dataset.getData();
		Object[][] objData = new Object[dataset.getNumRows()][dataset.getNumColumns()];
		for (int i=0; i < dataset.getNumRows(); i++)
		{
			for (int j=0; j < dataset.getNumColumns(); j++)
			{
				objData[i][j] = new Double(data[i][j]);
			}
		}
		
	
		// create table
		table = new JTable(objData, dataset.getColumnNames());
		
		// add it to Scrollpane
		scrollPane = new JScrollPane(table);
		table.setFillsViewportHeight(true);

		
		if (dataset.getNumRows()*dataset.getNumColumns()==0)
		{
			this.getContentPane().add(new JLabel("Empty Dataset"),BorderLayout.CENTER);
		} else {
			this.getContentPane().add(scrollPane, BorderLayout.CENTER);

		}
		
		this.setSize(400,300);
		this.setVisible(true);
	}

	@Override
	public void datasetChanged() {
		// TODO Auto-generated method stub
		
	}
	
}
