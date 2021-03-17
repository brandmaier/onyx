/*
 * Created on 06.09.2015
 */
package engine.backend;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Stack;

import engine.Statik;

/**
 * Models a dyadic data set in which participants play against each other. Estimates one skill parameter for each participant, with exception of the last, which is fixed to zero.
 * 
 *  Data structure is a square table of points, in each line indicating how many points that player won against the player of the corresponding column. 
 * 
 * @author timo
 */
public class DyadicIRTModel extends Model {

    private boolean DEBUGFLAG = false;
    private double BALANCEEPS = 0.001;      // a constant added to all cells of the win/loss matrix before estimation process to ensure that the match graph is connected. 
    
    /**
     * random:               randomly pairs participants
     * swiss:                round-based strategy: Sorts all players, chooses non-played with partner alternatingly from top and bottom.
     * localEntropy:         Computes p * (1-p) for each pair, where p is the winning chance for one; maximum is chosen.
     * globalEntropyHessian: Chooses the pair for which the determinant of the Hessian increases most, which is identical to the global Entropy if 
     *                       the parameter change because of the additional match is negligible. 
     * globalEntropy:        Walks through all possible pairs, estimates parameter, and chooses the one for which the Entropy increases most. 
     *  
     * @author timo
     */
    public enum PairingStrategy {random, swiss, localEntropy, globalEntropyHessian, globalEntropy};
    private Stack<int[]> round = new Stack<int[]>();

    // contains the probability of row winning against column, sigma(x_i - x_j).
    private double[][] winChance;
    private int initialPairingRounds;
    
    private int[] nextSuggestedPair = new int[2];
    private double[][] pairingValue;
    private double[] totalGames;
    private double[] totalPoints;
    private double[][] swissIdPointsFree;
    private double[][] hessianInverse;
    private double[] workVec;
    private int[][] initialPairing;
    
    public DyadicIRTModel(double[][] points) {
        super();
        setData(points);
    }
    
    @Override 
    public void setData(double[][] data) {
        this.data = data;
        anzPer = data.length; 
        anzVar = data.length;
        anzPar = data.length-1;
        position = Statik.ensureSize(position,  anzPar);
        winChance = Statik.ensureSize(winChance, anzPer, anzPer);
    }
    
    @Override
    public DyadicIRTModel copy() {
        DyadicIRTModel erg = new DyadicIRTModel(data);
        erg.position = Statik.copy(position);
        erg.setStrategy(this.getStrategy());
        return erg;
    }

    @Override
    public Model removeObservation(int obs) {
        DyadicIRTModel erg = copy();
        double[][] newData = Statik.submatrix(data, obs);
        erg.setData(newData);
        return erg;
    }

    @Override
    protected void computeMatrixTimesSigmaDev(int par, double[][] matrix, double[][] erg) {
        throw new RuntimeException ("computeMatrixTimesSigmaDev is not applicable for DyadicIRTModel.");
    }

    @Override
    protected void computeMatrixTimesSigmaDevDev(int par1, int par2,double[][] matrix, double[][] erg) {
        throw new RuntimeException ("computeMatrixTimesSigmaDevDev is not applicable for DyadicIRTModel.");
    }

    @Override
    protected void computeMatrixTimesMuDev(int par, double[][] matrix,double[] erg) {
        throw new RuntimeException ("computeMatrixTimesMuDev is not applicable for DyadicIRTModel.");
    }

    @Override
    public boolean setParameter(int nr, double value) {
        position[nr] = value;
        return true;
    }

    @Override
    public double getParameter(int nr) {
        return (nr >= anzPar?0.0:position[nr]);
    }

    @Override
    protected void removeParameterNumber(int nr) {
        throw new RuntimeException ("removeParameterNumber is not applicable for DyadicIRTModel; use removeObservation instead.");
    }

    @Override
    protected int maxParNumber() {
        return anzPar;
    }
    
    @Override
    public int getAnzPar() {return anzPar;}

    @Override
    public boolean isErrorParameter(int nr) {
        return false;
    }
    
    @Override
    public void evaluateMuAndSigma(double[] values) {
    }

    @Override
    public void computeLeastSquaresDerivatives(double[] value,boolean recomputeMuAndSigma) {
        throw new RuntimeException ("Least Squares is not implemented for DyadicIRTModel.");
    }
    
    @Override
    public boolean setParameter(double[] value) {
        if (position == null) position = Statik.copy(value);
        else Statik.copy(value, position);
        return true;
    }
    
    @Override
    public double getMinusTwoLogLikelihood(double[] value, boolean recomputeMuAndSigma) {
        if (value != null) setParameter(value);
        winChance = Statik.ensureSize(winChance, anzPer, anzPer);
        ll = 0;
        for (int winner=0; winner<anzPer; winner++) 
            for (int looser=winner+1; looser<anzPer; looser++) 
                {winChance[winner][looser] = Statik.sigmoid(getParameter(winner) - getParameter(looser)); winChance[looser][winner] = 1-winChance[winner][looser];}
        
        for (int winner=0; winner<anzPer; winner++) 
            for (int looser=0; looser<anzPer; looser++) 
                if (winner != looser) {
                    double x = getParameter(winner) - getParameter(looser);
                    ll += -2 * (data[winner][looser]+BALANCEEPS) * (x < -20?x:Math.log(winChance[winner][looser]));
                }
        return ll;
    }

    @Override
    public double[] estimateML() {position = Statik.ensureSize(position, anzPar); return estimateML(position);}

/*
    @Override
    public double[] estimateML(double[] position, double EPS) {
        for (int i=0; i<anzPer; i++) for (int j=0; j<anzPer; j++) data[i][j] += BALANCEEPS;
        double[] erg = super.estimateML(position, EPS);
        for (int i=0; i<anzPer; i++) for (int j=0; j<anzPer; j++) data[i][j] -= BALANCEEPS;
        return erg;
    }
 */   
    public void computeLogLikelihoodDerivatives() {computeLogLikelihoodDerivatives(null);}
    
    @Override
    public void computeLogLikelihoodDerivatives(double[] value, boolean recomputeMuAndSigma) {
        llD = Statik.ensureSize(llD, anzPar);
        llDD = Statik.ensureSize(llDD, anzPar, anzPar);
        winChance = Statik.ensureSize(winChance, anzPer, anzPer);

        if (value != null) setParameter(value);
        Statik.setToZero(llD); Statik.setToZero(llDD);
        ll = 0;
        
        for (int winner=0; winner<anzPer; winner++) 
            for (int looser=winner+1; looser<anzPer; looser++) 
                {winChance[winner][looser] = Statik.sigmoid(getParameter(winner) - getParameter(looser)); winChance[looser][winner] = 1-winChance[winner][looser];}

        for (int winner=0; winner<anzPer; winner++) 
            for (int looser=0; looser<anzPer; looser++) 
                if (winner != looser) {
                    double x = getParameter(winner) - getParameter(looser);
                    ll += -2 * (data[winner][looser]+BALANCEEPS) * (x < -20?x:Math.log(winChance[winner][looser]));
                    
                    if (winner < anzPar) llD[winner] += -2 * (data[winner][looser]+BALANCEEPS) * winChance[looser][winner];
                    if (looser < anzPar) llD[looser] += +2 * (data[winner][looser]+BALANCEEPS) * winChance[looser][winner];
                    
                    double aij = 2 * ((data[winner][looser]+BALANCEEPS) + (data[looser][winner]+BALANCEEPS)) * winChance[winner][looser] * winChance[looser][winner];
                    if (winner < anzPar)                    llDD[winner][winner] += aij;
                    if (winner < anzPar && looser < anzPar) llDD[winner][looser] += -aij;
            }
    }

    /**
     * Computes the distance of compare to the current skill parameters, correcting both for their mean to allow an arbitrary choice of the anchor. 
     * @param compare
     * @return
     */
    public double distanceToEstimate(double[] compare) {
        double meanPos = 0; for (int i=0; i<anzPer; i++) meanPos += this.getParameter(i); meanPos /= (double)anzPer;
        double meanCmp = Statik.mean(compare);
        double erg = 0;
        for (int i=0; i<anzPer; i++) 
            erg += (getParameter(i) - meanPos - compare[i] + meanCmp)*(getParameter(i) - meanPos - compare[i] + meanCmp);
        erg = Math.sqrt(erg / (anzPer-1));
        return erg;
    }
    
    private double computeExpectedHessianDetWithAdditionalData(int p1, int p2, boolean withParameterMovement) {
        workVec = Statik.ensureSize(workVec, anzPar);
        double[] pos = workVec;
        Statik.copy(position, pos);
        double det = 0;
        if (withParameterMovement) {
            double weight = winChance[p1][p2];
            for (int i=0; i<2; i++) {
                if (i==0) data[p1][p2] += 1; else data[p2][p1] += 1;
                estimateML(pos);
                det += (i==0?weight:(1-weight)) * Statik.determinant(llDD);
                if (i==0) data[p1][p2] -= 1; else data[p2][p1] -= 1;
            }
        } else {
            double localEntropy = winChance[p1][p2] * winChance[p2][p1];
            llDD[p1][p1] += localEntropy; 
            if (p2 < anzPar) {llDD[p1][p2] -= localEntropy; llDD[p2][p1] -= localEntropy; llDD[p2][p2] += localEntropy;}
            det = Statik.determinant(llDD);
            llDD[p1][p1] -= localEntropy; 
            if (p2 < anzPar) {llDD[p1][p2] += localEntropy; llDD[p2][p1] += localEntropy; llDD[p2][p2] -= localEntropy;}
        }
        setParameter(pos);
        return det;
    }
    
    /**
     * Creates a round based on the Swiss tournament system
     */
    private void assignNextSwissRound() {
        swissIdPointsFree = Statik.ensureSize(swissIdPointsFree, anzPer, 3);
        double maxGames = 0; for (int i=0; i<anzPer; i++) if (totalGames[i] > maxGames) maxGames = totalGames[i];
        double minFree = Double.POSITIVE_INFINITY;
        for (int i=0; i<anzPer; i++) {
            swissIdPointsFree[i][0] = i;
            swissIdPointsFree[i][2] = maxGames - totalGames[i];
            if (minFree > swissIdPointsFree[i][2]) minFree = swissIdPointsFree[i][2];
            swissIdPointsFree[i][1] += totalPoints[i] + swissIdPointsFree[i][2] + rand.nextDouble()*0.1;
        }
        Arrays.sort(swissIdPointsFree, new Comparator<double[]>() {
            @Override
            public int compare(double[] arg0, double[] arg1) {
                if (arg0[1] > arg1[1]) return 1; 
                if (arg0[1] < arg1[1]) return -1;
                return 0;
            }
        });
        int top = 0, bottom = anzPer-1;
        if (anzPer % 2 == 1) {
            int freegame = anzPer-1; while (swissIdPointsFree[freegame][2] > minFree) freegame--;
            for (int i=freegame; i<anzPer-1; i++) swissIdPointsFree[i] = swissIdPointsFree[i+1];
            bottom--;
        }
        // after here, the third entry in swissIdPointsFree will be used to indicate successful assignment. 
        double minPlayed = Double.POSITIVE_INFINITY; for (int i=0; i<anzPer; i++) for (int j=i+1; j<anzPer; j++) if (data[i][j] + data[j][i] < minPlayed) minPlayed = data[i][j] + data[j][i];
        while (top < bottom) {
            boolean fromTop = top <= anzPer-1-bottom;
            if (fromTop) {
                int i=top+1; while (i<=bottom && (Double.isNaN(swissIdPointsFree[i][2]) || data[(int)swissIdPointsFree[top][0]][(int)swissIdPointsFree[i][0]]+data[(int)swissIdPointsFree[i][0]][(int)swissIdPointsFree[top][0]] != minPlayed)) i++;
                if (i>bottom) {i=top+1; while (i<=bottom && Double.isNaN(swissIdPointsFree[i][2])) i++;}
                if (i<=bottom) {
                    swissIdPointsFree[i][2] = swissIdPointsFree[top][2] = Double.NaN;
                    round.push(new int[]{(int)swissIdPointsFree[top][0], (int)swissIdPointsFree[i][0]});
                    top++; while (top < bottom && Double.isNaN(swissIdPointsFree[top][2])) top++;
                } else top = bottom+1;
            } else {
                int i=bottom-1; while (i>=top && (Double.isNaN(swissIdPointsFree[i][2]) || data[(int)swissIdPointsFree[bottom][0]][(int)swissIdPointsFree[i][0]]+data[(int)swissIdPointsFree[i][0]][(int)swissIdPointsFree[bottom][0]] != minPlayed)) i--;
                if (i<top) {i=bottom-1; while (i>=top && Double.isNaN(swissIdPointsFree[i][2])) i--;}
                if (i>=top) {
                    swissIdPointsFree[i][2] = swissIdPointsFree[bottom][2] = Double.NaN;
                    round.push(new int[]{(int)swissIdPointsFree[bottom][0], (int)swissIdPointsFree[i][0]});
                    bottom--; while (bottom > top && swissIdPointsFree[bottom][2] == Double.NaN) bottom--;
                } else bottom = top-1;
            }
        }
    }
    
    public void setInitialPairingRounds(int anzRounds) {
        initialPairingRounds = anzRounds;
        if (initialPairing != null) {initialPairing = Statik.ensureSize(initialPairing, (anzPer+1) / 2, 2); initialPairing[0][0] = -1;}
        round.clear();
    }
    private void assignNextInitialPairingRound() {
        int anzPairs = (anzPer+1) / 2;
        boolean oddNumber = anzPer % 2 == 1;
        if (initialPairing == null || initialPairing[0][0] == -1) {
            initialPairing = Statik.ensureSize(initialPairing, anzPairs, 2);
            for (int i=0; i<anzPairs; i++) {initialPairing[i][0] = 2*i; initialPairing[i][1] = 2*i+1;}
            if (oddNumber) initialPairing[anzPairs-1][1] = -1;
        } else {
            int picked = 0; if (totalPoints[initialPairing[0][0]] < totalPoints[initialPairing[0][1]]) picked = 1;
            int upperTrailing = initialPairing[0][1-picked];
            for (int i=0; i<anzPairs-1; i++) {
                double pickPoints = totalPoints[initialPairing[i][picked]]; 
                int nextPick = 0; if (initialPairing[i+1][1] != -1 && Math.abs(totalPoints[initialPairing[i+1][0]]-pickPoints) > Math.abs(totalPoints[initialPairing[i+1][1]]-pickPoints)) nextPick = 1;
                initialPairing[i][1-picked] = initialPairing[i+1][nextPick];
                picked = 1 - nextPick;
            }
            initialPairing[anzPairs-1][1-picked] = upperTrailing;            
        }
        final double[] fpoints= totalPoints;
        Arrays.sort(initialPairing, new Comparator<int[]>() {
            public int compare(int[] arg0, int[] arg1) {
                if (arg0[1]==-1) return 1;
                if (arg1[1]==-1) return 0;
                if (fpoints[arg0[0]]+fpoints[arg0[1]] > fpoints[arg1[0]]+fpoints[arg1[1]]) return -1;
                if (fpoints[arg0[0]]+fpoints[arg0[1]] < fpoints[arg1[0]]+fpoints[arg1[1]]) return +1;
                return 0;
            }
        });
        round.clear();
        for (int i=0; i<(oddNumber?anzPairs-1:anzPairs); i++) round.push(initialPairing[i]);
        initialPairingRounds--;
        return;
    }
    
    /**
     * Suggests the next pairing in an adaptive testing situation. If pool is not null, only participants from the pool are selected, unless pre-prepared
     * rounds say differently.  
     * 
     * @param strategy      See strategy explanations
     * @param maxLagTeam    maximal allowed difference of games played per player; players above that difference will not be selected, unless there is exactly one player with less games. Inactive if maxLag == Double.Infinity
     * @param maxLagPair    maximal allowed difference of games played per pair; pairs above that difference will not be selected. 
     * @return
     */
    public int[] suggestNextPairing(PairingStrategy strategy, double maxLagPlayer, double maxLagPair) {return suggestNextPairing(strategy, maxLagPlayer, maxLagPair, null, 0);}
    public int[] suggestNextPairing(PairingStrategy strategy, double maxLagPlayer, double maxLagPair, int[] pool) {return suggestNextPairing(strategy, maxLagPlayer, maxLagPair, pool, pool.length);}
        public int[] suggestNextPairing(PairingStrategy strategy, double maxLagPlayer, double maxLagPair, int[] pool, int poolsize) {
        pairingValue = Statik.ensureSize(pairingValue, anzPer, anzPer);
        computeLogLikelihoodDerivatives();
        totalGames = Statik.ensureSize(totalGames, anzPer);
        totalPoints = Statik.ensureSize(totalPoints, anzPer);
        Statik.setToZero(totalGames); Statik.setToZero(totalPoints);
        for (int i=0; i<anzPer; i++) for (int j=0; j<anzPer; j++) if (i!=j) {totalPoints[i] += data[i][j]; totalGames[i] += data[i][j] + data[j][i];}
        
        if (round.isEmpty() && initialPairingRounds > 0) assignNextInitialPairingRound();
        if (round.isEmpty() && strategy == PairingStrategy.swiss) assignNextSwissRound();
        if (!round.isEmpty()) return round.pop();
        
        boolean defaultBackToRandom = false;
        if (strategy == PairingStrategy.globalEntropyHessian || DEBUGFLAG) {
            hessianInverse = Statik.ensureSize(hessianInverse, anzPar, anzPar);
            try {Statik.invert(llDD, hessianInverse);} catch (Exception e) {defaultBackToRandom = true;}
        }
        if (DEBUGFLAG) System.out.println("\r\n***DEBUG***\r\ni\tj\tlocal\tglobalFast\tglobalSlow\tglobal");

        for (int i=0; i<anzPer; i++) for (int j=i+1; j<anzPer; j++) {
            double localEntropy = winChance[i][j] * winChance[j][i];
            if (strategy == PairingStrategy.localEntropy) 
                pairingValue[i][j] = localEntropy;
            else if (strategy == PairingStrategy.globalEntropyHessian && !defaultBackToRandom) {
                if (j < anzPar) pairingValue[i][j] = localEntropy * (hessianInverse[i][i]+hessianInverse[j][j]-2*hessianInverse[i][j]);
                else            pairingValue[i][j] = localEntropy * hessianInverse[i][i];
            }
            else if (strategy == PairingStrategy.globalEntropy) {
                pairingValue[i][j] = computeExpectedHessianDetWithAdditionalData(i, j, true);
            }
            else pairingValue[i][j] = rand.nextDouble();
            
            if (DEBUGFLAG) {
                double globalfast = 0; 
                if (j < anzPar) globalfast = localEntropy * (hessianInverse[i][i]+hessianInverse[j][j]-2*hessianInverse[i][j]);
                else            globalfast = localEntropy * hessianInverse[i][i];
                System.out.println(i+"\t"+j+"\t"+Statik.doubleNStellen(Math.log(localEntropy),4)+"\t"+
                        Statik.doubleNStellen(Math.log((globalfast+1)*(defaultBackToRandom?Double.NEGATIVE_INFINITY:Statik.determinant(llDD))),4)+"\t"+
                        Statik.doubleNStellen(Math.log(defaultBackToRandom?Double.NEGATIVE_INFINITY:computeExpectedHessianDetWithAdditionalData(i,j,false)),4)+"\t"+
                        Statik.doubleNStellen(Math.log(defaultBackToRandom?Double.NEGATIVE_INFINITY:computeExpectedHessianDetWithAdditionalData(i,j,true)),4));
                computeLogLikelihoodDerivatives();
            }
        }
        
        if (maxLagPlayer < Double.POSITIVE_INFINITY) {
            double min = Double.POSITIVE_INFINITY; for (int i=0; i<(pool==null?anzPer:poolsize); i++) {
                double t = totalGames[(pool==null?i:pool[i])]; if (min > t) min = t;
            }
            int countAllowed = 0; for (int i=0; i<(pool==null?anzPer:poolsize); i++) if (totalGames[(pool==null?i:pool[i])] <= min + maxLagPlayer) countAllowed++;
            if (countAllowed == 1) {for (int i=0; i<anzPer; i++) for (int j=i+1; j<anzPer; j++) if (totalGames[i] > min + maxLagPlayer && totalGames[j] > min + maxLagPlayer) pairingValue[i][j] = Double.NEGATIVE_INFINITY;}
            else                   {for (int i=0; i<anzPer; i++) for (int j=i+1; j<anzPer; j++) if (totalGames[i] > min + maxLagPlayer || totalGames[j] > min + maxLagPlayer) pairingValue[i][j] = Double.NEGATIVE_INFINITY;}
        }
        if (maxLagPair < Double.POSITIVE_INFINITY) {
            double min = Double.POSITIVE_INFINITY; for (int i=0; i<anzPer; i++) for (int j=i+1; j<anzPer; j++) if (min > data[i][j] + data[j][i]) min = data[i][j] + data[j][i];
            for (int i=0; i<anzPer; i++) for (int j=i+1; j<anzPer; j++) if (data[i][j] + data[j][i] > min + maxLagPair) pairingValue[i][j] -= 1000000;
        }
        
        nextSuggestedPair[0] = nextSuggestedPair[1] = -1;
        double max = Double.NEGATIVE_INFINITY;
        for (int i=0; i<(pool==null?anzPer:poolsize); i++) for (int j=i+1; j<(pool==null?anzPer:poolsize); j++) {
            int vi = (pool==null?i:pool[i]), vj = (pool==null?j:pool[j]);
            if (pairingValue[vi][vj] > max) {max = pairingValue[vi][vj]; nextSuggestedPair[0] = vi; nextSuggestedPair[1] = vj;}
        }
        return nextSuggestedPair;
    }

    public int[] getNextSuggestedPair() {return nextSuggestedPair;}
    public double[][] getLastPairingValues() {return pairingValue;}
}
