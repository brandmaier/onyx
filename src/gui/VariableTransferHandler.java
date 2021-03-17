package gui;

import gui.views.DatasetList;

import java.awt.datatransfer.Transferable;

import javax.swing.JComponent;
import javax.swing.TransferHandler;



public class VariableTransferHandler extends TransferHandler {

	
	public Transferable createTransferable(JComponent c)
	 {
		//get DatasetList
		DatasetList datasetList = (DatasetList)c;
		
		Transferable transferable = new TransferableVariableList(datasetList.getDataset(), datasetList.getSelectedIndices());
		
		return transferable;
	 }
	
	   public int getSourceActions(JComponent c)
	   {
	      return LINK ;
	   }
}
