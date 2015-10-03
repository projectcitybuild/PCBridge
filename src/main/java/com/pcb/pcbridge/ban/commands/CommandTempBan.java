package com.pcb.pcbridge.ban.commands;

import com.pcb.pcbridge.library.controllers.commands.CommandPacket;
import com.pcb.pcbridge.library.controllers.commands.ICommand;
/**
 * Command: Ban the specified user (via username) for a temporary period of time
 */

public final class CommandTempBan implements ICommand 
{	
	public boolean Execute(CommandPacket e) 
	{
		if(e.Args.length == 0)
			return false;
		
		return true;
	}
	
}
