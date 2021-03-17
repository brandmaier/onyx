/*
 * Created on 23.10.2016
 */
package dirichletProcess;

public abstract class DirichletLikelihoodFunction {

    /**
     * Returns the log likelihood for the person to be in this group. 
     * 
     * @param person
     * @param group
     * @return
     */
    public abstract double getLogLikelihood(int person, int[] group);
}
