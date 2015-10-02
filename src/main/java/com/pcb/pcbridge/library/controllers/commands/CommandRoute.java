package com.pcb.pcbridge.library.controllers.commands;

/**
 * A container for a single command (ie. a text command and the class that executes it)
 */

public class CommandRoute 
{
	public String Alias;
	public ICommand Command;
	
	public CommandRoute(String alias, ICommand command)
	{
		this.Alias = alias;
		this.Command = command;
	}
}
