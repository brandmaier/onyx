/*
 * Created on 25.09.2023
 */
package arithmetik;

import engine.Statik;

public class AnalyticalFunctionPartlyFixed extends AnalyticalFunction {

    private AnalyticalFunction f;
    private int[] fixedToBig;
//    private double[] fixval;
    
    private final int anzPar; 
    private final double[] values;
    private final int[] twoPNR = new int[2];
    private final int[] freeToBig;
    
    public AnalyticalFunctionPartlyFixed(AnalyticalFunction f, int[] fixedToBig, double[] fixval)
    {
        this.f = f; this.fixedToBig = Statik.copy(fixedToBig);
        anzPar = f.anzPar()-fixedToBig.length;
        values =  new double[f.anzPar()];
        freeToBig = new int[anzPar];
        
        int k=0;
        for (int i=0; i<f.anzPar(); i++) {
            boolean isFixed = false; for (int j=0; j<fixedToBig.length; j++) if (fixedToBig[j]==i) isFixed = true;
            if (!isFixed) freeToBig[k++] = i;
        }
        setFixedValues(fixval);
    }

    public void setFixedValues(double[] fixval) {for (int i=0; i<fixedToBig.length; i++) values[fixedToBig[i]] = fixval[i];}
    public void setFixedValue(double fixval) {values[fixedToBig[0]] = fixval;}
    
    public void setFreeValues(double[] val) {
        for (int i=0; i<anzPar; i++) values[freeToBig[i]] = val[i]; 
    }
    public void setFreeValue(double val) {values[freeToBig[0]] = val;}
    
    public double eval(double[] val) {setFreeValues(val); return f.eval(values);}
    public double evalDev(int pnr, double[] val) {setFreeValues(val); return f.evalDev(freeToBig[pnr],values);}
    public double evalDev(int[] pnr, double[] val) {
        setFreeValues(val); 
        int[] npnr = (pnr.length==2?twoPNR:new int[pnr.length]);
        for (int i=0; i<pnr.length; i++) npnr[i] = freeToBig[i];
        return f.evalDev(npnr, values);
    }
    
    public int anzPar() {return anzPar;}
    
    public double[] getAllValues() {return values;}
    
    public AnalyticalFunction getParent() {return f;};
}
