/*
 * Created on 10.10.2014
 */
package machineLearning.preprocessor;

import java.util.Arrays;

import engine.Statik;

public class CSP extends Preprocessor {

    private int anzComponentUp, anzComponentDown;
    private boolean square;
    
    private double[][] transformation;
    private double[] transformedMean;
    
    public CSP(double[][] data, int[] target, int anzComponent, boolean square) {this(data, target, anzComponent/2 + anzComponent%2, anzComponent/2, square);}
    public CSP(double[][] data, int[] target, int anzComponentUp, int anzComponentDown, boolean square) {
        super(data,target);
        if (anzComponentUp == -1) {this.anzComponentDown = 0; this.anzComponentUp = data[0].length;}
        else {this.anzComponentDown = anzComponentDown; this.anzComponentUp = anzComponentUp;}
        this.square = square;
    }
    
    @Override
    public void train(int start, int end) {
        int g1 = target[start], g2 = 48629342; int j=start+1; while (j<end) if (target[j]!=g1) {g2 = target[j]; j=end; } else j++;
        if (g2 == 48629342) throw new RuntimeException("Training Range of CSP contains targets of only one group.");
        if (g1>g2) {int t = g1; g1=g2; g2=t;}
        
        int dim = data[start].length, anzPer = end-start, anzPer1=0, anzPer2=0;
        double[][] cov1=new double[dim][dim], cov2 = new double[dim][dim]; double[] mean1 = new double[dim], mean2 = new double[dim];
        for (int i=start; i<end; i++) {
            if (target[i] == g1) {anzPer1++; mean1 = Statik.add(mean1, data[i]); cov1 = Statik.add(cov1,Statik.multiply(data[i], data[i], true));}
            if (target[i] == g2) {anzPer2++; mean2 = Statik.add(mean2, data[i]); cov2 = Statik.add(cov2,Statik.multiply(data[i], data[i], true));}
        }
        mean1 = Statik.multiply(1/(double)(anzPer1), mean1); mean2 = Statik.multiply(1/(double)(anzPer2), mean2);
        cov1 = Statik.subtract(Statik.multiply(1/(double)(anzPer1-1), cov1), Statik.multiply(anzPer1/(double)(anzPer1-1), Statik.multiply(mean1, mean1, true)));
        cov2 = Statik.subtract(Statik.multiply(1/(double)(anzPer2-1), cov2), Statik.multiply(anzPer2/(double)(anzPer2-1), Statik.multiply(mean2, mean2, true)));

        double[][] covTotal = Statik.add(cov1, cov2);

        double[][] q = Statik.identityMatrix(dim); double[] ev = new double[dim]; 
        Statik.eigenvalues(covTotal, 0.0001, ev, q);
        
        double[][] subQ = new double[dim][dim];
        int p = 0;
        for (int i=0; i<dim; i++) {for (j=0; j<dim; j++) subQ[j][p] = q[j][i] / Math.sqrt(ev[i]); p++;} 
        double[][] transCov1 = new double[dim][dim];
        Statik.multiply(Statik.multiply(Statik.transpose(subQ),cov1),subQ, transCov1);
        double[][] q2 = Statik.identityMatrix(dim); double[] ev2 = new double[dim];
        Statik.eigenvalues(transCov1, 0.001, ev2, q2);
        
        double[] work = new double[dim];
        Statik.copy(ev2, work);
        Arrays.sort(ev2); int[] ixList = new int[dim], ixListInv = new int[dim];
        for (int i=0; i<dim; i++) ixList[i] = dim - 1 - Arrays.binarySearch(ev2, work[i]);
        for (int i=0; i<dim; i++) ixListInv[ixList[i]] = i;

        double[] t;
        double[][] q2T = Statik.transpose(q2);
        boolean[] treated =new boolean[dim]; for (int i=0; i<dim; i++) treated[i] = (ixListInv[i]==i);
        for (int i=0; i<dim; i++) if (!treated[i]) {
            int o=i; t = q2T[o]; j=ixListInv[i];  
            while (j!=i) {q2T[o] = q2T[j]; o=j; j= ixListInv[j]; treated[o] = true;}
            q2T[o] = t; treated[i] = true;
        }
        double[][] erg = Statik.multiply(q2T,Statik.transpose(subQ));

        int dim2 = anzComponentUp + anzComponentDown;
        if (dim == dim2) transformation = erg;
        else {
            transformation = new double[dim2][];
            for (int i=0; i<anzComponentUp; i++) transformation[i] = erg[i];  
            for (int i=0; i<anzComponentDown; i++) transformation[i+anzComponentUp] = erg[dim-1-i];  
        }
        transformedMean = Statik.multiply(transformation, Statik.multiply(0.5,Statik.add(mean1, mean2)));
    }

    @Override
    public double[] transform(double[] in) {
        double[] erg = Statik.multiply(transformation, in);
        if (square) for (int i=0; i<erg.length; i++) erg[i] = (erg[i]-transformedMean[i])*(erg[i]-transformedMean[i]);
        return erg;
    }

}
