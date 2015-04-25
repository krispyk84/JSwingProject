package ec.app.Texture;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FinancialFunctions {

	//Takes the average price over X seconds
	public static double averageOverX(int seconds, int start, String[] homeMarket){
		double average = 0.0;
		if((start-seconds) >= 0){
			for(int i = start; i>= start-seconds; i--){
				average += Double.parseDouble(homeMarket[i]);
			}
			
			average = average/seconds;
		}
		return average;
	}
	
	//Rate of change calculation of X seconds
	public static double rateOfChangeOverX(int seconds, int start){
		double roc = 0.0;
		
		if((start-seconds) >=0){
			double endPrice = Double.parseDouble(MultiValuedRegression.bitStampRecords[start]);
			double startPrice = Double.parseDouble(MultiValuedRegression.bitStampRecords[start-seconds]);
			roc = (endPrice/startPrice)*100;
		}
		return roc;
	}
	
	//Returns the relative strength index from the relative strength calculation
	public static double relativeStrengthIndexOverN(int seconds, int start){
		return (100 - (100/(1+relativeStrength(seconds, start))));
	}
	
	//Relative strength calculation, which takes the sum of returns from positive seconds in a window of X seconds, and the sum of negative returns from the same window, and returns the calculation below
	public static double relativeStrength(int seconds, int start){
		double relativeStrength = 0.0;
		double positiveSumReturn = 0;
		double negativeSumReturn = 0;
		if((start+seconds) <= MultiValuedRegression.bitstampNumOfRecords && (start-1) >= 0){
			for(int i = 0; i<seconds; i++){
				double currentSecond = Double.parseDouble(MultiValuedRegression.bitStampRecords[start+i]);
				double previousSecond = Double.parseDouble(MultiValuedRegression.bitStampRecords[start+i-1]);
				double returnValue = currentSecond - previousSecond;
				if(returnValue > 0){
					positiveSumReturn+= returnValue;
				} else if (returnValue < 0){
					negativeSumReturn+= returnValue;
				} else{
					//Do nothing
				}
			}
			relativeStrength = (positiveSumReturn/(-1*negativeSumReturn));
		}
		
		return relativeStrength;
	}
	
	//MACD Function, in two parts, requires EMA value, which is the exponential moving average
	public static double exponentialMovingAverage(int seconds, int start, String[] homeMarket){
		double ema = 0.0;
		if((start-seconds) >= 0){
			double secondsAverage = averageOverX(seconds, start-seconds, homeMarket);
			double currentClosingPrice = Double.parseDouble(homeMarket[start]);
			ema = currentClosingPrice*(2/(seconds+1))+secondsAverage*(1-(2/(seconds+1)));
		}
		
		return ema;
	}
	
	public static double MACD(int start, String[] homeMarket){
		double macd = exponentialMovingAverage(12,start, homeMarket) - exponentialMovingAverage(26,start,homeMarket);
		return macd;
	}
	
	//Maximum Value over X seconds
	public static double maxValueOverX(int seconds, int start){
		double max = 0.0;
		if((start-seconds) >= 0){
			for(int i = start; i>= (start-seconds); i--){
				if(Double.parseDouble(MultiValuedRegression.bitStampRecords[i]) > max){
					max = Double.parseDouble(MultiValuedRegression.bitStampRecords[i]);
				}
			}
		}
		return max;
	}
	
	//Minimum Value over X seconds
	public static double minValueOverX(int seconds, int start){
		double min = Double.MAX_VALUE;
		if((start-seconds) >=0){
			for(int i = start; i >= (start-seconds); i--){
				if(Double.parseDouble(MultiValuedRegression.bitStampRecords[i]) < min){
					min = Double.parseDouble(MultiValuedRegression.bitStampRecords[i]);
				}
			}
		}
		return min;
	}
		
	//AverageMarketVariance between two markets, home market & other, this is simply used to see what the average variance is between two markets, so when the price between two markets grows to above average, or drops to below average levels, we can take action
	public static double averageMarketVariance(String[] homeMarket, String[] otherMarket){
		int numberOfRecords = MultiValuedRegression.bitstampNumOfRecords;
		
		double differenceSum = 0;
		
		for(int i = 0; i< numberOfRecords; i++){
			differenceSum += Math.abs(Double.parseDouble(homeMarket[i]) - Double.parseDouble(otherMarket[i]));
		}
		
		return differenceSum/numberOfRecords;
	}
	
	//CurrentVarianceValue, this is the function that checks to see if a spike or crash is happening on one market and has yet to hit the home market
	public static double currentVarianceValue(String[] homeMarket, String[] otherMarket, double averageDiff, int start, int seconds){
		//double overAllAverage = averageMarketVariance(homeMarket, otherMarket); //Move this to multivalued regression and only calculate once
		double averageHome = averageOverX(seconds, start, homeMarket);
		double averageAway = averageOverX(seconds, start, otherMarket);
		
		double difference = averageHome-averageAway;
		
		if(Math.abs(difference) > 2*MultiValuedRegression.averageDiffBSandOK){
			return difference*1;
		}
		return 0.0;
	}
	
	//Absolute value of the difference between two numbers
	public double norm(double rate1, double rate2){
		return Math.abs(rate1-rate2);
	}
	
	//Calculates the volatility or standard deviation of the price over x time
	public static double volatility(int seconds, int start, String[] homeMarket){
		double standardDeviation = 0.0;
		if((start-seconds) >= 0){
			double mean = averageOverX(seconds, start, homeMarket);
			double sumOfSquares = 0.0;
			
			for(int i = start; i >= start-seconds; i--){
				sumOfSquares += Math.pow((Double.parseDouble(homeMarket[i])-mean),2);
			}
			standardDeviation = Math.sqrt(sumOfSquares);
		}
		return standardDeviation;
	}
	
	public static String[] buildRecordsArray(String source){
		String[] toReturn = new String[MultiValuedRegression.bitstampNumOfRecords];
		try{
			
			String line = "";
			
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(source));
			int i = 0;
			while((line = br.readLine()) != null && i < MultiValuedRegression.bitstampNumOfRecords){
				toReturn[i] = line;
				i++;
			}
			
		} catch (IOException e){
			
		}
		return toReturn;
	}
	
	public static int buyHoldSell(String[] homeMarket, int start){
		//-1 is sell, 0 is hold, 1 is buy
		if(start == 1553860 || (start+300) == 1553860){
			System.out.println("STOP");
		}
		if((start+35) < MultiValuedRegression.bitstampNumOfRecords){
			double currentPrice = Double.parseDouble(homeMarket[start]);
			double priceIn5Min = Double.parseDouble(homeMarket[start+35]);
			if((priceIn5Min-currentPrice) > calcFee(currentPrice, 0.005)){
				return 1;
			} else if ((priceIn5Min-currentPrice) < calcFee(currentPrice, 0.005)){
				return -1;
			} else { return 0; }
		} else {
			return 0;
		}
	}
	
	public static double calcFee(double currentPrice, double feeInPercent){
		double fee = feeInPercent*currentPrice;
		return fee;
	}

		
}
