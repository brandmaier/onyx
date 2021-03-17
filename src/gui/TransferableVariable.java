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
