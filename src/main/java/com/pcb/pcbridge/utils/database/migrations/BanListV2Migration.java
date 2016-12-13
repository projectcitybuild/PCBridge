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
package com.pcb.pcbridge.utils.database.migrations;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

import org.json.simple.parser.ParseException;

import com.pcb.pcbridge.schema.BanListContract;
import com.pcb.pcbridge.schema.PlayerContract;
import com.pcb.pcbridge.utils.UUIDFetcher;
import com.pcb.pcbridge.utils.database.IMigrate;

/**
 * Migration to execute when the plugin is run for the first time
 */

public class BanListV2Migration implements IMigrate {

	private Map<String, UUID> _uuidMap = new HashMap<>();
	
	@Override
	public boolean IsTransaction()
	{
		return true;
	}
	
	@Override
	public void OnMigrate(Connection connection, Logger logger) throws SQLException 
	{
		// wipe the current ban list
		try(Statement stmt = connection.createStatement())
		{
			stmt.executeUpdate("DELETE FROM " + BanListContract.TableUnbans.TABLE_NAME);
			stmt.executeUpdate("DELETE FROM " + BanListContract.TableBans.TABLE_NAME);
			stmt.executeUpdate("DELETE FROM " + PlayerContract.TablePlayers.TABLE_NAME);
		}
		
		// find the player id of CONSOLE
		Integer consoleId = GetPlayerID(connection, logger, "CONSOLE", "CONSOLE");
		if(consoleId == null)
		{
			logger.severe("Failed to get/create player id for CONSOLE");
			return;
		}
		
		// loop through every row of the old banlist
		UUIDFetcher fetcher = new UUIDFetcher();
		try(Statement stmt = connection.createStatement())
		{
			ResultSet rs = stmt.executeQuery("SELECT * FROM banlist");
			if(rs.first())
			{
				do
				{
					String name = rs.getString("name");
					String uuid = rs.getString("uuid");
					
					// discard rows with no UUID
					if(uuid == null || uuid == "")
					{
						logger.info("No UUID for " + name + ". Skipping");
						continue;
					}
						
					
					// create or get player row
					Integer playerId = GetPlayerID(connection, logger, name, uuid);
					if(playerId == null)
					{
						logger.severe( String.format("Error getting/creating player id for %s (%s)", name, uuid) );
						continue;
					}
					
					// create or get staff player row
					String staffName = rs.getString("admin");
					String staffUuid = rs.getString("admin_uuid");
					
					Integer staffId;
					if(staffUuid == null || staffUuid.equals("-") || staffName == null || staffName.equals("Trap"))
					{
						staffId = consoleId;
					}
					else
					{
						staffId = GetPlayerID(connection, logger, staffName, staffUuid);
						if(staffId == null)
						{
							logger.severe( String.format("Error getting/creating player id for %s (%s)", staffName, staffUuid) );
							continue;
						}
					}
					
					// create new ban record
					boolean isBanned = rs.getBoolean("is_active");
					Long unbanTime = rs.getLong("temptime");
					long banDate = rs.getLong("time");
					String reason = rs.getString("reason");
					String unbanner = rs.getString("unbanned_by");
					
					if(unbanTime == 0)
						unbanTime = null;
					
					if(reason.equalsIgnoreCase("griefing") || reason.equalsIgnoreCase("undefined"))
						reason = null;
					
					// convert any old time formats to a UNIX timestamp
					if(banDate >= 2011 && banDate <= 2016)
					{
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.YEAR, (int) banDate);
						banDate = cal.getTimeInMillis() / 1000;
					}
					
					// replace blank times with 2011's UNIX timestamp (since that's when the first version was
					// originally created)
					if(banDate == 0)
					{
						Calendar cal = Calendar.getInstance();
						cal.set(Calendar.YEAR, 2011);
						banDate = cal.getTimeInMillis() / 1000;
					}
					
					int banId = -1;
					try(PreparedStatement bStmt = connection.prepareStatement(
							"INSERT INTO " + BanListContract.TableBans.TABLE_NAME + "("
							+ BanListContract.TableBans.COL_PLAYER_ID + ","
							+ BanListContract.TableBans.COL_STAFF_ID + ","
							+ BanListContract.TableBans.COL_IS_BANNED + ","
							+ BanListContract.TableBans.COL_SERVER + ","
							+ BanListContract.TableBans.COL_REASON + ","
							+ BanListContract.TableBans.COL_TIMESTAMP + ","
							+ BanListContract.TableBans.COL_UNBAN_ON
							+ ") VALUES (?,?,?,?,?,?,?)", Statement.RETURN_GENERATED_KEYS))
					{
						bStmt.setInt(1, playerId);
						bStmt.setInt(2, staffId);
						bStmt.setBoolean(3, isBanned);
						bStmt.setString(4, "Minecraft");
						bStmt.setObject(5, reason);
						bStmt.setLong(6, banDate);
						bStmt.setObject(7, unbanTime);
						bStmt.executeUpdate();
						
						ResultSet lastId = bStmt.getGeneratedKeys();					
						if(lastId.first())
						{
							banId = lastId.getInt(1);
							logger.info("New ban id is " + banId);
						}
					}
					
					// create an unban record if needed
					if(!isBanned)
					{
						Integer unbanStaffId = null;
						
						// if unbanner is null, set it to CONSOLE
						if(unbanner == null || unbanner.equals(""))
						{
							unbanStaffId = consoleId;
						}
						else
						{
							// we don't have a UUID for the unbanner, so fetch their UUID
							// at the ban date (and cache it)
							UUID unbanUuid = _uuidMap.get(unbanner);					
							if(unbanUuid == null)
							{
								try {
									unbanUuid = fetcher.GetUUIDAt(unbanner, banDate);
								} catch (IOException | ParseException e) {
									e.printStackTrace();
								}
								
								if(unbanUuid == null)
								{
									logger.severe("Could not retrieve UUID for " + unbanner);
									continue;
								}
								
								_uuidMap.put(unbanner, unbanUuid);
							}
							
							unbanStaffId = GetPlayerID(connection, logger, unbanner, unbanUuid.toString());
							if(unbanStaffId == null)
							{
								logger.severe( String.format("Error getting/creating player id for %s (%s)", unbanner, unbanUuid) );
								continue;
							}
						}
						
						try(PreparedStatement bStmt = connection.prepareStatement(
								"INSERT INTO " + BanListContract.TableUnbans.TABLE_NAME + "("
								+ BanListContract.TableUnbans.COL_BAN_ID + ","
								+ BanListContract.TableUnbans.COL_STAFF_ID + ","
								+ BanListContract.TableUnbans.COL_TIMESTAMP
								+ ") VALUES (?,?,?)"))
						{
							// we don't have an unban date from previous versions, so just
							// use the date they were banned
														
							bStmt.setInt(1, banId);
							bStmt.setInt(2, unbanStaffId);
							bStmt.setLong(3, banDate);
							bStmt.executeUpdate();
						}
					}
				}
				while(rs.next());
			}
		}
	}
	
	private Integer GetPlayerID(Connection connection, Logger logger, String name, String uuid) throws SQLException
	{
		int playerId = -1;
		try(PreparedStatement pStmt = connection.prepareStatement(
				"SELECT " + PlayerContract.TablePlayers._ID 
				+ " FROM " + PlayerContract.TablePlayers.TABLE_NAME
				+ " WHERE " + PlayerContract.TablePlayers.COL_UUID + " = ?"))
		{
			pStmt.setString(1, uuid);
			ResultSet pRs = pStmt.executeQuery();
			
			if(pRs.first())
			{
				// player row exists, use that
				playerId = pRs.getInt(PlayerContract.TablePlayers._ID);
				logger.info("Found PlayerID for " + name + ": " + playerId);
				return playerId;
			}
			else
			{
				// no player row, insert one
				logger.info("No PlayerID for " + name + ". Creating new row");
				try(PreparedStatement nStmt = connection.prepareStatement(
						"INSERT INTO " + PlayerContract.TablePlayers.TABLE_NAME + "("
						+ PlayerContract.TablePlayers.COL_ALIAS + ","
						+ PlayerContract.TablePlayers.COL_UUID
						+ ") VALUES (?,?)", Statement.RETURN_GENERATED_KEYS))
				{
					nStmt.setString(1, name);
					nStmt.setString(2, uuid);
					nStmt.executeUpdate();
					
					ResultSet lastId = nStmt.getGeneratedKeys();					
					if(lastId.first())
					{
						playerId = lastId.getInt(1);
						logger.info("New ID is " + playerId);
						return playerId;
					}
				}
			}
		}
		
		return null;
	}

}
