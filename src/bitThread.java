import java.io.*;
import java.net.URL;

import javax.swing.JTextArea;

public class bitThread implements Runnable{
	//Do not make public/static because then they'll merge the threads
	private String URL;
	private String outputFileName;
	private int updateRate; //seconds eg. 5 = 5000ms.
	private int[] h;
	private JTextArea marketHeader;
	
	public bitThread(String URL, JTextArea marketHeader, String outputFileName, int[] header) 
	{
		this.URL = URL;
		this.outputFileName = outputFileName;
		this.updateRate = 5;
		this.h = header.clone();
		this.marketHeader = marketHeader;
	}
	
	public bitThread(String URL, JTextArea marketHeader, String outputFileName, int[] header, int updateRate) 
	{
		this.URL = URL;
		this.outputFileName = outputFileName;
		this.updateRate = updateRate;
		this.h = header.clone();
		this.marketHeader = marketHeader;
	}
		
	@Override
	public void run() 
	{
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
				marketHeader.append("Volume:" 	+ (h[4] < 0 ? apiData[h[4]] : "N/A") + "\n");
				marketHeader.append(apiData[h[0]] + "\nUpdated Every " + updateRate + "sec");

				if(BasicSwing.currentMarketTrading == 0){
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
