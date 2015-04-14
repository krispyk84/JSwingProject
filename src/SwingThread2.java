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
		FileWriter fw;
		BufferedWriter bw = null;
		try {
	        File file = new File("bitFinexHistoricalData.txt");
	        if (!file.exists()) {
				file.createNewFile();
			}
			
			fw = new FileWriter(file.getAbsoluteFile());
			bw = new BufferedWriter(fw);
			
			String[] headers = {"Mid: ", "Bid: ", "Ask: ", "Last: ", "TimeStamp: "};
			
			// TODO Auto-generated method stub
			int x = 0;
			long startTime = System.currentTimeMillis();
			while((System.currentTimeMillis()-startTime) < 604800000){
				Thread.sleep(5000);
				String apiDataFull = HelperMethods.readUrl("https://api.bitfinex.com/v1/ticker/btcusd");
				bw.write(apiDataFull);
				bw.newLine();
				bw.write(System.currentTimeMillis()+"");
				bw.newLine();
				bw.flush();
				String[] apiData = apiDataFull.split(",");
				for(int i = 0; i< apiData.length; i++){
					apiData[i] = apiData[i].replaceAll("[^0-9.,]+","");				
				}
				apiData[4] = HelperMethods.TimestampToDate(apiData[4]);
				BasicSwing.marketsHeaderBitfinex.setText(
						headers[3] + apiData[3] + "\n" +
						headers[1] + apiData[1] + "\n" +
						headers[2] + apiData[2] + "\n" +
						"Volume: N-A \n" + 
						apiData[4] + "\nUpdated Every 5 sec");

				x++;			 
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
