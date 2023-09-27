/*
 * Created on 09.10.2014
 */
package machineLearning.preprocessor;

public abstract class Preprocessor {
    
    protected double[][] data; 
    protected int[] target;
    
    public Preprocessor() {}
    public Preprocessor(double[][] data, int[] target) {setDataAndTarget(data, target);}
    public void setDataAndTarget(double[][] data, int[] target) {
        this.data = data; 
        this.target = target;
    }
    
    public void train() {train(0, data.length);}
    public abstract void train(int start, int end);
    public abstract double[] transform(double[] in);
    public double[][] transform(double[][] in) {
        double[][] erg = new double[in.length][];
        for (int i=0; i<erg.length; i++) erg[i] = transform(in[i]);
        return erg;
    }

}
