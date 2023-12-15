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

public class FIMLMissingKey {

    public double[] dataRow;
    public int perID;
    
    public FIMLMissingKey(double[] dataRow, int perID) {
        this.dataRow = dataRow;
        this.perID = perID;
    }
    
    public int hashCode() {
        int erg = 0;
        int pow = 1; for (int i=0; i<dataRow.length; i++) {if (!Model.isMissing(dataRow[i])) erg += pow; pow *= 2;}
        return erg;
    }
    
    public boolean isAllMissing() {
        for (int i=0; i<dataRow.length; i++) if (!Model.isMissing(dataRow[i])) return false;
        return true;
    }
    
    @Override
    public boolean equals(Object secondObject) {
        if (!(secondObject instanceof FIMLMissingKey)) return false;
        FIMLMissingKey second = (FIMLMissingKey)secondObject;
        if (dataRow.length != second.dataRow.length) return false;
        boolean erg = true;
        for (int i=0; i<dataRow.length; i++) 
            erg = erg && ((!Model.isMissing(dataRow[i]) && !Model.isMissing(second.dataRow[i])) || (Model.isMissing(dataRow[i]) && Model.isMissing(second.dataRow[i])));
        return erg;
    }
    
    public String toString() {return "Row "+Statik.matrixToString(dataRow);}
    
}
