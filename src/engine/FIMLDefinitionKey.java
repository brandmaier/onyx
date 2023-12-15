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
