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
 * Created on 06.01.2017
 */
package dirichletProcess;

import engine.Statik;
import engine.backend.Model;
import engine.backend.RAMModel;

public class SEMLikelihoodFunction extends DirichletLikelihoodFunction {

    private double[][] data;
    private RAMModel model;             // missingness is NOT treated as FIML, but with missing covariance matrix!
    private double[][] priorCov;
    private double[] priorMean;
    private double priorN;
    /** if fixedGroups is not null, then personIDs in the likelihood function will be interepreted as IDs of the supergroups. */
    private int[][] fixedGroups;        
    
    private double[] startingValues;
    
    public SEMLikelihoodFunction(RAMModel model, double[][] priorCov, double[] priorMean, double priorN) {this(model.data, model, priorCov, priorMean, priorN);}
    public SEMLikelihoodFunction(double[][] data, RAMModel model, double[][] priorCov, double[] priorMean, double priorN) {
        this.data = data; this.model = model; this.priorCov = priorCov; this.priorMean = priorMean; this.priorN = priorN; fixedGroups = null;
        model.setData(data);
        
        Statik.covarianceMatrixAndMeans(data, model.dataMean, model.dataCov, Model.MISSING);
        model.setDataDistribution(model.dataCov, model.dataMean, model.anzPer);
        startingValues = model.estimateML();
    }
    public SEMLikelihoodFunction(double[][] data, RAMModel model, double[][] priorCov, double[] priorMean, double priorN, int[][] fixedGroups) {
        this(data, model, priorCov, priorMean, priorN);
        setFixedGroups(fixedGroups);
    }
    
    /**
     * If fixedGroups is not null, personIDs in the likelihood function will be interpreted as IDs of the supergroups, which then will be
     * added to the likelihood function as a full group. This allows to bind some data lines together so that they can only be moved as full groups;
     * just specify these groups in the fixedGroups array and pass it here. It also allows to take out some participants from the clustering by
     * not repeating them in the supergroups. 
     * 
     * @param fixedGroups
     */
    public void setFixedGroups(int[][] fixedGroups) {this.fixedGroups = fixedGroups;}
    
    @Override
    public double getLogLikelihood(int person, int[] group) {
        int[] combinedGroup;
        if (fixedGroups == null || group == null) combinedGroup = group; else {
            int groupN = 0; for (int i=0; i<group.length; i++) groupN += fixedGroups[group[i]].length;
            combinedGroup = new int[groupN]; 
            int k = 0;
            for (int i=0; i<group.length; i++) for (int j=0; j<fixedGroups[group[i]].length; j++) combinedGroup[k++] = fixedGroups[group[i]][j]; 
        }
        
        Statik.covarianceMatrixAndMeans(data, model.dataMean, model.dataCov, model.MISSING, combinedGroup);
        int groupN = (combinedGroup==null?0:combinedGroup.length);
        double totalN = groupN + priorN;
        if (groupN >= 2) {
            for (int i=0; i<model.anzVar; i++) {
                model.dataMean[i] = (model.dataMean[i]*groupN + priorMean[i]*priorN) / totalN; 
                for (int j=0; j<model.anzVar; j++)
                    model.dataCov[i][j] = (model.dataCov[i][j]*groupN + priorCov[i][j]*priorN) / totalN;
            } 
        } else {Statik.copy(priorMean, model.dataMean); Statik.copy(priorCov, model.dataCov);}
        model.setDataDistribution(model.dataCov,  model.dataMean, (int)Math.round(totalN));
        model.estimateML(startingValues, model.suggestedEPS);
        
        if (fixedGroups == null) return -0.5 * model.getSinglePersonMinusTwoLogLikelihood(data[person]);
        else {
            double erg = 0;
            for (int i=0; i<fixedGroups[person].length; i++) erg += -0.5 * model.getSinglePersonMinusTwoLogLikelihood(data[fixedGroups[person][i]]);
            return erg;
        }
    }
}
