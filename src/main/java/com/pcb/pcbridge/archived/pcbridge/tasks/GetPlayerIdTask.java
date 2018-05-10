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
package com.pcb.pcbridge.archived.pcbridge.tasks;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import com.pcb.pcbridge.archived.pcbridge.schema.PlayerContract;

/**
 * Gets the id row (from db) for the given player
 * or creates one if it doesn't exist
 */
public class GetPlayerIdTask {
	
	public static int Run(Connection connection, String uuid, String alias)
	{	
		try 
		{
			// search for existing row and return the id
			String selectQuery = "SELECT " + PlayerContract.TablePlayers._ID 
					+ " FROM " + PlayerContract.TablePlayers.TABLE_NAME 
					+ " WHERE " + PlayerContract.TablePlayers.COL_UUID + "=?";
			
			PreparedStatement stmt = connection.prepareStatement(selectQuery);
			stmt.setString(1, uuid);
			ResultSet results = stmt.executeQuery();
			stmt.close();
			
			int id = -1;
			while(results.next())
			{
				id = results.getInt(PlayerContract.TablePlayers._ID);
			}
			results.close();
			
			// if no row found, insert one instead and return that
			if(id == -1)
			{
				String insertQuery = "INSERT INTO " + PlayerContract.TablePlayers.TABLE_NAME + " ("
						+ PlayerContract.TablePlayers.COL_ALIAS + ","
						+ PlayerContract.TablePlayers.COL_UUID + ") "
						+ " VALUES (?,?)";
					
				stmt = connection.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
				stmt.setString(1, alias);
				stmt.setString(2, uuid);
				stmt.executeUpdate();
				stmt.close();
				
				ResultSet lastId = stmt.getGeneratedKeys();					
				if(lastId.next())
				{
					id = lastId.getInt(1);
				}
				lastId.close();
			}

			return id;
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			return -1;
		}

	}	
	
}
