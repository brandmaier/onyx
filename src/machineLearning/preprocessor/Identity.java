/*
 * Created on 10.10.2014
 */
package machineLearning.preprocessor;

public class Identity extends Preprocessor {

    @Override
    public void train(int start, int end) {
    }

    @Override
    public double[] transform(double[] in) {
        return in;
    }

}
