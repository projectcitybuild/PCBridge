package com.pcb.pcbridge.bukkit.ban;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.library.PlayerUUID;
import com.pcb.pcbridge.library.async.IFutureCallback;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;

/**
 * Collection of Ban related methods used across most of the Ban commands
 */

public final class BanHelper 
{
	
	/**
	 * Gets the ban record of the specified username and/or UUID.
	 * Returns null if user is not currently banned.
	 * 
	 *  TODO: this should be refactored to be asynchronous!
	 * 
	 * @param adapter
	 * @param name
	 * @param uuid
	 * @return
	 * @throws SQLException
	 */
	/*public static List<HashMap<String, Object>> LookupPlayer(AbstractAdapter adapter, String name, String uuid) throws SQLException
	{
		String SQL = "SELECT * FROM pcban_active_bans WHERE is_active=1 ";
		List<HashMap<String, Object>> results;
		
		if(name != null && uuid != null)
		{
			results = adapter.Query(SQL + "and (banned_name=? or banned_uuid=?)",
				name, uuid
			);
		}
		else if(uuid == null)
		{
			results = adapter.Query(SQL + "and banned_name=?",
				name
			);
		}
		else
		{
			results = adapter.Query(SQL + "and banned_uuid=?",
				uuid
			);
		}
		
		// TODO: check if its a temp ban and has now expired
			
		return results;
	}*/
	
	/**
	 * Builds the SQL required to check if a name and/or uuid is banned, in async
	 * 
	 * @param adapter
	 * @param name
	 * @param uuid
	 * @param callback
	 * @return
	 * @throws SQLException
	 */
	/*public static AsyncAdapterParams GetLookupSQL(AbstractAdapter adapter, String name, String uuid)
	{
		String sql = "SELECT * FROM pcban_active_bans WHERE is_active=1 ";
		Object[] params;
		
		if(name != null && uuid != null)
		{
			sql += "and (banned_name=? or banned_uuid=?)";
			params = new Object[2];
			params[0] = name;
			params[1] = uuid;
			
		}
		else if(uuid == null)
		{
			sql += "and banned_name=?";
			params = new Object[1];
			params[0] = name;
		}
		else
		{
			sql += "and banned_uuid=?";
			params = new Object[1];
			params[0] = uuid;
		}
		
		return new AsyncAdapterParams(sql, params);
	}*/
	
	/**
	 * Checks if the specified username and/or UUID is currently banned
	 * 
	 * @param adapter
	 * @param name
	 * @param uuid
	 * @return
	 * @throws SQLException
	 */
	/*public static boolean IsPlayerBanned(AbstractAdapter adapter, String name, String uuid) throws SQLException
	{
		List<HashMap<String, Object>> results = LookupPlayer(adapter, name, uuid);
		return (results != null && results.size() > 0);
	}*/
	
	/**
	 * Retrieves a player's UUID regardless of online state or join history
	 * 
	 * @param plugin
	 * @param username
	 * @return
	 */
	public static void GetUUIDAsync(final PCBridge plugin, final String username, final IFutureCallback<PlayerUUID> callback)
	{
		UUID uuid = null;
		String ip = "";
		
		// check if player is currently online
		@SuppressWarnings("deprecation")
		Player player = plugin.getServer().getPlayer(username);
		
		if(player != null)
		{
			uuid 	= player.getUniqueId();
			ip 		= player.getAddress().getHostString();
			
			PlayerUUID playerUUID = new PlayerUUID(username, ip, uuid, true, true, player);
			callback.OnSuccess(playerUUID);
		}
		
		// otherwise check if player has played before
		@SuppressWarnings("deprecation")
		OfflinePlayer offlinePlayer	= plugin.getServer().getOfflinePlayer(username);
		
		boolean hasPlayedBefore = offlinePlayer.hasPlayedBefore();			
		if(hasPlayedBefore)
		{
			PlayerUUID playerUUID = new PlayerUUID(username, ip, offlinePlayer.getUniqueId(), false, true, null);
			callback.OnSuccess(playerUUID);
		}
		else
		{			
			// retrieve the player's UUID from Mojang (in async) because they've never joined the server before
			final String ipFinal = ip;
			Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() 
			{
				@Override
				public void run() 
				{
					try 
					{
						UUID result = plugin.GetUUIDFetcher().GetCurrentUUID(username);
						
						if(result == null)
							throw new NullPointerException("Mojang API UUID query returned null");
						
						PlayerUUID playerUUID = new PlayerUUID(username, ipFinal, result, false, false, null);
						callback.OnSuccess(playerUUID);
					} 
					catch (IOException | ParseException | NullPointerException err) 
					{
						callback.OnError(err);
					}
				}
			});
					
		}
	}
	
	/**
	 * Kind of duplicate code...
	 * 
	 * @param plugin
	 * @param username
	 * @return
	 */
	public static PlayerUUID GetUUID(PCBridge plugin, String username)
	{
		UUID uuid = null;
		String ip = "";
		
		// check if player is currently online
		@SuppressWarnings("deprecation")
		Player player = plugin.getServer().getPlayer(username);
		
		if(player != null)
		{
			uuid 	= player.getUniqueId();
			ip 		= player.getAddress().getHostString();
			
			return new PlayerUUID(username, ip, uuid, true, true, player);
		}
		
		// otherwise check if player has played before
		@SuppressWarnings("deprecation")
		OfflinePlayer offlinePlayer	= plugin.getServer().getOfflinePlayer(username);
		
		boolean hasPlayedBefore = offlinePlayer.hasPlayedBefore();			
		if(hasPlayedBefore)
		{
			uuid = offlinePlayer.getUniqueId();
		}
		else
		{
			uuid = null;
			
			// retrieve the player's UUID from Mojang because they've never joined the server before
			try 
			{
				uuid = plugin.GetUUIDFetcher().GetCurrentUUID(username);				
			} 
			catch (IOException | ParseException err) 
			{
				plugin.getLogger().severe("Could not retrieve UUID [" + username + "] from Mojang web service: " + err.getMessage());
			}
		}
		
		return new PlayerUUID(username, ip, uuid, false, hasPlayedBefore, null);	
	}
	
}
