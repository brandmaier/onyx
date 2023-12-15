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

import java.io.*;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import engine.Statik;



/**
 * @author timo
 * 
 * Creates missingness covariance matrices locally. Difficult to use on complex -2ll derivatives. 
 * 
 * 
 * 
 */
public class MissingDataModelNew extends MultiGroupModel 
{
      Model fullModel;
      int[][] observation;
      
      public double[][][] dataCovs;
      public double[][] dataMeans;
      
      public MissingDataModelNew(Model fullModel)
      {
          super();
          this.fullModel = fullModel;
          anzGroups = 0;
          observation = new int[0][];
          this.anzPar = fullModel.anzPar;
          this.anzVar = fullModel.anzVar;
          this.position = new double[anzPar];
          this.createParameterNames();
      }
      
      public MissingDataModelNew(Model fullModel, Model[] submodels, int[][] observation, double[] startingValues) {
          super (submodels, startingValues, fullModel.anzVar);
          this.fullModel = fullModel;
          this.observation = observation;
          this.anzPer = 0; for (int i=0; i<submodels.length; i++) anzPer += submodels[i].anzPer;
          this.copyStrategy(fullModel);
      }
      
      public MissingDataModelNew copy() {
          MissingDataModelNew copy = new MissingDataModelNew(this.fullModel.copy());
          copy.setData(this.data);
          return copy;
      }
/*
      private BigInteger codeKey(int[] key) {
          BigInteger two = new BigInteger("2");
          BigInteger erg = new BigInteger("0"); 
          for (int i=0; i<key.length; i++) erg = erg.add(two.pow(key[i]));
          return erg;
      }
  */    
    public void setData(double[][] data)
    {
        this.data = data; anzPer = data.length; if (anzPer==0) return;
        
        Hashtable<BigInteger,Vector<double[]>> hash = new Hashtable<BigInteger,Vector<double[]>>();
        Hashtable<BigInteger,int[]> keyHash = new Hashtable<BigInteger,int[]>();
        Hashtable<BigInteger,Vector<Integer>> foreignKeyLists = new Hashtable<BigInteger, Vector<Integer>>();
        
        for (int i=0; i<anzPer; i++) {
            int anzEx = 0; for (int j=0; j<data[i].length; j++) if (!Model.isMissing(data[i][j])) anzEx++;
            if (anzEx > 0) {
                int[] key = new int[anzEx]; double[] redDataRow = new double[anzEx];
                int p = 0; for (int j=0; j<data[i].length; j++) if (!Model.isMissing(data[i][j])) {key[p] = j; redDataRow[p] = data[i][j]; p++;}
                Vector<double[]> subData = null;
                if (hash.containsKey(codeKey(key))) subData = hash.get(codeKey(key)); else {
                    subData = new Vector<double[]>(); hash.put(codeKey(key), subData); keyHash.put(codeKey(key), key); 
                    foreignKeyLists.put(codeKey(key), new Vector<Integer>());}
                subData.add(redDataRow);
                foreignKeyLists.get(codeKey(key)).add(i);
            }
        }
        
        anzGroups = hash.size(); observation = new int[anzGroups][]; 
        submodel = new Model[anzGroups]; dataMeans = new double[anzGroups][]; dataCovs = new double[anzGroups][][];
        int p = 0; for (BigInteger obsKey:hash.keySet()) {
            int[] obs = keyHash.get(obsKey);
            observation[p] = obs;
            Arrays.sort(obs);
            int[] out = new int[fullModel.anzVar - obs.length];
            int outC = 0, outNr = 0; for (int i=0; i<obs.length; i++) {while (outNr < obs[i]) out[outC++] = outNr++; outNr++;}  
            while (outNr < fullModel.anzVar) out[outC++] = outNr++;
            
            Vector<double[]> v = hash.get(obsKey);
            double[][] subData = new double[v.size()][]; for (int i=0; i<v.size(); i++) subData[i] = v.elementAt(i);
            dataMeans[p] = new double[obs.length]; dataCovs[p] = new double[obs.length][obs.length];
            Statik.covarianceMatrixAndMeans(subData, dataMeans[p], dataCovs[p]);
            
            Vector<Integer> foreignKeyList = foreignKeyLists.get(obsKey);
            int[] foreignKeys = new int[v.size()]; for (int i=0; i<v.size(); i++) foreignKeys[i] = foreignKeyList.elementAt(i);
            
            submodel[p] = fullModel.removeObservation(out);
            submodel[p].setData(subData, foreignKeys);
            
            p++;
        }
        
        this.updateTranslationTable();
    }
    
    public double[][] getData() {return this.data;}

    public int getAnzPer() {return anzPer;}

    public double[][] createData (int anzPersons) {return createData(anzPersons, null);}
    public double[][] createData (int anzPersons, double[] values) {
        fullModel.setParameter(values==null?getParameter():values); double[][] data =  fullModel.createData(anzPersons); setData(data); return data; 
    }
    
    /** creates data and then adds missings; missing at any variable behind firstDrop has the given probability, 
     * any consequent variables are automatically dropped. 
     */
    public double[][] createDataWithDropout(int anzPersons, int firstDrop, double dropoutProbability) {
        createData(anzPersons);
        for (int i=0; i<anzPer; i++) {
            boolean dropped = false;
            for (int j=firstDrop; j<anzVar; j++) {
                if (rand.nextDouble() < dropoutProbability) dropped = true;
                if (dropped) data[i][j] = MISSING;
            }
        }
        setData(data);
        return data; 
    }
    /** creates data and then adds missings; missing at any variable behind has given probability 
     */
    public double[][] createDataWithRandomMissing(int anzPersons, double dropoutProbability) {
        createData(anzPersons);
        for (int i=0; i<anzPer; i++) {
            for (int j=0; j<anzVar; j++) {
                if (rand.nextDouble() < dropoutProbability) data[i][j] = MISSING;
            }
        }
        setData(data);
        return data; 
    }
    
    @Override
    public void computeLeastSquaresDerivatives(double[] value, boolean recomputeMuAndSigma) {
        if (recomputeMuAndSigma) evaluateMuAndSigma();
        super.computeLeastSquaresDerivatives(value, false);
        this.sigmaDet = fullModel.sigmaDet;
    }

    @Override
    public void computeLogLikelihoodDerivatives(double[] value, boolean recomputeMuAndSigma) {
        if (recomputeMuAndSigma) evaluateMuAndSigma();
        super.computeLogLikelihoodDerivatives(value, false);
        this.sigmaDet = fullModel.sigmaDet;
    }
    
    public double getKulbackLeibler(double[] saturatedMean, double[][] saturatedCov) {
        fullModel.evaluateMuAndSigma(position);
        return Statik.getKulbackLeiblerNormal(saturatedMean, saturatedCov, fullModel.mu, fullModel.sigma);
    }
    
    /** Computes the minus two log likelihood difference between the independent model and the saturated model. **/
    @Deprecated
    public double getIndependentChisqr() {
        double erg = 0.0;
        fullModel.setParameter(position);
        Statik.covarianceMatrixAndMeans(data, fullModel.dataMean, fullModel.dataCov, Model.MISSING);
        for (int i=0; i<anzGroups; i++) {
            submodel[i].sigma = Statik.submatrix(fullModel.sigma, observation[i], observation[i]);
            double value = 0;
            value -= Statik.logDeterminantOfPositiveDefiniteMatrix(submodel[i].sigma);
            for (int j=0; j<submodel[i].anzVar; j++) value += submodel[i].sigma[j][j]/fullModel.dataCov[observation[i][j]][observation[i][j]];
            value -= submodel[i].anzVar;
            for (int j=0; j<submodel[i].anzVar; j++) value += Math.log(fullModel.dataCov[observation[i][j]][observation[i][j]]);
            for (int j=0; j<submodel[i].anzVar; j++) {
                double v = (fullModel.dataMean[observation[i][j]]-fullModel.mu[observation[i][j]]); 
                value += v*v/fullModel.dataCov[observation[i][j]][observation[i][j]];
            }
            erg += submodel[i].anzPer * value;
        }
        return erg;
    }
    
    @Override
    public double getIndependentLL() {
        fullModel.setParameter(position);
        double[][] dataCov = new double[fullModel.anzVar][fullModel.anzVar];
        double[] dataMean = new double[fullModel.anzVar];
        Statik.covarianceMatrixAndMeans(data, dataMean, dataCov, Model.MISSING);
        for (int i=0; i<anzGroups; i++) {
            Statik.setToZero(submodel[i].sigma); 
            for (int j=0; j<submodel[i].anzVar; j++) submodel[i].sigma[j][j] = dataCov[observation[i][j]][observation[i][j]]; 
            for (int j=0; j<submodel[i].anzVar; j++) submodel[i].mu[j] = dataMean[observation[i][j]];
        }
        double erg = getMinusTwoLogLikelihood(null, false);
        evaluateMuAndSigma(position);
        return erg;
    }
    
    @Override
    public double getIndependentKulbackLeibler() {
        fullModel.setParameter(position);
        return fullModel.getIndependentKulbackLeibler();
    }

    public String getMissingnessPatternDescription() {
        String erg = "";
        for (int i=0; i<anzGroups; i++) {
            erg += Statik.matrixToString(observation[i])+"\t: "+submodel[i].anzPer+"\r\n";
        }
        return erg;
    }
    
    @Override
    public double getSigmaDet() {
        fullModel.evaluateMuAndSigma(position);
        return Statik.determinantOfPositiveDefiniteMatrix(fullModel.sigma);
    }
    
    @Override
    public int getRestrictedDF() {
        return getObservedStatistics() - anzPar;
    }
    
    @Override
    public int getObservedStatistics() {
        return fullModel.getObservedStatistics();
    }

    @Override
    protected void updateTranslationTable() {
        transTable = new int[anzPar][submodel.length][];
        for (int nr=0; nr<anzPar; nr++)
            for (int i=0; i<submodel.length; i++) transTable[nr][i] = new int[]{i,nr}; 
    }

    
/*
    @Override
    public void evaluateMuAndSigma(double[] values) {
        if (values != null) this.setParameter(values);
        fullModel.evaluateMuAndSigma(values);
        this.sigma = fullModel.sigma;
        this.mu = fullModel.mu;
        for (int i=0; i<anzGroups; i++) {
            for (int j=0; j<observation[i].length; j++) {
                submodel[i].mu[j] = fullModel.mu[observation[i][j]];
                for (int k=0; k<observation[i].length; k++) submodel[i].sigma[j][k] = fullModel.sigma[observation[i][j]][observation[i][k]];
            }
        }
    }
*/
}
