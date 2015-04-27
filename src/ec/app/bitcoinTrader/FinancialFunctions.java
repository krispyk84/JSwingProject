package ec.app.bitcoinTrader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class FinancialFunctions {

	// Takes the average price over X seconds
	public static double averageOverX(int seconds, int current, String[] homeMarket) {
		double average = 0.0;
		if ((current - seconds) >= 0) {
			for (int i = current; i >= current - seconds; i--) {
				average += Double.parseDouble(homeMarket[i]);
			}

			average = average / seconds;
		}
		return average;
	}

	// Rate of change calculation of X seconds
	public static double rateOfChangeOverX(int seconds, int current) {
		double roc = 0.0;

		if ((current - seconds) >= 0) {
			double endPrice = Double.parseDouble(MultiValuedRegression.bitStampRecords[current]);
			double startPrice = Double.parseDouble(MultiValuedRegression.bitStampRecords[current-seconds]);
			roc = (endPrice / startPrice) * 100;
		}
		return roc;
	}

	// Returns the relative strength index from the relative strength
	// calculation
	public static double relativeStrengthIndexOverN(int seconds, int current) {
		return (100 - (100 / (1 + relativeStrength(seconds, current))));
	}

	// Relative strength calculation, which takes the sum of returns from
	// positive seconds in a window of X seconds, and the sum of negative
	// returns from the same window, and returns the calculation below
	public static double relativeStrength(int seconds, int current) {
		double relativeStrength = 0.0;
		double positiveSumReturn = 0;
		double negativeSumReturn = 0;
		if ((current + seconds) <= MultiValuedRegression.totalSize && (current - 1) >= 0) {
			for (int i = 0; i < seconds; i++) {
				double currentSecond = Double.parseDouble(MultiValuedRegression.bitStampRecords[current + i]);
				double previousSecond = Double.parseDouble(MultiValuedRegression.bitStampRecords[current + i - 1]);
				double returnValue = currentSecond - previousSecond;
				if (returnValue > 0) {
					positiveSumReturn += returnValue;
				} else if (returnValue < 0) {
					negativeSumReturn += returnValue;
				} else {
					// Do nothing
				}
			}
			relativeStrength = (positiveSumReturn / (-1 * negativeSumReturn));
		}

		return relativeStrength;
	}

	// MACD Function, in two parts, requires EMA value, which is the exponential
	// moving average
	public static double exponentialMovingAverage(int seconds, int current, String[] homeMarket) {
		double ema = 0.0;
		if ((current - seconds) >= 0) {
			double secondsAverage = averageOverX(seconds, current - seconds, homeMarket);
			double currentClosingPrice = Double.parseDouble(homeMarket[current]);
			ema = currentClosingPrice * (2 / (seconds + 1)) + secondsAverage * (1 - (2 / (seconds + 1)));
		}
		return ema;
	}

	public static double MACD(int current, String[] homeMarket) {
		double macd = exponentialMovingAverage(12, current, homeMarket) - exponentialMovingAverage(26, current, homeMarket);
		return macd;
	}

	// Maximum Value over X seconds
	public static double maxValueOverX(int seconds, int current) {
		double max = 0.0;
		if ((current - seconds) >= 0) {
			for (int i = current; i >= (current - seconds); i--) {
				if (Double.parseDouble(MultiValuedRegression.bitStampRecords[i]) > max) {
					max = Double.parseDouble(MultiValuedRegression.bitStampRecords[i]);
				}
			}
		}
		return max;
	}

	// Minimum Value over X seconds
	public static double minValueOverX(int seconds, int current, String[] homeMarket) {
		double min = Double.MAX_VALUE;
		if ((current - seconds) >= 0) {
			for (int i = current; i >= (current - seconds); i--) {
				if (Double.parseDouble(homeMarket[i]) < min) {
					min = Double.parseDouble(homeMarket[i]);
				}
			}
		}
		return min;
	}

	// AverageMarketVariance between two markets, home market & other, this is
	// simply used to see what the average variance is between two markets, so
	// when the price between two markets grows to above average, or drops to
	// below average levels, we can take action
	public static double averageMarketVariance(String[] homeMarket,	String[] otherMarket) {
		int numberOfRecords = homeMarket.length;

		double differenceSum = 0;

		for (int i = 0; i < numberOfRecords; i++) {
			differenceSum += Math.abs(Double.parseDouble(homeMarket[i]) - Double.parseDouble(otherMarket[i]));
		}

		return differenceSum / numberOfRecords;
	}

	// CurrentVarianceValue, this is the function that checks to see if a spike
	// or crash is happening on one market and has yet to hit the home market
	public static double currentVarianceValue(String[] homeMarket, String[] otherMarket, double averageDiff, int current, int seconds) {
		double averageHome = averageOverX(seconds, current, homeMarket);
		double averageAway = averageOverX(seconds, current, otherMarket);
		double difference = averageAway - averageHome;
		double toReturn = 0.0;

		if (Math.abs(difference) > Math.abs(averageDiff)) {
			toReturn = difference;
		}
		return toReturn;
	}

	// Calculates the volatility or standard deviation of the price over x time
	public static double volatility(int seconds, int current, String[] homeMarket) {
		double standardDeviation = 0.0;
		if ((current - seconds) >= 0) {
			double mean = averageOverX(seconds, current, homeMarket);
			double sumOfSquares = 0.0;

			for (int i = current; i >= current - seconds; i--) {
				sumOfSquares += Math.pow((Double.parseDouble(homeMarket[i]) - mean), 2);
			}
			standardDeviation = Math.sqrt(sumOfSquares);
		}
		return standardDeviation;
	}

	public static String[] buildRecordsArray(String source) {
		String[] toReturn = new String[MultiValuedRegression.totalSize];
		try {
			@SuppressWarnings("resource")
			BufferedReader br = new BufferedReader(new FileReader(source));
			int i = 0;
			String line = "";
			while ((line = br.readLine()) != null && i < MultiValuedRegression.totalSize) {
				toReturn[i] = line;
				i++;
			}
		} catch (IOException e) {
		}
		return toReturn;
	}

	public static int buyHoldSell(String[] homeMarket, int current) {
		// -1 is sell, 0 is hold, 1 is buy
		if ((current + 35) < homeMarket.length) {
			double currentPrice = Double.parseDouble(homeMarket[current]);
			double priceIn5Min = Double.parseDouble(homeMarket[current + 35]);
			if ((priceIn5Min - currentPrice) > calcFee(currentPrice, 0.005)) {
				return 1;
			} else if ((priceIn5Min - currentPrice) < calcFee(currentPrice, 0.0025)) {
				return -1;
			} else {
				return 0;
			}
		} else {
			return 0;
		}
	}

	public static double calcFee(double currentPrice, double feeInPercent) {
		double fee = feeInPercent * currentPrice;
		return fee;
	}
}
