/*
 * Created on 24.10.2016
 */
package dirichletProcess;

import engine.Statik;

public class LikertScaleLikelihoodFunction extends DirichletLikelihoodFunction {

    public int[][] data;
    public int[] anzOptions;
    public double priorAlpha;               // total weight of the prior, uniformly distributed
    public double spillover;
    public boolean independent;            // if true, probabilities for all assumptions are assumed as independent. 
    
    private double[] weightPerPerson;       // normalizer of each participant's influence in independent mode
    private int anzPer;
    private int anzCells;
    private int anzVar;
    
    public LikertScaleLikelihoodFunction(int[][] data, int[] anzOptions, double priorAlpha, double spillover, boolean independent) {
        this.spillover = spillover;
        this.independent = independent;
        this.priorAlpha = priorAlpha;

        if (anzOptions==null) determineAnzOptions(data); else this.anzOptions = anzOptions;
        this.anzVar = anzOptions.length;
        anzCells = 1; for (int i=0; i<anzOptions.length; i++) anzCells *= anzOptions[i];
        
        setData(data);
    }
    
    public void determineAnzOptions(int[][] data) {
        if (data==null || data.length==0) {anzOptions = new int[0]; return;}
        anzVar = data[0].length;
        anzOptions = new int[anzVar];
        for (int i=0; i<anzVar;  i++) {
            int min = Integer.MAX_VALUE, max = Integer.MIN_VALUE;
            for (int j=0; j<anzPer; j++) { 
                if (data[j][i] > max) max = data[j][i];
                if (data[i][j] < min) min = data[j][i];
            }
            anzOptions[i] = max-min+1; 
        }
    }
    
    public void setData(int[][] data) {
        this.data = Statik.copy(data);
        anzPer = data.length;
        computeWeightPerPerson();
    }
    
    private void computeWeightPerPerson() {
        weightPerPerson = new double[anzPer];
        for (int i=0; i<data.length; i++) {
            weightPerPerson[i] = 1.0;
            for (int j=0; j<data[i].length; j++) {
                int distToTop = anzOptions[j] - data[i][j];
                int distToBottom = data[i][j] - 1;
                double fak = 1+(distToTop>=1?spillover:0) + (distToTop>=2?spillover*spillover:0);
                      fak += (distToBottom>=1?spillover:0) + (distToBottom>=2?spillover*spillover:0);
                weightPerPerson[i] *= fak;    
            }
        }
    }
    
    public int getDistance(int p1, int p2) {
        int dist = 0;
        for (int i=0; i<anzVar; i++) dist += Math.abs(data[p1][i]-data[p2][i]);
        return dist;
    }
    
    public double getIndependentLikelihood(int person, int[] group) {
        double erg = 1;
        for (int i=0; i<anzOptions.length; i++) {
            double localProbability = priorAlpha / (double)anzOptions[i];
            double sum = priorAlpha;
            if (group != null) for (int j=0; j<group.length; j++) {
                int distToTop = anzOptions[i] - data[group[j]][i];
                int distToBottom = data[group[j]][i] - 1;
                double fak = 1+(distToTop>=1?spillover:0) + (distToTop>=2?spillover*spillover:0);
                      fak += (distToBottom>=1?spillover:0) + (distToBottom>=2?spillover*spillover:0);
                int dist = Math.abs(data[person][i]-data[group[j]][i]);
                if (dist<3) localProbability += Math.pow(spillover, dist)/fak;
                sum += 1;
            }
            erg *= (localProbability/sum);
        }
        return erg;
    }
    
    public double getDependentLikelihood(int person, int[] group) {
        double erg = priorAlpha / (double)anzCells;
        double sum = priorAlpha;
        if (group != null) {
            for (int i=0; i<group.length; i++) if (group[i]!=person) {
                double dist = getDistance(person, group[i]);
                if (dist <3) erg += Math.pow(spillover, dist)/weightPerPerson[group[i]];
                sum += 1;
            }
        }
        return erg / sum;
    }
    
    @Override
    public double getLogLikelihood(int person, int[] group) {
        if (independent) return Math.log(getIndependentLikelihood(person, group));
        else return Math.log(getDependentLikelihood(person, group));
    }
}
