/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

//EDITED BY KARIM HAMASNI for COSC 4V82
//Student Number: 4423091

package ec.app.bitcoinTrader;

import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;

import java.text.DecimalFormat;
import java.util.*;
import java.awt.Point;

public class MultiValuedRegression extends GPProblem implements
		SimpleProblemForm 
	{

	//Initial dollar and bitcoin balances used to test how well our GP did in trading
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
	public static final String P_TRAIN = "trainingSetSize";

	//A Hased Data structure used to hold our initial test values
	Set<Integer> testingValues = new LinkedHashSet<Integer>();

	//Vars to hold the various sizes
	public static int totalSize;
	public int trainingSetSize;
	public int testingSetSize;

	//These are the arrays that hold the price line data for the 4 markets
	public static String[] okCoinRecords;
	public static String[] bitStampRecords;
	public static String[] btceRecords;
	public static String[] bitfinexRecords;

	//These arrays hold the expected values that are used for fitness function #2
	public static int[] expectedResults;
	public static int[] expectedTraining;
	public static int[] expectedTesting;
	public double currentQuality;
	public double expectedResult;
	
	//This var holds the buy and hold earnings expected if we simply buy at the beginning of the timeline, and sell at the end
	public static double buyAndHoldEarnings;

	// Doubles that store the average over x number of seconds
	public double average30seconds;
	public double average60seconds;
	public double average90seconds;
	public double average120seconds;

	// Doubles that hold the rate of change over x number of seconds
	public double rateOfChange30seconds;
	public double rateOfChange60seconds;
	public double rateOfChange90seconds;
	public double rateOfChange120seconds;

	// RelativeStengthIndexValues
	public double relativeStrengthI30s;
	public double relativeStrengthI60s;
	public double relativeStrengthI90s;
	public double relativeStrengthI120s;

	// MACD
	public double macdValue;

	// Max Values
	public double maxValue30s;
	public double maxValue60s;
	public double maxValue90s;
	public double maxValue120s;

	// Min Values
	public double minValue30s;
	public double minValue60s;
	public double minValue90s;
	public double minValue120s;

	//Variance between Bitstamp and Bitfinex
	public double varianceBetweenBSandBF60s;
	public double varianceBetweenBSandBF120s;
	public double varianceBetweenBSandBF240s;

	//Variance between Bitstamp and BTC-E
	public double varianceBetweenBSandBT60s;
	public double varianceBetweenBSandBT120s;
	public double varianceBetweenBSandBT240s;

	//Variance between Bitstamp and OK-Coin
	public double varianceBetweenBSandOK60s;
	public double varianceBetweenBSandOK120s;
	public double varianceBetweenBSandOK240s;
	
	// Volatility, aka Standard Deviation
	public double volatility30s;
	public double volatility60s;
	public double volatility120s;

	//These variables hold the averages between the home and away markets over the entire timeline
	public double averageDiffBSandBT;
	public double averageDiffBSandBF;
	public double averageDiffBSandOK;

	//Arrays that hold the training and testing data
	public double training[][];
	public double testing[][];

	public void setup(final EvolutionState state, final Parameter base) 
	{
		super.setup(state, base);
		//Set the sizes
		totalSize = state.parameters.getInt(base.push(P_SIZE), null, 1);
		trainingSetSize = state.parameters.getInt(base.push(P_TRAIN), null, 1);
		testingSetSize = totalSize - trainingSetSize;

		//Collect the data lines from the input text files
		okCoinRecords = FinancialFunctions.buildRecordsArray("okcoinEvery10Seconds.txt");
		bitStampRecords = FinancialFunctions.buildRecordsArray("bitstampEvery10Seconds.txt");
		btceRecords = FinancialFunctions.buildRecordsArray("btceEvery10Seconds.txt");
		bitfinexRecords = FinancialFunctions.buildRecordsArray("bitfinexEvery10Seconds.txt");

		//Calculate the averages betwen markets
		averageDiffBSandBT = FinancialFunctions.averageMarketVariance(bitStampRecords, btceRecords);
		averageDiffBSandBF = FinancialFunctions.averageMarketVariance(bitStampRecords, bitfinexRecords);
		averageDiffBSandOK = FinancialFunctions.averageMarketVariance(bitStampRecords, okCoinRecords);

		//Calculate the buy and hold earnings over the time line
		buyAndHoldEarnings = 1000 / (Double.parseDouble(bitStampRecords[0])) * (Double.parseDouble(bitStampRecords[totalSize - 1]));

		//This calculates the expected results by running the buy hold sell function that returns what trades should be made at a given time
		expectedResults = new int[totalSize];
		for (int i = 0; i < totalSize; i++) {
			expectedResults[i] = FinancialFunctions.buyHoldSell(bitStampRecords, i);
		}
		
		if (trainingSetSize < 1)
			state.output.fatal("Training Set Size must be an integer greater than 0", base.push(P_TRAIN));

		//This is a hashed data structures that hold the training random values. This structure is used so that inputs are not repeated
		Set<Integer> trainingRandomValues = new LinkedHashSet<Integer>();

		//We fill the training random values data structure
		while (trainingRandomValues.size() < trainingSetSize) {
			Integer next = rand.nextInt(totalSize) + 1;
			trainingRandomValues.add(next);
		}
		
		//We define our training, testing, and expected results arrays
		training = new double[trainingSetSize][33];
		testing = new double[testingSetSize][33];
		expectedTraining = new int[trainingSetSize];
		expectedTesting = new int[testingSetSize];

		//This loop iterates through and populates the training array for use in GP
		int j = 0;
		for (Integer i : trainingRandomValues) 
		{
			//This just prints out a progress report to console every 1000 lines of data to let us know how far along it is in execution
			if (j % 1000 == 0) {
				double ratio = (double) j / (double) trainingSetSize;
				System.out.println("BUILDING TRAINING TERMINALS: " + new DecimalFormat("##.#").format(100*ratio) + "%");
			}

			training[j][0] = FinancialFunctions.averageOverX(30, i,	bitStampRecords);
			training[j][1] = FinancialFunctions.averageOverX(60, i,	bitStampRecords);
			training[j][2] = FinancialFunctions.averageOverX(90, i,	bitStampRecords);
			training[j][3] = FinancialFunctions.averageOverX(120, i, bitStampRecords);

			training[j][4] = FinancialFunctions.rateOfChangeOverX(30, i);
			training[j][5] = FinancialFunctions.rateOfChangeOverX(60, i);
			training[j][6] = FinancialFunctions.rateOfChangeOverX(90, i);
			training[j][7] = FinancialFunctions.rateOfChangeOverX(120, i);

			training[j][8] = FinancialFunctions.relativeStrengthIndexOverN(30,i);
			training[j][9] = FinancialFunctions.relativeStrengthIndexOverN(30,i);
			training[j][10] = FinancialFunctions.relativeStrengthIndexOverN(30,i);
			training[j][11] = FinancialFunctions.relativeStrengthIndexOverN(30,i);

			training[j][12] = FinancialFunctions.MACD(i, bitStampRecords);
			
			training[j][13] = FinancialFunctions.maxValueOverX(30, i);
			training[j][14] = FinancialFunctions.maxValueOverX(60, i);
			training[j][15] = FinancialFunctions.maxValueOverX(90, i);
			training[j][16] = FinancialFunctions.maxValueOverX(120, i);

			training[j][17] = FinancialFunctions.minValueOverX(30, i, bitStampRecords);
			training[j][18] = FinancialFunctions.minValueOverX(60, i, bitStampRecords);
			training[j][19] = FinancialFunctions.minValueOverX(90, i, bitStampRecords);
			training[j][20] = FinancialFunctions.minValueOverX(120, i, bitStampRecords);

			training[j][21] = FinancialFunctions.currentVarianceValue(bitStampRecords, bitfinexRecords, averageDiffBSandBF,i, 6);
			training[j][22] = FinancialFunctions.currentVarianceValue(bitStampRecords, bitfinexRecords, averageDiffBSandBF,i, 12);
			training[j][23] = FinancialFunctions.currentVarianceValue(bitStampRecords, bitfinexRecords, averageDiffBSandBF,i, 24);

			training[j][24] = FinancialFunctions.currentVarianceValue(bitStampRecords, btceRecords, averageDiffBSandBT, i, 6);
			training[j][25] = FinancialFunctions.currentVarianceValue(bitStampRecords, btceRecords, averageDiffBSandBT, i, 12);
			training[j][26] = FinancialFunctions.currentVarianceValue(bitStampRecords, btceRecords, averageDiffBSandBT, i, 24);

			training[j][27] = FinancialFunctions.currentVarianceValue(bitStampRecords, okCoinRecords, averageDiffBSandOK, i, 6);
			training[j][28] = FinancialFunctions.currentVarianceValue(bitStampRecords, okCoinRecords, averageDiffBSandOK, i, 12);
			training[j][29] = FinancialFunctions.currentVarianceValue(bitStampRecords, okCoinRecords, averageDiffBSandOK, i, 24);

			training[j][30] = FinancialFunctions.volatility(30, i, bitStampRecords);
			training[j][31] = FinancialFunctions.volatility(60, i, bitStampRecords);
			training[j][32] = FinancialFunctions.volatility(90, i, bitStampRecords);
			expectedTraining[j] = expectedResults[i];

			j++;
		}
		
		//add all testing values from the total size
		for (j = 0; j < totalSize; j++) {
			testingValues.add(j);
		}
		//remove the training values from the testing values
		testingValues.removeAll(trainingRandomValues);

		j = 0;
		for (Integer i : testingValues) {
			if (j % 5000 == 0) {
				double ratio = (double) j / (double) testingSetSize;
				System.out.println("BUILDING TESTING TERMINALS: " + new DecimalFormat("##.#").format(100*ratio) + "%");
			}

			testing[j][0] = FinancialFunctions.averageOverX(30, i, bitStampRecords);
			testing[j][1] = FinancialFunctions.averageOverX(60, i, bitStampRecords);
			testing[j][2] = FinancialFunctions.averageOverX(90, i, bitStampRecords);
			testing[j][3] = FinancialFunctions.averageOverX(120, i, bitStampRecords);

			testing[j][4] = FinancialFunctions.rateOfChangeOverX(30, i);
			testing[j][5] = FinancialFunctions.rateOfChangeOverX(60, i);
			testing[j][6] = FinancialFunctions.rateOfChangeOverX(90, i);
			testing[j][7] = FinancialFunctions.rateOfChangeOverX(120, i);
			
			testing[j][8] = FinancialFunctions.relativeStrengthIndexOverN(30, i);
			testing[j][9] = FinancialFunctions.relativeStrengthIndexOverN(30, i);
			testing[j][10] = FinancialFunctions.relativeStrengthIndexOverN(30, i);
			testing[j][11] = FinancialFunctions.relativeStrengthIndexOverN(30, i);
			
			testing[j][12] = FinancialFunctions.MACD(i, bitStampRecords);
			
			testing[j][13] = FinancialFunctions.maxValueOverX(30, i);
			testing[j][14] = FinancialFunctions.maxValueOverX(60, i);
			testing[j][15] = FinancialFunctions.maxValueOverX(90, i);
			testing[j][16] = FinancialFunctions.maxValueOverX(120, i);
			
			testing[j][17] = FinancialFunctions.minValueOverX(30, i, bitStampRecords);
			testing[j][18] = FinancialFunctions.minValueOverX(60, i, bitStampRecords);
			testing[j][19] = FinancialFunctions.minValueOverX(90, i, bitStampRecords);
			testing[j][20] = FinancialFunctions.minValueOverX(120, i, bitStampRecords);
		
			testing[j][21] = FinancialFunctions.currentVarianceValue(bitStampRecords, bitfinexRecords, averageDiffBSandBF, i, 6);
			testing[j][22] = FinancialFunctions.currentVarianceValue(bitStampRecords, bitfinexRecords, averageDiffBSandBF, i, 12);
			testing[j][23] = FinancialFunctions.currentVarianceValue(bitStampRecords, bitfinexRecords, averageDiffBSandBF, i, 24);

			testing[j][24] = FinancialFunctions.currentVarianceValue(bitStampRecords, btceRecords, averageDiffBSandBT, i, 6);
			testing[j][25] = FinancialFunctions.currentVarianceValue(bitStampRecords, btceRecords, averageDiffBSandBT, i, 12);
			testing[j][26] = FinancialFunctions.currentVarianceValue(bitStampRecords, btceRecords, averageDiffBSandBT, i, 24);

			testing[j][27] = FinancialFunctions.currentVarianceValue(bitStampRecords, okCoinRecords, averageDiffBSandOK, i, 6);
			testing[j][28] = FinancialFunctions.currentVarianceValue(bitStampRecords, okCoinRecords, averageDiffBSandOK, i, 12);
			testing[j][29] = FinancialFunctions.currentVarianceValue(bitStampRecords, okCoinRecords, averageDiffBSandOK, i, 24);

			testing[j][30] = FinancialFunctions.volatility(30, i, bitStampRecords);
			testing[j][31] = FinancialFunctions.volatility(60, i, bitStampRecords);
			testing[j][32] = FinancialFunctions.volatility(90, i, bitStampRecords);
			expectedTesting[j] = expectedResults[i];
			j++;
		}
	}

	// The evaluate method is what gives us our program/function using the training data
	public void evaluate(final EvolutionState state, final Individual ind,
			final int subpopulation, final int threadnum) 
	{
		if (!ind.evaluated) {
			DoubleData input = (DoubleData) (this.input);

			int hits = 0;
			double result;

			for (int y = 0; y < trainingSetSize; y++) {

				// Doubles that store the average over x number of seconds
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
				/*
				// Fitness #1 - This returns a hit if it makes a profitable trade. 
				double dollarBalanceNew = 0, bitcoinBalanceNew = 0;
				if (input.x < 0) { // If sell
					double currentPrice = Double.parseDouble(bitStampRecords[y]);
					if (bitcoinBalance > 0) { // If we have bitcoins to sell
						dollarBalanceNew = currentPrice*bitcoinBalance;
						bitcoinBalanceNew = 0.0;
						if(dollarBalanceNew > dollarBalanceOld){
							hits++;	
						}
						bitcoinBalanceOld = bitcoinBalance;
						dollarBalance = dollarBalanceNew;
						bitcoinBalance = bitcoinBalanceNew;
					}
				} else if (input.x > 0) {
					if (dollarBalance > 0) {
						bitcoinBalanceNew = dollarBalance/Double.parseDouble(bitStampRecords[y]);
						dollarBalanceNew = 0.0;
						if(bitcoinBalanceNew > bitcoinBalanceOld){
							hits++;	
						}
						dollarBalanceOld = dollarBalance;
						dollarBalance = dollarBalanceNew;
						bitcoinBalance = bitcoinBalanceNew;
					}

				} else {  }
				*/
				
				// Fitness #2 - Expected Calculated Results
				if(input.x < 0 && expectedResult < 0){
					hits++;
				} else if (input.x > 0 && expectedResult > 0){
					hits++;
				} else {
					//Not a hit
				}
				
				
			}

			result = ((double) trainingSetSize - (double) hits) / (double) trainingSetSize;
			
			KozaFitness f = ((KozaFitness) ind.fitness);
			f.setStandardizedFitness(state, result);
			f.hits = hits;
			ind.evaluated = true;
		}
	}

	public void describe(EvolutionState state, Individual ind,
			int subpopulation, int threadnum, int log) 
	{
		DoubleData input = (DoubleData) (this.input);

		state.output.println("\n\nPerformance of Best Individual on Testing Set:\n", log);
		
		// Keep track of positive, negative hits, and determines the spread of
		// false positives to false negatives
		int positiveHits = 0;
		int negativeHits = 0;
		int truePositives = 0;
		int trueNegatives = 0;
		int falsePositives = 0;
		int falseNegatives = 0;

		for (int y = 0; y < testingSetSize; y++) 
		{
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
			expectedResult = expectedTesting[y];

			((GPIndividual) ind).trees[0].child.eval(state, threadnum, input,
					stack, ((GPIndividual) ind), this);

			/*
			//Fitness #1
			double dollarBalanceNew = 0, bitcoinBalanceNew = 0;
			if (input.x < 0) { //If sell
				if (bitcoinBalanceTest > 0){ //&& Double.parseDouble(bitStampRecords[y])*bitcoinBalanceTest > dollarBalanceOldTest) {
					dollarBalanceNew = Double.parseDouble(bitStampRecords[y])*bitcoinBalanceTest;
					bitcoinBalanceNew = 0.0;
					if(dollarBalanceNew > dollarBalanceOldTest){
						positiveHits++;
						trueNegatives++;	
					} else {
						negativeHits++;
						falsePositives++;
					}
					bitcoinBalanceOldTest = bitcoinBalanceTest;
					dollarBalanceTest = dollarBalanceNew;
					bitcoinBalanceTest = bitcoinBalanceNew;
				} else {
					//negativeHits++;
					//falseNegatives++;
				}
			} else if (input.x > 0) { //If buy
				if (dollarBalanceTest > 0) {  //&& dollarBalanceTest/Double.parseDouble(bitStampRecords[y]) > bitcoinBalanceOldTest) {
					bitcoinBalanceNew = dollarBalanceTest/Double.parseDouble(bitStampRecords[y]);
					dollarBalanceNew = 0.0;
					if(bitcoinBalanceNew > bitcoinBalanceOld){
						positiveHits++;
						truePositives++;	
					} else {
						negativeHits++;
						falseNegatives++;
					}
					
					dollarBalanceOldTest = dollarBalanceTest;
					dollarBalanceTest = dollarBalanceNew;
					bitcoinBalanceTest = bitcoinBalanceNew;
				} else {
					//negativeHits++;
					//falsePositives++;
				}
			} else { }
			*/
			
			// Fitness #2 - Expected Calculated Results
			if(input.x < 0 && expectedResult < 0){
				positiveHits++;
				trueNegatives++;
				if(bitcoinBalanceTest > 0){
					dollarBalanceTest = Double.parseDouble(bitStampRecords[y])*bitcoinBalanceTest;
					bitcoinBalanceTest = 0.0;	
				}
			} else if (input.x > 0 && expectedResult > 0){
				positiveHits++;
				truePositives++;
				if(dollarBalanceTest > 0){
					bitcoinBalanceTest = dollarBalanceTest/Double.parseDouble(bitStampRecords[y]);
					dollarBalanceTest = 0.0;	
				}
			} else {
				if(input.x > 0 && expectedResult < 0){
					falsePositives++;
				}else{
					falseNegatives++;
				}
			}
			System.out.println("Bitcoin Balance: " + bitcoinBalanceTest + " Dollar Balance: " + dollarBalanceTest);
			
		}
		double error = ((double) testingSetSize - (double) positiveHits)
				/ (double) testingSetSize;
		
		//System.out.println("Positive Hits: " + positiveHits
		state.output.println("Positive Hits: " + positiveHits
				+ "\nNegative Hits: " + negativeHits + "\nFalse Positives: "
				+ falsePositives + "\nFalse Negatives: " + falseNegatives
				+ "\nTrue Positives: " + truePositives + "\nTrue Negatives: "
				+ trueNegatives + "\n", log);

		state.output.println("Dollars: " + dollarBalanceTest + " Bitcoins: " + bitcoinBalanceTest, log);
		state.output.println("Buy and Hold Earnings: " + buyAndHoldEarnings, log);
		state.output.println("Price per bitcoin at the end: " + bitStampRecords[bitStampRecords.length - 1],log);
		if (bitcoinBalanceTest > 0) {
			state.output.println("Converted Bitcoins: "+ bitcoinBalanceTest* (Double.parseDouble(bitStampRecords[bitStampRecords.length - 1])),log);
		}
		// the fitness better be KozaFitness!
		KozaFitness f = (KozaFitness) (ind.fitness.clone()); 
		f.setStandardizedFitness(state, error);
		f.hits = positiveHits;
		f.printFitnessForHumans(state, log);
	}
}