package com.pcb.pcbridge.library.database.querybuilder.operations;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Contains the data necessary for a Builder to construct an SQL string
 */

public class SQLBuilderParams 
{
	private LinkedHashMap<String, Object> _parameters;
	private String _table;
	private List<String> _columns;
	
	public SQLBuilderParams(LinkedHashMap<String, Object> parameters, String table, List<String> columns)
	{
		this._parameters = parameters;
		this._table = table;
		this._columns = columns;
	}
	
	public LinkedHashMap<String, Object> GetParameters()
	{
		return _parameters;
	}
	
	public String GetTable()
	{
		return _table;
	}
	
	public List<String> GetColumns()
	{
		return _columns;
	}
}
