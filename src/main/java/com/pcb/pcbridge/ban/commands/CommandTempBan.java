package com.pcb.pcbridge.ban.commands;

import java.sql.SQLException;
import java.util.Date;

import org.bukkit.ChatColor;

import com.pcb.pcbridge.ban.BanHelper;
import com.pcb.pcbridge.ban.PlayerUUID;
import com.pcb.pcbridge.library.controllers.commands.CommandPacket;
import com.pcb.pcbridge.library.controllers.commands.ICommand;
import com.pcb.pcbridge.library.database.AbstractAdapter;
/**
 * Command: Ban the specified user (via username) for a temporary period of time
 */

public final class CommandTempBan implements ICommand 
{	
	public boolean Execute(CommandPacket e, Object... args) 
	{
		if(e.Args.length == 0)
			return false;
		
		// the code is mostly the same, so route the command data to CommandBan instead
		return e.Plugin.GetControllerManager().InvokeRoute(e, "ban", true);
	}
	
}
