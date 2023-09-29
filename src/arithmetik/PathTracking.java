/*
 * Created on 25.09.2023
 */
package arithmetik;

import java.util.Collection;
import java.util.Vector;

import engine.Statik;

/**
 * Class to solve an AnalyticFunction by Pathtracking or to create a Table for some parameter values along a path. Has no handling of singularities
 * along the pathes.  
 * 
 * @author vonoertzen
 */
public class PathTracking {
    
    private static final int MINTRIESTOLOOKGOOD = 5, MAXTRIES = 15;
    private static final double EPS = 0.00001;
    private static final double CONVERGENCERATE = 1.5;
    private static final double FRACTIONTOGIVEUP = 0.00001;
    public static final double MAXSTEP = 0.1;
    public static final double[] DEFAULTANCHORS = new double[] {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9};

    /**
     * for an input function f, creates a new function that adds one new parameter. 
     * @param f
     * @return f+x_n
     */
    private static AnalyticalFunction addAdditiveParameter(AnalyticalFunction f)
    {
        final AnalyticalFunction ff = f;
        return new AnalyticalFunction(){
            private final int anzPar = ff.anzPar()+1;
            private double[] val = new double[anzPar];
            private void fillValues(double[] values) {for (int i=0; i<values.length; i++) val[i] = values[i];}
            public double eval(double[] values) {fillValues(values); return ff.eval(val)+values[anzPar-1];}
            public double evalDev(int pnr, double[] values) {if (pnr==anzPar-1) return 1; else {fillValues(values); return ff.evalDev(pnr, val);}}
            public double evalDev(int[] pnr, double[] values) 
            {
                for (int i=0; i<pnr.length; i++) if (pnr[i]==anzPar-1) return 0.0;
                fillValues(val);
                return ff.evalDev(pnr,values);
            }
            public int anzPar() {return anzPar;}
            @SuppressWarnings("unused")
            public AnalyticalFunction getParent() {return ff;};
        };
    }

    /**
     * Tracks a path from pathStart to pathEnd adapting the parameter at freeIx to let the function be target. Assumes f(pathstart, startFree) = target
     * is already correct. Will stop at the end of the path or if a singularity is reached. 
     * 
     * If anchors is not zero, the anchor points (given as path lambda from 0 to 1) will be among the solutions unless the path
     * ends before the last one is reached. 
     * 
     * @param f
     * @param freeIx
     * @param startFree
     * @param pathStart
     * @param pathEnd
     * @param target
     * @param anchors
     * @param trackedPositions
     */
    public static double[] trackPathWithGoodStart(AnalyticalFunction f, int freeIx, double startFree, double[] pathStart, double[] pathEnd, 
            double target, double[] anchors, double maxStep, Collection<double[]> trackedPositions)
    {
        double[] posOnPath = Statik.copy(pathStart);
        double posFree = startFree;
        int[] pathIx = new int[f.anzPar()-1]; for (int i=0; i<pathIx.length; i++) pathIx[i] = (i<freeIx?i:i+1);
        AnalyticalFunctionPartlyFixed g = f.fixParameters(pathIx, posOnPath);
        
        double lastSuccessfull = 0.0, lastSuccessfullFree = startFree, nextTarget = 0.0;
        boolean succeeded = true, hitOnNoReelSolution = false;
        double step = maxStep;
        while (lastSuccessfull < 1.0 && !hitOnNoReelSolution) {
            if (succeeded) {
                if (trackedPositions != null) {
                    g.setFreeValue(posFree);
                    trackedPositions.add(Statik.copy(g.getAllValues()));
                }
                step *= 2; if (step > maxStep) step = maxStep;
                lastSuccessfull = nextTarget; lastSuccessfullFree = posFree;
                nextTarget = lastSuccessfull + step;
                if (anchors != null) for (int i=0; i<anchors.length; i++) 
                    if (anchors[i] > lastSuccessfull && anchors[i] < nextTarget) {step = anchors[i]-lastSuccessfull; nextTarget = anchors[i];}
                if (nextTarget > 1.0) {step = 1.0 - lastSuccessfull; nextTarget = 1.0;}
            }
            for (int i=0; i<posOnPath.length; i++) posOnPath[i] = (1-nextTarget)*pathStart[i] + nextTarget * pathEnd[i];
            g.setFixedValues(posOnPath);
            double width = Double.MAX_VALUE;
            int tries = 0; boolean looksGood = false; succeeded = false;
            posFree = lastSuccessfullFree;
            while (!succeeded && (tries < MINTRIESTOLOOKGOOD || looksGood) && tries < MAXTRIES) {
                double newStep = g.newtonStep(posFree, target);
                posFree += newStep;
                newStep = Math.abs(newStep);
                tries++;
                if (tries > 1) {
                    double convergenceRate = Math.log(newStep) / Math.log(width);
                    looksGood = convergenceRate > CONVERGENCERATE;
                }
                width = newStep;
                succeeded = Math.abs(width) < EPS;
            }
//            System.out.println("Checked lambda = "+nextTarget+", "+(succeeded?"succeeded":"failed"));
            if (!succeeded) {
                step /= 2; if (step < FRACTIONTOGIVEUP) hitOnNoReelSolution = true;
                nextTarget = lastSuccessfull + step;
            }
        }
        g.setFreeValue(posFree);
        return g.getAllValues();
    }
    
    public static double solveWithTrackPath(AnalyticalFunction f, int freeIx, double startFree, double[] fixedValues, double target, double[] anchors, Vector<double[]> trackedPositions)
    {
        int[] fixIx = new int[f.anzPar()-1]; for (int i=0; i<fixIx.length; i++) fixIx[i] = (i<freeIx?i:i+1);
        AnalyticalFunction g = f.fixParameters(fixIx, fixedValues);
        double currentValue = g.eval(startFree);
        AnalyticalFunction h = addAdditiveParameter(g);
        double[] pathStart = new double[] {-currentValue}, pathEnd = new double[] {-target};
        double[] solution = trackPathWithGoodStart(h, 0, startFree, pathStart, pathEnd, 0.0, anchors, 1.0, trackedPositions);
        return solution[0];
    }
}
