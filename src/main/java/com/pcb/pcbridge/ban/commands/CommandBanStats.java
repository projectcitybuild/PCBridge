package com.pcb.pcbridge.ban.commands;

import com.pcb.pcbridge.library.controllers.commands.CommandPacket;
import com.pcb.pcbridge.library.controllers.commands.ICommand;
/**
 * Command: Prints out a list of global and player specific ban stats
 */

public final class CommandBanStats implements ICommand 
{	
	public boolean Execute(CommandPacket e, Object... args) 
	{
		if(e.Args.length > 0)
			return false;
		
		return true;
	}
	
}
