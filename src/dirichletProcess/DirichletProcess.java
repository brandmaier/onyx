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
 * Created on 07.09.2017
 */
package dirichletProcess;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.Arrays;
import java.util.Date;

import parallelProcesses.ParallelProcess;
import parallelProcesses.ParallelProcessHandler;
import parallelProcesses.ParallelProcessHandler.ProcessStatus;
import parallelProcesses.ParallelProcessView;
import engine.Dataset;
import engine.OnyxModel;
import engine.Preferences;
import engine.RawDataset;
import engine.Statik;
import engine.backend.Model;
import machineLearning.clustering.Clustering;
import machineLearning.clustering.ClusteringDistribution;
//import meanPartition.PartitionDistribution;               // package and Class is missing. 
import BayesianNonparametrics.DPMixture;
import BayesianNonparametrics.DependentNormalsBase;

public class DirichletProcess implements ParallelProcess {
    
    ProcessStatus status = ProcessStatus.WAITING;
    ChineseRestaurant crp;
    OnyxModel model, copy;
    int anzSteps;
    ParallelProcessView mainProcessView;
    String targetName;
    
    boolean doPreClustering;
    int preClusteringBurnIn;
    int preClusteringSamples;
    int preClusteringThinning;
    double preClusteringAlpha;
    
    private int[] doPreClustering(double[][] observations) {
    	int dims = observations[0].length;    	
    	double[] priorMu = new double[dims];
		double priorKappa = dims, priorNu = dims;		
		double[][] priorPsi = new double[dims][dims];
		for(int i = 0; i< dims;i++)
			priorPsi[i][i] = 1;
				
		DPMixture dpm = new DPMixture(new DependentNormalsBase(priorMu, priorPsi, priorKappa, priorNu, observations), preClusteringAlpha);		
		int[][] samples = dpm.simulate(preClusteringBurnIn, preClusteringSamples, preClusteringThinning);		
		// @FIXME  Class PartitionDistribution is missing, probably lost in transition to github. 
//		return new PartitionDistribution(samples).getMeanPartition2();
		return null;
    }
    
    public DirichletProcess(ChineseRestaurant crp, OnyxModel copy, OnyxModel model, int anzSteps, String targetName) {this(crp, copy, model, anzSteps, false, 0, 0, 0, 0.0, targetName);}
    public DirichletProcess(ChineseRestaurant crp, OnyxModel copy, OnyxModel model, int anzSteps, boolean doPreClustering, int preClusteringBurnIn, int preClusteringSamples, int preClusteringThinning, double preClusteringAlpha, String targetName) {
    	this.crp = crp; this.model = model; this.copy = copy; 
        this.anzSteps = anzSteps; this.targetName = targetName;
        this.doPreClustering = doPreClustering;
        this.preClusteringBurnIn = preClusteringBurnIn; 
        this.preClusteringSamples = preClusteringSamples; 
        this.preClusteringThinning = preClusteringThinning;
        this.preClusteringAlpha = preClusteringAlpha;
    }
    
    @Override
    public void run() {
        status = ProcessStatus.RUNNING;
        double[][] fullData = copy.data;
        
        // here we do pre-clustering
        if(doPreClustering)
        	crp.setCurrentDistribution(new Clustering(doPreClustering(fullData)));
                        
        int anzVarNew = copy.anzVar+1;
        crp.step(anzSteps);
        status = ProcessStatus.FINISHED;
        Clustering mode;
        try {
            mode = crp.getDistributionMode();
        } catch (Exception e) {mode = new Clustering(new int[fullData.length]);}
        int x = -1, y = -1; if (mainProcessView!= null) {x = mainProcessView.getX(); y = mainProcessView.getY();}
        
//        int[] clusterID = mode.getIntegerArray();
//        int anzCluster = mode.getAnzCluster();
//        double[][] dataset = new double[fullData.length][anzVarNew];
//        Statik.copy(fullData, dataset);
//        for (int i=0; i<dataset.length; i++) dataset[i][anzVarNew-1] = (i >= clusterID.length || clusterID[i]==Clustering.NA?Model.MISSING:anzCluster-clusterID[i]);
//        for (int i=0; i<model.modelListener.length; i++) model.modelListener[i].addDataset(dataset, getTargetName(), new String[]{"Cluster ID"}, x, y);
//        
        RawDataset dataset = getClusteringDistributionDataset(fullData, 1);
        for (int i=0; i<model.modelListener.length; i++) model.modelListener[i].addDataset(dataset, x, y);
        writeOutputFile(fullData);
        status = ProcessStatus.DEAD;
    }
    
    public RawDataset getClusteringDistributionDataset(double[][] inputData, int anzClusterings) {
        int anzPer = inputData.length;
        int anzVar = inputData[0].length;
        
        ClusteringDistribution fullDistribution = crp.getSample();
        int[] freq = fullDistribution.getFrequencies();
        if (anzClusterings > freq.length) anzClusterings = freq.length;
        int[][] cluster = fullDistribution.toArray();
        
        //calculation of mean partition. TvO 26SEP2023: Class PartitionDistribution is missing, possibly lost in transition to github. @FIXME
//        int[] meanPartition = new PartitionDistribution(fullDistribution).getMeanPartition2();
        int[] meanPartition = null;
        		
        for (int cl=0; cl<anzClusterings; cl++) 
        {
            int max = Integer.MIN_VALUE; 
            for (int i=0; i<cluster.length; i++) if (cluster[i][cl] != Clustering.NA && cluster[i][cl] > max) max = cluster[i][cl];
            for (int i=0; i<cluster.length; i++) cluster[i][cl] = (cluster[i][cl] == Clustering.NA?Clustering.NA:max+1-cluster[i][cl]);
        }
        int anzVarNew = anzVar+anzClusterings+1;
        String[] colnames = new String[anzVarNew];
        String[] varNames = model.getObservedVariableNames();
        Statik.copy(varNames, colnames);
        for (int i=0; i<anzClusterings; i++) colnames[i+anzVar] = "ClusterID_K="+freq[i];
        
        //set column label for mean partition
        colnames[colnames.length - 1] = "Partition_mean";
        
        double[][] dataNew = new double[anzPer][anzVarNew];
        Statik.copy(inputData, dataNew);
        for (int i=0; i<anzPer; i++) for (int j=0; j<anzClusterings; j++) 
            dataNew[i][j+anzVar] = (i >= cluster.length || cluster[i][j]==Clustering.NA?Model.MISSING:cluster[i][j]);
        
        //insert mean partition into dataset
        for (int i=0; i<anzPer; i++)
        	dataNew[i][colnames.length - 1] = meanPartition[i];
        
        RawDataset erg = new RawDataset(dataNew, Arrays.asList(colnames));
        erg.setName(getTargetName());
        return erg;
    }
    
    public void writeOutputFile(double[][] inputData) {
        Date now = new Date();
        
        String filename = "DP Clustering at "+now.toString().replace(':', '_');
        File dir = new File(Preferences.getAsString("DefaultWorkingPath"));
        File file = new File(dir, filename+".txt");

        RawDataset result = getClusteringDistributionDataset(inputData, Integer.MAX_VALUE);
        result.save(file);
       /*
        try {
            PrintStream stream = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
            
            
            ClusteringDistribution raw = crp.getSample();
            stream.println("Clustering raw Mode: ");
            stream.println(raw.getMode());
//            stream.println("Clustering raw Mean: ");
//            stream.println(raw.getMean());
            stream.println("Clustering raw full distribution: ");
            stream.println(raw);
            stream.flush();

            ClusteringDistribution smoothed = raw.modes((int)Math.round(0.05*raw.getAnzPersons()));
            stream.println("Clustering smoothed Mode: ");
            stream.println(smoothed.getMode());
            stream.println("Clustering smoothed full distribution: ");
            stream.println(smoothed);
            System.out.println("hi there debug");
            
            stream.flush();
            stream.close();
        } catch (Exception e) 
        {
            System.err.println("Error in writing clustering output file: "+e);
        }
        */
    }
    
    @Override
    public ProcessStatus getStatus() {return status;}
    
    @Override
    public void requestTransferToStatus(ParallelProcessHandler.ProcessStatus status) {
        if (status == ProcessStatus.PAUSED) {crp.pause(); this.status = status;}
        else if (status == ProcessStatus.RUNNING) {crp.resume(); this.status = status;}
        else if (status == ProcessStatus.FINISHED) crp.terminate();
    }
    
    @Override
    public double getProgress() {return crp.getProgress();}

    @Override
    public void setMainParallelProcessView(ParallelProcessView view) {this.mainProcessView = view;}
    
    @Override
    public String getTargetName() {return targetName;}
}
