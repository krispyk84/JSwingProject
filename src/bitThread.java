import java.io.*;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JTextArea;

public class bitThread implements Runnable{
	//Do not make public/static because then they'll merge the threads
	private String URL;
	private String outputFileName;
	private int updateRate; //seconds eg. 5 = 5000ms.
	private int[] h;
	private JTextArea marketHeader;
	private boolean currentMarket = false;
	
	public bitThread(String URL, JTextArea marketHeader, String outputFileName, int[] header, boolean currentMarket) 
	{
		this.URL = URL;
		this.outputFileName = outputFileName;
		this.updateRate = 1;
		this.h = header.clone();
		this.marketHeader = marketHeader;
		this.currentMarket = currentMarket;
	}
	
	public bitThread(String URL, JTextArea marketHeader, String outputFileName, int[] header, int updateRate, boolean currentMarket) 
	{
		this.URL = URL;
		this.outputFileName = outputFileName;
		this.updateRate = updateRate;
		this.h = header.clone();
		this.marketHeader = marketHeader;
		this.currentMarket = currentMarket;
	}
		
	@Override
	public void run() 
	{
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
			System.out.println(outputFileName);
	        File file = new File(outputFileName);
	        if (!file.exists()) {
				file.createNewFile();
			}
	
			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
	
			long startTime = System.currentTimeMillis();
			while((System.currentTimeMillis()-startTime) < 604800000)
			{
				Thread.sleep(updateRate * 1000);
				String apiDataFull = HelperMethods.readUrl(URL);
				bw.write(apiDataFull);
				bw.newLine();
				bw.write(System.currentTimeMillis()+"");
				bw.newLine();
				bw.flush();
				String[] apiData = apiDataFull.split(",");
				for(int i = 0; i< apiData.length; i++)
				{
					apiData[i] = apiData[i].replaceAll("[^0-9.,]+","");
				}
				apiData[h[0]] = HelperMethods.TimestampToDate(apiData[h[0]]);
				
				marketHeader.setText("");
				marketHeader.append("Last:" 	+ apiData[h[1]] + "\n");
				marketHeader.append("Bid:" 		+ apiData[h[2]] + "\n");
				marketHeader.append("Ask:" 		+ apiData[h[3]] + "\n");
				marketHeader.append("Volume:" 	+ (h[4] >= 0 ? apiData[h[4]] : "N/A") + "\n");
				marketHeader.append(apiData[h[0]] + "\nUpdated Every " + updateRate + " sec");

				if(currentMarket){
					BasicSwing.currentMarketPrice = Double.parseDouble(apiData[h[3]]);
					BasicSwing.usdBtcEquivalent.setText("Buys: BTC "+ (BasicSwing.currentUSDBalance/(Double.parseDouble(apiData[h[3]]))));
					BasicSwing.btcUsdEquivalent.setText("Sells For: $" + (BasicSwing.currentBTCBalance*Double.parseDouble(apiData[h[3]])));
				}
			}	
			
			bw.close();
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
