package com.pcb.pcbridge.swearblock.commands;

import com.pcb.pcbridge.library.controllers.commands.CommandArgs;
import com.pcb.pcbridge.library.controllers.commands.AbstractCommand;

/**
 * Command: Toggles on/off the swear filter per player
 */

public final class CommandSwearBlock extends AbstractCommand 
{	
	/**
	 * Determines if it's a permanent or temporary ban
	 */
	@Override
	public boolean Execute(CommandArgs e) 
	{
		if(e.Args.length == 0 || e.Args.length > 1)
			return false;
		
		switch(e.Args[1].toLowerCase())
		{
			case "on":
				break;
			case "off":
				break;
			default:
				return false;
		}
		
		return true;
	}
	
}
