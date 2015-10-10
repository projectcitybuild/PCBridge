package com.pcb.pcbridge.ban;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.library.AsyncAdapterParams;
import com.pcb.pcbridge.library.AsyncCallback;
import com.pcb.pcbridge.library.UUIDLookup;
import com.pcb.pcbridge.library.database.adapters.AbstractAdapter;

/**
 * Collection of Ban related helper methods
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
	public static List<HashMap<String, Object>> LookupPlayer(AbstractAdapter adapter, String name, String uuid) throws SQLException
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
	}
	
	/**
	 * Checks if the specified username and/or UUID is currently banned
	 * 
	 * @param adapter
	 * @param name
	 * @param uuid
	 * @return
	 * @throws SQLException
	 */
	public static boolean IsPlayerBanned(AbstractAdapter adapter, String name, String uuid) throws SQLException
	{
		List<HashMap<String, Object>> results = LookupPlayer(adapter, name, uuid);
		return (results != null && results.size() > 0);
	}
	
	/**
	 * Retrieves a player's UUID regardless of online state or join history
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
			UUIDLookup fetcher = new UUIDLookup();
			try 
			{
				uuid = fetcher.GetCurrentUUID(username);				
			} 
			catch (IOException | ParseException err) 
			{
				plugin.getLogger().severe("Could not retrieve UUID [" + username + "] from Mojang web service: " + err.getMessage());
			}
		}
		
		return new PlayerUUID(username, ip, uuid, false, hasPlayedBefore, null);
			
	}
}
