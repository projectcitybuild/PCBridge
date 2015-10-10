package com.pcb.pcbridge.library.database.querybuilder.operations;

import java.util.LinkedHashMap;

/**
 * Contains the data necessary for a Builder to construct an SQL string
 */

public class SQLBuilderParams 
{
	private LinkedHashMap<String, Object> _parameters;
	private String _table;
	
	public SQLBuilderParams(LinkedHashMap<String, Object> parameters, String table)
	{
		this._parameters = parameters;
		this._table = table;
	}
	
	public LinkedHashMap<String, Object> GetParameters()
	{
		return _parameters;
	}
	
	public String GetTable()
	{
		return _table;
	}
}
