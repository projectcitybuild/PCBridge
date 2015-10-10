package com.pcb.pcbridge.library.database.querybuilder;

/**
 * Represents a named parameter for use in a prepared statement
 */
public class QueryNamedParameter 
{
	private final Object _value;
	
	public QueryNamedParameter(Object value)
	{
		this._value = value;
	}
	
	public Object GetValue()
	{
		return _value;
	}
}
