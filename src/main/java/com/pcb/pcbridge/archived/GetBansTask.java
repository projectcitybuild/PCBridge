//package com.pcb.pcbridge.archived;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.UUID;
//
//import com.pcb.pcbridge.PCBridge;
//import com.pcb.pcbridge.archived.PlayerBan;
//import com.pcb.pcbridge.archived.BanListContract;
//import com.pcb.pcbridge.archived.PlayerContract;
//
//public class GetBansTask {
//
//	/**
//	 * Gets the first active ban record for the given UUID.
//	 * Performs lookup in a cache instead if given
//	 *
//	 * @param uuid
//	 * @param cache
//	 * @return
//	 */
//	public static PlayerBan GetBanByUuid(UUID uuid)
//	{
//		List<PlayerBan> bans = GetAllByUuid(uuid.toString(), true);
//		if(bans.size() == 0)
//			return null;
//
//		return bans.get(0);
//	}
//
//	/**
//	 * Gets all bans for the given UUID
//	 *
//	 * @param connection
//	 * @param uuid
//	 * @param activeOnly
//	 * @return
//	 */
//	public static List<PlayerBan> GetAllByUuid(String uuid, boolean activeOnly)
//	{
//		try(Connection conn = PCBridge.GetConnectionPool().GetConnection(BanListContract.DATABASE))
//		{
//			String selectQuery = "SELECT *,"
//					+ " t1." + PlayerContract.TablePlayers.COL_ALIAS + " AS playerAlias,"
//					+ " t1." + PlayerContract.TablePlayers.COL_UUID + " AS playerUuid,"
//					+ " t2." + PlayerContract.TablePlayers.COL_ALIAS + " AS staffAlias,"
//					+ " t2." + PlayerContract.TablePlayers.COL_UUID + " AS staffUuid"
//					+ " FROM " + BanListContract.TableBans.TABLE_NAME + " AS s"
//					+ " LEFT JOIN " + PlayerContract.TablePlayers.TABLE_NAME + " AS t1"
//					+ " ON s." + BanListContract.TableBans.COL_PLAYER_ID + " = t1." + PlayerContract.TablePlayers._ID
//					+ " LEFT JOIN " + PlayerContract.TablePlayers.TABLE_NAME + " AS t2"
//					+ " ON s." + BanListContract.TableBans.COL_STAFF_ID + " = t2." + PlayerContract.TablePlayers._ID
//					+ " WHERE t1." + PlayerContract.TablePlayers.COL_UUID + " = ?";
//
//			if(activeOnly)
//				selectQuery += " AND s." + BanListContract.TableBans.COL_IS_BANNED + " = true";
//
//			selectQuery += " ORDER BY s." + BanListContract.TableBans.COL_TIMESTAMP + " DESC";
//
//			PreparedStatement stmt = conn.prepareStatement(selectQuery);
//			stmt.setString(1, uuid);
//			ResultSet results = stmt.executeQuery();
//
//			List<PlayerBan> bans = BuildSet(results);
//
//			stmt.close();
//			results.close();
//
//			return bans;
//		}
//		catch(SQLException e)
//		{
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	/**
//	 * Get a list of all active bans
//	 *
//	 * @return
//	 */
//	public static List<PlayerBan> GetAllActive()
//	{
//		try(Connection conn = PCBridge.GetConnectionPool().GetConnection(BanListContract.DATABASE))
//		{
//			String selectQuery = "SELECT * "
//					+ " FROM " + BanListContract.TableBans.TABLE_NAME + " AS s"
//					+ " LEFT JOIN " + PlayerContract.TablePlayers.TABLE_NAME + " AS t1"
//					+ " ON s." + BanListContract.TableBans.COL_PLAYER_ID + " = t1." + PlayerContract.TablePlayers._ID
//					+ " LEFT JOIN " + PlayerContract.TablePlayers.TABLE_NAME + " AS t2"
//					+ " ON s." + BanListContract.TableBans.COL_STAFF_ID + " = t2." + PlayerContract.TablePlayers._ID
//					+ " WHERE s." + BanListContract.TableBans.COL_IS_BANNED + " = ?"
//					+ " ORDER BY s." + BanListContract.TableBans.COL_TIMESTAMP + " DESC";
//
//			PreparedStatement stmt = conn.prepareStatement(selectQuery);
//			stmt.setBoolean(1, true);
//			ResultSet results = stmt.executeQuery();
//
//			List<PlayerBan> bans = BuildSet(results);
//
//			stmt.close();
//			results.close();
//
//			return bans;
//		}
//		catch(SQLException e)
//		{
//			e.printStackTrace();
//			return null;
//		}
//	}
//
//	/**
//	 * Takes in a ResultSet and spits out a list of PlayerBans
//	 * @param results
//	 * @return
//	 * @throws SQLException
//	 */
//	private static List<PlayerBan> BuildSet(ResultSet results)
//	{
//		ArrayList<PlayerBan> bans = new ArrayList<>();
//		try
//		{
//			while(results.next())
//			{
//				PlayerBan ban = new PlayerBan();
//				ban.Id 			= results.getInt(BanListContract.TableBans._ID);
//				ban.PlayerAlias = results.getString("playerAlias");
//				ban.PlayerUUID 	= results.getString("playerUuid");
//				ban.StaffAlias 	= results.getString("staffAlias");
//				ban.StaffUUID 	= results.getString("staffUuid");
//				ban.Reason 		= results.getString(BanListContract.TableBans.COL_REASON);
//				ban.Timestamp 	= results.getLong(BanListContract.TableBans.COL_TIMESTAMP);
//				ban.Server 		= results.getString(BanListContract.TableBans.COL_SERVER);
//				ban.IsActive 	= results.getBoolean(BanListContract.TableBans.COL_IS_BANNED);
//				ban.ExpiryTimestamp = results.getLong(BanListContract.TableBans.COL_UNBAN_ON);
//
//				// check expiry time and unban them if it's already passed
//				long now = System.currentTimeMillis() / 1000L;
//				if(ban.ExpiryTimestamp != 0 && now >= ban.ExpiryTimestamp)
//				{
//					ban.IsActive = false;
//					PCBridge.NewChain()
//						.asyncFirst(() -> SetBanInactive(ban.Id))
//						.execute();
//
//					continue;
//				}
//
//				bans.add(ban);
//			}
//		}
//		catch(SQLException e)
//		{
//			e.printStackTrace();
//		}
//
//		return bans;
//	}
//
//	/**
//	 * Sets the given ban id to inactive in the database
//	 *
//	 * @param id
//	 * @return
//	 */
//	private static boolean SetBanInactive(int id)
//	{
//		try(Connection conn = PCBridge.GetConnectionPool().GetConnection(BanListContract.DATABASE))
//		{
//			PreparedStatement stmt = conn.prepareStatement(
//					"UPDATE " + BanListContract.TableBans.TABLE_NAME
//					+ " SET " + BanListContract.TableBans.COL_IS_BANNED + " = false"
//					+ " WHERE " + BanListContract.TableBans._ID + " = ?");
//
//			stmt.setInt(1, id);
//			stmt.executeUpdate();
//
//			stmt.close();
//		}
//		catch(SQLException e)
//		{
//			e.printStackTrace();
//			return false;
//		}
//
//		return true;
//	}
//
//}
