/*
 * Created on 08.10.2016
 */
package machineLearning.clustering;

import java.util.Arrays;
import java.util.Comparator;

import engine.Statik;

public class Clustering {

    public final static int NA = -1;
    int[] clustering;               // -1 = not assigned
    int[][] cluster = null;         // can be null; will be created by updateCluster()
    
    public Clustering(int[] clustering) {
        this.clustering = clustering;
        updateCluster();
    }
    
    public Clustering(Clustering cl) {
        this.clustering = Statik.copy(cl.clustering);
        updateCluster();
    }
    
    /**
     * creates cluster array to access single cluster
     */
    private void updateCluster() {
        int max = -1; for (int i=0; i<clustering.length; i++) if (clustering[i]>max) max = clustering[i];
        cluster = new int[max+1][];
        for (int i=0; i<=max; i++) {
            int anz = 0; for (int j=0; j<clustering.length; j++) if (clustering[j] == i) anz++;
            cluster[i] = new int[anz]; int k=0; for (int j=0; j<clustering.length; j++) if (clustering[j] == i) cluster[i][k++] = j;
        }
    }
    
    /**
     * Renames all cluster such that they are sorted by size and by lowest element as tie breaker.
     * Empty clusters are removed. 
     */
    public void toCanonicalForm() {
        if (cluster == null) updateCluster();
        Arrays.sort(cluster, new Comparator<int[]>() {
            @Override
            public int compare(int[] arg0, int[] arg1) {
                if (arg0.length > arg1.length) return 1;
                if (arg0.length < arg1.length) return -1;
                int min=NA, erg = 0; 
                for (int i=0; i<arg0.length; i++) if (arg0[i]<min) {min=arg0[i]; erg = 1;}
                for (int i=0; i<arg1.length; i++) if (arg1[i]<min) return -1;
                return erg; 
            }
        });
        int anzEmpty = 0; for (int i=0; i<cluster.length; i++) if (cluster[i].length==0) anzEmpty++;
        if (anzEmpty > 0) {
            int[][] newCluster = new int[cluster.length - anzEmpty][];
            for (int i=0; i<newCluster.length; i++) newCluster[i] = cluster[i+anzEmpty];
            cluster = newCluster;
        }
        
        for (int i=0; i<cluster.length; i++) {
            Arrays.sort(cluster[i]);
            for (int j=0; j<cluster[i].length; j++) clustering[cluster[i][j]] = i;
        }
    }
    
    public int getAnzPer() {return clustering.length;}
    public int getAnzCluster() {if (cluster == null) updateCluster(); return cluster.length;}
    public int getAnzNonEmptyCluster() {int erg = 0; for (int i=0; i<cluster.length; i++) if (getAnzPerInCluster(i)>0) erg++; return erg;}
    public int getAnzPerInCluster(int i) {if (cluster == null) updateCluster(); return cluster[i].length; }
    public int[] getCluster(int cl) {if (cluster==null) updateCluster(); return cluster[cl];}
    public int getClusterOf(int per) {if (per >= clustering.length) return NA; else return clustering[per];}
    public boolean isEmpty() {return clustering.length==0;}
    public int[] getIntegerArray() {return clustering;    }
    
    public void removePer(int per) {assignPerToCluster(per, NA);}
    public void assignPerToCluster(int per, int cl) {
        if (per >= getAnzPer()) {
            int[] nClustering = new int[per+1]; for (int i=0; i<per+1; i++) nClustering[i] = -1;
            for (int i=0; i<clustering.length; i++) nClustering[i] = clustering[i]; 
            clustering = nClustering;
        }
        clustering[per] = cl;
        if (cluster != null) updateCluster();
    }
    
    @Override
    public boolean equals(Object cluster2) {
        if (!(cluster2 instanceof Clustering)) return false;
        Clustering cl2 = (Clustering)cluster2;
        for (int i=0; i<Math.max(cluster.length, cl2.cluster.length); i++) if (getClusterOf(i) != cl2.getClusterOf(i)) return false;
        return true;
    }
    
    public boolean isEquivalent(Clustering cluster2) {
        this.toCanonicalForm(); cluster2.toCanonicalForm(); 
        return this.equals(cluster2);
    }
    
    /**
     * Counts all elements in cluster 1 that are not in cluster 2 and the other way round, ignoring all elements for which the ignore flag is true. 
     * 
     * Assumes both groups to be sorted in increasing order. 
     * 
     * @param group1
     * @param group2
     * @param ignore    all elements marked as ignore will not be counted for the costs. 
     * @return 
     */
    private int difference(int[] group1, int[] group2, boolean[] ignore) {
        int costs = 0, z1=0, z2=0;
        while (z1<group1.length && z2<group2.length) {
            if (group1[z1]==group2[z2]) {z1++; z2++;}
            else if (group1[z1] < group2[z2]) {if (!ignore[group1[z1]]) costs++; z1++;}
            else if (group1[z1] > group2[z2]) {if (!ignore[group2[z2]]) costs++; z2++;}
        }
        if (z1<group1.length) costs += group1.length - z1;
        if (z2<group2.length) costs += group2.length - z2;
        return costs;
    }

    /**
     * computes the minimal number of transfers to change clustering 1 to clustering 2. Re-ordering of clusters are free. 
     * TODO: Union or split of clusters could be declared cheaper than by transferring all persons
     *  
     *  
     * @param cluster2
     * @return
     */
    @Deprecated
    public int distanceToOldVersion(Clustering cluster2) {
        this.toCanonicalForm(); cluster2.toCanonicalForm();
        
        int totalDistance = 0;
        int[] takeout1 = new int[cluster.length], takeout2 = new int[cluster2.cluster.length];
        int anzPer = Math.max(getAnzPer(), cluster2.getAnzPer());
        boolean[] finished = new boolean[anzPer]; for (int i=0; i<anzPer; i++) finished[i] = false;
        for (int i=0; i<takeout1.length; i++) takeout1[i] = i;
        for (int i=0; i<takeout2.length; i++) takeout2[i] = i; 
        int toMatch = Math.min(takeout1.length, takeout2.length);
        while (toMatch > 0) {
            int c1 = NA, c2 = NA, minCost = Integer.MAX_VALUE;
            for (int i=0; i<takeout1.length; i++) for (int j=0; j<takeout2.length; j++) 
                if (takeout1[i] != NA && takeout2[j] != NA) {
                    int costs= difference(cluster[takeout1[i]], cluster2.cluster[takeout2[j]], finished);
                    if (costs < minCost) {minCost = costs; c1=i; c2=j;}
                }
            int z1=0, z2=0;
            while (z1<cluster[c1].length && z2<cluster2.cluster[c2].length) {
                if (cluster[c1][z1]==cluster2.cluster[c2][z2]) {finished[cluster[c1][z1]]=true; finished[cluster2.cluster[c2][z2]]=true; z1++; z2++;}
                else if (cluster[c1][z1] < cluster2.cluster[c2][z2]) {if (!finished[cluster[c1][z1]]) {finished[cluster[c1][z1]]=true; totalDistance++;} z1++;}
                else if (cluster[c1][z1] > cluster2.cluster[c2][z2]) {if (!finished[cluster2.cluster[c2][z2]]) {finished[cluster2.cluster[c2][z2]]=true; totalDistance++;} z2++;}
            }
            for (int i=z1; i<cluster[c1].length; i++) if (!finished[cluster[c1][i]]) {finished[cluster[c1][i]]=true; totalDistance++;}
            for (int i=z2; i<cluster2.cluster[c2].length; i++) if (!finished[cluster2.cluster[c2][i]]) {finished[cluster2.cluster[c2][i]]=true; totalDistance++;}
            takeout1[c1] = NA; takeout2[c2] = NA;
            toMatch--;
        }
        for (int i=0; i<finished.length; i++) if (!finished[i]) totalDistance++;
        return totalDistance;
    }
    
    /**
     * 
     * computes the minimal number of transfers to change clustering 1 to clustering 2. Re-ordering of clusters are free.
     * Uses a reduction to the job assignment problem, then calls an implementation of the Hungarian Algorithm.  
     * TODO: Union or split of clusters could be declared cheaper than by transferring all persons
     * 
     * @param cluster2
     * @return
     */
    public int distanceTo(Clustering cluster2) {
        Clustering c1 = (this.getAnzCluster() > cluster2.getAnzCluster()?cluster2:this);
        Clustering c2 = (this.getAnzCluster() > cluster2.getAnzCluster()?this:cluster2);
        
        double[][] costMatrix = new double[c1.getAnzCluster()][c2.getAnzCluster()];

        for(int i=0;i<c1.getAnzCluster();i++)
            for(int j=0;j<c2.getAnzCluster();j++)
                for(int x=0;x<c1.clustering.length;x++)
                    costMatrix[i][j] += ((c1.clustering[x] == i) && (c2.clustering[x] != j) ? 1 : 0); 
                        
        int jobIndexForWorker[] = (new HungarianAlgorithm(costMatrix)).execute();
        
        int overallCosts = 0;
        for(int worker=0;worker < jobIndexForWorker.length;worker++)
            overallCosts += costMatrix[worker][jobIndexForWorker[worker]];
        
        return overallCosts;
        
    }
    
    @Override
    public int hashCode() {
        toCanonicalForm();
        int erg = cluster.length; 
        for (int i=0; i<cluster.length; i++) erg += cluster[i].length*Math.pow(2,i);
        for (int i=0; i<clustering.length; i++) erg += clustering[i]*Math.pow(2,i);
        return erg;
    }
    
    @Override
    public String toString() {
        String erg = "";
        updateCluster();
        for (int i=0; i<cluster.length; i++) {
            erg += "{";
            for (int j=0; j<cluster[i].length; j++) erg += cluster[i][j]+(j==cluster[i].length-1?"":",");
            erg += "}"+(i==cluster.length-1?"":", ");
        }
        return erg;
    }

    /**
     * computes the entropy of the distribution of the cluster memberships. 
     * @return
     */
    public double getEntropy() {
        if (cluster==null) updateCluster();
        int anzPer = getAnzPer();
        double erg = 0, logAnzPer = Math.log(anzPer);
        for (int i=0; i<cluster.length; i++)
            erg -= (cluster[i].length==0?0:cluster[i].length*(Math.log(cluster[i].length)-logAnzPer)/(double)anzPer);
        return erg;
    }
}