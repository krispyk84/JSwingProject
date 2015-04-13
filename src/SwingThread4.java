import java.util.*;
import java.io.*;
import java.net.URL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SwingThread4 implements Runnable{
	//Do not make public/static because then they'll merge the threads
	int start;
	int stop;
	String file;
	boolean lead = false;
	static String currentString = "Hey!";
	
	//OK COIN PUBLIC API DATA
	
	@Override
	public void run() {
		
		SSLContext ctx = null;
        TrustManager[] trustAllCerts = new X509TrustManager[]{new X509TrustManager(){
            public X509Certificate[] getAcceptedIssuers(){return null;}
            public void checkClientTrusted(X509Certificate[] certs, String authType){}
            public void checkServerTrusted(X509Certificate[] certs, String authType){}
        }};
        try {
            ctx = SSLContext.getInstance("SSL");
            ctx.init(null, trustAllCerts, null);
        } catch (Exception e) {
        	System.out.println("Didn't work");
        }

        SSLContext.setDefault(ctx);
		String[] headers = {"Date: ", "Buy: ", "High: ", "Last: ", "Low: ", "Sell: ", "Volume: "};
		//{"date":"1428907588","ticker":{"buy":"235.88","high":"237.38","last":"235.91","low":"233.43","sell":"235.96","vol":"8658.663"}}
        try {
			String[] apiData = readUrl("https://www.okcoin.com/api/ticker.do?ok=1").split(",");
			for(int i = 0; i<apiData.length; i++){
				apiData[i] = apiData[i].replaceAll("[^0-9.,]+","");
				System.out.println(headers[i] + apiData[i]);
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        		
		
		// TODO Auto-generated method stub
		int x = 0;
		while(x < 100000000){
			try {
				BasicSwing.jl4.setText("OK-COIN: " + "X:" + x + " : " + readUrl("https://www.okcoin.com/api/ticker.do?ok=1"));
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