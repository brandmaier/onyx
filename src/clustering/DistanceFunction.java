/*
 * Created on 04.05.2011
 */
package clustering;

public interface DistanceFunction {
    
    public final static DistanceFunction euclidean = new DistanceFunction() {
        public double distance(double[] data1, double[] data2) {
            double erg = 0; 
            for (int i=0; i<data1.length; i++) erg += (data1[i]-data2[i])*(data1[i]-data2[i]);
            return Math.sqrt(erg);
        }
    };

    public double distance(double[] data1, double[] data2);
}
