/*
 * Created on 10.10.2014
 */
package machineLearning.preprocessor;

import java.util.Arrays;

import engine.Statik;

public class PCA extends Preprocessor {

    public enum CutoffCriterion {BYMINIMALVARIANCE,BYTOTALVARIANCE,BYNUMBER};
    private CutoffCriterion cutoffCriterion;
    private double[][] transformation;
    private double minimalVarianceRatio;
    private double totalVarianceRatio;
    private int anzComponents;
    private boolean useCorrelation;
    
    public PCA(double[][] data, CutoffCriterion cutoffCriterion, double cutoffValue, boolean useCorrelation) {
        super(data, new int[data.length]);
        this.cutoffCriterion = cutoffCriterion;
        this.useCorrelation = useCorrelation;
        if (cutoffCriterion == CutoffCriterion.BYMINIMALVARIANCE) minimalVarianceRatio = cutoffValue;
        if (cutoffCriterion == CutoffCriterion.BYTOTALVARIANCE) totalVarianceRatio = cutoffValue;
        if (cutoffCriterion == CutoffCriterion.BYNUMBER) anzComponents = (int)Math.round(cutoffValue);
    }
    
    @Override
    public void train(int start, int end) {
        int[] subsample = null; if (start!=-1 && start < end) subsample = Statik.enumeratIntegersFrom(start, end);
        double[][] cov = Statik.covarianceMatrix(data, subsample);
        int dim = cov.length;
        if (useCorrelation) cov = Statik.correlationFromCovariance(cov);
        
        double[][] q = Statik.identityMatrix(dim); double[] ev = new double[dim]; 
        Statik.eigenvalues(cov, 0.000001, ev, q);
        
        double trace = Statik.trace(cov);
        int dim2 = Math.min(dim,anzComponents);
        if (cutoffCriterion == CutoffCriterion.BYMINIMALVARIANCE) { 
            dim2 = 0; for (int i=0; i<dim; i++) if (Math.abs(ev[i]) >= minimalVarianceRatio*trace) dim2++;
        } 
        if (cutoffCriterion == CutoffCriterion.BYTOTALVARIANCE) {
            dim2 = 0; double total = 0.0; for (int i=0; i<dim; i++) {if (total <= totalVarianceRatio*trace) dim2++; total += Math.abs(ev[i]); }
        } 
        if (dim2==0) transformation = new double[0][];
        
        double[] work = new double[dim];
        Statik.copy(ev, work);
        Arrays.sort(ev); int[] ixList = new int[dim], ixListInv = new int[dim];
        for (int i=0; i<dim; i++) ixList[i] = dim - 1 - Arrays.binarySearch(ev, work[i]);
        for (int i=0; i<dim; i++) ixListInv[ixList[i]] = i;

        transformation = new double[dim2][dim];
        for (int i=0; i<dim2; i++) for (int j=0; j<dim; j++) transformation[i][ixList[j]] = q[i][j];
    }

    @Override
    public double[] transform(double[] in) {
        return Statik.multiply(transformation, in);
    }

}
