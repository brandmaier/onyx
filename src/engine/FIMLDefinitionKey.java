/*
 * Created on 18.03.2012
 */
package engine;

import engine.backend.Model;

public class FIMLDefinitionKey {

    public double[] definitionRow;
    public int perID;
    
    public FIMLDefinitionKey(double[] definitionRow, int perID) {
        this.definitionRow = definitionRow;
        this.perID = perID;
    }
    
    public int hashCode() {
        int erg = 0;
        if (definitionRow != null) for (int i=0; i<definitionRow.length; i++) erg += Math.round(definitionRow[i]*1000);
        return erg;
    }
    
    @Override
    public boolean equals(Object secondObject) {
        if (!(secondObject instanceof FIMLDefinitionKey)) return false;
        FIMLDefinitionKey second = (FIMLDefinitionKey)secondObject;
        boolean erg = true;
        for (int i=0; i<definitionRow.length; i++) 
            erg = erg && (definitionRow[i] == second.definitionRow[i]);
        return erg;
    }
    
    public String toString() {return "Definition variables: "+(definitionRow==null?"null":Statik.matrixToString(definitionRow));}
    
}
