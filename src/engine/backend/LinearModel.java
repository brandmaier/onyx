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
 * Created on 26.04.2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package engine.backend;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import java.util.TreeSet;
import java.util.Vector;

//import arithmetik.AnalyticalFunction;

import engine.Statik;
//import sun.awt.SubRegionShowable;
//import tapping.DiscreteToContinuous;
//import tapping.DynamicSystemSimulator;


/**
 * @author timo
 * 
 * Restrictions from SEM: Structure must be constant, each entry of cov, err, and mean is either constant or a parameter.
 * All parameters must be used only once with exception of parameters only used in the error, which may appear multiple times.
 * 
 */
public class LinearModel extends Model
{
    public static enum parameterType {mean, facCov, err, inactive};
//    static Random rand = new Random();
    
    static int NOPARAMETER = -1;
    
    public int anzFac;
    
    public double[][] structure;
    public int[][] covPar;
    public double[][] covVal;
    public int[] meanPar;
    public double[] meanVal;
    public int[] errPar;
    public double[] errVal;
    
    // if not null, addErrorInfo is gives an additional additive term to the likelihood based on the value of the error parameters.
    // addErrorInfo contains triplets, first parameter number, second number of additional information units, and third summed squared of these units.
    double[][] addErrorInfo;
    
    // gives the position of the parameter (row, column); second is unused for mean and error, 1st <= 2nd for facCov
    int[][] parPos;
    
    // parameter types
    parameterType[] parType;
    
    // enumerates observations in different data rows that are identical; the first number is the multiplicity.
    int[][] equalObservations;
    
    // if true, the probability that independent observations described in equalObservations are equal is subtracted from the minus two log likelihood
    // and its derivatives.
    private boolean subtractEqualityLikelihoods = false;
    
    public double[][] fisherInformationMatrix;

    // Some working variables
    double[][] facCovWork, sigmaWork, sigmaWork2, sigmaWork3, sigmaWork4, structureWork, structureTransWork, parWork;
    double[] muWork, muWork2, posWork, p1w, p2w, cew, a1w, a2w;
    int[] sDevRow, sDevCol, cDevRow, cDevCol, mDev;
    
    public LinearModel(double[][] structure, int[][] covPar, double[][] covVal, int[] meanPar,
            double[] meanVal, int[] errPar, double[] errVal)
    {
        this.structure = structure; this.covPar = covPar; this.covVal = covVal; 
        this.meanPar = meanPar; this.meanVal = meanVal; this.errPar = errPar; this.errVal = errVal;
        
        anzFac = structure[0].length;
		anzVar = structure.length;

		collectParameterTypesAndPos();
		anzPer = 0; data = new double[0][];
		
		fisherInformationMatrix = new double[anzPar][anzPar];
    }
    public LinearModel(double[][] structure)
    {
        this.structure = structure;
        anzFac = structure[0].length;
        anzVar = structure.length;
        
        anzPar = 0;
        meanVal = new double[anzFac]; meanPar = new int[anzFac]; for (int i=0; i<anzFac; i++) meanPar[i] = anzPar++;
        covVal = new double[anzFac][anzFac]; covPar = new int[anzFac][anzFac];
        for (int i=0; i<anzFac; i++) covPar[i][i] = anzPar++;
        for (int i=0; i<anzFac; i++) for (int j=0; j<i; j++) covPar[i][j] = covPar[j][i] = anzPar++;
        errVal = new double[anzVar]; errPar = new int[anzVar];
        for (int i=0; i<anzVar; i++) errPar[i] = anzPar;
        anzPar++; 
		anzPer = 0; data = new double[0][];
		collectParameterTypesAndPos();
		fisherInformationMatrix = new double[anzPar][anzPar];
    }
    public LinearModel(double[][] structure, double[][] covVal, double[] meanVal, double errVal)
    {
        this(structure);
        this.covVal = covVal; this.meanVal = meanVal; this.errVal = new double[anzVar]; for (int i=0; i<anzVar; i++) this.errVal[i] = errVal;
    }
    public LinearModel(LinearModel toCopy)
    {
        anzFac = toCopy.anzFac;
        anzVar = toCopy.anzVar;
        anzPar = toCopy.anzPar;
        anzPer = toCopy.anzPer;

        structure = Statik.copy(toCopy.structure);
        meanVal = Statik.copy(toCopy.meanVal);
        meanPar = Statik.copy(toCopy.meanPar);
        covVal = Statik.copy(toCopy.covVal);
        covPar = Statik.copy(toCopy.covPar);
        errVal = Statik.copy(toCopy.errVal);
        errPar = Statik.copy(toCopy.errPar);
		data = (toCopy.data==null?null:Statik.copy(toCopy.data));
		computeMoments();
		fisherInformationMatrix = new double[anzPar][anzPar];
		collectParameterTypesAndPos();
    }
    
 /*   public LinearModel(LinearModelOld toCopy) {
        anzFac = toCopy.anzFac;
        anzVar = toCopy.anzVar;
        anzPar = toCopy.anzPar;
        anzPer = toCopy.anzPer;

        structure = Statik.copy(toCopy.structure);
        meanVal = Statik.copy(toCopy.facMeanVal);
        meanPar = Statik.copy(toCopy.facMeanPar);
        covVal = Statik.copy(toCopy.facCovVal);
        covPar = Statik.copy(toCopy.facCovPar);
        errVal = Statik.copy(toCopy.errVal);
        errPar = Statik.copy(toCopy.errPar);
        data = (toCopy.data==null?null:Statik.copy(toCopy.data));
        computeMoments();
        fisherInformationMatrix = new double[anzPar][anzPar];
        collectParameterTypesAndPos();
    }*/
    
    public LinearModel(double[][] structVal, int[][] covPar2,
            double[][] covVal2, int[] meanPar2, double[] meanVal2, int errPar, double errVal) {
        this(structVal, covPar2, covVal2, meanPar2, meanVal2, Statik.expandToArray(errPar, structVal.length), Statik.expandToArray(errVal, structVal.length));
        collectParameterTypesAndPos();
    }
    public static int[][] createSaturatedCovariance(int size, int startNr)
    {
        int[][] erg = new int[size][size];
        for (int i=0; i<size; i++) erg[i][i] = startNr++;
        for (int i=0; i<size; i++) for (int j=i+1; j<size; j++) erg[i][j] = erg[j][i] = startNr++;
        return erg;
    }
    
    public void collectParameterTypesAndPos() {setAnzParAndCollectParameter(0);}
    public void setAnzParAndCollectParameter(int newAnzPar)
    {
        if (newAnzPar == -1) newAnzPar = anzPar;
        int maxPar = maxParNumber(); anzPar = Math.max(maxPar+1, newAnzPar);
        
        parType = new parameterType[anzPar]; for (int i=0; i<anzPar; i++) parType[i] = parameterType.inactive;
        parPos = new int[anzPar][]; 
    
        for (int i=0; i<meanPar.length; i++) if (meanPar[i]!=NOPARAMETER) {parType[meanPar[i]] = parameterType.mean; parPos[meanPar[i]] = new int[]{i,0};}
        for (int i=0; i<anzFac; i++) for (int j=i; j<anzFac; j++) 
            if (covPar[i][j]!=NOPARAMETER) {parType[covPar[i][j]] = parameterType.facCov; parPos[covPar[i][j]] = new int[]{i,j};}
        for (int i=0; i<errPar.length; i++) if (errPar[i] != NOPARAMETER) {parType[errPar[i]] = parameterType.err; parPos[errPar[i]] = new int[]{i,0};}
        
        if (paraNames == null || paraNames.length != anzPar) inventParameterNames();
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
    
    public void removeParameterNumber(int nr) {
        for (int i=0; i<meanPar.length; i++) 
            if (meanPar[i] == nr) meanPar[i] = -1; else if (meanPar[i] > nr) meanPar[i]--; 
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++)
                if (covPar[i][j] == nr) covPar[i][j] = -1; else if (covPar[i][j] > nr) covPar[i][j]--;
        for (int i=0; i<errPar.length; i++)
            if (errPar[i] == nr) errPar[i] = -1; else if (errPar[i] > nr) errPar[i]--;
        collectParameterTypesAndPos();
        anzPar++;
    }
    
    /**
     * Computes the power for a simple two-latent-two-indicator model with middle edge weight x and errors err1 and err2.
     * 
     * @param var1		Real Variance of first latent
     * @param covar		Real Covariance of first to second latent
     * @param var2		Real Variance of second latent
     * @param var1Alt	Alternative Variance of first latent
     * @param covarAlt	Alternative Covariance of first to second latent
     * @param var2Alt	Alternative Variance of second latent
     * @param ADedge	Edge from first latent to second indicator
     * @param err1		Error Variance of first indicator
     * @param err2		Error Variance of second indicator
     * @param threshold	Threshold of significance
     */
    public static double powerOfTwoLatentTwoIndicator(double var1, double covar, double var2, double var1Alt, double covarAlt, double var2Alt, double ADedge, double err1, double err2, int N, double threshold, int additionalDF)
    {
        double s11 = var1+err1, s11Alt = var1Alt + err1;
        double s12 = ADedge*var1 + covar, s12Alt = ADedge*var1Alt+covarAlt;
        double s22 = ADedge*s12 + ADedge*covar + var2 + err2, s22Alt = ADedge*s12Alt + ADedge*covarAlt + var2Alt + err2;
        double det = s11*s22-s12*s12, detAlt = s11Alt*s22Alt - s12Alt*s12Alt;
        double c11 = Math.sqrt(s11), c11Alt = Math.sqrt(s11Alt);
        double c12 = s12 / c11, c12Alt = s12Alt / c11Alt;
        double c22 = Math.sqrt(det)/c11, c22Alt = Math.sqrt(detAlt)/c11Alt;
        
        double a = (-c11*c11*s22Alt+2*c12*c11*s12Alt-c12*c12*s11Alt)/detAlt+(c11*c11*s22-2*c11*c12*s12+c12*c12*s11)/det;
        double b = 2*c22*((c11*s12Alt - c12*s11Alt)/detAlt + (c12*s11 - c11*s12)/det );
        double c = c22*c22*(s11/det-s11Alt/detAlt);
        double d = Math.log(det/detAlt);
        
        System.out.println("a = "+a+", b = "+b+", c = "+c+", d = "+d);
        System.out.println("theoretical mean="+(-a-c-d)*N);
        
        double erg = 1.0-Statik.generalMultiDegreeTwoNormalDensity(-a,-b,-c,-d,N,threshold,0.01,additionalDF);
//        double erg = Statik.generalMultiDegreeTwoNormalDensityNumerical(-a,-b,-c,-d,N,10000,threshold);
        return erg;
    }
    
    /**
     * Converts a two-latent-many-indicator model to a two-latent-two-indicator model. Ignores all entries of eta, nu and errCovMat after andInd.
     * Destroys this arguments in the process. 
     * 
     * @param anzInd	Number of Indicators (all entries after this one ignored)
     * @param eta		Weights from first latent to indicators
     * @param nu		Weights from second latent to indicators
     * @param chol		Error Cholesky Matrix 
     * @return			three doubles as array: first error, second error, weight from first latent to second indicator. 
     */
    private static double[] convertTwoLatentToTwoLatentTwoIndicator(int anzInd, double[] eta, double[] nu, double[][] chol)
    {
        double t;
        int nuZero = -1;
        for (int i=0; i<anzInd; i++) if (nu[i]==0.0) nuZero = i;
        if (nuZero!=-1) 
        {
            t = nu[nuZero]; nu[nuZero] = nu[0]; nu[0] = t; 
            t = eta[nuZero]; eta[nuZero] = eta[0]; eta[0] = t;
            for (int i=0; i<anzInd; i++) {t = chol[nuZero][i]; chol[nuZero][i] = chol[0][i]; chol[0][i] = t;}
            for (int i=0; i<anzInd; i++) {t = chol[i][nuZero]; chol[i][nuZero] = chol[i][0]; chol[i][0] = t;}
        } else {
            eta[0] = eta[0] - eta[1] * nu[0]/nu[1];
            for (int i=0; i<anzInd; i++) chol[0][i] = chol[0][i] - chol[1][i] * nu[0]/nu[1];            
            nu[0] = 0; 
        }
        Statik.choleskyDecompose(Statik.multiply(chol,Statik.transpose(chol)),chol);
        chol[0][0] = chol[0][0] / eta[0];
        eta[0] = 1;
        // If all right, we should power equivalently have transformed the model such that nu[0] is zero and eta[0] = 1.
        
        if (chol[0][0]!=0.0)
        {
            for (int i=1; i<anzInd; i++)
            {
                double fak = chol[i][0] / chol[0][0];
                nu[i] = nu[i] - fak * nu[0];
                eta[i] = eta[i] - fak * eta[0];
                for (int j=0; j<anzInd; j++) chol[i][j] = chol[i][j] - fak * chol[0][j];
            }
        }
        // Now, all edges from the first error node to all inidicators but the first should be eliminated.
        if (anzInd == 2)
        {
            double e1 = chol[0][0]*chol[0][0];
            double e2 = chol[1][1]*chol[1][1]/(nu[1]*nu[1]);
            double x  = eta[1]/nu[1];
            return new double[]{e1,e2,x};
        } else {
            Statik.choleskyDecompose(Statik.multiply(chol,Statik.transpose(chol)),chol);
            double[] etadot = new double[anzInd-1]; for (int i=0; i<anzInd-1; i++) etadot[i] = eta[i+1];
            double[] nudot = new double[anzInd-1]; for (int i=0; i<anzInd-1; i++) nudot[i] = nu[i+1];
            double[][] choldot = new double[anzInd-1][anzInd-1];
            for (int i=0; i<anzInd-1; i++)
                for (int j=0; j<anzInd-1; j++) choldot[i][j] = chol[i+1][j+1];
            double[] zwerg = convertTwoLatentToTwoLatentTwoIndicator(anzInd-1,etadot,nudot,choldot);
            zwerg[0] = chol[0][0]*chol[0][0]*zwerg[0]/(chol[0][0]*chol[0][0]+zwerg[0]);
            return zwerg;
        }        
    }
    // Assumes a simple one-variable LGCM
    private LinearModel convertTwoLatentToTwoLatentTwoIndicator()
    {
        double[] eta = new double[anzVar], nu = new double[anzVar]; double[][] chol = new double[anzVar][anzVar];  
        for (int i=0; i<anzVar; i++) {eta[i] = structure[i][0]; nu[i] = structure[i][1]; chol[i][i] = Math.sqrt(errVal[i]);}
        double[] zwerg = convertTwoLatentToTwoLatentTwoIndicator(anzVar, eta, nu, chol);
        LinearModel erg = new LinearModel(new double[][]{{1,0},{zwerg[2],1}}, Statik.copy(covPar), Statik.copy(covVal), Statik.copy(meanPar),
        Statik.copy(meanVal), new int[]{-1,-1}, new double[]{zwerg[0],zwerg[1]});
        return erg;
    }
    
    /**
     * Computes the power for a simple two-latent-many-indicator model with edge weights lambda from first latent to indicators,
     * nu from second latent to indicator, errCovMat as covariance matrix of the errors and indCovMat and indCovMatAlt as 
     * the covariance matrices of the indicators and alternative, respectively.
     * 
     * @param anzInd	Number of indicators
     * @param eta		Weights from first latent to indicators
     * @param nu		Weiths from second latent ot indicators
     * @param errCovMat Error Covariance Matrix
     * @param indCovMat Indicator Covariance Matrix (true Model)
     * @param indCovMatAlt	Indicator Covariance Matrix (alternative Model)
     * @param threshold	significance threshold
     * @return			the probability to hit a -2ll difference higher than threshold with data from the true model.
     */
    public static double powerOfTwoLatent(int anzInd, double[] eta, double[] nu, double[][] errCovMat, double[][] indCovMat, double[][] indCovMatAlt, int N, double threshold, int df)
    {
        double[] zwerg = convertTwoLatentToTwoLatentTwoIndicator(anzInd, eta, nu, Statik.choleskyDecompose(errCovMat));
        return powerOfTwoLatentTwoIndicator(indCovMat[0][0],indCovMat[0][1],indCovMat[1][1],indCovMatAlt[0][0],indCovMatAlt[0][1],indCovMatAlt[1][1],zwerg[2],zwerg[0],zwerg[1],N,threshold,df);
    }
    
    /**
     * ASSUMES identical error in all observations.
     * 
     * Computes a matrix erg such that erg &* struct is upper right and the error structure is an identity matrix times sigma_err again.
     * 
     * @return
     */
    public LinearModel transformWithPowerEquivalence() {
        double[][] trans = computePowerEquivalenceTransformationMatrix();
        double[][] prod = Statik.multiply(trans,structure);
        double[][] newStruct = new double[anzFac][]; for (int i=0; i<anzFac; i++) newStruct[i] = prod[i];
        LinearModel erg = new LinearModel(this);
        erg.anzVar = anzFac; erg.structure = newStruct;
        double[] vec = new double[anzVar];
        double sqrsum = 0;
        for (int i=0; i<erg.data.length; i++) {
            vec = Statik.multiply(trans, erg.data[i]);
            for (int j=anzFac; j<anzVar; j++) sqrsum += vec[j]*vec[j];
            erg.data[i] = new double[anzFac];
            for (int j=0; j<anzFac; j++) erg.data[i][j] = vec[j];            
        }
        erg.computeMoments();
        double errvar = sqrsum / (anzPer*(anzVar-anzFac));
        erg.addErrorInfo = new double[][]{{errPar[0],anzPer*(anzVar-anzFac),sqrsum}};
        int[] newErrPar = new int[anzFac];
        double[] newErrVal = new double[anzFac];
        for (int i=0; i<anzFac; i++) {newErrPar[i] = errPar[0]; newErrVal[i] = errvar;}
        erg.errPar = newErrPar;
        erg.errVal = newErrVal;
        
        return erg;
    }

    
    /**
     * ASSUMES identical error in all observations.
     * 
     * Computes a matrix erg such that erg &* struct is upper right and the error structure is an identy matrix times sigma_err again.
     * 
     * @return
     */
    public double[][] computePowerEquivalenceTransformationMatrix()
    {
        double[][] work2 = new double[anzVar][anzVar], work3 = new double[anzVar][anzVar];

        double[][] erg = Statik.identityMatrix(anzVar);
        double[][] work = Statik.copy(structure);
        Statik.qrDecomposition(structure, erg, work);
//       for (int i=0; i<anzFac; i++)
//       {
//           double piv = work[i][i];
//           for (int j=0; j<anzFac; j++) {work[i][j] /= piv; erg[i][j] /= piv;}
//           for (int j=i+1; j<anzVar; j++)
//           {
//               double lead = work[j][i];
//               for (int k=0; k<anzFac; k++) {work[j][k] -= lead*work[i][k]; erg[j][k] -= lead*erg[i][k];} 
//           }
//       }
//       for (int i=anzFac-1; i>=0; i--)
//       {
//           for (int j=i-1; j>=0; j--)
//           {
//               double lead = work[j][i];
//               for (int k=0; k<anzFac; k++) {work[j][k] -= lead*work[i][k]; erg[j][k] -= lead*erg[i][k];}
//           }
//       }
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

    /**
     * ASSUMES same error parameter in all observations, and all covariance matrix parameters to be estimated.
     * 
     * Transforms the model into a minimal model and remaining error indicator. Then, the error and the covariance matrix are fit symbolically.
     * 
     * 
     * @param initial
     * @return
     */
    public double[] estimateMLFullCovarianceSupportedByPowerEquivalence() {return estimateMLFullCovarianceSupportedByPowerEquivalence(false);}
    public double[] estimateMLFullCovarianceSupportedByPowerEquivalence(boolean useDataDistribution)
    {
        double[][] trans = computePowerEquivalenceTransformationMatrix();
        double[][] prod = Statik.multiply(trans,structure);
        double[][] interStruct = new double[anzFac][]; for (int i=0; i<anzFac; i++) interStruct[i] = prod[i];
        double[][] prod2ul = Statik.invert(interStruct);
        double[][] prod2 = Statik.identityMatrix(anzVar);
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) prod2[i][j] = prod2ul[i][j];
        double[][] transnew = Statik.multiply(prod2, trans);
        
        double errvar;
        double[] mean = new double[anzFac];
        double[][] covD = new double[anzFac][anzFac];
        
        if (!useDataDistribution) {
            double[][] transData = new double[anzPer][anzVar];
            
            double sqrsum = 0;
            for (int i=0; i<anzPer; i++) {
                 transData[i] = Statik.multiply(transnew, data[i]);
                 for (int j=anzFac; j<anzVar; j++) sqrsum += transData[i][j]*transData[i][j];
            }
            errvar = sqrsum / (anzPer*(anzVar-anzFac));
            
            for (int i=0; i<anzPer; i++) 
                for (int j=0; j<anzFac; j++) mean[j] += transData[i][j];
            for (int i=0; i<anzFac; i++) mean[i] /= anzPer;
            for (int i=0; i<anzFac; i++) if (meanPar[i]==-1) mean[i] = meanVal[i];
            
            for (int i=0; i<anzPer; i++)
                for (int j=0; j<anzFac; j++)
                    for (int k=0; k<anzFac; k++) covD[j][k] += (transData[i][j]-mean[j])*(transData[i][k]-mean[k]);
            
            for (int i=0; i<anzFac; i++)
                for (int j=0; j<anzFac; j++) covD[i][j] /= (anzPer);
        } else {
            double[][] transCov = Statik.multiply(Statik.multiply(transnew,dataCov), Statik.transpose(transnew));
            double[] transMean = Statik.multiply(transnew, dataMean);
            
            errvar = 0; 
            for (int i=anzFac; i<anzVar; i++) errvar += transCov[i][i];
            errvar /= anzVar - anzFac;
            
            for (int i=0; i<anzFac; i++) if (meanPar[i]==-1) mean[i] = meanVal[i]; else mean[i] = transMean[i];
            for (int j=0; j<anzFac; j++)
                for (int k=0; k<anzFac; k++) covD[j][k] = transCov[j][k];
        }
        
        double[][] work = Statik.transpose(prod2ul);
        for (int i=0; i<anzFac; i++) 
            for (int j=0; j<anzFac; j++) work[i][j] *= errvar;
        
        double[][] subtractMatrix = Statik.multiply(prod2ul, work);
        
        double[] erg = new double[anzPar];
        if (errPar[0]!=-1) erg[errPar[0]] = errvar;
        for (int i=0; i<anzFac; i++) if (meanPar[i]!=-1) erg[meanPar[i]] = mean[i];
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<=i; j++) if (covPar[i][j]!=-1) erg[covPar[i][j]] = covD[i][j] - subtractMatrix[i][j];
        
        return erg;    
    }
    
    
    /**
     * ASSUMES same error parameter in all observations.
     * 
     * Transforms the model into a minimal model and remaining error indicator. Then, the error is fit and fixed in the remainder.
     * 
     * TODO: Call of estimate is still over SimpleLGM (to be changed, becomes deprecated)
     * TODO: Means are ignored.
     * 
     * @param initial
     * @return
     */
    public double[] estimateMLSupportedByPowerEquivalence(double[] initial)
    {
        if (initial == null) initial = estimateLS();
        double[][] trans = computePowerEquivalenceTransformationMatrix();
        double[][] prod = Statik.multiply(trans,structure);
        double[][] newStruct = new double[anzFac][]; for (int i=0; i<anzFac; i++) newStruct[i] = prod[i];
        LinearModel work = new LinearModel(this);
        work.anzVar = anzFac; work.structure = newStruct;
        double[] vec = new double[anzVar];
        double sqrsum = 0;
        for (int i=0; i<work.data.length; i++) {
            vec = Statik.multiply(trans, work.data[i]);
            for (int j=anzFac; j<anzVar; j++) sqrsum += vec[j]*vec[j];
            work.data[i] = new double[anzFac];
            for (int j=0; j<anzFac; j++) work.data[i][j] = vec[j];            
        }
        work.computeMoments();
        double errvar = sqrsum / (anzPer*(anzVar-anzFac)-1);
        work.fixParameter(errPar[0],errvar);
        
        double[] init = new double[initial.length-1];
        int j=0;
        for (int i=0; i<init.length; i++) if (j!=errPar[0]) init[i] = initial[j++];
        double[] zwerg = work.estimateML(init);
        double[] erg = new double[zwerg.length+1];
        j=0; 
        for (int i=0; i<erg.length; i++) if (i==errPar[0]) erg[i]=errvar; else erg[i] = zwerg[j++];
        

        return erg;
    }
    
    // temporary method creating bootstrap for Florian's data
    private void testBootstrapMLByPowerEquivalence()
    {
        double[][] Q = computePowerEquivalenceTransformationMatrix();
        double[][] newStruct = Statik.multiply(Q, structure);
        double[][] lambdaLatent = Statik.submatrix(newStruct, new int[]{0,1,2});
        double[][] LLInv = Statik.invert(lambdaLatent);
        double[][] transData = Statik.multiply(data, Statik.transpose(Q));

        int subN = anzPer;
        int N = anzPer;
        int trials = 100000;
        int[] nr = new int[subN]; 
        double[] mean = new double[3]; double[][] cov = new double[3][3];
        double[] numEst = new double[anzPar], anaEst = new double[anzPar];
        double[][] ergAna = new double[2][anzPar];
        double[][] ergNum = new double[2][anzPar];
        double ergAnaLL = 0, ergNumLL = 0;
        LinearModel work = new LinearModel(this);
        work.anzPer = subN; work.data = new double[subN][];

        long time = System.nanoTime();
        // for separate time measure, turn one method off.
        for (int rep=0; rep<trials; rep++)
        {
            for (int i=0; i<subN; i++) nr[i] = rand.nextInt(N);
            for (int i=0; i<3; i++) mean[i] = 0;
            for (int i=0; i<subN; i++) for (int j=0; j<3; j++) mean[j] += transData[nr[i]][j];
            for (int i=0; i<3; i++) mean[i] /= subN;
            double[] latentMean = Statik.multiply(LLInv, mean);

            double errvar = 0;
            for (int i=0; i<subN; i++) for (int j=3; j<anzVar; j++) 
                errvar += (transData[nr[i]][j]*transData[nr[i]][j]);
            errvar /= (subN*(anzVar-3));
            for (int i=0; i<3; i++) for (int j=0; j<3; j++) cov[i][j] = 0;
            for (int i=0; i<subN; i++) 
                for (int j=0; j<3; j++) for (int k=0; k<3; k++) cov[j][k] += (transData[nr[i]][j]-mean[j])*(transData[nr[i]][k]-mean[j]);
            for (int j=0; j<3; j++) for (int k=0; k<3; k++) cov[j][k] /= subN;
            for (int j=0; j<3; j++) cov[j][j] -= errvar;
            double[][] latentCov = Statik.multiply(LLInv, Statik.multiply(cov,Statik.transpose(LLInv)));
            
            int k=0; for (int i=0; i<3; i++) {anaEst[k] = latentMean[i]; k++;}
            for (int i=0; i<3; i++) for (int j=i; j<3; j++) {anaEst[k] = latentCov[i][j]; k++;}
            anaEst[k] = errvar; 

            for (int i=0; i<anzPar; i++) {ergAna[0][i] += anaEst[i]; ergAna[1][i] += anaEst[i]*anaEst[i];}

            for (int i=0; i<subN; i++) work.data[i] = data[nr[i]];
//            work.computeMoments();
//            numEst = work.estimateML(); double numLL = work.ll;
//            for (int i=0; i<anzPar; i++) {ergNum[0][i] += numEst[i]; ergNum[1][i] += numEst[i]*numEst[i];}

            work.setParameter(anaEst); work.evaluateMuAndSigma(); double anaLL = work.computeLogLikelihood(); 
            ergAnaLL += anaLL; 
//            ergNumLL += numLL;
        }
        time = System.nanoTime() - time;
        
        for (int i=0; i<anzPar; i++) 
        {
            ergAna[0][i] /= (double)trials; ergAna[1][i] = 1000*Math.sqrt(ergAna[1][i]/(double)trials - ergAna[0][i]*ergAna[0][i]); 
            ergNum[0][i] /= (double)trials; ergNum[1][i] = 1000*Math.sqrt(ergNum[1][i]/(double)trials - ergNum[0][i]*ergNum[0][i]); 
        }
        ergAnaLL /= trials; ergNumLL /= trials;
        
        System.out.println("Average LL (analytical): "+ergAnaLL);
        System.out.println("Average LL (numerical): "+ergNumLL);
        System.out.println("Average Estimate Analytical: "+Statik.matrixToString(ergAna[0]));
        System.out.println("Precision [1000] Analytical: "+Statik.matrixToString(ergAna[1]));
        System.out.println("Average Estimate Numerical : "+Statik.matrixToString(ergNum[0]));
        System.out.println("Precision [1000] Numerical : "+Statik.matrixToString(ergNum[1]));
        System.out.println("Overall Time "+time+" ns.");
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
    public boolean setParameter(int nr, double value)
    {
        boolean erg = false; 
        for (int i=0; i<anzFac; i++) 
            if (meanPar[i]==nr) {meanVal[i]=value; erg = true;}
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++) 
                if (covPar[i][j]==nr) {covVal[i][j]=value; erg = true;}
        for (int i=0; i<anzVar; i++)
            if (errPar[i]==nr) {errVal[i]=value; erg = true;}
        return erg;
    }
    
    public void setParameterValueAccordingToOtherModel(LinearModel reference, int nr, double value)
    {
        for (int i=0; i<anzFac; i++) 
            if (reference.meanPar[i]==nr) meanVal[i]=value;
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++) 
                if (reference.covPar[i][j]==nr) covVal[i][j]=value;
        for (int i=0; i<anzVar; i++)
            if (reference.errPar[i]==nr) errVal[i]=value;
    }
    public void setParameterValueAccordingToOtherModel(LinearModel reference, int nr)
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
        this.isIndirectData = false;
        computeMoments();
    }
    
    public double getParameter(int nr)
    {
        if (nr >= anzPar) throw new RuntimeException("Parameter "+nr+" existiert nicht.");
        for (int i=0; i<anzFac; i++) 
            if (meanPar[i]==nr) return meanVal[i];
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++) 
                if (covPar[i][j]==nr) return covVal[i][j];
        for (int i=0; i<anzVar; i++)
            if (errPar[i]==nr) return errVal[i];
        return MISSING;
    }
    
    public void computeMoments()
    {
        xsum = new double[anzVar];
        dataMean = new double[anzVar];
        if (data==null || data.length==0) return;
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

    public static LinearModel fixParameter(LinearModel in, int nr) {LinearModel erg = new LinearModel(in); erg.fixParameter(nr); return erg;}
    public static LinearModel fixParameter(LinearModel in, int nr, double value) {LinearModel erg = new LinearModel(in); erg.fixParameter(nr,value); return erg;}
    public void inactivateParameter(int nr)
    {
        for (int i=0; i<anzFac; i++)
            if (meanPar[i]==nr) meanPar[i] = NOPARAMETER;

        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++)
                if (covPar[i][j]==nr) covPar[i][j] = NOPARAMETER;

        for (int i=0; i<anzVar; i++)
            if (errPar[i]==nr) errPar[i] = NOPARAMETER;

        setAnzParAndCollectParameter(-1);
    }
    public static LinearModel picDatapoints(LinearModel in, int[] piced) {LinearModel erg = new LinearModel(in); erg.picDatapoints(piced); return erg;}
    public void picDatapoints(int[] piced)
    {
        anzVar = piced.length;
        if (anzPer > 0) 
            for (int i=0; i<anzPer; i++) data[i] = Statik.subvector(data[i], piced);

        structure = Statik.submatrix(structure, piced);
        errVal = Statik.subvector(errVal, piced);
        errPar = Statik.subvector(errPar, piced);
    }
    
    public double[][] createData (int anzPersons) {return createData(anzPersons, covVal, meanVal, structure, errVal);}
    public double[][] createData (int anzPersons, double[][] covMatrix, double[] muVector, double[][] structureMatrix, double[] error)
    {
        this.anzPer = anzPersons;
        int anzVar = structureMatrix.length;
        int anzFac = muVector.length;
        double[][] cholesky = Statik.choleskyDecompose(covMatrix);
        data = new double[anzPersons][anzVar];
        double[][] structureChol = Statik.multiply(structureMatrix,cholesky);
        double[] structureMu   = Statik.multiply(structureMatrix,muVector);

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
    
    public double[][] getData() {return data;}

    /**
     * Computes the distribution of parameters given a data covariance matrix and the dataMean assuming that sigma and mu from the model
     * are at the minimum for this data distribution.
     * 
     * The method inherits inefficiency from fisherInformation computation.
     * 
     * @param dataCov
     * @param dataMean
     * @param erg
     * @return
     */
    public double[][] getParameterDistributionCovariance(double[][] dataCov, double[] dataMean, double[] starting) {
        int anzPerMem = anzPer;
        evaluateMuAndSigma(); anzPer = 1;
        this.dataMean = Statik.copy(dataMean); this.dataCov = Statik.copy(dataCov); computeMomentsFromDataCovarianceAndMean();
        this.estimateML(starting);
        
        double[][] fisherInformation = computeFisherMatrix(dataCov, dataMean, null);
        double[][] hessianInv = Statik.invert(llDD);
        return Statik.multiply(hessianInv,Statik.multiply(fisherInformation,hessianInv));
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

        for (int i=0; i<anzVar; i++)
            for (int j=0; j<anzVar; j++)
            {
                double zwerg = 0;
                for (int k=0; k<anzFac; k++)
                    for (int l=0; l<anzFac; l++) 
                        zwerg += structure[i][k]*structure[j][l]*covVal[k][l];
                erg[i][j] = zwerg;                
            }
        for (int i=0; i<anzVar; i++) 
            erg[i][i] += errVal[i];
    }

    public double[] evaluateMu(double[] position) {if ((mu==null) || (mu.length!=anzVar)) mu = new double[anzVar]; evaluateMu(position, mu); return mu;}
    public void evaluateMu(double[] position, double[] erg)
    {
        if (position!=null) setParameter(position);
        if (position==null) position= getParameter();
        
        for (int i=0; i<anzVar; i++)
        {
            double zwerg = 0;
            for (int j=0; j<anzFac; j++) zwerg += structure[i][j]*meanVal[j];
            erg[i] = zwerg;                
        }
    }
    
    public double[][] evaluateFactorMatrix(double[] position) {double[][] erg = new double[anzFac][anzFac]; evaluateFactorMatrix(position, erg); return erg;}
    public void evaluateFactorMatrix(double[] position, double[][] erg)
    {
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++)
                erg[i][j] = (covPar[i][j]==NOPARAMETER?covVal[i][j]:position[covPar[i][j]]);
    }
    
    public double[][][] evaluateSigmaDev()
    {
        double[][][] erg = new double[anzPar][anzVar][anzVar];
        for (int i=0; i<anzVar; i++)
            for (int j=0; j<anzVar; j++)
                for (int k=0; k<anzFac; k++)
                    for (int l=0; l<anzFac; l++)
                        if (covPar[k][l]!=NOPARAMETER) erg[covPar[k][l]][i][j] += structure[i][k]*structure[j][l];
        for (int i=0; i<anzVar; i++)
            if (errPar[i]!=NOPARAMETER) erg[errPar[i]][i][i] += 1;
        return erg;
    }
    
    public double[][] evaluateMuDev()
    {
        double[][] erg = new double[anzPar][anzVar];
        for (int i=0; i<anzVar; i++)
            for (int j=0; j<anzFac; j++)
                if (meanPar[j]!=NOPARAMETER) erg[meanPar[j]][i] += structure[i][j];
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
        if ((muWork==null) || (muWork.length!=anzVar)) muWork = new double[anzVar];
        lsD = Statik.ensureSize(lsD, anzPar);
        lsDD = Statik.ensureSize(lsDD, anzPar, anzPar);
        
        if (value != null) setParameter(value);
        if (recomputeMuAndSigma) evaluateMuAndSigma(value);
        
        ls = 0;
        for (int i=0; i<anzVar; i++)
            for (int j=0; j<anzVar; j++) {sigmaWork[i][j] = sigma[i][j] - dataCov[i][j]; ls += 0.5*sigmaWork[i][j]*sigmaWork[i][j];}
        for (int i=0; i<anzVar; i++) {muWork[i] = mu[i] - dataMean[i]; ls += 0.5*muWork[i]*muWork[i];}
        
        for (int ap=0; ap<anzPar; ap++)
        {
            lsD[ap] = 0;
            if (parType[ap] == parameterType.mean)
                for (int i=0; i<anzVar; i++) lsD[ap] += structure[i][parPos[ap][0]]*muWork[i];
            if (parType[ap] == parameterType.facCov)
            {
                for (int i=0; i<anzVar; i++)
                    for (int j=0; j<anzVar; j++) 
                        lsD[ap] += (parPos[ap][0]==parPos[ap][1]?1:2)*(structure[i][parPos[ap][0]]*structure[j][parPos[ap][1]])*sigmaWork[i][j];
            }
            if (parType[ap] == parameterType.err)
                for (int i=0; i<anzVar; i++) lsD[ap] += (errPar[i]!=ap?0:sigmaWork[i][i]);
            for (int ap2l=ap; ap2l<anzPar; ap2l++)
            {
                int ap1 = ap2l, ap2 = ap;
                
                lsDD[ap1][ap2] = 0;
                if ((parType[ap1] == parameterType.facCov) && (parType[ap2] == parameterType.facCov))
                {
                    for (int i=0; i<anzVar; i++)
                        for (int j=0; j<anzVar; j++) 
                        {
                            double f1 = structure[i][parPos[ap1][0]]*structure[j][parPos[ap1][1]]+
                                        structure[j][parPos[ap1][0]]*structure[i][parPos[ap1][1]]; if (parPos[ap1][0]==parPos[ap1][1]) f1 /= 2;
                            double f2 = structure[i][parPos[ap2][0]]*structure[j][parPos[ap2][1]]+
                                        structure[j][parPos[ap2][0]]*structure[i][parPos[ap2][1]]; if (parPos[ap2][0]==parPos[ap2][1]) f2 /= 2;
                            lsDD[ap1][ap2] += f1*f2;
                        }
                }
                if ((parType[ap1] == parameterType.facCov) && (parType[ap2] == parameterType.err))
                    for (int i=0; i<anzVar; i++)
                        if (errPar[i]==ap2) lsDD[ap1][ap2] += (parPos[ap][0]==parPos[ap][1]?1:2)*(structure[i][parPos[ap1][0]]*structure[i][parPos[ap1][1]]);
                if ((parType[ap1] == parameterType.err) && (parType[ap2] == parameterType.err))
                    for (int i=0; i<anzVar; i++) if (errPar[i]==ap2) lsDD[ap1][ap2]++;
                if ((parType[ap1] == parameterType.mean) && (parType[ap2] == parameterType.mean))
                    for (int i=0; i<anzVar; i++)
                        lsDD[ap1][ap2] += structure[i][parPos[ap1][0]]*structure[i][parPos[ap2][0]];

                lsDD[ap2][ap1] = lsDD[ap1][ap2];
            }
        }
    }
    
    public double getMinusTwoLogLikelihood(double[] value, boolean recomputeMuAndSigma) {return computeLogLikelihood(value, recomputeMuAndSigma);} 
    public double computeLogLikelihood() {return computeLogLikelihood(null, false);}
    public double computeLogLikelihood(double[] value) {return computeLogLikelihood(value, true);}
    public double computeLogLikelihood(double[] value, boolean recomputeMuAndSigma)
    {
        if ((sigmaWork==null) || (sigmaWork.length!=anzVar) || (sigmaWork[0].length!=anzVar)) sigmaWork = new double[anzVar][anzVar];
        if ((sigInv==null) || (sigInv.length!=anzVar) || (sigInv[0].length!=anzVar)) sigInv = new double[anzVar][anzVar];

        if (value != null) setParameter(value);
        if (recomputeMuAndSigma) evaluateMuAndSigma();
        try {
            sigmaDet = Statik.invert(sigma,sigInv, sigmaWork);
        } catch (RuntimeException e) {sigmaDet = 0; ll = Double.NaN; return ll;}
        if ((Statik.determinantOfPositiveDefiniteMatrix(sigma)==-1) && (sigmaDet > 0))
            sigmaDet = Double.NaN;
        
        ll = anzPer*anzVar*LNTWOPI + anzPer*Math.log(sigmaDet);
        for (int i=0; i<anzVar; i++) 
            for (int j=0; j<anzVar; j++)
                ll += sigInv[i][j]*(xBiSum[i][j] - anzPer*mu[i]*dataMean[j] - anzPer*mu[j]*dataMean[i] + anzPer*mu[i]*mu[j]);
        
        // Additional Equality constraint information
        if (subtractEqualityLikelihoods) subtractEqualityCondidtions(false,false);
        return ll;
    }
    
    public void computeLogLikelihoodDerivatives(double[] value, boolean recomputeMuAndSigma) {
        if ((sigmaWork==null) || (sigmaWork.length!=anzVar) || (sigmaWork[0].length!=anzVar)) sigmaWork = new double[anzVar][anzVar];
        if ((sigmaWork2==null) || (sigmaWork2.length!=anzVar) || (sigmaWork2[0].length!=anzVar)) sigmaWork2 = new double[anzVar][anzVar];
        if ((sigmaWork3==null) || (sigmaWork3.length!=anzVar) || (sigmaWork3[0].length!=anzVar)) sigmaWork3 = new double[anzVar][anzVar];
        if ((sigmaWork4==null) || (sigmaWork4.length!=anzVar) || (sigmaWork4[0].length!=anzVar)) sigmaWork4 = new double[anzVar][anzVar];
        if ((sigInv==null) || (sigInv.length!=anzVar) || (sigInv[0].length!=anzVar)) sigInv = new double[anzVar][anzVar];
        llD = Statik.ensureSize(llD, anzPar);
        llDD = Statik.ensureSize(llDD, anzPar, anzPar);
        
        if (value != null) setParameter(value);
        if (recomputeMuAndSigma) evaluateMuAndSigma();
        
        sigmaDet = Statik.invert(sigma,sigInv, sigmaWork);
        if ((Statik.determinantOfPositiveDefiniteMatrix(sigma)==-1) && (sigmaDet > 0))
            sigmaDet = Double.NaN;
        
        ll = anzPer*anzVar*LNTWOPI + anzPer*Math.log(sigmaDet);
        for (int i=0; i<anzVar; i++) 
            for (int j=0; j<anzVar; j++)
                ll += sigInv[i][j]*(xBiSum[i][j] - anzPer*mu[i]*dataMean[j] - anzPer*mu[j]*dataMean[i] + anzPer*mu[i]*mu[j]);

        if (addErrorInfo != null)
        {
            for (int i=0; i<addErrorInfo.length; i++) {
                int ix = (int)addErrorInfo[i][0];
                ll += addErrorInfo[i][1]*Math.log(value[ix]) + addErrorInfo[i][2]/value[ix];
            }
        }
        
        for (int ap=0; ap<anzPar; ap++)
        {
            llD[ap] = 0.0;
            if (parType[ap] == parameterType.mean)
            {
                for (int i=0; i<anzVar; i++)
                    for (int j=0; j<anzVar; j++) llD[ap] -= 2*structure[i][parPos[ap][0]]*sigInv[i][j]*anzPer*(dataMean[j] - mu[j]);
            } 
            if (parType[ap] != parameterType.mean)
            {
                computeSigmaInvDev(ap, sigmaWork);
                double trace = 0;
                for (int i=0; i<anzVar; i++) trace += sigmaWork[i][i];
                llD[ap] += trace*anzPer;
                Statik.multiply(sigmaWork, sigInv, sigmaWork2);
                for (int i=0; i<anzVar; i++) 
                    for (int j=0; j<anzVar; j++) llD[ap] -= sigmaWork2[i][j]*(xBiSum[i][j] - anzPer*mu[i]*dataMean[j] - anzPer*mu[j]*dataMean[i] + anzPer*mu[i]*mu[j]);
            }
            if (addErrorInfo != null)
            {
                for (int i=0; i<addErrorInfo.length; i++) {
                    int ix = (int)addErrorInfo[i][0];
                    llD[ix] += (addErrorInfo[i][1]*value[ix] - addErrorInfo[i][2])/(value[ix]*value[ix]);
                }
            }
        }
        
        for (int ap1=0; ap1<anzPar; ap1++)
            for (int ap2=0; ap2<anzPar; ap2++)
                llDD[ap1][ap2] = 0;
        for (int ap1=0; ap1<anzPar; ap1++)
            for (int ap2=0; ap2<anzPar; ap2++)
            {
                
                // Term 4 in Eq. (13) of von Oertzen, Ghisletta, Lindenberger (2009)
                if ((parType[ap1]==parameterType.mean) && (parType[ap2]==parameterType.mean))
                {
                    for (int i=0; i<anzVar; i++)
                        for (int j=0; j<anzVar; j++)
                            llDD[ap1][ap2] += 2*anzPer*structure[i][parPos[ap1][0]]*structure[j][parPos[ap2][0]]*sigInv[i][j];
                }
                // Term 3 and 5 in Eq. (13) of von Oertzen, Ghisletta, Lindenberger (2009)
                if (((parType[ap1]!=parameterType.facCov) && (parType[ap1]!=parameterType.err) && (parType[ap2]!=parameterType.mean))
                ||  ((parType[ap1]!=parameterType.mean) && (parType[ap2]!=parameterType.facCov) && (parType[ap2]!=parameterType.err)))
                {
                    boolean exchange = (parType[ap1]==parameterType.facCov) || (parType[ap1]==parameterType.err)
                                        || (parType[ap2]==parameterType.mean);
                    int aq1 = (exchange?ap2:ap1), aq2 = (exchange?ap1:ap2);
                    computeSigmaInvDev(aq2, sigmaWork2);
                    Statik.multiply(sigmaWork2, sigInv, sigmaWork);
                    if (parType[aq1]==parameterType.mean)
                    {
                        for (int i=0; i<anzVar; i++)
                            for (int j=0; j<anzVar; j++)
                                llDD[ap1][ap2] += 2*structure[i][parPos[aq1][0]]*sigmaWork[i][j]*anzPer*(dataMean[j]-mu[j]);
                    } else {
                        for (int j=0; j<anzVar; j++)
                            llDD[ap1][ap2] += 2*meanVal[parPos[aq1][1]]*sigmaWork[parPos[aq1][0]][j]*anzPer*(dataMean[j]-mu[j]);
                    }
                }
                
                // Term 1,6,7 and 8 in Eq. (13) of von Oertzen, Ghisletta, Lindenberger (2009)
                if ((parType[ap1]!=parameterType.mean) && (parType[ap2]!=parameterType.mean))
                {
                    computeSigmaInvDev(ap1, sigmaWork);
                    computeSigmaInvDev(ap2, sigmaWork2);
                    computeSigmaInvDevDev(ap1, ap2, sigmaWork3);
                    Statik.multiply(sigmaWork, sigmaWork2, sigmaWork4);
                    double trace = 0;
                    for (int i=0; i<anzVar; i++) trace += sigmaWork3[i][i] - sigmaWork4[i][i];
                    llDD[ap1][ap2] += trace*anzPer;
                    Statik.multiply(sigmaWork4, sigInv, sigmaWork);
                    Statik.multiply(sigmaWork3, sigInv, sigmaWork2);
                    for (int i=0; i<anzVar; i++)
                        for (int j=0; j<anzVar; j++)
                            llDD[ap1][ap2] += (2*sigmaWork[i][j] - sigmaWork2[i][j])*(xBiSum[i][j] - anzPer*mu[i]*dataMean[j] - anzPer*mu[j]*dataMean[i] + anzPer*mu[i]*mu[j]);
                }
                
                // Additional Error Information
                if (addErrorInfo != null)
                {
                    for (int i=0; i<addErrorInfo.length; i++) {
                        int ix = (int)addErrorInfo[i][0];
                        llDD[ix][ix] += (2*addErrorInfo[i][2] - addErrorInfo[i][2]*value[ix])/(value[ix]*value[ix]*value[ix]);
                    }
                }
                
            }
        // Additional Equality constraint information
        if (subtractEqualityLikelihoods) subtractEqualityCondidtions(false,true);
    }

    protected void computeMatrixTimesMuDev(int par, double[][] matrix, double[] erg) {
        for (int i=0; i<anzVar; i++) erg[i] = 0;
        if (parType[par]==parameterType.mean) {
            for (int i=0; i<anzVar; i++)
            {
                erg[i] = 0;
                if (matrix==null) erg[i] = structure[i][parPos[par][0]];
                else for (int j=0; j<anzVar; j++) erg[i] += matrix[i][j] * structure[j][parPos[par][0]];
            }
        }
    }
    protected void computeMatrixTimesSigmaDev(int par, double[][] matrix, double[][] erg) {
        if (parType[par]==parameterType.mean) 
            for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) erg[i][j] = 0;
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
    }
    public LinearModel copy() {
        return new LinearModel(this);
    }
    public LinearModel removeObservation(int obs) {
        double[][] newStruct = new double[anzVar-1][anzFac];
        int[] newErrPar = new int[anzVar-1];
        double[] newErrVal = new double[anzVar-1];
        for (int i=0; i<anzVar-1; i++) {
            int k = (i>=obs?i+1:i);
            for (int j=0; j<anzFac; j++) newStruct[i][j] = structure[k][j];
            newErrPar[i] = errPar[k];
            newErrVal[i] = errVal[k];
        }
        return new LinearModel(newStruct, covPar, covVal, meanPar, meanVal, newErrPar, newErrVal);
        
    }
    public void evaluateMuAndSigma(double[] values) {
        evaluateSigma(values);
        evaluateMu(values);
    }
    public boolean isErrorParameter(int nr) {
        return parType[nr] == parameterType.err;
    }
    
    /*
    public double[] leastSquareAproxLinearModel() 
    {
        if (anzPar == 0) return new double[0];
        // initial vector set to (0,...,0) for easier computation

    	double[] fitDev1 = new double[anzMeanPar];
    	for (int i=0; i<anzMeanPar; i++) fitDev1[i] = 0.0;
    	double[][] fitJac1 = new double[anzMeanPar][anzMeanPar];
    	for (int i=0; i<anzMeanPar; i++)
    		for (int j=0; j<anzMeanPar; j++) fitJac1[i][j] = 0.0;

    	double[] fitDev2 = new double[anzVarPar + anzErrPar];
    	for (int i=0; i<anzVarPar + anzErrPar; i++) fitDev2[i] = 0.0;
    	double[][] fitJac2 = new double[anzVarPar + anzErrPar][anzVarPar + anzErrPar];
    	for (int i=0; i<anzVarPar + anzErrPar; i++)
    		for (int j=0; j<anzVarPar + anzErrPar; j++) fitJac2[i][j] = 0.0;

    	for (int i=0; i<anzVar; i++)
    	{
    	    double v = 0.0;
    	    for (int j=0; j<anzFac; j++) if (meanPar[j]==NOPARAMETER) v += structure[i][j] * meanVal[j]; 
    	    v -= dataMean[i]; v *= 2.0; 
    		for (int j=0; j<anzFac; j++)
    			if (meanPar[j]!=NOPARAMETER) fitDev1[meanPar[j]] += structure[i][j]*v;	// Ableitungen

    		for (int k=0; k<anzFac; k++)
    			for (int l=0; l<anzFac; l++)
    				if ((meanPar[k]!=NOPARAMETER) && (meanPar[l]!=NOPARAMETER)) 
    					fitJac1[meanPar[k]][meanPar[l]] += 2.0*structure[i][k]*structure[i][l];
    	}

    	for (int i=0; i<anzVar; i++)
    	{
    		for (int j=0; j<anzVar; j++)
    		{
    		    double v = 0.0; 
        	    for (int k=0; k<anzFac; k++) 
        	        for (int l=0; l<anzFac; l++)
        	            if (covPar[k][l]==NOPARAMETER) v += structure[i][k] * structure[j][l] * covVal[k][l];
        	    if ((i==j) && (errPar[i]==NOPARAMETER)) v += errVal[i];
    		    v -= dataCov[i][j]; v *= 2.0;
    		    
    			for (int k=0; k<anzFac; k++)
    			    for (int l=0; l<anzFac; l++)
    			        if (covPar[k][l]!=NOPARAMETER)
    			            fitDev2[covPar[k][l]-anzMeanPar] += structure[i][k] * structure[j][l] * v;	// Ableitungen
    			if ((i==j) && (errPar[i]!=NOPARAMETER)) fitDev2[errPar[i]-anzMeanPar] += v;
    			
    			for (int k=0; k<anzFac; k++)
    				for (int l=0; l<anzFac; l++)
    				    if (covPar[k][l]!=NOPARAMETER)
    		    			for (int k2=0; k2<anzFac; k2++)
    		    				for (int l2=0; l2<anzFac; l2++)
    		    				    if (covPar[k2][l2]!=NOPARAMETER) 
    		    				        fitJac2[covPar[k][l]-anzMeanPar][covPar[k2][l2]-anzMeanPar] += 2*structure[i][k]*structure[j][l]*structure[i][k2]*structure[j][l2];
    			if ((i==j) && (errPar[i]!=NOPARAMETER)) 
    			{
    			    for (int k=0; k<anzFac; k++)
    			        for (int l=0; l<anzFac; l++)
    			            if (covPar[k][l]!=NOPARAMETER) 
    			            {
    			                v = 2*structure[i][k]*structure[j][l];
    			                fitJac2[covPar[k][l]-anzMeanPar][errPar[i]-anzMeanPar] += v; fitJac2[errPar[i]-anzMeanPar][covPar[k][l]-anzMeanPar] += v;
    			            }
    			    fitJac2[errPar[i]-anzMeanPar][errPar[i]-anzMeanPar] += 2.0;
    			}
    		}
    	}
/*
    	final LinearModel fthis = this;
		DoubleFunction foo = new DoubleFunction(){
		    public double foo(double[] arg) {
				// Evaluation of Sigma
		        return fthis.sigmaSquaredError(arg);
		    }
		};
		double[] zero = new double[anzPar];
		double[][] numJacobian = Statik.numericalHessian(foo, zero, 0.001);
		System.out.println("Numerical Jacobian = \r\n"+Statik.matrixToString(numJacobian));
		double[] numGradient = Statik.numericalGradient(foo, zero, 0.001);
		System.out.println("Numerical Gradient = "+Statik.matrixToString(numGradient));
		
		System.out.println("Jacobian       = \r\n"+Statik.matrixToString(fitJac2));
		System.out.println("Gradient       = \r\n"+Statik.matrixToString(fitDev2));
    	
    	double[][] jinv1 = Statik.invert(fitJac1);
    	double[][] jinv2 = Statik.invert(fitJac2);

    	double[] erg = new double[anzPar];
    	for (int i=0; i<anzMeanPar; i++)
    		for (int j=0; j<anzMeanPar; j++) erg[i] -= jinv1[i][j]*fitDev1[j];
    		
    	for (int i=0; i<anzVarPar + anzErrPar; i++)
    		for (int j=0; j<anzVarPar + anzErrPar; j++) erg[i+anzMeanPar] -= jinv2[i][j]*fitDev2[j];

/*
    	double[][] theoSigma = null;
    	if (anzPar==6) theoSigma = evaluateSigma(new double[]{100,-20,100,50,0,1});
    	if (anzPar==5) theoSigma = evaluateSigma(new double[]{100,-20,100,50,1});
    	if (anzPar==3) theoSigma = evaluateSigma(new double[]{100,-20,1});
    	System.out.println(Statik.matrixToString(theoSigma));
    	System.out.println(Statik.matrixToString(observedCovariances));
    	System.out.println(Statik.matrixToString(erg));
    	
    	return erg;
    }
    */
    
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

    /*
    public void computeFisher(){computeFisher(getParameters());}
    public void computeFisher(double[] xd)
    {
		double[][][] sigmaDev = evaluateSigmaDev();
		double[][] muDev = evaluateMuDev();
		double[][] sigD = evaluateSigma(xd);
		double[][] sigMInvD = new double[anzVar][anzVar];
		double[][] mat1 = new double[anzVar][anzVar];	// Arbeitsplatz f�r Matrixinvertierung
		double[][][] sigMInvSigDevD = new double[anzPar][anzVar][anzVar];
		double[][][] sigMInvSigDevDSigMInv = new double[anzPar][anzVar][anzVar];

		Statik.invert(sigD, sigMInvD, mat1);
		for (int i=0; i<anzPar; i++)
		{
			Statik.multiply(sigMInvD, sigmaDev[i], sigMInvSigDevD[i]);
			Statik.multiply(sigMInvSigDevD[i], sigMInvD, sigMInvSigDevDSigMInv[i]);
		}
		// Aufbau der Jacobi-Matrix, zun�chst Mittelwert - Mittelwert
		for (int i=0; i<anzMeanPar; i++)
			for (int j=i; j<anzMeanPar; j++)
			{
				double sum = 0.0;
				for (int k=0; k<anzVar; k++)
					for (int l=0; l<anzVar; l++)
						sum += sigMInvD[k][l] * muDev[i][k] * muDev[j][l];
				fisherInformationMatrix[i][j] = fisherInformationMatrix[j][i] = 2*anzPer*sum; 
			}
		// jetzt Mittelwert - Varianz
		for (int i=0; i<anzMeanPar; i++)
			for (int j=anzMeanPar; j<anzPar; j++)
				fisherInformationMatrix[i][j] = fisherInformationMatrix[j][i] = 0.0; 
		// und jetzt Varianz - Varianz
		for (int i=anzMeanPar; i<anzPar; i++)
			for (int j=i; j<anzPar; j++)
			{
				Statik.multiply(sigMInvSigDevD[i-anzMeanPar],sigMInvSigDevD[j-anzMeanPar], mat1);

				double spur = 0.0;
				for (int k=0; k<anzVar; k++) spur += mat1[k][k];
				fisherInformationMatrix[i][j] = fisherInformationMatrix[j][i] = (double)anzPer * spur / 2.0; 
			}
    }
    */
    
    public static void steveSimulation()
    {
        double errvar = 0.1, errstdv = Math.sqrt(errvar);
        LinearModel model = new LinearModel(
                new double[][]{{1,-5,25},{1,-4,16},{1,-3,9},{1,-2,4},{1,-1,1},{1,0,0},{1,1,1},{1,2,4},{1,3,9},{1,4,16},{1,5,25}},
                new int[][]{{0,3,4},{3,1,5},{4,5,2}},
                new double[][]{{1,0,0},{0,1,0},{0,0,1}},
                new int[]{-1,-1,-1},
                new double[]{0,0,0},
                new int[]{6,6,6,6,6,6,6,6,6,6,6},
                new double[]{.1,.1,.1,.1,.1,.1,.1,.1,.1,.1,.1}
                );
        
//        double[] initial = new double[]{1,1,1,0,0,0,errvar};
        model.fixParameter(4, 0);
        double[] initial = new double[]{1,1,0,0,0,errvar};
        int N = 100;
        int dataLength = 100;
        double probPhaseJump = 0.05;
        double[][] fullData = new double[N][dataLength];
        
        java.util.Random rand = new java.util.Random();
        double f = 0.05;
        double phase = 0;
        f = 0.06; for (int n=0; n<N/3; n++)
            for (int i=0; i<dataLength; i++) 
            {if (rand.nextDouble()<probPhaseJump) phase = rand.nextDouble()*2*Math.PI; fullData[n][i] = Math.sin(2*Math.PI*i*f+phase) + rand.nextGaussian()*errstdv;};
        f = 0.08; for (int n=N/3; n<2*N/3; n++)
            for (int i=0; i<dataLength; i++) 
            {if (rand.nextDouble()<probPhaseJump) phase = rand.nextDouble()*2*Math.PI; fullData[n][i] = Math.sin(2*Math.PI*i*f+phase) + rand.nextGaussian()*errstdv;};
        f = 0.1; for (int n=2*N/3; n<N; n++)
            for (int i=0; i<dataLength; i++) 
            {if (rand.nextDouble()<probPhaseJump) phase = rand.nextDouble()*2*Math.PI; fullData[n][i] = Math.sin(2*Math.PI*i*f+phase) + rand.nextGaussian()*errstdv;};

        for (int i=30; i<70; i++) System.out.println(fullData[5][i]+"\t"+fullData[50][i]+"\t"+fullData[85][i]);
            
        // contains double[], first number of members in cluster, second -2ll value, sequential cluster members
        double[][] cluster = new double[2*N][];
        double[][][] data = new double[2*N][][];
        boolean[] isActive = new boolean[2*N]; 
        for (int i=0; i<2*N; i++) isActive[i] = false; for (int i=0; i<N; i++) isActive[i] = true;
        for (int i=0; i<N; i++)
            cluster[i] = new double[]{1,-1,i};
        
        long time = System.currentTimeMillis();
        
        for (int i=0; i<N; i++)
        {
            data[i] = new double[dataLength-11+1][11];
            for (int j=0; j<dataLength-11+1; j++)
                for (int k=0; k<11; k++) data[i][j][k] = fullData[i][j+k];
            model.data = data[i]; model.anzPer = model.data.length;
            model.computeMoments();
            double[] est1 = model.estimateMLSupportedByPowerEquivalence(initial); double ll1 = model.ll; ll1 = model.ll;
            double[] est2 = model.estimateML(initial); double ll2 = model.ll;
            double[] est3 = model.estimateMLFullCovarianceSupportedByPowerEquivalence(); double ll3 = model.ll;
            System.out.println(Statik.matrixToString(est1)+"\t\t"+ll1+"\r\n"+Statik.matrixToString(est2)+"\t\t"+ll2+
                    "\r\n"+Statik.matrixToString(est3)+"\t\t"+ll3+"\r\n");
            cluster[i][1] = model.ll;
        }

        // cluster1, cluster2, loss
        TreeSet<double[]> pairs = new TreeSet<double[]>(new Comparator<double[]>(){
            public int compare(double[] a, double[] b) {return (a[2]>b[2]?1:(a[2]<b[2]?-1:0));}
        });        

        for (int i=0; i<N; i++)
            for (int j=i+1; j<N; j++)
            {
                double[][] combinedData = new double[data[i].length+data[j].length][];
                for (int k=0; k<data[i].length; k++) combinedData[k] = data[i][k];
                for (int k=0; k<data[j].length; k++) combinedData[k+data[i].length] = data[j][k];
                model.data = combinedData; model.anzPer = model.data.length;
                model.computeMoments();
                model.estimateMLFullCovarianceSupportedByPowerEquivalence();
//              model.estimateMLSupportedByPowerEquivalence(initial);
//                workhorse.twollAproxLinearModel(model, initial); model.lastMinusTwoLL = workhorse.twollValue;
                double loss = model.ll - cluster[i][1] - cluster[j][1];
                pairs.add(new double[]{i,j,loss});
            }

        int ergcounter = 2*N-2;
        int[] ergnr = new int[2*N-1];
        double[][] memory = new double[2*N][];
        double[] t; double[][] t2;
        int anzCluster = N;
        // Start of clustering
        while (!pairs.isEmpty())
        {
            double[] winner = pairs.first();
            int wi = (int)winner[0], wj = (int)winner[1]; 
            double[][] winningData = new double[data[wi].length+data[wj].length][];
            for (int k=0; k<data[wi].length; k++) winningData[k] = data[wi][k];
            for (int k=0; k<data[wj].length; k++) winningData[k+data[wi].length] = data[wj][k];
            cluster[anzCluster] = cluster[wi]; data[anzCluster] = data[wi]; 
            cluster[wi] = new double[2+(int)Math.round(cluster[anzCluster][0]) + (int)Math.round(cluster[wj][0])];
            cluster[wi][0] = cluster[anzCluster][0] + cluster[wj][0]; cluster[wi][1] = winner[2] + cluster[anzCluster][1] + cluster[wj][1]; 
            for (int i=0; i<cluster[anzCluster][0]; i++) cluster[wi][i+2] = cluster[anzCluster][i+2];
            for (int i=0; i<cluster[wj][0]; i++) cluster[wi][i+(int)cluster[anzCluster][0]+2] = cluster[wj][i+2];
            data[wi] = winningData;
            isActive[wj] = false;
            anzCluster++;
            
            int count = 0;
            for (double[] w:pairs) 
                if (((int)w[0]==wi) || ((int)w[0]==wj) || ((int)w[1]==wi) || ((int)w[1]==wj)) memory[count++] = w;
            for (int i=0; i<count; i++) pairs.remove(memory[i]);
            ergnr[ergcounter--] = anzCluster-1; ergnr[ergcounter--] = wj;
            
            for (int i=0; i<cluster.length; i++) if ((i!=wi) && (isActive[i]))
            {
                double[][] combinedData = new double[data[i].length+data[wi].length][];
                for (int k=0; k<data[i].length; k++) combinedData[k] = data[i][k];
                for (int k=0; k<data[wi].length; k++) combinedData[k+data[i].length] = data[wi][k];
                model.data = combinedData; model.anzPer = model.data.length;
                model.computeMoments();
                model.estimateMLFullCovarianceSupportedByPowerEquivalence();
//                model.estimateMLSupportedByPowerEquivalence(initial);
//                workhorse.twollAproxLinearModel(model, initial); model.lastMinusTwoLL = workhorse.twollValue;
                double loss = model.ll - cluster[i][1] - cluster[wi][1];
                pairs.add(new double[]{i,wi,loss});
            }
            if (pairs.isEmpty()) ergnr[ergcounter--] = wi;
        }
        
        for (int i=0; i<10; i++) System.out.println(Statik.matrixToString(cluster[ergnr[i]])); 
            
        System.out.println("Total time = "+(System.currentTimeMillis() - time));
    }
    
    /**
     * Using Time Delayed Embedding and PEML, an LDE is fit to the rawData from offset to offset+length, embed many points per data line, in steps of tau.
     * DeltaT gives the time intervall between two data point steps.
     * 
     * Returns two double[] with weights to the 2nd derivative of x, from x, xd, y, and yd.
     */
    public static double[][] fitCoupledOscilatorModel(double[][] rawData, int offset, int length, int tau, int embed, double deltaT) {
        return fitCoupledOscilatorModel(rawData, offset, length, tau, embed, deltaT, false);
    }
    public static double[][] fitCoupledOscilatorModel(double[][] rawData, int offset, int length, int tau, int embed, double deltaT, boolean dataIsEmbeded)
    {
        int anzPer;  
        double[][] data;
        if (!dataIsEmbeded) {
            anzPer = length-embed*tau+1;
            data = new double[anzPer][2*embed];
            double mean1 = 0, mean2 = 0;
            for (int i=0; i<length; i++) {mean1 += rawData[offset+i][0]; mean2 += rawData[offset+i][1];}
            mean1 /= length; mean2 /= length;
            for (int i=0; i<data.length; i++)
            {
                for (int j=0; j<embed; j++) data[i][j] = rawData[offset+i+j*tau][0] - mean1;
                for (int j=0; j<embed; j++) data[i][embed+j] = rawData[offset+i+j*tau][1] - mean2;
            }
        } else {
            anzPer = length;
            data = rawData;
            double[] mean = Statik.meanVector(data);
            for (int i=0; i<length; i++) Statik.subtract(data[i], mean, data[i]);
        }

        double half = (embed-1) / 2.0;
        double[][] struct = new double[2*embed][6];
        for (int i=0; i<embed; i++)
            struct[i] = new double[]{1,(i-half)*deltaT*tau,(i-half)*deltaT*tau*(i-half)*deltaT*tau/2.0,0,0,0};
        for (int i=0; i<embed; i++)
            struct[i+embed] = new double[]{0,0,0,1,(i-half)*deltaT*tau,(i-half)*deltaT*tau*(i-half)*deltaT*tau/2.0};

        /*
        LinearModelOld model = new LinearModelOld(struct);
        double[][] subTransform = model.computePowerEquivalenceTransformationMatrix();
        double[][] intermediateStruct = Statik.multiply(subTransform, struct);
        double[][] top33 = new double[3][3];
        for (int i=0; i<3; i++) for (int j=0; j<3; j++) top33[i][j] = intermediateStruct[i][j];
        double[][] top33Inv = Statik.invert(top33);
        double[][] secondTrans = Statik.identityMatrix(embed);
        for (int i=0; i<3; i++) for (int j=0; j<3; j++) secondTrans[i][j] = top33Inv[i][j];
        double[][] transform = Statik.multiply(secondTrans, subTransform);
        double[][] totalTransform = new double[2*embed][2*embed];
        for (int i=0; i<embed; i++) for (int j=0; j<embed; j++) 
            totalTransform[i][j] = totalTransform[i+embed][j+embed] = transform[i][j];
        
        double[][] ergcov = new double[6][6];
        double sqrsum1 = 0, sqrsum2 = 0;
        double[] vec;
        for (int i=0; i<anzPer; i++) {
            vec = Statik.multiply(totalTransform, data[i]);
            for (int j=3; j<embed; j++) sqrsum1 += vec[j]*vec[j];
            for (int j=3; j<embed; j++) sqrsum2 += vec[j+embed]*vec[j+embed];
            for (int j=0; j<3; j++) for (int k=j; k<3; k++) ergcov[j][k] += vec[j]*vec[k];
            for (int j=0; j<3; j++) for (int k=0; k<3; k++) ergcov[j][3+k] += vec[j]*vec[embed+k];
            for (int j=0; j<3; j++) for (int k=j; k<3; k++) ergcov[3+j][3+k] += vec[embed+j]*vec[embed+k];
        }
        for (int j=0; j<6; j++) for (int k=j; k<6; k++)
            {ergcov[j][k] /= anzPer; ergcov[k][j] = ergcov[j][k];}
        double errvar1 = sqrsum1 / (anzPer*(embed-3));
        double errvar2 = sqrsum2 / (anzPer*(embed-3));

//        double[][] testDataTrans = Statik.transpose(Statik.multiply(totalTransform, Statik.transpose(data)));
//        Object[] testMeanCov = Statik.meanVectorAndCovarianceMatrix(testDataTrans);

        double[][] top33InvSqr = Statik.multiply(top33Inv, Statik.transpose(top33Inv));
        for (int i=0; i<3; i++)
            for (int j=0; j<3; j++) {
                ergcov[i][j] -= top33InvSqr[i][j]*errvar1;
                ergcov[3+i][3+j] -= top33InvSqr[i][j]*errvar2;
            }
        */
        
        LinearModel model = new LinearModel(struct);
        model.setData(data);
        double[] est = model.estimateMLFullCovarianceSupportedByPowerEquivalence();
        System.out.println("Full cov = "+Statik.matrixToString(est));

        model.setParameter(est);
        
        double[][] mat = new double[4][4];
        double[] vec1 = new double[4], vec2 = new double[4];
        double[][] ergcov = model.covVal;
        
        for (int i=0; i<2; i++)
        {
            for (int j=0; j<2; j++)
            {
                mat[i][j] = ergcov[i][j];
                mat[2+i][j] = ergcov[3+i][j];
                mat[i][2+j] = ergcov[i][3+j];
                mat[2+i][2+j] = ergcov[3+i][3+j];
            }
            vec1[i] = ergcov[2][i];
            vec1[2+i] = ergcov[2][3+i];
            vec2[i] = ergcov[5][i];
            vec2[2+i] = ergcov[5][3+i];
        }
        double[][] matinv = Statik.invert(mat);
        double[] weights1 = Statik.multiply(matinv, vec1);
        double[] weights2 = Statik.multiply(matinv, vec2);
        
        return new double[][]{weights1,weights2};
    }

    public static double[] fitOscilatorModel(double[] rawData, int offset, int length, int tau, int embed, double deltaT)
    {
//        Statik.removeRegressionLine(rawData);
        int anzPer;
        double[][] data;
        anzPer = length-embed*tau+1;
        data = new double[anzPer][embed];
        double mean1 = 0;
        for (int i=0; i<length; i++) mean1 += rawData[offset+i];
        mean1 /= length; 
        for (int i=0; i<data.length; i++)
            for (int j=0; j<embed; j++) data[i][j] = rawData[offset+i+j*tau] - mean1;
        return fitOscilatorModel(data, offset, anzPer, tau, embed, deltaT);
    }    
    
    /**
     * Using Time Delayed Embedding and PEML, an LDE is fit to the rawData from offset to offset+length, embed many points per data line, in steps of tau.
     * DeltaT gives the time intervall between two data point steps.
     * 
     * Returns one double[] with weights to the 2nd derivative of x, from x, xd
     */
    public static double[] fitOscilatorModel(double[][] data, int offset, int length, int tau, int embed, double deltaT)
    {
//        Statik.removeRegressionLine(rawData);
        int anzPer = data.length;
        double[] mean = Statik.meanVector(data);
        for (int i=0; i<length; i++) Statik.subtract(data[i], mean, data[i]);

        double half = (embed-1) / 2.0;
        double[][] struct = new double[embed][3];
        for (int i=0; i<embed; i++) 
            struct[i] = new double[]{1,(i-half)*deltaT*tau,(i-half)*deltaT*tau*(i-half)*deltaT*tau/2.0};

        LinearModel model = new LinearModel(struct);
        LinearModel model2 = new LinearModel(struct);
        model.setData(data); model2.setData(data);
        
        double[] est = model.estimateMLFullCovarianceSupportedByPowerEquivalence();
        // TvO 25.10.2017: use the following lines instead of the one above to use robust ML temporarily
//        model.setDataDistribution(Statik.covarianceMatrix(data, false), new double[model.anzVar]);
//        double[] est = model.estimateMLFullCovarianceSupportedByPowerEquivalence(true);
        
        model2.evaluateMuAndSigma(est); model2.computeLogLikelihood();
//        System.out.println(Statik.matrixToString(est)+"\t"+model2.ll); 

        model.setParameter(est);
        double[][] mat = new double[2][2];
        double[] vec = new double[2];
        

        for (int i=0; i<2; i++)
        {
            for (int j=0; j<2; j++)
                mat[i][j] = model.covVal[i][j];

            vec[i] = model.covVal[2][i];
        }
        double[][] matinv = Statik.invert(mat);
        double[] weights = Statik.multiply(matinv, vec);
        
        return weights;
    }
    
    private static void fidanData() {
        double[][] orgData = Statik.loadMatrix("fidanData.csv", ';');
        
        int anzPer = 10, anzVar = 3, anzTime = 40;
        double[][][] rawData = new double[anzPer][anzVar][anzTime];
        for (int i=0; i<anzPer; i++) {
            for (int j=0; j<anzVar; j++) 
                for (int k=0; k<anzTime; k++) 
                    rawData[i][j][k] = orgData[anzTime*i+k][j+8];
        }
        int vpnr = 4;
        System.out.println(Statik.matrixToString(rawData[vpnr][1]));
//        double[] dat = Statik.copy(rawData[5][1]);                works with freq = 0.425, damp = -0.03
        double[] dat = Statik.copy(rawData[vpnr][1]);
        Statik.removeRegressionLine(dat);
        System.out.println(Statik.matrixToString(dat));
        double[] weights = fitOscilatorModel(dat, 0, 40, 1, 5, 1);
        System.out.println("Weights = "+Statik.matrixToString(weights));
        double damping = weights[1] / 2;
        double frequency = Math.sqrt(-weights[0] - weights[1]/4);
        System.out.println("Frequency = "+frequency);
        System.out.println("Damping = "+damping);
    }

    
    public static void ldeFitDrumming(File file, int embed, double deltaT, int tau, int window, int omissions, 
            double[][] superResults, double[][] subResults)
    {
        double[][] inData = Statik.loadMatrix(file, '\t');
        ldeFitDrumming(inData, embed, deltaT, tau, window, omissions, superResults, subResults);
    }
    public static void ldeFitDrumming(double[][] inData, int embed, double deltaT, int tau, int window, int omissions, 
            double[][] superResults, double[][] subResults)
    {
        double[][] rawData = new double[inData.length][2];
        for (int i=0; i<rawData.length; i++)
            for (int j=0; j<rawData[i].length; j++) rawData[i][j] = inData[i][j];
//        for (int j=0; j<rawData[i].length; j++) rawData[i][j] = inData[i][j+1];
        
        int anzPoints = rawData.length-window/2;
        double[][] superInput = new double[anzPoints/omissions+1][2];        
        for (int i=window/2; i<anzPoints; i+=omissions)
        {
            double[][] weights = fitCoupledOscilatorModel(rawData, i-window/2, window, tau, embed, deltaT);
            superInput[i/omissions][0] = -weights[0][0];
            superInput[i/omissions][1] = -weights[1][2];
            if (i%400==0) System.out.print(i+", ");
        }
        double[][] subWeights = fitCoupledOscilatorModel(rawData, 0, anzPoints, tau, embed, deltaT);
        double[][] superWeights = fitCoupledOscilatorModel(superInput, 0, superInput.length, tau, embed, deltaT*omissions);
        
        Statik.copy(subWeights, subResults); Statik.copy(superWeights, superResults);
    }
    
    /*
    public static void loopLdeFitDrumming()
    {
        int embed = 50; 
        double deltaT = 0.001;
        int tau = 1;
        int window = 1800*tau;
        int omissions = 10;
        BufferedWriter w = null;
        try {
            w = new BufferedWriter(new FileWriter("twoLevelLDEOutput.txt"));
        } catch (Exception e) {System.out.println("Couldn't open output file."); System.exit(1);}

        double[][] superResult = new double[2][4];
        double[][] subResult = new double[2][4];
        long time = System.nanoTime();
        File dir = new File("discrete");
        File[] files = dir.listFiles();
        for (int i=0; i<files.length; i++)
        {
            try {
                if (files[i].getName().endsWith(".lst")) {
                    System.out.print("Processing file "+files[i].getName()+", millisecond processed: ");
                    double[][] discrete = Statik.loadMatrix(files[i], '\t');
                    double[][] continuous = new double[2][];
                    continuous[0] = DiscreteToContinuous.discreteToContinuous(discrete[0], 30);
                    continuous[1] = DiscreteToContinuous.discreteToContinuous(discrete[1], 30);
                    int len = (int)Math.min(continuous[0].length-discrete[0][0], continuous[1].length - discrete[1][0]);
                    double[][] contTrans = new double[len][2];
                    for (int k=0; k<len; k++) 
                        for (int j=0; j<2; j++) contTrans[k][j] = continuous[j][k+(int)Math.round(discrete[j][0])];
                    
                    ldeFitDrumming(contTrans,embed, deltaT, tau, window, omissions, superResult, subResult); 
                    System.out.println();
                    double[] ergd = new double[21];
                    ergd[0] = Double.parseDouble(files[i].getName().substring(6, 9)); 
                    ergd[1] = Double.parseDouble(files[i].getName().substring(10, 13)); 
                    ergd[2] = Double.parseDouble(files[i].getName().substring(14, 15)); 
                    ergd[3] = Double.parseDouble(files[i].getName().substring(16, 17)); 
                    ergd[4] = 0;
                    for (int j=0; j<2; j++) for (int k=0; k<4; k++) ergd[5+j*4+k] = superResult[j][k];
                    for (int j=0; j<2; j++) for (int k=0; k<4; k++) ergd[5+8+j*4+k] = subResult[j][k];

                    w.write(Statik.matrixToString(ergd)+"\r\n");
                    w.flush();
                }
            } catch (Exception e) {System.out.println(); System.out.println("I encountered problems with file "+files[i].getName()+", exception "+e);}
        }
        try {w.close();} catch (IOException e) {}
        
        time = System.nanoTime() - time;
        System.out.println((time/1000000000)+" seconds needed.");
    }
    */
    
    // Testklasse to fit 2-level LDE with windowed approach
    public static void ldeFitDrumming()
    {
        int embed = 50; 
        double deltaT = 0.001;
        int tau = 1;
        double additionalErrorSTDV = 0.0;
        int window = 1800*tau;

//        DynamicSystemSimulator simulator = new DynamicSystemSimulator();
//        simulator.simulateDynamicalSystem(0.00001, deltaT, 20000, new double[]{0,0,1,0.1,0.1}, new double[]{0,0,1,0.1,0.1}, new double[]{1,0,355,0}, new double[]{1,0,280,0}, .1, .1);
//        simulator.simulateDynamicalSystem(0.00001, deltaT, 20000, new double[]{0.1,0,1,0.3,0.3}, new double[]{0.1,0.1,1,0.3,0.3}, new double[]{1,0,355,0}, new double[]{1,0,280,0}, .1, .1);
//        double[][] rawData = Statik.transpose(simulator.value);
        double[][] inData = Statik.loadMatrix("continuous\\SE234__3_2_1_6.raw", '\t');
        double[][] rawData = new double[inData.length][2];
        for (int i=0; i<rawData.length; i++)
            for (int j=0; j<rawData[i].length; j++) rawData[i][j] = inData[i][j+1] + staticRandom.nextGaussian() * additionalErrorSTDV;
        
        for (int i=0; i<10000; i++) System.out.println(rawData[i][0]+"\t"+rawData[i][1]);

        int omissions = 10;
        int anzPoints = rawData.length-window/2;
        double[][] estRetraction = new double[anzPoints][2];
        double[][] superInput = new double[anzPoints/omissions][2];        
        double[][] allWeightsWindowed = new double[anzPoints][8];
        for (int i=window/2; i<anzPoints; i+=omissions)
        {
            double[][] weights = fitCoupledOscilatorModel(rawData, i-window/2, window, tau, embed, deltaT);
            estRetraction[i][0] = superInput[i/omissions][0] = -weights[0][0];
            estRetraction[i][1] = superInput[i/omissions][1] = -weights[1][2];
            if (i%400==0) System.out.println(i);
            for (int j=0; j<4; j++) {allWeightsWindowed[i][j] = weights[0][j]; allWeightsWindowed[i][4+j] = weights[1][j];}
            for (int j=0; j<omissions; j++) 
            {
                estRetraction[i+j][0] = estRetraction[i][0]; estRetraction[i+j][1] = estRetraction[i][1];
                for (int k=0; k<8; k++) allWeightsWindowed[i+j][k] = allWeightsWindowed[i][k];
            }
            
        }
        for (int i=0; i<window/2; i++) estRetraction[i] = estRetraction[window/2];
        for (int i=rawData.length-window/2; i<anzPoints; i++) estRetraction[i] = estRetraction[rawData.length-window/2];
        double[][] weights1 = fitCoupledOscilatorModel(rawData, 3725*tau-window/2, window, tau, embed, deltaT);
        double[][] weights2 = fitCoupledOscilatorModel(rawData, 3125*tau-window/2, window, tau, embed, deltaT);
        double[][] weights = fitCoupledOscilatorModel(rawData, 0, anzPoints, tau, embed, deltaT);
        double[][] superWeights = fitCoupledOscilatorModel(superInput, 0, superInput.length, tau, embed, deltaT*omissions);
        
        for (int i=0; i<window; i++) System.out.println(rawData[1125*tau-window/2+i][0]+"\t"+rawData[1125*tau-window/2+i][1]);
        
        System.out.println("Weights1 (x,xd,y,yd) -> xdd: "+Statik.matrixToString(weights1[0]));
        System.out.println("Weights1 (x,xd,y,yd) -> ydd: "+Statik.matrixToString(weights1[1]));
        System.out.println("Weights2 (x,xd,y,yd) -> xdd: "+Statik.matrixToString(weights2[0]));
        System.out.println("Weights2 (x,xd,y,yd) -> ydd: "+Statik.matrixToString(weights2[1]));
        System.out.println("Overall  (x,xd,y,yd) -> xdd: "+Statik.matrixToString(weights[0]));
        System.out.println("Overall  (x,xd,y,yd) -> ydd: "+Statik.matrixToString(weights[1]));
        System.out.println("Retract. (x,xd,y,yd) -> xdd: "+Statik.matrixToString(superWeights[0]));
        System.out.println("Retract. (x,xd,y,yd) -> ydd: "+Statik.matrixToString(superWeights[1]));

        Statik.writeMatrix(estRetraction, "testout.txt", '\t');
        Statik.writeMatrix(allWeightsWindowed, "testoutAllWeights.txt", '\t');
        
//        for (int i=1000; i<anzPoints; i++) System.out.println(estRetraction[i][0]+"\t"+estRetraction[i][1]); 
        
    }
    
    public static void testSolution()
    {
        int tau = 10;
        int embed = 25; 
        double deltaT = 1;
      
      int half = embed / 2;
      double[][] struct = new double[embed][];
      for (int i=0; i<embed; i++) 
      {
          struct[i] = new double[]{1,(i-half)*deltaT*tau,(i-half)*deltaT*tau*(i-half)*deltaT*tau};
      }
      int[][] cov = new int[][]{
              {0,1,2},
              {1,3,-1},
              {2,-1,4}};
      int[] errPar = new int[embed];
      for (int i=0; i<embed; i++) errPar[i] = 5;
      double[] errVal = new double[embed]; for (int i=0; i<embed; i++) errVal[i] = .2;
      
      LinearModel model = new LinearModel(
              struct,
              cov,
              new double[][]{{1,0.2,0.1},{0.2,1,0},{0.1,0,1}},
              new int[]{-1,-1,-1},
              new double[]{0,0,0},
              errPar,
              errVal
              );
      double[][] data = model.createData(100);
      model.computeMoments();
      
      double[] initial = model.getParameter();

      LinearModel intermediateModel = model.transformWithPowerEquivalence();
      double errvar = intermediateModel.errVal[0];
//      intermediateModel.addErrorInfo = null;
//      intermediateModel.fixParameter(intermediateModel.errPar[0],errvar);
//      double[] estimatesInterShort = intermediateModel.estimateML(intermediateModel.getParameter());
//      double[] estimatesInter = new double[estimatesInterShort.length+1]; for (int i=0; i<estimatesInterShort.length; i++) estimatesInter[i] = estimatesInterShort[i];
//      estimatesInter[estimatesInter.length-1] = errvar;
      double[] estimatesInter = intermediateModel.estimateML(intermediateModel.getParameter());
      
      // computing full covariance matrix symbolically
      double[][] structInv = Statik.invert(intermediateModel.structure);
      double[][] dataTrans = new double[model.anzPer][];
      for (int i=0; i<model.anzPer; i++) dataTrans[i] = Statik.multiply(structInv, intermediateModel.data[i]);
      double[][] diagonal = Statik.unityMatrix(3);
      for (int i=0; i<3; i++) {diagonal[i][i] = errvar; }
      double[][] errCov = Statik.multiply(Statik.multiply(structInv,diagonal),Statik.transpose(structInv));
      double[][] symbCovariance = Statik.covarianceMatrix(dataTrans);
      Statik.subtract(symbCovariance, errCov, symbCovariance);
      double[] symbEstimate = new double[model.anzPar]; for (int i=0; i<3; i++) for (int j=0; j<3; j++) 
          if (model.covPar[i][j]!=-1) symbEstimate[model.covPar[i][j]] = symbCovariance[i][j];
      symbEstimate[model.anzPar-1] = errvar;
      double symbLL = model.computeLogLikelihood(symbEstimate);
      System.out.println("Symbolic Estimate = "+Statik.matrixToString(symbEstimate));
      System.out.println("-2ll symbolic     = "+symbLL);
      
      double[] estimate = model.estimateML(initial);
      System.out.println("Warning Flag      = "+model.warningFlag);
      System.out.println("Estimate          = "+Statik.matrixToString(estimate));
      System.out.println("-2ll estimate     = "+model.ll);

      System.out.println("Estimate Inter    = "+Statik.matrixToString(estimatesInter));
      System.out.println("-2ll estimate     = "+model.computeLogLikelihood(estimatesInter));
    }
    
    public static void testCovarianceFixation()
    {
        double[][] cov = new double[][]{{1,.2,.2},{.2,1,.2},{.2,.2,1}};
        LinearModel model = new LinearModel(Statik.identityMatrix(3), new int[][]{{0,3,4},{3,1,5},{4,5,2}}, cov,
            new int[]{-1,-1,-1},new double[]{0,0,0}, new int[]{-1,-1,-1}, new double[]{0,0,0});

        model.fixParameter(5);
        double[] initial = model.getParameter();
        int anzTrials = 100;
        double[][] erg = new double[200][5];
        for (int i=0; i<200; i++)
        {
            double[] p = new double[5];
            double v = - 0.8 + 1.6*i/200.0;
            System.out.println(v);
            for (int j=0; j<anzTrials; j++)
            {
                model.setParameter(initial);
                model.covVal[1][2] = model.covVal[2][1] = 0.2;
                model.createData(10000);        
                model.covVal[1][2] = model.covVal[2][1] = v;
                try {Statik.add(p, model.estimateML(initial),p);} catch (Exception e) {}
            }
            for (int j=0; j<5; j++) p[j] /= anzTrials;
            erg[i] = p;
        }
        System.out.println(Statik.matrixToString(erg));
    }
    
    // simulation for Thomas Busey's grand proposal
    public static void  tomBusey(String filename)
    {
        // corresponds to correlation SS of .2, and II of .38 (for Vision vs. Audition)
        double[][] cov = new double[][]{{1,0,.38,0},{0,0.33,0,0.02},{0.38,0,1,0},{0,0.02,0,0.33}};
        cov[0][1] = cov[1][0] = cov[2][3] = cov[3][2] = 0.2 * Math.sqrt(cov[1][1]*cov[0][0]);
        if (filename != null) cov = Statik.loadMatrix(filename, '\t');
        double rel1 = 0.8174, rel2 = 0.7949, err1 = (1-rel1)/rel1, err2 = (1-rel2)/rel2;
        int N = 180;
        
//        LinearModelNew half = new LinearModelNew(new double[][]{{1,0},{1,.2},{1,.4},{1,.6},{1,.8},{1,1}});
//        LinearModelNew half = new LinearModelNew(new double[][]{{1,0},{1,.2},{1,.4}});
        LinearModel half = new LinearModel(new double[][]{{1,0},{1,.5},{1,1}});
        half = half.transformWithPowerEquivalence();
        double[][] fullStruct = new double[4][4]; for (int i=0; i<2; i++) for (int j=0; j<2; j++) fullStruct[i][j] = fullStruct[i+2][j+2] = half.structure[i][j];
        LinearModel model = new LinearModel(
                fullStruct,
                new int[][]{{0,4,5,6},{4,1,7,8},{5,7,2,9},{6,8,9,3}},
                cov,
                new int[]{-1,-1,-1,-1},
                new double[]{0,0,0,0},
                new int[]{-1,-1,-1,-1},
                new double[]{err1,err1,err2,err2}
                );
        LinearModel fix = LinearModel.fixParameter(model,8, 0.0);
        double[] initialFull = model.getParameter();
        double[] initialFix  = fix.getParameter();
        
        int trials = 10000;
        int anzEC = 20;
        for (int ec = 0; ec <= 6*anzEC/4; ec++)
        {
            int succ = 0;
            double sscov = (ec/(double)anzEC)*0.5*Math.sqrt(initialFull[1]*initialFull[3]);
            initialFull[8] = sscov;
            for (int t=0; t<trials; t++)
            {
                model.createData(N,initialFull);
                model.estimateML(initialFull);
                fix.setData(model.data);
                fix.estimateML(initialFix);
                if (fix.ll - model.ll > Statik.FIVEPERCENTTHRESHOLD[0]) succ++;
            }
            double power = (double)succ / (double)trials;
            System.out.println(((ec/(double)anzEC)*0.5)+"\t"+power);
        }

    }
    
    public static void testRandomGenerator()
    {
        double[][] struct = new double[][]{{1.0, -17.931066320262865, 0.10867312921371435}, {1.0, -20.9630798744932, 0.11539310022656807}, {1.0, -24.301507856215412, 0.1225286110397416}, {1.0, -27.972651797879347, 0.13010535719943883}, {1.0, -32.00489436903776, 0.13815062317570256}, {1.0, -36.42885618630104, 0.14669338061597734}, {1.0, -41.277564058698346, 0.1557643926743334}, {1.0, -46.586631483093925, 0.16539632479204944}, {1.0, -52.39445226133119, 0.17562386232848445}, {1.0, -58.74240817173822, 0.18648383546583563}};
        LinearModel model = new LinearModel(struct);
        double[] starting = new double[]{20, 0.05, 20, 20.0, 1.0, 20.0, 0, 0, 0, 7.5};
        model.setParameter(starting);
        model.createData(10000);
        model.evaluateMuAndSigma();
        System.out.println(Statik.matrixToString(model.mu));
        System.out.println(Statik.matrixToString(model.dataMean));
        System.out.println(Statik.matrixToString(Statik.subtract(model.mu,model.dataMean)));
        
        double[] est = model.estimateML(starting);
        double[] estS = model.estimateMLFullCovarianceSupportedByPowerEquivalence();
        
        System.out.println("Population Values = "+Statik.matrixToString(starting));
        System.out.println("Estimation Values = "+Statik.matrixToString(est));
        System.out.println("Symbolic   Values = "+Statik.matrixToString(estS));
        System.out.println("Population -2ll   = "+model.computeLogLikelihood(starting));
        System.out.println("Estimation -2ll   = "+model.computeLogLikelihood(est));
        System.out.println("Symbolic   -2ll   = "+model.computeLogLikelihood(estS));
        
        for (int j=0; j<10; j++)
        {
            double mean = 0;
            for (int i=0; i<model.anzPer; i++) mean += staticRandom.nextGaussian();
            mean /= model.anzPer;
            System.out.print(mean+"\t");
        }
    }

    /** 
     * Fits the dyadic drumming system
     */
    public static void annaModell()
    {
        int[][] parOfCouples = new int[][]{{4,5,6,7},{5,8,9,10},{6,8,11,12},{7,9,11,13}}; 
        double[][] coupleD = Statik.loadMatrix("probandenzuordnung.txt",'\t');
        int[][] couple = new int[coupleD.length][coupleD[0].length]; 
        for (int i=0; i<coupleD.length; i++)
            for (int j=0; j<coupleD[i].length; j++) couple[i][j] = (int)Math.round(coupleD[i][j]);
        double[][] structure = new double[144][72 + 144];
        for (int i=0; i<structure.length; i++)
            for (int j=0; j<structure[0].length; j++) structure[i][j] = 0;
        for (int c=0; c<couple.length; c++) {
            structure[c][couple[c][0]] = 1;
            structure[c][couple[c][1]] = 1;
            structure[c][72 + c]    = 1;
        }
        
        int[][] facCovPar = new int[72+144][72+144]; double[][] facCovVal = new double[72+144][72+144];
        for (int i=0; i<facCovPar.length; i++) 
            for (int j=0; j<facCovPar[i].length; j++) {facCovPar[i][j] = LinearModel.NOPARAMETER; facCovVal[i][j] = 0.0;}
        for (int g=0; g<4; g++) 
            for (int i=0; i<18; i++) facCovPar[i+18*g][i+18*g] = g;
        for (int c=0; c<couple.length; c++)
            facCovPar[72 + c][72 + c] = parOfCouples[couple[c][0]/18][couple[c][1]/18];
        
        double[] facMeanVal = new double[144+72]; for (int i=0; i<144+72; i++) facMeanVal[i] = 0.0;
        int[] facMeanPar = new int[144+72]; for (int i=0; i<144+72; i++) facMeanPar[i] = LinearModel.NOPARAMETER; 
            
        int[] errPar = new int[144]; for (int i=0; i<144; i++) errPar[i] = 14;
        double[] errVal = new double[144];
        
        LinearModel model = new LinearModel(structure, facCovPar, facCovVal, facMeanPar, facMeanVal, errPar, errVal);
        
        double[] trueVals = new double[]{100,100,100,100,20,20,20,20,20,20,20,20,20,20,10};
        model.setParameter(trueVals);
        model.createData(1);

//        double[] leastSquareEstimate = model.leastSquareAproxLinearModel();
        double[] twollEstimate = model.estimateML(trueVals);
        
        for (int i=0; i<trueVals.length; i++)
            System.out.println(i+"\t"+trueVals[i]+"\t"+twollEstimate[i]);
    }
    
    /** 
     * Fits the dyadic drumming system assuming no variance contribution from the age group combinations
     */
    public static void annaModellSimple()
    {
        int[][] parOfCouples = new int[][]{{4,5,6,7},{5,8,9,10},{6,8,11,12},{7,9,11,13}}; 
        double[][] coupleD = Statik.loadMatrix("probandenzuordnung.txt",'\t');
        int[][] couple = new int[coupleD.length][coupleD[0].length]; 
        for (int i=0; i<coupleD.length; i++)
            for (int j=0; j<coupleD[i].length; j++) couple[i][j] = (int)Math.round(coupleD[i][j]);
        double[][] structure = new double[144][72 + 144];
        for (int i=0; i<structure.length; i++)
            for (int j=0; j<structure[0].length; j++) structure[i][j] = 0;
        for (int c=0; c<couple.length; c++) {
            structure[c][couple[c][0]] = 1;
            structure[c][couple[c][1]] = 1;
            structure[c][72 + c]    = 1;
        }
        
        int[][] facCovPar = new int[72+144][72+144]; double[][] facCovVal = new double[72+144][72+144];
        for (int i=0; i<facCovPar.length; i++) 
            for (int j=0; j<facCovPar[i].length; j++) {facCovPar[i][j] = LinearModel.NOPARAMETER; facCovVal[i][j] = 0.0;}
        for (int g=0; g<4; g++) 
            for (int i=0; i<18; i++) facCovPar[i+18*g][i+18*g] = g;
        for (int c=0; c<couple.length; c++)
            facCovPar[72 + c][72 + c] = LinearModel.NOPARAMETER;
        
        double[] facMeanVal = new double[144+72]; for (int i=0; i<144+72; i++) facMeanVal[i] = 0.0;
        int[] facMeanPar = new int[144+72]; for (int i=0; i<144+72; i++) facMeanPar[i] = LinearModel.NOPARAMETER; 
            
        int[] errPar = new int[144]; for (int i=0; i<144; i++) errPar[i] = 5;
        double[] errVal = new double[144];
        
        LinearModel model = new LinearModel(structure, facCovPar, facCovVal, facMeanPar, facMeanVal, errPar, errVal);
        
        double[] trueVals = new double[]{100,100,100,100,10};
        model.setParameter(trueVals);
        model.createData(1);

//        double[] leastSquareEstimate = model.leastSquareAproxLinearModel();
        double[] twollEstimate = model.estimateML(trueVals);
        
        for (int i=0; i<trueVals.length; i++)
            System.out.println(i+"\t"+trueVals[i]+"\t"+twollEstimate[i]);
    }
    
    // last element of active is the number of valid entries.
    public static void uniteRows(double[][] matrix, int row1, int row2, int[] active)
    {
        int anzActive = active[active.length-1];
//        System.out.println(row1+"\t"+row2);
//        System.out.println(Statik.matrixToString(matrix)); 
        for (int i=0; i<anzActive; i++) matrix[row1][active[i]] += matrix[row2][active[i]];
        for (int i=0; i<anzActive; i++) matrix[active[i]][row1] += matrix[active[i]][row2];
        int k=0;
        for (int j=0; j<anzActive-1; j++) {if (active[j]==row2) k=1; active[j] = active[j+k];}
        active[active.length-1]--;
//        System.out.println(Statik.matrixToString(matrix));
//        System.out.println(Statik.matrixToString(active)+"\r\n");
    }
    
    public static double[][] buildPrecisionFromTDE(double[][] prec, int len) {
        int dim = prec.length;
        int anz = len-dim+1;
        double[][] work = new double[anz*dim][anz*dim];
        for (int i=0; i<anz; i++) for (int j=0; j<dim; j++) for (int k=0; k<dim; k++) work[i*dim+j][i*dim+k] = prec[j][k];
        int anzActive = anz*dim;
        int[] active = new int[anzActive+1]; for (int i=0; i<anzActive; i++) active[i] = i; active[anzActive] = anzActive;
        for (int i=0; i<dim-1; i++)
            for (int j=0; j<i; j++)
                uniteRows(work, i, i+(j+1)*(dim-1), active);
        
        for (int i=0; i<anz; i++)
            for (int j=1; (j<dim) && (i*dim+(j+1)*(dim-1)<anz*dim); j++)
                uniteRows(work, i*dim+dim-1, i*dim+(j+1)*(dim-1), active);

        double[][] erg = new double[len][len];
        for (int i=0; i<len; i++) for (int j=0; j<len; j++) erg[i][j] = work[active[i]][active[j]];
//        System.out.println(Statik.matrixToString(erg));
        return erg;
    }
    
    /**
     * Computes the log likelihood that in a model of len-dim+1 blocks of length dim, independent from each other, each normally distributed with
     * cov and mean, the overlapping variables in a time delayed embedding are identical (i.e, X_21 = X_12, X_31 = X_22 = X13, ...). 
     * 
     * Uses the fact that the log likelihood for n independent variables of variance v_0,...,v_{n-1} and means m_0,...,m_{n-1} to 
     * be equal is given by
     *      ln |C| + mu C^{-1} mu = ln |C| + (1/|C|) * (sum_i sum_j (m_i-m_j)^2 prod_{k \not= i,j} v_k) 
     * where C is the covariance matrix of the n-1 differences of X_i to X_0 and mu there mean, i.p., 
     *      |C| = (prod_i v_i) (sum_i (1/v_i)) 
     * 
     * 
     * @param cov
     * @param mean
     * @param len
     * @return
     */
    public static double computeAdditionalLogLikelihood(double[][] cov, double[] mean, int len) {
        int dim = cov.length;
        double erg = 0;
        for (int j=1; j<=2*dim-3; j++) {
            int a = 0, b = j; if (j > dim-1) {a = j-(dim-1); b = dim-1;}
            double varprod = 1, varInvSum = 0;
            for (int i=a; i<=b; i++) {
                double var = cov[i][i]; varprod *= var; varInvSum += 1/var;
            }
            double cdet = varprod * varInvSum;
            double sum = 0; for (int i=a; i<=b; i++) for (int k=i+1; k<=b; k++) sum += varprod*(mean[i]-mean[k])*(mean[i]-mean[k])/(cov[i][i]*cov[k][k]);
            double ll = Math.log(cdet) + sum/cdet;
            erg += ll * (j!=dim-1?1:len-2*(dim-1));
        }
        return erg;
    }

    /** 
     * If set to true, the probability that the observations in equalObservations in different persons are identical is subtracted from the minus
     * two log likelihood, and analogously the derivatives. If null, the methods automatically sets equalObservations as to reflect a time delayed
     * embedding with gap one. 
     *  
     * @param set the subtract flag to be set
     * @return    the input if successfully.
     */
    public boolean setSubtractEqualityLikelihoods(boolean set) {
        subtractEqualityLikelihoods = set;
        if ((set) && (equalObservations==null)) {
            equalObservations = new int[2*anzVar-3][anzVar+1];
            for (int i=1; i<anzVar-1; i++) {
                equalObservations[i-1][0] = 1; 
                for (int j=0; j<=i; j++) equalObservations[i-1][j+1] = j; 
                for (int j=i+1; j<anzVar; j++) equalObservations[i-1][j+1] = -1;
            }
            equalObservations[anzVar-2][0] = anzPer-2*(anzVar-2); for (int j=0; j<anzVar; j++) equalObservations[anzVar-2][j+1] = j;
            for (int i=1; i<anzVar-1; i++) {
                equalObservations[anzVar-2+i][0] = 1; 
                for (int j=0; j<i; j++) equalObservations[anzVar-2+i][j+1+anzVar-i] = -1;
                for (int j=i; j<anzVar; j++) equalObservations[anzVar-2+i][j+1-i] = j;
            }
        }
        return subtractEqualityLikelihoods;
    }

    /**
     * subtracts the probability that the observations in equalObservations in different persons are identical is subtracted from the minus
     * two log likelihood, and analogously the derivatives. If null, the methods automatically sets equalObservations as to reflect a time delayed
     * embedding with gap one.
     * 
     *  NOTE: So far only implemented for the means!
     */
    private void subtractEqualityCondidtions() {subtractEqualityCondidtions(false,true);}
    private void subtractEqualityCondidtions(boolean recomputeMuAndSigma, boolean computeDerivatives) {
        if (recomputeMuAndSigma) evaluateMuAndSigma();
        
        for (int t=0; t<equalObservations.length; t++) {
//        for (int t=0; t<1; t++) {
            int N = equalObservations[t].length-1;
            double prod = 1, sum = 0, muSum = 0;
            for (int i=0; (i<N) && (equalObservations[t][i+1]!=-1); i++) {
                prod *= sigma[equalObservations[t][i+1]][equalObservations[t][i+1]];
                sum  += 1/sigma[equalObservations[t][i+1]][equalObservations[t][i+1]];
                muSum += mu[equalObservations[t][i+1]] / sigma[equalObservations[t][i+1]][equalObservations[t][i+1]];
            }
            double cov = prod*sum;
            double f = 0; 
            for (int i=0; (i<N) && (equalObservations[t][i+1]!=-1); i++)
                for (int j=i+1; (j<equalObservations[t].length-1) && (equalObservations[t][j+1]!=-1); j++) {
                    double a = mu[equalObservations[t][i+1]]-mu[equalObservations[t][j+1]]; 
                    f += a*a / (sigma[equalObservations[t][i+1]][equalObservations[t][i+1]]*sigma[equalObservations[t][j+1]][equalObservations[t][j+1]]);
                }
            f *= prod;
            double multiplicity = equalObservations[t][0];
            ll -= multiplicity*(Math.log(cov) + f/cov);
            if (computeDerivatives) {
                for (int p=0; p<anzPar; p++) {
                    if (parType[p]==parameterType.mean) {
                        double s = 0;
                        for (int i=0; (i<N) && (equalObservations[t][i+1]!=-1); i++) {
                            int obsNr = equalObservations[t][i+1]; 
                            s += structure[obsNr][parPos[p][0]]*2*(mu[obsNr]*(sum-1/sigma[obsNr][obsNr]) -
                                          (muSum - mu[obsNr]/sigma[obsNr][obsNr])
                                         )*prod/sigma[obsNr][obsNr];
                        }
                        llD[p] -= multiplicity*(s / cov);
                    }
                    for (int p2=p; p2<anzPar; p2++) {
                        if ((parType[p]==parameterType.mean) && (parType[p2]==parameterType.mean)) {
                            double s = 0;
                            for (int i=0; (i<N) && (equalObservations[t][i+1]!=-1); i++) {
                                int obsNr = equalObservations[t][i+1]; double prodWOi = prod/sigma[obsNr][obsNr];
                                s += structure[obsNr][parPos[p][0]]*structure[obsNr][parPos[p2][0]]*2*prodWOi*
                                    (sum-(1/sigma[obsNr][obsNr]));
                                for (int j=i+1; (j<N) && (equalObservations[t][j+1]!=-1); j++) {
                                    int obsNr2 = equalObservations[t][j+1]; double prodWOij = prodWOi/sigma[obsNr2][obsNr2];
                                    s -= 2*(structure[obsNr][parPos[p][0]]*structure[obsNr2][parPos[p2][0]]+structure[obsNr2][parPos[p][0]]*structure[obsNr][parPos[p2][0]]) *prodWOij;
                                }
                            }
                            llDD[p][p2] -= multiplicity*(s / cov); if (p2!=p) llDD[p2][p] -= multiplicity*(s / cov);
                        }
                    }
                }
            }
        }
        
        // TODO add variances.
    }
    
    public double[][] computeInverseHessianAtData(double[][] parDataCovariance, double[] parDataMean) {
        return computeInverseHessianAtData(parDataCovariance, parDataMean, null);
    }
    public double[][] computeInverseHessianAtData(double[][] parDataCovariance, double[] parDataMean, double[] starting) {
        double[][] memDataCov = dataCov; double[] memDataMean = dataMean;
        dataCov = parDataCovariance; dataMean = parDataMean;
        computeMomentsFromDataCovarianceAndMean();
        if (starting != null) estimateML(starting); else estimateML();
        double[][] erg = computeInverseFisherRespectingData();
        dataCov = memDataCov; dataMean = memDataMean; 
        return erg;
    }
    /**
     * Computes the inverse of the Hessian matrix at the given position assuming the actual data set as generating model. 
     * @return
     */
    public double[][] computeInverseFisherRespectingData() {
        if ((parWork==null) || (parWork.length!=anzPar) || (parWork[0].length!=anzPar)) parWork = new double[anzPar][anzPar];
        this.computeLogLikelihoodDerivatives(getParameter(), false);
        Statik.invert(llDD, fisherInformationMatrix, parWork);
        return fisherInformationMatrix;
    }
    
    /**
     * Computes the inverse of the Hessian matrix at the position described by the current parameters assuming that
     * the generating model is also described by this position.
     * 
     * @return
     */
    public double[][] computeInverseFisherAtPosition() {
        if ((muWork==null) || (muWork.length!=anzVar)) muWork = new double[anzVar];
        if ((muWork2==null) || (muWork2.length!=anzVar)) muWork2 = new double[anzVar];
        if ((sigmaWork==null) || (sigmaWork.length!=anzVar) || (sigmaWork[0].length!=anzVar)) sigmaWork = new double[anzVar][anzVar];
        if ((sigmaWork2==null) || (sigmaWork2.length!=anzVar) || (sigmaWork2[0].length!=anzVar)) sigmaWork2 = new double[anzVar][anzVar];
        if ((sigmaWork3==null) || (sigmaWork3.length!=anzVar) || (sigmaWork3[0].length!=anzVar)) sigmaWork3 = new double[anzVar][anzVar];
        if ((sigmaWork4==null) || (sigmaWork4.length!=anzVar) || (sigmaWork4[0].length!=anzVar)) sigmaWork4 = new double[anzVar][anzVar];
        if ((parWork==null) || (parWork.length!=anzPar) || (parWork[0].length!=anzPar)) parWork = new double[anzPar][anzPar];
        if ((sigInv==null) || (sigInv.length!=anzVar) || (sigInv[0].length!=anzVar)) sigInv = new double[anzVar][anzVar];
        double[][] erg = new double[anzPar][anzPar];
        evaluateMuAndSigma();
        Statik.invert(sigma, sigInv, sigmaWork);
        for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) {
            parWork[i][j] = 0;
            // sigma part
            computeSigmaInvDev(i, sigmaWork2);
            computeSigmaInvDev(j, sigmaWork3);
            Statik.multiply(sigmaWork2, sigmaWork3, sigmaWork4);
            sigmaWork[i][j] = 0;
            for (int k=0; k<anzVar; k++) for (int l=0; l<anzVar; l++) parWork[i][j] += sigmaWork4[k][l]*sigInv[k][l];
            
            // mu part
            computeMatrixTimesMuDev(i, null, muWork);
            computeMatrixTimesMuDev(j, null, muWork2);
            for (int k=0; k<anzVar; k++) for (int l=0; l<anzVar; l++) parWork[i][j] += 2*muWork[k]*sigInv[k][l]*muWork2[l];
        }
        Statik.invert(parWork, erg, new double[anzPar][anzPar]);
        
        return erg;
    }

    
    public static void testSteveTDE()
    {
        int twide = 10;
        int emDim = 4;
        double varI = 1.0, cov = 0.1, varS = 0.3, meanS = 1.0, err = 0.01;
        double[][] struct = new double[twide][]; for (int i=0; i<twide; i++) struct[i] = new double[]{1,i/(double)twide};
        double[][] structE = new double[emDim][]; for (int i=0; i<emDim; i++) structE[i] = new double[]{1,i/(double)twide};
        int[] errPar = new int[twide]; for (int i=0; i<twide; i++) errPar[i] = -1;
        double[] errVal = new double[twide]; for (int i=0; i<twide; i++) errVal[i] = err;
        int[] errParE = new int[emDim]; for (int i=0; i<emDim; i++) errParE[i] = -1;
        double[] errValE = new double[emDim]; for (int i=0; i<emDim; i++) errValE[i] = err;
        
        LinearModel wide = new LinearModel(struct, new int[][]{{-1,-1},{-1,-1}}, new double[][]{{varI,cov},{cov,varS}}, 
                new int[]{0,1}, new double[]{0,meanS}, errPar, errVal);
        LinearModel wideR = new LinearModel(wide); wideR.fixParameter(1);
        LinearModel embed = new LinearModel(structE, new int[][]{{-1,-1},{-1,-1}}, new double[][]{{varI,cov},{cov,varS}}, 
                new int[]{0,1}, new double[]{0,meanS}, errParE, errValE);
        LinearModel embedR = new LinearModel(embed); embedR.fixParameter(1);
        LinearModel trans = new LinearModel(wide);
        trans.evaluateMuAndSigma();
        
        double[] start = wide.getParameter();
        double[] startR = wideR.getParameter();
        
        for (int i=0; i<1000; i++) {
            wide.setParameter(start);
            wide.createData(100); 
            double[][] emData = new double[twide-emDim+1][emDim];
            for (int j=0; j<twide-emDim+1; j++) for (int k=0; k<emDim; k++) emData[j][k] = wide.data[0][j+k];
            embed.setData(emData); trans.setData(wide.data);
            embedR.setData(emData); wideR.setData(wide.data); 

            double[] estWide = wide.estimateML(start);
            double[] estEmbed = embed.estimateML(start);
            double[] estWideR = wideR.estimateML(startR);
            double[] estEmbedR = embedR.estimateML(startR);
//            double[] estWide = wide.estimateMLFullCovarianceSupportedByPowerEquivalence();
//            double[] estEmbed = embed.estimateMLFullCovarianceSupportedByPowerEquivalence();
            embed.evaluateMuAndSigma(estEmbed);
            double[][] transPrec = buildPrecisionFromTDE(Statik.invert(embed.sigma), twide);
            trans.sigma = Statik.invert(transPrec);
            double vtrans = trans.computeLogLikelihood();
            embedR.evaluateMuAndSigma(estEmbedR);
            transPrec = buildPrecisionFromTDE(Statik.invert(embedR.sigma), twide);
            trans.sigma = Statik.invert(transPrec);
            double vtransR = trans.computeLogLikelihood();
            embed.evaluateMuAndSigma(estEmbed);
            double vadd = computeAdditionalLogLikelihood(embed.sigma, embed.mu, twide);
            embedR.evaluateMuAndSigma(estEmbedR);
            double vaddR = computeAdditionalLogLikelihood(embedR.sigma, embedR.mu, twide);
            double trans2LR = embedR.ll - vaddR - embed.ll + vadd;
            
            double wideLR = wideR.ll - wide.ll, embedLR = embedR.ll - embed.ll, transLR = vtransR - vtrans;
            double embedLL = embed.ll;
            embed.setSubtractEqualityLikelihoods(true); 
            double[] estEmbedEqu = embed.estimateML(start); 
            embed.setSubtractEqualityLikelihoods(false);
            embedR.setSubtractEqualityLikelihoods(true); double[] estEmbedREqu = embedR.estimateML(startR); embedR.setSubtractEqualityLikelihoods(false);
            double embedEqLL = embed.ll, embedEqLR = embedR.ll - embed.ll;
            
            System.out.println(i+"\t"+Statik.matrixToString(estWide)+wide.ll+"\t"+wideLR+"\t"+Statik.matrixToString(estEmbed)+embedLL+"\t"+embedLR+"\t"+
                    vtrans+"\t"+transLR+"\t"+(embedLL - vadd)+"\t"+trans2LR+"\t"+Statik.matrixToString(estEmbedEqu)+embedEqLL+"\t"+embedEqLR);
        }
        
        
    }

    public static void testPanel()
    {
        LinearModel model = new LinearModel(new double[][]{{1,0},{0,1},{0,0}}, new int[][]{{0,1},{1,2}}, 
                                                  new double[][]{{1.0,0.0},{0.0,1.0}},new int[]{-1,-1},new double[]{0,0},new int[]{3,3,3}, new double[]{0.1,0.1,0.1});
        
        double[] start = model.getParameter();
        for (int i=0; i<1000; i++)
        {
            model.setParameter(start);
            model.createData(1);
//            double[] est = model.estimateML(start);
            double[] est = model.estimateMLFullCovarianceSupportedByPowerEquivalence();
            System.out.println(Statik.matrixToString(est));
        }
    }
    
    public static void testSteveTDEAdditionalVariance() {
        int halfWide = 3;
        double errWide = 0.1, errAlt = .1;
        double varI = 1, cov = 0, varS = 1;
        double meanS = 0;
        int N = 100;
        int trials = 100000;
        
        int wide = 2*halfWide-1;
        double[][] struct = new double[wide][]; for (int i=0; i<wide; i++) struct[i] = new double[]{1,-0.5 + i/(double)(wide-1)};
        double[][] structAlt = new double[halfWide][]; 
        for (int i=0; i<halfWide; i++) structAlt[i] = new double[]{1,-0.25 + i/(double)(wide-1)};
//      double[][] struct = new double[wide][]; for (int i=0; i<wide; i++) struct[i] = new double[]{1,1};
//      double[][] structAlt = new double[halfWide][]; 
//      for (int i=0; i<halfWide; i++) structAlt[i] = new double[]{1,1};
        int[] errParWide = new int[wide]; for (int i=0; i<wide; i++) errParWide[i] = -1;
        int[] errParAlt = new int[halfWide]; for (int i=0; i<halfWide; i++) errParAlt[i] = -1;
        double[] errValWide = new double[wide]; for (int i=0; i<wide; i++) errValWide[i] = errWide;
        double[] errValAlt = new double[halfWide]; for (int i=0; i<halfWide; i++) errValAlt[i] = errAlt;
        LinearModel wideModel = new LinearModel(struct, new int[][]{{-1,-1},{-1,0}}, new double[][]{{varI,cov},{cov,varS}}, 
                new int[]{-1,1}, new double[]{0,meanS}, errParWide, errValWide);
        LinearModel wideR = new LinearModel(wideModel); wideR.fixParameter(0);
        LinearModel altModel = new LinearModel(structAlt, new int[][]{{-1,-1},{-1,0}}, 
                new double[][]{{varI,cov},{cov,varS}}, 
                new int[]{-1,1}, new double[]{0,meanS}, errParAlt, errValAlt);
        LinearModel altR = new LinearModel(altModel); altR.fixParameter(0);
        double[] weights = new double[wide]; for (int i=0; i<wide; i++) weights[i] = struct[i][1]; 
        
        double[] trueV = wideModel.getParameter();
        int anzPar = trueV.length;
        int[] picedRows = new int[halfWide]; for (int i=0; i<halfWide; i++) picedRows[i] = i+halfWide/2;
        wideModel.evaluateMuAndSigma();
        double[][] subMatrix = Statik.submatrix(wideModel.sigma, picedRows, picedRows);
        double[] subMean = Statik.subvector(wideModel.mu, picedRows);

        double[][] wideVar = Statik.multiply(2,wideModel.computeInverseFisherAtPosition());
        double[][] altVar = Statik.multiply(2,altModel.computeInverseFisherAtPosition());
        altModel.anzPer = N;

        altModel.dataCov = subMatrix; altModel.dataMean = subMean;
        altModel.computeMomentsFromDataCovarianceAndMean();
        altModel.estimateML(trueV);
        double[][] parCov = altModel.getParameterDistributionCovariance(subMatrix, subMean, trueV);
        
        System.out.println("Variance wide                     = \r\n"+Statik.matrixToString(wideVar));
        System.out.println("Variance alternative              = \r\n"+Statik.matrixToString(altVar));
        System.out.println("Variance alt. (wide distribution) = \r\n"+Statik.matrixToString(parCov));
        
        int miss = 0;
        double[] meanWideEst = new double[anzPar], meanAltEst = new double[anzPar];
        double[][] varWideEst = new double[anzPar][anzPar], varAltEst = new double[anzPar][anzPar];
        double[][] altData = new double[N][halfWide];
        for (int tr=0; tr<trials; tr++) {
            try {
                wideModel.setParameter(trueV); wideModel.createData(N); 
                for (int k=0; k<N; k++) for (int j=0; j<halfWide; j++) altData[k][j] = wideModel.data[k][j+halfWide/2]; 
                altModel.setData(altData); 
                
                double[] est = wideModel.estimateML(trueV); 
                for (int i=0; i<anzPar; i++) meanWideEst[i] += est[i];
                for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) varWideEst[i][j] += est[i]*est[j];
                est = altModel.estimateML(trueV);
                for (int i=0; i<anzPar; i++) meanAltEst[i] += est[i];
                for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) varAltEst[i][j] += est[i]*est[j];
                
            } catch (Exception e)  {miss++;}
        }
        Statik.multiply(1.0/(double)trials,meanWideEst, meanWideEst); Statik.multiply(1.0/trials,varWideEst, varWideEst);
        for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) varWideEst[i][j] -= meanWideEst[i]*meanWideEst[j];
        Statik.multiply(1.0/(double)trials,meanAltEst, meanAltEst); Statik.multiply(1.0/trials,varAltEst, varAltEst);
        for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) varAltEst[i][j] -= meanAltEst[i]*meanAltEst[j];

        System.out.println("Misses:\t"+miss);
        System.out.println("Variance in the wide model          = \r\n"+Statik.matrixToString(varWideEst));
        System.out.println("Total variance alt model            = \r\n"+Statik.matrixToString(varAltEst));
        
        System.out.println("Estimate wide model = "+Statik.matrixToString(meanWideEst));
        System.out.println("Estimate alt. model = "+Statik.matrixToString(meanAltEst));
    }
    
    private static void fitCensusData() {
        double[][] fileData = Statik.loadMatrix("population.txt", '\t');
        String[] south = new String[]{"CA","CO","TX","TN","GA","FL","SC","NC","VA"};
        String[] north = new String[]{"ND","SD","IL","MI","PA","NY","MA","NJ"};
        String[] header = new String[]{"U.S.","AL","CA","CO","CT","DE","DC","FL","GA","ID","IL","IN","IA","KS","KY","LA","ME","MD","MA","MI","MN","MS","MO","MT",
            "NE","NV","NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI","SC","SD","TN","TX","UT","VT","VA","WA","WV","WI","WY"};
        int fraction = 15;
        int anzVar = fileData.length/fraction;
        System.out.println("Every "+fraction+"th observation, "+anzVar+" total.");
        
        double[][] dataSouth = new double[south.length][anzVar];
        double[][] dataNorth = new double[north.length][anzVar];
        int s = 0, n = 0;
        for (int i=0; i<header.length; i++) {
            if ((s < south.length) && (header[i].equals(south[s]))) {for (int j=0; j<anzVar; j++) dataSouth[s][j] = fileData[j*fraction][i]; s++;}  
            if ((n < north.length) && (header[i].equals(north[n]))) {for (int j=0; j<anzVar; j++) dataNorth[n][j] = fileData[j*fraction][i]; n++;}  
        }
        double[][] struct = new double[anzVar][3]; for (int i=0; i<anzVar; i++) struct[i] = new double[]{1,i,Math.exp((double)i/(double)anzVar)};
        LinearModel modelNorth = new LinearModel(struct, new double[][]{{1,0,0},{0,1,0},{0,0,1}}, new double[]{0,0,0}, 10.0);
        LinearModel modelSouth = new LinearModel(struct, new double[][]{{1,0,0},{0,1,0},{0,0,1}}, new double[]{0,0,0}, 10.0);
        modelNorth.fixParameter(5); modelNorth.fixParameter(6); modelNorth.fixParameter(6);
        modelNorth.setData(dataNorth);
        modelSouth.setData(dataSouth);
        double[] estNorth = modelNorth.estimateML();
        double[] estSouth = modelSouth.estimateML();
        
        System.out.println("North = "+Statik.matrixToString(estNorth));
        System.out.println("South = "+Statik.matrixToString(estSouth));
        
        System.out.println("-2ll North = "+modelNorth.ll);
        System.out.println("-2ll South = "+modelSouth.ll);
        
    }
    
    private static void testFisher() {
        LinearModel model = new LinearModel(new double[][]{{1,-1},{1,0},{1,1}}, new double[][]{{1,-0.3},{-0.3,1}}, new double[]{5,-3}, 0.1);
        model.evaluateMuAndSigma(); model.anzPer = 1;
        model.dataMean = Statik.copy(model.mu); model.dataCov = Statik.copy(model.sigma); model.computeMomentsFromDataCovarianceAndMean();
        model.computeLogLikelihoodDerivatives(model.getParameter(), true);
        double[][] fish = model.computeFisherMatrix(model.sigma, model.mu, null);
        System.out.println("Hessian = \r\n"+Statik.matrixToString(model.llDD));
        System.out.println("Fisher  = \r\n"+Statik.matrixToString(fish));
    }
    
    public static void testSteveTDE2() {
        int twide = 20;
        int emDim = 4;

        boolean embedOverlap = false;
        int embeddingN = (embedOverlap?twide-emDim+1:twide/emDim);
        double varI = 1.0, cov = 0.2, varS = 0.3, meanS = 0.0, err = 0.05;
        double[][] struct = new double[twide][]; for (int i=0; i<twide; i++) struct[i] = new double[]{1,-0.5 + i/(double)(twide-1)};
        double[][] structE = new double[emDim][]; for (int i=0; i<emDim; i++) structE[i] = new double[]{1,-(emDim-1)/(2*(double)(twide-1)) + i/(double)(twide-1)};
        int[] errPar = new int[twide]; for (int i=0; i<twide; i++) errPar[i] = -1;
        double[] errVal = new double[twide]; for (int i=0; i<twide; i++) errVal[i] = err;
        int[] errParE = new int[emDim]; for (int i=0; i<emDim; i++) errParE[i] = -1;
        double[] errValE = new double[emDim]; for (int i=0; i<emDim; i++) errValE[i] = err;
        
        LinearModel wide = new LinearModel(struct, new int[][]{{-1,-1},{-1,-1}}, new double[][]{{varI,cov},{cov,varS}}, 
                new int[]{-1,0}, new double[]{0,meanS}, errPar, errVal);
        LinearModel wideR = new LinearModel(wide); wideR.fixParameter(0);
        LinearModel embed = new LinearModel(structE, new int[][]{{-1,-1},{-1,-1}}, new double[][]{{varI,cov},{cov,varS}}, 
                new int[]{-1,0}, new double[]{0,meanS}, errParE, errValE);
        LinearModel embedR = new LinearModel(embed); embedR.fixParameter(0);

        double wideVar = 2*wide.computeInverseFisherAtPosition()[0][0];
        double embedVar = 2*embed.computeInverseFisherAtPosition()[0][0];
        double faktor = wideVar / (embedVar/(double)embeddingN);
        System.out.println("Fisher factor = \t"+faktor);
        System.out.println(wideVar+"\t"+"\t"+"\t"+"1.0\t"+(embedVar/(double)embeddingN)+"\t"+"\t"+"\t"+faktor);
        
        double[] trueV = wide.getParameter();
        int miss = 0;
        double mean1 = 0, mean2 = 0;
        int trials = 1000;
        for (int i=0; i<trials; i++) {
            try {
//                System.out.print(".");
                wide.setParameter(trueV);
                wide.createData(1); wideR.setData(wide.data);
                double[][] emData = new double[embeddingN][emDim];
                if (embedOverlap) for (int j=0; j<twide-emDim+1; j++) for (int k=0; k<emDim; k++) emData[j][k] = wide.data[0][j+k];
                else for (int j=0; j<embeddingN; j++) for (int k=0; k<emDim; k++) emData[j][k] = wide.data[0][j*emDim+k];
                embed.setData(emData); embedR.setData(emData);
                
                double[] estWide = wide.estimateML(trueV); wideR.computeLogLikelihood(null, true); double wideRLL = wideR.ll;
                double[] estEmbed = embed.estimateML(trueV); double embedLL = embed.ll; embedR.computeLogLikelihood(null, true); double embedRLL = embedR.ll;
                
                mean1 += (embedR.ll - embed.ll);
                mean2 += (wideR.ll - wide.ll);
                
                
        //        System.out.println("True values  : "+Statik.matrixToString(trueV));
        //        System.out.println("Wide         : "+Statik.matrixToString(estWide)+wide.ll+"\t"+wideRLL+"\t"+(wideRLL-wide.ll));
        //        System.out.println("Normal       : "+Statik.matrixToString(estEmbed)+embedLL+"\t"+embedRLL+"\t"+(embedRLL-embedLL));
        //        System.out.println("With Equality: "+Statik.matrixToString(estEmbedCorr)+embedCorrLL+"\t"+embedRCorrLL+"\t"+(embedRCorrLL-embedCorrLL));
                
                System.out.println(Statik.matrixToString(estWide)+wide.ll+"\t"+wideRLL+"\t"+(wideRLL-wide.ll)+"\t"+
                                   Statik.matrixToString(estEmbed)+embedLL+"\t"+embedRLL+"\t"+(embedRLL-embedLL));
            } catch (Exception e)  {miss++;}
        }
        double erg = mean1/mean2;
        
        
        System.out.println("\r\nEmpirical Ratio =\t"+erg+" ("+(erg/faktor)+")");
        System.out.println("Misses: "+miss);
        
    }
    
//    private AnalyticalFunction analyticalFunctionOfLL() {
//        final LinearModel fthis = this;
//        return new AnalyticalFunction() {
//
//            public int anzPar() {return fthis.anzVar+fthis.anzPar;}
//
//            public double eval(double[] val) {
//                if (fthis.anzPer!=1) fthis.setData(new double[1][fthis.anzVar]);
//                for (int i=0; i<fthis.anzVar; i++) fthis.data[0][i] = val[i];
//                fthis.computeMoments(true);
//                for (int i=0; i<fthis.anzPar; i++) fthis.setParameter(i, val[i+fthis.anzVar]);
//                fthis.evaluateMuAndSigma();
//                return fthis.computeLogLikelihood();
//            }
//        };
//    }
//    
//    private static void testDerivativesWithOneDataPoint() {
//        LinearModel model = new LinearModel(new double[][]{{1,-1},{1,0},{1,1}}, new double[][]{{1,-0.3},{-0.3,1}}, new double[]{5,-3}, 0.1);
//        model.setData(new double[][]{{-10,0,5}});
//        double[] pos = new double[model.anzPar+model.anzVar]; 
//        for (int i=0; i<model.anzVar; i++) pos[i] = model.data[0][i];
//        for (int i=0; i<model.anzPar; i++) pos[i+model.anzVar] = model.getParameter(i);
//        AnalyticalFunction f = model.analyticalFunctionOfLL();
//        
//        double[][] numDev = new double[model.anzPar][model.anzVar];
//        for (int i=0; i<model.anzPar; i++) for (int j=0; j<model.anzVar; j++) numDev[i][j] = f.evalDev(new int[]{i+model.anzVar,j}, pos);
//        
//        double[][] symDev = model.getGradientDerivativeWRTData();
//        
//        System.out.println("Numerically = \r\n"+Statik.matrixToString(numDev));
//        System.out.println("Symbolically = \r\n"+Statik.matrixToString(symDev));
//    }
    
    private static void testAndysGroups() {
        LinearModel model = new LinearModel(new double[][]{{1,0},{1,1},{1,2},{1,3},{1,4}}, new int[][]{{0,-1},{-1,1}}, new double[][]{{1,0},{0,1}},new int[]{2,3}, new double[]{0,0}, 4, 1);
        double[][] dataGroup1 = Statik.loadMatrix("group1.txt", '\t');
        double[][] dataGroup2 = Statik.loadMatrix("group2.txt", '\t');
        model.setData(dataGroup1);
        System.out.println(Statik.matrixToString(model.dataMean));
        System.out.println(Statik.matrixToString(model.dataCov));
        double[] start = model.getParameter();
        double[] est1 = model.estimateLS();
        System.out.println("Estimate Group 1 = "+Statik.matrixToString(est1)+", ls = "+model.ls);
        model.setData(dataGroup2);
        double[] est2 = model.estimateLS();
        System.out.println("Estimate Group 2 = "+Statik.matrixToString(est2)+", ls = "+model.ls);
        double[][] dataBoth = new double[dataGroup1.length + dataGroup2.length][];
        for (int i=0; i<dataGroup1.length; i++) dataBoth[i] = dataGroup1[i];
        for (int i=0; i<dataGroup2.length; i++) dataBoth[dataGroup1.length+i] = dataGroup2[i];
        model.setData(dataBoth);
        double[] estBoth = model.estimateLS();
        System.out.println("Estimate Both = "+Statik.matrixToString(estBoth)+", ls = "+model.ls);
           
        
    }
    
    public static void main(String args[])
    {
    }
    
    /**
     * Creates a LGCM with given degree and number of observations
     *  
     * @param degree
     * @param observations
     * @return
     */
    public static LinearModel getLatentGrowthCurveModel(int degree, int observations) {
        int anzVar = observations;
        int anzFac = degree+1;
        double[][] struct = new double[anzVar][anzFac];
        for (int i=0; i<anzVar; i++) {
            double t = -1 + 2* (double)i/(double)(anzVar-1);
            for (int j=0; j<anzFac; j++) struct[i][j] = Math.pow(t,j);
        }
        return new LinearModel(struct);
    }
    
    
}
