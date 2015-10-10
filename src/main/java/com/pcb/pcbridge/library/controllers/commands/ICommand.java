package com.pcb.pcbridge.library.controllers.commands;

/**
 * An interface for a command handler
 */

public interface ICommand 
{
	public boolean Execute(CommandArgs e, Object... args);
}
