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
				objData[i][j] = Double.valueOf(data[i][j]);
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
