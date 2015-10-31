package com.pcb.pcbridge.library.controllers.commands;

/**
 * A command's definition used for initialisation at runtime 
 * (ie. a text command and the class that executes it)
 */

public class CommandRoute 
{
	public String Alias;
	public AbstractCommand Command;
	
	public CommandRoute(String alias, AbstractCommand command)
	{
		this.Alias = alias;
		this.Command = command;
	}
}
