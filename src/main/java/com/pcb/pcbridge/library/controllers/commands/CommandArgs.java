package com.pcb.pcbridge.library.controllers.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.pcb.pcbridge.PCBridge;

/**
 * A container for grouping a command's parameters when handling a command
 */

public class CommandArgs 
{
	public CommandSender Sender;
	public String Alias;
	public String[] Args;
	public boolean IsPlayer;
	public PCBridge Plugin;
	
	public CommandArgs(PCBridge plugin, CommandSender sender, String alias, String[] args)
	{
		this.Plugin		= plugin;
		this.Sender		= sender;
		this.Alias 		= alias;
		this.Args 		= args;
		this.IsPlayer 	= sender instanceof Player;
	}
}
