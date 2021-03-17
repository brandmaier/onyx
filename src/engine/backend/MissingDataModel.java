/*
 * Created on 26.04.2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package engine.backend;


import java.math.BigInteger;

import java.util.Hashtable;

import java.util.Vector;

import engine.Statik;



/**
 * @author timo
 */
public class MissingDataModel extends MultiGroupModel 
{
    static int NOPARAMETER = -1, NOTCOMPUTED = -1;

      Model fullModel;
      /**
       * For each submodel, an array of indices where the observation is in the full data set. The list of observations can be of different length, depending
       * on the number of observations in the corresponding submodel. 
       */
      int[][] observation;
      
      public MissingDataModel(Model fullModel)
      {
          super(new Model[]{fullModel}, fullModel.anzVar);
          this.fullModel = fullModel;
          observation = new int[1][anzVar]; for (int i=0; i<anzVar; i++) observation[0][i] = i;
          collectParameterNames();
      }
      
      public MissingDataModel copy() {
          MissingDataModel erg = new MissingDataModel(fullModel.copy());
          if (data != null) erg.setData(data);
          return erg;
      }

    /**
     * Computes and creates the submodels according to the missingness patterns. Part of this code (generation of the <code>observation</code> array) has been
     * moved to Model and will be calling this method in the future. 
     */
    public void setData(double[][] data)
    {
        this.data = data; anzPer = data.length; if (anzPer==0) return;
        
        Hashtable<BigInteger,Vector<double[]>> hash = new Hashtable<BigInteger,Vector<double[]>>();
        Hashtable<BigInteger,int[]> keyHash = new Hashtable<BigInteger,int[]>();
        
        for (int i=0; i<anzPer; i++) {
            int anzEx = 0; for (int j=0; j<data[i].length; j++) if (!Model.isMissing(data[i][j])) anzEx++;
            if (anzEx > 0) {
                int[] key = new int[anzEx]; double[] redDataRow = new double[anzEx];
                int p = 0; for (int j=0; j<data[i].length; j++) if (!Model.isMissing(data[i][j])) {key[p] = j; redDataRow[p] = data[i][j]; p++;}
                Vector<double[]> subData = null;
                if (hash.containsKey(codeKey(key))) subData = hash.get(codeKey(key)); else {
                    subData = new Vector<double[]>(); hash.put(codeKey(key), subData); keyHash.put(codeKey(key), key);}
                subData.add(redDataRow);
            }
        }
        
        anzGroups = hash.size(); observation = new int[anzGroups][]; submodel = new Model[anzGroups];
        int p = 0; for (BigInteger obsKey:hash.keySet()) {
            int[] obs = keyHash.get(obsKey);
            observation[p] = obs;
            Model sub = fullModel.copy();
            for (int j=anzVar-1; j>obs[obs.length-1]; j--) sub = sub.removeObservation(j);
            for (int i=obs.length-2; i>=0; i--) for (int j=obs[i+1]-1; j>obs[i]; j--) sub = sub.removeObservation(j);
            for (int j=obs[0]-1; j>=0; j--) sub = sub.removeObservation(j);
            
//            for (int j=0; j<obs[0]; j++) sub = sub.removeObservation(j); 
//            for (int i=0; i<obs.length-1; i++) for (int j=obs[i]+1; j<obs[i+1]; j++) sub = sub.removeObservation(j);
//            for (int j=obs[obs.length-1]+1; j<anzVar; j++) sub = sub.removeObservation(j); 
            Vector<double[]> v = hash.get(obsKey);
            double[][] subData = new double[v.size()][]; for (int i=0; i<v.size(); i++) subData[i] = v.elementAt(i);
            sub.setData(subData);
            sub.setParameter(fullModel.getParameter());
            submodel[p] = sub;
            p++;
        }
        
        collectParameterNames();
        computeMoments();
        System.gc();
    }

    @Override
    protected void collectParameterNames() {
        if (fullModel != null) {
            anzPar = fullModel.anzPar;
            paraNames = fullModel.paraNames; 
            updateTranslationTable();
            updatePosition();
        } else paraNames = null; 
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
}
