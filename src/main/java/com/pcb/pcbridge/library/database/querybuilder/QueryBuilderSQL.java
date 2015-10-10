package com.pcb.pcbridge.library.database.querybuilder;

import java.util.LinkedList;

/**
 * Represents the final output of a QueryBuilder
 */

public class QueryBuilderSQL
{
	private String _sql;
	private LinkedList<Object> _parameters;
	
	public QueryBuilderSQL(String sql, LinkedList<Object> parameters)
	{
		this._sql = sql;
		this._parameters = parameters;
	}
	
	public String GetSQL() 
	{
		return _sql;
	}
	
	public LinkedList<Object> GetParameters()
	{
		return _parameters;
	}
}
