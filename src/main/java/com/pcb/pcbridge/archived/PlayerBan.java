/*
 * The MIT License
 *
 * Copyright 2016 Andy Saw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.pcb.pcbridge.archived;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

public class PlayerBan {
	
	public int Id;
	public String PlayerAlias;
	public String PlayerUUID;
	public long Timestamp;
	public String Reason;
	public String StaffAlias;
	public String StaffUUID;
	public String Server;
	public boolean IsActive;
	public Long ExpiryTimestamp;
	
	/**
	 * Returns a string with the remaining time until expiry (eg. 5 hours, 30 minutes)
	 * 
	 * @return
	 */
	public String GetTimeUntilExpiry()
	{
		if(ExpiryTimestamp == null || ExpiryTimestamp == 0)
			return "Never";
		
		long now = System.currentTimeMillis() / 1000L;
		long difference = ExpiryTimestamp - now;
		
		if(difference <= 0)
			return "Expired";

		long days = difference / 86400L;
		difference %= 86400L;
		
		long hours = difference / 3600L;
		difference %= 3600L;
		
		long minutes = difference / 60L;
		difference %= 60L;
		
		long seconds = difference;
		
		// combine into a read-friendly string
		Map<String, Long> values = new LinkedHashMap<String, Long>();
		if(days > 0) 	values.put("day", days);
		if(hours > 0) 	values.put("hour", hours);
		if(minutes > 0) values.put("min", minutes);
		if(seconds > 0) values.put("sec", seconds);
		
		StringBuilder builder = new StringBuilder();
		Iterator<Entry<String, Long>> i = values.entrySet().iterator();
		while(i.hasNext())
		{
			Entry<String, Long> pair = i.next();
			builder.append(pair.getValue())
				.append(" ")
				.append(pair.getKey());
			
			// append plural to text if appropriate
			if((long)pair.getValue() > 1) 
				builder.append("s");
			
			// append delimiter if appropriate
			if(i.hasNext())
				builder.append(", ");
		}
		
		return builder.toString();
	}
	
}
