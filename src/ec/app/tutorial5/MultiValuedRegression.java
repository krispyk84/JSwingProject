/*
  Copyright 2006 by Sean Luke
  Licensed under the Academic Free License version 3.0
  See the file "LICENSE" for more information
 */

package ec.app.tutorial5;

import ec.util.*;
import ec.*;
import ec.gp.*;
import ec.gp.koza.*;
import ec.simple.*;


// other imports
import java.io.*;
import java.util.*;
import java.awt.*;
import java.awt.image.BufferedImage;
//import java.awt.Color;
import javax.imageio.*;
import java.awt.Point;

public class MultiValuedRegression extends GPProblem implements
		SimpleProblemForm {
	private static final long serialVersionUID = 1;

	// Terminals
	public double term1; 	// 
	public double term2;	// 
	public double term3; 	// 
	public double term4;	// 
	public double term5; 	// 
	public double term6; 	// 
	public double term7;	// 
	public double term8; 	// 
	public double term9;	// 
	public double term10; 	// 
	public double term11; 	// 

	
	// ---------------------- INPUT ADDITIONS ----------------------
	
	public static final String P_SIZE = "size";
	public static final String P_FILE = "file";

	

	// these are read-only during evaluation-time, so
	// they can be just light-cloned and not deep cloned.
	// cool, huh?
	public Random rand;
	public double currentValue;		
	public int setSize;				// number of sets in the file
	public int trainerSetSize;		// number of trainer sets
	public int testingSetSize;		// number of testing sets
	public int trainerSetCounter;		// counter of trainer sets
	public int testingSetCounter;		// counter of testing sets
	public double trainerSet[][];	// trainer sets
	public double testingSet[][];	// testing sets
	public double outputs[];
	public String types[];
	public BufferedImage ib;		// buffered image
	public double[] testingOutputs;
	public int trainSize;
	public int posPix;
	ArrayList<Point> trainCoords = new ArrayList<Point>();
	ArrayList<Point> testCoords = new ArrayList<Point>();
	
	//3x3
	int[][] avgArr3;
	double[][] stDev3;
	//5x5
	int[][] avgArr5;
	double[][] stDev5;
	//7x7
	int[][] avgArr7;
	double[][] stDev7;
	//Monochrome
	int[][] monoArr;
	//Max
	int[][] maxArr3;
	int[][] maxArr5;
	int[][] maxArr7;
	//testing
	double[][] testArr;
	// don't bother cloning the inputs and outputs; they're read-only :-)
	// don't bother cloning the currentValue; it's transitory
	
	public double func(double x, double y){
		//return (x * x) - (y * y) + (x * y + x) - y;
		return (x + y);
	}
	
	//3x3 Average	
	public void Avg3(BufferedImage img,int[][] avgArr, double[][] stDev){
	int average;
	int div;
	Color[] col = new Color[9];
	for (int x = 0; x < img.getWidth(); x++){
		for (int y = 0;y < img.getHeight(); y++){
			average = 0;
			div = 0;
			//Check the 4 corners
			//Top Left
			if ((x-1)<0 && (y-1)<0){
				col[0] = new Color(img.getRGB(x,y));
				col[1] = new Color(img.getRGB(x+1, y));
				col[2] = new Color(img.getRGB(x+1, y+1));
				col[3] = new Color(img.getRGB(x, y+1));	
				div = 4;
			} else if ((x+1)>=img.getWidth() && (y+1)>=img.getHeight()){ 
				col[0] = new Color(img.getRGB(x,y));
				col[1] = new Color(img.getRGB(x, y-1));
				col[2] = new Color(img.getRGB(x-1, y));
				col[3] = new Color(img.getRGB(x-1, y-1));
				div = 4;
			} else if ((x+1)>=img.getWidth() && (y-1)<0){
				col[0] = new Color(img.getRGB(x,y));
				col[1] = new Color(img.getRGB(x, y+1));
				col[2] = new Color(img.getRGB(x-1, y+1));
				col[3] = new Color(img.getRGB(x-1, y));	
				div = 4;
			} else if ((x-1)<0 && (y+1)>=img.getHeight()){
				col[0] = new Color(img.getRGB(x,y));
				col[1] = new Color(img.getRGB(x+1, y));
				col[2] = new Color(img.getRGB(x+1,y-1));
				col[3] = new Color(img.getRGB(x, y-1));	
				div = 4;
			} else if ((y-1)<0){
				col[0] = new Color(img.getRGB(x,y));
				col[1] = new Color(img.getRGB(x+1, y));
				col[2] = new Color(img.getRGB(x+1, y+1));
				col[3] = new Color(img.getRGB(x, y+1));
				col[4] = new Color(img.getRGB(x-1, y+1));
				col[5] = new Color(img.getRGB(x-1, y));	
				div = 6;
			} else if ((x-1)<0){
				col[0] = new Color(img.getRGB(x,y));
				col[1] = new Color(img.getRGB(x+1, y));
				col[2] = new Color(img.getRGB(x+1, y+1));
				col[3] = new Color(img.getRGB(x+1,y-1));
				col[4] = new Color(img.getRGB(x, y+1));
				col[5] = new Color(img.getRGB(x, y-1));
				div = 6;
			} else if ((x+1)>=img.getWidth()){
				col[0] = new Color(img.getRGB(x,y));
				col[1] = new Color(img.getRGB(x, y+1));
				col[2] = new Color(img.getRGB(x, y-1));
				col[3] = new Color(img.getRGB(x-1, y+1));
				col[4] = new Color(img.getRGB(x-1, y));
				col[5] = new Color(img.getRGB(x-1, y-1));	
				div = 6;
			} else if ((y+1)>=img.getHeight()){
				col[0] = new Color(img.getRGB(x,y));
				col[1] = new Color(img.getRGB(x+1, y));
				col[2] = new Color(img.getRGB(x+1,y-1));
				col[3] = new Color(img.getRGB(x, y-1));
				col[4] = new Color(img.getRGB(x-1, y));
				col[5] = new Color(img.getRGB(x-1, y-1));
				div = 6;
			} else {
				col[0] = new Color(img.getRGB(x,y));
				col[1] = new Color(img.getRGB(x+1, y));
				col[2] = new Color(img.getRGB(x+1, y+1));
				col[3] = new Color(img.getRGB(x+1,y-1));
				col[4] = new Color(img.getRGB(x, y+1));
				col[5] = new Color(img.getRGB(x, y-1));
				col[6] = new Color(img.getRGB(x-1, y+1));
				col[7] = new Color(img.getRGB(x-1, y));
				col[8] = new Color(img.getRGB(x-1, y-1));
				div = 9;
			}
			
			for (int i=0;i<div; i++){
				average = average + col[i].getRed();
			}
			average = average/div;
			avgArr[x][y] = average;
			double temp = 0;
			for (int q = 0; q<div; q++){
				temp = temp + Math.pow((col[q].getRed()-average),2);
			}
			temp = (temp/div);
			temp = Math.sqrt(temp);
			stDev[x][y] = temp;
			}
		}
	}
	
	public void getAvgK(BufferedImage img,int[][] avgArr, double[][] stDev, int size){      
		Color[] col = new Color[size*size];
        for(int y = 0; y < img.getWidth(); y++){
            for(int x = 0; x < img.getHeight(); x++){
                int average = 0;
                int div = 0;
                for(int j = y - (size / 2); j < (y + (size / 2)); j++){
                    for(int i = x - (size / 2); i < (x + (size / 2)); i++){
                        if((i >= 0 && i < img.getWidth()) && (j >= 0 && j < img.getHeight())){
                            Color c = new Color(img.getRGB(i, j));
							col[div] = new Color(img.getRGB(i, j));
                            average+= c.getRed();
                            div++;
                        }
                    }
                }
                average = average/div;
                if(x == 155 && y == 163){
                	System.out.println(size + "Average for X = " + x + ", " + y + " : " + average);
                }
                avgArr[x][y] = average;             
                //Standard Dev
				double dev = 0;
				for (int q = 0; q<div; q++){
					dev = dev + Math.pow((col[q].getRed()-average),2);
				}
				dev = (dev/div);
				dev = Math.sqrt(dev);
				stDev[x][y] = dev;
            }
        }
	}
	
	public void getMono(BufferedImage img,int[][] monoArr){   
	Color col;
	for (int x=0; x < img.getWidth(); x++)
		for (int y=0; y < img.getHeight(); y++){
			col = new Color(img.getRGB(x,y)); 
			if (col.getRed() > 127){
				monoArr[x][y] = 255;
			}//white
			else{
				monoArr[x][y] = 0;
			}//black
		}
	}
	
	public void getMaxK(BufferedImage img,int[][] maxArr, int size){      
		Color col;
		Color col2;
        for(int y = 0; y < img.getWidth(); y++){
            for(int x = 0; x < img.getHeight(); x++){
				int tempRed = 0;
                int average = 0;
                int div = 0;
				col = new Color(img.getRGB(x,y));
				tempRed = col.getRed();
                for(int j = y - (size / 2); j < (y + (size / 2)); j++){
                    for(int i = x - (size / 2); i < (x + (size / 2)); i++){
                        if((i >= 0 && i < img.getWidth()) && (j >= 0 && j < img.getHeight())){
							//find max
							col2 = new Color(img.getRGB(i,j));
							if (tempRed < col2.getRed()){
										tempRed = col2.getRed();
							}
							//end
                        }
                    }
                }   
				maxArr[x][y] = tempRed;
				tempRed = 0;
            }
        }
	}
	
	public void createSet(BufferedImage img,double[][] testArr){
	//Create array of 1000 random pixels, formated data in testarr
		Random rand = new Random();
		Color col;
		int ranX = 0;
		int ranY = 0;
		for (int i = 0; i < testArr.length; i++) {
			//rand.nextInt((max - min) + 1) + min;
			ranX = rand.nextInt(((img.getWidth()-2) - 1) + 1) + 1;
			ranY = rand.nextInt(((img.getHeight()-2) - 1) + 1) + 1;
			//System.out.println("this is randX "+ranX);
			//System.out.println("this is randY "+ranY);
			/*if (ranX > 510|| ranY >510){
				System.out.println("500this is randX "+ranX);
				System.out.println("500this is randY "+ranY);
			}*/
			col = new Color(img.getRGB(ranX,ranY));
			testArr[i][0] = avgArr3[ranX][ranY];
			testArr[i][1] = avgArr5[ranX][ranY];
			testArr[i][2] = avgArr7[ranX][ranY];
			testArr[i][3] = stDev3[ranX][ranY];
			testArr[i][4] = stDev5[ranX][ranY];
			testArr[i][5] = stDev7[ranX][ranY];
			testArr[i][6] = maxArr3[ranX][ranY];
			testArr[i][7] = maxArr5[ranX][ranY];
			testArr[i][8] = maxArr7[ranX][ranY];
			testArr[i][9] = monoArr[ranX][ranY];
			testArr[i][10] = col.getRed(); //grab red, since its a greyscale image
			//testArr[i][10] = 20;
		}		
	}
	
	
	public void coordSetup(int trainSize, int posPix, BufferedImage img){
		//we take the full training size, subtrack positives, to get negatives
		Random rand = new Random();
		int negPix = trainSize - posPix;
		int x = 0;
		int y = 0;
		for (int i = 0; i<posPix; i++){
			//random x,y val for top left picture
			x = rand.nextInt(255+1);
			y = rand.nextInt(255+1);
			trainCoords.add(new Point(x,y));
		}
		x = 0;
		y = 0;
		for (int j = 0; j<negPix; j++){
			x = rand.nextInt((512-256)+1) + 255;
			y = rand.nextInt((512-256)+1) + 255;
			trainCoords.add(new Point(x,y));
		}
		
		//Add everything for the testing, 
		for (int i = 0; i < 512; i++){
			for (int j = 0; j < 512;j++){
				testCoords.add(new Point(i,j));
			}
		}		
		//then remove the training points
		System.out.println("testcoord size before "+testCoords.size());
		(testCoords).removeAll(trainCoords);
		System.out.println("testcoord size aftter "+testCoords.size());
		//Shuffle coordinates
		Collections.shuffle(trainCoords);
		Collections.shuffle(testCoords);		
	}

	
	
	// ---------------------- INPUT ADDITIONS END ----------------------

	public void setup(final EvolutionState state, final Parameter base) {
		// very important, remember this
		super.setup(state, base);

		// verify our input is the right class (or subclasses from it)
		if (!(input instanceof DoubleData))
			state.output.fatal("GPData class must subclass from "
					+ DoubleData.class, base.push(P_DATA), null);
		
		// ---------------------- INPUT ADDITIONS --------------------
		setSize = state.parameters.getInt(base.push(P_SIZE), null, 1);
		if (setSize < 1){
			state.output.fatal(
					"Training Set Size must be an integer greater than 0",
					base.push(P_SIZE));
					setSize = 100;
		}

		
		try{
			state.output.message("Attempting to read Image");		

			// Read from an input stream
			InputStream is = state.parameters.getResource(base.push(P_FILE),null);

			ib = ImageIO.read(is);
			
			posPix = 6500;
			trainSize = 10000;									
			
			//3x3
			avgArr3 = new int[ib.getWidth()][ib.getHeight()];
			stDev3 = new double[ib.getWidth()][ib.getHeight()];
			//5x5
			avgArr5 = new int[ib.getWidth()][ib.getHeight()];
			stDev5 = new double[ib.getWidth()][ib.getHeight()];
			//7x7
			avgArr7 = new int[ib.getWidth()][ib.getHeight()];
			stDev7 = new double[ib.getWidth()][ib.getHeight()];
			//Monochrome
			monoArr = new int[ib.getWidth()][ib.getHeight()];
			//Max
			maxArr3 = new int[ib.getWidth()][ib.getHeight()];
			maxArr5 = new int[ib.getWidth()][ib.getHeight()];
			maxArr7 = new int[ib.getWidth()][ib.getHeight()];
			//Computations
			Avg3(ib, avgArr3, stDev3);
			getAvgK(ib,avgArr5, stDev5, 5);
			getAvgK(ib,avgArr7, stDev7, 7);
			getMono(ib, monoArr);
			getMaxK(ib, maxArr3, 3);
			getMaxK(ib, maxArr5, 5);
			getMaxK(ib, maxArr7, 7);

			coordSetup(trainSize, posPix, ib);
			trainerSetSize = trainSize;
			
			//testing things, currently set for only 1000 elements
			testArr = new double[2000][11];
			createSet(ib,testArr);	
			
			testingOutputs= new double[262144];
		}catch(IOException e){}
		
		testingSetSize = testCoords.size();
		
		
		state.output.message("SetSize " + setSize);
		state.output.message("TestingSet size " + testingSetSize);
		state.output.message("TrainerSet size " + trainerSetSize);

		
		trainerSet = new double[trainerSetSize][12];		
		testingSet = new double[testingSetSize][12];
		outputs = new double[setSize];
		types = new String[11];
		trainerSetCounter = 0;
		testingSetCounter = 0;
		
		state.output.message("testing set size "+testingSetSize);

		double rand = 0;

				
				types[0] = "Avg3";
				types[1] = "Avg5";
				types[2] = "Avg7";
				types[3] = "StDev3";
				types[4] = "StDev5";
				types[5] = "StDev7";				
				types[6] = "Max3";
				types[7] = "Max5";
				types[8] = "Max7";	
				types[9] = "Mono";
				types[10] = "Color";
				//trainer size 
				int trainingIndex = 0;
				Color col;
				
				
				for (Point coord: trainCoords) {
					int x = coord.x;
					int y = coord.y;
					col = new Color(ib.getRGB(x,y));
					trainerSet[trainingIndex][0] = avgArr3[x][y];	
					trainerSet[trainingIndex][1] = avgArr5[x][y];
					trainerSet[trainingIndex][2] = avgArr7[x][y];
					trainerSet[trainingIndex][3] = stDev3[x][y];
					trainerSet[trainingIndex][4] = stDev5[x][y];
					trainerSet[trainingIndex][5] = stDev7[x][y];							
					trainerSet[trainingIndex][6] = maxArr3[x][y];
					trainerSet[trainingIndex][7] = maxArr5[x][y];
					trainerSet[trainingIndex][8] = maxArr7[x][y];
					trainerSet[trainingIndex][9] = monoArr[x][y];
					trainerSet[trainingIndex][10] = col.getRed(); //color	
					//is it a positive pixel or negative?
					if (x < 256 && y < 256){
						trainerSet[trainingIndex][11] = 1;						
					} else {
						trainerSet[trainingIndex][11] = -1;	
					}//negative
					trainingIndex++;	
				}
				int testIndex = 0;		
				System.out.println("test cooords "+testCoords.size());
				for (Point coord: testCoords) {
					int x = coord.x;
					int y = coord.y;						
					col = new Color(ib.getRGB(x,y));
					testingSet[testIndex][0] = avgArr3[x][y];	
					testingSet[testIndex][1] = avgArr5[x][y];
					testingSet[testIndex][2] = avgArr7[x][y];
					testingSet[testIndex][3] = stDev3[x][y];
					testingSet[testIndex][4] = stDev5[x][y];
					testingSet[testIndex][5] = stDev7[x][y];							
					testingSet[testIndex][6] = maxArr3[x][y];
					testingSet[testIndex][7] = maxArr5[x][y];
					testingSet[testIndex][8] = maxArr7[x][y];
					testingSet[testIndex][9] = monoArr[x][y];
					testingSet[testIndex][10] = col.getRed(); //color	
					//is it a positive pixel or negative?
					if (x < 256 && y < 256){
						testingSet[testIndex][11] = 1;						
					} else {
						testingSet[testIndex][11] = -1;	
					}//negative
					testIndex++;
				}

		// ---------------------- INPUT ADDITIONS ----------------------
		
		
	}

	public void evaluate(final EvolutionState state, final Individual ind,
			final int subpopulation, final int threadnum) {
		if (!ind.evaluated) // don't bother reevaluating
		{
			DoubleData input = (DoubleData) (this.input);

			int hits = 0;
			double sum = 0.0;
			double expectedResult;
			double result;
				
			for (int y = 0; y < trainerSetSize; y++) {
								
				term1 = trainerSet[y][0];
				term2 = trainerSet[y][1];
				term3 = trainerSet[y][2];
				term4 = trainerSet[y][3];
				term5 = trainerSet[y][4];
				term6 = trainerSet[y][5];
				term7 = trainerSet[y][6];
				term8 = trainerSet[y][7];
				term9 = trainerSet[y][8];
				term10 = trainerSet[y][9];
				term11 = trainerSet[y][10];				
								
				expectedResult = trainerSet[y][11];			
						
				((GPIndividual) ind).trees[0].child.eval(state, threadnum,
						input, stack, ((GPIndividual) ind), this);
					
				
				if(input.x < 0 && expectedResult < 0) {
					hits++; 		
				} 
				
				if(input.x > 0 && expectedResult > 0) { 
					hits++;
				}
				
			}
			result = (((double)trainerSetSize - (double)hits)/(double)trainerSetSize);
			KozaFitness f = ((KozaFitness) ind.fitness);
			f.setStandardizedFitness(state, result);
			f.hits = hits;
			ind.evaluated = true;
		}
	}
			
	final static double PROBABLY_ZERO = 1.11E-15;
    final static double BIG_NUMBER = 1.0e15;                // the same as lilgp uses

    /** Returns the error between the result and the expected result of a single
        data point. */
    public double error(double result, double expectedResult)
        {
        double delta = Math.abs(result - expectedResult);

        // It's possible to get NaN because cos(infinity) and
        // sin(infinity) are undefined (hence cos(exp(3000)) zings ya!)
        // So since NaN is NOT =,<,>,etc. any other number, including
        // NaN, we're CAREFULLY wording our cutoff to include NaN.

        if (! (delta < BIG_NUMBER ) )   // *NOT* (delta >= BIG_NUMBER)
            delta = BIG_NUMBER;

        // very slight math errors can creep in when evaluating
        // two equivalent by differently-ordered functions, like
        // x * (x*x*x + x*x)  vs. x*x*x*x + x*x
        // So we're assuming that very small values are actually zero

        else if (delta < PROBABLY_ZERO)  // slightly off
            delta = 0.0;
        return delta;
        }
			
		//describe func
		public void describe(EvolutionState state, Individual ind, int subpopulation, int threadnum, int log)
        {
        DoubleData input = (DoubleData)(this.input);

        // we do the testing set here
        
        state.output.println("\n\nPerformance of Best Individual on Testing Set:\n", log);
                
        int hits = 0;
        double sum = 0.0;
        for (int y=0;y<testingSetSize;y++)
            {
            currentValue = testingSet[y][11];
            ((GPIndividual)ind).trees[0].child.eval(
                state,threadnum,input,stack,((GPIndividual)ind),this);

            double error = error(input.x, testingOutputs[y]);
                        
            // We'll keep the auxillary hits measure for tradition only 
            final double HIT_LEVEL = 0.01;
            //if (error <= HIT_LEVEL) hits++; 
			if(input.x < 0 && currentValue < 0) {
				hits++; 		
			} 				
			if(input.x > 0 && currentValue > 0) { 
				hits++;
			} 

            //sum += error;              
            }
                        
        // the fitness better be KozaFitness!
		sum = ((testingSetSize - hits)/testingSetSize);
        KozaFitness f = (KozaFitness)(ind.fitness.clone());     // make a copy, we're just printing it out
        f.setStandardizedFitness(state, sum);
        f.hits = hits;
                
        f.printFitnessForHumans(state, log);
        }
			
}
