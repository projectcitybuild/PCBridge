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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import com.pcb.pcbridge.archived.PlayerContract;
import com.pcb.pcbridge.archived.IMigrate;

public class CreatePlayersMigration implements IMigrate {

	@Override
	public boolean IsTransaction()
	{
		return true;
	}
	
	@Override
	public void OnMigrate(Connection connection, Logger logger) throws SQLException 
	{
		Statement stmt;
		
		stmt = connection.createStatement();
		stmt.executeUpdate(PlayerContract.TablePlayers.SQL_CREATE);
		stmt.close();
		
		PreparedStatement prepStmt = connection.prepareStatement(
				"INSERT INTO " + PlayerContract.TablePlayers.TABLE_NAME
				+ " (" + PlayerContract.TablePlayers.COL_ALIAS + "," + PlayerContract.TablePlayers.COL_UUID + ") "
				+ " VALUES (?,?)");
		prepStmt.setString(1, "CONSOLE");
		prepStmt.setString(2, "CONSOLE");
		prepStmt.executeUpdate();
		prepStmt.close();
	}

}
