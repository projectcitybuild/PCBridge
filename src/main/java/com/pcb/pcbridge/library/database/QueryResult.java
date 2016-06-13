package com.pcb.pcbridge.library.database;

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * Represents an immutable result of an adapter SELECT query.
 */

public class QueryResult
{
	private final List<HashMap<String, Object>> _rows;
	
	public QueryResult(List<HashMap<String, Object>> data)
	{
		this._rows = data;
	}
	
	/**
	 * @return Number of rows
	 */
	public int RowCount()
	{
		return _rows.size();
	}
	
	/**
	 * @return Whether table contains any rows
	 */
	public boolean IsEmpty()
	{
		return _rows.size() == 0;
	}
	
	/**
	 * Get the HashMap (cols) at the specified index (row)
	 * 
	 * @param index
	 * @return HashMap of data by columns
	 */
	public HashMap<String, Object> Get(int index)
	{
		return _rows.get(index);
	}
	
	@Override
	public String toString()
	{
		StringBuilder output = new StringBuilder();
		
		int index = 0;
		ListIterator<HashMap<String, Object>> i = _rows.listIterator();
		while(i.hasNext())
		{
			HashMap<String, Object> row = i.next();
			
			output.append("Row ").append(index).append("\n");
			output.append("- ").append(row.toString());
			
			if(i.hasNext())
			{
				output.append("\n---\n");
			}
			
			index++;
		}
		
		return output.toString();
	}
}
