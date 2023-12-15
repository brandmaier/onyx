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
