package com.pcb.pcbridge.ban.commands;

import java.util.UUID;

import org.bukkit.Bukkit;
import com.pcb.pcbridge.library.UUIDLookup;
import com.pcb.pcbridge.library.controllers.commands.CommandPacket;
import com.pcb.pcbridge.library.controllers.commands.ICommand;

/**
 * Command: Queries the Mojang web api for the specified user's UUID
 */

public final class CommandLookup implements ICommand 
{	
	private UUIDLookup _uuidLookup = new UUIDLookup();
	
	public boolean Execute(final CommandPacket e, Object... args) 
	{
		if(e.Args.length > 1 || e.Args.length == 0)
			return false;
		
		final String username = e.Args[0];
		
		Bukkit.getScheduler().runTaskAsynchronously(e.Plugin, new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					UUID uuid = _uuidLookup.Query(username);
					OnSuccess(e, uuid);
				}
				catch(Exception err)
				{
					OnError(e, err);
				}
			}
		});
		
		return true;
	}
	
	private void OnSuccess(CommandPacket e, UUID uuid)
	{
		e.Sender.sendMessage(e.Args[0] + ": " + uuid.toString());
	}
	
	private void OnError(CommandPacket e, Exception err)
	{
		e.Plugin.getLogger().severe(err.getMessage());
	}
}
