/*
 * Created on 26.04.2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package engine.backend;

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import engine.Statik;

//import arithmetik.AnalyticalFunction;

//import silvers.Statik;

/**
 * @author timo
 */
public class MultiGroupModel extends Model 
{
    static int NOPARAMETER = -1, NOTCOMPUTED = -1;

    static final double LNTWOPI = Math.log(2*Math.PI);

      int anzGroups;
      
      Model[] submodel;
      
      // Some working variables
      double[][] sigInv;
      
      public double[][] fisherInformationMatrix;
      
      public double lastSaturated;
      public double lastIndependend;
      public double lastEqualCovariancesSaturated;
      public double[] lastEstimate;
      
      // for each parameter a list of tuples containing submodel and parameter number in submodel.
      protected int[][][] transTable;
      
      public MultiGroupModel() {}
      
      public MultiGroupModel(Model[] submodel, int anzVar)
      {
         this.submodel = submodel;
         this.anzGroups = submodel.length;
//         this.anzVar = submodel[0].anzVar;

         collectParameterNames();
      }

      public MultiGroupModel(Model[] submodel, double[] startingValues, int anzVar)
      {
         this.submodel = submodel;
         this.anzGroups = submodel.length;
         this.anzVar = anzVar;
         
         this.startingValues = startingValues;
         
         collectParameterNames();
         if (submodel.length > 0) copyStrategy(submodel[0]);
      }

      public MultiGroupModel(MultiGroupModel toCopy)
      {
          this.submodel = new Model[toCopy.anzGroups];
          for (int i=0; i<toCopy.anzGroups; i++) this.submodel[i] = toCopy.submodel[i].copy();
          
          this.anzGroups = submodel.length;
          this.anzVar = submodel[0].anzVar;
          this.startingValues = Statik.copy(toCopy.startingValues);

          collectParameterNames();
          if ((toCopy.data!=null) && (toCopy.data.length > 0)) this.setData(toCopy.data);

          computeMoments();
          fisherInformationMatrix = new double[anzPar][anzPar];
      }
      
      protected void collectParameterNames() {
          anzPar = 0;
          Hashtable<String,Integer> hash = new Hashtable<String, Integer>();
          for (int i=0; i<submodel.length; i++) {
              String[] subnames = submodel[i].getParameterNames();
              for (int j=0; j<subnames.length; j++)
                  if (!hash.containsKey(subnames[j])) hash.put(subnames[j], new Integer(anzPar++));
          }
          paraNames = new String[anzPar]; 
          for (String k:hash.keySet()) paraNames[hash.get(k).intValue()] = k;
          updateTranslationTable();
          updatePosition();
      }

      protected void updateTranslationTable() {
          transTable = new int[anzPar][][];
          for (int i=0; i<anzPar; i++) {
              int k = 0; for (int j=0; j<anzGroups; j++) if (submodel[j].containsParameter(paraNames[i])) k++;
              transTable[i] = new int[k][];
              k = 0;
              for (int j=0; j<anzGroups; j++) if (submodel[j].containsParameter(paraNames[i])) 
                  transTable[i][k++] = new int[]{j, submodel[j].getParameterNumber(paraNames[i])};
          }
      }
      
      protected void updatePosition() {
          position = Statik.ensureSize(position, anzPar);
          for (int i=0; i<anzPar; i++)  for (int j=0; j<submodel.length; j++) position[i] = submodel[j].getParameter(paraNames[i]);
      }
      
      protected void removeParameterNumber(int nr) {
          for (int i=0; i<transTable[nr].length; i++) submodel[transTable[nr][i][0]].fixParameter(transTable[nr][i][1]);
          setParameterNames(Statik.subarray(getParameterNames(), nr));
          position = Statik.subvector(position, nr);
          anzPar--;
      }
      
      @Override
      public double getLeastSquares(double[] value)
      {
          ls = 0;
          int anzPer = 0; for (int i=0; i<anzGroups; i++) anzPer += submodel[i].anzPer;
          for (int i=0; i<anzGroups; i++) 
              if (submodel[i].anzPer>1)
                  ls += ((double)submodel[i].anzPer / (double)anzPer)*submodel[i].getLeastSquares(value);
          return ls;        
      }

      @Override
      public void computeLeastSquaresDerivatives(double[] value, boolean recomputeMuAndSigma) {
          if (value != null) setParameter(value); else setParameter(getParameter());
          if (recomputeMuAndSigma) evaluateMuAndSigma(value);
          lsD = Statik.ensureSize(lsD, anzPar);
          lsDD = Statik.ensureSize(lsDD, anzPar, anzPar);
          
          ls = 0; Statik.setToZero(lsD); Statik.setToZero(lsDD); 
          
          int anzPer = 0; for (int i=0; i<anzGroups; i++) anzPer += submodel[i].anzPer;
          for (int i=0; i<anzGroups; i++) 
              if (submodel[i].anzPer>1)
              {
                  double fak = (double)submodel[i].anzPer / (double)anzPer;
                  submodel[i].computeLeastSquaresDerivatives(null, false);
                  ls += fak*submodel[i].ls;
                  for (int j=0; j<submodel[i].paraNames.length; j++) lsD[getParameterNumber(submodel[i].paraNames[j])] += fak*submodel[i].lsD[j]; 
                  for (int j=0; j<submodel[i].paraNames.length; j++) for (int k=0; k<submodel[i].paraNames.length; k++) 
                      lsDD[getParameterNumber(submodel[i].paraNames[j])][getParameterNumber(submodel[i].paraNames[k])] += fak*submodel[i].lsDD[j][k]; 
              }
      }

      @Override
      protected void computeMatrixTimesMuDev(int par, double[][] matrix, double[] erg) {
          Statik.setToZero(erg);
          double[] zw = new double[erg.length];
          for (int i=0; i<anzGroups; i++) {
              submodel[i].computeMatrixTimesMuDev(par, matrix, zw);
              Statik.add(erg, zw, erg);
          }
      }

      @Override
      protected void computeMatrixTimesSigmaDev(int par, double[][] matrix,double[][] erg) {
          Statik.setToZero(erg);
          double[][] zw = Statik.copy(erg);
          for (int i=0; i<anzGroups; i++) {
              submodel[i].computeMatrixTimesSigmaDev(par, matrix, zw);
              Statik.add(erg, zw, erg);
          }
      }

      @Override
      protected void computeMatrixTimesSigmaDevDev(int par1, int par2,double[][] matrix, double[][] erg) {
          Statik.setToZero(erg);
          double[][] zw = Statik.copy(erg);
          for (int i=0; i<anzGroups; i++) {
              submodel[i].computeMatrixTimesSigmaDevDev(par1, par2, matrix, zw);
              Statik.add(erg, zw, erg);
          }
      }

      @Override
      public Model copy() {
          return new MultiGroupModel(this);
      }
      
      @Override
      public Model removeObservation(int obs) {
          MultiGroupModel erg = new MultiGroupModel(this);
          for (int i=0; i<anzGroups; i++) erg.submodel[i] = erg.submodel[i].removeObservation(obs);
          return erg;
      }

      @Override
      public void evaluateMuAndSigma(double[] values) {
          if (values != null) setParameter(values);
          for (int i=0; i<anzGroups; i++) submodel[i].evaluateMuAndSigma(null);          
      }

      @Override
      public double getParameter(int nr) {
          return position[nr];
      }

      public boolean setParameter(int nr, double value) {
          boolean erg = true;
          for (int i=0; i<transTable[nr].length; i++) erg = erg & submodel[transTable[nr][i][0]].setParameter(transTable[nr][i][1], value);
          position[nr] = value;
          return erg;
      }
      public boolean setParameter(double[] value) {
          boolean erg = true;
          for (int i=0; i<value.length; i++) 
              erg = erg & setParameter(i, value[i]);
          return erg;
      }
      
      
      @Override
      public boolean isErrorParameter(int nr) {
          boolean erg = false; for (int i=0; i<anzGroups; i++) if (submodel[i].isErrorParameter(nr)) erg = true;
          for (int i=0; i<anzGroups; i++) if (!submodel[i].isErrorParameter(paraNames[nr]) && submodel[i].containsParameter(paraNames[nr])) erg = false;
          return erg;
      }

      public int maxParNumber()
      {
          return anzPar - 1;
      }
    
    public int getAnzPar() {
        return anzPar;
    }
    public int getAnzVar() {
        return anzVar;
    }
    
    public double[] getParameter() {
        return Statik.copy(position);
    }
    
    public double[][] getData()
    {
        getAnzPer();
        this.data = new double[anzPer][];
        int k=0; for (int i=0; i<anzGroups; i++) for (int j=0; j<submodel[i].anzPer; j++) data[k++] = Statik.copy(submodel[i].data[j]); 
        return data;
    }
    
    public void setData(double[][] data) {int[] anzPers = new int[anzGroups]; for (int i=0; i<anzGroups; i++) anzPers[i] = submodel[i].anzPer; setData(data, anzPers);}
    public void setData(double[][] data, int[] anzPers)
    {
        this.data = data; anzPer = 0; 
        int k = 0;
        for (int i=0; i<anzGroups; i++) {
            double[][] subdata = new double[anzPers[i]][]; 
            for (int j=0; j<anzPers[i]; j++) subdata[j] = Statik.copy(data[k++]);
            submodel[i].setData(subdata);
            anzPer += anzPers[i];
        }
        
        computeMoments();
    }
    
    public void setData(double[][][] data) {
        for (int i=0; i<anzGroups; i++) submodel[i].setData(data[i]);
        getData();
    }

    public int getAnzPer() {this.anzPer = getAnzPerInSubmodels(); return anzPer;}
    public int getAnzPerInSubmodels() {int anzPer = 0; for (int i=0; i<anzGroups; i++) anzPer += submodel[i].anzPer; return anzPer;}

    public double[][] createData (int anzPersons) {return createData(anzPersons, null);}
    public double[][] createData (int anzPersons, double[] values) {return createData(anzPersons, null, null, true);}
    public double[][] createData (int[] anzPersons) {return createData(-1, anzPersons, null, false);}
    public double[][] createData (int[] anzPersons, double[] values) {return createData(-1, anzPersons, values, false);}
    private double[][] createData (int anzPersons, int[] anzMultPersons, double[] values, boolean equalGroups) {
        if (values != null) setParameter(values);
        int anzPG = anzPersons / anzGroups;
        for (int i=0; i<anzGroups; i++) submodel[i].createData( (equalGroups?anzPG + (i < anzPersons % anzGroups?1:0): anzMultPersons[i]));
        return getData();
    }
    
    public void computeMoments() {
        for (int i=0; i<anzGroups; i++) submodel[i].computeMoments();
    }
    
    public double getMinusTwoLogLikelihood(double[] value) {return getMinusTwoLogLikelihood(value, true);}
    public double getMinusTwoLogLikelihood(double[] value, boolean recomputeSigma)
    {
        ll = 0;
        for (int i=0; i<anzGroups; i++) 
            if (submodel[i].anzPer > 0) ll += submodel[i].getMinusTwoLogLikelihood(value, recomputeSigma);
        return ll;        
    }
    
    /**
     *  % override
     */
    public void setDataDistribution() {
        for (int i=0; i<anzGroups; i++) submodel[i].setDataDistribution();
    }
    
    public void computeLogLikelihoodDerivatives(double[] value, boolean recomputeMuAndSigma)
    {
        if (value != null) setParameter(value); else setParameter(getParameter());
        if (recomputeMuAndSigma) evaluateMuAndSigma(value);
        llD = Statik.ensureSize(llD, anzPar);
        llDD = Statik.ensureSize(llDD, anzPar, anzPar);
        
        ll = 0; Statik.setToZero(llD); Statik.setToZero(llDD); 
        
        for (int i=0; i<anzGroups; i++)
        {
            if (submodel[i].anzPer > 0) {
                submodel[i].computeLogLikelihoodDerivatives(null, false);
                ll += submodel[i].ll;
                for (int j=0; j<submodel[i].paraNames.length; j++) llD[getParameterNumber(submodel[i].paraNames[j])] += submodel[i].llD[j]; 
                for (int j=0; j<submodel[i].paraNames.length; j++) for (int k=0; k<submodel[i].paraNames.length; k++) 
                    llDD[getParameterNumber(submodel[i].paraNames[j])][getParameterNumber(submodel[i].paraNames[k])] += submodel[i].llDD[j][k];
            }
        }
    }
    
    /** computes the Fisher Information Matrix in its general form, i.e. as E(g_i * g_j). Coincides with
     * the Hessian if dataCov and dataMean are sigma and mu from the model. 
     * 
     * @param dataCov
     * @param dataMean
     * @param erg
     */
    public double[][] computeFisherMatrix(double[][] dataCov, double[] dataMean) {return computeFisherMatrix(dataCov, dataMean, null);}
    public double[][] computeFisherMatrix(double[][] dataCov, double[] dataMean, double[][] erg) {return computeFisherMatrix(erg);}
    public double[][] computeFisherMatrix() {return computeFisherMatrix(null);}
    public double[][] computeFisherMatrix(double[][] erg) {
        if ((erg==null) || (erg.length!=anzPar) || (erg[0].length!=anzPar)) erg = new double[anzPar][anzPar];
        for (int i=0; i<erg.length; i++) for (int j=0; j<erg.length; j++) erg[i][j] = 0;
        for (int g=0; g<anzGroups; g++) {
            double[][] work = new double[submodel[g].anzPar][submodel[g].anzPar];
            submodel[g].computeFisherMatrix(submodel[g].dataCov, submodel[g].dataMean, work);
            for (int i=0; i<erg.length; i++) for (int j=0; j<erg.length; j++) 
                erg[i][j] += submodel[g].anzPer*work[submodel[g].getParameterNumber(paraNames[i])][submodel[g].getParameterNumber(paraNames[j])];
        }
        return erg;
    }

    @Override
    // Since MultiGroupModel is not a normal model, the method returns false if at least two groups are present.
    public boolean isConstantSingular() {
        if (anzGroups == 0) return true;
        if (anzGroups == 1) return submodel[0].isConstantSingular();
        return false;
    }

    @Override
    public double[] getRandomStartingValues() {return getRandomStartingValues(1000);}
    @Override
    public double[] getRandomStartingValues(double priorvariance) {
        double[] erg = new double[anzPar];
        for (int i=0; i<anzGroups; i++) {
            double[] v = submodel[i].getRandomStartingValues(priorvariance);
            Statik.add(v, erg, erg);
        }
        Statik.multiply(1.0/(double)anzGroups, erg, erg);
        return erg;
    }

    @Override
    public int getObservedStatistics() {
        int stats = 0; 
        for (int i=0; i<anzGroups; i++) stats += submodel[i].getObservedStatistics();
        return stats;
    }

    @Override
    public String[] getVariableNames() {return this.submodel[0].getVariableNames();}
    public int[] getObservedVariables() {return this.submodel[0].getObservedVariables();}
    
//    @Override
//    public double getChisquare() {
//        return Double.NaN;
//    }
//    
//    @Override
//    public double getRMSEA() {
//        return Double.NaN;
//    }
    
//    public double getKulbackLeibler(double[] saturatedMean, double[][] saturatedCov) {
//        return Double.NaN;
//    }
    
    @Override
    public double getSigmaDet() {return Double.NaN;}

}
