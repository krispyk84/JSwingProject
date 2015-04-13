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
import java.io.*;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import javax.imageio.ImageIO;
import java.awt.Color;

public class MultiValuedRegression extends GPProblem implements
		SimpleProblemForm {

	public Random rand = new Random();

	private static final long serialVersionUID = 1;

	public static final String P_SIZE = "size";
	public static final String P_FILE = "file";
	public static final String P_POS = "pos";
	public static final String P_TRAIN = "trainingSetSize";
	public static final String P_IMAGE = "image";
	public static final String P_SQUARE = "square";

	public int shiftX = 0;
	public int shiftY = 0;
	public int square;

	public static int totalSize;
	public int trainingSetSize;
	public int testingSetSize;

	private BufferedImage imageBuffer = null;
	private int imageWidth;
	private int imageHeight;

	// TERMINALS
	public double currentAverageFor5x5;
	public double currentAverageFor7x7;
	public double StandardDeviationFor7x7;
	public double StandardDeviationFor11x11;
	public double maxFor3x3;
	public double maxFor5x5;
	public double currentValue;
	public double currentQuality;
	public double entropy7x7;
	public double entropy11x11;
	public double guassian3x3;
	public double guassian5x5;
	public double currentMono;

	public double training[][];
	public double testing[][];

	ArrayList<Point> trainingCoordinates;
	ArrayList<Point> testingCoordinates;

	public void setup(final EvolutionState state,

	final Parameter base) {
		super.setup(state, base);
		totalSize = state.parameters.getInt(base.push(P_SIZE), null, 1);
		trainingSetSize = state.parameters.getInt(base.push(P_TRAIN), null, 1);
		testingSetSize = totalSize - trainingSetSize;

		if (trainingSetSize < 1)
			state.output.fatal(
					"Training Set Size must be an integer greater than 0",
					base.push(P_TRAIN));

		square = state.parameters.getInt(base.push(P_SQUARE), null, 1);

		//This is the quadrant picker. If a certain square is set in params, it sets the shift value
		if (square == 2) {
			shiftX = 256;
			shiftY = 0;
		} else if (square == 3) {
			shiftX = 0;
			shiftY = 256;
		} else if (square == 4) {
			shiftX = 256;
			shiftY = 256;
		}

		// Receiving the image
		try {
			InputStream imageFile = state.parameters.getResource(
					base.push(P_IMAGE), null);
			imageBuffer = ImageIO.read(imageFile);
		} catch (IOException e) {
		}

		//Set the image width and height variables
		imageWidth = imageBuffer.getWidth();
		imageHeight = imageBuffer.getHeight();

		int numberOfPositives = state.parameters.getInt(base.push(P_POS), null,
				1);
		int numberOfNegatives = trainingSetSize - numberOfPositives;
		//
		training = new double[trainingSetSize][11];
		testing = new double[testingSetSize][11];
		trainingCoordinates = new ArrayList<Point>(); // X & Y values that we
														// use in the image
		testingCoordinates = new ArrayList<Point>(); // DITTO

		ArrayList<Point> posCoordinates = new ArrayList<Point>();
		ArrayList<Point> negCoordinates = new ArrayList<Point>();

		if (imageBuffer != null) {
			for (int x = 0; x < 256; x++) {
				for (int y = 0; y < 256; y++) {
					posCoordinates.add(new Point(x + shiftX, y + shiftY));
				}
			}

			for (int x = 0; x < 512; x++) {
				for (int y = 0; y < 512; y++) {
					negCoordinates.add(new Point(x, y));
				}
			}
			negCoordinates.removeAll(posCoordinates);
			
			//Shuffles the coordinate values for randomization	
			Collections.shuffle(posCoordinates);
			Collections.shuffle(negCoordinates);

			//Takes the first number of positives and negatives from the shuffled set as the random training points
			for (int i = 0; i < numberOfPositives; i++) {
				Point c = posCoordinates.get(i);
				trainingCoordinates.add(c);
			}

			for (int i = 0; i < numberOfNegatives; i++) {
				Point c = negCoordinates.get(i);
				trainingCoordinates.add(c);
			}

			//One last shuffle of the training coordinates
			Collections.shuffle(trainingCoordinates);

		}

		//Set the training terminal nodes by calling the filter functions
		int i = 0;
		for (Point p : trainingCoordinates) {
			int x = p.x;
			int y = p.y;
			//
			training[i][0] = averageArea(5, x, y);
			training[i][1] = averageArea(7, x, y);
			training[i][2] = standardDev(7, x, y);
			training[i][3] = standardDev(11, x, y);
			training[i][4] = new Color(imageBuffer.getRGB(x, y)).getRed();
			training[i][5] = quality(x, y);
			training[i][6] = entropy(7, x, y);
			training[i][7] = entropy(11, x, y);
			training[i][8] = guassian(3, x, y);
			training[i][9] = guassian(5, x, y);
			training[i][10] = monochrome(x,y);
			i++;
		}

		// ///////// Testing set calculated here
		// populate testing set with all possible pixels
		for (int x = 0; x < imageWidth; x++) {
			for (int y = 0; y < imageWidth; y++) {
				testingCoordinates.add(new Point(x, y));
			}
		}
		
		//Remove the training coordinates from the testing coordinates so they are not used
		testingCoordinates.removeAll(trainingCoordinates);
		// shuffle the coordinates selected
		Collections.shuffle(testingCoordinates);
		i = 0;
		double enti = entropy(256, (256 / 2), (256 / 2));
		for (Point p : testingCoordinates) {
			int x = p.x;
			int y = p.y;
			//
			testing[i][0] = averageArea(5, x, y);
			testing[i][1] = averageArea(7, x, y);
			testing[i][2] = standardDev(7, x, y);
			testing[i][3] = standardDev(11, x, y);
			testing[i][4] = new Color(imageBuffer.getRGB(x, y)).getRed(); 
			testing[i][5] = quality(x, y);
			testing[i][6] = entropy(7, x, y);
			testing[i][7] = entropy(11, x, y);
			testing[i][8] = guassian(3, x, y);
			testing[i][9] = guassian(5, x, y);
			testing[i][10] = monochrome(x,y);
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
			double expectedResult;
			double result;

			for (int y = 0; y < trainingSetSize; y++) {
				//Set the variables with the training data
				currentAverageFor5x5 = training[y][0];
				currentAverageFor7x7 = training[y][1];
				StandardDeviationFor7x7 = training[y][2];
				StandardDeviationFor11x11 = training[y][3];
				currentValue = training[y][4];
				currentQuality = training[y][5];
				entropy7x7 = training[y][6];
				entropy11x11 = training[y][7];
				guassian3x3 = training[y][8];
				guassian5x5 = training[y][9];
				currentMono = training[y][10];
				//Expected result is the current location of the pixel
				expectedResult = currentQuality;

				((GPIndividual) ind).trees[0].child.eval(state, threadnum,
						input, stack, ((GPIndividual) ind), this);
				// fitness
				if (input.x < 0 && expectedResult < 0) {
					hits++;
				}

				if (input.x > 0 && expectedResult > 0) {
					hits++;
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
		double error2 = 0.0;
		String colourData = "";
		
		//Create the image to be drawn to with the red, green, yellow and black
		BufferedImage myImage = new BufferedImage(imageBuffer.getWidth(),
				imageBuffer.getHeight(), BufferedImage.TYPE_INT_RGB);
		myImage.getGraphics().drawImage(imageBuffer, 0, 0, null);

		for (int y = 0; y < testingSetSize; y++) {
			
			currentAverageFor5x5 = testing[y][0];
			currentAverageFor7x7 = testing[y][1];
			StandardDeviationFor7x7 = testing[y][2];
			StandardDeviationFor11x11 = testing[y][3];
			currentValue = testing[y][4];
			currentQuality = testing[y][5];
			entropy7x7 = testing[y][6];
			entropy11x11 = testing[y][7];
			guassian3x3 = testing[y][8];
			guassian5x5 = testing[y][9];
			currentMono = testing[y][10];

			int xVal = testingCoordinates.get(y).x;
			int yVal = testingCoordinates.get(y).y;
			String colour = "";

			((GPIndividual) ind).trees[0].child.eval(state, threadnum, input,
					stack, ((GPIndividual) ind), this);

			// TRUE Neg Neg Pos Pos
			if ((input.x < 0 && currentQuality < 0)
					|| (input.x >= 0 && currentQuality >= 0)) {
				positiveHits++;
				// True Pos
				if (input.x >= 0) {
					truePositives++;
					myImage.setRGB(xVal, yVal, Color.green.getRGB());
					// True NEG
				} else {
					trueNegatives++;
					myImage.setRGB(xVal, yVal, Color.black.getRGB());
				}
				// False Neg Pos Pos Neg
			} else if ((input.x < 0 && currentQuality >= 0)
					|| (input.x >= 0 && currentQuality < 0)) {
				negativeHits++;
				// False Neg
				if (input.x < 0) {
					falseNegatives++;
					myImage.setRGB(xVal, yVal, Color.yellow.getRGB());
					// False Pos
				} else {
					falsePositives++;
					myImage.setRGB(xVal, yVal, Color.red.getRGB());
				}
			}
		}

		double error = ((double) testingSetSize - (double) positiveHits)
				/ (double) testingSetSize;
		state.output.println("Positive Hits: " + positiveHits
				+ "\nNegative Hits: " + negativeHits + "\nFalse Positives: "
				+ falsePositives + "\nFalse Negatives: " + falseNegatives
				+ "\nTrue Positives: " + truePositives + "\nTrue Negatives: "
				+ trueNegatives + "\n", log);
		System.out.println(colourData);
		// the fitness better be KozaFitness!
		KozaFitness f = (KozaFitness) (ind.fitness.clone()); // make a copy,
																// we're just
																// printing it
																// out
		f.setStandardizedFitness(state, error);
		f.hits = positiveHits;

		makePNG("myimage.png", myImage);

		f.printFitnessForHumans(state, log);
	}

	public void makePNG(String filename, BufferedImage myImage) {
		try {
			File outputfile = new File(filename);
			ImageIO.write(myImage, "png", outputfile);
		} catch (IOException e) {
		}
	}

	//The standard deviation filter
	public double standardDev(int size, int x, int y) {
		int popSize = size * size;
		double mean = averageArea(size, x, y);
		size = (int) Math.floor(size / 2);
		double result = 0;

		for (int i = -size; i <= size; i++) {
			for (int j = -size; j <= size; j++) {
				if ((x + i) >= 0 && (x + i) < imageWidth) {
					if ((y + j) >= 0 && (y + j) < imageHeight) {
						int d = convertToGreyScale(x + i, y + j);
						result += Math.pow((d - mean), 2);
					}
				}
			}
		}

		return Math.sqrt(result / popSize);
	}
	//Monochrome Filter that converts pixels to black and white
	public int monochrome(int x, int y)
  	{	int greyValue = new Color(imageBuffer.getRGB(x, y)).getRed();
  		if(greyValue > 127){
  			greyValue = 255;
  		} else {
  			greyValue = 0;
  		}
    	return greyValue;
  	}
	//Quality which returns +1 if it is in the quadrant we are training for
	public int quality(int x, int y) {
		if (x <= 256 + shiftX && x >= shiftX) {
			if (y <= 256 + shiftY && y >= shiftY) {
				return 1;
			}
		}
		return -1;
	}
	
	//Calculates the average area values based on a certain size
	public double averageArea(int size, int x, int y) {
		double avg = 0.0;
		int originalSize = size;
		size = (int) Math.floor(size / 2);

		for (int i = -size; i <= size; i++) {
			for (int j = -size; j <= size; j++) {
				if ((x + i) >= 0 && (x + i) < imageWidth) {
					if ((y + j) >= 0 && (y + j) < imageHeight) {
						int d = convertToGreyScale(x + i, y + j);
						avg += d;
					}
				}
			}
		}
		return avg / (originalSize * originalSize);
	}
	//Just returns a single colors value (which is the same for R, G & B) so that it returns just the greyscale integer value
	public int convertToGreyScale(int x, int y) {
		return (new Color(imageBuffer.getRGB(x, y))).getRed();
	}
	//The gaussian blur filter. Creates the mask based on size, then applies it to the pixel data of the image
	public double guassian(int size, int x, int y) {
		double[][] mask = new double[size][size];
		int newSize = (int) Math.floor(size / 2);
		for (int i = -newSize, k = 0; i <= newSize; i++, k++) {
			for (int j = -newSize, l = 0; j <= newSize; j++, l++) {
				double sizeDouble = (double) size / 2;
				mask[k][l] = 2
						* (1 / (2 * Math.PI * Math.pow((sizeDouble), 2)))
						* Math.pow(
								Math.E,
								(-1 * (Math.pow(i, 2) + Math.pow(j, 2)) / (2 * Math
										.pow(sizeDouble, 2))));
			}
		}

		int[][] colourValuesInSquare = new int[size][size];
		int orig = size;
		size = (int) Math.floor(size / 2);
		for (int i = -size, k = 0; i <= size; i++, k++) {
			for (int j = -size, l = 0; j <= size; j++, l++) {
				if ((x + i) >= 0 && (x + i) < imageWidth) {
					if ((y + j) >= 0 && (y + j) < imageHeight) {
						int d = (new Color(imageBuffer.getRGB(x + i, y + j)))
								.getRed();
						colourValuesInSquare[k][l] = d;
					}
				}
			}
		}
		double sum = 0.0;
		for (int i = 0; i < colourValuesInSquare.length; i++) {
			for (int j = 0; j < colourValuesInSquare[0].length; j++) {
				sum += colourValuesInSquare[i][j] * mask[i][j];
			}
		}

		int r = (int) sum / (colourValuesInSquare.length)
				* (colourValuesInSquare[0].length);
		Color c = new Color(r, r, r);
		return c.getRed();
	}
	
	//Entropy Function, puts pixel data value into a histogram P*log2(P) function on every value in the histogram to build the entropy value
	public double entropy(int size, int x, int y) {
		List<String> values = new ArrayList<String>();
		int n = 0;

		Map<Integer, Integer> histogram = new HashMap<>();
		size = (int) Math.floor(size / 2);

		for (int i = -size; i <= size; i++) {
			for (int j = -size; j <= size; j++) {
				if ((x + i) >= 0 && (x + i) < imageWidth) {
					if ((y + j) >= 0 && (y + j) < imageHeight) {
						int d = convertToGreyScale(x + i, y + j);

						if (!values.contains(String.valueOf(d)))
							values.add(String.valueOf(d));
						if (histogram.containsKey(d)) {
							histogram.put(d, histogram.get(d) + 1);
						} else {
							histogram.put(d, 1);
						}
						++n;
					}
				}
			}
		}
		double e = 0.0;
		for (Map.Entry<Integer, Integer> entry : histogram.entrySet()) {
			int cx = entry.getKey();
			double p = (double) entry.getValue() / n;
			e += p * log2(p);
		}
		return e * (-1.0);
	}

	public double log2(double x) {
		return (double) (Math.log(x) / Math.log(2));
	}
	
	//Sobel function, uses the 3x3 sobel mask to apply it to a 3x3 area of pixels
	public int sobel(int size, int x, int y) {
		int[][] sobelX = new int[][] { { -1, 0, 1 }, { -2, 0, 2 }, { -1, 0, 1 } };

		int[][] sobelY = new int[][] { { -1, -2, -1 }, { 0, 0, 0 }, { 1, 2, 1 } };

		int originalSize = size;
		size = (int) Math.floor(size / 2);
		double magnitude = 0.0;
		double sum = 0.0;
		double pixelX = 0.0;
		double pixelY = 0.0;

		if ((x - 1) >= 0 && (x + 1) < imageWidth) {
			if ((y - 1) >= 0 && (y + 1) < imageHeight) {
				int a = new Color(imageBuffer.getRGB(x - 1, y - 1)).getRed();
				int b = new Color(imageBuffer.getRGB(x - 1, y)).getRed();
				int c = new Color(imageBuffer.getRGB(x - 1, y + 1)).getRed();
				int d = new Color(imageBuffer.getRGB(x, y - 1)).getRed();
				int e = new Color(imageBuffer.getRGB(x, y)).getRed();
				int f = new Color(imageBuffer.getRGB(x, y + 1)).getRed();
				int g = new Color(imageBuffer.getRGB(x + 1, y - 1)).getRed();
				int h = new Color(imageBuffer.getRGB(x + 1, y)).getRed();
				int i = new Color(imageBuffer.getRGB(x + 1, y + 1)).getRed();

				pixelX = (sobelX[0][0] * a) + (sobelX[0][1] * d)
						+ (sobelX[0][2] * g) + (sobelX[1][0] * b)
						+ (sobelX[1][1] * e) + (sobelX[1][2] * h)
						+ (sobelX[2][0] * c) + (sobelX[2][1] * f)
						+ (sobelX[2][2] * i);

				pixelY = (sobelY[0][0] * a) + (sobelY[0][1] * d)
						+ (sobelY[0][2] * g) + (sobelY[1][0] * b)
						+ (sobelY[1][1] * e) + (sobelY[1][2] * h)
						+ (sobelY[2][0] * c) + (sobelY[2][1] * f)
						+ (sobelY[2][2] * i);
			}
		}

		magnitude = Math.ceil(Math.sqrt((pixelX * pixelX) + (pixelY * pixelY)));

		int r = (int) magnitude;

		if (r > 255) {
			r = 255;
		} else if (r < 0) {
			r = 0;
		}
		Color c = new Color(r, r, r);
		return c.getRed();
	}
}
