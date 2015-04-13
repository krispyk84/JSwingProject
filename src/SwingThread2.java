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
		int x = 0;
		while(x < 100000000){
			try {
				BasicSwing.jl.setText("BITFINEX: " + "X:" + x + " : " + readUrl("https://api.bitfinex.com/v1/ticker/btcusd"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
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
