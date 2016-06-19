package com.pcb.pcbridge.bukkit.ban.commands;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.json.simple.parser.ParseException;

import com.pcb.pcbridge.PCBridge;
import com.pcb.pcbridge.library.PlayerUUID;
import com.pcb.pcbridge.library.TimestampHelper;
import com.pcb.pcbridge.library.UUIDLookup;
import com.pcb.pcbridge.library.controllers.AbstractCommand;
import com.pcb.pcbridge.library.controllers.CommandArgs;

/**
 * Command: Queries the Mojang web api for the specified user's UUID
 */

public final class CommandUUID extends AbstractCommand 
{	
	private UUIDLookup _uuidLookup = new UUIDLookup();
	
	public boolean Execute(final CommandArgs e) 
	{
		if(e.Args.length > 2 || e.Args.length < 1)
			return false;
		
		String operation = e.Args[0];
		switch(operation.toLowerCase())
		{
			case "uuid":
				GetCurrentUUID(e);
				break;
			case "history":
				GetNameHistory(e);
				break;
			default:
				return false;
		}
				
		return true;
	}
	
	/**
	 * Lookup the current UUID of the specified username
	 * 
	 * @param plugin
	 * @param e
	 */
	private void GetCurrentUUID(final CommandArgs e)
	{
		final String username = e.Args[1];
		
		Bukkit.getScheduler().runTaskAsynchronously(_plugin, new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					UUID uuid = _uuidLookup.GetCurrentUUID(username);
					OnSuccess(e, username + ": " + uuid.toString());
				}
				catch(Exception err)
				{
					OnError(e, "ERROR: An error occured trying to perform a lookup", err.getMessage());
				}
			}
		});
	}
	
	/**
	 * Get the name history of the given username
	 * 
	 * @param e
	 */
	private void GetNameHistory(final CommandArgs e)
	{
		final String username = e.Args[1];
		final PlayerUUID uuid = GetUUID(_plugin, username);
		
		if(uuid.GetUUID() == "")
		{
			e.Sender.sendMessage("ERROR: Could not determine the player's UUID");
			return;
		}
		
		Bukkit.getScheduler().runTaskAsynchronously(_plugin, new Runnable()
		{
			@Override
			public void run()
			{
				HashMap<String, Long> history = null;
				
				try
				{
					history = _uuidLookup.GetNameHistory(uuid.GetUUID());
				}
				catch(Exception err)
				{
					OnError(e, "ERROR: An error occured trying to perform a lookup", err.getMessage());
				}
				
				StringBuilder output = new StringBuilder();
				output.append("Name history of ")
					.append(username)
					.append("\n(UUID: ").append(uuid.GetUUID()).append(")")
					.append("\n---\n");
				
				Iterator<Entry<String, Long>> i = history.entrySet().iterator();
				while(i.hasNext())
				{
					Entry<String, Long> pair = i.next();
					
					output.append("Name: ")
						.append(pair.getKey());
					
					// only show the 'name change date' if it exists
					if(pair.getValue() != null)
					{
						Long timestamp = pair.getValue();
						Date date = TimestampHelper.GetDateFromTimestamp(timestamp);
						
						output.append("\n")
							.append("Changed to on: ")
							.append(date);
					}					
					
					if(i.hasNext())
						output.append("\n---\n");
				}
				
				OnSuccess(e, output.toString());
			}
		});
	}
	
	private PlayerUUID GetUUID(PCBridge plugin, String username)
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
	
	
	private void OnSuccess(CommandArgs e, String message)
	{
		e.Sender.sendMessage(message);
	}
	
	private void OnError(CommandArgs e, String playerMsg, String logMsg)
	{
		e.Sender.sendMessage(playerMsg);
		_plugin.getLogger().severe(logMsg);
	}
}
