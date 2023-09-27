/*
 * Created on 26.09.2023
 */
package arithmetik;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

public class AnalyticalPathBalanceFunction extends AnalyticalFunction {

    public double EPS = 0.001;
    
    private AnalyticalFunction f;
    public double target;

    private TreeSet<double[]> anchors;
    
    public boolean isInitialized = false;
    
    private Comparator<double[]> comp = new Comparator<double[]>() {

        @Override
        public int compare(double[] o1, double[] o2) {
            if (o1[0] > o2[0]) return 1;
            if (o1[0] < o2[0]) return -1;
            return 0;
        }
    };
    
    public AnalyticalPathBalanceFunction(AnalyticalFunction g, int freeIx, double startFree, int controlIx, double startCtrl, double[] otherValues, double target) {
        int[] fixedIx = new int[g.anzPar()-2];
        int j=0; for (int i=0; i<fixedIx.length; i++) {while (i+j==freeIx || i+j==controlIx) j++; fixedIx[i] = j+i;}
        AnalyticalFunction h = g.fixParameters(fixedIx, otherValues);
        if (freeIx > controlIx) f = h.exchangeOrderOfParameter(new int[] {1,0}); 
        else f = h;
        initialize(startCtrl, startFree);
    }

    public boolean initialize(double val, double starting) {
        double y = PathTracking.solveWithTrackPath(f, 1, starting, new double[] {val}, target, null, null);
        double[] p = new double[] {val,y};
        double check = f.eval(p);
        if (Math.abs(check-target)<EPS) {
            anchors = new TreeSet<double[]>(comp);
            anchors.add(new double[] {val,y});
            isInitialized = true;
            return true;
        } else {isInitialized = false; return false;}
    }
    
    @Override
    public double eval(double[] val) {
        return eval(val[0]);
    }
    
    @Override
    public double eval(double val) {
        if (!isInitialized) {
            initialize(val, 0.0);
            if (!isInitialized) return Double.NaN;
        } 
    
        double[] cmp = new double[] {val,0};
        double[] d1 = anchors.floor(cmp);
        double[] d2 = anchors.ceiling(cmp);
        double[] start;
        
        if (d1 != null && (d2==null || Math.abs(val-d1[0])<Math.abs(val-d2[0]))) start = d1; else start = d2;
        double[] end = PathTracking.trackPathWithGoodStart(f, 1, start[0], new double[] {start[1]}, new double[] {val}, target, null, 0.1, anchors);
        if (end[0] == val) return end[1]; else return Double.NaN;
    }

    @Override
    public int anzPar() {return 1;}

}
