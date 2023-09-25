/*
 * Created on 04.05.2011
 */
package machineLearning.clustering;

import java.util.Random;

public abstract class ClusteringAlgorithm {
    
    double[][] data;
    int[] lastClustering;
    int anzVar;
    int anzPer;
    
    Random random = new Random();
    
    public ClusteringAlgorithm() {this.data = null; anzPer =0; anzVar = 0;}
    public ClusteringAlgorithm(double[][] data) {this.data = data; anzPer = data.length; anzVar = data[0].length;}
    
    public void setData(double[][] data) {this.data = data; anzPer = data.length; anzVar = data[0].length;}
    
    public abstract int[] cluster();    
}
