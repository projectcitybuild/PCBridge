package com.pcb.pcbridge.library.database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

/**
 * A container to get rid of those ugly List<HashMap<String, Object>> declarations.
 * Represents the result of an adapter SELECT query.
 */

public class QueryResult 
{
	private final List<QueryResultRow> _rows;
	
	/**
	 * Convert HashMap<String, Object> into QueryResultRows and store it
	 * 
	 * @param data
	 */
	public QueryResult(List<HashMap<String, Object>> data)
	{
		List<QueryResultRow> table = new ArrayList<QueryResultRow>();
		
		ListIterator<HashMap<String, Object>> i = data.listIterator();
		while(i.hasNext())
		{
			QueryResultRow row = new QueryResultRow(i.next());
			table.add(row);
		}
		
		_rows = table;
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
	
	@Override
	public String toString()
	{
		StringBuilder output = new StringBuilder();
		
		int index = 0;
		ListIterator<QueryResultRow> i = _rows.listIterator();
		while(i.hasNext())
		{
			QueryResultRow row = i.next();
			
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
