/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

//EDITED BY KARIM HAMASNI for COSC 4V82
//Student Number: 4423091

package ec.app.Texture;

import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

import java.util.*;
import java.awt.Point;

public class MultiValuedRegression extends GPProblem implements
		SimpleProblemForm {

	double dollarBalance = 1000.00;
	double bitcoinBalance = 0.0;
	double dollarBalanceTest = 1000.00;
	double bitcoinBalanceTest = 0.0;
	double dollarBalanceOld = 1000.00;
	double bitcoinBalanceOld = 0.0;
	double dollarBalanceOldTest = 1000.00;
	double bitcoinBalanceOldTest = 0.0;
	
	public Random rand = new Random();

	private static final long serialVersionUID = 1;

	public static final String P_SIZE = "size";
	public static final String P_FILE = "file";
	public static final String P_POS = "pos";
	public static final String P_TRAIN = "trainingSetSize";
	
	Set<Integer> testingValues = new LinkedHashSet<Integer>();
	
	public static int totalSize;
	public int trainingSetSize;
	public int testingSetSize;

	//Integers that state the number of historic records we have for each market
	public int okCoinNumOfRecords;
	public static int bitstampNumOfRecords;
	public int btceNumOfRecords;
	public int bitfinexNumOfRecords;
	
	public static String[] okCoinRecords;
	public static String[] bitStampRecords;
	public static String[] btceRecords;
	public static String[] bitfinexRecords;
	
	public static int[] expectedResults;
	public static int[] expectedTraining;
	public static int[] expectedTesting;
	
	public static double buyAndHoldEarnings;
	
	public double currentQuality;
	
	//Doubles that store the average over x number of seconds
	public double average30seconds;
	public double average60seconds;
	public double average90seconds;
	public double average120seconds;
	
	//Doubles that hold the rate of change over x number of seconds
	public double rateOfChange30seconds;
	public double rateOfChange60seconds;
	public double rateOfChange90seconds;
	public double rateOfChange120seconds;
	
	//RelativeStengthIndexValues
	public double relativeStrengthI30s;
	public double relativeStrengthI60s;
	public double relativeStrengthI90s;
	public double relativeStrengthI120s;
	
	//MACD 
	public double macdValue;
	
	//Max Values
	public double maxValue30s;
	public double maxValue60s;
	public double maxValue90s;
	public double maxValue120s;
	
	//Min Values
	public double minValue30s;
	public double minValue60s;
	public double minValue90s;
	public double minValue120s;
	
	public double varianceBetweenBSandBF60s;
	public double varianceBetweenBSandOK60s;
	public double varianceBetweenBSandBT60s;
	
	public double varianceBetweenBSandBF120s;
	public double varianceBetweenBSandOK120s;
	public double varianceBetweenBSandBT120s;
	
	public double varianceBetweenBSandBF240s;
	public double varianceBetweenBSandOK240s;
	public double varianceBetweenBSandBT240s;

	//Volatility, aka Standard Deviation
	public double volatility30s;
	public double volatility60s;
	public double volatility120s;
	
	public double averageDiffBSandBT;//= FinancialFunctions.averageMarketVariance(bitStampRecords, btceRecords);
	public double averageDiffBSandBF;// = FinancialFunctions.averageMarketVariance(bitStampRecords, bitfinexRecords);
	public static double averageDiffBSandOK;// = FinancialFunctions.averageMarketVariance(bitStampRecords, okCoinRecords);
	

	public double training[][];
	public double testing[][];

	public void setup(final EvolutionState state,

	final Parameter base) {
		super.setup(state, base);

		totalSize = state.parameters.getInt(base.push(P_SIZE), null, 1);
		trainingSetSize = state.parameters.getInt(base.push(P_TRAIN), null, 1);
		testingSetSize = totalSize - trainingSetSize;
		
		okCoinNumOfRecords = totalSize;
		bitstampNumOfRecords = totalSize;
		btceNumOfRecords = totalSize;
		bitfinexNumOfRecords = totalSize;
		
		okCoinRecords  = FinancialFunctions.buildRecordsArray("okcoinEvery10Seconds.txt");
		bitStampRecords  = FinancialFunctions.buildRecordsArray("bitstampEvery10Seconds.txt");
		btceRecords  = FinancialFunctions.buildRecordsArray("btceEvery10Seconds.txt");
		bitfinexRecords  = FinancialFunctions.buildRecordsArray("bitfinexEvery10Seconds.txt");
		
		averageDiffBSandBT = FinancialFunctions.averageMarketVariance(bitStampRecords, btceRecords);
		averageDiffBSandBF = FinancialFunctions.averageMarketVariance(bitStampRecords, bitfinexRecords);
		averageDiffBSandOK = FinancialFunctions.averageMarketVariance(bitStampRecords, okCoinRecords);
		
		buyAndHoldEarnings =  1000/(Double.parseDouble(bitStampRecords[0]))*(Double.parseDouble(bitStampRecords[totalSize-1]));

		expectedResults = new int[bitstampNumOfRecords];
		for(int i = 0; i<bitstampNumOfRecords; i++){
			expectedResults[i] = FinancialFunctions.buyHoldSell(bitStampRecords, i);
		}
				
		if (trainingSetSize < 1)
			state.output.fatal(
					"Training Set Size must be an integer greater than 0",
					base.push(P_TRAIN));

		Random rand = new Random();
		
		//BUILDS A LIST OF RANDOM VALUES FOR TRAINING THAT ARE NOT REPEATED
		Set<Integer> trainingRandomValues = new LinkedHashSet<Integer>();
		
		while(trainingRandomValues.size() < trainingSetSize){
			Integer next = rand.nextInt(totalSize) +1;
			trainingRandomValues.add(next);
		}
		
		training = new double[trainingSetSize][33];
		testing = new double[testingSetSize][33];
		expectedTraining = new int[trainingSetSize];
		expectedTesting = new int[testingSetSize];
		
		//Set the training terminal nodes by calling the filter functions
		int i = 0;
		for(Integer start : trainingRandomValues){
			if(i%1000 == 0){
				System.out.println("TR" + i);	
			}
			
			training[i][0] = FinancialFunctions.averageOverX(30, start, bitStampRecords);
			training[i][1] = FinancialFunctions.averageOverX(60, start, bitStampRecords);
			training[i][2] = FinancialFunctions.averageOverX(90, start, bitStampRecords);
			training[i][3] = FinancialFunctions.averageOverX(120, start, bitStampRecords);
			
			training[i][4] = FinancialFunctions.rateOfChangeOverX(30, start);
			training[i][5] = FinancialFunctions.rateOfChangeOverX(60, start);
			training[i][6] = FinancialFunctions.rateOfChangeOverX(90, start);
			training[i][7] = FinancialFunctions.rateOfChangeOverX(120, start);
			
			training[i][8] = FinancialFunctions.relativeStrengthIndexOverN(30, start);
			training[i][9] = FinancialFunctions.relativeStrengthIndexOverN(30, start);
			training[i][10] = FinancialFunctions.relativeStrengthIndexOverN(30, start);
			training[i][11] = FinancialFunctions.relativeStrengthIndexOverN(30, start);
			
			training[i][12] = FinancialFunctions.MACD(start, bitStampRecords);
			
			training[i][13] = FinancialFunctions.maxValueOverX(30, start);
			training[i][14] = FinancialFunctions.maxValueOverX(60, start);
			training[i][15] = FinancialFunctions.maxValueOverX(90, start);
			training[i][16] = FinancialFunctions.maxValueOverX(120, start);

			training[i][17] = FinancialFunctions.minValueOverX(30, start);
			training[i][18] = FinancialFunctions.minValueOverX(60, start);
			training[i][19] = FinancialFunctions.minValueOverX(90, start);
			training[i][20] = FinancialFunctions.minValueOverX(120, start);
			
			training[i][21] = FinancialFunctions.currentVarianceValue(bitStampRecords, bitfinexRecords, averageDiffBSandBF, start, 6);
			training[i][22] = FinancialFunctions.currentVarianceValue(bitStampRecords, bitfinexRecords, averageDiffBSandBF, start, 12);
			training[i][23] = FinancialFunctions.currentVarianceValue(bitStampRecords, bitfinexRecords, averageDiffBSandBF, start, 24);

			training[i][24] = FinancialFunctions.currentVarianceValue(bitStampRecords, btceRecords, averageDiffBSandBT, start, 6);
			training[i][25] = FinancialFunctions.currentVarianceValue(bitStampRecords, btceRecords, averageDiffBSandBT, start, 12);
			training[i][26] = FinancialFunctions.currentVarianceValue(bitStampRecords, btceRecords, averageDiffBSandBT, start, 24);
			
			training[i][27] = FinancialFunctions.currentVarianceValue(bitStampRecords, okCoinRecords, averageDiffBSandOK, start, 6);
			training[i][28] = FinancialFunctions.currentVarianceValue(bitStampRecords, okCoinRecords, averageDiffBSandOK, start, 12);
			training[i][29] = FinancialFunctions.currentVarianceValue(bitStampRecords, okCoinRecords, averageDiffBSandOK, start, 24);
			
			training[i][30] = FinancialFunctions.volatility(30, start, bitStampRecords);
			training[i][31] = FinancialFunctions.volatility(60, start, bitStampRecords);
			training[i][32] = FinancialFunctions.volatility(90, start, bitStampRecords);
			expectedTraining[i] = expectedResults[start];
			//Build expected training here
			i++;
		}
		
		
		for(int j = 0; j<totalSize; j++){
			testingValues.add(j);
		}
		testingValues.removeAll(trainingRandomValues);
		
		i = 0;
		for(Integer start : testingValues){
			if(i%5000 == 0){
				System.out.println("TE" + i);	
			}
			
			testing[i][0] = FinancialFunctions.averageOverX(30, start, bitStampRecords);
			testing[i][1] = FinancialFunctions.averageOverX(60, start, bitStampRecords);
			testing[i][2] = FinancialFunctions.averageOverX(90, start, bitStampRecords);
			testing[i][3] = FinancialFunctions.averageOverX(120, start, bitStampRecords);
			
			testing[i][4] = FinancialFunctions.rateOfChangeOverX(30, start);
			testing[i][5] = FinancialFunctions.rateOfChangeOverX(60, start);
			testing[i][6] = FinancialFunctions.rateOfChangeOverX(90, start);
			testing[i][7] = FinancialFunctions.rateOfChangeOverX(120, start);
			
			testing[i][8] = FinancialFunctions.relativeStrengthIndexOverN(30, start);
			testing[i][9] = FinancialFunctions.relativeStrengthIndexOverN(30, start);
			testing[i][10] = FinancialFunctions.relativeStrengthIndexOverN(30, start);
			testing[i][11] = FinancialFunctions.relativeStrengthIndexOverN(30, start);
			
			testing[i][12] = FinancialFunctions.MACD(start, bitStampRecords);
			
			testing[i][13] = FinancialFunctions.maxValueOverX(30, start);
			testing[i][14] = FinancialFunctions.maxValueOverX(60, start);
			testing[i][15] = FinancialFunctions.maxValueOverX(90, start);
			testing[i][16] = FinancialFunctions.maxValueOverX(120, start);

			testing[i][17] = FinancialFunctions.minValueOverX(30, start);
			testing[i][18] = FinancialFunctions.minValueOverX(60, start);
			testing[i][19] = FinancialFunctions.minValueOverX(90, start);
			testing[i][20] = FinancialFunctions.minValueOverX(120, start);
			
			testing[i][21] = FinancialFunctions.currentVarianceValue(bitStampRecords, bitfinexRecords, averageDiffBSandBF, start, 60);
			testing[i][22] = FinancialFunctions.currentVarianceValue(bitStampRecords, bitfinexRecords, averageDiffBSandBF, start, 120);
			testing[i][23] = FinancialFunctions.currentVarianceValue(bitStampRecords, bitfinexRecords, averageDiffBSandBF, start, 20);

			testing[i][24] = FinancialFunctions.currentVarianceValue(bitStampRecords, btceRecords, averageDiffBSandBT, start, 60);
			testing[i][25] = FinancialFunctions.currentVarianceValue(bitStampRecords, btceRecords, averageDiffBSandBT, start, 120);
			testing[i][26] = FinancialFunctions.currentVarianceValue(bitStampRecords, btceRecords, averageDiffBSandBT, start, 20);
			
			testing[i][27] = FinancialFunctions.currentVarianceValue(bitStampRecords, okCoinRecords, averageDiffBSandOK, start, 60);
			testing[i][28] = FinancialFunctions.currentVarianceValue(bitStampRecords, okCoinRecords, averageDiffBSandOK, start, 120);
			testing[i][29] = FinancialFunctions.currentVarianceValue(bitStampRecords, okCoinRecords, averageDiffBSandOK, start, 20);
			
			testing[i][30] = FinancialFunctions.volatility(30, start, bitStampRecords);
			testing[i][31] = FinancialFunctions.volatility(60, start, bitStampRecords);
			testing[i][32] = FinancialFunctions.volatility(90, start, bitStampRecords);
			expectedTesting[i] = expectedResults[start];
			//Build expected Testing Here
			i++;
		}
	}

	// The evaluate method is what gives us our program/function using the
	// training data
	public void evaluate(final EvolutionState state, final Individual ind,
			final int subpopulation, final int threadnum) {
		if (!ind.evaluated) {
			DoubleData input = (DoubleData) (this.input);

			int hits = 0;
			int negHits = 0;
			double expectedResult;
			double result;

			for (int y = 0; y < trainingSetSize; y++) {
				
				//Doubles that store the average over x number of seconds
				average30seconds = training[y][0];
				average60seconds = training[y][1];
				average90seconds = training[y][2];
				average120seconds = training[y][3];
				rateOfChange30seconds = training[y][4];
				rateOfChange60seconds = training[y][5];
				rateOfChange90seconds = training[y][6];
				rateOfChange120seconds = training[y][7];
				relativeStrengthI30s = training[y][8];
				relativeStrengthI60s = training[y][9];
				relativeStrengthI90s = training[y][10];
				relativeStrengthI120s = training[y][11];
				macdValue = training[y][12];
				maxValue30s = training[y][13];
				maxValue60s = training[y][14];
				maxValue90s = training[y][15];
				maxValue120s = training[y][16];
				minValue30s = training[y][17];
				minValue60s = training[y][18];
				minValue90s = training[y][19];
				minValue120s = training[y][20];
				varianceBetweenBSandBF60s = training[y][21];
				varianceBetweenBSandOK60s = training[y][22];
				varianceBetweenBSandBT60s = training[y][23];
				varianceBetweenBSandBF120s = training[y][24];
				varianceBetweenBSandOK120s = training[y][25];
				varianceBetweenBSandBT120s = training[y][26];
				varianceBetweenBSandBF240s = training[y][27];
				varianceBetweenBSandBT240s = training[y][28];
				varianceBetweenBSandOK240s = training[y][29];
				volatility30s = training[y][30];
				volatility60s = training[y][31];
				volatility120s = training[y][32];
			
				expectedResult = expectedTraining[y]; 
				
				((GPIndividual) ind).trees[0].child.eval(state, threadnum,
						input, stack, ((GPIndividual) ind), this);
				// fitness
				double dollarBalanceNew = 0, bitcoinBalanceNew = 0;
				if(input.x < 0){ //If sell
					if(bitcoinBalance > 0){ //If we have bitcoins to sell
						dollarBalanceNew = Double.parseDouble(bitStampRecords[y])*bitcoinBalance; //We would earn this much money
						bitcoinBalanceNew = 0.0; //By selling, we would have this many bitcoins left, 0
						if(dollarBalanceNew > dollarBalanceOld){
							hits++;
						} else {
							negHits++;
							//System.out.println(negHits);
						}
						bitcoinBalanceOld = bitcoinBalance;
						dollarBalance = dollarBalanceNew;
						bitcoinBalance = bitcoinBalanceNew;
					}
				}else if(input.x >0){
					if(dollarBalance > 0){
						bitcoinBalanceNew = dollarBalance/Double.parseDouble(bitStampRecords[y]);
						dollarBalanceNew = 0.0;
						if(bitcoinBalanceNew > bitcoinBalanceOld){
							hits++;
						} else {
							negHits++;
							//System.out.println(negHits);
						}
						
						dollarBalanceOld = dollarBalance;
						dollarBalance = dollarBalanceNew;
						bitcoinBalance = bitcoinBalanceNew;
					}
					
				} else {

				}
			}

			result = ((double) trainingSetSize - (double) hits)
					/ (double) trainingSetSize;
			// the fitness better be KozaFitness!
			KozaFitness f = ((KozaFitness) ind.fitness);
			f.setStandardizedFitness(state, result);
			f.hits = hits;
			ind.evaluated = true;
		}
	}

	// This is the function that was originally found in the Benchmark class for
	// the Regression example provided with ECJ
	public void describe(EvolutionState state, Individual ind,
			int subpopulation, int threadnum, int log) {
		DoubleData input = (DoubleData) (this.input);

		state.output.println(
				"\n\nPerformance of Best Individual on Testing Set:\n", log);
		// Keep track of positive, negative hits, and determines the spread of
		// false positives to false negatives
		int positiveHits = 0;
		int negativeHits = 0;
		int truePositives = 0;
		int trueNegatives = 0;
		int falsePositives = 0;
		int falseNegatives = 0;

		// Set my error variable
		//double error2 = 0.0;
		String colourData = "";
		
		for (int y = 0; y < testingSetSize; y++) {
			
			average30seconds = testing[y][0];
			average60seconds = testing[y][1];
			average90seconds = testing[y][2];
			average120seconds = testing[y][3];
			rateOfChange30seconds = testing[y][4];
			rateOfChange60seconds = testing[y][5];
			rateOfChange90seconds = testing[y][6];
			rateOfChange120seconds = testing[y][7];
			relativeStrengthI30s = testing[y][8];
			relativeStrengthI60s = testing[y][9];
			relativeStrengthI90s = testing[y][10];
			relativeStrengthI120s = testing[y][11];
			macdValue = testing[y][12];
			maxValue30s = testing[y][13];
			maxValue60s = testing[y][14];
			maxValue90s = testing[y][15];
			maxValue120s = testing[y][16];
			minValue30s = testing[y][17];
			minValue60s = testing[y][18];
			minValue90s = testing[y][19];
			minValue120s = testing[y][20];
			varianceBetweenBSandBF60s = testing[y][21];
			varianceBetweenBSandOK60s = testing[y][22];
			varianceBetweenBSandBT60s = testing[y][23];
			varianceBetweenBSandBF120s = testing[y][24];
			varianceBetweenBSandOK120s = testing[y][25];
			varianceBetweenBSandBT120s = testing[y][26];
			varianceBetweenBSandBF240s = testing[y][27];
			varianceBetweenBSandBT240s = testing[y][28];
			varianceBetweenBSandOK240s = testing[y][29];
			volatility30s = testing[y][30];
			volatility60s = testing[y][31];
			volatility120s = testing[y][32];
			currentQuality = expectedTesting[y];
			
			((GPIndividual) ind).trees[0].child.eval(state, threadnum, input,
					stack, ((GPIndividual) ind), this);

			double dollarBalanceNew = 0, bitcoinBalanceNew = 0;
			if(input.x < 0){
				if(bitcoinBalanceTest > 0){
					dollarBalanceNew = Double.parseDouble(bitStampRecords[y])*bitcoinBalanceTest;
					bitcoinBalanceNew = 0.0;
					if(dollarBalanceNew > dollarBalanceOldTest){
						positiveHits++;
					} else {
						negativeHits++;
					}
					bitcoinBalanceOldTest = bitcoinBalanceTest;
					dollarBalanceTest = dollarBalanceNew;
					bitcoinBalanceTest = bitcoinBalanceNew;
				}
				
			}else if(input.x >0){
				if(dollarBalanceTest > 0){
					bitcoinBalanceNew = dollarBalanceTest/Double.parseDouble(bitStampRecords[y]);
					dollarBalanceNew = 0.0;
					if(bitcoinBalanceNew > bitcoinBalanceOldTest){
						positiveHits++;
					} else {
						negativeHits++;
					}
					dollarBalanceOldTest = dollarBalanceTest;
					dollarBalanceTest = dollarBalanceNew;
					bitcoinBalanceTest = bitcoinBalanceNew;
				}
				
			} else {
				
			}
			//System.out.println("$"+dollarBalance + "B"+bitcoinBalance);
		}

		double error = ((double) testingSetSize - (double) positiveHits)
				/ (double) testingSetSize;
		state.output.println("Positive Hits: " + positiveHits
				+ "\nNegative Hits: " + negativeHits + "\nFalse Positives: "
				+ falsePositives + "\nFalse Negatives: " + falseNegatives
				+ "\nTrue Positives: " + truePositives + "\nTrue Negatives: "
				+ trueNegatives + "\n", log);
		
		System.out.println("Dollars: "+ dollarBalanceTest + " Bitcoins: " + bitcoinBalanceTest);
		System.out.println("Buy and Hold Earnings: " + buyAndHoldEarnings);
		System.out.println("Price per bitcoin at the end: " + bitStampRecords[bitStampRecords.length-1]);
		if(bitcoinBalanceTest > 0){
			System.out.println("Converted Bitcoins: " + bitcoinBalanceTest*(Double.parseDouble(bitStampRecords[bitStampRecords.length-1])));
		}
		// the fitness better be KozaFitness!
		KozaFitness f = (KozaFitness) (ind.fitness.clone()); // make a copy,
																// we're just
																// printing it
																// out
		f.setStandardizedFitness(state, error);
		f.hits = positiveHits;

		f.printFitnessForHumans(state, log);
	}
}