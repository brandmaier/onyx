/*
 * Created on 04.05.2011
 */
package machineLearning.clustering;

import engine.Statik;
import machineLearning.clustering.LloydsAlgorithm.methodType;

public class AgglomerativeClustering extends ClusteringAlgorithm {

    public static enum agglomerativeDistanceType {MEAN, MAX, MIN};
    public agglomerativeDistanceType aggDist = agglomerativeDistanceType.MEAN;
    public DistanceFunction distance;
    public double[][] distanceTable;
    public HierarchicalCluster[] cluster;
    public HierarchicalCluster tree;
    
    public AgglomerativeClustering(double[][] data) {super(data);}
    public AgglomerativeClustering(double[][] data, DistanceFunction distance, agglomerativeDistanceType aggDist) 
        {super(data); this.distance = distance; this.aggDist = aggDist;}
    public AgglomerativeClustering(double[][] distanceTable, boolean isDistance) {this.data = null; anzPer = distanceTable.length; anzVar = -1; this.distanceTable = distanceTable;}
    
    @Override
    public int[] cluster() {
        if (data != null && distance != null) {
            distanceTable = Statik.ensureSize(distanceTable, anzPer, anzPer);
            for (int i=0; i<anzPer; i++) 
                for (int j=0; j<anzPer; j++) {
                    distanceTable[i][j] = distance.distance(data[i], data[j]);
                }
        }
//        System.out.println(Statik.matrixToLatexString(distanceTable));
        cluster = new HierarchicalCluster[anzPer];
        for (int i=0; i<anzPer; i++) cluster[i] = new HierarchicalCluster(new int[]{i}, null);
        int len = anzPer;
        while (len > 1) {
            double min = Double.MAX_VALUE; int min1 = -1, min2 = -1;
            for (int i=0; i<len; i++) for (int j=i+1; j<len; j++) if (distanceTable[i][j] < min) {min = distanceTable[i][j]; min1 = i; min2 = j;}
            System.out.println("Clustering "+cluster[min1].toShortString()+" and "+cluster[min2].toShortString());
            
            cluster[min1] = new HierarchicalCluster(Statik.append(cluster[min1].member, cluster[min2].member), new HierarchicalCluster[]{cluster[min1],cluster[min2]});
            cluster[min1].distance = min;
            for (int i=min2; i<len-1; i++) cluster[i] = cluster[i+1];
            
            for (int i=0; i<len; i++) {
                double newdist = 0;
                if (aggDist == agglomerativeDistanceType.MAX) newdist = Math.max(distanceTable[min1][i],distanceTable[min2][i]);
                if (aggDist == agglomerativeDistanceType.MIN) newdist = Math.min(distanceTable[min1][i],distanceTable[min2][i]);
                if (aggDist == agglomerativeDistanceType.MEAN) newdist = (cluster[min1].member.length*distanceTable[min1][i]+cluster[min2].member.length*distanceTable[min2][i])/
                        (cluster[min1].member.length+cluster[min2].member.length);
                distanceTable[min1][i] = distanceTable[i][min1] = newdist;
            }
            for (int i=min2; i<len-1; i++) for (int j=0; j<len; j++) distanceTable[i][j] = distanceTable[i+1][j];
            for (int i=min2; i<len-1; i++) for (int j=0; j<len; j++) distanceTable[j][i] = distanceTable[j][i+1];
            len--;
        }
        tree = cluster[0];
        
        lastClustering = new int[anzPer];
        for (int i=0; i<tree.children[0].children[0].member.length; i++) lastClustering[tree.children[0].children[0].member[i]] = 0;
        for (int i=0; i<tree.children[0].children[1].member.length; i++) lastClustering[tree.children[0].children[1].member[i]] = 1;
        for (int i=0; i<tree.children[1].children[0].member.length; i++) lastClustering[tree.children[1].children[0].member[i]] = 2;
        for (int i=0; i<tree.children[1].children[1].member.length; i++) lastClustering[tree.children[1].children[1].member[i]] = 3;
        
        return lastClustering;
    }

}
