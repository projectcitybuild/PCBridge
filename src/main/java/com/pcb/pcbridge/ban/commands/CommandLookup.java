package com.pcb.pcbridge.ban.commands;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.bukkit.ChatColor;
import org.json.simple.parser.ParseException;

import com.pcb.pcbridge.ban.BanHelper;
import com.pcb.pcbridge.ban.PlayerUUID;
import com.pcb.pcbridge.library.UUIDLookup;
import com.pcb.pcbridge.library.controllers.commands.CommandPacket;
import com.pcb.pcbridge.library.controllers.commands.ICommand;
import com.pcb.pcbridge.library.database.AbstractAdapter;

/**
 * Command: Ban the specified user (via username) from the server
 */

public final class CommandLookup implements ICommand 
{	
	private UUIDLookup _uuidLookup = new UUIDLookup();
	
	public boolean Execute(CommandPacket e, Object... args) 
	{
		if(e.Args.length > 1 || e.Args.length == 0)
			return false;
		
		String username = e.Args[0];
		UUID uuid;
		try 
		{
			uuid = _uuidLookup.Query(username);
			e.Sender.sendMessage(uuid.toString());
			
		} 
		catch (IOException | ParseException e1) 
		{
			e1.printStackTrace();
		}
		
		return true;
	}
	
}
