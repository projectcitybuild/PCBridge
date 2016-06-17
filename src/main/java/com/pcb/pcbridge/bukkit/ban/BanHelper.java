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
 * 
 * TODO: delete this
 */

public final class BanHelper 
{
	
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
