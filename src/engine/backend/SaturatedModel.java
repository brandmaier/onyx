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

import engine.Statik;


/**
 * @author timo
 */
public class SaturatedModel extends Model
{
    static int NOPARAMETER = -1;
    static final double LNTWOPI = Math.log(2*Math.PI);
//    static Random rand = new Random(32462346111L);
//    static Random rand = new Random();
    
    public static enum parameterType {sig, mean, inactive};

    int[][] sigPar;
    int[] muPar;
    
  // parameter types
  parameterType[] parType;
  
  // Some working variables
  double[][] facCovWork, sigmaWork, sigmaWork2, sigmaWork3, sigmaWork4, sigmaWork5, structureWork, structureTransWork, loadTransWork1, loadTransWork2;
  double[] muWork, muWork2, muWork3;
  int[] sDevRow, sDevCol, cDevRow, cDevCol, mDev;
  
  public SaturatedModel(int[][] sigPar, int[] muPar, double[][] sigma, double[] mu) {
      anzVar = muPar.length;
      this.sigPar = sigPar; this.muPar = muPar;
      this.sigma = sigma; this.mu = mu;
      setAnzParAndCollectParameter(0);
  }
  public SaturatedModel(int[][] sigPar, int[] muPar) {
      anzVar = muPar.length;
      this.sigPar = sigPar; this.muPar = muPar;
      this.sigma = new double[anzVar][anzVar]; mu = new double[anzVar];
      setAnzParAndCollectParameter(0);
  }
  
      public SaturatedModel(int anzVar)
      {
          this.anzVar = anzVar; this.sigPar = new int[anzVar][anzVar]; this.muPar = new int[anzVar];
          for (int i=0; i<anzVar; i++) muPar[i] = i;
          int c = anzVar; for (int i=0; i<anzVar; i++) for (int j=i; j<anzVar; j++) sigPar[i][j] = sigPar[j][i] = c++;
          sigma = Statik.identityMatrix(anzVar); mu = new double[anzVar];
          setAnzParAndCollectParameter(0);
          getParameter();
      }
      
      public SaturatedModel(SaturatedModel toCopy)
      {
          this(Statik.copy(toCopy.sigPar),Statik.copy(toCopy.muPar));
      }
      
      public static void estimateCovarianceAndMean(double[][] data, double[][] cov, double[] mu, double MISSING) {
          int anzVar = data[0].length;
          Statik.covarianceMatrixAndMeans(data, mu, cov, MISSING);
          int anzOk = 0; for (int i=0; i<anzVar; i++) if (cov[i][i] != 0) anzOk++;
          double[][] subData = new double[data.length][anzOk]; 
          int k=0; for (int i=0; i<anzVar; i++)
              if (cov[i][i] > 0) {
                  for (int j=0; j<data.length; j++) subData[j][k] = data[j][i];
                  k++;
              }
          MissingDataModel m = new MissingDataModel(new SaturatedModel(anzOk));
          double oldMiss = Model.MISSING;
          Model.MISSING = MISSING;
          m.setData(subData);
          m.fullModel.setParameter(m.estimateML());
          k=0; for (int i=0; i<anzVar; i++) 
              if (cov[i][i] > 0) {
                  mu[i] = m.fullModel.mu[k];
                  int k2=0; for (int j=0; j<anzVar; j++) if (cov[j][j] > 0) cov[i][j] = m.fullModel.sigma[k][k2++];
                  k++;
              }
          Model.MISSING = oldMiss;
      }
      
      public void setAnzParAndCollectParameter(int newAnzPar)
      {
          if (newAnzPar == -1) newAnzPar = anzPar;
          int maxPar = maxParNumber(); anzPar = Math.max(maxPar+1, newAnzPar);
          
          parType = new parameterType[anzPar]; for (int i=0; i<anzPar; i++) parType[i] = parameterType.inactive;
          
          for (int i=0; i<anzVar; i++)
              for (int j=0; j<anzVar; j++) 
                  if (sigPar[i][j]!=NOPARAMETER) parType[sigPar[i][j]] = parameterType.sig;
          for (int i=0; i<anzVar; i++) if (muPar[i] != NOPARAMETER) parType[muPar[i]] = parameterType.mean;
          
      }
      
      public int maxParNumber()
      {
          int erg = NOPARAMETER;
          for (int i=0; i<anzVar; i++)
              for (int j=0; j<anzVar; j++) erg = Math.max(sigPar[i][j], erg);
          for (int i=0; i<anzVar; i++) erg = Math.max(muPar[i], erg);
          return erg;
      }
      
      
      public void renumberParameter(int oldNumber, int newNumber)
      {
          for (int i=0; i<anzVar; i++)
              for (int j=0; j<anzVar; j++)
                  if (sigPar[i][j] == oldNumber) sigPar[i][j] = newNumber;
          for (int i=0; i<anzVar; i++) if (muPar[i] == oldNumber) muPar[i] = newNumber;
          setAnzParAndCollectParameter((newNumber > anzPar?newNumber:anzPar));
      }
    
      public int getAnzPar() {return anzVar*(anzVar+1)/2 + anzVar;}
      
    public int getAnzVar() {return anzVar;}

    public void computeMatrixTimesSigmaDev(int pnr, double[][] matrix, double[][] erg) {
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) erg[i][j] = 0;
        if (parType[pnr]==parameterType.sig)
        {
            sigmaWork4 = Statik.ensureSize(sigmaWork4, anzVar, anzVar);
            for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) sigmaWork4[i][j] = (sigPar[i][j]==pnr?1:0);
            Statik.multiply(matrix, sigmaWork4, erg);
        }        
    }

    protected void computeMatrixTimesMuDev(int pnr, double[][] matrix, double[] erg) {
        for (int i=0; i<anzVar; i++) erg[i] = 0;
        if (parType[pnr]==parameterType.mean)
        {
            muWork3 = Statik.ensureSize(muWork3, anzVar);
            for (int i=0; i<anzVar; i++) muWork3[i] = (muPar[i]==pnr?1:0);
            Statik.multiply(matrix, muWork3, erg);
        }
    }
    
    public void computeMatrixTimesSigmaDevDev(int pnr1, int pnr2, double[][] matrix, double[][] erg) {
        for (int i=0; i<anzVar; i++) 
            for (int j=0; j<anzVar; j++) erg[i][j] = 0;
    }

    public boolean setParameter(int nr, double value)
    {
        if (nr >= anzPar) return false;
        if (parType[nr]==parameterType.sig)
            for (int i=0; i<anzVar; i++)
                for (int j=0; j<anzVar; j++)
                    if (sigPar[i][j]==nr) sigma[i][j] = value; 
        if (parType[nr]==parameterType.mean) for (int i=0; i<anzVar; i++) if (muPar[i] == nr) mu[i] = value;
        return true;
    }
    
    
    public static SaturatedModel fixParameter(SaturatedModel in, int nr, double value) {SaturatedModel erg = new SaturatedModel(in); erg.fixParameter(nr,value); return erg;}
    public void inactivateParameter(int nr)
    {
        if (parType[nr]==parameterType.sig)
            for (int i=0; i<anzVar; i++)
                for (int j=0; j<anzVar; j++)
                    if (sigPar[i][j]==nr) sigPar[i][j] = -1;

        if (parType[nr]==parameterType.mean) for (int i=0; i<anzVar; i++) if (muPar[i]==nr) muPar[i] = -1; 
        setAnzParAndCollectParameter(-1);
    }
    
    public void evaluateMuAndSigma(double[] values)
    {
        setParameter((values==null?getParameter():values));
    }
            
    public double getMinusTwoLogLikelihood(double[] value) {return getMinusTwoLogLikelihood(value, true);}
    public double getMinusTwoLogLikelihood(double[] value, boolean recomputeSigma) 
    {
        if ((sigmaWork==null) || (sigmaWork.length!=anzVar) || (sigmaWork[0].length!=anzVar)) sigmaWork = new double[anzVar][anzVar];
        if ((sigInv==null) || (sigInv.length!=anzVar) || (sigInv[0].length!=anzVar)) sigInv = new double[anzVar][anzVar];

        if (value != null) setParameter(value);
        if (recomputeSigma) evaluateMuAndSigma(value);
        
        double det = Statik.invert(sigma,sigInv, sigmaWork);
        
        ll = anzPer*anzVar*LNTWOPI + anzPer*Math.log(det);
        for (int i=0; i<anzVar; i++) 
            for (int j=0; j<anzVar; j++)
                ll += (xBiSum[i][j]-mu[i]*xsum[j]-xsum[i]*mu[j]+mu[i]*mu[j]*anzPer)*sigInv[i][j];
        
        return ll;
    }


//    public void computeLogLikelihoodDerivatives(double[] value) {computeLogLikelihoodDerivatives(value,true);}
    public void computeLogLikelihoodDerivatives(double[] value, boolean recomputeSigma)
    {
        if ((sigmaWork==null) || (sigmaWork.length!=anzVar) || (sigmaWork[0].length!=anzVar)) sigmaWork = new double[anzVar][anzVar];
        if ((sigmaWork2==null) || (sigmaWork2.length!=anzVar) || (sigmaWork2[0].length!=anzVar)) sigmaWork2 = new double[anzVar][anzVar];
        if ((sigmaWork3==null) || (sigmaWork3.length!=anzVar) || (sigmaWork3[0].length!=anzVar)) sigmaWork3 = new double[anzVar][anzVar];
        muWork = Statik.ensureSize(muWork, anzVar);
        if ((sigInv==null) || (sigInv.length!=anzVar) || (sigInv[0].length!=anzVar)) sigInv = new double[anzVar][anzVar];
        llD = Statik.ensureSize(llD, anzPar);
        llDD = Statik.ensureSize(llDD, anzPar, anzPar);
        
        if (value != null) setParameter(value);
        if (recomputeSigma) evaluateMuAndSigma(value);
        
        double det = Statik.invert(sigma,sigInv, sigmaWork);
        
        ll = anzPer*anzVar*LNTWOPI + anzPer*Math.log(det);
        for (int i=0; i<anzVar; i++) 
            for (int j=0; j<anzVar; j++)
                ll += (xBiSum[i][j]-mu[i]*xsum[j]-xsum[i]*mu[j]+mu[i]*mu[j]*anzPer)*sigInv[i][j];
        
        for (int ap=0; ap<anzPar; ap++)
        {
            llD[ap] = 0.0;
            if (parType[ap]==parameterType.sig) {
                computeSigmaInvDev(ap, sigmaWork);
                for (int i=0; i<anzVar; i++) llD[ap] += sigmaWork[i][i];
                llD[ap] *= anzPer;
                Statik.multiply(sigmaWork, sigInv, sigmaWork2);
                for (int i=0; i<anzVar; i++) 
                    for (int j=0; j<anzVar; j++) llD[ap] -= (xBiSum[i][j]-mu[i]*xsum[j]-xsum[i]*mu[j]+mu[i]*mu[j]*anzPer)*sigmaWork2[i][j];
            } else if (parType[ap]==parameterType.mean) {
                computeMatrixTimesMuDev(ap, sigInv, muWork);
                for (int i=0; i<anzVar; i++) llD[ap] += 2 * muWork[i] * (mu[i]*anzPer - xsum[i]); 
            }
        }
        
        for (int ap1=0; ap1<anzPar; ap1++)
            for (int ap2=ap1; ap2<anzPar; ap2++)
            {
                llDD[ap1][ap2] = 0;
                if (parType[ap1]==parameterType.mean && parType[ap2]==parameterType.mean) {
                    for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) if (muPar[i]==ap1 && muPar[j]==ap2) llDD[ap1][ap2] += 2*anzPer*sigInv[i][j]; 
                } else if (parType[ap1]==parameterType.sig && parType[ap2]==parameterType.sig) {
                    computeSigmaInvDev(ap1, sigmaWork);
                    computeSigmaInvDev(ap2, sigmaWork2);
                    Statik.multiply(sigmaWork,sigmaWork2, sigmaWork3);
                    for (int i=0; i<anzVar; i++) llDD[ap1][ap2] += sigmaWork3[i][i];
                    llDD[ap1][ap2] *= -anzPer;
                    Statik.multiply(sigmaWork3,sigInv,sigmaWork);
                    for (int i=0; i<anzVar; i++) 
                        for (int j=0; j<anzVar; j++) llDD[ap1][ap2] += 2*(xBiSum[i][j]-mu[i]*xsum[j]-xsum[i]*mu[j]+mu[i]*mu[j]*anzPer)*sigmaWork[i][j];
                } else {
                    int p1 = ap1, p2 = ap2; 
                    if (parType[p1] == parameterType.mean) {p1 = ap2; p2 = ap1;}
                    computeSigmaInvDev(p1, sigmaWork);
                    computeMatrixTimesMuDev(p2, sigInv, muWork);
                    for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) llDD[ap1][ap2] -= 2*(mu[i]*anzPer - xsum[i])*sigmaWork[i][j]*muWork[j];
                }
                llDD[ap2][ap1] = llDD[ap1][ap2];
            }
    }
    

    public double getParameter(int pnr) {
        for (int i=0; i<anzVar; i++) if (pnr==muPar[i]) return mu[i];
        for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) if (sigPar[i][j]==pnr) return sigma[i][j];
        return Double.NaN;
    }
    
    public double getLeastSquares(double[] values) {
        evaluateMuAndSigma(values);
        ls = 0;
        for (int i=0; i<anzVar; i++)
            for (int j=0; j<anzVar; j++) ls += 0.5*(sigma[i][j] - dataCov[i][j])*(sigma[i][j] - dataCov[i][j]);
        for (int i=0; i<anzVar; i++) ls += 0.5*(mu[i] - dataMean[i])*(mu[i] - dataMean[i]);
        return ls;
        
    }

    @Override
    public void computeLeastSquaresDerivatives(double[] value, boolean recomputeMuAndSigma) {
        lsD = Statik.ensureSize(lsD, anzPar);
        lsDD = Statik.ensureSize(lsDD, anzPar, anzPar);
        
        if (value != null) setParameter(value);
        if (recomputeMuAndSigma) evaluateMuAndSigma(value);
        
        for (int i=0; i<anzVar; i++)
            for (int j=0; j<anzVar; j++) ls += 0.5*(sigma[i][j] - dataCov[i][j])*(sigma[i][j] - dataCov[i][j]);
        for (int i=0; i<anzVar; i++) ls += 0.5*(mu[i] - dataMean[i])*(mu[i] - dataMean[i]);
        
        for (int ap=0; ap<anzPar; ap++)
        {
            lsD[ap] = 0.0;
            if (parType[ap]==parameterType.sig) {
                for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar ;j++) if (sigPar[i][j]==ap) lsD[ap] += sigma[i][j] - dataCov[i][j];
            } else if (parType[ap]==parameterType.mean) {
                for (int i=0; i<anzVar; i++) if (muPar[i]==ap) lsD[ap] += (mu[i]-dataMean[i]);
            }
        }
        
        for (int ap1=0; ap1<anzPar; ap1++)
            for (int ap2=ap1; ap2<anzPar; ap2++)
            {
                lsDD[ap1][ap2] = 0;
                if (parType[ap1]==parameterType.mean && parType[ap2]==parameterType.mean) {
                    for (int i=0; i<anzVar; i++) if (muPar[i]==ap1 && muPar[i] == ap2) lsDD[ap1][ap2] += 1; 
                } else if (parType[ap1]==parameterType.sig && parType[ap2]==parameterType.sig) {
                    for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) 
                        if (sigPar[i][j]==ap1 && sigPar[i][j]==ap2) lsDD[ap1][ap2] += 1; 
                } 
                lsDD[ap2][ap1] = lsDD[ap1][ap2];
            }
    }


    @Override
    public Model copy() {
        return new SaturatedModel(this);
    }

    @Override
    public boolean isErrorParameter(int nr) {
        boolean erg = false; for (int i=0; i<anzVar; i++) if (sigPar[i][i] == nr) erg = true;
        for (int i=0; i<anzVar; i++) for (int j=i+1; j<anzVar; j++) if (sigPar[i][j] == nr) erg = false;
        return erg;
    }

    @Override
    public SaturatedModel removeObservation(int obs) {
        int[][] nSigPar = new int[anzVar-1][anzVar-1];
        for (int i=0; i<anzVar-1; i++) for (int j=0; j<anzVar-1; j++) nSigPar[i][j] = sigPar[(i>=obs?i+1:i)][(j>=obs?j+1:j)];
        int[] nMuPar = new int[anzVar-1];
        for (int i=0; i<anzVar-1; i++) nMuPar[i] = muPar[(i>=obs?i+1:i)];
        return new SaturatedModel(nSigPar, nMuPar);
    }

    public static void main(String args[])
    {
        SaturatedModel fullModel = new SaturatedModel(new int[][]{{0,1,2,3},{1,4,5,6},{2,5,7,8},{3,6,8,9}}, new int[]{10,11,12,13},
                                                 new double[][]{{1,.1,.2,.3},{.1,1,.4,.5},{.2,.4,1,.6},{.3,.5,.6,1}}, new double[]{10,11,12,13});
//        model.testDerivatives(System.out, starting, 0.00001);
        
        MissingDataModel model = new MissingDataModel(fullModel);
//        SaturatedModel model = fullModel;
        model.logStream = System.out;
        double[] starting = model.getParameter();
        model.setParameter(starting);
        
        model.createDataWithRandomMissing(200, 0.1);
//        model.createData(200);
        Statik.copy(fullModel.dataCov, fullModel.sigma); Statik.copy(fullModel.dataMean, fullModel.mu); double[] trueEst = fullModel.getParameter();
        double[] lsEst = model.estimateLS(starting);
        double[] mlEst = model.estimateML(lsEst);
        
        System.out.println("starting   = "+Statik.matrixToString(starting));
        System.out.println("True Result= "+Statik.matrixToString(trueEst));
        System.out.println("LSestimate = "+Statik.matrixToString(lsEst));
        System.out.println("MLestimate = "+Statik.matrixToString(mlEst));
    }
    
    @Override
    protected void removeParameterNumber(int nr) {
        for (int i=0; i<sigPar.length; i++) for (int j=0; j<sigPar[i].length; j++) if (sigPar[i][j] >= nr) sigPar[i][j]--;
        for (int i=0; i<muPar.length; i++) if (muPar[i] >= nr) muPar[i]--;
    }
    
}
