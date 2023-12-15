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
 * Created on 14.02.2014
 */
package engine;

import engine.backend.Model;

public class OptimizationHistory {

    private final int MAXSIZE = 300;
    private String[] parameterNames;
    private final int INITIALSIZE = 20;
    private double[][] history;
    private int size;
    private int pos;
    public int anzPar;
    private int anzVal;
    private final int ANZFIX = 4;     // Fit, |Sigma|, lastGain, lastSteplength
    private final int ANZPERPAR = 3;  // position, gradient, move
    private final int ANZPERDIM = 3;  // Eigenvalue, gradientProjection, factor
    
    public OptimizationHistory(int anzPar) {this(null, anzPar);}
    public OptimizationHistory(String[] parameterNames) {this(parameterNames, parameterNames.length);}
    
    private OptimizationHistory(String[] parameterNames, int anzPar) {
        this.parameterNames = parameterNames;
        this.anzPar = anzPar;
        this.anzVal = ANZFIX + anzPar * (ANZPERPAR+ANZPERDIM);
        history = new double[INITIALSIZE][anzVal];
    }
    
    private void increaseSize() {
        int newsize = Math.min(history.length*2, MAXSIZE);
        if (newsize > history.length) {
            double[][] newHist = new double[history.length*2][anzVal];
            Statik.copy(history, newHist);
            history = newHist;
        }
    }
    
    private int getIx(int pos) {
        int p = pos % MAXSIZE;
        return pos % MAXSIZE;
    }
    
    public int getSize() {return size;}
    
    public void addPoint() {
        size++;
        if (size >= history.length) increaseSize();
        pos = size-1;
        empty(pos);
    }
    
    public void empty(int ix) {
        int p = getIx(ix);
        for (int i=0; i<anzVal; i++) history[p][i] = Model.MISSING;
    }
    
    public void writeFixed(double fit, double sigmaDet, double lastGain, double lastSteplength) {
        int p = getIx(pos);
        if (!Model.isMissing(fit)) history[p][0] = fit;
        if (!Model.isMissing(sigmaDet)) history[p][1] = sigmaDet;
        if (!Model.isMissing(lastGain)) history[p][2] = lastGain;
        if (!Model.isMissing(lastSteplength)) history[p][3] = lastSteplength;
    }
    
    public void writePerParameter(int pnr, double position, double gradient, double move) {
        int p = getIx(pos);
        if (!Model.isMissing(position)) history[p][ANZFIX+pnr*ANZPERPAR] = position;
        if (!Model.isMissing(gradient)) history[p][ANZFIX+pnr*ANZPERPAR+1] = gradient;
        if (!Model.isMissing(move)) history[p][ANZFIX+pnr*ANZPERPAR+2] = move;
    }
    
    public void writePerDimension(int dimnr, double eigenvalue, double gradient, double factor) {
        int p = getIx(pos);
        if (!Model.isMissing(eigenvalue)) history[p][ANZFIX+anzPar*ANZPERPAR+dimnr*ANZPERDIM] = eigenvalue;
        if (!Model.isMissing(gradient)) history[p][ANZFIX+anzPar*ANZPERPAR+dimnr*ANZPERDIM+1] = gradient;
        if (!Model.isMissing(factor)) history[p][ANZFIX+anzPar*ANZPERPAR+dimnr*ANZPERDIM+2] = factor;
    }
    
    public void reset() {pos = 0;}
    public boolean next() {if (pos < size-1) {pos++; return true;} else return false;}
    public boolean previous() {if (pos > 0) {pos--; return true;} else return false;}
    public boolean setTo(int pos) {if (pos >= 0 && pos <= size-1) {this.pos = pos; return true;} else return false;}
    
    public double getFit() {return getFit(pos);}
    public double getSigmaDet() {return getSigmaDet(pos);}
    public double getLastGain() {return getLastGain(pos);}
    public double getLastSteplength() {return getLastSteplength(pos);}
    public double getFit(int pos) {return history[getIx(pos)][0];}
    public double getSigmaDet(int pos) {return history[getIx(pos)][1];}
    public double getLastGain(int pos) {return history[getIx(pos)][2];}
    public double getLastSteplength(int pos) {if (pos < 0) return Double.MAX_VALUE; else return history[getIx(pos)][3];}
    
    public double getPosition(int par) {return getPosition(pos, par);}
    public double getGradient(int par) {return getGradient(pos, par);}
    public double getMove(int par) {return getMove(pos, par);}
    public double getPosition(int pos, int par) {return history[getIx(pos)][ANZFIX+par*ANZPERPAR];}
    public double getGradient(int pos, int par) {return history[getIx(pos)][ANZFIX+par*ANZPERPAR+1];}
    public double getMove(int pos, int par) {return history[getIx(pos)][ANZFIX+par*ANZPERPAR+2];}
    
    public double[] getAllPositions() {return getAllPositions(pos);}
    public double[] getAllPositions(int pos) {double[] erg = new double[anzPar]; for (int i=0; i<anzPar; i++) erg[i] = getPosition(pos,i); return erg;}
    
    public double getEigenvalue(int dim) {return getEigenvalue(pos, dim);}
    public double getGradientOnEV(int dim) {return getGradientOnEV(pos, dim);}
    public double getFactor(int dim) {return getFactor(pos,dim);}
    public double getEigenvalue(int pos, int dim) {return history[getIx(pos)][ANZFIX+anzPar*ANZPERPAR+dim*ANZPERDIM];}
    public double getGradientOnEV(int pos, int dim) {return history[getIx(pos)][ANZFIX+anzPar*ANZPERPAR+dim*ANZPERDIM+1];}
    public double getFactor(int pos, int dim) {return history[getIx(pos)][ANZFIX+anzPar*ANZPERPAR+dim*ANZPERDIM+2];}
    
    public String toString() {
        String erg = "Step\tFit     \t|Sigma| \tGain    \tStep    \t";
        for (int i=0; i<anzPar; i++) erg += (parameterNames==null?"Pos "+i+"   ":parameterNames[i])+"\tGrad "+i+"  \tMove "+i+"  \t";
        for (int i=0; i<anzPar; i++) erg += "EV "+i+"    \tProjGrad "+i+"\tFactor "+i+(i<anzPar-1?"\t":"\r\n");
        for (int i=Math.max(0, size-MAXSIZE); i<size; i++) 
            erg += i+"\t"+Statik.matrixToString(history[getIx(i)],5)+"\r\n";
        return erg;
    }
}
