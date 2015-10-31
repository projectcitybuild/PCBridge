package com.pcb.pcbridge.library.database.adapters;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.pcb.pcbridge.library.async.IFutureCallback;

/**
 * Abstract interface for different storage implementations
 */

public abstract class AbstractAdapter
{	
	/**
	 * Executes a query on the current connection
	 * 
	 * @param sql	SQL query
	 * @param args	Query parameters to be injected
	 * @return
	 * @throws SQLException
	 */
	public abstract int Execute(String sql, Object... args) throws SQLException;
	public abstract int Execute(String sql) throws SQLException;
	
	
	/**
	 * Creates a prepared statement and adds it to a batch
	 * @param sql
	 * @param args
	 * @throws SQLException
	 */
	//public abstract void AddToBatch(String sql, Object... args) throws SQLException;
	
	/**
	 * Executes (and then clears) all prepared statements currently in the batch
	 * 
	 * @throws SQLException
	 */
	//public abstract void ExecuteBatch() throws SQLException;
	
	/**
	 * Queries the current connection for a ResultSet
	 * 
	 * @param sql	SQL query
	 * @param args	Query parameters to be injected
	 * @return
	 * @throws SQLException
	 */
	public abstract List<HashMap<String, Object>> Query(String sql, Object... args) throws SQLException;
	public abstract List<HashMap<String, Object>> Query(String sql) throws SQLException;
	
	/**
	 * Queries the current connection for a row count
	 * 
	 * @param sql	SQL query
	 * @param args	Query parameters to be injected
	 * @return
	 * @throws SQLException
	 */
	public abstract int Count(String sql, Object... args) throws SQLException;
	public abstract int Count(String sql) throws SQLException;
	
	/**
	 * Switches the connection to the specified database
	 * 
	 * @param name
	 * @throws SQLException
	 */
	public abstract void UseDatabase(String name) throws SQLException;
}
