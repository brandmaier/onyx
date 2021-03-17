/*
 * Created on 23.10.2016
 */
package dirichletProcess;

public class UniformDirichletLikelihoodFunction extends DirichletLikelihoodFunction {

    @Override
    public double getLogLikelihood(int person, int[] group) {
        return -Math.log(group.length);
    }

}
