/*
 * Created on 25.03.2017
 */
package engine.backend;

import engine.Statik;

/**
 * Wraps the first two moments of a distribution ss
 * @author timo
 */
public class Distribution {
    public int anzVar;
    public double[] mean;
    public double[][] covariance;
    
    public Distribution(double[] mean, double[][] covariance) {
        this.anzVar = mean.length;
        this.mean = mean;
        this.covariance = covariance;
    }
    
    public Distribution() {
        this.anzVar = 0;
        this.mean = new double[0];
        this.covariance = new double[0][0];
    }
    
    public Distribution(double[][] covariance) {
        this.anzVar = covariance.length;
        this.mean = new double[anzVar];
        this.covariance = covariance;
    }

    public Distribution(int anzVar) {
        this.anzVar = anzVar;
        this.mean = new double[anzVar];
        this.covariance = Statik.identityMatrix(anzVar);
    }

    public Distribution copy() {
        return new Distribution(Statik.copy(mean), Statik.copy(covariance));
    }
    
    public void copyFrom(Distribution toCopy) 
    {
        Statik.copy(toCopy.mean, mean);
        Statik.copy(toCopy.covariance, covariance);
    }
    
    public static Distribution ensureSize(Distribution erg, int anzVar) {
        if (erg == null) return new Distribution(anzVar);
        erg.anzVar = anzVar;
        erg.mean = Statik.ensureSize(erg.mean, anzVar);
        erg.covariance = Statik.ensureSize(erg.covariance, anzVar, anzVar);
        return erg;
    }
    
    public String toString() {
        String erg = Statik.matrixToString(mean, 5);
        erg += "\r\n\r\n"+Statik.matrixToString(covariance, 5);
        return erg;
    }

    public static Distribution copy(Distribution source, Distribution target) {
        if (source == null) return null;
        if (target == null) return source.copy();
        target.copyFrom(source);
        return target;
    }
}
