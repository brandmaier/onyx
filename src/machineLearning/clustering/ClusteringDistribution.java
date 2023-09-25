/*
 * Created on 23.10.2016
 */
package machineLearning.clustering;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;
import java.util.Vector;

public class ClusteringDistribution {
    
    private Hashtable<Clustering, int[]> sample;
    private int anzSamples;
    
    public ClusteringDistribution() {
        sample = new Hashtable<Clustering, int[]>();
        anzSamples = 0;
    }
    
    public int getAnzSamples() {return anzSamples;}
    public int getFrequencyOf(Clustering cl) {return sample.get(cl)[0];}
    public double getProbabilityOf(Clustering cl) {return getFrequencyOf(cl)/(double)anzSamples;}
    public int getAnzPersons() {
        if (sample.isEmpty()) return -1;
        int erg = Integer.MIN_VALUE;
        Enumeration<Clustering> key = sample.keys();
        while (key.hasMoreElements()) {int x = key.nextElement().getAnzPer(); if (x>erg) erg=x;}
        return erg;
    }
    
    public void addSample(Clustering cl) {addSample(cl, 1);}
    public void addSample(Clustering cl, int anz) {
        int[] entry = sample.get(cl);
        if (entry==null) {
            entry = new int[]{0};
            sample.put(new Clustering(cl),entry);
        }
        entry[0] += anz;
        anzSamples++;
    }

    
    
    /**
     * converts the distribution to an array of tuples, first the cluster, second its frequency. Sorted by frequency (more frequent first), with the 
     * cluster entropy as tie breaker. 
     * 
     * @return
     */
    public Object[][] asArray() {
        Set<Clustering> keySet = sample.keySet();
        Object[][] erg = new Object[keySet.size()][2];
        int i=0; for (Clustering cl:keySet) erg[i++] = new Object[]{cl,sample.get(cl)};

        Arrays.sort(erg, new Comparator<Object[]>() {
            @Override
            public int compare(Object[] arg0, Object[] arg1) {
                if (((int[])arg0[1])[0] > ((int[])arg1[1])[0]) return -1; 
                if (((int[])arg0[1])[0] < ((int[])arg1[1])[0]) return 1;
                if (((Clustering)arg0[0]).getEntropy() < ((Clustering)arg1[0]).getEntropy()) return -1;
                if (((Clustering)arg0[0]).getEntropy() > ((Clustering)arg1[0]).getEntropy()) return 1;
                return 0; 
            }
        });
        return erg;
    }
    
    public int[][] distanceMatrix() {
        Object[][] arr = asArray();
        int[][] erg = new int[arr.length][arr.length];
        for (int i=0; i<erg.length; i++) 
            for (int j=i+1; j<erg.length; j++) erg[i][j] = erg[j][i] = ((Clustering)arr[i][0]).distanceTo((Clustering)arr[j][0]);
        return erg;
    }
    
    /**
     * Returns a new distribution over the clusterings in which frequent clusterings consume small clusterings with distance tolerance or smaller to it,
     * greedily as sorted by samples and entropy as tie breaker.  
     * 
     * @param tolerance
     * @return
     */
    public ClusteringDistribution modes(int tolerance) {
        Object[][] arr = asArray();
        int[][] distance = distanceMatrix();
        boolean[] taken = new boolean[arr.length]; for (int i=0; i<taken.length; i++) taken[i] = false;
        ClusteringDistribution erg = new ClusteringDistribution();
        int anzTaken = 0;
        while (anzTaken < taken.length) {
            int p = 0; while (taken[p]) p++;
            taken[p] = true; anzTaken++;
            for (int q=0; q<distance.length; q++) if (!taken[q] && distance[p][q] <= tolerance) {
                taken[q] = true; anzTaken++;
                ((int[])arr[p][1])[0] += ((int[])arr[q][1])[0];
            }
            erg.addSample((Clustering)arr[p][0], ((int[])arr[p][1])[0]);
        }
        return erg;
    }

    public Clustering getMode() {
        return (Clustering)asArray()[0][0];
    }
    
    /**
     * selects among all clusters in the distribution the one which has minimal squared distance to all other cluster.
     * 
     * @return
     */
    public Clustering getClusterWithMinimalDistance() {
        int minDistance = Integer.MAX_VALUE;
        Clustering erg = null;
        Set<Clustering> keys = sample.keySet();
        for (Clustering candidate:keys) {
            int dist = 0; 
            for (Clustering sam:keys)
                dist += sample.get(sam)[0] * Math.pow(sam.distanceTo(candidate),2);
            if (dist < minDistance) {erg = candidate; minDistance = dist;}
        }
        return erg;
    }
    
    /**
     * Approximates a solution to the problem of specifying one clustering such that the squared distance to all clusterings in the distribution is 
     * minimal. Algorithm is taken from the explanation of TG, no check whether this is optimal.
     *  
     * @return
     */
    public Clustering getMean() {
        int anzPer = this.getAnzPersons();
        Clustering erg = new Clustering(getClusterWithMinimalDistance());
        erg.toCanonicalForm();
        erg.cluster = null;     // fix naming. 
        boolean goon = true;
        Set<Clustering> keys = this.sample.keySet();
        while (goon) {
            goon = false;
            for (int per = 0; per < anzPer; per++) {
                int minDistance = 0; for (Clustering sam:keys)
                    minDistance += sample.get(sam)[0] * Math.pow(sam.distanceTo(erg),2);
                int oldValue = erg.clustering[per], anzCluster = erg.getAnzCluster(), winner = oldValue;
                for (int i=0; i<anzCluster+1; i++) if (i != oldValue) {
                    erg.assignPerToCluster(per, i);
                    int dist = 0; for (Clustering sam:keys)
                        dist += sample.get(sam)[0] * Math.pow(sam.distanceTo(erg),2);
                    if (dist < minDistance) {winner = i; minDistance = dist;}
                }
                System.out.println("Distance = "+minDistance+", goon = "+goon+", per = "+per);
                erg.assignPerToCluster(per, winner);
                if (winner != oldValue) goon = true;
            }
        }
        erg.toCanonicalForm();
        return erg;
    }
    
    public String toString() {return toString(Integer.MAX_VALUE);}
    public String toString(int maxlines) {
        Object[][] arr = asArray();
        String erg = "";
        for (int i=0; i<arr.length && i < maxlines; i++)
            erg += ((int[])arr[i][1])[0]+" x {"+((Clustering)arr[i][0])+"}\r\n";
        
        return erg;
    }
    
    public int[][] toArray() {
        Object[][] arr = asArray();
        int per = getAnzPersons();
        int[][] erg = new int[per][arr.length];
        for (int i=0; i<arr.length; i++) {
            Clustering cl = ((Clustering)arr[i][0]);
            for (int j=0; j<per; j++) erg[j][i] = cl.getClusterOf(j); 
        }
        return erg;
    }
    
    public int[] getFrequencies() {
        Object[][] arr = asArray();
        int anz = arr.length;
        int[] erg = new int[anz];
        for (int i=0; i<anz; i++) erg[i] = ((int[])arr[i][1])[0];
        return erg;
    }
}
