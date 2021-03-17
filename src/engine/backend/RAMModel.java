/*
 * Created on 10.11.2011
 */
package engine.backend;

import java.util.Vector;
import java.util.logging.LogRecord;







import arithmetik.FastPolynomial;
import arithmetik.QPolynomial;
import arithmetik.Qelement;
//import arithmetik.Squarerootable;
import engine.*;
import groebner.DoubleField;
import groebner.DoublePolynomial;
import groebner.Monomial;

/**
 * Takes a model in RAM notation via matrices A (asymmetrical, for single-headed arrows) and S (symmetrical, for double headed arrows), a mean vector,
 * and a filter that selectes anzVar rows and columns from the matrix (represented in this comment, but not in the code as matrix multiplication with a filter) 
 * 
 * The covariance matrix is (
 *                                              
 *                                                       Sigma = F (I-A)^{-1} S (I-A)^{-T} F^T
 *                                                       
 * The matrix allows a single parameter at every position of A,S and m, possibly repeating. Dummy inner nodes
 * can be used to model non-linear structure matrix parts.  
 * 
 * Note: d(I-A)^{-1} / dtheta = (I-A)^{-1} dA/dtheta (I-A)^{-1}     (without any transposes)
 *
 *   
 *   
 *   
 * @author Timo
 */
public class RAMModel extends NumericalDerivativeModel {

    private final double UNUSED = -999999;

    public int anzFac;     // number of all variables, including inner nodes and manifest
    
    public double[][] symVal;
    public int[][] symPar;
    public double[][] asyVal;
    public int[][] asyPar;
    public double[] meanVal;
    public int[] meanPar;
    public int[] filter;
    
    public double[][] fisherInformationMatrix;
    public double[][] iMinusAInv;
    
    public double[][] FTSigInvF,E,sigmaInvFB, sigmaInvFE,BSBTFTSigInvFB,BSBTFTSigInvFBSBT,BTFTSigInvFB,
        BTFTSigInvFBSBT, sigmaInvData, iMinusSigmaInvData, BT, BSBTFTC, BTFT, BTFTC, 
        EFTSigInvD, BTFTSigInvD, EFT, EFTC, anzPerDataCov, FB, FE;
    public double[] meanBig;
    
    // Some working variables
    double[][] facWork2, facWork3, facWork4, sigmaWork, sigmaWork2, sigmaWork3, sigmaWork4, structureWork, structureTransWork, parWork, muDev, BmDev, 
        latSq1, latSq2, latSq3, latRct1, latRct2, latRct3;
    double[] meanWork, muWork, muWork2, posWork, p1w, p2w, cew, a1w, a2w, b, workVec, workVec2, workVecLat, workVecVar,workVecVar2,sigmaInvb, Bm;
    int[] sDevRow, sDevCol, cDevRow, cDevCol, mDev;
    double[][][] pa1,pa2,pa3,pa4,pa5, BADev;
    boolean[] paAct1, paAct2, paAct3, paAct4, paAct5;
    int[][] asyParT;
    
    public double[][][][] squareBracket;
    public boolean[][] bracketsAct;
    public boolean[] boolWork;
    public double[][] sigmaBig, sigmaBigInv, facWork1, B, BTrans, C, BC, DBig, sigmaInvB, BTC, sigmaMinusD;
    
    // A representation of all variables as polynomial. Each variable is a polynomial expression of independent normally distributed
    // variables with variance polynomialRepresentationVariances, which are the polynomial's variables. Used in particular for product-SEMs. Values are only created with explicit call of
    // computePolynomialRepresentation, exists only on acyclic A-matrices.
    private DoublePolynomial[] polynomialRepresentation;
    private double[] polynomialRepresentationVariances;
    
    public boolean[] isMultiplicationVariable;

    public RAMModel() {}
    
    public RAMModel(int[][] symPar, double[][] symVal, int[][] asyPar, double[][] asyVal, int[] meanPar, double[] meanVal, int anzVar) {
        anzFac = symPar.length;
        this.anzVar = anzVar;
        this.symPar = symPar; this.symVal = symVal; this.asyPar = asyPar; this.asyVal = asyVal; this.meanPar = meanPar; this.meanVal = meanVal;
        this.filter = new int[anzVar]; for (int i=0; i<anzVar; i++) filter[i] = (anzFac-anzVar)+i; 

        inventParameterNames();
        setAnzParAndCollectParameter(-1);            
        anzPer = 0; data = new double[0][];
        
        fisherInformationMatrix = new double[anzPar][anzPar];
    }
    
    public RAMModel(RAMModel toCopy) {
        this.anzFac = toCopy.anzFac;
        this.anzVar = toCopy.anzVar;
        this.symPar = Statik.copy(toCopy.symPar); this.symVal = Statik.copy(toCopy.symVal); this.asyPar = Statik.copy(toCopy.asyPar); this.asyVal = Statik.copy(toCopy.asyVal); 
        this.meanPar = Statik.copy(toCopy.meanPar); this.meanVal = Statik.copy(toCopy.meanVal);
        
        this.paraNames = Statik.copy(toCopy.paraNames);
        this.position = Statik.copy(toCopy.position);
        this.anzPar = toCopy.anzPar;
        if (toCopy.isIndirectData) this.setDataDistribution(toCopy.dataCov, toCopy.dataMean, toCopy.anzPer); 
        else this.setData(Statik.copy(toCopy.data), Statik.copy(toCopy.auxiliaryData), Statik.copy(toCopy.controlData));
        this.filter = Statik.copy(toCopy.filter); 
        this.startingValues= Statik.copy(toCopy.startingValues);
        this.anzPer = toCopy.anzPer;
        this.dataForeignKey = (toCopy.dataForeignKey!=null?Statik.copy(toCopy.dataForeignKey):null);
        if (toCopy.isMultiplicationVariable!=null) this.isMultiplicationVariable = Statik.copy(toCopy.isMultiplicationVariable);
    }

    public RAMModel(int[][] symPar, double[][] symVal, int[][] asyPar, double[][] asyVal, int[] meanPar, double[] meanVal, int[] filter) {
        anzFac = symPar.length;
        this.anzVar = filter.length;
        this.symPar = symPar; this.symVal = symVal; this.asyPar = asyPar; this.asyVal = asyVal; this.meanPar = meanPar; this.meanVal = meanVal;
        this.filter = Statik.copy(filter); 

        inventParameterNames();
        setAnzParAndCollectParameter(-1);            
        anzPer = 0; data = new double[0][];
        
        fisherInformationMatrix = new double[anzPar][anzPar];
    }
    
    public void setData(double[][] data, double[][] auxiliaryData, double[][] controlData) {
        super.setData(data);
        this.auxiliaryData = auxiliaryData;
        this.controlData = controlData;
        if (auxiliaryData != null && auxiliaryData.length != 0) anzAux = auxiliaryData[0].length; else anzAux = 0;
        if (controlData != null && controlData.length != 0) anzCtrl = controlData[0].length; else anzCtrl = 0;
    }
    
    public void setFilter(int[] newFilter) {
        anzVar = newFilter.length;
        filter = newFilter;
    }
    
    @Override
    public void computeLeastSquaresDerivatives(double[] value, boolean recomputeMuAndSigma) {
        
        // semantics of squareBracket in RAM derivative paper (counting starts with zero here and with one in the paper)
        
        // For least squares, 
        // bracket 1,2,3 -> 0, FBA'EF^T
        // bracket 4,5,6 -> 3, FEA'B^TF^T (which is bracket 1 transposed)
        // brackets 7,8,9 -> 6, FBS'B^TF^T
        // as in paper, 10-17 -> 9-16, with C = (Sigma - D)
        // 17-23 and all gaps are unused.

        if (squareBracket==null) squareBracket = new double[24][][][];
        squareBracket[0] = Statik.ensureSize(squareBracket[0],anzPar, anzVar, anzVar);
        squareBracket[3] = Statik.ensureSize(squareBracket[3],anzPar, anzVar, anzVar);
        squareBracket[6] = Statik.ensureSize(squareBracket[6],anzPar, anzVar, anzVar);
        squareBracket[17] = Statik.ensureSize(squareBracket[17],anzPar, anzVar, anzVar);
        squareBracket[9] = Statik.ensureSize(squareBracket[9],anzPar, anzVar, anzFac);
        squareBracket[13] = Statik.ensureSize(squareBracket[13],anzPar, anzVar, anzFac);
        squareBracket[15] = Statik.ensureSize(squareBracket[15],anzPar, anzVar, anzFac);
        squareBracket[16] = Statik.ensureSize(squareBracket[16],anzPar, anzVar, anzFac);
        squareBracket[10] = Statik.ensureSize(squareBracket[10],anzPar, anzFac, anzVar);
        squareBracket[11] = Statik.ensureSize(squareBracket[11],anzPar, anzFac, anzVar);
        squareBracket[12] = Statik.ensureSize(squareBracket[12],anzPar, anzFac, anzVar);
        squareBracket[14] = Statik.ensureSize(squareBracket[14],anzPar, anzFac, anzVar);
        asyParT = Statik.ensureSize(asyParT, anzFac, anzFac);
        facWork1 = Statik.ensureSize(facWork1, anzFac, anzFac);
        facWork2 = Statik.ensureSize(facWork2, anzFac, anzFac);
        sigmaWork = Statik.ensureSize(sigmaWork, anzVar, anzVar);
        sigma = Statik.ensureSize(sigma, anzVar, anzVar);
        E = Statik.ensureSize(E, anzFac, anzFac);
        EFT = Statik.ensureSize(EFT, anzFac, anzVar);
        EFTC = Statik.ensureSize(EFTC, anzFac, anzVar);
        FB = Statik.ensureSize(FB, anzVar, anzFac);
        FE = Statik.ensureSize(FE, anzVar, anzFac);
        C = Statik.ensureSize(C, anzVar, anzVar);
        B = Statik.ensureSize(B, anzFac, anzFac);
        BT = Statik.ensureSize(BT, anzFac, anzFac);
        BTFT = Statik.ensureSize(BTFT,anzFac, anzVar);
        BTFTC = Statik.ensureSize(BTFTC,anzFac, anzVar);
        b = Statik.ensureSize(b, anzVar);
        mu = Statik.ensureSize(mu, anzVar);
        workVec = Statik.ensureSize(workVec, anzFac);
        workVec2 = Statik.ensureSize(workVec2, anzFac);
        workVecVar = Statik.ensureSize(workVecVar, anzVar);
        workVecVar2 = Statik.ensureSize(workVecVar2, anzVar);
        muDev = Statik.ensureSize(muDev, anzPar, anzVar);
        Bm = Statik.ensureSize(Bm, anzFac);
        BmDev = Statik.ensureSize(BmDev, anzPar, anzFac);
        BADev = Statik.ensureSize(BADev, anzPar, anzFac, anzFac);
        lsD = Statik.ensureSize(lsD, anzPar);
        lsDD = Statik.ensureSize(lsDD, anzPar, anzPar);
        
        // preparing dataCov
//        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) dataCov[i][j] = xBiSum[i][j]/(double)anzPer - dataMean[i]*dataMean[j]; 
        
        // Preparing B, C, E   (B and E like in paper, but C = (Sigma - S)
        if (value != null) setParameter(value); else value = getParameter();
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) asyParT[i][j] = asyPar[j][i];
        setParameter(value);
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) facWork1[i][j] = (i==j?1:0) - asyVal[i][j];
        Statik.invert(facWork1, B, facWork2);
        Statik.transpose(B, BT);
        Statik.multiply(B, symVal, facWork1); Statik.multiply(facWork1, BT, E);
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) sigma[i][j] = E[filter[i]][filter[j]];
//        Statik.invert(sigma, sigInv, sigmaWork);
//        double lndet = Statik.logResult;
        Statik.subtract(sigma, dataCov, C);
        
        // Preparing filtered matrices
        matrixFilterMatrix(null, B, FB, true);
        matrixFilterMatrix(null, E, FE, true);
        matrixFilterMatrix(E, null, EFT, false);
        Statik.multiply(EFT, C, EFTC);
        matrixFilterMatrix(BT, null, BTFT, false);
        Statik.multiply(BTFT, C, BTFTC);
        
        // Preparing boxes from RAM derivative paper
        computeMatSpaMat(FB, asyPar, EFT, squareBracket[0]);
        for (int i=0; i<anzPar; i++) Statik.transpose(squareBracket[0][i], squareBracket[3][i]);
        computeMatSpaMat(FB, symPar, BTFT, squareBracket[6]);
        for (int p=0; p<anzPar; p++) {
            Statik.add(squareBracket[0][p],squareBracket[3][p],squareBracket[17][p]); Statik.add(squareBracket[6][p], squareBracket[17][p], squareBracket[17][p]);
        }
        computeMatSpaMat(FB, asyPar, null, squareBracket[9]);
        computeMatSpaMat(B, asyPar, EFTC, squareBracket[10]);
        computeMatSpaMat(B, symPar, BTFTC, squareBracket[11]);
        computeMatSpaMat(E, asyParT, BTFTC, squareBracket[12]);
        computeMatSpaMat(FE, asyParT, BT, squareBracket[13]);
        computeMatSpaMat(null, asyParT, BTFTC, squareBracket[14]);
        computeMatSpaMat(FB, symPar, BT, squareBracket[15]);
        computeMatSpaMat(FB, asyPar, E, squareBracket[16]);

        // Preparing mu, Bm, b, and BADev
        Statik.setToZero(mu);
        Statik.multiply(B, meanVal, Bm);
        for (int i=0; i<filter.length; i++) mu[i] = Bm[filter[i]];

        Statik.subtract(mu, dataMean, b);
        computeMatSpaMat(B, asyPar, null, BADev);
        
        // Computation of least square index, covariance contribution and mean contribution
        ls = 0;
        for (int i=0; i<anzVar; i++)
            for (int j=0; j<anzVar; j++) ls += 0.5*(sigma[i][j] - dataCov[i][j])*(sigma[i][j] - dataCov[i][j]);
        for (int i=0; i<anzVar; i++) ls += 0.5*(mu[i] - dataMean[i])*(mu[i] - dataMean[i]);
        
        // covariance contribution to first and second derivative
        for (int p1 = 0; p1 < anzPar; p1++) {
            lsD[p1] = 0; lsD[p1] += dotMultiply(squareBracket[17][p1],C);
            for (int p2 = p1; p2 < anzPar; p2++) {
                lsDD[p1][p2] = dotMultiply(squareBracket[17][p1],squareBracket[17][p2]);
                lsDD[p1][p2] += dotMultiply(squareBracket[9][p1], squareBracket[10][p2]) + dotMultiply(squareBracket[9][p2],squareBracket[10][p1]) + 
                               dotMultiply(squareBracket[9][p1], squareBracket[11][p2]) + dotMultiply(squareBracket[9][p1],squareBracket[12][p2]) +
                               dotMultiply(squareBracket[9][p2], squareBracket[11][p1]) + dotMultiply(squareBracket[13][p2],squareBracket[14][p1]) +
                               dotMultiply(squareBracket[13][p1], squareBracket[14][p2]) + dotMultiply(squareBracket[15][p2],squareBracket[14][p1]) +
                               dotMultiply(squareBracket[16][p2], squareBracket[14][p1]) + dotMultiply(squareBracket[15][p1],squareBracket[14][p2]);
                lsDD[p2][p1] = lsDD[p1][p2];
            }
        }
        
        // preparing BmDev and muDev
        for (int p=0; p<anzPar; p++) {
            Statik.setToZero(BmDev[p]);
            for (int i=0; i<anzFac; i++) if (meanPar[i]==p) for (int j=0; j<anzFac; j++) BmDev[p][j] += B[j][i];
            Statik.multiply(BADev[p],Bm,workVec);
            Statik.setToZero(muDev[p]);
            for (int j=0; j<anzVar; j++) muDev[p][j] += workVec[filter[j]] + BmDev[p][filter[j]];
        }
        
        // Mean contribution to first and second derivative
        for (int p1 = 0; p1 < anzPar; p1++) {
            lsD[p1] += Statik.multiply(muDev[p1],b);
            
            for (int p2 = p1; p2 < anzPar; p2++) {
                lsDD[p1][p2] += Statik.multiply(muDev[p1],muDev[p2]);
                
                // Term with second derivative of mu
                Statik.multiply(BADev[p1], BmDev[p2], workVec); for (int i=0; i<anzVar; i++) workVecVar[i] = workVec[filter[i]];
                double adder = Statik.multiply(b, workVecVar);
                Statik.multiply(BADev[p2], BmDev[p1], workVec); for (int i=0; i<anzVar; i++) workVecVar[i] = workVec[filter[i]];
                adder += Statik.multiply(b, workVecVar);
                Statik.multiply(BADev[p2], Bm, workVec); Statik.multiply(BADev[p1], workVec, workVec2); for (int i=0; i<anzVar; i++) workVecVar[i] = workVec2[filter[i]];
                adder += Statik.multiply(b, workVecVar);
                Statik.multiply(BADev[p1], Bm, workVec); Statik.multiply(BADev[p2], workVec, workVec2); for (int i=0; i<anzVar; i++) workVecVar[i] = workVec2[filter[i]];
                adder += Statik.multiply(b, workVecVar);
                                
                lsDD[p1][p2] += adder; lsDD[p2][p1] = lsDD[p1][p2];
            }
        }
    }

    /**
     * Computes left * dmid/dt * right for all theta. Assumes the pa matrices to be of correct number (anzPar) with correct sizes, which may differ.
     * zero Matrices are marked as false in paAct. left and right may be null, in that case assumed to be identity.
     * left may have any number of rows, right may have any number of columns. If null, assumed to be big (anzFac x anzFac) 
     * Assumes facWork1 to be unused and prepared.  
     *  
     * @param left
     * @param right
     * @param p
     */
    private void computeMatSpaMat(double[][] left, int[][] midPar, double[][] right, double[][][] pa, boolean[] paAct) {
        for (int i=0; i<anzPar; i++) for (int j=0; j<anzFac; j++) {paAct[i] = false; pa[i][j][0] = UNUSED;}
        // multiplication with right
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (midPar[i][j] != -1) {
            paAct[midPar[i][j]] = true;
            if (pa[midPar[i][j]][j][0] == UNUSED) for (int k=0; k<anzFac; k++) pa[midPar[i][j]][i][k] = 0.0;
            if (right == null) pa[midPar[i][j]][i][j] += 1;
            else for (int k=0;k<right[0].length; k++) pa[midPar[i][j]][i][k] += right[j][k];
        }
        for (int p=0; p<anzPar; p++) if (paAct[p]) {
            Statik.copy(pa[p], facWork1); Statik.setToZero(pa[p]);
            for (int k=0; k<anzFac; k++) if (facWork1[k][0] != UNUSED) {
                if (left == null) for (int j=0; j<anzFac; j++) pa[p][k][j] += facWork1[k][j];
                else for (int i=0; i<left.length; i++) for (int j=0; j<anzFac; j++) pa[p][i][j] += left[i][k]*facWork1[k][j]; 
            }
        }
    }

    private void computeMatSpaMat(double[][] left, int[][] midPar, double[][] right, double[][][] pa) {
        for (int i=0; i<anzPar; i++) Statik.setToZero(pa[i]);
        int z = (left==null?anzFac:left.length);
        int c = (right==null?anzFac:right[0].length);
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (midPar[i][j] != Model.NOPARAMETER) {
            if       (left==null) for (int l=0; l<c; l++) pa[midPar[i][j]][i][l] += right[j][l];
            else if (right==null) for (int k=0; k<z; k++) pa[midPar[i][j]][k][j] += left[k][i];
            else for (int k=0; k<z; k++) for (int l=0; l<c; l++) pa[midPar[i][j]][k][l] += left[k][i]*right[j][l];
        }
    }
    
    
    /**
     * If left is true, computes anzVar * anzFac matrix, ie., mat1 F mat2. Otherwise, computes anzFac * anzVar matrix, i.e. mat1 F^T mat2
     * @param mat1
     * @param mat2
     * @param erg
     * @param left
     */
    private void matrixFilterMatrix(double[][] mat1, double[][] mat2, double[][] erg, boolean left) {
        if (left) for (int i=0; i<anzVar; i++) for (int j=0; j<anzFac; j++) {
            if (mat1==null) erg[i][j] = mat2[filter[i]][j];
            else {erg[i][j] = 0; for (int k=0; k<anzVar; k++) erg[i][j] += mat1[i][k]*mat2[filter[k]][j];}
        } else for (int i=0; i<anzFac; i++) for (int j=0; j<anzVar; j++) {
            if (mat2==null) erg[i][j] = mat1[i][filter[j]];
            else {erg[i][j] = 0; for (int k=0; k<anzVar; k++) erg[i][j] += mat1[i][filter[k]]*mat2[k][j];}
        }
    }
    
    private double dotMultiply(double[][] mat1, double[][] mat2) {
        double erg = 0;
        for (int i=0; i<mat1.length; i++) for (int j=0; j<mat1[i].length; j++) erg += mat1[i][j]*mat2[j][i];
        return erg;
    }
    
    public int callCount;
    public void debugComparison(double[] value, boolean recomputeMuAndSigma, boolean symbolic) {
        callCount++;
        if (!symbolic) super.computeLogLikelihoodDerivatives(value, recomputeMuAndSigma);
        else computeLogLikelihoodDerivatives(value, recomputeMuAndSigma);        
    }
    
    public boolean isManifest(int facNr) {
        for (int i=0; i<filter.length; i++) if (filter[i] == facNr) return true;
        return false;
    }
    
    /**
     * returns true exactly if the variable has no incoming single-heaeded edges. 
     * @param facNr
     * @return
     */
    public boolean isRoot(int facNr) {
        for (int i=0; i<anzFac; i++) 
            if (i != facNr && (asyPar[facNr][i] != NOPARAMETER || asyVal[facNr][i] != 0.0)) return false; 
        return true;
    }
    
    /**
     * returns true if the variable is latent and has no incoming edges, or if the variable is manifest and has incoming edges from an exogenous latent.
     * @param facNr
     * @return
     */
    public boolean isExogenous(int facNr) {
        if (isManifest(facNr)) {
            for (int i=0; i<anzFac; i++) 
                if (i != facNr && (asyPar[facNr][i] != NOPARAMETER || asyVal[facNr][i] != 0.0) && !isManifest(i) && isExogenous(i)) return true; 
            return false;
        } else return isRoot(facNr); 
    }
    
    public boolean debugGradientAndHessianComputationIsNumerical = false;
    public void computeLogLikelihoodDerivatives(double[] value, boolean recomputeMuAndSigma) {
        callCount++;
        if (debugGradientAndHessianComputationIsNumerical) super.computeLogLikelihoodDerivatives(value, recomputeMuAndSigma);
        else correctComputeLogLikelihoodDerivatives(value, recomputeMuAndSigma);        
    }
    
//    f = det*det*g
//    f' = 2*det'*det*g + det*det*g'
//       = 2*det*det*Tr(siginv sigdev)*g + det*det*g'
//       = det*det*(2*tr*g + g')
    
//    [ln(det)]' = Trace(siginv sigdev);
//    [ln(det)]' = det' / det
//    det * Tr(siginv sigdev) = det'
    
    /**
     * Computes |Sigma|^2 * gradient into llD and its derivative into llDD. If dividedBySigmaDetSqr is true, both llD and llDD are divided by |Sigma|^2.
     * This ensures that llD is a polynomial function, with the same zeros as llD in the permissible range, in which |Sigma| is positive. Note that llDD is then
     * not symmetrical.
     * This implementation is inefficient (because general) and should be overwritten in specific models. 
     * 
     * @param value
     * @param recomputeMuAndSigma
     * @param dividedBySigmaDetSqr
     */
    public void computeLogLikelihoodDerivativesNumerator(double[] value, boolean recomputeMuAndSigma, boolean dividedBySigmaDetSqr) {
        correctComputeLogLikelihoodDerivatives(value, recomputeMuAndSigma);
        
        double sigDetSqr = sigmaDet*sigmaDet;
        for (int i=0; i<anzPar; i++) {
            double tr = Statik.trace(squareBracket[17][i]);
            for (int j=0; j<anzPar; j++) llDD[i][j] += 2*llD[j]*tr;
            if (!dividedBySigmaDetSqr) for (int j=0; j<anzPar; j++) llDD[i][j] *= sigDetSqr;
        }
        if (!dividedBySigmaDetSqr) for (int i=0; i<anzPar; i++) llD[i] *= sigDetSqr;
    }
    
    
    public void computeLogLikelihoodDerivativesNumerical(double[] value, boolean recomputeMuAndSigma) 
        {super.computeLogLikelihoodDerivatives(value, recomputeMuAndSigma);}
    public void correctComputeLogLikelihoodDerivatives(double[] value, boolean recomputeMuAndSigma) {
        
        // If multiplication nodes are in the model, numerical estimation of the derivatives is called. 
        if (containsMultiplicationVariables()) {
            super.computeLogLikelihoodDerivatives(value, recomputeMuAndSigma); 
            return;
        }
        
        // semantics of squareBracket in RAM derivative paper (counting starts with zero here and with one in the paper)
        
        // Preparing memory; first 17 directly from the paper, 17,18, and 19 are sums of 0,3,6 ; 1,4,7 ; 2,5,8 respectively.
        // 20,21,22,23 are 10,11,12,14 but without the tailing C. 
        if (squareBracket==null) squareBracket = new double[24][][][];
        for (int i=0; i<9; i++) squareBracket[i] = Statik.ensureSize(squareBracket[i],anzPar, anzVar, anzVar);
        for (int i=17; i<20; i++) squareBracket[i] = Statik.ensureSize(squareBracket[i],anzPar, anzVar, anzVar);
        squareBracket[9] = Statik.ensureSize(squareBracket[9],anzPar, anzVar, anzFac);
        squareBracket[13] = Statik.ensureSize(squareBracket[13],anzPar, anzVar, anzFac);
        squareBracket[15] = Statik.ensureSize(squareBracket[15],anzPar, anzVar, anzFac);
        squareBracket[16] = Statik.ensureSize(squareBracket[16],anzPar, anzVar, anzFac);
        squareBracket[10] = Statik.ensureSize(squareBracket[10],anzPar, anzFac, anzVar);
        squareBracket[11] = Statik.ensureSize(squareBracket[11],anzPar, anzFac, anzVar);
        squareBracket[12] = Statik.ensureSize(squareBracket[12],anzPar, anzFac, anzVar);
        squareBracket[14] = Statik.ensureSize(squareBracket[14],anzPar, anzFac, anzVar);
        squareBracket[20] = Statik.ensureSize(squareBracket[20],anzPar, anzFac, anzVar);
        squareBracket[21] = Statik.ensureSize(squareBracket[21],anzPar, anzFac, anzVar);
        squareBracket[22] = Statik.ensureSize(squareBracket[22],anzPar, anzFac, anzVar);
        squareBracket[23] = Statik.ensureSize(squareBracket[23],anzPar, anzFac, anzVar);
        asyParT = Statik.ensureSize(asyParT, anzFac, anzFac);
        facWork1 = Statik.ensureSize(facWork1, anzFac, anzFac);
        facWork2 = Statik.ensureSize(facWork2, anzFac, anzFac);
        sigmaWork = Statik.ensureSize(sigmaWork, anzVar, anzVar);
        sigma = Statik.ensureSize(sigma, anzVar, anzVar);
        sigInv = Statik.ensureSize(sigInv, anzVar, anzVar);
        E = Statik.ensureSize(E, anzFac, anzFac);
        EFT = Statik.ensureSize(EFT, anzFac, anzVar);
        EFTC = Statik.ensureSize(EFTC, anzFac, anzVar);
        sigmaInvFB = Statik.ensureSize(sigmaInvFB, anzVar, anzFac);
        sigmaInvFE = Statik.ensureSize(sigmaInvFE, anzVar, anzFac);
        BTFTSigInvFB = Statik.ensureSize(BTFTSigInvFB, anzFac, anzFac); 
        C = Statik.ensureSize(C, anzVar, anzVar);
        B = Statik.ensureSize(B, anzFac, anzFac);
        BT = Statik.ensureSize(BT, anzFac, anzFac);
        BTFT = Statik.ensureSize(BTFT,anzFac, anzVar);
        BTFTC = Statik.ensureSize(BTFTC,anzFac, anzVar);
        sigmaInvData = Statik.ensureSize(sigmaInvData,anzVar, anzVar);
        EFTSigInvD = Statik.ensureSize(EFTSigInvD, anzFac, anzVar);
        BTFTSigInvD = Statik.ensureSize(BTFTSigInvD,anzFac, anzVar);
        b = Statik.ensureSize(b, anzVar);
        mu = Statik.ensureSize(mu, anzVar);
        workVec = Statik.ensureSize(workVec, anzFac);
        workVec2 = Statik.ensureSize(workVec2, anzFac);
        workVecVar = Statik.ensureSize(workVecVar, anzVar);
        workVecVar2 = Statik.ensureSize(workVecVar2, anzVar);
        sigmaInvb = Statik.ensureSize(sigmaInvb, anzVar);
        muDev = Statik.ensureSize(muDev, anzPar, anzVar);
        Bm = Statik.ensureSize(Bm, anzFac);
        BmDev = Statik.ensureSize(BmDev, anzPar, anzFac);
        BADev = Statik.ensureSize(BADev, anzPar, anzFac, anzFac);
        llD = Statik.ensureSize(llD, anzPar);
        llDD = Statik.ensureSize(llDD, anzPar, anzPar);
        anzPerDataCov = Statik.ensureSize(anzPerDataCov, anzVar, anzVar);
        
        // preparing anzPerDataCov
        Statik.multiply(dataMean, dataMean, sigmaWork);
        Statik.multiply(anzPer, sigmaWork, sigmaWork);
        Statik.subtract(xBiSum, sigmaWork, anzPerDataCov);
        
        
        // Preparing B, C, E
        if (value != null) setParameter(value); else value = getParameter();
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) asyParT[i][j] = asyPar[j][i];
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) facWork1[i][j] = (i==j?1:0) - asyVal[i][j];
        Statik.invert(facWork1, B, facWork2);
        Statik.transpose(B, BT);
        Statik.multiply(B, symVal, facWork1); Statik.multiply(facWork1, BT, E);
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) sigma[i][j] = E[filter[i]][filter[j]];
        try {sigmaDet = Statik.invert(sigma, sigInv, sigmaWork, logresult); }
        catch (Exception e) {
            ll = Double.NEGATIVE_INFINITY;
            for (int i=0; i<llD.length; i++) llD[i] = 0.0;
            for (int i=0; i<llDD.length; i++) for (int j=0; j<llDD[i].length; j++) llDD[i][j] = 0.0;
            return;
        }
        double lndet = logresult[0];
        Statik.multiply(sigInv, anzPerDataCov, sigmaInvData);
        Statik.multiply(1.0/(double)anzPer, sigmaInvData, sigmaInvData);
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) C[i][j] = (i==j?1:0) - sigmaInvData[i][j];
        
        // Preparing filtered matrices
        matrixFilterMatrix(sigInv, B, sigmaInvFB, true);
        matrixFilterMatrix(sigInv, E, sigmaInvFE, true);
        matrixFilterMatrix(E, null, EFT, false);
        matrixFilterMatrix(E, sigmaInvData, EFTSigInvD, false);
        Statik.subtract(EFT, EFTSigInvD, EFTC);
        matrixFilterMatrix(BT, null, BTFT, false);
        matrixFilterMatrix(BT, sigmaInvData, BTFTSigInvD, false);
        Statik.subtract(BTFT, BTFTSigInvD, BTFTC);
        
        // Preparing boxes from RAM derivative paper
        computeMatSpaMat(sigmaInvFB, asyPar, EFT, squareBracket[0]);
        computeMatSpaMat(sigmaInvFB, asyPar, EFTSigInvD, squareBracket[1]);
        computeMatSpaMat(sigmaInvFB, asyPar, EFTC, squareBracket[2]);           // TODO replace by subtraction
        computeMatSpaMat(sigmaInvFE, asyParT, BTFT, squareBracket[3]);
        computeMatSpaMat(sigmaInvFE, asyParT, BTFTSigInvD, squareBracket[4]);
        computeMatSpaMat(sigmaInvFE, asyParT, BTFTC, squareBracket[5]);          // TODO replace by subtraction
        computeMatSpaMat(sigmaInvFB, symPar, BTFT, squareBracket[6]);
        computeMatSpaMat(sigmaInvFB, symPar, BTFTSigInvD, squareBracket[7]);
        computeMatSpaMat(sigmaInvFB, symPar, BTFTC, squareBracket[8]);          // TODO replace by subtraction
        computeMatSpaMat(sigmaInvFB, asyPar, null, squareBracket[9]);
        computeMatSpaMat(B, asyPar, EFTC, squareBracket[10]);
        computeMatSpaMat(B, symPar, BTFTC, squareBracket[11]);
        computeMatSpaMat(E, asyParT, BTFTC, squareBracket[12]);
        computeMatSpaMat(sigmaInvFE, asyParT, BT, squareBracket[13]);
        computeMatSpaMat(null, asyParT, BTFTC, squareBracket[14]);
        computeMatSpaMat(sigmaInvFB, symPar, BT, squareBracket[15]);
        computeMatSpaMat(sigmaInvFB, asyPar, E, squareBracket[16]);
        for (int p=0; p<anzPar; p++) {
            Statik.add(squareBracket[0][p],squareBracket[3][p],squareBracket[17][p]); Statik.add(squareBracket[6][p], squareBracket[17][p], squareBracket[17][p]);
            Statik.add(squareBracket[1][p],squareBracket[4][p],squareBracket[18][p]); Statik.add(squareBracket[7][p], squareBracket[18][p], squareBracket[18][p]);
            Statik.add(squareBracket[2][p],squareBracket[5][p],squareBracket[19][p]); Statik.add(squareBracket[8][p], squareBracket[19][p], squareBracket[19][p]);
        }
        computeMatSpaMat(B, asyPar, EFT, squareBracket[20]);
        computeMatSpaMat(B, symPar, BTFT, squareBracket[21]);
        computeMatSpaMat(E, asyParT, BTFT, squareBracket[22]);
        computeMatSpaMat(null, asyParT, BTFT, squareBracket[23]);

        // Preparing mu, Bm, b, sigmaInvb, and BADev
        Statik.setToZero(mu);
        Statik.multiply(B, meanVal, Bm);
        for (int i=0; i<filter.length; i++) mu[i] = Bm[filter[i]];

        Statik.subtract(dataMean, mu, b);
        Statik.multiply(sigInv, b, sigmaInvb);
        computeMatSpaMat(B, asyPar, null, BADev);
        
        // Computation of minus two log likelihood, covariance contribution and mean contribution
//        ll = anzPer*anzVar*LNTWOPI + dotMultiply(sigInv, anzPerDataCov) + anzPer*lndet;
        ll = anzPer*anzVar*LNTWOPI + dotMultiply(sigInv, anzPerDataCov) + anzPer*lndet;
        Statik.multiply(sigInv, b, workVecVar); 
        double adder = Statik.multiply(b, workVecVar);
        ll += anzPer*adder; 
        
        // covariance contribution to first and second derivative
        for (int p1 = 0; p1 < anzPar; p1++) {
            llD[p1] = 0; for (int i=0; i<anzVar; i++) llD[p1] += squareBracket[19][p1][i][i];
            llD[p1] *= anzPer;
            for (int p2 = p1; p2 < anzPar; p2++) {
                llDD[p1][p2] = -dotMultiply(squareBracket[17][p1],squareBracket[19][p2]) + dotMultiply(squareBracket[17][p2],squareBracket[18][p1]);
                llDD[p1][p2] += dotMultiply(squareBracket[9][p1], squareBracket[10][p2]) + dotMultiply(squareBracket[9][p2],squareBracket[10][p1]) + 
                               dotMultiply(squareBracket[9][p1], squareBracket[11][p2]) + dotMultiply(squareBracket[9][p1],squareBracket[12][p2]) +
                               dotMultiply(squareBracket[9][p2], squareBracket[11][p1]) + dotMultiply(squareBracket[13][p2],squareBracket[14][p1]) +
                               dotMultiply(squareBracket[13][p1], squareBracket[14][p2]) + dotMultiply(squareBracket[15][p2],squareBracket[14][p1]) +
                               dotMultiply(squareBracket[16][p2], squareBracket[14][p1]) + dotMultiply(squareBracket[15][p1],squareBracket[14][p2]);
                llDD[p1][p2] *= anzPer;
                llDD[p2][p1] = llDD[p1][p2];
            }
        }
        
        // preparing BmDev and muDev
        for (int p=0; p<anzPar; p++) {
            Statik.setToZero(BmDev[p]);
            for (int i=0; i<anzFac; i++) if (meanPar[i]==p) for (int j=0; j<anzFac; j++) BmDev[p][j] += B[j][i];
            Statik.multiply(BADev[p],Bm,workVec);
            Statik.setToZero(muDev[p]);
            for (int j=0; j<anzVar; j++) muDev[p][j] += workVec[filter[j]] + BmDev[p][filter[j]];
        }
        
        // Mean contribution to first and second derivative
        for (int p1 = 0; p1 < anzPar; p1++) {
            llD[p1] -= 2*anzPer*Statik.multiply(muDev[p1],sigmaInvb);
            Statik.multiply(squareBracket[17][p1],sigmaInvb,workVecVar); 
            llD[p1] -= anzPer*Statik.multiply(b,workVecVar);
            
            for (int p2 = p1; p2 < anzPar; p2++) {
                adder = 0;
                // Terms without second derivatives
                Statik.multiply(squareBracket[17][p2],sigmaInvb, workVecVar); 
                Statik.multiply(squareBracket[17][p1], workVecVar, workVecVar2);
                adder += 2*anzPer*Statik.multiply(b, workVecVar2);
                adder += 2*anzPer*Statik.multiply(muDev[p1], workVecVar);
                Statik.multiply(squareBracket[17][p1],sigmaInvb, workVecVar); 
                adder += 2*anzPer*Statik.multiply(muDev[p2], workVecVar);
                Statik.multiply(sigInv,muDev[p1], workVecVar); 
                adder += 2*anzPer*Statik.multiply(muDev[p2], workVecVar);
                
                // Term with second derivative of mu
                Statik.multiply(BADev[p1], BmDev[p2], workVec); for (int i=0; i<anzVar; i++) workVecVar[i] = workVec[filter[i]];
                adder -= 2*anzPer*Statik.multiply(sigmaInvb, workVecVar);
                Statik.multiply(BADev[p2], BmDev[p1], workVec); for (int i=0; i<anzVar; i++) workVecVar[i] = workVec[filter[i]];
                adder -= 2*anzPer*Statik.multiply(sigmaInvb, workVecVar);
                Statik.multiply(BADev[p2], Bm, workVec); Statik.multiply(BADev[p1], workVec, workVec2); for (int i=0; i<anzVar; i++) workVecVar[i] = workVec2[filter[i]];
                adder -= 2*anzPer*Statik.multiply(sigmaInvb, workVecVar);
                Statik.multiply(BADev[p1], Bm, workVec); Statik.multiply(BADev[p2], workVec, workVec2); for (int i=0; i<anzVar; i++) workVecVar[i] = workVec2[filter[i]];
                adder -= 2*anzPer*Statik.multiply(sigmaInvb, workVecVar);
                
                // Term with second derivative of Sigma
                Statik.multiply(squareBracket[20][p2],sigmaInvb,workVec); Statik.multiply(squareBracket[9][p1], workVec, workVecVar); 
                adder -= anzPer*Statik.multiply(b,workVecVar);
                Statik.multiply(squareBracket[20][p1],sigmaInvb,workVec); Statik.multiply(squareBracket[9][p2], workVec, workVecVar); 
                adder -= anzPer*Statik.multiply(b,workVecVar);
                Statik.multiply(squareBracket[21][p2],sigmaInvb,workVec); Statik.multiply(squareBracket[9][p1], workVec, workVecVar); 
                adder -= anzPer*Statik.multiply(b,workVecVar);
                Statik.multiply(squareBracket[22][p2],sigmaInvb,workVec); Statik.multiply(squareBracket[9][p1], workVec, workVecVar); 
                adder -= anzPer*Statik.multiply(b,workVecVar);
                Statik.multiply(squareBracket[21][p1],sigmaInvb,workVec); Statik.multiply(squareBracket[9][p2], workVec, workVecVar); 
                adder -= anzPer*Statik.multiply(b,workVecVar);
                Statik.multiply(squareBracket[23][p1],sigmaInvb,workVec); Statik.multiply(squareBracket[13][p2], workVec, workVecVar); 
                adder -= anzPer*Statik.multiply(b,workVecVar);
                Statik.multiply(squareBracket[23][p2],sigmaInvb,workVec); Statik.multiply(squareBracket[13][p1], workVec, workVecVar); 
                adder -= anzPer*Statik.multiply(b,workVecVar);
                Statik.multiply(squareBracket[23][p1],sigmaInvb,workVec); Statik.multiply(squareBracket[15][p2], workVec, workVecVar); 
                adder -= anzPer*Statik.multiply(b,workVecVar);
                Statik.multiply(squareBracket[23][p1],sigmaInvb,workVec); Statik.multiply(squareBracket[16][p2], workVec, workVecVar); 
                adder -= anzPer*Statik.multiply(b,workVecVar);
                Statik.multiply(squareBracket[23][p2],sigmaInvb,workVec); Statik.multiply(squareBracket[15][p1], workVec, workVecVar); 
                adder -= anzPer*Statik.multiply(b,workVecVar);
                
                llDD[p1][p2] += adder; llDD[p2][p1] = llDD[p1][p2];
            }
        }
    }

    
    @Override
    protected void computeMatrixTimesMuDev(int par, double[][] matrix, double[] erg) {
        B = Statik.ensureSize(B, anzFac, anzFac);
        BT = Statik.ensureSize(BT, anzFac, anzFac);
        BTFT = Statik.ensureSize(BTFT,anzFac, anzVar);
        sigmaInvFB = Statik.ensureSize(sigmaInvFB, anzVar, anzFac);
        Bm = Statik.ensureSize(Bm, anzFac);
        workVec = Statik.ensureSize(workVec, anzFac);
        workVec2 = Statik.ensureSize(workVec2, anzVar);
        
        // Preparing B
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) facWork1[i][j] = (i==j?1:0) - asyVal[i][j];
        Statik.invert(facWork1, B, facWork2);
        Statik.transpose(B, BT);

        // Preparing filtered matrix, sigmaInvFB is here just FB
        matrixFilterMatrix(null, B, sigmaInvFB, true);
        Statik.multiply(B, meanVal, Bm);

        double[][] AD = null; Statik.setToZero(workVec);
        for (int i=0; i<anzFac; i++) { 
            for (int j=0; j<anzFac; j++)
                if (asyPar[i][j] == par) {if (AD == null) AD = new double[anzFac][anzFac]; AD[i][j] = 1;} 
            if (meanPar[i] == par) for (int j=0; j<BmDev.length; j++) workVec[j] += B[j][i];
        }
        Statik.multiply(sigmaInvFB,workVec,erg);
        
        if (AD != null) {
            Statik.multiply(AD, Bm, workVec);
            Statik.multiply(sigmaInvFB, workVec, workVec2);
            Statik.add(erg, workVec2, erg);
        }
    }
    

    @Override
    /**
     * uses dSigma/dt = Filter * (I-A)^{-1} (dA/dt + dS/dt) (I-A)^{-1} * Filter^T.
     * 
     * NEEDS SPEEDUP BY USING THAT dA/dt and dS/dt ARE SPARSE
     */
    protected void computeMatrixTimesSigmaDev(int par, double[][] matrix, double[][] erg) {
        facWork1 = Statik.ensureSize(facWork1, anzFac, anzFac);
        facWork2 = Statik.ensureSize(facWork2, anzFac, anzFac);
        sigmaWork = Statik.ensureSize(sigmaWork, anzVar, anzFac);
        sigmaWork2 = Statik.ensureSize(sigmaWork2, anzVar, anzVar);
        E = Statik.ensureSize(E, anzFac, anzFac);
        EFT = Statik.ensureSize(EFT, anzFac, anzVar);
        B = Statik.ensureSize(B, anzFac, anzFac);
        BT = Statik.ensureSize(BT, anzFac, anzFac);
        BTFT = Statik.ensureSize(BTFT,anzFac, anzVar);
        sigmaInvFB = Statik.ensureSize(sigmaInvFB, anzVar, anzFac);
        sigmaInvFE = Statik.ensureSize(sigmaInvFE, anzVar, anzFac);

        // Preparing B, C, E
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) facWork1[i][j] = (i==j?1:0) - asyVal[i][j];
        Statik.invert(facWork1, B, facWork2);
        Statik.transpose(B, BT);
        Statik.multiply(B, symVal, facWork1); Statik.multiply(facWork1, BT, E);

        // Preparing filtered matrices; sigmaInvFB is here just FB, and sigmaInvFE is FE
        matrixFilterMatrix(null, B, sigmaInvFB, true);
        matrixFilterMatrix(null, E, sigmaInvFE, true);
        matrixFilterMatrix(E, null, EFT, false);
        matrixFilterMatrix(BT, null, BTFT, false);

        double[][] AD = null, SD = null;
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) {
            if (asyPar[i][j] == par) {if (AD == null) AD = new double[anzFac][anzFac]; AD[i][j] = 1;}
            if (symPar[i][j] == par) {if (SD == null) SD = new double[anzFac][anzFac]; SD[i][j] = 1;}
        }
        Statik.setToZero(erg);
        if (AD != null) {
            Statik.multiply(sigmaInvFB,AD,sigmaWork); Statik.multiply(sigmaWork, EFT, sigmaWork2);
            Statik.symmetrize(sigmaWork2);
            Statik.add(erg, sigmaWork2, erg);
        }
        if (SD != null) {
            Statik.multiply(sigmaInvFB,SD, sigmaWork); Statik.multiply(sigmaWork, BTFT, sigmaWork2);
            Statik.add(erg, sigmaWork2, erg);
        }
        Statik.multiply(matrix, erg, sigmaWork2);
        Statik.copy(sigmaWork2, erg);
    }
        
    @Override
    // doesn't use any simplification.
    protected void computeMatrixTimesSigmaDevDev(int par1, int par2, double[][] matrix, double[][] erg) {

        facWork1 = Statik.ensureSize(facWork1, anzFac, anzFac);
        facWork2 = Statik.ensureSize(facWork2, anzFac, anzFac);
        sigmaWork = Statik.ensureSize(sigmaWork, anzVar, anzFac);
        sigmaWork2 = Statik.ensureSize(sigmaWork2, anzVar, anzFac);
        sigmaWork3 = Statik.ensureSize(sigmaWork3, anzVar, anzVar);
        E = Statik.ensureSize(E, anzFac, anzFac);
        EFT = Statik.ensureSize(EFT, anzFac, anzVar);
        B = Statik.ensureSize(B, anzFac, anzFac);
        BT = Statik.ensureSize(BT, anzFac, anzFac);
        BTFT = Statik.ensureSize(BTFT,anzFac, anzVar);
        sigmaInvFB = Statik.ensureSize(sigmaInvFB, anzVar, anzFac);
        sigmaInvFE = Statik.ensureSize(sigmaInvFE, anzVar, anzFac);

        // Preparing B, C, E
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) facWork1[i][j] = (i==j?1:0) - asyVal[i][j];
        Statik.invert(facWork1, B, facWork2);
        Statik.transpose(B, BT);
        Statik.multiply(B, symVal, facWork1); Statik.multiply(facWork1, BT, E);

        // Preparing filtered matrices; sigmaInvFB is here just FB, and sigmaInvFE is FE
        matrixFilterMatrix(null, B, sigmaInvFB, true);
        matrixFilterMatrix(null, E, sigmaInvFE, true);
        matrixFilterMatrix(E, null, EFT, false);
        matrixFilterMatrix(BT, null, BTFT, false);

        double[][] AD1 = null, AD2 = null, SD1 = null, SD2 = null;
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) {
            if (asyPar[i][j] == par1) {if (AD1 == null) AD1 = new double[anzFac][anzFac]; AD1[i][j] = 1;}
            if (asyPar[i][j] == par2) {if (AD2 == null) AD2 = new double[anzFac][anzFac]; AD2[i][j] = 1;}
            if (symPar[i][j] == par1) {if (SD1 == null) SD1 = new double[anzFac][anzFac]; SD1[i][j] = 1;}
            if (symPar[i][j] == par2) {if (SD2 == null) SD2 = new double[anzFac][anzFac]; SD2[i][j] = 1;}
        }
        Statik.setToZero(erg);
        if (AD1 != null && AD2 != null) {
            Statik.multiply(sigmaInvFB,AD1,sigmaWork); Statik.multiply(sigmaWork, B, sigmaWork2); 
            Statik.multiply(sigmaWork2, AD2, sigmaWork); Statik.multiply(sigmaWork, EFT, sigmaWork3);
            Statik.add(erg, sigmaWork3, erg);
            Statik.multiply(sigmaInvFB,AD2,sigmaWork); Statik.multiply(sigmaWork, B, sigmaWork2); 
            Statik.multiply(sigmaWork2, AD1, sigmaWork); Statik.multiply(sigmaWork, EFT, sigmaWork3);
            Statik.add(erg, sigmaWork3, erg);
            Statik.transpose(AD2, facWork1);
            Statik.multiply(sigmaInvFB,AD1,sigmaWork); Statik.multiply(sigmaWork, E, sigmaWork2); 
            Statik.multiply(sigmaWork2, facWork1, sigmaWork); Statik.multiply(sigmaWork, BTFT, sigmaWork3);
            Statik.add(erg, sigmaWork3, erg);
        }
        if (AD1 != null && SD2 != null) {
            Statik.multiply(sigmaInvFB,AD1,sigmaWork); Statik.multiply(sigmaWork, B, sigmaWork2); 
            Statik.multiply(sigmaWork2, SD2, sigmaWork); Statik.multiply(sigmaWork, BTFT, sigmaWork3);
            Statik.add(erg, sigmaWork3, erg);
        }
        if (AD2 != null && SD1 != null) {
            Statik.multiply(sigmaInvFB,AD2,sigmaWork); Statik.multiply(sigmaWork, B, sigmaWork2); 
            Statik.multiply(sigmaWork2, SD1, sigmaWork); Statik.multiply(sigmaWork, BTFT, sigmaWork3);
            Statik.add(erg, sigmaWork3, erg);
        }
        Statik.multiply(matrix, erg, sigmaWork3);
        Statik.symmetrize(sigmaWork3, erg);
    }

    @Override
    public Model copy() {
        return new RAMModel(Statik.copy(symPar), Statik.copy(symVal), Statik.copy(asyPar), Statik.copy(asyVal), Statik.copy(meanPar), 
                Statik.copy(meanVal), Statik.copy(filter));
    }

    @Override
    public void evaluateMuAndSigma(double[] values) {
        if (containsMultiplicationVariables()) {evaluateMuAndSigmaWithMultiplicationNodes(values); return;}

        sigmaBig = Statik.ensureSize(sigmaBig, anzFac, anzFac);
        sigma= Statik.ensureSize(sigma, anzVar, anzVar);
        meanBig = Statik.ensureSize(meanBig, anzFac);
        mu = Statik.ensureSize(mu, anzVar);

        if (anzFac == 0) return; 

        if (values!=null) setParameter(values);

       
        iMinusAInv = Statik.ensureSize(iMinusAInv, anzFac, anzFac);
        facWork1 = Statik.ensureSize(facWork1, anzFac, anzFac);
        facWork2 = Statik.ensureSize(facWork2, anzFac, anzFac);
        
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) facWork1[i][j] = (i==j?1-asyVal[i][j]:-asyVal[i][j]); 
        try {
            Statik.invert(facWork1, iMinusAInv, facWork2);
        } catch (Exception e) {
            System.out.println("Warning: Accelerating cycle in asymetric matrix.");
        }
        Statik.multiply(iMinusAInv, symVal, Statik.transpose(iMinusAInv), sigmaBig, facWork1);
        Statik.submatrix(sigmaBig, filter, filter, sigma);
        Statik.multiply(iMinusAInv, meanVal, meanBig);
        Statik.subvector(meanBig, filter, mu);
    }

    @Override
    public double getParameter(int nr) {
        for (int i=0; i<anzFac; i++) if (meanPar[i]==nr) return meanVal[i];
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (symPar[i][j]==nr) return symVal[i][j];
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (asyPar[i][j]==nr) return asyVal[i][j];
        return -1;
    }

    @Override
    public void removeParameterNumber(int nr) {
        for (int i=0; i<anzFac; i++) if (meanPar[i]>nr) meanPar[i]--; else if (meanPar[i]==nr) meanPar[i] = -1;
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (symPar[i][j]>nr) symPar[i][j]--; else if (symPar[i][j]==nr) symPar[i][j] = -1;
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (asyPar[i][j]>nr) asyPar[i][j]--; else if (asyPar[i][j]==nr) asyPar[i][j] = -1;

        position = Statik.subvector(position, nr);
        paraNames = Statik.subarray(paraNames, nr);
        if (startingValues != null) startingValues = Statik.subvector(startingValues, nr);
    }

    @Override
    public boolean isErrorParameter(int nr) {
        for (int i=0; i<anzFac; i++) if (meanPar[i]==nr) return false;
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (asyPar[i][j]==nr) return false;
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (symPar[i][j]==nr) {
            if (i!=j) return false;
            boolean isManifest = false; for (int k=0; k<anzVar; k++) if (filter[k]==i) isManifest = true;
            if (!isManifest) return false;
            for (int k=0; k<anzFac; k++) if ((asyVal[k][i] != 0) || (asyPar[k][i] != -1)) return false; 
        }
        return true;
    }

    @Override
    public RAMModel removeObservation(int obs) {
        RAMModel copy = (RAMModel)this.copy();
        copy.filter = Statik.subvector(copy.filter, obs);
        copy.anzVar--;
        return copy;
    }
    
    protected void setAnzParAndCollectParameter(int newAnzPar) {
        if (newAnzPar == -1) newAnzPar = anzPar;

        int erg = NOPARAMETER;
        for (int i=0; i<anzFac; i++) erg = Math.max(meanPar[i], erg);
        for (int i=0; i<anzFac; i++)
            for (int j=0; j<anzFac; j++) {erg = Math.max(asyPar[i][j], erg); erg = Math.max(symPar[i][j], erg);}
        
        anzPar = Math.max(erg+1, newAnzPar);
    }

    public boolean setParameter(int nr, double value) {
        boolean erg = false;
        for (int i=0; i<anzFac; i++) if (meanPar[i]==nr) {meanVal[i] = value; erg = true;}
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (symPar[i][j]==nr) {symVal[i][j] = value; erg = true;}
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (asyPar[i][j]==nr) {asyVal[i][j] = value; erg = true;}
        position[nr] = value;
        return erg;
    }

    protected int maxParNumber() {
        int erg = -1;
        for (int i=0; i<anzFac; i++) erg = Math.max(erg, meanPar[i]);
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) erg = Math.max(erg, symPar[i][j]);
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) erg = Math.max(erg, asyPar[i][j]);
        return erg;
    }

    @Override
    public double computeLeastSquares(double[] value) {
        return getLeastSquares(value);
    }

    @Override
    public double computeLogLikelihood(double[] value) {
        return getMinusTwoLogLikelihood(value);
    }

    @Override
    public double[] getRandomStartingValues() {return getRandomStartingValues(1000);}
    @Override
    public double[] getRandomStartingValues(double priorvariance) {
        int df = 2*anzFac;
        
        double[] erg = new double[anzPar];
        for (int i=0; i<anzFac; i++) if (meanPar[i]!=NOPARAMETER) erg[meanPar[i]] = 0;
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (asyPar[i][j]!=NOPARAMETER) erg[asyPar[i][j]] = rand.nextGaussian()*Math.sqrt(priorvariance);
        for (int i=0; i<anzFac; i++) if (symPar[i][i]!=NOPARAMETER) erg[symPar[i][i]] = priorvariance;
        for (int i=0; i<anzVar; i++) if (symPar[filter[i]][filter[i]]!=NOPARAMETER) erg[symPar[filter[i]][filter[i]]] = 3*priorvariance;
        for (int i=0; i<anzFac; i++) for (int j=i+1; j<anzFac; j++) if (symPar[i][j]!=NOPARAMETER) erg[symPar[i][j]] = 0.1;
        setParameter(erg);
        double[][] chol = null;
        try {
            chol = Statik.choleskyDecompose(symVal,0.0001);
        } catch (Exception e) {
            chol = Statik.identityMatrix(anzFac);
        }
        
        double[] val = new double[anzFac], raw = new double[anzFac];
        double[] drawMean = new double[anzFac];
        double[][] drawCov = new double[anzFac][anzFac];
        for (int i=0; i<df; i++) {
            for (int j=0; j<anzFac; j++) raw[j] = rand.nextGaussian();
            Statik.multiply(chol, raw, val);
            for (int j=0; j<anzFac; j++) drawMean[j] += val[j];
            for (int j=0; j<anzFac; j++) for (int k=0; k<anzFac; k++) drawCov[j][k] += val[j]*val[k];
        }
        for (int j=0; j<anzFac; j++) drawMean[j] /= df;
        for (int j=0; j<anzFac; j++) for (int k=0; k<anzFac; k++) drawCov[j][k] = drawCov[j][k] / (double)df - drawMean[j]*drawMean[k];
        
        for (int i=0; i<anzFac; i++) if (meanPar[i]!=NOPARAMETER) erg[meanPar[i]] = drawMean[i];
        for (int i=0; i<anzFac; i++) for (int j=i; j<anzFac; j++) if (symPar[i][j]!=NOPARAMETER) erg[symPar[i][j]] = drawCov[i][j];
        
        return erg;
    }
    
    public void createRandomDataCovarianceAndMean(double priorVariance) {
        evaluateMuAndSigma(getRandomStartingValues(priorVariance));
        dataCov = Statik.ensureSize(dataCov, anzVar, anzVar);
        Statik.copy(sigma, dataCov);
        dataMean = Statik.ensureSize(dataMean, anzVar);
        Statik.copy(mu, dataMean);
        anzPer = 1;
        computeMomentsFromDataCovarianceAndMean();
    }

    public static RAMModel uniteHierarchicalMultilevel(RAMModel teacher, RAMModel student, int anzStudents, int[] linkFilter) {
        if (linkFilter.length != student.anzVar) throw new RuntimeException("Multi Level Model must have links equal to observations in lower level.");
        int anzNonConnectTeacher = teacher.anzFac - linkFilter.length;
        int newFac = anzStudents*student.anzFac+anzNonConnectTeacher;
        int[] nonLink = new int[anzNonConnectTeacher];
        int l = 0;
        for (int i=0; i<teacher.anzFac; i++) {
            
            boolean inLink = false; 
            for (int j=0; j<linkFilter.length; j++) 
                if (linkFilter[j]==i) inLink = true;
            if (!inLink) nonLink[l++] = i;
        }
        
        double[][] symVal = new double[newFac][newFac];
        int[][] symPar = new int[newFac][newFac]; Statik.setTo(symPar, -1);
        for (int i=0; i<anzNonConnectTeacher; i++) for (int j=0; j<anzNonConnectTeacher; j++) {
            symVal[i][j] = teacher.symVal[i][j];
            symPar[i][j] = teacher.symPar[i][j];
        }
        for (int i=0; i<anzStudents; i++) 
            for (int j=0; j<student.anzFac; j++) 
                for (int k=0; k<student.anzFac; k++) {
                    symVal[anzNonConnectTeacher+i*student.anzFac+j][anzNonConnectTeacher+i*student.anzFac+k] = student.symVal[j][k];
                    symPar[anzNonConnectTeacher+i*student.anzFac+j][anzNonConnectTeacher+i*student.anzFac+k] = student.symPar[j][k];
                }
        double[][] asyVal = new double[newFac][newFac];
        int[][] asyPar = new int[newFac][newFac]; Statik.setTo(asyPar, -1);
        for (int i=0; i<anzNonConnectTeacher; i++) for (int j=0; j<anzNonConnectTeacher; j++) {
            asyVal[i][j] = teacher.asyVal[nonLink[i]][nonLink[j]];
            asyPar[i][j] = teacher.asyPar[nonLink[i]][nonLink[j]];
        }
        for (int k=0; k<anzStudents; k++)
            for (int i=0; i<student.anzVar; i++) 
                for (int j=0; j<anzNonConnectTeacher; j++)
                {
                    asyVal[anzNonConnectTeacher+k*student.anzFac+student.filter[i]][j] = teacher.asyVal[linkFilter[i]][nonLink[j]];
                    asyPar[anzNonConnectTeacher+k*student.anzFac+student.filter[i]][j] = teacher.asyPar[linkFilter[i]][nonLink[j]];
                }
        for (int i=0; i<anzStudents; i++) 
            for (int j=0; j<student.anzFac; j++) 
                for (int k=0; k<student.anzFac; k++) {
                    asyVal[anzNonConnectTeacher+i*student.anzFac+j][anzNonConnectTeacher+i*student.anzFac+k] = student.asyVal[j][k];
                    asyPar[anzNonConnectTeacher+i*student.anzFac+j][anzNonConnectTeacher+i*student.anzFac+k] = student.asyPar[j][k];
                }
        double[] meanVal = new double[newFac];
        int[] meanPar = new int[newFac]; Statik.setTo(meanPar, -1);
        for (int i=0; i<anzNonConnectTeacher; i++) {meanVal[i] = teacher.meanVal[nonLink[i]]; meanPar[i] = teacher.meanPar[nonLink[i]];}
        for (int k=0; k<anzStudents; k++) 
            for (int i=0; i<student.anzFac; i++) {
                meanVal[anzNonConnectTeacher + k*student.anzFac + i] = student.meanVal[i]; 
                meanPar[anzNonConnectTeacher + k*student.anzFac + i] = student.meanPar[i];
            }
        int[] filter = new int[teacher.anzVar - student.anzVar + student.anzVar*anzStudents];
        l = 0;
        for (int i=0; i<nonLink.length; i++)
            if (teacher.isManifest(i)) filter[l++] = i;
        for (int k=0; k<anzStudents; k++) 
            for (int i=0; i<student.anzVar; i++) 
                filter[l++] = anzNonConnectTeacher + k*student.anzFac + student.filter[i];
        
        RAMModel erg = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        return erg;
    }
    
    // NEEDS Checking for exact input format; assumption at the moment, no teacher observed variables are considered. 
    public static MultiGroupModel getMultiLevelPreProcessingMaximumLikelihoodModel(RAMModel teacher, RAMModel student, int anzStudents, int[] linkFilter, double[][] data) {
        double[] work = new double[student.anzVar*anzStudents];
        
        int newFac = teacher.anzFac+linkFilter.length;
        int[][] symPar = new int[newFac][newFac]; for (int i=0; i<newFac; i++) for (int j=0; j<newFac; j++) symPar[i][j] = -1;
        Statik.copy(teacher.symPar, symPar);
        double[][] symVal = new double[newFac][newFac]; 
        Statik.copy(teacher.symVal, symVal);
        int[][] asyPar = new int[newFac][newFac]; for (int i=0; i<newFac; i++) for (int j=0; j<newFac; j++) asyPar[i][j] = -1;
        Statik.copy(teacher.asyPar, asyPar);
        double[][] asyVal = new double[newFac][newFac]; 
        Statik.copy(teacher.asyVal, asyVal);
        int[] meanPar = new int[newFac]; for (int i=0; i<newFac; i++) meanPar[i] = -1;
        Statik.copy(teacher.meanPar, meanPar);
        double[] meanVal = new double[newFac];
        Statik.copy(teacher.meanVal, meanVal);
        int[] filter = Statik.copy(teacher.filter);
        int[] newLink = Statik.copy(linkFilter);
        double correctionValue = Math.sqrt(anzStudents);
        for (int i=0; i<linkFilter.length; i++) {
            for (int k=0; k<filter.length; k++) if (filter[k]==linkFilter[i]) filter[k] = teacher.anzFac+i;
            newLink[i] = teacher.anzFac+i;
            asyVal[teacher.anzFac+i][linkFilter[i]] = correctionValue;
        }
        RAMModel correctedTeacher = new RAMModel(symPar, symVal, asyPar,asyVal, meanPar, meanVal, filter);
        
        RAMModel group1Model = uniteHierarchicalMultilevel(correctedTeacher, student, 1, newLink);
        RAMModel group2Model = (RAMModel)student.copy();
        
        int anzSchools = data.length;
        double[][] data1 = new double[anzSchools][student.anzVar];
        double[][] data2 = new double[anzSchools*(anzStudents-1)][student.anzVar];
        for (int i=0; i<anzSchools; i++) {
            Model.multiplyWithMLOrthogonalTransformation(anzStudents, student.anzVar, data[i], work);
            for (int j=0; j<student.anzVar; j++) data1[i][j] = work[j];
            for (int j=1; j<anzStudents; j++) for (int k=0; k<student.anzVar; k++) data2[i*(anzStudents-1)+(j-1)][k] = work[j*student.anzVar+k];
        }
        MultiGroupModel erg = new MultiGroupModel(new Model[]{group1Model, group2Model}, group1Model.anzVar+group2Model.anzVar);
        erg.setData(new double[][][]{data1,data2});
        return erg;
    }
    
    /**
     * converts a multi-level long data format (every unit on the lowest level in a single row) to a wide format (every unit on the highest level in one row).
     * Assumes the ID of the corresponding higher order levels in the columns "hierarchicalGroupVariables", beginning with an ID on the lowest level to higher-level
     * IDs. The first ID is not used. in <code>levelVariables</code>, the rows for the variables of each level are listed, again beginning at the lowest level.
     * 
     * The output is one line per highest-level unit, followed by iterated repetitions, beginning with the highest level. If the number of lower-level units
     * are different, the maximum is taken, and empty slots filled by NA.
     * 
     * @param dataIn
     * @param hierarchicalGroupVariables
     * @param levelVariables
     * @return
     */
    public double[][] convertLongToWideDataFormat(double[][] dataIn, int[] hierarchicalGroupVariables, int[][] levelVariables) {
        int anzLevel = hierarchicalGroupVariables.length;
        int[] maxUnits = new int[anzLevel];
        
        return null;
    }
    
    public String getMatrixDescription() {
        final int PARLENGTH = 7;
        int[] columnWidth = new int[anzFac]; 
        String[] mVector = new String[anzFac];
        for (int i=0; i<anzFac; i++) {
            mVector[i] = "";
            if (meanPar[i] != -1) mVector[i] = Statik.abbreviateName(paraNames[meanPar[i]], PARLENGTH, false) + "=";
            mVector[i] += Statik.doubleNStellen(meanVal[i], 2);
            columnWidth[i] = Math.max(columnWidth[i], mVector[i].length());
        }
        
        String[][] aMatrix = new String[anzFac][anzFac];
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) {
            aMatrix[i][j] = "";
            if (asyPar[i][j] != -1) aMatrix[i][j] = Statik.abbreviateName(paraNames[asyPar[i][j]], PARLENGTH, false) + "=";
            aMatrix[i][j] += Statik.doubleNStellen(asyVal[i][j], 2);
            columnWidth[j] = Math.max(columnWidth[j], aMatrix[i][j].length());
        }

        String[][] sMatrix = new String[anzFac][anzFac];
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) {
            sMatrix[i][j] = "";
            if (symPar[i][j] != -1) sMatrix[i][j] = Statik.abbreviateName(paraNames[symPar[i][j]], PARLENGTH, false) + "=";
            sMatrix[i][j] += Statik.doubleNStellen(symVal[i][j], 2);
            columnWidth[j] = Math.max(columnWidth[j], sMatrix[i][j].length());
        }
        
        String[][] fMatrix = new String[anzVar][anzFac];
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzFac; j++) fMatrix[i][j] = "0";
        for (int i=0; i<anzVar; i++) fMatrix[i][filter[i]] = "1";

        for (int i=0; i<anzFac; i++) columnWidth[i] += 1;
        
        String erg = "m = (  ";
        for (int i=0; i<anzFac; i++) erg += Statik.abbreviateName(mVector[i], columnWidth[i], true);
        erg += ")\r\n\r\n";
        
        for (int i=0; i<anzFac; i++) {
            if (i==(anzFac-1)/2) erg += "A = "; else erg += "    ";
            if (i==0) erg += "/  "; else if (i==anzFac-1) erg += "\\  "; else erg += "|  ";
            for (int j=0; j<anzFac; j++) erg += Statik.abbreviateName(aMatrix[i][j], columnWidth[j], true);
            if (i==0) erg += " \\\r\n"; else if (i==anzFac-1) erg += " /\r\n"; else erg += " |\r\n";
        }
        erg += "\r\n\r\n";
        
        for (int i=0; i<anzFac; i++) {
            if (i==(anzFac-1)/2) erg += "S = "; else erg += "    ";
            if (i==0) erg += "/  "; else if (i==anzFac-1) erg += "\\  "; else erg += "|  ";
            for (int j=0; j<anzFac; j++) erg += Statik.abbreviateName(sMatrix[i][j], columnWidth[j], true);
            if (i==0) erg += " \\\r\n"; else if (i==anzFac-1) erg += " /\r\n"; else erg += " |\r\n";
        }
        erg += "\r\n\r\n";
        
        for (int i=0; i<anzVar; i++) {
            if (i==(anzVar-1)/2) erg += "F = "; else erg += "    ";
            if (i==0) erg += "/  "; else if (i==anzVar-1) erg += "\\  "; else erg += "|  ";
            for (int j=0; j<anzFac; j++) erg += Statik.abbreviateName(fMatrix[i][j], columnWidth[j], true);
            if (i==0) erg += " \\\r\n"; else if (i==anzVar-1) erg += " /\r\n"; else erg += " |\r\n";
        }
        erg += "\r\n\r\n";

        erg += "Model Covariance matrix = F (I-A)^{-1} S (I-A)^{-T} F^T\r\nModel Mean Vector       = F (I-A)^{-1} m";

        return erg;
    }
    
    private static String[] createVectorString(double[] value, int[] parameter, String[] parameterName, int[] columnWidth, int PARLENGTH) {
        int anzFac = value.length;
        String[] erg = new String[anzFac];
        for (int i=0; i<anzFac; i++) {
            erg[i] = "";
            if (parameter[i] != -1) erg[i] = Statik.abbreviateName(parameterName[parameter[i]], PARLENGTH, false) + "=";
            erg[i] += Statik.doubleNStellen(value[i], 2);
            columnWidth[i] = Math.max(columnWidth[i], erg[i].length());
        }
        return erg;
    }

    private static String[][] createMatrixString(double[][] value, int[][] parameter, String[] parameterName, int[] columnWidth, int PARLENGTH) {
        int anzRow = value.length;
        if (anzRow == 0) return new String[0][];
        int anzCol = value[0].length;
        String[][] erg = new String[anzRow][anzCol];
        for (int i=0; i<anzRow; i++) for (int j=0; j<anzCol; j++) {
            erg[i][j] = "";
            if (parameter[i][j] != -1) erg[i][j] = Statik.abbreviateName(parameterName[parameter[i][j]], PARLENGTH, false) + "=";
            erg[i][j] += Statik.doubleNStellen(value[i][j], 2);
            columnWidth[j] = Math.max(columnWidth[j], erg[i][j].length());
        }
        return erg;
    }
    
    private static String createMatrixStringFromArray(String name, String[][] matrix, int[] columnWidth) {
        String erg = "";
        if (matrix.length==0) return name+" = ( )";
        if (matrix.length==1) {
            erg = name+" = (  ";
            for (int j=0; j<matrix[0].length; j++) erg += Statik.abbreviateName(matrix[0][j], columnWidth[j], true); erg +=" ) \r\n";
            return erg;
        }
        for (int i=0; i<matrix.length; i++) {
            if (i==(matrix.length-1)/2) erg += name+" = "; else erg += Statik.repeatString(" ", 3+name.length());
            if (i==0) erg += "/  "; else if (i==matrix.length-1) erg += "\\  "; else erg += "|  ";
            for (int j=0; j<matrix[i].length; j++) erg += Statik.abbreviateName(matrix[i][j], columnWidth[j], true);
            if (i==0) erg += " \\\r\n"; else if (i==matrix.length-1) erg += " /\r\n"; else erg += " |\r\n";
        }
        return erg;
    }
    
    
    public String getLISRELMatrixDescription() {
        final int PARLENGTH = 7;
        
        boolean[] isLatentExogenous = new boolean[anzFac], isLatentEndogenous = new boolean[anzFac], isManifestExogenous = new boolean[anzFac], isManifestEndogenous = new boolean[anzFac];
        int anzLEx = 0, anzLEn = 0, anzMEx = 0, anzMEn = 0;
        String rejectAsNonLisrel = "";
        for (int i=0; i<anzFac; i++) {
            if (isManifest(i)) {
                for (int j=0; j<anzFac; j++) 
                    if (i!=j && (asyPar[i][j] != NOPARAMETER || asyVal[i][j] != 0.0) && isManifest(j) && rejectAsNonLisrel.length()==0) rejectAsNonLisrel = "Edges from manifest to manifest variables exist."; 
                if (isExogenous(i)) {
                    isManifestExogenous[i] = true; anzMEx++;
                    for (int j=0; j<anzFac; j++) 
                        if (i!=j && (asyPar[i][j] != NOPARAMETER || asyVal[i][j] != 0.0) && !isExogenous(j) && rejectAsNonLisrel.length()==0) rejectAsNonLisrel = "Manifest has incoming edges from endgenous and exogenous latent variables."; 
                } else {
                    isManifestEndogenous[i] = true; anzMEn++;
                    for (int j=0; j<anzFac; j++) 
                        if (i!=j && (asyPar[i][j] != NOPARAMETER || asyVal[i][j] != 0.0) && isExogenous(j) && rejectAsNonLisrel.length()==0) rejectAsNonLisrel = "Manifest has incoming edges from endgenous and exogenous latent variables."; 
                }
            } else {
                for (int j=0; j<anzFac; j++) 
                    if (i!=j && (asyPar[i][j] != NOPARAMETER || asyVal[i][j] != 0.0) && isManifest(j) && rejectAsNonLisrel.length()==0) rejectAsNonLisrel = "Edges from manifest to latent variables exist."; 
                if (isExogenous(i)) {isLatentExogenous[i] = true; anzLEx++;} else {isLatentEndogenous[i] = true; anzLEn++;}
            }
        }
        int[] latentEx = new int[anzLEx]; int k=0; for (int i=0; i<anzFac; i++) if (isLatentExogenous[i]) latentEx[k++] = i;
        int[] latentEn = new int[anzLEn]; k=0; for (int i=0; i<anzFac; i++) if (isLatentEndogenous[i]) latentEn[k++] = i;
        int[] manifestEx = new int[anzMEx]; k=0; for (int i=0; i<anzFac; i++) if (isManifestExogenous[i]) manifestEx[k++] = i;
        int[] manifestEn = new int[anzMEn]; k=0; for (int i=0; i<anzFac; i++) if (isManifestEndogenous[i]) manifestEn[k++] = i;
        
        for (int i=0; i<anzLEx; i++) for (int j=0; j<anzLEn; j++) if (symVal[latentEx[i]][latentEn[j]] != 0.0 || symPar[latentEx[i]][latentEn[j]] != NOPARAMETER) rejectAsNonLisrel = "Covariances between endogenous and exogenous latent variables exist.";
        for (int i=0; i<anzMEx; i++) for (int j=0; j<anzMEn; j++) if (symVal[manifestEx[i]][manifestEn[j]] != 0.0 || symPar[manifestEx[i]][manifestEn[j]] != NOPARAMETER) rejectAsNonLisrel = "Covariances between endogenous and exogenous manifest variables exist.";
        for (int i=0; i<anzLEx; i++) for (int j=0; j<anzMEn; j++) if (symVal[latentEx[i]][manifestEn[j]] != 0.0 || symPar[latentEx[i]][manifestEn[j]] != NOPARAMETER) rejectAsNonLisrel = "Covariances between latent and manifest variables exist. ";
        for (int i=0; i<anzLEx; i++) for (int j=0; j<anzMEx; j++) if (symVal[latentEx[i]][manifestEx[j]] != 0.0 || symPar[latentEx[i]][manifestEx[j]] != NOPARAMETER) rejectAsNonLisrel = "Covariances between latent and manifest variables exist. ";
        for (int i=0; i<anzLEn; i++) for (int j=0; j<anzMEn; j++) if (symVal[latentEn[i]][manifestEn[j]] != 0.0 || symPar[latentEn[i]][manifestEn[j]] != NOPARAMETER) rejectAsNonLisrel = "Covariances between latent and manifest variables exist. ";
        for (int i=0; i<anzLEn; i++) for (int j=0; j<anzMEx; j++) if (symVal[latentEn[i]][manifestEx[j]] != 0.0 || symPar[latentEn[i]][manifestEx[j]] != NOPARAMETER) rejectAsNonLisrel = "Covariances between latent and manifest variables exist. ";
        
        if (rejectAsNonLisrel.length()!=0) 
            return "Error: This model can not be described in LISREL notation for the following reason: \r\n"+rejectAsNonLisrel;
        
        String erg = "";
        if (this instanceof OnyxModel) {
            erg += "Exogenous latent variables    : ";
            for (int i=0; i<anzLEx; i++) erg += ((OnyxModel)this).variableNames[latentEx[i]] + (i==anzLEx-1?"\r\n":", ");
            erg += "Endogenous latent variables   : ";
            for (int i=0; i<anzLEn; i++) erg += ((OnyxModel)this).variableNames[latentEn[i]] + (i==anzLEn-1?"\r\n":", ");
            erg += "Exogenous manifest variables  : ";
            for (int i=0; i<anzMEx; i++) erg += ((OnyxModel)this).variableNames[manifestEx[i]] + (i==anzMEx-1?"\r\n":", ");
            erg += "Endogenous manifest variables : ";
            for (int i=0; i<anzMEn; i++) erg += ((OnyxModel)this).variableNames[manifestEn[i]] + (i==anzMEn-1?"\r\n":", ");
            erg += "\r\n";
        }

        double[][] phiVal = Statik.submatrix(symVal, latentEx, latentEx); int[][] phiPar = Statik.submatrix(symPar, latentEx, latentEx);
        double[][] psiVal = Statik.submatrix(symVal, latentEn, latentEn); int[][] psiPar = Statik.submatrix(symPar, latentEn, latentEn);
        double[][] deltaVal = Statik.submatrix(symVal, manifestEx, manifestEx); int[][] deltaPar = Statik.submatrix(symPar, manifestEx, manifestEx);
        double[][] epsilonVal = Statik.submatrix(symVal, manifestEn, manifestEn); int[][] epsilonPar = Statik.submatrix(symPar, manifestEn, manifestEn);
        
        double[][] gammaVal = Statik.submatrix(asyVal, latentEn, latentEx); int[][] gammaPar = Statik.submatrix(asyPar, latentEn, latentEx);
        double[][] betaVal = Statik.submatrix(asyVal, latentEn, latentEn); int[][] betaPar = Statik.submatrix(asyPar, latentEn, latentEn);
        double[][] lambdaXVal = Statik.submatrix(asyVal, manifestEx, latentEx); int[][] lambdaXPar = Statik.submatrix(asyPar, manifestEx, latentEx);
        double[][] lambdaYVal = Statik.submatrix(asyVal, manifestEn, latentEn); int[][] lambdaYPar = Statik.submatrix(asyPar, manifestEn, latentEn);
        
        int[] columnWidth = new int[anzFac]; 
        String[] mVector = createVectorString(meanVal, meanPar, paraNames, columnWidth, PARLENGTH);
        String[][] phi = createMatrixString(phiVal, phiPar, paraNames, columnWidth, PARLENGTH);
        String[][] psi = createMatrixString(psiVal, psiPar, paraNames, columnWidth, PARLENGTH);
        String[][] delta = createMatrixString(deltaVal, deltaPar, paraNames, columnWidth, PARLENGTH);
        String[][] epsilon = createMatrixString(epsilonVal, epsilonPar, paraNames, columnWidth, PARLENGTH);
        String[][] gamma = createMatrixString(gammaVal, gammaPar, paraNames, columnWidth, PARLENGTH);
        String[][] beta = createMatrixString(betaVal, betaPar, paraNames, columnWidth, PARLENGTH);
        String[][] lambdaX = createMatrixString(lambdaXVal, lambdaXPar, paraNames, columnWidth, PARLENGTH);
        String[][] lambdaY = createMatrixString(lambdaYVal, lambdaYPar, paraNames, columnWidth, PARLENGTH);

        for (int i=0; i<anzFac; i++) columnWidth[i] += 1;
        
        erg += "m       = (  ";
        for (int i=0; i<anzFac; i++) erg += Statik.abbreviateName(mVector[i], columnWidth[i], true);
        erg += ")\r\n\r\n";
        
        erg += createMatrixStringFromArray("Phi    ", phi, columnWidth)+"\r\n\r\n";
        erg += createMatrixStringFromArray("Psi    ", psi, columnWidth)+"\r\n\r\n";
        erg += createMatrixStringFromArray("delta  ", delta, columnWidth)+"\r\n\r\n";
        erg += createMatrixStringFromArray("epsilon", epsilon, columnWidth)+"\r\n\r\n";
        erg += createMatrixStringFromArray("gamma  ", gamma, columnWidth)+"\r\n\r\n";
        erg += createMatrixStringFromArray("beta   ", beta, columnWidth)+"\r\n\r\n";
        erg += createMatrixStringFromArray("lambdaX", lambdaX, columnWidth)+"\r\n\r\n";
        erg += createMatrixStringFromArray("lambdaY", lambdaY, columnWidth)+"\r\n\r\n";

        erg += "Phi     : Variances and covariances of exogenous latent variables.\r\n";
        erg += "Psi     : Variances and covariances of endogenous latent variables (containing zeta on the diagonal). \r\n";
        erg += "delta   : Variances and covariances of exogenous manifest variables (also called Theta_delta). \r\n";
        erg += "epsilon : Variances and covariances of endogenous manifest variables (also called Theta_epsilon). \r\n";
        erg += "gamma   : Paths from exogenous to endogenous latent Variables. \r\n";
        erg += "beta    : Paths from endogenous to endogenous latent variables. \r\n";
        erg += "lambdaX : Paths from exogenous latent variables to exogenous manifest variables. \r\n";
        erg += "lambdaY : Paths from endogenous latent variables to endogenous manifest variables. \r\n";
        erg += "\r\nAll other paths and covariances are zero. \r\n";
        
        return erg;
    } 
    
    public SaturatedRAMModel getSaturatedModel(boolean meansFixedToZero) {
        int[][] asyPar = new int[anzVar][anzVar]; for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) asyPar[i][j] = -1;
        double[][] asyVal = new double[anzVar][anzVar];
        int[][] symPar = new int[anzVar][anzVar]; 
        int k=0; for (int i=0; i<anzVar; i++) for (int j=i;j<anzVar; j++) symPar[i][j] = symPar[j][i] = k++;
        double[][] symVal = Statik.identityMatrix(anzVar);
        int[] meanPar = new int[anzVar]; for (int i=0; i<anzVar; i++) meanPar[i] = (meansFixedToZero?-1:k++);
        double[] meanVal = new double[anzVar];
        SaturatedRAMModel saturatedModel = new SaturatedRAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, anzVar);
        
        return saturatedModel;
    }

    /*  Is included in super anyway!
    public void computeLogLikelihoodDerivativesNumerical(double EPS) {
        double[][] oneStep = new double[anzPar][2];
        double[][][][] twoStep = new double[anzPar][anzPar][2][2];
        
        ll = getMinusTwoLogLikelihood();
        double[] orgPosition = getParameter();
        double[] myPos = Statik.copy(orgPosition);
        for (int i=0; i<anzPar; i++) {
            myPos[i] -= EPS/2.0;
            oneStep[i][0] = getMinusTwoLogLikelihood(myPos);
            for (int j=0; j<anzPar; j++) if (i!=j) {
                myPos[j] -= EPS/2.0;
                twoStep[i][j][0][0] = getMinusTwoLogLikelihood(myPos);
                myPos[j] += EPS;
                twoStep[i][j][0][1] = getMinusTwoLogLikelihood(myPos);
                myPos[j] = orgPosition[j];
            }
            myPos[i] += EPS;
            oneStep[i][1] = getMinusTwoLogLikelihood(myPos);
            for (int j=0; j<anzPar; j++) if (i!=j) {
                myPos[j] -= EPS/2.0;
                twoStep[i][j][1][0] = getMinusTwoLogLikelihood(myPos);
                myPos[j] += EPS;
                twoStep[i][j][1][1] = getMinusTwoLogLikelihood(myPos);
                myPos[j] = orgPosition[j];
            }
            myPos[i] = orgPosition[i];
        }
        setParameter(orgPosition);

        for (int i=0; i<anzPar; i++) llD[i] = (oneStep[i][1]-oneStep[i][0])/EPS;
        for (int i=0; i<anzPar; i++) llDD[i][i] = ( 2*(oneStep[i][1]-ll)/EPS  -  2*(ll-oneStep[i][0])/EPS  ) / EPS;
        for (int i=0; i<anzPar; i++) for (int j=0; j<anzPar; j++) if (i!=j) {
            llDD[i][j] = ( (twoStep[i][j][1][1] - twoStep[i][j][1][0])/EPS - (twoStep[i][j][0][1] - twoStep[i][j][0][0])/EPS ) / EPS;
        }
    }
    */
    
    public static RAMModel createFactorModel(double[][][] dataset) {return createFactorModel(dataset, true);}
    public static RAMModel createFactorModel(double[][][] dataset, boolean withMeans) {
        int anzFac = dataset.length;
        int anzMan = 0; int[] anzInd = new int[anzFac]; for (int i=0; i<anzFac; i++) {anzInd[i] = dataset[i][0].length; anzMan += anzInd[i];}
        int anzPer = dataset[0].length;
        RAMModel erg = createFactorModel(anzPer, anzInd, withMeans);
        
        double[][] data = new double[anzPer][anzMan];
        int startCol = 0;
        for (int i=0; i<anzFac; i++) {
            for (int j=0; j<anzPer; j++) for (int k=0; k<anzInd[i]; k++) data[j][startCol+k] = dataset[i][j][k]; 
            startCol += anzInd[i];
        }
        erg.setData(data);
        erg.setStrategy(Strategy.defaul);
        return erg;
    }
    
    public static RAMModel createFactorModel(int anzPer, int[] anzInd, boolean withMeans) {
        int anzFac = anzInd.length;
        int anzMan = 0; for (int i=0; i<anzFac; i++) anzMan += anzInd[i];
        int anzVar = anzFac + anzMan;
        int[][] symPar = new int[anzVar][anzVar]; double[][] symVal = new double[anzVar][anzVar];
        Statik.setTo(symPar, -1);
        int pnr = 0;
        for (int i=0; i<anzFac; i++) for (int j=i+1; j<anzFac; j++) {symPar[i][j] = symPar[j][i] = pnr++; symVal[i][j] = symVal[j][i] = 0.05;}
        for (int i=0; i<anzFac; i++) {symPar[i][i] = -1; symVal[i][i] = 1.0;}

        int[][] asyPar = new int[anzVar][anzVar]; double[][] asyVal = new double[anzVar][anzVar];
        Statik.setTo(asyPar, -1);

        // errors and loadings
        int row = anzFac;
        for (int i=0; i<anzFac; i++) {
            if (anzInd[i]==1) {symPar[row][row] = -1; symVal[row][row] = 0.0; asyPar[row][i] = pnr++; asyVal[row][i] = 1.0; row++;}
            else if (anzInd[i]==2) {symPar[row][row] = pnr++; symPar[row+1][row+1] = pnr++; symVal[row][row]=symVal[row+1][row+1] = 2.0; 
                                    asyPar[row][i] = pnr; asyPar[row+1][i] = pnr++; asyVal[row][i] = asyVal[row+1][i] = 1.0; row += 2;}
            else for (int j=0; j<anzInd[i]; j++) {symPar[row][row] = pnr++; symVal[row][row] = 2.0; asyPar[row][i] = pnr++; asyVal[row][i] = 1.0; row++;}
        }
        
        int[] meanPar = new int[anzVar]; 
        Statik.setTo(meanPar, -1);
        if (withMeans) for (int i=0; i<anzMan; i++) meanPar[i+anzFac] = pnr++;
        double[] meanVal = new double[anzVar];
        int[] filter = Statik.enumeratIntegersFrom(anzFac, anzVar-1);
        
        RAMModel erg = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        
        return erg;
    }
    
    /**
     * returns true if the A matrix has an Eigenvalue with absolute value close to or above one.
     * @return
     */
    public boolean hasAcceleratingCycle() {
        final double EPS = 0.01;
        double[] ev = Statik.eigenvalues(asyVal, EPS*EPS);
        for (int i=0; i<ev.length; i++) if (Math.abs(ev[i]) > 1.0 - EPS) return true;
        return false;   
    }
    
    public double[][] getLatentScores() {return getLatentScores(null, null);}
    public double[][] getLatentScores(double[] position, double[][] data) {
        double[][] zwerg = getAllScores(position, data);
        double[][] erg = new double[zwerg.length][anzFac - anzVar];
        for (int i=0; i<zwerg.length; i++) {
            int l = 0; for (int j=0; j<anzFac; j++)
                if (!isManifest(j)) erg[i][l++] = zwerg[i][j];
        }
        return erg;
    }

    /**
     * Adds the contribution of all participants to the saturated big matrix in erg. Participants with missing indicators are ignored.
     * 
     * @param candidateSigma    sigma matrix
     * @param erg
     */
    private void addScoresToSaturatedCovarianceMatrixAndMean(double[] candidateMean, double[][] candidateSigma, double[] ergMean, double[][] ergCov) {
        int anzLat = anzFac - anzVar;
        
        sigmaWork4 = Statik.ensureSize(sigmaWork4, anzLat, anzLat);
        latRct3 = Statik.ensureSize(latRct3,anzLat, anzVar);
        workVecVar = Statik.ensureSize(workVecVar, anzVar);
        workVecLat = Statik.ensureSize(workVecLat, anzLat);

        computeScoreMatrixAndScoreCovMatrix(candidateSigma, latRct3, sigmaWork4);
        
        for (int i=0; i<anzPer; i++) {
            boolean hasMissing = false; for (int j=0; j<data[i].length; j++) if (Model.isMissing(data[i][j])) hasMissing = true;
            if (!hasMissing) {
                for (int j=0; j<anzVar; j++) workVecVar[j] = data[i][j] - candidateMean[filter[j]];
                Statik.multiply(latRct3, workVecVar, workVecLat);
                int k1=0, k2=0; 
                for (int k=0; k<anzFac; k++) {
                    if (isManifest(k)) {
                        ergMean[k] += data[i][k1]; 
                        // TvO 31.3.17: Questionable whether the workVecVar in the following lines shouldn't be corrected by the mean difference of
                        // input and output?
                        int j1=0, j2=0; for (int j=0; j<anzFac; j++) if (isManifest(j)) ergCov[k][j] += workVecVar[k1]*workVecVar[j1++]; 
                                                                     else               ergCov[k][j] += workVecVar[k1]*workVecLat[j2++]; 
                        k1++;
                    } else {
                        ergMean[k] += candidateMean[k] + workVecLat[k2]; 
                        int j1=0, j2=0; for (int j=0; j<anzFac; j++) if (isManifest(j)) ergCov[k][j] += workVecLat[k2]*workVecVar[j1++]; 
                                                                     else               ergCov[k][j] += workVecLat[k2]*workVecLat[j2] + sigmaWork4[k2][j2++]; 
                        k2++;
                    }
                }
            }
        }
    }

    private static void addScoresToSaturatedCovAndMeanForMultiGroupRAMModel(Model model, double[] candidateMean, double[][] candidateSigma, double[] ergMean, double[][] ergCov) {
        if (model instanceof RAMModel) {
            ((RAMModel)model).addScoresToSaturatedCovarianceMatrixAndMean(candidateMean, candidateSigma, ergMean, ergCov);
        } else if (model instanceof MultiGroupModel) {
            MultiGroupModel mmodel = (MultiGroupModel)model;
            for (int i=0; i<mmodel.anzGroups; i++) 
                addScoresToSaturatedCovAndMeanForMultiGroupRAMModel(mmodel.submodel[i], candidateMean, candidateSigma, ergMean, ergCov);
        } 
    }
    
    /**
     * Computes the saturated covariance matrix of the Multi Group RAMModel model. ParentModel provides the filter for the manifest variables. 
     * Uses the EM-Algorithm described in Jamshidian and Bentler (1999).
     * 
     * TvO, 7.7.16: It is in question whether the algorithm works when all information is missing in a dislinked part of the model (in extreme, if one participant
     * is missing on all variables). Have to check back against the algorithm. For now, we adjust the number of parameters by not counting those that are missing
     * on all variables. 
     * 
     * @param parentModel
     * @param model
     * @return
     */
    public static void getSaturatedCovarianceOfMultigroupRAMModel(RAMModel parentModel, Model model, double[] ergMean, double[][] ergCov) {
        if (model instanceof RAMModel) {
            RAMModel ram = (RAMModel)model;
            if (ram.isIndirectData) {Statik.copy(ram.dataMean, ergMean); Statik.copy(ram.dataCov, ergCov);}
            else Statik.covarianceMatrixAndMeans(ram.data, ergMean, ergCov, Model.MISSING);
        } else {
            MultiGroupModel mmodel = ((MultiGroupModel)model);
            int n = parentModel.anzFac, anzPer = mmodel.getAnzPerInSubmodels();
            double[][] currentCov = new double[n][n], prevCov = Statik.identityMatrix(n);
            double[] currentMean = new double[n], prevMean = new double[n];
//            Statik.setTo(prevMean, 1.0); // debug
//            for (int i=0; i<n; i++) for (int j=i+1; j<n; j++) prevCov[i][j] = prevCov[j][i] = 0.2; // debug
            double EPS = 0.0001;
            double dist = 0; for (int i=0; i<n; i++) for (int j=0; j<n; j++) dist += (currentCov[i][j]-prevCov[i][j])*(currentCov[i][j]-prevCov[i][j]); dist /= (n*n);
            while (dist >= EPS*EPS) {
                Statik.setToZero(currentCov); Statik.setToZero(currentMean); 
                addScoresToSaturatedCovAndMeanForMultiGroupRAMModel(mmodel, prevMean, prevCov, currentMean, currentCov);
                for (int i=0; i<n; i++) {currentMean[i] /= anzPer; for (int j=0; j<n; j++) currentCov[i][j] /= anzPer;}
                dist = 0; for (int i=0; i<n; i++) for (int j=0; j<n; j++) dist += (currentCov[i][j]-prevCov[i][j])*(currentCov[i][j]-prevCov[i][j]); dist /= (n*n);
                double[][] t = currentCov; currentCov = prevCov; prevCov = t;
                double[] t2 = currentMean; currentMean = prevMean; prevMean = t2;
            }
            for (int i=0; i<parentModel.anzVar; i++) {
                ergMean[i] = prevMean[parentModel.filter[i]];
                for (int j=0; j<parentModel.anzVar; j++) ergCov[i][j] = prevCov[parentModel.filter[i]][parentModel.filter[j]];
            }
        }
    }
    
    /** computes the KL difference to the saturated Model. */
    @Override
    public double getIndependentKulbackLeibler() {
        if (!isIndirectData) return super.getIndependentKulbackLeibler();
        else return Model.getIndependentKulbackLeibler(dataMean, dataCov, dataMean, dataCov);
    }
    
    /**
     * Let cov be splitted into an observed and a latent part, Sigma_oo, Sigma_om, and Sigma_mm (m for missing is latent). This function returns
     * 
     * ergScoreMatrix    = Simga_mo times Sigma_oo^{-1} and
     * ergScoreCovMatrix = Sigma_mm - Sigma_mo Sigma_oo^{-1} Sigma_mo^T if ergScoreCovMatrix in the input is not null.
     * 
     * following Jamshidian and Bentler (1999). 
     * 
     * The function expects the input matrices to be of correct size and uses sigmaWork, sigmaWork1, sigmaWork2, latRct1, and latRct2 for the computation.
     * 
     * The best estimate of the mean for an observed set y is mean_m + ergScoreMatrix * (y-mean_o), and for the covariance matrix is
     * ergScoreCovMatrix + (y-mu_o)(y-mu_o)^T.
     * 
     * @param candidateSigma    an anzFac x anzFac sigma matrix that contains a candidate for the model matrix. 
     * @param ergScoreMatrix
     * @param ergScoreCovMatrix
     * @param data
     */
    private void computeScoreMatrixAndScoreCovMatrix(double[][] candidateSigma, double[][] ergScoreMatrix, double[][] ergScoreCovMatrix) {
        int anzLat = anzFac - anzVar;

        sigmaWork = Statik.ensureSize(sigmaWork,  anzVar,  anzVar);
        sigmaWork2 = Statik.ensureSize(sigmaWork2,  anzVar,  anzVar);
        sigmaWork3 = Statik.ensureSize(sigmaWork3,  anzVar,  anzVar);
        latRct1 = Statik.ensureSize(latRct1,anzVar, anzLat);
        latRct2 = Statik.ensureSize(latRct2,anzVar, anzLat);
        workVecVar = Statik.ensureSize(workVecVar, anzVar);
        int i1=0, j1=0, j2=0; 
        for (int i=0; i<anzFac; i++) 
            if (isManifest(i)) {
                for (int j=0; j<anzFac; j++) {
                    if (!isManifest(j)) latRct1[i1][j1++] = candidateSigma[i][j];
                    else sigmaWork[i1][j2++] = candidateSigma[i][j];
                }
                i1++; j1=j2=0;
            }
        Statik.invert(sigmaWork, sigmaWork2, sigmaWork3);
        Statik.multiply(sigmaWork2, latRct1, latRct2);
        Statik.transpose(latRct2, ergScoreMatrix);
        
        if (ergScoreCovMatrix != null) {
            Statik.multiply(ergScoreMatrix, latRct1, ergScoreCovMatrix);
            i1=0; j1=0; 
            for (int i=0; i<anzFac; i++) 
                if (!isManifest(i)) {
                    for (int j=0; j<anzFac; j++)
                        if (!isManifest(j)) ergScoreCovMatrix[i1][j1] = candidateSigma[i][j] - ergScoreCovMatrix[i1][j1++];
                    i1++; j1=0;
                }
        }
    }
    
    public double[][] getAllScores() {return getAllScores(null, null);}
    public double[][] getAllScores(double[] position, double[][] data) {
        if (position != null) setParameter(position);
        evaluateMuAndSigma();
        double[][] manifestData = (data==null?this.data:data);
        // working matrices of size anzFac x anzVar
        int anzLat = anzFac - anzVar;
        int anzPer = manifestData.length;
        
        latRct3 = Statik.ensureSize(latRct3,anzLat, anzVar);
        workVecVar = Statik.ensureSize(workVecVar, anzVar);

        computeScoreMatrixAndScoreCovMatrix(sigmaBig, latRct3, null);        
        
        double[][] erg = new double[anzPer][anzFac];
        for (int i=0; i<anzPer; i++) {
            boolean hasMissing = false; for (int j=0; j<manifestData[i].length; j++) if (Model.isMissing(manifestData[i][j])) hasMissing = true;
            if (!hasMissing) {
                for (int j=0; j<anzVar; j++) workVecVar[j] = manifestData[i][j] - meanBig[filter[j]];
                double[] latents = Statik.multiply(latRct3, workVecVar);
                int m=0, l=0; for (int j=0; j<anzFac; j++) if (isManifest(j)) erg[i][j] = manifestData[i][m++]; else erg[i][j] = latents[l++] + meanBig[j];
            }
            else Statik.setTo(erg[i],  Model.MISSING);
        }
        return erg;
    }
    
    /**
     * Returns all manifests, latent scores, and missing scores from the model. A valid model is a RAMModel or a MultiGroupModel of valid models; all other models will throw a CastException. 
     * 
     * @param model
     * @return
     */
    public static double[][] getAllScoresOfMultigroupRAMModel(Model model) {return getAllScoresOfMultigroupRAMModel(model, (model instanceof MultiGroupModel?((MultiGroupModel)model).getAnzPer():model.anzPer));}
    public static double[][] getAllScoresOfMultigroupRAMModel(Model model, int anzPerWithAllMissings) {
        double[][] erg = new double[anzPerWithAllMissings][];
        getAllScoresOfMultigroupRAMModel(model, erg);
        
        // This code for participants that are fully missing, they get the meanBig value. 
        Model m = model;
        while (!(m instanceof RAMModel)) m = ((MultiGroupModel)m).submodel[0];
        for (int i=0; i<erg.length; i++) if (erg[i] == null) erg[i] = Statik.copy(((RAMModel)m).meanBig);
        
        return erg;
    }
    private static void getAllScoresOfMultigroupRAMModel(Model model, double[][] erg) {
        if (model instanceof RAMModel) {
            double[][] zwerg = ((RAMModel)model).getAllScores();
            for (int i=0; i<zwerg.length; i++) {
                try {
                    erg[model.dataForeignKey[i]] = zwerg[i];
                } catch (ArrayIndexOutOfBoundsException e) {
                    System.out.println("Multi Group score computation Array out of Bounds, foreign key "+model.dataForeignKey[i]+" > "+erg.length);
                }
            }
        } else if (model instanceof MultiGroupModel) {
            MultiGroupModel mmodel = (MultiGroupModel)model;
            for (int i=0; i<mmodel.anzGroups; i++) getAllScoresOfMultigroupRAMModel(mmodel.submodel[i], erg);
        } 
    }

    public static double computeMinusTwoLogLikelihoodOfMultiGroupRAMModelForDistribution(Model model, double[][] sigma, double[][] precomputedSaturatedCov) {
        // TODO continue
        return 0;
    }
    
    /**
     * Method estimates the ML for a 2-latent structure with cov = 0 and variances both y, with error x repeated K times with each variance being 1, and data covariance matrix the 2x2 identity. 
     * @return
     */
    public static double[] estimateMLOfMimimizedModel(double a, double b, int K) {
        QPolynomial[] gradll = QPolynomial.getTwoByTwoLikelihoodDerivatives();
        for (int i=0; i<2; i++) gradll[i] = gradll[i].evaluate(new int[]{2,3,4,5}, new Qelement[]{Qelement.fromDouble(a,10), Qelement.fromDouble(b,10), new Qelement(K), Qelement.ONE});
        double check1 = gradll[0].evaluate(new int[]{0,1},new Qelement[]{Qelement.ONE,Qelement.ONE}).leadingFactor().toDouble();
        double check2 = gradll[1].evaluate(new int[]{0,1},new Qelement[]{Qelement.ONE,Qelement.ONE}).leadingFactor().toDouble();
        System.out.println("Check values = "+check1+", "+check2);
        
        Vector<Qelement> ergX = new Vector<Qelement>();
        Vector<Qelement> ergY = new Vector<Qelement>();
        double[] erg = new double[2]; double[] dist = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        for (int ix=0; ix<2; ix++) {
            FastPolynomial resultant = gradll[0].resultant(gradll[1], 1-ix);
            for (QPolynomial factor:resultant.member) {
                while (factor.getCoefficient(ix, 0).isZero()) factor = factor.divide(new QPolynomial(ix));
                if (factor.getDegreeIn(ix)>0) {
                    System.out.println("One factor of resultant "+ix+" is "+factor.toDoubleString());
                    Qelement[] zeros = factor.getZerosBetween(ix, new Qelement(0), new Qelement(2), 20, 20);
                    for (int i=0; i<zeros.length; i++) {
                        (ix==0?ergX:ergY).add(zeros[i]);
                        double dis = Math.abs(zeros[i].toDouble()-1.0); if (dis<dist[ix]) {dist[ix]=dis; erg[ix] = zeros[i].toDouble();}
                    }
                }
            }
        }
        System.out.print("Results for X: "); for (Qelement q:ergX) System.out.print(Statik.doubleNStellen(q.toDouble(), 10)+", "); System.out.println();
        System.out.print("Results for Y: "); for (Qelement q:ergY) System.out.print(Statik.doubleNStellen(q.toDouble(), 10)+", "); System.out.println();
        
        return erg;
    }
    
    public static RAMModel fromSEMMatrix(double[][] struct, int[][] covPar, int errPar, double[][] errStruct) {
        int anzVar = struct.length, anzLat = covPar.length, anzFac = 2*anzVar+anzLat;
        int[][] symPar = new int[anzFac][anzFac]; Statik.setTo(symPar, -1);
        for (int i=0; i<anzLat; i++) for (int j=0; j<anzLat; j++) symPar[2*anzVar+i][2*anzVar+j] = covPar[i][j];
        for (int i=0; i<anzVar; i++) symPar[anzVar+i][anzVar+i] = errPar;
        double[][] symVal = new double[anzFac][anzFac];
        int[][] asyPar = new int[anzFac][anzFac]; Statik.setTo(asyPar, -1);
        double[][] asyVal = new double[anzFac][anzFac];
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzLat; j++) asyVal[i][2*anzVar+j] = struct[i][j];
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) asyVal[i][anzVar+j] = errStruct[i][j];
        int[] meanPar = new int[anzFac]; Statik.setTo(meanPar, -1);
        double[] meanVal = new double[anzFac];
        int[] filter = Statik.enumeratIntegersFrom(0, anzVar-1);
        return new RAMModel(symPar, symVal,  asyPar,  asyVal,  meanPar,  meanVal, filter);
    }
    
    /**
     * This method takes a structure matrix of dimension (K+2 x 2) and a data covariance matrix of dimension (K+2 x K+2) and returns the variances var1, var2, and err of an SEM with two latents with variances var1 and var2 and covariance fixed to zero,
     * and homogeneous error with variance err.
     * 
     * It first transforms the model into values a,b, and K such that with Sigma = {{y,ax+by},{ax+by,y}}, the minimum x,y of
     * 
     * -2ll_transformed = K (ln(x) + 1/x) + ln(|Sigma|) + Tr(Sigma^{-1})
     * 
     * can be transformed back to var1, var2, and err. The method estimateMLOfMimimizedModel is used for the minimization of this equation.
     * 
     *  The method is tested and works. 
     * 
     * @param dataCov
     * @param structure
     * @return
     */
    public static double[] estimateMLOfTwoVariableNoCov(double[][] dataCov, double[][] structure) {
        int anzVar = dataCov.length, K = anzVar - 2;
        
        // debugCheck
        double[] dc = new double[]{2,3,5};
        double[][] struct = Statik.copy(structure);
        int[][] covPar = new int[][]{{1, -1}, {-1,2}};
        double[][] errStruct = Statik.identityMatrix(4);
        RAMModel model = RAMModel.fromSEMMatrix(struct, covPar, 0, errStruct);
        model.setDataDistribution(dataCov, new double[4]);
        model.computeLogLikelihoodDerivatives(new double[]{dc[0],dc[1],dc[2]});
        System.out.println("0. Check, gradient = "+Statik.matrixToString(model.llD,10));
        
        // Transformation 1; PPML-Reduction
        double[][] Q1 = new double[anzVar][anzVar], R = new double[anzVar][2];
        Statik.qrDecomposition(structure, Q1, R);
        double[][] A1 = new double[2][2]; for (int i=0; i<2; i++) for (int j=0; j<2; j++) A1[i][j] = R[i][j];
        double[][] S1 = Statik.multiply(Q1,  Statik.multiply(dataCov, Statik.transpose(Q1)));
        double s = 0; for (int i=2; i<anzVar; i++) s+= S1[i][i]; 
        s /= (anzVar - 2);

        // debugCheck
        Statik.setTo(struct, 0.0); Statik.copy(A1,struct);
        model = RAMModel.fromSEMMatrix(struct, covPar, 0, errStruct);
        model.setDataDistribution(S1, new double[4]);
        model.computeLogLikelihoodDerivatives(new double[]{dc[0],dc[1],dc[2]});
        System.out.println("1. Check, gradient = "+Statik.matrixToString(model.llD,10));
        
        // Transformation 2; data is transformed to identity matrix
        double trace = S1[0][0]+S1[1][1], det = S1[0][0]*S1[1][1]-S1[0][1]*S1[1][0];
        double sqrt = Math.sqrt(trace*trace/4.0 - det);
        double ev1 = trace / 2 + sqrt, ev2 = trace / 2.0 - sqrt;
        double abs1 = Math.sqrt(S1[0][1]*S1[0][1]+(ev1-S1[0][0])*(ev1-S1[0][0])), abs2 = Math.sqrt(S1[0][1]*S1[0][1]+(ev2-S1[0][0])*(ev2-S1[0][0]));
        double[][] Q2 = new double[][]{{S1[0][1]/(abs1*Math.sqrt(ev1)),(ev1-S1[0][0])/(abs1*Math.sqrt(ev1))},{S1[0][1]/(abs2*Math.sqrt(ev2)),(ev2-S1[0][0])/(abs2*Math.sqrt(ev2))}};
        double c2 = Math.sqrt(1.0/ev1);
        double d2 = Math.sqrt(1.0/ev2);
        double[][] A2 = Statik.multiply(Q2,A1);
        double[][] debugChol = Statik.choleskyDecompose(new double[][]{{c2*c2,0},{0,d2*d2}});
        double[][] debugCheckEV = Statik.multiply(Q2, Statik.multiply(new double[][]{{S1[0][0],S1[0][1]},{S1[1][0],S1[1][1]}}, Statik.transpose(Q2)));
        
        // debugCheck
        Statik.copy(A2,struct);
        Statik.copy(debugChol, errStruct);
        model = RAMModel.fromSEMMatrix(struct, covPar, 0, errStruct);
        model.setDataDistribution(Statik.identityMatrix(4), new double[4]);
        model.computeLogLikelihoodDerivatives(new double[]{dc[0],dc[1],dc[2]});
        System.out.println("2. Check, gradient = "+Statik.matrixToString(model.llD,10));
        
        // Transformation 3; the second latent variance is re-parameterized such that both columns of A have the same length
        double a11a21Overa12a22 = (A2[0][0]*A2[0][0] + A2[1][0]*A2[1][0]) / (A2[0][1]*A2[0][1] + A2[1][1]*A2[1][1]);
        double[][] A3 = Statik.copy(A2); A3[0][1] *= Math.sqrt(a11a21Overa12a22); A3[1][1] *= Math.sqrt(a11a21Overa12a22);
        
        // Transformation 4; vectors of A are rotated to have equal ankles to the diagonal, making the 2 main diagonal elements and the 2 off-diagonal elements equal
        double befSqrt = A3[0][0] + A3[0][1] - A3[1][0] - A3[1][1];
        double befVal  = A3[0][0] + A3[0][1] + A3[1][0] + A3[1][1];
        double q = Math.sqrt(befSqrt*befSqrt / (befSqrt*befSqrt + befVal*befVal));
        if (Math.signum(befSqrt) != Math.signum(befVal)) q = -q;
        double[][] Q4 = new double[][]{{q, Math.sqrt(1-q*q)},{Math.sqrt(1-q*q),-q}};
        double[][] A4 = Statik.multiply(Q4, A3);
        double[][] E4 = Statik.multiply(Q4, Statik.multiply(new double[][]{{c2*c2, 0},{0,d2*d2}}, Statik.transpose(Q4)));
        double[][] debugChol2 = Statik.multiply(Q4, debugChol);
        double[][] debugTest = Statik.multiply(debugChol2, Statik.transpose(debugChol2));
        debugChol = Statik.choleskyDecompose(E4);

        // debugCheck
        Statik.copy(A4,struct);
        Statik.copy(debugChol, errStruct);
        model = RAMModel.fromSEMMatrix(struct, covPar, 0, errStruct);
        model.setDataDistribution(Statik.identityMatrix(4), new double[4]);
        model.computeLogLikelihoodDerivatives(new double[]{dc[0],dc[1],dc[2]/a11a21Overa12a22});
        System.out.println("4. Check, gradient = "+Statik.matrixToString(model.llD,10));
        
        
        // Transformation 5; setting main diagonal elements of structure matrix to 1 and re-parameterizing the error.
        double[][] A5 = new double[][]{{1,A4[0][1]/A4[1][1]},{A4[1][0]/A4[0][0],1}};
        double[][] E5 = Statik.multiply(s, E4);
        debugChol = Statik.choleskyDecompose(E5);

        // debugCheck
        Statik.copy(A5,struct);
        Statik.copy(debugChol, errStruct);
        model = RAMModel.fromSEMMatrix(struct, covPar, 0, errStruct);
        model.setDataDistribution(Statik.identityMatrix(4), new double[4]);
        model.computeLogLikelihoodDerivatives(new double[]{dc[0]/s,dc[1]*(A4[0][0]*A4[0][0]),dc[2]*(A4[1][1]*A4[1][1]/a11a21Overa12a22)});
        System.out.println("5. Check, gradient = "+Statik.matrixToString(model.llD,10));
        
        // Transformation 6; equalizing variance of the final sigma
        double a5 = A5[0][1];
        double esum = E5[0][0] + E5[1][1];
        double a = -a5*esum/(a5*a5+1) + E5[0][1];
        double b = 2*a5/(a5*a5+1);
        
        // debugCheck
        System.out.println("6. Check, a = "+Statik.doubleNStellen(a,10));
        System.out.println("6. Check, b = "+Statik.doubleNStellen(b,10));
        System.out.println("6. Check, s = "+Statik.doubleNStellen(s,10));
        System.out.println("6. Check, a+b = "+Statik.doubleNStellen(a+b,10));
        
        // Minimize x and y K (ln(x) + 1/x) + ln(|Sigma|) + Tr(Sigma^{-1}) with Sigma = {{y,ax+by},{ax+by,y}}
        double[] xy = estimateMLOfMimimizedModel(a, b, K);
        
        // Invert re-parameterizations
        double x = xy[0], y = xy[1];
        double err = x*s;
        double z5 = ((1-a5*a5)*y - (E5[1][1]-E5[0][0]*a5*a5)*x) / (1-a5*a5*a5*a5);
        double y5 = ((1-a5*a5)*y - (E5[0][0]-E5[1][1]*a5*a5)*x) / (1-a5*a5*a5*a5);
        double v1 = y5 / (A4[0][0]*A4[0][0]);
        double v2 = z5*a11a21Overa12a22 / (A4[1][1]*A4[1][1]);
        
        return new double[]{v1,v2,err};
    }
    
    /**
     * The constraints are linear constraints on the parameter, where the first entry is the error parameter of a homogenous error, the next L are the variances, and then the covariances in natural order (as given by ix in the method). 
     * 
     * 
     * @param dataCov
     * @param structure
     * @return
     */
    public static double[] estimateMLAnalyticallyOfFixedStructure(double[][] dataCov, double[][] structure, double[][] constraints) {
        int anzVar = dataCov.length, L = structure[0].length, K = anzVar - L, anzPar = (L*(L+1))/2 + 1, anzConstraints = constraints.length, df = anzPar - anzConstraints;

        // Transformation 1; PPML-Reduction
        double[][] Q1 = new double[anzVar][anzVar], R = new double[anzVar][L];
        Statik.qrDecomposition(structure, Q1, R);
        double[][] A1 = new double[L][L]; for (int i=0; i<L; i++) for (int j=0; j<L; j++) A1[i][j] = R[i][j];
        double[][] S1 = Statik.multiply(Q1,  Statik.multiply(dataCov, Statik.transpose(Q1)));
        double s = 0; for (int i=L; i<anzVar; i++) s+= S1[i][i]; 
        s /= (double)K;

        // creating re-parameterization matrix and re-parameterizing the constraints. 
        int[][] ix = new int[L][L]; 
        int k=1; for (int i=0; i<L; i++) ix[i][i] = k++; for (int i=0; i<L; i++) for (int j=i+1; j<L; j++) ix[i][j] = ix[j][i] = k++; 
        double[][] rep = new double[anzPar][anzPar];
        for (int i=0; i<L; i++) for (int j=i; j<L; j++) for (k=0; k<L; k++) for (int l=0; l<L; l++)
            rep[ix[i][j]][ix[k][l]] += A1[i][k]*A1[j][l];
        rep[0][0] = 1;
        double[][] A1sqr = Statik.multiply(A1, Statik.transpose(A1));
        for (int i=0; i<L; i++) for (int j=i; j<L; j++) 
            rep[ix[i][j]][0] = A1sqr[i][j];
        double[][] invrep = Statik.invert(rep);
        double[][] nconstraints = Statik.multiply(invrep, Statik.transpose(constraints));
        
        // Creation of the C matrix with rows spanning the orthogonal space of the constraints.
        double[][] QC = new double[anzPar][anzPar], RC = new double[anzPar][anzConstraints];
        Statik.qrDecomposition(nconstraints, QC, RC);
        double[][] basis = new double[df][anzPar]; for (int i=0; i<df; i++)for (int j=0; j<anzPar; j++) basis[i][j] = QC[j][i+anzConstraints];
        double[][] QC2 = new double[anzPar][anzPar], C = new double[df][anzPar];
        Statik.qrDecomposition(basis, QC2, C);
        for (int i=0; i<df; i++) for (int j=0; j<anzPar; j++) C[i][j] /= C[i][i];
        
        // Creating all derivative matrices. 
        FastPolynomial det=null, tr=null; // det Sigma and trace of the adjoint of Sigma
        FastPolynomial[] detdev = new FastPolynomial[anzPar], trdev = new FastPolynomial[anzPar], g = new FastPolynomial[df], h = new FastPolynomial[df];
        if (L == 2) {det = new FastPolynomial("(X0+X1)*(X0+X2)-X3*X3"); tr = new FastPolynomial("X1+X2+2*X0");}
        if (L == 3) {det = new FastPolynomial("(X1+X0)*(X2+X0)*(X3+X0) + X4*X6*X5 + X5*X4*X6 - X5^2*(X2+X0) - X4^2*(X3+X0) - X6^2*(X1+X0)");
                     tr  = new FastPolynomial("(X2+X0)*(X3+X0)-X6^2 + (X1+X0)*(X3+X0)-X5^2 + (X1+X0)*(X2+X0)-X4^2");}
        if (L >= 4) {throw new RuntimeException("Determinant and trace are not implement for more than 3x3 matrices at the moment.");}
        for (int i=0; i<anzPar; i++) {
            detdev[i] = det.derive(i);
            trdev[i] = tr.derive(i);
        }
        for (int i=0; i<df; i++) {
            g[i] = new FastPolynomial(); for (int j=0; j<anzPar; j++) g[i] = g[i].add(detdev[i].multiply(new FastPolynomial(Qelement.fromDouble(C[i][j],10))));
            h[i] = new FastPolynomial(); for (int j=0; j<anzPar; j++) h[i] = h[i].add(trdev[i].multiply(new FastPolynomial(Qelement.fromDouble(C[i][j],10))));
        }
        FastPolynomial e = new FastPolynomial(0);
        FastPolynomial eMinusS = e.subtract(new FastPolynomial(Qelement.fromDouble(s, 10)));
        FastPolynomial kpol = new FastPolynomial(new Qelement(K));
        FastPolynomial[] f = new FastPolynomial[df];
        f[0] = kpol.multiply(det.sqr()).multiply(eMinusS).add(e.sqr().multiply(g[0].multiply(det.subtract(tr)).add(h[0].multiply(det))));
        for (int i=1; i<df; i++) f[i] = g[i].multiply(det.subtract(tr)).add(h[i].multiply(det));
        
        FastPolynomial[] gen = new FastPolynomial[anzPar];
        if (df == 1) gen[0] = f[0];
        if (df == 2) {
            gen[0] = kpol.multiply(eMinusS).multiply(g[1].multiply(tr).subtract(h[1].multiply(det))).subtract(e.sqr().multiply(g[0]).multiply(h[1])).add(e.sqr().multiply(h[0]).multiply(g[1]));
            gen[1] = f[1]; 
        }
        if (df == 3) {
            gen[0] = kpol.multiply(eMinusS).multiply(g[1].multiply(tr).subtract(h[1].multiply(det))).subtract(e.sqr().multiply(g[0]).multiply(h[1])).add(e.sqr().multiply(h[0]).multiply(g[1]));
            gen[1] = kpol.multiply(eMinusS).multiply(g[2].multiply(tr).subtract(h[2].multiply(det))).subtract(e.sqr().multiply(g[0]).multiply(h[2])).add(e.sqr().multiply(h[0]).multiply(g[2]));
            gen[2] = h[1].multiply(g[2]).subtract(h[2].multiply(g[1]));
        }
        if (df >= 4) {
            gen[0] = kpol.multiply(eMinusS).multiply(g[1].multiply(tr).subtract(h[1].multiply(det))).subtract(e.sqr().multiply(g[0]).multiply(h[1])).add(e.sqr().multiply(h[0]).multiply(g[1]));
            for (int i=1; i<df; i++) gen[i] = h[i].multiply(g[i+1]).subtract(h[i+1].multiply(g[i]));
        }
        for (int i=df; i<anzPar; i++)  {
            gen[i] = new FastPolynomial();
            for (int j=0; j<anzPar; j++) gen[i] = gen[i].add((new FastPolynomial(j)).multiply(new FastPolynomial(Qelement.fromDouble(nconstraints[j][i-df],10))));
        }
        QPolynomial[] genQ = new QPolynomial[anzPar]; for (int i=0; i<anzPar; i++) genQ[i] = gen[i].expand();
        
        QPolynomial[] groebner = QPolynomial.computeGroebnerBasis(genQ, QPolynomial.lexorder);
        
        // debug
        FastPolynomial resultant = gen[0].resultant(gen[1], 1);
        
//        Statik.chiSquareDistribution(degreesOfFreedom, noncentrality, x);
        
        return null;
    }

    public static RAMModel getSaturatedModel(int anzVar) {
        int[][] asyPar = new int[anzVar][anzVar];
        int[] meanPar = new int[anzVar]; 
        int[][] symPar = new int[anzVar][anzVar];
        int pnr = 0;
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) asyPar[i][j] = -1;
        for (int i=0; i<anzVar; i++) meanPar[i] = pnr++;
        for (int i=0; i<anzVar; i++) symPar[i][i] = pnr++;
        for (int i=0; i<anzVar; i++) for (int j=i+1; j<anzVar; j++) symPar[i][j] = symPar[j][i] = pnr++;
        return new RAMModel(symPar, Statik.identityMatrix(anzVar), asyPar, new double[anzVar][anzVar], meanPar, new double[anzVar], anzVar);
    }
    
    
    public boolean isMultiplicationVariable(int n) {
        if (isMultiplicationVariable==null || !isMultiplicationVariable[n]) return false;
        return true;
    }
    
    public boolean containsMultiplicationVariables() {
        if (isMultiplicationVariable==null) return false;
        for (int i=0; i<anzFac; i++) if (isMultiplicationVariable[i]) return true;
        return false;
    }
    
    /**
     * Conditions: This method only works if the asymmetric matrix is acyclic and the symmetric matrix is positive semi definite, otherwise it will call
     * a Runtime Exception. 
     * 
     * The methods evaluates the mean and covariance matrix of the model, allowing for multiplication variables. Note that the distribution of the model
     * in that case is no longer normal, higher moments are ignored. 
     * 
     * @param values optional parameter values, if null, parameter values are not changed. 
     */
    public void evaluateMuAndSigmaWithMultiplicationNodes(double[] values) {
        sigmaBig = Statik.ensureSize(sigmaBig, anzFac, anzFac);
        sigma= Statik.ensureSize(sigma, anzVar, anzVar);
        meanBig = Statik.ensureSize(meanBig, anzFac);
        mu = Statik.ensureSize(mu, anzVar);

        if (anzFac == 0) return; 
        if (values!=null) setParameter(values);

        computePolynomialRepresentation();
        for (int i=0; i<anzFac; i++) meanBig[i] = polynomialToMoment(polynomialRepresentation[i]);
        for (int i=0; i<anzFac; i++) for (int j=i; j<anzFac; j++) 
            sigmaBig[i][j] = polynomialToMoment(polynomialRepresentation[i].times(polynomialRepresentation[j])) - meanBig[i]*meanBig[j];
        for (int i=0; i<anzFac; i++) for (int j=0; j<i; j++) sigmaBig[i][j] = sigmaBig[j][i]; 

        Statik.submatrix(sigmaBig, filter, filter, sigma);
        Statik.subvector(meanBig, filter, mu);
    }

    /**
     * Computes the expectation of the polynomial if all variables in the polynomial are independently normally distributed with mean zero
     * and variances given by polynomialRepresentationVariances. 
     * 
     * @param polynomial
     * @return
     */
    private double polynomialToMoment(DoublePolynomial polynomial) {
        double erg = 0;
        for (Monomial<DoubleField> monom:polynomial.monomials) {
            double zwerg = monom.coeff.value;
            for (int i=0; i<monom.exp.length; i++) {
                if (monom.exp[i] % 2 == 1) {zwerg = 0; break;}
                for (int j=0; j<= (monom.exp[i]/2)-1; j++) zwerg *= (2*j+1);
                zwerg *= Math.pow(polynomialRepresentationVariances[i], monom.exp[i]/2);
            }
            erg += zwerg;
        }
        return erg;
    }
    
    /**
     *  Recursive helper function for computePolynomialRepresentation
     */
    private void appendPolynomialRepresentation(int n, short[] status) {
        if (status[n] == 2) return;
        if (status[n] == 1) throw new RuntimeException("\"Polynomial representation is impossible as asymmetric matrix is cyclic.");
        status[n] = 1;

        for (int i=0; i<anzFac; i++) if (i != n && asyVal[n][i] != 0.0 && status[i]!=2) appendPolynomialRepresentation(i, status);  
        for (int i=0; i<anzFac; i++) if (i != n && asyVal[n][i] != 0.0) {
            DoublePolynomial term = new DoublePolynomial(asyVal[n][i]);
            term.multiply(polynomialRepresentation[i]);
            if (isMultiplicationVariable(n)) polynomialRepresentation[n].multiply(term);
            else polynomialRepresentation[n].add(term);
        }
        status[n] = 2;
    }
    
    /**
     * Computes a representation of every variable as a polynomial in independent standard normally distributed random variables and stores
     * the result in the field polynomialRepresentation.
     */
    public void computePolynomialRepresentation() {
        polynomialRepresentation = new DoublePolynomial[anzFac];
        for (int i=0; i<anzFac; i++) 
            if (isMultiplicationVariable(i) && meanVal[i]==0.0) polynomialRepresentation[i] = new DoublePolynomial(1.0);
            else polynomialRepresentation[i] = new DoublePolynomial(meanVal[i]);

        /* old version with Cholesky, doesn't work with non-positive definite S, but probably easier to extend to derivatives. Also, 
         * polynomials are more sparse with the Cholesky variant. An alternative could be to use a sign switch in the Cholesky decomposition,
         * thereby working with normally distributed variables of variance either 1 or -1. 
        double[][] cholesky = null;
        try {
            cholesky = Statik.choleskyDecompose(symVal,0.001);
        } catch (Exception e) {
            throw new RuntimeException("Polynomial representation is impossible because symmetric matrix is not positive definite: "+e);
        }

        int[] x = new int[anzFac]; int k = 0; for (int i=0; i<anzFac; i++) if (cholesky[i][i]!=0.0) x[i] = k++;
        for (int i=0; i<anzFac; i++) for (int j=0; j<=i; j++) if (cholesky[i][j] != 0.0) polynomialRepresentation[i].addMonomial(cholesky[i][j], x[j]);
        polynomialRepresentationVariances = Statik.ensureSize(polynomialRepresentationVariances, k);
        for (int i=0; i<polynomialRepresentationVariances.length; i++) polynomialRepresentationVariances[i] = 1.0;
        */
        
        polynomialRepresentationVariances = Statik.ensureSize(polynomialRepresentationVariances, anzFac);
        double[][] diagonalization = Statik.identityMatrix(anzFac);
        Statik.eigenvalues(symVal, 0.0001, polynomialRepresentationVariances, diagonalization);
        
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) 
            if (diagonalization[i][j] != 0.0 && polynomialRepresentationVariances[j] != 0.0) 
                polynomialRepresentation[i].addMonomial(diagonalization[i][j], j);
        
        short[] done = new short[anzFac]; 
        for (int i=0; i<anzFac; i++) appendPolynomialRepresentation(i, done);
    }
    
    public DoublePolynomial[] getPolynomialRepresentation() {return polynomialRepresentation;}

    /**
     * 
     * @return true is all meanValues are zero and no parameters are used in the meanPar
     */
    public boolean isConstantZeroMeanModel() {
        for (int i=0; i<anzVar; i++) if (meanPar[i] != -1 || meanVal[i] != 0) return false;
        return true;
    }
    
    @Override
    /**
     * overrides the test in Model to add a simple count of parameters.
     */
    public int hessianIsConstantSingular(double[] parameterValues) {
        int anzStatistics = anzVar*(anzVar+1)/2;
        if (!isConstantZeroMeanModel()) anzStatistics += anzVar;
        if (anzStatistics > anzPar) return 1;
        return super.hessianIsConstantSingular(parameterValues);
    }
}
