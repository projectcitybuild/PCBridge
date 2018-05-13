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
package com.pcb.pcbridge.archived;

import java.sql.Connection;
import java.sql.SQLException;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * A connection to a single database
 */
public class ConnectionSource {

	private final HikariDataSource _dataSource;
	private final String _databaseName;
	
	public ConnectionSource(String host, String port, String db, String user, String pass)
	{
		HikariConfig config = new HikariConfig();
		config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + db);
		
		config.setUsername(user);
		if(pass != null && !pass.equals(""))
			config.setPassword(pass);
		
		config.addDataSourceProperty("cachePrepStmts", "true");
		config.addDataSourceProperty("prepStmtCacheSize", "250");
		config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
		config.addDataSourceProperty("useServerPrepStmts", "true");
		
		this._dataSource = new HikariDataSource(config);
		this._databaseName = db;
	}
	
	public ConnectionSource(String host, String port, String db, String user)
	{
		this(host, port, db, user, null);
	}
	
	/**
	 * Returns the name of the database this source connects to
	 * @return
	 */
	public String GetDatabaseName()
	{
		return _databaseName;
	}
	
	/**
	 * Returns the data source itself
	 * @return
	 */
	public HikariDataSource GetDataSource()
	{
		return _dataSource;
	}
	
	/**
	 * Returns a new connection to the data source from the pool
	 * 
	 * @return
	 * @throws SQLException
	 */
	public Connection GetConnection() throws SQLException
	{
		return _dataSource.getConnection();
	}
	
	/**
	 * Closes all connections to this data source
	 */
	public void Close()
	{
		_dataSource.close();
	}
	
}
