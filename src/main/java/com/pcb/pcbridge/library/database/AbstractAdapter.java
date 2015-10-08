package com.pcb.pcbridge.library.database;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

import com.pcb.pcbridge.library.AsyncAdapterParams;
import com.pcb.pcbridge.library.AsyncCallback;

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
	public abstract void ExecuteAsync(final AsyncAdapterParams params, final AsyncCallback callback);
	
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
	public abstract void QueryAsync(final AsyncAdapterParams params, final AsyncCallback callback);
	
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
	public abstract void CountAsync(final AsyncAdapterParams params, final AsyncCallback callback);
	
}
