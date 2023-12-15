/*
* Copyright 2023 by Timo von Oertzen and Andreas M. Brandmaier
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
/*
 * Created on 11.10.2010
 */
package engine.backend;

public abstract class NumericalDerivativeModel extends Model {

    private double approxEps = 0.00001;
    
    public abstract double computeLeastSquares(double[] value);
    public abstract double computeLogLikelihood(double[] value);
    public void setApproximationEpsilon(double eps) {approxEps = eps;}
    public double getApproximationEpsilon() {return approxEps;}
    
    private double[] work;
    
    private void computeLSorMLDerivatives(boolean recomputeMuAndSigma, boolean isLS) {
        double[] value = getParameter();
        if (recomputeMuAndSigma) evaluateMuAndSigma(value);
        
        if ((work==null) || (work.length != anzPar)) work = new double[anzPar];
        if ((lsD==null) || (lsD.length != anzPar)) lsD = new double[anzPar];
        if ((llD==null) || (llD.length != anzPar)) llD = new double[anzPar];
        if ((llDD==null) || (llDD.length != anzPar) || (llDD[0].length != anzPar)) llDD = new double[anzPar][anzPar];
        if ((lsDD==null) || (lsDD.length != anzPar) || (lsDD[0].length != anzPar)) lsDD = new double[anzPar][anzPar];
        
        double approxEpsSqr = approxEps * approxEps;
        double val = (isLS?computeLeastSquares(value):computeLogLikelihood(value));
        for (int i=0; i<anzPar; i++) {
            value[i] += approxEps; 
            work[i] = (isLS?computeLeastSquares(value):computeLogLikelihood(value)); 
            value[i] -= approxEps;
            double erg = (work[i] - val) / approxEps;
            if (isLS) lsD[i] = erg; else llD[i] = erg; 
        }
        for (int i=0; i<anzPar; i++) for (int j=i; j<anzPar; j++) {
            value[i] += approxEps; value[j] += approxEps;
            double valPP = (isLS?computeLeastSquares(value):computeLogLikelihood(value));
            value[i] -= approxEps; value[j] -= approxEps;
            double erg = (valPP - work[i] - work[j] + val) / approxEpsSqr;
            if (isLS) lsDD[i][j] = lsDD[j][i] = erg; else llDD[i][j] = llDD[j][i] = erg; 
        }
        setParameter(value);
    }
    
    public void setEpsilon(double epsilon) {this.approxEps = epsilon;}
    public double getEpsilon() {return this.approxEps;}
    
    @Override
    public void computeLeastSquaresDerivatives(double[] value, boolean recomputeMuAndSigma) {
        if (value != null) setParameter(value); 
        computeLSorMLDerivatives(recomputeMuAndSigma, true);
    }

    @Override
    public void computeLogLikelihoodDerivatives(double[] value, boolean recomputeMuAndSigma) {
        if (value != null) setParameter(value); 
        computeLSorMLDerivatives(recomputeMuAndSigma, false);
    }

    @Override
    protected void computeMatrixTimesMuDev(int par, double[][] matrix, double[] erg) {
        throw new RuntimeException("Derivative of mu is not available in descendants of NumericalDerivativeModels.");
    }

    @Override
    protected void computeMatrixTimesSigmaDev(int par, double[][] matrix, double[][] erg) {
        throw new RuntimeException("Derivative of sigma is not available in descendants of NumericalDerivativeModels.");
    }

    @Override
    protected void computeMatrixTimesSigmaDevDev(int par1, int par2, double[][] matrix, double[][] erg) {
        throw new RuntimeException("Derivative of sigma is not available in descendants of NumericalDerivativeModels.");
    }
}
