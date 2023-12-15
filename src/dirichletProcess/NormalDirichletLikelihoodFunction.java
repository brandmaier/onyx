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
import engine.backend.SaturatedModel;

/**
 * A dirichlet clustering base likelihood function using a multivariate normal distribution.
 * 
 * @author Timo
 */
public class NormalDirichletLikelihoodFunction extends DirichletLikelihoodFunction {

    private double[][] data;
    private double[][] priorCov;
    private double[] priorMean;
    private double priorN;
    private double[] dataMean;
    private double[][] dataCov;
    private int anzVar;

    /** if fixedGroups is not null, then personIDs in the likelihood function will be interepreted as IDs of the supergroups. */
    private int[][] fixedGroups;        
    
    private double[][] work, covInv;

    public NormalDirichletLikelihoodFunction(double[][] data, double[][] priorCov, double[] priorMean, double priorN) {
        this.data = data; this.priorCov = priorCov; this.priorMean = priorMean; this.priorN = priorN; fixedGroups = null;

        anzVar = priorMean.length;
        dataMean = new double[anzVar]; dataCov = new double[anzVar][anzVar];
        Statik.covarianceMatrixAndMeans(data, dataMean, dataCov, Model.MISSING);
    }
    public NormalDirichletLikelihoodFunction(double[][] data, double[][] priorCov, double[] priorMean, double priorN, int[][] fixedGroups) {
        this(data, priorCov, priorMean, priorN);
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
        
        Statik.covarianceMatrixAndMeans(data, dataMean, dataCov, Model.MISSING, combinedGroup);
        int groupN = (combinedGroup==null?0:combinedGroup.length);
        double totalN = groupN + priorN;
        if (groupN >= 2) {
            for (int i=0; i<anzVar; i++) {
                dataMean[i] = (dataMean[i]*groupN + priorMean[i]*priorN) / totalN; 
                for (int j=0; j<anzVar; j++)
                    dataCov[i][j] = (dataCov[i][j]*groupN + priorCov[i][j]*priorN) / totalN;
            } 
        } else {Statik.copy(priorMean, dataMean); Statik.copy(priorCov, dataCov);}

        if (fixedGroups == null) return -0.5 * getSinglePersonMinusTwoLogLikelihood(data[person]);
        else {
            double erg = 0;
            for (int i=0; i<fixedGroups[person].length; i++) erg += -0.5 * getSinglePersonMinusTwoLogLikelihood(data[fixedGroups[person][i]]);
            return erg;
        }
    }
    
    private double getSinglePersonMinusTwoLogLikelihood(double[] datarow) {
        work = Statik.ensureSize(work, anzVar, anzVar);
        covInv = Statik.ensureSize(covInv, anzVar, anzVar);

        double covDet = Double.NaN;
        try {covDet = Statik.invert(dataCov,covInv, work);} catch(RuntimeException e) {covDet = 0;}

        double erg = anzVar*Model.LNTWOPI + Math.log(covDet);

        for (int i=0; i<anzVar; i++) 
            for (int j=0; j<anzVar; j++)
                erg += covInv[i][j]*(datarow[i]-dataMean[i])*(datarow[j]-dataMean[j]);
        
        return erg;
    }
    
}
