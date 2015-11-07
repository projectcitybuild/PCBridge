package com.pcb.pcbridge.bukkit.utility.commands;

import com.pcb.pcbridge.library.MessageHelper;
import com.pcb.pcbridge.library.MessageType;
import com.pcb.pcbridge.library.controllers.AbstractCommand;
import com.pcb.pcbridge.library.controllers.CommandArgs;
/**
 * Command: Ban the specified user (via username) for a temporary period of time
 */

public final class CommandPCBridge extends AbstractCommand 
{	
	public boolean Execute(CommandArgs e) 
	{
		if(e.Args.length == 0)
			return false;
		
		switch(e.Args[0].toLowerCase())
		{
			case "config":
				return SetConfig(e);
			case "reload":
				return Reload(e);
			default:
				return false;
		}
	}
	
	/**
	 * Updates the config for the given key and value
	 * 
	 * @param e
	 * @return
	 */
	public boolean SetConfig(CommandArgs e)
	{
		if(e.Args.length != 3)
			return false;
		
		String value = e.Args[2].toLowerCase();
		if(value.equals("on") == false && value.equals("off") == false)
			return false;
		
		boolean on = e.Args[2] == "on" ? true : false;
		switch(e.Args[1].toLowerCase())
		{
			case "db_test":
				_plugin.getConfig().set("database.boot_test_connection", on);
				MessageHelper.Send(MessageType.INFO, e.Sender, "'DB test on boot' set to " + e.Args[2].toUpperCase());
				_plugin.saveConfig();
				break;
			default:
				return false;
		}		
		
		return true;
	}
	
	/**
	 * Reloads the plugin
	 * 
	 * @param e
	 * @return
	 */
	public boolean Reload(CommandArgs e)
	{
		if(e.Args.length > 1)
			return false;
		
		_plugin.onDisable();
		_plugin.onEnable();
		
		MessageHelper.Send(MessageType.INFO, e.Sender, "PCBridge plugin reloaded");
		
		return true;
	}
}
