package com.pcb.pcbridge.ban.commands;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;

import com.pcb.pcbridge.ban.BanHelper;
import com.pcb.pcbridge.ban.PlayerUUID;
import com.pcb.pcbridge.library.TimestampHelper;
import com.pcb.pcbridge.library.UUIDLookup;
import com.pcb.pcbridge.library.controllers.commands.CommandArgs;
import com.pcb.pcbridge.library.controllers.commands.ICommand;

/**
 * Command: Queries the Mojang web api for the specified user's UUID
 */

public final class CommandLookup implements ICommand 
{	
	private UUIDLookup _uuidLookup = new UUIDLookup();
	
	public boolean Execute(final CommandArgs e, Object... args) 
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
		
		Bukkit.getScheduler().runTaskAsynchronously(e.Plugin, new Runnable()
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
		final PlayerUUID uuid = BanHelper.GetUUID(e.Plugin, username);
		
		if(uuid.GetUUID() == "")
		{
			e.Sender.sendMessage("ERROR: Could not determine the player's UUID");
			return;
		}
		
		Bukkit.getScheduler().runTaskAsynchronously(e.Plugin, new Runnable()
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
	
	
	private void OnSuccess(CommandArgs e, String message)
	{
		e.Sender.sendMessage(message);
	}
	
	private void OnError(CommandArgs e, String playerMsg, String logMsg)
	{
		e.Sender.sendMessage(playerMsg);
		e.Plugin.getLogger().severe(logMsg);
	}
}
