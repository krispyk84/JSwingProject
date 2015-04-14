import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.*;

public class HelperMethods {
	
	public static String TimestampToDate(String unixtime)
	{
		String[] timeStamp = new String[2];
		if(unixtime.contains(".")){
			timeStamp = unixtime.split("\\.");
		} else {
			timeStamp[0] = unixtime;
		}
		Long timestamp = Long.valueOf(timeStamp[0]).longValue();
		Date date = new Date(timestamp*1000L); // *1000 is to convert seconds to milliseconds
		SimpleDateFormat sdf = new SimpleDateFormat("MM:dd:yy:HH:mm:ss"); // the format of your date
		
		sdf.setTimeZone(TimeZone.getTimeZone("GMT-5")); // give a timezone reference for formating (see comment at the bottom
		
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
	public static String readUrl(String urlString) throws Exception {
	    BufferedReader reader = null;
	    try {
	        URL url = new URL(urlString);
	        //added this connection test
	        URLConnection stream = url.openConnection();
	        reader = new BufferedReader(new InputStreamReader(stream.getInputStream()));
	        //reader = new BufferedReader(new InputStreamReader(url.openStream()));
	        StringBuffer buffer = new StringBuffer();
	        int read;
	        char[] chars = new char[1024];
	        while ((read = reader.read(chars)) != -1)
	            buffer.append(chars, 0, read); 

	        return buffer.toString();
	    } catch (MalformedURLException e) { 
	        // new URL() failed
	    	System.err.println("Malformed URL: "+e);
	    	Thread.sleep(1000); //retry connection after 1 second
	    	readUrl(urlString);
			return null; 
	    } 
	    catch (IOException e) {   
	        // openConnection() failed
	    	System.err.println("Connection Error: "+e);
	    	Thread.sleep(1000); //retry connection after 1 second
	    	readUrl(urlString);
			return null; 
	    } 
	    finally {
	        if (reader != null)
	            reader.close();
	    }
	}
	
}
