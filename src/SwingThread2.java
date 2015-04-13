import java.util.*;
import java.io.*;
import java.net.URL;

public class SwingThread2 implements Runnable{
	//Do not make public/static because then they'll merge the threads
	int start;
	int stop;
	String file;
	boolean lead = false;
	static String currentString = "Hey!";
	
	//BITFINEX PUBLIC API DATA
	
	@Override
	public void run() {
		// TODO Auto-generated method stub

		String[] headers = {"Mid: ", "Bid: ", "Ask: ", "Last: ", "TimeStamp: "};
		
		// TODO Auto-generated method stub
		int x = 0;
		while(x < 100000000){
			try {
				String[] apiData = readUrl("https://api.bitfinex.com/v1/ticker/btcusd").split(",");
				for(int i = 0; i< apiData.length; i++){
					apiData[i] = apiData[i].replaceAll("[^0-9.,]+","");
					
				}
				BasicSwing.marketsHeaderBitfinex.setText("X:" + x + "\n" +
						headers[0] + apiData[0] + "\n" + 
						headers[1] + apiData[1] + "\n" + 
						headers[2] + apiData[2] + "\n" + 
						headers[3] + apiData[3] + "\n" + 
						headers[4] + apiData[4] + "\n");
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			x++;			 
		}	
	}
	
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
	
	public static String getCurrentString(){
		String current = currentString;
		return current;
	}

	
	
}
