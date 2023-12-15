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
 * Created on 13.11.2010
 */
package machineLearning.preprocessor;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import engine.Statik;

public class DataPreprocessing {

    public static double[][] whiteningTransformationMatrix(double[][] cov, double minimalVariance) {
        int dim = cov.length;
        
        double[][] q = Statik.identityMatrix(dim); double[] ev = new double[dim]; 
        Statik.eigenvalues(cov, 0.000001, ev, q);
        
        int dim2 = 0; for (int i=0; i<dim; i++) if (Math.abs(ev[i]) >= minimalVariance) dim2++;
        if (dim2==0) return new double[0][];
        
        double[] work = new double[dim];
        Statik.copy(ev, work);
        Arrays.sort(ev); int[] ixList = new int[dim], ixListInv = new int[dim];
        for (int i=0; i<dim2; i++) ixList[i] = dim - 1 - Arrays.binarySearch(ev, work[i]);
        for (int i=0; i<dim2; i++) ixListInv[ixList[i]] = i;

        double[][] erg = new double[dim][dim2];
        for (int i=0; i<dim2; i++) for (int j=0; j<dim; j++) erg[j][ixList[i]] = q[j][i] / Math.sqrt(ev[dim - 1 - i]);
        
        return erg;   
    }
        
    public static double[][] pcaTransformationMatrix(double[][] cov, double minimalVariance) {
        int dim = cov.length;
        
        double[][] q = Statik.identityMatrix(dim); double[] ev = new double[dim]; 
        Statik.eigenvalues(cov, 0.000001, ev, q);
        
        int dim2 = 0; for (int i=0; i<dim; i++) if (Math.abs(ev[i]) >= minimalVariance) dim2++;
        if (dim2==0) return new double[0][];
        
        double[] work = new double[dim];
        Statik.copy(ev, work);
        Arrays.sort(ev); int[] ixList = new int[dim], ixListInv = new int[dim];
        for (int i=0; i<dim2; i++) ixList[i] = dim - 1 - Arrays.binarySearch(ev, work[i]);
        for (int i=0; i<dim2; i++) ixListInv[ixList[i]] = i;

        double[][] erg = new double[dim][dim2];
        for (int i=0; i<dim2; i++) for (int j=0; j<dim; j++) erg[j][ixList[i]] = q[j][i];
        
        return erg;   
    }
    
    /**
     * Computes a CSP based on the covariance matrices of the two classes and returns the transformation matrix. If minimalVariance is above zero, a pca is 
     * applied first and all vectors with eigenvalues below minimalVariance are eliminated. 
     *  
     * @param covGroup1
     * @param covGroup2
     * @param minimalVariance
     * @return
     */
    public static double[][] cspTransformationMatrix(double[][] covGroup1, double[][] covGroup2, double minimalVariance) {
        int dim = covGroup1.length;
        
        double[][] covTotal = Statik.add(covGroup1, covGroup2);

        System.out.println("Total covariance matrix = \r\n"+Statik.matrixToString(covTotal));
        System.out.println("Total covariance matrix [Maple] = \r\n"+Statik.matrixToMapleString(covTotal));
        
        double[][] q = Statik.identityMatrix(dim); double[] ev = new double[dim]; 
        Statik.eigenvalues(covTotal, 0.0001, ev, q);
        
        System.out.println("Eigenvalues        = "+Statik.matrixToString(ev));
        System.out.println("Eigenvector matrix = \r\n"+Statik.matrixToString(q));
        
        int dim2 = 0; for (int i=0; i<dim; i++) if (Math.abs(ev[i]) >= minimalVariance) dim2++;
        if (dim2==0) return new double[0][];
        double[][] subQ = new double[dim][dim2];
        int p = 0;
        for (int i=0; i<dim; i++) if (Math.abs(ev[i]) >= minimalVariance) {for (int j=0; j<dim; j++) subQ[j][p] = q[j][i] / Math.sqrt(ev[i]); p++;} 
        double[][] transCov1 = new double[dim2][dim2];
        Statik.multiply(Statik.multiply(Statik.transpose(subQ),covGroup1),subQ, transCov1);
        double[][] q2 = Statik.identityMatrix(dim2); double[] ev2 = new double[dim2];
        Statik.eigenvalues(transCov1, 0.001, ev2, q2);
        
        System.out.println("Submapping             = \r\n "+Statik.matrixToString(subQ));
        System.out.println("Eigenvalues of group 1 = "+Statik.matrixToString(ev2));
        System.out.println("Eigenvector group 1    = \r\n"+Statik.matrixToString(q2));

        double[] work = new double[dim2];
        Statik.copy(ev2, work);
        Arrays.sort(ev2); int[] ixList = new int[dim2], ixListInv = new int[dim2];
        for (int i=0; i<dim2; i++) ixList[i] = dim2 - 1 - Arrays.binarySearch(ev2, work[i]);
        for (int i=0; i<dim2; i++) ixListInv[ixList[i]] = i;

        double[] t;
        double[][] q2T = Statik.transpose(q2);
        boolean[] treated =new boolean[dim2]; for (int i=0; i<dim2; i++) treated[i] = (ixListInv[i]==i);
        for (int i=0; i<dim2; i++) if (!treated[i]) {
            int o=i; t = q2T[o]; int j=ixListInv[i];  
            while (j!=i) {q2T[o] = q2T[j]; o=j; j= ixListInv[j]; treated[o] = true;}
            q2T[o] = t; treated[i] = true;
        }
        double[][] erg = Statik.multiply(q2T,Statik.transpose(subQ));

        System.out.println("Final mapping        = \r\n "+Statik.matrixToString(erg));
        
        return erg;
    }
    
    public static void speedTestJulian() {
        String filename = "CSV_2ndTry";
        System.out.println("Proband ID "+filename);
        File file = new File(filename);
        File[] files = file.listFiles();
        double[][] label = Statik.loadMatrix(new File(filename+"\\y.csv"), ',');
        Vector<double[]> data1Vec = new Vector<double[]>();
        Vector<double[]> data2Vec = new Vector<double[]>();
        int anzChannel = -1;
        for (File f:files) {
            String fn = f.getName();
            if (fn.startsWith("x_")) {
                int trial = Integer.parseInt(fn.substring(2,fn.indexOf(".")))-1;
                int lab = (int)Math.round(label[0][trial]);
                double[][] sub = Statik.loadMatrix(f, ',');
                anzChannel = sub[0].length;
                for (double[] d:sub) (lab==0?data1Vec:data2Vec).add(d);
            }
        }
        double[][] data1 = new double[data1Vec.size()][]; for (int i=0; i<data1.length; i++) data1[i] = data1Vec.elementAt(i);
        double[][] data2 = new double[data2Vec.size()][]; for (int i=0; i<data2.length; i++) data2[i] = data2Vec.elementAt(i);
        
        double[][] cov1 = Statik.covarianceMatrix(data1);
        double[][] cov2 = Statik.covarianceMatrix(data2);
        System.out.println("Starting... ");
        int trials = 1;
        long time = System.nanoTime();
        for (int i=0; i<trials; i++) {
            double[][] trans = DataPreprocessing.cspTransformationMatrix(cov1, cov2, 100);
        }
        time = (System.nanoTime() - time)/(1000000*trials);
        
        System.out.println("Took me "+time+" ms. per trial.");
    }
    
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        speedTestJulian();
        
//        double[][] cov1 = new double[][]{{1.01,0.8,0.8},{0.8,1.0,0.9},{0.8,0.9,1.0}}, cov2 = new double[][]{{1.01,-0.8,-0.8},{-0.8,1.02,0.95},{-0.8,0.95,1.03}};
        
        double[][] cov1 = Statik.loadMatrix("linksCov.txt", '\t');
        double[][] cov2 = Statik.loadMatrix("rechtsCov.txt", '\t');
        
        double[][] trans = DataPreprocessing.cspTransformationMatrix(cov1, cov2, 0.0005);
        System.out.println("Transformation = \r\n"+Statik.matrixToString(trans));
        double[][] cov1Trans = Statik.multiply(trans, Statik.multiply(cov1, Statik.transpose(trans)));
        double[][] cov2Trans = Statik.multiply(trans, Statik.multiply(cov2, Statik.transpose(trans)));
        System.out.println("cov 1 transformed = \r\n"+Statik.matrixToString(cov1Trans));
        System.out.println("cov 2 transformed = \r\n"+Statik.matrixToString(cov2Trans));
        
        double[][] cov = Statik.add(cov1,cov2);
        double[][] transPca = DataPreprocessing.pcaTransformationMatrix(cov, 0.0005);
        double[][] covTrans = Statik.multiply(Statik.transpose(transPca), Statik.multiply(cov, transPca));
        System.out.println("Erg = \r\n"+Statik.matrixToString(covTrans));

        double[][] transWhite = DataPreprocessing.whiteningTransformationMatrix(cov, 0.0005);
        double[][] covWhite = Statik.multiply(Statik.transpose(transWhite), Statik.multiply(cov, transWhite));
        System.out.println("Erg = \r\n"+Statik.matrixToString(covWhite));
    }

    /**
     * Returns a multi-Dimensional Scaling result for the data in as many coordinates as participants are given. If screeValue is not null, it is 
     * filled with the eigenvalues to allow for a scree plot. The result has every participant in one row. Coordinates are sorted such that the projection
     * on the first k columns gives the best MDS for k dimensions. 
     * 
     * @param distance
     * @param screeValue
     * @param dim 
     * @param precision
     * @return
     */
    public static double[][] multiDimensionalScaling(double[][] distance) {return multiDimensionalScaling(distance, null, distance.length, 0.001);}
    public static double[][] multiDimensionalScaling(double[][] distance, double[] screeValue) {return multiDimensionalScaling(distance, screeValue, screeValue.length, 0.001);}
    public static double[][] multiDimensionalScaling(double[][] distance, double[] screeValue, int dim) {return multiDimensionalScaling(distance, screeValue, dim, 0.001);}
    public static double[][] multiDimensionalScaling(double[][] distance, double[] screeValue, int dim, double precision) {
        return multiDimensionalScaling(distance, screeValue, null, null, dim, precision);}
    public static double[][] multiDimensionalScaling(double[][] distance, double[] screeValue, double[][] Q, double[] rowMeans, int dim, double precision) {
        int anzPer = distance.length;
        double[] scree = screeValue;
        if (screeValue == null || scree.length != anzPer) scree = new double[anzPer];
        double[][] A = new double[anzPer][anzPer];
        for (int i=0; i<anzPer; i++) for (int j=0; j<anzPer; j++) A[i][j] = -0.5*distance[i][j]*distance[i][j];
        if (rowMeans == null) rowMeans = new double[anzPer]; 
        double fullMean = 0;
        for (int i=0; i<anzPer; i++) for (int j=0; j<anzPer; j++) {rowMeans[i] += A[i][j]; fullMean += A[i][j];}
        fullMean /= (anzPer*anzPer);
        for (int i=0; i<anzPer; i++) rowMeans[i] /= anzPer;
        for (int i=0; i<anzPer; i++) for (int j=0; j<anzPer; j++) A[i][j] += fullMean - rowMeans[i] - rowMeans[j];
        double[][] ev = (Q==null?new double[anzPer][anzPer]:Q);
        Statik.setToZero(ev); Statik.identityMatrix(ev);
        
        Statik.eigenvalues(A, precision, scree, ev);
        
        double[][] diag = Statik.multiply(Statik.transpose(ev), Statik.multiply(A, ev)); 
        
        double[] screeAbs = new double[anzPer]; for (int i=0; i<anzPer; i++) screeAbs[i] = Math.abs(scree[i]); 
        Statik.transpose(ev,ev);
        Statik.sortMatrixRowsByVector(screeAbs, ev, false);
        for (int i=0; i<anzPer; i++) for (int j=0; j<anzPer; j++) ev[i][j] *= Math.sqrt(screeAbs[i]);
        Statik.transpose(ev,ev);

        if (screeValue != null) for (int i=0; i<Math.min(anzPer, screeValue.length); i++) screeValue[i] = scree[i];
        double[][] erg = ev;
        if (anzPer != dim) {
            erg = new double[anzPer][dim];
            for (int i=0; i<anzPer; i++) for (int j=0; j<dim; j++) erg[i][j] = ev[i][j];
        }
        
        // DEBUG 
        double[][] QQT = Statik.multiply(ev, Statik.transpose(ev));
        double[] test = Statik.multiply(Statik.transpose(ev), A[0]);
        
        return erg;
    }
    
//    Q A Q^T = D     Q A = Q D 
    
    /**
     * Takes an existing multidimensional scaling of n points by the non-strict orthogonal transformation matrix  
     * and the rowMeans as well as a new vector of distances, and fits in a new point with these distances assuming the old
     * points are not to be moved. 
     * 
     * @param Q non-strict orthogonal transformation matrix Q such that QQ^T = A, where A is negative half squared distances plus full means minus row means
     * @param rowMeans  means of the rows of A
     * @param distances distances of the new point to the existing points. 
     * @return A fit of the new point with all n coordinates to the existing points. 
     */
    public static double[] fitPointIntoMultidimensionalScaling(double[][] Q, double[] rowMeans, double[] distances) {
        int anzPer = distances.length;
        double[] v = new double[anzPer];
        double fullMean = 0; for (int i=0; i<anzPer; i++) fullMean += rowMeans[i];
        fullMean /= anzPer;
        double vMean = 0;
        for (int i=0; i<anzPer; i++) {v[i] = -0.5*distances[i]*distances[i]; vMean += v[i];}
        vMean /= anzPer;
        for (int i=0; i<anzPer; i++) v[i] += fullMean - rowMeans[i] - vMean;
        return Statik.multiply(Q,  v);
    }
}
