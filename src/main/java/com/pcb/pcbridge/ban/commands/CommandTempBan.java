package com.pcb.pcbridge.ban.commands;

import com.pcb.pcbridge.library.controllers.commands.CommandArgs;
import com.pcb.pcbridge.library.controllers.commands.ICommand;
/**
 * Command: Ban the specified user (via username) for a temporary period of time
 */

public final class CommandTempBan implements ICommand 
{	
	public boolean Execute(CommandArgs e, Object... args) 
	{
		if(e.Args.length == 0)
			return false;
		
		// the code is mostly the same, so route the command data to CommandBan instead
		return e.Plugin.GetControllerManager().InvokeRoute(e, "ban", true);
	}
	
}
