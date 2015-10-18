package com.pcb.pcbridge.library.database;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A representation of a table row from a SELECT query
 */

public class QueryResultRow 
{
	private final Map<String, Object> _columns;
	
	public QueryResultRow(HashMap<String, Object> data)
	{
		_columns = data;
	}
	
	/**
	 * Gets the value of a column
	 * 
	 * @param key	Column name
	 * @return
	 */
	public Object Get(String key)
	{
		Object cell = _columns.get(key);
		
		if(cell == null)
			throw new NullPointerException("QueryResultRow does not contain column: " + key);
		
		return cell;
	}
	
	@Override
	public String toString()
	{
		StringBuilder output = new StringBuilder();
		
		Iterator<Entry<String, Object>> i = _columns.entrySet().iterator();
		while(i.hasNext())
		{
			Entry<String, Object> column = i.next();
			output.append(column.getKey())
				.append(": ")
				.append(column.getValue());
			
			if(i.hasNext())
				output.append("\n");
		}
		
		return output.toString();
	}
}
