/*
 * Created on 15.09.2012
 */
package engine;

import java.util.List;

import javax.swing.JOptionPane;

import engine.backend.Model;

public class SimulatedDataset extends RawDataset {
    
    private OnyxModel model;
    private int percentMissing;
    
    public SimulatedDataset(List<String> variableNames, OnyxModel model, int percentMissing) {
        super(model.anzPer, model.anzVar);
        this.model = model;
        this.setName("Simulated Dataset");
        this.percentMissing = percentMissing;
        this.columnNames = variableNames;
        if (!simulate()) this.data = null;
    }
    
    /**
     * 
     * @return  true if successful, false if failed. 
     */
    public boolean simulate() {
    	try {
    	    if (model.hasDefinitionVariables()) {JOptionPane.showMessageDialog(null, "Data cannot be simulated from this model because it contains definition variables."); return false;}
    		model.createData(model.anzPer);
    	} catch (Exception e) {
    		// probably due to cholesky error
    		JOptionPane.showMessageDialog(null, "Data cannot be simulated from this model. Is the model covariance matrix positive definite?");
    		return false;
    	}
        
        // add missingness
        for (int i=0; i<model.anzPer; i++) 
            for (int j=0; j<model.anzVar; j++) {
                if (model.getRandom().nextInt(100) < percentMissing) model.data[i][j] = Model.MISSING;
            }
        
        this.setData(model.data,columnNames);
        return true;
    }

}
