/*
 * Created on 06.09.2015
 */
package engine.backend;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;

import engine.Statik;

/**
 * Models a general IRT model with a sigmoid likelihood function. C (>= 1) category dimensions are defined (typically one if no persons are defined, or
 * two for items and persons, but can be more). Each dimension c has K_c (>= 0) covariates, each such covariate comes with one slope parameter zeta_{ci} 
 * to be fitted; 
 * in addition, one intercept parameter ipsilon_{cv} for each category value v in each dimension is fitted (difficulty resp. ability parameter for items 
 * and persons). 
 * By default, the first intercept parameter (for category zero in each dimension) is fixed to zero. Each data row comes with a category value z_c for each
 * dimension, with K_c covariates y_ci for each dimension, and exactly one binary result x (0 or 1).
 * 
 *  The model assumes independence conditioned on category values and covariates, with the likelihood function
 *  
 *  L(x = 1 | z_{c}, y_{cv}, zeta_{ci}, ipsilon_{cv}) = sigma(f(x)) 
 *  where f(x) = sum_{c=1}^C [ipsilon_{c, z_{c}} + sum_{i=1}^{K_i} zeta_{ci} z_{ci}
 *
 *  UNFINISHED
 *  
 * @author timo
 */
public class IRTModel extends Model {

    private boolean DEBUGFLAG = false;

    // Number of dimensions in covariate structure; choose 1 for a model with items alone, 2 for a model with items crossed with participants,
    // 3 or more for multi-level structures in either persons or items.
    public int anzDimensions;
    // Number of covariates for each dimension. Can be zero (indicating that only the intercept parameters should be used).
    public int[] anzCovariates;
    // position of the target (0-1 variable) in the data set.
    public int targetColumn;
    // position of the categories for each dimension in the date set; e.g., for item dimension, the item number.
    public int[] categoryColumns;
    // position of the covariates for each dimensions in the data set.
    public int[][] covariateColumns;
    
    public IRTModel(double[][] points) {
        super();
        setData(points);
    }
    
    @Override 
    public void setData(double[][] data) {
        this.data = data;
        anzPer = data.length; 
        anzVar = data.length;
        anzPar = data.length-1;
    }
    
    @Override
    public IRTModel copy() {
        IRTModel erg = new IRTModel(data);
        erg.position = Statik.copy(position);
        erg.setStrategy(this.getStrategy());
        return erg;
    }

    @Override
    public Model removeObservation(int obs) {
        IRTModel erg = copy();
        double[][] newData = Statik.submatrix(data, obs);
        erg.setData(newData);
        return erg;
    }

    @Override
    protected void computeMatrixTimesSigmaDev(int par, double[][] matrix, double[][] erg) {
        throw new RuntimeException ("computeMatrixTimesSigmaDev is not applicable for IRTModel.");
    }

    @Override
    protected void computeMatrixTimesSigmaDevDev(int par1, int par2,double[][] matrix, double[][] erg) {
        throw new RuntimeException ("computeMatrixTimesSigmaDevDev is not applicable for IRTModel.");
    }

    @Override
    protected void computeMatrixTimesMuDev(int par, double[][] matrix,double[] erg) {
        throw new RuntimeException ("computeMatrixTimesMuDev is not applicable for IRTModel.");
    }

    @Override
    public boolean setParameter(int nr, double value) {
        position[nr] = value;
        return true;
    }

    @Override
    public double getParameter(int nr) {
        return (nr >= anzPar?0.0:position[nr]);
    }

    @Override
    protected void removeParameterNumber(int nr) {
        throw new RuntimeException ("removeParameterNumber is not applicable for IRTModel; use removeObservation instead.");
    }

    @Override
    protected int maxParNumber() {
        return anzPar;
    }
    
    @Override
    public int getAnzPar() {return anzPar;}

    @Override
    public boolean isErrorParameter(int nr) {
        return false;
    }
    
    @Override
    public void evaluateMuAndSigma(double[] values) {
    }

    @Override
    public void computeLeastSquaresDerivatives(double[] value,boolean recomputeMuAndSigma) {
        throw new RuntimeException ("Least Squares is not implemented for IRTModel.");
    }
    
    @Override
    public boolean setParameter(double[] value) {
        if (position == null) position = Statik.copy(value);
        else Statik.copy(value, position);
        return true;
    }
    
    @Override
    public double getMinusTwoLogLikelihood(double[] value, boolean recomputeMuAndSigma) {
        ll = 0;
        return ll;
    }

    @Override
    public double[] estimateML() {position = Statik.ensureSize(position, anzPar); return estimateML(position);}

    public void computeLogLikelihoodDerivatives() {computeLogLikelihoodDerivatives(null);}
    
    @Override
    public void computeLogLikelihoodDerivatives(double[] value, boolean recomputeMuAndSigma) {
        llD = Statik.ensureSize(llD, anzPar);
        llDD = Statik.ensureSize(llDD, anzPar, anzPar);
    }

}
