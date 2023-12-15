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
import java.util.ArrayList;
import java.util.List;

import engine.Dataset;
import engine.RawDataset;

public class TransferableVariableList implements Transferable {

	//public DataFlavor datasetFlavor(Dataset.class, "Dataset");
	public static DataFlavor datasetFlavor = new DataFlavor(RawDataset.class, "Dataset");
//	public static DataFlavor integerFlavor = new DataFlavor(Integer.class, "Integer");
	public static DataFlavor integerListFlavor = new DataFlavor(ArrayList.class, "IntegerList");
	
	int[] index;
	Dataset dataset;
	
	public int[] getIndex() {
		return index;
	}

	public void setIndex(int[] index) {
		this.index = index;
	}

	public Dataset getDataset() {
		return dataset;
	}

	public void setDataset(RawDataset dataset) {
		this.dataset = dataset;
	}

	public TransferableVariableList(Dataset dataset, int[] i)
	{
		this.index = i;
		this.dataset = dataset;
	}
	
	@Override
	public Object getTransferData(DataFlavor flavor)
			throws UnsupportedFlavorException, IOException {
		if (flavor == integerListFlavor) {
			List<Integer> list = new ArrayList<Integer>();
			for (int i=0; i < index.length;i++)
				list.add(index[i]);
			return list;
			
		}
		
		if (flavor == datasetFlavor) {
			return dataset;
		}
		
		return null;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return new DataFlavor[]{datasetFlavor, integerListFlavor};
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		// TODO Auto-generated method stub
		return false;
	}

}
