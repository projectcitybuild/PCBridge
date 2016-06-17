package com.pcb.pcbridge.bukkit.utility.commands;

import com.pcb.pcbridge.library.MessageHelper;
import com.pcb.pcbridge.library.MessageType;
import com.pcb.pcbridge.library.controllers.AbstractCommand;
import com.pcb.pcbridge.library.controllers.CommandArgs;


public final class CommandPCBridge extends AbstractCommand 
{		
	public boolean Execute(CommandArgs e) 
	{
		if(e.Args.length == 0)
			return false;

		switch(e.Args[0])
		{
			case "maintenance":
				return SetMaintenanceMode(e);
			default:
				return false;
		}
	}
	
	/**
	 * Puts the server into 'maintenance mode': only players with the [pcbridge.maintenance.access] permission node can enter
	 * 
	 * @param e
	 * @return
	 */
	private boolean SetMaintenanceMode(CommandArgs e)
	{		
		if(e.Args.length < 2)
			return false;
		
		String arg = e.Args[1].toLowerCase();
		if(!arg.equals("on") && !arg.equals("off"))
			return false;
		
		Boolean value = (arg == "on") ? true : false;
		
		_plugin.getConfig().set("settings.maintenance_mode", value);
		_plugin.saveConfig();
		
		MessageHelper.Send(MessageType.INFO, e.Sender, "Maintenance mode toggled " + arg.toUpperCase());
		
		return true;
	}	
		
}
