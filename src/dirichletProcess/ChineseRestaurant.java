/*
 * Created on 24.10.2016
 */
package dirichletProcess;

import java.util.Random;

import engine.Statik;
import clustering.*;

public class ChineseRestaurant {

    public DirichletLikelihoodFunction likelihood = new UniformDirichletLikelihoodFunction();
    public ClusteringDistribution sample = new ClusteringDistribution();
    public int burninSteps = 100;
    public int anzPer = Integer.MAX_VALUE;
    public double alpha = 2.0;
    
    private Clustering currentClustering = new Clustering(new int[0]);
    private int step = 0;
    private int nextPerson = 0;
    private Random rand = new Random();
    private int[] shuffledPersons;
    
    public boolean DEBUGFLAG = false;
    public boolean paused = false;
    public boolean terminate = false;
    public double progress;
    
    public ChineseRestaurant() {}
    
    public ChineseRestaurant(DirichletLikelihoodFunction likelihoodFunction, int anzPer, double alpha, int burninSteps)
    {
        likelihood = likelihoodFunction;
        this.anzPer = anzPer;
        this.alpha = alpha;
        this.burninSteps = burninSteps;
        terminate = false;
        paused = false;
    }
    
    public void setSeed(long seed) {rand.setSeed(seed);}
    public void setRandom(Random rand) {this.rand = rand;}
    public ClusteringDistribution getSample() {return sample;}
    public Clustering getDistributionMode() {return sample.getMode();}
    public void setCurrentDistribution(Clustering clustering) {this.currentClustering = clustering;}
    
    public void step() {
        if (nextPerson==0 && anzPer == Integer.MAX_VALUE) shuffledPersons = null;
        if (nextPerson==0 && anzPer < Integer.MAX_VALUE) {
            if (shuffledPersons == null || shuffledPersons.length != anzPer) shuffledPersons = new int[anzPer];
            for (int i=0; i<anzPer; i++) shuffledPersons[i] = i;
            Statik.shuffle(shuffledPersons, rand);
        }
        int per = (shuffledPersons==null?nextPerson:shuffledPersons[nextPerson]);
        if (DEBUGFLAG) System.out.println(currentClustering);
        int anzOptions = currentClustering.getAnzCluster() + 1;
        
        double logAlpha = Math.log(alpha);
        double[] cumulativeProbability = new double[anzOptions];
        for (int i=0; i<anzOptions; i++) {
            double logProbability;
            if (i < anzOptions-1) logProbability = Math.log(currentClustering.getAnzPerInCluster(i) + (currentClustering.getClusterOf(per)==i?-1:0))
                                  + likelihood.getLogLikelihood(per, currentClustering.getCluster(i));
            else logProbability = logAlpha + likelihood.getLogLikelihood(per, null);
            cumulativeProbability[i] = logProbability;
        }
        Statik.computeProbabilitiesFromRelativeLogLikelihoods(cumulativeProbability, cumulativeProbability);
        for (int i=1; i<cumulativeProbability.length; i++) cumulativeProbability[i] = cumulativeProbability[i-1] + cumulativeProbability[i];

        double randomValue = cumulativeProbability[anzOptions-1] * rand.nextDouble();
        int target = 0;
        while (randomValue > cumulativeProbability[target]) target++;
        
        currentClustering.assignPerToCluster(per, target);
        currentClustering.toCanonicalForm();
        
        step++;
        nextPerson++; 
        
        if (nextPerson == anzPer) {
        	if (step > burninSteps)
        		sample.addSample(currentClustering);
        	nextPerson = 0;
        }
    }
    
    public void pause() {paused = true;}
    public void resume() {paused = false;}
    public void terminate() {terminate = true;}
    public double getProgress() {return progress;}
    
    public void step(int anzSteps) {
        terminate = false;
        progress = 0.0;
        for (int i=0; (i<anzSteps && !terminate); i++) {
            while (paused == true) {
                try {Thread.sleep(200);} catch (InterruptedException e) {}
            } 
            step();
            progress = i/(double)anzSteps;
        }
    }
}
