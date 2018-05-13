//package com.pcb.pcbridge.archived;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.logging.Logger;
//
//import com.pcb.pcbridge.archived.PlayerContract;
//import com.pcb.pcbridge.spigot.warnings.schema.WarningContract;
//import com.pcb.pcbridge.archived.IMigrate;
//
///**
// * Migration to execute when the plugin is run for the first time
// */
//
//public class WarningsV2Migration implements IMigrate {
//
//	@Override
//	public boolean IsTransaction()
//	{
//		return true;
//	}
//
//	@Override
//	public void OnMigrate(Connection connection, Logger logger) throws SQLException
//	{
//		// rename the old warnings table
//		boolean tableExists = false;
//		try(Statement stmt = connection.createStatement())
//		{
//			ResultSet rs = stmt.executeQuery("SELECT count(*) FROM information_schema.tables WHERE table_schema = 'warnings_old'");
//			if(rs.first())
//			{
//				do {
//					if(rs.getInt(1) > 1)
//					{
//						tableExists = true;
//						break;
//					}
//				}
//				while(rs.next());
//
//				if(!tableExists)
//				{
//					try(Statement stmt2 = connection.createStatement())
//					{
//						stmt2.executeUpdate("ALTER TABLE warnings RENAME TO warnings_old");
//
//						// and create the new table
//						try(Statement stmt3 = connection.createStatement())
//						{
//							stmt3.executeUpdate(WarningContract.TableWarnings.SQL_CREATE);
//						}
//					}
//					catch(SQLException e)
//					{
//						e.printStackTrace();
//						return;
//					}
//				}
//			}
//		}
//
//		// convert the name+uuid columns into foreign key values
//		try(Statement stmt = connection.createStatement())
//		{
//			ResultSet rs = stmt.executeQuery("SELECT * FROM warnings_old");
//			if(rs.first())
//			{
//				do
//				{
//					String playerName = rs.getString("player_name");
//					String playerUuid = rs.getString("player_uuid");
//					String staffName = rs.getString("staff_name");
//					String staffUuid = rs.getString("staff_uuid");
//					String reason = rs.getString("reason");
//					Long timestamp = rs.getLong("created_on");
//
//					int playerId = GetPlayerID(connection, logger, playerName, playerUuid);
//					int staffId = GetPlayerID(connection, logger, staffName, staffUuid);
//
//					try(PreparedStatement bStmt = connection.prepareStatement(
//							"INSERT INTO " + WarningContract.TableWarnings.TABLE_NAME + "("
//							+ WarningContract.TableWarnings.COL_PLAYER_ID + ","
//							+ WarningContract.TableWarnings.COL_STAFF_ID + ","
//							+ WarningContract.TableWarnings.COL_REASON + ","
//							+ WarningContract.TableWarnings.COL_TIMESTAMP
//							+ ") VALUES (?,?,?,?)"))
//					{
//						bStmt.setInt(1, playerId);
//						bStmt.setInt(2, staffId);
//						bStmt.setString(3, reason);
//						bStmt.setLong(4, timestamp);
//						bStmt.executeUpdate();
//					}
//
//					logger.info("Inserted new row for " + playerName);
//				}
//				while(rs.next());
//			}
//		}
//	}
//
//	private Integer GetPlayerID(Connection connection, Logger logger, String name, String uuid) throws SQLException
//	{
//		int playerId = -1;
//		try(PreparedStatement pStmt = connection.prepareStatement(
//				"SELECT " + PlayerContract.TablePlayers._ID
//				+ " FROM " + PlayerContract.TablePlayers.TABLE_NAME
//				+ " WHERE " + PlayerContract.TablePlayers.COL_UUID + " = ?"))
//		{
//			pStmt.setString(1, uuid);
//			ResultSet pRs = pStmt.executeQuery();
//
//			if(pRs.first())
//			{
//				// player row exists, use that
//				playerId = pRs.getInt(PlayerContract.TablePlayers._ID);
//				logger.info("Found PlayerID for " + name + ": " + playerId);
//				return playerId;
//			}
//			else
//			{
//				// no player row, insert one
//				logger.info("No PlayerID for " + name + ". Creating new row");
//				try(PreparedStatement nStmt = connection.prepareStatement(
//						"INSERT INTO " + PlayerContract.TablePlayers.TABLE_NAME + "("
//						+ PlayerContract.TablePlayers.COL_ALIAS + ","
//						+ PlayerContract.TablePlayers.COL_UUID
//						+ ") VALUES (?,?)", Statement.RETURN_GENERATED_KEYS))
//				{
//					nStmt.setString(1, name);
//					nStmt.setString(2, uuid);
//					nStmt.executeUpdate();
//
//					ResultSet lastId = nStmt.getGeneratedKeys();
//					if(lastId.first())
//					{
//						playerId = lastId.getInt(1);
//						logger.info("New ID is " + playerId);
//						return playerId;
//					}
//				}
//			}
//		}
//
//		return null;
//	}
//
//}
