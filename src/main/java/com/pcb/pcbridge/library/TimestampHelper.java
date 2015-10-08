package com.pcb.pcbridge.library;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

/**
 * A collection of date/time related methods
 */

public class TimestampHelper 
{
	/**
	 * Calculates the time difference between two UNIX timestamps
	 * then outputs it in a read-friendly format (eg. 1 hour, 3 seconds)
	 * 
	 * @param timestamp1
	 * @param timestamp2
	 * @return
	 */
	public static String GetTimeDifference(long timestamp1, long timestamp2)
	{
		long difference = Math.abs(timestamp1 - timestamp2);

		System.out.print(difference);
		long days = difference / 86400L;
		difference %= 86400L;		
		System.out.print(days + " days");
		
		long hours = difference / 3600L;
		difference %= 3600L;
		
		long minutes = difference / 60L;
		difference %= 60L;
		
		long seconds = difference;
		
		// combine into a read-friendly string
		LinkedHashMap<String, Long> values = new LinkedHashMap<String, Long>();
		if(days > 0) 	values.put("day", days);
		if(hours > 0) 	values.put("hour", hours);
		if(minutes > 0) values.put("min", minutes);
		if(seconds > 0) values.put("sec", seconds);
		
		StringBuilder output = new StringBuilder();
		Iterator<Entry<String, Long>> i = values.entrySet().iterator();
		while(i.hasNext())
		{
			Entry<String, Long> pair = i.next();
			output.append(pair.getValue()).append(" ").append(pair.getKey());
			
			// append plural to text if appropriate
			if((long)pair.getValue() > 1) 
			{
				output.append("s");
			}
			// append delimiter if appropriate
			if(i.hasNext()) 
			{
				output.append(", ");
			}
			
			i.remove();
		}
				
		return output.toString();
	}
	
	public static String GetTimeDifference(Date date1, Date date2)
	{
		return GetTimeDifference(
				date1.getTime() / 1000L, 
				date2.getTime() / 1000L
				);
	}
	
	/**
	 * Returns a UNIX timestamp of the current time
	 * 
	 * @return
	 */
	public static long GetNowTimestamp()
	{
		return GetTimestampFromDate(new Date());
	}
	
	/**
	 * Converts a Date into a UNIX timestamp
	 * 
	 * @param date
	 * @return
	 */
	public static long GetTimestampFromDate(Date date)
	{
		return date.getTime() / 1000L;
	}
	
	/**
	 * Converts a UNIX timestamp into a Date
	 * @param timestamp
	 * @return
	 */
	public static Date GetDateFromTimestamp(long timestamp)
	{
		return new Date(timestamp * 1000L);
	}
	
	public static Date GetDateFromTimestamp(int timestamp)
	{
		return GetDateFromTimestamp((long)timestamp);
	}
}
