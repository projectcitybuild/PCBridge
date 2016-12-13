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
package com.pcb.pcbridge.utils.database;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.pcb.pcbridge.PCBridge;

/**
 * Performs database schema tasks.
 */

public class MigrationHandler {
	
	private final Logger _logger;
	public MigrationHandler(Logger logger)
	{
		this._logger = logger;
	}
	
	/**
	 * Runs the given Migration on the supplied connection.
	 * Note: Does NOT close the supplied connection after completion
	 * 
	 * @param <T>			Class which implements IMigrate
	 * @param conn			Database connection
	 * @param migration		Migration to run
	 */
	public <T extends IMigrate> void Migrate(Connection conn, Class<T> migration) 
			throws SQLException
	{
		try 
		{			
			IMigrate task = migration.newInstance();
			
			conn.setAutoCommit(!task.IsTransaction());

			_logger.info("Running migration: " + migration.getName());
			task.OnMigrate(conn, _logger);
			
			if(task.IsTransaction())
			{
				conn.commit();
				conn.setAutoCommit(true);
			}
		} 
		catch (InstantiationException | IllegalAccessException e) 
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Runs the given Migration on a new connection and then closes it
	 * after completion
	 * 
	 * @param migration
	 * @param database	Database to run the migration on
	 */
	public <T extends IMigrate> void Migrate(Class<T> migration, String database)
	{
		try(Connection conn = PCBridge.GetConnectionPool().GetConnection(database))
		{			
			Migrate(conn, migration);
		} 
		catch (SQLException e) 
		{
			e.printStackTrace();
		}
	}
	
}
