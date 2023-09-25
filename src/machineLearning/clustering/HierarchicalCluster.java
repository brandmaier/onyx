/*
 * Created on 04.05.2011
 */
package machineLearning.clustering;

import engine.Statik;

public class HierarchicalCluster  {

    public HierarchicalCluster[] children;
    public int[] member;
    public double distance;
    
    public HierarchicalCluster(int[] member, HierarchicalCluster[] children) {
        this.member = member; this.children = children;
    }
    
    public String toString() {
        return toString(0);
    }
    public String toString(int depth) {
        String erg = ""; for (int i=0; i<depth; i++) erg += "  ";
        erg += "Cluster ("+Statik.matrixToString(member)+")\r\n";
        if (children!=null) for (int i=0; i<children.length; i++) erg += children[i].toString(depth+1);
        return erg;
    }

    public String toShortString() {
        return "("+Statik.matrixToString(member)+")";
    }
}
