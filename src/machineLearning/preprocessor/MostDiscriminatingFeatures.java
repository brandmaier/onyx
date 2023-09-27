/*
 * Created on 10.10.2014
 */
package machineLearning.preprocessor;

import java.util.Arrays;
import java.util.Comparator;

public class MostDiscriminatingFeatures extends Preprocessor {

    private double[][] indexAndEntro;
    private int anzComponents;
    
    public MostDiscriminatingFeatures(double[][] data, int[] target, int anzComponents) {
        super(data, target);
        this.anzComponents = anzComponents;
    }
    
    @Override
    public void train(int start, int end) {
        int g1 = target[start], g2 = 2034234; int j=start+1; while (j<end) if (target[j]!=g1) {g2 = target[j]; j=end; } else j++;
        if (g2 == 2034234) throw new RuntimeException("Training Range of CSP contains targets of only one group.");
        if (g1>g2) {int t = g1; g1=g2; g2=t;}
        
        int anzPer = end-start, anzVar = data[0].length;
        double[][] dataWT = new double[anzPer][anzVar+1];
        for (int i=start; i<end; i++) {dataWT[i-start][anzVar] = (target[i-start]==g1?0:1); for (j=0; j<anzVar; j++) dataWT[i-start][j] = data[i][j];}
        
        int[][] c = new int[2][2];
        int[] tot = new int[2]; for (int i=start; i<end; i++) tot[(int)dataWT[i][anzVar]]++;
        indexAndEntro = new double[anzVar][2];
        for (int v=0; v<anzVar; v++) {
            indexAndEntro[v][0] = v;
            final int fv = v;
            Arrays.sort(dataWT, new Comparator<double[]>() {
                public int compare(double[] arg0, double[] arg1) {return (int) Math.signum(arg0[fv]-arg1[fv]);}
            });
            c[0][0] = c[0][1] = 0; c[1][0] = tot[0]; c[1][1] = tot[1];
            indexAndEntro[v][1] = Double.MAX_VALUE;
            for (int i=0; i<anzPer-1; i++) {
                double val = dataWT[i][v];
                while(i<anzPer && dataWT[i][v]==val) {c[0][(int)dataWT[i][anzVar]]++; c[1][(int)dataWT[i][anzVar]]--; i++;} 
                i--;
                int t1 = c[0][0]+c[0][1], t2 = c[1][0]+c[1][1];
                double p1 = c[0][0]/(double)t1, p2 = c[1][0]/(double)t2;
                double nentro = -(p1==0||(1-p1)==0?0:((t1/(double)anzPer) * (p1 * Math.log(p1) + (1-p1)*Math.log(1-p1)))) +
                               -(p2==0||(1-p2)==0?0:((t2/(double)anzPer) * (p2 * Math.log(p2) + (1-p2)*Math.log(1-p2))));
                if (nentro < indexAndEntro[v][1]) indexAndEntro[v][1] = nentro;
            }
        }
        
        Arrays.sort(indexAndEntro, new Comparator<double[]>() {
            public int compare(double[] arg0, double[] arg1) {return (int) Math.signum(arg0[1]-arg1[1]);}
        });
    }

    @Override
    public double[] transform(double[] in) {
        double[] erg = new double[anzComponents];
        for (int i=0; i<Math.min(anzComponents,indexAndEntro.length); i++) erg[i] = in[(int)indexAndEntro[i][0]];
        return erg;
    }

}
