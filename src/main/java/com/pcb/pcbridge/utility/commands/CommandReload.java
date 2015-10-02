package com.pcb.pcbridge.utility.commands;

import com.pcb.pcbridge.library.controllers.commands.CommandPacket;
import com.pcb.pcbridge.library.controllers.commands.ICommand;

public final class CommandReload implements ICommand 
{	
	// this class does nothing yet...
	
	public boolean Execute(CommandPacket e) 
	{
		if(e.Args.length > 0)
			return false;
		
		e.Plugin.reloadConfig();
		
		e.Sender.sendMessage("PCBridge config reloaded.");
		
		return true;
	}
}
