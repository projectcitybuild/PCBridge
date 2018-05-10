/*
 * The MIT License
 *
 * Copyright 2016 Andy Saw
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.pcb.pcbridge.archived.utils.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Provides access to a collection of data sources and their connections
 */
public class ConnectionPool {
	
	private Map<String, ConnectionSource> _dataSources = new HashMap<>();
	private final Logger _logger;
	
	public ConnectionPool(Logger logger)
	{
		this._logger = logger;
	}
	
	/**
	 * Adds a data source into the pool
	 * 
	 * @param database	Name of database
	 * @param source
	 * @return
	 */
	public ConnectionPool AddSource(ConnectionSource source)
	{
		_dataSources.put(source.GetDatabaseName(), source);
		return this;
	}
	
	/**
	 * Gets a connection to a data source from the pool
	 * 
	 * @param database	Name of database
	 * @return
	 * @throws SQLException
	 * @throws NullPointerException
	 */
	public Connection GetConnection(String database) throws SQLException, NullPointerException
	{
		ConnectionSource source = _dataSources.get(database);
		if(source == null)
			throw new NullPointerException("Could not find datasource for database '" + database + "'");
		
		return source.GetConnection();
	}
	
	/**
	 * Closes all connections and prevents any further use of them
	 */
	public void Destroy()
	{
		_dataSources.values().forEach(source -> source.Close());
		_dataSources = null;
	}
	
	/**
	 * Tests the connection to every data source.
	 * Returns false if a connection fails to any source
	 * 
	 * @return boolean Success
	 */
	public boolean TestConnections()
	{
		for(ConnectionSource source : _dataSources.values())
		{
			_logger.info("Testing database connection to: " + source.GetDatabaseName());
			
			long start = System.nanoTime();
			try(Connection conn = GetConnection( source.GetDatabaseName() ))
			{
				long end = System.nanoTime() - start;
				_logger.info("Connection success [" + end / 1000000 + "ms]");
			}
			catch(SQLException e)
			{
				_logger.severe("Connection failed.");
				e.printStackTrace();
				return false;
			}
		}
		
		return true;
	}

}
