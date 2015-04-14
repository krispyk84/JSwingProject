import java.io.*;
import java.net.URL;

public class SwingThread implements Runnable{
	//Do not make public/static because then they'll merge the threads
	int start;
	int stop;
	String file;
	boolean lead = false;
	static String currentString = "Hey!";
	
	//BITSTAMP PUBLIC API DATA
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		FileWriter fw;
		BufferedWriter bw = null;
		try {
	        File file = new File("bitstampHistoricData.txt");
	        if (!file.exists()) {
				file.createNewFile();
			}
	
			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
	
			String[] headers = {"High: ", "Last: ", "TimeStamp: ", "Bid: ", "VWAP: ", "Volume: ", "Low: ", "Ask: "};
	
			long startTime = System.currentTimeMillis();
			while((System.currentTimeMillis()-startTime) < 604800000){
				Thread.sleep(5000);
				String apiDataFull = HelperMethods.readUrl("https://www.bitstamp.net/api/ticker/");
				bw.write(apiDataFull);
				bw.newLine();
				bw.write(System.currentTimeMillis()+"");
				bw.newLine();
				bw.flush();
				String[] apiData = apiDataFull.split(",");
				for(int i = 0; i< apiData.length; i++){
					apiData[i] = apiData[i].replaceAll("[^0-9.,]+","");
				}
				apiData[2] = HelperMethods.TimestampToDate(apiData[2]);
				BasicSwing.marketsHeaderBitstamp.setText(
						headers[1] + apiData[1] + "\n" +
						headers[3] + apiData[3] + "\n" +
						headers[7] + apiData[7] + "\n" +
						headers[5] + apiData[5] + "\n" +
						apiData[2] + "\nUpdated Every 5 sec");
				if(BasicSwing.currentMarketTrading == 0){
					BasicSwing.currentMarketPrice = Double.parseDouble(apiData[7]);
					BasicSwing.usdBtcEquivalent.setText("Buys: BTC "+ (BasicSwing.currentUSDBalance/(Double.parseDouble(apiData[7]))));
					BasicSwing.btcUsdEquivalent.setText("Sells For: $" + (BasicSwing.currentBTCBalance*Double.parseDouble(apiData[7])));
				}
			}	
			
			bw.close();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	private static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } finally {
	        if (reader != null)
	            reader.close();
	    }
	}
	*/
	public static String getCurrentString(){
		String current = currentString;
		return current;
	}

	
	
}
