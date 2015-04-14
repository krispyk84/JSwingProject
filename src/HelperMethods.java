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
	
}
