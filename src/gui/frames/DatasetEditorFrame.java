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
