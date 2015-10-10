package com.pcb.pcbridge.library.database.querybuilder;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;

import com.pcb.pcbridge.library.database.querybuilder.operations.*;

/**
 * Builds SQL queries via method chaining
 */

public class QueryBuilder
{
	private LinkedHashMap<String, Object> _parameters = new LinkedHashMap<String, Object>();
	private List<QueryCriteria> _criteria = new ArrayList<QueryCriteria>();
	private String _table;
	private QueryType _operation;
	
	
	public QueryBuilder Select(String table, String[] columns)
	{
		return SetOperation(table, QueryType.SELECT);
	}
	
	public QueryBuilder Update(String table)
	{
		return SetOperation(table, QueryType.UPDATE);
	}
	
	public QueryBuilder Insert(String table)
	{
		return SetOperation(table, QueryType.INSERT);
	}
	
	public QueryBuilder Delete(String table)
	{
		return SetOperation(table, QueryType.DELETE);
	}
	
	/**
	 * Sets the query's operation type (insert, select, etc)
	 * 
	 * @param table
	 * @param operation
	 * @return
	 */
	private QueryBuilder SetOperation(String table, QueryType operation)
	{
		this._table = table;
		this._operation = operation;
		return this;
	}
	
	/**
	 * Adds a parameter to be injected into the prepared statement
	 * 
	 * @param column
	 * @param value
	 * @return
	 */
	public QueryBuilder Field(String column, Object value)
	{
		_parameters.put(column, value);
		return this;
	}
	
	/**
	 * Appends a WHERE condition to the query
	 * 
	 * @param column	Column to search in
	 * @param operator	Comparison type
	 * @param value		Value to search for
	 * @return
	 */
	public QueryBuilder Where(String column, String operator, Object value)
	{
		QueryCriteria criteria = new QueryCriteria(column, operator, value);
		_criteria.add(criteria);
		
		return this;
	}
	
	public QueryBuilder Where(String column, Object value)
	{
		return Where(column, "=", value);
	}
	
	/**
	 * Appends a WHERE condition - looks for multiple values in one column
	 * 
	 * @param column	Column to search in
	 * @param operator	Comparison type
	 * @param values	List of values to search for
	 * @return
	 */
	public QueryBuilder Where(String column, String operator, List<Object> values)
	{
		StringBuilder right = new StringBuilder();
		right.append("(");
		
		ListIterator<Object> i = values.listIterator();
		while(i.hasNext())
		{
			if(i.hasPrevious())
				right.append(",");
			
			right.append(i.next().toString());
		}
		
		right.append(")");
		
		return Where(column, operator, right.toString());
	}
		
	/**
	 * Builds the SQL string and returns the final output
	 */
	public QueryBuilderSQL Build()
	{
		AbstractSQLBuilder builder = null;
		
		switch(_operation)
		{
			case SELECT:
				break;
			case INSERT:
				builder = new InsertSQLBuilder();				
				break;
			case UPDATE:
			case DELETE:
				break;
		}
		
		SQLBuilderParams args = new SQLBuilderParams(_parameters, _table);
		String sql = builder.Build(args);
		
		return new QueryBuilderSQL(sql, builder.GetStoredParameters());
	}
	
}
