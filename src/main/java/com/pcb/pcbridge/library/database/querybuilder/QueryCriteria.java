package com.pcb.pcbridge.library.database.querybuilder;

/**
 * Represents a single WHERE condition in a query
 */

public class QueryCriteria 
{
	private String _column;
	private String _operator;
	private Object _value;
	
	public QueryCriteria(String column, String operator, Object value)
	{
		this._column 	= column;
		this._operator 	= operator;
		this._value 	= value;
	}
	
	@Override
	public String toString()
	{
		StringBuilder output = new StringBuilder();
		
		return output.append(_column)
			.append(_operator)
			.append(_value)
			.toString();
	}
}
