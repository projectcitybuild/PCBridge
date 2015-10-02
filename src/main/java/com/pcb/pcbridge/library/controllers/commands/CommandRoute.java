package com.pcb.pcbridge.library.controllers.commands;

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
