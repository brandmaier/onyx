/*
 * Created on 26.04.2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package scripts;
import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeSet;

import arithmetik.AnalyticalFunction;
import engine.Statik;
import engine.backend.LinearModel;
import engine.backend.Model;
import engine.backend.LinearModel.parameterType;


/**
 * @author timo
 * 
 * This is a model as described by Browne (1993), approximating a non-linear model f(t,theta) by the first part of its Taylor series at theta_mean, the
 * mean of the parameters,
 * 
 *  f(t,theta) = f(t,theta_mean) + theta*_1 [df/d theta_1](t,theta_mean) + ... + theta*_n [df/d theta_n] (t,theta_mean)
 *  
 * theta*_i is then an zero-mean variable with covariance structure approximately the same as theta_i.
 * The function f is given as a AnalyticalFunction, which should implement the first three derivaties (first one for the approximation,
 * and two following for the Hessian) for all but the first parameter, which is time. 
 * Most part of the code is similar to LinearModel, just the mean variables (which are complex weights on the
 * edges) and the leading f(t,theta_mean) are added. 
 * 
 * 
 */
public class LinearApproximationModel extends Model
{
    public static enum parameterType {mean, facCov, err};
    
    AnalyticalFunction func;
    double[] timeBasis;             // time points of the observations
    
    int anzFac;
    
    // Note that the mean is not the mean of the factors as usual (which is fixed to zero), but the weights on the edges depeding on the derivatives.
    double[][] structure;
    int[] meanPar;  
    double[] meanVal;
    int[][] covPar;
    double[][] covVal;
    int[] errPar;
    double[] errVal;
    
    // gives the position of the parameter (row, column); second is unused for mean, 1st <= 2nd for facCov
    int[][] parPos;
    
    // parameter types
    parameterType[] parType;
    
    public static final AnalyticalFunction exponentialDecline = new AnalyticalFunction() {
        public double eval(double[] val)
        {
            return val[1] - Math.exp(val[2]*(val[0]-val[3]));
        }
        public double eval(int pnr, double v, double[] val) {
            if (pnr!=0) return super.eval(pnr, v, val); 
            else return val[0] - Math.exp(val[1]*(v-val[2]));
        }
        
        public int anzPar() {return 4;}
        
        public double evalDev(int par, double[] val) {
            double diff = val[0]-val[3];
            double exp = Math.exp(val[2]*diff);
            if (par==0) return -val[2]*exp; 
            if (par==1) return 1; 
            if (par==2) return -diff*exp; 
            if (par==3) return val[2]*exp;
            throw new RuntimeException("Exponential Function says: Can't compute derivative wrt. parameter "+par);
        }
        
        public double evalDev(int[] devRow, double[] val) {
            if (devRow.length==0) return eval(val);
            if (devRow.length==1) return evalDev(devRow[0], val);
            double diff = val[0]-val[3];
            double exp = Math.exp(val[2]*diff);
            for (int i=0; i<devRow.length; i++) if (devRow[i]==1) return 0;
            if (devRow.length==2)
            {
                if (((devRow[0]==0) && (devRow[1]==0)) || 
                    ((devRow[0]==3) && (devRow[1]==3))) return -val[2]*val[2]*exp;
                if (((devRow[0]==2) && (devRow[1]==2))) return -diff*diff*exp; 
                if (((devRow[0]==0) && (devRow[1]==2)) || 
                    ((devRow[0]==2) && (devRow[1]==0))) return -(1+val[2]*diff)*exp; 
                if (((devRow[0]==2) && (devRow[1]==3)) || 
                    ((devRow[0]==3) && (devRow[1]==2))) return (1+val[2]*diff)*exp; 
                if (((devRow[0]==0) && (devRow[1]==3)) || 
                    ((devRow[0]==3) && (devRow[1]==0))) return val[2]*val[2]*exp;
                throw new RuntimeException("Exponential Function says: Can't compute derivative wrt. parameters "+devRow[0]+", "+devRow[1]);
            }
            if (devRow.length==3)
            {
                if (    ((devRow[0]==0) && (devRow[1]==0) && (devRow[2]==0)) ||
                        ((devRow[0]==0) && (devRow[1]==3) && (devRow[2]==3)) ||
                        ((devRow[0]==3) && (devRow[1]==0) && (devRow[2]==3)) ||
                        ((devRow[0]==3) && (devRow[1]==3) && (devRow[2]==0))) return -val[2]*val[2]*val[2]*exp; 
                if (    ((devRow[0]==2) && (devRow[1]==2) && (devRow[2]==2))) return -diff*diff*diff*exp; 
                if (    ((devRow[0]==3) && (devRow[1]==3) && (devRow[2]==3)) ||
                        ((devRow[0]==0) && (devRow[1]==0) && (devRow[2]==3)) ||
                        ((devRow[0]==0) && (devRow[1]==3) && (devRow[2]==0)) ||
                        ((devRow[0]==3) && (devRow[1]==0) && (devRow[2]==0))) return val[2]*val[2]*val[2]*exp; 
                if (    ((devRow[0]==0) && (devRow[1]==0) && (devRow[2]==2)) ||
                        ((devRow[0]==0) && (devRow[1]==2) && (devRow[2]==0)) ||
                        ((devRow[0]==2) && (devRow[1]==0) && (devRow[2]==0)) ||
                        ((devRow[0]==3) && (devRow[1]==3) && (devRow[2]==2)) ||
                        ((devRow[0]==3) && (devRow[1]==2) && (devRow[2]==3)) ||
                        ((devRow[0]==2) && (devRow[1]==3) && (devRow[2]==3))) return -val[2]*(2+val[2]*diff)*exp;
                if (    ((devRow[0]==0) && (devRow[1]==2) && (devRow[2]==2)) ||
                        ((devRow[0]==2) && (devRow[1]==0) && (devRow[2]==2)) ||
                        ((devRow[0]==2) && (devRow[1]==2) && (devRow[2]==0))) return -diff*(2+val[2]*diff)*exp;
                if (    ((devRow[0]==0) && (devRow[1]==2) && (devRow[2]==3)) ||
                        ((devRow[0]==0) && (devRow[1]==3) && (devRow[2]==2)) ||
                        ((devRow[0]==2) && (devRow[1]==0) && (devRow[2]==3)) ||
                        ((devRow[0]==3) && (devRow[1]==0) && (devRow[2]==2)) ||
                        ((devRow[0]==2) && (devRow[1]==3) && (devRow[2]==0)) ||
                        ((devRow[0]==3) && (devRow[1]==2) && (devRow[2]==0))) return val[2]*(2+val[2]*diff)*exp;
                if (    ((devRow[0]==2) && (devRow[1]==2) && (devRow[2]==3)) ||
                        ((devRow[0]==2) && (devRow[1]==3) && (devRow[2]==2)) ||
                        ((devRow[0]==3) && (devRow[1]==2) && (devRow[2]==2))) return diff*(2+val[2]*diff)*exp;
                throw new RuntimeException("Exponential Function says: Can't compute derivative wrt. parameters "+devRow[0]+", "+devRow[1]+", "+devRow[2]);
            }
            
            // for higher derivatives:
            return super.evalDev(devRow, val);
            
        }
    };
    
    public double[][] fisherInformationMatrix;

    // Some working variables
    double[][][] sigmaDev;
    double[][] facCovWork, sigmaWork, sigmaWork2, sigmaWork3, sigmaWork4, structureWork, structureWork2, structureTransWork, muDev;
    double[] muWork, muWork2, posWork, p1w, p2w, cew, a1w, a2w, parWork;
    int[] sDevRow, sDevCol, cDevRow, cDevCol, mDev, devRow1, devRow2;
    
    public LinearApproximationModel(AnalyticalFunction function, double[] timeBasis) {this(function, timeBasis, null, null, -1);}
    public LinearApproximationModel(AnalyticalFunction function, double[] timeBasis, double[] mean, double[][] cov, double errorVariance)
    {
        this.func = function;
        this.timeBasis = timeBasis;
        this.anzFac = func.anzPar() - 1;
        this.anzVar = timeBasis.length;
        this.structure = new double[anzVar][anzFac];
        this.anzPar = anzFac + anzFac*(anzFac+1)/2 + 1;
        this.meanPar = new int[anzFac];
        this.meanVal = new double[anzFac]; if (mean!=null) meanVal = Statik.copy(mean);
        for (int i=0; i<anzFac; i++) meanPar[i] = i;
        this.covPar = createSaturatedCovariance(anzFac, anzFac);
        this.errPar = new int[anzVar]; for (int i=0; i<anzVar; i++) errPar[i] = anzPar-1;
        this.errVal = new double[anzVar]; for (int i=0; i<anzVar; i++) errVal[i] = (errorVariance<0?1:errorVariance);
        this.covVal = new double[anzFac][anzFac]; if (cov != null) covVal = Statik.copy(cov);

		collectParameterTypesAndPos();
		anzPer = 0; data = new double[0][];
		
		fisherInformationMatrix = new double[anzPar][anzPar];
    }
    
    public LinearApproximationModel(LinearApproximationModel toCopy) {
        func = toCopy.func;
        anzFac = toCopy.anzFac;
        anzVar = toCopy.anzVar;
        anzPar = toCopy.anzPar;
        anzPer = toCopy.anzPer;

        timeBasis = Statik.copy(toCopy.timeBasis);
        structure = Statik.copy(toCopy.structure);
        meanPar = Statik.copy(toCopy.meanPar);
        meanVal = Statik.copy(toCopy.meanVal);
        covVal = Statik.copy(toCopy.covVal);
        covPar = Statik.copy(toCopy.covPar);
        errVal = Statik.copy(toCopy.errVal);
        errPar = Statik.copy(toCopy.errPar);
        data = (toCopy.data==null?null:Statik.copy(toCopy.data));
        computeMoments();
        fisherInformationMatrix = new double[anzPar][anzPar];
        collectParameterTypesAndPos();
    }
    
    public static int[][] createSaturatedCovariance(int size, int startNr)
    {
        int[][] erg = new int[size][size];
        for (int i=0; i<size; i++) erg[i][i] = startNr++;
        for (int i=0; i<size; i++) for (int j=i+1; j<size; j++) erg[i][j] = erg[j][i] = startNr++;
        return erg;
    }
    
    public void collectParameterTypesAndPos()
    {
        int maxPar = maxParNumber();
        parType = new parameterType[maxPar+1]; 
        parPos = new int[maxPar+1][];
    
        for (int i=0; i<meanPar.length; i++) if (meanPar[i]!=NOPARAMETER) {parType[meanPar[i]] = parameterType.mean; parPos[meanPar[i]] = new int[]{i,0};}
        for (int i=0; i<anzFac; i++) for (int j=i; j<anzFac; j++) 
            if (covPar[i][j]!=NOPARAMETER) {parType[covPar[i][j]] = parameterType.facCov; parPos[covPar[i][j]] = new int[]{i,j};}
        for (int i=0; i<errPar.length; i++) if (errPar[i] != NOPARAMETER) {parType[errPar[i]] = parameterType.err; parPos[errPar[i]] = new int[]{i,0};}
        anzPar = maxPar +1;
    }
    
    /**
     * Fixes regression weights parameters (ie., fixed development point of the model function) and
     * converts the Model to a LinearModel. If error is homogenous, this can then be estimated using PPML. 
     * 
     * @return Linear Model with the same fit.
     */
    public LinearModel convertToLinearModel() {return convertToLinearModel(null);}
    public LinearModel convertToLinearModel(double[] pos)
    {
        LinearApproximationModel copy = new LinearApproximationModel(this);
        double[] newMeanVal = new double[anzFac]; for (int i=0; i<anzFac; i++) newMeanVal[i] = 0;
        copy.evaluateStructure(pos);
        LinearModel erg = new LinearModel(copy.structure, copy.covPar, copy.covVal, copy.meanPar, newMeanVal, copy.errPar, copy.errVal);
        copy.evaluateMu();
        for (int i=0; i<anzPer; i++)
            for (int j=0; j<anzVar; j++) copy.data[i][j] -= copy.mu[j];
        erg.setData(copy.data);
        return erg;
    }
    
    /**
     * ASSUMES regression weights (ie., the development point of the model, given in meanVal) are not too far from optimum, and homogenous error, 
     * 
     * 
     * converts the Model to a LinearModel and estimates it using PPML. 
     * 
     * @return ML fit approximation
     */
    public double[] estimateMLSupportedByPowerEquivalence() {return estimateMLSupportedByPowerEquivalence(null, false);}
    public double[] estimateMLSupportedByPowerEquivalence(boolean meansFixed) {return estimateMLSupportedByPowerEquivalence(null, meansFixed);}
    public double[] estimateMLSupportedByPowerEquivalence(double[] starting) {return estimateMLSupportedByPowerEquivalence(null, false);}
    public double[] estimateMLSupportedByPowerEquivalence(double[] starting, boolean meansFixed)
    {
        LinearModel linearModel = convertToLinearModel(starting);
        if (meansFixed) for (int i=0; i<anzFac; i++) if (linearModel.meanPar[i]!=NOPARAMETER) linearModel.fixParameter(linearModel.meanPar[i]);
        boolean covSaturated = true; for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) covSaturated &= (covPar[i][j] != NOPARAMETER);
        double[] subEstimate = (covSaturated?
                linearModel.estimateMLFullCovarianceSupportedByPowerEquivalence():
                linearModel.estimateMLSupportedByPowerEquivalence(starting));
        linearModel.setParameter(subEstimate);
        double[] erg = new double[anzPar];
        for (int i=0; i<anzFac; i++) if (meanPar[i]!=NOPARAMETER) erg[meanPar[i]] = linearModel.meanVal[i] + (starting==null?meanVal[i]:starting[meanPar[i]]);
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (covPar[i][j]!=NOPARAMETER) erg[covPar[i][j]] = linearModel.covVal[i][j];
        for (int i=0; i<anzVar; i++) if (errPar[i] != NOPARAMETER) erg[errPar[i]] = linearModel.errVal[i];
        this.setParameter(erg);
        return erg;
    }
    
    public int maxParNumber()
    {
        int erg = NOPARAMETER;
        for (int i=0; i<meanPar.length; i++) erg = Math.max(meanPar[i], erg);
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++) erg = Math.max(covPar[i][j], erg);
        for (int i=0; i<errPar.length; i++) erg = Math.max(errPar[i], erg);
        return erg;
    }
    
    public int[] getActiveParameters()
    {
        int toPar = maxParNumber()+1;
        int[] ergO = new int[toPar];
        for (int i=0; i<meanPar.length; i++)
            if (meanPar[i] != NOPARAMETER) ergO[meanPar[i]]=1;
        for (int i=0; i<anzFac; i++)
            for (int j=i; j<anzFac; j++)
                if (covPar[i][j] != NOPARAMETER) ergO[covPar[i][j]]=1;
        for (int i=0; i<errPar.length;i++) 
            if (errPar[i] != NOPARAMETER) ergO[errPar[i]]=1;
        int anz = 0; for (int i=0; i<toPar; i++) if (ergO[i]!=0) anz++;
        int[] erg = new int[anz]; int j=0; 
        for (int i=0; i<toPar; i++) if (ergO[i]!=0) erg[j++] = i;
        Arrays.sort(erg);
        return erg;
    }
    
    public void renumberParameter(int oldNumber, int newNumber)
    {
        for (int i=0; i<meanPar.length; i++)
            if (meanPar[i] == oldNumber) meanPar[i] = newNumber;
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++)
                if (covPar[i][j] == oldNumber) covPar[i][j] = newNumber;
        for (int i=0; i<errPar.length; i++)
            if (errPar[i] == oldNumber) errPar[i] = newNumber;
        collectParameterTypesAndPos();
    }
    
    /**
     * ASSUMES identical error in all observations, and constant structure matrix.
     * 
     * Computes a matrix erg such that erg &* struct is upper right and the error structure is an identy matrix times sigma_err again.
     * 
     * @return
     */
    public double[][] computePowerEquivalenceTransformationMatrix()
    {
       double[][] erg = Statik.identityMatrix(anzVar);
       double[][] work = Statik.copy(structure);
       double[][] work2 = new double[anzVar][anzVar], work3 = new double[anzVar][anzVar];
       for (int i=0; i<anzFac; i++)
       {
           double piv = work[i][i];
           for (int j=0; j<anzFac; j++) {work[i][j] /= piv; erg[i][j] /= piv;}
           for (int j=i+1; j<anzVar; j++)
           {
               double lead = work[j][i];
               for (int k=0; k<anzFac; k++) {work[j][k] -= lead*work[i][k]; erg[j][k] -= lead*erg[i][k];} 
           }
       }
       for (int i=anzFac-1; i>=0; i--)
       {
           for (int j=i-1; j>=0; j--)
           {
               double lead = work[j][i];
               for (int k=0; k<anzFac; k++) {work[j][k] -= lead*work[i][k]; erg[j][k] -= lead*erg[i][k];}
           }
       }
       Statik.transpose(erg, work3);
       Statik.multiply(erg, work3, work2);
       Statik.transposeContradiagonal(work2,work2);
       Statik.transpose(work2,work2);       
       Statik.choleskyDecompose(work2, work3);
       Statik.transposeContradiagonal(work3,work3);
       Statik.transpose(work3, work3);
       
       for (int i=anzVar-1; i>=0; i--)
       {
           double piv = work3[i][i];
           for (int j=0; j<anzVar; j++) {work3[i][j] /= piv; erg[i][j] /= piv;}
           for (int j=i-1; j>=0; j--)
           {
               double lead = work3[j][i];
               for (int k=0; k<anzVar; k++) {work3[j][k] -= lead*work3[i][k]; erg[j][k] -= lead*erg[i][k];}
           }
       }
       
       return erg;
    }

    public int getAnzFac() {
        return anzFac;
    }
    public int getAnzPar() {
        return anzPar;
    }
    public int getAnzVar() {
        return anzVar;
    }
    public boolean setParameterValue(int nr, double value)
    {
        boolean succ = true;
        for (int i=0; i<anzFac; i++) 
            if (meanPar[i]==nr) {meanVal[i]=value; succ = true;}
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++) 
                if (covPar[i][j]==nr) {covVal[i][j]=value; succ = true;}
        for (int i=0; i<anzVar; i++)
            if (errPar[i]==nr) {errVal[i]=value; succ = true;}
        return succ;
    }
    public void setParameterValue(double[] vals)
    {
        for (int i=0; i<vals.length; i++) setParameterValue(i, vals[i]);
    }
    public void setParameterValueAccordingToOtherModel(LinearApproximationModel reference, int nr, double value)
    {
        for (int i=0; i<anzFac; i++) 
            if (reference.meanPar[i]==nr) meanVal[i]=value;
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++) 
                if (reference.covPar[i][j]==nr) covVal[i][j]=value;
        for (int i=0; i<anzVar; i++)
            if (reference.errPar[i]==nr) errVal[i]=value;
    }
    public void setParameterValueAccordingToOtherModel(LinearApproximationModel reference, int nr)
    {
        for (int i=0; i<anzFac; i++) 
            if (reference.meanPar[i]==nr) meanVal[i]=reference.meanVal[i];
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++) 
                if (reference.covPar[i][j]==nr) covVal[i][j]=reference.covVal[i][j];
        for (int i=0; i<anzVar; i++)
            if (reference.errPar[i]==nr) errVal[i]=reference.errVal[i];
    }
    public void setData(double[][] data)
    {
        this.data = data;
        this.anzPer = data.length;
        computeMoments();
    }
    
    public double getParameterValue(int nr)
    {
        for (int i=0; i<anzFac; i++) 
            if (meanPar[i]==nr) return meanVal[i];
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++) 
                if (covPar[i][j]==nr) return covVal[i][j];
        for (int i=0; i<anzVar; i++)
            if (errPar[i]==nr) return errVal[i];
        throw new RuntimeException("Parameter "+nr+" does not exist.");
    }
    
    public double[] getParameters()
    {
        double[] erg = new double[anzPar];
        for (int i=0; i<anzFac; i++) 
            if (meanPar[i]!=NOPARAMETER) erg[meanPar[i]] = meanVal[i];
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++) 
                if (covPar[i][j]!=NOPARAMETER) erg[covPar[i][j]] = covVal[i][j];
        for (int i=0; i<anzVar; i++)
            if (errPar[i]!=NOPARAMETER) erg[errPar[i]] = errVal[i];
        return erg;
    }
        
    public void computeMoments()
    {
        if (data==null) return;
        xsum = new double[anzVar];
        dataMean = new double[anzVar];
		for (int i=0; i<anzVar; i++) xsum[i] = 0.0;
		for (int i=0; i<anzPer; i++)
			for (int j=0; j<anzVar; j++) xsum[j] += data[i][j];
		for (int i=0; i<anzVar; i++) dataMean[i] = xsum[i] / (double)anzPer;
			
		xBiSum = new double[anzVar][anzVar];
		dataCov = new double[anzVar][anzVar];
		for (int i=0; i<anzVar; i++)
			for (int j=0; j<=i; j++)
			{
				xBiSum[i][j] = 0.0;
				for (int k=0; k<anzPer; k++) xBiSum[i][j] += data[k][i] * data[k][j];
				for (int k=0; k<anzPer; k++) dataCov[i][j] += (data[k][i]-dataMean[i]) * (data[k][j]-dataMean[j]);
				xBiSum[j][i] = xBiSum[i][j];
				dataCov[i][j] /= (double)anzPer;
				dataCov[j][i] = dataCov[i][j];
			}
    }

    public static LinearApproximationModel fixParameter(LinearApproximationModel in, int nr) {LinearApproximationModel erg = new LinearApproximationModel(in); erg.fixParameter(nr); return erg;}
    public static LinearApproximationModel fixParameter(LinearApproximationModel in, int nr, double value) {LinearApproximationModel erg = new LinearApproximationModel(in); erg.fixParameter(nr,value); return erg;}
    public void removeParameterNumber(int nr) {fixParameter(nr, getParameter(nr));}
    public boolean fixParameter(int nr, double value)
    {
        anzPar--;
        for (int i=0; i<anzFac; i++)
        {
            if (meanPar[i]==nr) { meanVal[i] = value; meanPar[i] = NOPARAMETER;}
            if (meanPar[i] >nr)  meanPar[i]--;
        }
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++)
            {
                if (covPar[i][j]==nr) { covVal[i][j] = value; covPar[i][j] = -1;}
                if (covPar[i][j] >nr)  covPar[i][j]--;
            }
        for (int i=0; i<anzVar; i++)
        {
            if (errPar[i]==nr) { errVal[i] = value; errPar[i] = NOPARAMETER;}
            if (errPar[i] >nr)  errPar[i]--;
        }
        collectParameterTypesAndPos();
        return true;
    }
    public static LinearApproximationModel picDatapoints(LinearApproximationModel in, int[] piced) {LinearApproximationModel erg = new LinearApproximationModel(in); erg.picDatapoints(piced); return erg;}
    public void picDatapoints(int[] piced)
    {
        anzVar = piced.length;
        if (anzPer > 0) 
            for (int i=0; i<anzPer; i++) data[i] = Statik.subvector(data[i], piced);

        structure = Statik.submatrix(structure, piced);
        errVal = Statik.subvector(errVal, piced);
        errPar = Statik.subvector(errPar, piced);
    }
/*
    public double[][] createData (int anzPersons) {return createData(anzPersons, covVal, meanVal, structure, errVal);}
    public double[][] createData (int anzPersons, double[][] covMatrix, double[] muVector, double[][] structureMatrix, double[] error)
    {
        this.anzPer = anzPersons;
        int anzVar = structureMatrix.length;
        int anzFac = muVector.length;
        double[][] cholesky = Statik.choleskyDecompose(covMatrix);
        data = new double[anzPersons][anzVar];
        double[][] structureChol = Statik.multiply(structureMatrix,cholesky);
        double[] structureMu   = evaluateMu();

        double[] m = new double[anzFac];
        double[] sm = new double[anzVar];
        for (int i=0; i<anzPersons; i++)
        {
            for (int j=0; j<anzFac; j++) m[j] = rand.nextGaussian();
            Statik.multiply(structureChol,m,sm); Statik.add(sm,structureMu,sm);
            for (int j=0; j<anzVar; j++) data[i][j] = sm[j] + rand.nextGaussian()*Math.sqrt(error[j]);
        }
        computeMoments();
        return data;
    }
  */  
    public double[][] createNonlinearData(int anzPersons)
    {
        if ((parWork == null) || (parWork.length!=anzFac+1)) parWork = new double[anzFac+1];

        evaluateMuAndSigma();
        
        this.anzPer = anzPersons;
        double[][] cholesky = Statik.choleskyDecompose(covVal);
        data = new double[anzPersons][anzVar];

        double[] m = new double[anzFac];
        double[] m2 = new double[anzFac];
        for (int i=0; i<anzPersons; i++)
        {
            for (int j=0; j<anzFac; j++) m[j] = rand.nextGaussian();
            Statik.multiply(cholesky,m,m2); Statik.add(m2,meanVal,m2);
            for (int j=0; j<anzFac; j++) parWork[j+1] = m2[j];
            for (int j=0; j<anzVar; j++) {parWork[0] = timeBasis[j]; data[i][j] = func.eval(parWork) + rand.nextGaussian()*Math.sqrt(errVal[j]);}
        }
        computeMoments();
        return data;
    }
    
    public double[][] evaluateStructure() {return evaluateStructure(null);}
    public double[][] evaluateStructure(double[] position)
    {
        if ((structure==null) || (structure.length!=anzVar) || (structure[0].length!=anzFac)) structure = new double[anzVar][anzFac];
        if ((meanVal == null) || (meanVal.length!=anzFac)) meanVal = new double[anzFac];
        if (position!=null) for (int i=0; i<anzFac; i++) if (meanPar[i]!=NOPARAMETER) meanVal[i] = position[meanPar[i]];
        if ((parWork == null) || (parWork.length!=anzFac+1)) parWork = new double[anzFac+1];
        for (int i=0; i<anzFac; i++) parWork[i+1] = meanVal[i]; 
        
        for (int i=0; i<anzVar; i++)
            for (int j=0; j<anzFac; j++) {parWork[0] = timeBasis[i]; structure[i][j] = func.evalDev(j+1, parWork);}
        return structure;
    }        

    public double[][] evaluateStructureDev(int par, double[][] erg) {return evaluateStructureDev(par, null, erg);}
    public double[][] evaluateStructureDev(int par, double[] position, double[][] erg)
    {
        if ((meanVal == null) || (meanVal.length!=anzFac)) meanVal = new double[anzFac];
        if (position!=null) for (int i=0; i<anzFac; i++) if (meanPar[i]!=NOPARAMETER) meanVal[i] = position[meanPar[i]];
        if ((parWork == null) || (parWork.length!=anzFac+1)) parWork = new double[anzFac+1];
        if ((devRow1 == null) || (devRow1.length!=2)) devRow1 = new int[2];
        for (int i=0; i<anzFac; i++) parWork[i+1] = meanVal[i]; 
        devRow1[0] = -1; for (int i=0; i<anzFac; i++) if (meanPar[i] == par) devRow1[0] = i+1;
        if (devRow1[0]==-1) {for (int i=0; i<anzVar; i++) for (int j=0; j<anzFac; j++) erg[i][j] = 0;}
        else {
            for (int i=0; i<anzVar; i++)
                for (int j=0; j<anzFac; j++) {parWork[0] = timeBasis[i]; devRow1[1] = j+1; erg[i][j] = func.evalDev(devRow1, parWork);}
        }
        return erg;
    }

    public double[][] evaluateStructureDevDev(int par1, int par2, double[][] erg) {return evaluateStructureDevDev(par1, par2, null, erg);}
    public double[][] evaluateStructureDevDev(int par1, int par2, double[] position, double[][] erg)
    {
        if ((meanVal == null) || (meanVal.length!=anzFac)) meanVal = new double[anzFac];
        if (position!=null) for (int i=0; i<anzFac; i++) if (meanPar[i]!=NOPARAMETER) meanVal[i] = position[meanPar[i]];
        if ((parWork == null) || (parWork.length!=anzFac+1)) parWork = new double[anzFac+1];
        if ((devRow2 == null) || (devRow2.length!=3)) devRow2 = new int[3];
        for (int i=0; i<anzFac; i++) parWork[i+1] = meanVal[i]; 
        devRow2[0] = -1; devRow2[1] = -1; for (int i=0; i<anzFac; i++) {if (meanPar[i] == par1) devRow2[0] = i+1; if (meanPar[i] == par2) devRow2[1] = i+1;}
        if ((devRow2[0]==-1) || (devRow2[1]==-1)) {for (int i=0; i<anzVar; i++) for (int j=0; j<anzFac; j++) erg[i][j] = 0;} 
        else {
            for (int i=0; i<anzVar; i++)
                for (int j=0; j<anzFac; j++) {parWork[0] = timeBasis[i]; devRow2[2] = j+1; erg[i][j] = func.evalDev(devRow2, parWork);}
        }                
        return erg;
    }
    
    
    public double[][] evaluateSigma() {return evaluateSigma(null);}
    public double[][] evaluateSigma(double[] position) {
        if ((sigma==null) || (sigma.length!=anzVar) || (sigma[0].length!=anzVar)) sigma = new double[anzVar][anzVar]; 
        evaluateSigma(position, sigma);
        return sigma;
    }
    public void evaluateSigma(double[] position, double[][] erg)
    {
        if (position!=null) setParameter(position);
        if (position==null) position= getParameter();

        evaluateStructure(position);
        for (int i=0; i<anzVar; i++)
            for (int j=0; j<anzVar; j++)
            {
                double zwerg = 0;
                for (int k=0; k<anzFac; k++)
                    for (int l=0; l<anzFac; l++) 
                        zwerg += structure[i][k]*structure[j][l]*(covPar[k][l]==NOPARAMETER?covVal[k][l]:position[covPar[k][l]]);
                erg[i][j] = zwerg;                
            }
        for (int i=0; i<anzVar; i++) 
            erg[i][i] += (errPar[i]==NOPARAMETER?errVal[i]:position[errPar[i]]);
    }

    public double[] evaluateMu() {return evaluateMu(null);}
    public double[] evaluateMu(double[] position) {if ((mu==null) || (mu.length!=anzVar)) mu = new double[anzVar]; evaluateMu(position, mu); return mu;}
    public void evaluateMu(double[] position, double[] erg)
    {
        if (position!=null) for (int i=0; i<anzFac; i++) if (meanPar[i]!=NOPARAMETER) meanVal[i] = position[meanPar[i]];
        if ((parWork == null) || (parWork.length!=anzFac+1)) parWork = new double[anzFac+1];
        for (int i=0; i<anzFac; i++) parWork[i+1] = meanVal[i]; 
        for (int i=0; i<anzVar; i++) {parWork[0] = timeBasis[i]; erg[i] = func.eval(parWork);}
    }
    
    public double[][] evaluateFactorMatrix(double[] position) {double[][] erg = new double[anzFac][anzFac]; evaluateFactorMatrix(position, erg); return erg;}
    public void evaluateFactorMatrix(double[] position, double[][] erg)
    {
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++)
                erg[i][j] = (covPar[i][j]==NOPARAMETER?covVal[i][j]:position[covPar[i][j]]);
    }
    
    public double[][][] evaluateSigmaDev(double[][][] erg)
    {
        if ((structureWork==null) || (structureWork.length!=anzVar) || (structureWork[0].length!=anzFac)) structureWork = new double[anzVar][anzFac];
        if ((sigmaWork==null) || (sigmaWork.length!=anzVar) || (sigmaWork[0].length!=anzVar)) sigmaWork = new double[anzVar][anzVar];
        if ((structureWork2==null) || (structureWork2.length!=anzVar) || (structureWork2[0].length!=anzFac)) structureWork2 = new double[anzVar][anzFac];
        if ((structureTransWork==null) || (structureTransWork.length!=anzFac) || (structureTransWork[0].length!=anzVar)) structureTransWork = new double[anzFac][anzVar];
        if ((erg==null) || (erg.length!=anzPar) || (erg[0].length!=anzVar) || (erg[0][0].length!=anzVar) ) erg = new double[anzPar][anzVar][anzVar];
        for (int i=0; i<anzPar; i++) for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) erg[i][j][k] = 0;
        
        for (int i=0; i<anzVar; i++)
            for (int j=0; j<anzVar; j++)
                for (int k=0; k<anzFac; k++)
                    for (int l=0; l<anzFac; l++)
                        if (covPar[k][l]!=NOPARAMETER) erg[covPar[k][l]][i][j] += structure[i][k]*structure[j][l];
        for (int i=0; i<anzVar; i++)
            if (errPar[i]!=NOPARAMETER) erg[errPar[i]][i][i] += 1;
        for (int i=0; i<meanPar.length; i++)
            if (meanPar[i]!=NOPARAMETER) {
                evaluateStructureDev(meanPar[i], structureWork);
                Statik.transpose(structure, structureTransWork);
                Statik.multiply(structureWork, covVal, structureWork2);
                Statik.multiply(structureWork2, structureTransWork, sigmaWork);
                for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) erg[meanPar[i]][j][k] += sigmaWork[j][k] + sigmaWork[k][j];
            }
        return erg;
    }
    
    public double[][] evaluateMuDev(double[][] erg)
    {
        if (erg==null) erg = new double[anzPar][anzVar]; 
        if ((parWork == null) || (parWork.length!=anzFac+1)) parWork = new double[anzFac+1];
        for (int i=0; i<anzFac; i++) parWork[i+1] = meanVal[i]; 

        for (int i=0; i<meanPar.length; i++)
            if (meanPar[i]!=NOPARAMETER) 
                for (int j=0; j<anzVar; j++) {parWork[0] = timeBasis[j]; erg[meanPar[i]][j] = func.evalDev(i+1,parWork);}

        return erg;
    }
    
    public double[] evaluateMuDevDev(int par1, int par2, double[] erg)
    {
        if (erg==null) erg = new double[anzFac]; 
        for (int i=0; i<anzVar; i++) erg[i] = 0;
        if ((parType[par1]!=parameterType.mean) || (parType[par2]!=parameterType.mean)) return erg;
        if ((devRow1 == null) || (devRow1.length!=2)) devRow1 = new int[2];
        devRow1[0] = parPos[par1][0]+1; devRow1[1] = parPos[par2][0]+1;
        if ((parWork == null) || (parWork.length!=anzFac+1)) parWork = new double[anzFac+1];
        for (int i=0; i<anzFac; i++) parWork[i+1] = meanVal[i]; 

        for (int i=0; i<anzVar; i++)
        {
            parWork[0] = timeBasis[i]; erg[i] = func.evalDev(devRow1, parWork);
        }

        return erg;
    }

    public boolean factorPositiveDefinite(double[] position)
    {
        if ((facCovWork==null) || (facCovWork.length!=anzVar) || (facCovWork[0].length!=anzVar)) facCovWork = new double[anzVar][anzVar]; 
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++)
                facCovWork[i][j] = (covPar[i][j]==NOPARAMETER?covVal[i][j]:position[covPar[i][j]]);
        return (Statik.determinantOfPositiveDefiniteMatrix(facCovWork) != -1); 
    }
    
    public void raiseVarianceParameters(double[] position)
    {
        for (int i=0; i<anzFac; i++)
        	if (covPar[i][i]!=NOPARAMETER) position[covPar[i][i]] = Math.abs(position[covPar[i][i]]) + 10;
                
        for (int i=0; i<errPar.length; i++)
            if (errPar[i] != NOPARAMETER) position[errPar[i]] = Math.abs(position[errPar[i]]) + 10;
    }
    
    public double evaluateLeastSquareIndex(double[][] observedCovariances, double[] observedMeans, double[] position)
    {
        double[][] sigma = evaluateSigma(position);
        double[] mu = evaluateMu(position);
        double erg = 0;
        for (int i=0; i<anzVar; i++)
            for (int j=0; j<anzVar; j++) erg += (sigma[i][j] - observedCovariances[i][j])*(sigma[i][j] - observedCovariances[i][j]);
        for (int i=0; i<anzVar; i++) erg += (mu[i] - observedMeans[i])*(mu[i] - observedMeans[i]);
        return erg;
    }

    public void computeLeastSquaresDerivatives(double[] value, boolean recomputeMuAndSigma) {
        if ((sigmaWork==null) || (sigmaWork.length!=anzVar) || (sigmaWork[0].length!=anzVar)) sigmaWork = new double[anzVar][anzVar];
        if ((sigmaWork2==null) || (sigmaWork2.length!=anzVar) || (sigmaWork2[0].length!=anzVar)) sigmaWork2 = new double[anzVar][anzVar];
        if ((sigmaDev==null) || (sigmaDev.length!=anzPar) || (sigmaDev[0].length!=anzVar) || (sigmaDev[0][0].length!=anzVar) ) sigmaDev = new double[anzPar][anzVar][anzVar];
        if ((muDev==null) || (muDev.length!=anzPar) || (muDev[0].length!=anzVar)) muDev = new double[anzPar][anzVar];
        if ((muWork==null) || (muWork.length!=anzVar)) muWork = new double[anzVar];
        if ((muWork2==null) || (muWork2.length!=anzVar)) muWork2 = new double[anzVar];
        if ((lsD==null) || (lsD.length!=anzPar)) lsD = new double[anzPar];
        if ((lsDD==null) || (lsDD.length!=anzVar) || (lsDD[0].length!=anzVar)) lsDD = new double[anzPar][anzPar];
        
        if (value != null) setParameter(value);
        if (recomputeMuAndSigma) {evaluateMuAndSigma(value); evaluateStructure(value);}
        
        evaluateMuDev(muDev);
        evaluateSigmaDev(sigmaDev);
        ls = 0;
        for (int i=0; i<anzVar; i++)
            for (int j=0; j<anzVar; j++) {sigmaWork[i][j] = sigma[i][j] - dataCov[i][j]; ls += 0.5*sigmaWork[i][j]*sigmaWork[i][j];}
        for (int i=0; i<anzVar; i++) {muWork[i] = mu[i] - dataMean[i]; ls += 0.5*muWork[i]*muWork[i];}
        
        for (int p=0; p<anzPar; p++)
        {
            lsD[p] = 0;
            if (parType[p] == parameterType.mean)
            {
                for (int i=0; i<anzVar; i++) lsD[p] += muDev[p][i]*muWork[i];
            }
            if ((parType[p] == parameterType.facCov) || (parType[p] == parameterType.mean))
            {
                for (int i=0; i<anzVar; i++)
                    for (int j=0; j<anzVar; j++)
                        lsD[p] += sigmaDev[p][i][j] * sigmaWork[i][j];
            }
            if (parType[p] == parameterType.err)
                for (int i=0; i<anzVar; i++) lsD[p] += sigmaWork[i][i];
            for (int p2l=p; p2l<anzPar; p2l++)
            {
                int p1 = p2l, p2 = p;
                
                lsDD[p1][p2] = 0;
                // contribution from the mu vector
                if ((parType[p1] == parameterType.mean) && (parType[p2] == parameterType.mean))
                {
                    evaluateMuDevDev(p1,p2,muWork2);
                    for (int i=0; i<anzVar; i++)
                        lsDD[p1][p2] += muWork2[i]*muWork[i] + muDev[p1][i]*muDev[p2][i];
                }

                if (((parType[p1] == parameterType.facCov) || (parType[p1] == parameterType.mean) || (parType[p1] == parameterType.err)) &&
                    ((parType[p2] == parameterType.facCov) || (parType[p2] == parameterType.mean) || (parType[p2] == parameterType.err))) 
                {
                    // contribution from sigmaWorkDevDev (only if both parameters are weights on the regression edges)
                    if ((parType[p1] == parameterType.mean) && (parType[p2] == parameterType.mean)) {
                        computeMatrixTimesSigmaDevDev(p1, p2, null, sigmaWork2);
                        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) lsDD[p1][p2] += sigmaWork2[i][j]*sigmaWork[i][j]; 
                    }
                    for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) lsDD[p1][p2] += sigmaDev[p1][i][j]*sigmaDev[p2][i][j]; 
                }

                lsDD[p2][p1] = lsDD[p1][p2];
            }
        }
    }
    
    public double computeLogLikelihood(double[] value)
    {
        if ((sigmaWork==null) || (sigmaWork.length!=anzVar) || (sigmaWork[0].length!=anzVar)) sigmaWork = new double[anzVar][anzVar];
        if ((sigInv==null) || (sigInv.length!=anzVar) || (sigInv[0].length!=anzVar)) sigInv = new double[anzVar][anzVar];

        evaluateMuAndSigma(value);
        evaluateStructure(value);
        sigmaDet = Statik.invert(sigma,sigInv, sigmaWork);
        if ((Statik.determinantOfPositiveDefiniteMatrix(sigma)==-1) && (sigmaDet > 0))
            sigmaDet = Double.NaN;
        
        ll = anzPer*anzVar*LNTWOPI + anzPer*Math.log(sigmaDet);
        for (int i=0; i<anzVar; i++) 
            for (int j=0; j<anzVar; j++)
                ll += sigInv[i][j]*(xBiSum[i][j] - anzPer*mu[i]*dataMean[j] - anzPer*mu[j]*dataMean[i] + anzPer*mu[i]*mu[j]);
        
        return ll;
    }
    
    public void computeLogLikelihoodDerivatives(double[] value, boolean recomputeMuAndSigma) {
        if ((sigmaWork==null) || (sigmaWork.length!=anzVar) || (sigmaWork[0].length!=anzVar)) sigmaWork = new double[anzVar][anzVar];
        if ((sigmaWork2==null) || (sigmaWork2.length!=anzVar) || (sigmaWork2[0].length!=anzVar)) sigmaWork2 = new double[anzVar][anzVar];
        if ((sigmaWork3==null) || (sigmaWork3.length!=anzVar) || (sigmaWork3[0].length!=anzVar)) sigmaWork3 = new double[anzVar][anzVar];
        if ((sigmaWork4==null) || (sigmaWork4.length!=anzVar) || (sigmaWork4[0].length!=anzVar)) sigmaWork4 = new double[anzVar][anzVar];
        if ((sigInv==null) || (sigInv.length!=anzVar) || (sigInv[0].length!=anzVar)) sigInv = new double[anzVar][anzVar];
        if ((sigmaDev==null) || (sigmaDev.length!=anzPar) || (sigmaDev[0].length!=anzVar) || (sigmaDev[0][0].length!=anzVar) ) sigmaDev = new double[anzPar][anzVar][anzVar];
        if ((muDev==null) || (muDev.length!=anzPar) || (muDev[0].length!=anzVar)) muDev = new double[anzPar][anzVar];
        if ((muWork==null) || (muWork.length!=anzVar)) muWork = new double[anzVar];
        if ((llD==null) || (llD.length!=anzPar)) llD = new double[anzPar];
        if ((llDD==null) || (llDD.length!=anzVar) || (llDD[0].length!=anzVar)) llDD = new double[anzPar][anzPar];
        
        if (value != null) setParameter(value);
        if (recomputeMuAndSigma) evaluateMuAndSigma(value);
        
        sigmaDet = Statik.invert(sigma,sigInv, sigmaWork);
        if ((Statik.determinantOfPositiveDefiniteMatrix(sigma)==-1) && (sigmaDet > 0))
            sigmaDet = Double.NaN;
        
        ll = anzPer*anzVar*LNTWOPI + anzPer*Math.log(sigmaDet);
        for (int i=0; i<anzVar; i++) 
            for (int j=0; j<anzVar; j++)
                ll += sigInv[i][j]*(xBiSum[i][j] - anzPer*mu[i]*dataMean[j] - anzPer*mu[j]*dataMean[i] + anzPer*mu[i]*mu[j]);

        evaluateMuDev(muDev);
        evaluateSigmaDev(sigmaDev);     // will be sigmaInv * sigmaDev hereafter.
        for (int i=0; i<anzPar; i++)
        {
            Statik.multiply(sigInv, sigmaDev[i], sigmaWork);
            Statik.copy(sigmaWork, sigmaDev[i]);
        }
        
        for (int p=0; p<anzPar; p++)
        {
            llD[p] = 0.0;
            if (parType[p] == parameterType.mean)
            {
                for (int i=0; i<anzVar; i++)
                    for (int j=0; j<anzVar; j++) llD[p] -= 2*muDev[p][i]*sigInv[i][j]*anzPer*(dataMean[j] - mu[j]);
            } 
            double trace = 0;
            for (int i=0; i<anzVar; i++) trace += sigmaDev[p][i][i];
            llD[p] += trace*anzPer;
            Statik.multiply(sigmaDev[p], sigInv, sigmaWork2);
            for (int i=0; i<anzVar; i++) 
                for (int j=0; j<anzVar; j++) llD[p] -= sigmaWork2[i][j]*(xBiSum[i][j] - anzPer*mu[i]*dataMean[j] - anzPer*mu[j]*dataMean[i] + anzPer*mu[i]*mu[j]);
        }
        
        for (int p1=0; p1<anzPar; p1++)
            for (int p2=p1; p2<anzPar; p2++)
            {
                llDD[p1][p2] = 0;
                // Term 2 and 4 in Eq. (13) of von Oertzen, Ghisletta, Lindenberger (2009)
                if ((parType[p1]==parameterType.mean) && (parType[p2]==parameterType.mean))
                {
                    evaluateMuDevDev(p1, p2, muWork);
                    for (int i=0; i<anzVar; i++)
                        for (int j=0; j<anzVar; j++)
                            llDD[p1][p2] += -2*muWork[i]*sigInv[i][j]*anzPer*(dataMean[j] - mu[j]) + 2*anzPer*muDev[p1][i]*sigInv[i][j]*muDev[p2][j];
                }
                // Term 3 in Eq. (13) of von Oertzen, Ghisletta, Lindenberger (2009)
                if (parType[p1]==parameterType.mean)
                {
                    Statik.multiply(sigmaDev[p2], sigInv, sigmaWork);
                    for (int i=0; i<anzVar; i++)
                        for (int j=0; j<anzVar; j++)
                            llDD[p1][p2] += 2*muDev[p1][i]*sigmaWork[i][j]*anzPer*(dataMean[j]-mu[j]);
                }
                // Term 5 in Eq. (13) of von Oertzen, Ghisletta, Lindenberger (2009)
                if (parType[p2]==parameterType.mean)
                {
                    Statik.multiply(sigmaDev[p1], sigInv, sigmaWork);
                    for (int i=0; i<anzVar; i++)
                        for (int j=0; j<anzVar; j++)
                            llDD[p1][p2] += 2*muDev[p2][i]*sigmaWork[i][j]*anzPer*(dataMean[j]-mu[j]);
                }
                
                // Term 1,6,7 and 8 in Eq. (13) of von Oertzen, Ghisletta, Lindenberger (2009)
                computeSigmaInvDevDev(p1, p2, sigmaWork3);
                Statik.multiply(sigmaDev[p1], sigmaDev[p2], sigmaWork4);
                double trace = 0;
                for (int i=0; i<anzVar; i++) trace += sigmaWork3[i][i] - sigmaWork4[i][i];
                llDD[p1][p2] += trace*anzPer;
                Statik.multiply(sigmaWork4, sigInv, sigmaWork);
                Statik.multiply(sigmaWork3, sigInv, sigmaWork2);
                for (int i=0; i<anzVar; i++)
                    for (int j=0; j<anzVar; j++)
                        llDD[p1][p2] += (2*sigmaWork[i][j] - sigmaWork2[i][j])*(xBiSum[i][j] - anzPer*mu[i]*dataMean[j] - anzPer*mu[j]*dataMean[i] + anzPer*mu[i]*mu[j]);

                llDD[p2][p1] = llDD[p1][p2];
            }
    }

    protected void computeMatrixTimesMuDev(int par, double[][] matrix, double[] erg) {
        for (int i=0; i<anzVar; i++) erg[i] = 0;
        if (parType[par]==parameterType.mean) {
            if ((parWork == null) || (parWork.length!=anzFac+1)) parWork = new double[anzFac+1];
            if ((muWork==null) || (muWork.length!=anzVar)) muWork = new double[anzVar];
            for (int i=0; i<anzFac; i++) parWork[i+1] = meanVal[i]; 

            for (int j=0; j<anzVar; j++) {parWork[0] = timeBasis[j]; erg[j] = func.evalDev(parPos[par][0]+1,parWork);}

            if (matrix==null) {
                Statik.multiply(matrix, erg, muWork);
                for (int i=0; i<anzVar; i++) erg[i] = muWork[i];
            }
        }
    }
    
    protected void computeMatrixTimesSigmaDev(int par, double[][] matrix, double[][] erg) {
        if (parType[par]==parameterType.mean)
        {
            if ((structureWork==null) || (structureWork.length!=anzVar) || (structureWork[0].length!=anzFac)) structureWork = new double[anzVar][anzFac];
            if ((sigmaWork==null) || (sigmaWork.length!=anzVar) || (sigmaWork[0].length!=anzVar)) sigmaWork = new double[anzVar][anzVar];
            if ((structureWork2==null) || (structureWork2.length!=anzVar) || (structureWork2[0].length!=anzFac)) structureWork2 = new double[anzVar][anzFac];
            if ((structureTransWork==null) || (structureTransWork.length!=anzFac) || (structureTransWork[0].length!=anzVar)) structureTransWork = new double[anzFac][anzVar];

            evaluateStructureDev(par, structureWork);
            Statik.transpose(structure, structureTransWork);
            Statik.multiply(structureWork, covVal, structureWork2);
            Statik.multiply(structureWork2, structureTransWork, sigmaWork);
            for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) erg[j][k] = sigmaWork[j][k] + sigmaWork[k][j];
            if (matrix!=null)
            {
                Statik.multiply(matrix, erg, sigmaWork);
                Statik.copy(sigmaWork,erg);
            }
        }
        if (parType[par]==parameterType.facCov)
        {
            if ((sigmaWork4==null) || (sigmaWork4.length!=anzVar) || (sigmaWork4[0].length!=anzVar)) sigmaWork4 = new double[anzVar][anzVar];
            for (int i=0; i<anzVar; i++) for (int j=i; j<anzVar; j++) 
            {
                sigmaWork4[i][j] = sigmaWork4[j][i] = structure[i][parPos[par][0]] * structure[j][parPos[par][1]] + 
                      (parPos[par][0]!=parPos[par][1]?structure[j][parPos[par][0]] * structure[i][parPos[par][1]]:0);
            }
            if (matrix==null) Statik.copy(sigmaWork4,erg); else Statik.multiply(matrix, sigmaWork4, erg);            
        }
        if (parType[par]==parameterType.err)
        {
            for (int i=0; i<anzVar;  i++) 
                for (int j=0; j<anzVar; j++) 
                    erg[i][j] = (errPar[j]!=par?0:(matrix==null?(i==j?1:0):matrix[i][j]));
        }
    }

    protected void computeMatrixTimesSigmaDevDev(int par1, int par2, double[][] matrix, double[][] erg) {
        for (int i=0; i<anzVar; i++) 
            for (int j=0; j<anzVar; j++) erg[i][j] = 0;
        if ((parType[par1] == parameterType.err) || (parType[par2]  == parameterType.err)) return;
        if ((parType[par1] == parameterType.mean) || (parType[par2] == parameterType.mean))
        {
            if ((structureWork==null) || (structureWork.length!=anzVar) || (structureWork[0].length!=anzFac)) structureWork = new double[anzVar][anzFac];
            if ((sigmaWork==null) || (sigmaWork.length!=anzVar) || (sigmaWork[0].length!=anzVar)) sigmaWork = new double[anzVar][anzVar];
            if ((sigmaWork2==null) || (sigmaWork2.length!=anzVar) || (sigmaWork2[0].length!=anzVar)) sigmaWork2 = new double[anzVar][anzVar];
            if ((structureWork2==null) || (structureWork2.length!=anzVar) || (structureWork2[0].length!=anzFac)) structureWork2 = new double[anzVar][anzFac];
            if ((structureTransWork==null) || (structureTransWork.length!=anzFac) || (structureTransWork[0].length!=anzVar)) structureTransWork = new double[anzFac][anzVar];
            for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) sigmaWork[i][j] = 0;
            
            if ((parType[par1] == parameterType.mean) && (parType[par2] == parameterType.mean))
            {
                evaluateStructureDev(par1, structureWork2);
                Statik.multiply(structureWork2, covVal, structureWork);
                evaluateStructureDev(par2, structureWork2);
                Statik.transpose(structureWork2, structureTransWork);
                Statik.multiply(structureWork, structureTransWork, sigmaWork);

                evaluateStructureDevDev(par1, par2, structureWork2);
                Statik.multiply(structureWork2, covVal, structureWork);
                Statik.transpose(structure, structureTransWork);
                Statik.multiply(structureWork, structureTransWork, sigmaWork2);
                Statik.add(sigmaWork, sigmaWork2, sigmaWork);
            } else
            {
                if (parType[par2] == parameterType.mean) {int t = par2; par2 = par1; par1 = t;}
                evaluateStructureDev(par1, structureWork);
                for (int i=0; i<anzVar; i++)
                {
                    double v1 = structureWork[i][parPos[par2][0]];
                    double v2 = structureWork[i][parPos[par2][1]];
                    for (int j=0; j<anzFac; j++) structureWork[i][j] = 0; 
                    structureWork[i][parPos[par2][1]] = v1;
                    structureWork[i][parPos[par2][0]] = v2;
                }
                Statik.transpose(structure, structureTransWork);
                Statik.multiply(structureWork, structureTransWork, sigmaWork2);
                Statik.add(sigmaWork, sigmaWork2, sigmaWork);
            }
            if (matrix==null)
            {
                for (int i=0; i<anzVar; i++)
                    for (int j=0; j<anzVar; j++) erg[i][j] = sigmaWork[i][j] + sigmaWork[j][i];
            } else {
                for (int i=0; i<anzVar; i++)
                    for (int j=0; j<anzVar; j++) sigmaWork2[i][j] = sigmaWork[i][j] + sigmaWork[j][i];
                Statik.multiply(matrix,sigmaWork2, erg);
            }
        }
    }
    
    public LinearApproximationModel copy() {
        return new LinearApproximationModel(this);
    }
    public LinearApproximationModel removeObservation(int obs) {
        double[] newTimeBasis = new double[timeBasis.length-1];
        for (int i=0; i<newTimeBasis.length; i++) newTimeBasis[i] = timeBasis[(i>=obs?i+1:i)];
        return new LinearApproximationModel(this.func, newTimeBasis);
    }
    
    public void evaluateMuAndSigma(double[] values) {
        evaluateSigma(values);
        evaluateMu(values);
    }
    public double getParameter(int nr) {
        return getParameterValue(nr);
    }
    public boolean isErrorParameter(int nr) {
        boolean erg = false;
        for (int i=0; i<errPar.length; i++) if (errPar[i] == nr) erg = true;
        return erg;
    }
    
    public boolean setParameter(int nr, double value) {
        return setParameterValue(nr,value);
    }
    
    public double sigmaSquaredError(double[] position)
    {
        double[][] sigma = evaluateSigma(position);
        double error = 0;
        for (int i=0; i<dataCov.length; i++)
            for (int j=0; j<dataCov[i].length; j++) error += (sigma[i][j]-dataCov[i][j])*(sigma[i][j]-dataCov[i][j]);
        return error;
    }
    
    public boolean significantlyDifferent(int parameter, double value, double compare)
    {
        double[][] fisherInv = Statik.invert(fisherInformationMatrix);
        double v = Math.abs(value-compare) / Math.sqrt(fisherInv[parameter][parameter]);
        return (v > 1.645);
    }
    public static void main(String args[])
    {
        int t = 16; 
        double[] timeBasis = new double[t]; for (int i=0; i<t; i++) timeBasis[i] = 30+i;
        LinearApproximationModel model = new LinearApproximationModel(exponentialDecline, timeBasis, 
                new double[]{30,0.05,20}, 
                new double[][]{{20,0,0},{0,0.01,0},{0,0,20}},
                30*(1-0.8)/0.8
        );
//        model.fixParameter(0); model.fixParameter(0); model.fixParameter(0); 
        double[] start = model.getParameters();
//        double[][] data1 = model.createNonlinearData(10000);
        System.out.println("Mean Nonlinear = "+Statik.matrixToString(model.dataMean));
        System.out.println("Cov  Nonlinear = \r\n"+Statik.matrixToString(model.dataCov));
//        double[][] data2 = model.createData(10000);
        System.out.println("Mean approxim. = "+Statik.matrixToString(model.dataMean));
        System.out.println("Cov  approxim. = \r\n"+Statik.matrixToString(model.dataCov));
        // let's be cheating...
//        Statik.copy(model.sigma,model.dataCov); Statik.copy(model.mu,model.dataMean);
//        for (int i=0; i<model.anzVar; i++) model.xsum[i] = model.dataMean[i]*model.anzPer; 
//        for (int i=0; i<model.anzVar; i++) for (int j=0; j<model.anzVar; j++) 
//            model.xBiSum[i][j] = model.dataCov[i][j]*(model.anzPer-1)+model.anzPer*model.dataMean[i]*model.dataMean[j]; 
        
        model.evaluateMuAndSigma();
        System.out.println("Theoretical me = "+Statik.matrixToString(model.mu));
        System.out.println("Theoretical cov= \r\n"+Statik.matrixToString(model.sigma));
        
        double[] testpoint = new double[]{20.2, 0.06, 20.1, 19.8, 1.0, 18.8, 1.2, 0.5, -0.8, 7};
//        double[] testpoint = new double[]{19.8, 1.0, 18.8, 1.2, 0.5, -0.8, 7};
        model.computeLogLikelihoodDerivatives(testpoint); System.out.println("off = "+Statik.matrixToString(model.llD));
        model.computeLogLikelihoodDerivatives(start); System.out.println("start = "+Statik.matrixToString(model.llD));
        double[] est = model.estimateML(start);
        model.computeLogLikelihoodDerivatives(est); System.out.println("hit = "+Statik.matrixToString(model.llD));

        
        System.out.println("Population Values = "+Statik.matrixToString(start));
        System.out.println("Estimation Values = "+Statik.matrixToString(est));
        System.out.println("Population -2ll   = "+model.computeLogLikelihood(start));
        System.out.println("Estimation -2ll   = "+model.computeLogLikelihood(est));
        
        System.out.println();
        model.testDerivatives(System.out, start, 0.000000001);

        double[] pos = new double[]{5,30.5, 0.04, 20.2};
        /*
        AnalyticalFunction numExp = exponentialDecline.numericalCopy();
        System.out.println("Value : "+numExp.eval(pos)+" : "+exponentialDecline.eval(pos));
        for (int i=0; i<4; i++)
        {
            System.out.println("1st("+i+") : "+numExp.evalDev(i,pos)+" : "+exponentialDecline.evalDev(i,pos)+" = "+Math.abs(numExp.evalDev(i,pos)-exponentialDecline.evalDev(i,pos)));
            for (int j=0; j<4; j++) {
                System.out.println("2nd("+i+","+j+") : "+numExp.evalDev(new int[]{i,j},pos)+" : "+exponentialDecline.evalDev(new int[]{i,j},pos)+" = "+Math.abs(numExp.evalDev(new int[]{i,j},pos)-exponentialDecline.evalDev(new int[]{i,j},pos)));
                for (int k=0; k<4; k++) {
                    System.out.println("3rd("+i+","+j+","+k+") : "+numExp.evalDev(new int[]{i,j,k},pos)+" : "+exponentialDecline.evalDev(new int[]{i,j,k},pos)+" = "+Math.abs(numExp.evalDev(new int[]{i,j,k},pos)-exponentialDecline.evalDev(new int[]{i,j,k},pos)));
                }
            }
        }
        */
        
        System.out.println("Selftest Exponential Function: ");
        exponentialDecline.selftest(pos);
        
        final LinearApproximationModel fthis = model;
        final int fv = 2;
        AnalyticalFunction mu = new AnalyticalFunction() {
            public int anzPar() {return fthis.anzPar;}
            public double eval(double[] pos) {fthis.evaluateMu(pos); return fthis.mu[fv];}
            public double evalDev(int p, double[] pos) {double[][] devMu = fthis.evaluateMuDev(null); return devMu[p][fv];}
            public double evalDev(int[] p, double[] pos) {
                double[] devdevMu=new double[fthis.anzVar]; 
                fthis.setParameter(pos); 
                fthis.evaluateMuDevDev(p[0], p[1], devdevMu); 
                return devdevMu[fv];
            }
        };
        
        System.out.println("Selftest Mu: ");
        mu.selftest(testpoint);

        final int fv1 = 2, fv2 = 2;
        AnalyticalFunction sigma = new AnalyticalFunction() {
            public int anzPar() {return fthis.anzPar;}
            public double eval(double[] pos) {fthis.evaluateSigma(pos); return fthis.sigma[fv1][fv2];}
            public double evalDev(int p, double[] pos) {double[][][] devSigma = fthis.evaluateSigmaDev(null); return devSigma[p][fv1][fv2];}
            public double evalDev(int[] p, double[] pos) {
                double[][] devdevSigma=new double[fthis.anzVar][fthis.anzVar]; 
                fthis.setParameter(pos); fthis.evaluateMuAndSigma();
                fthis.computeMatrixTimesSigmaDevDev(p[0], p[1], null, devdevSigma); 
                return devdevSigma[fv1][fv2];
            }
        };
        System.out.println("Selftest Sigma: ");
        sigma.selftest(testpoint);

        AnalyticalFunction struct = new AnalyticalFunction() {
            public int anzPar() {return fthis.anzPar;}
            public double eval(double[] pos) {fthis.evaluateStructure(pos); return fthis.structure[fv1][fv2];}
            public double evalDev(int p, double[] pos) {
                double[][] devStruct = new double[fthis.anzVar][fthis.anzFac]; 
                fthis.evaluateStructureDev(p, devStruct); return devStruct[fv1][fv2];}
            public double evalDev(int[] p, double[] pos) {
                double[][] devdevStruct=new double[fthis.anzVar][fthis.anzVar]; 
                fthis.setParameter(pos); 
                fthis.evaluateStructureDevDev(p[0], p[1], devdevStruct); 
                return devdevStruct[fv1][fv2];
            }
        };
        System.out.println("Selftest Struct: ");
        struct.selftest(testpoint);
        
        model.setParameter(start);
        model.createNonlinearData(500);
//        Statik.writeMatrix(model.data, "toPaolo_data.txt", '\t');
//        Statik.writeMatrix(model.structure, "toPaolo_structure.txt", '\t');
        
//        model.createData(1000);
        double[] estSym = model.estimateMLSupportedByPowerEquivalence(start, true);
        double[] estSymCorr = Statik.copy(estSym); for (int i=0; i<3; i++) estSymCorr[i] = start[i];
        System.out.println("Population Values      = "+Statik.matrixToString(start));
        System.out.println("PPML Estimation Values = "+Statik.matrixToString(estSym));
        System.out.println("PPML Estimation Corr   = "+Statik.matrixToString(estSymCorr));
        System.out.println("Population -2ll        = "+model.computeLogLikelihood(start));
        System.out.println("PPML Estimation -2ll   = "+model.computeLogLikelihood(estSym));
        System.out.println("Cor. PPML Estimation-2 = "+model.computeLogLikelihood(estSymCorr));

        // Model with fixed shift
        AnalyticalFunction expDec = exponentialDecline.fixParameter(3, 20);
        LinearApproximationModel model2 = new LinearApproximationModel(expDec, timeBasis, 
                new double[]{30,0.05}, 
                new double[][]{{20,0},{0,0.01}},
                30*(1-0.8)/0.8
        );
        start = model2.getParameter();
//        model2.createData(1000);
        model2.createNonlinearData(1000);
        estSym = model2.estimateMLSupportedByPowerEquivalence(start, false);
        estSymCorr = Statik.copy(estSym); for (int i=0; i<2; i++) estSymCorr[i] = start[i];
        System.out.println("\r\nWithout Shift: ");
        System.out.println("Population Values      = "+Statik.matrixToString(start));
        System.out.println("PPML Estimation Values = "+Statik.matrixToString(estSym));
        System.out.println("PPML Estimation Corr   = "+Statik.matrixToString(estSymCorr));
        try {
            System.out.println("Population -2ll        = "+model2.computeLogLikelihood(start));
            System.out.println("PPML Estimation -2ll   = "+model2.computeLogLikelihood(estSym));
            System.out.println("Cor. PPML Estimation-2 = "+model2.computeLogLikelihood(estSymCorr));
        } catch (Exception e)  {System.out.println("Runtime Exception: (Matrix has no inverse)");}

        // Model with fixed rate
        AnalyticalFunction expDec3 = exponentialDecline.fixParameter(2, 0.05);
        LinearApproximationModel model3 = new LinearApproximationModel(expDec3, timeBasis, 
                new double[]{30,20}, 
                new double[][]{{20,0},{0,20}},
                30*(1-0.8)/0.8
        );
        start = model3.getParameter();
//        model3.createData(1000);
        model3.createNonlinearData(500);
        Statik.writeMatrix(model3.data, "toPaolo_data.txt", '\t');
        estSym = model3.estimateMLSupportedByPowerEquivalence(start, false);
        estSymCorr = Statik.copy(estSym); for (int i=0; i<2; i++) estSymCorr[i] = start[i];
        System.out.println("\r\nWithout Rate: ");
        System.out.println("Population Values      = "+Statik.matrixToString(start));
        System.out.println("PPML Estimation Values = "+Statik.matrixToString(estSym));
        System.out.println("PPML Estimation Corr   = "+Statik.matrixToString(estSymCorr));
        try {
            System.out.println("Population -2ll        = "+model3.computeLogLikelihood(start));
            System.out.println("PPML Estimation -2ll   = "+model3.computeLogLikelihood(estSym));
            System.out.println("Cor. PPML Estimation-2 = "+model3.computeLogLikelihood(estSymCorr));
        } catch (Exception e)  {System.out.println("Runtime Exception: (Matrix has no inverse)");}
        
        model3.setParameter(start);
        model3.evaluateMuAndSigma();
        System.out.println("Approximate mu  = "+Statik.matrixToString(model3.mu));
        System.out.println("Approximate cov = "+Statik.matrixToString(model3.sigma));
        System.out.println("\r\nData mu  = "+Statik.matrixToString(model3.dataMean));
        System.out.println("Data cov = "+Statik.matrixToString(model3.dataCov));
        
        System.out.println("Finished.");
    }
}
