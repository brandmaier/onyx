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
 * Created on 04.05.2011
 */
package machineLearning.clustering;

import engine.Statik;

public class LloydsAlgorithm extends ClusteringAlgorithm {

    public static enum methodType {MEAN, NORMAL};
    public methodType method = methodType.MEAN;
    
    public double[][] mean;
    public double[][][] cov;
    public int anzCluster;
    
    public LloydsAlgorithm(double[][] data) {super(data);}
    
    public int[] cluster() {return cluster(2, null);}
    public int[] cluster(int anz, int[] initial) {
        anzCluster = anz;
        
        int[] clust = new int[anzPer]; int[] sizes = new int[anz];
        if (initial == null) {
            for (int i=0; i<anzPer; i++) {int cl = random.nextInt(anz); clust[i] = cl;} 
        } else {
            for (int i=0; i<anzPer; i++) clust[i] = initial[i];
        }
        
        mean = new double[anz][anzVar]; cov = null; double[][][] covInv = null;
        if (method == methodType.NORMAL) {cov = new double[anz][anzVar][anzVar]; covInv = new double[anz][anzVar][anzVar];}
        boolean goon = true;
        while (goon) {
            for (int i=0; i<anz; i++) sizes[i] = 0;
            for (int i=0; i<anz; i++) {for (int j=0; j<anzVar; j++) {mean[i][j] = 0; if (method == methodType.NORMAL) for (int k=0; k<anzVar; k++) {cov[i][j][k] = 0; covInv[i][j][k] = 0;}}} 
            for (int j=0; j<anzPer; j++) {
                for (int k=0; k<anzVar; k++) mean[clust[j]][k] += data[j][k];  
                if (method == methodType.NORMAL) for (int k1=0; k1<anzVar; k1++) for (int k2=0; k2<anzVar; k2++) cov[clust[j]][k1][k2] += data[j][k1]*data[j][k2];
                sizes[clust[j]]++;
            }
            for (int i=0; i<anz; i++) {
                for (int j=0; j<anzVar; j++) mean[i][j] /= sizes[i];
                if (method == methodType.NORMAL) for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) cov[i][j][k] = (cov[i][j][k] / sizes[i]) - mean[i][j]*mean[i][k];
            }
            boolean covarianceMatrixOk = true;
            try {
                if (method == methodType.NORMAL) for (int i=0; i<anz; i++) covInv[i] = Statik.invert(cov[i]);
            } catch (Exception e) {if (method == methodType.NORMAL) System.out.println("Covariance matrix in k-means singular, using only means instead."); covarianceMatrixOk = false;}
            goon = false; 
            for (int i=0; i<anzPer; i++) {
                double minDist = Double.MAX_VALUE; int win = -1;
                for (int j=0; j<anz; j++) {
                    double dist = 0; 
                    if (method == methodType.MEAN || (method == methodType.NORMAL && !covarianceMatrixOk)) for (int k=0; k<anzVar; k++) dist += (data[i][k] - mean[j][k])*(data[i][k] - mean[j][k]);
                    if (method == methodType.NORMAL && covarianceMatrixOk) {
                        dist += Math.log(Statik.determinant(cov[j]));
                        for (int k1=0; k1<anzVar; k1++) for (int k2=0; k2<anzVar; k2++) dist += (data[i][k1] - mean[j][k1])*covInv[j][k1][k2]*(data[i][k2] - mean[j][k2]);
                    }
                    if (dist < minDist) {minDist = dist; win = j;}
                }
                if (win != clust[i]) {clust[i] = win; goon = true;}
            }
        }

        lastClustering = clust;
        return clust;
    }
    
    public int[] repeatedClustering(int anzCluster, int[] initial, int trials) {
        int[] erg = null;
        double minEntropy = Double.MAX_VALUE;
        for (int i=0; i<trials; i++) {
            int[] zwerg = cluster(anzCluster, null);
            double entropy = getEntropy();
            if (entropy < minEntropy) {erg = zwerg; minEntropy = entropy;} 
        }
        cluster(anzCluster,erg);
        return erg;
    }
    
    public double getEntropy() {
        double entropy = -anzPer*Math.log(anzCluster);
        entropy += anzPer * (-2)*Math.log(Statik.SQRTTWOPI);
        
        double[] det = new double[anzCluster];
        double[][][] covInv = null; 
        if (method == methodType.NORMAL) {
            covInv = new double[anzCluster][][]; double[][] work = new double[anzVar][anzVar];            
            for (int i=0; i<anzCluster; i++) {
                covInv[i] = new double[anzVar][anzVar];
                det[i] = Statik.invert(cov[i], covInv[i], work);
            }
        }
        for (int i=0; i<anzPer; i++) {
            if (method == methodType.MEAN) for (int k=0; k<anzVar; k++) entropy += (data[i][k] - mean[lastClustering[i]][k])*(data[i][k] - mean[lastClustering[i]][k]);
            if (method == methodType.NORMAL) {
                entropy += Math.log(det[lastClustering[i]]);
                for (int k1=0; k1<anzVar; k1++) for (int k2=0; k2<anzVar; k2++) entropy += (data[i][k1] - mean[lastClustering[i]][k1])*covInv[lastClustering[i]][k1][k2]*(data[i][k2] - mean[lastClustering[i]][k2]);
            }
        }
        return entropy;
    }

    public void setMethod(methodType method) { this.method = method;}
    
    

}
