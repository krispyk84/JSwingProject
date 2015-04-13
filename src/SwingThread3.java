import java.io.*;
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
			
	        String[] headers = {"High: ", "Low: ", "Average: ", "Volume: ", "VolCur: ", "Last: ", "Buy: ", "Sell: ", "Updated: ", "TimeStamp: "};
			
			// TODO Auto-generated method stub
			int x = 0;
			
			long startTime = System.currentTimeMillis();
			
			while((System.currentTimeMillis()-startTime) < 604800000){
				Thread.sleep(5000);
				String apiDataFull = readUrl("https://btc-e.com/api/2/btc_usd/ticker");
				bw.write(apiDataFull);
				bw.newLine();
				bw.write(System.currentTimeMillis()+"");
				bw.newLine();
				bw.flush();
				String[] apiData = apiDataFull.split(",");
				for(int i = 0; i< apiData.length; i++){
					apiData[i] = apiData[i].replaceAll("[^0-9.,]+","");				
				}
				BasicSwing.marketsHeaderBtcE.setText("X:" + x + "\n" +
					headers[0] + apiData[0] + "\n" + 
					headers[1] + apiData[1] + "\n" + 
					headers[2] + apiData[2] + "\n" + 
					headers[3] + apiData[3] + "\n" + 
					headers[4] + apiData[4] + "\n" + 
					headers[5] + apiData[5] + "\n" +
					headers[6] + apiData[6] + "\n" +
					headers[7] + apiData[7] + "\n" +
					headers[8] + apiData[8] + "\n" +
					headers[9] + apiData[9]);
				x++;			 
			}
			bw.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
