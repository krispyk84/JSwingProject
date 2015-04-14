import java.io.*;
import java.net.*;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SwingThread3 implements Runnable{
	//Do not make public/static because then they'll merge the threads
	int start;
	int stop;
	String file;
	boolean lead = false;
	static String currentString = "Hey!";
	
	//BTC-E PUBLIC API DATA
	
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

        FileWriter fw;
		BufferedWriter bw = null;
		try {
	        File file = new File("btcEHistoricalData.txt");
	        if (!file.exists()) {
				file.createNewFile();
			}
	
			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			
	        String[] headers = {"High: ", "Low: ", "Average: ", "Volume: ", "VolCur: ", "Last: ", "Bid: ", "Ask: ", "Updated: ", "TimeStamp: "};
			
			// TODO Auto-generated method stub
			int x = 0;
			
			long startTime = System.currentTimeMillis();
			
			while((System.currentTimeMillis()-startTime) < 604800000){
				Thread.sleep(5000);
				String apiDataFull = HelperMethods.readUrl("https://btc-e.com/api/2/btc_usd/ticker");
				if(apiDataFull == null) { break; } //added this check if returned null value
				bw.write(apiDataFull);
				bw.newLine();
				bw.write(System.currentTimeMillis()+"");
				bw.newLine();
				bw.flush();
				String[] apiData = apiDataFull.split(",");
				for(int i = 0; i< apiData.length; i++){
					apiData[i] = apiData[i].replaceAll("[^0-9.,]+","");				
				}
				apiData[9] = HelperMethods.TimestampToDate(apiData[9]);
				BasicSwing.marketsHeaderBtcE.setText(
					headers[5] + apiData[5] + "\n" + 
					headers[6] + apiData[6] + "\n" + 
					headers[7] + apiData[7] + "\n" + 
					headers[3] + apiData[3] + "\n" + 
					apiData[9] + "\nUpdated Every 5 sec");
				x++;			 
			}
			bw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String getCurrentString(){
		String current = currentString;
		return current;
	}	
}
