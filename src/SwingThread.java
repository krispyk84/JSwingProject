import java.util.*;
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
	
			int x = 0;
			
			String[] headers = {"High: ", "Last: ", "TimeStamp: ", "Bid: ", "VWAP: ", "Volume: ", "Low: ", "Ask: "};
			String tempOldData = "";
	
			while(x < 100){
				try {
					String apiDataFull = readUrl("https://www.bitstamp.net/api/ticker/");
					if(!apiDataFull.equals(tempOldData)){
						tempOldData = apiDataFull;
						bw.write(apiDataFull);
						bw.newLine();
						String[] apiData = apiDataFull.split(",");
						for(int i = 0; i< apiData.length; i++){
							apiData[i] = apiData[i].replaceAll("[^0-9.,]+","");
							
						}
						BasicSwing.marketsHeaderBitstamp.setText("X:" + x + "\n" +
								headers[0] + apiData[0] + "\n" + 
								headers[1] + apiData[1] + "\n" + 
								headers[2] + apiData[2] + "\n" + 
								headers[3] + apiData[3] + "\n" + 
								headers[5] + apiData[5] + "\n" + 
								headers[6] + apiData[6] + "\n" + 
								headers[7] + apiData[7]);
					}
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				x++;			 
			}	
			
			bw.close();
		
		} catch (IOException e) {
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
