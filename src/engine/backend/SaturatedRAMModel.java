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
 * Created on 04.10.2015
 */
package engine.backend;

import engine.Statik;

public class SaturatedRAMModel extends RAMModel {

    public SaturatedRAMModel(int[][] symPar, double[][] symVal, int[][] asyPar, double[][] asyVal, int[] meanPar, double[] meanVal, int anzVar) {
        super(symPar, symVal, asyPar, asyVal, meanPar, meanVal, anzVar);
    }

    public SaturatedRAMModel(int anzVar, boolean meansFixedToZero) {
        this.anzFac = this.anzVar = anzVar;
        
        asyPar = new int[anzVar][anzVar]; for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) asyPar[i][j] = -1;
        asyVal = new double[anzVar][anzVar];
        symPar = new int[anzVar][anzVar]; 
        int k=0; for (int i=0; i<anzVar; i++) for (int j=i;j<anzVar; j++) symPar[i][j] = symPar[j][i] = k++;
        symVal = Statik.identityMatrix(anzVar);
        meanPar = new int[anzVar]; for (int i=0; i<anzVar; i++) meanPar[i] = (meansFixedToZero?-1:k++);
        meanVal = new double[anzVar];
        
        filter = Statik.enumeratIntegersFrom(0, anzVar-1); 

        inventParameterNames();
        setAnzParAndCollectParameter(-1);            
        anzPer = 0; data = new double[0][];
        
        fisherInformationMatrix = new double[anzPar][anzPar];
    }
    

    public void setParameterToDistribution(double[] mean, double[][] cov) {
        for (int i=0; i<anzFac; i++) {
            if (meanPar[i] != NOPARAMETER && mean != null) setParameter(meanPar[i], mean[i]);
            for (int j=0; j<anzFac; j++) setParameter(symPar[i][j], cov[i][j]);
        }
    }
    
}
