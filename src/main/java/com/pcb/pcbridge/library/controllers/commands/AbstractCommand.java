package com.pcb.pcbridge.library.controllers.commands;

import com.pcb.pcbridge.PCBridge;

/**
 * An interface for a command handler
 */

public abstract class AbstractCommand 
{
	/**
	 * The logic to run upon command invoke
	 * 
	 * @param e
	 * @return Boolean (valid command input)
	 */
	public abstract boolean Execute(CommandArgs e);
	
	/**
	 * The permission string (eg. 'pcbridge.ban.looukp') required to invoke the command
	 * 
	 * @return String
	 */
	//public String GetPermission();
	
	
	protected PCBridge _plugin;
	public void SetPlugin(PCBridge plugin)
	{
		this._plugin = plugin;
	}
}
