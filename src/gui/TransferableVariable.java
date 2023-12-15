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
package gui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import engine.Dataset;
import engine.RawDataset;

/**@deprecated */

public class TransferableVariable implements Transferable {

	//public DataFlavor datasetFlavor(Dataset.class, "Dataset");
	public static DataFlavor datasetFlavor = new DataFlavor(Dataset.class, "Dataset");
	public static DataFlavor integerFlavor = new DataFlavor(Integer.class, "IntegerList");
	
	int index;
	Dataset dataset;
	
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(Dataset dataset) {
		this.dataset = dataset;
	}

	public TransferableVariable(RawDataset dataset, int i)
	{
		this.index = i;
		this.dataset = dataset;
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor == integerFlavor) {
			return new Integer(index);
		}
		
		if (flavor == datasetFlavor) {
			return dataset;
		}
		
		return null;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{datasetFlavor, integerFlavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		// TODO Auto-generated method stub
		return false;
	}

}
