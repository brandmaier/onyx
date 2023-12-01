/*
 * Created on 11.10.2011
 */
package scripts;

import engine.backend.*;
import engine.backend.DyadicIRTModel.PairingStrategy;
import engine.backend.Model.warningFlagTypes;
import machineLearning.clustering.AgglomerativeClustering;
import machineLearning.clustering.LloydsAlgorithm;
import machineLearning.clustering.LloydsAlgorithm.methodType;
import machineLearning.preprocessor.DataPreprocessing;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import arithmetik.AnalyticalFunction;
import arithmetik.AnalyticalPathBalanceFunction;
import arithmetik.FastPolynomial;
import arithmetik.PathTracking;
import arithmetik.QPolynomial;
import arithmetik.Qelement;
import engine.Dataset;

//import arithmetik.AnalyticalFunction;

//import differentiableDoubles.DifferentialDouble;
//import differentiableDoubles.DifferentialMatrix;
import engine.ModelRunUnit;
import engine.OnyxModel;
import engine.OnyxModel.Until;
import engine.RawDataset;
import engine.Statik;
//import gui.actions.LoadDataAction;

public class Scripts {

    public static boolean globalWarning = false;
    
    public static void multiLevelClassSkiing() {
        LinearModel model = new LinearModel(new double[][]{{1,0},{1,1},{0,1}}, new int[][]{{0,1},{1,2}}, new double[][]{{2.0,0},{0,2.0}}, new int[]{-1,-1}, new double[]{0,0}, 3, 1.0);
        model.rand = new Random(1235235234);
        model.createData(200);
        
        double[][] out = new double[model.anzPer * model.anzVar][6];
        int k=0;
        for (int i=0; i<model.anzPer; i++) for (int j=0; j<model.anzVar; j++) {
            out[k][0] = k; 
            out[k][1] = i;
            out[k][2] = j;
            out[k][3] = model.structure[j][0];
            out[k][4] = model.structure[j][1];
            out[k][5] = model.data[i][j];
            k++;
        }
        Statik.writeMatrix(out, "skiingData.txt", '\t', "\tParticipantID\tScoreID\tEye\tSkiing\tValue");
        Statik.writeMatrix(Statik.addRowNumber(model.data), "skiingDataSEM.txt", '\t', "\tArchery\tBiathlon\tDownhill");
        
        double[] estimate = model.estimateML(model.getParameter());
        System.out.println("Direct estimates = "+Statik.matrixToString(estimate)+", ll = "+model.ll);
        estimate[0] = Math.sqrt(estimate[0]);
        estimate[2] = Math.sqrt(estimate[2]);
        estimate[3] = Math.sqrt(estimate[3]);
        estimate[1] = estimate[1] / (estimate[0]*estimate[2]);
        System.out.println("Transformed estimates = "+Statik.matrixToString(estimate));
        System.out.println("-2Log Likelihood = "+model.ll);
        
    }
    
    public static void multiLevelClassACE() {
//        LinearModel monoModel = new LinearModel(new double[][]{{1,1},{1,1}}, new int[][]{{0,5},{5,1}}, new double[][]{{10.0,0},{0,15.0}}, new int[]{3,4}, new double[]{0,0}, 2, 20.0);
        LinearModel monoModel = new LinearModel(new double[][]{{1,1},{1,1}}, new int[][]{{1,2},{2,3}}, new double[][]{{10.0,0},{0,15.0}}, new int[]{4,5}, new double[]{0,0}, 0, 20.0);
        LinearModel biModel = new LinearModel(new double[][]{{0.5,1},{0.5,1}}, monoModel.covPar, monoModel.covVal, monoModel.meanPar, monoModel.meanVal, monoModel.errPar[0], monoModel.errVal[0]);
        LinearModel unModel = new LinearModel(new double[][]{{0,1},{0,1}}, monoModel.covPar, monoModel.covVal, monoModel.meanPar, monoModel.meanVal, monoModel.errPar[0], monoModel.errVal[0]);
        MultiGroupModel model = new MultiGroupModel(new Model[]{monoModel, biModel, unModel}, monoModel.anzVar);
        monoModel.anzPer = biModel.anzPer = unModel.anzPer = 100;
        model.rand = new Random(12352354);
        double[] start = model.getParameter();
        model.createData(300);
//        monoModel.evaluateMuAndSigma(); monoModel.setDataDistribution(monoModel.sigma, monoModel.mu);
//        biModel.evaluateMuAndSigma(); biModel.setDataDistribution(biModel.sigma, biModel.mu);
//        unModel.evaluateMuAndSigma(); unModel.setDataDistribution(unModel.sigma, unModel.mu);

        double[][] out = new double[model.anzPer * model.anzVar][5];
        int k=0; int pb = 0;
        for (int g=0; g<3; g++) for (int i=0; i<model.submodel[g].anzPer; i++) {
            for (int j=0; j<model.submodel[g].anzVar; j++) {
                out[k][0] = k; 
                out[k][1] = pb;
                out[k][2] = j;
                out[k][3] = ((LinearModel)model.submodel[g]).structure[j][0];
                out[k][4] = model.submodel[g].data[i][j];
                k++;
            }
            pb++;
        }
        Statik.writeMatrix(out, "twinData.txt", '\t', "\tTwinPair\tParticipant\tGenetic\tValue");
        
        double[][] out2 = new double[model.anzPer][4];
        k=0; 
        for (int g=0; g<3; g++) for (int i=0; i<model.submodel[g].anzPer; i++) {
            out2[k][0] = k;
            out2[k][1] = ((LinearModel)model.submodel[g]).structure[0][0];
            out2[k][2] = model.submodel[g].data[i][0];
            out2[k][3] = model.submodel[g].data[i][1];
            k++;
        }
        Statik.writeMatrix(out2, "twinDataSEM.txt", '\t', "\tGenetic\tTwin1\tTwin2");
     
//        model.setDataDistribution();
        double[] est = model.estimateML();
        System.out.println(Statik.matrixToString(est));
        System.out.println("-2Log Likelihood = "+model.ll);
//        model.setParameter(new double[]{10.066, 15.309, 19.558, -0.4286, 0.3064}); model.getMinusTwoLogLikelihood();
        System.out.println("-2Log Likelihood = "+model.ll);
        
        double[][] fisher = model.computeFisherMatrix();
//        System.out.println(Statik.matrixToString(fisher));
//        System.out.println(Statik.matrixToString(model.llDD));
        double[][] inv = Statik.invert(fisher);
        for (int i=0; i<model.anzPar; i++) System.out.print(Math.sqrt(inv[i][i])+"\t");
        System.out.println();
        System.out.println("Correlation fixed effects = "+(inv[3][4] / Math.sqrt(inv[3][3]*inv[4][4])));
        System.out.println("Hessian = \r\n"+Statik.matrixToString(model.llDD));
        System.out.println("Hessian inv = \r\n"+Statik.matrixToString(Statik.invert(model.llDD)));
        System.out.println("Check Id = \r\n"+Statik.matrixToString(Statik.multiply(model.llDD,Statik.invert(model.llDD))));
        System.out.println("Eigenvalues = "+Statik.matrixToString(Statik.eigenvalues(model.llDD, 0.001)));
        System.out.println("Determinant = "+Statik.determinant(model.llDD));
        
        double[] stdvs = model.getParameterSTDV();
        System.out.println("Parameter STDV = "+Statik.matrixToString(stdvs));
        
        double[] mean = new double[model.anzPar], var = new double[model.anzPar];
        int trials = 5000;
        for (int i=0; i<trials; i++) {
            model.setParameter(start);
            model.createData(300);
            est = model.estimateML(start);
            if (model.warningFlag != Model.warningFlagTypes.OK) System.out.println("Warning");
            for (int j=0; j<model.anzPar; j++) {mean[j] += est[j]; var[j] += est[j]*est[j];}
        }
        for (int j=0; j<model.anzPar; j++) {mean[j] /= (double)trials; var[j] = Math.sqrt(var[j]/(double)trials - mean[j]*mean[j]);}
        System.out.println("Mean estimates = "+Statik.matrixToString(mean));
        System.out.println("STDV estimates = "+Statik.matrixToString(var));
    }

    public static void multiLevelClassHW6() {
        LinearModel model = new LinearModel(new double[][]{{1,0},{1,1}}, new int[][]{{0,-1},{-1,1}}, new double[][]{{1,0},{0,1.0}}, new int[]{-1,-1}, new double[]{0,0}, -1, 1.0);
        model.setDataDistribution(new double[][]{{3,1},{1,4}}, new double[]{0,0}, 1000);
        double[] est = model.estimateML();
        System.out.println(Statik.matrixToString(est));
    }
    
    public static void multiLevelClassHW7() {
        LinearModel model = new LinearModel(new double[][]{{1,0},{1,0},{0,1},{0,1}}, new int[][]{{0,1},{1,2}}, new double[][]{{2.0,0.8},{0.8,2.0}}, new int[]{-1,-1}, new double[]{0,0}, 3, 0.5);
        model.rand = new Random(345243234);
        model.createData(200);
        double[] est = model.estimateML();
        double[][] dataOutput = new double[4*model.anzPer][]; 
        for (int i=0; i<model.anzPer; i++) {
            dataOutput[4*i+0] = new double[]{4*i+1, i, 1, 0, model.data[i][0]};
            dataOutput[4*i+1] = new double[]{4*i+2, i, 1, 0, model.data[i][1]};
            dataOutput[4*i+2] = new double[]{4*i+3, i, 0, 1, model.data[i][2]};
            dataOutput[4*i+3] = new double[]{4*i+4, i, 0, 1, model.data[i][3]};
        }
        Statik.writeMatrix(dataOutput, "homework7Data.txt", '\t', "\tID\tD\tB\tScore");
        System.out.println("Estimate = "+Statik.matrixToString(est)+", sum = "+(Statik.mean(est)*4));
    }
    
    public static void multiLevelClassLiterature() {
        LinearModel model = new LinearModel(new double[][]{{1,0,0},{1,1,0},{1,0,1}}, new int[][]{{0,1,2},{1,3,4},{2,4,5}}, new double[][]{{2.0,-1.8,0.0},{-1.8,2.0,0.0},{0.0,0.0,0.8}}, new int[]{6,7,8}, new double[]{9,0,0}, -1, 0.0);
        model.rand = new Random(235235234);
        model.createData(1000);
        double[] est = model.estimateML();
        System.out.println(Statik.matrixToString(est)+", -2ll = "+model.ll);
        Statik.writeMatrix(Statik.addRowNumber(model.data), "literatureData.txt", '\t', "\tShakespeare\tTolstoi\tDickens");
    }
  
    public static double[] floParToTimoPar(double[] floPar, boolean useStdv) {
        double[] timoPar = new double[14 + 10 + 15];
        timoPar[0] = floPar[2]; timoPar[1] = floPar[3];
        timoPar[2] = floPar[8]; timoPar[3] = floPar[9]; timoPar[4] = floPar[11]; timoPar[5] = floPar[12]; timoPar[6] = floPar[14]; timoPar[7] = floPar[15];
        timoPar[8] = floPar[26]; timoPar[9] = floPar[28]; timoPar[10] = floPar[30]; 
        timoPar[11] = floPar[38]; timoPar[12] = floPar[40]; timoPar[13] = floPar[42];
        timoPar[14] = floPar[0];
        timoPar[15] = floPar[4] ;timoPar[16] = floPar[5]; timoPar[17] = floPar[6];
        timoPar[18] = floPar[16] ;timoPar[19] = floPar[17]; timoPar[20] = floPar[18];
        timoPar[21] = floPar[22] ;timoPar[22] = floPar[23]; timoPar[23] = floPar[24];
        for (int i=0; i<6; i++) timoPar[24+i] = floPar[43+i];
        for (int i=0; i<3; i++) timoPar[30+i] = floPar[49+2*i]/2.0 + floPar[19+i];
        for (int i=0; i<6; i++) timoPar[33+i] = floPar[55+i];
        if (useStdv) {
            for (int i=14; i<39; i++) timoPar[i] = Math.sqrt(timoPar[i]);
        }
        return timoPar;
    }

    public static double[] floParToTimoParBig(double[] floPar, boolean useStdv) {
        double[] timoPar = new double[14 + 13 + 15];
        timoPar[0] = floPar[2]; timoPar[1] = floPar[3];
        timoPar[2] = floPar[8]; timoPar[3] = floPar[9]; timoPar[4] = floPar[11]; timoPar[5] = floPar[12]; timoPar[6] = floPar[14]; timoPar[7] = floPar[15];
        timoPar[8] = floPar[26]; timoPar[9] = floPar[28]; timoPar[10] = floPar[30]; 
        timoPar[11] = floPar[38]; timoPar[12] = floPar[40]; timoPar[13] = floPar[42];
        timoPar[14] = floPar[0];
        timoPar[15] = floPar[4] ;timoPar[16] = floPar[5]; timoPar[17] = floPar[6];
        timoPar[18] = floPar[16] ;timoPar[19] = floPar[17]; timoPar[20] = floPar[18];
        timoPar[21] = floPar[19] ;timoPar[22] = floPar[20]; timoPar[23] = floPar[21];
        timoPar[24] = floPar[22] ;timoPar[25] = floPar[23]; timoPar[26] = floPar[24];
        for (int i=0; i<6; i++) timoPar[27+i] = floPar[43+i];
        for (int i=0; i<3; i++) timoPar[33+i] = floPar[49+2*i];
        for (int i=0; i<6; i++) timoPar[36+i] = floPar[55+i];
        if (useStdv) {
            for (int i=14; i<42; i++) timoPar[i] = Math.sqrt(timoPar[i]);
        }
        return timoPar;
    }
    
    public static double[] timoParToFloPar(double[] timoParIn, double[]  corr2nd, boolean useStdv) {
        double[] timoPar = Statik.copy(timoParIn);
        if (useStdv) {
            for (int i=14; i<39; i++) timoPar[i] = timoPar[i]*timoPar[i];
        }
        double[] floPar = new double[61];
        floPar[1] = 1;
        floPar[2] = timoPar[0]; floPar[3] = timoPar[1];
        floPar[7] = floPar[10] = floPar[13] = 1;
        floPar[8] = timoPar[2]; floPar[9] = timoPar[3]; floPar[11] = timoPar[4]; floPar[12] = timoPar[5]; floPar[14] = timoPar[6]; floPar[15] = timoPar[7]; 
        floPar[25] = floPar[27] = floPar[29] = floPar[31] = floPar[32] = floPar[33] = floPar[34] = floPar[35] = floPar[36] = floPar[37] = floPar[39] = floPar[41] = 1;
        floPar[26] = timoPar[8]; floPar[28] = timoPar[9]; floPar[30] = timoPar[10];
        floPar[38] = timoPar[11]; floPar[40] = timoPar[12]; floPar[42] = timoPar[13];
        floPar[0] = timoPar[14];
        floPar[4] = timoPar[15]; floPar[5] = timoPar[16]; floPar[6] = timoPar[17];
        floPar[16] = timoPar[18]; floPar[17] = timoPar[19]; floPar[18] = timoPar[20]; 
        floPar[22] = timoPar[21]; floPar[23] = timoPar[22]; floPar[24] = timoPar[23];
        for (int i=0; i<6; i++) floPar[43+i] = timoPar[24+i];
        for (int i=0; i<3; i++) {
            double tot = timoPar[30+i];
            double above = (timoPar[14]*Math.pow(timoPar[0],2)+timoPar[16])*Math.pow((i==0?1:timoPar[3+i]),2);
            floPar[19+i] = (2*tot*corr2nd[i]+corr2nd[i]*above-above) / (corr2nd[i]+1);
            if (floPar[19+i] < 0) {
                System.out.println("Transformation yielded variance below zero at parameter "+(19+i)+", value = "+floPar[19+i]+", I'll set it to zero."); 
                floPar[19+i]=0;
            }
            floPar[49+2*i] = floPar[49+2*i+1] = 2*(tot-floPar[19+i]); 
        }
        for (int i=0; i<6; i++) floPar[55+i] = timoPar[33+i];
        return floPar;
    }
    
    public static double[] timoParBigToFloPar(double[] timoParIn, boolean useStdv) {
        double[] timoPar = Statik.copy(timoParIn);
        if (useStdv) {
            for (int i=14; i<39; i++) timoPar[i] = timoPar[i]*timoPar[i];
        }
        double[] floPar = new double[61];
        floPar[1] = 1;
        floPar[2] = timoPar[0]; floPar[3] = timoPar[1];
        floPar[7] = floPar[10] = floPar[13] = 1;
        floPar[8] = timoPar[2]; floPar[9] = timoPar[3]; floPar[11] = timoPar[4]; floPar[12] = timoPar[5]; floPar[14] = timoPar[6]; floPar[15] = timoPar[7]; 
        floPar[25] = floPar[27] = floPar[29] = floPar[31] = floPar[32] = floPar[33] = floPar[34] = floPar[35] = floPar[36] = floPar[37] = floPar[39] = floPar[41] = 1;
        floPar[26] = timoPar[8]; floPar[28] = timoPar[9]; floPar[30] = timoPar[10];
        floPar[38] = timoPar[11]; floPar[40] = timoPar[12]; floPar[42] = timoPar[13];
        floPar[0] = timoPar[14];
        floPar[4] = timoPar[15]; floPar[5] = timoPar[16]; floPar[6] = timoPar[17];
        floPar[16] = timoPar[18]; floPar[17] = timoPar[19]; floPar[18] = timoPar[20]; 
        floPar[19] = timoPar[21]; floPar[20] = timoPar[22]; floPar[21] = timoPar[23];
        floPar[22] = timoPar[24]; floPar[23] = timoPar[25]; floPar[24] = timoPar[26];
        for (int i=0; i<6; i++) floPar[43+i] = timoPar[27+i];
        for (int i=0; i<3; i++) floPar[49+2*i] = floPar[49+2*i+1] = timoPar[33+i];
        for (int i=0; i<6; i++) floPar[55+i] = timoPar[36+i];
        return floPar;
    }
    
    public static double[] timoParBigToFloParSASSorted(double[] timoParIn, boolean useStdv) {
        double[] timoPar = Statik.copy(timoParIn);
        if (useStdv) {
            for (int i=14; i<39; i++) timoPar[i] = timoPar[i]*timoPar[i];
        }
        double[] floPar = new double[42];
        floPar[0] = timoPar[14];
        floPar[1] = timoPar[0]; floPar[2] = timoPar[1];
        floPar[3] = timoPar[15]; floPar[4] = timoPar[16]; floPar[5] = timoPar[17];
        floPar[6] = timoPar[2]; floPar[7] = timoPar[3]; floPar[8] = timoPar[4]; floPar[9] = timoPar[5]; floPar[10] = timoPar[6]; floPar[11] = timoPar[7]; 

        floPar[12] = timoPar[18]; floPar[13] = timoPar[19]; floPar[14] = timoPar[20]; 
        floPar[15] = timoPar[21]; floPar[16] = timoPar[22]; floPar[17] = timoPar[23];
        floPar[18] = timoPar[24]; floPar[19] = timoPar[25]; floPar[20] = timoPar[26];
        
        floPar[21] = timoPar[8]; floPar[22] = timoPar[9]; floPar[23] = timoPar[10];
        floPar[24] = timoPar[11]; floPar[25] = timoPar[12]; floPar[26] = timoPar[13];
        
        for (int i=0; i<6; i++) floPar[27+i] = timoPar[27+i];
        for (int i=0; i<3; i++) floPar[33+i] = timoPar[33+i];
        for (int i=0; i<6; i++) floPar[36+i] = timoPar[36+i];
        return floPar;
    }
    
    public static void floToFlo() {
        int partNr = 2;
        double[][] floFormatEst = Statik.loadMatrix("FloData"+File.separator+"estimates_"+partNr+".dat", ' ', true);
        double[] floEstFloFormat = Statik.subvector(floFormatEst[0],0);
        double[] sasSorted = timoParBigToFloParSASSorted(floParToTimoParBig(floEstFloFormat, true), true);
        System.out.println(Statik.matrixToString(sasSorted));
    }

    
    public static void collapseCovarianceFlo(double[][] covIn, double[][] covOut, double[] corr) {
        corr[0] = covIn[6][7] / Math.sqrt(covIn[6][6]*covIn[7][7]);
        corr[1] = covIn[8][9] / Math.sqrt(covIn[8][8]*covIn[9][9]);
        corr[2] = covIn[10][11] / Math.sqrt(covIn[10][10]*covIn[11][11]);
        double[][] collapsedCov = Statik.copy(covIn);
        Statik.covarianceUnderEquality(collapsedCov, new int[]{6,7});
        Statik.covarianceUnderEquality(collapsedCov, new int[]{8,9});
        Statik.covarianceUnderEquality(collapsedCov, new int[]{10,11});

        for (int i=0; i<15; i++) for (int j=0; j<15; j++) {
            int oi = i; if (i>=7) oi++; if (i>=8) oi++; if (i>=9) oi++; 
            int oj = j; if (j>=7) oj++; if (j>=8) oj++; if (j>=9) oj++;
            covOut[i][j] = collapsedCov[oi][oj];
        }
    }
    
    private static RAMModel florianFlatFactorModelRAM() {
        // V0: Main Factor
        // V1-V3: 1st level
        // V4-V6, V7-V9, V10-V12: 2nd level
        // V13-V30: manifest
        int pnr = 0;
        int[][] asyPar = new int[31][31]; for (int i=0; i<asyPar.length; i++) for (int j=0; j<asyPar[i].length; j++) asyPar[i][j] = -1;
        for (int i=0; i<18; i++) asyPar[13+i][0] = (i>6 && i<=12 && i%2==1?asyPar[13+i-1][0]:pnr++);
        for (int i=0; i<18; i++) asyPar[13+i][1+i/6] = (i>6 && i<=12 && i%2==1?asyPar[13+i-1][1+i/6]:pnr++);
        for (int i=0; i<18; i++) asyPar[13+i][4+i/2] = (i>6 && i<=12 && i%2==1?asyPar[13+i-1][4+i/2]:pnr++);
        int[][] symPar = new int[31][31]; for (int i=0; i<symPar.length; i++) for (int j=0; j<symPar[i].length; j++) symPar[i][j] = -1;
        for (int i=0; i<6; i++) symPar[13+i][13+i] = pnr++; 
        for (int i=0; i<3; i++) symPar[13+6+2*i][13+6+2*i] = symPar[13+7+2*i][13+7+2*i] = pnr++; 
        for (int i=0; i<6; i++) symPar[13+12+i][13+12+i] = pnr++; 
        double[][] symVal = new double[31][31];
        for (int i=0; i<13; i++) symVal[i][i] = 1.0;
        int[] meanPar = new int[31]; for (int i=0; i<meanPar.length; i++) meanPar[i] = -1;
        double[][] asyVal = new double[31][31]; double[] meanVal =  new double[31];
        int[] filter = new int[18]; for (int i=0; i<18; i++) filter[i] = i+13;
        RAMModel erg = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        return erg;
    }

    private static RAMModel florianNonflatFactorModelRAM() {
        
        // V0: Main Factor
        // V1-V3: 1st level
        // V4-V6, V7-V9, V10-V12: 2nd level
        // V13-V30: manifest
        int pnr = 0;
        int[][] asyPar = new int[31][31]; for (int i=0; i<asyPar.length; i++) for (int j=0; j<asyPar[i].length; j++) asyPar[i][j] = -1;
        for (int i=0; i<3; i++) asyPar[1+i][0] = pnr++;
        for (int i=0; i<9; i++) asyPar[4+i][1+i/3] = pnr++;
        for (int i=0; i<6; i++) asyPar[13+i][4+i/2] = pnr++;
        for (int i=0; i<3; i++) asyPar[19+2*i][7+i] = asyPar[20+2*i][7+i] = pnr++;
        for (int i=0; i<6; i++) asyPar[25+i][10+i/2] = pnr++;
        int[][] symPar = new int[31][31]; for (int i=0; i<symPar.length; i++) for (int j=0; j<symPar[i].length; j++) symPar[i][j] = -1;
        for (int i=0; i<6; i++) symPar[13+i][13+i] = pnr++; 
        for (int i=0; i<3; i++) symPar[13+6+2*i][13+6+2*i] = symPar[13+7+2*i][13+7+2*i] = pnr++; 
        for (int i=0; i<6; i++) symPar[13+12+i][13+12+i] = pnr++; 
        double[][] symVal = new double[31][31];
        for (int i=0; i<4; i++) symVal[i][i] = 1.0;
        double[][] asyVal = new double[31][31]; 
        for (int i=0; i<9; i++) asyVal[13+2*i][4+i] = asyVal[13+1+2*i][4+i] = 1.0;
        int[] meanPar = new int[31]; for (int i=0; i<meanPar.length; i++) meanPar[i] = -1;
        double[] meanVal =  new double[31];
        int[] filter = new int[18]; for (int i=0; i<18; i++) filter[i] = i+13;
        RAMModel erg = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        return erg;
    }

    /**
     * Creates a two-group model as NonflatFactorModelRAM()
     * 
     * @return
     */
    private static RAMModel florianTwoGroupNonflatFactorModelRAM() {
        // V0: Main Factor
        // V1-V3: 1st level
        // V4-V6, V7-V9, V10-V12: 2nd level
        // V13-V30: manifest
        int pnr = 0;
        int[][] asyPar = new int[62][62]; for (int i=0; i<asyPar.length; i++) for (int j=0; j<asyPar[i].length; j++) asyPar[i][j] = -1;
        for (int i=0; i<3; i++) {asyPar[1+i][0] = pnr++; asyPar[31+1+i][31] = pnr++;}
        for (int i=0; i<9; i++) {asyPar[4+i][1+i/3] = pnr++; asyPar[31+4+i][31+1+i/3] = pnr++;}
        for (int i=0; i<6; i++) {asyPar[13+i][4+i/2] = pnr++; asyPar[31+13+i][31+4+i/2] = pnr++;}
        for (int i=0; i<3; i++) {asyPar[19+2*i][7+i] = asyPar[20+2*i][7+i] = pnr++; asyPar[31+19+2*i][31+7+i] = asyPar[31+20+2*i][31+7+i] = pnr++;}
        for (int i=0; i<6; i++) {asyPar[25+i][10+i/2] = pnr++; asyPar[31+25+i][31+10+i/2] = pnr++;}
        int[][] symPar = new int[62][62]; for (int i=0; i<symPar.length; i++) for (int j=0; j<symPar[i].length; j++) symPar[i][j] = -1;
        for (int i=0; i<6; i++) {symPar[13+i][13+i] = pnr++; symPar[31+13+i][31+13+i] = pnr++;}
        for (int i=0; i<3; i++) {symPar[13+6+2*i][13+6+2*i] = symPar[13+7+2*i][13+7+2*i] = pnr++; symPar[31+13+6+2*i][31+13+6+2*i] = symPar[31+13+7+2*i][31+13+7+2*i] = pnr++;}
        for (int i=0; i<6; i++) {symPar[13+12+i][13+12+i] = pnr++; symPar[31+13+12+i][31+13+12+i] = pnr++; } 
        double[][] symVal = new double[62][62];
        for (int i=0; i<13; i++) {symVal[i][i] = 1.0; symVal[31+i][31+i] = 1.0;}
        double[][] asyVal = new double[62][62]; 
        for (int i=0; i<9; i++) {asyVal[13+2*i][4+i] = asyVal[13+1+2*i][4+i] = 1.0; asyVal[31+13+2*i][31+4+i] = asyVal[31+13+1+2*i][31+4+i] = 1.0;}
        int[] meanPar = new int[62]; for (int i=0; i<meanPar.length; i++) meanPar[i] = -1;
        double[] meanVal =  new double[62];
        int[] filter = new int[36]; for (int i=0; i<18; i++) filter[i] = i+13; for (int i=18; i<36; i++) filter[i] = i-18+13+31;
        RAMModel erg = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        return erg;
    }

    /**
     * Creates a two-group model with 3-level factors. Left Model is as usual. Right model has a dummy node for a copy of the top factor and the 2nd level
     * factors, to be used as additional weights to the two right factors below. These can be fixed to zero to test the hypothesis of equal weights ratios. 
     * 
     * @return
     */
    private static RAMModel florianTwoGroupNonflatFactorModelRAMOneWeightFixed() {

        int anzReal = 31, anzDummies = 4, anzFac = 2*anzReal + anzDummies;
        int pnr = 0;
        int[][] asyPar = new int[anzFac][anzFac]; for (int i=0; i<asyPar.length; i++) for (int j=0; j<asyPar[i].length; j++) asyPar[i][j] = Model.NOPARAMETER;
        for (int i=1; i<3; i++) {
            asyPar[1+i][0] = asyPar[anzReal+1+i][anzReal] = pnr++; asyPar[anzReal+1+i][2*anzReal] = pnr++;
        }
        for (int i=0; i<9; i++) if (i%3 != 0) {
            asyPar[4+i][1+i/3] = asyPar[anzReal+4+i][anzReal+1+i/3] = pnr++; asyPar[anzReal+4+i][2*anzReal+1+i/3] = pnr++;
        }
        for (int i=0; i<6; i++) {if (i%2 != 0) {asyPar[13+i][4+i/2] = pnr++; asyPar[anzReal+13+i][anzReal+4+i/2] = pnr++;}}
        for (int i=0; i<6; i++) {if (i%2 != 0) {asyPar[25+i][10+i/2] = pnr++; asyPar[anzReal+25+i][anzReal+10+i/2] = pnr++;}}

        double[][] asyVal = new double[anzFac][anzFac];
        asyVal[1][0] = 1; asyVal[anzReal+1][anzReal] = 1; asyVal[2*anzReal][anzReal] = 1;
        for (int i=0; i<3; i++) {asyVal[4+3*i][1+i] = 1; asyVal[anzReal+4+3*i][anzReal+1+i] = 1; asyVal[2*anzReal+1+i][anzReal+1+i] = 1;}
        for (int i=0; i<9; i++) {asyVal[13+2*i][4+i] = asyVal[13+1+2*i][4+i] = 1.0; asyVal[31+13+2*i][31+4+i] = asyVal[31+13+1+2*i][31+4+i] = 1.0;}
        
        int[][] symPar = new int[anzFac][anzFac]; for (int i=0; i<symPar.length; i++) for (int j=0; j<symPar[i].length; j++) symPar[i][j] = -1;
        for (int i=0; i<13; i++) {symPar[i][i] = pnr++; symPar[anzReal+i][anzReal+i] = pnr++;}
        for (int i=0; i<6; i++) {symPar[13+i][13+i] = pnr++; symPar[anzReal+13+i][anzReal+13+i] = pnr++;}
        for (int i=0; i<3; i++) {symPar[13+6+2*i][13+6+2*i] = symPar[13+7+2*i][13+7+2*i] = pnr++; 
                                 symPar[anzReal+13+6+2*i][31+13+6+2*i] = symPar[anzReal+13+7+2*i][31+13+7+2*i] = pnr++;}
        for (int i=0; i<6; i++) {symPar[13+12+i][13+12+i] = pnr++; symPar[anzReal+13+12+i][anzReal+13+12+i] = pnr++; } 
        double[][] symVal = new double[anzFac][anzFac];
        int[] meanPar = new int[anzFac]; for (int i=0; i<meanPar.length; i++) meanPar[i] = -1;
        double[] meanVal =  new double[anzFac];
        int[] filter = new int[36]; for (int i=0; i<18; i++) filter[i] = i+13; for (int i=18; i<36; i++) filter[i] = i-18+13+anzReal;
        RAMModel erg = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        return erg;
    }

    /**
     * Creates a single-group model with 3-level factors with parameters being standardized weights. 
     * 
     * @return
     */
    private static RAMModel florianFactorModelRAMStandardizedWeights() {

        int anzReal = 13+18, anzDummies = 13, anzFac = anzReal + anzDummies;
        int pnr = 0;
        int[][] asyPar = new int[anzFac][anzFac]; for (int i=0; i<asyPar.length; i++) for (int j=0; j<asyPar[i].length; j++) asyPar[i][j] = Model.NOPARAMETER;
        double[][] asyVal = new double[anzFac][anzFac];
        for (int i=0; i<3; i++) {
            asyPar[1+i][0] = asyPar[1+i][anzReal+1+i] = pnr++; 
        }
        for (int i=0; i<9; i++) {
            asyPar[4+i][1+i/3] = asyPar[4+i][anzReal+4+i] = pnr++; 
        }
        for (int i=0; i<6; i++) {asyPar[13+i][4+i/2] = pnr++; }
        for (int i=0; i<3; i++) {asyPar[19+2*i][7+i] = asyPar[20+2*i][7+i] = pnr++; }
        for (int i=0; i<6; i++) {asyPar[25+i][10+i/2] = pnr++; }

        
        int[][] symPar = new int[anzFac][anzFac]; for (int i=0; i<symPar.length; i++) for (int j=0; j<symPar[i].length; j++) symPar[i][j] = Model.NOPARAMETER;
        double[][] symVal = new double[anzFac][anzFac];
        for (int i=0; i<13; i++) {symVal[i][i] = 1; symVal[anzReal+i][anzReal+i] = -1;}
        for (int i=0; i<6; i++) {symPar[13+i][13+i] = pnr++;}
        for (int i=0; i<3; i++) {symPar[19+2*i][19+2*i] = symPar[20+2*i][20+2*i] = pnr++;}
        for (int i=0; i<6; i++) {symPar[25+i][25+i] = pnr++; } 
        int[] meanPar = new int[anzFac]; for (int i=0; i<meanPar.length; i++) meanPar[i] = Model.NOPARAMETER;
        double[] meanVal =  new double[anzFac];
        int[] filter = new int[18]; for (int i=0; i<18; i++) filter[i] = 13+i; 
        RAMModel erg = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        return erg;
    }

    /**
     * Creates a single-group model with 3-level factors with parameters being standardized weights. Bottom row is treated identical. 
     * 
     * @return
     */
    private static RAMModel florianFactorModelRAMStandardizedWeightsWMTreatedStandard() {

        int anzReal = 13+18, anzDummies = 13, anzFac = anzReal + anzDummies;
        int pnr = 0;
        int[][] asyPar = new int[anzFac][anzFac]; for (int i=0; i<asyPar.length; i++) for (int j=0; j<asyPar[i].length; j++) asyPar[i][j] = Model.NOPARAMETER;
        double[][] asyVal = new double[anzFac][anzFac];
        for (int i=0; i<3; i++) {
            asyPar[1+i][0] = asyPar[1+i][anzReal+1+i] = pnr++; 
        }
        for (int i=0; i<9; i++) {
            asyPar[4+i][1+i/3] = asyPar[4+i][anzReal+4+i] = pnr++; 
        }
        for (int i=0; i<18; i++) {asyPar[13+i][4+i/2] = pnr++; }
        
        int[][] symPar = new int[anzFac][anzFac]; for (int i=0; i<symPar.length; i++) for (int j=0; j<symPar[i].length; j++) symPar[i][j] = Model.NOPARAMETER;
        double[][] symVal = new double[anzFac][anzFac];
        for (int i=0; i<13; i++) {symVal[i][i] = 1; symVal[anzReal+i][anzReal+i] = -1;}
        for (int i=0; i<18; i++) {symPar[13+i][13+i] = pnr++;}
        int[] meanPar = new int[anzFac]; for (int i=0; i<meanPar.length; i++) meanPar[i] = Model.NOPARAMETER;
        double[] meanVal =  new double[anzFac];
        int[] filter = new int[18]; for (int i=0; i<18; i++) filter[i] = 13+i; 
        RAMModel erg = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        return erg;
    }
    
    
    /** 
     * creates a 2-group model with new parameter from a single-group model
     * @param model
     * @return
     */
    private static RAMModel florianDoubleModel(RAMModel model) {return florianDoubleModel(model, true);}
    private static RAMModel florianDoubleModel(RAMModel model, boolean newParameter) {
        int anzPar = model.anzPar, anzVar = model.anzVar, anzFac = model.anzFac; 
        int[][] asyPar = new int[2*anzFac][2*anzFac]; for (int i=0; i<2*anzFac; i++) for (int j=0; j<2*anzFac; j++) asyPar[i][j] = Model.NOPARAMETER;
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) {
            asyPar[i][j] = asyPar[i+anzFac][j+anzFac] = model.asyPar[i][j]; if (newParameter && model.asyPar[i][j]!=Model.NOPARAMETER) asyPar[i+anzFac][j+anzFac] += anzPar;
        }
        double[][] asyVal = new double[2*anzFac][2*anzFac]; 
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) asyVal[i][j] = asyVal[i+anzFac][j+anzFac] = model.asyVal[i][j];
        int[][] symPar = new int[2*anzFac][2*anzFac]; for (int i=0; i<2*anzFac; i++) for (int j=0; j<2*anzFac; j++) symPar[i][j] = Model.NOPARAMETER;
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) {
            symPar[i][j] = symPar[i+anzFac][j+anzFac] = model.symPar[i][j]; if (newParameter && model.symPar[i][j] != Model.NOPARAMETER) symPar[i+anzFac][j+anzFac] += anzPar;
        }
        double[][] symVal = new double[2*anzFac][2*anzFac]; 
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) symVal[i][j] = symVal[i+anzFac][j+anzFac] = model.symVal[i][j];
        int[] meanPar = new int[2*anzFac]; for (int i=0; i<anzFac; i++) {
            meanPar[i] = meanPar[i+anzFac] = model.meanPar[i]; if (newParameter && model.meanPar[i] != Model.NOPARAMETER) meanPar[i+anzFac] += anzPar;
        }
        double[] meanVal = new double[2*anzFac]; for (int i=0; i<anzFac; i++) meanVal[i] = meanVal[i+anzFac] = model.meanVal[i];
        int[] filter =new int[2*anzVar]; for (int i=0; i<anzVar; i++) {filter[i] = model.filter[i]; filter[i+anzVar] = model.filter[i]+anzFac;}
        return new RAMModel(symPar, symVal, asyPar,asyVal, meanPar, meanVal, filter);
    }

    /**
     * par is always the parameter number without fixations. The par in the second half of the model is searched, and the edge from the 
     * lower part of the model is copied, and par2 mad to be additive to that. 
     *  
     * @param model
     * @param par1
     * @return
     */
    private static RAMModel florianAddEqualtiyConstraintInAsymetric(RAMModel model, int par, int firstParInSecondHalf, int factorPerGroup) {
        int anzPar = model.anzPar, anzVar = model.anzVar, anzFac = model.anzFac;
        
        int par2 = par + firstParInSecondHalf;
        int extra = 0; for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (model.asyPar[i][j]==par2) extra++;
        int[] to = new int[extra], from = new int[extra]; 
        int k=0; for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) if (model.asyPar[i][j]==par2) {to[k] = i; from[k] = j; k++;}
        int[][] asyPar = new int[anzFac+extra][anzFac+extra]; Statik.setTo(asyPar, Model.NOPARAMETER); Statik.copy(model.asyPar, asyPar);
        double[][] asyVal = new double[anzFac+extra][anzFac+extra]; Statik.copy(model.asyVal, asyVal);
        for (int i=0; i<extra; i++) {
            asyVal[anzFac+i][from[i]] = 1; 
            asyPar[to[i]][anzFac+i] = par2; 
            asyPar[to[i]][from[i]] = asyPar[to[i]-factorPerGroup][from[i]-factorPerGroup];
            asyVal[to[i]][from[i]] = asyVal[to[i]-factorPerGroup][from[i]-factorPerGroup];
        }
        int[][] symPar = new int[anzFac+extra][anzFac+extra]; Statik.setTo(symPar, Model.NOPARAMETER); Statik.copy(model.symPar, symPar);
        double[][] symVal = new double[anzFac+extra][anzFac+extra]; Statik.copy(model.symVal, symVal);
        int[] meanPar = new int[anzFac+extra]; Statik.setTo(meanPar, Model.NOPARAMETER); Statik.copy(model.meanPar, meanPar);
        double[] meanVal = new double[anzFac+extra]; Statik.copy(model.meanVal, meanVal);
        return new RAMModel(symPar,symVal, asyPar, asyVal, meanPar, meanVal, model.filter);
    }
    
    
    /**
     * Creates a two-group model with simple factor structure. 
     * 
     * @return
     */
    private static RAMModel florianDebugTwoGroupSimpleFactorModel() {

        int anzReal = 4, anzDummies = 1, anzFac = 2*anzReal + anzDummies;
        int pnr = 0;
        int[][] asyPar = new int[anzFac][anzFac]; for (int i=0; i<asyPar.length; i++) for (int j=0; j<asyPar[i].length; j++) asyPar[i][j] = Model.NOPARAMETER;
        for (int i=1; i<3; i++) {
            asyPar[1+i][0] = asyPar[anzReal+1+i][anzReal] = pnr++; asyPar[anzReal+1+i][2*anzReal] = pnr++;
        }

        double[][] asyVal = new double[anzFac][anzFac];
        asyVal[1][0] = 1; asyVal[anzReal+1][anzReal] = 1; asyVal[2*anzReal][anzReal] = 1;
        
        int[][] symPar = new int[anzFac][anzFac]; for (int i=0; i<symPar.length; i++) for (int j=0; j<symPar[i].length; j++) symPar[i][j] = -1;
        for (int i=0; i<4; i++) {symPar[i][i] = pnr++; symPar[anzReal+i][anzReal+i] = pnr++;}
        double[][] symVal = new double[anzFac][anzFac];
        int[] meanPar = new int[anzFac]; for (int i=0; i<meanPar.length; i++) meanPar[i] = -1;
        double[] meanVal =  new double[anzFac];
        int[] filter = new int[6]; for (int i=0; i<3; i++) {filter[i] = i+1; filter[3+i] = 3+i+2;}
        RAMModel erg = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        return erg;
    }

    
    private static RAMModel florianFlatFactorModelRAMPairedCovariance() {
        // V0: Main Factor
        // V1-V3: 1st level
        // V4-V21: manifest
        int pnr = 0;
        int[][] asyPar = new int[22][22]; for (int i=0; i<asyPar.length; i++) for (int j=0; j<asyPar[i].length; j++) asyPar[i][j] = -1;
        for (int i=0; i<18; i++) asyPar[4+i][0] = (i>6 && i<=12 && i%2==1?asyPar[4+i-1][0]:pnr++);
        for (int i=0; i<18; i++) asyPar[4+i][1+i/6] = (i>6 && i<=12 && i%2==1?asyPar[4+i-1][1+i/6]:pnr++);
        int[][] symPar = new int[22][22]; for (int i=0; i<symPar.length; i++) for (int j=0; j<symPar[i].length; j++) symPar[i][j] = -1;
        for (int i=0; i<9; i++) symPar[4+2*i][4+1+2*i] = symPar[4+1+2*i][4+2*i] = pnr++; 
        for (int i=0; i<6; i++) symPar[4+i][4+i] = pnr++; 
        for (int i=0; i<3; i++) symPar[4+6+2*i][4+6+2*i] = symPar[4+7+2*i][4+7+2*i] = pnr++; 
        for (int i=0; i<6; i++) symPar[4+12+i][4+12+i] = pnr++; 
        double[][] symVal = new double[22][22];
        for (int i=0; i<4; i++) symVal[i][i] = 1.0;
        int[] meanPar = new int[22]; for (int i=0; i<meanPar.length; i++) meanPar[i] = -1;
        double[][] asyVal = new double[22][22]; double[] meanVal =  new double[22];
        int[] filter = new int[18]; for (int i=0; i<18; i++) filter[i] = i+4;
        RAMModel erg = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        return erg;
    }
    
    
    private static RAMModel florianFlatFactorModelRAMNoEqualityConstraints() {
        // V0: Main Factor
        // V1-V3: 1st level
        // V4-V6, V7-V9, V10-V12: 2nd level
        // V13-V30: manifest
        int pnr = 0;
        int[][] asyPar = new int[31][31]; for (int i=0; i<asyPar.length; i++) for (int j=0; j<asyPar[i].length; j++) asyPar[i][j] = -1;
        for (int i=0; i<18; i++) asyPar[13+i][0] = pnr++;
        for (int i=0; i<18; i++) asyPar[13+i][1+i/6] = pnr++;
        for (int i=0; i<18; i++) asyPar[13+i][4+i/2] = pnr++;
        int[][] symPar = new int[31][31]; for (int i=0; i<symPar.length; i++) for (int j=0; j<symPar[i].length; j++) symPar[i][j] = -1;
        for (int i=0; i<6; i++) symPar[13+i][13+i] = pnr++; 
        for (int i=0; i<6; i++) symPar[13+6+i][13+6+i] = pnr++; 
        for (int i=0; i<6; i++) symPar[13+12+i][13+12+i] = pnr++; 
        double[][] symVal = new double[31][31];
        for (int i=0; i<13; i++) symVal[i][i] = 1.0;
        int[] meanPar = new int[31]; for (int i=0; i<meanPar.length; i++) meanPar[i] = -1;
        double[][] asyVal = new double[31][31]; double[] meanVal =  new double[31];
        int[] filter = new int[18]; for (int i=0; i<18; i++) filter[i] = i+13;
        RAMModel erg = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        return erg;
    }

    
    public static void florianFlatModelTesting() {
        Model model = florianFlatFactorModelRAM();
//        Model model = florianFlatFactorModel();
        int anzVar = model.anzVar;
        double[][] erg = new double[101][84];
        
//        model.logStream = System.out;
        int[] allButOne = new int[anzVar]; for (int i=0; i<anzVar; i++) allButOne[i] = i+1;
        for (int partNr = 3; partNr <=3; partNr++) {
            System.out.println("Starting with Participant "+partNr);
            long timestamp = System.nanoTime(); 
            
            double[][] orgDataWithID = Statik.loadMatrix("FloData"+File.separator+"person_"+partNr+".dat", ' ', true);
            double[][] orgData = Statik.submatrix(orgDataWithID, null, allButOne);
            Statik.replace(orgData, 0.0, Model.MISSING);
            double[][] orgCov = new double[anzVar][anzVar]; double[] orgMean = new double[anzVar];
            Statik.covarianceMatrixAndMeans(orgData, orgMean, orgCov, Model.MISSING);
            double[][] cor = Statik.correlationFromCovariance(orgCov);
            for (int i=0; i<orgData.length; i++) for (int j=0; j<orgData[i].length; j++) if (!Model.isMissing(orgData[i][j])) orgData[i][j] -= orgMean[j];
            model.setData(orgData);
            double[] ar = model.getArbitraryStartingValues();
            for (int i=0; i<ar.length; i++) if (ar[i] > 2) ar[i] = ar[i]*ar[i];
            model.setParameter(ar);
            double ll = model.getMinusTwoLogLikelihood();

            model.setMaximalNumberOfIterations(200);
            System.out.println("Starting estimation");
            double[] vals = model.estimateML(model.getArbitraryStartingValues(),0.001);
            System.out.println("Estimation done, time = "+((System.nanoTime()-timestamp)/1000000)+" ms.");
            System.out.println("LogH Estimates = \t"+Statik.matrixToString(vals));
            double estLL = model.ll; 

            int anzPar = model.anzPar;
            double[][] eigenVec = Statik.identityMatrix(anzPar);
            double[] evHess = new double[anzPar];
            Statik.eigenvalues(model.llDD, 0.0001, evHess, eigenVec);
//            Arrays.sort(evHess);
            System.out.println("Eigenvalues Hessian = "+Statik.matrixToString(evHess,5));
            System.out.println(Statik.matrixToString(Statik.transpose(eigenVec)));
            
            
            int c = 0;
            int[] hypoPara = null;
            for (int fixRow = 0; fixRow < 3; fixRow++) for (int fixCol=0; fixCol < (fixRow==0?1:(fixRow==1?3:9)); fixCol++) {
                if (fixRow == 0) hypoPara = new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14};
                if (fixRow == 1) {if (fixCol ==0) hypoPara = new int[]{15,16,17,18,19,20};
                             else if (fixCol ==1) hypoPara = new int[]{21,22,23};
                             else if (fixCol ==2) hypoPara =new int[]{24,25,26,27,28,29};
                }
                if (fixRow == 2) {if (fixCol < 3) hypoPara = new int[]{30+2*fixCol,31+2*fixCol+1};
                             else if (fixCol >= 3 && fixCol < 6) hypoPara = new int[]{33+fixCol};
                             else if (fixCol >= 6) hypoPara =new int[]{39+2*(fixCol-6),39+2*(fixCol-6)+1};
                }
                double[] resStart = Statik.subvector(vals, hypoPara, false);
                Model resModel = model.copy();
                resModel.fixParameter(hypoPara, new double[hypoPara.length]);
                resModel.setData(orgData);
                resModel.estimateML(resStart,0.0001);

                model.setParameter(vals);
                double[] chiMultiplier = model.computeMisspecification(hypoPara, false, vals, false);
                for (int i=0; i<chiMultiplier.length; i++) chiMultiplier[i] = Math.abs(chiMultiplier[i]);
                Arrays.sort(chiMultiplier);
                int df = hypoPara.length;
                double[] nonNullMultiplier = new double[df]; for (int i=0; i<df; i++) nonNullMultiplier[i] = chiMultiplier[chiMultiplier.length-1-i];
                double crit = Statik.inverseMixtureOfChisquares(0.95, nonNullMultiplier, Model.staticRandom);
                
                double lr = resModel.ll - estLL;
                System.out.println(fixRow+"\t"+fixCol+"\t"+crit+"\t"+Statik.FIVEPERCENTTHRESHOLD[hypoPara.length-1]+"\t"+lr+"\t"+
                        (lr>Statik.FIVEPERCENTTHRESHOLD[hypoPara.length-1])+"\t"+(lr>crit)+"\t"+
                        Statik.matrixToString(Statik.subvector(nonNullMultiplier, new int[]{0,1,2,3,4})));
                erg[partNr-1][c*2] = lr; erg[partNr-1][c*2+1] = crit;
                Statik.writeMatrix(erg, "FloData"+File.separator+"result.txt", '\t');
                c++;
            }
            
        }
    }
    public static void florianNonFlatFlatModelTesting(int startPer) {
        Model model = florianNonflatFactorModelRAM();
//        Model model = florianFlatFactorModel();
        int anzVar = model.anzVar;
        double[][] erg = new double[101][84];
        
//        model.logStream = System.out;
        int[] allButOne = new int[anzVar]; for (int i=0; i<anzVar; i++) allButOne[i] = i+1;
        for (int partNr = startPer; partNr <=101; partNr++) {
            System.out.println("Starting with Participant "+partNr);
            long timestamp = System.nanoTime(); 
            
            double[][] orgDataWithID = Statik.loadMatrix("FloData"+File.separator+"person_"+partNr+".dat", ' ', true);
            double[][] orgData = Statik.submatrix(orgDataWithID, null, allButOne);
            Statik.replace(orgData, 0.0, Model.MISSING);
            double[][] orgCov = new double[anzVar][anzVar]; double[] orgMean = new double[anzVar];
            Statik.covarianceMatrixAndMeans(orgData, orgMean, orgCov, Model.MISSING);
            double[][] cor = Statik.correlationFromCovariance(orgCov);
            for (int i=0; i<orgData.length; i++) for (int j=0; j<orgData[i].length; j++) if (!Model.isMissing(orgData[i][j])) orgData[i][j] -= orgMean[j];
//            model.setData(orgData);
            model.setDataDistribution(cor, new double[model.anzVar], orgData.length);
            double[] ar = model.getArbitraryStartingValues();
            for (int i=0; i<ar.length; i++) if (ar[i] > 2) ar[i] = ar[i]*ar[i];
            model.setParameter(ar);
            double ll = model.getMinusTwoLogLikelihood();

            model.setMaximalNumberOfIterations(200);
            System.out.println("Starting estimation");
            double[] vals = model.estimateML(model.getArbitraryStartingValues(),0.001);
            System.out.println("Estimation done, time = "+((System.nanoTime()-timestamp)/1000000)+" ms.");
            System.out.println("LogH Estimates = \t"+Statik.matrixToString(vals,5));
            double estLL = model.ll; 

            int anzPar = model.anzPar;
            double[][] eigenVec = Statik.identityMatrix(anzPar);
            double[] evHess = new double[anzPar];
            Statik.eigenvalues(model.llDD, 0.0001, evHess, eigenVec);
//            Arrays.sort(evHess);
            System.out.println("Eigenvalues Hessian = "+Statik.matrixToString(evHess,5));
            System.out.println(Statik.matrixToString(Statik.transpose(eigenVec)));
            
            
            int c = 0;
            int[] hypoPara = null;
            for (int fixRow = 0; fixRow < 3; fixRow++) for (int fixCol=0; fixCol < (fixRow==0?1:(fixRow==1?3:9)); fixCol++) {
                if (fixRow == 0) hypoPara = new int[]{0,1,2};
                if (fixRow == 1) {if (fixCol ==0) hypoPara = new int[]{0,3,4,5};
                             else if (fixCol ==1) hypoPara = new int[]{1,6,7,8};
                             else if (fixCol ==2) hypoPara =new int[]{2,9,10,11};
                }
                if (fixRow == 2) {
                    if (fixCol < 3) hypoPara = new int[]{3+fixCol,12+2*fixCol,13+2*fixCol};
                    else if (fixCol < 6) hypoPara = new int[]{3+fixCol,12+fixCol};
                    else hypoPara = new int[]{3+fixCol,9+2*fixCol,10+2*fixCol};
                }
                double[] resStart = Statik.subvector(vals, hypoPara, false);
                Model resModel = model.copy();
                resModel.fixParameter(hypoPara, new double[hypoPara.length]);
//                resModel.setData(orgData);
                resModel.setDataDistribution(cor, new double[model.anzVar], orgData.length);
                double[] resEst = resModel.estimateML(resStart,0.0001);
//                if (fixRow == 1 && fixCol == 2) System.out.println(Statik.matrixToString(resEst,5));

                model.setParameter(vals);
                double[] chiMultiplier = model.computeMisspecification(hypoPara, false, vals, false);
                for (int i=0; i<chiMultiplier.length; i++) chiMultiplier[i] = Math.abs(chiMultiplier[i]);
                Arrays.sort(chiMultiplier);
                int df = hypoPara.length;
                double[] nonNullMultiplier = new double[df]; for (int i=0; i<df; i++) nonNullMultiplier[i] = chiMultiplier[chiMultiplier.length-1-i];
                double crit = Statik.inverseMixtureOfChisquares(0.95, nonNullMultiplier, Model.staticRandom);
                
                double lr = resModel.ll - estLL;
                System.out.println(fixRow+"\t"+fixCol+"\t"+crit+"\t"+Statik.FIVEPERCENTTHRESHOLD[hypoPara.length-1]+"\t"+lr+"\t"+
                        (lr>Statik.FIVEPERCENTTHRESHOLD[hypoPara.length-1])+"\t"+(lr>crit)+"\t"+
                        Statik.matrixToString(Statik.subvector(nonNullMultiplier, new int[]{0,1,2,3,4})));
                erg[partNr-1][c*2] = lr; erg[partNr-1][c*2+1] = crit;
                Statik.writeMatrix(erg, "FloData"+File.separator+"result.txt", '\t');
                c++;
            }
            
        }
    }
    
    private static double[] florianTranslateEstimates(double[] usual) {
        double[] old = Statik.copy(usual);
        double[] erg = new double[usual.length];
        for (int i=0; i<3; i++) erg[36+2*i] = old[24+4*i]*old[24+4*i];
        for (int i=0; i<3; i++) erg[37+2*i] = old[25+4*i]*old[25+4*i];
        for (int i=0; i<3; i++) erg[42+2*i] = old[36+2*i]*old[36+2*i];
        for (int i=0; i<3; i++) erg[43+2*i] = old[37+2*i]*old[37+2*i];
        for (int i=0; i<3; i++) erg[48+2*i] = old[42+4*i]*old[42+4*i];
        for (int i=0; i<3; i++) erg[49+2*i] = old[43+4*i]*old[43+4*i];
        for (int i=0; i<3; i++) {old[6+2*i] *= old[24+4*i]; old[26+4*i] /= old[24+4*i];}
        for (int i=0; i<3; i++) {old[7+2*i] *= old[25+4*i]; old[27+4*i] /= old[25+4*i];}
        for (int i=0; i<3; i++) old[12+2*i]*= old[36+2*i];
        for (int i=0; i<3; i++) old[13+2*i]*= old[37+2*i];
        for (int i=0; i<3; i++) {old[18+2*i]*= old[42+4*i]; old[44+4*i] /= old[42+4*i];}
        for (int i=0; i<3; i++) {old[19+2*i]*= old[43+4*i]; old[45+4*i] /= old[43+4*i];}
        for (int i=0; i<3; i++) erg[30+2*i] = old[6+6*i]*old[6+6*i];
        for (int i=0; i<3; i++) erg[31+2*i] = old[7+6*i]*old[7+6*i];
        for (int i=0; i<3; i++) {old[0+2*i] *= old[6+6*i]; old[8+6*i] /= old[6+6*i]; old[10+6*i] /= old[6+6*i];}
        for (int i=0; i<3; i++) {old[1+2*i] *= old[7+6*i]; old[9+6*i] /= old[7+6*i]; old[11+6*i] /= old[7+6*i];}
        erg[28] = old[0]*old[0]; erg[29] = old[1]*old[1];
        old[2] /= old[0]; old[3] /= old[1]; old[4] /= old[0]; old[5] /= old[1];
        for (int i=0; i<4; i++) erg[i] = old[2+i];
        for (int i=0; i<4; i++) erg[4+i] = old[8+i];
        for (int i=0; i<4; i++) erg[8+i] = old[14+i];
        for (int i=0; i<4; i++) erg[12+i] = old[20+i];
        for (int i=0; i<2; i++) erg[16+i] = old[26+i];
        for (int i=0; i<2; i++) erg[18+i] = old[30+i];
        for (int i=0; i<2; i++) erg[20+i] = old[34+i];
        for (int i=0; i<2; i++) erg[22+i] = old[44+i];
        for (int i=0; i<2; i++) erg[24+i] = old[48+i];
        for (int i=0; i<2; i++) erg[26+i] = old[52+i];
        
        for (int i=0; i<30; i++) erg[54+i] = old[54+i];
        
        for (int i=0; i<8; i++) erg[1+2*i] = erg[1+2*i]-erg[2*i];
        return erg;
    }

    /**
     * Multi Group Comparison of within and between structure. 
     * @param startPer
     */
    public static void florianNonFlatFlatModelTestingBetweenVsWithin(int startPer) {
        
        final double PRECISION = 0.00001;
        
        Model model = florianTwoGroupNonflatFactorModelRAMOneWeightFixed();
//        Model model2 = florianTwoGroupNonflatFactorModelRAM();
        int anzVar = model.anzVar;

        int[][] hypoPara = new int[][]{{1,3},{1,3,5,7,9}};
        double[][] erg = new double[101][2*hypoPara.length+1];
        
        
//        model.logStream = System.out;
        int[] allButOne = new int[anzVar/2]; for (int i=0; i<anzVar/2; i++) allButOne[i] = i+1;
        for (int partNr = startPer; partNr <=101; partNr++) {
            System.out.println("Starting with Participant "+partNr);
            long timestamp = System.nanoTime(); 

            // Check for local overspecification and correction
            model = florianTwoGroupNonflatFactorModelRAMOneWeightFixed();
            if (partNr == 1) {
//              model.fixParameter(new int[]{10,50}, new double[]{11.527, 5.633}); 
//                model.fixParameter(new int[]{10,50}, new double[]{11.54, 66.55}); 
//                model.fixParameter(new int[]{10,50}, new double[]{1, 1}); 
//                hypoPara = new int[][]{{1,3},{1,3,5,7,9}};
            }

            double[][] wthnDataWithID = Statik.loadMatrix("FloData"+File.separator+"person_"+partNr+".dat", ' ', true);
            double[][] wthnData = Statik.submatrix(wthnDataWithID, null, allButOne);
            Statik.replace(wthnData, 0.0, Model.MISSING);
            double[][] wthnCov = new double[anzVar/2][anzVar/2]; double[] wthnMean = new double[anzVar/2];
            Statik.covarianceMatrixAndMeans(wthnData, wthnMean, wthnCov, Model.MISSING);
            double[][] wthnCor = Statik.correlationFromCovariance(wthnCov);
            for (int i=0; i<wthnData.length; i++) for (int j=0; j<wthnData[i].length; j++) if (!Model.isMissing(wthnData[i][j])) wthnData[i][j] -= wthnMean[j];

            double[][] btwDataWithID = Statik.loadMatrix("FloData"+File.separator+"between_"+partNr+".dat", ' ', true);
            double[][] btwData = Statik.submatrix(btwDataWithID, null, allButOne);
            Statik.replace(btwData, 0.0, Model.MISSING);
            double[][] btwCov = new double[anzVar/2][anzVar/2]; double[] btwMean = new double[anzVar/2];
            Statik.covarianceMatrixAndMeans(btwData, btwMean, btwCov, Model.MISSING);
            double[][] btwCor = Statik.correlationFromCovariance(btwCov);
            for (int i=0; i<btwData.length; i++) for (int j=0; j<btwData[i].length; j++) if (!Model.isMissing(btwData[i][j])) btwData[i][j] -= btwMean[j];

            double[][] jointCor = new double[btwCor.length+wthnCor.length][btwCor.length+wthnCor.length];
            for (int i=0; i<btwCor.length; i++) for (int j=0; j<btwCor.length; j++) {
                jointCor[i][j] = wthnCor[i][j]; jointCor[i+wthnCor.length][j+wthnCor.length] = btwCor[i][j];
            }

            double[] debugValues = new double[]{2,2,3,3,4,4,2,2,1,1,1,1,2,2,1,1,1,1,2,2,1,1,1,1,3,3,1,1,3,3,1,1,3,3,1,1,2,2,2,2,2,2,3,3,1,1,3,3,1,1,3,3,1,1,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5};

            model.setDataDistribution(jointCor, new double[model.anzVar], wthnData.length);
//            model2.setDataDistribution(jointCor, new double[model.anzVar], wthnData.length);
//            double[] starting2 = model2.getArbitraryStartingValues();
            double[] starting = model.getArbitraryStartingValues(); 
            for (int i=0; i<8; i++) starting[1+2*i] = 0;
            model.setParameter(starting); // model2.setParameter(starting2);
            double ll = model.getMinusTwoLogLikelihood();

            model.setMaximalNumberOfIterations(200);
            System.out.println("Starting estimation");
            double[] vals = model.estimateML(starting,PRECISION);
//            double[] vals = starting;
            System.out.println("Estimation done, time = "+((System.nanoTime()-timestamp)/1000000)+" ms.");
            System.out.println("LogH Estimates = \t"+Statik.matrixToString(vals,5));
            double estLL = model.ll; 
            System.out.println("Starting ll = "+ll+", Final ll = "+estLL);
            erg[partNr-1][0] = estLL;

            int anzPar = model.anzPar;
            
            double[][] kernel = new double[anzPar][anzPar];
            System.out.println("Hessian = "+Statik.matrixToMapleString(model.llDD,6));
            int kernelDim = Statik.kernel(model.llDD, kernel, 0.002);
            if (kernelDim > 0) {
                System.out.println(kernelDim+" dimensional kernel of Hessian found, \r\n"+Statik.matrixToString(kernel));
            }
            
            
//            double[][] eigenVec = Statik.identityMatrix(anzPar);
//            double[] evHess = new double[anzPar];
//            Statik.eigenvalues(model.llDD, 0.0001, evHess, eigenVec);
//            Arrays.sort(evHess);
//            System.out.println("Eigenvalues Hessian = "+Statik.matrixToString(evHess,5));
//            System.out.println(Statik.matrixToString(Statik.transpose(eigenVec)));
            
            for (int c=0; c<hypoPara.length; c++) {
                double[] resStart = Statik.subvector(vals, hypoPara[c], false);
                Model resModel = model.copy();
                resModel.fixParameter(hypoPara[c], new double[hypoPara[c].length]);
                resModel.setDataDistribution(jointCor, new double[model.anzVar], wthnData.length);
                ll = resModel.getMinusTwoLogLikelihood();
                timestamp = System.nanoTime();
                double[] resEst = resModel.estimateML(resStart,PRECISION);
//                if (fixRow == 1 && fixCol == 2) System.out.println(Statik.matrixToString(resEst,5));
                System.out.println("Estimation done, time = "+((System.nanoTime()-timestamp)/1000000)+" ms.");
                System.out.println("restricted Estimates = \t"+Statik.matrixToString(resEst,5));
                System.out.println("Starting ll = "+ll+", Final ll = "+resModel.ll);

                model.setParameter(vals);
                timestamp = System.nanoTime();
                double[] chiMultiplier = model.computeMisspecification(hypoPara[c], false, vals, false);
                System.out.println("chi multiplier computed, time = "+((System.nanoTime()-timestamp)/1000000)+" ms.");
                System.out.println("Multiplier = "+Statik.matrixToString(chiMultiplier));
                for (int i=0; i<chiMultiplier.length; i++) chiMultiplier[i] = Math.abs(chiMultiplier[i]);
                Arrays.sort(chiMultiplier);
                int df = hypoPara[c].length;
                double[] nonNullMultiplier = new double[df]; for (int i=0; i<df; i++) nonNullMultiplier[i] = chiMultiplier[chiMultiplier.length-1-i];
                double crit = Statik.inverseMixtureOfChisquares(0.95, nonNullMultiplier, Model.staticRandom);
                
                double lr = resModel.ll - estLL;
                System.out.println(c+"\t"+crit+"\t"+Statik.FIVEPERCENTTHRESHOLD[df-1]+"\t"+lr+"\t"+
                        (lr>Statik.FIVEPERCENTTHRESHOLD[df-1])+"\t"+(lr>crit)+"\t"+
                        Statik.matrixToString(Statik.subvector(nonNullMultiplier, new int[]{0,1,2,3,4})));
                erg[partNr-1][1+c*2] = lr; erg[partNr-1][c*2+2] = crit;
                Statik.writeMatrix(erg, "FloData"+File.separator+"resultBtwWhtn.txt", '\t');
            }
        }
    }
    
    private static double florianGetValueForParameterBeforeFixation(RAMModel model, int pnr) {
        return model.asyVal[pnr+1][(pnr<3?0:1+(pnr-3)/3)];
    }
    
    /**
     * removes negative signs in the upper estimates. Changes model asyVal, but changes on fixed parameters (if fixed to positive) will always be canceled.
     * 
     * @param model
     * @param est
     * @param add
     */
    private static void florianVorzeichenBuersten(RAMModel model, double[] est, int add) {
        for (int pnr = 0; pnr < 12; pnr++) {
            int top = add + (pnr<3?0:1+(pnr-3)/3);
            int mid = add + pnr+1;
            int bot = add + (pnr<3?4+pnr*3:13+pnr*2);
            if (model.asyVal[mid][top] < 0) {
                model.asyVal[mid][top] *= -1; if (model.asyPar[mid][top] != Model.NOPARAMETER) est[model.asyPar[mid][top]] *= -1;
                for (int i=0; i<(pnr<3?3:2); i++) {
                    model.asyVal[bot+i][mid] *= -1; if (model.asyPar[bot+i][mid] != Model.NOPARAMETER) est[model.asyPar[bot+i][mid]] *= -1;
                }
            }
        }
    }
    
    /**
     * Takes the raw_person data and computes the differences between subsequent items. Missing in one summand leads to missing. Data lines are considered equal-distance steps (different days are ignored). Stores all files in "diff_person"
     */
    public static void florianComputerDifferenceData() {
        for (int partNr = 1; partNr <=101; partNr++) {
            
            double[][] wthnDataWithID = Statik.loadMatrix("FloData"+File.separator+"person_"+partNr+".dat", ' ', true);
            int anzVar = wthnDataWithID[0].length - 1, anzLine = wthnDataWithID.length;
            Statik.replace(wthnDataWithID, 0.0, Model.MISSING);
            double[][] diffData = new double[wthnDataWithID.length][anzVar+1];
            for (int i=0; i<anzLine-1; i++) {
                diffData[i][0] = wthnDataWithID[i][0];
                for (int j=1; j<anzVar+1; j++) {
                    if (wthnDataWithID[i][j] == Model.MISSING || wthnDataWithID[i+1][j] == Model.MISSING) diffData[i][j] = Model.MISSING;
                    else diffData[i][j] = wthnDataWithID[i+1][j] - wthnDataWithID[i][j];
                }
            }
            Statik.writeMatrix(diffData, "FloData"+File.separator+"diff_person_"+partNr+".dat", '\t');
            
        }
        
    }
    
    
    /**
     * Creates a model for two participants, fits it and computes the RMSEA value.
     * @param participant
     */
    public static double florianCompareRMSEABetweenRowCorrections(int participant, String name, boolean differenceScore) {
        final double PRECISION = 0.001;
//        final String withinDataName = "diff_person";
//        final String withinDataName = "person";
        final String withinDataName = name;
      
        RAMModel model1 = florianFactorModelRAMStandardizedWeights();
        Model model = florianDoubleModel(model1, true);
        int anzVar = model.anzVar; int anzVarGroup = anzVar/2;
        int[] allButOne = new int[anzVarGroup]; for (int i=0; i<anzVarGroup; i++) allButOne[i] = i+1;
        System.out.println(withinDataName+" df    = "+model.getRestrictedDF());
        System.out.println(withinDataName+" par   = "+model.anzPar);

        int partNr = participant;
        double[][] wthnDataWithID = Statik.loadMatrix("FloData"+File.separator+withinDataName+"_"+partNr+".dat", ' ', true, ".");
//        double[][] wthnDataWithID = Statik.loadMatrix("FloData"+File.separator+withinDataName+"_"+partNr+".dat", '\t', true);
        double[][] wthnData = Statik.submatrix(wthnDataWithID, null, allButOne);
//        Statik.replace(wthnData, 0.0, Model.MISSING);
        
        if (differenceScore) wthnData = Statik.getDifferenceScores(wthnData, Model.MISSING);

        double[][] wthnCov = new double[anzVarGroup][anzVarGroup]; double[] wthnMean = new double[anzVarGroup];
        Statik.covarianceMatrixAndMeans(wthnData, wthnMean, wthnCov, Model.MISSING);
        double[][] wthnCor = Statik.correlationFromCovariance(wthnCov);
        double det = Statik.determinantOfPositiveDefiniteMatrix(wthnCor);
        System.out.println("Data determinant = "+det);
        
        int anzPer = wthnData.length;
        double[][] doubleData = new double[anzPer/2][anzVar];
        for (int i=0; i<anzPer/2; i++) 
            for (int j=0; j<anzVarGroup; j++) {doubleData[i][j] = wthnData[i*2][j]; doubleData[i][anzVarGroup+j] = wthnData[i*2+1][j];}
        double[][] dobCov = new double[anzVar][anzVar]; double[] dobMean = new double[anzVar];
        Statik.covarianceMatrixAndMeans(doubleData, dobMean, dobCov, Model.MISSING);
        double[][] dobCovCheck = Statik.covarianceMatrix(doubleData);
        double[][] dobCor = Statik.correlationFromCovariance(dobCov);
        double[][] dobCorCheck = Statik.correlationFromCovariance(dobCovCheck);
       

        // remove when including between row miss-specification
        // for (int i=0; i<anzVarGroup; i++) for (int j=0; j<anzVarGroup; j++) dobCor[i+anzVarGroup][j] = dobCor[i][j+anzVarGroup] = 0.0;
        
        det = Statik.determinantOfPositiveDefiniteMatrix(dobCor);
        System.out.println("Data doubled determinant       = "+det);
        if (det == -1) return Double.NaN;
        det = Statik.determinantOfPositiveDefiniteMatrix(dobCorCheck);
        System.out.println("Data doubled determinant Check = "+det);
        
        model.setDataDistribution(dobCor, new double[anzVar], anzPer/2);
        double[] starting = new double[model.anzPar]; for (int i=0; i<starting.length; i++) starting[i] = 0.5;
        for (int i=0; i<model.anzPar; i++) if (model.isErrorParameter(i)) starting[i] = 1.0;
        model.setParameter(starting); // model2.setParameter(starting2);
        model.setMaximalNumberOfIterations(200);
//        model.logStream = System.out;

        System.out.println("Starting estimation");
        double[] vals = model.estimateML(starting,PRECISION);
        double ll = model.getMinusTwoLogLikelihood();
        double satLL = model.getSaturatedLL();
        double rmsea = model.getRMSEA();
        System.out.println(withinDataName+" LL = "+ll);
        System.out.println(withinDataName+" Saturated LL = "+satLL);
        System.out.println(withinDataName+" RMSEA = "+rmsea);
        
        return model.getChisquare();
    }

    /**
     * Creates a model for two participants, fits it and computes the RMSEA value.
     * @param participant
     */
    public static double florianBetweenRowCorrections(int participant, String name, boolean differenceScore, boolean fullARCorrection) {
        final String withinDataName = name;

        int partNr = participant;
        double[][] wthnDataWithID = Statik.loadMatrix("FloData"+File.separator+withinDataName+"_"+partNr+".dat", ' ', true, ".");
        int anzVar = 2*(wthnDataWithID[0].length-1); int anzVarGroup = anzVar/2;
        int[] allButOne = new int[anzVarGroup]; for (int i=0; i<anzVarGroup; i++) allButOne[i] = i+1;
        double[][] wthnData = Statik.submatrix(wthnDataWithID, null, allButOne);

        // debug
        anzVar = 4; anzVarGroup = 2; 
        wthnData = new double[100][anzVarGroup];
        Random rand = new Random();
        double[][] B = new double[anzVarGroup][anzVarGroup];
        for (int i=0; i<anzVarGroup; i++) for (int j=0; j<anzVarGroup; j++) B[i][j] = (i==j?2:0.1)*rand.nextDouble();
        B = new double[][]{{0.5,0.2},{0.2,0.5}};
        for (int i=0; i<wthnData.length; i++) {
            for (int j=0; j<wthnData[i].length; j++) wthnData[i][j] = rand.nextGaussian()*0.75;
            if (i>0) wthnData[i] = Statik.add(wthnData[i], Statik.multiply(B,wthnData[i-1]));
        }
        
        if (differenceScore) wthnData = Statik.getDifferenceScores(wthnData, Model.MISSING);

        double[][] wthnCov = new double[anzVarGroup][anzVarGroup]; double[] wthnMean = new double[anzVarGroup];
        Statik.covarianceMatrixAndMeans(wthnData, wthnMean, wthnCov, Model.MISSING);
        double[][] wthnCor = Statik.correlationFromCovariance(wthnCov);
        double det = Statik.determinantOfPositiveDefiniteMatrix(wthnCor);
        System.out.println("Data determinant = "+det);
        
        int anzPer = wthnData.length;
        double[][] doubleData = new double[anzPer/2][anzVar];
        for (int i=0; i<anzPer/2; i++) 
            for (int j=0; j<anzVarGroup; j++) {doubleData[i][j] = wthnData[i*2][j]; doubleData[i][anzVarGroup+j] = wthnData[i*2+1][j];}
        double[][] dobCov = new double[anzVar][anzVar]; double[] dobMean = new double[anzVar];
        Statik.covarianceMatrixAndMeans(doubleData, dobMean, dobCov, Model.MISSING);
        double[][] dobCor = Statik.correlationFromCovariance(dobCov);
        
        if (fullARCorrection) {
            double[][] pairCov = Statik.submatrix(dobCov, Statik.enumeratIntegersFrom(0,anzVarGroup-1), Statik.enumeratIntegersFrom(anzVarGroup,anzVar-1));
            double[][] inv = Statik.invert(wthnCov);
            double[][] A = Statik.multiply(inv,pairCov);
            Statik.transpose(A,A); 
            double[][] fullARData = new double[anzPer-1][anzVar];
            for (int i=0; i<anzPer-1; i++)
                fullARData[i] = Statik.subtract(wthnData[i+1],Statik.multiply(A,wthnData[i]));
            
            for (int i=0; i<(anzPer-1)/2; i++) 
                for (int j=0; j<anzVarGroup; j++) {doubleData[i][j] = fullARData[i*2][j]; doubleData[i][anzVarGroup+j] = fullARData[i*2+1][j];}
            double[][] dobCovOld = dobCov;
            dobCov = new double[anzVar][anzVar]; dobMean = new double[anzVar];
            Statik.covarianceMatrixAndMeans(doubleData, dobMean, dobCov, Model.MISSING);
            dobCor = Statik.correlationFromCovariance(dobCov);
        }        

        double[][] dobCorNoCrossCovariance = Statik.copy(dobCor);
        for (int i=0; i<anzVarGroup; i++) for (int j=0; j<anzVarGroup; j++) 
            dobCorNoCrossCovariance[i+anzVarGroup][j] = dobCorNoCrossCovariance[i][j+anzVarGroup] = 0.0;
        
        det = Statik.determinantOfPositiveDefiniteMatrix(dobCor);
        System.out.println("Data doubled determinant       = "+det);
        if (det == -1) return Double.NaN;

        return Statik.chiSquareToSaturated(dobCor, dobCorNoCrossCovariance);
        
    }
    
    
    /**
     * Creates a model for one participant to computes the RMSEA value.
     * @param participant
     */
    public static double florianCompareRMSEA(int participant, String name, boolean differenceScore) {
        final double PRECISION = 0.001;
        final String withinDataName = name;
      
        RAMModel model = florianFactorModelRAMStandardizedWeights();
        int anzVar = model.anzVar;
        int[] allButOne = new int[anzVar]; for (int i=0; i<anzVar; i++) allButOne[i] = i+1;
        System.out.println(withinDataName+" df     = "+model.getRestrictedDF());
        System.out.println(withinDataName+" anzpar = "+model.anzPar);

        int partNr = participant;
        double[][] wthnDataWithID = Statik.loadMatrix("FloData"+File.separator+withinDataName+"_"+partNr+".dat", ' ', true, ".");
//        double[][] wthnDataWithID = Statik.loadMatrix("FloData"+File.separator+withinDataName+"_"+partNr+".dat", '\t', true);
        double[][] wthnData = Statik.submatrix(wthnDataWithID, null, allButOne);
//        Statik.replace(wthnData, 0.0, Model.MISSING);
        
        // remove for raw
        if (differenceScore) wthnData = Statik.getDifferenceScores(wthnData, Model.MISSING);

        double[][] wthnCov = new double[anzVar][anzVar]; double[] wthnMean = new double[anzVar];
        Statik.covarianceMatrixAndMeans(wthnData, wthnMean, wthnCov, Model.MISSING);
        double[][] wthnCor = Statik.correlationFromCovariance(wthnCov);
        double det = Statik.determinantOfPositiveDefiniteMatrix(wthnCor);
        System.out.println("Data determinant = "+det);
        
        int anzPer = wthnData.length;

        model.setDataDistribution(wthnCor, new double[anzVar], anzPer/2);
        double[] starting = new double[model.anzPar]; for (int i=0; i<starting.length; i++) starting[i] = 0.5;
        for (int i=0; i<model.anzPar; i++) if (model.isErrorParameter(i)) starting[i] = 1.0;
        model.setParameter(starting); 
        model.setMaximalNumberOfIterations(200);
//        model.logStream = System.out;

        System.out.println("Starting estimation");
        double[] vals = model.estimateML(starting,PRECISION);
        double ll = model.getMinusTwoLogLikelihood();
        double satLL = model.getSaturatedLL();
        double rmsea = model.getRMSEA();
        System.out.println(withinDataName+" LL = "+ll);
        System.out.println(withinDataName+" Saturated LL = "+satLL);
        System.out.println(withinDataName+" RMSEA = "+rmsea);
        return model.getChisquare();
    }
    
    
    /**
     * Multi Group Comparison of within and between structure. 
     */
    public static void florianStandardizedWeights(int startPer, int anzParticipants) {
        
        final double PRECISION = 0.0001;
//        final String withinDataName = "person";
        final String withinDataName = "raw_person";
        
        RAMModel model1 = florianFactorModelRAMStandardizedWeights();
        int factorPerGroup = model1.anzFac;
        Model model = florianDoubleModel(model1);
//        Model model2 = florianTwoGroupNonflatFactorModelRAM();
//        Model model = florianFactorModelSEMStandardizedWeightsTwoGroups();
        int anzVar = model.anzVar;

        int[][] hypoPara = new int[][]{{0,1,2},{0,1,2,3,4,5,6,7,8,9,10,11}};
//        int[][] hypoPara = new int[][]{{0,1,2,3,4,5,6,7,8,9,10,11}};
        
        double[][] erg = new double[(anzParticipants>101?101:anzParticipants)][2*hypoPara.length+1];
        
        
//        model.logStream = System.out;
        int anzVarGroup = anzVar/2;
        int[] allButOne = new int[anzVarGroup]; for (int i=0; i<anzVarGroup; i++) allButOne[i] = i+1;
        int runindex = 0;
        for (int partNr = startPer; (partNr <=101 && partNr < startPer+anzParticipants); partNr++) {
            System.out.println("Starting with Participant "+partNr);
            long timestamp = System.nanoTime(); 

            int[] fixedParameter = new int[0]; double[] fixedValue = new double[0];
            if (partNr == 1) {fixedParameter = new int[]{10,11,23,24}; fixedValue = new double[]{0,0.431,0,0};}
            if (fixedParameter.length != 0) {
                model.fixParameter(fixedParameter, fixedValue);
            }
            
            double[][] wthnDataWithID = Statik.loadMatrix("FloData"+File.separator+withinDataName+"_"+partNr+".dat", ' ', true);
            double[][] wthnData = Statik.submatrix(wthnDataWithID, null, allButOne);
            Statik.replace(wthnData, 0.0, Model.MISSING);
            double[][] wthnCov = new double[anzVarGroup][anzVarGroup]; double[] wthnMean = new double[anzVarGroup];
            Statik.covarianceMatrixAndMeans(wthnData, wthnMean, wthnCov, Model.MISSING);
            double[][] wthnCor = Statik.correlationFromCovariance(wthnCov);
            for (int i=0; i<wthnData.length; i++) for (int j=0; j<wthnData[i].length; j++) if (!Model.isMissing(wthnData[i][j])) wthnData[i][j] -= wthnMean[j];

            double[][] btwDataWithID = Statik.loadMatrix("FloData"+File.separator+"between_"+partNr+".dat", ' ', true);
            double[][] btwData = Statik.submatrix(btwDataWithID, null, allButOne);
            Statik.replace(btwData, 0.0, Model.MISSING);
            double[][] btwCov = new double[anzVarGroup][anzVarGroup]; double[] btwMean = new double[anzVarGroup];
            Statik.covarianceMatrixAndMeans(btwData, btwMean, btwCov, Model.MISSING);
            double[][] btwCor = Statik.correlationFromCovariance(btwCov);
            for (int i=0; i<btwData.length; i++) for (int j=0; j<btwData[i].length; j++) if (!Model.isMissing(btwData[i][j])) btwData[i][j] -= btwMean[j];

            double[][] jointCor = new double[btwCor.length+wthnCor.length][btwCor.length+wthnCor.length];
            for (int i=0; i<btwCor.length; i++) for (int j=0; j<btwCor.length; j++) {
                jointCor[i][j] = wthnCor[i][j]; jointCor[i+wthnCor.length][j+wthnCor.length] = btwCor[i][j];
            }

            model.setDataDistribution(jointCor, new double[model.anzVar], wthnData.length);
//            double[] starting = model.getArbitraryStartingValues(); 
//            for (int i=0; i<8; i++) starting[1+2*i] = 0;
            double[] starting = new double[model.anzPar]; for (int i=0; i<starting.length; i++) starting[i] = 0.5;
            for (int i=0; i<model.anzPar; i++) if (model.isErrorParameter(i)) starting[i] = 1.0;
            model.setParameter(starting); // model2.setParameter(starting2);
            model.computeLogLikelihoodDerivatives(starting);
//            System.out.println("Hessian before start = "+Statik.matrixToMapleString(model.llDD));
            double ll = model.getMinusTwoLogLikelihood();

            model.setMaximalNumberOfIterations(200);
            System.out.println("Starting estimation");
            
            double[] vals = model.estimateML(starting,PRECISION);
//          vals = new double[]{0.6239425474773991, 0.0017828150121331709, 0.007792752118130576, 0.7563216755284523, 1.1541186881291043, 0.9347010774756253, 82.38716365691059, 0.008373765039017957, 0.011607090815844382, 0.009766957133611961, 179.86104216714594, 4.2255615399562716E-4, 0.786566721585652, 0.8985284480495023, 0.5953814084822497, 0.6660911863236589, 0.6924929718777415, 0.8202546689422769, 0.5924690838620736, 0.6134880449012997, 0.22280923248292256, 0.6606277990543158, 0.1292148007968488, 0.1119893371568832, 0.1843236188654938, 0.0367893155212789, 4.747711569390663, 0.38131383417649767, 0.19264798738441347, 0.6455223682264559, 0.5563242709801146, 0.5204547171230842, 0.32718400826994243, 0.6587506898501919, 0.6236331010523013, 0.9503562427903014, 0.5635760144488912, 0.9833037307544022, 0.9890474145693029, 0.9703294470136132, 0.9986466135536038, -21.538870714613445, 0.33786387417529384, 0.9651607716284942, 0.47487155939490266, 1.025502206709787, 0.7615754724758621, 0.6049602089164943, 0.47297991362457625, 0.3654524360137802, 0.454133017413924, 0.7812107269441542, 0.8443857615196222, 0.5602521712844366, 0.9533885247483296, 0.9803886702363513, 0.9618390302250948, 0.979675267949067, 0.9831516829922071, 0.9295188599185997, 0.8470693649628669, 0.7549492154435555, 0.9421836211531056, 0.9807904051364982, 0.7228835345313662, 0.9539404722044631, 0.906028912376218, 0.8284002527231447, 0.9458793389629714, 0.09105032087821174, 0.038838055272204075, 0.07486567993565138, 0.04023636936892912, 0.03341276822959881, 0.1359946890556295, 0.28247349094140617, 0.43005168210115996, 0.1122900240308159, 0.038050181192185686, 0.47743939550344205, 0.08999757549032478, 0.17911160993837133, 0.31375302128823157, 0.10531227612297028};
            double estLL = model.ll; 
            
            florianVorzeichenBuersten((RAMModel)model, vals, 0);
            florianVorzeichenBuersten((RAMModel)model, vals, ((RAMModel)model).anzFac/2);
            model.setParameter(vals);
            double checkLL = model.getMinusTwoLogLikelihood();
            
            
//            double[] vals = starting;
            System.out.println("Estimation done, time = "+((System.nanoTime()-timestamp)/1000000)+" ms.");
            System.out.println("LogH Estimates = \t"+Statik.matrixToString(vals,5));
            System.out.println("Starting ll = "+ll+", Final ll = "+estLL);
            erg[runindex][0] = estLL;

            int anzPar = model.anzPar;
            
//            double[][] kernel = new double[anzPar][anzPar];
//            System.out.println("Hessian = "+Statik.matrixToMapleString(model.llDD,6));
//            int kernelDim = Statik.kernel(model.llDD, kernel, 0.002);
//            if (kernelDim > 0) {
//                System.out.println(kernelDim+" dimensional kernel of Hessian found, \r\n"+Statik.matrixToString(kernel));
//            }
            
            
//            double[][] eigenVec = Statik.identityMatrix(anzPar);
//            double[] evHess = new double[anzPar];
//            Statik.eigenvalues(model.llDD, 0.0001, evHess, eigenVec);
//            Arrays.sort(evHess);
//            System.out.println("Eigenvalues Hessian = "+Statik.matrixToString(evHess,5));
//            System.out.println(Statik.matrixToString(Statik.transpose(eigenVec)));
            
            
            for (int c=0; c<hypoPara.length; c++) {
                int firstPar2nd = (model.anzPar+fixedParameter.length)/2 - fixedParameter.length;
                int[] upHypoPar = Statik.copy(hypoPara[c]); for (int i=0; i<upHypoPar.length; i++) upHypoPar[i] += firstPar2nd;                
                
                model.setParameter(vals);
                RAMModel stub = (RAMModel)model.copy();
                for (int i=0; i<hypoPara[c].length; i++) stub = florianAddEqualtiyConstraintInAsymetric((RAMModel)stub, hypoPara[c][i], firstPar2nd, factorPerGroup);
                double[] estOnEquality = Statik.copy(vals); 
//                for (int i=0; i<upHypoPar.length; i++) estOnEquality[upHypoPar[i]] -= florianGetValueForParameterBeforeFixation(stub,hypoPara[c][i]);
                for (int i=0; i<upHypoPar.length; i++) {
                    estOnEquality[hypoPara[c][i]] = 0.5*(estOnEquality[hypoPara[c][i]]+estOnEquality[upHypoPar[i]]); 
                    estOnEquality[upHypoPar[i]] -= florianGetValueForParameterBeforeFixation(stub,hypoPara[c][i]);
                }
                // DEBUG
//                double[] debugResStartBig = Statik.copy(estOnEquality); for (int i=0; i<upHypoPar.length; i++) debugResStartBig[upHypoPar[i]] = 0;
//                stub.setDataDistribution(jointCor, new double[model.anzVar], wthnData.length);
//                double checkll1 = stub.getMinusTwoLogLikelihood(debugResStartBig);
//                double[] debugResStartOrg = Statik.copy(vals); 
//                for (int i=0; i<upHypoPar.length; i++) debugResStartOrg[upHypoPar[i]] = debugResStartOrg[hypoPara[c][i]] = 0.5*(vals[upHypoPar[i]]+vals[hypoPara[c][i]]);
//                double checkll2 = model.getMinusTwoLogLikelihood(debugResStartOrg);
                
                double[] resStart = Statik.subvector(estOnEquality, upHypoPar, false);
                Model resModel = stub.copy();
                resModel.fixParameter(upHypoPar, new double[upHypoPar.length]);
                resModel.setDataDistribution(jointCor, new double[model.anzVar], wthnData.length);
                ll = resModel.getMinusTwoLogLikelihood(resStart);
                
                // debug
//                for (int i=0; i<stub.sigma.length; i++) for (int j=0; j<stub.sigma[i].length; j++) if (stub.sigma[i][j] != ((RAMModel)model).sigma[i][j]) 
//                    System.out.println("Error at row = "+i+", col = "+j);
                
                timestamp = System.nanoTime();
                double[] resEst = resModel.estimateML(resStart,PRECISION);
//                if (fixRow == 1 && fixCol == 2) System.out.println(Statik.matrixToString(resEst,5));
                System.out.println("Estimation done, time = "+((System.nanoTime()-timestamp)/1000000)+" ms.");
                System.out.println("restricted Estimates = \t"+Statik.matrixToString(resEst,5));
                System.out.println("Starting ll = "+ll+", Final ll = "+resModel.ll);

//                stub.setParameter(estOnEquality);
//                timestamp = System.nanoTime();
//                stub.setDataDistribution(jointCor, new double[model.anzVar], wthnData.length);
//                stub.computeLogLikelihoodDerivatives(estOnEquality, true);
//                System.out.println("Hessian stub = "+Statik.matrixToMapleString(stub.llDD));
                int df = upHypoPar.length;
                
                // TEMPORARILY turned off
//                double[] chiMultiplier = stub.computeMisspecification(upHypoPar, false, estOnEquality, false);
//                System.out.println("chi multiplier computed, time = "+((System.nanoTime()-timestamp)/1000000)+" ms.");
//                System.out.println("Multiplier = "+Statik.matrixToString(chiMultiplier));
//                for (int i=0; i<chiMultiplier.length; i++) chiMultiplier[i] = Math.abs(chiMultiplier[i]);
//                Arrays.sort(chiMultiplier);
//                double[] nonNullMultiplier = new double[df]; for (int i=0; i<df; i++) nonNullMultiplier[i] = chiMultiplier[chiMultiplier.length-1-i];
//                double crit = Statik.inverseMixtureOfChisquares(0.95, nonNullMultiplier, Model.staticRandom);
                double crit = 100; double[] nonNullMultiplier = new double[df];
                
                double lr = resModel.ll - estLL;
                System.out.println(c+"\t"+crit+"\t"+Statik.FIVEPERCENTTHRESHOLD[df-1]+"\t"+lr+"\t"+
                        (lr>Statik.FIVEPERCENTTHRESHOLD[df-1])+"\t"+(lr>crit)+"\t"+
                        Statik.matrixToString(Statik.subvector(nonNullMultiplier, new int[]{0,1,2,3,4})));
                erg[runindex][1+c*2] = lr; erg[runindex][c*2+2] = crit;
                Statik.writeMatrix(erg, "FloData"+File.separator+"resultBtwWthn_"+startPer+"_"+(startPer+anzParticipants-1)+".txt", '\t');
            }
            runindex++;
        }
    }
    
    public static void florianCombineResults() {
        double[][] erg = new double[101][5];
        String[] files = (new File("FloData")).list();
        for (String name:files) {
            if (name.endsWith(".txt") && name.startsWith("resultBtwWthn_")) {
                int ix1 = name.indexOf("_"), ix2 = name.indexOf("_",ix1+1); // , ix3 = name.indexOf(".",ix2);
                String start = name.substring(ix1+1, ix2); //, end = name.substring(ix2+1,ix3);
                int six = Integer.parseInt(start); // , eix = Integer.parseInt(end);
                double[][] zwerg = Statik.loadMatrix("FloData"+File.separator+name, '\t');
                for (int i=0; i<zwerg.length; i++) erg[six+i-1] = zwerg[i];
            }
        }
        Statik.writeMatrix(erg, "FloData"+File.separator+"resultBtwWthnCombined.txt", '\t');
    }
    
    public static void florianFlatModelTestingPairedCovariance() {
        Model model = florianFlatFactorModelRAMPairedCovariance();
//        Model model = florianFlatFactorModel();
        int anzVar = model.anzVar;
        double[][] erg = new double[101][84];
        
//        model.logStream = System.out;
        int[] allButOne = new int[anzVar]; for (int i=0; i<anzVar; i++) allButOne[i] = i+1;
        for (int partNr = 4; partNr <=4; partNr++) {
            System.out.println("Starting with Participant "+partNr);
            long timestamp = System.nanoTime(); 
            
            double[][] orgDataWithID = Statik.loadMatrix("FloData"+File.separator+"person_"+partNr+".dat", ' ', true);
            double[][] orgData = Statik.submatrix(orgDataWithID, null, allButOne);
            Statik.replace(orgData, 0.0, Model.MISSING);
            double[][] orgCov = new double[anzVar][anzVar]; double[] orgMean = new double[anzVar];
            Statik.covarianceMatrixAndMeans(orgData, orgMean, orgCov, Model.MISSING);
            double[][] cor = Statik.correlationFromCovariance(orgCov);
            for (int i=0; i<orgData.length; i++) for (int j=0; j<orgData[i].length; j++) if (!Model.isMissing(orgData[i][j])) orgData[i][j] -= orgMean[j];
            model.setData(orgData);
            double[] ar = model.getArbitraryStartingValues();
            for (int i=0; i<ar.length; i++) if (ar[i] > 2) ar[i] = ar[i]*ar[i];
            model.setParameter(ar);
            double ll = model.getMinusTwoLogLikelihood();

            model.setMaximalNumberOfIterations(200);
            System.out.println("Starting estimation");
            double[] vals = model.estimateML(model.getArbitraryStartingValues(),0.00001);
            System.out.println("Estimation done, time = "+((System.nanoTime()-timestamp)/1000000)+" ms.");
            System.out.println("LogH Estimates = \t"+Statik.matrixToString(vals));
            double estLL = model.ll;
            System.out.println("ll = "+estLL);

            int anzPar = model.anzPar;
            double[][] eigenVec = Statik.identityMatrix(anzPar);
            double[] evHess = new double[anzPar];
            Statik.eigenvalues(model.llDD, 0.0001, evHess, eigenVec);
//            Arrays.sort(evHess);
            System.out.println("Eigenvalues Hessian = "+Statik.matrixToString(evHess,5));
            System.out.println(Statik.matrixToString(Statik.transpose(eigenVec)));
            
            
            int c = 0;
            int[] hypoPara = null;
            for (int fixRow = 0; fixRow < 3; fixRow++) for (int fixCol=0; fixCol < (fixRow==0?1:(fixRow==1?3:9)); fixCol++) {
                if (fixRow == 0) hypoPara = new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14};
                if (fixRow == 1) {if (fixCol ==0) hypoPara = new int[]{15,16,17,18,19,20};
                             else if (fixCol ==1) hypoPara = new int[]{21,22,23};
                             else if (fixCol ==2) hypoPara =new int[]{24,25,26,27,28,29};
                }
                if (fixRow == 2) hypoPara = new int[]{30+fixCol};
                double[] resStart = Statik.subvector(vals, hypoPara, false);
                Model resModel = model.copy();
                resModel.fixParameter(hypoPara, new double[hypoPara.length]);
                resModel.setData(orgData);
                resModel.estimateML(resStart,0.0001);

                model.setParameter(vals);
                double[] chiMultiplier = model.computeMisspecification(hypoPara, false, vals, false);
                for (int i=0; i<chiMultiplier.length; i++) chiMultiplier[i] = Math.abs(chiMultiplier[i]);
                Arrays.sort(chiMultiplier);
                int df = hypoPara.length;
                double[] nonNullMultiplier = new double[df]; for (int i=0; i<df; i++) nonNullMultiplier[i] = chiMultiplier[chiMultiplier.length-1-i];
                double crit = Statik.inverseMixtureOfChisquares(0.95, nonNullMultiplier, Model.staticRandom);
                
                double lr = resModel.ll - estLL;
                System.out.println(fixRow+"\t"+fixCol+"\t"+crit+"\t"+Statik.FIVEPERCENTTHRESHOLD[hypoPara.length-1]+"\t"+lr+"\t"+
                        (lr>Statik.FIVEPERCENTTHRESHOLD[hypoPara.length-1])+"\t"+(lr>crit)+"\t"+
                        Statik.matrixToString(Statik.subvector(nonNullMultiplier, new int[]{0,1,2,3,4})));
                erg[partNr-1][c*2] = lr; erg[partNr-1][c*2+1] = crit;
                Statik.writeMatrix(erg, "FloData"+File.separator+"result.txt", '\t');
                c++;
            }
            
        }
    }
    
    
    public static void florianFlatModelTestingNoEqualtyConstraints() {
        Model model = florianFlatFactorModelRAMNoEqualityConstraints();
//        Model model = florianFlatFactorModel();
        int anzVar = model.anzVar;
        double[][] erg = new double[101][84];
        
//        model.logStream = System.out;
        int[] allButOne = new int[anzVar]; for (int i=0; i<anzVar; i++) allButOne[i] = i+1;
        for (int partNr = 3; partNr <=3; partNr++) {
            System.out.println("Starting with Participant "+partNr);
            long timestamp = System.nanoTime(); 
            
            double[][] orgDataWithID = Statik.loadMatrix("FloData"+File.separator+"person_"+partNr+".dat", ' ', true);
            double[][] orgData = Statik.submatrix(orgDataWithID, null, allButOne);
            Statik.replace(orgData, 0.0, Model.MISSING);
            double[][] orgCov = new double[anzVar][anzVar]; double[] orgMean = new double[anzVar];
            Statik.covarianceMatrixAndMeans(orgData, orgMean, orgCov, Model.MISSING);
            double[][] cor = Statik.correlationFromCovariance(orgCov);
            for (int i=0; i<orgData.length; i++) for (int j=0; j<orgData[i].length; j++) if (!Model.isMissing(orgData[i][j])) orgData[i][j] -= orgMean[j];
            model.setData(orgData);
            double[] ar = model.getArbitraryStartingValues();
            for (int i=0; i<ar.length; i++) if (ar[i] > 2) ar[i] = ar[i]*ar[i];
            model.setParameter(ar);
            double ll = model.getMinusTwoLogLikelihood();

            model.setMaximalNumberOfIterations(200);
            System.out.println("Starting estimation");
            double[] vals = model.estimateML(model.getArbitraryStartingValues(),0.001);
            System.out.println("Estimation done, time = "+((System.nanoTime()-timestamp)/1000000)+" ms.");
            System.out.println("LogH Estimates = \t"+Statik.matrixToString(vals));
            double estLL = model.ll; 

            int anzPar = model.anzPar;
            double[][] eigenVec = Statik.identityMatrix(anzPar);
            double[] evHess = new double[anzPar];
            Statik.eigenvalues(model.llDD, 0.0001, evHess, eigenVec);
//            Arrays.sort(evHess);
//            System.out.println("Eigenvalues Hessian = "+Statik.matrixToString(evHess,5));
//            System.out.println(Statik.matrixToString(Statik.transpose(eigenVec)));
            
            
            int c = 0;
            int[] hypoPara = null;
            for (int fixRow = 0; fixRow < 3; fixRow++) for (int fixCol=0; fixCol < (fixRow==0?1:(fixRow==1?3:9)); fixCol++) {
                if (fixRow == 0) hypoPara = new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17};
                if (fixRow == 1) {if (fixCol ==0) hypoPara = new int[]{18,19,20,21,22,23};
                             else if (fixCol ==1) hypoPara = new int[]{24,25,26,27,28,29};
                             else if (fixCol ==2) hypoPara =new int[]{30,31,32,33,34,35};
                }
                if (fixRow == 2) hypoPara = new int[]{36+2*fixCol,36+2*fixCol+1};

                double[] resStart = Statik.subvector(vals, hypoPara, false);
                Model resModel = model.copy();
                resModel.fixParameter(hypoPara, new double[hypoPara.length]);
                resModel.setData(orgData);
                resModel.estimateML(resStart,0.0001);

                model.setParameter(vals);
                double[] chiMultiplier = model.computeMisspecification(hypoPara, false, vals, false);
                for (int i=0; i<chiMultiplier.length; i++) chiMultiplier[i] = Math.abs(chiMultiplier[i]);
                Arrays.sort(chiMultiplier);
                int df = hypoPara.length;
                double[] nonNullMultiplier = new double[df]; for (int i=0; i<df; i++) nonNullMultiplier[i] = chiMultiplier[chiMultiplier.length-1-i];
                double crit = Statik.inverseMixtureOfChisquares(0.95, nonNullMultiplier, Model.staticRandom);
                
                double lr = resModel.ll - estLL;
                System.out.println(fixRow+"\t"+fixCol+"\t"+crit+"\t"+Statik.FIVEPERCENTTHRESHOLD[hypoPara.length-1]+"\t"+lr+"\t"+
                        (lr>Statik.FIVEPERCENTTHRESHOLD[hypoPara.length-1])+"\t"+(lr>crit)+"\t"+
                        Statik.matrixToString(Statik.subvector(nonNullMultiplier, new int[]{0,1,2,3,4})));
                erg[partNr-1][c*2] = lr; erg[partNr-1][c*2+1] = crit;
                Statik.writeMatrix(erg, "FloData"+File.separator+"result.txt", '\t');
                c++;
            }
            
        }
    }

    public static void tTestInSEMLecture() {
        // t Critical: 100 -> 1.66, 10 -> 1.812
        int[] N = new int[]{2,5,10,15,20,30,40,50,60,70,80,90,100};
        double[] tCritical = new double[]{6.313,2.132,1.833,1.761,1.729,1.699,1.685,1.677,1.671,1.667,1.664,1.662,1.660};
        
        int anzTrials = 200000;

        Random rand = new Random();
        LinearModel model = new LinearModel(new double[][]{{1}}, new int[][]{{0}}, new double[][]{{1.0}}, new int[]{1}, new double[]{0.0}, -1, 0);
        LinearModel res = new LinearModel(new double[][]{{1}}, new int[][]{{0}}, new double[][]{{1.0}}, new int[]{-1}, new double[]{0.0}, -1, 0);
        model.rand = rand;
        
        double[][] erg = new double[N.length][2];
        double[][] matrix1 = new double[2][2], matrix2 = new double[2][2];
        for (int n=0; n<N.length; n++) {
        
            double balance = Math.sqrt(2.7055/N[n]);
            
            erg[n][0] = erg[n][1] = 0;
            System.out.println("N = "+N[n]);
            for (int tr=0; tr<anzTrials; tr++) {
                model.setParameter(new double[]{1.0,0.0}); 
                model.createData(N[n]); res.setData(model.data);
                double[] est = model.estimateML();
                double t = Math.sqrt(N[n]) * est[1] / Math.sqrt(est[0]*N[n]/(double)(N[n]-1));
                double tsqr = t*t;
                res.estimateML();
                double lr = res.ll - model.ll;
                int lrSig = (est[1] > 0 && lr > 2.7055?1:0);
                int tSig  = (t > tCritical[n]?1:0);
                
    //            System.out.println(lr + "\t"+tsqr+"\t"+lrSig+"\t"+tSig);
                matrix1[lrSig][tSig]++; if (lrSig==1 && tSig==0) erg[n][0]++;

                model.setParameter(new double[]{1.0,balance}); 
                model.createData(N[n]); res.setData(model.data);
                est = model.estimateML();
                t = Math.sqrt(N[n]) * est[1] / Math.sqrt(est[0]*N[n]/(double)(N[n]-1));
                tsqr = t*t;
                res.estimateML();
                lr = res.ll - model.ll;
                lrSig = (est[1] > 0 && lr > 2.7055?1:0);
                tSig  = (t > tCritical[n]?1:0);
                
                if (N[n] == 30 && tr < 15) System.out.println(lr + "\t"+tsqr+"\t"+lrSig+"\t"+tSig);
                matrix2[lrSig][tSig]++; if (lrSig==1 && tSig==0) erg[n][1]++;
            }
            Statik.multiply(1/(double)anzTrials, matrix1, matrix1);
            Statik.multiply(1/(double)anzTrials, matrix2, matrix2);
            System.out.println("Matrix Type I  = \r\n"+Statik.matrixToString(matrix1));
            System.out.println("Matrix Type II = \r\n"+Statik.matrixToString(matrix2));
        }
        Statik.multiply(1/(double)anzTrials, erg, erg);
        System.out.println("Total Erg = \r\n"+Statik.matrixToString(erg,5));
    }
    
    public static void fTestInSEMLecture() {
        int K = 5;
        int[] N = new int[]{10,20,30,40,50,60,70,80,90,100,1000};
//        double[] fCritical = new double[]{14.081, 9.446, 8.737, 8.451, 8.297, 8.200, 8.134, 8.085, 8.049, 8.020, 7.802}; // for K = 5
        double[] fCritical = new double[]{20.769, 12.222, 11.035, 10.566, 10.315, 10.159, 10.052, 9.975, 9.916, 9.870, 9.524}; // for K = 5
//        double[] fCritical = new double[]{25.961, 15.278, 13.793, 13.207, 12.894, 12.698, 12.565, 12.468, 12.395, 12.337, 11.904}; // for K = 5
//        int[] N = new int[]{1000};
//        double[] fCritical = new double[]{9.5240}; // for K = 5
//        double[] fCritical = new double[]{11.9043}; // for K = 5
        
        int anzTrials = 200000;

        Random rand = new Random();
        LinearModel model1 = new LinearModel(new double[][]{{1}}, new int[][]{{0}}, new double[][]{{1.0}}, new int[]{1}, new double[]{0.0}, -1, 0);
        LinearModel model2 = new LinearModel(new double[][]{{1}}, new int[][]{{0}}, new double[][]{{1.0}}, new int[]{2}, new double[]{0.0}, -1, 0);
        LinearModel model3 = new LinearModel(new double[][]{{1}}, new int[][]{{0}}, new double[][]{{1.0}}, new int[]{3}, new double[]{0.0}, -1, 0);
        LinearModel model4 = new LinearModel(new double[][]{{1}}, new int[][]{{0}}, new double[][]{{1.0}}, new int[]{4}, new double[]{0.0}, -1, 0);
        LinearModel model5 = new LinearModel(new double[][]{{1}}, new int[][]{{0}}, new double[][]{{1.0}}, new int[]{5}, new double[]{0.0}, -1, 0);
        MultiGroupModel model = new MultiGroupModel(new Model[]{model1, model2, model3, model4, model5}, model1.anzVar);
        MultiGroupModel res = new MultiGroupModel(new Model[]{model1.copy(), model1.copy(), model1.copy(), model1.copy(), model1.copy()},2); 
        model.rand = rand;

        double[] dist = new double[anzTrials];
        double[][] erg = new double[N.length][2];
        double[][] matrix1 = new double[2][2], matrix2 = new double[2][2];
        for (int n=0; n<N.length; n++) {
        
            double balance = Math.sqrt(60.0/(double)N[n]);
            
            erg[n][0] = erg[n][1] = 0;
            System.out.println("N = "+N[n]);
            for (int tr=0; tr<anzTrials; tr++) {
                model.setParameter(new double[]{1.0,0.0,0.0,0.0,0.0,0.0}); 
                model.createData(N[n]); res.setData(model.getData(), new int[]{
                        model.submodel[0].anzPer,model.submodel[1].anzPer,model.submodel[2].anzPer,model.submodel[3].anzPer,model.submodel[4].anzPer,});
                double[] est = model.estimateML();
                double mean = 0, sigmaBtw = 0; for (int i=1; i<6; i++) {mean += est[i]; sigmaBtw += est[i]*est[i];}
                mean /= 5.0; sigmaBtw = (sigmaBtw/5.0 - mean*mean)*(5.0/4.0);
                double f = (4.0/5.0)*(N[n]-5) * sigmaBtw / (est[0]*N[n]/(double)(N[n]-1));
                res.estimateML();
                double lr = res.ll - model.ll;
                int lrSig = (lr > 9.8700?1:0);
                int fSig  = (f > fCritical[n]?1:0);
                dist[tr] = f;
                
                matrix1[lrSig][fSig]++; if (lrSig==1 && fSig==0) erg[n][0]++;

                model.setParameter(new double[]{1.0,balance,0,0,0,0}); 
                model.createData(N[n]); res.setData(model.getData(), new int[]{
                    model.submodel[0].anzPer,model.submodel[1].anzPer,model.submodel[2].anzPer,model.submodel[3].anzPer,model.submodel[4].anzPer,});
                est = model.estimateML();
                mean = 0; sigmaBtw = 0; for (int i=1; i<6; i++) {mean += est[i]; sigmaBtw += est[i]*est[i];}
                mean /= 5.0; sigmaBtw = (sigmaBtw/5.0 - mean*mean)*(5.0/4.0);
                f = (4.0/5.0)*(N[n]-5) * sigmaBtw / (est[0]*N[n]/(double)(N[n]-1));
                res.estimateML();
                lr = res.ll - model.ll;
                lrSig = (lr > 9.8700?1:0);
                fSig  = (f > fCritical[n]?1:0);
                
                if (N[n] == 30 && tr < 15) System.out.println(Statik.doubleNStellen(lr, 2) + "\t"+Statik.doubleNStellen(f, 2)+"\t"+lrSig+"\t"+fSig);
                matrix2[lrSig][fSig]++; if (lrSig==1 && fSig==0) erg[n][1]++;
            }
            Statik.multiply(1/(double)anzTrials, matrix1, matrix1);
            Statik.multiply(1/(double)anzTrials, matrix2, matrix2);
            System.out.println("Matrix Type I  = \r\n"+Statik.matrixToString(matrix1));
            System.out.println("Matrix Type II = \r\n"+Statik.matrixToString(matrix2));
        }
        Statik.multiply(1/(double)anzTrials, erg, erg);
        System.out.println("Total Erg = \r\n"+Statik.matrixToString(erg,5));
        
        Arrays.sort(dist);
        System.out.println(dist[(int)Math.round(0.95*anzTrials)]);
    }    
    
    public static void danHacketTest() {
        int N = 5000;
        // latents: GA, manifest XYZ
        double[][] struct = new double[][]{{1,1},{2,1}, {3,1}};
        LinearModel model = new LinearModel(struct);
        model.setParameter(new double[]{0,0,1.0,1.0,0,1.0}); model.evaluateMuAndSigma();
        System.out.println("Model covariance  = \r\n"+Statik.matrixToString(model.sigma));
//        model.setDataDistribution(new double[][]{{6.5,1.6,.34},{1.6,3.1,.02},{.34,.02,2.1}}, new double[]{.3,.35,.034}, 100);
        model.setDataDistribution(model.sigma, model.mu, N);
        double[] est = model.estimateML();
        System.out.println("Estimates = "+Statik.matrixToString(est));
        
        double[] ppmlEst = model.estimateMLFullCovarianceSupportedByPowerEquivalence(true);
        System.out.println("PPML estimates = "+Statik.matrixToString(est));
        
        model.setParameter(new double[]{0,0,1.0,1.0,0,1.0}); model.evaluateMuAndSigma();
        System.out.println("Model covariance  = \r\n"+Statik.matrixToString(model.sigma));
        model.createData(N);
        est = model.estimateML();
        System.out.println("Estimates = "+Statik.matrixToString(est));
        
        ppmlEst = model.estimateMLFullCovarianceSupportedByPowerEquivalence(false);
        System.out.println("PPML estimates = "+Statik.matrixToString(est));
        
    }
    
    
    public static void multiLevelClassHomework91() {
        LinearModel model = new LinearModel(new double[][]{{1},{1},{1},{1},{1},{1},{1},{1},{1},{1}}, new int[][]{{0}}, new double[][]{{10}}, new int[]{2}, new double[]{20}, 1, 1.0);
        LinearModel res = new LinearModel(new double[][]{{1},{1},{1},{1},{1},{1},{1},{1},{1},{1}}, new int[][]{{0}}, new double[][]{{10}}, new int[]{-1}, new double[]{20}, 1, 1.0);
        LinearModel model2 = new LinearModel(new double[][]{{1},{1},{1},{1},{1},{1},{1},{1},{1},{1}}, new int[][]{{-1}}, new double[][]{{0}}, new int[]{0}, new double[]{20}, 1, 1.0);
        LinearModel res2 = new LinearModel(new double[][]{{1},{1},{1},{1},{1},{1},{1},{1},{1},{1}}, new int[][]{{-1}}, new double[][]{{0}}, new int[]{-1}, new double[]{20}, 1, 1.0);
        model.rand = new Random(124231232);
        model.createData(100); res.setData(model.data);
        Statik.writeMatrix(Statik.addRowNumber(model.data), "homework9data1.txt", '\t', "\tBar1\tBar2\tBar3\tBar4\tBar5\tBar6\tBar7\tBar8\tBar9\tBar10");
        
        double[] est = model.estimateML(); res.estimateML();
        System.out.println("Estimate = "+Statik.matrixToString(est)+", lr = "+(res.ll - model.ll));
        model2.setData(model.data); res2.setData(model.data);
        double[] est2 = model2.estimateML(); res2.estimateML();
        System.out.println("Estimate 2 = "+Statik.matrixToString(est2)+", lr = "+(res2.ll - model2.ll));
        
    }
    
    public static void multiLevelClassHomework92() {
        LinearModel model1 = new LinearModel( new double[][]{{1},{1},{1}}, new int[][]{{0}}, new double[][]{{2.0}}, new int[]{2}, new double[]{0.0}, 1, 1.0);
        LinearModel model2 = new LinearModel( new double[][]{{1},{1}}, new int[][]{{0}}, new double[][]{{2.0}}, new int[]{2}, new double[]{0.0}, 1, 1.0);
        LinearModel model3 = new LinearModel( new double[][]{{1}}, new int[][]{{0}}, new double[][]{{2.0}}, new int[]{2}, new double[]{0.0}, 1, 1.0);
        MultiGroupModel model = new MultiGroupModel(new Model[]{model1, model2, model3},model1.anzVar);
        MultiGroupModel res = new MultiGroupModel(model);
        res.fixParameter(new int[]{0,2}, new double[]{0,0});
        model.rand = new Random(124231232);
        model.createData(new int[]{10,20,50});
        double[][] outData = new double[model1.anzPer+model2.anzPer+model3.anzPer][3];
        for (int i=0; i<model1.anzPer; i++) outData[i] = model1.data[i];
        for (int i=0; i<model2.anzPer; i++) outData[model1.anzPer+i] = new double[]{model2.data[i][0], model2.data[i][1], -999};
        for (int i=0; i<model3.anzPer; i++) outData[model1.anzPer+model2.anzPer+i] = new double[]{model3.data[i][0], -999, -999};
        Statik.writeMatrix(Statik.addRowNumber(outData), "homework9data2.txt", '\t', "\tChild1\tChild2\tChild3");
        
        double[] est = model.estimateML();
        res.setData(model.getData(), new int[]{model.submodel[0].anzPer,model.submodel[1].anzPer,model.submodel[2].anzPer});
        res.estimateML();
        System.out.println("Estimate = "+Statik.matrixToString(est)+", lr = "+(res.ll - model.ll));
    }
    
    public static void multiLevelClassHomework93()
    {
        int[][] dyade = new int[][]{{0,4},{0,8},{0,12},{1,5},{1,9},{1,13},
                                    {2,6},{2,10},{2,14},{3,7},{3,11},{3,15},
                                    {4,9},{4,13},{5,10},{5,14},{6,11},{6,15},
                                    {7,8},{7,12},{8,14},{9,15},{10,12},{11,13}};

        for (int i=0; i<16; i++) {
            System.out.print(i+":  (");
            for (int j=0; j<24; j++) if (dyade[j][0]==i || dyade[j][1]==i) System.out.print((j+1)+",");
            System.out.println(")");
        }
        
        String[] bin = new String[]{"","","","","",""};
        double[][] struct = new double[24][16+6];
        for (int i=0; i<24; i++) {
            struct[i][dyade[i][0]] = struct[i][dyade[i][1]] = 1;
            int k = -1;
             if (dyade[i][0]<4 && dyade[i][1]<8) k=16;
             else if (dyade[i][0]<4 && dyade[i][1]<12) k=17;
             else if (dyade[i][0]<4 && dyade[i][1]<16) k=18;
             else if (dyade[i][0]<8 && dyade[i][1]<12) k=19;
             else if (dyade[i][0]<8 && dyade[i][1]<16) k=20;
             else if (dyade[i][0]<12&& dyade[i][1]<16) k=21;
             bin[k-16] += ((i+1)+", ");
             struct[i][k] = 1;
        } 
        for (int i=0; i<6;i++) System.out.println("Bin "+i+": "+bin[i]);
        double[][] covVal = Statik.diagonalMatrix(new double[]{10,10,10,10,4,4,4,4,1,1,1,1,2,2,2,2,0,0,0,0,0,0}); int[][] covPar = Statik.diagonalMatrix(new int[]{1,1,1,1,2,2,2,2,3,3,3,3,4,4,4,4,-1,-1,-1,-1,-1,-1}, -1);
//        double[] meanVal = new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0, 60, 110, 60, 150, 100, 150}; int[] meanPar = new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1, 5,6,7,8,9,10};
        double[] meanVal = new double[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0, 58, 82, 70, 120, 110, 130}; int[] meanPar = new int[]{-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1,-1, 5,6,7,8,9,10};
        LinearModel model = new LinearModel(struct, covPar, covVal, meanPar, meanVal, 0, 2.0);
        model.evaluateMuAndSigma();
        model.rand = new Random(124231232);
        double[][] data = model.createData(10, model.mu, model.sigma, model.rand); model.setData(data); 
        String header = ""; for (int i=0; i<24; i++) header += "\tDyad"+(i+1);
        Statik.writeMatrix(Statik.addRowNumber(data), "homework9data3.txt", '\t', header);

        double[] start = model.getParameter();
//        model.logStream = System.out;
        double[] est = model.estimateML(start); 
        System.out.println("Creation with = "+Statik.matrixToString(start));
        System.out.println("Full estimate = "+Statik.matrixToString(est)+", -2ll = "+model.ll);

        double[] smeanVal = new double[]{10,10,10,10,50,50,50,50,50,50,50,50,50,50,50,50,0,0,0,0,0,0}; int[] smeanPar = new int[]{5,5,5,5,6,6,6,6,7,7,7,7,8,8,8,8,-1,-1,-1,-1,-1,-1};
        int[] smeanPar1 = new int[]{5,5,5,5,5,5,5,5,6,6,6,6,7,7,7,7,-1,-1,-1,-1,-1,-1};
        int[] smeanPar2 = new int[]{5,5,5,5,6,6,6,6,6,6,6,6,7,7,7,7,-1,-1,-1,-1,-1,-1};
        int[] smeanPar3 = new int[]{5,5,5,5,6,6,6,6,7,7,7,7,7,7,7,7,-1,-1,-1,-1,-1,-1};
        LinearModel smodel = new LinearModel(struct, covPar, covVal, smeanPar, smeanVal, 0, 2.0);
        LinearModel smodel1 = new LinearModel(struct, covPar, covVal, smeanPar1, smeanVal, 0, 2.0);
        LinearModel smodel2 = new LinearModel(struct, covPar, covVal, smeanPar2, smeanVal, 0, 2.0);
        LinearModel smodel3 = new LinearModel(struct, covPar, covVal, smeanPar3, smeanVal, 0, 2.0);

//        smodel.logStream = System.out; smodel3.logStream = System.out;
        smodel.setData(data); smodel1.setData(data);smodel2.setData(data);smodel3.setData(data);
        double[] sest = smodel.estimateML(smodel.getParameter()); 
        double[] sest1 = smodel1.estimateML(smodel1.getParameter()), sest2 =  smodel2.estimateML(smodel2.getParameter()), sest3 = smodel3.estimateML(smodel3.getParameter());
        System.out.println("Small estimate = "+Statik.matrixToString(sest)+", LRs = "+(smodel1.ll-smodel.ll)+", "+(smodel2.ll-smodel.ll)+", "+(smodel3.ll-smodel.ll));
        System.out.println("Small estimate1 = "+Statik.matrixToString(sest1)+", LRs = "+(smodel1.ll-smodel.ll)+", "+(smodel2.ll-smodel.ll)+", "+(smodel3.ll-smodel.ll));
        System.out.println("Small estimate2 = "+Statik.matrixToString(sest2)+", LRs = "+(smodel1.ll-smodel.ll)+", "+(smodel2.ll-smodel.ll)+", "+(smodel3.ll-smodel.ll));
        System.out.println("Small estimate3 = "+Statik.matrixToString(sest3)+", LRs = "+(smodel1.ll-smodel.ll)+", "+(smodel2.ll-smodel.ll)+", "+(smodel3.ll-smodel.ll));
        System.out.println("LR full / small = "+(smodel.ll - model.ll));
        
        double[] drei = new double[9]; Statik.copy(sest3, drei); drei[8] = drei[7];
        smodel.computeLogLikelihood(drei, true); 
        System.out.println("ll = "+smodel.ll);
        smodel.estimateML();
        smodel.estimateML(drei);
    }
    
    public static void multiLevelClassOrdinal() {
        LinearModel model = new LinearModel(new double[][]{{1},{1},{1}}, new int[][]{{-1}}, new double[][]{{1.0}}, new int[]{-1}, new double[]{0.0}, -1, 1.0);
        model.createData(5000);
        double[] th = new double[]{1.0,0.5,-0.5};
        double[][] ordData = new double[model.anzPer][3];
        for (int i=0; i<model.anzPer; i++) for (int j=0; j<3; j++) ordData[i][j] = (model.data[i][j] < th[j]?0.0:1.0);
        Statik.writeMatrix(Statik.addRowNumber(ordData), "ordinalExampleData.txt", '\t', "\tArchery\tShooting\tDart");
    }
    
    public static void multiLevelClassHomework10() {
        Random rand = new Random(31423453);
        int anzClubs = 32, anzPer = 64;
        double[][] data = new double[anzClubs][2*anzPer];
        double eff = 0.5, stdvClub = 1.0, stdvPer = 1.0;
        for (int i=0; i<anzClubs; i++) {
            double club = rand.nextGaussian()*stdvClub;
            for (int j=0; j<anzPer; j++) {
                double per = rand.nextGaussian()*stdvPer;
                data[i][2*j] = j%2; data[i][2*j+1] = club+per+(j%2==0?0:eff);
            }
        }
        String head = ""; for (int i=0; i<anzPer; i++) head += "\tTreatment"+(1+i)+"\tScore"+(1+i);
        Statik.writeMatrix(Statik.addRowNumber(data), "homework10data.txt", '\t', head);
        
        double stdvMatch = 1.0;
        String[][] ordData = new String[1+anzClubs][1+anzPer*4];
        ordData[0][0] = ""; for (int i=0; i<anzPer; i++) {ordData[0][1+4*i] = "Treatment"+(i+1); ordData[0][1+4*i+1] = "MatchA"+(i+1); ordData[0][1+4*i+2] = "MatchB"+(i+1); ordData[0][1+4*i+3] = "MatchC"+(i+1);}
        for (int i=0; i<anzClubs; i++) {
            ordData[i+1][0] = ""+(i+1);
            for (int j=0; j<anzPer; j++) {
                ordData[i+1][1+4*j] = ""+data[i][2*j]; 
                for (int k=0; k<3; k++) {
                    double match = rand.nextGaussian()*stdvMatch, sc = match + data[i][2*j+1];
//                    ordData[i+1][2+4*j+k] = (sc < -0.2?"LOSS":(sc > 0.2?"WIN":"DRAW"));
                    ordData[i+1][2+4*j+k] = (sc < -0.2?"LOSS":"WIN");
                }
            }
        }
        Statik.writeMatrix(ordData, "homework10Data2.txt", '\t', null);
                                                    
        stdvMatch = 1.0;
        ordData = new String[1+anzClubs][1+anzPer*3];
        ordData[0][0] = ""; for (int i=0; i<anzPer; i++) {ordData[0][1+3*i] = "MatchA"+(i+1); ordData[0][1+3*i+1] = "MatchB"+(i+1); ordData[0][1+3*i+2] = "MatchC"+(i+1);}
        for (int i=0; i<anzClubs; i++) {
            ordData[i+1][0] = ""+(i+1);
            for (int j=0; j<anzPer; j++) {
                for (int k=0; k<3; k++) {
                    double match = rand.nextGaussian()*stdvMatch, sc = match + data[i][2*j+1];
//                    ordData[i+1][2+4*j+k] = (sc < -0.2?"LOSS":(sc > 0.2?"WIN":"DRAW"));
                    ordData[i+1][1+3*j+k] = (sc < -0.2?"LOSS":"WIN");
                }
            }
        }
        Statik.writeMatrix(ordData, "homework10Data3.txt", '\t', null);

        
        
//        int p = 2, cl = 32;
//        int p = 4, cl = 16;
//        int p = 8, cl = 8;
//        int p = 16, cl = 4;
        int p = 32, cl = 2;
        double[][] struct = new double[p][1]; for (int i=0;i<p; i++) struct[i][0] = 1;
        LinearModel model1 = new LinearModel(struct, new int[][]{{0}}, new double[][]{{1}}, new int[]{2}, new double[]{0.0}, 1, 1.0);
        LinearModel model2 = new LinearModel(struct, new int[][]{{0}}, new double[][]{{1}}, new int[]{3}, new double[]{0.0}, 1, 1.0);
        MultiGroupModel model = new MultiGroupModel(new Model[]{model1.copy(), model2.copy()}, model1.anzVar);
        MultiGroupModel res = new MultiGroupModel(new Model[]{model1.copy(), model1.copy()}, model.anzVar-1);
        double[][] dat1 = new double[cl][p]; for (int i=0; i<cl; i++) for (int j=0; j<p; j++) dat1[i][j] = data[i][4*j+3];
        double[][] dat2 = new double[cl][p]; for (int i=0; i<cl; i++) for (int j=0; j<p; j++) dat2[i][j] = data[i][4*j+1];
        model.setData(new double[][][]{dat1,dat2}); res.setData(new double[][][]{dat1,dat2});
        
        for (int t=0; t<1; t++) {
            dat1 = new double[cl][p]; for (int i=0; i<cl; i++) for (int j=0; j<p; j++) dat1[i][j] = data[cl*t+i][p*4*t+4*j+3];
            dat2 = new double[cl][p]; for (int i=0; i<cl; i++) for (int j=0; j<p; j++) dat2[i][j] = data[cl*t+i][p*4*t+4*j+1];
            model.setData(new double[][][]{dat1,dat2}); res.setData(new double[][][]{dat1,dat2});
            double[] est = model.estimateML(new double[]{stdvClub*stdvClub,stdvPer*stdvPer,0,eff}); res.estimateML(new double[]{stdvClub*stdvClub,stdvPer*stdvPer,0});
            System.out.println("Estimates = "+Statik.matrixToString(est));
            System.out.println("-2ll = "+model.ll+", "+res.ll+", LR = "+(res.ll - model.ll)+", "+(res.ll - model.ll>3.81?"!!!":""));
        }
    }
    
    public static void bommaeDataSimulation() {
        int anzT = 3, N = 100000; 
        double[][] struct = new double[anzT][2]; 
        for (int i=0; i<anzT; i++) struct[i] = new double[]{1,1+i}; 
        LinearModel model1 = new LinearModel(struct, 
                new int[][]{{2,3},{3,4}}, new double[][]{{1,0},{0,1}},
                new int[]{0,1}, new double[]{0,0}, 5, 1.0);
        model1.rand = new Random(234651151435L);
        model1.createData(N);
        double[] start = model1.getParameter();
        double[] estDir1 = model1.estimateML(start);
        System.out.println(Statik.matrixToString(estDir1));
        Statik.writeMatrix(model1.data, "homogenousGroup.txt", '\t');
        
    }
    
    public static void testModelComputeCovarianceCloseToRealWithParameterConstraint() {
        LinearModel model = new LinearModel(new double[][]{{1,0},{1,1},{1,2}}, new int[][]{{0,1},{1,2}}, new double[][]{{10,2},{2,10}}, 
                new int[]{-1,-1}, new double[]{0,0}, 3, 5.0);
//        LinearModel res = new LinearModel(new double[][]{{1,0},{1,1},{1,2}}, new int[][]{{0,-1},{-1,1}}, new double[][]{{10,0},{0,10}}, 
//                new int[]{-1,-1}, new double[]{0,0}, 2, 5.0);
        model.createData(100);
        double[] fullEst = model.estimateML(); 
//        fullEst[1] = 0.0;
        double[][] sugCov = model.computeCovarianceCloseToRealWithParameterConstraint(null, fullEst);
        System.out.println("Data cov = \r\n"+Statik.matrixToString(model.dataCov));
        System.out.println("Best Guess = \r\n"+Statik.matrixToString(sugCov));
        model.setDataDistribution(sugCov, new double[]{0,0,0}, 100);
        double[] est = model.estimateML();
        System.out.println("est = "+Statik.matrixToString(est));
    }
    
    public static void testFilteredInverse() {
//        double[][] mat = new double[][]{{0,1,1,2},{0,1,1,2},{0,0,1,0}};
//        double[][] mat = new double[][]{{0,1,1,2,5},{0,1,1,2,5},{0,2,2,3,9}};
        double[][] mat = new double[][]{{0,1,1,2,0},{0,1,1,2,0},{0,2,2,3,1}};
        double[][] inv = Statik.pseudoInvert(mat);
        System.out.println(Statik.matrixToString(inv));
        double[][] res = Statik.multiply(mat, inv);
        System.out.println(Statik.matrixToString(res));
        double[] vec1 = new double[]{0,1,1,2,0}, vec2 = new double[]{1,2,0,-1,-1};
        double[] vec = Statik.add(vec1,vec2);
        double[] v1 = Statik.multiply(mat,vec);
        System.out.println(Statik.matrixToString(v1));
        double[] v2 = Statik.multiply(inv,v1);
        System.out.println(Statik.matrixToString(v2));
    }
    
    public static void ttt() {
        double[][] erg = new double[][]{{0.172,3.377,1.744,1.579,0.219,2.902,7.814,3.818,14.099,3.130,0.811,6.714,7.591,0.004,0.000,1.257,0.008,
                -5.008,16.080,11.008,4.785,3.043,0.262,0.062,1.966,1.867,0.259,0.666,3.018,0.593,4.642,5.261,0.000,0.007,8.261,7.241,13.337,0.296,7.949,0.181,-11.681,
                0.046,1.991,0.517,0.027,7.446,-0.955,0.000,-0.006,5.085,0.039,0.102,-0.697,3.361,0.808,0.257,5.463,-0.817,4.076,0.539,1.001,-7.079,0.010,3.362,0.000,
                6.990,4.776,0.094,12.979,0.138,2.403,-0.469,-16.209,5.540,1.677,-0.529,-4.209,4.656,2.852,11.176,-0.313,-4.796,1.677,0.016,-1.980,1.344,-8.194,1.138,
                2.723,5.245,6.827,2.361,6.450,2.844,-0.000,0.005,0.007,3.904,4.036,0.425}};
        System.out.println(Statik.matrixToString(Statik.transpose(erg)));
    }
    
    public static void colloquiumPresentationExample() {

        Random rand = new Random();
        double err = 0.5;
        double[][] realSigma = new double[][]{{1,1,0.4},{1,2,3},{0.4,3,8}};
//        double[][] realSigma = new double[][]{{1,1,1},{1,2,3},{1,3,5}};
        for (int i=0; i<3; i++) realSigma[i][i] += err;
        double[] realMean = new double[]{0,0,0};
        
        LinearModel anaModel = new LinearModel(new double[][]{{1,0},{1,1},{1,2}}, new int[][]{{0,1},{1,2}}, 
                new double[][]{{1,0},{0,1}}, new int[]{-1,-1}, new double[]{0,0}, 3, err);
        LinearModel resModel = anaModel.copy(); resModel.fixParameter(new int[]{0});
        anaModel.evaluateMuAndSigma();
        double[] truePar = anaModel.getParameter();
        double[] resPar = resModel.getParameter();
        anaModel.setDataDistribution(realSigma, realMean); anaModel.anzPer = 1;
        double[] estPar = anaModel.estimateML(truePar);
        System.out.println("Estimated Parameter = "+Statik.matrixToString(estPar));
        double[][] estFacCov = anaModel.covVal;
        System.out.println("Estimated sigma = \r\n"+Statik.matrixToString(anaModel.sigma));

        double[] ev = Model.computeMisspecification(realSigma, realMean, anaModel, new int[]{0}, false, true);
        System.out.println(Statik.matrixToString(ev));
        System.out.println(Statik.inverseMixtureOfChisquares(0.95, new double[]{ev[0]}, rand)+" != "+Statik.FIVEPERCENTTHRESHOLD[0]);
//        System.exit(0);
        
        int trials = 10000, count = 0; double aveEst = 0; int N = 100;
        for (int i=0; i<trials; i++) {
            double[][] data = Model.createData(N, realMean, realSigma, rand);
            anaModel.setData(data);
            double[] est = anaModel.estimateML(truePar); aveEst += est[0];
            double llFull = anaModel.ll;
            resModel.setData(data);
            resModel.estimateML(resPar);
            double lr = resModel.ll - llFull;
            boolean significant = lr > Statik.FIVEPERCENTTHRESHOLD[0];
//            System.out.println("Estimate = "+est[0]+", lr = "+lr+", "+(significant?" *** ":""));
            if (significant) count++;
        }
        System.out.println("Average Estimate = "+(aveEst/trials)+", Significant = "+count); 
    }
     
    public static void colloquiumPresentationExample2() {

            Random rand = new Random();
            double[][] realSigma = new double[][]{{1,0.2},{0.2,1}};
            double[] realMean = new double[]{0,0};
            
            LinearModel anaModel = new LinearModel(new double[][]{{1,1},{0,1}}, new int[][]{{-1,-1},{-1,-1}}, 
                    new double[][]{{0,0},{0,0}}, new int[]{0,1}, new double[]{0,0}, 2, 1.0);
            LinearModel resModel = new LinearModel(new double[][]{{1},{1}}, new int[][]{{-1}}, 
                    new double[][]{{0}}, new int[]{0}, new double[]{0}, 1, 1.0);
            anaModel.evaluateMuAndSigma();
            double[] truePar = anaModel.getParameter();
            double[] resPar = resModel.getParameter();
            anaModel.setDataDistribution(realSigma, realMean); anaModel.anzPer = 1;
            double[] estPar = anaModel.estimateML(truePar);
            System.out.println("Estimated Parameter = "+Statik.matrixToString(estPar));
            double[][] estFacCov = anaModel.covVal;
            System.out.println("Estimated cov = \r\n"+Statik.matrixToString(estFacCov));

            double[] ev = Model.computeMisspecification(realSigma, realMean, anaModel, new int[]{0}, false, true);
            System.out.println(Statik.matrixToString(ev));
            System.out.println(Statik.inverseMixtureOfChisquares(0.95, new double[]{ev[0]}, rand)+" != "+Statik.FIVEPERCENTTHRESHOLD[0]);
//            System.exit(0);
            
            int trials = 10000, count = 0; double aveEst = 0;
            for (int i=0; i<trials; i++) {
                double[][] data = Model.createData(200, realMean, realSigma, rand);
                anaModel.setData(data);
                double[] est = anaModel.estimateML(truePar); aveEst += est[0];
                double llFull = anaModel.ll;
                resModel.setData(data);
                resModel.estimateML(resPar);
                double lr = resModel.ll - llFull;
                boolean significant = lr > Statik.FIVEPERCENTTHRESHOLD[0];
//                System.out.println("Estimate = "+est[0]+", lr = "+lr+", "+(significant?" *** ":""));
                if (significant) count++;
            }
            System.out.println("Average Estimate = "+(aveEst/trials)+", Significant = "+count); 
    }

    public static void parseOutput() {
        File file = new File("output.txt");
        try {
            BufferedReader b = new BufferedReader(new FileReader(file));

            while (b.ready()) 
            {
                String line = Statik.loescheRandWhitespaces(b.readLine());
                while ((line.length()==0) && (b.ready())) line = Statik.loescheRandWhitespaces(b.readLine());
                if (line.length()>0)
                {
                    if (line.contains("LR = ")) {
                        double v = Double.parseDouble(line.substring(line.indexOf("LR = ")+5));
                        System.out.println(v);
                    }
                }
            }
            b.close();
        } catch (Exception e) {System.out.println("Error reading from file "+file.getName()+"."); return;}
    }
    
    public static void ulmanQuickPower() {
        
        int N = 100;
        double svar = 0.1;
        double sMean = 0.1;
        double rel = 0.9;
        int occ = 3;
        double[][] struct = new double[occ][2];
        for (int i=0; i<occ; i++) struct[i] = new double[]{1,i/((double)occ-1.0)};
        
        LinearModel model = new LinearModel(struct, 
                new int[][]{{2,3},{3,4}}, new double[][]{{1,0},{0,svar}},
                new int[]{0,1}, new double[]{0,sMean}, 5, (1.0-rel)/rel);
        
        LinearModel model2 = model.copy(); model2.fixParameter(1, 0.0); 
        LinearModel model3 = model.copy(); model3.fixParameter(4, 0.0); model3.fixParameter(3, 0.0);
        
        double[] par = model.getParameter(), par2 = model2.getParameter(), par3 = model3.getParameter();
        int trials = 100000;
        double p1 = 0, p2 = 0;
        for (int t=0; t<trials; t++) {
            model.setParameter(par);
            model.createData(N);
            model.estimateML(par);
            model2.setData(model.data);
            model3.setData(model.data);
            model2.estimateML(par2); if (model2.ll - model.ll > Statik.FIVEPERCENTTHRESHOLD[0]) p1++;
            model3.estimateML(par3); if (model3.ll - model.ll > Statik.FIVEPERCENTTHRESHOLD[1]) p2++;
        }
        p1 /= (double)trials; p2 /= (double)trials;
        System.out.println("Power means = "+p1);
        System.out.println("Power var slope = "+p2);
    }
    
    public static void ulmanMartinPlanSimulation() {

        int trials = 10000;
        double tNormVar = 9;   // # years of observation
        double annualVarianceSlope = 0.15 / (tNormVar*tNormVar);
//        double annualVarianceSlope = 0.20 / (tNormVar*tNormVar);
        double dropoutInThreeYears = 0.85;
        double interceptSlopeCorrelation = 0.3;
        
        int[] caseN = new int[]{140,180};
        int[] caseM = new int[]{2,3};
        double[] caseRel = new double[]{0.7, 0.8, 0.85, 0.9};
        double[] caseMS = new double[]{0.02, 0.04};
        double[][] caseKT = new double[][]{{3,9},{4,9},{2,3},{2,4.5},{2,6}};

//        Model.rand = new Random(2342342432L);
        long time = System.nanoTime();
        int anzCond = caseN.length*caseM.length*caseKT.length*caseRel.length*caseMS.length;
        double[][] erg = new double[caseN.length*caseM.length*caseRel.length*caseMS.length][caseKT.length*2];
        // 62
        for (int condc = 0; condc<anzCond; condc++) {
            System.out.println("Starting condition "+condc);
            int cond = condc;
            int cKT = cond % caseKT.length; cond /= caseKT.length;
            int cMS = cond % caseMS.length; cond /= caseMS.length;
            int cRel = cond % caseRel.length; cond /= caseRel.length;
            int cM = cond % caseM.length; cond /= caseM.length;
            int cN = cond % caseN.length; cond /= caseN.length;
            
            int N = caseN[cN];
            int M = caseM[cM];                    // # indicator
            int K = (int)caseKT[cKT][0];          // # occasions
            double t = caseKT[cKT][1];            // # years of observation
            double reliability = caseRel[cRel];   // Reliability
            double annualMeanSlope = caseMS[cMS]; // mean slope (per year)
            double dropoutPerOccasion = Math.pow(dropoutInThreeYears, t/(3*(K-1)));
            double isCov = Math.sqrt(annualVarianceSlope) * interceptSlopeCorrelation;
            
            double err = (1-reliability)/(M*reliability);
            double[][] struct = new double[K][2]; for (int i=0; i<K; i++) struct[i] = new double[]{1, i*t/(K-1)};
            
            LinearModel fullModel = new LinearModel(struct, new int[][]{{0,1},{1,2}}, new double[][]{{1,isCov},{isCov,annualVarianceSlope}}, 
                    new int[]{3,4}, new double[]{0,annualMeanSlope}, 5, err);
            MissingDataModel simple = new MissingDataModel(fullModel);
//            LinearModel simple = fullModel; 
            if (K==2) simple.fixParameter(1,0);
            Model resModel1 = simple.copy(); if (K==2) resModel1.fixParameter(1,0); else resModel1.fixParameter(new int[]{1,2}, new double[]{0,0}); 
            Model resModel2 = simple.copy(); if (K==2) resModel2.fixParameter(3,0.0); else resModel2.fixParameter(4,0.0);
            
            double[] start = fullModel.getParameter();
            double[] startRes1 = Statik.subvector(start, new int[]{0,3,4,5});
            double[] startRes2 = Statik.subvector(start, new int[]{0,1,2,3,5});
            if (K==2) {
                startRes1 = Statik.subvector(start, new int[]{0,2,3,4});
                startRes2 = Statik.subvector(start, new int[]{0,1,2,5});
            }
            
//            simple.logStream = resModel1.logStream = System.out;
            double pow1 = 0, pow2 = 0;
            for (int tr = 0; tr < trials; tr++) {
                simple.setParameter(start);
                simple.createDataWithDropout(N, 1, 1-dropoutPerOccasion);
//                simple.createData(N);
                resModel1.setData(simple.getData());
                resModel2.setData(simple.getData());
                
                boolean ok = true;
                try {
                    simple.estimateML(start); 
                    resModel1.estimateML(startRes1); 
                    resModel2.estimateML(startRes2);
                } catch (Exception e) {System.out.println("Failed try!"); trials--; ok = false;}
                if (ok) {
                    if (K == 2) if (resModel1.ll - simple.ll > Statik.FIVEPERCENTTHRESHOLD[0]) pow1++;
                    if (K != 2) if (resModel1.ll - simple.ll > Statik.FIVEPERCENTTHRESHOLD[1]) pow1++;
                    if (resModel2.ll - simple.ll > Statik.FIVEPERCENTTHRESHOLD[0]) pow2++;
                }
            }
            pow1 /= trials; pow2 /= trials;
//            System.out.println(pow1);
//            System.out.println(pow2);
            erg[cMS+caseMS.length*cRel+caseMS.length*caseRel.length*cM+caseMS.length*caseRel.length*caseM.length*cN][cKT] = pow1;
            erg[cMS+caseMS.length*cRel+caseMS.length*caseRel.length*cM+caseMS.length*caseRel.length*caseM.length*cN][cKT+caseKT.length] = pow2;
            Statik.writeMatrix(erg, "martinUlmanBigProject.out", '\t');
        }
        time = (System.nanoTime() - time)/1000000;
        System.out.println("Total Time = "+time+" ms");
    }
    
    public static double computePForThree(double w1, double w2, double w3, double v) {
        Random rand = new Random();
        int trials = 1000000;
        double erg = 0;
        for (int i=0; i<trials; i++) {
            double v1 = rand.nextGaussian();
            double v2 = rand.nextGaussian();
            double v3 = rand.nextGaussian();
            if (w1*v1*v1+w2*v2*v2+w3*v3*v3 > v) erg++;
        }
        erg /= (double)trials;
        return erg;
    }

    // Test of speed for sparse RAM computation. Needs manipulation in RAMModel to activate numerical/symoblic choice. 
    public static void sparseRAMFitTimeComparison() {
        
        int step = 5;
        int anzsteps = 0;
        int startK = 30;
        
        double[][] erg = new double[20][4];
        int ergpos = 0;
        for (int k=startK; k<=startK+anzsteps*step; k+=step) {
            int trials = (k<20?20:(k<40?20:10));
            System.out.println("k = "+k);
            double[][] sym = new double[k+2][k+2], asy = new double[k+2][k+2];
            int[][] symPar = new int[k+2][k+2], asyPar = new int[k+2][k+2]; for (int i=0; i<k+2; i++) for (int j=0; j<k+2; j++) symPar[i][j] = asyPar[i][j] = -1;
            sym[0][0] = 5; sym[1][1] = 5; symPar[0][0] = 0; symPar[1][1] = 1; symPar[0][1] = symPar[1][0] = 2; for (int i=0; i<k; i++) {symPar[2+i][2+i] = 6+i; sym[2+i][2+i] = 1;}
            for (int i=0; i<k; i++) {asy[i+2][0] = 1; asy[i+2][1] = i; asyPar[i+2][0] = 3;} 
            double[] mean= new double[k+2]; int[] meanPar = new int[k+2]; for (int i=0; i<k+2; i++) {mean[i] = 0; meanPar[i] = -1;}
            meanPar[0] = 4; meanPar[1] = 5;
            
            RAMModel model = new RAMModel(symPar, sym, asyPar, asy, meanPar, mean, k);
            double[] start = model.getParameter(); 

            model.rand = new Random(2341211); model.callCount = 0;
            model.debugGradientAndHessianComputationIsNumerical = true;       // for this to be effective, computeLogLikelihoodDerivatives need to be manipulated. 
            int excounter = 0;
            long time = System.nanoTime();
            for (int i=0; i<trials; i++) {
                model.setParameter(start);
                model.createData(100); 
                try {model.estimateML();} catch (Exception e) {excounter++;}
            }
            time = System.nanoTime() - time;
            double effTime = (double)time / ((double)model.callCount * 1000000.0);
            double totTime = (double)time / ((double)trials * 1000000);
            System.out.println("Numerical Time Total        = "+totTime+" ms.");
            System.out.println("Numerical Time Per Hessian  = "+effTime+" ms.");
            System.out.println("Numerical Calls of Hessian  = "+(double)model.callCount / (double)trials+".");
            System.out.println("Numerical Unsuccessful Opt  = "+excounter+".");
            erg[ergpos][0] = totTime; erg[ergpos][1] = (double)model.callCount / (double)trials; 
            
            model.rand = new Random(2341211); model.callCount = 0;
            model.debugGradientAndHessianComputationIsNumerical = false;       // for this to be effective, computeLogLikelihoodDerivatives need to be manipulated. 
            excounter = 0;
            time = System.nanoTime();
            for (int i=0; i<trials; i++) {
                model.setParameter(start);
                model.createData(100); 
                try {model.estimateML();} catch (Exception e) {excounter++;}
            }
            time = System.nanoTime() - time;
            effTime = (double)time / ((double)model.callCount * 1000000.0);
            totTime = (double)time / ((double)trials * 1000000);
            System.out.println("Symbolic  Time Total        = "+totTime+" ms.");
            System.out.println("Symbolic  Time Per Hessian  = "+effTime+" ms.");
            System.out.println("Symbolic  Calls of Hessian  = "+(double)model.callCount / (double)trials+".");
            System.out.println("Symbolic  Unsuccessful Opt  = "+excounter+".");
            erg[ergpos][2] = totTime; erg[ergpos][3] = (double)model.callCount / (double)trials; 
            System.out.println(Statik.matrixToString(erg));
            ergpos++;
        }
    }
    
    
    public static void midtermDatamining() {
        double[][] data = Statik.loadMatrix("midtermDM2012.txt", '\t');
        System.out.println("Data set = \r\n"+Statik.matrixToLatexString(data,0));
        double[][] cov = Statik.covarianceMatrix(data);
        double[] mean = Statik.meanVector(data);
        System.out.println("Mean = "+Statik.matrixToString(mean)+"\r\nCov = \r\n"+Statik.matrixToLatexString(cov));
        System.out.println("Trace of Sigma = "+Statik.trace(cov));
        double[][] evec = Statik.identityMatrix(5); double[] eval = new double[5];
        Statik.eigenvalues(cov, 0.0001, eval, evec);
        System.out.println("Eigenvalues = "+Statik.matrixToString(eval)+"\r\nEigenvectors = \r\n"+Statik.matrixToString(evec));
        
        double[][] datawm = new double[10][5];
        for (int i=0; i<10; i++) for (int j=0; j<2; j++) datawm[i][j] -= 15;
        double[][] covW = Statik.multiply(data, Statik.transpose(datawm));
        System.out.println("CovW  = \r\n"+Statik.matrixToLatexString(covW));
        double[][] evec2 = Statik.identityMatrix(10); double[] eval2 = new double[10];
        Statik.eigenvalues(covW, 0.0001, eval2, evec2);
        System.out.println("Eigenvalues W = "+Statik.matrixToString(eval2)+"\r\nEigenvectors W = \r\n"+Statik.matrixToString(evec2));
        
        double[][] qw = new double[][]{{.3,.45,-.15,.21,.15},{-.1,-.23,.05,.84,-.06}};
        double[][] transDataW = Statik.multiply(qw, Statik.transpose(data));
        System.out.println("Transformed = \r\n"+Statik.matrixToString(transDataW));
        double[][] backTrans = Statik.multiply(Statik.transpose(qw), transDataW);
        System.out.println("back = \r\n"+Statik.matrixToString(backTrans));
        
        double[] me = Statik.meanVector(Statik.transpose(transDataW));
        System.out.println("mean = "+Statik.matrixToString(me));
        
        double[][] qw2 = new double[][]{{-.99,-0.01,-0.99,-.01,1},{-0.01,-0.99,-0.99,-0.01,2}};
        double[][] transDataW2 = Statik.multiply(qw2, Statik.transpose(data));
        System.out.println("Transformed = \r\n"+Statik.matrixToString(transDataW2));
        double[][] backTrans2 = Statik.multiply(Statik.transpose(qw), transDataW2);
        System.out.println("back = \r\n"+Statik.matrixToString(backTrans2));
        
        double[] me2 = Statik.meanVector(Statik.transpose(transDataW2));
        System.out.println("mean = "+Statik.matrixToString(me2));
        
    }
    
    public static void testInversion() {
        int trials = 10;
        
        int size = 100;
        Random rand = new Random(21387102984L);
        double[][] mat = new double[size][size];
        double[][] inv = new double[size][size];
        double[][] work = new double[size][size];
        double[][] work2 = new double[size][size];
        double[][] work3 = new double[size][size];
        double[][] work4 = new double[size][size];
        double[] workVec1 = new double[size];
        double[] workVec2 = new double[size];
        double[] workVec3 = new double[size];
        double[] b = new double[size];
        double[] erg = new double[size];
        double[] cor = new double[size];
        double[] cor2 = new double[size];
        
        for (int i=0; i<size; i++) b[i] = rand.nextDouble()*2-1;

        // create matrix & solution
        double[] firstEV = new double[]{0};
        for (int i=0; i<size; i++) for (int j=0; j<size; j++) work[i][j] = work[j][i] = rand.nextDouble()*2-1;
        Statik.orthogonalize(work);
        for (int i=0; i<size; i++) Statik.normalize(work[i]);
        Statik.setToZero(mat);
        Statik.setToZero(cor);
        final double ZEROEPS = 0.00001;
        for (int i=0; i<size; i++) {
            double ev = 1;
            if (i<firstEV.length) ev = firstEV[i];
            for (int j=0; j<size; j++) for (int k=0; k<size; k++) mat[j][k] += ev*work[i][j]*work[i][k];
            
            double v = Statik.multiply(b,work[i]);
            for (int j=0; j<size; j++) if (ev != 0.0) cor[j] += (1.0/ev)*work[i][j]*v;
            for (int j=0; j<size; j++) 
                if (ev > ZEROEPS)       cor2[j] += (1.0/ev)*work[i][j]*v;
                else if (ev < -ZEROEPS) cor2[j] -= (1.0/ev)*work[i][j]*v;
                else                    cor2[j] += (1.0/ZEROEPS)*work[i][j]*v;
        }
        
//        mat = new double[][]{{3,7,10},{2,3,5},{5,6,11}}; 

        long time; double quality;

        time = System.nanoTime();
        for (int i=0; i<trials; i++) Statik.solveNaiveWithEigenvaluePositiviation(mat, b, erg, ZEROEPS, workVec1, work, work2, work3);
        time = System.nanoTime() - time;
        Statik.multiply(mat, erg, workVec1);
        Statik.subtract(workVec1, b, workVec2);
        double quality1 = Statik.norm(workVec2);
        Statik.subtract(erg, cor2, workVec2);
        double quality2 = Statik.norm(workVec2);
        System.out.println("Naive hybrid\tTime = \t"+(time/1000000)+"ms,\tquality = "+(1.0/quality1)+",\talternative quality = "+(1.0/quality2)+".");
        
        /*
        time = System.nanoTime();
        for (int i=0; i<trials; i++) Statik.solveByConjugateGradient(mat, b, 0.000001, 0, 0, true, erg, workVec1, workVec2, workVec3);
        time = System.nanoTime() - time;
        Statik.multiply(mat, erg, workVec1);
        Statik.subtract(workVec1, b, workVec2);
        quality = Statik.norm(workVec2);
        System.out.println("CG Method\tTime = \t"+(time/1000000)+"ms,\tquality = "+(1.0/quality)+".");
        */
        time = System.nanoTime();
        for (int i=0; i<trials; i++) Statik.invert(mat, inv, work);
        time = System.nanoTime() - time;
        Statik.multiply(inv, b, erg);
        Statik.multiply(mat, erg, workVec1);
        Statik.subtract(workVec1, b, workVec2);
        quality = Statik.norm(workVec2);
        System.out.println("Gauss    \tTime = \t"+(time/1000000)+"ms,\tquality = "+(1.0/quality)+".");
        
        try {
        time = System.nanoTime();
        for (int i=0; i<trials; i++) Statik.invertSymmetricalPositiveDefinite(mat, inv);
        time = System.nanoTime() - time;
        Statik.multiply(inv, b, erg);
        Statik.multiply(mat, erg, workVec1);
        Statik.subtract(workVec1, b, workVec2);
        quality = Statik.norm(workVec2);
        System.out.println("Cholesky\tTime = \t"+(time/1000000)+"ms,\tquality = "+(1.0/quality)+".");
        } catch (Exception e) {System.out.println("Cholesky failed.");}

        time = System.nanoTime();
        for (int i=0; i<trials; i++) Statik.pseudoInvertSquare(mat, inv, work, work2, work3, 0.000001);
        time = System.nanoTime() - time;
        Statik.multiply(inv, b, erg);
        Statik.multiply(mat, erg, workVec1);
        Statik.subtract(workVec1, b, workVec2);
        quality = Statik.norm(workVec2);
        System.out.println("Gauss(Pseudo)\tTime = \t"+(time/1000000)+"ms,\tquality = "+(1.0/quality)+".");

        time = System.nanoTime();
        for (int i=0; i<trials; i++) Statik.invertNegativeEigenvalues(mat, inv, work, work2, workVec1, true, false, null);
        time = System.nanoTime() - time;
        Statik.multiply(inv, b, erg);
        Statik.multiply(mat, erg, workVec1);
        Statik.subtract(workVec1, b, workVec2);
        quality = Statik.norm(workVec2);
        System.out.println("Eigenvalues\tTime = \t"+(time/1000000)+"ms,\tquality = "+(1.0/quality)+".");

    
    }
    
    public static void ldeFitTest() {
        
        int length = 1000;
        double noise = 1;
        double weightFromDisplacement = -.16;
        double weightFromDerivative = 0.02;
        double resetValue = 4*noise;
        
        double eps = 0.01, deltaT = 1;
        double x = 0, xd = 1, xdd = 0, t = 0;
        double[] data = new double[length];
        int dataIx = 0;
        for (int i=0; dataIx < length; i++) {
            xdd = weightFromDisplacement*x + weightFromDerivative*xd;
            x += xd * eps;
            xd += xdd*eps;
            t += eps;
            if (Math.abs(x) > resetValue) {x = 0; xd = 1; xdd = 0;}
            
            if (Math.floor((i+1)*eps/deltaT) > Math.floor(i*eps/deltaT)) 
                data[dataIx++] = x + Model.staticRandom.nextGaussian()*noise;
        }
        
        
        double[] fits = LinearModel.fitOscilatorModel(data, 0, length, 1, 8, deltaT);  
        Statik.writeMatrix(data, "toDan.txt");
        System.out.println("Fits  = "+Statik.matrixToString(fits));
    }

    public static void coupledLdeFitTest() {
        
        int length = 500;
        double noise = 0.1;
        double weightFromDisplacementX = -.15;
        double weightFromDerivativeX = 0.05;
        double weightFromDisplacementYX = -.00225;
        double weightFromDerivativeYX = 0.0030;
        double weightFromDisplacementY = -.20;
        double weightFromDerivativeY = -0.5;
        double weightFromDisplacementXY = -.00075;
        double weightFromDerivativeXY = 0.0015;
        
        double eps = 0.01, deltaT = 1;
        double x = 0, xd = 1, xdd = 0, y = 0, yd = 1, ydd = 0, t = 0;
        double[][] data = new double[length][2];
        int dataIx = 0;
        for (int i=0; dataIx < length; i++) {
            xdd = weightFromDisplacementX*x + weightFromDerivativeX*xd + weightFromDisplacementYX*y + weightFromDerivativeYX*yd;
            ydd = weightFromDisplacementY*y + weightFromDerivativeY*yd + weightFromDisplacementXY*x + weightFromDerivativeXY*xd;
            x += xd * eps;
            xd += xdd*eps;
            y += yd * eps;
            yd += ydd*eps;
            t += eps;
            
            if (Math.floor((i+1)*eps/deltaT) > Math.floor(i*eps/deltaT)) { 
                data[dataIx][0] = x + Model.staticRandom.nextGaussian()*noise;
                data[dataIx][1] = y + Model.staticRandom.nextGaussian()*noise;
                dataIx++;
            }
        }
        
        
        double[][] fits = LinearModel.fitCoupledOscilatorModel(data, 0, length, 1, 5, deltaT);
        
        System.out.println("Data  = "+Statik.matrixToString(Statik.submatrix(data, new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19})));
        System.out.println("Fits  = "+Statik.matrixToString(fits));
    }
    
    public static void coupledLdeFitDan() {
        
        double[][] rawData = Statik.loadMatrix("SmallLENAClean.csv", ',');
        
        Vector<double[]> datVec = new Vector<double[]>();
        for (int i=0; i<rawData.length; i++) {
            if (rawData[i][0] == 1) {
                datVec.add(new double[]{rawData[i][2], rawData[i][4]});
            }
        }
        int length = datVec.size();
        double[][] data = new double[length][];
        double[] justX = new double[length];
        for (int i=0; i<length; i++) data[i] = datVec.elementAt(i);
        for (int i=0; i<length; i++) justX[i] = data[i][0];
        
        double deltaT = 1;
        int embedDim = 8;
        int tau = 1;
        
        double[][] fits = LinearModel.fitCoupledOscilatorModel(data, 0, length, tau, embedDim, deltaT);
        double[] fitsSingle = LinearModel.fitOscilatorModel(justX, 0, length, tau, embedDim, deltaT);
        
        System.out.println("Data  = "+Statik.matrixToString(Statik.submatrix(data, new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19})));
        System.out.println("Fits  (First line to xdd, from x,xd,y,yd, second line to ydd, same order) = \r\n"+Statik.matrixToString(fits));
        System.out.println("Fits  just 1st = \r\n"+Statik.matrixToString(fitsSingle));
    }
    
    public static void coupledLdeFitDanEmbeded() {
        
        String filename = "tEmbeddedNleNADirect.csv";
        double[][] data = Statik.loadMatrix(filename, ',');
        
        int length = data.length;
        
        double deltaT = 1;
        int embedDim = 8;
        int tau = 1;
        
        double[][] fits = LinearModel.fitCoupledOscilatorModel(data, 0, length, tau, embedDim, deltaT, true);
        
        System.out.println("Data  = "+Statik.matrixToString(Statik.submatrix(data, new int[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19})));
        System.out.println("Fits "+filename+" (First line to xdd, from x,xd,y,yd, second line to ydd, same order) = \r\n"+Statik.matrixToString(fits));

        double[][] justPLE = new double[length][embedDim];
        for (int i=0; i<length; i++) for (int j=0; j<justPLE[i].length; j++) justPLE[i][j] = data[i][j];
        
        double[] fitsSingle = LinearModel.fitOscilatorModel(justPLE, 0, length, tau, embedDim, deltaT);
        System.out.println("Fits non-coupled oscillator = \r\n"+Statik.matrixToString(fitsSingle));
    }

    public static void danPleOnlyFitCoupled() {
        
        double[][] data = Statik.loadMatrix("tEmbedPLE.csv", ',');
        
        int length = data.length;
        
        double deltaT = 1;
        int embedDim = 8;
        int tau = 1;
        
        double[] fitsSingle = LinearModel.fitOscilatorModel(data, 0, length, tau, embedDim, deltaT);
        System.out.println("Fits non-coupled oscillator = \r\n"+Statik.matrixToString(fitsSingle));
    }

    
    public static void ldeFitDanData() {
        
        double[][] fileData = Statik.loadMatrix("DanData.txt", '\t');

        int anzPer = 335, sr = 0, embedDim = 8;
        double[][] erg = new double[anzPer][5];
        int pid = 1;
        for (int i=0; pid<anzPer; i++) {
            double[] ple = new double[61], nle = new double[61];
            for (int j=0; j<61; j++)  {ple[j] = fileData[sr+j][1]; nle[j] = fileData[sr+j][2];}
            
            double[] fitsPle = LinearModel.fitOscilatorModel(ple, 0, 61, 1, embedDim, 1.0);  
            double[] fitsNle = LinearModel.fitOscilatorModel(nle, 0, 61, 1, embedDim, 1.0);
            pid = (int)Math.round(fileData[sr][0]);
            erg[i][0] = fileData[sr][0]; erg[i][1] = fitsPle[0]; erg[i][2] = fitsPle[1]; erg[i][3] = fitsNle[0]; erg[i][4] = fitsNle[1];
            System.out.println(Statik.matrixToString(erg[i]));
            sr += 61;
        }
        
        double[][] simulationDataFile = Statik.loadMatrix("DanSimulationData.txt", '\t');
        double[] simulatinData = new double[61];
        for (int i=0; i<61; i++) simulatinData[i] = simulationDataFile[i][0];
        double[] simulationFit = LinearModel.fitOscilatorModel(simulatinData, 0, 61, 1, embedDim, 1.0);
        System.out.println("Simulation = "+Statik.matrixToString(simulationFit));
        
        
    }
    
    public static void dataMiningHW6() {
        double[][] data = new double[][]{{2  ,   0  ,   1  ,   4   ,  3},
                {8  ,   1  ,   4  ,   1   ,  1},
                {2  ,   1  ,   2  ,   6   ,  2},
                {9  ,   5  ,   6  ,   1   ,  4},
                {7  ,   8  ,   6  ,   1   ,  2},
                {6  ,   2  ,   4  ,   3   ,  0},
                {0  ,   5  ,   4  ,   7   ,  5},
                {7  ,   9  ,   8  ,   1   ,  8},
                {1  ,   9  ,   8  ,   9   ,  8},
                {1  ,   1  ,   3  ,   4   ,  1}};
        double[][] Q = new double[][]{{0.2255,-0.7172},{0.7144,0.0997},{0.4694,-0.0333},{-0.0484,0.6234},{0.4649,0.2933}};
        double[] mean = Statik.meanVector(data);
        for (int i=0; i<data.length;  i++) for (int j=0; j<data[i].length; j++) data[i][j] -= mean[j];
        double[][] transData = Statik.multiply(Statik.transpose(Q), Statik.transpose(data));
        System.out.println(Statik.matrixToString(Statik.transpose(transData)));
        double[][] reTrans = Statik.multiply(Q, transData);
        System.out.println(Statik.matrixToLatexString(Statik.transpose(reTrans)));
        System.out.println("\r\n\r\n"+Statik.matrixToLatexString(Statik.covarianceMatrix(Statik.transpose(reTrans))));

        double[][] q = Statik.identityMatrix(5); double[] ev = new double[5];
        Statik.eigenvalues(Statik.covarianceMatrix(data), 0.0001, ev, q);
        System.out.println("\r\nPCA Q = \r\n"+Statik.matrixToString(q));
        for (int i=0; i<5; i++) for (int j=0; j<5; j++) q[i][j] /= Math.sqrt(ev[j]);
        System.out.println("\r\nWhite Q = \r\n"+Statik.matrixToLatexString(q));
        
        double[][] whiteData = Statik.multiply(Statik.transpose(q), Statik.transpose(data));
        System.out.println("\r\nWhite data = \r\n"+Statik.matrixToLatexString(Statik.transpose(whiteData)));
        for (int i=0; i<data.length;  i++) for (int j=0; j<data[i].length; j++) data[i][j] += mean[j];
        double[][] whiteDataMeans = Statik.multiply(Statik.transpose(q), Statik.transpose(data));
        System.out.println("\r\nWhite data = \r\n"+Statik.matrixToLatexString(Statik.transpose(whiteDataMeans)));
        System.out.println("White Covariance = "+Statik.matrixToString(Statik.covarianceMatrix(Statik.transpose(whiteData))));
        
        
        
    }
    
    public static void mathPsyHW8() {
        int[][] symPar = new int[7][7]; for (int i=0; i<7; i++) for (int j=0; j<7; j++) symPar[i][j] = -1;
        symPar[0][0] = 0; symPar[1][1] = 1; symPar[0][1] = symPar[1][0] = 2; for (int i=2; i<7; i++) symPar[i][i] = i+1;
        double[][] symVal = new double[7][7]; symVal[0][0] = symVal[1][1] = 1.0; symVal[0][1] = symVal[1][0] = 0.25; for (int i=2; i<7; i++) symVal[i][i] = 0.5;
        int[][] asyPar = new int[7][7]; for (int i=0; i<7; i++) for (int j=0; j<7; j++) asyPar[i][j] = -1;
        double[][] asyVal = new double[7][7]; for (int i=2; i<5; i++) asyVal[i][0] = 1; for (int i=5; i<7; i++) asyVal[i][1] = 1;
        int[] meanPar = new int[7]; for (int i=0; i<7; i++) meanPar[i] = -1;

        Model model = new RAMModel(symPar,symVal,asyPar, asyVal, meanPar, new double[7], new int[]{2,3,4,5,6});
        model.setRandomSeed(342342432L);
        double[][] data = model.createData(100);
        Statik.writeMatrix(data, "Homework8Data.txt", '\t', "A1\tA2\tA3\tB1\tB2");
        double[] est = model.estimateML();
        System.out.println("Estimates = "+Statik.matrixToString(est)+", -2ll = "+model.ll);
    }
    
    public static void testQRDecomposition() {
//        double[][] matrix = new double[][]{{5,2,3},{1,3,1},{8,11,3},{7,2,4}};
//        double[][] matrix = new double[][]{{1,1,0,0},{1,2,0,0},{1,3,0,0},{0,0,1,1},{0,0,1,2},{0,0,1,3}};
        double[][] matrix = new double[][]{{5,2,3,4},{1,3,1,2},{8,11,3,0}};
        int n = matrix.length, m = matrix[0].length;
        double[][] q = new double[n][n], r = new double[n][m];
        double[] work = new double[n];
        Statik.qrDecomposition(matrix, q, r, work);
        
        System.out.println("Q^T = "+Statik.matrixToString(q));
        System.out.println("R = "+Statik.matrixToString(r));
        System.out.println("Q^TR = "+Statik.matrixToString(Statik.multiply(Statik.transpose(q),r)));
        System.out.println("Q^TQ = "+Statik.matrixToString(Statik.multiply(Statik.transpose(q),q)));
        double det = Statik.determinant(q);
        System.out.println("Det Q = "+det);
    }
    
    public static void vonOertzenBrandmaierPANDASecondSimulation() {
        int n = 6;
        double se = 24;
        double si = 80;
//        double ss = 80;
        double ss = 0.44;
        double survivalPerYear = 0.8279; 
//        double survivalPerYear = 1.0; 
        double[] t = new double[]{0,2,4,6,8.5,13};
        for (int i=0; i<t.length; i++) t[i] /= 13.0;
        int waves = t.length; 
        
        double per0 = 491.88;
        double[][] erg = new double[60][3];
        for (int i=0; i<erg.length; i++) {
            double lambda = 1 + 0.5*i;
//            double lambda = 13;
            double tsum = 0, tsqrsum =0, tjsum = 0, per = per0, jsqrsum = 0;
            double[] totsum = new double[3];
            for (int j=1; j<=waves; j++) {
                tsum += t[j-1]; tsqrsum += t[j-1]*t[j-1]; tjsum += j*t[j-1]; jsqrsum += j*j; 
                double eqSl = se*(si*j+se) / ((si*j+se)*lambda*lambda*tsqrsum - si*lambda*lambda*tsum*tsum); totsum[0] += per/eqSl;
                double eqIn = se*(ss*lambda*lambda*tsqrsum+se) / ((ss*lambda*lambda*tsqrsum+se)*j - ss*lambda*lambda*tsum*tsum); totsum[1] += per/eqIn;
                
                double ltjss = lambda*lambda*tjsum*tjsum, denom = ((jsqrsum+lambda*tjsum)*(jsqrsum+lambda*tjsum));
                double a11 = si*jsqrsum*jsqrsum/ltjss + ss*ltjss/denom + se*jsqrsum/denom;
                double a12 = ss*ltjss/denom;
                double a22 = ss + se*jsqrsum/(jsqrsum*lambda*lambda*tsqrsum-lambda*lambda*tjsum*tjsum);
                double det = a11*a22 - a12*a12;
                double eqCo = det/(a11+a22-2*a12); if (j>1) totsum[2] += per/eqCo;
                
                if (j < t.length) per *= Math.pow(survivalPerYear, lambda*(t[j]-t[j-1]));
            }
            for (int j=0; j<erg[i].length; j++) erg[i][j] = per0/totsum[j];
            System.out.println(Statik.matrixToString(erg[i]));
        }
    }
    
    private static double vonOertzenBrandmaierPANDAEquation2(double se, double si, int n, double[] t) {
        double tsum = 0, tsqrsum = 0;
        for (int j=1; j<=t.length; j++) {
            tsum += t[j-1]; tsqrsum += t[j-1]*t[j-1]; 
        }
        return se*(si*n+se) / ((si*n+se)*tsqrsum - si*tsum*tsum);
    }
    
    public static void vonOertzenBrandmaierPANDAPowerSimulation() {
        double se = 24;
        double si = 80;
        double ss = .44;
        int repetitions = 500;
        int trials = 10000;
        int anzPer = 100;
        Random rand = Model.staticRandom;
        rand.setSeed(235293423412L);
        double[] start = new double[]{ss};

        LinearModel modelFull1 = new LinearModel(new double[2][2], new int[][]{{-1,-1},{-1,0}}, new double[][]{{si,0},{0,ss}}, new int[]{-1,-1}, new double[]{0,0}, -1, se);
        LinearModel modelRes1 = new LinearModel(new double[2][2], new int[][]{{-1,-1},{-1,-1}}, new double[][]{{si,0},{0,0}}, new int[]{-1,-1}, new double[]{0,0}, -1, se);
        LinearModel modelFull2 = new LinearModel(new double[][]{{1.0}}, new int[][]{{0}}, new double[][]{{ss}}, new int[]{-1}, new double[]{0}, -1, 0.0);
        LinearModel modelRes2 = new LinearModel(new double[][]{{1.0}}, new int[][]{{-1}}, new double[][]{{0}}, new int[]{-1}, new double[]{0}, -1, 0.0);
        double[][] errorsVal = new double[15][]; int[][] errorsPar = new int[15][]; 
        for (int i=0; i<errorsVal.length; i++) {
            errorsVal[i] = new double[i]; for (int j=0; j<i; j++) errorsVal[i][j] = se;
            errorsPar[i] = new int[i]; for (int j=0; j<i; j++) errorsPar[i][j] = -1;
        }
        
        double[][] erg = new double[repetitions][2];
        for (int r = 0; r<repetitions; r++) {
            int n = rand.nextInt(9)+2;
            double[] t = new double[n]; t[0] = 0; 
            for (int i=1; i<n; i++) t[i] = t[i-1] + (double)rand.nextInt(16)/8.0;
            double[][] struct = new double[n][2]; for (int i=0; i<n; i++) struct[i] = new double[]{1,t[i]};
            modelFull1.structure = struct; modelFull1.anzVar = n; modelRes1.structure = struct; modelRes1.anzVar = n;
            modelFull1.errVal = errorsVal[n]; modelFull1.errPar = errorsPar[n]; 
            modelRes1.errVal = errorsVal[n]; modelRes1.errPar = errorsPar[n];
            modelFull2.errVal[0] = modelRes2.errVal[0] = vonOertzenBrandmaierPANDAEquation2(se, si, n, t);
            
            int c1 = 0, c2 = 0;
            for (int tr=0; tr < trials; tr++) {
                modelFull1.setParameter(start); double[][] data1 = modelFull1.createData(anzPer); modelRes1.setData(data1); 
                modelFull1.estimateML(start); modelRes1.evaluateMuAndSigma(); modelRes1.computeLogLikelihood();
                double lr1 = modelRes1.ll - modelFull1.ll;
                modelFull2.setParameter(start); double[][] data2 = modelFull2.createData(anzPer); modelRes2.setData(data2);
                modelFull2.estimateML(start); modelRes2.evaluateMuAndSigma(); modelRes2.computeLogLikelihood();
                double lr2 = modelRes2.ll - modelFull2.ll;
                
                if (lr1 > Statik.FIVEPERCENTTHRESHOLD[0]) c1++; 
                if (lr2 > Statik.FIVEPERCENTTHRESHOLD[0]) c2++; 
            }
            erg[r][0] = (double)c1/(double)trials;
            erg[r][1] = (double)c2/(double)trials;
            if (r < 10) System.out.println(Statik.matrixToString(t)+"\t"+erg[r][0]+"\t"+erg[r][1]);
        }
        System.out.println(Statik.matrixToString(erg));
    }
    
    public static void evaluation4005() {
        Vector<Vector<String>> v = (Vector<Vector<String>>)Statik.loadDataMatrix("pretest.txt", '\t');
        
        int anzPre = v.size();
        Vector<double[]> data = new Vector<double[]>(anzPre); // ID, is4005, 11 errors, 3 non-errors, 11 post-errors, 2 post-non-errors
        for (Vector<String> line:v) {
            double[] ergLine = new double[29];
            ergLine[0] = Double.parseDouble(line.elementAt(1));
            ergLine[1] = line.elementAt(0).length()==0?0:Double.parseDouble(line.elementAt(0));
            for (int i=0; i<11; i++) ergLine[2+i] = (2+i>=line.size() || line.elementAt(2+i).length()==0?0:Double.parseDouble(line.elementAt(2+i)));
            for (int i=0; i<3; i++) ergLine[13+i] = (13+i>=line.size() || line.elementAt(13+i).length()==0?0:Double.parseDouble(line.elementAt(13+i)));
            data.addElement(ergLine);
            for (int i=0; i<13; i++) ergLine[16+i] = Model.MISSING;
        }
        
        v = (Vector<Vector<String>>)Statik.loadDataMatrix("posttest.txt", '\t');
        
        int anzPost = v.size();
        for (Vector<String> line:v) {
            int id = Integer.parseInt(line.elementAt(1));
            double[] ergLine = null;
            for (int i=0; i<data.size(); i++) if (id == data.elementAt(i)[0]) ergLine = data.elementAt(i);
            if (ergLine == null) {
                ergLine = new double[29]; 
                for (int i=0; i<16; i++) ergLine[i] = Model.MISSING;
                ergLine[0] = Double.parseDouble(line.elementAt(1));
                ergLine[1] = line.elementAt(0).length()==0?0:Double.parseDouble(line.elementAt(0));
                data.add(ergLine);
            }
            for (int i=0; i<11; i++) ergLine[16+i] = (2+i>=line.size() || Statik.loescheRandWhitespaces(line.elementAt(2+i)).length()==0?0:Double.parseDouble(line.elementAt(2+i)));
            for (int i=0; i<2; i++) ergLine[16+11+i] = (13+i>=line.size() || Statik.loescheRandWhitespaces(line.elementAt(13+i)).length()==0?0:Double.parseDouble(line.elementAt(13+i)));
        }
        int anzPer = data.size();
        double[][] dData = new double[anzPer][];
        int j = 0; for (double[] e:data) dData[j++] = e;
        System.out.println(Statik.matrixToString(dData));
        
        double[][] sumScore = new double[anzPer][4]; // ID, is4005, pretest total score, posttest total score
        for (int i=0; i<anzPer; i++) {
            sumScore[i][0] = dData[i][0]; sumScore[i][1] = dData[i][1];
            if (Model.isMissing(dData[i][2])) sumScore[i][2] = Model.MISSING; else {
                sumScore[i][2] = 0;
                for (j=2; j<13; j++) sumScore[i][2] += dData[i][j];
                for (j=13; j<16; j++) sumScore[i][2] -= dData[i][j];
            }
            if (Model.isMissing(dData[i][16])) sumScore[i][3] = Model.MISSING; else {
                sumScore[i][3] = 0;
                for (j=16; j<27; j++) sumScore[i][3] += dData[i][j];
                for (j=27; j<29; j++) sumScore[i][3] -= dData[i][j];
            }
        }
        System.out.println("Total Scores Post / Pre: \r\nID\tis4005\tPretest\tPosttest\r\n"+Statik.matrixToString(sumScore));
        
        double[][] sumScore2 = new double[anzPer][4]; // ID, is4005, pretest positive score, posttest positive score
        for (int i=0; i<anzPer; i++) {
            sumScore2[i][0] = dData[i][0]; sumScore2[i][1] = dData[i][1];
            if (Model.isMissing(dData[i][2])) sumScore2[i][2] = Model.MISSING; else {
                sumScore2[i][2] = 0;
                for (j=2; j<13; j++) sumScore2[i][2] += dData[i][j];
            }
            if (Model.isMissing(dData[i][16])) sumScore2[i][3] = Model.MISSING; else {
                sumScore2[i][3] = 0;
                for (j=16; j<27; j++) sumScore2[i][3] += dData[i][j];
            }
        }
        System.out.println("Total Positive Post / Pre: \r\nID\tis4005\tPretest\tPosttest\r\n"+Statik.matrixToString(sumScore2));
        
    }
    
    public static void sinyLGMsimulation() {
        
        int anzObs = 100;
        
        int[][] symPar = new int[2+anzObs][2+anzObs];
        double[][] symVal = new double[2+anzObs][2+anzObs];
        int[][] asyPar = new int[2+anzObs][2+anzObs];
        double[][] asyVal = new double[2+anzObs][2+anzObs];
        int[] meanPar = new int[2+anzObs];
        double[] meanVal = new double[2+anzObs];
        int[] filter = new int[anzObs];
        
        for (int i=0; i<anzObs+2; i++) {
            for (int j=0; j<anzObs+2; j++) {
                symPar[i][j] = -1; symVal[i][j] = 0;
                asyPar[i][j] = -1; asyVal[i][j] = 0;
            }
            meanPar[i] = -1; meanVal[i] = 0;
        }
        for (int i=0; i<anzObs; i++) filter[i] = i+2;
        
        symPar[0][0] = 0; symVal[0][0] = 1.0;
        symPar[1][1] = 1; symVal[1][1] = 0.1;
        symPar[0][1] = symPar[1][0] = 2; symVal[0][1] = symVal[1][0] = 0.01;
        for (int i=0; i<anzObs; i++) {symPar[i+2][i+2] = 3; symVal[i+2][i+2] = 0.1;}
        
        for (int i=0; i<anzObs; i++) {asyVal[i+2][0] = 1;}
        for (int i=0; i<anzObs; i++) {asyVal[i+2][1] = i++;}
        
        RAMModel model = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        
        double[] starting = model.getParameter();
        double[][] data = model.createData(100, starting);
        model.setData(data);
        double[] estimates = model.estimateML(starting);
        
        Statik.writeMatrix(data, "E:\\onyx\\simulation.project\\SimulatedData\\LGMsimulate.txt", '\t');
        
        System.out.println("Starting values are  "+Statik.matrixToString(starting));
        System.out.println("Estimated values are "+Statik.matrixToString(estimates));
        
        for (double p1 = 0.01; p1 <0.04; p1 += 0.001){
            for (double p2 = 0.5; p2 < 1.5; p2 += 0.05)
            {
                starting[0] = p2;
                starting[2] = p1;
                double mtll = model.computeLogLikelihood(starting);
                System.out.print(mtll+"\t");
            }
            System.out.println();
        }  
    }
   
    
   public static void sinyLGMsimulationtest() {
        
        int anzObs = 4;
        
        int[][] symPar = new int[2+anzObs][2+anzObs];
        double[][] symVal = new double[2+anzObs][2+anzObs];
        int[][] asyPar = new int[2+anzObs][2+anzObs];
        double[][] asyVal = new double[2+anzObs][2+anzObs];
        int[] meanPar = new int[2+anzObs];
        double[] meanVal = new double[2+anzObs];
        int[] filter = new int[anzObs];
        
        for (int i=0; i<anzObs+2; i++) {
            for (int j=0; j<anzObs+2; j++) {
                symPar[i][j] = -1; symVal[i][j] = 0;
                asyPar[i][j] = -1; asyVal[i][j] = 0;
            }
            meanPar[i] = -1; meanVal[i] = 0;
        }
        for (int i=0; i<anzObs; i++) filter[i] = i+2;
        
        symPar[0][0] = 0; symVal[0][0] = 1.0;
        symPar[1][1] = 1; symVal[1][1] = 0.1;
        symPar[0][1] = symPar[1][0] = 2; symVal[0][1] = symVal[1][0] = 0.01;
        for (int i=0; i<anzObs; i++) {symPar[i+2][i+2] = 3; symVal[i+2][i+2] = 0.1;}
        
        for (int i=0; i<anzObs; i++) {asyVal[i+2][0] = 1;}
        for (int i=0; i<anzObs; i++) {asyVal[i+2][1] = i++;}
        
        RAMModel model = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        
        double[] starting = model.getParameter();
        double[][] data = model.createData(100, starting);
        model.setData(data);
        double[] estimates = model.estimateML(starting);
        
        Statik.writeMatrix(data, "E:\\onyx\\simulation.project\\SimulatedData\\LGMsimulatetest.txt", '\t');
        
        System.out.println("Starting values are  "+Statik.matrixToString(starting));
        System.out.println("Estimated values are "+Statik.matrixToString(estimates));
        
        }  
    
    
    public static void sinyLGMsimulation1() {
        
        int anzObs = 100;
        
        int[][] symPar = new int[2+anzObs][2+anzObs];
        double[][] symVal = new double[2+anzObs][2+anzObs];
        int[][] asyPar = new int[2+anzObs][2+anzObs];
        double[][] asyVal = new double[2+anzObs][2+anzObs];
        int[] meanPar = new int[2+anzObs];
        double[] meanVal = new double[2+anzObs];
        int[] filter = new int[anzObs];
        
        for (int i=0; i<anzObs+2; i++) {
            for (int j=0; j<anzObs+2; j++) {
                symPar[i][j] = -1; symVal[i][j] = 0;
                asyPar[i][j] = -1; asyVal[i][j] = 0;
            }
            meanPar[i] = -1; meanVal[i] = 0;
        }
        for (int i=0; i<anzObs; i++) filter[i] = i+2;
        
        symPar[0][0] = 0; symVal[0][0] = 1.0;
        symPar[1][1] = 1; symVal[1][1] = 0.1;
        symPar[0][1] = symPar[1][0] = 2; symVal[0][1] = symVal[1][0] = 0.01;
        for (int i=0; i<anzObs; i++) {symPar[i+2][i+2] = 3; symVal[i+2][i+2] = 0.1;}
        
        for (int i=0; i<anzObs; i++) {asyVal[i+2][0] = 1;}
        for (int i=0; i<anzObs; i++) {asyVal[i+2][1] = (double)i / (double)(anzObs-1);}
        
        RAMModel model = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        
        double[] starting = model.getParameter();
        double[][] data = model.createData(100, starting);
        model.setData(data);
        double[] estimates = model.estimateML(starting);
        
        Statik.writeMatrix(data, "E:\\onyx\\simulation.project\\SimulatedData\\LGMsimulateddata.txt", '\t');
        
        System.out.println("Starting values are  "+Statik.matrixToString(starting));
        System.out.println("Estimated values are "+Statik.matrixToString(estimates));
        
        for (double p1 = 0.01; p1 <0.04; p1 += 0.001){
            for (double p2 = 0.5; p2 < 1.5; p2 += 0.05)
            {
                starting[0] = p2;
                starting[2] = p1;
                double mtll = model.computeLogLikelihood(starting);
                System.out.print(mtll+"\t");
            }
            System.out.println();
        }
        
    }

    public static void sinyScript() {
        
        int[][] symPar = new int[][]{{-1,-1},{-1,1}};
        double[][] symVal = new double[][]{{1,0},{0,2}};
        int[][] asyPar = new int[][]{{-1,-1},{0,-1}};
        double[][] asyVal = new double[][]{{0,0},{1,0}};
        int[] meanPar = new int[]{-1,-1};
        double[] meanVal = new double[]{0,0};
        int[] filter = new int[]{1};
        RAMModel model = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        
        double[] starting = model.getParameter();
        double[][] data = model.createData(100, starting);
        model.setData(data);
        double[] estimates = model.estimateML(starting);
        
        System.out.println("Starting values are  "+Statik.matrixToString(starting));
        System.out.println("Estimated values are "+Statik.matrixToString(estimates));
        
    }
    public static void florianLaengsVsQuer() {
        int anzTrial = 100;
        int anzPer = 100;
        int anzFac = 2;
        int T = 3;
        double meanSlopeFluid = 0.0;
        double[][] genStruct = new double[anzFac*T*2][];
        for (int t=0; t<T; t++) for (int i=0; i<anzFac; i++) {
            genStruct[2*(t*anzFac+i)+0] = new double[]{1,t,0,0};
            genStruct[2*(t*anzFac+i)+1] = new double[]{1,t,1,t};
        }
        // Intercept G, Slope G, Intercept F, Slope F
        double[][] genCovVal = new double[][]{{1,0,0,0},{0,1,0,0},{0,0,1,0},{0,0,0,1}};
        double[] genMeanVal = new double[]{0,0,0,meanSlopeFluid};
        double genErrVal = 0.1;
        Model genModel = new LinearModel(genStruct, genCovVal, genMeanVal, genErrVal);
        
        double[][] anaStruct = new double[2*anzFac+1][];
        for (int i=0; i<anzFac; i++) {anaStruct[2*i+0] = new double[]{1,0,0}; anaStruct[2*i+1] = new double[]{1,1,0};}
        anaStruct[2*anzFac] = new double[]{0,0,1};
        double[][] covVal = new double[][]{{1,0,0},{0,1,0},{0,0,1}};
        int[][] covPar = new int[][]{{0,-1,1},{-1,2,3},{1,3,4}};
        double[] meanVal = new double[]{0,0,0};
        int[] meanPar = new int[]{5,6,7};
        double[] errVal = new double[2*anzFac+1]; for (int i=0; i<errVal.length-1; i++) errVal[i] = 0.5;
        int[] errPar = new int[2*anzFac+1]; for (int i=0; i<errPar.length-1; i++) errPar[i] = i+8;
        errVal[errVal.length-1] = 0.0; errPar[errPar.length-1] = -1;
        LinearModel anaModel = new LinearModel(anaStruct, covPar, covVal, meanPar, meanVal, errPar, errVal);
        LinearModel resModel = anaModel.copy(); resModel.fixParameter(3,0.0);
        
        double[] starting = anaModel.getParameter();
        int succ = 0;
        System.out.println("cov_TF\tcov/varT\tLR\tsignificant");
        for (int trial=0; trial < anzTrial; trial++) {
            genModel.createData(anzPer);
            double[][] anaData = new double[anzPer*T][2*anzFac+1];
            for (int i=0; i<anzPer; i++) for (int t=0; t<T; t++) {
                for (int j=0; j<2*anzFac; j++) anaData[i*T+t][j] = genModel.data[i][2*anzFac*t+j];
                anaData[i*T+t][2*anzFac] = t;
            }
            anaModel.setData(anaData); resModel.setData(anaData);
            double[] est = anaModel.estimateML(starting);
            resModel.estimateML(Statik.subvector(est, 3));
            
            boolean significant = resModel.ll - anaModel.ll > Statik.FIVEPERCENTTHRESHOLD[0];
            System.out.println(est[3]+"\t"+est[3]/est[4]+"\t"+(resModel.ll-anaModel.ll)+"\t"+(significant?"1":"0"));
            if (significant) succ++;
        }
        System.out.println("Power = "+(double)succ / (double)anzTrial);
        
        
    }
    
    public static void testTwoByTwoTest() {
        long bin = Statik.binomial(64, 32);
        System.out.println(bin);
        
//        int a = 5, b = 5, c = 5, d = 5;
        int a,b,c,d; a = b = c = d = 16;
        double p = Statik.fisherExactTest(a,b,c,d);
        System.out.println(p);
    }
    
    public static void ppmlSimulationDifferentModels() {
        int anzTrials = 5;
        int anzPer = 1000;
        
        for (int anzObs = 10; anzObs <= 300; anzObs = (anzObs==10?50:anzObs+50)) {

            LinearModel linearModel = LinearModel.getLatentGrowthCurveModel(1,anzObs);
            LinearModel quadraticModel = LinearModel.getLatentGrowthCurveModel(2, anzObs);
            LinearModel cubicModel = LinearModel.getLatentGrowthCurveModel(3, anzObs);
            LinearModel lde = LinearModel.getLatentGrowthCurveModel(2, anzObs);
            double[] timeBasis = new double[anzObs]; for (int i=0; i<anzObs; i++) timeBasis[i] = -1.0 + 2* (double)i/(double)(anzObs-1); 
            LinearApproximationModel slcmLAM = new LinearApproximationModel(LinearApproximationModel.exponentialDecline, timeBasis); 
            LinearModel slcm = (slcmLAM.convertToLinearModel(new double[]{1,1,1}));
            
            int classes = anzObs/5;
            double[][] structMultiLevel = new double[anzObs][1+classes];
            for (int i=0; i<anzObs; i++) {structMultiLevel[i][0] = 1; structMultiLevel[i][1+(i/5)] = 1;}
            int pnr = 0;
            int[] mlMeanPar = new int[1+classes]; for (int i=0; i<1+classes; i++) mlMeanPar[i] = pnr++;
            int[][] mlFacPar = new int[1+classes][1+classes]; for (int i=0; i<1+classes; i++) mlFacPar[i][i] = pnr++;
            LinearModel multiLevelModel = new LinearModel(structMultiLevel,mlFacPar, new double[1+classes][1+classes], mlMeanPar, new double[1+classes], pnr++, 25);

            LinearModel model = null;
            for (int mc = 5; mc<6; mc++) {
                if (mc == 0) model = linearModel;
                else if (mc == 1) model = quadraticModel;
                else if (mc == 2) model = cubicModel;
                else if (mc == 3) model = lde;
                else if (mc == 4) model = slcm;
                else if (mc == 5) model = multiLevelModel;

                double[] par = new double[model.anzPar];
                for (int i=0; i<model.anzFac; i++) {par[i] = 0; par[i+model.anzFac] = 100;}
                for (int i=2*model.anzFac; i<par.length-1; i++) par[i] = 10;
                par[par.length-1] = 25;
                
                long usualTime = 0, ppmlTime = 0;
                for (int tr=0; tr<anzTrials; tr++) {
                    double[][] data = new double[anzPer][anzObs];
                    model.setParameter(par);
                    if (mc == 3) {
                        int k = 0;
                        for (int i=0; i<anzObs; i++) {
                            data[0][i] = Math.sin(2*k*Math.PI/100.0) + model.rand.nextGaussian()*Math.sqrt(model.errVal[0]);
                            k++;
                        }
                        for (int i=1; i<anzPer; i++) {
                            for (int j=0; j<anzObs-1; j++) data[i][j] = data[i-1][j+1];
                            data[i][anzObs-1] = Math.sin(2*k*Math.PI/100.0) + model.rand.nextGaussian()*Math.sqrt(model.errVal[0]);
                            k++;
                        }
                        model.setData(data);
                    } else model.createData(anzPer);
                    long t = System.nanoTime();
                    model.estimateML(par);
                    usualTime += System.nanoTime() - t;
                    double checkLL = model.ll;
                    t = System.nanoTime();
                    if (mc != 5) model.estimateMLFullCovarianceSupportedByPowerEquivalence();
                    else model.estimateMLSupportedByPowerEquivalence(par);
                    ppmlTime += System.nanoTime() - t;
                    if (Math.abs(model.ll - checkLL) > 0.1) System.out.println("Usual LL = "+checkLL+", PPML ll = "+model.ll);
                }
                System.out.println(anzObs+"\t"+mc+"\t"+(usualTime/1000000)+"\t"+(ppmlTime/1000000));
            }
        }
    }
    
    public static void testMissspecification() {
        int anzCond1 = 15, anzCond2 = 15;
        double[][] erg = new double[anzCond1][anzCond2];
    
        
        // Linear Model with Quadratic Miss-Specification
        for (int cond1=0; cond1<anzCond1; cond1++) for (int cond2=0; cond2<anzCond2; cond2++) {
            double varQuadratic = cond1*0.1;
            int t = 3+2*cond2;

            double[][] realStruct = new double[t][]; for (int i=0; i<t; i++) realStruct[i] = new double[]{1,i-(t-1)*0.5,(i-(t-1)*0.5)*(i-(t-1)*0.5)};
            int[][] realCovPar = new int[][]{{0,1,2},{1,3,4},{2,4,5}}; double[][] realCovVal = Statik.diagonalMatrix(new double[]{10,1,varQuadratic});
            double[] realErrVal = new double[t]; int[] realErrPar = new int[t]; for (int i=0; i<t; i++) {realErrVal[i] = 0.5; realErrPar[i] = 6;}
            int[] realMeanPar = new int[]{-1,-1,-1}; double[] realMeanVal = new double[]{0,0,0};
            LinearModel realModel = new LinearModel(realStruct, realCovPar, realCovVal, realMeanPar, realMeanVal, realErrPar, realErrVal);
            realModel.evaluateMuAndSigma();
            
            double[][] anaStruct = new double[t][]; for (int i=0; i<t; i++) anaStruct[i] = new double[]{1,i-(t-1)*0.5};
            int[][] anaCovPar = new int[][]{{0,1},{1,2}}; double[][] anaCovVal = Statik.diagonalMatrix(new double[]{10,1});
            double[] anaErrVal = new double[t]; int[] anaErrPar = new int[t]; for (int i=0; i<t; i++) {anaErrVal[i] = 0.5; anaErrPar[i] = 3;}
            int[] anaMeanPar = new int[]{-1,-1,-1}; double[] anaMeanVal = new double[]{0,0,0};
            LinearModel anaModel = new LinearModel(anaStruct, anaCovPar, anaCovVal, anaMeanPar, anaMeanVal, anaErrPar, anaErrVal);

            double[] ev = Model.computeMisspecification(realModel.sigma, realModel.mu, anaModel, new int[]{0,1,2}, false, true);
            erg[cond1][cond2] = Statik.inverseMixtureOfChisquares(0.95, ev, realModel.rand);
            System.out.println(Statik.matrixToString(ev));
        }
        System.out.println("Linear with Quadratic Missspecification: \r\n"+Statik.matrixToString(erg));
        
        
        /* Factor Model with additional Factor
        for (int cond1=0; cond1<anzCond1; cond1++) for (int cond2=0; cond2<anzCond2; cond2++) {
            double varAddFactor = cond1*0.1;
            int ind = 2+(cond2+2);
            
            double[][] realLoadVal = new double[3][ind]; int[][] realLoadPar =new int[3][ind];
            for (int i=0; i<ind; i++) {realLoadVal[0][i] = (i<ind/2?1:0); realLoadVal[1][i] = (i<ind/2?0:1); realLoadVal[2][i] = 1; 
                                       for (int j=0; j<3; j++) realLoadPar[j][i] = -1;}
            FactorModel realModel = new FactorModel(realLoadPar, realLoadVal, new int[][]{{-1,-1,-1},{-1,-1,-1},{-1,-1,-1}}, Statik.diagonalMatrix(new double[]{1,1,varAddFactor}), -1, .1);
            realModel.evaluateMuAndSigma();
            
            double[][] anaLoadVal = new double[2][ind]; int[][] anaLoadPar =new int[2][ind];
            for (int i=0; i<ind; i++) {anaLoadVal[0][i] = (i<ind/2?1:0); anaLoadVal[1][i] = (i<ind/2?0:1); anaLoadPar[0][i] = -1; anaLoadPar[1][i] = i; }
            FactorModel anaModel = new FactorModel(anaLoadPar, anaLoadVal, new int[][]{{ind+1,0},{0,ind+2}}, Statik.diagonalMatrix(new double[]{1,1}), ind+3, .1);
            
            double[] ev = computeMisspecification(realModel.sigma, realModel.mu, anaModel, new int[]{0,1}, false, false);
            erg[cond1][cond2] = Statik.inverseMixtureOfChisquares(0.95, new double[]{ev[0],ev[1]}, rand);
            System.out.println(Statik.matrixToString(ev));
        }
        System.out.println("Linear with Quadratic Missspecification: \r\n"+Statik.matrixToString(erg));
        */
    }

    public static void testCG() {
        double[][] mat = new double[][]{{4,1},{1,3}};
        double[] b = new double[]{1,2};
        double[] erg = Statik.solveByConjugateGradient(mat, b, 0.00001, 0, 0);
        System.out.println(Statik.matrixToString(erg));
    }
    
    public static void missingModel() {
        int[][] asyPar = new int[][]{{-1,-1,},{-1,-1}};
        int[][] symPar = new int[][]{{0,1},{1,2}};
        int[] meanPar = new int[]{-1,-1};
        double[] meanVal = new double[]{0,0};
        RAMModel rammodel = new RAMModel(symPar, Statik.identityMatrix(2),asyPar, new double[2][2], meanPar, meanVal, 2);
        OnyxModel model = new OnyxModel(rammodel);
        
        double[][] data = new double[][]{{2,-999},{-2,-999},{-999,Math.sqrt(8)},{-999,-Math.sqrt(8)},{2,1},{-2,-1}};
        
        model.runFor(data, 100);
        
        Model mgModel = model.modelRun.getWorkModel();
        mgModel.setParameter(new double[]{2,1,4});
        mgModel.computeLogLikelihoodDerivatives(mgModel.getParameter(), true);
        
        System.out.println(model.getBestEstimateRunner().getDescription());
        
    }
    
    public static void semClassHW4() {
        LinearModel model = new LinearModel(new double[][]{{1,0},{1,1}}, new int[][]{{0,2},{2,1}}, new double[][]{{1,0},{0,1}}, new int[]{-1,-1}, new double[]{0,0}, -1, 1.0);
        model.setDataDistribution(new double[][]{{3,1},{1,4}}, new double[]{0,0}, 1000);
        model.anzPer = 1000;
        model.setParameter(new double[]{2,1,0});
        double ll = model.getMinusTwoLogLikelihood();
        double ls = model.getLeastSquares();
        System.out.println("LL = "+ll+", LS = "+ls);
        model.setParameter(new double[]{1.572,2.287,0});
        ll = model.getMinusTwoLogLikelihood();
        ls = model.getLeastSquares();
        System.out.println("LL = "+ll+", LS = "+ls);
        model.setParameter(new double[]{2,3,-1});
        double ll2 = model.getMinusTwoLogLikelihood();
        System.out.println("LL = "+ll2+", LR = "+(ll-ll2));
    }
    
    public static void steveMiddleRace() {
        int anzT = 9;
        int anzPer = 1000;
        Random rand = new Random(235723L);
        double offProbability = 0.5;
        int time = 300;
        
        int[][] facPar = new int[][]{{0,3,4},{3,1,5},{4,5,2}}; 
        double[][] facVal = new double[][]{{2,0.8,0.6},{0.8,1.5,0.4},{0.6,0.4,1.0}};
        int[] meanPar = new int[]{6,7,8};
        double[] meanVal = new double[]{0.08,-0.2,-0.4};
        LinearModel[] subModel = new LinearModel[anzT];
        for (int i=1; i<=anzT; i++) {
            int[][] structPar = new int[i][]; double[][] structVal = new double[i][]; 
            for (int j=0; j<i; j++) {structPar[j] = new int[]{-1,-1,-1}; structVal[j] = new double[]{1,(j-anzT/2),(j-anzT/2)*(j-anzT/2)};}
            subModel[i-1] = new LinearModel(structVal, facPar, facVal, meanPar, meanVal, 9, 0.2);
        }
        MultiGroupModel model = new MultiGroupModel(subModel,subModel[0].anzVar);

        double[] starting = subModel[anzT-1].getParameter();
        subModel[anzT-1].createData(anzPer);
        double[][] fullData = Statik.copy(subModel[anzT-1].data);
        double[][][] dataCov = new double[anzT][][]; for (int i=1; i<=anzT; i++) dataCov[i-1]= new double[i][i];
        double[][] dataMean = new double[anzT][]; for (int i=1; i<=anzT; i++) dataMean[i-1]= new double[i];
        int[] anzSubPer = new int[anzT];
        
        int[] startTime = new int[anzPer]; for (int i=0; i<anzPer; i++) startTime[i] = rand.nextInt(100);
        int[] stepTime = new int[anzPer]; for (int i=0; i<anzPer; i++) stepTime[i] = rand.nextInt(10)+1;
        
        for (int i=1; i<=anzT; i++) subModel[i-1].setDataDistribution(dataCov[i-1], dataMean[i-1], anzSubPer[i-1]);
        model.initEstimation(model.getParameter(), true);
        
        for (int t=0; t<time; t++) {
            for (int i=0; i<anzT; i++) {Statik.setToZero(dataCov[i]); Statik.setToZero(dataMean[i]); Statik.setTo(anzSubPer, 0);}
            int anzOut = 0;
            for (int i=0; i<anzPer; i++) {
                int obs = 1 + (t-startTime[i])/stepTime[i];
                if (t < startTime[i] || rand.nextDouble() < offProbability) obs = 0;
                if (obs > 0) {
                    if (obs > anzT) obs = anzT;
                    for (int j=0; j<obs; j++) {
                        dataMean[obs-1][j] += fullData[i][j];
                        for (int k=0; k<obs; k++) dataCov[obs-1][j][k] += fullData[i][j]*fullData[i][k];
                    }
                    anzSubPer[obs-1]++;
                } else anzOut++;
            }
            for (int i=1; i<=anzT; i++) {
                for (int j=0; j<i; j++) dataMean[i-1][j] /= (double)anzSubPer[i-1];
                for (int j=0; j<i; j++) for (int k=0; k<i; k++) dataCov[i-1][j][k] = (dataCov[i-1][j][k] / (double)anzSubPer[i-1]) - dataMean[i-1][j]*dataMean[i-1][k];
                subModel[i-1].setDataDistribution(dataCov[i-1], dataMean[i-1], anzSubPer[i-1]);
            }
            if (Double.isNaN(model.getMinusTwoLogLikelihood())) 
                model.setParameter(starting);
            model.computeLogLikelihoodDerivatives(model.position);
            model.moveWithOptimalDamping(0.0001, true);
            System.out.println(t+"\t"+anzOut+"\t"+Statik.matrixToString(anzSubPer)+model.ll+"\t"+Statik.matrixToString(model.getParameter())+model.lastGain+"\t"+model.lastSteplength);
        }
    }
    
    public static void semClassHW8() {
        double[][] data = Statik.loadMatrix("Homework8Data.csv", '\t');
        double[][] cov = Statik.covarianceMatrix(data);
        double[][] evec = Statik.identityMatrix(6);
        double[] eval = new double[6];
        Statik.eigenvalues(cov, 0.0001, eval, evec);
        System.out.println(Statik.matrixToString(evec));
        System.out.println("\r\n"+Statik.matrixToString(eval));
    }
    
    private static Vector<String[]> comparisonSportModelRead(Hashtable<String,Integer> teams, String filename) {
        try {
            BufferedReader read = new BufferedReader(new FileReader(filename));
            if (teams == null) teams = new Hashtable<String, Integer>();
            Vector<String[]> erg = new Vector<String[]>();
            while (read.ready()) {
                String line = read.readLine();
                line = line.substring(line.lastIndexOf('\t')+1);
                int ixV = line.indexOf("v.");
                int ixO = line.indexOf("(", ixV);
                int ixS = line.indexOf("-", ixO);
                int ixE = line.indexOf(")", ixS);
                String fi = line.substring(0,ixV).trim();
                String se = line.substring(ixV+2, ixO).trim();
                String go1 = line.substring(ixO+1,ixS).trim();
                String go2 = line.substring(ixS+1,ixE).trim();
                if (!teams.containsKey(fi)) teams.put(fi,teams.size());
                if (!teams.containsKey(se)) teams.put(se,teams.size());
                erg.add(new String[]{fi,se,go1,go2});
            }
            return erg;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static void donnaDataTransformation() {
        double[][] inData = Statik.loadMatrix("donnaDataIn.csv", '\t', false, 12);
        double[][] outData = new double[695][37];
        int[] occurenceCount = new int[695];
        Statik.setTo(outData, Model.MISSING);
        for (int i=0; i<inData.length; i++) {
            int p = (int)Math.round(inData[i][0]-1);
            boolean isIgnoreLine = inData[i][2] == Model.MISSING || 
                                   (occurenceCount[p]==2 && inData[i][2]==3 && inData[i][11]==Model.MISSING) ||
                                   (occurenceCount[p]==1 && inData[i][2]==2 && inData[i][10]==Model.MISSING && inData[i][11]==Model.MISSING);
            if (!isIgnoreLine) {
                int gr = (int)Math.round(inData[i][2]-1);
                outData[p][0] = inData[i][1]; 
                outData[p][1+5*gr] = inData[i][4];
                for (int j=0; j<4; j++) outData[p][1+5*gr+1+j] = inData[i][5+j];
            }
            if (Model.isMissing(outData[p][36])) outData[p][36] = inData[i][9];
            occurenceCount[p]++;
        }
        Statik.writeMatrix(outData, "donnaDataOut.csv", '\t');
    }
    
    public static void ulmanRegressionModel() {
        double[][] symVal = Statik.identityMatrix(4);
        int[][] symPar = new int[][]{{-1,2,-1,-1},{2,-1,-1,-1},{-1,-1,0,3},{-1,-1,3,1}};
        double[][] asyVal = new double[][]{{0,0,0,0},{0,0,0,0},{1,1,0,0},{1,1,0,0}};
        int[][] asyPar = new int[][]{{-1,-1,-1,-1},{-1,-1,-1,-1},{4,6,-1,-1,},{7,5,-1,-1}};
        int[][] asyParRes = new int[][]{{-1,-1,-1,-1},{-1,-1,-1,-1},{4,6,-1,-1,},{6,5,-1,-1}};
        double[] meanVal = new double[]{0,0,0,0};
        int[] meanPar = new int[]{-1,-1,-1,-1};
        RAMModel model = new RAMModel(symPar,symVal, asyPar, asyVal, meanPar, meanVal, new int[]{0,1,2,3});
        RAMModel modelRes = new RAMModel(symPar, symVal, asyParRes, asyVal, meanPar, meanVal, new int[]{0,1,2,3});
        
        
        double alpha = 0.707;
        double betaSteps = 0.05;
        int trials = 10000;
        double[] creationParameter = new double[]{1,1,0,0,alpha,alpha,0.0,0.0};
        double[] startingValuesRes = new double[]{1,1,0,0,alpha,alpha,0.0};
        int anzCond = 8;
        double[][] erg = new double[8][(int)Math.ceil(1.0/betaSteps)];
        for (int cond = 4; cond<8; cond++) {
            System.out.print("Starting condition "+cond+", ");
            int c = cond;
            double baseBeta = ((c%4)==0?0.0:((c%4)==1?0.15:((c%4)==2?0.3:0.45))); c /= 4;
            int N = (c==0?140:180);
            System.out.println("Startbeta = "+baseBeta+", N = "+N);
            
            for (double effectBeta = baseBeta; effectBeta < 1.0; effectBeta += betaSteps) {
                System.out.println("Beta (effect) = "+effectBeta);
                creationParameter[6] = baseBeta; creationParameter[7] = effectBeta;
                startingValuesRes[6] = ( baseBeta + effectBeta ) / 2.0;
                for (int trial = 0; trial < trials; trial++) {
                    model.setParameter(creationParameter);
                    model.createData(N);
                    double[][] cor = Statik.correlationMatrix(model.data);
                    model.setDataDistribution(cor, new double[model.anzVar], N);
                    model.estimateML(creationParameter);
                    modelRes.setDataDistribution(cor, new double[model.anzVar], N);
                    modelRes.estimateML(startingValuesRes);
                    double lr = modelRes.ll - model.ll;
                    erg[cond][(int)Math.round(effectBeta/betaSteps)] += (lr > Statik.FIVEPERCENTTHRESHOLD[0]?1:0); 
                }
                erg[cond][(int)Math.round(effectBeta/betaSteps)] /= trials;
                Statik.writeMatrix(erg, "ulmanPower.txt", '\t');
            }
        }
        System.out.println(Statik.matrixToString(erg));
    }
    
    public static void clusteringSimulation() {
        int anzVar = 9;
        double variance = 1.0;
        int anzPer = 103;
        Random rand = new Random();
        int anzTrial = 100;
        int maxCluster = 5;
        
        double stdv = Math.sqrt(variance);
        double[][] data = new double[anzPer][anzVar];
        double[][] erg = new double[maxCluster][2];

        for (int trial = 0; trial < anzTrial; trial++) {
            System.out.print(".");
//            for (int i=0; i<anzPer; i++) for (int j=0; j<anzVar; j++) data[i][j] = rand.nextGaussian()*stdv;
            for (int i=0; i<anzPer; i++) for (int j=0; j<anzVar; j++) data[i][j] = rand.nextInt(8) + rand.nextGaussian()*0.01;
            
            LloydsAlgorithm cluster = new LloydsAlgorithm(data);
            cluster.setMethod(methodType.NORMAL);
            for (int i=1; i<=maxCluster; i++) {
                double minEntropy = Double.MAX_VALUE;
                for (int j=0; j<100; j++) {
                    try {cluster.cluster(i, null);
                        double entropy = cluster.getEntropy();
                        if (entropy < minEntropy) {minEntropy = entropy;}
                    } catch (Exception e) {}
                }
                erg[i-1][0] += minEntropy; erg[i-1][1] += minEntropy*minEntropy;
            }
        }
        System.out.println();
        for (int i=0; i<maxCluster; i++) {
            erg[i][0] /= anzTrial;
            erg[i][1] = Math.sqrt(erg[i][1]/anzTrial - erg[i][0]*erg[i][0]);
        }
        System.out.println(Statik.matrixToString(erg));
    }

    public static void caileyScript() {
        for (int trial = 1; trial < 1000; trial++) {
            
            OnyxModel model = null; // EVERYTHING FROM THE ONYX SCRIPT;
            
            int missingProbability = 20;
            double[][] caileyData = model.createData(100);
            for (int part=0; part<100; part++) {
                for (int var=0; var<2; var++) {
                    if (model.rand.nextInt(100) < missingProbability) {
                        caileyData[part][var] = Model.MISSING;
                    }
                }
            }
            
            OnyxModel.Until until = OnyxModel.Until.CONVERGED;
            boolean success = model.runUntil(caileyData, until);
            
            ModelRunUnit res = model.getBestEstimateRunner();
            String resultString = trial+"\t"+res.chisqr+"\t"+res.kulbackLeibler;
            System.out.println(resultString);
        }
    }
    
    public static void florianDebugTest() {
        Model model = florianDebugTwoGroupSimpleFactorModel();
        model.setRandomSeed(23423212749L);
        model.setParameter(new double[]{1,0,1,0,1,1,0.1,0.1,0.1,0.1,0.1,0.1});
        double[][] data = model.createData(100);
        for (int i=0; i<100; i++) {data[i][1] += (2*(i%2)-1)*5; data[i][2] += (2*(i%2)-1)*5;}
        model.setData(data);
        double[] est = model.estimateML(0.000001);

        int[] hypoPara = new int[]{1,3};
        Model res = model.copy();
        res.fixParameter(hypoPara);
        res.setData(data);
        
        double[] resEst = res.estimateML(0.0001);
        model.setParameter(est);
        double[] chiMultiplier = model.computeMisspecification(hypoPara, false, est, false);
        System.out.println("Estimate = "+Statik.matrixToString(est));
        System.out.println("Multiplier = "+Statik.matrixToString(chiMultiplier));
        System.out.println("Hessian = "+Statik.matrixToMapleString(model.llDD, 5));
    }
    
    public static void testKernelFinder() {
//        double[][] matrix = new double[][]{{1,1,1,0},{1,1,1,0},{0,0,1,0},{0,0,0,0}};
        double[][] matrix = new double[][]{{3,4,1,2},{1,8,7,2},{-1,2,3,0},{4,2,-2,2}};
        double[][] erg = new double[4][4];
        double[][] kernel = new double[4][4];
        boolean isPD = Statik.pseudoInvertSquare(matrix, erg, new double[4][4], new double[4][4], new double[4][4], kernel, 0.001);
        System.out.println(Statik.matrixToString(erg));
        System.out.println("Is pd = "+isPD);
        System.out.println("Kernel = \r\n"+Statik.matrixToString(kernel));
    }
    
    public static void testFisherComputation() {
//        RAMModel model = new RAMModel(new int[][]{{0,-1,-1},{-1,1,-1},{-1,-1,1}}, new double[3][3],
//                                      new int[][]{{-1,-1,-1},{2,-1,-1},{2,-1,-1}}, new double[3][3],
//                                      new int[]{-1,-1,-1}, new double[3], new int[]{1,2});
        
        RAMModel model = florianFactorModelRAMStandardizedWeights();

        double[] starting = new double[model.anzPar]; for (int i=0; i<starting.length; i++) starting[i] = 0.5234;
        model.evaluateMuAndSigma(starting);
        double[] dataMean = Statik.copy(model.mu);
        double[][] dataCov = Statik.copy(model.sigma);
        Random rand = new Random(29834923);
        double miss = 0.05;
        for (int i=0; i<dataCov.length; i++) for (int j=0; j<dataCov.length; j++) dataCov[i][j] += rand.nextGaussian()*miss;

        model.setDataDistribution(dataCov, dataMean, 1);
        double[] par = model.estimateML(starting);
        model.computeLogLikelihoodDerivatives(par);
        double[][] directHessian = model.llDD;
        double[][] expectedHessian = model.computeExpectedHessian(dataCov, dataMean);
        double[][] fisher = model.computeFisherMatrix(dataCov, dataMean);
        
        System.out.println("Estimate = "+Statik.matrixToString(par));
        System.out.println("Direct Hessian = \r\n"+Statik.matrixToString(Statik.multiply(100,directHessian)));
        System.out.println("Expected Hessian = \r\n"+Statik.matrixToString(Statik.multiply(100,expectedHessian)));
        System.out.println("Fisher = \r\n"+Statik.matrixToString(Statik.multiply(100,fisher)));
    }
    
    public static void testKLofMixture() {
        double[] trueWeights = new double[]{0.3,0.7};
        double[][] trueMeans = new double[][]{{0,0},{0,0}};
        double[][][] trueCovs = new double[][][]{      {{1,0},{0,1}}, {{2,0},{0,2}}    };
        double[] wrongWeights = new double[]{0.3,0.7};
        double[][] wrongMeans = new double[][]{{0,0},{0,0}};
        double[][][] wrongCovs = new double[][][]{      {{1,0},{0,1}}, {{2,0.1},{0.1,2}}    };
        double kl = Statik.kullbackLeiblerMixtureOfGaussians(trueWeights, trueMeans, trueCovs, wrongWeights, wrongMeans, wrongCovs, 1000, new Random());
        System.out.println(kl);

        trueWeights = new double[]{0.333,0.333,0.334};
        trueMeans = new double[][]{{0.0786,-0.058},{3.045,2.996},{-2.945,-3.011}};
        trueCovs = new double[][][]{      {{0.923,-0.077},{-0.077,0.907}}, {{1.136,0.031},{0.031,1.256}}, {{1.201,0.078},{0.078,0.787}} };
        wrongWeights = new double[]{0.367,0.3,0.333};
        wrongMeans = new double[][]{{0.389,0.192},{2.995,3.030},{-2.945,-3.011}};
        wrongCovs = new double[][][]{      {{1.877,0.748},{0.748,1.583}}, {{1.159,0.009},{0.009,1.225}}, {{1.201,0.078},{0.078,0.787}}    };
        kl = Statik.kullbackLeiblerMixtureOfGaussians(trueWeights, trueMeans, trueCovs, wrongWeights, wrongMeans, wrongCovs, 10000, new Random());
        System.out.println(kl);
    
        trueWeights = new double[]{0.333,0.333,0.334};
        trueMeans = new double[][]{{0.0786,-0.058},{3.045,2.996},{-2.945,-3.011}};
        trueCovs = new double[][][]{      {{0.923,-0.077},{-0.077,0.907}}, {{1.136,0.031},{0.031,1.256}}, {{1.201,0.078},{0.078,0.787}} };
        wrongWeights = new double[]{0.433,0.233,0.334};
        wrongMeans = new double[][]{{0.777,0.683},{3.019,2.929},{-2.945,-3.011}};
        wrongCovs = new double[][][]{      {{2.572,1.714},{1.714,2.786}}, {{1.209,-0.025},{-0.025,1.329}}, {{1.201,0.078},{0.078,0.787}}    };
        kl = Statik.kullbackLeiblerMixtureOfGaussians(trueWeights, trueMeans, trueCovs, wrongWeights, wrongMeans, wrongCovs, 10000, new Random());
        System.out.println(kl);

        trueWeights = new double[]{0.5,0.5};
        trueMeans = new double[][]{{-0.3607,-0.1881},{-0.323,0.418}};
        trueCovs = new double[][][]{      {{0.260,0.001},{0.001,0.183}}, {{0.402,0.000},{0.000,0.144}}};
        wrongWeights = new double[]{0.253,0.747};
        wrongMeans = new double[][]{{-0.413,-0.342},{-0.318,0.270}};
        wrongCovs = new double[][][]{      {{0.219,-0.050},{-0.050,0.169}}, {{0.366,0.015},{0.015,0.190}}    };
        kl = Statik.kullbackLeiblerMixtureOfGaussians(trueWeights, trueMeans, trueCovs, wrongWeights, wrongMeans, wrongCovs, 100, new Random());
        System.out.println(kl);

    }
    
    public static double[][] floGetCrosstimeCorrelation(int participant, int lag) {
        int partNr = participant;
        String withinDataName = "person";
        double[][] wthnDataWithID = Statik.loadMatrix("FloData"+File.separator+withinDataName+"_"+partNr+".dat", ' ', true, ".");
        int anzVar = 2*(wthnDataWithID[0].length-1); int anzVarGroup = anzVar/2;
        int[] allButOne = new int[anzVarGroup]; for (int i=0; i<anzVarGroup; i++) allButOne[i] = i+1;
        double[][] wthnData = Statik.submatrix(wthnDataWithID, null, allButOne);
        
        double[][] wthnCov = new double[anzVarGroup][anzVarGroup]; double[] wthnMean = new double[anzVarGroup];
        Statik.covarianceMatrixAndMeans(wthnData, wthnMean, wthnCov, Model.MISSING);
        double[][] wthnCor = Statik.correlationFromCovariance(wthnCov);
        double det = Statik.determinantOfPositiveDefiniteMatrix(wthnCor);
        
        int anzPer = wthnData.length;
        double[][] doubleData = new double[anzPer-lag][anzVar];
        for (int i=0; i<anzPer-lag; i++) 
            for (int j=0; j<anzVarGroup; j++) {doubleData[i][j] = wthnData[i][j]; doubleData[i][anzVarGroup+j] = wthnData[i+lag][j];}
        double[][] dobCov = new double[anzVar][anzVar]; double[] dobMean = new double[anzVar];
        Statik.covarianceMatrixAndMeans(doubleData, dobMean, dobCov, Model.MISSING);
        double[][] dobCor = Statik.correlationFromCovariance(dobCov);
        double[][] erg = Statik.submatrix(dobCor, Statik.enumeratIntegersFrom(0, anzVarGroup-1), Statik.enumeratIntegersFrom(anzVarGroup, anzVar));
        System.out.println("Lag "+lag+" 2-norm = \t"+Statik.norm(erg));
        
        return erg;
    }

    public static double[][] floGetSimulationCovariance(int participant, int[] hypothesis, int steps) {
        final double PRECISION = 0.00001;
        int partNr = participant;
        String withinDataName = "person";
        double[][] wthnDataWithID = Statik.loadMatrix("FloData"+File.separator+withinDataName+"_"+partNr+".dat", ' ', true, ".");
        int anzVar = wthnDataWithID[0].length-1;
        int[] allButOne = new int[anzVar]; for (int i=0; i<anzVar; i++) allButOne[i] = i+1;
        double[][] wthnData = Statik.submatrix(wthnDataWithID, null, allButOne);
        
        double[][] wthnCov = new double[anzVar][anzVar]; double[] wthnMean = new double[anzVar];
        Statik.covarianceMatrixAndMeans(wthnData, wthnMean, wthnCov, Model.MISSING);
        double[][] wthnCor = Statik.correlationFromCovariance(wthnCov);
        double det = Statik.determinantOfPositiveDefiniteMatrix(wthnCor);
        int anzPer = wthnData.length;
        
        RAMModel model = florianFactorModelRAMStandardizedWeights();
        double[] starting = new double[model.anzPar]; for (int i=0; i<starting.length; i++) starting[i] = 0.5;
        for (int i=0; i<model.anzPar; i++) if (model.isErrorParameter(i)) starting[i] = 1.0;
//        model.logStream = System.out;

        RAMModel restrictedModel = new RAMModel(model);
        restrictedModel.fixParameter(hypothesis, new double[hypothesis.length]);
        restrictedModel.setDataDistribution(wthnCor,  new double[anzVar], anzPer);
        double[] startingRestricted = Statik.subvector(starting, hypothesis, false);
        restrictedModel.setMaximalNumberOfIterations(200);
        
        System.out.println("Starting estimation");
        double[] valsRestricted = restrictedModel.estimateML(startingRestricted,PRECISION);
        double[][] simCov = restrictedModel.getCovarianceMatrixWithSameModelFitClosestTo(wthnCor);
        double[][] simCor = Statik.correlationFromCovariance(simCov);
        
        System.out.println("Preparing all time points covariance matrix.");
        double[][] bigCor = new double[anzVar*steps][anzVar*steps];
        for (int i=0; i<steps; i++) for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) bigCor[i*anzVar+j][i*anzVar+k] = simCor[j][k];
        for (int lag=1; lag < steps; lag++) {
            double[][] doubleData = new double[anzPer-lag][2*anzVar];
            for (int i=0; i<anzPer-lag; i++) 
                for (int j=0; j<anzVar; j++) {doubleData[i][j] = wthnData[i][j]; doubleData[i][anzVar+j] = wthnData[i+lag][j];}
            double[][] dobCov = new double[2*anzVar][2*anzVar]; double[] dobMean = new double[2*anzVar];
            Statik.covarianceMatrixAndMeans(doubleData, dobMean, dobCov, Model.MISSING);
            double[][] dobCor = Statik.correlationFromCovariance(dobCov);
            for (int i=0; i<steps-lag; i++) for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) bigCor[i*anzVar+j][(i+lag)*anzVar+k] = bigCor[(i+lag)*anzVar+k][i*anzVar+j] = dobCor[j][anzVar+k];
        }
        
//        double[][] cholDecomp = Statik.choleskyDecompose(bigCor, 0.001);
//        double[][] check = Statik.multiply(cholDecomp, Statik.transpose(cholDecomp));
        return bigCor;
    }
    
    public static double[][] floSimulateDataset(double[][] cor, int anzVar, int anzPer, Random rand) {
        
        double debugDet = Statik.determinant(cor);
        double[][] pairCov = Statik.submatrix(cor, Statik.enumeratIntegersFrom(0,anzVar-1), Statik.enumeratIntegersFrom(anzVar,2*anzVar-1));
        double[][] diagCov = Statik.submatrix(cor, Statik.enumeratIntegersFrom(0,anzVar-1), Statik.enumeratIntegersFrom(0,anzVar-1));
        double[][] inv = Statik.invert(diagCov);
        double[][] A = Statik.multiply(inv,pairCov);
        Statik.transpose(A,A); 
        double[][] diagChol = Statik.choleskyDecompose(diagCov);
        double[][] B = Statik.subtract(diagCov, Statik.multiply(A, Statik.multiply(diagCov,Statik.transpose(A))));
        double[][] BChol = Statik.choleskyDecompose(B);
        
        double[][] erg = new double[anzPer][anzVar];
        double[] vec = new double[anzVar];
        double[] vec2 = new double[anzVar]; for (int i=0; i<anzVar; i++) vec2[i] = rand.nextGaussian();
        erg[0] = Statik.multiply(diagChol, vec2);
        for (int i=1; i<anzPer; i++) {
            Statik.multiply(A, erg[i-1], erg[i]);
            for (int j=0; j<anzVar; j++) vec2[j] = rand.nextGaussian();
            Statik.multiply(BChol, vec2, vec);
            Statik.add(erg[i], vec, erg[i]);
        }
        double[][] debugSimCov= Statik.covarianceMatrix(erg);
        
        return erg;
    }
    
    public static void floReduceCrosscorrelation(double[][] cor) {
        int anzVar = cor.length/2;
        double det = Statik.determinant(cor);
        while (det < 0) {
            for (int i=0; i<anzVar; i++) for (int j=0; j<anzVar; j++) cor[i][anzVar+j] = cor[anzVar+j][i] = cor[i][anzVar+j]*0.9;
            det = Statik.determinant(cor);
        }
    }
    
    public static double[] floMultipleRuns(double[][] suggestedStartings, Model model, int trials, double EPSILON) {
        
        double[] lsStarting = null;
        try {
            lsStarting = model.estimateLS(suggestedStartings[0], EPSILON);
            if (model.warningFlag == warningFlagTypes.FAILED) lsStarting = null;
        } catch (Exception e) {}
        double[] bestPar = null;
        double bestLL = Double.MAX_VALUE;
        for (int trial=0; trial<trials; trial++) {
            double[] starting = null;
            if (trial < suggestedStartings.length) starting = suggestedStartings[trial];
            if (trial == suggestedStartings.length && lsStarting != null) starting = lsStarting;
            while (starting == null) {
//                starting = model.getRandomStartingValues();
                starting = new double[model.anzPar]; for (int i=0; i<starting.length; i++) starting[i] = 2*model.rand.nextDouble()-1.0;
                for (int i=0; i<model.anzPar; i++) if (model.isErrorParameter(i)) starting[i] = 2.0;
                
                double ll = model.getMinusTwoLogLikelihood(starting, true);
                while (Double.isNaN(ll)) {
                    for (int i=0; i<starting.length; i++) if (model.isErrorParameter(i)) starting[i] *= 1.1;
                    ll = model.getMinusTwoLogLikelihood(starting, true);
                }
            }
//            model.logStream = System.out;
            model.estimateML(starting, EPSILON);
            System.out.println(model.ll + (model.warningFlag == warningFlagTypes.FAILED?" (Max steps)":""));
            if (model.ll < bestLL) {
                bestLL = model.ll;
                bestPar = model.getParameter();
            }
        }
        model.setParameter(bestPar); model.ll = bestLL;
        return bestPar;
    }
    
    public static double floFindSimulatedPValue(int participant, int[] hypothesis, int simulationTrials) {
        final double PRECISION = 0.0001;
        final int REPEATEDSTARTS = 10;
        
        int partNr = participant;
        String withinDataName = "person";
        double[][] wthnDataWithID = Statik.loadMatrix("FloData"+File.separator+withinDataName+"_"+partNr+".dat", ' ', true, ".");
        int anzVar = wthnDataWithID[0].length-1;
        int[] allButOne = new int[anzVar]; for (int i=0; i<anzVar; i++) allButOne[i] = i+1;
        double[][] wthnData = Statik.submatrix(wthnDataWithID, null, allButOne);
        
        double[][] wthnCov = new double[anzVar][anzVar]; double[] wthnMean = new double[anzVar];
        Statik.covarianceMatrixAndMeans(wthnData, wthnMean, wthnCov, Model.MISSING);
        double[][] wthnCor = Statik.correlationFromCovariance(wthnCov);
        double det = Statik.determinantOfPositiveDefiniteMatrix(wthnCor);
        int anzPer = wthnData.length;

        RAMModel model = florianFactorModelRAMStandardizedWeights();
        model.setDataDistribution(wthnCor, new double[anzVar], anzPer);
        RAMModel restrictedModel = new RAMModel(model);
        restrictedModel.fixParameter(hypothesis, new double[hypothesis.length]);
        restrictedModel.setDataDistribution(wthnCor,  new double[anzVar], anzPer);

        double[] starting = new double[model.anzPar]; for (int i=0; i<starting.length; i++) starting[i] = 0.5;
        for (int i=0; i<model.anzPar; i++) if (model.isErrorParameter(i)) starting[i] = 1.0;
        double[] startingRestricted = Statik.subvector(starting, hypothesis, false);

        restrictedModel.setMaximalNumberOfIterations(200);
        System.out.println("Starting restricted estimation on originial data set.");
        double[] valsRestricted = floMultipleRuns(new double[][]{startingRestricted}, restrictedModel, REPEATEDSTARTS, PRECISION);
        double llRestricted = restrictedModel.ll;
        model.setParameter(restrictedModel.getParameterNames(), restrictedModel.getParameter());
        double[] valsResBig = model.getParameter();
        for (int i=0; i<hypothesis.length; i++) valsResBig[hypothesis[i]]=0;
        
        model.setMaximalNumberOfIterations(200);
        System.out.println("Starting estimation on original data set.");
        double[] valsFull =  floMultipleRuns(new double[][]{starting, valsResBig}, model, REPEATEDSTARTS, PRECISION);
        double llFull = model.ll;

        double lr = llRestricted - llFull;
        double[][] diagSimCov = restrictedModel.getCovarianceMatrixWithSameModelFitClosestTo(wthnCor);
        double[][] diagSimCor = Statik.correlationFromCovariance(diagSimCov);
        
        
        System.out.println("Preparing two time points covariance matrix.");
        int steps = 2;
        double[][] simCor = new double[anzVar*steps][anzVar*steps];
        for (int i=0; i<steps; i++) for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) simCor[i*anzVar+j][i*anzVar+k] = diagSimCor[j][k];
        for (int lag=1; lag < steps; lag++) {
            double[][] doubleData = new double[anzPer-lag][2*anzVar];
            for (int i=0; i<anzPer-lag; i++) 
                for (int j=0; j<anzVar; j++) {doubleData[i][j] = wthnData[i][j]; doubleData[i][anzVar+j] = wthnData[i+lag][j];}
            double[][] dobCov = new double[2*anzVar][2*anzVar]; double[] dobMean = new double[2*anzVar];
            Statik.covarianceMatrixAndMeans(doubleData, dobMean, dobCov, Model.MISSING);
            double[][] dobCor = Statik.correlationFromCovariance(dobCov);
            for (int i=0; i<steps-lag; i++) for (int j=0; j<anzVar; j++) for (int k=0; k<anzVar; k++) simCor[i*anzVar+j][(i+lag)*anzVar+k] = simCor[(i+lag)*anzVar+k][i*anzVar+j] = dobCor[j][anzVar+k];
        }
        
        floReduceCrosscorrelation(simCor);
        
        Random rand = new Random();
        int countAbove = 0, countBelow = 0;
        double[] vals = new double[simulationTrials];
        for (int i=0; i<simulationTrials; i++) {
            double[][] simData = floSimulateDataset(simCor, 18, anzPer, rand);

            restrictedModel.setDataDistribution(Statik.correlationMatrix(simData), new double[anzVar], anzPer);
            System.out.println("Starting restricted estimation on simulated data set "+i+".");
            double simLLRes = Double.NaN;
            double[] simValRes = null;
            try {
                restrictedModel.warningFlag = warningFlagTypes.OK;
                simValRes = floMultipleRuns(new double[][]{valsRestricted,  startingRestricted}, restrictedModel, REPEATEDSTARTS,PRECISION);
                if (restrictedModel.warningFlag != warningFlagTypes.FAILED) simLLRes = restrictedModel.ll;
            } catch (Exception e) {}
            
            model.setParameter(restrictedModel.getParameterNames(), restrictedModel.getParameter());
            model.setParameter(hypothesis, new double[hypothesis.length]);
            double[] startingFull = model.getParameter();
            
            model.setDataDistribution(Statik.correlationMatrix(simData), new double[anzVar], anzPer);
            System.out.println("Starting estimation on simulated data set "+i+".");
            double simLLFull = Double.NaN;
            try {
                if (!Double.isNaN(simLLRes)) {
                    model.warningFlag = warningFlagTypes.OK;
                    double[] simValFull = floMultipleRuns(new double[][]{startingFull, starting,  valsFull}, model, REPEATEDSTARTS,PRECISION);
                    if (model.warningFlag != warningFlagTypes.FAILED) simLLFull = model.ll;
                }
            } catch (Exception e) {}
            
            double simLR = simLLRes - simLLFull;
            vals[i] = simLR;
            if (!Double.isNaN(simLR))
                if (simLR > lr) countAbove++; else countBelow++;
        }
        double erg = (double)countAbove / (double)(countAbove+countBelow);
        Arrays.sort(vals);
        System.out.println("LR: "+lr);
        System.out.println("Simulation LRs :"+Statik.matrixToString(vals));
        System.out.println("p - Value: "+erg);
        return erg;
    }
    
    public static void timoJoeyModel() {
        LinearModel full = new LinearModel(new double[][]{{1,0},{1,1},{1,2}}, new int[][]{{0,3},{3,1}}, new double[][]{{2,1},{1,2}}, new int[]{-1,-1}, new double[]{0,0}, 2, 1.0);
        LinearModel res  = new LinearModel(new double[][]{{1,0},{1,1},{1,2}}, new int[][]{{0,-1},{-1,1}}, new double[][]{{2,0},{0,2}}, new int[]{-1,-1}, new double[]{0,0}, 2, 1.0);
        
        full.evaluateMuAndSigma(); double[][] S1 = full.sigma;
        res.setDataDistribution(S1, new double[3]);
        double[] est = res.estimateML(0.0001);
        System.out.println(Statik.matrixToString(est,4)+"\r\n");
        System.out.println(Statik.matrixToString(res.sigma));
        double[][] Q1 = new double[][]{{-Math.sqrt(1.0/3.0),-Math.sqrt(1.0/3.0),-Math.sqrt(1.0/3.0)},{Math.sqrt(0.5),0,-Math.sqrt(0.5)},{Math.sqrt(1.0/6.0),-Math.sqrt(2.0/3.0),Math.sqrt(1.0/6.0)}};
        double[][] Sigma2 = Statik.multiply(Q1, Statik.multiply(res.sigma, Statik.transpose(Q1)));
        System.out.println(Statik.matrixToString(Sigma2));
        double lambda1 = Math.sqrt(12 + Math.sqrt(103)), lambda2 = Math.sqrt(12 - Math.sqrt(103));
        double[][] Q2 = new double[][]{{-0.919/lambda1,-0.394/lambda1,0},{0.394/lambda2, -0.919/lambda2,0},{0,0,1}};
        double[][] check1 = Statik.multiply(Q1, Statik.multiply(S1,Statik.transpose(Q1)));
        System.out.println(Statik.matrixToString(check1));
        double[][] check2 = Statik.multiply(Q2, Statik.multiply(check1,Statik.transpose(Q2)));
        System.out.println(Statik.matrixToString(check2));
        double[][] Sigma3 = Statik.multiply(Q2, Statik.multiply(Sigma2,Statik.transpose(Q2)));
        System.out.println(Statik.matrixToString(Sigma3));
    }
    
    public static RAMModel floVeryFlatFactorModel() {
        int[][] asyPar = new int[10][10]; for (int i=0; i<10; i++) for (int j=0; j<10; j++) asyPar[i][j] = -1; for (int i=1; i<10; i++) asyPar[i][0] = i-1;
        int[][] symPar = new int[10][10]; for (int i=0; i<10; i++) for (int j=0; j<10; j++) symPar[i][j] = -1; for (int i=1; i<10; i++) symPar[i][i] = 10+i-1;       
        double[][] asyVal = new double[10][10]; 
        double[][] symVal = new double[10][10]; symVal[0][0] = 1.0;
        int[] meanPar = new int[10]; for (int i=0; i<10; i++) meanPar[i] = -1;
        double[] meanVal = new double[10];
        int[] filter = new int[9]; for (int i=0; i<9; i++) filter[i] = i+1;
        RAMModel erg = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        return erg;
    }

    public static void floOutputDetrentLongFormat(boolean useTaskCombination) {
        String withinDataName = "raw_person";
        int anzPerFull = 101, anzVar = 18, corSize =  (useTaskCombination?9:18);
        int maxSessions = 110;
        
        double[][] detrendedLong = new double[anzPerFull*maxSessions*corSize][4];
        int rowCount = 0;
        
        int[] omit = new int[]{};
        int nextOmit = 0;
        for (int partNr=1; partNr<=anzPerFull; partNr++) if (nextOmit >= omit.length || partNr != omit[nextOmit]) {
            double[][] wthnDataWithID = Statik.loadMatrix("FloData"+File.separator+withinDataName+"_"+partNr+".dat", ' ', true, ".");
            int[] allButOne = new int[anzVar]; for (int i=0; i<anzVar; i++) allButOne[i] = i+1;
            double[][] wthnData = Statik.submatrix(wthnDataWithID, null, allButOne);
            double[][] wthnDataSmooth = Statik.gaussSmoothening(wthnData, 3.0, 0.0, Model.MISSING, true);
            double[][] wthnDataDetrend = new double[wthnData.length][wthnData[0].length];
            for (int i=0; i<wthnData.length; i++) for (int j=0; j<wthnData[i].length; j++) { 
                wthnDataDetrend[i][j] = (wthnData[i][j]==Model.MISSING?Model.MISSING:wthnData[i][j]-wthnDataSmooth[i][j]);
                if (j%2==1) detrendedLong[rowCount++] = new double[]{partNr,i+1,(j/2)+1,
                        (wthnDataDetrend[i][j]==Model.MISSING||wthnDataDetrend[i][j-1]==Model.MISSING?Model.MISSING:(wthnDataDetrend[i][j]+wthnDataDetrend[i][j-1])/2.0)};
            }
        }
        Statik.writeMatrix(detrendedLong, "DetrendedDataLong"+(useTaskCombination?"CombinedTasks":"Indicator")+".txt",'\t');
    }
    
    /**
     * Computes for every participant the predicted loadings based on a linear regression from the preferences to the loadings on all other participants.
     * If preferences is null, the intercept only model is fit i.e., the centroid of the remaining participants.
     * 
     * @param preferences
     * @param loadings
     */
    public static double[][] floComputePredictedLoadingsFromPreferences(double[][] preferences, double[][] loadings) {
        int anzPer = loadings.length;
        int anzVar = loadings[0].length;
        
        double[][] erg = new double[anzPer][anzVar];
        for (int p=0; p<anzPer; p++) {
            double[] weights = new double[anzVar], icept = new double[anzVar];
            for (int v=0; v<anzVar; v++) {
                double[] y = new double[anzPer-1]; for (int k=0; k<anzPer-1; k++) y[k] = loadings[(k<p?k:k+1)][v];
                double[] x = new double[anzPer-1]; for (int k=0; k<anzPer-1; k++) x[k] = (preferences==null?0:preferences[(k<p?k:k+1)][v]);
                double xsum = 0, xsumsq = 0, ysum =0 , xysum = 0;
                for (int i=0; i<y.length; i++) {xsum += x[i]; xsumsq += x[i]*x[i]; ysum += y[i]; xysum += x[i]*y[i];}
                double xmean = xsum/anzPer, ymean = ysum/anzPer, xstdv = Math.sqrt(xsumsq/anzPer-xmean*xmean), xycov = xysum/anzPer - xmean*ymean;
                weights[v] = (preferences==null?0:xycov / xstdv); icept[v]= ymean - weights[v]*xmean;
            }
            for (int i=0; i<anzVar; i++) erg[p][i] = (preferences==null?0:weights[i]*preferences[p][i]) + icept[i];
        }
        return erg;
    }
    
    public static void floClustering() {floClustering(true,false,false,false);}
    public static void floClustering(boolean useDetrend, boolean useFirstFactor, boolean useTaskCombination, boolean ignoreBetweenInMDS) {
        
        // Setting constants and reserving memory space
        String betweenDataName = "between";
        String withinDataName = "raw_person";
        int anzPerFull = 101, anzVar = 18, corSize = (useFirstFactor || useTaskCombination?9:18);
        
        double[] avgPersonCorWthn = new double[anzPerFull+1]; Statik.setTo(avgPersonCorWthn, -999.0);           
        double[] avgPersonCorBtw = new double[anzPerFull+1]; Statik.setTo(avgPersonCorBtw, -999.0);   
        double[] highestEValCorWthn = new double[anzPerFull+1]; Statik.setTo(highestEValCorWthn, -999.0);
        double[] highestEValCorBtwn = new double[anzPerFull+1]; Statik.setTo(highestEValCorBtwn, -999.0);
        double[] highestEValCorSmooth = new double[anzPerFull+1]; Statik.setTo(highestEValCorBtwn, -999.0);
        double[][] highestEVecCorWthn = new double[anzPerFull+1][corSize]; Statik.setTo(highestEVecCorWthn, -999.0);
        double[][] highestEVecCorBtwn = new double[anzPerFull+1][corSize]; Statik.setTo(highestEVecCorBtwn, -999.0);
        double[][] highestEVecCorSmooth = new double[anzPerFull+1][corSize]; Statik.setTo(highestEVecCorBtwn, -999.0);
        double[][] flatFactorCoefficient = new double[anzPerFull+1][corSize]; Statik.setTo(flatFactorCoefficient, -999.0);
        double[][] preferences = Statik.loadMatrix("FloData"+File.separator+"cogito_preferences.dat", '\t');
        int[] omit = new int[]{};
        int nextOmit = 0;
        Vector<Integer> id = new Vector<Integer>();
        Vector<double[][]> corv = new Vector<double[][]>();
        double[][] wthnAvgCov = new double[corSize][corSize], btwnAvgCov = new double[corSize][corSize];
        double[][][] wthnCorMem = new double[anzPerFull][corSize][corSize];
        double[][][] smoothCorMem = new double[anzPerFull][corSize][corSize];
        double[][] wthnDataSmoothRaise = new double[anzPerFull][corSize];
        double[][] wthnMean = new double[anzPerFull][corSize];
        double[][] wthnReducedAvgCov = new double[corSize][corSize], btwnReducedAvgCov = new double[corSize][corSize];
        double[][][] pretestData = new double[anzPerFull][][];
        double[][] pretestMean = new double[anzPerFull][];
        double[][][] pretestCov = new double[anzPerFull][][];
        double[][][] combinedData = new double[anzPerFull][][];
        double[][] combinedMean = new double[anzPerFull][corSize];
        double[][][] combinedWthnCov = new double[anzPerFull][corSize][corSize];

        /*
         * In this loop, the covariance and correlation matrices "within" and "between" are computed
         * for each participant. The within covariance matrix is based on the participant's longitudinal
         * data, the between covariance matrix is based on the pretest data for all participants with the 
         * same time settings as the current participant. Depending on the script parameters, the data is either raw or detrended,
         * and the two indicators for each variable are either left separate or collapsed into one. 
         * Participants can be omitted from the analysis using the omit vector. All participants with numerical errors
         * are reported to standard output.
         */ 
        System.out.print("Failed IDs = ");
        for (int partNr=1; partNr<=anzPerFull; partNr++) if (nextOmit >= omit.length || partNr != omit[nextOmit]) {
            double[][] wthnDataWithID = Statik.loadMatrix("FloData"+File.separator+withinDataName+"_"+partNr+".dat", ' ', true, ".");
            int[] allButOne = new int[anzVar]; for (int i=0; i<anzVar; i++) allButOne[i] = i+1;
            double[][] wthnData = Statik.submatrix(wthnDataWithID, null, allButOne);
            double[][] wthnDataSmooth = Statik.gaussSmoothening(wthnData, 3.0, 0.0, Model.MISSING, true);
            double[][] wthnDataDetrend = new double[wthnData.length][wthnData[0].length];
            for (int i=0; i<wthnData.length; i++) for (int j=0; j<wthnData[i].length; j++) 
                wthnDataDetrend[i][j] = (wthnData[i][j]==Model.MISSING?Model.MISSING:wthnData[i][j]-wthnDataSmooth[i][j]);
            
            int k = anzVar / corSize;
            for (int i=0; i<corSize; i++) {
                wthnDataSmoothRaise[partNr-1][i] = 0;
                for (int j=0; j<k; j++) wthnDataSmoothRaise[partNr-1][i] += wthnDataSmooth[wthnDataSmooth.length-1][k*i+j]-wthnDataSmooth[0][k*i+j];
                wthnDataSmoothRaise[partNr-1][i] /= (double)k;
            }
            double[][] wthnCov = new double[anzVar][anzVar]; double[] wthnMeanAll = new double[anzVar];
            Statik.covarianceMatrixAndMeans((useDetrend?wthnDataDetrend:wthnData), wthnMeanAll, wthnCov, Model.MISSING);
            double[][] wthnCor = Statik.correlationFromCovariance(wthnCov);
            globalWarning = false;
            if (useFirstFactor) {
                wthnCor = computePairwiseFactorCorrelationSimple(wthnCor);
                if (globalWarning) System.out.print(partNr+"\t");
            }
            if (useTaskCombination) {
                wthnCor = computePairwiseCombinedCorrelationSimple(wthnCor);
                if (globalWarning) System.out.print(partNr+"\t");
            }
            if (!globalWarning) {
                corv.add(wthnCor);
                id.add(partNr);
                wthnAvgCov = Statik.add(wthnAvgCov, wthnCor);
                wthnCorMem[partNr-1] = Statik.copy(wthnCor);
                if (useFirstFactor || useTaskCombination) wthnMean[partNr-1] = Statik.copy(wthnMeanAll); 
                else for (int i=0; i<corSize; i++) wthnMean[partNr-1][i] = (wthnMeanAll[2*i]+wthnMeanAll[2*i+1])/2.0;
            }
            
            double[][] smoothCov = new double[anzVar][anzVar]; double[] smoothMean = new double[anzVar];
            Statik.covarianceMatrixAndMeans(wthnDataSmooth, smoothMean, smoothCov);
            double[][] smoothCor = Statik.correlationFromCovariance(smoothCov);
            globalWarning = false;
            if (useFirstFactor) {
                smoothCor = computePairwiseFactorCorrelationSimple(smoothCor);
                if (globalWarning) System.out.print(partNr+"\t");
            }
            if (useTaskCombination) {
                smoothCor = computePairwiseCombinedCorrelationSimple(smoothCor);
                if (globalWarning) System.out.print(partNr+"\t");
            }
            if (!globalWarning) {
                smoothCorMem[partNr-1] = Statik.copy(smoothCor);
            }
            
            double[][] btwDataWithID = Statik.loadMatrix("FloData"+File.separator+betweenDataName+"_"+partNr+".dat", ' ', true, ".");
            allButOne = new int[anzVar]; for (int i=0; i<anzVar; i++) allButOne[i] = i+1;
            double[][] btwnData = Statik.submatrix(btwDataWithID, null, allButOne);

            pretestData[partNr-1] = btwnData;
            if (useTaskCombination) {
                pretestData[partNr-1] = new double[btwnData.length][9]; for (int i=0; i<btwnData.length; i++) {
                    double[] row = btwnData[i];
                    for (int j=0; j<9; j++) pretestData[partNr-1][i][j] = (Model.isMissing(row[2*j]) || Model.isMissing(row[2*j+1])?Model.MISSING:(row[2*j]+row[2*j+1])/2.0);
                }
            }
            pretestMean[partNr-1] = new double[corSize]; pretestCov[partNr-1] = new double[corSize][corSize];
            Statik.covarianceMatrixAndMeans(pretestData[partNr-1], pretestMean[partNr-1], pretestCov[partNr-1], Model.MISSING);
            globalWarning = false;
            double[][] btwCor = Statik.correlationFromCovariance(pretestCov[partNr-1]);
            if (!globalWarning) {
                if (!ignoreBetweenInMDS) corv.add(btwCor);
                if (!ignoreBetweenInMDS) id.add(partNr+1000);
                btwnAvgCov = Statik.add(btwnAvgCov, btwCor);
            } else System.out.print("*\t");
            
            avgPersonCorWthn[partNr] = 0; for (int i=0; i<wthnCor.length; i++) for (int j=i+1; j<wthnCor.length; j++) avgPersonCorWthn[partNr] += wthnCor[i][j]; 
            avgPersonCorWthn[partNr] /= (wthnCor.length*(wthnCor.length-1)/2);
            avgPersonCorBtw[partNr] = 0; for (int i=0; i<btwCor.length; i++) for (int j=i+1; j<btwCor.length; j++) avgPersonCorBtw[partNr] += btwCor[i][j]; 
            avgPersonCorBtw[partNr] /= (btwCor.length*(btwCor.length-1)/2);
            double[] eval = new double[corSize];
            double[][] evec = Statik.eigenvectorsOfSymmetrical(wthnCor, eval, true);
            highestEValCorWthn[partNr] = eval[0];
            highestEVecCorWthn[partNr] = Statik.multiply((evec[0][0]<0?-1:1), evec[0]);
            evec = Statik.eigenvectorsOfSymmetrical(btwCor, eval, true);
            highestEValCorBtwn[partNr] = eval[0];
            highestEVecCorBtwn[partNr] = Statik.multiply((evec[0][0]<0?-1:1), evec[0]);
            evec = Statik.eigenvectorsOfSymmetrical(smoothCor, eval, true);
            highestEValCorSmooth[partNr] = eval[0];
            highestEVecCorSmooth[partNr] = Statik.multiply((evec[0][0]<0?-1:1), evec[0]);
            if (useFirstFactor || useTaskCombination) {
                RAMModel flatFactor = floVeryFlatFactorModel();
                flatFactor.setDataDistribution(wthnCor, new double[9], 1);
                double[] starting = new double[18]; for (int i=0; i<9; i++) {
                    starting[i] = highestEVecCorWthn[partNr][i]; 
                    starting[i+9] = 1-Math.pow(highestEVecCorWthn[partNr][i],2);
                }
                double[] est = flatFactor.estimateML(starting); 
                flatFactorCoefficient[partNr] = new double[9]; for (int i=0; i<9; i++) flatFactorCoefficient[partNr][i] = est[i];
            }
            
            combinedData[partNr-1] = (useDetrend?wthnDataDetrend:wthnData);
            if (useTaskCombination) {
                combinedData[partNr-1] = new double[wthnData.length][9]; for (int i=0; i<wthnData.length; i++) {
                    double[] row = (useDetrend?wthnDataDetrend:wthnData)[i];
                    for (int j=0; j<9; j++) combinedData[partNr-1][i][j] = (Model.isMissing(row[2*j]) || Model.isMissing(row[2*j+1])?Model.MISSING:(row[2*j]+row[2*j+1])/2.0);
                }
            }
            Statik.covarianceMatrixAndMeans(combinedData[partNr-1], combinedMean[partNr-1], combinedWthnCov[partNr-1], Model.MISSING);
        } else nextOmit++;
        btwnAvgCov = Statik.multiply(1.0/anzPerFull, btwnAvgCov);
        wthnAvgCov = Statik.multiply(1.0/anzPerFull, wthnAvgCov);

        // Preparing computation of residual error when predicting a single value from covariance matrix. 
        double[] residualWthnBiasOnWthnData = new double[corSize], residualWthnR2OnWthnData = new double[corSize], 
                residualAvgWthnBiasOnWthnData = new double[corSize], residualAvgWthnR2OnWthnData = new double[corSize], 
                residualAvgWthnBiasOnBtwnData = new double[corSize], residualAvgWthnR2OnBtwnData = new double[corSize], 
                residualAvgBtwnBiasOnWthnData = new double[corSize], residualAvgBtwnR2OnWthnData = new double[corSize],
                residualAvgBtwnBiasOnBtwnData = new double[corSize], residualAvgBtwnR2OnBtwnData = new double[corSize], 
                residualZeroBiasOnWthnData = new double[corSize], residualZeroR2OnWthnData = new double[corSize],
                residualZeroBiasOnBtwnData = new double[corSize], residualZeroR2OnBtwnData = new double[corSize];
        
        double[] grandMean = new double[corSize]; double[][] combinedBtwnCov = new double[corSize][corSize];
        Statik.covarianceMatrixAndMeans(combinedMean, grandMean, combinedBtwnCov, Model.MISSING);
        double[][] combinedWthnAvgCov = new double[corSize][corSize];
        for (int partNr=1; partNr <= anzPerFull; partNr++) for (int i=0; i<corSize; i++) for (int j=0; j<corSize; j++) combinedWthnAvgCov[i][j] += combinedWthnCov[partNr-1][i][j];
        combinedWthnAvgCov = Statik.multiply(1.0/(double)anzPerFull, combinedWthnAvgCov);
        
        /*
         * The following loop computes all data prediction precision. There are 2x4 conditions: Prediction is either based on combined data, in which the two indicators
         * for each variable are collapsed, or on the full data. The matrix used is either the pretest covariance matrix (between condition), on the average within covariance matrix,
         * on the specific within covariance matrix of this participant, or on the identity matrix. 
         * 
         */
        int anzCond = 8; int[] totMeasures = new int[anzCond];
        double[][] residualBias = new double[anzCond][corSize], residualR2 = new double[anzCond][corSize];
        double[][] ident = Statik.identityMatrix(corSize);
        for (int cond=0; cond<anzCond; cond++) {
            for (int partNr=1; partNr <= anzPerFull; partNr++) {
                double[][] data = null, matrix = null;
                if (cond==0) {data = pretestData[partNr-1]; matrix = pretestCov[partNr-1];}
                if (cond==1) {data = pretestData[partNr-1]; matrix = combinedWthnAvgCov;}
                if (cond==2) {data = pretestData[partNr-1]; matrix = combinedWthnCov[partNr-1];}
                if (cond==3) {data = pretestData[partNr-1]; matrix = ident;}
                if (cond==4) {data = combinedData[partNr-1]; matrix = pretestCov[partNr-1];}
                if (cond==5) {data = combinedData[partNr-1]; matrix = combinedWthnAvgCov;}
                if (cond==6) {data = combinedData[partNr-1]; matrix = combinedWthnCov[partNr-1];}
                if (cond==7) {data = combinedData[partNr-1]; matrix = ident;}
                double[] mean = Statik.meanVector(data,Model.MISSING);
                
                for (int t=0; t<data.length; t++) {
                    boolean hasMissing = false; for (int i=0; i<corSize; i++) if (Model.isMissing(data[t][i])) hasMissing=true;
                    if (!hasMissing) {
                        double[] datarow = Statik.subtract(data[t], mean); 
                        SaturatedRAMModel sat = new SaturatedRAMModel(corSize, true);
                        sat.filter = new int[corSize-1];
                        sat.setParameterToDistribution(new double[corSize], matrix);
                        for (int i=0; i<corSize; i++) {
                            for (int j=0; j<corSize-1; j++) sat.filter[j] = (j<i?j:j+1);
                            sat.setFilter(sat.filter);
                            double[] scores = sat.getAllScores(null, new double[][]{Statik.subvector(datarow, i)})[0];
                            double residual = scores[i]-datarow[i];
                            residualBias[cond][i] += residual; residualR2[cond][i] += residual*residual;
                        }
                        totMeasures[cond]++;
                    }
                }
            }
        }
        System.out.println("\r\n\r\nPrediction Precision bias and R^2");
        for (int cond=0; cond<anzCond; cond++) {
            residualBias[cond] = Statik.multiply(1.0/(double)(totMeasures[cond]), residualBias[cond]);
            residualR2[cond] = Statik.multiply(1.0/(double)(totMeasures[cond]), residualR2[cond]);
        }
        for (int cond=0; cond<anzCond; cond++) System.out.println("Bias Condition "+cond+" : \t"+Statik.matrixToString(residualBias[cond]));
        for (int cond=0; cond<anzCond; cond++) System.out.println("R^2 Condition "+cond+"  : \t"+Statik.matrixToString(residualR2[cond]));
        // END prediction precision
        
        // BEGIN computation of Ergodic Subspace Analysis, not used in Schmiedeck et al. manuscript
        
        System.out.println("\r\nWithin Data smoothed, difference last to first day");
        System.out.println(Statik.matrixToString(wthnDataSmoothRaise));

        System.out.println("\r\nWithin Data smoothed, highest Eigenvector");
        System.out.println(Statik.matrixToString(highestEVecCorSmooth));        
        
//        int[] omitFromPref = new int[]{7,31,54,55,56,60,62,69,77,100};        // end solution for smoothed
//        int[] omitFromPref = new int[]{6,60,62};
//        int[] omitFromPref = new int[]{6,7,54,55,56,60,62,69,77,100};
//        int[] omitFromPref = new int[]{2,7,24,60,61,62,84};
        int[] omitFromPref = new int[]{7,31,54,55,56,60,62,69,77,100};        // end solution for raw
//        int[] omitFromPref = new int[]{};
        System.out.println("\r\nDistances from preferences and between matrix.");
        System.out.println("ID\tpredicted\tIntercept\tbetween\tdirect pred\tdir icept\tdir btw");
        boolean compareToSmoothed = false;
        double[][] loadingsFull = new double[anzPerFull][]; 
        for (int i=0; i<anzPerFull; i++) loadingsFull[i] = (compareToSmoothed?Statik.multiply(Math.sqrt(highestEValCorSmooth[i+1]),highestEVecCorSmooth[i+1]):flatFactorCoefficient[i+1]);
        
        double[][] loadings = new double[anzPerFull-omitFromPref.length][];
        int r=0, s=0; for (int i=0; i<anzPerFull; i++) if (r<omitFromPref.length && i==omitFromPref[r]) r++; else loadings[s++] = loadingsFull[i];
        for (int i=0; i<loadings.length; i++) {double sum=0; for (int j=0; j<loadings[i].length; j++) sum+=loadings[i][j]; if (sum<0) for (int j=0; j<loadings[i].length; j++) loadings[i][j] *= -1;}
        
        double[][] pref = new double[anzPerFull-omitFromPref.length][];
        r=0; s=0; for (int i=0; i<anzPerFull; i++) if (r<omitFromPref.length && i==omitFromPref[r]) r++; else pref[s++] = Statik.copy(preferences[i]);
        for (int i=0; i<pref.length; i++) {
            double sum = 0, sqsum = 0; for (int j=0; j<pref[i].length; j++) {sum+= pref[i][j]; sqsum += pref[i][j]*pref[i][j];}
            double mean = sum/pref[i].length, stdv = sqsum/pref[i].length - mean*mean;
            for (int j=0; j<pref[i].length; j++) pref[i][j] = (stdv==0?0:(pref[i][j]-mean)/stdv);
        }
        
        int[] numb = new int[anzPerFull-omitFromPref.length];
        r=0; s=0; for (int i=0; i<anzPerFull; i++) if (r<omitFromPref.length && i==omitFromPref[r]) r++; else numb[s++] = i+1;
        
        
        double distPredSum = 0;
        double[][] estimatedLoadings = floComputePredictedLoadingsFromPreferences(pref, loadings);
        double[][] estimatedLoadingsIcept = floComputePredictedLoadingsFromPreferences(null, loadings);
        r=0; s=0;
        for (int i=0; i<anzPerFull; i++) if (r<omitFromPref.length && i==omitFromPref[r]) r++; else {
            double[][] predictedCor = Statik.multiply(estimatedLoadings[s], estimatedLoadings[s], true); 
            for (int j=0; j<estimatedLoadings[s].length; j++) predictedCor[j][j] = 1.0;
            double[][] predictedCorIcept = Statik.multiply(estimatedLoadingsIcept[s], estimatedLoadings[s], true); 
            for (int j=0; j<estimatedLoadingsIcept[s].length; j++) predictedCorIcept[j][j] = 1.0;
            double[][] btwFFCor = Statik.multiply(highestEValCorBtwn[i+1], Statik.multiply(highestEVecCorBtwn[i+1], highestEVecCorBtwn[i+1], true));
            for (int j=0; j<highestEVecCorBtwn[i+1].length; j++) btwFFCor[j][j] = 1.0;
            double[][] correctCor = (compareToSmoothed?smoothCorMem[i]:wthnCorMem[i]);
            double distPredicted = Statik.symmetricalKullbackLeiblerNormal(predictedCor, correctCor);
            distPredSum += distPredicted;
            double distPredictedIcept = Statik.symmetricalKullbackLeiblerNormal(predictedCorIcept, correctCor);
            double distBtw = Statik.symmetricalKullbackLeiblerNormal(btwFFCor, correctCor);
            double dirDistPred = Statik.distance(estimatedLoadings[s], loadings[s]);
            double dirDistPredIcept = Statik.distance(estimatedLoadingsIcept[s], loadings[s]);
            double dirDistPredBtw = Statik.distance(highestEVecCorBtwn[i+1], loadings[s]);
            System.out.println((i+1)+"\t"+distPredicted+"\t"+distPredictedIcept+"\t"+distBtw+"\t"+dirDistPred+"\t"+dirDistPredIcept+"\t"+dirDistPredBtw);
            s++;
        }
        double avgRealDist = distPredSum /= numb.length;
        
        // Null Distribution to sampling
        System.out.println("Starting to sample null distribution...");
        int trials = 1000; Random rand = new Random();
        int countBetter = 0;
        double sumAvgDist = 0, sumSqrAvgDist = 0, sumAvgDirDist = 0, sumSqrAvgDirDist = 0;
        for (int t=0; t<trials; t++) {
            int[] shuffle = Statik.enumeratIntegersFrom(0, pref.length-1); Statik.shuffle(shuffle, rand);
            double[][] shufflePref = new double[pref.length][]; for (int i=0; i<pref.length; i++) shufflePref[i] = pref[shuffle[i]];
            estimatedLoadings = floComputePredictedLoadingsFromPreferences(shufflePref, loadings);
            double sumDist = 0.0, sumDirDist = 0.0;
            r=0; s=0;
            for (int i=0; i<anzPerFull; i++) if (r<omitFromPref.length && i==omitFromPref[r]) r++; else {
                double[][] predictedCor = Statik.multiply(estimatedLoadings[s], estimatedLoadings[s], true); 
                for (int j=0; j<estimatedLoadings[s].length; j++) predictedCor[j][j] = 1.0;
                double[][] correctCor = (compareToSmoothed?smoothCorMem[i]:wthnCorMem[i]);
                double distPredicted = Statik.symmetricalKullbackLeiblerNormal(predictedCor, correctCor);
                double distDir = Statik.distance(estimatedLoadings[s], loadings[s]);
                sumDist += distPredicted; sumDirDist += distDir;
                s++;
            }
            double avgDist = sumDist / estimatedLoadings.length;
            sumAvgDist += avgDist; sumSqrAvgDist += avgDist*avgDist;
            double avgDirDist = sumDirDist / estimatedLoadings.length;
            sumAvgDirDist += avgDirDist; sumSqrAvgDirDist += avgDirDist*avgDirDist;
            if (avgDist < avgRealDist) countBetter++;
        }
        double avgDist = sumAvgDist / trials, avgStdv = Math.sqrt( sumSqrAvgDist / trials - avgDist*avgDist );
        double avgDirDist = sumAvgDirDist / trials, avgDirStdv = Math.sqrt( sumSqrAvgDirDist / trials - avgDirDist*avgDirDist );
        System.out.println("Average matrix distance shuffled = "+avgDist+" +- "+avgStdv);
        System.out.println("Average direct distance shuffled = "+avgDirDist+" +- "+avgDirStdv);
        System.out.println("Cases better than unshuffled: "+countBetter+" of "+trials+", = "+(countBetter/(double)trials));
        
        System.out.println("\r\nAverage Correlation and Highest Eigenvalue");
        System.out.println("ID\tBTW\tWTHN\tBTW\tWTHN");
        for (int i=1; i<anzPerFull+1; i++) {
            System.out.println(i+"\t"+avgPersonCorBtw[i]+"\t"+avgPersonCorWthn[i]+"\t"+highestEValCorBtwn[i]+"\t"+highestEValCorWthn[i]);
        }
        System.out.println("\r\nHighest Eigenvectors Between and correlation to preference.");
        for (int i=1; i<anzPerFull+1; i++) {
            System.out.println(i+"\t"+Statik.matrixToString(highestEVecCorBtwn[i])+"\t"+Statik.correlation(highestEVecCorBtwn[i], preferences[i-1]));
        }
        System.out.println("\r\nHighest Eigenvectors Within and correlation to preference.");
        for (int i=1; i<anzPerFull+1; i++) {
            System.out.println(i+"\t"+Statik.matrixToString(highestEVecCorWthn[i])+"\t"+Statik.correlation(highestEVecCorWthn[i], preferences[i-1]));
        }
        if (corSize == 9) {
            System.out.println("\r\nFirst Factor Within and correlation to preference.");
            for (int i=1; i<anzPerFull+1; i++) {
                System.out.println(i+"\t"+Statik.matrixToString(flatFactorCoefficient[i])+"\t"+Statik.correlation(flatFactorCoefficient[i], preferences[i-1]));
            }
        }

        // END computation of Ergodic Subspace Analysis, not used in Schmiedeck et al. manuscript
        
        /*
         * The following block prepares the Kullback Leibler Divergences between all "between" and "within" covariance matrices and establishes by simulation the null distributions. 
         */
        System.out.println();
        int anzPer = corv.size();
        double[][][] cor = new double[anzPer][][];
        corv.toArray(cor);
        Integer[] idI = new Integer[anzPer]; id.toArray(idI);
        int[] ids = new int[anzPer]; for (int i=0; i<anzPer; i++) ids[i] = idI[i].intValue();
        
        int anzBtw = 0; for (int i=0; i<anzPer; i++) if (ids[i] > 1000) anzBtw++;
        double[][][] allBtwCov = new double[anzBtw][][], block1BtwCov = new double[anzBtw][][], 
                block2BtwCov = new double[anzBtw][][], block3BtwCov = new double[anzBtw][][]; 
        int k = 0; for (int i=0; i<anzPer; i++) if (ids[i] > 1000) {allBtwCov[k] = cor[i]; block1BtwCov[k] = Statik.submatrix(cor[i], new int[]{0,1,2}, new int[]{0,1,2});
           block2BtwCov[k] = Statik.submatrix(cor[i], new int[]{3,4,5}, new int[]{3,4,5}); block3BtwCov[k] = Statik.submatrix(cor[i], new int[]{6,7,8}, new int[]{6,7,8});
           k++;
        }
        int simTrials = 100;
        System.out.println("********** Starting simulation of null distribution for average KL (within) ************");
        floSimulateAverageKLUnderNull(wthnAvgCov, null, simTrials);
        System.out.println("********** Starting simulation of null distribution for average KL (between) ************");
        floSimulateAverageKLUnderNull(btwnAvgCov, allBtwCov, simTrials);
        System.out.println("********** Starting simulation of null distribution for average KL (PS within) ************");
        floSimulateAverageKLUnderNull(Statik.submatrix(wthnAvgCov, new int[]{0,1,2}, new int[]{0,1,2}), null, simTrials);
        System.out.println("********** Starting simulation of null distribution for average KL (PS between) ************");
        floSimulateAverageKLUnderNull(Statik.submatrix(btwnAvgCov, new int[]{0,1,2}, new int[]{0,1,2}), block1BtwCov, simTrials);
        System.out.println("********** Starting simulation of null distribution for average KL (WM within) ************");
        floSimulateAverageKLUnderNull(Statik.submatrix(wthnAvgCov, new int[]{3,4,5}, new int[]{3,4,5}), null, simTrials);
        System.out.println("********** Starting simulation of null distribution for average KL (WM between) ************");
        floSimulateAverageKLUnderNull(Statik.submatrix(btwnAvgCov, new int[]{3,4,5}, new int[]{3,4,5}), block2BtwCov, simTrials);
        System.out.println("********** Starting simulation of null distribution for average KL (EM within) ************");
        floSimulateAverageKLUnderNull(Statik.submatrix(wthnAvgCov, new int[]{6,7,8}, new int[]{6,7,8}), null, simTrials);
        System.out.println("********** Starting simulation of null distribution for average KL (EM between) ************");
        floSimulateAverageKLUnderNull(Statik.submatrix(btwnAvgCov, new int[]{6,7,8}, new int[]{6,7,8}), block3BtwCov, simTrials);

        floErgodicSubspace(btwnAvgCov, wthnAvgCov);
        
        if (!ignoreBetweenInMDS) {
            System.out.println("Distances of within covariance matrices to between centroid (5% significant greater than 62.70), then distance to corresponding between: ");
            for (int i=0; i<anzPer; i++) if (ids[i] < 1000) {
                double distToBtwCentroid = Statik.symmetricalKullbackLeiblerNormal(cor[i], btwnAvgCov);
                double distToCorBtw = Statik.symmetricalKullbackLeiblerNormal(cor[i], cor[i+1]);
                System.out.println(ids[i]+"\t"+distToBtwCentroid+"\t"+distToCorBtw);
            }
        }
        
        double[][] distance = new double[anzPer][anzPer];
        for (int i=0; i<anzPer; i++) for (int j=i; j<anzPer; j++) distance[i][j] = distance[j][i] = Statik.symmetricalKullbackLeiblerNormal(cor[i], cor[j]);
        // following two lines just to iron out numerical inprecisions on the diagonal
        for (int i=0; i<anzPer; i++) for (int j=i+1; j<anzPer; j++) distance[i][j] = distance[j][i] = distance[i][j] - (distance[i][i] + distance[j][j])/2.0;
        for (int i=0; i<anzPer; i++) distance[i][i] = 0.0; 

        double[][] btwToWthnDist = new double[4][anzPer];
        int totWthn = 0, totBtwn = 0;
        // averageKLWithin is the average KL of all within covariance matrix. averageKLBetween is the average KL from with to between for each participant.
        double averageKLWithin = 0, averageKLBetween = 0, stdvKLBetween = 0; 
        for (int i=0; i<anzPer; i++) for (int j=0; j<anzPer; j++) 
            if (ids[i]<1000 && ids[j]<1000)        {totWthn++; averageKLWithin += distance[i][j];}
            else if (ids[i]<1000 && ids[j]==ids[i]+1000) {totBtwn++; averageKLBetween += distance[i][j]; 
                                                          stdvKLBetween += distance[i][j]*distance[i][j]; btwToWthnDist[0][i] = distance[i][j];}
        averageKLWithin /= (double)totWthn; averageKLBetween /= (double)totBtwn;
        stdvKLBetween = Math.sqrt( stdvKLBetween/(double)totBtwn - averageKLBetween*averageKLBetween);
        System.out.println("Average KL within: "+averageKLWithin);
        if (!ignoreBetweenInMDS) System.out.println("Average KL within to between: "+averageKLBetween+" +- "+stdvKLBetween);
        
        for (int block = 0; block < 3; block++) {
            int[] selector = new int[]{3*block, 3*block+1, 3*block+2};
            totWthn = 0; totBtwn = 0;
            averageKLWithin = 0; averageKLBetween = 0; stdvKLBetween = 0; 
            for (int i=0; i<anzPer; i++) for (int j=0; j<anzPer; j++) {
                double value = Statik.symmetricalKullbackLeiblerNormal(Statik.submatrix(cor[i],selector,selector), Statik.submatrix(cor[j],selector,selector));
                if (ids[i]<1000 && ids[j]<1000)        {totWthn++; averageKLWithin += value;}
                else if (ids[i]<1000 && ids[j]==ids[i]+1000) {totBtwn++; averageKLBetween += value; 
                                                              stdvKLBetween += value*value; btwToWthnDist[1+block][i] = value;}
            }
            averageKLWithin /= (double)totWthn; averageKLBetween /= (double)totBtwn; 
            stdvKLBetween = Math.sqrt( stdvKLBetween/(double)totBtwn - averageKLBetween*averageKLBetween );
            System.out.println("Average KL within (Block "+block+"): "+averageKLWithin);
            if (!ignoreBetweenInMDS) System.out.println("Average KL within to between (Block "+block+"): "+averageKLBetween+" +- "+stdvKLBetween);
        }
        double[][] btwToWthnDistPruned = new double[totBtwn][4];
        k = 0; for (int i=0; i<anzPer; i++) if (btwToWthnDist[0][i] != 0.0) {for (int j=0; j<4; j++) btwToWthnDistPruned[k][j] = btwToWthnDist[j][i]; k++;} 
        System.out.println("All between/within distances: \r\n All\tPS\tWM\tEM\r\n"+Statik.matrixToString(btwToWthnDistPruned));

        /*
         * The following block starts the multidimensional scaling, using the standard algorithm implemented in Onyx. 
         */
        int dim = 5;
        double[] scree = new double[dim];
        double[][] coordinates = DataPreprocessing.multiDimensionalScaling(distance, scree, dim, 0.001);
        Statik.writeMatrix(coordinates, "toFlo80DimMDS.csv",'\t');
        
        /*
         *  The following block computes the agglomerative clustering, using the standard algorithm implemented in Onyx. 
         */
        AgglomerativeClustering cluster = new AgglomerativeClustering(distance, true);
        int[] membership = cluster.cluster();

        double[][] coordID = new double[anzPer][dim+2]; 
        for (int i=0; i<anzPer; i++) {
            coordID[i][0] = id.elementAt(i); 
            coordID[i][1] = membership[i];
            for (int j=0; j<dim; j++) coordID[i][j+2] = coordinates[i][j];
        } 
        System.out.println("\t\t"+Statik.matrixToString(scree)+"\r\n");
        System.out.println(Statik.matrixToString(coordID));

        System.out.println("Within Cov = \r\n"+Statik.matrixToString(wthnAvgCov));
        System.out.println("Between Cov = \r\n"+Statik.matrixToString(btwnAvgCov));
        
        // BEGIN block to compute the model fit, not used in the Schmiedeck et al. manuscript
        
        if (!useFirstFactor && !useTaskCombination) {
            RAMModel model = florianFactorModelRAMStandardizedWeights();
            model.setMaximalNumberOfIterations(200);
    
            double[] starting = new double[model.anzPar]; for (int i=0; i<starting.length; i++) starting[i] = 0.5;
            for (int i=0; i<model.anzPar; i++) if (model.isErrorParameter(i)) starting[i] = 1.0;
    
            model.setDataDistribution(wthnAvgCov, new double[anzVar], anzPer);
            double[] valsWthn =  floMultipleRuns(new double[][]{starting}, model, 2, 0.0001);
            
            model.setDataDistribution(btwnAvgCov, new double[anzVar], anzPer);
            double[] valsBtwn =  floMultipleRuns(new double[][]{starting}, model, 2, 0.0001);
            
            valsBtwn[0] = -0.1; valsBtwn[1] = 0.1; valsBtwn[2] = 0.1;
            double[] valsBtwn2 =  floMultipleRuns(new double[][]{valsBtwn}, model, 2, 0.0001);
            
            model.evaluateMuAndSigma(valsBtwn2);
            System.out.println("corrected -2LL: "+model.getMinusTwoLogLikelihood());
            System.out.println("Sigma between = \r\n"+Statik.matrixToString(model.sigma));
            
            System.out.println("Estimates Within  = "+Statik.matrixToString(valsWthn,5));
            System.out.println("Estimates Between = "+Statik.matrixToString(valsBtwn2,5));
        }
    }
    
    public static double[][] computePairwiseFactorCorrelation(double[][] fullCorrelation) {
        int anzVar = fullCorrelation.length, anzFac = anzVar /2;
        int[][] symPar = new int[anzFac+anzVar][anzFac+anzVar]; for (int i=0; i<anzFac+anzVar; i++) for (int j=0; j<anzFac+anzVar; j++) symPar[i][j] = Model.NOPARAMETER;
        double[][] symVal = new double[anzFac+anzVar][anzFac+anzVar];
        int pnr = 0;
        for (int i=0; i<anzFac; i++) for (int j=i+1; j<anzFac; j++) {symPar[i][j] = symPar[j][i] = pnr++; symVal[i][j] = symVal[j][i] = 0.1;}
        for (int i=0; i<anzVar; i++) {symPar[i+anzFac][i+anzFac] = pnr++; symVal[i+anzFac][i+anzFac] = 1.0;}
        for (int i=0; i<anzFac; i++) symVal[i][i] = 1.0;
        
        int[][] asyPar = new int[anzFac+anzVar][anzFac+anzVar]; for (int i=0; i<anzFac+anzVar; i++) for (int j=0; j<anzFac+anzVar; j++) asyPar[i][j] = Model.NOPARAMETER;
        double[][] asyVal = new double[anzFac+anzVar][anzFac+anzVar];
        for (int i=0; i<anzFac; i++) {asyPar[2*i+anzFac][i] = pnr++; asyPar[2*i+anzFac+1][i] = pnr++; asyVal[2*i+anzFac][i] = 1; asyVal[2*i+anzFac+1][i] = 1;}
        
        int[] filter = new int[anzVar]; for (int i=0; i<anzVar; i++) filter[i] = i+anzFac;
        int[] meanPar = new int[anzFac+anzVar]; for (int i=0; i<anzFac+anzVar; i++) meanPar[i] = Model.NOPARAMETER;
        double[] meanVal = new double[anzFac+anzVar];
        
        RAMModel facCorModel = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, meanVal, filter);
        
        facCorModel.setDataDistribution(fullCorrelation, new double[anzVar]);
        facCorModel.estimateLS(facCorModel.getParameter());
        
        double[][] erg = new double[anzFac][anzFac];
        for (int i=0; i<anzFac; i++) for (int j=0; j<anzFac; j++) erg[i][j] = facCorModel.symVal[i][j];
        
        return erg;
    }
    
    public static double[][] computePairwiseFactorCorrelationSimple(double[][] fullCorrelation) {
        int anzVar = fullCorrelation.length, anzFac = anzVar /2;
        double[][] erg = new double[anzFac][anzFac];
        double[][] org = fullCorrelation;
        for (int i=0; i<anzFac; i++) {
            erg[i][i] = 1.0;
            for (int j=i+1; j<anzFac; j++) {
                double corsum = org[2*i][2*j]+org[2*i][2*j+1]+org[2*i+1][2*j]+org[2*i+1][2*j+1];
                double var1 = org[2*i][2*i+1], var2 = org[2*j+1][2*j];
                if (var1<0 || var2<0) {erg[i][j] = erg[j][i] = 1.0; globalWarning = true;}
                else erg[i][j] = erg[j][i] = corsum/(4*Math.sqrt(var1*var2));
                if (erg[i][j] > 1.0) {erg[i][j] = erg[j][i] = 1.0; globalWarning = true;} 
                if (erg[i][j] <-1.0) {erg[i][j] = erg[j][i] = -1.0; globalWarning = true;}
            }
        }
        return erg;
    }
    
    public static double[][] computePairwiseCombinedCorrelationSimple(double[][] fullCorrelation) {
        int anzVar = fullCorrelation.length, anzFac = anzVar /2;
        double[][] erg = new double[anzFac][anzFac];
        double[][] org = fullCorrelation;
        for (int i=0; i<anzFac; i++) {
            erg[i][i] = 1.0;
            for (int j=i+1; j<anzFac; j++) {
                double corsum = org[2*i][2*j]+org[2*i][2*j+1]+org[2*i+1][2*j]+org[2*i+1][2*j+1];
                double var1 = org[2*i][2*i]+org[2*i+1][2*i+1]+org[2*i+1][2*i]+org[2*i][2*i+1],
                       var2 = org[2*j][2*j]+org[2*j+1][2*j+1]+org[2*j+1][2*j]+org[2*j][2*j+1];
                if (var1<0 || var2<0) {erg[i][j] = erg[j][i] = 1.0; globalWarning = true;}
                else erg[i][j] = erg[j][i] = corsum/(Math.sqrt(var1*var2));
                if (erg[i][j] > 1.0) {erg[i][j] = erg[j][i] = 1.0; globalWarning = true;} 
                if (erg[i][j] <-1.0) {erg[i][j] = erg[j][i] = -1.0; globalWarning = true;}
            }
        }
        return erg;
    }

    public static void andyMinimalModel() {
        
        double[] times = new double[]{0,4.5,7.5,11.3};
        int anzVar = times.length;
        int[][] symPar = new int[anzVar+2][anzVar+2]; Statik.setTo(symPar, -1); symPar[1][1] = 0; symPar[0][0] = 1; for (int j=0; j<anzVar; j++) symPar[2+j][2+j] = 3;
        symPar[0][1] = symPar[1][0] = 2;
        double[][] symVal = new double[anzVar+2][anzVar+2]; symVal[0][0] = 73.572; symVal[1][1] = 0.3; for (int j=0; j<anzVar; j++) symVal[j+2][j+2] = 21.993;
        symVal[0][1] = symVal[1][0] = -2.438;
        int[][] asyPar = new int[anzVar+2][anzVar+2]; Statik.setTo(asyPar, -1);
        double[][] asyVal = new double[anzVar+2][anzVar+2]; for (int j=0; j<anzVar; j++) {asyVal[2+j][0] = 1; asyVal[2+j][1] = times[j];}
        int[] meanPar = new int[anzVar+2]; Statik.setTo(meanPar, -1);
        int[] filter = new int[anzVar]; for (int j=0; j<anzVar; j++) filter[j] = 2+j;
        RAMModel lgcm = new RAMModel(symPar, symVal, asyPar, asyVal, meanPar, new double[anzVar+2], filter);
        
        RAMModel model = new RAMModel(new int[][]{{0,-1},{-1,-1}}, new double[][]{{50,0},{0,191.588}}, 
                new int[][]{{-1,-1},{-1,-1}}, new double[][]{{0,0},{1,0}}, new int[]{-1,-1}, new double[]{0,0}, new int[]{1});
        
        double[] starting = new double[]{50};
        
        model = lgcm;
        starting = new double[]{0.3, 74, 0, 21.993};
        
        RAMModel restricted = new RAMModel(model); restricted.fixParameter(0, 0.0);
        
        int trials = 10000, N = 75;
        
        int succ = 0;
        for (int i=0; i<trials; i++) {
            model.setParameter(starting);
            double[][] data = model.createData(N);
            model.setData(data);
            model.estimateML(starting);
            restricted.setData(data);
            restricted.estimateML(Statik.subvector(starting, 0));
            double lr = restricted.ll - model.ll;
            if (lr > Statik.FIVEPERCENTTHRESHOLD[0]) succ++;
        }
        double power = (double)succ / (double)trials;
        System.out.println("Power = "+power);
    }
    
    public static void bigHoferSimulation(boolean fixed, boolean typeOne, boolean fixCorForCov, boolean seperateCovarianceFromVariances, boolean twoDFTest, int trials) {
        int[] percentInterceptVariance = new int[]{20,40,60,80,100,200,500,1000,10000};
//        int[] percentInterceptVariance = new int[]{100};
        String header = ""; 
        for (int nIx = 0; nIx < 2; nIx++) 
            for (int startIx = 0; startIx < 2; startIx++) 
                for (int pICix = 0; pICix < percentInterceptVariance.length; pICix++) 
                    header += "per_var_I = "+percentInterceptVariance[pICix]+", N = "+(nIx==0?"Wald":"LR")+", var = "+(startIx==0?"Y":"X")+"\t";
        
        double[][] hoferTable = Statik.loadMatrix("HoferTableAll.txt", '\t');
        int anzStudy = hoferTable.length;
        double[][] erg = new double[2*anzStudy][percentInterceptVariance.length*2*2];
        long starttime = System.nanoTime();
        for (int studyNr = 0; studyNr < hoferTable.length; studyNr++) {
//        for (int studyNr = 13; studyNr < 14; studyNr++) {
            System.out.println("\r\nStarting study "+studyNr);
            double[] study = hoferTable[studyNr];
            int anzVar = (int)study[1];
            double[][] struct, covVal; int[][] covPar;
            if (!seperateCovarianceFromVariances) {
                struct = new double[anzVar][2]; 
                for (int i=0; i<anzVar; i++) {struct[i][0] = 1; struct[i][1] = study[2+i];}
                covPar = (fixed?new int[][]{{-1,-1},{-1,0}}:new int[][]{{3,2},{2,0}}); 
                covVal = new double[2][2];
            } else {
                struct = new double[anzVar][3]; 
                for (int i=0; i<anzVar; i++) {struct[i][0] = 1; struct[i][1] = study[2+i]; struct[i][2] = 1 + study[2+i];}
                covPar = (fixed?new int[][]{{-1,-1,-1},{-1,0,-1},{-1,-1,-1}}:new int[][]{{3,-1,-1},{-1,0,-1},{-1,-1,2}}); 
                covVal = new double[3][3];
            }
            int[] meanPar = new int[covVal.length]; double[] meanVal = new double[covVal.length]; 
            for (int i=0; i<covVal.length; i++) {meanPar[i] = -1; meanVal[i] = 0;}
            int errPar = (fixed?-1:1);
            LinearModel model = new LinearModel(struct, covPar, covVal, meanPar, meanVal, errPar, 0.0);
            LinearModel restricted = model.copy(); 
            if (twoDFTest) restricted.fixParameter(2, 0.0); 
            restricted.fixParameter(0, 0.0);
            int[][] anzPers = new int[][]{{(int)study[7],(int)study[9]},{(int)study[8],(int)study[10]}};
            for (int nIx = 0; nIx < anzPers.length; nIx++) 
                for (int varIx = 0; varIx < 2; varIx++)
                    for (int pICix = 0; pICix < percentInterceptVariance.length; pICix++) {
//                    for (int pICix = 2; pICix < 3; pICix++) {
                double orgVarI = (varIx==0?study[11]:study[13]);
                orgVarI = (double)orgVarI*percentInterceptVariance[pICix] / (double)100;
                double cov = study[(varIx==0?15:20)];
                if (fixCorForCov) cov *= Math.sqrt((double)percentInterceptVariance[pICix] / (double)100);
                double vI = orgVarI - (seperateCovarianceFromVariances?cov:0.0);
                double vS = (typeOne?0.0:study[(varIx==0?12:14)]) - (seperateCovarianceFromVariances?cov:0.0); 
                double testVS = 0.0 - (seperateCovarianceFromVariances && !twoDFTest?cov:0.0);
                double err = study[(varIx==0?21:22)];
                int anzPer = anzPers[varIx][nIx];
                
                if (seperateCovarianceFromVariances) {
                    model.covVal[2][2] = restricted.covVal[2][2] = cov;
                    if (twoDFTest) restricted.covVal[2][2] = 0.0;
                }
                else {
                    model.covVal[0][1] = model.covVal[1][0] = restricted.covVal[0][1] = restricted.covVal[1][0] = cov;
                    if (twoDFTest) restricted.covVal[0][1] = restricted.covVal[1][0] = 0.0;
                }
                model.covVal[1][1] = vS; restricted.covVal[1][1] = testVS;
                for (int j=0; j<model.anzVar; j++) model.errVal[j] = restricted.errVal[j] = err;
                model.covVal[0][0] = restricted.covVal[0][0] = vI;
                boolean startIsPositiveDefinite = Statik.isPositiveDefinite(model.covVal);
                if (!startIsPositiveDefinite)
                    System.out.println("Model not positive definite with "+percentInterceptVariance[pICix]);
                double[] starting = model.getParameter();
                model.evaluateMuAndSigma(starting);
//                model.setDataDistribution(model.sigma, model.mu, 1);
//                restricted.setDataDistribution(model.sigma, model.mu, 1);
//                double[] theoryEst = restricted.estimateML(Statik.subvector(starting, 0));
//                double chi = restricted.getMinusTwoLogLikelihood() - model.getMinusTwoLogLikelihood();
//                System.out.println(vI+"\t"+chi);
//                double[][] delta = Statik.subtract(model.sigma, restricted.sigma);
//                System.out.println(Statik.matrixToString(delta,7));
//                double lnRes = Math.log(Statik.determinant(restricted.sigma));
//                double lnFul = Math.log(Statik.determinant(model.sigma));
//                double tr = Statik.trace(Statik.multiply(Statik.invert(restricted.sigma), delta));
//                System.out.println("ln(|Res|) = "+lnRes+", ln(|Ful|) = "+lnFul+", trace = "+tr+", sum = "+(lnRes-lnFul+tr));
                
                int succLR = 0, succWald = 0, totalLr = 0, totalWald = 0;
                for (int i=0; (startIsPositiveDefinite && i<trials); i++) {
                    try {
                        if (seperateCovarianceFromVariances) {
                            model.covVal[2][2] = restricted.covVal[2][2] = cov;
                            if (twoDFTest) restricted.covVal[2][2] = 0.0;
                        }
                        else {
                            model.covVal[0][1] = model.covVal[1][0] = restricted.covVal[0][1] = restricted.covVal[1][0] = cov;
                            if (twoDFTest) restricted.covVal[0][1] = restricted.covVal[1][0] = 0.0;
                        }
                        model.covVal[1][1] = vS; restricted.covVal[1][1] = testVS;
                        for (int j=0; j<model.anzVar; j++) model.errVal[j] = restricted.errVal[j] = err;
                        model.covVal[0][0] = restricted.covVal[0][0] = vI; 
                        starting = model.getParameter();
                        model.evaluateMuAndSigma(starting);
                        double[][] data = Model.createData(anzPer, model.mu, model.sigma, model.getRandom());
//                        double[][] data = Model.createData(1000000, model.mu, model.sigma, model.getRandom());
//                        double[] means = Statik.meanVector(data);
//                        for (int j=0; j<data.length; j++) for (int k=0; k<anzVar; k++) data[j][k] -= means[k];
                        model.setData(data);
                        double[] fullEst = model.estimateML(starting,0.00001);
                        double[] stdv = model.getParameterSTDV();
                        if (!Double.isNaN(stdv[0]) && stdv[0] != 0.0) {
                            totalWald++;
//                            if ((model.position[0]-testVS)/stdv[0] > 1.644) succWald++;
                            if (Math.abs(model.position[0]-testVS)/stdv[0] > 1.960) succWald++;
                        }
                        restricted.setData(data);
                        double[] resStart = (twoDFTest?Statik.subvector(starting, 2):starting);
                        resStart = Statik.subvector(resStart, 0);
                        double[] resEst = restricted.estimateML(resStart,0.00001);
                        double lr = restricted.getMinusTwoLogLikelihood() - model.getMinusTwoLogLikelihood();
                        if (!Double.isNaN(lr)) {
                            if (lr > Statik.FIVEPERCENTTHRESHOLD[ (twoDFTest?1:0) ]) succLR++;
                            totalLr++;
                        }
                    } catch (Exception e) {}
                }
                double powerLR = (totalLr==0.0?Model.MISSING:(double)succLR / (double)totalLr);
                double powerWald = (totalWald==0.0?Model.MISSING:(double)succWald / (double)totalWald);
                erg[studyNr][pICix+nIx*percentInterceptVariance.length+varIx*2*percentInterceptVariance.length] = powerLR;
                erg[studyNr+anzStudy][pICix+nIx*percentInterceptVariance.length+varIx*2*percentInterceptVariance.length] = powerWald;
                Statik.writeMatrix(erg, "hoferBigSimulationOut.txt", '\t', header);
            }
        }
        long time = System.nanoTime() - starttime;
        System.out.println("\r\nTotal Time = "+time/1000000000+" s.");
    }
    
    public static void rebeccaWeast() {
        double[][] data = Statik.loadMatrix("RebeccaWeast.txt", '\t', false, "MISS");
        int[] all = new int[data.length]; for (int i=0; i<all.length; i++) all[i] = i;
        double[][] propVsSpeedNotoolLeft = Statik.submatrix(data, all, new int[]{0,2});
        double[][] function = Statik.gaussianInterpolation(propVsSpeedNotoolLeft, 0.1, 100);
        System.out.println(Statik.matrixToString(function));
    }
    
    public static void floErgodicSubspace(double[][] covBtwn, double[][] covWthn) {
        System.out.println("Ergodic Subspace");
        int n = covBtwn.length;
        double[][] sum = Statik.add(covBtwn, covWthn);
        double[] eval = new double[n];
        double[][] evec = Statik.eigenvectorsOfSymmetrical(sum, eval, false);
        Statik.sortMatrixRowsByVectorAbsolute(eval, evec);
        System.out.println("Eval = "+Statik.matrixToString(eval));
        System.out.println("Evec = "+Statik.matrixToString(evec));
        double[][] diffProj = new double[n][n];
        for (int i=0; i<n; i++) {
            double[][] add = Statik.multiply(eval[n-1-i], Statik.multiply(evec[n-1-i], evec[n-1-i], true));
            diffProj = Statik.add(diffProj, add);
            System.out.println((i+1)+". component = "+Statik.matrixToString(evec[n-1-i]));
            System.out.println((i+1)+" components: \r\n"+Statik.matrixToString(diffProj));
        }
        eval = ergodicSubspaceAnalysisByCSP(covBtwn, covWthn);
        System.out.println("CSP ESA: "+Statik.matrixToString(eval));
        System.out.println("KL: "+Statik.kullbackLeiblerNormal(covBtwn, covWthn));
    }

    public static double[][][] createNonergodicData(int anzPer, int anzT, double[][] populationBetween, double[][] populationWithin) {
        int anzVar = populationBetween.length;
        Random rand = new Random();
        double[][][] erg = new double[anzPer][anzT][anzVar];
        double[][] pData = Model.createData(anzPer, new double[anzVar], populationBetween, rand);
        for (int i=0; i<anzPer; i++) {
            double[][] wData = Model.createData(anzT, new double[anzVar], populationWithin, rand);
            for (int j=0; j<anzT; j++) for (int k=0; k<anzVar; k++) erg[i][j][k] = pData[i][k] + wData[j][k];
        }
        return erg;
    }
    
    public static double[] ergodicSubspaceAnalysisBySum(double[][] btwCov, double[][] wthCov) {
        int anzVar = btwCov.length; 
        btwCov = Statik.correlationFromCovariance(btwCov);
        wthCov = Statik.correlationFromCovariance(wthCov);
        double[][] sum = Statik.add(btwCov, wthCov);
        double[] eVal= new double[anzVar];
        double[][] eVec = Statik.eigenvectorsOfSymmetrical(sum, eVal, true);
        
//        System.out.println("Between full = \r\n"+Statik.matrixToString(btwCov));
//        System.out.println("Within full = \r\n"+Statik.matrixToString(wthCov));
        double btwTrace = Statik.trace(btwCov), wthTrace = Statik.trace(wthCov);
        double[][] btwPart = new double[anzVar][anzVar], wthPart = new double[anzVar][anzVar];
        for (int i=0; i<anzVar; i++) {
            double sumExplained = Statik.multiply(eVec[i], sum, eVec[i]); 
            double btwExplained = Statik.multiply(eVec[i], btwCov, eVec[i]);
            double wthExplained = Statik.multiply(eVec[i], wthCov, eVec[i]);
            
//            System.out.println("Component "+i+" explains between "+btwExplained+" ("+(btwExplained/btwTrace)+"), within "
//                                                                  +wthExplained+" ("+(wthExplained/wthTrace)+"), sum "
//                                                                  +sumExplained+" ("+(sumExplained/(btwTrace+wthTrace))+")");
            
            double[][] btwEx = Statik.multiply(btwExplained, Statik.multiply(eVec[i],eVec[i],true));
            double[][] wthEx = Statik.multiply(wthExplained, Statik.multiply(eVec[i],eVec[i],true));
            btwPart = Statik.add(btwPart, btwEx);
            wthPart = Statik.add(wthPart, wthEx);
//            System.out.println("Between explained already = \r\n"+Statik.matrixToString(btwPart));
//            System.out.println("Within explained already = \r\n"+Statik.matrixToString(wthPart));
        }
        return eVal;
    }
    
    public static double[] ergodicSubspaceAnalysisByCSP(double[][] btwCov, double[][] wthCov) {
        int anzVar = btwCov.length; 
        btwCov = Statik.correlationFromCovariance(btwCov);
        wthCov = Statik.correlationFromCovariance(wthCov);
        
        double[][] q = cspTransformationMatrix(btwCov, wthCov, 0.0);
//        System.out.println("CSP transformation = \r\n"+Statik.matrixToString(q));

        double[][] btwTrans = Statik.multiply(q,Statik.multiply(btwCov,Statik.transpose(q)));
        double[][] wthTrans = Statik.multiply(q,Statik.multiply(wthCov,Statik.transpose(q)));
        
        double[] erg = new double[anzVar];
        for (int i=0; i<anzVar; i++) {
            erg[i] = (btwTrans[i][i] - wthTrans[i][i])/2.0;
        }
        return erg;
    }
    
    public static void testESAScree() {
        int anzPer = 100, anzT = 100, anzVar = 9, anzBins = 500;
        int range = 10;
        
        Random rand = new Random();
        int trials = 50000;
        double ergodicity = 1.0;
        double indexSum = 0, indexSqrSum = 0, valueSum = 0, valueSqrSum = 0, maxValue = 16;
        double[] sum = new double[anzVar];
        int[] bin = new int[anzBins];
        for (int i=0; i<trials; i++) {
            double[][] popWthnPure = Statik.sampleFromWishart(anzVar*2, anzVar, rand);
            double[][] popBtwnPure = Statik.sampleFromWishart(anzVar*2, anzVar, rand);
            double[][] popErgoPure = Statik.sampleFromWishart(anzVar*2, anzVar, rand);
            
            double[][] popBtwn = Statik.add(Statik.multiply(ergodicity,popErgoPure),Statik.multiply(1-ergodicity,popBtwnPure));
            double[][] popWthn = Statik.add(Statik.multiply(ergodicity,popErgoPure),Statik.multiply(1-ergodicity,popWthnPure));
            
            double[][] empBtwn = Statik.sampleSampleCovariance(popBtwn, anzPer, rand);
            double[][] empWthn = Statik.sampleSampleCovariance(popWthn, anzPer, rand);
            
            double[] eval = ergodicSubspaceAnalysisByCSP(empBtwn, empWthn);
            sum = Statik.add(sum, eval);
            double kl = Statik.kullbackLeiblerNormal(empBtwn, empWthn);
            double index = kl;
//            double index = eval[0] - eval[anzVar - 1];
            indexSum += index;
            indexSqrSum += index*index;
            int ix = (int)Math.round(index / maxValue * anzBins);
            for (int j=0; j<range; j++) if (ix-range/2+j >= 0 && ix-range/2+j < anzBins) bin[ix-range/2+j]++;
            for (int j=0; j<anzVar; j++) valueSum += eval[j];
            for (int j=0; j<anzVar; j++) valueSqrSum += eval[j] * eval[j];
                    
        }
        sum = Statik.multiply(1.0/(double)trials, sum);
//            System.out.println("\r\n"+Statik.matrixToString(sum));
        double mean = indexSum / trials; 
        double stdv = Math.sqrt(indexSqrSum / trials - mean*mean);
        double meanV = valueSum / (trials * anzVar);
        double stdvV = Math.sqrt((valueSqrSum / (trials * anzVar)) - meanV*meanV);
        System.out.println(ergodicity+"\t"+mean+"\t"+stdv);
        System.out.println("Value \t"+meanV+"\t"+stdvV);
        System.out.println(Statik.matrixToString(bin));
    }
    
    public static void testErgodicSubspaceAnalysis() {
        int anzPer = 100, anzT = 100, anzVar = 9;

        Random rand = new Random();
        double[][] popWthn = Statik.sampleFromWishart(anzVar*2, anzVar, rand);
//        double[][] popBtwn = Statik.sampleFromWishart(anzVar*2, anzVar, rand);
        double[][] popBtwn = Statik.copy(popWthn);
        
        double[][][] data = createNonergodicData(anzPer, anzT, popBtwn, popWthn);
        double[] loc = new double[anzVar];
        
        double[] btwMean = new double[anzVar]; double[][] btwCov = new double[anzVar][anzVar];
        for (int i=0; i<anzPer; i++) for (int j=0; j<anzT; j++)
            for (int k=0; k<anzVar; k++) {btwMean[k] += data[i][j][k]; for (int k2=0; k2<anzVar; k2++) btwCov[k][k2] += data[i][j][k]*data[i][j][k2];}
        for (int k=0; k<anzVar; k++) btwMean[k] /= (anzPer*anzT);
        for (int k=0; k<anzVar; k++) for (int k2=0; k2<anzVar; k2++) btwCov[k][k2] = (btwCov[k][k2]/(anzPer*anzT) - btwMean[k]* btwMean[k2]);
        
        double[] wthMean = new double[anzVar]; double[][] wthCov = new double[anzVar][anzVar];
        for (int i=0; i<anzPer; i++) {
            Statik.setTo(loc, 0.0);
            for (int j=0; j<anzT; j++) loc = Statik.add(loc, data[i][j]);
            for (int k=0; k<anzVar; k++) loc[k] /= anzT;
            for (int j=0; j<anzT; j++) for (int k=0; k<anzVar; k++) {
                    wthMean[k] += (data[i][j][k]-loc[k]); 
                    for (int k2=0; k2<anzVar; k2++) wthCov[k][k2] += (data[i][j][k]-loc[k])*(data[i][j][k2]-loc[k]);
            }
        }
        for (int k=0; k<anzVar; k++) wthMean[k] /= (anzPer*anzT);
        for (int k=0; k<anzVar; k++) for (int k2=0; k2<anzVar; k2++) wthCov[k][k2] = (wthCov[k][k2]/(anzPer*anzT) - wthMean[k]* wthMean[k2]);
        
        btwCov = Statik.subtract(btwCov, wthCov);
        
        System.out.println("Population Within = \r\n"+Statik.matrixToString(popWthn));
        System.out.println("Actual Within = \r\n"+Statik.matrixToString(wthCov));
        System.out.println("Population Between = \r\n"+Statik.matrixToString(popBtwn));
        System.out.println("Actual Between = \r\n"+Statik.matrixToString(btwCov));
        
        floErgodicSubspace(btwCov, wthCov);
        double[][] q = cspTransformationMatrix(btwCov, wthCov, 0.1);
        double[][] check = Statik.multiply(Statik.multiply(q, wthCov), Statik.transpose(q));
        System.out.println(Statik.matrixToString(check));
        ergodicSubspaceAnalysisBySum(btwCov, wthCov);
    }
    
    /**
     * Computes a CSP based on the covariance matrices of the two classes and returns the transformation matrix. If minimalVariance is above zero, a pca is 
     * applied first and all vectors with eigenvalues below minimalVariance are eliminated. 
     *  
     * @param covGroup1
     * @param covGroup2
     * @param minimalVariance
     * @return
     */
    public static double[][] cspTransformationMatrix(double[][] covGroup1, double[][] covGroup2, double minimalVariance) {
        int dim = covGroup1.length;
        
        double[][] covTotal = Statik.add(covGroup1, covGroup2);

//        System.out.println("Total covariance matrix = \r\n"+Statik.matrixToString(covTotal));
//        System.out.println("Total covariance matrix [Maple] = \r\n"+Statik.matrixToMapleString(covTotal));
        
        double[] ev = new double[dim];
        double[][] q = Statik.eigenvectorsOfSymmetrical(covTotal, ev, true);
        
//        System.out.println("Eigenvalues        = "+Statik.matrixToString(ev));
//        System.out.println("Eigenvector matrix = \r\n"+Statik.matrixToString(q));
        
        int dim2 = 0; for (int i=0; i<dim; i++) if (Math.abs(ev[i]) >= minimalVariance) dim2++;
        if (dim2==0) return new double[0][];
        double[][] subQ = new double[dim2][dim];
        int p = 0;
        for (int i=0; i<dim; i++) if (Math.abs(ev[i]) >= minimalVariance) {for (int j=0; j<dim; j++) subQ[p][j] = q[i][j] / Math.sqrt(ev[i]); p++;} 
        double[][] transCov1 = new double[dim2][dim2];
        Statik.multiply(Statik.multiply(subQ,covGroup1),Statik.transpose(subQ), transCov1);
        
        double[] ev2 = new double[dim2];
        double[][] q2 = Statik.eigenvectorsOfSymmetrical(transCov1, ev2, true);
        
//        System.out.println("Submapping             = \r\n "+Statik.matrixToString(subQ));
//        System.out.println("Eigenvalues of group 1 = "+Statik.matrixToString(ev2));
//        System.out.println("Eigenvector group 1    = \r\n"+Statik.matrixToString(q2));

        double[][] erg = Statik.multiply(q2,subQ);

//        System.out.println("Final mapping        = \r\n "+Statik.matrixToString(erg));
        
        return erg;
    }
    
    public static void floPreferenceClustering() {
        double[][] data = Statik.loadMatrix(new File("FloData\\cogito_preferences.dat"), '\t', false, -1, "", 0);
        int anzPer = data.length;
        int maxCluster = 5;
        double[][] erg = new double[anzPer][maxCluster];
        double[] entropies = new double[maxCluster];
        
        LloydsAlgorithm cluster = new LloydsAlgorithm(data);
        cluster.setMethod(methodType.NORMAL);
        for (int i=1; i<=maxCluster; i++) {
            int[] minCl = null, cl = null;
            double minEntropy = Double.MAX_VALUE;
            for (int j=0; j<100; j++) {
                try {
                    cl = cluster.cluster(i, null);
                    double entropy = cluster.getEntropy();
                    if (entropy < minEntropy) {minEntropy = entropy; minCl = cl;}
                } catch (Exception e) {}
            }
            for (int j=0; j<anzPer; j++) erg[j][i-1] = minCl[j];
            entropies[i-1] += minEntropy; 
        }
        System.out.println("Cluster Entropies: "+Statik.matrixToString(entropies));
        System.out.println("Cluster results: \r\n"+Statik.matrixToString(erg));
    }
    
    public static void danDissSim1() {
        File file = new File("testmodels\\testmodelBattery\\simpleRegression.xml");
        boolean ex = file.exists();
        OnyxModel model = OnyxModel.load(file);
        double trueEffect = 1.0;
        int trials = 100000;
        int anzPer = 100;
        double[][] data = new double[anzPer][2];
        int meanDiff = 100;
        int stdv = 1;
        OnyxModel restricted = model.copy(); restricted.fixParameter("Weight_TO_Speed", trueEffect);
        model.killModelRun();
        
        int total = 0, succ = 0;
        for (int tr=0; tr<trials; tr++) {
            for (int i=0; i<anzPer; i++) data[i][0] = model.rand.nextGaussian()*stdv;
            for (int i=0; i<anzPer/2; i++) data[i][1] = model.rand.nextGaussian()*stdv + trueEffect*data[i][0];
            for (int i=anzPer/2; i<anzPer; i++) data[i][1] = model.rand.nextGaussian()*stdv + meanDiff + trueEffect*data[i][0];
            model.setData(data);
            model.estimateML();
            restricted.setData(data);
            restricted.estimateML();
            if (!Double.isNaN(model.ll) && !Double.isNaN(restricted.ll)) {
                total++;
                double lr = restricted.ll-model.ll; 
                succ += (lr > Statik.FIVEPERCENTTHRESHOLD[0]?1:0);
            }
        }
        System.out.println("MeanDiff = "+meanDiff+", stdv = "+stdv+", Power = "+(succ/(double)total));        
    }
    
    public static void sinySimulation() {
        int nPer = 100, nClass = 3;
        Random random = new Random();
        int[] nOffencesInClass = new int[]{3,5,7};
        
        // simulation
        int[] classMembership = new int[nPer];
        for (int i=0; i<nPer; i++) classMembership[i] = random.nextInt(nClass);
        
        double[] timeOut = new double[nPer];
        for (int i=0; i<nPer; i++) timeOut[i] = random.nextDouble();  // generates random numbers from 0.0 to 1.0
        
        int[] offences = new int[nPer];
        for (int i=0; i<nPer; i++) offences[i] = (int)Math.round(nOffencesInClass[classMembership[i]] * timeOut[i] + random.nextInt(2)-1 );
        
        // Analysis part 1: regression from timeOut to Offences.
        double[][] offencesAndTimeout = new double[nPer][]; for (int i=0; i<nPer; i++) offencesAndTimeout[i] = new double[]{offences[i],timeOut[i]};
        double[] mean = Statik.meanVector(offencesAndTimeout);
        double[][] cov = Statik.covarianceMatrix(offencesAndTimeout);
        double regressionWeight = cov[0][1] / cov[1][1];
        
        double[][] latentOffences = new double[nPer][1];
        for (int i=0; i<nPer; i++) latentOffences[i][0] = offences[i] - regressionWeight * (timeOut[i]-mean[0]);
        
        // Analysis part 3: KNN on the residuals
        LloydsAlgorithm kMeans = new LloydsAlgorithm(latentOffences);
        kMeans.method = LloydsAlgorithm.methodType.MEAN;
        int[] result = kMeans.cluster(nClass, null);
        
        // Output organisation: The following creates a hitTable with the rows being the correct class, the columns being the fitted class.
        int[][] hitTable = new int[nClass][nClass];
        for (int i=0; i<nPer; i++) hitTable[classMembership[i]][result[i]]++;
        for (int i=0; i<nClass; i++) {
            int bestRow = -1; int maxNumber = -1;
            for (int j=i; j<nClass; j++) if (hitTable[j][i] > maxNumber) {maxNumber = hitTable[j][i]; bestRow = j;}
            int[] t = hitTable[i]; hitTable[i] = hitTable[bestRow]; hitTable[bestRow] = t;
        }
        
        // Output
        int nCorrect = 0; for (int i=0; i<nClass; i++) nCorrect += hitTable[i][i];
        
        System.out.println(Statik.matrixToString(hitTable)+"\r\n\r\nratio correct = "+(nCorrect / (double)nPer));
        
    }
    
    public static void covMinimumSimulation() {
        double rho = 20;
        LinearModel model = new LinearModel(new double[][]{{1,1},{1,2},{1,3},{1,4}}, new int[][]{{-1,1},{1,2}},new double[][]{{100,rho},{rho,0.125}},
                new int[]{-1,-1},new double[]{0,0},-1,10);
        for (rho = -20; rho < 20; rho+=1) {
            LinearModel full = new LinearModel(new double[][]{{1,1},{1,2},{1,3},{1,4}}, new int[][]{{-1,1},{1,2}},new double[][]{{100,rho},{rho,0.125}},
                    new int[]{-1,-1},new double[]{0,0},-1,10);
            LinearModel restricted = new LinearModel(new double[][]{{1,1},{1,2},{1,3},{1,4}}, new int[][]{{-1,1},{1,2}},new double[][]{{100,0},{0,0}},
                    new int[]{-1,-1},new double[]{0,0},-1,10);
            full.evaluateMuAndSigma();
            restricted.setDataDistribution(full.sigma, full.mu,1);
            restricted.estimateML();
            double chisq = restricted.ll - full.getMinusTwoLogLikelihood();
            System.out.println(rho+"\t"+chisq);
        }
    }

    public static void testingMDSProjection() {
        double[][] distances = new double[][]{{0,2,2,2},{2,0,2,2},{2,2,0,2},{2,2,2,0}};
        int dim = distances.length, pointsOut = 2;
        double[][] fullCoordinates = DataPreprocessing.multiDimensionalScaling(distances);
        
        double[][] estDist = new double[dim][dim]; for (int i=0; i<dim; i++) for (int j=0; j<=i; j++) estDist[i][j] = estDist[j][i] = Statik.distance(fullCoordinates[i], fullCoordinates[j]);
        
        double[][] q = new double[dim][dim], r = new double[dim][dim];
        Statik.qrDecomposition(Statik.transpose(fullCoordinates), q, r);
        for (int i=dim-pointsOut-1; i<dim; i++) for (int j=0; j<dim; j++) r[i][j] = 0;
        double[][] reducedCoordinates = Statik.transpose(r);
        
        double[][] cov = new double[dim][dim]; double[] mean = new double[dim];
        Statik.covarianceMatrixAndMeans(reducedCoordinates, mean, cov);
        double[][] evec = Statik.identityMatrix(dim); double[] eval = new double[dim];
        Statik.eigenvalues(cov, 0.0000001, eval, evec);
        double[][] erg = Statik.multiply(evec, reducedCoordinates);
        
        double[][] subdist = new double[dim-pointsOut][dim-pointsOut];
        for (int i=0; i<dim-pointsOut; i++) for (int j=0; j<dim-pointsOut; j++) subdist[i][j] = distances[i][j];
        double[][] subCoordinates = DataPreprocessing.multiDimensionalScaling(subdist);
        
        System.out.println(Statik.matrixToString(fullCoordinates));
        System.out.println(Statik.matrixToString(erg));
        System.out.println("\r\n"+Statik.matrixToString(subCoordinates));
    }
    
    public static void simulateIVBias() {
        Random rand = new Random();
        double mu = 1;
        int N = 10000;    // per Group
        double[] mean = new double[N];
        double[] stdv = new double[N];
        int trials = 10000;
        for (int i=0; i<trials; i++) {
            double rho = 0; double correct = 0;
            for (int j=1; j<=N; j++) {
                double x = rand.nextGaussian() + mu;
                double y = rand.nextGaussian() - mu;
                if (j>1) {if (x>rho) correct++; if (y<rho) correct++;}
                rho = ((j-1)*rho + (x+y)) / ((double)j);
                double succ = correct / ((double)(2*(j-1)));
                mean[j-1] += succ; stdv[j-1] += succ*succ;
            }
        }
        for (int i=0; i<N; i++) {mean[i] /= (double)trials; stdv[i] = Math.sqrt(stdv[i]/(double)trials - mean[i]*mean[i]);}
        for (int i=0; i<N; i++) System.out.println(mean[i]+"\t"+stdv[i]);
    }
    
    public static void muellerEtAlScript() {
        int[][] n = new int[][]{{19,3,1},{9,3,2},{13,6,3}};    // 19 with 2-2, 9 with 2-3, 12+1 extra with 4-5 (child-adult)
        int nTot = n[0][0]+n[1][0]+n[2][0];
        double varianceFixInPerson = 0.1;                      // percentage of variance constant wthn person and time point, with different signs child/adult
        double varianceFixInTime = 0.2;
        int trials = 1000;
        Random rand = new Random(); 
        System.out.println("Trial\t#out\t#equal\t#changed\tmeanChange\tstdvChange\tmeanEqual\tstdvEqual\tmeanChanged\tstdvChanged");
        for (int i=0; i<trials; i++) {
            double[] fixPerson = new double[nTot]; for (int j=0; j<nTot; j++) fixPerson[j] = rand.nextGaussian()*Math.sqrt(varianceFixInPerson);
            double[][] data = new double[nTot][2];   // PSI before and after
            for (int time=0; time<2; time++){
                int counter = 0;
                for (int j=0; j<n.length; j++) {
                    for (int k=0; k<n[j][0]; k++) {
                        double fixTime = rand.nextGaussian()*Math.sqrt(varianceFixInTime);
                        double fix = fixTime + fixPerson[counter];
                        double sum = 0, sumsq = 0, max1 = Double.NEGATIVE_INFINITY, max2 = Double.NEGATIVE_INFINITY;
                        for (int l=0; l<n[j][1]; l++) {
                            double x = rand.nextDouble()*Math.sqrt(1-varianceFixInPerson-varianceFixInTime)+fix;
                            sum += x; sumsq += x*x; max1 = (max1>x?max1:x); 
                        }
                        for (int l=0; l<n[j][2]; l++) {
                            double x = rand.nextDouble()*Math.sqrt(1-varianceFixInPerson-varianceFixInTime)-fix;
                            sum += x; sumsq += x*x; max2 = (max2>x?max2:x); 
                        }
                        double nl = (double)(n[j][1] + n[j][2]);
                        double mean = sum /nl, stdv = Math.sqrt((sumsq / (nl-1)) - mean*mean*(nl/(nl-1)));
                        max1 = (max1-mean)/stdv; max2 = (max2-mean)/stdv;
                        if (time==0 && max1-max2 < 0.25) {                              // person is excluded; his explained parts are rerolled. 
                            k--; 
                            fixPerson[counter]= rand.nextGaussian()*Math.sqrt(varianceFixInPerson);
                        }
                        else data[counter++][time] = max1-max2;
//                        if (Double.isNaN(max1-max2)) 
//                            System.out.println("NaN occured");
                    }
                }
            }
            // analysis
            int countOut = 0, countChanged = 0, countEqual = 0; 
            double sumDiff = 0, sumSqDiff = 0, sumCh = 0, sumSqCh = 0, sumEq = 0,sumSqEq = 0;
            for (int j=0; j<nTot; j++) {
                if (data[j][1] > -0.25 && data[j][1] < 0.25) countOut++;
                if (data[j][1] < -0.25) {
                    countChanged++;
                    double diff = data[j][1] - data[j][0];
                    sumDiff += diff; sumSqDiff += diff*diff;
                    sumCh += data[j][1]; sumSqCh += data[j][1]*data[j][1];
                }
                if (data[j][1] > 0.25) {
                    countEqual++;
                    sumEq += data[j][1]; sumSqEq += data[j][1]*data[j][1];
                }
            }
            double nl = (double)countChanged;
            double meanDiff = sumDiff / nl, stdvDiff = Math.sqrt((sumSqDiff / (nl-1)) - meanDiff*meanDiff*(nl/(nl-1)));
            double meanCh = sumCh / nl, stdvCh = Math.sqrt((sumSqCh / (nl-1)) - meanCh*meanCh*(nl/(nl-1)));
            nl = (double)countEqual; double meanEq = sumEq / nl, stdvEq = Math.sqrt((sumSqEq / (nl-1)) - meanEq*meanEq*(nl/(nl-1)));
            if (Double.isNaN(meanDiff)) 
                System.out.println("NaN occurred.");
            System.out.println(i+"\t"+countOut+"\t"+countEqual+"\t"+countChanged+"\t"+meanDiff+"\t"+stdvDiff+"\t"+meanEq+"\t"+stdvEq+"\t"+meanCh+"\t"+stdvCh);
//            System.out.println(Statik.matrixToString(data));
        }
    }

    /**
     * Simulates N data with the covariance matrix and computes the average KL between covariance matrices of these data sets. 
     * If coviaranceList is not null, the is generated with different matrix for each participant from the list, and the average distance to this generating
     * matrix is returned. The covariance argument is ignored in this case. 
     * 
     * @param covariance
     * @param covarianceList
     * @param trials
     */
    public static void floSimulateAverageKLUnderNull(double[][] covariance, double[][][] covarianceList, int trials) {
        int K = covariance.length;
        int N = 100;
        int time = 100;
        Random rand = new Random();
        
        if (covarianceList != null) N = covarianceList.length;
        double[][] chol = null; if (covariance != null) chol = Statik.choleskyDecompose(covariance);
        double[][][] simCov = new double[N][K][K];
        double[][] simDist = new double[N][N];
        double[] res = new double[trials];
        for (int trial = 0; trial < trials; trial++) {
            for (int i=0; i<N; i++) {
                Statik.setToZero(simCov[i]);
                if (covarianceList != null) chol = Statik.choleskyDecompose(covarianceList[i]);
                for (int j=0; j<time; j++) {
                    double[] v = new double[K]; for (int k=0; k<K; k++) v[k] = rand.nextGaussian();
                    double[] v2 = Statik.multiply(chol, v);
                    for (int k1=0; k1<K; k1++) for (int k2=0; k2<=k1; k2++) simCov[i][k1][k2] += v2[k1]*v2[k2];
                }
                for (int k1=0; k1<K; k1++) for (int k2=0; k2<k1; k2++) simCov[i][k2][k1] = simCov[i][k1][k2];
                simCov[i] = Statik.correlationFromCovariance(simCov[i]);
            }
            double avgKL = 0;
            if (covarianceList == null) {
                for (int i=0; i<N; i++) for (int j=0; j<i; j++) simDist[i][j] = simDist[j][i] = Statik.kullbackLeiblerNormal(simCov[i], simCov[j]);
                for (int i=0; i<N; i++) for (int j=0; j<N; j++) avgKL += simDist[i][j];
                avgKL /= (double)(N*N);
            } else {
                for (int i=0; i<N; i++) avgKL += Statik.kullbackLeiblerNormal(simCov[i], covarianceList[i]);
                avgKL /= (double)N;
            }
            res[trial] = avgKL;
        }
        double mean = Statik.mean(res);
        double stdv = Statik.stdv(res);
//        for (int i=0; i<trials; i++) System.out.println(res[i]);
        System.out.println("Average = "+mean+" +- "+stdv);
    }
  
    public static void adaptiveDyadicTestingSimulation() {
        int anzPer = 20;
        double skillSTDV = 2.66;
        int anzMatches = 4000;
        PairingStrategy[] strategies = new PairingStrategy[]{PairingStrategy.random, PairingStrategy.swiss, 
                                    PairingStrategy.localEntropy, PairingStrategy.globalEntropyHessian
                                    , PairingStrategy.globalEntropy};
//        double maxLagPlayerGlobal = Double.POSITIVE_INFINITY, maxLagPlayerLocalAndAdHoc = Double.POSITIVE_INFINITY;
//        double maxLagGamesGlobal = Double.POSITIVE_INFINITY, maxLagGamesLocalAndAdHoc = Double.POSITIVE_INFINITY;
        double maxLagPlayerGlobal = Double.POSITIVE_INFINITY, maxLagPlayerLocalAndAdHoc = 10;
        double maxLagGamesGlobal = Double.POSITIVE_INFINITY, maxLagGamesLocalAndAdHoc = 5;
        
        
        long fullSeed = (new Random()).nextLong(); // 104723094807L;
//        fullSeed = -1267366352633949171l;
        System.out.println("Seed = "+fullSeed);
        Random rand = new Random(fullSeed);
        long[][] seed = new long[anzPer][anzPer];
        Random[][] gen = new Random[anzPer][anzPer];
        for (int i=0; i<anzPer; i++) for (int j=i+1; j<anzPer; j++) seed[i][j] = rand.nextLong(); 
        double[] skill = new double[anzPer];
        for (int i=0; i<anzPer; i++) skill[i] = rand.nextGaussian()*skillSTDV;

        double[][] points = new double[anzPer][anzPer]; 
        DyadicIRTModel model = new DyadicIRTModel(points);
        model.setRandomSeed(rand.nextLong());
        
        long[] times = new long[strategies.length];
        double[][][] plot = new double[2][anzMatches][strategies.length];
        for (int s=0; s<=strategies.length-1; s++) {
            System.out.print("Starting "+strategies[s]);
            for (int i=0; i<anzPer; i++) for (int j=0; j<anzPer; j++) gen[i][j] = new Random(seed[i][j]);
            Statik.setToZero(points);
            model.setData(points);
            model.setInitialPairingRounds(2);
            double[] pos = model.estimateML();
            double maxLagPlayer = (s>=3?maxLagPlayerGlobal:maxLagPlayerLocalAndAdHoc);
            double maxLagGames = (s>=3?maxLagGamesGlobal:maxLagGamesLocalAndAdHoc);
            for (int step=0; step < anzMatches; step++) {
                if (step % 50 == 0) System.out.print(".");
                long startTime = System.nanoTime();
                int[] pair = model.suggestNextPairing(strategies[s], maxLagPlayer, maxLagGames);
                times[s] += System.nanoTime() - startTime;
                if (pair[0] > pair[1]) {int t = pair[0]; pair[0] = pair[1]; pair[1] = t;}
                double prob = Statik.sigmoid(skill[pair[0]] - skill[pair[1]]);
                if (gen[pair[0]][pair[1]].nextDouble() < prob) points[pair[0]][pair[1]]++; else points[pair[1]][pair[0]]++; 
                model.setData(points);
                pos = model.estimateML(pos);
                plot[0][step][s] = model.logDetHessian;
                plot[1][step][s] = model.distanceToEstimate(skill);
            }
            System.out.println(": "+plot[0][anzMatches-1][s]+", "+plot[1][anzMatches-1][s]+", "+(times[s]/1000000)+" ms");
        }
        System.out.println("Curves for information: \r\n"+Statik.matrixToString(plot[0]));
        System.out.println("Curves for precision  : \r\n"+Statik.matrixToString(plot[1]));
    }
  
    public static void testResultants() {
        QPolynomial[] dev = QPolynomial.getTwoByTwoLikelihoodDerivatives();
        
        QPolynomial check1 = dev[0].evaluate(new int[]{0,1,2,3,4,5}, new Qelement[]{Qelement.ONE, Qelement.ONE, new Qelement(-12185667,100000000), new Qelement(11637179,100000000), Qelement.TWO, Qelement.ONE});
        QPolynomial check2 = dev[0].evaluate(new int[]{0,1,5}, new Qelement[]{Qelement.ONE, Qelement.ONE, Qelement.ONE});
        System.out.println("check 1 = "+check1);
        System.out.println("check 2 = "+check2);
        
//        QPolynomial test1 = dev[0].evaluate(new int[]{2 ,3,4,5}, new Qelement[]{Qelement.TWO, Qelement.TWO, Qelement.TWO, Qelement.TWO});
//        QPolynomial test2 = dev[1].evaluate(new int[]{2,3,4,5}, new Qelement[]{Qelement.TWO, Qelement.TWO, Qelement.TWO, Qelement.TWO});
        QPolynomial test1 = dev[0].evaluate(new int[]{2,3,4,5}, new Qelement[]{new Qelement(11), new Qelement(3), new Qelement(7), new Qelement(11)});
        QPolynomial test2 = dev[1].evaluate(new int[]{2,3,4,5}, new Qelement[]{new Qelement(11), new Qelement(3), new Qelement(7), new Qelement(11)});
        QPolynomial p1seq1= dev[0].evaluate(new int[]{5}, new Qelement[]{Qelement.ONE});
        QPolynomial p2seq1= dev[1].evaluate(new int[]{5}, new Qelement[]{Qelement.ONE});
        
        System.out.println("first test: "+test1);
        System.out.println("secon test: "+test2);
        System.out.println("p1 s=1: "+p1seq1);
        System.out.println("p2 s=1: "+p2seq1);
        
//        QPolynomial p1 = new QPolynomial("3*X0^5 + 2*X0^4*X1 + 3*X0^3*X1^2 + 2*X0^2*X1^3 + 1*X0*X1^4 + X1^5 + 7*X0^4 + 6*X0^3*X1 + 5*X0^2*X1^2 + 4*X0*X1^3 + 2*X1");
//        QPolynomial p2 = new QPolynomial("11*X0^3 + 4*X0^2*X1 + 2*X0*X1^2 + 1*X1^3 + 3*X0^2 + 5*X0*X1 + 7*X1^2");
        QPolynomial p1 = test1;
        QPolynomial p2 = test2;
//        QPolynomial res = p1.computeSubresultantPRS(p2, 0, QPolynomial.ONE);
        long time = System.nanoTime();
        FastPolynomial res = p1.resultant(p2, 1);
        System.out.println("Resultant computation: "+(System.nanoTime()-time)/1000000+" ms.");
        String ress = res.toString();
//        Statik.writeToFile("out.txt", ress);
        System.out.println("resultant = "+ress);
        System.out.println("resultant at (1,1) = "+((QPolynomial)res.member.elementAt(0)).evaluate(new int[]{0,1},new Qelement[]{Qelement.ONE, Qelement.ONE}));
        for (int i=0; i<res.member.size(); i++) {
            QPolynomial factor = (QPolynomial)res.member.elementAt(i);
            System.out.println("Factor "+i+": "+factor);
        }
        time = System.nanoTime();
        QPolynomial[] gb = QPolynomial.computeGroebnerBasis(new QPolynomial[]{p1,  p2}, QPolynomial.grevlexorder);
        System.out.println("Groebner Basis computation: "+(System.nanoTime()-time)/1000000+" ms.");
        System.out.println("Groebner Basis with graded reversed lexicographical order:");
        for (int i=0; i<gb.length; i++) System.out.println("Basis element "+i+": "+gb[i]);
    }

    public static void translateBaluUndDuDiaries() {
        System.out.println("Starting search and replace");
        File directory = new File("BaluUndDuTagebuecher");
        if (!directory.exists()) System.out.println("Directory 'BaluUndDuTagebuecher' is missing.");
        String[] files = (new File("BaluUndDuTagebuecher")).list();
        for (String name:files) {
            if (name.endsWith(".txt") && Character.isDigit(name.charAt(0))) {
                
                System.out.println("Search and replace on "+name);
                String erg = "";
                boolean blockReached = false;
                boolean finished = false;
                try {
                    BufferedReader b = new BufferedReader(new FileReader(new File(directory, name)));

                    while (b.ready() && !finished) 
                    {
                        String line = b.readLine().trim();
                        if (line.startsWith("Informelles Lernen") || line.startsWith("Emotion")) finished = true;
                        if (blockReached && !finished && line.length() > 0 && !line.equals("$")) erg += line+"\r\n";
                        if (line.startsWith("Allgemeines")) blockReached = true;
                    }
                    b.close();
                } catch (Exception e) {System.out.println("Error reading from file "+name+", please check. Exception: "+e); }
                
                if (blockReached = false) System.out.println("General Content block not reached in file "+name+", please check.");
                if (blockReached = true && finished == false) System.out.println("File ended in general content block in file "+name+", please check.");
                
                try {
                    BufferedWriter w = new BufferedWriter(new FileWriter(new File(directory, "AllgemeinBalu"+name)));
                    w.write(erg);
                    w.flush();
                    w.close();
                } catch (Exception e) {System.out.println("Error writing file "+"AllgemeinBalu"+name+", please check. Exception: "+e);}
            }
        }
        System.out.println("Finished search and replace");
    }
    
    public static void thedeMathewettbewerb() {

        boolean findAllSolutions = false;
        boolean checkProbablyUnsolvable = false;
        
        for (int n=4; n<=40; n++) if (checkProbablyUnsolvable || (n%4 == 0 || n%4==1)) {
            int[] from = new int[n];
            boolean[] open = new boolean[2*n];
            
            for (int i=0; i<n; i++) from[i] = -1;
            for (int i=0; i<2*n; i++) open[i] = true;
            // place largest connection
            from[n-1] = 0; open[0] = false; open[n] = false;
            
            int anzSolutions = 0;
            System.out.println("******************************************\r\n** Starting to find solutions for n = "+n+".\r\n");
            int currentDist = 0;
            int source = -1;
            while(currentDist >= 0 && currentDist < n-1) {
                source++;
                if (source >= 2*n) {
                    currentDist--; 
                    if (currentDist >= 0) {
                        int prevTarget = from[currentDist]+currentDist+1; if (prevTarget >= 2*n) prevTarget -= 2*n;
                        source = from[currentDist];
                        open[from[currentDist]] = true; open[prevTarget] = true; from[currentDist] = -1;
                    }
                } else {
                    int target = source+currentDist+1; if (target >= 2*n) target -= 2*n;
                    if (open[source] && open[target]) {
                        // place currentDist
                        open[source] = false; open[target] = false;
                        from[currentDist] = source;
                        source = -1;
                        currentDist++;
                        if (currentDist >= n-1) {
                            // declare found solution
                            anzSolutions++;
                            if (anzSolutions==1) {
                                System.out.println("1st Solution found: ");
                                for (int i=0; i<n; i++)
                                    System.out.println((i+1)+"\t"+from[i]+"\t"+(from[i]+i+1>=2*n?from[i]+i+1-2*n:from[i]+i+1));
                            }
                            
                            // initiate backstep to search for next solution
                            if (findAllSolutions) {
                                currentDist--;
                                int prevTarget = from[currentDist]+currentDist+1; if (prevTarget >= 2*n) prevTarget -= 2*n;
                                source = from[currentDist];
                                open[from[currentDist]] = true; open[prevTarget] = true; from[currentDist] = -1;
                            }
                        }
                    }
                }
            }
            System.out.println("Total solutions found = "+anzSolutions+"\r\n");
        }
    }
    
    public static void ingaAndTheNorwegians() {
        Vector<Vector<String>> dataWithNonnumerical = Statik.loadDataMatrix("ingaData\\mace data timo.txt", '\t', false, true);
        double[][] data = Statik.loadMatrix("ingaData\\mace data timo.txt", '\t', false);
        
        final int AGEIN = 6;
        final int ITEMSTART = 7;
        final int ITEMANZCOLS = 20;
        final int anzItem = 55;
        final int anzPer = 157;
        final int reachBeforeAfter = 2;
        final boolean[] reversed = new boolean[anzItem]; for (int i=0; i<anzItem; i++) reversed[i] = false;
        reversed[42] = reversed[43] = reversed[44] = reversed[45] = reversed[52] = reversed[53] = reversed[54] = true;
                
        // 0 = stays good, 1 = stays bad, 2 = gets worse, 3 = gets better, -999 = don't know
        double[][] res = new double[anzPer][anzItem];
        for (int i=1; i<=anzPer; i++) {
            int age = (int)Math.round(data[i][AGEIN]);
            for (int j=0; j<anzItem; j++) {
                if (age==-999) res[i-1][j] = -999; else {
                    boolean goodBefore = true, goodAfter = true;
                    for (int k=1; k<=reachBeforeAfter; k++) {
                        if (!reversed[j]) {
                            if (age-k>0 && data[i][ITEMSTART+ITEMANZCOLS*j+age-k]!=-999) goodBefore = false; 
                            if (age+k<=18 && data[i][ITEMSTART+ITEMANZCOLS*j+age+k]!=-999) goodAfter = false; 
                        } else {
                            if (age-k>0 && data[i][ITEMSTART+ITEMANZCOLS*j+age-k]==-999) goodBefore = false; 
                            if (age+k<=18 && data[i][ITEMSTART+ITEMANZCOLS*j+age+k]==-999) goodAfter = false; 
                        }
                    }
                    res[i-1][j] = (goodBefore&&goodAfter?0:
                                (!goodBefore && !goodAfter?1:
                                (goodBefore && !goodAfter?2:
                                3)));
                    if (age<=1 || data[i][ITEMSTART+ITEMANZCOLS*j]==99) res[i-1][j] = -999;
                    if (data[i][ITEMSTART+ITEMANZCOLS*j+19] == 99) {
                        boolean noTicksAnywhere = true; for (int k=1; k<=18; k++) if (data[i][ITEMSTART+ITEMANZCOLS*j+k] != -999) noTicksAnywhere = false;
                        if (noTicksAnywhere) res[i-1][j] = -999; 
                    }
                }
            }            
        }
        
        System.out.println(Statik.matrixToString(res));
        
        System.out.println("Done.");
    }
    
    public static void pathTrackingCheck() {
        AnalyticalFunction f = new AnalyticalFunction() {
            
            @Override
            public double eval(double[] val) {return 0.7*val[0]*val[0] + 5*val[0] + val[1]*val[1] + 8*val[1];}
            
            @Override
            public int anzPar() {return 2;}
        };
        Vector<double[]> res = new Vector<double[]>();
        PathTracking.trackPathWithGoodStart(f, 1, 0.0, new double[] {0}, new double[] {3}, 0, PathTracking.DEFAULTANCHORS, PathTracking.MAXSTEP, res);
        for (double[] x:res) System.out.println(Statik.matrixToString(x));
        
        double v = PathTracking.solveWithTrackPath(f, 1, 0.0, new double[] {0.0}, 5.0, null, null);
        System.out.println("Solves to "+v);
        
        AnalyticalFunction f2 = new AnalyticalFunction() {
            
            @Override
            public double eval(double[] x) {return 5*x[0]*x[0]+2*x[0]*x[1]-x[0]*x[3]+7*x[2]*x[3]-2*x[3]*x[3];}
            
            @Override
            public int anzPar() {return 4;}
        };
        AnalyticalFunction fb = new AnalyticalPathBalanceFunction(f2, 0, 0.0, 1, 0.0, new double[] {1.0,2.0}, 0.0);
        
        for (int i=0; i<10; i++) 
            System.out.println((i/10.0)+"\t"+fb.eval(i/10.0));
        
    }

    public static void miToBacTranslation() {
        AnalyticalFunction mi = new AnalyticalFunction() {

            @Override
            public double eval(double[] val) {
                double p = val[0], q1 = val[1], q2 = 1-val[2];
                double q = p*q1 + (1-p)*q2;
                double erg = -  q*Math.log(q)-((1-q)*Math.log(1-q)) + p*(q1*Math.log(q1)+(1-q1)*Math.log(1-q1))+(1-p)*(q2*Math.log(q2)+(1-q2)*Math.log(1-q2));
                return erg;
            }

            @Override
            public int anzPar() {
                return 3;
            }
        };
        
        double p = 0.8;
        System.out.print("q \\ q1 \t");
        for (int j=0; j<=40; j++)
            System.out.print((j==0?0.001:(j==40?0.9999:j/40.0))+"\t");
        System.out.println();
        for (int i=1; i<=9; i++) {
            double q = i/10.0;
            System.out.print(q+"\t");
            double v = mi.eval(new double[] {p,q,q});
            AnalyticalFunction f = new AnalyticalPathBalanceFunction(mi, 2, q, 1, q, new double[] {p}, v);
            for (int j=0; j<=40; j++) {
                double q1 = (j==0?0.001:(j==40?0.9999:j/40.0));
                double q2 = f.eval(q1);
                System.out.print(q2+"\t");
            }
            System.out.println();
        }
    }

    public static void miraIngaHanne(boolean useSum) {
        RawDataset data = RawDataset.loadRawDataset("miraIngaHanne/timeData.txt");
        double[][] d = data.getData();
        for (int i=0; i<d.length; i++) for (int j=0; j<d.length; j++) if (d[i][j]==99) d[i][j] = Model.MISSING;
        
        String[] scaleName = new String[] {"PVA","PNVEA","PPA","EN","PN","WITP","WITS","PEERE","PEERP","SEXA"};
        int[] scaleCutoff = new int[] {3,2,3,4,2,3,2,3,2,1};
        int[] anzItems = new int[] {4,5,6,7,6,6,5,4,4,8};
        
        Vector<double[]> resultColumns = new Vector<double[]>();
        Vector<String> resultHeader = new Vector<String>();
        File file = new File("miraIngaHanne/Models");
        for (int snr=0; snr<scaleName.length; snr++) {
            File f = new File (file, scaleName[snr]+".xml");
            OnyxModel model = OnyxModel.load(f);
            int latentIx = -1;
            for (int i=0; i<model.variableNames.length; i++)
                if (model.variableNames[i].equals(model.name)) latentIx = i;

            System.out.println("Starting Model "+model.name+" general question.");
            boolean allok = model.runUntil(data, Until.CONVERGED, 60000);
            if (!allok) System.err.println("Model "+model.name+" (general question) did not converge reliably.");
            double sign = Math.signum(model.modelRun.getBestUnit().getParameterValue("load"));
            double[][] zwerg = model.getLatentAndMissingScores(model.modelRun.getBestUnit());
            double[] zwergCol = new double[zwerg.length];
            double[] zwergMultiCol = new double[zwerg.length];
            double[] zwergTenCol = new double[zwerg.length];
            for (int i=0; i<zwerg.length; i++) 
                if (!useSum) {
                    zwergCol[i] = sign * zwerg[i][latentIx];
                } else {
                    zwergCol[i] = 0;
                    for (int j=0; j<zwerg[i].length; j++) if (j!=latentIx) zwergCol[i] += zwerg[i][j];
                    if (zwergCol[i] < 0) zwergCol[i] = 0; if (zwergCol[i] > anzItems[snr]) zwergCol[i] = anzItems[snr];
                    zwergMultiCol[i] = (zwergCol[i] >= scaleCutoff[snr]?1:0);
                    zwergTenCol[i] = zwergCol[i]*10 / (double)anzItems[snr];
                }
            resultColumns.add(zwergCol);
            resultHeader.add("FIML_EQ_"+scaleName[snr]);
            if (useSum) {
                resultColumns.add(zwergMultiCol);
                resultHeader.add("FIML_EQ_MULTI_"+scaleName[snr]);
                resultColumns.add(zwergTenCol);
                resultHeader.add("FIML_EQ_TEN_"+scaleName[snr]);
            }
            model.killModelRun();
            
            for (int age=1; age<=18; age++) {
                System.out.println("Starting Model "+model.name+" age "+age+".");
                OnyxModel modelAtAge = model.copy();
                for (int i=0; i<modelAtAge.variableNames.length; i++)
                    if (modelAtAge.variableNames[i].startsWith("M")) {
                        String name = modelAtAge.variableNames[i]+"_"+age;
                        String reversedItemName = modelAtAge.variableNames[i]+"r_"+age;
                        if (data.getColumnNumber(reversedItemName)!=-1) name = reversedItemName;
                        modelAtAge.variableNames[i] = name;
                    }
                
                allok = modelAtAge.runUntil(data, Until.CONVERGED, 60000);
                if (!allok) System.err.println("Model "+model.name+" at age "+age+" did not converge reliably.");
                sign = Math.signum(modelAtAge.modelRun.getBestUnit().getParameterValue("load"));
                zwerg = modelAtAge.getLatentAndMissingScores(modelAtAge.modelRun.getBestUnit());
                zwergCol = new double[zwerg.length];
                zwergMultiCol = new double[zwerg.length];
                zwergTenCol = new double[zwerg.length];
                for (int i=0; i<zwerg.length; i++) 
                    if (!useSum) {
                        zwergCol[i] = sign * zwerg[i][latentIx];
                    } else {
                        zwergCol[i] = 0;
                        for (int j=0; j<zwerg[i].length; j++) if (j!=latentIx) zwergCol[i] += zwerg[i][j];
                        if (zwergCol[i] < 0) zwergCol[i] = 0; if (zwergCol[i] > anzItems[snr]) zwergCol[i] = anzItems[snr];
                        zwergMultiCol[i] = (zwergCol[i] >= scaleCutoff[snr]?1:0);
                        zwergTenCol[i] = zwergCol[i]*10 / (double)anzItems[snr];
                    }
                resultColumns.add(zwergCol);
                resultHeader.add("FIML_EQ_"+model.name+"_"+age);
                if (useSum) {
                    resultColumns.add(zwergMultiCol);
                    resultHeader.add("FIML_EQ_MULTI_"+scaleName[snr]+"_"+age);
                    resultColumns.add(zwergTenCol);
                    resultHeader.add("FIML_EQ_TEN_"+scaleName[snr]+"_"+age);
                }
            }
        }
        System.out.println("Finished, starting output generation.");
        try {
            PrintStream resultStream = new PrintStream("miraIngaHanne/result.txt");
            for (String colHeader:resultHeader) resultStream.print(colHeader+"\t");
            resultStream.println();
            for (int i=0; i<data.getNumRows(); i++) {
                for (int j=0; j<resultColumns.size(); j++) resultStream.print(resultColumns.elementAt(j)[i]+"\t");
                resultStream.println();
            }
            resultStream.close();
        } catch (Exception e) {
            System.err.println("Something's wrong at saving result, "+e);
        }
        System.out.println("done.");
    }
    
    public static void main(String[] args) {
        
//        multiLevelClassACE();
//        multiLevelClassHW8();
//        multiLevelClassSkiing();
//        multiLevelClassLiteratureBig();
//        testFlorianSEMDebug();
//        testMissingnessInSEMModel();
        
//        florianFlatModelTestingNoEqualtyConstraints(); 
//        florianFlatFactorModelRAM();
//        tTestInSEMLecture();
//        fTestInSEMLecture();
//        danHacketTest();
//        multiLevelClassComputerModelingRevision();
//        multiLevelClassHomework93();
//        multiLevelClassOrdinal();
//        multiLevelClassHomework10();
//        bommaeDataSimulation();
//        multiLevelFinal();
//        testModelComputeCovarianceCloseToRealWithParameterConstraint();
//        testFilteredInverse();
//        ttt();
//        colloquiumPresentationExample();
//        testPositivation() ;
//        testExampleMissspecification();
//        parseOutput();
//        ulmanQuickPower();
//        ulmanMartinPlanSimulation();
//        System.out.println(computePForThree(4.972, 1.916, 0.860, 8.978)+"");
//        testModelDerivative();
//        floToFlo();
//        midtermDatamining();
//        ldeFitDanData();
//        dataMiningHW6();
//        mathPsyHW8();
//        coupledLdeFitTest();
//        coupledLdeFitDanEmbeded();
//        danPleOnlyFitCoupled();
//        testQRDecomposition();
//        ldeFitTest();
//        vonOertzenBrandmaierPANDASecondSimulation();
//        vonOertzenBrandmaierPANDAPowerSimulation();
//        evaluation4005();
//        Scripts.sinyLGMsimulationtest();
//        florianLaengsVsQuer();
//        testTwoByTwoTest();
//        ppmlSimulationDifferentModels();
//        testInversion();
//        testCG();
//        missingModel();
//        semClassHW4();
//        steveMiddleRace();
//        semClassHW8();
//        comparisonSportModel();
//        causation();
//        sparseRAMFitTimeComparison();
//        donnaDataTransformation();
//        simulateComparisonSportData();

//        if (args.length!=0) florianNonFlatFlatModelTesting(Integer.parseInt(args[0]));
//        florianNonFlatFlatModelTesting(101);
//        ulmanRegressionModel();
        
//      testFisherComputation();
//        clusteringSimulation();
//        manuelExponentialModel();
//        if (args.length>0) florianNonFlatFlatModelTestingBetweenVsWithin(Integer.parseInt(args[0]));
//        else florianNonFlatFlatModelTestingBetweenVsWithin(1);
//        florianDebugTest();
//        testKernelFinder();
//          System.out.println("arg0 = "+args[0]);
//          florianStandardizedWeights(Integer.parseInt(args[0]),1);

//      testKLofMixture();

//        if (args.length>0) florianStandardizedWeights(Integer.parseInt(args[0]),1);
//        else florianStandardizedWeights(18,101-17);
//        florianCombineResults();
//        florianComputerDifferenceData();

//        double[][] erg = new double[101][8];
//        for (int pnr = 41; pnr <=60; pnr++) {
//            erg[pnr][0] = florianCompareRMSEA(pnr, "raw_person", false);
//            erg[pnr][1] = florianCompareRMSEABetweenRowCorrections(pnr, "raw_person", false);
//            erg[pnr][2] = florianCompareRMSEA(pnr, "raw_person", true);
//            erg[pnr][3] = florianCompareRMSEABetweenRowCorrections(pnr, "raw_person", true);
//            erg[pnr][4] = florianCompareRMSEA(pnr, "person", false);
//            erg[pnr][5] = florianCompareRMSEABetweenRowCorrections(pnr, "person", false);
//            erg[pnr][6] = florianCompareRMSEA(pnr, "person", true);
//            erg[pnr][7] = florianCompareRMSEABetweenRowCorrections(pnr, "person", true);
//            Statik.writeMatrix(erg, "FloData\\chisquareValues.txt", '\t');
//        }
//        
//        double[][] erg2 = new double[102][4];
//        for (int pnr = 1; pnr <=101; pnr++) {
////            erg2[pnr][0] = florianBetweenRowCorrections(pnr, "person", false, false);
//            erg2[pnr][1] = florianBetweenRowCorrections(pnr, "person", false, true);
//            Statik.writeMatrix(erg2, "FloData\\chisquareValuesSaturated.txt", '\t');
//        }
        
//        for (int i=0; i<75; i++) floGetCrosstimeCorrelation(1,i);
//        double[][] cov = floGetSimulationCovariance(2, new int[]{0,1,2}, 2);
//        double[][] simData = floSimulateDataset(cov, 18, 1000, new Random());
//        double[][] chol = Statik.choleskyDecompose(new double[][]{{1,0.1,0.2,0.2},{0.1,1,0.2,0.2},{0.2,0.2,1,0.1},{0.2,0.2,.1,1}});
//        double[][] simData = floSimulateDataset(chol, 2, 2, 1000, new Random());
        
//        long time = System.nanoTime();
//        double p = floFindSimulatedPValue(77, new int[]{0,1,2}, 100);
//        time = System.nanoTime() - time;
//        System.out.println("Total Time = "+((double)time / 1000000000.0)+" seconds");
        
//        timoJoeyModel();
//        floClustering(true, false, true, false);
//        andyMinimalModel();
//        rebeccaWeast();
//        bigHoferSimulation(false, false, true, false, true, 10000);
//        testErgodicSubspaceAnalysis();
//        testESAScree();
//        floOutputDetrentLongFormat(true);
//        testAdaptiveTesting();
        
        
//        (new Test()).testMultiLevelPPML();
//        saraAnalysisScript();
//        floPreferenceClustering();
//        danDissSim1();
//        sinySimulation();
//        covMinimumSimulation();
//        testingMDSProjection();
//        simulateIVBias();
//        muellerEtAlScript();
//        adaptiveDyadicTestingSimulation();
//        testResultants();
//        translateBaluUndDuDiaries();
//        joshuaMLPPMLExample();
//        sabineLDA();
//        thedeMathewettbewerb();
//        ingaAndTheNorwegians();
//        miToBacTranslation();
        miraIngaHanne(true);
    }
    
}
