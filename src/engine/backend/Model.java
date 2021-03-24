/*
 * Created on 11.09.2009
 */
package engine.backend;

import java.io.PrintStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.util.Vector;

//import sun.reflect.ReflectionFactory.GetReflectionFactoryAction;
import engine.GivensSeries;
import engine.OptimizationHistory;
import engine.Statik;


/**
 * Superclass for all models. 
 * 
 * @author Timo
 */
public abstract class Model 
{
      /** Indicates whether computeLogLikelihoodDerivatives and computeLeastSquaresDerivatives compute the first
       *  and second derivatives wrt to lambda, where lambda 
       */
      static public final boolean supportsPathDerivatives = false;

      static public final int NOPARAMETER = -1;
      static public double MISSING = -999;
      static public final double LNTWOPI = Math.log(2*Math.PI);
//      static protected Random rand = new Random(213432465L);
      public Random rand = new Random();
      static public final Random staticRandom = new Random();
      static public double suggestedEPS = 0.000001;
//      static public final double suggestedEPS = 0.001;
      protected int MAXRUNS = 50; 
      protected int MINRUNS = 1;

      // is true if the data is given by an external distribution
      protected boolean isIndirectData;
      
      // standard meta parameter are power, alpha, and n. 
      public static final int META_POWER = 0, META_ALPHA = 1, META_N = 2;
      
      public static enum warningFlagTypes {OK, SUSPICIOUS, FAILED};
      public warningFlagTypes warningFlag;
      
      /** if null, no messages will be issued **/
      public PrintStream logStream = null;

      public OptimizationHistory history;
      
      public int anzPar;        // number of parameters, length of the parameter name String array.
      public int anzVar;
      
      protected String[] paraNames;
      public double[] position;           // current parameter values, as named in paraNames.
      public double[] startingValues;        // not quaranteed to be set, contains last used starting values. 
      
      public int anzPer;
      public double[][] data;
      public double[] dataMean;
      public double[] xsum;
      public double[][] dataCov;
      public double[][] xBiSum;
      public int[] dataForeignKey;      // optional; is used if model is submodel in a MultiGroupModel to store the id in the supermodel.
    
      public double[][] auxiliaryData;
      public double[][] controlData;
      public double[][] jointData;      // data including auxiliary and control data
      public int anzAux, anzCtrl;
      
      public double[][] sigma;
      public double[] mu;
      public double sigmaDet;

      public enum Objective {maximumLikelihood, Leastsquares};
      public Objective fitFunction;                       // remembers last initialization of fit process with ml or ls
      public enum Strategy {classic, defaul, user, defaultWithEMSupport, MCMC};
      public Strategy lastChosenStrategyPreset;
      // Flags and constants for the optimization process. 
      public boolean strategyUseClassicalOnyx = true;        // Flag for using old newton step algorithm
      public boolean strategyUseHessian = true;              // Flag for Hessian-based algorithms
      public boolean strategyUseCholeskyFirst = true;        // Flag for trying to invert the Hessian by Cholesky first
      public boolean strategyUseOertzenOptimization = true;  // Flag for using a Tridiagonal transformation and working on the Eigenvalues separately
      public boolean strategyUseGradientNumerator = false;    // Flag for using the gradient*|Sigma|^2 instead of gradient, which is polynomial for SEMs.
      public boolean strategyUseLineSearch = true;           // Flag for using a linesearch on the optimization direction, is always on the original ll function.
      public boolean strategyUseWarp = false;                // Flag for inverting signs on directions that seem to approach an outside horizontal asymptote.
      public boolean strategyAllowNonPDSigma = false;        // If true, intermediate and final steps with non-positive definite Sigma are permitted.
      public boolean strategyUseEMWithSaturated = false;     // Only used in RAMModel and subtypes 
      public double strategyCholeskyTolerance = 0.00001;     // Epsilon for what negative values are permitted in Cholesky decomposition to continue
      public double strategyMaximalStepFactor = 1000;        // Highest permitted factor for a direction in the Newton step
      public double strategyReductionPowerOnNegative = 0.8;  // Power to which negative Eigenvalue steps are taken; 0 = gradient descent, 1 = full positivation. 
      public double strategyGradientDescentDamping = 0.8;    // Damping in gradient descent
      public double strategyWarpMinimalSpeed = 1.0;          // Minimal speed to the outside to allow warp
      public int strategyWarpInrun = 5;                      // Minimal number of successive steps with that speed
      public int strategyMaxInnerIterations = 100;           // Maximal number of inner iterations in line search.
      public int strategyOverwarp = 3;                       // Factor of moving in the negative direction on warp. 1 = set to zero, 2 = mirrow, > 2 = exaggerate warp
      
      // all derivatives are enumerated according to the paraNames list.
      public double ll;
      public double[] llD;
      public double[][] llDD;
      public double ls;
      public double[] lsD;
      public double[][] lsDD;
      public double pathLambda;
      public double llPathD;
      public double[] llPathDD;
      public double[] pathDirection;
      public double logDetHessian = Double.NaN;
 
      // target Model overwrite from EstimationRunContainer so that the model operates as its own run container
      public Model estimationModel = this;
      
      // if true, modelMove will be implemented without further actions.
      public boolean isWarping;
      
      // Some working variables
      private double[][][] modelPrecisionDev;
      private double[][] modelSigmaWork, modelSigmaWork1, modelSigmaWork2, modelSigmaWorkPD, modelSigmaWork2PD, modelWork1, modelWork2, modelWork3, modelWork4, modelVarParWork;
      private double[] p1w, p2w, cew, a1w, a2w, modelMove, modelMuWork, modelMuWork1, modelMuWork2, modelMuWork3, modelVecWork1, modelVecWork2, modelVecWork3;
      private double[][] rectWork, rectWork2;
      protected double[] logresult = new double[1];
      private GivensSeries workGivens = new GivensSeries(100, true);
      
      // Working variables for missing subgroup subdistributions;
      private double[][] availableMean, missingMean;
      private double[][][] availableCov, availableMissingCov, missingAvailableCov, missingCov;
      private double[][][] availableCovInv;
      public Distribution distributionPositionEM;
      private Distribution distributionWork;
      private int[][][] missingPattern;
      public double[] emMethodLastPosition;
      public double emMethodLastFit;
      public boolean emMethodIsConverged;
      private double[][] ctrlCov, ctrlCovWork, ctrlTargetCov, targetCtrlCov, ctrlB;

      double[][] sigInv;
      double[] lastEstimate;
      public double convergenceIndex; // <=1 no convergence, ca. above 1.3 good convergence, 2 theoretically optimal (quadratic convergence); set to 2 if step small than EPS.
      public double lastGain, lastSteplength, lastDamping;
      public int steps;
      public int stepsEM;
      
      public double[][] fisherInformationMatrix;
      
      public static boolean isMissing(double value) {
          if (Double.isNaN(value)) return true;
          return value == MISSING;
      }
      
      public void setRandomSeed(long seed) {rand.setSeed(seed);}
      public void setRandomSeed() {rand = new Random();}

      public void setHistory(OptimizationHistory history) {this.history = history;}
      
      public int getAnzPar() {return anzPar = paraNames.length;}
      public int getAnzVar() {return anzVar;}

      /** copies this */
      public abstract Model copy(); 
      /** copies the model and removes the indicated observation **/
      public abstract Model removeObservation(int obs); 
      /** Computes matrix multiplied by the derivative of sigma wrt. par (active Parameter Number), matrix = null computes only the derivative. */
      protected abstract void computeMatrixTimesSigmaDev(int par, double[][] matrix, double[][] erg);
      /** Computes matrix multiplied by the 2nd derivative of sigma wrt. par1, par2 (active Parameter Number), matrix = null computes only the derivative. */
      protected abstract void computeMatrixTimesSigmaDevDev(int par1, int par2, double[][] matrix, double[][] erg);
      /** Computes matrix multiplied by the derivative of mu wrt. par (active Parameter Number), matrix = null computes only the derivative. */
      protected abstract void computeMatrixTimesMuDev(int par, double[][] matrix, double[] erg);
      /** Computes matrix multiplied by the 2nd derivative of mu wrt. par 1, par2 (active Parameter Number), matrix = null computes only the derivative. 
       *  Has a default implementation that returns zero. */
      protected void computeMatrixTimesMuDevDev(int i, int j, double[][] matrix, double[] erg) {Statik.setToZero(erg);}
      
      /** Computes the inverse of sigma multiplied by the derivative of sigma wrt. par (active Parameter Number). */
      protected void computeSigmaInvDev(int par, double[][] erg) {computeMatrixTimesSigmaDev(par, sigInv, erg);}
      /** Computes the inverse of sigma multiplied by the 2nd derivative of sigma wrt. par1, par2 (active Parameter Number).  */
      protected void computeSigmaInvDevDev(int par1, int par2, double[][] erg) {computeMatrixTimesSigmaDevDev(par1, par2, sigInv, erg);}
      /** Computes the inverse of sigma multiplied by the derivative of mu wrt. par (active Parameter Number) */
      protected void computeSigmaInvMuDev(int par, double[] erg) {computeMatrixTimesMuDev(par, sigInv, erg);}
      
      public void setStrategy(Strategy strategy) {
          if (strategy == Strategy.classic) strategyUseClassicalOnyx = true;
          if (strategy == Strategy.defaul || strategy == Strategy.defaultWithEMSupport) {
              double EPS = 0.001;
              strategyUseClassicalOnyx = false; strategyUseHessian = true; strategyUseCholeskyFirst = true; strategyUseOertzenOptimization = true; 
              strategyUseLineSearch = true; strategyUseGradientNumerator = false; strategyUseWarp = true;
              strategyCholeskyTolerance = EPS*EPS; 
              strategyMaximalStepFactor = 1.0 / (EPS*EPS*EPS);
              strategyReductionPowerOnNegative = 0.8;
              strategyMaxInnerIterations = 100;
              strategyWarpMinimalSpeed = 1.0;               
              strategyWarpInrun = 5;                      
              strategyMaxInnerIterations = 100;           
              strategyOverwarp = 3;
              strategyAllowNonPDSigma = false;
              strategyUseEMWithSaturated= false;
          }
          if (strategy == Strategy.defaultWithEMSupport) strategyUseEMWithSaturated = true;
          
          lastChosenStrategyPreset = strategy;
      }
      
      public Strategy getStrategy() {return lastChosenStrategyPreset;}
      
      public void setData(double[][] data, int[] foreignKey) {
          this.dataForeignKey = foreignKey;
          setData(data);
      }
      public void setData(double[][] data)
      {
          isIndirectData = false;
          this.data = data;
          if (data != null) {
              this.anzPer = data.length;
              computeMoments();
          }
      }
      
      public Random getRandom() {return rand;}
    
      public void computeMoments() {computeMoments(true);}
      public void computeMoments(boolean recomputeDataMeanAndCov)
      {
          if (data==null) return;
          if ((xsum==null) || (xsum.length!=anzVar)) xsum = new double[anzVar];
          if (recomputeDataMeanAndCov && ((dataMean==null) || (dataMean.length!=anzVar))) dataMean = new double[anzVar];
          for (int i=0; i<anzVar; i++) xsum[i] = 0.0;
          try {
              for (int i=0; i<anzPer; i++)
                  for (int j=0; j<anzVar; j++) xsum[j] += data[i][j];
          } catch (Exception e) {
              // DEBUG
              System.out.println("Exception in compute Moments, person = "+anzPer+", variable = "+anzVar+", xsum length = "+xsum.length+", data length = "+data.length+", data width = "+(data.length>0?data[0].length:-1)+".");
              throw new RuntimeException(e);
          }
          if (recomputeDataMeanAndCov) for (int i=0; i<anzVar; i++) dataMean[i] = xsum[i] / (double)anzPer;
            
          if ((xBiSum==null) || (xBiSum.length!=anzVar) || (xBiSum[0].length!=anzVar)) xBiSum = new double[anzVar][anzVar];
          if (recomputeDataMeanAndCov && ((dataCov==null) || (dataCov.length!=anzVar) || (dataCov[0].length!=anzVar))) dataCov = new double[anzVar][anzVar];
          if (recomputeDataMeanAndCov) Statik.setToZero(dataCov); 
          for (int i=0; i<anzVar; i++)
              for (int j=0; j<=i; j++)
              {
                  xBiSum[i][j] = 0.0;
                  for (int k=0; k<anzPer; k++) xBiSum[i][j] += data[k][i] * data[k][j];
                  if (recomputeDataMeanAndCov) for (int k=0; k<anzPer; k++) dataCov[i][j] += (data[k][i]-dataMean[i]) * (data[k][j]-dataMean[j]);
                  xBiSum[j][i] = xBiSum[i][j];
                  if (recomputeDataMeanAndCov) dataCov[i][j] /= ((double)anzPer);
                  if (recomputeDataMeanAndCov) dataCov[j][i] = dataCov[i][j];
              }
      }
      
      public void computeMomentsFromDataCovarianceAndMean()
      {
          if ((xsum==null) || (xsum.length!=anzVar)) xsum = new double[anzVar];
          if ((xBiSum==null) || (xBiSum.length!=anzVar) || (xBiSum[0].length!=anzVar)) xBiSum = new double[anzVar][anzVar];
          for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) xBiSum[i][j] = anzPer*dataCov[i][j] + anzPer*dataMean[i]*dataMean[j];
          for (int i=0; i<anzVar; i++) xsum[i] = anzPer*dataMean[i];
      }

      public void setMaximalNumberOfIterations(int max) {MAXRUNS = max;}
      public int getMaximalNumberOfIterations() {return MAXRUNS;}
      public void setMinimalNumberOfIterations(int min) {MINRUNS = min;}
      public int getMinimalNumberOfIterations() {return MINRUNS;}
      
      public void setDataDistribution() {
          evaluateMuAndSigma();
          setDataDistribution(sigma, mu, anzPer);           
      }
      public void setDataDistribution(double[][] dataCovariance, double[] dataMean) {setDataDistribution(dataCovariance, dataMean, 1);}
      public void setDataDistribution(double[][] dataCovariance, double[] dataMean, int anzPer) {
          this.dataCov = Statik.copy(dataCovariance);
          this.dataMean = Statik.copy(dataMean);
          this.anzPer = anzPer;
          try {computeMomentsFromDataCovarianceAndMean();} catch (Exception e) {}
          this.isIndirectData = true;
      }
      
      /**
       * Expects a String of the format "XXX = 123", "123", or "XXX". If XXX is present, the parameter is added to the parameter name list or found
       * in it, and stored in the first entry of the return array. If format is "XXX = 123", the parameter value in the position array is set to that 
       * value (setParameter is not used to allow this method begin used during initialization). if "123" is present, that value is returned in the
       * second entry of the return array.
       * 
       * @param in
       * @return
       */
      protected double[] parseParameterNameAndValue(String in) {
          in = Statik.loescheRandWhitespaces(in);
          if (Character.isDigit(in.charAt(0))) {
              int ix = 1; while (ix < in.length() && (Character.isDigit(in.charAt(ix)) || in.charAt(ix)=='.' || in.charAt(ix)=='-' || in.charAt(ix)=='E')) ix++;
              return new double[]{-1,Double.parseDouble(in.substring(0,ix))};
          } else {
              String[] sp = in.split("=");
              String parName = Statik.loescheRandWhitespaces(sp[0]);
              int pnr = getParameterNumber(parName);
              if (pnr == -1) {
                  String[] t = this.paraNames; paraNames = new String[paraNames.length+1]; 
                  for (int i=0; i<t.length; i++) paraNames[i] = t[i];
                  double[] tp = this.position; position = new double[tp.length+1]; Statik.copy(tp, position);
                  pnr = position.length;
              }
              if (sp.length==1) return new double[]{pnr,Double.NaN};
              String val = Statik.loescheRandWhitespaces(sp[1]);
              int ix = 1; while (ix < val.length() && (Character.isDigit(val.charAt(ix)) || val.charAt(ix)=='-' || val.charAt(ix)=='E' || val.charAt(ix)=='.')) ix++;
              return new double[]{pnr,Double.parseDouble(val.substring(0,ix))};
          }
      }
      /** Sets parameter to the given value. Must be overwritten */
      public abstract boolean setParameter(int nr, double value);
      /** Returns parameter value. Returns Missing if their is no parameter to that number in the model. */
      public abstract double getParameter(int nr);
      /** Replaces the parameter with the given number by NOPARAMETER and reduces all higher numbers by one. */
      protected abstract void removeParameterNumber(int nr);
      /** Returns the highest parameter number in the model structure. */
      protected abstract int maxParNumber();

      protected void fixParameter(int nr) {removeParameterNumber(nr); anzPar--;}
      
      /** Invents parameter Names. May be overwritten, but doesn't need to.*/
      protected void inventParameterNames(String prefix) {
          int count = 0;
          int max = maxParNumber();
          for (int i=0; i<=max; i++) {
              double v = getParameter(i);
              if (Model.isMissing(v)) fixParameter(i); else count++;
          }
          anzPar = count;
          position = Statik.ensureSize(position, anzPar); 
          startingValues = Statik.ensureSize(startingValues, anzPar);
          paraNames = new String[anzPar];
          for (int i=0; i<anzPar; i++) paraNames[i] = prefix + "p"+i;          
      }
      protected void inventParameterNames() {inventParameterNames("");}
      
      protected void removeParameterName(int nr) {
          String[] newParName = new String[paraNames.length-1];
          for (int i=0; i<newParName.length; i++) newParName[i] = paraNames[(i>=nr?i-1:i)];
          paraNames = newParName;
      }
      protected void addParameterName(String parameterName) {
          String[] newParName = new String[paraNames.length+1];
          for (int i=0; i<paraNames.length; i++) newParName[i] = paraNames[i];
          newParName[newParName.length-1] = parameterName;
          paraNames = newParName;
      }
      
      /** Returns all parameter names in the correct order */
      public String[] getParameterNames() {return paraNames;}
      public void setParameterNames(String[] paraNames) {this.paraNames = Statik.copy(paraNames);}
      
      /** Sets all parameter to the given values, in order of the parameter Name String. If length exceeds anzPar, the remaining values are ignored.
       * If length is smaller than anzPar, remaining parameter are not set. 
       * returns true if length equals anzPar and all single setParameter returned true.
       */
      public boolean setParameter(double[] values) {
          position = Statik.ensureSize(position, anzPar);
          boolean erg = (anzPar == values.length); for (int i=0; i<Math.min(anzPar,values.length); i++) erg = setParameter(i,values[i]) & erg;
          return erg;
      }      

      protected int getParameterNumber(String paraName) {
          for (int i=0; i<paraNames.length; i++) if (paraNames[i].equals(paraName)) return i;
          return -1;          
      }
      /** updates the position array and returns a copy */
      public double[] getParameter() {
          position = Statik.ensureSize(position, getAnzPar());
          for (int i=0; i<position.length; i++) position[i] = getParameter(i); 
          return Statik.copy(position);
      }
      public boolean fixParameter(int nr, double value) {
          boolean erg = setParameter(nr, value);
          fixParameter(nr);
          return erg;
      }
      public boolean setParameter(String name, double value) {
          for (int i=0; i<paraNames.length; i++) if (paraNames[i].equals(name)) return setParameter(i, value);
          return false;
      }
      public boolean fixParameter(String name) {
          for (int i=0; i<paraNames.length; i++) if (paraNames[i].equals(name)) {fixParameter(i); return true;}
          return false;
      }
      public boolean fixParameter(String name, double value) {
          boolean erg = setParameter(name, value);
          erg = fixParameter(name) && erg;
          return erg;
      }
      public List<String> getSortedParameterNames()
      {
          List<String> list = new ArrayList<String>(paraNames.length);
          for (int i=0; i<paraNames.length; i++) list.add(paraNames[i]);
          Collections.sort(list);
          return list;
      }
      public int getParameterIndex(String name) {
          for (int i=0; i<paraNames.length; i++) if (paraNames[i].equals(name)) return i;
          return NOPARAMETER;
      }
      public double getParameter(String name) {
          for (int i=0; i<paraNames.length; i++) if (paraNames[i].equals(name)) return getParameter(i);
          return MISSING;
      }
      public double getStartingValue(String name) {
          for (int i=0; i<paraNames.length; i++) if (paraNames[i].equals(name) && startingValues != null && startingValues.length > i) return startingValues[i];
          return MISSING;
      }
      public boolean setParameter(String[] names, double[] values) {
          boolean erg = true; for (int i=0; i<Math.min(names.length,values.length); i++) erg = setParameter(names[i], values[i]) && erg;
          return erg;
      }
      public boolean setParameter(int[] index, double[] values) {
          boolean erg = true;
          for (int i=0; i<Math.min(index.length,values.length); i++) erg = setParameter(index[i], values[i]) && erg;
          return erg;
      }
      public double[] getParameter(String[] names) {
          double[] erg = new double[names.length];
          for (int i=0; i<erg.length; i++) erg[i] = getParameter(names[i]);
          return erg;
      }
      public boolean copyParameterFromModel(Model model) {
          return setParameter(model.getParameterNames(), model.getParameter());
      }
      
      public abstract boolean isErrorParameter(int nr);
      public boolean isErrorParameter(String paraName) {return isErrorParameter(getParameterNumber(paraName));}
      
      public boolean containsParameter(String paraName) {for (int i=0; i<paraNames.length; i++) if (paraName.equals(paraNames[i])) return true; return false;}
      
      
      public double[] getMetaParameter() {return new double[]{Double.NaN, Double.NaN, anzPer};}
      public double getMetaParameter(int nr) {if (nr==META_N) return anzPer; else return Double.NaN;}
      public void setMetaParameter(int nr, double val) {if (nr==META_N) anzPer = (int)Math.round(val); }
      public void setMetaParameter(double[] val) {for (int i=0; i<val.length; i++) setMetaParameter(i, val[i]);}
      public boolean isIntegerMetaParameter(int nr) {if (nr==META_N) return true; else return false;}
    
      /** Fixes the parameters to the value and sets it to be inactive. */
      public void fixParameter(int[] nrs) {for (int i=nrs.length-1; i>=0; i--) fixParameter(nrs[i]);}
      /** Fixes the parameters to the given value and sets it to be inactive. */
      public void fixParameter(int[] nrs, double[] values) {for (int i=nrs.length-1; i>=0; i--) fixParameter(nrs[i], values[i]);}
      
      /** Creates a data set according to model sigma. */
      public double[][] createData (int anzPersons, double[] values) {setParameter(values); return createData(anzPersons);}
      public double[][] createData (int anzPersons) {
          this.anzPer = anzPersons;
          data = new double[anzPersons][anzVar];
          
          evaluateMuAndSigma(null);
          setData(createData(anzPersons, mu, sigma, rand));
          computeMoments();
          return data;
      }
      public static double[][] createData (int anzPersons, double[] mu, double[][] sigma, Random rand)
      {
          int anzVar = mu.length;
          double[][] erg = new double[anzPersons][anzVar];
          double[][] cholesky = Statik.choleskyDecompose(sigma,.0001);

          double[] m = new double[anzVar], m2 = new double[anzVar];
          for (int i=0; i<anzPersons; i++)
          {
              for (int j=0; j<anzVar; j++) m[j] = rand.nextGaussian();
              Statik.multiply(cholesky,m,m2);
              for (int j=0; j<anzVar; j++) erg[i][j] = m2[j]+mu[j];
          }
          return erg;
      }
          
      /** Recomputes mu and Sigma of the model. */
      public void evaluateMuAndSigma() {evaluateMuAndSigma(null);}
      /** Recomputes mu and Sigma, applying the given values for the parameter. values can be of the length of active or all parameters.
       *  values = null means using the actual parameter values. */
      public abstract void evaluateMuAndSigma(double[] values);
    
      /** Computes the least Squares index. */
      public double getLeastSquares() {return getLeastSquares(null);}
      /** Applies the parameter values (null => actual values) and computes the least Squares index. */
      public double getLeastSquares(double[] values)
      {
          evaluateMuAndSigma(values);
          ls = 0;
          for (int i=0; i<anzVar; i++)
              for (int j=0; j<anzVar; j++) ls += 0.5*(sigma[i][j] - dataCov[i][j])*(sigma[i][j] - dataCov[i][j]);
          for (int i=0; i<anzVar; i++) ls += 0.5*(mu[i] - dataMean[i])*(mu[i] - dataMean[i]);
          return ls;
      }
        
      /** Computes the -2 log likelihood index. */
      public double getMinusTwoLogLikelihood() {return getMinusTwoLogLikelihood(null, true);}
      /** Applies the parameter values (null => actual values) and computes the -2 log likelihood index. */
      public double getMinusTwoLogLikelihood(double[] value) {return getMinusTwoLogLikelihood(value, true);}
      /** Applies the parameter values (null => actual values) and computes the -2 log likelihood index. 
       * If recomputeMuAndSigma is false, these values will not be recomputed. */
      public double getMinusTwoLogLikelihood(double[] value, boolean recomputeMuAndSigma)
      {
          if ((modelSigmaWork==null) || (modelSigmaWork.length!=anzVar) || (modelSigmaWork[0].length!=anzVar)) modelSigmaWork = new double[anzVar][anzVar];
          if ((sigInv==null) || (sigInv.length!=anzVar) || (sigInv[0].length!=anzVar)) sigInv = new double[anzVar][anzVar];

          if (value != null) setParameter(value);
          if (recomputeMuAndSigma) evaluateMuAndSigma(value);
        
          if (anzVar == 0) {
              sigmaDet = Double.NaN; ll = Double.NaN; 
          } else {
          
            try {sigmaDet = Statik.invert(sigma,sigInv, modelSigmaWork);} catch(RuntimeException e) {sigmaDet = 0;}
            if ((Statik.determinantOfPositiveDefiniteMatrix(sigma)==-1) && (sigmaDet > 0))
            {
                if (logStream != null) logStream.println("Warning: Not positive defnite Sigma, I set ll to NaN.");
                sigmaDet = Double.NaN;
            }
            
            //TODO: modification by brandmaier
            // detect bad range for determinant
            /*if (sigmaDet  < 0.000001) {
            	sigmaDet = Double.NaN;
            }*/
            

            ll = anzPer*anzVar*LNTWOPI + anzPer*Math.log(sigmaDet);

            
            // Deprecated, replaced by taking the difference of ll to getSaturatedLL().
//            if (relativeToSaturatedModel) {
//                double dataCovDet = Statik.determinant(dataCov);
//                ll -= anzPer*(Math.log(dataCovDet) + anzVar);
//            } else ll += anzPer*anzVar*LNTWOPI;
            
            for (int i=0; i<anzVar; i++) 
                for (int j=0; j<anzVar; j++)
                    ll += sigInv[i][j]*(xBiSum[i][j] - anzPer*mu[i]*dataMean[j] - anzPer*mu[j]*dataMean[i] + anzPer*mu[i]*mu[j]);
            
            if ((logStream!=null) && (ll < 0)) logStream.println("Warning: encountered negative Log Likelihood (possible, but very rare). I accept it as such.");
          }
          
        return ll;
    }
      /**
       * Returns the standard deviation of the parameter estimates as the diagonal elements of the inverted Hessian.
       * 
       * @return the standard deviation of the parameter estimates 
       */
      public double[] getParameterSTDV() {
         double[][] inv = new double[anzPar][anzPar];
         Statik.invert(llDD, inv, logresult);
         logDetHessian = logresult[0];
         double[] erg = new double[anzPar];
         for (int i=0; i<anzPar; i++) erg[i] = Math.sqrt(2*inv[i][i]);
         return erg;
      }

      /**
       * Computes the least square index and for each active parameter its derivative and second derivative. 
       * If values is not null, these parameter values are applied; otherwise, the method assumes all values are correct.
       */
      public void computeLeastSquaresDerivatives(double[] value) {computeLeastSquaresDerivatives(value,true);}
      /**
       * Computes the least square index and for each active parameter its derivative and second derivative. 
       * If values is not null, these parameter values are applied; otherwise, the method assumes all values are correct.
       * If recomputeMuAndSigma is false, these values will not be recomputed.
       */
      public abstract void computeLeastSquaresDerivatives(double[] value, boolean recomputeMuAndSigma);

      /** Finds arbitrary starting values */
    public double[] getArbitraryStartingValues()
    {
        double[] erg = new double[anzPar];
        
        for (int i=0; i<anzPar; i++)
        {
            erg[i] = 1.0555;
            if (isErrorParameter(i)) erg[i] = 3.0555;
        }
        
        return erg;
    }

    /** Computes the minus two log likelihood index and its first and second derivatives wrt to all active parameters. 
     * If value is null, actual parameter values are assumed */
    public void computeLogLikelihoodDerivatives(double[] value) {computeLogLikelihoodDerivatives(value,true);}
    /** Computes the minus two log likelihood index and its first and second derivatives wrt to all active parameters. 
     * If value is null, actual parameter values are assumed.
     * If recomputeMuAndSigma is false, these values will not be recomputed. */
    public abstract void computeLogLikelihoodDerivatives(double[] value, boolean recomputeMuAndSigma);

    public void computeFitGradientAndHessian(double[] value, boolean useMaximumLikelihood) {computeFitGradientAndHessian(value, true, useMaximumLikelihood);}
    public void computeFitGradientAndHessian(double[] value, boolean recomputeMuAndSigma, boolean useMaximumLikelihood) {
        if (useMaximumLikelihood) fitFunction = Objective.maximumLikelihood; else fitFunction = Objective.Leastsquares;
        if (!useMaximumLikelihood) computeLeastSquaresDerivatives(value, recomputeMuAndSigma);
        else if (strategyUseGradientNumerator) computeLogLikelihoodDerivativesNumerator(value, recomputeMuAndSigma, true);
        else computeLogLikelihoodDerivatives(value, recomputeMuAndSigma);
    }
    
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
        modelSigmaWork1 = Statik.ensureSize(modelSigmaWork1, anzVar, anzVar);
        modelSigmaWork2 = Statik.ensureSize(modelSigmaWork2, anzVar, anzVar);
        
        computeLogLikelihoodDerivatives(value, recomputeMuAndSigma);
        double sigDetSqr = sigmaDet*sigmaDet;
        if (!dividedBySigmaDetSqr) for (int i=0; i<anzPar; i++) llD[i] *= sigDetSqr;
        for (int i=0; i<anzPar; i++) {
            computeMatrixTimesSigmaDev(i, sigInv, modelSigmaWork1);
            double tr = Statik.trace(modelSigmaWork1);
            for (int j=0; j<anzPar; j++) llDD[i][j] += 2*llD[j]*tr;
            if (!dividedBySigmaDetSqr) for (int j=0; j<anzPar; j++) llDD[i][j] *= sigDetSqr;
        }
    }
   
    /**
     * Computes an optimization step according to the strategy flags. Returns result in modelMove.
     * 
     * @param model
     */
    private boolean suggestOptimizationStep(double[] gradient, double[][] hessian) {
        modelWork1 = Statik.ensureSize(modelWork1, anzPar, anzPar);

        ll = getMinusTwoLogLikelihood();

        boolean globalWarpCondition = false;
        if (strategyUseWarp && history != null && history.getSize() > strategyWarpInrun+1) {
            globalWarpCondition = true;
            
            for (int i=history.getSize() - strategyWarpInrun - 2; i<history.getSize()-2; i++) {
                if (history.getLastGain(i+1) > history.getLastGain(i) || 
                    history.getLastSteplength(i+1) < history.getLastSteplength(i) || 
                    history.getLastSteplength(i) < strategyWarpMinimalSpeed) {globalWarpCondition = false; break;}
            }
        }         
        
        if (strategyUseHessian) {
            if (strategyUseClassicalOnyx) {
                double[] debug = new double[anzPar];
                Statik.solveNaiveWithEigenvaluePositiviation(hessian, gradient, debug, suggestedEPS,
                        modelVecWork1, modelWork1, modelWork2, modelWork3);
    
                boolean wasPD = false;
                try {
                    wasPD = Statik.pseudoInvertSquare(hessian, modelWork1, modelWork2, modelWork3, modelWork4, suggestedEPS, true, logresult);
                } catch (Exception e) {
                    logDetHessian = logresult[0]; 
                    return false;
                }
                logDetHessian = logresult[0]; 
                double norm = Statik.norm(modelWork1);
                if (norm != 0.0) { 
                    Statik.multiply(modelWork1, gradient, modelMove);
                } else Statik.copy(gradient, modelMove);
                 
//                if (wasPD && norm != 0.0 && Statik.norm(Statik.subtract(debug, modelMove))>0.1)
//                    System.out.println("DEBUG: Different suggested moves");

                double storeNewtonStepLength = Statik.abs(modelMove)*(modelMove[0]>=0?-1:1);
                if (storeNewtonStepLength == 0) return false;
                if (!wasPD) Statik.multiply(1.00354/Math.abs(storeNewtonStepLength), modelMove, modelMove);
                
                for (int i=0; i<anzPar; i++) modelMove[i] = -modelMove[i];
                
                logDetHessian = logresult[0];                
                return true;
            } else {
                boolean isFinished = false;
                if (strategyUseCholeskyFirst) {
                    try {
                        Statik.solveSymmetricalPositiveDefinite(hessian, gradient, modelMove, -strategyCholeskyTolerance, logresult);
                        logDetHessian = logresult[0];
                        isFinished = true;
                    } catch (Exception e) {isFinished = false;}
                }
                double[] eigenvalues = modelVecWork1, gradientEV = modelVecWork2;
                double[][] householder = modelWork1;
                GivensSeries givens = workGivens;
                if (!isFinished && strategyUseOertzenOptimization) {
                    Statik.eigenvaluesOfSymmetrical(hessian, eigenvalues, householder, givens);
                    
                    Statik.multiplyHouseholderAndGivensSeriesToVector(gradient, householder, givens, gradientEV, false);
                    
                    for (int i=0; i<anzPar; i++) {
                        double factor = 1.0 / eigenvalues[i];
                        boolean evNegative = factor < 0;
                        boolean evZero = Math.abs(factor) > strategyMaximalStepFactor; 
                        if (evZero) factor = strategyMaximalStepFactor;
                        if (evNegative) factor = Math.pow(Math.abs(factor),strategyReductionPowerOnNegative);
                        if (history != null) history.writePerDimension(i, eigenvalues[i], gradientEV[i], factor);
                        
                        gradientEV[i] *= factor;
                    }
                    Statik.multiplyHouseholderAndGivensSeriesToVector(gradientEV, householder, givens, modelMove, true);
                    logDetHessian = 0; for (int i=0; i<anzPar; i++) logDetHessian += Math.log(eigenvalues[i]);
                    
                    isFinished = true;
                } 
                if (!isFinished) { Statik.qrSolve(hessian, gradient, modelMove); isFinished = true;}
                for (int i=0; i<anzPar; i++) modelMove[i] = -modelMove[i];
                isWarping = false;
                double[] warpDirection = modelMove;
                if (globalWarpCondition) {
                    boolean eigenvaluesAvailable = true;
                    for (int j=history.getSize() - strategyWarpInrun - 2; j<history.getSize()-2; j++) if (Model.isMissing(history.getEigenvalue(j,0))) eigenvaluesAvailable = false;
                    // this part computes the warp direction, may reset globalWarpCondition to false
                    if (eigenvaluesAvailable) {
                        // If Eigenvalues are known, local warp conditions are computed to identify warp direction.
                        warpDirection = new double[anzPar]; Statik.setTo(warpDirection, 1);
                        for (int j=history.getSize() - strategyWarpInrun - 2; j<history.getSize()-2; j++)  {
                            for (int i=0; i<anzPar; i++) {
                                if (Math.abs(history.getGradientOnEV(j,i)) < Math.abs(history.getGradientOnEV(j+1, i)) || 
                                    Math.abs(history.getEigenvalue(j,i)) < Math.abs(history.getEigenvalue(j+1, i)) || 
                                    history.getEigenvalue(j,i) / history.getEigenvalue(j, i) > history.getEigenvalue(j+1,i) / history.getEigenvalue(j+1, i)) 
                                    warpDirection[i] = 0.0;
                            }
                        }
                        globalWarpCondition = false; for (int i=0; i<anzPar; i++) if (warpDirection[i] != 0.0) {globalWarpCondition= true; break;}
                        if (globalWarpCondition) {
                            Statik.multiplyHouseholderAndGivensSeriesToVector(warpDirection, householder, givens, modelMove, true);
                            Statik.copy(modelMove, warpDirection);
                        }
                    }
                }
                if (globalWarpCondition) {
                    // computes the modelMove as a Householder reflection that negates the contribution of the warpDirection on the current position.
                    double normsqr = 0, prod = 0;
                    for (int i=0; i<anzPar; i++) {normsqr += warpDirection[i]*warpDirection[i]; prod += warpDirection[i]*position[i];}
                    for (int i=0; i<anzPar; i++) modelMove[i] = - strategyOverwarp*prod*warpDirection[i] / normsqr; 
                    isWarping = true;
                }
                return true;
            }
        } else {
            Statik.multiply(strategyGradientDescentDamping, gradient, modelMove);
            return true;
        }
    }
    
    /** 
     * performs a line search in the given move direction. The damped movement is returned in the argument. 
     * Gradient is used only for initial search direction. Returns true if step works, false if no step can be made. 
     * 
     * @param move
     * @return flag whether step worked
     */
    public boolean lineSearch(double[] pos, double[] move, boolean useMaximumLikelihood, double EPS) {
        p1w = Statik.ensureSize(p1w, pos.length);
        p2w = Statik.ensureSize(p2w, pos.length);
        cew = Statik.ensureSize(cew, pos.length);
        a1w = Statik.ensureSize(a1w, pos.length);
        a2w = Statik.ensureSize(a2w, pos.length);

        double startValue = (useMaximumLikelihood?getMinusTwoLogLikelihood(pos):getLeastSquares(pos));
        Statik.copy(pos,p1w);
        double p1v = startValue;
        int n = anzPar;
        
        double[] gradient = (useMaximumLikelihood?llD:lsD), t;
        double td;
        double derivative = Statik.multiply(gradient, move);
        Statik.multiply((int)Math.signum(-derivative), move, p2w);
        Statik.add(pos,p2w,p2w);
        double p2v = (useMaximumLikelihood?getMinusTwoLogLikelihood(p2w):getLeastSquares(p2w));
        
        int inRuns = 0;
        while (((Double.isNaN(p2v)) || (Double.isInfinite(p2v)) || (p2v > p1v)) && (inRuns<strategyMaxInnerIterations)) {
            for (int i=0; i<n; i++) p2w[i] = (p1w[i]+p2w[i])/2.0; 
            p2v = (useMaximumLikelihood?getMinusTwoLogLikelihood(p2w):getLeastSquares(p2w));
            inRuns++;
        }
        
        double cev = p2v+EPS*EPS, a1v, a2v, dist;
        
        // p1 will not be manipulated in this loop.
        while ((p2v < p1v) && (p2v < cev))
        {
            for (int i=0; i<n; i++) cew[i] = p2w[i]; cev = p2v;
            for (int i=0; i<n; i++) p2w[i] = 2 * cew[i] - p1w[i];
            p2v = (useMaximumLikelihood?getMinusTwoLogLikelihood(p2w):getLeastSquares(p2w));
            while (((Double.isNaN(p2v)) || (Double.isInfinite(p2v))) && (inRuns<strategyMaxInnerIterations)) {
                for (int i=0; i<n; i++) p2w[i] = (cew[i]+p2w[i])/2.0; 
                p2v = (useMaximumLikelihood?getMinusTwoLogLikelihood(p2w):getLeastSquares(p2w));
                inRuns++;
            }

            if (inRuns == 30 && logStream!=null) logStream.println("I seem to approach an infinite minimum, best value = "+
                    p2v+", previous was = "+cev+", start = "+startValue+", I'll try some more moves.");
            if (inRuns > 100) {
                if (logStream != null) logStream.println("I hit a boundary or infinite minimum, I give up the inner iteration."); 
                return false;
            }
            inRuns++;
        }

        for (int i=0; i<n; i++) cew[i] = (p1w[i]+p2w[i])/2.0; cev = (useMaximumLikelihood?getMinusTwoLogLikelihood(cew):getLeastSquares(cew));
        // at this point, it should be guaranteed that a minimum lies between p1w and p2w. 
        if (p2v < p1v) {t = p1w; p1w = p2w; p2w = t; td = p1v; p1v = p2v; p2v = td;}
        boolean goon = true; inRuns = 0;
        while ((goon) && (inRuns < strategyMaxInnerIterations))
        {
            while ((((cev>p1v) && (cev>p2v)) || (Double.isInfinite(cev)) || (Double.isNaN(cev))) && (inRuns < strategyMaxInnerIterations))
            {
                // This case means a maximum between p1v and p2v, or a illegal area, which is bad; 
                // taking the lower point is the best guess we can make. We will converge out of the prohibited area, but possibly
                // to a stretch without a minimum.
                t = p2w; p2w = cew; cew = t; td = p2v; p2v = cev; cev = td;
                for (int i=0; i<n; i++) cew[i] = (p1w[i]+p2w[i])/2.0; cev = (useMaximumLikelihood?getMinusTwoLogLikelihood(cew):getLeastSquares(cew));
                inRuns++;
            }
            if ((cev>p1v) || (cev>p2v)) {
                if (cev>p1v) {t = p2w; p2w = cew; cew = t; p2v = cev;} else {t = p1w; p1w = cew; cew = t; p1v = cev;}  
                for (int i=0; i<n; i++) cew[i] = (p1w[i]+p2w[i])/2.0; cev = (useMaximumLikelihood?getMinusTwoLogLikelihood(cew):getLeastSquares(cew));
            } else {
                // This is the case that center is the smaller than both p1 and p2. Quarters need to be computed to make sure
                // that a minimum lies in the part.
                for (int i=0; i<n; i++) a1w[i] = (p1w[i]+cew[i])/2.0; a1v = (useMaximumLikelihood?getMinusTwoLogLikelihood(a1w):getLeastSquares(a1w)); 
                for (int i=0; i<n; i++) a2w[i] = (p2w[i]+cew[i])/2.0; a2v = (useMaximumLikelihood?getMinusTwoLogLikelihood(a2w):getLeastSquares(a2w)); 
                if (a1v < cev) {t = p2w; p2w = cew; cew = t; p2v = cev; t = cew; cew = a1w; a1w = t; cev = a1v;}
                else if (a2v < cev) {t = p1w; p1w = cew; cew = t; p1v = cev; t = cew; cew = a2w; a2w = t; cev = a2v;}
                else {
                    // This is the case that both quarters are higher than the center.
                    t = p2w; p2w = a2w; a2w = t; p2v = a2v;
                    t = p1w; p1w = a1w; a1w = t; p1v = a1v;
                }
            }
            if (p2v < p1v) {t = p1w; p1w = p2w; p2w = t; td = p1v; p1v = p2v; p2v = td;}
            dist = 0; for (int i=0; i<n; i++) dist += (p1w[i]-p2w[i])*(p1w[i]-p2w[i]);
            if (dist < EPS*EPS) goon = false;
            
            if ((inRuns == 30) && (logStream!=null)) 
                logStream.println("It takes me some time to converge, best value so far = "+p1v+", alternative = "+p2v+", start = "+startValue);
            if (inRuns > strategyMaxInnerIterations) {
                if (logStream!=null) logStream.println("I give up at inner iteration now."); 
                return false;
            }
            inRuns++;
        }
        if (p1v < startValue) {
            for (int i=0; i<n; i++) move[i] = p1w[i] - pos[i];
        } else for (int i=0; i<n; i++) move[i] = 0.0;

        if (p1v < 0) {
            if (logStream!=null) logStream.println("Inner Iteration finished on a negative value: "+p1v); 
        }
        return true;
    }
    
    /**
     * Computes a single step in the newton method, using optimal damping in the direction suggested. Returns false if any problems occured.
     * If true is returned, convergence measures are computed.
     * 
     * @param useMaxLikelihood      If true, the maximum likelihood is optimized, otherwise the least squares.
     * @return                      true if successful, false if stuck or negative result
     */
    public boolean moveWithOptimalDamping(double EPS, boolean useMaximumLikelihood) {return moveWithOptimalDampingPregivenStep(EPS, useMaximumLikelihood, true);}

    /**
     * Computes a single step in the pregiven direction if computeNewtonStep is false and in the newton method if true.
     * 
     * @param EPS
     * @param useMaximumLikelihood
     * @param computeNewtonStep
     * @return
     */
    public boolean moveWithOptimalDampingPregivenStep(double EPS, boolean useMaximumLikelihood, boolean computeNewtonStep)
    {
        modelVecWork1 = Statik.ensureSize(modelVecWork1, anzPar);
        modelVecWork2 = Statik.ensureSize(modelVecWork2, anzPar);
        
        double[] pos = Statik.copy(position);      // just simpler naming
        
        steps++;
        if (history != null) {
            history.addPoint(); 
            history.writeFixed(MISSING, getSigmaDet(), MISSING, MISSING);
            for (int i=0; i<anzPar; i++) history.writePerParameter(i, position[i], (useMaximumLikelihood?llD:lsD)[i], MISSING);
        }

        
        double[] debug = new double[anzPar];
        if (computeNewtonStep)
        {
            boolean allOk = suggestOptimizationStep((useMaximumLikelihood?llD:lsD), (useMaximumLikelihood?llDD:lsDD));
            if (!allOk) return false;
            
            /*
             // THIS PART IS DEPRECATED. 
             
            // outsourced version; CG method won't compute logDetHessian!
//            Statik.solveByConjugateGradient((useMaximumLikelihood?llDD:lsDD), (useMaximumLikelihood?llD:lsD), EPS, 1, 1, true);
            
               // This solution usually works, needs more clarification whether it always does. 
            Statik.solveNaiveWithEigenvaluePositiviation((useMaximumLikelihood?llDD:lsDD), (useMaximumLikelihood?llD:lsD), debug, EPS, 
                    modelVecWork1, modelWork1, modelWork2, modelWork3);
//          
            
//            Statik.setToZero(debug);
            // DEBUG: REACTIVATE outsourced version and deactivate old version. 
            
            try {
                // TODO make this clever!
                wasPD = Statik.pseudoInvertSquare((useMaximumLikelihood?llDD:lsDD), modelWork1, modelWork2, modelWork3, modelWork4, EPS, true);
//                double debugVal = modelWork1[0][0];
//                wasPD = Statik.invertNegativeEigenvalues((useMaximumLikelihood?llDD:lsDD), modelWork1, modelWork2, modelWork3, p1w, true, false);
//                wasPD = Statik.invertNegativeEigenvalues((useMaximumLikelihood?llDD:lsDD), modelWork1, modelWork2, modelWork3, p1w, false, false);
//                double debugVal2 = modelWork1[0][0];
//                if (debugVal != debugVal2)
//                    System.out.println(".");
            } catch (Exception e) {
                logDetHessian = Statik.logResult; 
                return false;
            }
            logDetHessian = Statik.logResult; 
//            double[][] debug = Statik.copy(modelWork1);
//            try {Statik.invert((useMaximumLikelihood?llDD:lsDD), modelWork1, modelWork2);} catch (Exception e) {logDetHessian = Statik.logResult; return false;}
//            logDetHessian = Statik.logResult; 
//            Statik.copy(debug, modelWork1);
            double norm = Statik.norm(modelWork1);
            if (norm != 0.0) { 
                Statik.multiply(modelWork1, (useMaximumLikelihood?llD:lsD), modelMove);
            } else Statik.copy((useMaximumLikelihood?llD:lsD), modelMove);
            
            if (wasPD && norm != 0.0 && Statik.norm(Statik.subtract(debug, modelMove))>0.1)
                System.out.println("DEBUG: Different suggested moves");
//            double[] t = Statik.copy(modelMove); Statik.copy(debug, modelMove); debug = t; 
            
            logDetHessian = Statik.logResult;
            */
        }
        double startValue = (useMaximumLikelihood?getMinusTwoLogLikelihood(pos):getLeastSquares(pos));

        double storeNewtonStepLength = Statik.abs(modelMove)*(modelMove[0]>=0?-1:1);
        if (storeNewtonStepLength == 0) return false;
        
        if (strategyUseClassicalOnyx || (!isWarping && strategyUseLineSearch)) lineSearch(pos, modelMove, useMaximumLikelihood, EPS);
        
        for (int i=0; i<anzPar; i++) pos[i] += modelMove[i];
        
        // the return true block, computes some statistics about the convergence process and updates the second derivatives.
        try {
            setParameter(pos);
            computeFitGradientAndHessian(pos, useMaximumLikelihood);
            if (useMaximumLikelihood) getMinusTwoLogLikelihood(pos); else getLeastSquares(pos); // this is to make sure the same method is used for the comparison
            double steplength = Statik.abs(modelMove); 
            convergenceIndex = (steplength < 1.0?(steplength > EPS?Math.log(steplength)/Math.log(lastSteplength):2):1);
            lastDamping = steplength*(modelMove[0]>=0?1:-1) / storeNewtonStepLength; 
            lastSteplength = steplength;
            lastGain = startValue - (useMaximumLikelihood?ll:ls);
            if (history != null) {
                history.writeFixed(startValue, MISSING, lastGain, storeNewtonStepLength);
                for (int i=0; i<anzPar; i++) history.writePerParameter(i, MISSING, MISSING, modelMove[i]);
            }
//            for (int i=0; i<n; i++) pos[i] = pos[i] - modelMove[i];
//            for (int i=0; i<n; i++) modelMove[i] = p1w[i] - pos[i];
            if ((Double.isInfinite(lastGain)) || (!strategyAllowNonPDSigma && Double.isNaN(lastGain)))
                return false;
            return true;
        } catch (Exception e) {
            return false;
        }
    }    

    public double getSigmaDet() {return sigmaDet;}

    /**
     * Computes a single step in the newton method, using optimal damping in the direction suggested. Returns false if any problems occured.
     * If true is returned, convergence measures are computed.
     * 
     * @param useMaxLikelihood      If true, the maximum likelihood is optimized, otherwise the least squares.
     * @return                      true if successful, false if stuck or negative result
     */
    public boolean moveNewtonStep(double EPS, boolean useMaximumLikelihood)
    {
        double[] pos = Statik.copy(position), move = modelMove;      // just simpler naming
        
        steps++;
        
        double lastValue = (useMaximumLikelihood?ll:ls);
        Statik.invert((useMaximumLikelihood?llDD:lsDD), modelWork1, modelWork2, logresult);
        logDetHessian = logresult[0];
        Statik.multiply(modelWork1, (useMaximumLikelihood?llD:lsD), move);
        
        double derivative = Statik.multiply((useMaximumLikelihood?llD:lsD), move);
        Statik.multiply((int)Math.signum(-derivative), move, move);
        Statik.add(pos,move,pos);
        
        // the return true block, computes some statistics about the convergence process and updates the second derivatives.
        computeFitGradientAndHessian(pos, useMaximumLikelihood);
        double steplength = Statik.abs(move); 
        convergenceIndex = (lastSteplength < 1.0?(steplength > EPS?Math.log(steplength)/Math.log(lastSteplength):2):1);
        lastSteplength = steplength;
        lastGain = lastValue - (useMaximumLikelihood?ll:ls);
        return true;
    }    

    public void initEstimation(double[] starting, boolean useMaximumLikelihood)
    {
        if (useMaximumLikelihood) fitFunction = Objective.maximumLikelihood; else fitFunction = Objective.Leastsquares;

        warningFlag = warningFlagTypes.OK;
        modelMove = Statik.ensureSize(modelMove, anzPar);
        position = Statik.ensureSize(position, anzPar);
        modelWork1 = Statik.ensureSize(modelWork1, anzPar, anzPar);
        modelWork2 = Statik.ensureSize(modelWork2, anzPar, anzPar);
        modelWork3 = Statik.ensureSize(modelWork3, anzPar, anzPar);
        modelWork4 = Statik.ensureSize(modelWork4, anzPar, anzPar);

        if (starting == null) getParameter();
        else setParameter(starting);
        
        if (history == null || history.anzPar != anzPar) history = (paraNames==null?new OptimizationHistory(anzPar):new OptimizationHistory(paraNames));
        history.reset();
        
        steps = 0; stepsEM = 0; lastSteplength = Double.POSITIVE_INFINITY; lastGain = Double.POSITIVE_INFINITY;
        computeFitGradientAndHessian(position, useMaximumLikelihood);
        if (logStream != null) {
            logStream.print("-2ll\t|gradient|\t");
            for (int i=0; i<anzPar; i++) logStream.print("EVec1_"+i+"\t");
            for (int i=0; i<anzPar; i++) logStream.print("EVal_"+i+"\tGrad_"+i+"\tMove_"+i+"\t");
            logStream.println();
        }
        
        if (strategyUseEMWithSaturated) {
            distributionPositionEM = Distribution.ensureSize(distributionPositionEM, anzVar + anzAux + anzCtrl);
            distributionWork = Distribution.ensureSize(distributionWork, anzVar + anzAux + anzCtrl);

            emMethodIsConverged = false;
            createJointDataset();
            Statik.setToZero(distributionWork.mean);
            Statik.identityMatrix(distributionWork.covariance);
            distributionPositionEM = estimateSaturatedModel(jointData, suggestedEPS, distributionWork);
            controlVariables(distributionPositionEM, anzVar, anzVar+anzAux, anzCtrl);
            for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) dataCov[i][j] = distributionPositionEM.covariance[i][j];
            for (int i=0; i<anzVar; i++) dataMean[i] = distributionPositionEM.mean[i];
            try {computeMomentsFromDataCovarianceAndMean();} catch (Exception e) {System.out.println("Warning: In initializing estimation of EM, computing moments from data failed."); e.printStackTrace(System.out);}
            emMethodLastPosition = Statik.copy(position);
            emMethodLastFit = (useMaximumLikelihood?getMinusTwoLogLikelihood():getLeastSquares());
            missingPattern = computeMissingPattern(jointData);
            isIndirectData = true;
        }
    }
    
    /** Estimates Parameters by minimizing the least squares distance. */
    public double[] estimateLS() {return estimateLS(getArbitraryStartingValues(),suggestedEPS);}
    /** Estimates Parameters by minimizing the least squares distance, EPS gives the precision */
    public double[] estimateLS(double EPS) {return estimateLS(getArbitraryStartingValues(),EPS);}
    /** Estimates Parameters by minimizing the least squares distance, with the pregiven starting values. */
    public double[] estimateLS(double[] starting) {return estimateMLOrLS(starting, suggestedEPS, false);}
    /** Estimates Parameters by minimizing the least squares distance, EPS gives the precision, with the pregiven starting values. */
    public double[] estimateLS(double[] starting, double EPS) {return estimateMLOrLS(starting, EPS, false);}
    /** Estimates Parameters by minimizing the -2 log likelihood index with the least squares estimate as starting values */
    public double[] estimateML() {return estimateML(estimateLS(getArbitraryStartingValues(),suggestedEPS), suggestedEPS);}
    /** Estimates Parameters by minimizing the -2 log likelihood index, EPS gives the precision, with the least squares estimate as starting values */
    public double[] estimateML(double EPS) {return estimateML(estimateLS(getArbitraryStartingValues(),EPS), EPS);}
    /** Estimates Parameters by minimizing the -2 log likelihood index, with the pregiven starting values. */
    public double[] estimateML(double[] starting) {return estimateML(starting, suggestedEPS);}
    /** Estimates Parameters by minimizing the -2 log likelihood index, EPS gives the precision, with the pregiven starting values. */
    public double[] estimateML(double[] starting, double EPS) {return estimateMLOrLS(starting, EPS, true);}
    /** Estimates Parameters by minimizing either the -2 log likelihood or the least squares index */
    private double[] estimateMLOrLS(double[] starting, double EPS, boolean useMaximumLikelihood)
    {
        if (anzPar==0) {
            computeFitGradientAndHessian(starting, true, useMaximumLikelihood);
            return new double[0];
        }
        if (starting != null) setParameter(starting);
        starting = getParameter();
//        if (starting == null) {if (logStream != null) logStream.println("Starting values not given."); warningFlag = warningFlagTypes.FAILED; return null;}

        int startTries = 0; boolean allOk = false; double startValue = 0;
        while ((startTries < 3) && (!allOk)) {
            allOk = false; 
//            try {
                initEstimation(starting, useMaximumLikelihood);
                startValue = (useMaximumLikelihood?ll:ls);
                if (Double.isNaN(startValue) || Double.isInfinite(startValue)) allOk = false; else allOk = true;
//            } catch (Exception e) {    
//                if (logStream!=null) logStream.println("Initalizing with these starting values failed. ");
//                e.printStackTrace(logStream);
//                allOk = false;
//            }
            if (!allOk) {
                do {
                    for (int i=0; i<anzPar; i++) {
                        if (isErrorParameter(i)) starting[i] = Math.abs(2*starting[i]+0.1);
                        else starting[i] = starting[i] + rand.nextGaussian() * 0.01;
                    }
                    startValue = (useMaximumLikelihood?getMinusTwoLogLikelihood(starting):getLeastSquares(starting));
                    startTries++;
                } while (startTries < 3 && ((Double.isNaN(startValue)) || (Double.isInfinite(startValue))));
                if (startTries < 3) {if (logStream!=null) logStream.println("The index of the starting values is not defined, I try with different starting values!");} 
                else if (logStream!=null) logStream.println("Sorry, I couldn'nt make it work with these starting values.");
            }
        }
        if (!allOk) {
            if (logStream!=null) logStream.println("The index of the starting values is not defined, I can't optimize!"); 
            warningFlag = warningFlagTypes.FAILED; return starting;
        }
        
//        while ((runs<=MAXRUNS) && (steplength > EPS) && (lastGain > EPS))
//        while ((steps<=MAXRUNS) && (lastSteplength > EPS))
//        while ((steps<=MAXRUNS) && (lastSteplength > EPS || lastGain > EPS))
//        while ((steps < MINRUNS) || ((steps<=MAXRUNS) && (lastGain > EPS)))
        boolean devIsZero = true; for (int i=0; i<anzPar; i++) if (Math.abs((useMaximumLikelihood?llD[i]:lsD[i]))>EPS) devIsZero = false;
        if (devIsZero) {lastGain = 0; steps = MINRUNS;}
        while ((steps < MINRUNS) || ((steps<=MAXRUNS) && (lastGain/Math.min(Math.abs(lastDamping), 1.0) > EPS)))
        {
            boolean success = stepOptimization(EPS, useMaximumLikelihood);
            if (logStream != null) logStream.println(steps+" iteration, value = "+(useMaximumLikelihood?ll:ls)+", last damping = "+lastDamping+", last gain = "+lastGain);

            if (! success) 
            {
                setParameter(starting);

                if (useMaximumLikelihood) computeLogLikelihoodDerivatives(position); else computeLeastSquaresDerivatives(position);
                try {
                    Statik.invert((useMaximumLikelihood?llDD:lsDD), modelWork1, modelWork2, logresult);
                } catch (RuntimeException e) {logDetHessian = logresult[0]; if (logStream!=null) logStream.println("Hessian in Optimization became singular."); return null;}
                logDetHessian = logresult[0];
                Statik.multiply(modelWork1, (useMaximumLikelihood?llD:lsD), modelMove);
                double v;
                do {
                    for (int i=0; i<anzPar; i++) {
                        position[i] = starting[i] + rand.nextGaussian() * Math.abs(modelMove[i]) / 3.0;
                        if (isErrorParameter(i)) position[i] += Math.abs(position[i]+2*starting[i]);
                    }
                    v = (useMaximumLikelihood?getMinusTwoLogLikelihood(position):getLeastSquares(position));
                } while ((Double.isNaN(v)) || (Double.isInfinite(v)));
                if (useMaximumLikelihood) computeLogLikelihoodDerivatives(position); else computeLeastSquaresDerivatives(position);
                if (logStream!=null) logStream.println("Headed towards boundary minimum, I restarted with perturbed values and higher error.");
                warningFlag = warningFlagTypes.SUSPICIOUS;
            }
        }

        if (steps > MAXRUNS) 
        {
            if (logStream!=null) logStream.println("Warning: More than 50 runs needed, final steplength = "+lastSteplength); 
            warningFlag = warningFlagTypes.FAILED;
        } else {
            double value = (useMaximumLikelihood?ll:ls); 
            if ((value > startValue) || (Double.isNaN(value)) || (Double.isInfinite(value))) {
                if (logStream!=null) logStream.println("I was unable to improve the starting -2ll ("+startValue+"), converged on "+value+"."); 
                warningFlag = warningFlagTypes.FAILED;
            }  
            else if (warningFlag == warningFlagTypes.SUSPICIOUS) 
            {
                if (logStream!=null) logStream.println("It seemed I was able to solve out the problem, convergences reached on -2ll = "+
                        value+", better than starting ("+startValue+".)");
            }
        }
        if (useMaximumLikelihood && (Double.isInfinite(ll) || Double.isNaN(ll))) warningFlag = warningFlagTypes.FAILED;
//        if (useMaximumLikelihood && warningFlag == warningFlagTypes.OK && Statik.abs(llD) > 10*EPS) warningFlag = warningFlagTypes.SUSPICIOUS;
        
        lastEstimate = Statik.ensureSize(lastEstimate, anzPar); Statik.copy(position, lastEstimate);
        return getParameter();
    }
    
    /* ************************************************************************************************************************
    All Fit Indices in the following block are based on full data models. Submodels that allow missingness must override the 
    methods getSaturatedLL, getIndependentLL, getRestrictedDF(), and getIndependentDF().
    ************************************************************************************************************************ */
    
    /** Computes the degrees of freedom between the actual and a saturated model, saturated in covariance and means. */
    public int getRestrictedDF() {return anzVar+anzVar*(anzVar+1)/2 - anzPar;}
    
    /** Computes the degrees of freedom between the independent and the saturated model, both in covariance and means. */
    public int getIndependentDF() {return getRestrictedDF() + anzPar - 2*anzVar;}
    
    /** Computes the -2 log likelihood for the data set assuming the data covariance matrix and data mean as mu and sigma. */
    public double getSaturatedLL() {
        double erg = Statik.logDeterminantOfPositiveDefiniteMatrix(dataCov);
        erg += anzVar*(LNTWOPI+1);
        erg *= anzPer;
        return erg;
    }
    
    /** Computes the -2 log likelihood for the data set assuming the independent sigma as sigma. */
    public double getIndependentLL() 
    {
        if (dataCov == null) computeMoments(true);
        double erg = 0;
        for (int i=0; i<anzVar; i++) erg += Math.log(dataCov[i][i]);
        erg += anzVar*(LNTWOPI+1);
        erg *= anzPer;
        return erg;
    }
    
    /** Computes the KL difference between this and saturated model */
    public double getKulbackLeibler(double[] saturatedMean, double[][] saturatedCov) {return getKulbackLeibler(mu, sigma, saturatedMean, saturatedCov); }
    
    /** computes the KL difference to the saturated Model. */
    public double getIndependentKulbackLeibler() {
        dataMean = Statik.ensureSize(dataMean, anzVar);
        dataCov = Statik.ensureSize(dataCov, anzVar, anzVar);
        Statik.covarianceMatrixAndMeans(data, dataMean, dataCov, Model.MISSING);
        return Model.getIndependentKulbackLeibler(mu, sigma, dataMean, dataCov);
    }

    /** Computes the chisquare index from the independent to the saturated model, the independent ll minus the saturated ll. */
    public double getIndependentChisquare() {return getIndependentChisquare(getIndependentLL(), getSaturatedLL());}
    
    /** computes the chi-square, the ll minus the saturated ll, for this model **/ 
    public double getChisquare() {return getChisquare(getMinusTwoLogLikelihood(), getSaturatedLL());}
    
    /** Computes the RMSEA fit index for this model. */
    public double getRMSEA() {return getRMSEA(getMinusTwoLogLikelihood(), getSaturatedLL(), getRestrictedDF(), anzPer);}
    
    /** Computes the Tucker-Lewis-Index (also called Non-normed fit index, NNFI) in comparison to the independent model */
    public double getTLI() {return getTLI(getMinusTwoLogLikelihood(), getSaturatedLL(), getIndependentLL(), getRestrictedDF(), getIndependentDF());}
    
    /** Computes the Standardized Root Mean Squared Residual (SRMR). Note that this index does not include mean miss-specification. */
    public double getSRMR() {return getSRMR(sigma, dataCov);}
    
    /** Computes the AIC (Akaike fit index). */
    public double getAIC() {return getAIC(getMinusTwoLogLikelihood(), anzPar);}
    
    /** Computes the AICc (corrected Akaike fit index for small n). */
    public double getAICc() {return getAICc(getMinusTwoLogLikelihood(), anzPar, anzPer);}
    
    /** Computes the BIC (Bayesian Information Criterion). */
    public double getBIC() {return getBIC(getMinusTwoLogLikelihood(), anzPar, anzPer);}
    
    /** Computes the comparative RMSEA fit index for this model compared to an restricted model. */
    public double getDeltaRMSEA(Model restrictedModel) {
        return getDeltaRMSEA(restrictedModel.getMinusTwoLogLikelihood(), getMinusTwoLogLikelihood(), restrictedModel.anzPar, anzPar, anzPer);
    }
    
    /** Computes the CFI (comparative fit index) for this model compared to the independent model.  */
    public double getCFI() {return getCFI(getMinusTwoLogLikelihood(), getSaturatedLL(), getIndependentLL(), getRestrictedDF(), getIndependentDF());}
    
    /** Computes the CFI (comparative fit index) for this model compared to a restricted model.  */
    public double getCFI(Model restrictedModel) {
        return getCFI(getMinusTwoLogLikelihood(), getSaturatedLL(), restrictedModel.getMinusTwoLogLikelihood(), getRestrictedDF(), restrictedModel.getRestrictedDF());
    }
    
    /** Computes the delta CFI (difference of comparative fit indices) for this model compared to a restricted model. */
    public double getDeltaCFI(Model restrictedModel) {return getCFI() - restrictedModel.getCFI();}
    
    /** Computes the McDonald Centrality Index. */
    public double getMcDonaldsCentralityIndex() {return getMcDonaldCentralityIndex(getMinusTwoLogLikelihood(), getSaturatedLL(), getRestrictedDF(), anzPer);}
    
    /**
     * Returns the number of degrees of freedom in the observed data set. In general, this is the number of entries in the covariance matrix
     * and the mean vector, unless this number is larger than the total number of observed variables. For multiple groups, this is the sum
     * of all these values for all groups. 
     * 
     * @return
     */
    public int getObservedStatistics() {
        return Math.min(anzPer*anzVar, anzVar + anzVar*(anzVar+1)/2);
    }

    /** computes the chisquare index, difference between ll and saturatedLL. */
    public static double getChisquare(double ll, double saturatedLL) {return ll - saturatedLL;}

    /** computes the chisquare index from saturated to independent model, difference between independent ll and saturatedLL. */
    public static double getIndependentChisquare(double saturatedLL, double indepLL) {return indepLL - saturatedLL;}
    
    /** computes the KL difference to the saturated Model. */
    public static double getKulbackLeibler(double[] mu, double[][] sigma, double[] saturatedMean, double[][] saturatedCov) {
        return Statik.getKulbackLeiblerNormal(saturatedMean, saturatedCov, mu, sigma);
    }
    
    /** computes the KL difference to the independent Model. All non-diagonal entries in IndependentSigma are ignored. */
    public static double getIndependentKulbackLeibler(double[] mu, double[][] sigma, double[] independentMean, double[][] independentCov) {
        double erg = 0.0; 
        int anzVar = mu.length;
        erg -= Statik.logDeterminantOfPositiveDefiniteMatrix(sigma);
        for (int j=0; j<anzVar; j++) erg += sigma[j][j]/independentCov[j][j];
        erg -= anzVar;
        for (int j=0; j<anzVar; j++) erg += Math.log(independentCov[j][j]);
        for (int j=0; j<anzVar; j++) {
            double v = (independentMean[j]-mu[j]); 
            erg += v*v/independentCov[j][j];
        }
        return erg;
    }
    
    /** computes RMSEA lower and upper CI 
     * 
     * orientiert sich an AB's OpenMx Code:
     * http://openmx.psyc.virginia.edu/thread/1269
     * */
    public static double[] getRMSEACI(double ll, double saturatedLL, int df, int n) {
    	
    	final double ciUpper = 0.975;
    	final double ciLower = 0.025;
    	
    	
    	
    	double chi2 = getChisquare(ll, saturatedLL);
    			
    	double[] result = new double[2];
    	
    	DoubleUnivariateFunction flow = new DoubleUnivariateFunction() {
			
    		int df;
    		double chi2;
    		
    		public DoubleUnivariateFunction init(int df, double chi2) {
    			this.df=df;
    			this.chi2=chi2;
    			return(this);
    		}
    		
			@Override
			public double foo(double arg) {
				return Statik.chiSquareDistribution(df, arg, chi2) - ciLower;
			}
		}.init(df, chi2);
		
    	DoubleUnivariateFunction fup = new DoubleUnivariateFunction() {
			
    		int df;
    		double chi2;
    		
    		public DoubleUnivariateFunction init(int df, double chi2) {
    			this.df=df;
    			this.chi2=chi2;
    			return(this);
    		}
    		
			@Override
			public double foo(double arg) {
				return Statik.chiSquareDistribution(df, arg, chi2) - ciUpper;
			}
		}.init(df, chi2);
		
		
		final double EPS = 0.001;
    	double lambdaLower = Statik.uniRoot(flow, 0, ll, EPS, 50);
    	
    	double heur = Math.max(n, chi2*4);
    	double lambdaUpper = Statik.uniRoot(fup, 0, heur, EPS, 50);
    	
    	double denom = (n-1)*df;
    	
    	result[0] = Math.sqrt(lambdaLower/denom);
    	result[1] = Math.sqrt(lambdaUpper/denom);
    	
    	return result;
    }

    /** computes the RMSEA index 
     * AB: 30.11.2015 should we change (n-1) to (n) to match OpenMx output ?!*/
    public static double getRMSEA(double ll, double saturatedLL, int df, int n) {
        return (df==0?0.0:Math.sqrt(Math.max((getChisquare(ll, saturatedLL) - df),0) / (df*(n-1.0))));}
    
    /** computes the RMSEA based on the kl index */
    public static double getRMSEAKL(double kl, double nonmissRatio, int df, int n) {
        double insqrt = (kl*n*nonmissRatio - df) / (df*(n-1)*nonmissRatio);
//        return (insqrt<0?-Math.sqrt(-insqrt):Math.sqrt(insqrt));
        return (df==0?0.0:Math.sqrt(Math.max((kl*n*nonmissRatio - df),0) / (df*(n-1)*nonmissRatio)));
    }

    /** computes the RMSEA with df correction */
    public static double getRMSEADF(double ll, double saturatedLL, double nonmissRatio, int df, int n) {
        return (df==0?0.0:Math.sqrt(Math.max((getChisquare(ll, saturatedLL) - nonmissRatio*df),0) / (nonmissRatio*df*(n-1.0))));
    }

    /** computes the McDonald Centrality Index to the saturated model */
    public static double getMcDonaldCentralityIndex(double ll, double saturatedLL, int df, int n) {return Math.exp(-0.5*(ll-saturatedLL-df)/(n-1));}
    
    /** computes the Tucker-Lewis-index */
    public static double getTLI(double ll, double saturatedLL, double independentLL, int df, int independentDF) {
        double indepChi = getIndependentChisquare(saturatedLL, independentLL);
        return Math.min(
        		(indepChi/independentDF - getChisquare(ll, saturatedLL)/df) 
        		/ (indepChi/independentDF - 1.0) 
        		, 1.0);
    }
    
    /** computes the Squared Root Mean Error, covariance matrix only. */
    public static double getSRMR(double[][] sigma, double[][] saturatedCov) {
        double erg = 0;
        for (int i=0; i<sigma.length; i++) for (int j=i; j<sigma.length; j++) {
            double a = saturatedCov[i][j]/Math.sqrt(saturatedCov[i][i]*saturatedCov[j][j]) - sigma[i][j]/Math.sqrt(sigma[i][i]*sigma[j][j]);
            erg += a*a;
        }
        erg /= sigma.length*(sigma.length+1)/2;
        return Math.sqrt(erg);
    }
    
    /** computes the Akaike Information Criterion index*/
    public static double getAIC(double ll, int anzPar) {return ll + 2*anzPar;}

    /** computes the corrected AIC (for low n) */
    public static double getAICc(double ll, int anzPar, int n) {
        if (n-anzPar-1<=0) return Double.NaN; else return ll + 2*anzPar + 2*anzPar*(anzPar+1) / (n - anzPar -1);
    }
    
    /** computes the Bayesian Information Criterion */
    public static double getBIC(double ll, int anzPar, int n) {return ll + Math.log(n)*anzPar;}

    /** AB: computes the Bayesian Information Criterion, sample size adjusted (see Mplus or OpenMx) */
    public static double getBICadjusted(double ll, int anzPar, int n) {return (getBIC(ll, anzPar, n)+Math.log((n+2)/24));}

    
    /** computes the RMSEA to a restricted model. */
    public static double getDeltaRMSEA(double restrictedLL, double ll, int restrictedAnzPar, int anzPar, int n) {
        return getRMSEA(restrictedLL, ll, anzPar-restrictedAnzPar, n);
    }
    
    /** computes the Comparative Fit Index to a restricted model; the "default" CFI is to the independent model */
    public static double getCFI(double ll, double saturatedLL, double independentLL, int df, int independentDF)
    {
        double chi = ll - saturatedLL;
        double indepChi = independentLL - saturatedLL;
        double denom = Math.max(chi-df, indepChi-independentDF);
        return 1.0 - ((chi-df) / denom);
    }

    /** computes the difference of CFI indices of two models, typically one nested in the other */
    public static double getDeltaCFI(double ll, double saturatedLL, double independentLL, double restrictedLL, int df, int independentDF, int restrictedDF) {
        return getCFI(ll, saturatedLL, independentLL, df, independentDF) -  getCFI(restrictedLL, saturatedLL, independentLL, restrictedDF, independentDF);
    }
    
    
    /**
     * Computes the derivative of ML with respect to the path coefficient lambda. The actual position is in lambda.
     *   
     * @param baseSigma
     * @param diffSigma
     * @param baseMu
     * @param diffMu
     */
    protected void computePathDerivatives(double lambda, double[][] baseSigma, double[][] diffSigma, 
                                          double[] baseMu, double[] diffMu, boolean computePathDirection)
    {
        if ((modelSigmaWorkPD==null) || (modelSigmaWorkPD.length!=anzVar) || (modelSigmaWorkPD[0].length!=anzVar)) modelSigmaWorkPD = new double[anzVar][anzVar];
        if ((modelMuWork==null) || (modelMuWork.length!=anzVar)) modelMuWork = new double[anzVar];
        if ((modelSigmaWork2PD==null) || (modelSigmaWork2PD.length!=anzVar) || (modelSigmaWork2PD[0].length!=anzVar)) modelSigmaWork2PD = new double[anzVar][anzVar];
        if ((llPathDD==null) || (llPathDD.length!=anzPar)) llPathDD = new double[anzPar];
        
        llPathD = 0;
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) llPathD += diffSigma[i][j]*sigInv[i][j];
        llPathD *= anzPer-1;
        double v = 0;
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) v += diffMu[i]*(baseMu[j]+lambda*diffMu[j])*sigInv[i][j];
        llPathD += 2*v*anzPer;
        
        for (int p=0; p<anzPar; p++)
        {
            computeSigmaInvDev(p, modelSigmaWorkPD);
            Statik.multiply(modelSigmaWorkPD, sigInv, modelSigmaWork2PD);
            v = 0;
            for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) v += diffSigma[i][j]*modelSigmaWork2PD[i][j];
            llPathDD[p] = -v*(anzPer-1);
            v = 0;
            for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) v += diffMu[i]*modelSigmaWork2PD[i][j]*(baseMu[j]+lambda*diffMu[j]);
            llPathDD[p] -= 2*v*anzPer;
            v = 0;
            computeMatrixTimesMuDev(p, sigInv, modelMuWork);
            for (int i=0; i<anzVar; i++) v += modelMuWork[i]*diffMu[i];
            llPathDD[p] -= 2*anzPer*v;
            
        }
        if (computePathDirection)
        {
            if ((pathDirection==null) || (pathDirection.length!=anzPar)) pathDirection = new double[anzPar];
            if ((modelWork1==null) || (modelWork1.length!=anzPar) || (modelWork1[0].length!=anzPar)) modelWork1 = new double[anzPar][anzPar];
            if ((modelWork2==null) || (modelWork2.length!=anzPar) || (modelWork2[0].length!=anzPar)) modelWork2 = new double[anzPar][anzPar];
            
            Statik.invert(llDD, modelWork1, modelWork2, logresult); logDetHessian = logresult[0];
            Statik.multiply(modelWork1, llPathDD, pathDirection);
            for (int i=0; i<anzPar; i++) pathDirection[i] = -pathDirection[i];
        }
    }

    public double[] estimateMLByPathtracking(double[] startPos, double EPS)
    {
        final int MAXNEWTONSTEPS = 1000;
//        final double INITNOISE = 0.01;
        final double INITNOISE = 0.001;
        final int ANZCAMPS = 2;
        final int ANZSTEPS = 4;
        final double BONUSSTEPS = 0.01;
        final int MAXSTEPS = 7;
        final int ABSOLUTEMAXSTEPS = 50;
        final int TOPCAMPMAXSTEPS = 5;
        // Results that are better than ACCLEVEL times the difference between basecamp and the linear
        // interpolated ll - outcome are accepted as successfully converged. A higher acceptance factor leads to more errors on the path
        // tracking, but less intermediate steps. 
        final double ACCEPTANCEFACTOR = 1.5;        
        final int LASTTRIES = 50;
                                                    
        if (startPos == null) startPos = getArbitraryStartingValues();

        if ((modelSigmaWorkPD==null) || (modelSigmaWorkPD.length!=anzVar) || (modelSigmaWorkPD[0].length!=anzVar)) modelSigmaWorkPD = new double[anzVar][anzVar];
        if ((modelSigmaWork2PD==null) || (modelSigmaWork2PD.length!=anzVar) || (modelSigmaWork2PD[0].length!=anzVar)) modelSigmaWork2PD = new double[anzVar][anzVar];

        double[][] diffSigma = new double[anzVar][anzVar];
        double[] diffMu = new double[anzVar];
        
        this.setParameter(startPos); getMinusTwoLogLikelihood();
        Model baseCamp = this.copy();
        baseCamp.setParameter(startPos);
        for (int i=0; i<anzPar; i++) baseCamp.position[i] += rand.nextGaussian()*INITNOISE;     
        baseCamp.setParameter(baseCamp.position);
        baseCamp.pathLambda = 0.0;
        baseCamp.computeLogLikelihoodDerivatives(null, true);
        double startLS = baseCamp.getLeastSquares();
        for (int i=0; i<anzVar; i++) baseCamp.dataMean[i] = baseCamp.mu[i];
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) baseCamp.dataCov[i][j] = baseCamp.sigma[i][j];
        for (int i=0; i<anzVar; i++) diffMu[i] = dataMean[i] - baseCamp.dataMean[i];
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) diffSigma[i][j] = dataCov[i][j] - baseCamp.dataCov[i][j];
        baseCamp.computeMomentsFromDataCovarianceAndMean();
        baseCamp.getMinusTwoLogLikelihood(null, false);
        baseCamp.computePathDerivatives(0.0, baseCamp.dataCov, diffSigma, baseCamp.dataMean, diffMu, true);
        
        Model[] spawn = new Model[ANZCAMPS];
        double[] acceptanceLevel = new double[ANZCAMPS];
        
        int anzSpawns = 0;

        this.setParameter(baseCamp.position);
        this.initEstimation(null, true);
        double startValue = ll;
        if (Double.isNaN(ll)) this.warningFlag = warningFlagTypes.FAILED;
        double epssqr = EPS*EPS;
        
        int newtonSteps = 0, spawningRounds = 0;
        if (logStream!=null) logStream.println("(lambda, steps, ll, convergenceIndex, acceptanceLevel, alive)\r\nFunction Distance = "+startLS);
        while ((newtonSteps<=MAXNEWTONSTEPS) && ((anzSpawns==0) || (spawn[0].pathLambda<1.0) || 
                                                 (spawn[0].lastSteplength > EPS) || (spawn[0].warningFlag != warningFlagTypes.OK)))
        {
            int newCamp = -1;
            int localMaxSteps = Math.min(MAXSTEPS + (int)Math.round(Math.pow(2,spawningRounds) * BONUSSTEPS),ABSOLUTEMAXSTEPS);
            int localAnzSteps = Math.max(ANZSTEPS, localMaxSteps/ANZCAMPS);
            int runs = 0;
            while ((newCamp==-1) && (runs < localAnzSteps) && (newtonSteps <= MAXNEWTONSTEPS))
            {
                for (int spnr = 0; (newCamp==-1 && spnr < anzSpawns); spnr++)
                    if ((spawn[spnr].warningFlag == warningFlagTypes.OK) && (spawn[spnr].lastSteplength > epssqr) && 
                       ((spawn[spnr].pathLambda<1.0) || (spawn[spnr].steps < TOPCAMPMAXSTEPS)) && ((spawn[spnr].steps < localMaxSteps)))
                    {
                        if (!spawn[spnr].moveWithOptimalDamping(epssqr, true)) spawn[spnr].warningFlag = warningFlagTypes.FAILED;
                        else if ((spawn[spnr].lastSteplength < epssqr) && (spawn[spnr].ll<acceptanceLevel[spnr])) newCamp= spnr;
                        newtonSteps++;
                    }
                for (int i=0; i<anzSpawns; i++)
                    if (logStream!=null) logStream.print("("+Statik.doubleNStellen(spawn[i].pathLambda,2)+","+
                                         spawn[i].steps+","+
                                         Statik.doubleNStellen(spawn[i].ll,2)+","+
                                         Statik.doubleNStellen(spawn[i].lastSteplength,2)+","+
                                         Statik.doubleNStellen(acceptanceLevel[i],2)+","+
                            (spawn[i].warningFlag != warningFlagTypes.OK?"DEAD":(spawn[i].lastSteplength>epssqr?"RUNS":"DONE"))+") - ");
                if (logStream!=null) logStream.println();
                runs++;
            }

            if ((anzSpawns > 0) && (spawn[0].pathLambda==1.0) && (spawn[0].ll < this.ll)) {this.setParameter(spawn[0].position); this.ll = spawn[0].ll;}
            if (newCamp == -1)
            {
                // new spawn
                spawningRounds++;
                if (anzSpawns == ANZCAMPS) {
                    anzSpawns--; 
                    Model t = spawn[0]; for (int i=0; i<anzSpawns; i++) {spawn[i] = spawn[i+1]; acceptanceLevel[i] = acceptanceLevel[i+1];}
                    spawn[anzSpawns] = t;                    
                }

                if (spawn[anzSpawns]==null) spawn[anzSpawns] = this.copy();
                spawn[anzSpawns].pathLambda = (anzSpawns==0?1.0:spawn[anzSpawns-1].pathLambda/2.0);
                for (int i=0; i<anzVar; i++) spawn[anzSpawns].dataMean[i] = baseCamp.dataMean[i] + spawn[anzSpawns].pathLambda*diffMu[i];
                for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) 
                    spawn[anzSpawns].dataCov[i][j] = baseCamp.dataCov[i][j] + spawn[anzSpawns].pathLambda*diffSigma[i][j];
                spawn[anzSpawns].computeMomentsFromDataCovarianceAndMean();
                
                spawn[anzSpawns].initEstimation(baseCamp.position, true);
                for (int i=0; i<anzPar; i++) spawn[anzSpawns].modelMove[i] = spawn[anzSpawns].pathLambda * baseCamp.pathDirection[i];
                spawn[anzSpawns].warningFlag = warningFlagTypes.OK;
                spawn[anzSpawns].moveWithOptimalDampingPregivenStep(epssqr, true, false);  // Optimaler Step in Richtung des Pfades
                newtonSteps++;
                
                acceptanceLevel[anzSpawns] = baseCamp.ll + spawn[anzSpawns].pathLambda*baseCamp.llPathD *
                                               (baseCamp.llPathD<0?1.0/ACCEPTANCEFACTOR:ACCEPTANCEFACTOR);
                anzSpawns++;
            } else {
                // new basecamp
                try {
                    // These two lines correct the result to the parameter result to reinsure global minimum, on the risk of not
                    // moving forward anymore.
//                    for (int i=0; i<anzVar; i++) spawn[newCamp].dataMean[i] = spawn[newCamp].mu[i];
//                    for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) spawn[newCamp].dataCov[i][j] = spawn[newCamp].sigma[i][j];
                    
                    for (int i=0; i<anzVar; i++) diffMu[i] = dataMean[i] - spawn[newCamp].dataMean[i];
                    for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) diffSigma[i][j] = dataCov[i][j] - spawn[newCamp].dataCov[i][j];
                    spawn[newCamp].computePathDerivatives(0.0, spawn[newCamp].dataCov, diffSigma, spawn[newCamp].dataMean, diffMu, true);
                    spawningRounds = 0;
                    Model t = baseCamp; baseCamp = spawn[newCamp]; spawn[newCamp] = t;
                    anzSpawns = 0;
                    double ls = 0;
                    for (int i=0; i<anzVar; i++) ls += (dataMean[i] - baseCamp.dataMean[i])*(dataMean[i] - baseCamp.dataMean[i]);
                    for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) 
                        ls += (dataCov[i][j] - baseCamp.dataCov[i][j])*(dataCov[i][j] - baseCamp.dataCov[i][j]);
                    if (logStream!=null) logStream.println("Establishing new basecamp at lambda = "+baseCamp.pathLambda+", function distance = "+ls);
                    baseCamp.pathLambda = 0;
//                    if (logStream != null) logStream.println("New Basecamp Variance of Slope = "+((CholeskyModel)baseCamp).facCov[1][1]);
                    if (logStream != null) logStream.println("New Basecamp Parameters = "+Statik.matrixToString(baseCamp.position));
                } catch (Exception e)
                {
                    // new camp was not fit for basecamp, possilby because of singular Hessian. 
                    spawn[newCamp].warningFlag = warningFlagTypes.FAILED;
                }
            }
        }

        this.initEstimation(null, true);
        while ((this.lastSteplength > EPS) && (this.steps<LASTTRIES)) this.moveWithOptimalDamping(EPS, true);
        if (newtonSteps > MAXNEWTONSTEPS) 
        {
            
            if (logStream!=null) logStream.println("It took me too many iterations in the pathtracking, the last steplength for "+
                    "the original model was "+lastSteplength); 
            warningFlag = warningFlagTypes.FAILED;
        } else {
            double value = ll; 
            if ((value > startValue) || (Double.isNaN(value)) || (Double.isInfinite(value))) {
                if (logStream!=null) logStream.println("I was unable to improve the starting -2ll ("+startValue+"), converged on "+value+"."); 
                warningFlag = warningFlagTypes.FAILED;
            }  
            else if (warningFlag == warningFlagTypes.SUSPICIOUS) 
            {
                if (logStream!=null) logStream.println("It seemed I was able to solve out the problem, convergences reached on -2ll = "+
                        value+", better than starting ("+startValue+".)");
            }
        }
        if (logStream!=null) logStream.println(newtonSteps+" steps needed overall.");
        if ((lastEstimate == null) || (lastEstimate.length != anzPar)) lastEstimate = new double[anzPar];
        double[] erg = new double[anzPar];
        for (int i=0; i<anzPar; i++) lastEstimate[i] = erg[i] = position[i];
        steps = newtonSteps;
        return erg;
    }

    
    public boolean testDerivatives(PrintStream log, double[] pos, double eps)
    {
        boolean erg = true;
        setParameter(pos);
        pos = getParameter();
        if (data==null) createData(10000);
        computeLogLikelihoodDerivatives(pos, true);
        double v = ll;
        double[] vd = Statik.copy(llD), numD = new double[anzPar];
        double[][] vdd = Statik.copy(llDD), numDD = new double[anzPar][anzPar];
        for (int i=0; i<anzPar; i++)
        {
            pos[i] += eps;
            setParameter(pos);
            computeLogLikelihoodDerivatives(pos, true);
            pos[i] -= eps;
            numD[i] = (ll - v)/eps;
            erg = erg && (Math.abs(numD[i] - vd[i]) < 10*eps);
            for (int j=0; j<anzPar; j++) {numDD[i][j] = (llD[j]-vd[j])/eps; erg = erg && (Math.abs(numDD[i][j] - vdd[i][j]) < 10*eps);}
        }
        if (log != null)
        {
            log.println("Numerical  Derivative = "+Statik.matrixToString(numD));
            log.println("Symbolical Derivative = "+Statik.matrixToString(vd));
            log.println("Numerical  2nd Derivative = \r\n"+Statik.matrixToString(numDD));
            log.println("Symbolical 2nd Derivative = \r\n"+Statik.matrixToString(vdd));
            log.println("Test "+(erg?"ok":"failed"));
        }
        return erg;
    }

    /**
     * 
     * @param nested
     * @param restrictionParameterNumber    a list of parameter numbers of the restricted model in this
     * @param alpha
     * @param anzPer
     * @return
     */
    public double sarrisSatorraPower(Model nested, int[] restrictionParameterNumber, double alpha, int anzPer) {
        this.anzPer = nested.anzPer = anzPer;
        this.evaluateMuAndSigma();
        Statik.copy(this.sigma, nested.dataCov);
        Statik.copy(this.mu, nested.dataMean);
        nested.computeMomentsFromDataCovarianceAndMean();
        double[] start = new double[nested.anzPar];
        double[] thisPar = getParameter();
        for (int i=0; i<nested.anzPar; i++) start[i] = thisPar[restrictionParameterNumber[i]];
        nested.estimateML(start);
        double noncentrality = nested.ll - this.getMinusTwoLogLikelihood();
        int df = this.anzPar - nested.anzPar;

        double power = Statik.chiSquareDistribution(df, noncentrality, Statik.inverseChiSquareDistribution(df, 0.0, alpha));
        return power;
    }

    /**
     * Returns the derivatives of the gradient wrt one data vector. If constantPartOnly is true, the part of the derivative that involves data values
     * are ignored. Otherwise, datamean is used for this part. 
     *
     * @param constantPartOnly
     * @return matrix with anzPar rows and anzVar columns.
     */
    public double[][] getGradientDerivativeWRTData() {return getGradientDerivativeWRTData(false, null, dataMean);}
    public double[][] getGradientDerivativeWRTData(double[] dataMean) {return getGradientDerivativeWRTData(false, null, dataMean);}
    public double[][] getGradientDerivativeWRTData(boolean constantPartOnly) {return getGradientDerivativeWRTData(constantPartOnly, null, dataMean);}
    public double[][] getGradientDerivativeWRTData(boolean constantPartOnly, double[][] erg, double[] dataMean) {
        if ((sigInv==null) || (sigInv.length!=anzVar) || (sigInv[0].length!=anzVar)) sigInv = new double[anzVar][anzVar];
        if ((modelSigmaWork==null) || (modelSigmaWork.length!=anzVar) || (modelSigmaWork[0].length!=anzVar)) modelSigmaWork = new double[anzVar][anzVar];
        if ((modelSigmaWork2==null) || (modelSigmaWork2.length!=anzVar) || (modelSigmaWork2[0].length!=anzVar)) modelSigmaWork2 = new double[anzVar][anzVar];
        if ((modelMuWork==null) || (modelMuWork.length!=anzVar)) modelMuWork = new double[anzVar];
        if ((erg==null) || (erg.length!=anzPar) || (erg[0].length!=anzVar)) erg = new double[anzPar][anzVar];
        
        Statik.invert(sigma, sigInv, modelSigmaWork);
        for (int i=0; i<anzPar; i++) {
            this.computeMatrixTimesMuDev(i, sigInv, modelMuWork);
            this.computeMatrixTimesSigmaDev(i, sigInv, modelSigmaWork2);
            Statik.multiply(modelSigmaWork2, sigInv, modelSigmaWork);

            for (int j=0; j<anzVar; j++) {
                erg[i][j] = -2*modelMuWork[j];
                for (int k=0; k<anzVar; k++) erg[i][j] += 2*modelSigmaWork[j][k]*mu[k];
                if (!constantPartOnly)
                    for (int k=0; k<anzVar; k++) erg[i][j] -= 2*modelSigmaWork[j][k]*dataMean[k];
            }
        }
        return erg;
    }

    
    
    /**
     * Computes the distribution of parameters given a data covariance matrix and the dataMean assuming that sigma and mu from the model
     * are at the minimum for this data distribution.
     * 
     * The method is awfully inefficient at the moment and needs to be polished! At the moment, it doesn't seem to be working, either.
     * 
     * @param dataCov
     * @param dataMean
     * @param erg
     * @return
     */
    public double[][] getParameterDistributionCovariance_directVersion(double[][] dataCov, double[] dataMean, double[][] erg) {
        if ((modelVarParWork==null) || (modelVarParWork.length!=anzPar) || (modelVarParWork[0].length!=anzVar)) modelVarParWork = new double[anzPar][anzVar];
        if ((modelPrecisionDev==null) || (modelPrecisionDev.length!=anzPar) || (modelPrecisionDev[0].length!=anzVar) || 
                (modelPrecisionDev[0][0].length!=anzVar)) modelPrecisionDev = new double[anzPar][anzVar][anzVar];
        if ((sigInv==null) || (sigInv.length!=anzVar) || (sigInv[0].length!=anzVar)) sigInv = new double[anzVar][anzVar];
        if ((modelSigmaWork==null) || (modelSigmaWork.length!=anzVar) || (modelSigmaWork[0].length!=anzVar)) modelSigmaWork = new double[anzVar][anzVar];
        if ((modelSigmaWork2==null) || (modelSigmaWork2.length!=anzVar) || (modelSigmaWork2[0].length!=anzVar)) modelSigmaWork2 = new double[anzVar][anzVar];
        if ((modelMuWork==null) || (modelMuWork.length!=anzVar)) modelMuWork = new double[anzVar];
        if ((modelMuWork2==null) || (modelMuWork2.length!=anzVar)) modelMuWork2 = new double[anzVar];
        if ((modelWork1==null) || (modelWork1.length!=anzPar) || (modelWork1[0].length!=anzPar)) modelWork1 = new double[anzPar][anzPar];
        if ((modelWork2==null) || (modelWork2.length!=anzPar) || (modelWork2[0].length!=anzPar)) modelWork2 = new double[anzPar][anzPar];
        if ((modelWork3==null) || (modelWork3.length!=anzPar) || (modelWork3[0].length!=anzPar)) modelWork3 = new double[anzPar][anzPar];
        if ((erg==null) || (erg.length!=anzPar) || (erg[0].length!=anzPar)) erg = new double[anzPar][anzPar];

        // corrected Model Means
        for (int i=0; i<anzVar; i++) modelMuWork[i] = mu[i] - dataMean[i];
        
        // computes all derivatives of the precision matrix
        Statik.invert(sigma, sigInv, modelSigmaWork);
        for (int i=0; i<anzPar; i++) {
            this.computeMatrixTimesSigmaDev(i, sigInv, modelSigmaWork2);
            Statik.multiply(modelSigmaWork2, sigInv, modelPrecisionDev[i]);
        }

        // computes the constant part of the derivative of the gradient wrt. the data
        for (int i=0; i<anzPar; i++) {
            this.computeMatrixTimesMuDev(i, sigInv, modelMuWork);

            for (int j=0; j<anzVar; j++) {
                modelVarParWork[i][j] = -2*modelMuWork[j];
                for (int k=0; k<anzVar; k++) modelVarParWork[i][j] += 2*modelPrecisionDev[i][j][k]*mu[k];
            }
        }
        
        // computes the result without the Hessian
        for (int a=0; a<anzPar; a++)
            for (int b=0; b<anzPar; b++) {
                modelWork1[a][b] = 0;
                
                // 2nd moment part
                for (int i=0; i<anzVar; i++) 
                    for (int j=0; j<anzVar; j++) 
                        modelWork1[a][b] += modelVarParWork[a][i]*dataCov[i][j]*modelVarParWork[b][j];
                
                // 4th moment part
                for (int i=0; i<anzVar; i++) 
                    for (int j=0; j<anzVar; j++)
                        for (int k=0; k<anzVar; k++) 
                            for (int l=0; l<anzVar; l++) 
                                modelWork1[a][b] += 4*modelPrecisionDev[a][i][j]*modelPrecisionDev[b][k][l]*
                                            (dataCov[i][j]*dataCov[k][l] + dataCov[i][k]*dataCov[j][l] + dataCov[i][l]*dataCov[j][k]);
            }

        Statik.invert(llDD, modelWork2, modelWork3, logresult); logDetHessian = logresult[0];
        Statik.multiply(modelWork2, modelWork1, modelWork3);
        Statik.multiply(modelWork3, modelWork2, erg);
        
        return erg;
        
    }
    
    /** computes 1/2 times the Fisher Information Matrix in its general form, i.e. as 0.25*E(g_i * g_j). Coincides with
     * the Hessian if dataCov and dataMean are sigma and mu from the model. 
     * 
     * @param dataCov
     * @param dataMean
     * @param erg
     */
    public double[][] computeFisherMatrix(double[][] dataCov, double[] dataMean) {return computeFisherMatrix(dataCov, dataMean, null);}
    public double[][] computeFisherMatrix(double[][] dataCov, double[] dataMean, double[][] erg) {
        if ((modelVarParWork==null) || (modelVarParWork.length!=anzPar) || (modelVarParWork[0].length!=anzVar)) modelVarParWork = new double[anzPar][anzVar];
        if ((sigInv==null) || (sigInv.length!=anzVar) || (sigInv[0].length!=anzVar)) sigInv = new double[anzVar][anzVar];
        if ((modelSigmaWork==null) || (modelSigmaWork.length!=anzVar) || (modelSigmaWork[0].length!=anzVar)) modelSigmaWork = new double[anzVar][anzVar];
        if ((modelSigmaWork1==null) || (modelSigmaWork1.length!=anzVar) || (modelSigmaWork1[0].length!=anzVar)) modelSigmaWork1 = new double[anzVar][anzVar];
        if ((modelSigmaWork2==null) || (modelSigmaWork2.length!=anzVar) || (modelSigmaWork2[0].length!=anzVar)) modelSigmaWork2 = new double[anzVar][anzVar];
        if ((modelMuWork==null) || (modelMuWork.length!=anzVar)) modelMuWork = new double[anzVar];
        if ((modelMuWork1==null) || (modelMuWork1.length!=anzVar)) modelMuWork1 = new double[anzVar];
        if ((modelMuWork2==null) || (modelMuWork2.length!=anzVar)) modelMuWork2 = new double[anzVar];
        if ((erg==null) || (erg.length!=anzPar) || (erg[0].length!=anzPar)) erg = new double[anzPar][anzPar];

        // corrected Model Means
        for (int i=0; i<anzVar; i++) modelMuWork[i] = mu[i] - dataMean[i];
        Statik.invert(sigma, sigInv, modelSigmaWork);

        for (int a = 0; a<anzPar; a++) {
            computeMatrixTimesSigmaDev(a, sigInv, modelSigmaWork);
            double t1 = 0; for (int i=0; i<anzVar; i++) t1 += modelSigmaWork[i][i];
            Statik.multiply(modelSigmaWork, sigInv, modelSigmaWork1);
            Statik.negate(modelSigmaWork1);
            computeMatrixTimesMuDev(a, null, modelMuWork1);
            for (int b=0; b<anzPar; b++) {
//                System.out.println("Starting with Fisher position ("+a+","+b+")");
                computeMatrixTimesSigmaDev(b, sigInv, modelSigmaWork);
                double t2 = 0; for (int i=0; i<anzVar; i++) t2 += modelSigmaWork[i][i];
                Statik.multiply(modelSigmaWork, sigInv, modelSigmaWork2);
                Statik.negate(modelSigmaWork2);
                computeMatrixTimesMuDev(b, null, modelMuWork2);
                
                erg[a][b] = t1*t2;
                for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) 
                    erg[a][b] += t1*(modelMuWork[i]*modelMuWork[j]*modelSigmaWork2[i][j] + modelSigmaWork2[i][j]*dataCov[i][j] + 
                                     2*modelMuWork2[i]*modelMuWork[j]*sigInv[i][j])
                               + t2*(modelMuWork[i]*modelMuWork[j]*modelSigmaWork1[i][j] + modelSigmaWork1[i][j]*dataCov[i][j] + 
                                     2*modelMuWork1[i]*modelMuWork[j]*sigInv[i][j]);
                for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) for (int l=0; l<anzVar; l++) {
                    erg[a][b] += modelSigmaWork1[i][j]*modelSigmaWork2[k][l]*(modelMuWork[i]*modelMuWork[j]*modelMuWork[k]*modelMuWork[l]
                                   +modelMuWork[i]*modelMuWork[j]*dataCov[k][l]+modelMuWork[i]*modelMuWork[k]*dataCov[j][l]+modelMuWork[i]*modelMuWork[l]*dataCov[j][k]
                                   +modelMuWork[j]*modelMuWork[k]*dataCov[i][l]+modelMuWork[j]*modelMuWork[l]*dataCov[i][k]+modelMuWork[k]*modelMuWork[l]*dataCov[i][j]
                                   +dataCov[i][j]*dataCov[k][l]+dataCov[i][k]*dataCov[j][l]+dataCov[i][l]*dataCov[j][k])
                               - 2*modelSigmaWork1[i][j]*sigInv[k][l]*(modelMuWork[i]*modelMuWork[j]*modelMuWork2[k]*modelMuWork[l]
                                   +modelMuWork[i]*modelMuWork2[k]*dataCov[j][l]+modelMuWork[j]*modelMuWork2[k]*dataCov[i][l]+modelMuWork2[k]*modelMuWork[l]*dataCov[i][j])
                               - 2*modelSigmaWork2[i][j]*sigInv[k][l]*(modelMuWork[i]*modelMuWork[j]*modelMuWork1[k]*modelMuWork[l]
                                   +modelMuWork[i]*modelMuWork1[k]*dataCov[j][l]+modelMuWork[j]*modelMuWork1[k]*dataCov[i][l]+modelMuWork1[k]*modelMuWork[l]*dataCov[i][j])
                               + 4*sigInv[i][j]*sigInv[k][l]*(modelMuWork1[i]*modelMuWork[j]*modelMuWork2[k]*modelMuWork[l]+modelMuWork1[i]*modelMuWork2[k]*dataCov[j][l]);
                }
                erg[a][b] /= 2;         // I haven't got a clue why this division by two is in, but it seems to be right when comparing Hessian and Fisher.
            }
        }
        return erg;
    }
    
    /** computes the Fisher Information Matrix in its general form, i.e. as E(g_i * g_j), for a model that assumes independent and identically 
     * distributed data rows, while the data is really time delayed embedded. dataCov and dataMean are given for the complete time series, but
     * without repetitions. 
     * 
     * @param dataCov
     * @param dataMean
     * @param erg
     */
    public double[][] computeFisherMatrixOfTimeDelayedEmbedding(double[][] dataCov, double[] dataMean, double[][] erg) {
        return computeFisherMatrixOfTimeDelayedEmbedding(dataCov, dataMean, erg, 1);}
    public double[][] computeFisherMatrixOfTimeDelayedEmbedding(double[][] dataCov, double[] dataMean, double[][] erg, int stepWidth) {
        int sw = stepWidth;
        int anzT = dataCov.length - anzVar*stepWidth + stepWidth;
        
        if ((modelVarParWork==null) || (modelVarParWork.length!=anzPar) || (modelVarParWork[0].length!=anzVar)) modelVarParWork = new double[anzPar][anzVar];
        if ((sigInv==null) || (sigInv.length!=anzVar) || (sigInv[0].length!=anzVar)) sigInv = new double[anzVar][anzVar];
        if ((modelSigmaWork==null) || (modelSigmaWork.length!=anzVar) || (modelSigmaWork[0].length!=anzVar)) modelSigmaWork = new double[anzVar][anzVar];
        if ((modelSigmaWork1==null) || (modelSigmaWork1.length!=anzVar) || (modelSigmaWork1[0].length!=anzVar)) modelSigmaWork1 = new double[anzVar][anzVar];
        if ((modelSigmaWork2==null) || (modelSigmaWork2.length!=anzVar) || (modelSigmaWork2[0].length!=anzVar)) modelSigmaWork2 = new double[anzVar][anzVar];
        if ((modelMuWork==null) || (modelMuWork.length!=anzVar)) modelMuWork = new double[anzVar];
        if ((modelMuWork1==null) || (modelMuWork1.length!=anzVar)) modelMuWork1 = new double[anzVar];
        if ((modelMuWork2==null) || (modelMuWork2.length!=anzVar)) modelMuWork2 = new double[anzVar];
        if ((modelMuWork3==null) || (modelMuWork3.length!=anzVar)) modelMuWork3 = new double[anzVar];
        if ((erg==null) || (erg.length!=anzPar) || (erg[0].length!=anzPar)) erg = new double[anzPar][anzPar];

        // corrected Model Means
//        for (int i=0; i<anzVar; i++) modelMuWork[i] = mu[i] - dataMean[i];   
        Statik.invert(sigma, sigInv, modelSigmaWork);

        for (int a = 0; a<anzPar; a++) {
            computeMatrixTimesSigmaDev(a, sigInv, modelSigmaWork);
            double t1 = 0; for (int i=0; i<anzVar; i++) t1 += modelSigmaWork[i][i];
            Statik.multiply(modelSigmaWork, sigInv, modelSigmaWork1);
            Statik.negate(modelSigmaWork1);
            computeMatrixTimesMuDev(a, null, modelMuWork1);
            t1 *= anzT;
            for (int b=0; b<anzPar; b++) {
                computeMatrixTimesSigmaDev(b, sigInv, modelSigmaWork);
                double t2 = 0; for (int i=0; i<anzVar; i++) t2 += modelSigmaWork[i][i];
                Statik.multiply(modelSigmaWork, sigInv, modelSigmaWork2);
                Statik.negate(modelSigmaWork2);
                computeMatrixTimesMuDev(b, null, modelMuWork2);
                t2 *= anzT;
                
                erg[a][b] = t1*t2;
                for (int tt1=0; tt1<anzT; tt1++) {for (int i=0; i<anzVar; i++) modelMuWork[i] = mu[i] - dataMean[i*sw+tt1];
                    for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) 
                        erg[a][b] += t1*(modelMuWork[i]*modelMuWork[j]*modelSigmaWork2[i][j] + modelSigmaWork2[i][j]*dataCov[i*sw+tt1][j*sw+tt1] + 
                                         2*modelMuWork2[i]*modelMuWork[j]*sigInv[i][j])
                                   + t2*(modelMuWork[i]*modelMuWork[j]*modelSigmaWork1[i][j] + modelSigmaWork1[i][j]*dataCov[i*sw+tt1][j*sw+tt1] + 
                                         2*modelMuWork1[i]*modelMuWork[j]*sigInv[i][j]);
                    for (int tt2=0; tt2<anzT; tt2++) {for (int i=0; i<anzVar; i++) modelMuWork3[i] = mu[i] - dataMean[i*sw+tt2];
                        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) for (int l=0; l<anzVar; l++) {
                            erg[a][b] += modelSigmaWork1[i][j]*modelSigmaWork2[k][l]*(modelMuWork[i]*modelMuWork[j]*modelMuWork[k]*modelMuWork[l]
                                           +modelMuWork[i]*modelMuWork[j]*dataCov[k*sw+tt2][l*sw+tt2]
                                           +modelMuWork[i]*modelMuWork[k]*dataCov[j*sw+tt1][l*sw+tt2]
                                           +modelMuWork[i]*modelMuWork[l]*dataCov[j*sw+tt1][k*sw+tt2]
                                           +modelMuWork[j]*modelMuWork[k]*dataCov[i*sw+tt1][l*sw+tt2]
                                           +modelMuWork[j]*modelMuWork[l]*dataCov[i*sw+tt1][k*sw+tt2]
                                           +modelMuWork[k]*modelMuWork[l]*dataCov[i*sw+tt1][j*sw+tt1]
                                           +dataCov[i*sw+tt1][j*sw+tt1]*dataCov[k*sw+tt2][l*sw+tt2]+dataCov[i*sw+tt1][k*sw+tt2]*dataCov[j*sw+tt1][l*sw+tt2]
                                           +dataCov[i*sw+tt1][l*sw+tt2]*dataCov[j*sw+tt1][k*sw+tt2])
                                       - 2*modelSigmaWork1[i][j]*sigInv[k][l]*(modelMuWork[i]*modelMuWork[j]*modelMuWork2[k]*modelMuWork[l]
                                           +modelMuWork[i]*modelMuWork2[k]*dataCov[j*sw+tt1][l*sw+tt2]
                                           +modelMuWork[j]*modelMuWork2[k]*dataCov[i*sw+tt1][l*sw+tt2]
                                           +modelMuWork2[k]*modelMuWork[l]*dataCov[i*sw+tt1][j*sw+tt1])
                                       - 2*modelSigmaWork2[i][j]*sigInv[k][l]*(modelMuWork[i]*modelMuWork[j]*modelMuWork1[k]*modelMuWork[l]
                                           +modelMuWork[i]*modelMuWork1[k]*dataCov[j*sw+tt1][l*sw+tt2]
                                           +modelMuWork[j]*modelMuWork1[k]*dataCov[i*sw+tt1][l*sw+tt2]
                                           +modelMuWork1[k]*modelMuWork[l]*dataCov[i*sw+tt1][j*sw+tt2])
                                       + 4*sigInv[i][j]*sigInv[k][l]*(modelMuWork1[i]*modelMuWork[j]*modelMuWork2[k]*modelMuWork[l]
                                           +modelMuWork1[i]*modelMuWork2[k]*dataCov[j*sw+tt1][l*sw+tt2]);
                        }
                    }
                }
                erg[a][b] /= 4;         // I haven't got a clue why this division by two is in, but it seems to be right when comparing Hessian and Fisher.
            }
        }
        return erg;
    }
    
    public double[][] computeExpectedHessian() {return computeExpectedHessian(null, null, null);}
    public double[][] computeExpectedHessian(double[][] erg) {return computeExpectedHessian(null, null, erg);}
    public double[][] computeExpectedHessian(double[][] dataCov, double[] dataMean) {return computeExpectedHessian(dataCov, dataMean, null);} 
    public double[][] computeExpectedHessian(double[][] dataCov, double[] dataMean, double[][] erg) {
        erg = Statik.ensureSize(erg, anzPar, anzPar);
        if (dataCov == null) dataCov = sigma;
        if (dataMean == null) dataMean = mu;

        double[][] modelDataCov = this.dataCov; double[] modelDataMean = this.dataMean; int modelN = this.anzPer;
        this.setDataDistribution(dataCov, dataMean);
        computeLogLikelihoodDerivatives(position, false);
        erg = Statik.copy(llDD);
        this.setDataDistribution(modelDataCov, modelDataMean, modelN);
        return erg;
    }
    
    /**
     * Computes the expected Hessian matrix at the current position of the model (ie., using the models sigma and mu). If dataCov and/or dataMean are not null,
     * the expected Hessian is returned for these data distribution; otherwise, sigma and mu are also used for these.
     * 
     * @deprecated It can be shown that the expected Hessian under the data distribution is the Hessian at the data distribution. Both methods return different
     * results at the moment for unknown reason. Small models show no difference, big ones do; potentially numerical? I deprecate the method for now, renaming
     * it to old and adding a new method
     *
     * @param dataCov
     * @param dataMean
     * @param erg
     * @return
     */
    public double[][] computeExpectedHessianOld() {return computeExpectedHessian(null, null, null);}
    public double[][] computeExpectedHessianOld(double[][] erg) {return computeExpectedHessian(null, null, erg);}
    public double[][] computeExpectedHessianOld(double[][] dataCov, double[] dataMean) {return computeExpectedHessian(dataCov, dataMean, null);} 
    public double[][] computeExpectedHessianOld(double[][] dataCov, double[] dataMean, double[][] erg) {
        erg = Statik.ensureSize(erg, anzPar, anzPar);
        if (dataCov == null) dataCov = sigma;
        if (dataMean == null) dataMean = mu;
        double[] mean = Statik.subtract(mu, dataMean);
        
        double[][] muDev = new double[anzPar][anzVar]; double[] muDevDev = new double[anzVar];
        double[][] sigInvMuDev = new double[anzPar][anzVar];
        double[][][] sigInvSigDev = new double[anzPar][anzVar][anzVar]; 
        double[][] sigInvSigDevDev = new double[anzVar][anzVar];
        double[][] invDevDevInv = new double[anzVar][anzVar];
        double[][] invDevInvDev = new double[anzVar][anzVar], invDevInvDevInvSym = new double[anzVar][anzVar];
        
        double[][] sigInv = Statik.invert(sigma);
        for (int ap1=0; ap1<anzPar; ap1++) {
            computeMatrixTimesMuDev(ap1, null, muDev[ap1]);
            computeMatrixTimesMuDev(ap1, sigInv, sigInvMuDev[ap1]);
            computeMatrixTimesSigmaDev(ap1, sigInv, sigInvSigDev[ap1]);
            for (int ap2=0; ap2<=ap1; ap2++) {
//                if (logStream != null) logStream.println("Starting with Hessian position ("+ap1+","+ap2+")");
                computeMatrixTimesMuDevDev(ap1, ap2, sigInv, muDevDev);
                computeMatrixTimesSigmaDevDev(ap1, ap2, sigInv, sigInvSigDevDev);

                Statik.multiply(sigInvSigDevDev, sigInv, invDevDevInv);
                Statik.multiply(sigInvSigDev[ap1], sigInvSigDev[ap2], invDevInvDev);
                Statik.multiply(invDevInvDev, sigInv, invDevInvDevInvSym);
                Statik.symmetrize(invDevInvDevInvSym);                
                
                double er = 0; for (int i=0; i<anzVar; i++) er += sigInvSigDevDev[i][i] - invDevInvDev[i][i];
                for (int i=0; i<anzVar; i++) er += muDev[ap1][i]*sigInvMuDev[ap2][i]+muDev[ap2][i]*sigInvMuDev[ap1][i];
                for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) er -= mean[i]*(sigInvSigDev[ap1][i][j]+sigInvSigDev[ap1][j][i])*sigInvMuDev[ap2][j];
                for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) er -= mean[i]*(sigInvSigDev[ap2][i][j]+sigInvSigDev[ap2][j][i])*sigInvMuDev[ap1][j];
                for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) er += mean[i]*(sigInv[i][j]+sigInv[j][i])*muDevDev[j];
                for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) er += mean[i]*invDevInvDevInvSym[i][j]*mean[j];
                for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) er += invDevInvDevInvSym[i][j]*dataCov[i][j];
                for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) er -= invDevDevInv[i][j]*dataCov[i][j];
//                erg[ap1][ap2] = erg[ap2][ap1] = er / 2;     // no clue where the /2 comes from, but seems right on final result.
            }
        }
        
        return erg;
    }
    
    /**
     * Computes the expected Hessian matrix at the current position of the model (ie., using the models sigma and mu). If dataCov and/or dataMean are not null,
     * the expected Hessian is returned for these data distribution; otherwise, sigma and mu are also used for these. The model that assumes independent and 
     * identically distributed data rows, while the data is really time delayed embedded. dataCov and dataMean are given for the complete time series, but
     * without repetitions. 
     * 
     * @param dataCov
     * @param dataMean
     * @param erg
     * @return
     */
    public double[][] computeExpectedHessianOfTimeDelayedEmbedding(int anzT) {return computeExpectedHessianOfTimeDelayedEmbedding(null, null, null, anzT, 1);}
    public double[][] computeExpectedHessianOfTimeDelayedEmbedding(int anzT, int stepWidth) {return computeExpectedHessianOfTimeDelayedEmbedding(null, null, null, anzT, stepWidth);}
    public double[][] computeExpectedHessianOfTimeDelayedEmbedding(double[][] erg, int anzT) {return computeExpectedHessianOfTimeDelayedEmbedding(null, null, erg, anzT, 1);}
    public double[][] computeExpectedHessianOfTimeDelayedEmbedding(double[][] erg, int anzT, int stepWidth) {return computeExpectedHessianOfTimeDelayedEmbedding(null, null, erg, anzT, stepWidth);}
    public double[][] computeExpectedHessianOfTimeDelayedEmbedding(double[][] dataCov, double[] dataMean) {int anzT = dataCov.length-anzVar+1; return computeExpectedHessianOfTimeDelayedEmbedding(dataCov, dataMean, null,anzT,1);} 
    public double[][] computeExpectedHessianOfTimeDelayedEmbedding(double[][] dataCov, double[] dataMean, int stepWidth) {int anzT = dataCov.length-anzVar+1; return computeExpectedHessianOfTimeDelayedEmbedding(dataCov, dataMean, null,anzT,stepWidth);} 
    public double[][] computeExpectedHessianOfTimeDelayedEmbedding(double[][] dataCov, double[] dataMean, double[][] erg, int anzT, int stepWidth) {
        erg = Statik.ensureSize(erg, anzPar, anzPar);
        if (dataCov == null || dataMean == null) {
            computeExpectedHessian(null, null, erg);
            Statik.multiply(anzT, erg, erg);
            return erg;
        }
        assert(anzT == dataCov.length - anzVar*stepWidth + stepWidth);
        int sw = stepWidth;
        
        double[][] muDev = new double[anzPar][anzVar]; double[] muDevDev = new double[anzVar];
        double[][] sigInvMuDev = new double[anzPar][anzVar];
        double[][][] sigInvSigDev = new double[anzPar][anzVar][anzVar]; 
        double[][] sigInvSigDevDev = new double[anzVar][anzVar];
        double[][] invDevDevInv = new double[anzVar][anzVar];
        double[][] invDevInvDev = new double[anzVar][anzVar], invDevInvDevInvSym = new double[anzVar][anzVar];
        
        double[][] sigInv = Statik.invert(sigma);
        for (int ap1=0; ap1<anzPar; ap1++) {
            computeMatrixTimesMuDev(ap1, null, muDev[ap1]);
            computeMatrixTimesMuDev(ap1, sigInv, sigInvMuDev[ap1]);
            computeMatrixTimesSigmaDev(ap1, sigInv, sigInvSigDev[ap1]);
            for (int ap2=0; ap2<=ap1; ap2++) {
                computeMatrixTimesMuDevDev(ap1, ap2, sigInv, muDevDev);
                computeMatrixTimesSigmaDevDev(ap1, ap2, sigInv, sigInvSigDevDev);

                Statik.multiply(sigInvSigDevDev, sigInv, invDevDevInv);
                Statik.multiply(sigInvSigDev[ap1], sigInvSigDev[ap2], invDevInvDev);
                Statik.multiply(invDevInvDev, sigInv, invDevInvDevInvSym);
                Statik.symmetrize(invDevInvDevInvSym);                
                
                double er = 0; 
                for (int i=0; i<anzVar; i++) er += anzT * (sigInvSigDevDev[i][i] - invDevInvDev[i][i]);
                for (int i=0; i<anzVar; i++) er += anzT * (muDev[ap1][i]*sigInvMuDev[ap2][i]+muDev[ap2][i]*sigInvMuDev[ap1][i]);
                for (int t1 = 0; t1<anzT; t1++) for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) {
                    er -= (mu[i]-dataMean[i+t1])*(sigInvSigDev[ap1][i][j]+sigInvSigDev[ap1][j][i])*sigInvMuDev[ap2][j];
                    er -= (mu[i]-dataMean[i+t1])*(sigInvSigDev[ap2][i][j]+sigInvSigDev[ap2][j][i])*sigInvMuDev[ap1][j];
                    er += (mu[i]-dataMean[i+t1])*(sigInv[i][j]+sigInv[j][i])*muDevDev[j];
                    er += (mu[i]-dataMean[i+t1])*invDevInvDevInvSym[i][j]*(mu[j]-dataMean[j+t1]);
                    er += invDevInvDevInvSym[i][j]*dataCov[i+t1][j+t1];
                    er -= invDevDevInv[i][j]*dataCov[i+t1][j+t1];
                }
                erg[ap1][ap2] = erg[ap2][ap1] = er/2;       // no clue where the /2 comes from, but seems right.
            }
        }
        
        return erg;
    }

    
    /**
     * returns a matrix M such that for independent standard normal x, x^TMx is the distribution of the LR under the null hypothesis. 
     * The method could probably be done more efficient.
     * 
     * @return C^T H_model C, where C is Cholesky decomposition of H^{-1} I H^{-1}, with H the hessian and I the fisher
     */
    public double[][] specificationMatrix(double[][] hessianModel, double[][] hessianData, double[][] fisher, double[][] erg) {
        modelWork1 = Statik.ensureSize(modelWork1, anzPar, anzPar);
        modelWork2 = Statik.ensureSize(modelWork2, anzPar, anzPar);
        modelWork3 = Statik.ensureSize(modelWork3, anzPar, anzPar);
        erg = Statik.ensureSize(erg, anzPar, anzPar);
        
        Statik.invert(hessianData, modelWork1, modelWork2);
        Statik.multiply(modelWork1, fisher, modelWork2);
        Statik.multiply(modelWork2, modelWork1, modelWork3);
        Statik.choleskyDecompose(modelWork3, modelWork2);
        Statik.multiply(hessianModel, modelWork2, modelWork3);
//        Statik.multiply(hessianData, modelWork2, modelWork3);
        Statik.transpose(modelWork2, modelWork1);
        Statik.multiply(modelWork1, modelWork3, erg);
        
        return erg;
    }

    /**
     * returns a matrix M such that for independent standard normal x, x^TMx is the distribution of the LR under the null hypothesis. 
     * The method could probably be done more efficient.
     * 
     * TODO: At the moment, Hessian of the model is ignored and  Hessian of the Data is used instead. Seems to work on test cases. Reason unknown.
     * 
     * @param hessian: The Hessian 
     * @param fisher : The Fisher
     * @param erg    : returned matrix (null allocates a new one)
     * @param hypothesisParameter: parameter Numbers forming the hypothsis (assumed to be set to zero here)
     * @return C^T H^{-1} C, where C is Cholesky decomposition of H^{-1} I H^{-1}, with H the hessian taken from this (llDD), and the fisher taken as input
     * 
     * 
     * 
     */
    public double[][] specificationMatrix(double[][] hessianModel, double[][] hessianData, double[][] fisher, int[] hypothesisParameter, double[][] erg) {
        modelWork1 = Statik.ensureSize(modelWork1, anzPar, anzPar);
        modelWork2 = Statik.ensureSize(modelWork2, anzPar, anzPar);
        modelWork3 = Statik.ensureSize(modelWork3, anzPar, anzPar);
        erg = Statik.ensureSize(erg, anzPar, anzPar);

        hessianModel = hessianData;
        
        int[] hyp = Statik.copy(hypothesisParameter); Arrays.sort(hyp);
        int anzHyp = hypothesisParameter.length;
        double[][] A = new double[anzPar][anzPar];
        double[][] sqrtA = new double[anzPar][anzPar];
        double[][] Q = new double[anzPar][anzPar];
        Statik.invert(hessianData, modelWork1, modelWork2);
        Statik.multiply(modelWork1, fisher, modelWork2);
        Statik.multiply(modelWork2, modelWork1, A);
        try {Statik.choleskyDecompose(A, sqrtA); } catch (Exception e) {
//            System.out.println("Cholesky in computation of Specification Matrix failed, soft algorithm used instead."); 
            Statik.choleskyDecompose(A, sqrtA, Double.POSITIVE_INFINITY);
        }
        System.out.println(Statik.matrixToMapleString(hessianData,5));
        System.out.println(Statik.matrixToMapleString(fisher,5));
        System.out.println(Statik.matrixToMapleString(A,4));
        
        int[] noHyp = new int[anzPar - anzHyp]; 
        int d1 = 0, d2 = 0; for (int i=0; i<anzPar; i++) if (d1 >= hyp.length || hyp[d1] != i) noHyp[d2++] = i; else d1++; 
        double[][] Q2p = Statik.submatrix(hessianData, noHyp, noHyp);
        double[][] Q12p = Statik.submatrix(hessianData, noHyp, hyp);
        double[][] Q12 = Statik.multiply(Statik.invert(Q2p), Q12p);
        
        // TODO The computation of modelWork3 = H - Q^T H Q in the next ~20 lines works, but is suboptimal. Instead, one should directly compute
        // modelwork3 = /  H11 - H12 H22^-1 H12^T       0  \
        //              \             0                 0  /
        // and preferably already reduce the dimensionality of the result by picking the right rows from sqrtA. 
        // It can be shown that if H is p.d. ==> H11 - H12 H22^-1 H12^T p.d. by representing the quadratic from around the right matrix 
        // in the quadratic form around H.
        for (int i=0; i<anzHyp; i++) {
            int c1=0, c2 = 0; for (int j=0; j<anzPar; j++) if (c1 < anzHyp && hyp[c1]==j) {
                c1++; 
            } else {
                Q[j][j] = 1;
                Q[j][hyp[i]] = Q12[c2][i]; c2++;
            }
        }
        Statik.transpose(Q, modelWork2);
        Statik.multiply(modelWork2, hessianModel, modelWork3);
        Statik.multiply(modelWork3, Q, modelWork2);
//        Statik.transpose(Q, modelWork1);
//        Statik.multiply(Q, hessianModel, modelWork3);
//        Statik.multiply(modelWork3, modelWork1, modelWork2);
        Statik.subtract(hessianModel, modelWork2, modelWork3);
        
        Statik.transpose(sqrtA, modelWork1);
        Statik.multiply(modelWork1, modelWork3, modelWork2);
        Statik.multiply(modelWork2, sqrtA, erg);
        
//        System.out.println("\r\nQ = "+Statik.matrixToString(Q));
//        System.out.println("\r\nFree       Parameter Covariance: \r\n"+Statik.matrixToString(A));
//        System.out.println("\r\nRestricted Parameter Covariance: \r\n"+Statik.matrixToString(restrictedParameterCovariance));
        
        
        return erg;
    }
    
    /** 
     * Finds a data covariance matrix with minimal least squares distance to dataCov with parameter estimates equal to targetParameterValues.
     * ASSUMES means in the data set and in the model fixed to zero.
     *  
     *  STATUS untested, probably working
     *  
     * @param dataCov
     * @param targetParameterValues
     * @return  the data covariance matrix.
     */
    public double[][] computeCovarianceCloseToRealWithParameterConstraint(double[][] dataCov, double[] targetParameterValues) {
        return computeCovarianceCloseToRealWithParameterConstraint(dataCov, targetParameterValues, null);}
    public double[][] computeCovarianceCloseToRealWithParameterConstraint(double[][] dataCov, double[] targetParameterValues, double[][] erg) {
        int anzStat = (int)(anzVar*(anzVar+1) / 2.0);
             
        rectWork = Statik.ensureSize(rectWork, anzPar, anzStat);
        rectWork2 = Statik.ensureSize(rectWork2, anzStat, anzPar);
        modelWork1 = Statik.ensureSize(modelWork1, anzPar, anzPar);
        modelWork2 = Statik.ensureSize(modelWork2, anzPar, anzPar);
        modelWork3 = Statik.ensureSize(modelWork3, anzPar, anzPar);
        modelWork4 = Statik.ensureSize(modelWork4, anzPar, anzPar);
        modelVecWork1 = Statik.ensureSize(modelVecWork1, anzStat);
        modelVecWork2 = Statik.ensureSize(modelVecWork2, anzPar);
        modelVecWork3 = Statik.ensureSize(modelVecWork3, anzStat);
        sigInv = Statik.ensureSize(sigInv, anzVar, anzVar);
        modelSigmaWork = Statik.ensureSize(modelSigmaWork, anzVar, anzVar);
        modelSigmaWork2 = Statik.ensureSize(modelSigmaWork2, anzVar, anzVar);
        p1w = Statik.ensureSize(p1w, anzPar);
        
        if (dataCov == null) dataCov = this.dataCov;
        if (erg == null) erg = new double[anzVar][anzVar];
        
        evaluateMuAndSigma(targetParameterValues);
        Statik.invert(sigma, sigInv, modelSigmaWork);
        for (int par=0; par<anzPar; par++) {
            computeMatrixTimesSigmaDev(par, sigInv, modelSigmaWork);
            Statik.multiply(modelSigmaWork, sigInv, modelSigmaWork2);
            int k =0; for (int i=0; i<anzVar; i++) for (int j=0; j<=i; j++) rectWork[par][k++] = modelSigmaWork2[i][j] * (i!=j?2:1);
            p1w[par] = 0; for (int i=0; i<anzVar; i++) p1w[par] += modelSigmaWork[i][i];
        }
        int k=0; for (int i=0; i<anzVar; i++) for (int j=0; j<=i; j++) modelVecWork1[k++] = dataCov[i][j];
        
        Statik.pseudoInvert(rectWork, rectWork2, modelWork1, modelWork2, modelWork3, modelWork4, new int[anzPar], new boolean[anzStat]);
        Statik.multiply(rectWork, modelVecWork1, modelVecWork2);
        Statik.multiply(rectWork2, modelVecWork2, modelVecWork3);
        Statik.subtract(modelVecWork1, modelVecWork3, modelVecWork3);
        Statik.multiply(rectWork2, p1w, modelVecWork1);
        k=0; for (int i=0; i<anzVar; i++) for (int j=0; j<=i; j++) {erg[i][j] = modelVecWork1[k] + modelVecWork3[k]; k++;}
        for (int i=0; i<anzVar; i++) for (int j=i+1; j<anzVar; j++) erg[i][j] = erg[j][i];
        return erg;
    }

    /** Simulates data from true distribution and analysis it with analysisModel, fixing the hypoParameters (to the value they are set to upon call). If
     * timeDelayEmbed is true, it is assumed that the true distribution describes a complete time series, which is then cut in overlapping 
     * samples with the anzVar of the analysis model as embedding dimension. Returns the critical value to exceed 5%, which is, the 95% percentile of the distribution.  
     * 
     * @param N
     * @param trueCovariance
     * @param trueMean
     * @param analysisModel
     * @param hypoPara
     * @param timeDelayEmbed
     * @return
     */
    public double simulateMisspecification(int N, double[][] trueCovariance, double[] trueMean, int[] hypoPara, boolean timeDelayEmbed, int trials, double[] allValues) {
        return simulateMisspecification(N, trueCovariance, trueMean, this, hypoPara, timeDelayEmbed, trials, allValues);
    }
    public static double simulateMisspecification(int N, double[][] trueCovariance, double[] trueMean, Model analysisModel, int[] hypoPara, 
            boolean timeDelayEmbed, int trials) {return simulateMisspecification(N, trueCovariance, trueMean, analysisModel,hypoPara, timeDelayEmbed, trials, null);}
    public static double simulateMisspecification(int N, double[][] trueCovariance, double[] trueMean, Model analysisModel, int[] hypoPara, 
            boolean timeDelayEmbed, int trials, double[] allValues) {
        Model restrictedModel = analysisModel.copy(); 
        restrictedModel.fixParameter(hypoPara);
        return simulateMisspecification(N, trueCovariance, trueMean, analysisModel, restrictedModel, timeDelayEmbed, trials, allValues);
    }
    public static double simulateMisspecification(int N, double[][] trueCovariance, double[] trueMean, Model analysisModel, Model restrictedModel,
            boolean timeDelayEmbed, int trials, double[] allValues) {
        return simulateMisspecificationPrivate(N, trueCovariance, trueMean, analysisModel, restrictedModel, null, timeDelayEmbed, trials, allValues);
    }
    private static double simulateMisspecificationPrivate(int N, double[][] trueCovariance, double[] trueMean, Model analysisModel, Model restrictedModel,
            int[] hypoPara, boolean timeDelayEmbed, int trials, double[] allValues) {
        allValues = Statik.ensureSize(allValues, trials);
        if (hypoPara != null) {
            restrictedModel = analysisModel.copy(); 
            restrictedModel.fixParameter(hypoPara);
        }
        double[] fullStart = analysisModel.getParameter();
        double[] resStart = restrictedModel.getParameter();
        int storeMinIter = analysisModel.getMinimalNumberOfIterations();
        analysisModel.setMinimalNumberOfIterations(10);
        double[][] mapRot = null; double[] mapMov = null;
        if (hypoPara == null) {
            mapRot = new double[analysisModel.anzPar][analysisModel.anzPar];
            mapMov = new double[analysisModel.anzPar];
            boolean isNested = analysisModel.isNestedSubmodel(restrictedModel, mapMov, mapRot, 0.00001);
            if (!isNested) System.out.println("Warning: Restricted model is not nested in full model.");
        }
        
        double mean = 0;
        final double EPS = 0.000000001;
        for (int t=0; t<trials; t++) {
            double[][] data = Model.createData(N, trueMean, trueCovariance, analysisModel.rand);
            if (timeDelayEmbed) {
                int rows = data[0].length - analysisModel.anzVar + 1;
                double[][] embedData = new double[rows*N][analysisModel.anzVar];
                for (int i=0; i<N; i++) 
                    for (int j=0; j<rows; j++) 
                        for (int k=0; k<analysisModel.anzVar; k++) embedData[i*rows+j][k] = data[i][j+k];
                data = embedData;
            }
            restrictedModel.setData(data);
            double[] est2 = restrictedModel.estimateML(resStart,EPS);
            System.out.println("Res  Estimates = "+Statik.matrixToString(est2));
            double[] start = null;
            if (hypoPara != null) {
                analysisModel.setParameter(restrictedModel.getParameterNames(), restrictedModel.getParameter());
                for (int i=0; i<hypoPara.length; i++) analysisModel.setParameter(hypoPara[i], fullStart[hypoPara[i]]);
                start = analysisModel.getParameter();
            } else {
                double[] vec = new double[analysisModel.anzPar]; Statik.copy(est2, vec);
                start = Statik.multiply(mapRot, vec);
                Statik.add(vec, mapMov, vec);
            }
            analysisModel.setData(data);
            double[] est1 = analysisModel.estimateML(start,EPS);
            System.out.println("Full Estimates = "+Statik.matrixToString(est1));
            double lr = restrictedModel.ll - analysisModel.ll;
            mean += lr;
            allValues[t] = lr;
            if (analysisModel.logStream != null) analysisModel.logStream.println("\r\nTrial "+t+", full -2ll = "+analysisModel.ll+", res -2ll = "+restrictedModel.ll+", LR = "+lr);
            System.out.println("\r\nTrial "+t+", full -2ll = "+analysisModel.ll+", res -2ll = "+restrictedModel.ll+", LR = "+lr);
        }
        mean /= (double)trials;
        Arrays.sort(allValues);
        double erg = allValues[(int)Math.round((double)trials*0.95)];
        analysisModel.setMinimalNumberOfIterations(storeMinIter);
        return erg;
    }

    /** 
     * Computes the miss-specification weights on the chi-square distribution that occurs under normal miss-specification when creating data 
     * from true distribution and analysis it with analysisModel, fixing the hypoParameters (to the value they are set to upon call). If
     * timeDelayEmbed is true, it is assumed that the true distribution describes a complete time series, which is then cut in overlapping 
     * samples with the anzVar of the analysis model as embedding dimension. if analyticalPossible is true, analytical estimation is called instead
     * of numerical estimation. 
     * 
     * @param trueCovariance
     * @param trueMean
     * @param analysisModel
     * @param hypoPara
     * @param timeDelayEmbed
     * @param analyticalPossible
     * @return
     */
    public double[] computeMisspecification(double[][] trueCovariance, double[] trueMean, int[] hypoPara, boolean timeDelayEmbed, boolean analyticalPossible) {
        return computeMisspecification(trueCovariance, trueMean, this, hypoPara, timeDelayEmbed, analyticalPossible);
    }
    public static double[] computeMisspecification(double[][] trueCovariance, double[] trueMean, Model analysisModel, int[] hypoPara, 
            boolean timeDelayEmbed, boolean analyticalPossible) {
        return computeMisspecification(trueCovariance, trueMean, analysisModel, hypoPara, timeDelayEmbed, analyticalPossible, null);}
    public static double[] computeMisspecification(double[][] trueCovariance, double[] trueMean, Model analysisModel, int[] hypoPara, 
            boolean timeDelayEmbed, boolean analyticalPossible, double[] estimate) {

        int rows = 0;
        if (timeDelayEmbed) {
            rows = trueCovariance.length - analysisModel.anzVar + 1;
            int o = analysisModel.anzVar;
            double[] tdMean = new double[o]; 
            for (int i=0; i<rows; i++) for (int j=0; j<o; j++) tdMean[j] += trueMean[i+j];
            for (int i=0; i<o; i++) tdMean[i] /= rows;
            double[][] tdCov = new double[o][o]; 
            for (int i=0; i<rows; i++) for (int j=0; j<o; j++) for (int k=0; k<o; k++) 
                tdCov[j][k] += trueCovariance[i+j][i+k] + (trueMean[i+j]-tdMean[j])*(trueMean[i+k]-tdMean[k]);  
            for (int j=0; j<o; j++) for (int k=0; k<o; k++) tdCov[j][k] /= rows;
            analysisModel.setDataDistribution(tdCov, tdMean);
        } else analysisModel.setDataDistribution(trueCovariance, trueMean); 

//        System.out.print("Computing Estimates...");
        if (estimate == null) {
            if (analyticalPossible) estimate = ((LinearModel)analysisModel).estimateMLFullCovarianceSupportedByPowerEquivalence(true);
            else estimate = analysisModel.estimateML();
        }

//        System.out.print("done. \r\nComputing Hessian of the Data...");
        double[][] hessianData = (timeDelayEmbed?analysisModel.computeExpectedHessianOfTimeDelayedEmbedding(trueCovariance, trueMean, null, rows,1):
                                                 analysisModel.computeExpectedHessian(trueCovariance, trueMean, null) );
//        System.out.print("done. \r\nComputing Fisher Information Matrix...");
        double[][] fisher = (timeDelayEmbed?analysisModel.computeFisherMatrixOfTimeDelayedEmbedding(trueCovariance, trueMean, null):
                                            analysisModel.computeFisherMatrix(trueCovariance, trueMean, null) );
//        System.out.print("done. \r\nComputing Specification Matrix...");
        double[][] lrDist = analysisModel.specificationMatrix(hessianData, hessianData, fisher, hypoPara, null);
//        System.out.print("done. \r\nComputing Eigenvalues...");
        double[] eigenvalues = new double[lrDist.length]; Statik.eigenvalues(lrDist, 0.001, eigenvalues, null);
//        System.out.println("done.");
        return eigenvalues;
    }

    public double[] computeMisspecification(int[] hypoPara, boolean analyticalPossible, double[] start, boolean correctData) {

        final int ANZSTEPS = 500;
        final double PRECISION = 0.0000001;
        
        int storeMaxSteps = this.MAXRUNS;
        this.setMaximalNumberOfIterations(ANZSTEPS);
        
        double[] trueParameter;
        double[] trueMean; 
        double[][] trueCovariance;
        if (start == null) start = getParameter();
        if (correctData) {
            Model restrictedModel = copy(); 
            restrictedModel.fixParameter(hypoPara);
            restrictedModel.setDataDistribution(this.dataCov, this.dataMean, 1);
            double[] resStart = restrictedModel.getParameter();
    
            if (analyticalPossible) ((LinearModel)restrictedModel).estimateMLFullCovarianceSupportedByPowerEquivalence(true);
            else restrictedModel.estimateML(resStart,0.00001);
            
            setParameter(restrictedModel.getParameterNames(), restrictedModel.getParameter());
            setParameter(hypoPara, Statik.subvector(start, hypoPara));
            trueParameter = getParameter();
            
            trueMean = new double[anzVar];
            trueCovariance = computeCovarianceCloseToRealWithParameterConstraint(dataCov, trueParameter);            

        } else {
            trueCovariance = Statik.copy(this.dataCov);
            trueMean = Statik.copy(this.dataMean);
            if (analyticalPossible) ((LinearModel)this).estimateMLFullCovarianceSupportedByPowerEquivalence(true);
            else this.estimateML(start,PRECISION);
            trueParameter = getParameter(); 
        }

        double[] ev = Statik.eigenvalues(trueCovariance, 0.001);
        double min = Double.MAX_VALUE; for (int i=0; i<ev.length;  i++) if (ev[i] < min) min = ev[i];
        if ((min < 0) && logStream != null) logStream.println("Warning: Best guess for true covariance under restriction is not positive definite, minimal EV = "+min);

        this.setDataDistribution(trueCovariance, trueMean, 1);
        this.computeLogLikelihoodDerivatives(trueParameter);
        if (!Statik.isPositiveDefinite(this.llDD)) {
            this.warningFlag = Model.warningFlagTypes.FAILED; 
            if (logStream != null) logStream.println("Warning: Miss specification is so severe that Hessian at optimum is no longer positive definite."); 
        }

        double[] erg = computeMisspecification(trueCovariance, trueMean, this, hypoPara, false, analyticalPossible, trueParameter);
        this.setDataDistribution(trueCovariance, trueMean);
        
        this.setMaximalNumberOfIterations(storeMaxSteps);
        return erg;
    }

    /**
     * Computes the misspecification weights on the chi-square distribution that occurs under normal miss-specification when creating data 
     * from true distribution and analysis it with analysisModel, restricting the model such that for restrictionMapping A and
     * restrictionMove b, in the parameter space theta that maps to the actual parameter as A theta + b, the last anzRestricted
     * parameters are mapped to zero. 
     * If analyticalPossible is true, analytical estimation is called instead of numerical estimation. 
     * 
     * @param trueCovariance
     * @param trueMean
     * @param analysisModel
     * @param restrictionMapping
     * @param restrictionMove       should be meaningless
     * @param anzRestricted
     * @param analyticalPossible
     * @param estimate
     * @return
     */
    public static double[] computeMisspecification(double[][] trueCovariance, double[] trueMean, Model analysisModel, double[][] restrictionMapping, 
            double[] restrictionMove, int anzRestricted, boolean analyticalPossible, double[] estimate) {

        analysisModel.setDataDistribution(trueCovariance, trueMean); 

//        System.out.print("Computing Estimates...");
        if (estimate == null) {
            if (analyticalPossible) estimate = ((LinearModel)analysisModel).estimateMLFullCovarianceSupportedByPowerEquivalence(true);
            else estimate = analysisModel.estimateML(0.0000001);
        }

//        System.out.print("done. \r\nComputing Hessian of the Data...");
        double[][] hessianData = analysisModel.computeExpectedHessian(trueCovariance, trueMean, null);
        Statik.multiply(restrictionMapping, hessianData, restrictionMapping, analysisModel.modelWork1, analysisModel.modelWork2);
        Statik.copy(analysisModel.modelWork1, hessianData);
        Statik.multiply(restrictionMapping, hessianData, analysisModel.modelWork1);
//        System.out.print("done. \r\nComputing Fisher Information Matrix...");
        double[][] fisher = analysisModel.computeFisherMatrix(trueCovariance, trueMean, null);
        Statik.multiply(restrictionMapping, fisher, restrictionMapping, analysisModel.modelWork1, analysisModel.modelWork2);
        Statik.copy(analysisModel.modelWork1, fisher);
//        System.out.print("done. \r\nComputing Specification Matrix...");
        int[] hypoPara = new int[anzRestricted]; for (int i=0; i<anzRestricted; i++) hypoPara[i] = i+analysisModel.anzPar-anzRestricted;
        double[][] lrDist = analysisModel.specificationMatrix(hessianData, hessianData, fisher, hypoPara, null);
//        System.out.print("done. \r\nComputing Eigenvalues...");
        double[] eigenvalues = new double[lrDist.length]; Statik.eigenvalues(lrDist, 0.001, eigenvalues, null);
//        System.out.println("done.");
        return eigenvalues;
    }
    
    
    public double[] getVariableDescriptivesArray(int column) {
        int anzMiss = 0;
        for (int i=0; i<anzPer; i++) if (Model.isMissing(data[i][column])) anzMiss++;
        int anz = anzPer - anzMiss;
        double[] col = new double[anz]; int j = 0; for (int i=0; i<anzPer; i++) if (!Model.isMissing(data[i][column])) col[j++] = data[i][column];
        double min = Double.NaN, max = Double.NaN, fqu = Double.NaN, median = Double.NaN, mean = Double.NaN, tqu = Double.NaN, stdv = Double.NaN;
        if (col.length > 0) {
            Arrays.sort(col);
            min = col[0]; max = col[anz-1];
            int ix1st = (anz-1)/4; if (((anz-1)/4) % 4 == 0) {fqu = col[ix1st]; tqu = col[anz-1-ix1st]; } else {fqu = (col[ix1st]+col[ix1st+1])/2.0; tqu = (col[anz-1-ix1st]+col[anz-2-ix1st])/2.0;}
            if (anz % 2 == 0) median = (col[anz/2]+col[anz/2+1])/2.0; else median = col[anz/2];
            mean = 0; double var = 0; for (int i=0; i<anz; i++) {mean += col[i]; var += col[i]*col[i];}
            mean /= anz; var = var / anz - mean*mean; stdv = Math.sqrt(var);
        }
        return new double[]{min, fqu, median, mean, tqu, max, stdv, anzPer, anzMiss};
    }
    
    public String getVariableDescriptives(int column) {return getVariableDescriptives(column, 5);}
    public String getVariableDescriptives(int column, int digits) {
        double[] desc = getVariableDescriptivesArray(column);
        String erg  = 
               "Min.   :"+Statik.doubleNStellen(desc[0], digits);
        erg += "1st Qu.:"+Statik.doubleNStellen(desc[1], digits);
        erg += "Median :"+Statik.doubleNStellen(desc[2], digits);
        erg += "Mean   :"+Statik.doubleNStellen(desc[3], digits);
        erg += "3rd Qu.:"+Statik.doubleNStellen(desc[4], digits);
        erg += "Max.   :"+Statik.doubleNStellen(desc[5], digits);
        erg += "Stdv   :"+Statik.doubleNStellen(desc[6], digits);
        return erg;        
    }

    /** 
     * indicates whether sigma of this model has zero or negative Eigenvalues on all parameter sets.
     * 
     * @return
     */
    public boolean isConstantSingular() {
        if (anzVar == 0) return false;
        final int TRIALS = 10;
        for (int i=0; i<TRIALS; i++) {
            evaluateMuAndSigma(getRandomStartingValues(5));
            try {
                Statik.choleskyDecompose(sigma, 0.00001, logresult);
            } catch (RuntimeException e) {return false;}
            if (!Double.isInfinite(logresult[0])) return false;            
        }
        return true;
    }
    
    public double[] getRandomStartingValues() {return getRandomStartingValues(1000);}
    public double[] getRandomStartingValues(double priorvariance) {
        double[] erg = new double[anzPar]; 
        for (int i=0; i<anzPar; i++) erg[i] = rand.nextGaussian()*Math.sqrt(priorvariance);
        return erg;
    }
    
    /**
     * 
     * @return -1 if Hessian is not singular, otherwise the largest entry of the first kernel Eigenvector. 
     * 
     * 
     */
    public int hessianIsSingular() {
        this.modelWork1 = Statik.ensureSize(modelWork1, anzPar, anzPar);
        this.modelVecWork1 = Statik.ensureSize(modelVecWork1, anzPar);
        
        final double EPS = 1.0 / 1000000.0;
        final double precisionRelative = 10000.0; 

        Statik.identityMatrix(modelWork1);
        double absTrace = Math.abs(Statik.trace(llDD));
        double precision = EPS * absTrace;
        Statik.eigenvalues(llDD, 0.0001, modelVecWork1, modelWork1);
        Statik.sortMatrixRowsByVector(modelVecWork1, modelWork1);
        for (int j=0; j<modelVecWork1.length; j++) 
            if (Math.abs(modelVecWork1[j]) < precision || 
                    (j>0 && Math.abs(modelVecWork1[j]) < precisionRelative && Math.abs(modelVecWork1[j]/modelVecWork1[j-1]) < precisionRelative)) {
            int ix = -1; double max = 0; for (int i=0; i<anzPar; i++) if (Math.abs(modelWork1[i][j]) > max) {max = Math.abs(modelWork1[i][j]); ix = i;}
            return ix;
        }
        return -1;
    }
    
    /**
     * Evaluates model at the input values, at arbitrary starting values, and at random starting values. returns -1 if
     * any Hessian under these values is of full rank. Otherwise, it returns the index of one entry that is nonzero on all trials.
     * 
     *  RAMModel overrides this function to add a parameter count, which is not possible for models with unknown structure. 
     * 
     * @param parameterValues
     * @return  -1 if matrix is not singular for one parameter set, and index of any one non-zero kernel entry otherwise.  
     */
    public int hessianIsConstantSingular(double[] parameterValues) {
        final int TRIALS = 12;
        
        int saveAnzPer = anzPer;
        boolean saveIsIndirect = isIndirectData;
        double[] saveDataMean = null; double[][] saveDataCovariance = null;
        if (isIndirectData) {
            saveDataMean = Statik.copy(dataMean);
            saveDataCovariance = Statik.copy(dataCov);
        }
        anzPer = 1;
        
        int erg = -1;
        for (int i=0; i<TRIALS; i++) {
            double[] start = null;
            if (i==0) start = parameterValues;
            if (i==1) start = getArbitraryStartingValues();
            if (i >1) start = getRandomStartingValues();
            setParameter(start);
            setDataDistribution();
            computeLogLikelihoodDerivatives(start, false);
            if (!Double.isInfinite(ll)) {
                int v = hessianIsSingular();
                if (v == -1) {erg = -1; i = TRIALS; }
                else if (erg == -1) erg = v;
            }
        }

        this.anzPer = saveAnzPer;
        this.isIndirectData = saveIsIndirect;
        if (isIndirectData) {
            Statik.copy(saveDataMean,dataMean);
            Statik.copy(saveDataCovariance, dataCov);
            computeMomentsFromDataCovarianceAndMean();
        } else computeMoments();
        return erg;
    }

    public String[] getVariableNames() {return null;}
    public int[] getObservedVariables() {return null;}

    /**
     * Tests if the submodel provided is nested in this by finding the mapping of parameters. Is also used to compute misspecification matrix.
     * After completetion, mov and rot are such that for a parameter set theta of the submodel filled up with zeros, 
     * 
     *      rot * thetat + mov
     *      
     * gives the corresponding parameter set of this model. 
     * 
     * @param nestedModel
     * @param mov
     * @param rot
     * @param EPS
     * @return
     */
    public boolean isNestedSubmodel(Model nestedModel, double EPS) {return isNestedSubmodel(nestedModel, new double[anzPar], new double[anzPar][anzPar], EPS);}
    public boolean isNestedSubmodel(Model nestedModel, double[] mov, double[][] rot, double EPS) 
    {
        if (this.anzVar != nestedModel.anzVar) return false;
        if (anzVar == 0) return true;
        
        double[][] storeDataCov = dataCov; double[] storeDataMu = dataMean; dataCov = new double[anzVar][anzVar]; dataMean = new double[anzVar];
        
        double[] baseNested = nestedModel.getArbitraryStartingValues();
        nestedModel.evaluateMuAndSigma(baseNested);
        this.setDataDistribution(nestedModel.sigma, nestedModel.mu);        
        double[] baseFull = estimateLS(getArbitraryStartingValues(), EPS/10);
        if (Math.abs(ls) > EPS) {setDataDistribution(storeDataCov,storeDataMu); return false;}
        for (int i=0; i<nestedModel.anzPar; i++) {
            baseNested[i]+=1.0;
            nestedModel.evaluateMuAndSigma(baseNested);
            this.setDataDistribution(nestedModel.sigma, nestedModel.mu);        
            rot[i] = estimateLS(baseFull, EPS/10);
            baseNested[i]-=1.0;
            if (Math.abs(ls) > EPS) {setDataDistribution(storeDataCov,storeDataMu); return false;}
            Statik.subtract(rot[i], baseFull, rot[i]);
        }
        Statik.findOrthogonalCompletion(rot, nestedModel.anzPar, EPS/10);
        Statik.transpose(rot, rot);
        Statik.multiply(rot, baseNested, mov);
        Statik.subtract(baseFull, mov, mov);
        
        if (storeDataCov != null && storeDataMu != null) setDataDistribution(storeDataCov,storeDataMu); 
        return true;
    }

    /**
     * Computes vector times an orthogonal matrix Q such that Q (I,I,...,I) = (I,0,...,0) for n identity matrices of size m x m, and such that a 
     * block diagonal matrix with blocks of size m x m is commuting with Q.
     * 
     * The matrix Q consists of Householder reflections; the first row is 1/sqrt(n), the i-th row has i-1 zeros, 
     * then -sqrt(k/(k+1)), 1/sqrt(k*(k+1)) ... for k = n-(i-1) entries.  
     * 
     * @param n
     * @return
     */
    public static void multiplyWithMLOrthogonalTransformation(int anzGroups, int anzObs, double[] vec, double[] erg) {
        Statik.setToZero(erg);
        for (int v=0; v<anzObs; v++) {
            double f0 = Math.sqrt(anzGroups);
            erg[v] = 0; for (int i=0; i<anzGroups; i++) erg[v] += vec[v+i*anzObs] / f0;
            for (int i=1; i<anzGroups; i++) {
                int k=anzGroups-i;
                       f0 = Math.sqrt((double)k / (double)(k+1));
                double f1 = Math.sqrt(1.0 / (double)(k*(k+1)));
                erg[v+i*anzObs] -= f0*vec[v+(i-1)*anzObs];
                for (int j=i; j<anzGroups; j++) erg[v+i*anzObs] += vec[v+j*anzObs] * f1;
            }
        }
    }

    public void setDataWithoutPassingToSubmodels(double[][] data) {
        this.data = data;
        this.anzPer = (data==null?0:data.length);        
    }

    public static boolean isMissing(String value) {
        if (value.length()==0 || 
                value.toLowerCase().equals("nan") || 
                value.toLowerCase().equals("na") || 
                value.toLowerCase().equals("miss") || 
                value.toLowerCase().equals("missing") || 
                value.equals(".") ||
                value.toLowerCase().equals("x")) return true;
        try {
            double val = Double.parseDouble(value);
            if (isMissing(val)) return true;
        } catch (Exception e) {}
        return false;
    }
    
    public static boolean isMissingOrNumber(String value) {
        if (isMissing(value)) return true;
        try {
            Double.parseDouble(value);
            return true;
        } catch (Exception e) {}
        return false;
    }

    public Model removeObservation(int[] out) {
        int[] copy = Statik.copy(out);
        Arrays.sort(copy);
        Model erg = this;
        for (int i=0; i<copy.length; i++) erg = erg.removeObservation(copy[i]-i);
        return erg;
    }

    public void createParameterNames() {
        paraNames = new String[anzPar];
        for (int i=0; i<anzPar; i++) paraNames[i] = "p"+i;
    }
    /**
     * Computes the derivative of the gradient with respect to all entries of the data covariance matrix. 
     * 
     * @param erg
     * @return
     */
    public double[][] getGradientDerivativeWRTDataCovarianceEntries() {double[][] erg = new double[anzPar][anzVar*(anzVar+1)/2]; return getGradientDerivativeWRTDataCovarianceEntries(erg);}
    public double[][] getGradientDerivativeWRTDataCovarianceEntries(double[][] erg) {
        sigInv = Statik.ensureSize(sigInv, anzVar, anzVar);
        modelSigmaWork = Statik.ensureSize(modelSigmaWork, anzVar, anzVar);
        modelSigmaWork2 = Statik.ensureSize(modelSigmaWork2, anzVar, anzVar);
        erg = Statik.ensureSize(erg, anzPar, anzVar*(anzVar+1)/2);
        
        Statik.invert(sigma, sigInv, modelSigmaWork);
        for (int i=0; i<anzPar; i++) {
            this.computeMatrixTimesSigmaDev(i, sigInv, modelSigmaWork2);
            Statik.multiply(modelSigmaWork2, sigInv, modelSigmaWork);

            int l=0;
            for (int j=0; j<anzVar; j++) for (int k=j; k<anzVar; k++) {
                erg[i][l++] = (j==k?1:2)*modelSigmaWork[j][k];
            }
        }
        return erg;
    }

    /**
     * When sigma is the current model predicted covariance matrix, this method computes a covariance matrix that will provide the same parameter estimates and is closest to
     * target (assumed to be symmetrical). The resulting matrix will be symmetrical, but not necessarily positive definite. Means are ignored in this method.
     * 
     * The method works by computing the derivative of the gradient wrt. the entries of the covariance matrix. Then, a shift for the data matrix is computed that is in the 
     * kernel of this matrix, i.e., that doesn't change the gradient (which is zero). In this way, the parameters remain unchanged. 
     * 
     * @param target
     * @param erg
     * @return
     */
    public double[][] getCovarianceMatrixWithSameModelFitClosestTo(double[][] target) {double[][] erg = new double[anzVar][anzVar]; return getCovarianceMatrixWithSameModelFitClosestTo(target, erg);}
    public double[][] getCovarianceMatrixWithSameModelFitClosestTo(double[][] target, double[][] erg) {
        erg = Statik.ensureSize(erg, anzVar, anzVar);
        int anzStats = anzVar*(anzVar+1)/2;
        double[] differenceVector = new double[anzStats];
        int l=0; for (int i=0; i<anzVar; i++) for (int j=i; j<anzVar; j++) differenceVector[l++] = target[i][j] - sigma[i][j];
        double[][] gradientDev = getGradientDerivativeWRTDataCovarianceEntries();
        double[] projectionVector = Statik.projectOnKernel(gradientDev, differenceVector);
        
//        double[] check = Statik.multiply(gradientDev, projectionVector);
        
        l=0; for (int i=0; i<anzVar; i++) for (int j=i; j<anzVar; j++) erg[i][j] = erg[j][i] = sigma[i][j] + projectionVector[l++];
        return erg;
    }

    public boolean isIndirectData() {return isIndirectData;}
    
    public void copyStrategy(Model toCopy) {
        this.strategyUseClassicalOnyx = toCopy.strategyUseClassicalOnyx;       
        this.strategyUseHessian = toCopy.strategyUseHessian;              
        this.strategyUseLineSearch = toCopy.strategyUseLineSearch;              
        this.strategyUseGradientNumerator = toCopy.strategyUseGradientNumerator;              
        this.strategyUseCholeskyFirst = toCopy.strategyUseCholeskyFirst;  
        this.strategyUseOertzenOptimization = toCopy.strategyUseOertzenOptimization;  
        this.strategyCholeskyTolerance = toCopy.strategyCholeskyTolerance;     
        this.strategyMaximalStepFactor = toCopy.strategyMaximalStepFactor;     
        this.strategyReductionPowerOnNegative = toCopy.strategyReductionPowerOnNegative;   
        this.strategyGradientDescentDamping = toCopy.strategyGradientDescentDamping;    
        this.strategyMaxInnerIterations = toCopy.strategyMaxInnerIterations;    
        this.strategyUseWarp = toCopy.strategyUseWarp;
        this.strategyWarpInrun = toCopy.strategyWarpInrun;
        this.strategyWarpMinimalSpeed = toCopy.strategyWarpMinimalSpeed;
        this.strategyAllowNonPDSigma = toCopy.strategyAllowNonPDSigma;
        this.strategyUseEMWithSaturated = toCopy.strategyUseEMWithSaturated;
    }
    
 
    public double getSinglePersonMinusTwoLogLikelihood(double[] datarow) {
        try {sigmaDet = Statik.invert(sigma,sigInv, modelSigmaWork);} catch(RuntimeException e) {sigmaDet = 0;}
        if ((Statik.determinantOfPositiveDefiniteMatrix(sigma)==-1) && (sigmaDet > 0))
        {
            if (logStream != null) logStream.println("Warning: Not positive defnite Sigma, I set ll to NaN.");
            sigmaDet = Double.NaN;
        }
        double erg = anzVar*LNTWOPI + Math.log(sigmaDet);

        for (int i=0; i<anzVar; i++) 
            for (int j=0; j<anzVar; j++)
                erg += sigInv[i][j]*(datarow[i]-mu[i])*(datarow[j]-mu[j]);
        
        if ((logStream!=null) && (erg < 0)) logStream.println("Warning: encountered negative Log Likelihood (possible, but very rare). I accept it as such.");
        
        return erg;
    }
    
    /**
     * Computes a hash key from an integer array.
     * 
     * @param key
     * @return
     */
    protected BigInteger codeKey(int[] key) {
        BigInteger two = new BigInteger("2");
        BigInteger erg = new BigInteger("0"); 
        for (int i=0; i<key.length; i++) erg = erg.add(two.pow(key[i]));
        return erg;
    }

    @SuppressWarnings("unchecked")
    public int[][][] computeMissingPattern(double[][] data) {
        if (data==null) data = this.data;
        int anzPer = data.length; if (anzPer==0) return new int[0][][];
        int anzVar = data[0].length;
        
        // array is one Vector<Integer> (participants with this pattern) and two int[] (available indices and missing indices) 
        Hashtable<BigInteger,Object[]> subsets = new Hashtable<BigInteger,Object[]>();
        
        for (int i=0; i<anzPer; i++) {
            int anzEx = 0; for (int j=0; j<data[i].length; j++) if (!Model.isMissing(data[i][j])) anzEx++;
            int[] available = new int[anzEx], missing = new int[anzVar - anzEx]; 
            int aIx = 0, mIx = 0; for (int j=0; j<data[i].length; j++) 
                if (!Model.isMissing(data[i][j])) available[aIx++]=j; else missing[mIx++]=j;
            BigInteger hash = codeKey(available);
            Object[] triple = subsets.get(hash);
            if (triple == null) {
                triple = new Object[]{new Vector<Integer>(), available, missing}; subsets.put(codeKey(available), triple);}
            ((Vector<Integer>)triple[0]).add(i);
        }
        
        int[][][] erg = new int[subsets.size()][3][];
        int i=0; for (BigInteger hash:subsets.keySet()) {
            Object[] pair = subsets.get(hash);
            erg[i][0] = new int[((Vector<Integer>)pair[0]).size()]; for (int k=0; k<erg[i][0].length; k++) erg[i][0][k] = ((Vector<Integer>)pair[0]).elementAt(k);
            erg[i][1] = (int[])pair[1];
            erg[i][2] = (int[])pair[2];
            i++;
        }
        return erg;
    }
    
    /**
     * Assumes that the input distribution is the covariance matrix of the population, returns a sample covariance matrix for the <code>data</code> field 
     * when imputing random variables for every missing entry. Used iteratively until no more changes occur, this will return the ML estimate of the 
     * saturated model. 
     * 
     * @param   missingPattern for each missingness patterns, three lists, the first containing the participants with this pattern, 
     *          the second the variables available, the third the variables missing.
     * @param   input
     * @return
     */
    public Distribution suggestFullCovarinaceMatrix(Distribution input) {return suggestFullCovarianceMatrix(null, input, null, null);}
    public Distribution suggestFullCovarinaceMatrix(int[][][] missingPattern, Distribution input) {return suggestFullCovarianceMatrix(missingPattern, input, null, null);}
    public Distribution suggestFullCovarinaceMatrix(Distribution input, Distribution erg) {return suggestFullCovarianceMatrix(null, input, erg, null);}
    public Distribution suggestFullCovarianceMatrix(int[][][] missingPattern, Distribution input, Distribution erg, double[][] data) {
        if (data == null) data = this.data;
        int anzPer = data.length; if (anzPer==0) {erg.mean = new double[0]; erg.covariance= new double[0][]; return erg;}
        int anzVar = data[0].length;
        if (missingPattern == null) missingPattern = computeMissingPattern(data);
        int anzGroups = missingPattern.length;
        if (availableMean==null || availableMean.length != anzGroups) availableMean = new double[anzGroups][]; 
        if (missingMean==null || missingMean.length != anzGroups) missingMean = new double[anzGroups][]; 
        if (availableCov==null || availableCov.length != anzGroups) availableCov = new double[anzGroups][][]; 
        if (availableMissingCov==null || availableMissingCov.length != anzGroups) availableMissingCov = new double[anzGroups][][]; 
        if (missingAvailableCov==null || missingAvailableCov.length != anzGroups) missingAvailableCov = new double[anzGroups][][]; 
        if (missingCov==null || missingCov.length != anzGroups) missingCov = new double[anzGroups][][]; 
        if (availableCovInv==null || availableCovInv.length != anzGroups) availableCovInv = new double[anzGroups][][];
        modelMuWork1 = Statik.ensureSize(modelMuWork1, anzVar);
        modelMuWork = Statik.ensureSize(modelMuWork, anzVar);
        if (erg == null) erg = new Distribution(new double[anzVar], new double[anzVar][anzVar]);
        else {Statik.setToZero(erg.mean); Statik.setToZero(erg.covariance);}
        
        for (int i=0; i<anzGroups; i++) {
            int anzAvail = missingPattern[i][1].length, anzMiss = anzVar-anzAvail;
            availableMean[i] = Statik.ensureSize(availableMean[i], anzAvail);
            missingMean[i] = Statik.ensureSize(missingMean[i], anzMiss);
            availableCov[i] = Statik.ensureSize(availableCov[i], anzAvail, anzAvail);
            availableMissingCov[i] = Statik.ensureSize(availableMissingCov[i], anzAvail, anzMiss);
            missingAvailableCov[i] = Statik.ensureSize(missingAvailableCov[i], anzMiss, anzAvail);
            missingCov[i] = Statik.ensureSize(missingCov[i], anzMiss, anzMiss);
            availableCovInv[i] = Statik.ensureSize(availableCovInv[i], anzAvail, anzAvail);
            
            for (int j=0; j<anzAvail; j++) availableMean[i][j] = input.mean[missingPattern[i][1][j]];
            for (int j=0; j<anzMiss; j++) missingMean[i][j] = input.mean[missingPattern[i][2][j]];
            for (int j=0; j<anzAvail; j++) for (int k=0; k<anzAvail; k++) 
                availableCov[i][j][k] = input.covariance[missingPattern[i][1][j]][missingPattern[i][1][k]];
            for (int j=0; j<anzAvail; j++) for (int k=0; k<anzMiss; k++) 
                availableMissingCov[i][j][k] = input.covariance[missingPattern[i][1][j]][missingPattern[i][2][k]];
            for (int j=0; j<anzMiss; j++) for (int k=0; k<anzAvail; k++) 
                missingAvailableCov[i][j][k] = input.covariance[missingPattern[i][2][j]][missingPattern[i][1][k]];
            for (int j=0; j<anzMiss; j++) for (int k=0; k<anzMiss; k++) 
                missingCov[i][j][k] = input.covariance[missingPattern[i][2][j]][missingPattern[i][2][k]];
            
            Statik.invert(availableCov[i], availableCov[i], availableCovInv[i]);        // availableCov now has its inverse
            Statik.multiply(missingAvailableCov[i], availableCov[i], missingAvailableCov[i], modelMuWork1);     // missingAvailableCov now has regression matrix
            for (int j=0; j<anzMiss; j++) for (int k=0; k<anzMiss; k++) 
                for (int l=0; l<anzAvail; l++) missingCov[i][j][k] -= missingAvailableCov[i][j][l]*availableMissingCov[i][l][k]; 
                                                                                        // missingCov now has new missing Covs
            // adding contribution to covariance of missing part
            for (int j=0; j<anzMiss; j++) for (int k=0; k<anzMiss; k++) 
                erg.covariance[missingPattern[i][2][j]][missingPattern[i][2][k]] += missingPattern[i][0].length*missingCov[i][j][k];
            for (int j=0; j<missingPattern[i][0].length; j++) {
                // computing observed minus mean for each available variable
                for (int l=0; l<anzAvail; l++) modelMuWork[l] = data[missingPattern[i][0][j]][missingPattern[i][1][l]]-input.mean[missingPattern[i][1][l]];
                // computing expected value for each missing variable
                for (int k=0; k<anzMiss; k++) {
//                    modelMuWork1[k] = input.mean[missingPattern[i][2][k]];
                    modelMuWork1[k] = 0;
                    for (int l=0; l<anzAvail; l++) modelMuWork1[k] += missingAvailableCov[i][k][l]*modelMuWork[l];
                }
                // adding contribution to mean of missing part and covariance missing/available, for each person...
                for (int k=0; k<anzMiss; k++) {
                    // ... to mean
                    erg.mean[missingPattern[i][2][k]] += modelMuWork1[k]+input.mean[missingPattern[i][2][k]];
                    // ... to covariance missing/missing
                    for (int l=0; l<anzMiss; l++)
                        erg.covariance[missingPattern[i][2][k]][missingPattern[i][2][l]] += modelMuWork1[k]*modelMuWork1[l];
                    // ... to covariance missing/available, and for covariance available/missing
                    for (int l=0; l<anzAvail; l++) {
                        erg.covariance[missingPattern[i][2][k]][missingPattern[i][1][l]] += modelMuWork1[k]*modelMuWork[l];
                        erg.covariance[missingPattern[i][1][l]][missingPattern[i][2][k]] = erg.covariance[missingPattern[i][2][k]][missingPattern[i][1][l]]; 
                    }
                }
                // adding contribution to mean of available part and covariance of available part...
                for (int k=0; k<anzAvail; k++) {
                    erg.mean[missingPattern[i][1][k]] += modelMuWork[k] + input.mean[missingPattern[i][1][k]];
                    for (int l=0; l<anzAvail; l++) erg.covariance[missingPattern[i][1][k]][missingPattern[i][1][l]] += modelMuWork[k]*modelMuWork[l];
                }
            }
        }
        
        for (int j=0; j<anzVar; j++) erg.mean[j] /= anzPer;
        for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) erg.covariance[j][k] = erg.covariance[j][k]/anzPer;
//        for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) erg.covariance[j][k] = erg.covariance[j][k]/anzPer - input.mean[j]*input.mean[k];
//        for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) erg.covariance[j][k] = erg.covariance[j][k]/anzPer - erg.mean[j]*erg.mean[k];
        
        return erg;
    }
    
    /**
     * Corrects the distribution such that the indices <code>ctrl</code> act as control variables on the <code>target</code> variables. Does so by
     * computing the regression matrix b = sigma_{target,ctrl} sigma_{ctrl,ctrl}^{-1} and subtracting b sigma_{ctrl,target) from the covariance matrix
     * sigma_{target,target} and b mu_{ctrl} from mu_{target} 
     * @param dist                  Original distribution, result will be in this distribution.
     * @param anzVar                number of target variables (assumed to be at the beginning)
     * @param startCtrl             start of the control variables
     * @param anzCtrl               number of control variables
     * @param removeContribution    if true, the contriubion of the control variables is added (thereby removed) instead of subtracted
     * @param noRecomputation       if true, the method assumes that the computation of the regression matrix is still valid.
     */
    public void controlVariables(Distribution dist, int anzVar, int startCtrl, int anzCtrl) {controlVariables(dist, anzVar, startCtrl, anzCtrl, false, false);}
    public void controlVariables(Distribution dist, int anzVar, int startCtrl, int anzCtrl, boolean removeContribution, boolean noRecomputation) {
        if (anzCtrl == 0) return;
        if (!noRecomputation) {
            ctrlCov = Statik.ensureSize(ctrlCov, anzCtrl, anzCtrl);
            ctrlCovWork = Statik.ensureSize(ctrlCovWork, anzCtrl, anzCtrl);
            targetCtrlCov = Statik.ensureSize(targetCtrlCov, anzVar, anzCtrl);
            ctrlTargetCov = Statik.ensureSize(ctrlTargetCov, anzCtrl, anzVar);
            ctrlB = Statik.ensureSize(ctrlB, anzVar, anzCtrl);
        
            for (int i=0; i<anzCtrl; i++) for (int j=0; j<anzCtrl; j++) ctrlCov[i][j] = dist.covariance[startCtrl+i][startCtrl+j];
            for (int i=0; i<anzVar; i++) for (int j=0; j<anzCtrl; j++) targetCtrlCov[i][j] = dist.covariance[i][startCtrl+j];
            for (int i=0; i<anzCtrl; i++) for (int j=0; j<anzVar; j++) ctrlTargetCov[i][j] = dist.covariance[startCtrl+i][j];
        
            Statik.invert(ctrlCov, ctrlCov, ctrlCovWork);                   // ctrlCov now has its invers
            Statik.multiply(targetCtrlCov, ctrlCov, targetCtrlCov);         // targetCtrlCov now has the regression matrix
        }
        // subtracting the control contribution from the mean
        for (int i=0; i<anzVar; i++) {
            double v = 0; for (int j=0; j<anzCtrl; j++) v += targetCtrlCov[i][j] * dist.mean[startCtrl+j];
            if (!removeContribution) dist.mean[i] -= v; else dist.mean[i] += v;
        }
        // subtracting the predicted variance from the target variance
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) {
            for (int k=0; k<anzCtrl; k++) 
                if (!removeContribution) dist.covariance[i][j] -= targetCtrlCov[i][k]*ctrlTargetCov[k][j];
                else dist.covariance[i][j] += targetCtrlCov[i][k]*ctrlTargetCov[k][j];
        }
    }
    
    public Distribution estimateSaturatedModel(double[][] data, double EPS, Distribution starting) {
        if (data == null) data = this.data;
        int anzPer = data.length; if (anzPer==0) {return new Distribution();}
        int anzVar = data[0].length;
        int[][][] missingPattern = computeMissingPattern(data);

        Distribution previous = (starting==null?new Distribution(anzVar):starting.copy()) , erg = new Distribution(anzVar);
        double distance = Double.MAX_VALUE;
        while (distance > EPS*EPS) {
            erg = suggestFullCovarianceMatrix(missingPattern, previous, erg, data);
            distance = 0;   
            for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) distance += Math.pow(previous.covariance[i][j]-erg.covariance[i][j],2);
            distance /= (double)(anzVar*anzVar);
            Distribution t = erg; erg = previous; previous = t;
        }
        return erg;
    }
    
    private void createJointDataset() {
        jointData = Statik.ensureSize(jointData, anzPer, anzVar+anzAux+anzCtrl);
        for (int i=0; i<anzPer; i++) {
            int j=0; 
            for (int k=0; k<anzVar; k++) jointData[i][j++] = data[i][k];
            for (int k=0; k<anzAux; k++) jointData[i][j++] = auxiliaryData[i][k];
            for (int k=0; k<anzCtrl; k++) jointData[i][j++] = controlData[i][k];
        }
    }
    
    /**
     * Computes a single optimization step. By default, forwards the call to moveWithOptimalDamping, with EM strategy active, it works by using
     * the EM imputation with random variables. 
     * 
     * 
     * @param EPS
     * @param useMaximumLikelihood
     * @return
     */
    public boolean stepOptimization(double EPS, boolean useMaximumLikelihood) {
        if (!strategyUseEMWithSaturated) return moveWithOptimalDamping(EPS, useMaximumLikelihood);

        boolean erg = moveWithOptimalDamping(EPS, useMaximumLikelihood);
        boolean nowConverged = (history.getSize() >= 2) && 
                (lastGain/Math.min((lastDamping==0.0?1.0:Math.abs(lastDamping)), 1.0) < EPS) &&
                (history.getLastSteplength(history.getSize()-1) <= history.getLastSteplength(history.getSize()-2)) &&
                (lastSteplength < EPS);
        if (nowConverged) {
            isIndirectData=false; 
            lastSteplength = 0.0;
            for (int i=0; i<anzPar; i++) lastSteplength += (emMethodLastPosition[i] - position[i])*(emMethodLastPosition[i] - position[i]);
            lastSteplength = Math.sqrt(lastSteplength/anzPar);
            double ll = getMinusTwoLogLikelihood();
            lastGain = emMethodLastFit - (useMaximumLikelihood?ll:ls);
            if (lastGain < 0) {
                System.out.println("negative gain.");
            }
            Statik.copy(position, emMethodLastPosition);
            emMethodLastFit = (useMaximumLikelihood?ll:ls);
            if (lastGain < EPS && lastSteplength < EPS)
                emMethodIsConverged = true;
            else {
                isIndirectData=true;
                Statik.copy(distributionPositionEM.covariance, distributionWork.covariance);
                Statik.copy(distributionPositionEM.mean, distributionWork.mean);
                for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) distributionWork.covariance[i][j] = sigma[i][j];
                for (int i=0; i<anzVar; i++) distributionWork.mean[i] = mu[i];
                controlVariables(distributionWork, anzVar, anzVar+anzAux, anzCtrl, true, true);
                suggestFullCovarianceMatrix(missingPattern, distributionWork, distributionPositionEM, jointData);
                controlVariables(distributionPositionEM, anzVar, anzVar+anzAux, anzCtrl);
                for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) dataCov[i][j] = distributionPositionEM.covariance[i][j];
                for (int i=0; i<anzVar; i++) dataMean[i] = distributionPositionEM.mean[i];
                try {computeMomentsFromDataCovarianceAndMean();} catch (Exception e) {erg = false;}
            }
        }
        stepsEM++;
        return erg;
    }

    public double[] estimateMLByEM(double[] starting, double EPS) {
        strategyUseEMWithSaturated = true;
        emMethodIsConverged = false;
        initEstimation(starting, true);
        while (emMethodIsConverged == false)
            stepOptimization(EPS, true);
        return position;
    }

    public boolean hasAuxiliaryOrControl() {
        return anzAux > 0 || anzCtrl > 0;
    }

    public double getFit() {
        if (fitFunction == Objective.Leastsquares) return ls;
        if (fitFunction == Objective.maximumLikelihood && strategyUseEMWithSaturated) return emMethodLastFit;
        if (fitFunction == Objective.maximumLikelihood && !strategyUseEMWithSaturated) return ll;
        return Double.NaN;
    }

    /**
     * Computes the standard errors of all parameters using the second derivative of the likelihood, provided it exists; otherwise, NaN is returned.
     * 
     * @return parameter standard errors
     */
    public double[] getStandardErrors() {
        double[][] hessianInverse = new double[anzPar][anzPar];
        try {
            hessianInverse = Statik.invert(llDD);
        } catch (Exception e) {
            Statik.setTo(hessianInverse, Double.NaN);
        }
        double[] erg = new double[anzPar];
        for (int i=0; i<anzPar; i++) erg[i] = (hessianInverse[i][i] < 0? Double.NaN:Math.sqrt(2*hessianInverse[i][i]));
        return erg;
        
    }
}
