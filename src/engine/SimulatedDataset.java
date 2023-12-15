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
