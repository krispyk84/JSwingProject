import java.text.SimpleDateFormat;
import java.util.*;

public class HelperMethods {
	
	public static String TimestampToDate(String unixtime)
	{
		Long timestamp = Long.valueOf(unixtime).longValue();
		Date date = new Date(timestamp*1000L); // *1000 is to convert seconds to milliseconds
		SimpleDateFormat sdf = new SimpleDateFormat("MM:dd:yyyy:HH:mm:ss:z"); // the format of your date
		
		sdf.setTimeZone(TimeZone.getTimeZone("GMT-4")); // give a timezone reference for formating (see comment at the bottom
		
		String formattedDate = sdf.format(date);
		return formattedDate;
	}
	
}
